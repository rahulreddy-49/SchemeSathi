import React, { useState, useEffect, useCallback } from 'react';
import { 
  ShieldCheck, 
  Layers, 
  CheckCircle, 
  FileEdit, 
  Archive, 
  AlertTriangle, 
  Search, 
  Plus, 
  RefreshCw, 
  ExternalLink, 
  Edit3, 
  Trash2, 
  UploadCloud, 
  X, 
  AlertCircle,
  BookOpen
} from 'lucide-react';
import { adminApi, schemesApi } from '../api';
import { useAuth } from '../AuthContext';

const getTodayDateString = () => new Date().toISOString().split('T')[0];

const FIELD_OPTIONS = [
  { value: 'age', label: 'Age (Years)', type: 'numeric', placeholder: 'e.g. 18 or 18:60 for BETWEEN' },
  { value: 'annualIncome', label: 'Annual Family Income (₹)', type: 'numeric', placeholder: 'e.g. 250000' },
  { value: 'gender', label: 'Gender', type: 'categorical', placeholder: 'e.g. Female,Male,Other' },
  { value: 'state', label: 'State', type: 'categorical', placeholder: 'e.g. Telangana,Andhra Pradesh' },
  { value: 'district', label: 'District', type: 'categorical', placeholder: 'e.g. Hyderabad' },
  { value: 'occupation', label: 'Occupation', type: 'categorical', placeholder: 'e.g. Farmer,Artisan,Student' },
  { value: 'educationLevel', label: 'Education Level', type: 'categorical', placeholder: 'e.g. 10th,12th,Graduate' },
  { value: 'category', label: 'Social Category', type: 'categorical', placeholder: 'e.g. General,OBC,SC,ST' },
  { value: 'disabilityStatus', label: 'Person with Disability (PwD)', type: 'boolean', placeholder: 'true or false' },
  { value: 'disabilityPercentage', label: 'Disability Percentage (%)', type: 'numeric', placeholder: 'e.g. 40' },
  { value: 'disabilityType', label: 'Disability Type', type: 'categorical', placeholder: 'e.g. Locomotor,Visual' },
  { value: 'landOwnership', label: 'Land Ownership', type: 'boolean', placeholder: 'true or false' },
  { value: 'landArea', label: 'Agricultural Land Area (Acres)', type: 'numeric', placeholder: 'e.g. 5.0' },
  { value: 'employmentStatus', label: 'Employment Status', type: 'categorical', placeholder: 'e.g. Unemployed,SelfEmployed' },
  { value: 'residenceType', label: 'Residence (Rural / Urban)', type: 'categorical', placeholder: 'e.g. Rural,Urban' },
  { value: 'maritalStatus', label: 'Marital Status', type: 'categorical', placeholder: 'e.g. Single,Married,Widow' },
  { value: 'familySize', label: 'Family Size', type: 'numeric', placeholder: 'e.g. 4' },
  { value: 'bplStatus', label: 'Below Poverty Line (BPL)', type: 'boolean', placeholder: 'true or false' },
  { value: 'rationCardType', label: 'Ration Card Type', type: 'categorical', placeholder: 'e.g. AAY,PHH,NPHH' },
  { value: 'minorityStatus', label: 'Minority Community', type: 'boolean', placeholder: 'true or false' },
  { value: 'exServiceman', label: 'Ex-Serviceman / Defense', type: 'boolean', placeholder: 'true or false' }
];

const OPERATOR_OPTIONS = [
  { value: 'EQ', label: 'Equal To (==)' },
  { value: 'NEQ', label: 'Not Equal (!=)' },
  { value: 'LTE', label: 'Less Than or Equal (≤)' },
  { value: 'LT', label: 'Less Than (<)' },
  { value: 'GTE', label: 'Greater Than or Equal (≥)' },
  { value: 'GT', label: 'Greater Than (>)' },
  { value: 'BETWEEN', label: 'Between Range (min:max)' },
  { value: 'IN', label: 'One Of List (comma separated)' },
  { value: 'NOT_IN', label: 'Not In List (comma separated)' },
  { value: 'BOOL', label: 'Boolean Flag (Yes/No)' },
  { value: 'EXISTS', label: 'Field Must Exist' }
];

