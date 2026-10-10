import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { schemesApi } from '../api';
import { useAuth } from '../AuthContext';
import { 
  Search, 
  Bookmark, 
  ExternalLink, 
  Building,
  Info
} from 'lucide-react';

export default function SchemesPage() {
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  
  const [schemes, setSchemes] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState(searchParams.get('search') || '');
  const [selectedCategory, setSelectedCategory] = useState(searchParams.get('category') || '');
  const [selectedScope, setSelectedScope] = useState('');
  const [selectedState, setSelectedState] = useState('');
  const [savingId, setSavingId] = useState(null);

  const statesList = [
    'All States',
    'Telangana',
    'Andhra Pradesh',
    'Uttar Pradesh',
    'Maharashtra',
    'Bihar',
    'Rajasthan',
    'Madhya Pradesh',
    'Karnataka',
    'Tamil Nadu',
    'West Bengal',
    'Gujarat'
  ];

  useEffect(() => {
    // Fetch categories
    schemesApi.getCategories()
      .then(res => {
        if (res.data?.success) setCategories(res.data.data);
      })
      .catch(() => {
        setCategories(['Agriculture', 'Healthcare', 'Education', 'Housing', 'Social Security', 'Employment', 'Women Empowerment']);
      });
  }, []);

  useEffect(() => {
    setLoading(true);
    const params = {};
    if (searchTerm) params.search = searchTerm;
    if (selectedCategory) params.category = selectedCategory;
    if (selectedState && selectedState !== 'All States') params.state = selectedState;

    schemesApi.getAll(params)
      .then(res => {
        if (res.data?.success) {
          let list = res.data.data;
          if (selectedScope) {
            list = list.filter(s => s.scope === selectedScope);
          }
          setSchemes(list);
        }
      })
      .catch(err => {
        console.error('Failed to load schemes', err);
      })
      .finally(() => setLoading(false));
  }, [searchTerm, selectedCategory, selectedState, selectedScope]);

  const handleToggleSave = async (schemeId, currentSaved) => {
    if (!user) {
      alert('Please login to save schemes to your personal profile.');
      return;
    }
    setSavingId(schemeId);
    try {
      if (currentSaved) {
        await schemesApi.unsave(schemeId);
      } else {
        await schemesApi.save(schemeId);
      }
      setSchemes(prev => prev.map(s => s.id === schemeId ? { ...s, saved: !currentSaved } : s));
    } catch (e) {
      console.error(e);
    } finally {
      setSavingId(null);
    }
  };

  return (
    <div className="container fade-in" style={{ padding: '40px 24px 80px' }}>
      {/* Header */}
      <div style={{ marginBottom: '32px' }}>
        <span className="badge badge-primary">Direct Welfare Directory</span>
        <h1 style={{ fontSize: '2.4rem', marginTop: '8px', marginBottom: '8px' }}>
          Explore Government Schemes
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '750px' }}>
          Browse verified government initiatives across central ministries and state departments. Search, filter, and test your eligibility with verified criteria.
        </p>
      </div>

      {/* Filter and Search Bar */}
      <div className="glass-card" style={{ padding: '24px', marginBottom: '36px' }}>
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: '16px',
          alignItems: 'center'
        }}>
          {/* Keyword Search */}
          <div style={{ position: 'relative' }}>
            <Search size={18} color="var(--primary)" style={{ position: 'absolute', left: '14px', top: '14px' }} />
            <input
              type="text"
              placeholder="Search keyword..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ width: '100%', paddingLeft: '40px' }}
            />
          </div>

          {/* Category Dropdown */}
          <div>
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              style={{ width: '100%' }}
            >
              <option value="">All Categories</option>
              {categories.map((c, i) => (
                <option key={i} value={c}>{c}</option>
              ))}
            </select>
          </div>

          {/* Scope Dropdown */}
          <div>
            <select
              value={selectedScope}
              onChange={(e) => setSelectedScope(e.target.value)}
              style={{ width: '100%' }}
            >
              <option value="">All Jurisdictions</option>
              <option value="CENTRAL">Central Schemes (Nationwide)</option>
              <option value="STATE">State Schemes</option>
            </select>
          </div>

          {/* State Dropdown */}
          <div>
            <select
              value={selectedState}
              onChange={(e) => setSelectedState(e.target.value)}
              style={{ width: '100%' }}
            >
              <option value="">All States</option>
              {statesList.map((st, i) => (
                <option key={i} value={st}>{st}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Active Filters Summary */}
        {(searchTerm || selectedCategory || selectedScope || selectedState) && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '16px', flexWrap: 'wrap' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>Active Filters:</span>
            {searchTerm && (
              <span className="badge badge-primary">Keyword: {searchTerm}</span>
            )}
            {selectedCategory && (
              <span className="badge badge-secondary">Category: {selectedCategory}</span>
            )}
            {selectedScope && (
              <span className="badge badge-warning">Scope: {selectedScope}</span>
            )}
            {selectedState && (
              <span className="badge badge-success">State: {selectedState}</span>
            )}
            <button
              onClick={() => {
                setSearchTerm('');
                setSelectedCategory('');
                setSelectedScope('');
                setSelectedState('');
              }}
              style={{
                background: 'transparent',
                color: '#ef4444',
                fontSize: '0.8rem',
                cursor: 'pointer',
                marginLeft: '8px',
                textDecoration: 'underline'
              }}
            >
              Reset Filters
            </button>
          </div>
        )}
      </div>

      {/* Schemes Results */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px', color: 'var(--text-muted)' }}>
          <div style={{ fontSize: '1.2rem', marginBottom: '8px' }}>Fetching official schemes...</div>
        </div>
      ) : schemes.length === 0 ? (
        <div className="glass-card" style={{ padding: '60px 24px', textAlign: 'center' }}>
          <Info size={40} color="var(--primary)" style={{ marginBottom: '16px' }} />
          <h3 style={{ fontSize: '1.3rem', marginBottom: '8px' }}>No matching schemes found</h3>
          <p style={{ color: 'var(--text-muted)', maxWidth: '450px', margin: '0 auto 20px', fontSize: '0.9rem' }}>
            Try broadening your search term, clearing state filters, or selecting "All Categories".
          </p>
          <button
            onClick={() => {
              setSearchTerm('');
              setSelectedCategory('');
              setSelectedScope('');
              setSelectedState('');
            }}
            className="btn btn-primary btn-sm"
          >
            Clear All Filters
          </button>
        </div>
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))',
          gap: '24px'
        }}>
          {schemes.map((scheme) => (
            <div
              key={scheme.id}
              className="glass-card"
              style={{
                padding: '24px',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                position: 'relative'
              }}
            >
              <div>
                {/* Header tags */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '14px' }}>
                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                    <span className="badge badge-primary">{scheme.benefitCategory || 'General'}</span>
                    <span className="badge badge-secondary">
                      {scheme.scope === 'CENTRAL' ? 'Central' : scheme.state || 'State'}
                    </span>
                  </div>

                  {/* Bookmark Button */}
                  <button
                    onClick={() => handleToggleSave(scheme.id, scheme.saved)}
                    disabled={savingId === scheme.id}
                    title={scheme.saved ? 'Remove from saved' : 'Save scheme'}
                    style={{
                      background: scheme.saved ? 'rgba(249, 115, 22, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                      border: scheme.saved ? '1px solid var(--primary)' : '1px solid var(--border-subtle)',
                      borderRadius: '8px',
                      padding: '6px',
                      color: scheme.saved ? 'var(--primary)' : 'var(--text-dim)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      cursor: 'pointer'
                    }}
                  >
                    <Bookmark size={18} fill={scheme.saved ? 'currentColor' : 'none'} />
                  </button>
                </div>

                {/* Scheme Name */}
                <h3 style={{ fontSize: '1.25rem', marginBottom: '8px', color: '#fff', lineHeight: 1.3 }}>
                  <Link to={`/schemes/${scheme.id}`} style={{ color: 'inherit' }}>
                    {scheme.name}
                  </Link>
                </h3>

                {/* Authority */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: 'var(--text-dim)', marginBottom: '14px' }}>
                  <Building size={14} />
                  <span>{scheme.governmentAuthority || 'Government of India'}</span>
                </div>

                {/* Description */}
                <p style={{
                  fontSize: '0.85rem',
                  color: 'var(--text-muted)',
                  lineHeight: 1.5,
                  marginBottom: '16px',
                  display: '-webkit-box',
                  WebkitLineClamp: 3,
                  WebkitBoxOrient: 'vertical',
                  overflow: 'hidden'
                }}>
                  {scheme.description}
                </p>

                {/* Key Benefits */}
                <div style={{
                  background: 'rgba(255, 255, 255, 0.03)',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: '10px',
                  padding: '10px 14px',
                  fontSize: '0.8rem',
                  marginBottom: '16px'
                }}>
                  <div style={{ fontWeight: 700, color: '#38bdf8', marginBottom: '2px' }}>
                    Benefit Overview
                  </div>
                  <div style={{ color: 'var(--text-main)', lineHeight: 1.4 }}>
                    {scheme.benefits ? scheme.benefits.split('.')[0] : 'Financial and welfare assistance.'}
                  </div>
                </div>

                {/* Quick Rules Preview */}
                {scheme.eligibilityRules && scheme.eligibilityRules.length > 0 && (
                  <div style={{ marginBottom: '18px' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', fontWeight: 600, marginBottom: '6px' }}>
                      Key Eligibility Criteria:
                    </div>
                    <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                      {scheme.eligibilityRules.slice(0, 3).map((r, idx) => (
                        <span key={idx} style={{
                          fontSize: '0.72rem',
                          background: 'rgba(255, 255, 255, 0.05)',
                          padding: '3px 8px',
                          borderRadius: '6px',
                          border: '1px solid var(--border-subtle)',
                          color: 'var(--text-muted)'
                        }}>
                          {r.displayLabel || r.criterionField}: {r.value}
                        </span>
                      ))}
                      {scheme.eligibilityRules.length > 3 && (
                        <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)', alignSelf: 'center' }}>
                          +{scheme.eligibilityRules.length - 3} more
                        </span>
                      )}
                    </div>
                  </div>
                )}
              </div>

              {/* Action buttons */}
              <div style={{ display: 'flex', gap: '10px', paddingTop: '12px', borderTop: '1px solid var(--border-subtle)' }}>
                <Link
                  to={`/schemes/${scheme.id}`}
                  className="btn btn-primary btn-sm"
                  style={{ flex: 1 }}
                >
                  View Full Details
                </Link>
                {scheme.officialUrl && (
                  <a
                    href={scheme.officialUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="btn btn-secondary btn-sm"
                    title="Visit Official Government Portal"
                  >
                    <ExternalLink size={16} />
                  </a>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
