import React from 'react';
import { ShieldCheck, AlertTriangle, AlertCircle, ExternalLink } from 'lucide-react';

/**
 * Validates whether a given string is a safe, well-formed HTTP/HTTPS URL.
 * Rejects javascript:, data:, relative URLs, or invalid hostnames.
 */
export function isValidHttpUrl(string) {
  if (!string || typeof string !== 'string') return false;
  const trimmed = string.trim();
  if (!trimmed.startsWith('http://') && !trimmed.startsWith('https://')) return false;
  try {
    const parsed = new URL(trimmed);
    return (parsed.protocol === 'http:' || parsed.protocol === 'https:') && Boolean(parsed.hostname);
  } catch {
    return false;
  }
}

/**
 * Normalizes verification status and provides consistent display configuration.
 */
export function getVerificationStatusInfo(status) {
  const norm = (status || '').toUpperCase().trim();
  if (norm === 'VERIFIED') {
    return {
      key: 'VERIFIED',
      label: 'Source verified',
      badgeClass: 'badge-success',
      icon: ShieldCheck,
      color: '#34d399',
      borderColor: 'rgba(16, 185, 129, 0.3)',
      bg: 'rgba(16, 185, 129, 0.12)',
      isVerified: true
    };
  }
  if (norm === 'NEEDS_REVIEW') {
    return {
      key: 'NEEDS_REVIEW',
      label: 'Source needs review',
      badgeClass: 'badge-warning',
      icon: AlertTriangle,
      color: '#fbbf24',
      borderColor: 'rgba(245, 158, 11, 0.3)',
      bg: 'rgba(245, 158, 11, 0.12)',
      isVerified: false
    };
  }
  return {
    key: 'UNVERIFIED',
    label: 'Source not yet verified',
    badgeClass: 'badge-unverified',
    icon: AlertCircle,
    color: '#94a3b8',
    borderColor: 'rgba(148, 163, 184, 0.3)',
    bg: 'rgba(148, 163, 184, 0.12)',
    isVerified: false
  };
}

/**
 * Verification status badge displayed on scheme cards and detail headers.
 */
export function SchemeVerificationBadge({ status, className = '' }) {
  const info = getVerificationStatusInfo(status);
  const Icon = info.icon;
  return (
    <span
      className={`badge ${info.badgeClass} ${className}`.trim()}
      role="status"
      aria-label={`Verification status: ${info.label}`}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '5px',
        color: info.color,
        background: info.bg,
        border: `1px solid ${info.borderColor}`
      }}
    >
      <Icon size={12} aria-hidden="true" />
      <span>{info.label}</span>
    </span>
  );
}

/**
 * Educational advisory notice shown when a scheme is marked NEEDS_REVIEW or UNVERIFIED.
 */
export function SchemeVerificationNotice({ status, officialUrl, compact = false }) {
  const info = getVerificationStatusInfo(status);
  if (info.isVerified) return null;

  const validUrl = isValidHttpUrl(officialUrl) ? officialUrl.trim() : null;

  if (compact) {
    return (
      <div
        role="note"
        aria-label="Verification advisory"
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          gap: '8px',
          padding: '8px 12px',
          background: 'rgba(245, 158, 11, 0.08)',
          border: '1px solid rgba(245, 158, 11, 0.22)',
          borderRadius: '8px',
          fontSize: '0.76rem',
          color: '#fbbf24',
          lineHeight: 1.4,
          margin: '10px 0'
        }}
      >
        <AlertTriangle size={14} style={{ flexShrink: 0, marginTop: '2px' }} aria-hidden="true" />
        <div>
          <span>
            <strong>Notice:</strong> This scheme information has not completed the platform's source-verification process. Please confirm details using the official government source before applying.
          </span>
          {validUrl && (
            <div style={{ marginTop: '4px' }}>
              <a
                href={validUrl}
                target="_blank"
                rel="noreferrer"
                style={{
                  color: '#38bdf8',
                  textDecoration: 'underline',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px'
                }}
              >
                Official portal <ExternalLink size={11} aria-hidden="true" />
              </a>
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div
      role="alert"
      aria-label="Scheme verification advisory"
      style={{
        display: 'flex',
        alignItems: 'flex-start',
        gap: '14px',
        padding: '16px 20px',
        background: 'rgba(245, 158, 11, 0.08)',
        border: '1px solid rgba(245, 158, 11, 0.3)',
        borderRadius: '12px',
        margin: '20px 0 24px'
      }}
    >
      <AlertTriangle size={22} color="#fbbf24" style={{ flexShrink: 0, marginTop: '2px' }} aria-hidden="true" />
      <div>
        <h4 style={{ fontSize: '0.95rem', color: '#fbbf24', marginBottom: '4px', fontWeight: 700 }}>
          {status === 'NEEDS_REVIEW' ? 'Source Needs Review' : 'Source Not Yet Verified'}
        </h4>
        <p style={{ fontSize: '0.88rem', color: 'var(--text-main)', lineHeight: 1.5, margin: 0 }}>
          This scheme information has not completed the platform's source-verification process. While this scheme is published for citizen awareness, you should confirm all benefit amounts, eligibility criteria, required documents, and application deadlines directly with the official government source before applying.
        </p>
        {validUrl && (
          <div style={{ marginTop: '12px' }}>
            <a
              href={validUrl}
              target="_blank"
              rel="noreferrer"
              className="btn btn-secondary btn-sm"
              style={{
                fontSize: '0.82rem',
                padding: '6px 14px',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px'
              }}
            >
              Confirm on Official Source <ExternalLink size={13} aria-hidden="true" />
            </a>
          </div>
        )}
      </div>
    </div>
  );
}

export default SchemeVerificationBadge;
