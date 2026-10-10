import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { schemesApi, demoApi } from '../api';
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
  ListOrdered
} from 'lucide-react';

export default function SchemeDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();

  const [scheme, setScheme] = useState(null);
  const [loading, setLoading] = useState(true);
  const [eligibilityResult, setEligibilityResult] = useState(null);
  const [checkingEligibility, setCheckingEligibility] = useState(false);
  const [saved, setSaved] = useState(false);

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

  const handleToggleSave = async () => {
    if (!user) {
      alert('Please login to save schemes to your account.');
      return;
    }
    try {
      if (saved) {
        await schemesApi.unsave(id);
        setSaved(false);
      } else {
        await schemesApi.save(id);
        setSaved(true);
      }
    } catch (e) {
      console.error(e);
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

          <div style={{ display: 'flex', gap: '12px' }}>
            <button
              onClick={handleToggleSave}
              className="btn btn-secondary btn-sm"
              style={{ display: 'flex', alignItems: 'center', gap: '6px', color: saved ? 'var(--primary)' : 'inherit' }}
            >
              <Bookmark size={16} fill={saved ? 'currentColor' : 'none'} />
              {saved ? 'Saved in Profile' : 'Save Scheme'}
            </button>
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
    </div>
  );
}
