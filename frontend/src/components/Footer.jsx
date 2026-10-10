import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldCheck, PhoneCall, Heart, Globe, AlertCircle } from 'lucide-react';

const currentYear = new Date().getFullYear();

export default function Footer() {
  return (
    <footer style={{
      background: '#070a10',
      borderTop: '1px solid var(--border-subtle)',
      padding: '48px 0 24px',
      marginTop: '80px',
      color: 'var(--text-muted)'
    }}>
      <div className="container">
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '36px',
          marginBottom: '40px'
        }}>
          {/* Brand Info */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '14px' }}>
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '10px',
                background: 'linear-gradient(135deg, #f97316 0%, #06b6d4 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <ShieldCheck size={22} color="#ffffff" />
              </div>
              <span style={{ fontFamily: 'var(--font-heading)', fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-main)' }}>
                Scheme<span style={{ color: 'var(--primary)' }}>Sathi</span>
              </span>
            </div>
            <p style={{ fontSize: '0.85rem', lineHeight: 1.6, marginBottom: '16px' }}>
              An AI-assisted citizen entitlement discovery platform. Bridging the gap between welfare policies and citizens across India with verifiable eligibility analysis.
            </p>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              fontSize: '0.75rem',
              background: 'rgba(16, 185, 129, 0.1)',
              color: '#34d399',
              padding: '4px 10px',
              borderRadius: '6px',
              border: '1px solid rgba(16, 185, 129, 0.2)'
            }}>
              <Globe size={13} />
              Verified Official Data Sources
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 style={{ color: 'var(--text-main)', fontSize: '0.95rem', marginBottom: '16px', fontWeight: 700 }}>
              Quick Navigation
            </h4>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '0.85rem' }}>
              <li><Link to="/schemes" style={{ hover: { color: 'var(--primary)' } }}>Browse All Schemes</Link></li>
              <li><Link to="/eligibility">Eligibility Assessment</Link></li>
              <li><Link to="/ai">AI Sahayak Assistant</Link></li>
              <li><Link to="/demo">Sandbox Demo Mode</Link></li>
              <li><Link to="/profile">Citizen Profile Dashboard</Link></li>
            </ul>
          </div>

          {/* Official Helplines */}
          <div>
            <h4 style={{ color: 'var(--text-main)', fontSize: '0.95rem', marginBottom: '16px', fontWeight: 700 }}>
              National Helplines
            </h4>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '0.85rem' }}>
              <li style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <PhoneCall size={14} color="#f97316" />
                <span>PM-KISAN: <strong>155261 / 011-24300606</strong></span>
              </li>
              <li style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <PhoneCall size={14} color="#06b6d4" />
                <span>Ayushman Bharat: <strong>14555</strong></span>
              </li>
              <li style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <PhoneCall size={14} color="#10b981" />
                <span>PMMVY Maternity: <strong>1098 / 181</strong></span>
              </li>
              <li style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <PhoneCall size={14} color="#8b5cf6" />
                <span>National Scholarship: <strong>0120-6619540</strong></span>
              </li>
            </ul>
          </div>

          {/* Legal & Disclaimer */}
          <div>
            <h4 style={{ color: 'var(--text-main)', fontSize: '0.95rem', marginBottom: '16px', fontWeight: 700 }}>
              Public Welfare Disclaimer
            </h4>
            <div style={{
              background: 'rgba(255, 255, 255, 0.03)',
              border: '1px solid var(--border-subtle)',
              borderRadius: '10px',
              padding: '12px',
              fontSize: '0.78rem',
              lineHeight: 1.5
            }}>
              <p style={{ display: 'flex', gap: '6px', marginBottom: '6px', color: '#fbbf24' }}>
                <AlertCircle size={15} style={{ flexShrink: 0, marginTop: '2px' }} />
                <span>Independent Civic Tech Initiative</span>
              </p>
              SchemeSathi is an informational discovery tool. Eligibility evaluations are guidance only; actual approvals are made solely by respective government departments.
            </div>
          </div>
        </div>

        {/* Bottom Bar */}
        <div style={{
          borderTop: '1px solid var(--border-subtle)',
          paddingTop: '20px',
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '12px',
          fontSize: '0.8rem'
        }}>
          <div>
            &copy; {currentYear} SchemeSathi (योजना साथी). Built for Digital India empowerment.
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              Made with <Heart size={13} color="#ef4444" fill="#ef4444" /> for citizens of India
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
}
