import React, { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { profileApi, schemesApi } from '../api';
import { Link, useSearchParams } from 'react-router-dom';
import { 
  User, 
  Bookmark, 
  CheckCircle2, 
  Sparkles, 
  Save,
  Trash2,
  ArrowRight
} from 'lucide-react';

export default function ProfilePage() {
  const { user, profile, updateProfile } = useAuth();
  const [searchParams] = useSearchParams();

  const [activeTab, setActiveTab] = useState(searchParams.get('tab') === 'saved' ? 'SAVED' : 'PROFILE');
  const [savedSchemes, setSavedSchemes] = useState([]);
  const [loadingSaved, setLoadingSaved] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Form fields
  const [formData, setFormData] = useState({
    age: '',
    gender: 'Male',
    state: 'Telangana',
    district: '',
    occupation: '',
    annualIncome: '',
    educationLevel: '10th',
    category: 'General',
    disabilityStatus: false,
    disabilityType: '',
    disabilityPercentage: 0,
    landOwnership: false,
    landArea: '',
    employmentStatus: 'Employed',
    residenceType: 'Urban',
    maritalStatus: 'Single',
    familySize: 3,
    bplStatus: false,
    rationCardType: 'APL',
    minorityStatus: false,
    exServiceman: false
  });

  const loadSavedSchemes = useCallback(async () => {
    setLoadingSaved(true);
    try {
      const res = await schemesApi.getSaved();
      if (res.data?.success) {
        setSavedSchemes(res.data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingSaved(false);
    }
  }, []);

  useEffect(() => {
    if (profile) {
      const timer = setTimeout(() => {
        setFormData(prev => ({
          ...prev,
          ...profile,
          age: profile.age ?? '',
          annualIncome: profile.annualIncome ?? '',
          landArea: profile.landArea ?? ''
        }));
      }, 0);
      return () => clearTimeout(timer);
    }
  }, [profile]);

  useEffect(() => {
    if (activeTab === 'SAVED') {
      loadSavedSchemes();
    }
  }, [activeTab, loadSavedSchemes]);

  const handleUnsave = async (schemeId) => {
    try {
      await schemesApi.unsave(schemeId);
      setSavedSchemes(prev => prev.filter(s => s.id !== schemeId));
    } catch (e) {
      console.error(e);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSaveSuccess(false);

    try {
      const payload = {
        ...formData,
        age: formData.age ? Number(formData.age) : null,
        annualIncome: formData.annualIncome ? Number(formData.annualIncome) : null,
        landArea: formData.landArea ? Number(formData.landArea) : null,
        familySize: formData.familySize ? Number(formData.familySize) : null
      };
      const res = await profileApi.save(payload);
      if (res.data?.success) {
        updateProfile(res.data.data);
        setSaveSuccess(true);
        setTimeout(() => setSaveSuccess(false), 4000);
      }
    } catch (err) {
      console.error(err);
      alert('Failed to save profile. Please verify fields.');
    } finally {
      setSaving(false);
    }
  };

  const completeness = profile?.completenessPercentage ?? 0;

  return (
    <div className="container fade-in" style={{ padding: '40px 24px 80px', maxWidth: '1000px' }}>
      {/* Profile Header */}
      <div className="glass-card" style={{ padding: '32px', marginBottom: '32px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '18px' }}>
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '20px',
              background: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '1.8rem',
              fontWeight: 800,
              color: '#fff',
              boxShadow: '0 0 24px rgba(6, 182, 212, 0.35)'
            }}>
              {user?.fullName?.charAt(0) || 'C'}
            </div>
            <div>
              <h1 style={{ fontSize: '1.8rem', marginBottom: '4px', color: '#fff' }}>
                {user?.fullName || 'Citizen User'}
              </h1>
              <p style={{ color: 'var(--text-dim)', fontSize: '0.85rem' }}>
                {user?.email} • Verified Citizen Account
              </p>
            </div>
          </div>

          <Link to="/eligibility" className="btn btn-primary btn-sm">
            <Sparkles size={16} /> Evaluate All Schemes
          </Link>
        </div>

        {/* Completeness Bar */}
        <div style={{ marginTop: '24px', paddingTop: '20px', borderTop: '1px solid var(--border-subtle)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem', marginBottom: '8px' }}>
            <span style={{ fontWeight: 600 }}>Profile Completeness for Statutory Evaluation</span>
            <span style={{ fontWeight: 800, color: completeness >= 80 ? '#34d399' : '#fb923c' }}>
              {completeness}%
            </span>
          </div>
          <div style={{ height: '8px', background: 'rgba(255, 255, 255, 0.08)', borderRadius: '4px', overflow: 'hidden' }}>
            <div style={{
              height: '100%',
              width: `${completeness}%`,
              background: 'linear-gradient(90deg, #f97316 0%, #06b6d4 100%)',
              transition: 'width 0.4s ease'
            }} />
          </div>
          {completeness < 70 && (
            <p style={{ fontSize: '0.78rem', color: 'var(--text-dim)', marginTop: '6px' }}>
              Complete remaining criteria below (income, category, occupation) to unlock 100% eligibility matching precision.
            </p>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
        <button
          onClick={() => setActiveTab('PROFILE')}
          className={`btn ${activeTab === 'PROFILE' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <User size={16} /> Citizen Parameters
        </button>
        <button
          onClick={() => setActiveTab('SAVED')}
          className={`btn ${activeTab === 'SAVED' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Bookmark size={16} /> Saved Schemes ({savedSchemes.length})
        </button>
      </div>

      {/* Tab: Profile Form */}
      {activeTab === 'PROFILE' && (
        <form onSubmit={handleSaveProfile} className="glass-card" style={{ padding: '32px' }}>
          {saveSuccess && (
            <div style={{
              background: 'rgba(16, 185, 129, 0.15)',
              border: '1px solid rgba(16, 185, 129, 0.3)',
              color: '#34d399',
              padding: '12px 16px',
              borderRadius: '10px',
              marginBottom: '20px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              fontSize: '0.9rem'
            }}>
              <CheckCircle2 size={18} /> Profile parameters updated successfully!
            </div>
          )}

          <h3 style={{ fontSize: '1.25rem', marginBottom: '20px', color: '#fff' }}>
            Demographic & Socioeconomic Details
          </h3>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
            gap: '20px',
            marginBottom: '28px'
          }}>
            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Age (Years)</label>
              <input type="number" name="age" value={formData.age} onChange={handleChange} min="1" max="110" style={{ width: '100%' }} />
            </div>

            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Gender</label>
              <select name="gender" value={formData.gender} onChange={handleChange} style={{ width: '100%' }}>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>

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
              <input type="text" name="district" value={formData.district} onChange={handleChange} placeholder="e.g., Medak, Pune, Jaipur" style={{ width: '100%' }} />
            </div>

            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Occupation</label>
              <input type="text" name="occupation" value={formData.occupation} onChange={handleChange} placeholder="e.g., Farmer, Student, Artisan" style={{ width: '100%' }} />
            </div>

            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Annual Household Income (₹)</label>
              <input type="number" name="annualIncome" value={formData.annualIncome} onChange={handleChange} placeholder="e.g., 180000" style={{ width: '100%' }} />
            </div>

            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Social Category / Caste</label>
              <select name="category" value={formData.category} onChange={handleChange} style={{ width: '100%' }}>
                <option value="General">General</option>
                <option value="OBC">OBC (Other Backward Class)</option>
                <option value="SC">SC (Scheduled Caste)</option>
                <option value="ST">ST (Scheduled Tribe)</option>
                <option value="EWS">EWS (Economically Weaker Section)</option>
              </select>
            </div>

            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Education Level</label>
              <select name="educationLevel" value={formData.educationLevel} onChange={handleChange} style={{ width: '100%' }}>
                <option value="Primary">Primary</option>
                <option value="10th">10th Class (Secondary)</option>
                <option value="12th">12th Class (Higher Secondary)</option>
                <option value="Graduate">Graduate</option>
                <option value="PostGraduate">Post Graduate</option>
                <option value="None">None</option>
              </select>
            </div>

            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Employment Status</label>
              <select name="employmentStatus" value={formData.employmentStatus} onChange={handleChange} style={{ width: '100%' }}>
                <option value="Employed">Employed (Salaried)</option>
                <option value="SelfEmployed">Self Employed</option>
                <option value="Student">Student</option>
                <option value="Unemployed">Unemployed</option>
                <option value="Retired">Retired</option>
              </select>
            </div>

            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Residence Area</label>
              <select name="residenceType" value={formData.residenceType} onChange={handleChange} style={{ width: '100%' }}>
                <option value="Rural">Rural (Gram Panchayat)</option>
                <option value="Urban">Urban (Municipality/Corporation)</option>
                <option value="SemiUrban">Semi-Urban</option>
              </select>
            </div>

            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Ration Card Category</label>
              <select name="rationCardType" value={formData.rationCardType} onChange={handleChange} style={{ width: '100%' }}>
                <option value="PHH">PHH (Priority Household)</option>
                <option value="AAY">AAY (Antyodaya Anna)</option>
                <option value="BPL">BPL (Below Poverty Line)</option>
                <option value="APL">APL (Above Poverty Line)</option>
                <option value="None">None</option>
              </select>
            </div>

            <div>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Family Size</label>
              <input type="number" name="familySize" value={formData.familySize} onChange={handleChange} min="1" max="25" style={{ width: '100%' }} />
            </div>
          </div>

          {/* Land Ownership */}
          <div style={{
            background: 'rgba(255, 255, 255, 0.03)',
            borderRadius: '12px',
            padding: '18px',
            marginBottom: '28px',
            border: '1px solid var(--border-subtle)'
          }}>
            <h4 style={{ fontSize: '0.95rem', marginBottom: '14px', color: '#fff' }}>Landholding & Special Attributes</h4>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '0.85rem', cursor: 'pointer' }}>
                <input type="checkbox" name="landOwnership" checked={formData.landOwnership} onChange={handleChange} />
                <span>Owns Agricultural Land</span>
              </label>

              {formData.landOwnership && (
                <div>
                  <label style={{ fontSize: '0.75rem', color: 'var(--text-dim)', display: 'block', marginBottom: '2px' }}>Land Size (Acres)</label>
                  <input type="number" step="0.1" name="landArea" value={formData.landArea} onChange={handleChange} placeholder="e.g. 2.5" style={{ width: '100%' }} />
                </div>
              )}

              <label style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '0.85rem', cursor: 'pointer' }}>
                <input type="checkbox" name="bplStatus" checked={formData.bplStatus} onChange={handleChange} />
                <span>BPL Status (Below Poverty Line)</span>
              </label>

              <label style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '0.85rem', cursor: 'pointer' }}>
                <input type="checkbox" name="disabilityStatus" checked={formData.disabilityStatus} onChange={handleChange} />
                <span>Person with Disability (PwD)</span>
              </label>

              <label style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '0.85rem', cursor: 'pointer' }}>
                <input type="checkbox" name="minorityStatus" checked={formData.minorityStatus} onChange={handleChange} />
                <span>Religious/Linguistic Minority</span>
              </label>
            </div>
          </div>

          <button type="submit" disabled={saving} className="btn btn-primary btn-lg">
            <Save size={18} /> {saving ? 'Saving...' : 'Save Profile Changes'}
          </button>
        </form>
      )}

      {/* Tab: Saved Schemes */}
      {activeTab === 'SAVED' && (
        <div>
          {loadingSaved ? (
            <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
              Loading saved schemes...
            </div>
          ) : savedSchemes.length === 0 ? (
            <div className="glass-card" style={{ padding: '60px 24px', textAlign: 'center' }}>
              <Bookmark size={40} color="var(--primary)" style={{ marginBottom: '14px' }} />
              <h3 style={{ fontSize: '1.25rem', marginBottom: '8px' }}>No saved schemes yet</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', maxWidth: '400px', margin: '0 auto 20px' }}>
                Bookmark interesting schemes while browsing the catalog to track their deadlines and application documents.
              </p>
              <Link to="/schemes" className="btn btn-primary btn-sm">
                Explore Schemes
              </Link>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {savedSchemes.map((scheme) => (
                <div key={scheme.id} className="glass-card" style={{ padding: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
                  <div>
                    <div style={{ display: 'flex', gap: '8px', marginBottom: '6px' }}>
                      <span className="badge badge-primary">{scheme.benefitCategory}</span>
                      <span className="badge badge-secondary">{scheme.scope}</span>
                    </div>
                    <h3 style={{ fontSize: '1.2rem', color: '#fff', marginBottom: '4px' }}>
                      {scheme.name}
                    </h3>
                    <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', maxWidth: '650px' }}>
                      {scheme.description}
                    </p>
                  </div>

                  <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    <Link to={`/schemes/${scheme.id}`} className="btn btn-primary btn-sm">
                      View Details <ArrowRight size={14} />
                    </Link>
                    <button
                      onClick={() => handleUnsave(scheme.id)}
                      className="btn btn-secondary btn-sm"
                      style={{ color: '#ef4444' }}
                      title="Remove from saved"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
