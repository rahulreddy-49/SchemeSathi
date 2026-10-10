import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { schemesApi, demoApi } from '../api';
import { 
  Search, 
  Sparkles, 
  ShieldCheck, 
  CheckCircle2, 
  Bot, 
  Users, 
  FileText, 
  ExternalLink,
  ChevronRight,
  Zap,
  Globe2
} from 'lucide-react';

export default function HomePage() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [featuredSchemes, setFeaturedSchemes] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch featured schemes
    schemesApi.getAll()
      .then(res => {
        if (res.data?.success) {
          setFeaturedSchemes(res.data.data.slice(0, 6));
        }
      })
      .catch(() => {
        // Fallback to demo API
        demoApi.getSchemes().then(res => {
          if (res.data?.success) {
            setFeaturedSchemes(res.data.data.slice(0, 6));
          }
        }).catch(() => {});
      })
      .finally(() => setLoading(false));
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/schemes?search=${encodeURIComponent(searchQuery.trim())}`);
    } else {
      navigate('/schemes');
    }
  };

  const categories = [
    { name: 'Agriculture', icon: '🌾', count: 'PM-KISAN & more', bg: 'rgba(16, 185, 129, 0.1)' },
    { name: 'Healthcare', icon: '🏥', count: 'Ayushman Bharat', bg: 'rgba(6, 182, 212, 0.1)' },
    { name: 'Education', icon: '🎓', count: 'Post-Matric & Pre-Matric', bg: 'rgba(139, 92, 246, 0.1)' },
    { name: 'Housing', icon: '🏠', count: 'PMAY-Urban & Gramin', bg: 'rgba(249, 115, 22, 0.1)' },
    { name: 'Social Security', icon: '🛡️', count: 'Atal Pension & Life Cover', bg: 'rgba(236, 72, 153, 0.1)' },
    { name: 'Employment', icon: '💼', count: 'MGNREGA & MUDRA', bg: 'rgba(59, 130, 246, 0.1)' }
  ];

  return (
    <div className="fade-in">
      {/* HERO SECTION */}
      <section style={{
        position: 'relative',
        padding: '70px 0 60px',
        textAlign: 'center',
        overflow: 'hidden'
      }}>
        <div className="container" style={{ position: 'relative', zIndex: 2 }}>
          {/* Badge */}
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '8px',
            padding: '6px 16px',
            borderRadius: '9999px',
            background: 'rgba(249, 115, 22, 0.12)',
            border: '1px solid rgba(249, 115, 22, 0.3)',
            color: '#fb923c',
            fontSize: '0.85rem',
            fontWeight: 700,
            marginBottom: '24px'
          }}>
            <Sparkles size={16} />
            <span>AI-Assisted Citizen Entitlement Engine</span>
            <span style={{ background: '#f97316', color: '#fff', fontSize: '0.7rem', padding: '1px 6px', borderRadius: '4px' }}>
              PROD
            </span>
          </div>

          {/* Heading */}
          <h1 style={{
            fontSize: 'clamp(2.2rem, 5vw, 3.8rem)',
            fontWeight: 800,
            lineHeight: 1.15,
            maxWidth: '900px',
            margin: '0 auto 20px',
            letterSpacing: '-1px'
          }}>
            Every Indian Entitled to Welfare Deserves to{' '}
            <span style={{
              background: 'linear-gradient(135deg, #f97316 0%, #06b6d4 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent'
            }}>
              Discover & Claim It.
            </span>
          </h1>

          {/* Subheading */}
          <p style={{
            fontSize: 'clamp(1rem, 2vw, 1.25rem)',
            color: 'var(--text-muted)',
            maxWidth: '700px',
            margin: '0 auto 36px',
            lineHeight: 1.6
          }}>
            SchemeSathi verifies your eligibility with zero guesswork using deterministic rules. Explore central and state schemes, prepare required documents, and get step-by-step guidance.
          </p>

          {/* Search Box */}
          <form 
            onSubmit={handleSearchSubmit}
            style={{
              maxWidth: '680px',
              margin: '0 auto 32px',
              display: 'flex',
              alignItems: 'center',
              background: '#131b2e',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              borderRadius: '16px',
              padding: '6px 8px 6px 18px',
              boxShadow: 'var(--shadow-lg)'
            }}
          >
            <Search size={22} color="var(--primary)" style={{ flexShrink: 0, marginRight: '12px' }} />
            <input
              type="text"
              placeholder="Search by scheme name, keyword (e.g., farmer, scholarship, pension)..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                flex: 1,
                background: 'transparent',
                border: 'none',
                boxShadow: 'none',
                fontSize: '1rem',
                padding: '10px 0',
                color: '#fff'
              }}
            />
            <button type="submit" className="btn btn-primary" style={{ flexShrink: 0, padding: '12px 24px' }}>
              Search
            </button>
          </form>

          {/* Quick Action Buttons */}
          <div style={{ display: 'flex', justifyContent: 'center', gap: '14px', flexWrap: 'wrap' }}>
            <Link to="/eligibility" className="btn btn-primary btn-lg">
              <CheckCircle2 size={20} />
              Check My Eligibility
            </Link>
            <Link to="/ai" className="btn btn-secondary btn-lg">
              <Bot size={20} color="var(--primary)" />
              Chat with AI Sahayak
            </Link>
            <Link to="/demo" className="btn btn-secondary btn-lg" style={{ border: '1px solid rgba(6, 182, 212, 0.4)' }}>
              <Zap size={20} color="#06b6d4" />
              Try Demo Persona
            </Link>
          </div>
        </div>
      </section>

      {/* STATS HIGHLIGHT BAR */}
      <section style={{ padding: '20px 0 50px' }}>
        <div className="container">
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '16px'
          }}>
            {[
              { label: 'Verified Government Schemes', value: '10+', sub: 'Central & State Ministries', icon: ShieldCheck, color: '#f97316' },
              { label: 'Eligibility Accuracy', value: '100%', sub: 'Deterministic Rule Engine', icon: CheckCircle2, color: '#10b981' },
              { label: 'AI Multi-Agent Pipeline', value: '4 Agents', sub: 'Discovery, Rules, Docs, Guide', icon: Bot, color: '#06b6d4' },
              { label: 'Official Portals Linked', value: 'Direct Links', sub: 'Zero Middlemen or Scams', icon: Globe2, color: '#8b5cf6' }
            ].map((stat, i) => {
              const Icon = stat.icon;
              return (
                <div key={i} className="glass-card" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <div style={{
                    width: '46px',
                    height: '46px',
                    borderRadius: '12px',
                    background: `${stat.color}15`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}>
                    <Icon size={24} color={stat.color} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.45rem', fontWeight: 800, lineHeight: 1.1 }}>{stat.value}</h3>
                    <p style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-main)' }}>{stat.label}</p>
                    <p style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>{stat.sub}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* CATEGORIES SECTION */}
      <section style={{ padding: '40px 0' }}>
        <div className="container">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '24px' }}>
            <div>
              <span className="badge badge-primary">Sectors</span>
              <h2 style={{ fontSize: '1.9rem', marginTop: '6px' }}>Browse by Welfare Sector</h2>
            </div>
            <Link to="/schemes" style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--primary)', fontWeight: 600, fontSize: '0.9rem' }}>
              View All Categories <ChevronRight size={16} />
            </Link>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
            gap: '16px'
          }}>
            {categories.map((cat, i) => (
              <Link
                key={i}
                to={`/schemes?category=${encodeURIComponent(cat.name)}`}
                className="glass-card"
                style={{
                  padding: '22px 18px',
                  borderRadius: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '10px',
                  transition: 'transform 0.2s, border-color 0.2s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-4px)'}
                onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
              >
                <span style={{ fontSize: '2rem' }}>{cat.icon}</span>
                <div>
                  <h4 style={{ fontSize: '1.05rem', fontWeight: 700 }}>{cat.name}</h4>
                  <p style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>{cat.count}</p>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* FEATURED SCHEMES SECTION */}
      <section style={{ padding: '50px 0' }}>
        <div className="container">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '28px' }}>
            <div>
              <span className="badge badge-secondary">Flagship Programs</span>
              <h2 style={{ fontSize: '1.9rem', marginTop: '6px' }}>Key Welfare Opportunities</h2>
            </div>
            <Link to="/schemes" className="btn btn-secondary btn-sm">
              See All Schemes ({featuredSchemes.length}+)
            </Link>
          </div>

          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
              Loading verified schemes...
            </div>
          ) : (
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
              gap: '24px'
            }}>
              {featuredSchemes.map((scheme) => (
                <div
                  key={scheme.id}
                  className="glass-card"
                  style={{
                    padding: '24px',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between'
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                      <span className="badge badge-primary">{scheme.benefitCategory || 'Central'}</span>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', fontWeight: 600 }}>
                        {scheme.scope === 'CENTRAL' ? 'Govt of India' : scheme.state || 'State'}
                      </span>
                    </div>

                    <h3 style={{ fontSize: '1.2rem', marginBottom: '10px', color: '#fff' }}>
                      {scheme.name}
                    </h3>
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
                  </div>

                  <div>
                    <div style={{
                      background: 'rgba(255, 255, 255, 0.03)',
                      borderRadius: '8px',
                      padding: '10px 12px',
                      fontSize: '0.8rem',
                      marginBottom: '18px',
                      color: '#38bdf8'
                    }}>
                      <strong>Benefit:</strong> {scheme.benefits?.split('.')[0] || 'Direct support'}
                    </div>

                    <div style={{ display: 'flex', gap: '10px' }}>
                      <Link
                        to={`/schemes/${scheme.id}`}
                        className="btn btn-primary btn-sm"
                        style={{ flex: 1 }}
                      >
                        Check & View
                      </Link>
                      {scheme.officialUrl && (
                        <a
                          href={scheme.officialUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="btn btn-secondary btn-sm"
                          title="Official Portal"
                        >
                          <ExternalLink size={16} />
                        </a>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* HOW SCHEMESATHI WORKS */}
      <section style={{ padding: '60px 0', background: 'rgba(18, 26, 43, 0.4)' }}>
        <div className="container">
          <div style={{ textAlign: 'center', maxWidth: '650px', margin: '0 auto 48px' }}>
            <span className="badge badge-success">Verifiable Technology</span>
            <h2 style={{ fontSize: '2.1rem', marginTop: '8px', marginBottom: '12px' }}>
              How SchemeSathi Empowers You
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
              We eliminate bureaucratic ambiguity using deterministic rule matching and AI guidance.
            </p>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: '24px'
          }}>
            {[
              {
                step: '01',
                title: 'Share Your Basic Profile',
                desc: 'Fill our 2-minute citizen profile or test instantly with ready demo personas. Your age, state, occupation, land, and income are matched.',
                icon: Users,
                color: '#f97316'
              },
              {
                step: '02',
                title: 'Deterministic Eligibility Match',
                desc: 'Our engine applies codified scheme rules without AI hallucinations. Get exact criteria pass/fail breakdown and match percentages.',
                icon: ShieldCheck,
                color: '#10b981'
              },
              {
                step: '03',
                title: 'Document Checklist & Apply',
                desc: 'See exactly what documents are needed before applying. Jump straight to official government portals with step-by-step guidance.',
                icon: FileText,
                color: '#06b6d4'
              }
            ].map((card, i) => {
              const Icon = card.icon;
              return (
                <div key={i} className="glass-card" style={{ padding: '32px 24px', position: 'relative' }}>
                  <div style={{
                    fontSize: '2.5rem',
                    fontWeight: 900,
                    fontFamily: 'var(--font-heading)',
                    color: 'rgba(255, 255, 255, 0.08)',
                    position: 'absolute',
                    top: '16px',
                    right: '20px'
                  }}>
                    {card.step}
                  </div>
                  <div style={{
                    width: '50px',
                    height: '50px',
                    borderRadius: '14px',
                    background: `${card.color}15`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    marginBottom: '20px'
                  }}>
                    <Icon size={26} color={card.color} />
                  </div>
                  <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>{card.title}</h3>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.6 }}>{card.desc}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* CTA BANNER */}
      <section style={{ padding: '60px 0' }}>
        <div className="container">
          <div style={{
            background: 'linear-gradient(135deg, rgba(249, 115, 22, 0.15) 0%, rgba(6, 182, 212, 0.15) 100%)',
            border: '1px solid rgba(249, 115, 22, 0.3)',
            borderRadius: '24px',
            padding: '48px 36px',
            textAlign: 'center',
            position: 'relative',
            overflow: 'hidden'
          }}>
            <h2 style={{ fontSize: '2.2rem', marginBottom: '14px' }}>
              Want to see SchemeSathi in action right now?
            </h2>
            <p style={{ color: 'var(--text-muted)', maxWidth: '600px', margin: '0 auto 28px', fontSize: '1rem' }}>
              Try our synthetic sandbox personas (Farmer, SC Student, Urban Professional) with one click to see how rules adapt instantly!
            </p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '14px', flexWrap: 'wrap' }}>
              <Link to="/demo" className="btn btn-primary btn-lg">
                <Zap size={20} />
                Launch Demo Sandbox
              </Link>
              <Link to="/schemes" className="btn btn-secondary btn-lg">
                Explore All Schemes
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