export default function AdminSchemesPage() {
  const { user } = useAuth();

  // State
  const [schemes, setSchemes] = useState([]);
  const [stats, setStats] = useState(null);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Filters
  const [search, setSearch] = useState('');
  const [filterPubStatus, setFilterPubStatus] = useState('ALL');
  const [filterVerStatus, setFilterVerStatus] = useState('ALL');
  const [filterCategory, setFilterCategory] = useState('ALL');
  const [filterScope, setFilterScope] = useState('ALL');

  // Modals
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [currentScheme, setCurrentScheme] = useState(null);
  const [verifyModalOpen, setVerifyModalOpen] = useState(false);
  const [schemeToVerify, setSchemeToVerify] = useState(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [schemeToDelete, setSchemeToDelete] = useState(null);

  // Form states
  const [formData, setFormData] = useState(getInitialFormData());
  const [verifyForm, setVerifyForm] = useState(() => ({
    sourceUrl: '',
    verificationStatus: 'VERIFIED',
    verificationNotes: '',
    verificationDate: getTodayDateString()
  }));

  function getInitialFormData() {
    return {
      name: '',
      scope: 'CENTRAL',
      state: '',
      benefitCategory: 'Agriculture',
      governmentAuthority: '',
      description: '',
      objective: '',
      benefits: '',
      applicationProcess: '',
      officialUrl: '',
      applicationUrl: '',
      source: '',
      sourceUrl: '',
      publicationStatus: 'DRAFT',
      verificationStatus: 'UNVERIFIED',
      verificationNotes: '',
      verificationDate: '',
      deadline: '',
      importantWarnings: '',
      commonRejectionReasons: '',
      active: true,
      eligibilityRules: [],
      requiredDocuments: []
    };
  }

  // Fetch initial data
  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setErrorMsg(null);

      const [schemesRes, statsRes, catRes] = await Promise.all([
        adminApi.getSchemes({
          search: search || undefined,
          publicationStatus: filterPubStatus !== 'ALL' ? filterPubStatus : undefined,
          verificationStatus: filterVerStatus !== 'ALL' ? filterVerStatus : undefined,
          category: filterCategory !== 'ALL' ? filterCategory : undefined,
          scope: filterScope !== 'ALL' ? filterScope : undefined
        }),
        adminApi.getStats(),
        schemesApi.getCategories().catch(() => ({ data: { data: [] } }))
      ]);

      if (schemesRes.data?.success) {
        setSchemes(schemesRes.data.data || []);
      }
      if (statsRes.data?.success) {
        setStats(statsRes.data.data);
      }
      if (catRes.data?.success) {
        setCategories(catRes.data.data || []);
      }
    } catch (err) {
      console.error('Failed to load admin schemes:', err);
      setErrorMsg(err.response?.data?.message || 'Failed to load schemes. Please verify admin privileges.');
    } finally {
      setLoading(false);
    }
  }, [search, filterPubStatus, filterVerStatus, filterCategory, filterScope]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Handle Search submit
  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadData();
  };

  // Open Create Modal
  const handleOpenCreate = () => {
    setCurrentScheme(null);
    setFormData(getInitialFormData());
    setEditModalOpen(true);
    setErrorMsg(null);
    setSuccessMsg(null);
  };

  // Open Edit Modal
  const handleOpenEdit = async (scheme) => {
    try {
      setActionLoading(true);
      const res = await adminApi.getSchemeById(scheme.id);
      if (res.data?.success) {
        const fullScheme = res.data.data;
        setCurrentScheme(fullScheme);
        setFormData({
          ...fullScheme,
          eligibilityRules: fullScheme.eligibilityRules || [],
          requiredDocuments: fullScheme.requiredDocuments || []
        });
        setEditModalOpen(true);
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to load scheme details');
    } finally {
      setActionLoading(false);
    }
  };

  // Open Verification Modal
  const handleOpenVerify = (scheme) => {
    setSchemeToVerify(scheme);
    setVerifyForm({
      sourceUrl: scheme.sourceUrl || scheme.officialUrl || '',
      verificationStatus: scheme.verificationStatus || 'VERIFIED',
      verificationNotes: scheme.verificationNotes || '',
      verificationDate: scheme.verificationDate || getTodayDateString()
    });
    setVerifyModalOpen(true);
    setErrorMsg(null);
  };

  // Submit Verification Workflow
  const handleVerifySubmit = async (e) => {
    e.preventDefault();
    try {
      setActionLoading(true);
      setErrorMsg(null);
      const res = await adminApi.verifyScheme(schemeToVerify.id, verifyForm);
      if (res.data?.success) {
        setSuccessMsg(`Source verification updated for "${schemeToVerify.name}"`);
        setVerifyModalOpen(false);
        await loadData();
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Verification update failed');
    } finally {
      setActionLoading(false);
    }
  };

  // Publish Scheme
  const handlePublish = async (scheme) => {
    try {
      setActionLoading(true);
      setErrorMsg(null);
      setSuccessMsg(null);
      const res = await adminApi.publishScheme(scheme.id);
      if (res.data?.success) {
        setSuccessMsg(`Scheme "${scheme.name}" published successfully. It is now live in the citizen catalog.`);
        await loadData();
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to publish scheme');
    } finally {
      setActionLoading(false);
    }
  };

  // Archive Scheme
  const handleArchive = async (scheme) => {
    try {
      setActionLoading(true);
      setErrorMsg(null);
      setSuccessMsg(null);
      const res = await adminApi.archiveScheme(scheme.id);
      if (res.data?.success) {
        setSuccessMsg(`Scheme "${scheme.name}" archived. It is no longer visible to citizens.`);
        await loadData();
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to archive scheme');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete Scheme
  const handleDeleteConfirm = async () => {
    if (!schemeToDelete) return;
    try {
      setActionLoading(true);
      setErrorMsg(null);
      const res = await adminApi.deleteScheme(schemeToDelete.id);
      if (res.data?.success) {
        setSuccessMsg(`Scheme "${schemeToDelete.name}" deleted successfully.`);
        setDeleteModalOpen(false);
        setSchemeToDelete(null);
        await loadData();
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to delete scheme');
    } finally {
      setActionLoading(false);
    }
  };

  // Save Create/Edit Scheme Form
  const handleSaveScheme = async (e, targetStatus) => {
    e.preventDefault();
    try {
      setActionLoading(true);
      setErrorMsg(null);

      const payload = {
        ...formData,
        publicationStatus: targetStatus || formData.publicationStatus
      };

      let res;
      if (currentScheme?.id) {
        res = await adminApi.updateScheme(currentScheme.id, payload);
      } else {
        res = await adminApi.createScheme(payload);
      }

      if (res.data?.success) {
        setSuccessMsg(currentScheme ? 'Scheme updated successfully' : 'Scheme created successfully');
        setEditModalOpen(false);
        await loadData();
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Operation failed. Check all field requirements.');
    } finally {
      setActionLoading(false);
    }
  };

  // Rule Builders
  const handleAddRule = () => {
    setFormData(prev => ({
      ...prev,
      eligibilityRules: [
        ...prev.eligibilityRules,
        {
          criterionField: 'age',
          operator: 'LTE',
          value: '',
          displayLabel: '',
          description: '',
          mandatory: true,
          priority: 0
        }
      ]
    }));
  };

  const handleRemoveRule = (index) => {
    setFormData(prev => ({
      ...prev,
      eligibilityRules: prev.eligibilityRules.filter((_, i) => i !== index)
    }));
  };

  const handleRuleChange = (index, field, value) => {
    setFormData(prev => {
      const updated = [...prev.eligibilityRules];
      updated[index] = { ...updated[index], [field]: value };
      return { ...prev, eligibilityRules: updated };
    });
  };

  // Document Builders
  const handleAddDoc = () => {
    setFormData(prev => ({
      ...prev,
      requiredDocuments: [
        ...prev.requiredDocuments,
        {
          documentName: '',
          description: '',
          mandatory: true,
          alternatives: ''
        }
      ]
    }));
  };

  const handleRemoveDoc = (index) => {
    setFormData(prev => ({
      ...prev,
      requiredDocuments: prev.requiredDocuments.filter((_, i) => i !== index)
    }));
  };

  const handleDocChange = (index, field, value) => {
    setFormData(prev => {
      const updated = [...prev.requiredDocuments];
      updated[index] = { ...updated[index], [field]: value };
      return { ...prev, requiredDocuments: updated };
    });
  };

  return (
    <div style={{ padding: '40px 0 80px 0', minHeight: 'calc(100vh - 150px)' }}>
      <div className="container">
        
        {/* Page Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '20px', marginBottom: '32px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <div style={{
                background: 'rgba(249, 115, 22, 0.15)',
                color: 'var(--primary)',
                padding: '6px 12px',
                borderRadius: '8px',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                fontSize: '0.85rem',
                fontWeight: 700
              }}>
                <ShieldCheck size={18} />
                ADMINISTRATOR ACCESS
              </div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-dim)' }}>
                Authenticated as: <strong>{user?.fullName || user?.email}</strong>
              </span>
            </div>
            <h1 style={{ fontSize: '2.2rem', fontWeight: 800, letterSpacing: '-0.5px' }}>
              Scheme Management & Verification
            </h1>
            <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '750px', marginTop: '6px' }}>
              Maintain official government scheme facts, verify primary statutory sources, configure deterministic eligibility rules, and govern publication status for citizens.
            </p>
          </div>

          <div style={{ display: 'flex', gap: '12px' }}>
            <button 
              onClick={loadData} 
              className="btn btn-secondary"
              style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
              disabled={loading || actionLoading}
            >
              <RefreshCw size={16} className={loading ? 'spin' : ''} />
              Refresh
            </button>
            <button 
              onClick={handleOpenCreate} 
              className="btn btn-primary"
              style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
            >
              <Plus size={18} />
              New Scheme
            </button>
          </div>
        </div>

        {/* Success & Error Notifications */}
        {errorMsg && (
          <div style={{
            background: 'rgba(239, 68, 68, 0.15)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            borderRadius: '12px',
            padding: '16px 20px',
            color: '#fca5a5',
            marginBottom: '24px',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '12px'
          }}>
            <AlertCircle size={20} style={{ flexShrink: 0, marginTop: '2px' }} />
            <div style={{ flex: 1 }}>
              <strong style={{ display: 'block', fontSize: '0.95rem' }}>Action Prevented / Validation Error</strong>
              <span style={{ fontSize: '0.9rem', whiteSpace: 'pre-line' }}>{errorMsg}</span>
            </div>
            <button onClick={() => setErrorMsg(null)} style={{ background: 'transparent', color: '#fca5a5' }}>
              <X size={18} />
            </button>
          </div>
        )}

        {successMsg && (
          <div style={{
            background: 'rgba(16, 185, 129, 0.15)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            borderRadius: '12px',
            padding: '16px 20px',
            color: '#86efac',
            marginBottom: '24px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px'
          }}>
            <CheckCircle size={20} />
            <span style={{ flex: 1, fontSize: '0.95rem' }}>{successMsg}</span>
            <button onClick={() => setSuccessMsg(null)} style={{ background: 'transparent', color: '#86efac' }}>
              <X size={18} />
            </button>
          </div>
        )}

        {/* Dashboard Metrics Banner */}
        {stats && (
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
            gap: '16px',
            marginBottom: '32px'
          }}>
            <div className="card" style={{ padding: '20px', background: 'var(--bg-card)', border: '1px solid var(--border-subtle)' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-dim)', marginBottom: '8px' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Total Schemes</span>
                <Layers size={18} color="var(--primary)" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-main)' }}>{stats.totalSchemes}</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>In database</span>
            </div>

            <div className="card" style={{ padding: '20px', background: 'rgba(16, 185, 129, 0.05)', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: '#86efac', marginBottom: '8px' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Published</span>
                <CheckCircle size={18} color="#10b981" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#10b981' }}>{stats.publishedSchemes}</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Live to citizens</span>
            </div>

            <div className="card" style={{ padding: '20px', background: 'rgba(249, 115, 22, 0.05)', border: '1px solid rgba(249, 115, 22, 0.2)' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: '#fdba74', marginBottom: '8px' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Drafts</span>
                <FileEdit size={18} color="#f97316" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#f97316' }}>{stats.draftSchemes}</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Unpublished work</span>
            </div>

            <div className="card" style={{ padding: '20px', background: 'rgba(6, 182, 212, 0.05)', border: '1px solid rgba(6, 182, 212, 0.2)' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: '#67e8f9', marginBottom: '8px' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Source Verified</span>
                <ShieldCheck size={18} color="#06b6d4" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#06b6d4' }}>{stats.verifiedSchemes}</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Official gazette audited</span>
            </div>

            <div className="card" style={{ padding: '20px', background: 'rgba(245, 158, 11, 0.05)', border: '1px solid rgba(245, 158, 11, 0.2)' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: '#fcd34d', marginBottom: '8px' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Needs Review</span>
                <AlertTriangle size={18} color="#f59e0b" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#f59e0b' }}>{stats.needsReviewSchemes}</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Pending re-audit</span>
            </div>

            <div className="card" style={{ padding: '20px', background: 'rgba(100, 116, 139, 0.05)', border: '1px solid rgba(100, 116, 139, 0.2)' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-dim)', marginBottom: '8px' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Archived</span>
                <Archive size={18} color="#94a3b8" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#94a3b8' }}>{stats.archivedSchemes}</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Retired schemes</span>
            </div>
          </div>
        )}

        {/* Filter and Search Bar */}
        <div className="card" style={{ padding: '20px', marginBottom: '28px', background: 'var(--bg-card)' }}>
          <form onSubmit={handleSearchSubmit} style={{ display: 'flex', flexWrap: 'wrap', gap: '14px', alignItems: 'center' }}>
            <div style={{ flex: '2 1 250px', position: 'relative' }}>
              <Search size={18} color="var(--text-dim)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
              <input
                type="text"
                placeholder="Search scheme name, description, authority..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{ width: '100%', paddingLeft: '38px' }}
              />
            </div>

            <div style={{ flex: '1 1 140px' }}>
              <select 
                value={filterPubStatus} 
                onChange={(e) => setFilterPubStatus(e.target.value)}
                style={{ width: '100%' }}
              >
                <option value="ALL">All Publications</option>
                <option value="PUBLISHED">Published</option>
                <option value="DRAFT">Draft</option>
                <option value="ARCHIVED">Archived</option>
              </select>
            </div>

            <div style={{ flex: '1 1 140px' }}>
              <select 
                value={filterVerStatus} 
                onChange={(e) => setFilterVerStatus(e.target.value)}
                style={{ width: '100%' }}
              >
                <option value="ALL">All Verifications</option>
                <option value="VERIFIED">Verified</option>
                <option value="NEEDS_REVIEW">Needs Review</option>
                <option value="UNVERIFIED">Unverified</option>
              </select>
            </div>

            <div style={{ flex: '1 1 140px' }}>
              <select 
                value={filterCategory} 
                onChange={(e) => setFilterCategory(e.target.value)}
                style={{ width: '100%' }}
              >
                <option value="ALL">All Categories</option>
                {categories.map(c => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>

            <div style={{ flex: '1 1 120px' }}>
              <select 
                value={filterScope} 
                onChange={(e) => setFilterScope(e.target.value)}
                style={{ width: '100%' }}
              >
                <option value="ALL">All Scopes</option>
                <option value="CENTRAL">Central</option>
                <option value="STATE">State</option>
              </select>
            </div>

            <button type="submit" className="btn btn-secondary" style={{ padding: '10px 18px' }}>
              Filter
            </button>
          </form>
        </div>

        {/* Schemes List */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '60px 0' }}>
            <RefreshCw size={36} className="spin" color="var(--primary)" style={{ margin: '0 auto 16px' }} />
            <p style={{ color: 'var(--text-muted)' }}>Loading government schemes and verification records...</p>
          </div>
        ) : schemes.length === 0 ? (
          <div className="card" style={{ textAlign: 'center', padding: '60px 20px', background: 'var(--bg-card)' }}>
            <BookOpen size={48} color="var(--text-dim)" style={{ margin: '0 auto 16px' }} />
            <h3 style={{ fontSize: '1.2rem', marginBottom: '8px' }}>No schemes matched your criteria</h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '20px' }}>Try resetting the filters or create a new scheme record.</p>
            <button onClick={handleOpenCreate} className="btn btn-primary">
              <Plus size={16} style={{ marginRight: '6px' }} /> Create Scheme
            </button>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {schemes.map((scheme) => (
              <div 
                key={scheme.id} 
                className="card"
                style={{
                  padding: '24px',
                  background: 'var(--bg-card)',
                  border: scheme.publicationStatus === 'PUBLISHED' 
                    ? '1px solid rgba(16, 185, 129, 0.25)' 
                    : scheme.publicationStatus === 'DRAFT'
                    ? '1px solid rgba(249, 115, 22, 0.25)'
                    : '1px solid var(--border-subtle)',
                  borderRadius: '16px'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
                  
                  {/* Scheme Overview */}
                  <div style={{ flex: '1 1 500px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginBottom: '10px' }}>
                      {/* Publication Status Badge */}
                      <span style={{
                        padding: '4px 10px',
                        borderRadius: '6px',
                        fontSize: '0.75rem',
                        fontWeight: 700,
                        letterSpacing: '0.5px',
                        background: scheme.publicationStatus === 'PUBLISHED' 
                          ? 'rgba(16, 185, 129, 0.15)' 
                          : scheme.publicationStatus === 'DRAFT'
                          ? 'rgba(249, 115, 22, 0.15)'
                          : 'rgba(100, 116, 139, 0.15)',
                        color: scheme.publicationStatus === 'PUBLISHED'
                          ? '#10b981'
                          : scheme.publicationStatus === 'DRAFT'
                          ? '#f97316'
                          : '#94a3b8',
                        border: `1px solid ${
                          scheme.publicationStatus === 'PUBLISHED' 
                            ? 'rgba(16, 185, 129, 0.3)' 
                            : scheme.publicationStatus === 'DRAFT'
                            ? 'rgba(249, 115, 22, 0.3)'
                            : 'rgba(100, 116, 139, 0.3)'
                        }`
                      }}>
                        {scheme.publicationStatus}
                      </span>

                      {/* Verification Status Badge */}
                      <span style={{
                        padding: '4px 10px',
                        borderRadius: '6px',
                        fontSize: '0.75rem',
                        fontWeight: 700,
                        letterSpacing: '0.5px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                        background: scheme.verificationStatus === 'VERIFIED'
                          ? 'rgba(6, 182, 212, 0.15)'
                          : scheme.verificationStatus === 'NEEDS_REVIEW'
                          ? 'rgba(245, 158, 11, 0.15)'
                          : 'rgba(239, 68, 68, 0.15)',
                        color: scheme.verificationStatus === 'VERIFIED'
                          ? '#06b6d4'
                          : scheme.verificationStatus === 'NEEDS_REVIEW'
                          ? '#f59e0b'
                          : '#ef4444',
                        border: `1px solid ${
                          scheme.verificationStatus === 'VERIFIED'
                            ? 'rgba(6, 182, 212, 0.3)'
                            : scheme.verificationStatus === 'NEEDS_REVIEW'
                            ? 'rgba(245, 158, 11, 0.3)'
                            : 'rgba(239, 68, 68, 0.3)'
                        }`
                      }}>
                        {scheme.verificationStatus === 'VERIFIED' ? <CheckCircle size={12} /> : <AlertTriangle size={12} />}
                        {scheme.verificationStatus}
                      </span>

                      {/* Category & Scope */}
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', background: 'rgba(255,255,255,0.05)', padding: '4px 8px', borderRadius: '4px' }}>
                        {scheme.scope} {scheme.state ? `(${scheme.state})` : ''}
                      </span>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', background: 'rgba(255,255,255,0.05)', padding: '4px 8px', borderRadius: '4px' }}>
                        {scheme.benefitCategory}
                      </span>
                    </div>

                    <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '6px' }}>
                      {scheme.name}
                    </h3>

                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '14px', lineHeight: 1.5 }}>
                      {scheme.description}
                    </p>

                    {/* Metadata & Source Links */}
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '18px', fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                      <div>
                        <strong>Authority:</strong> {scheme.governmentAuthority || 'Not specified'}
                      </div>

                      <div>
                        <strong>Official Source:</strong>{' '}
                        {scheme.sourceUrl ? (
                          <a 
                            href={scheme.sourceUrl} 
                            target="_blank" 
                            rel="noopener noreferrer" 
                            style={{ color: '#06b6d4', textDecoration: 'underline', display: 'inline-flex', alignItems: 'center', gap: '3px' }}
                          >
                            Gazette / Ministry Portal <ExternalLink size={12} />
                          </a>
                        ) : (
                          <span style={{ color: '#ef4444' }}>Missing Source URL</span>
                        )}
                      </div>

                      <div>
                        <strong>Last Checked:</strong>{' '}
                        {scheme.verificationDate ? (
                          <span>{scheme.verificationDate}</span>
                        ) : (
                          <span style={{ color: '#f59e0b' }}>Not verified</span>
                        )}
                      </div>

                      <div>
                        <strong>Rules & Docs:</strong>{' '}
                        <span>{scheme.eligibilityRules?.length || 0} eligibility rules, {scheme.requiredDocuments?.length || 0} documents</span>
                      </div>
                    </div>

                    {scheme.verificationNotes && (
                      <div style={{ marginTop: '10px', fontSize: '0.8rem', color: 'var(--text-dim)', background: 'rgba(255,255,255,0.03)', padding: '8px 12px', borderRadius: '6px' }}>
                        <strong>Review Note:</strong> {scheme.verificationNotes}
                      </div>
                    )}
                  </div>

                  {/* Action Buttons */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', minWidth: '150px' }}>
                    <button 
                      onClick={() => handleOpenVerify(scheme)}
                      className="btn btn-secondary btn-sm"
                      style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
                      title="Record official gazette or portal verification"
                    >
                      <ShieldCheck size={14} color="#06b6d4" />
                      Verify Source
                    </button>

                    <button 
                      onClick={() => handleOpenEdit(scheme)}
                      className="btn btn-secondary btn-sm"
                      style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
                    >
                      <Edit3 size={14} />
                      Edit Details
                    </button>

                    {scheme.publicationStatus !== 'PUBLISHED' ? (
                      <button 
                        onClick={() => handlePublish(scheme)}
                        className="btn btn-sm"
                        style={{ 
                          display: 'flex', 
                          alignItems: 'center', 
                          justifyContent: 'center', 
                          gap: '6px',
                          background: 'rgba(16, 185, 129, 0.2)',
                          color: '#10b981',
                          border: '1px solid rgba(16, 185, 129, 0.4)'
                        }}
                        title="Publish scheme after quality checks"
                      >
                        <UploadCloud size={14} />
                        Publish Live
                      </button>
                    ) : (
                      <button 
                        onClick={() => handleArchive(scheme)}
                        className="btn btn-sm"
                        style={{ 
                          display: 'flex', 
                          alignItems: 'center', 
                          justifyContent: 'center', 
                          gap: '6px',
                          background: 'rgba(100, 116, 139, 0.2)',
                          color: '#94a3b8',
                          border: '1px solid rgba(100, 116, 139, 0.4)'
                        }}
                        title="Archive and hide scheme from public citizens"
                      >
                        <Archive size={14} />
                        Archive
                      </button>
                    )}

                    <button 
                      onClick={() => { setSchemeToDelete(scheme); setDeleteModalOpen(true); }}
                      className="btn btn-sm"
                      style={{ 
                        display: 'flex', 
                        alignItems: 'center', 
                        justifyContent: 'center', 
                        gap: '6px',
                        background: 'rgba(239, 68, 68, 0.1)',
                        color: '#ef4444',
                        border: '1px solid rgba(239, 68, 68, 0.2)'
                      }}
                      title="Permanently remove scheme record"
                    >
                      <Trash2 size={14} />
                      Delete
                    </button>
                  </div>

                </div>
              </div>
            ))}
          </div>
        )}

      </div>

      {/* CREATE / EDIT MODAL */}
      {editModalOpen && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(10, 14, 23, 0.85)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '20px'
        }}>
          <div style={{
            background: '#121a2b',
            border: '1px solid var(--border-subtle)',
            borderRadius: '16px',
            width: '100%',
            maxWidth: '900px',
            maxHeight: '90vh',
            display: 'flex',
            flexDirection: 'column',
            boxShadow: 'var(--shadow-lg)'
          }}>
            {/* Modal Header */}
            <div style={{
              padding: '20px 24px',
              borderBottom: '1px solid var(--border-subtle)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <div>
                <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>
                  {currentScheme ? 'Edit Government Scheme Record' : 'Create New Government Scheme Record'}
                </h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)' }}>
                  Accurate, statutory facts verified against competent authority notifications.
                </p>
              </div>
              <button 
                onClick={() => setEditModalOpen(false)}
                style={{ background: 'transparent', color: 'var(--text-dim)', padding: '6px' }}
              >
                <X size={20} />
              </button>
            </div>

            {/* Modal Scrollable Body */}
            <div style={{ padding: '24px', overflowY: 'auto', flex: 1 }}>
              <form id="scheme-form" onSubmit={(e) => handleSaveScheme(e)}>
                
                {/* 1. Identification */}
                <h4 style={{ fontSize: '1.05rem', color: 'var(--primary)', marginBottom: '14px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '6px' }}>
                  1. Scheme Identification & Classification
                </h4>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px', marginBottom: '20px' }}>
                  <div style={{ gridColumn: '1 / -1' }}>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Official Scheme Name *
                    </label>
                    <input
                      type="text"
                      required
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      placeholder="e.g. PM-KISAN (Pradhan Mantri Kisan Samman Nidhi)"
                      style={{ width: '100%' }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Scope *
                    </label>
                    <select
                      value={formData.scope}
                      onChange={(e) => setFormData({ ...formData, scope: e.target.value })}
                      style={{ width: '100%' }}
                    >
                      <option value="CENTRAL">Central Government</option>
                      <option value="STATE">State Government</option>
                    </select>
                  </div>

                  {formData.scope === 'STATE' && (
                    <div>
                      <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                        Applicable State *
                      </label>
                      <input
                        type="text"
                        required
                        value={formData.state || ''}
                        onChange={(e) => setFormData({ ...formData, state: e.target.value })}
                        placeholder="e.g. Telangana, Andhra Pradesh"
                        style={{ width: '100%' }}
                      />
                    </div>
                  )}

                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Benefit Category *
                    </label>
                    <select
                      value={formData.benefitCategory}
                      onChange={(e) => setFormData({ ...formData, benefitCategory: e.target.value })}
                      style={{ width: '100%' }}
                    >
                      <option value="Agriculture">Agriculture</option>
                      <option value="Education">Education</option>
                      <option value="Health">Health</option>
                      <option value="Housing">Housing</option>
                      <option value="Employment">Employment</option>
                      <option value="Social Security">Social Security</option>
                      <option value="Women Empowerment">Women Empowerment</option>
                      <option value="Insurance">Insurance</option>
                    </select>
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Government Authority / Ministry
                    </label>
                    <input
                      type="text"
                      value={formData.governmentAuthority || ''}
                      onChange={(e) => setFormData({ ...formData, governmentAuthority: e.target.value })}
                      placeholder="e.g. Ministry of Agriculture & Farmers Welfare"
                      style={{ width: '100%' }}
                    />
                  </div>
                </div>

                {/* 2. Descriptions & Assistance */}
                <h4 style={{ fontSize: '1.05rem', color: 'var(--primary)', marginBottom: '14px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '6px' }}>
                  2. Description, Assistance & Procedure
                </h4>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '16px', marginBottom: '20px' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Brief Description *
                    </label>
                    <textarea
                      required
                      rows={3}
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      placeholder="Concise, factual description of who this scheme serves and its core provision..."
                      style={{ width: '100%' }}
                    />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>
                    <div>
                      <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                        Statutory Benefits
                      </label>
                      <textarea
                        rows={3}
                        value={formData.benefits || ''}
                        onChange={(e) => setFormData({ ...formData, benefits: e.target.value })}
                        placeholder="Exact financial assistance or in-kind entitlement..."
                        style={{ width: '100%' }}
                      />
                    </div>

                    <div>
                      <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                        Application Procedure Steps
                      </label>
                      <textarea
                        rows={3}
                        value={formData.applicationProcess || ''}
                        onChange={(e) => setFormData({ ...formData, applicationProcess: e.target.value })}
                        placeholder="1. Visit official portal\n2. Fill application\n3. Verification by Nodal Officer..."
                        style={{ width: '100%' }}
                      />
                    </div>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>
                    <div>
                      <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                        Important Warnings & Eligibility Exclusions
                      </label>
                      <textarea
                        rows={2}
                        value={formData.importantWarnings || ''}
                        onChange={(e) => setFormData({ ...formData, importantWarnings: e.target.value })}
                        placeholder="e.g. Income tax payers are strictly excluded..."
                        style={{ width: '100%' }}
                      />
                    </div>

                    <div>
                      <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                        Common Rejection Reasons
                      </label>
                      <textarea
                        rows={2}
                        value={formData.commonRejectionReasons || ''}
                        onChange={(e) => setFormData({ ...formData, commonRejectionReasons: e.target.value })}
                        placeholder="e.g. Bank account not Aadhaar-linked..."
                        style={{ width: '100%' }}
                      />
                    </div>
                  </div>
                </div>

                {/* 3. Official Source Links */}
                <h4 style={{ fontSize: '1.05rem', color: '#06b6d4', marginBottom: '14px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '6px' }}>
                  3. Official Source Metadata & Government Verification
                </h4>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px', marginBottom: '20px' }}>
                  <div style={{ gridColumn: '1 / -1' }}>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Official Source URL (Gazette / Ministry Portal)
                    </label>
                    <input
                      type="url"
                      value={formData.sourceUrl || ''}
                      onChange={(e) => setFormData({ ...formData, sourceUrl: e.target.value })}
                      placeholder="https://pmkisan.gov.in/ or https://egazette.gov.in/..."
                      style={{ width: '100%' }}
                    />
                    <small style={{ color: 'var(--text-dim)', fontSize: '0.75rem', marginTop: '4px', display: 'block' }}>
                      Must be a valid HTTP/HTTPS link to official government portal or notification.
                    </small>
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Official Scheme Website
                    </label>
                    <input
                      type="url"
                      value={formData.officialUrl || ''}
                      onChange={(e) => setFormData({ ...formData, officialUrl: e.target.value })}
                      placeholder="https://..."
                      style={{ width: '100%' }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Online Application Portal URL
                    </label>
                    <input
                      type="url"
                      value={formData.applicationUrl || ''}
                      onChange={(e) => setFormData({ ...formData, applicationUrl: e.target.value })}
                      placeholder="https://..."
                      style={{ width: '100%' }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Source Verification Status
                    </label>
                    <select
                      value={formData.verificationStatus}
                      onChange={(e) => setFormData({ ...formData, verificationStatus: e.target.value })}
                      style={{ width: '100%' }}
                    >
                      <option value="UNVERIFIED">UNVERIFIED (Draft / Pending Audit)</option>
                      <option value="VERIFIED">VERIFIED (Reviewed against Gazette)</option>
                      <option value="NEEDS_REVIEW">NEEDS_REVIEW (Conflicting / Needs Audit)</option>
                    </select>
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Verification Date
                    </label>
                    <input
                      type="date"
                      value={formData.verificationDate || ''}
                      onChange={(e) => setFormData({ ...formData, verificationDate: e.target.value })}
                      style={{ width: '100%' }}
                    />
                  </div>

                  <div style={{ gridColumn: '1 / -1' }}>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                      Verification Notes / Gazette Reference
                    </label>
                    <input
                      type="text"
                      value={formData.verificationNotes || ''}
                      onChange={(e) => setFormData({ ...formData, verificationNotes: e.target.value })}
                      placeholder="e.g. Verified against Gazette of India notification CG-DL-E-14082024-492..."
                      style={{ width: '100%' }}
                    />
                  </div>
                </div>

                {/* 4. Structured Eligibility Rules */}
                <div style={{ marginBottom: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <div>
                      <h4 style={{ fontSize: '1.05rem', color: '#10b981' }}>
                        4. Structured Eligibility Rules (Deterministic Rule Engine)
                      </h4>
                      <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                        Rules are strictly validated and evaluated deterministically by the eligibility engine.
                      </p>
                    </div>
                    <button 
                      type="button" 
                      onClick={handleAddRule}
                      className="btn btn-secondary btn-sm"
                      style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                      <Plus size={14} /> Add Criterion Rule
                    </button>
                  </div>

                  {formData.eligibilityRules.length === 0 ? (
                    <div style={{ padding: '20px', background: 'rgba(255,255,255,0.02)', border: '1px dashed var(--border-subtle)', borderRadius: '10px', textAlign: 'center', color: 'var(--text-dim)', fontSize: '0.85rem' }}>
                      No eligibility rules configured. At least 1 rule is required to publish this scheme.
                    </div>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                      {formData.eligibilityRules.map((rule, idx) => (
                        <div 
                          key={idx}
                          style={{
                            background: 'rgba(255,255,255,0.03)',
                            border: '1px solid var(--border-subtle)',
                            borderRadius: '10px',
                            padding: '14px',
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr)) auto',
                            gap: '12px',
                            alignItems: 'center'
                          }}
                        >
                          <div>
                            <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-dim)', marginBottom: '4px' }}>
                              Field *
                            </label>
                            <select
                              value={rule.criterionField}
                              onChange={(e) => handleRuleChange(idx, 'criterionField', e.target.value)}
                              style={{ width: '100%', fontSize: '0.85rem', padding: '8px' }}
                            >
                              {FIELD_OPTIONS.map(f => (
                                <option key={f.value} value={f.value}>{f.label}</option>
                              ))}
                            </select>
                          </div>

                          <div>
                            <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-dim)', marginBottom: '4px' }}>
                              Operator *
                            </label>
                            <select
                              value={rule.operator}
                              onChange={(e) => handleRuleChange(idx, 'operator', e.target.value)}
                              style={{ width: '100%', fontSize: '0.85rem', padding: '8px' }}
                            >
                              {OPERATOR_OPTIONS.map(o => (
                                <option key={o.value} value={o.value}>{o.label}</option>
                              ))}
                            </select>
                          </div>

                          <div>
                            <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-dim)', marginBottom: '4px' }}>
                              Required Value *
                            </label>
                            <input
                              type="text"
                              value={rule.value || ''}
                              onChange={(e) => handleRuleChange(idx, 'value', e.target.value)}
                              placeholder={
                                rule.operator === 'BETWEEN' ? 'min:max (e.g. 18:60)' :
                                rule.operator === 'BOOL' ? 'true or false' :
                                rule.operator === 'IN' ? 'val1,val2' : 'value'
                              }
                              style={{ width: '100%', fontSize: '0.85rem', padding: '8px' }}
                            />
                          </div>

                          <div>
                            <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-dim)', marginBottom: '4px' }}>
                              Display Label
                            </label>
                            <input
                              type="text"
                              value={rule.displayLabel || ''}
                              onChange={(e) => handleRuleChange(idx, 'displayLabel', e.target.value)}
                              placeholder="e.g. Age Limit"
                              style={{ width: '100%', fontSize: '0.85rem', padding: '8px' }}
                            />
                          </div>

                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', paddingTop: '18px' }}>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem', cursor: 'pointer' }}>
                              <input
                                type="checkbox"
                                checked={rule.mandatory !== false}
                                onChange={(e) => handleRuleChange(idx, 'mandatory', e.target.checked)}
                              />
                              Mandatory
                            </label>

                            <button
                              type="button"
                              onClick={() => handleRemoveRule(idx)}
                              style={{ background: 'transparent', color: '#ef4444', padding: '6px' }}
                              title="Delete rule"
                            >
                              <Trash2 size={16} />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* 5. Required Documents Checklist */}
                <div style={{ marginBottom: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <h4 style={{ fontSize: '1.05rem', color: '#8b5cf6' }}>
                      5. Required Documents Checklist
                    </h4>
                    <button 
                      type="button" 
                      onClick={handleAddDoc}
                      className="btn btn-secondary btn-sm"
                      style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                      <Plus size={14} /> Add Document
                    </button>
                  </div>

                  {formData.requiredDocuments.length === 0 ? (
                    <div style={{ padding: '16px', background: 'rgba(255,255,255,0.02)', border: '1px dashed var(--border-subtle)', borderRadius: '10px', textAlign: 'center', color: 'var(--text-dim)', fontSize: '0.85rem' }}>
                      No documents configured yet.
                    </div>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                      {formData.requiredDocuments.map((doc, idx) => (
                        <div 
                          key={idx}
                          style={{
                            background: 'rgba(255,255,255,0.03)',
                            border: '1px solid var(--border-subtle)',
                            borderRadius: '10px',
                            padding: '14px',
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr)) auto',
                            gap: '12px',
                            alignItems: 'center'
                          }}
                        >
                          <div>
                            <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-dim)', marginBottom: '4px' }}>
                              Document Name *
                            </label>
                            <input
                              type="text"
                              value={doc.documentName}
                              onChange={(e) => handleDocChange(idx, 'documentName', e.target.value)}
                              placeholder="e.g. Aadhaar Card, Income Certificate"
                              style={{ width: '100%', fontSize: '0.85rem', padding: '8px' }}
                            />
                          </div>

                          <div>
                            <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-dim)', marginBottom: '4px' }}>
                              Acceptable Alternatives
                            </label>
                            <input
                              type="text"
                              value={doc.alternatives || ''}
                              onChange={(e) => handleDocChange(idx, 'alternatives', e.target.value)}
                              placeholder="e.g. Voter ID, Passport"
                              style={{ width: '100%', fontSize: '0.85rem', padding: '8px' }}
                            />
                          </div>

                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', paddingTop: '18px' }}>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem', cursor: 'pointer' }}>
                              <input
                                type="checkbox"
                                checked={doc.mandatory !== false}
                                onChange={(e) => handleDocChange(idx, 'mandatory', e.target.checked)}
                              />
                              Mandatory
                            </label>

                            <button
                              type="button"
                              onClick={() => handleRemoveDoc(idx)}
                              style={{ background: 'transparent', color: '#ef4444', padding: '6px' }}
                              title="Delete document"
                            >
                              <Trash2 size={16} />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

              </form>
            </div>

            {/* Modal Footer Controls */}
            <div style={{
              padding: '16px 24px',
              borderTop: '1px solid var(--border-subtle)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              background: 'rgba(0,0,0,0.2)'
            }}>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-dim)' }}>
                Current Status: <strong>{formData.publicationStatus}</strong> | Verification: <strong>{formData.verificationStatus}</strong>
              </div>

              <div style={{ display: 'flex', gap: '10px' }}>
                <button
                  type="button"
                  onClick={() => setEditModalOpen(false)}
                  className="btn btn-secondary"
                  disabled={actionLoading}
                >
                  Cancel
                </button>

                <button
                  type="button"
                  onClick={(e) => handleSaveScheme(e, 'DRAFT')}
                  className="btn btn-secondary"
                  disabled={actionLoading}
                >
                  Save as Draft
                </button>

                <button
                  type="button"
                  onClick={(e) => handleSaveScheme(e, 'PUBLISHED')}
                  className="btn btn-primary"
                  disabled={actionLoading}
                  style={{ background: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)' }}
                >
                  Save & Publish
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* SOURCE VERIFICATION WORKFLOW MODAL */}
      {verifyModalOpen && schemeToVerify && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(10, 14, 23, 0.85)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '20px'
        }}>
          <div style={{
            background: '#121a2b',
            border: '1px solid rgba(6, 182, 212, 0.3)',
            borderRadius: '16px',
            width: '100%',
            maxWidth: '560px',
            padding: '24px',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
              <div style={{ padding: '8px', background: 'rgba(6, 182, 212, 0.15)', borderRadius: '10px' }}>
                <ShieldCheck size={22} color="#06b6d4" />
              </div>
              <div>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 700 }}>Record Official Source Verification</h3>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                  Scheme: <strong>{schemeToVerify.name}</strong>
                </p>
              </div>
            </div>

            <form onSubmit={handleVerifySubmit}>
              <div style={{ marginBottom: '16px' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                  Official Source / Gazette URL *
                </label>
                <input
                  type="url"
                  required
                  value={verifyForm.sourceUrl}
                  onChange={(e) => setVerifyForm({ ...verifyForm, sourceUrl: e.target.value })}
                  placeholder="https://..."
                  style={{ width: '100%' }}
                />
                <small style={{ color: 'var(--text-dim)', fontSize: '0.75rem', marginTop: '4px', display: 'block' }}>
                  Primary authority portal or gazette notification establishing scheme facts.
                </small>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '16px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                    Verification Status *
                  </label>
                  <select
                    value={verifyForm.verificationStatus}
                    onChange={(e) => setVerifyForm({ ...verifyForm, verificationStatus: e.target.value })}
                    style={{ width: '100%' }}
                  >
                    <option value="VERIFIED">VERIFIED (Affirmed by review)</option>
                    <option value="NEEDS_REVIEW">NEEDS_REVIEW (Pending audit)</option>
                    <option value="UNVERIFIED">UNVERIFIED (Not yet checked)</option>
                  </select>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                    Review Date *
                  </label>
                  <input
                    type="date"
                    required
                    value={verifyForm.verificationDate}
                    onChange={(e) => setVerifyForm({ ...verifyForm, verificationDate: e.target.value })}
                    style={{ width: '100%' }}
                  />
                </div>
              </div>

              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                  Audit Review Notes
                </label>
                <textarea
                  rows={3}
                  value={verifyForm.verificationNotes}
                  onChange={(e) => setVerifyForm({ ...verifyForm, verificationNotes: e.target.value })}
                  placeholder="Document the exact notification number, order date, or official circular reviewed..."
                  style={{ width: '100%' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button
                  type="button"
                  onClick={() => setVerifyModalOpen(false)}
                  className="btn btn-secondary"
                  disabled={actionLoading}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={actionLoading}
                >
                  {actionLoading ? 'Recording...' : 'Affirm Verification'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {deleteModalOpen && schemeToDelete && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(10, 14, 23, 0.85)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '20px'
        }}>
          <div style={{
            background: '#121a2b',
            border: '1px solid rgba(239, 68, 68, 0.4)',
            borderRadius: '16px',
            width: '100%',
            maxWidth: '460px',
            padding: '24px',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px', color: '#ef4444' }}>
              <AlertCircle size={28} />
              <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Confirm Destructive Action</h3>
            </div>

            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginBottom: '16px', lineHeight: 1.5 }}>
              Are you sure you want to permanently delete <strong>"{schemeToDelete.name}"</strong>?
              This will remove the scheme and its associated rules and documents from the database.
            </p>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button
                type="button"
                onClick={() => { setDeleteModalOpen(false); setSchemeToDelete(null); }}
                className="btn btn-secondary"
                disabled={actionLoading}
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleDeleteConfirm}
                className="btn"
                style={{ background: '#ef4444', color: '#ffffff' }}
                disabled={actionLoading}
              >
                {actionLoading ? 'Deleting...' : 'Permanently Delete'}
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
