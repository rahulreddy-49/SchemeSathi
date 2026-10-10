import React, { useState, useEffect, useCallback } from 'react';
import { demoApi } from '../api';
import { Link } from 'react-router-dom';
import { 
  CheckCircle2, 
  XCircle, 
  Zap, 
  RefreshCw,
  ChevronDown,
  ChevronUp
} from 'lucide-react';

export default function DemoPage() {
  const [demoProfiles, setDemoProfiles] = useState([]);
  const [selectedPersona, setSelectedPersona] = useState(null);
  const [schemes, setSchemes] = useState([]);
  const [evaluations, setEvaluations] = useState([]);
  const [loadingProfiles, setLoadingProfiles] = useState(true);
  const [evaluating, setEvaluating] = useState(false);
  const [expandedSchemeId, setExpandedSchemeId] = useState(null);

  const evaluatePersona = useCallback(async (persona, schemeList) => {
    if (!persona || !schemeList || schemeList.length === 0) return;
    setEvaluating(true);
    setEvaluations([]);
    try {
      const evalPromises = schemeList.map(s => 
        demoApi.checkEligibility(s.id, persona.profile)
      );
      const responses = await Promise.all(evalPromises);
      const results = responses
        .map(r => r.data?.data)
        .filter(Boolean)
        .sort((a, b) => b.matchScore - a.matchScore);
      setEvaluations(results);
    } catch (err) {
      console.error(err);
    } finally {
      setEvaluating(false);
    }
  }, []);

  useEffect(() => {
    // Load demo profiles and schemes
    Promise.all([demoApi.getProfiles(), demoApi.getSchemes()])
      .then(([profilesRes, schemesRes]) => {
        if (profilesRes.data?.success) {
          const profs = profilesRes.data.data;
          setDemoProfiles(profs);
          if (profs.length > 0) {
            setSelectedPersona(profs[0]);
          }
        }
        if (schemesRes.data?.success) {
          setSchemes(schemesRes.data.data);
        }
      })
      .catch(err => console.error(err))
      .finally(() => setLoadingProfiles(false));
  }, []);

  // When selected persona changes, evaluate against schemes
  useEffect(() => {
    if (selectedPersona && schemes.length > 0) {
      evaluatePersona(selectedPersona, schemes);
    }
  }, [selectedPersona, schemes, evaluatePersona]);

  return (
    <div className="container fade-in" style={{ padding: '40px 24px 80px' }}>
      {/* Sandbox Banner */}
      <div style={{
        background: 'rgba(6, 182, 212, 0.1)',
        border: '1px solid rgba(6, 182, 212, 0.3)',
        borderRadius: '16px',
        padding: '16px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '12px',
        marginBottom: '32px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Zap size={22} color="#06b6d4" />
          <div>
            <strong style={{ color: '#38bdf8', fontSize: '0.95rem' }}>Synthetic Sandbox Environment</strong>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Explore how deterministic eligibility rules adapt dynamically without creating an account or submitting real documents.
            </p>
          </div>
        </div>
        <span className="badge badge-secondary">100% Isolated Data</span>
      </div>

      {/* Title */}
      <div style={{ marginBottom: '32px' }}>
        <span className="badge badge-primary">Interactive Playground</span>
        <h1 style={{ fontSize: '2.4rem', marginTop: '8px', marginBottom: '8px' }}>
          Explore with Synthetic Personas
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '750px' }}>
          Click any persona below to see how our engine evaluates age, landholding, caste category, and income against actual statutory criteria in real time.
        </p>
      </div>

      {/* Persona Selection Cards */}
      {loadingProfiles ? (
        <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
          Loading personas...
        </div>
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
          gap: '20px',
          marginBottom: '40px'
        }}>
          {demoProfiles.map((p, idx) => {
            const isSelected = selectedPersona?.name === p.name;
            return (
              <div
                key={idx}
                onClick={() => setSelectedPersona(p)}
                className="glass-card"
                style={{
                  padding: '20px',
                  cursor: 'pointer',
                  border: isSelected ? '2px solid var(--primary)' : '1px solid var(--border-subtle)',
                  background: isSelected ? 'rgba(249, 115, 22, 0.1)' : 'var(--bg-card)',
                  boxShadow: isSelected ? '0 0 20px rgba(249, 115, 22, 0.2)' : 'none',
                  transition: 'all 0.2s'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                  <span className="badge badge-primary">Persona #{idx + 1}</span>
                  {isSelected && (
                    <span style={{ fontSize: '0.75rem', color: '#fb923c', fontWeight: 700 }}>Active</span>
                  )}
                </div>
                <h3 style={{ fontSize: '1.2rem', marginBottom: '6px', color: '#fff' }}>
                  {p.name.replace('Demo: ', '')}
                </h3>
                <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '14px' }}>
                  {p.description}
                </p>

                {/* Profile snippet */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '0.78rem' }}>
                  <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '6px 10px', borderRadius: '6px' }}>
                    <span style={{ color: 'var(--text-dim)' }}>Occupation: </span>
                    <strong>{p.profile.occupation}</strong>
                  </div>
                  <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '6px 10px', borderRadius: '6px' }}>
                    <span style={{ color: 'var(--text-dim)' }}>Income: </span>
                    <strong>₹{p.profile.annualIncome?.toLocaleString()}</strong>
                  </div>
                  <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '6px 10px', borderRadius: '6px' }}>
                    <span style={{ color: 'var(--text-dim)' }}>Category: </span>
                    <strong>{p.profile.category}</strong>
                  </div>
                  <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '6px 10px', borderRadius: '6px' }}>
                    <span style={{ color: 'var(--text-dim)' }}>Land: </span>
                    <strong>{p.profile.landOwnership ? `${p.profile.landArea} ac` : 'None'}</strong>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Real-time Evaluation Results for Selected Persona */}
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <div>
            <h2 style={{ fontSize: '1.6rem', color: '#fff' }}>
              Statutory Evaluation for {selectedPersona?.name.replace('Demo: ', '')}
            </h2>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              Deterministic pass/fail results generated by the SchemeSathi engine.
            </p>
          </div>

          {evaluating && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#06b6d4', fontSize: '0.9rem' }}>
              <RefreshCw size={16} className="spin" /> Calculating...
            </div>
          )}
        </div>

        {evaluations.length === 0 ? (
          <div className="glass-card" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            Evaluating schemes for selected persona...
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {evaluations.map((res) => {
              const isEligible = res.overallStatus === 'ELIGIBLE';
              const isProbable = res.overallStatus === 'PROBABLY_ELIGIBLE';
              const isExpanded = expandedSchemeId === res.schemeId;

              return (
                <div
                  key={res.schemeId}
                  className="glass-card"
                  style={{
                    padding: '24px',
                    borderLeft: `5px solid ${isEligible ? '#10b981' : isProbable ? '#06b6d4' : '#ef4444'}`
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '16px', flexWrap: 'wrap' }}>
                    <div style={{ flex: 1, minWidth: '280px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
                        <span className={`badge ${isEligible ? 'badge-success' : isProbable ? 'badge-secondary' : 'badge-warning'}`}>
                          {res.overallStatus}
                        </span>
                        <span style={{ fontSize: '0.85rem', fontWeight: 800, color: '#fff' }}>
                          {res.matchScore}% Match
                        </span>
                      </div>

                      <h3 style={{ fontSize: '1.25rem', color: '#fff', marginBottom: '6px' }}>
                        <Link to={`/schemes/${res.schemeId}`} style={{ color: 'inherit' }}>
                          {res.schemeName}
                        </Link>
                      </h3>

                      <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', lineHeight: 1.5, marginBottom: '10px' }}>
                        {res.explanation}
                      </p>
                    </div>

                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                      <button
                        onClick={() => setExpandedSchemeId(isExpanded ? null : res.schemeId)}
                        className="btn btn-secondary btn-sm"
                        style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                      >
                        {isExpanded ? <>Hide Rules <ChevronUp size={14} /></> : <>Inspect Rules ({res.criteriaResults?.length}) <ChevronDown size={14} /></>}
                      </button>
                      <Link to={`/schemes/${res.schemeId}`} className="btn btn-primary btn-sm">
                        View Details
                      </Link>
                    </div>
                  </div>

                  {/* Expanded Rules Breakdown */}
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
                        Detailed Criteria Evaluation:
                      </div>

                      {res.criteriaResults?.map((cr, i) => (
                        <div
                          key={i}
                          style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            background: 'rgba(255, 255, 255, 0.02)',
                            padding: '10px 14px',
                            borderRadius: '8px',
                            fontSize: '0.82rem'
                          }}
                        >
                          <div>
                            <span style={{ fontWeight: 700, color: '#fff' }}>{cr.displayLabel || cr.criterionName}</span>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', marginTop: '2px' }}>
                              Rule: {cr.requiredValue} | Persona Value: {cr.citizenValue}
                            </div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                              {cr.explanation}
                            </div>
                          </div>
                          {cr.status === 'PASS' ? (
                            <span style={{ color: '#34d399', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '4px' }}>
                              <CheckCircle2 size={16} /> PASS
                            </span>
                          ) : (
                            <span style={{ color: '#f87171', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '4px' }}>
                              <XCircle size={16} /> INELIGIBLE
                            </span>
                          )}
                        </div>
                      ))}

                      {/* Required Documents checklist for this scheme */}
                      {res.requiredDocuments?.length > 0 && (
                        <div style={{ marginTop: '8px' }}>
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', fontWeight: 600 }}>Required Verification Documents:</span>
                          <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginTop: '4px' }}>
                            {res.requiredDocuments.map((doc, dIdx) => (
                              <span key={dIdx} style={{ fontSize: '0.72rem', background: 'rgba(6, 182, 212, 0.1)', color: '#38bdf8', padding: '3px 8px', borderRadius: '4px' }}>
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
        )}
      </div>
    </div>
  );
}
