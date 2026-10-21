import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { schemesApi, demoApi, applicationsApi, householdApi } from '../api';
import { useAuth } from '../AuthContext';
import { 
  Building, 
  ExternalLink, 
  CheckCircle2, 
  XCircle, 
  AlertTriangle, 
  FileText, 
  ArrowLeft, 
  Bookmark, 
  ShieldCheck, 
  ListOrdered,
  Clock,
  X,
  CheckCircle,
  AlertCircle
} from 'lucide-react';

export default function SchemeDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();

  const [scheme, setScheme] = useState(null);
  const [loading, setLoading] = useState(true);
  const [eligibilityResult, setEligibilityResult] = useState(null);
  const [checkingEligibility, setCheckingEligibility] = useState(false);
  const [saved, setSaved] = useState(false);

  // Application Tracking State
  const [trackedApplications, setTrackedApplications] = useState([]);
  const [showTrackModal, setShowTrackModal] = useState(false);
  const [householdMembers, setHouseholdMembers] = useState([]);
  const [trackForm, setTrackForm] = useState({
    householdMemberId: '',
    status: 'PLANNING',
    applicationReference: '',
    appliedDate: '',
    nextActionDate: '',
    nextActionNotes: '',
    notes: ''
  });
  const [trackingSaving, setTrackingSaving] = useState(false);
  const [trackingError, setTrackingError] = useState('');
  const [trackingSuccess, setTrackingSuccess] = useState(false);

  useEffect(() => {
    let isMounted = true;
    setLoading(true);

    schemesApi.getById(id)
      .then(res => {
        if (isMounted && res.data?.success) {
          setScheme(res.data.data);
          setSaved(res.data.data.saved);
        }
      })
      .catch(() => {
        // Fallback to demo API
        demoApi.getScheme(id)
          .then(demoRes => {
            if (isMounted && demoRes.data?.success) {
              setScheme(demoRes.data.data);
            }
          })
          .catch(err => console.error(err));
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [id]);

  const checkMyEligibility = async () => {
    if (!user) {
      alert('Please log in with your profile, or try our Demo Persona Sandbox to test eligibility!');
      return;
    }
    setCheckingEligibility(true);
    try {
      const res = await schemesApi.checkEligibility(id);
      if (res.data?.success) {
        setEligibilityResult(res.data.data);
      }
    } catch (e) {
      alert(e.response?.data?.message || 'Could not evaluate eligibility. Make sure your profile is completed.');
    } finally {
      setCheckingEligibility(false);
    }
  };

  useEffect(() => {
    if (user && id) {
      applicationsApi.getByScheme(id)
        .then(res => {
          if (res.data?.success) {
            setTrackedApplications(res.data.data || []);
          }
        })
        .catch(err => console.error(err));
    }
  }, [user, id]);

  const handleOpenTrackModal = async () => {
    if (!user) {
      alert('Please log in to track government scheme applications.');
      return;
    }
    try {
      const res = await householdApi.getAll();
      if (res.data?.success) {
        setHouseholdMembers(res.data.data || []);
      }
    } catch (err) {
      console.error(err);
    }
    setTrackingError('');
    setTrackingSuccess(false);
    setShowTrackModal(true);
  };

  const handleSaveTrackApplication = async (e) => {
    e.preventDefault();
    setTrackingSaving(true);
    setTrackingError('');

    try {
      const payload = {
        schemeId: Number(id),
        householdMemberId: trackForm.householdMemberId ? Number(trackForm.householdMemberId) : null,
        status: trackForm.status,
        applicationReference: trackForm.applicationReference ? trackForm.applicationReference.trim() : null,
        appliedDate: trackForm.appliedDate || null,
        nextActionDate: trackForm.nextActionDate || null,
        nextActionNotes: trackForm.nextActionNotes ? trackForm.nextActionNotes.trim() : null,
        notes: trackForm.notes ? trackForm.notes.trim() : null
      };

      const res = await applicationsApi.create(payload);
      if (res.data?.success) {
        setTrackedApplications(prev => [res.data.data, ...prev]);
        setTrackingSuccess(true);
        setTimeout(() => {
          setShowTrackModal(false);
          setTrackingSuccess(false);
        }, 1500);
      }
    } catch (err) {
      console.error(err);
      setTrackingError(err.response?.data?.message || 'Failed to start tracking application.');
    } finally {
      setTrackingSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '80px 24px', textAlign: 'center', color: 'var(--text-muted)' }}>
        Loading scheme documentation...
      </div>
    );
  }

  if (!scheme) {
    return (
      <div className="container" style={{ padding: '80px 24px', textAlign: 'center' }}>
        <h2>Scheme not found</h2>
        <Link to="/schemes" className="btn btn-primary" style={{ marginTop: '16px' }}>
          Back to Directory
        </Link>
      </div>
    );
  }

  return (
    <div className="container fade-in" style={{ padding: '30px 24px 80px' }}>
      {/* Back button */}
      <Link to="/schemes" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '20px' }}>
        <ArrowLeft size={16} /> Back to Directory
      </Link>

      {/* Scheme Header Banner */}
      <div className="glass-card" style={{ padding: '32px', marginBottom: '32px', position: 'relative' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px', marginBottom: '16px' }}>
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <span className="badge badge-primary">{scheme.benefitCategory || 'Central Scheme'}</span>
            <span className="badge badge-secondary">{scheme.scope === 'CENTRAL' ? 'All India' : scheme.state}</span>
            <span className="badge badge-success">Official Portal Verified</span>
          </div>

          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <button
              onClick={handleToggleSave}
              className="btn btn-secondary btn-sm"
              style={{ display: 'flex', alignItems: 'center', gap: '6px', color: saved ? 'var(--primary)' : 'inherit' }}
            >
              <Bookmark size={16} fill={saved ? 'currentColor' : 'none'} />
              {saved ? 'Saved in Profile' : 'Save Scheme'}
            </button>

            {trackedApplications.length > 0 ? (
              <Link
                to="/profile?tab=applications"
                className="btn btn-secondary btn-sm"
                style={{ display: 'flex', alignItems: 'center', gap: '6px', borderColor: '#38bdf8', color: '#38bdf8' }}
              >
                <Clock size={16} /> Tracked: {trackedApplications[0].statusDisplayName || trackedApplications[0].status}
              </Link>
            ) : (
              <button
                type="button"
                onClick={handleOpenTrackModal}
                className="btn btn-secondary btn-sm"
                style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
              >
                <Clock size={16} /> Track Application
              </button>
            )}

            {scheme.applicationUrl && (
              <a
                href={scheme.applicationUrl}
                target="_blank"
                rel="noreferrer"
                className="btn btn-primary btn-sm"
              >
                Apply on Official Portal <ExternalLink size={15} />
              </a>
            )}
          </div>
        </div>

        <h1 style={{ fontSize: 'clamp(1.8rem, 3.5vw, 2.4rem)', marginBottom: '12px', color: '#fff' }}>
          {scheme.name}
        </h1>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-dim)', fontSize: '0.9rem', marginBottom: '20px' }}>
          <Building size={16} />
          <span>{scheme.governmentAuthority || 'Government of India'}</span>
          {scheme.verificationDate && (
            <span style={{ marginLeft: '12px' }}>• Verified: {scheme.verificationDate}</span>
          )}
        </div>

        <p style={{ fontSize: '1.05rem', color: 'var(--text-main)', lineHeight: 1.6, maxWidth: '950px' }}>
          {scheme.description}
        </p>
      </div>

      {/* Main Content Layout */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 2fr) minmax(0, 1fr)', gap: '32px' }} className="detail-grid">
        <style>{`
          @media (max-width: 900px) {
            .detail-grid { grid-template-columns: 1fr !important; }
          }
        `}</style>

        {/* Left Column: Scheme Details */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
          {/* Objective & Benefits */}
          <div className="glass-card" style={{ padding: '28px' }}>
            <h2 style={{ fontSize: '1.3rem', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ShieldCheck size={20} color="var(--primary)" />
              Objectives & Entitlements
            </h2>
            <div style={{ marginBottom: '16px' }}>
              <h4 style={{ fontSize: '0.95rem', color: 'var(--text-muted)', marginBottom: '6px' }}>Objective</h4>
              <p style={{ fontSize: '0.95rem', lineHeight: 1.6 }}>{scheme.objective || scheme.description}</p>
            </div>
            <div>
              <h4 style={{ fontSize: '0.95rem', color: '#38bdf8', marginBottom: '6px' }}>Benefits Breakdown</h4>
              <div style={{
                background: 'rgba(6, 182, 212, 0.08)',
                border: '1px solid rgba(6, 182, 212, 0.2)',
                borderRadius: '12px',
                padding: '16px',
                fontSize: '0.95rem',
                lineHeight: 1.6
              }}>
                {scheme.benefits}
              </div>
            </div>
          </div>

          {/* Eligibility Rules (Deterministic Breakdown) */}
          <div className="glass-card" style={{ padding: '28px' }}>
            <h2 style={{ fontSize: '1.3rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CheckCircle2 size={20} color="#10b981" />
              Verifiable Eligibility Criteria ({scheme.eligibilityRules?.length || 0})
            </h2>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginBottom: '18px' }}>
              All criteria below are evaluated deterministically against citizen records.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {scheme.eligibilityRules?.map((rule, idx) => (
                <div
                  key={idx}
                  style={{
                    background: 'rgba(255, 255, 255, 0.03)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: '12px',
                    padding: '16px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    gap: '16px'
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                      <span style={{ fontWeight: 700, fontSize: '0.95rem', color: '#fff' }}>
                        {rule.displayLabel || rule.criterionField}
                      </span>
                      {rule.mandatory ? (
                        <span style={{ fontSize: '0.68rem', background: 'rgba(239, 68, 68, 0.15)', color: '#f87171', padding: '1px 6px', borderRadius: '4px', fontWeight: 600 }}>
                          Mandatory
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.68rem', background: 'rgba(255, 255, 255, 0.1)', color: 'var(--text-dim)', padding: '1px 6px', borderRadius: '4px' }}>
                          Optional
                        </span>
                      )}
                    </div>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                      {rule.description || `Must satisfy ${rule.operator} condition`}
                    </p>
                  </div>
                  <div style={{
                    background: 'rgba(249, 115, 22, 0.1)',
                    color: '#fb923c',
                    border: '1px solid rgba(249, 115, 22, 0.25)',
                    padding: '6px 14px',
                    borderRadius: '8px',
                    fontSize: '0.85rem',
                    fontWeight: 700,
                    whiteSpace: 'nowrap'
                  }}>
                    {rule.value}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Required Documents */}
          <div className="glass-card" style={{ padding: '28px' }}>
            <h2 style={{ fontSize: '1.3rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <FileText size={20} color="#06b6d4" />
              Document Preparation Checklist ({scheme.requiredDocuments?.length || 0})
            </h2>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '14px' }}>
              {scheme.requiredDocuments?.map((doc, idx) => (
                <div
                  key={idx}
                  style={{
                    background: 'rgba(255, 255, 255, 0.03)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: '12px',
                    padding: '16px'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                    <FileText size={16} color="var(--secondary)" />
                    <span style={{ fontWeight: 700, fontSize: '0.9rem', color: '#fff' }}>
                      {doc.documentName}
                    </span>
                  </div>
                  {doc.description && (
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)', marginBottom: '8px' }}>
                      {doc.description}
                    </p>
                  )}
                  {doc.alternatives && (
                    <div style={{ fontSize: '0.75rem', color: '#38bdf8' }}>
                      <strong>Alternatives:</strong> {doc.alternatives}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Application Procedure */}
          {scheme.applicationProcess && (
            <div className="glass-card" style={{ padding: '28px' }}>
              <h2 style={{ fontSize: '1.3rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <ListOrdered size={20} color="var(--primary)" />
                Step-by-Step Application Procedure
              </h2>
              <div style={{ whiteSpace: 'pre-line', fontSize: '0.95rem', lineHeight: 1.8, color: 'var(--text-main)' }}>
                {scheme.applicationProcess}
              </div>
            </div>
          )}

          {/* Important Warnings & Common Rejection Reasons */}
          {(scheme.importantWarnings || scheme.commonRejectionReasons) && (
            <div className="glass-card" style={{ padding: '28px', borderLeft: '4px solid #f59e0b' }}>
              <h2 style={{ fontSize: '1.25rem', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px', color: '#fbbf24' }}>
                <AlertTriangle size={20} />
                Important Cautions & Common Rejection Reasons
              </h2>
              {scheme.importantWarnings && (
                <div style={{ marginBottom: '14px', fontSize: '0.9rem', lineHeight: 1.6 }}>
                  <strong>Key Warnings:</strong> {scheme.importantWarnings}
                </div>
              )}
              {scheme.commonRejectionReasons && (
                <div style={{ fontSize: '0.9rem', lineHeight: 1.6, color: 'var(--text-muted)' }}>
                  <strong>Avoid Common Mistakes:</strong> {scheme.commonRejectionReasons}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Right Column: Live Eligibility Checker Widget */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          {/* Eligibility Checker Box */}
          <div className="glass-card" style={{ padding: '24px', position: 'sticky', top: '90px' }}>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ShieldCheck size={18} color="var(--primary)" />
              Live Eligibility Test
            </h3>
            <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '16px' }}>
              Test your profile directly against this scheme's rules.
            </p>

            <button
              onClick={checkMyEligibility}
              disabled={checkingEligibility}
              className="btn btn-primary"
              style={{ width: '100%', marginBottom: '16px' }}
            >
              {checkingEligibility ? 'Evaluating Criteria...' : 'Evaluate My Profile'}
            </button>

            {!user && (
              <div style={{
                background: 'rgba(249, 115, 22, 0.08)',
                border: '1px solid rgba(249, 115, 22, 0.2)',
                borderRadius: '8px',
                padding: '10px 12px',
                fontSize: '0.78rem',
                color: '#fb923c',
                marginBottom: '16px'
              }}>
                Want instant testing without logging in? Try our <Link to="/demo" style={{ textDecoration: 'underline', fontWeight: 700 }}>Sandbox Demo Personas</Link>.
              </div>
            )}

            {/* Live Evaluation Result */}
            {eligibilityResult && (
              <div style={{
                background: 'rgba(255, 255, 255, 0.04)',
                borderRadius: '12px',
                padding: '16px',
                border: '1px solid var(--border-subtle)'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)', fontWeight: 600 }}>Verdict:</span>
                  <span className={`badge ${
                    eligibilityResult.overallStatus === 'ELIGIBLE' ? 'badge-success' :
                    eligibilityResult.overallStatus === 'PROBABLY_ELIGIBLE' ? 'badge-secondary' : 'badge-warning'
                  }`}>
                    {eligibilityResult.overallStatus}
                  </span>
                </div>

                <div style={{ marginBottom: '12px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', marginBottom: '4px' }}>
                    <span>Match Score</span>
                    <strong>{eligibilityResult.matchScore}%</strong>
                  </div>
                  <div style={{ height: '8px', background: 'rgba(255, 255, 255, 0.1)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{
                      height: '100%',
                      width: `${eligibilityResult.matchScore}%`,
                      background: eligibilityResult.matchScore >= 80 ? '#10b981' : eligibilityResult.matchScore >= 50 ? '#06b6d4' : '#ef4444'
                    }} />
                  </div>
                </div>

                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: 1.4, marginBottom: '14px' }}>
                  {eligibilityResult.explanation}
                </p>

                {/* Criteria Pass/Fail list */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {eligibilityResult.criteriaResults?.map((cr, i) => (
                    <div key={i} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.78rem' }}>
                      <span style={{ color: 'var(--text-muted)' }}>{cr.displayLabel || cr.criterionName}:</span>
                      {cr.status === 'PASS' ? (
                        <span style={{ color: '#34d399', display: 'flex', alignItems: 'center', gap: '3px' }}>
                          <CheckCircle2 size={13} /> Pass
                        </span>
                      ) : cr.status === 'FAIL' ? (
                        <span style={{ color: '#f87171', display: 'flex', alignItems: 'center', gap: '3px' }}>
                          <XCircle size={13} /> Ineligible
                        </span>
                      ) : (
                        <span style={{ color: '#fbbf24' }}>Missing Data</span>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Official Portal Box */}
            <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid var(--border-subtle)' }}>
              <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-dim)', marginBottom: '8px' }}>
                Official Verification Link:
              </div>
              {scheme.officialUrl ? (
                <a
                  href={scheme.officialUrl}
                  target="_blank"
                  rel="noreferrer"
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    color: 'var(--secondary)',
                    fontSize: '0.85rem',
                    wordBreak: 'break-all'
                  }}
                >
                  <ExternalLink size={14} style={{ flexShrink: 0 }} />
                  {scheme.officialUrl}
                </a>
              ) : (
                <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>Visit district welfare office</span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Track Application Modal */}
      {showTrackModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)',
          backdropFilter: 'blur(5px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '20px'
        }}>
          <div className="glass-card" style={{
            maxWidth: '560px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            padding: '28px',
            borderRadius: '16px',
            position: 'relative'
          }}>
            <button
              onClick={() => setShowTrackModal(false)}
              style={{
                position: 'absolute',
                top: '20px',
                right: '20px',
                background: 'transparent',
                border: 'none',
                color: 'var(--text-muted)',
                cursor: 'pointer'
              }}
            >
              <X size={20} />
            </button>

            <h2 style={{ fontSize: '1.4rem', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Clock size={20} color="var(--primary)" /> Track Scheme Application
            </h2>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-dim)', marginBottom: '20px' }}>
              Record your application for <strong>{scheme.name}</strong> to stay organized with submission dates, reference IDs, and next actions.
            </p>

            {trackingError && (
              <div style={{
                background: 'rgba(239, 68, 68, 0.12)',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                borderRadius: '8px',
                padding: '10px 14px',
                color: '#f87171',
                fontSize: '0.85rem',
                marginBottom: '16px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <AlertCircle size={16} /> {trackingError}
              </div>
            )}

            {trackingSuccess ? (
              <div style={{
                textAlign: 'center',
                padding: '30px 20px',
                color: '#34d399'
              }}>
                <CheckCircle size={48} style={{ margin: '0 auto 12px' }} />
                <h3 style={{ fontSize: '1.2rem', marginBottom: '6px', color: '#fff' }}>Application Tracking Started!</h3>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                  Your tracking record has been created. You can view and update it anytime from your citizen profile.
                </p>
              </div>
            ) : (
              <form onSubmit={handleSaveTrackApplication} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                    Who is applying for this scheme? *
                  </label>
                  <select
                    value={trackForm.householdMemberId}
                    onChange={(e) => setTrackForm(prev => ({ ...prev, householdMemberId: e.target.value }))}
                    style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', background: 'var(--surface)', color: 'var(--text-main)', border: '1px solid var(--border-subtle)' }}
                  >
                    <option value="">Myself (Account Holder: {user?.fullName || user?.email})</option>
                    {householdMembers.map(m => (
                      <option key={m.id} value={m.id}>
                        {m.fullName} ({m.relationship})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                    Current Application Status *
                  </label>
                  <select
                    value={trackForm.status}
                    onChange={(e) => setTrackForm(prev => ({ ...prev, status: e.target.value }))}
                    style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', background: 'var(--surface)', color: 'var(--text-main)', border: '1px solid var(--border-subtle)' }}
                  >
                    <option value="PLANNING">Planning to Apply (Gathering Documents)</option>
                    <option value="READY_TO_APPLY">Ready to Apply (Documents Complete)</option>
                    <option value="SUBMITTED">Already Submitted to Government Portal / Office</option>
                    <option value="UNDER_REVIEW">Under Official Scrutiny / Review</option>
                  </select>
                </div>

                {(trackForm.status === 'SUBMITTED' || trackForm.status === 'UNDER_REVIEW') && (
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                    <div>
                      <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                        Acknowledgement / Reference ID
                      </label>
                      <input
                        type="text"
                        placeholder="e.g. ACK-2026-987654"
                        value={trackForm.applicationReference}
                        onChange={(e) => setTrackForm(prev => ({ ...prev, applicationReference: e.target.value }))}
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '8px' }}
                      />
                    </div>
                    <div>
                      <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                        Date Submitted
                      </label>
                      <input
                        type="date"
                        value={trackForm.appliedDate}
                        onChange={(e) => setTrackForm(prev => ({ ...prev, appliedDate: e.target.value }))}
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '8px' }}
                      />
                    </div>
                  </div>
                )}

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                      Next Action Date (Optional)
                    </label>
                    <input
                      type="date"
                      value={trackForm.nextActionDate}
                      onChange={(e) => setTrackForm(prev => ({ ...prev, nextActionDate: e.target.value }))}
                      style={{ width: '100%', padding: '10px 12px', borderRadius: '8px' }}
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                      Next Step Reminder Note
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Visit MeeSeva / submit bank passbook"
                      value={trackForm.nextActionNotes}
                      onChange={(e) => setTrackForm(prev => ({ ...prev, nextActionNotes: e.target.value }))}
                      style={{ width: '100%', padding: '10px 12px', borderRadius: '8px' }}
                    />
                  </div>
                </div>

                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                    Notes & Preparation Details (Optional)
                  </label>
                  <textarea
                    rows={2}
                    placeholder="Enter any relevant details, documents gathered, or application instructions..."
                    value={trackForm.notes}
                    onChange={(e) => setTrackForm(prev => ({ ...prev, notes: e.target.value }))}
                    style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', resize: 'vertical' }}
                  />
                </div>

                <div style={{
                  padding: '10px 12px',
                  borderRadius: '8px',
                  background: 'rgba(255, 255, 255, 0.03)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '0.78rem',
                  color: 'var(--text-dim)'
                }}>
                  Note: This application tracker is citizen-managed to organize your scheme records. It does not communicate directly with government servers.
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '8px' }}>
                  <button
                    type="button"
                    onClick={() => setShowTrackModal(false)}
                    className="btn btn-secondary btn-sm"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={trackingSaving}
                    className="btn btn-primary btn-sm"
                  >
                    {trackingSaving ? 'Recording...' : 'Start Tracking'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
