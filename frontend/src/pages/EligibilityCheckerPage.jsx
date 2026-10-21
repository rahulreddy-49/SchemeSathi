import React, { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { schemesApi, demoApi, householdApi } from '../api';
import { Link, useSearchParams } from 'react-router-dom';
import { 
  CheckCircle2, 
  XCircle, 
  ArrowRight, 
  ShieldCheck, 
  Sparkles, 
  RefreshCw,
  ChevronDown,
  ChevronUp,
  AlertTriangle,
  Users
} from 'lucide-react';

export default function EligibilityCheckerPage() {
  const { user, profile } = useAuth();
  const [searchParams] = useSearchParams();

  // Citizen Profile Form State
  const [formData, setFormData] = useState({
    age: 30,
    gender: 'Female',
    state: 'Telangana',
    district: 'Hyderabad',
    occupation: 'Farmer',
    annualIncome: 180000,
    educationLevel: '10th',
    category: 'OBC',
    disabilityStatus: false,
    disabilityPercentage: 0,
    landOwnership: true,
    landArea: 2.0,
    employmentStatus: 'SelfEmployed',
    residenceType: 'Rural',
    bplStatus: false,
    rationCardType: 'PHH',
    familySize: 4
  });

  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState([]);
  const [expandedSchemeId, setExpandedSchemeId] = useState(null);
  const [activeTab, setActiveTab] = useState('ALL'); // ALL, ELIGIBLE, POTENTIAL
  const [errorMessage, setErrorMessage] = useState(null);
  const [hasSubmitted, setHasSubmitted] = useState(false);

  const [householdMembers, setHouseholdMembers] = useState([]);
  const [selectedMemberId, setSelectedMemberId] = useState(searchParams.get('memberId') || 'self');

  // Prepopulate if logged-in user profile exists and self is selected
  useEffect(() => {
    if (selectedMemberId === 'self' && profile && profile.age) {
      const timer = setTimeout(() => {
        setFormData(prev => ({
          ...prev,
          ...profile
        }));
      }, 0);
      return () => clearTimeout(timer);
    }
  }, [profile, selectedMemberId]);

  // Load household members if user is authenticated
  useEffect(() => {
    if (user) {
      householdApi.getAll()
        .then(res => {
          if (res.data?.success) {
            const members = res.data.data || [];
            setHouseholdMembers(members);

            const paramId = searchParams.get('memberId');
            if (paramId) {
              const target = members.find(m => String(m.id) === String(paramId));
              if (target) {
                setSelectedMemberId(String(target.id));
                setFormData({
                  age: target.age ?? '',
                  gender: target.gender || 'Female',
                  state: target.state || 'Telangana',
                  district: target.district || '',
                  occupation: target.occupation || '',
                  annualIncome: target.annualIncome ?? '',
                  educationLevel: target.educationLevel || '10th',
                  category: target.category || 'General',
                  disabilityStatus: Boolean(target.disabilityStatus),
                  disabilityPercentage: target.disabilityPercentage || 0,
                  landOwnership: Boolean(target.landOwnership),
                  landArea: target.landArea ?? '',
                  employmentStatus: target.employmentStatus || 'Employed',
                  residenceType: target.residenceType || 'Rural',
                  bplStatus: Boolean(target.bplStatus),
                  rationCardType: target.rationCardType || 'APL',
                  familySize: target.familySize || 4
                });
              }
            }
          }
        })
        .catch(err => console.error('Failed to load household members:', err));
    }
  }, [user, searchParams]);

  const handleMemberSelect = (targetId) => {
    setSelectedMemberId(targetId);
    if (targetId === 'self') {
      if (profile) {
        setFormData(prev => ({
          ...prev,
          ...profile
        }));
      }
    } else {
      const target = householdMembers.find(m => String(m.id) === String(targetId));
      if (target) {
        setFormData({
          age: target.age ?? '',
          gender: target.gender || 'Female',
          state: target.state || 'Telangana',
          district: target.district || '',
          occupation: target.occupation || '',
          annualIncome: target.annualIncome ?? '',
          educationLevel: target.educationLevel || '10th',
          category: target.category || 'General',
          disabilityStatus: Boolean(target.disabilityStatus),
          disabilityPercentage: target.disabilityPercentage || 0,
          landOwnership: Boolean(target.landOwnership),
          landArea: target.landArea ?? '',
          employmentStatus: target.employmentStatus || 'Employed',
          residenceType: target.residenceType || 'Rural',
          bplStatus: Boolean(target.bplStatus),
          rationCardType: target.rationCardType || 'APL',
          familySize: target.familySize || 4
        });
      }
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : (type === 'number' ? (value === '' ? '' : Number(value)) : value)
    }));
  };

  const handleRunAssessment = async (e) => {
    if (e) e.preventDefault();
    if (loading) return; // Prevent duplicate concurrent submissions

    setLoading(true);
    setErrorMessage(null);

    // Format clean payload with numeric conversions
    const payload = {
      ...formData,
      age: formData.age !== '' && formData.age !== null ? Number(formData.age) : null,
      annualIncome: formData.annualIncome !== '' && formData.annualIncome !== null ? Number(formData.annualIncome) : null,
      landArea: formData.landOwnership && formData.landArea !== '' && formData.landArea !== null ? Number(formData.landArea) : 0,
      familySize: formData.familySize !== '' && formData.familySize !== null ? Number(formData.familySize) : null,
      disabilityPercentage: formData.disabilityStatus && formData.disabilityPercentage !== '' && formData.disabilityPercentage !== null ? Number(formData.disabilityPercentage) : 0
    };

    try {
      const res = user
        ? await schemesApi.evaluateAllCriteria(payload)
        : await demoApi.checkAllEligibility(payload);

      if (res.data?.success) {
        setResults(res.data.data || []);
        setHasSubmitted(true);
        setErrorMessage(null);
      } else {
        setResults([]);
        setErrorMessage(res.data?.message || 'Assessment could not be completed.');
      }
    } catch (err) {
      console.error('Eligibility evaluation error:', err);
      setResults([]);
      const msg = err.response?.data?.message || 'Error evaluating schemes. Please ensure all values are valid.';
      setErrorMessage(msg);
    } finally {
      setLoading(false);
    }
  };

  const filteredResults = results.filter(r => {
    if (activeTab === 'ELIGIBLE') return r.overallStatus === 'ELIGIBLE';
    if (activeTab === 'POTENTIAL') return r.overallStatus === 'PROBABLY_ELIGIBLE';
    return true;
  });

  return (
    <div className="container fade-in" style={{ padding: '40px 24px 80px' }}>
      {/* Header */}
      <div style={{ textAlign: 'center', maxWidth: '750px', margin: '0 auto 40px' }}>
        <span className="badge badge-success">Deterministic Verification Engine</span>
        <h1 style={{ fontSize: '2.5rem', marginTop: '10px', marginBottom: '12px' }}>
          Interactive Eligibility Assessment
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem', lineHeight: 1.6 }}>
          Enter your demographic, socioeconomic, and occupation parameters below to evaluate your eligibility across all Indian welfare schemes in real time.
        </p>
      </div>

      {/* Grid: Form on Left, Results on Right */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 1.4fr)', gap: '36px' }} className="eligibility-grid">
        <style>{`
          @media (max-width: 960px) {
            .eligibility-grid { grid-template-columns: 1fr !important; }
          }
        `}</style>

        {/* PROFILE INPUT FORM */}
        <div className="glass-card" style={{ padding: '28px', height: 'fit-content' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h2 style={{ fontSize: '1.3rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ShieldCheck size={20} color="var(--primary)" />
              Citizen Criteria
            </h2>
            <button
              type="button"
              onClick={() => {
                setFormData({
                  age: 35,
                  gender: 'Female',
                  state: 'Telangana',
                  district: 'Medak',
                  occupation: 'Farmer',
                  annualIncome: 180000,
                  educationLevel: '10th',
                  category: 'OBC',
                  disabilityStatus: false,
                  disabilityPercentage: 0,
                  landOwnership: true,
                  landArea: 2.5,
                  employmentStatus: 'SelfEmployed',
                  residenceType: 'Rural',
                  bplStatus: false,
                  rationCardType: 'PHH',
                  familySize: 5
                });
              }}
              style={{ background: 'transparent', color: '#06b6d4', fontSize: '0.8rem', cursor: 'pointer', textDecoration: 'underline' }}
            >
              Fill Sample Data
            </button>
          </div>

          <form onSubmit={handleRunAssessment} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {/* Household Member Selector for Authenticated Citizens */}
            {user && (
              <div style={{
                background: 'rgba(6, 182, 212, 0.08)',
                border: '1px solid rgba(6, 182, 212, 0.25)',
                padding: '12px 14px',
                borderRadius: '8px',
                display: 'flex',
                flexDirection: 'column',
                gap: '6px'
              }}>
                <label style={{ fontSize: '0.82rem', fontWeight: 600, color: '#38bdf8', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Users size={16} /> Evaluating Eligibility For:
                </label>
                <select
                  value={selectedMemberId}
                  onChange={(e) => handleMemberSelect(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', background: 'var(--bg-card)', color: '#fff', borderRadius: '6px', border: '1px solid var(--border-subtle)' }}
                >
                  <option value="self">Myself ({user.fullName} - Account Holder)</option>
                  {householdMembers.map(m => (
                    <option key={m.id} value={m.id}>
                      {m.fullName} ({m.relationship}{m.age ? `, ${m.age} yrs` : ''})
                    </option>
                  ))}
                </select>
                {selectedMemberId !== 'self' && (
                  <span style={{ fontSize: '0.74rem', color: 'var(--text-dim)' }}>
                    Evaluating schemes using {householdMembers.find(m => String(m.id) === String(selectedMemberId))?.fullName || 'member'}'s demographic parameters.
                  </span>
                )}
              </div>
            )}

            {/* Age & Gender */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Age (Years)</label>
                <input type="number" name="age" value={formData.age} onChange={handleChange} min="1" max="110" style={{ width: '100%' }} />
              </div>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Gender</label>
                <select name="gender" value={formData.gender} onChange={handleChange} style={{ width: '100%' }}>
                  <option value="Female">Female</option>
                  <option value="Male">Male</option>
                  <option value="Other">Other</option>
                </select>
              </div>
            </div>

            {/* State & District */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>State</label>
                <select name="state" value={formData.state} onChange={handleChange} style={{ width: '100%' }}>
                  <option value="Telangana">Telangana</option>
                  <option value="Andhra Pradesh">Andhra Pradesh</option>
                  <option value="Uttar Pradesh">Uttar Pradesh</option>
                  <option value="Maharashtra">Maharashtra</option>
                  <option value="Bihar">Bihar</option>
                  <option value="Rajasthan">Rajasthan</option>
                  <option value="Madhya Pradesh">Madhya Pradesh</option>
                  <option value="Karnataka">Karnataka</option>
                  <option value="Tamil Nadu">Tamil Nadu</option>
                </select>
              </div>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>District</label>
                <input type="text" name="district" value={formData.district} onChange={handleChange} style={{ width: '100%' }} />
              </div>
            </div>

            {/* Occupation & Employment */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Occupation</label>
                <select name="occupation" value={formData.occupation} onChange={handleChange} style={{ width: '100%' }}>
                  <option value="Farmer">Farmer / Agriculture</option>
                  <option value="Student">Student</option>
                  <option value="Daily Wage Worker">Daily Wage Worker</option>
                  <option value="Artisan">Artisan / Handloom</option>
                  <option value="Self Employed">Self Employed</option>
                  <option value="IT Professional">Salaried Employee</option>
                  <option value="Unemployed">Unemployed</option>
                </select>
              </div>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Employment Status</label>
                <select name="employmentStatus" value={formData.employmentStatus} onChange={handleChange} style={{ width: '100%' }}>
                  <option value="SelfEmployed">Self Employed</option>
                  <option value="Employed">Employed</option>
                  <option value="Student">Student</option>
                  <option value="Unemployed">Unemployed</option>
                  <option value="Retired">Retired</option>
                </select>
              </div>
            </div>

            {/* Annual Income & Social Category */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Annual Income (₹)</label>
                <input type="number" name="annualIncome" value={formData.annualIncome} onChange={handleChange} style={{ width: '100%' }} />
              </div>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Social Category</label>
                <select name="category" value={formData.category} onChange={handleChange} style={{ width: '100%' }}>
                  <option value="OBC">OBC</option>
                  <option value="SC">SC</option>
                  <option value="ST">ST</option>
                  <option value="General">General</option>
                  <option value="EWS">EWS</option>
                </select>
              </div>
            </div>

            {/* Land Ownership & Land Area */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Land Ownership</label>
                <select name="landOwnership" value={formData.landOwnership ? 'true' : 'false'} onChange={(e) => setFormData(p => ({ ...p, landOwnership: e.target.value === 'true' }))} style={{ width: '100%' }}>
                  <option value="true">Owns Agricultural Land</option>
                  <option value="false">No Land (Landless)</option>
                </select>
              </div>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Land Area (Acres)</label>
                <input type="number" step="0.1" name="landArea" value={formData.landArea} onChange={handleChange} disabled={!formData.landOwnership} style={{ width: '100%' }} />
              </div>
            </div>

            {/* Residence & BPL Status */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Residence Area</label>
                <select name="residenceType" value={formData.residenceType} onChange={handleChange} style={{ width: '100%' }}>
                  <option value="Rural">Rural (Village / Panchayat)</option>
                  <option value="Urban">Urban (City / Municipality)</option>
                  <option value="SemiUrban">Semi-Urban</option>
                </select>
              </div>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Ration Card Type</label>
                <select name="rationCardType" value={formData.rationCardType} onChange={handleChange} style={{ width: '100%' }}>
                  <option value="PHH">PHH (Priority Household)</option>
                  <option value="AAY">AAY (Antyodaya Anna)</option>
                  <option value="BPL">BPL Card</option>
                  <option value="APL">APL / White Card</option>
                  <option value="None">None</option>
                </select>
              </div>
            </div>

            {errorMessage && (
              <div style={{
                background: 'rgba(239, 68, 68, 0.1)',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                color: '#f87171',
                padding: '12px 14px',
                borderRadius: '8px',
                fontSize: '0.85rem',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <AlertTriangle size={16} style={{ flexShrink: 0 }} />
                <span>{errorMessage}</span>
              </div>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary"
              style={{ width: '100%', marginTop: '8px', padding: '14px' }}
            >
              {loading ? (
                <>
                  <RefreshCw size={18} className="spin" /> Evaluating Schemes...
                </>
              ) : (
                <>
                  <CheckCircle2 size={18} /> Evaluate My Eligibility (All Schemes)
                </>
              )}
            </button>
          </form>
        </div>

        {/* RESULTS SECTION */}
        <div>
          {loading ? (
            <div className="glass-card" style={{ padding: '60px 24px', textAlign: 'center' }}>
              <RefreshCw size={48} color="var(--primary)" className="spin" style={{ marginBottom: '16px' }} />
              <h3 style={{ fontSize: '1.4rem', marginBottom: '10px' }}>Analyzing Welfare Schemes</h3>
              <p style={{ color: 'var(--text-muted)', maxWidth: '420px', margin: '0 auto', fontSize: '0.92rem' }}>
                Evaluating your criteria against codified statutory rules in real time...
              </p>
            </div>
          ) : results.length === 0 ? (
            <div className="glass-card" style={{ padding: '60px 24px', textAlign: 'center' }}>
              <Sparkles size={48} color="var(--primary)" style={{ marginBottom: '16px' }} />
              <h3 style={{ fontSize: '1.4rem', marginBottom: '10px' }}>
                {hasSubmitted ? 'No Matching Schemes Found' : 'Ready for Instant Assessment'}
              </h3>
              <p style={{ color: 'var(--text-muted)', maxWidth: '420px', margin: '0 auto 24px', fontSize: '0.92rem' }}>
                {hasSubmitted
                  ? 'No schemes match the criteria provided. Review your entries or adjust parameters.'
                  : 'Click "Evaluate My Eligibility" to see which of India\'s flagship central and state schemes you qualify for.'}
              </p>
              {!hasSubmitted && (
                <button onClick={handleRunAssessment} className="btn btn-secondary btn-sm">
                  Run Assessment Now
                </button>
              )}
            </div>
          ) : (
            <div>
              {/* Results Tabs & Summary */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
                <div>
                  <h3 style={{ fontSize: '1.3rem' }}>
                    Assessment Results ({results.length} Schemes Analyzed)
                  </h3>
                </div>
                <div style={{ display: 'flex', gap: '6px' }}>
                  <button
                    onClick={() => setActiveTab('ALL')}
                    className={`btn btn-sm ${activeTab === 'ALL' ? 'btn-primary' : 'btn-secondary'}`}
                  >
                    All ({results.length})
                  </button>
                  <button
                    onClick={() => setActiveTab('ELIGIBLE')}
                    className={`btn btn-sm ${activeTab === 'ELIGIBLE' ? 'btn-primary' : 'btn-secondary'}`}
                  >
                    Eligible ({results.filter(r => r.overallStatus === 'ELIGIBLE').length})
                  </button>
                  <button
                    onClick={() => setActiveTab('POTENTIAL')}
                    className={`btn btn-sm ${activeTab === 'POTENTIAL' ? 'btn-primary' : 'btn-secondary'}`}
                  >
                    Potential ({results.filter(r => r.overallStatus === 'PROBABLY_ELIGIBLE').length})
                  </button>
                </div>
              </div>

              {/* Scheme Results Cards */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {filteredResults.map((item) => {
                  const isExpanded = expandedSchemeId === item.schemeId;
                  const isEligible = item.overallStatus === 'ELIGIBLE';
                  const isProbable = item.overallStatus === 'PROBABLY_ELIGIBLE';

                  return (
                    <div
                      key={item.schemeId}
                      className="glass-card"
                      style={{
                        padding: '20px',
                        borderLeft: `4px solid ${isEligible ? '#10b981' : isProbable ? '#06b6d4' : '#ef4444'}`
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '16px' }}>
                        <div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                            <span className={`badge ${isEligible ? 'badge-success' : isProbable ? 'badge-secondary' : 'badge-warning'}`}>
                              {item.overallStatus}
                            </span>
                            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#fff' }}>
                              {item.matchScore}% Match
                            </span>
                          </div>
                          <h4 style={{ fontSize: '1.15rem', color: '#fff', marginBottom: '6px' }}>
                            <Link to={`/schemes/${item.schemeId}`} style={{ color: 'inherit' }}>
                              {item.schemeName}
                            </Link>
                          </h4>
                          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.5 }}>
                            {item.explanation}
                          </p>
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', alignItems: 'flex-end', flexShrink: 0 }}>
                          <Link to={`/schemes/${item.schemeId}`} className="btn btn-primary btn-sm">
                            View <ArrowRight size={14} />
                          </Link>
                          <button
                            onClick={() => setExpandedSchemeId(isExpanded ? null : item.schemeId)}
                            style={{
                              background: 'transparent',
                              color: 'var(--text-dim)',
                              fontSize: '0.75rem',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '4px',
                              cursor: 'pointer'
                            }}
                          >
                            {isExpanded ? <>Hide Rules <ChevronUp size={14} /></> : <>Criteria ({item.criteriaResults?.length || 0}) <ChevronDown size={14} /></>}
                          </button>
                        </div>
                      </div>

                      {/* Expandable Criteria breakdown */}
                      {isExpanded && (
                        <div style={{
                          marginTop: '16px',
                          paddingTop: '16px',
                          borderTop: '1px solid var(--border-subtle)',
                          display: 'flex',
                          flexDirection: 'column',
                          gap: '10px'
                        }}>
                          <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-dim)' }}>
                            Rule Verification Breakdown:
                          </div>

                          {item.criteriaResults?.map((rule, idx) => (
                            <div
                              key={idx}
                              style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                background: 'rgba(255, 255, 255, 0.02)',
                                padding: '8px 12px',
                                borderRadius: '8px',
                                fontSize: '0.8rem'
                              }}
                            >
                              <div>
                                <span style={{ fontWeight: 600, color: '#fff' }}>{rule.displayLabel || rule.criterionName}</span>
                                <div style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>
                                  Required: {rule.requiredValue} | Provided: {rule.citizenValue}
                                </div>
                              </div>
                              {rule.status === 'PASS' ? (
                                <span style={{ color: '#34d399', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px' }}>
                                  <CheckCircle2 size={14} /> Pass
                                </span>
                              ) : rule.status === 'FAIL' ? (
                                <span style={{ color: '#f87171', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px' }}>
                                  <XCircle size={14} /> Fail
                                </span>
                              ) : rule.status === 'NEEDS_VERIFICATION' ? (
                                <span style={{ color: '#38bdf8', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px' }}>
                                  <ShieldCheck size={14} /> Verification Req.
                                </span>
                              ) : (
                                <span style={{ color: '#fbbf24', fontWeight: 600 }}>Missing</span>
                              )}
                            </div>
                          ))}

                          {item.requiredDocuments?.length > 0 && (
                            <div style={{ marginTop: '6px' }}>
                              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', fontWeight: 600 }}>Documents Required:</span>
                              <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginTop: '4px' }}>
                                {item.requiredDocuments.map((doc, dIdx) => (
                                  <span key={dIdx} style={{ fontSize: '0.72rem', background: 'rgba(6, 182, 212, 0.1)', color: '#38bdf8', padding: '2px 8px', borderRadius: '4px' }}>
                                    {doc}
                                  </span>
                                ))}
                              </div>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
