import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../AuthContext';
import { profileApi, schemesApi, householdApi, applicationsApi } from '../api';
import { Link, useSearchParams } from 'react-router-dom';
import { 
  User, 
  Bookmark, 
  CheckCircle2, 
  Sparkles, 
  Save,
  Trash2,
  ArrowRight,
  Users,
  Plus,
  Edit2,
  AlertCircle,
  X,
  Clock,
  Calendar,
  FileText,
  AlertTriangle,
  History,
  Info
} from 'lucide-react';
import { SchemeVerificationBadge } from '../components/SchemeVerificationBadge';

export default function ProfilePage() {
  const { user, profile, updateProfile } = useAuth();
  const [searchParams] = useSearchParams();

  const [activeTab, setActiveTab] = useState(
    searchParams.get('tab') === 'saved' ? 'SAVED' :
    searchParams.get('tab') === 'household' ? 'HOUSEHOLD' :
    searchParams.get('tab') === 'applications' ? 'APPLICATIONS' : 'PROFILE'
  );
  const [savedSchemes, setSavedSchemes] = useState([]);
  const [loadingSaved, setLoadingSaved] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Application Tracking State
  const [applications, setApplications] = useState([]);
  const [loadingApplications, setLoadingApplications] = useState(false);
  const [appSummary, setAppSummary] = useState(null);
  const [appStatusFilter, setAppStatusFilter] = useState('ALL');
  const [appApplicantFilter, setAppApplicantFilter] = useState('ALL');
  const [selectedApp, setSelectedApp] = useState(null);
  const [appModalMode, setAppModalMode] = useState('VIEW'); // 'VIEW' or 'UPDATE'
  const [updateAppForm, setUpdateAppForm] = useState({
    status: '',
    statusChangeNote: '',
    applicationReference: '',
    appliedDate: '',
    nextActionDate: '',
    nextActionNotes: '',
    notes: ''
  });
  const [updatingApp, setUpdatingApp] = useState(false);
  const [appMessage, setAppMessage] = useState(null); // { type: 'success' | 'error', text: '' }
  const [appToDelete, setAppToDelete] = useState(null);

  // Household Members State
  const [householdMembers, setHouseholdMembers] = useState([]);
  const [loadingHousehold, setLoadingHousehold] = useState(false);
  const [showMemberForm, setShowMemberForm] = useState(false);
  const [editingMemberId, setEditingMemberId] = useState(null);
  const [memberSaving, setMemberSaving] = useState(false);
  const [memberMessage, setMemberMessage] = useState(null); // { type: 'success' | 'error', text: '' }
  const [memberToDelete, setMemberToDelete] = useState(null);

  const initialMemberForm = {
    fullName: '',
    relationship: 'Spouse',
    age: '',
    gender: 'Female',
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
    residenceType: 'Rural',
    maritalStatus: 'Married',
    familySize: 4,
    bplStatus: false,
    rationCardType: 'APL',
    minorityStatus: false,
    exServiceman: false
  };

  const [memberFormData, setMemberFormData] = useState(initialMemberForm);

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

  const loadHouseholdMembers = useCallback(async () => {
    setLoadingHousehold(true);
    try {
      const res = await householdApi.getAll();
      if (res.data?.success) {
        setHouseholdMembers(res.data.data || []);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingHousehold(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'HOUSEHOLD' || activeTab === 'PROFILE') {
      loadHouseholdMembers();
    }
  }, [activeTab, loadHouseholdMembers]);

  const getStatusMeta = (status) => {
    switch (status) {
      case 'PLANNING':
        return { label: 'Planning to Apply', bg: 'rgba(245, 158, 11, 0.15)', color: '#fbbf24', border: 'rgba(245, 158, 11, 0.4)' };
      case 'READY_TO_APPLY':
        return { label: 'Ready to Apply', bg: 'rgba(14, 165, 233, 0.15)', color: '#38bdf8', border: 'rgba(14, 165, 233, 0.4)' };
      case 'SUBMITTED':
        return { label: 'Submitted', bg: 'rgba(59, 130, 246, 0.15)', color: '#60a5fa', border: 'rgba(59, 130, 246, 0.4)' };
      case 'UNDER_REVIEW':
        return { label: 'Under Review', bg: 'rgba(168, 85, 247, 0.15)', color: '#c084fc', border: 'rgba(168, 85, 247, 0.4)' };
      case 'ACTION_REQUIRED':
        return { label: 'Action Required', bg: 'rgba(244, 63, 94, 0.15)', color: '#fb7185', border: 'rgba(244, 63, 94, 0.4)' };
      case 'APPROVED':
        return { label: 'Approved', bg: 'rgba(16, 185, 129, 0.15)', color: '#34d399', border: 'rgba(16, 185, 129, 0.4)' };
      case 'REJECTED':
        return { label: 'Rejected', bg: 'rgba(225, 29, 72, 0.15)', color: '#f43f5e', border: 'rgba(225, 29, 72, 0.4)' };
      case 'COMPLETED':
        return { label: 'Completed', bg: 'rgba(20, 184, 166, 0.15)', color: '#2dd4bf', border: 'rgba(20, 184, 166, 0.4)' };
      case 'CANCELLED':
        return { label: 'Cancelled', bg: 'rgba(107, 114, 128, 0.15)', color: '#9ca3af', border: 'rgba(107, 114, 128, 0.4)' };
      default:
        return { label: status, bg: 'rgba(255, 255, 255, 0.08)', color: '#fff', border: 'var(--border-subtle)' };
    }
  };

  const getPermittedNextStatuses = (currentStatus) => {
    switch (currentStatus) {
      case 'PLANNING':
        return ['READY_TO_APPLY', 'SUBMITTED', 'CANCELLED'];
      case 'READY_TO_APPLY':
        return ['PLANNING', 'SUBMITTED', 'CANCELLED'];
      case 'SUBMITTED':
        return ['UNDER_REVIEW', 'ACTION_REQUIRED', 'APPROVED', 'REJECTED', 'CANCELLED'];
      case 'UNDER_REVIEW':
        return ['ACTION_REQUIRED', 'APPROVED', 'REJECTED', 'CANCELLED'];
      case 'ACTION_REQUIRED':
        return ['SUBMITTED', 'UNDER_REVIEW', 'REJECTED', 'CANCELLED'];
      case 'APPROVED':
        return ['COMPLETED', 'CANCELLED'];
      case 'REJECTED':
        return ['PLANNING', 'CANCELLED'];
      case 'CANCELLED':
        return ['PLANNING'];
      case 'COMPLETED':
      default:
        return [];
    }
  };

  const loadApplications = useCallback(async () => {
    setLoadingApplications(true);
    try {
      const [listRes, sumRes] = await Promise.all([
        applicationsApi.getAll(),
        applicationsApi.getSummary()
      ]);
      if (listRes.data?.success) {
        setApplications(listRes.data.data || []);
      }
      if (sumRes.data?.success) {
        setAppSummary(sumRes.data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingApplications(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'APPLICATIONS') {
      loadApplications();
      loadHouseholdMembers();
    }
  }, [activeTab, loadApplications, loadHouseholdMembers]);

  const handleOpenAppDetails = async (app, mode = 'VIEW') => {
    try {
      const res = await applicationsApi.getById(app.id);
      if (res.data?.success) {
        setSelectedApp(res.data.data);
      } else {
        setSelectedApp(app);
      }
    } catch {
      setSelectedApp(app);
    }
    setAppModalMode(mode);
    setUpdateAppForm({
      status: app.status || '',
      statusChangeNote: '',
      applicationReference: app.applicationReference || '',
      appliedDate: app.appliedDate || '',
      nextActionDate: app.nextActionDate || '',
      nextActionNotes: app.nextActionNotes || '',
      notes: app.notes || ''
    });
    setAppMessage(null);
  };

  const handleSaveAppUpdate = async (e) => {
    e.preventDefault();
    if (!selectedApp) return;
    setUpdatingApp(true);
    setAppMessage(null);

    try {
      const payload = {
        status: updateAppForm.status !== selectedApp.status ? updateAppForm.status : null,
        statusChangeNote: updateAppForm.statusChangeNote ? updateAppForm.statusChangeNote.trim() : null,
        applicationReference: updateAppForm.applicationReference ? updateAppForm.applicationReference.trim() : null,
        appliedDate: updateAppForm.appliedDate || null,
        nextActionDate: updateAppForm.nextActionDate || null,
        nextActionNotes: updateAppForm.nextActionNotes ? updateAppForm.nextActionNotes.trim() : null,
        notes: updateAppForm.notes ? updateAppForm.notes.trim() : null
      };

      const res = await applicationsApi.update(selectedApp.id, payload);
      if (res.data?.success) {
        const updated = res.data.data;
        setSelectedApp(updated);
        setApplications(prev => prev.map(a => a.id === updated.id ? updated : a));
        setAppMessage({ type: 'success', text: 'Application details updated successfully!' });
        loadApplications();
        setTimeout(() => setAppMessage(null), 3000);
      }
    } catch (err) {
      console.error(err);
      setAppMessage({ type: 'error', text: err.response?.data?.message || 'Failed to update application.' });
    } finally {
      setUpdatingApp(false);
    }
  };

  const handleDeleteApp = async () => {
    if (!appToDelete) return;
    try {
      const res = await applicationsApi.delete(appToDelete.id);
      if (res.data?.success) {
        setApplications(prev => prev.filter(a => a.id !== appToDelete.id));
        setAppToDelete(null);
        if (selectedApp?.id === appToDelete.id) {
          setSelectedApp(null);
        }
        loadApplications();
      }
    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message || 'Failed to delete application tracking record.');
    }
  };

  const handleOpenAddMember = () => {
    setMemberFormData({
      ...initialMemberForm,
      state: profile?.state || 'Telangana',
      district: profile?.district || '',
      category: profile?.category || 'General',
      residenceType: profile?.residenceType || 'Rural'
    });
    setEditingMemberId(null);
    setMemberMessage(null);
    setShowMemberForm(true);
  };

  const handleOpenEditMember = (member) => {
    setMemberFormData({
      fullName: member.fullName || '',
      relationship: member.relationship || 'Spouse',
      age: member.age ?? '',
      gender: member.gender || 'Female',
      state: member.state || 'Telangana',
      district: member.district || '',
      occupation: member.occupation || '',
      annualIncome: member.annualIncome ?? '',
      educationLevel: member.educationLevel || '10th',
      category: member.category || 'General',
      disabilityStatus: Boolean(member.disabilityStatus),
      disabilityType: member.disabilityType || '',
      disabilityPercentage: member.disabilityPercentage || 0,
      landOwnership: Boolean(member.landOwnership),
      landArea: member.landArea ?? '',
      employmentStatus: member.employmentStatus || 'Employed',
      residenceType: member.residenceType || 'Rural',
      maritalStatus: member.maritalStatus || 'Married',
      familySize: member.familySize || 4,
      bplStatus: Boolean(member.bplStatus),
      rationCardType: member.rationCardType || 'APL',
      minorityStatus: Boolean(member.minorityStatus),
      exServiceman: Boolean(member.exServiceman)
    });
    setEditingMemberId(member.id);
    setMemberMessage(null);
    setShowMemberForm(true);
  };

  const handleMemberFormChange = (e) => {
    const { name, value, type, checked } = e.target;
    setMemberFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSaveMember = async (e) => {
    e.preventDefault();
    if (!memberFormData.fullName.trim()) {
      setMemberMessage({ type: 'error', text: 'Member full name is required.' });
      return;
    }
    setMemberSaving(true);
    setMemberMessage(null);

    const payload = {
      ...memberFormData,
      age: memberFormData.age !== '' ? Number(memberFormData.age) : null,
      annualIncome: memberFormData.annualIncome !== '' ? Number(memberFormData.annualIncome) : null,
      landArea: memberFormData.landArea !== '' ? Number(memberFormData.landArea) : null,
      familySize: memberFormData.familySize !== '' ? Number(memberFormData.familySize) : null,
      disabilityPercentage: memberFormData.disabilityPercentage !== '' ? Number(memberFormData.disabilityPercentage) : 0
    };

    try {
      const res = editingMemberId
        ? await householdApi.update(editingMemberId, payload)
        : await householdApi.create(payload);

      if (res.data?.success) {
        setMemberMessage({ type: 'success', text: editingMemberId ? 'Household member updated successfully!' : 'Household member added successfully!' });
        setShowMemberForm(false);
        setEditingMemberId(null);
        loadHouseholdMembers();
        setTimeout(() => setMemberMessage(null), 4000);
      } else {
        setMemberMessage({ type: 'error', text: res.data?.message || 'Failed to save household member.' });
      }
    } catch (err) {
      console.error(err);
      const msg = err.response?.data?.message || 'Error saving household member. Please check fields.';
      setMemberMessage({ type: 'error', text: msg });
    } finally {
      setMemberSaving(false);
    }
  };

  const handleDeleteMember = async () => {
    if (!memberToDelete) return;
    try {
      const res = await householdApi.delete(memberToDelete.id);
      if (res.data?.success) {
        setMemberMessage({ type: 'success', text: `${memberToDelete.fullName} removed from household.` });
        setHouseholdMembers(prev => prev.filter(m => m.id !== memberToDelete.id));
        setMemberToDelete(null);
        setTimeout(() => setMemberMessage(null), 4000);
      }
    } catch (err) {
      console.error(err);
      alert('Failed to delete member. Please try again.');
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
      <div style={{ display: 'flex', gap: '8px', marginBottom: '24px', flexWrap: 'wrap' }}>
        <button
          onClick={() => setActiveTab('PROFILE')}
          className={`btn ${activeTab === 'PROFILE' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <User size={16} /> Citizen Parameters
        </button>
        <button
          onClick={() => setActiveTab('HOUSEHOLD')}
          className={`btn ${activeTab === 'HOUSEHOLD' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Users size={16} /> Household Members ({householdMembers.length})
        </button>
        <button
          onClick={() => setActiveTab('SAVED')}
          className={`btn ${activeTab === 'SAVED' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Bookmark size={16} /> Saved Schemes ({savedSchemes.length})
        </button>
        <button
          onClick={() => setActiveTab('APPLICATIONS')}
          className={`btn ${activeTab === 'APPLICATIONS' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Clock size={16} /> Tracked Applications ({applications.length})
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

      {/* Tab: Household Members */}
      {activeTab === 'HOUSEHOLD' && (
        <div>
          {memberMessage && (
            <div style={{
              background: memberMessage.type === 'success' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
              border: `1px solid ${memberMessage.type === 'success' ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`,
              color: memberMessage.type === 'success' ? '#34d399' : '#f87171',
              padding: '12px 16px',
              borderRadius: '10px',
              marginBottom: '20px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              fontSize: '0.9rem'
            }}>
              {memberMessage.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
              <span>{memberMessage.text}</span>
            </div>
          )}

          {/* Delete Confirmation Alert Modal */}
          {memberToDelete && (
            <div className="glass-card" style={{
              padding: '24px',
              marginBottom: '24px',
              border: '1px solid rgba(239, 68, 68, 0.4)',
              background: 'rgba(239, 68, 68, 0.08)'
            }}>
              <h4 style={{ color: '#f87171', fontSize: '1.1rem', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <AlertCircle size={18} /> Confirm Member Removal
              </h4>
              <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', marginBottom: '16px' }}>
                Are you sure you want to remove <strong>{memberToDelete.fullName}</strong> ({memberToDelete.relationship}) from your household? This profile will no longer be available for family eligibility evaluations.
              </p>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button
                  type="button"
                  onClick={handleDeleteMember}
                  className="btn btn-sm"
                  style={{ background: '#ef4444', color: '#fff', border: 'none' }}
                >
                  <Trash2 size={14} /> Yes, Remove Member
                </button>
                <button
                  type="button"
                  onClick={() => setMemberToDelete(null)}
                  className="btn btn-secondary btn-sm"
                >
                  Cancel
                </button>
              </div>
            </div>
          )}

          {showMemberForm ? (
            /* Add / Edit Form */
            <form onSubmit={handleSaveMember} className="glass-card" style={{ padding: '32px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '16px' }}>
                <div>
                  <h3 style={{ fontSize: '1.25rem', color: '#fff', marginBottom: '4px' }}>
                    {editingMemberId ? 'Edit Household Member' : 'Add Family / Household Member'}
                  </h3>
                  <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                    Provide demographic parameters to accurately match this member with government schemes.
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => setShowMemberForm(false)}
                  style={{ background: 'transparent', border: 'none', color: 'var(--text-dim)', cursor: 'pointer' }}
                >
                  <X size={20} />
                </button>
              </div>

              {/* Basic Info: Name, Relationship, Age, Gender */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                gap: '20px',
                marginBottom: '24px'
              }}>
                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>
                    Full Name <span style={{ color: '#ef4444' }}>*</span>
                  </label>
                  <input
                    type="text"
                    name="fullName"
                    value={memberFormData.fullName}
                    onChange={handleMemberFormChange}
                    placeholder="e.g. Ramesh Reddy"
                    required
                    style={{ width: '100%' }}
                  />
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>
                    Relationship to Account Holder <span style={{ color: '#ef4444' }}>*</span>
                  </label>
                  <select
                    name="relationship"
                    value={memberFormData.relationship}
                    onChange={handleMemberFormChange}
                    style={{ width: '100%' }}
                  >
                    <option value="Spouse">Spouse</option>
                    <option value="Child">Child (Son / Daughter)</option>
                    <option value="Parent">Parent (Father / Mother)</option>
                    <option value="Sibling">Sibling (Brother / Sister)</option>
                    <option value="Grandparent">Grandparent</option>
                    <option value="Other">Other Family Member</option>
                  </select>
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Age (Years)</label>
                  <input
                    type="number"
                    name="age"
                    value={memberFormData.age}
                    onChange={handleMemberFormChange}
                    min="0"
                    max="125"
                    style={{ width: '100%' }}
                  />
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Gender</label>
                  <select name="gender" value={memberFormData.gender} onChange={handleMemberFormChange} style={{ width: '100%' }}>
                    <option value="Female">Female</option>
                    <option value="Male">Male</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>State</label>
                  <select name="state" value={memberFormData.state} onChange={handleMemberFormChange} style={{ width: '100%' }}>
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
                  <input
                    type="text"
                    name="district"
                    value={memberFormData.district}
                    onChange={handleMemberFormChange}
                    style={{ width: '100%' }}
                  />
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Occupation</label>
                  <select name="occupation" value={memberFormData.occupation} onChange={handleMemberFormChange} style={{ width: '100%' }}>
                    <option value="">Select Occupation</option>
                    <option value="Farmer">Farmer / Agriculture</option>
                    <option value="Student">Student</option>
                    <option value="Daily Wage Worker">Daily Wage Worker</option>
                    <option value="Artisan">Artisan / Handloom</option>
                    <option value="Self Employed">Self Employed</option>
                    <option value="Salaried">Salaried Employee</option>
                    <option value="Homemaker">Homemaker</option>
                    <option value="Unemployed">Unemployed</option>
                    <option value="Retired">Retired / Senior Citizen</option>
                  </select>
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Employment Status</label>
                  <select name="employmentStatus" value={memberFormData.employmentStatus} onChange={handleMemberFormChange} style={{ width: '100%' }}>
                    <option value="Employed">Employed</option>
                    <option value="SelfEmployed">Self Employed</option>
                    <option value="Student">Student</option>
                    <option value="Unemployed">Unemployed</option>
                    <option value="Retired">Retired</option>
                  </select>
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Annual Income (₹)</label>
                  <input
                    type="number"
                    name="annualIncome"
                    value={memberFormData.annualIncome}
                    onChange={handleMemberFormChange}
                    style={{ width: '100%' }}
                  />
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Social Category</label>
                  <select name="category" value={memberFormData.category} onChange={handleMemberFormChange} style={{ width: '100%' }}>
                    <option value="General">General</option>
                    <option value="OBC">OBC</option>
                    <option value="SC">SC</option>
                    <option value="ST">ST</option>
                    <option value="EWS">EWS</option>
                  </select>
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Education Level</label>
                  <select name="educationLevel" value={memberFormData.educationLevel} onChange={handleMemberFormChange} style={{ width: '100%' }}>
                    <option value="Primary">Primary (1st-5th)</option>
                    <option value="10th">10th / Secondary</option>
                    <option value="12th">12th / Intermediate</option>
                    <option value="Graduate">Graduate</option>
                    <option value="PostGraduate">Post Graduate</option>
                    <option value="None">None / Literate</option>
                  </select>
                </div>

                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>Residence Area</label>
                  <select name="residenceType" value={memberFormData.residenceType} onChange={handleMemberFormChange} style={{ width: '100%' }}>
                    <option value="Rural">Rural</option>
                    <option value="Urban">Urban</option>
                    <option value="SemiUrban">Semi-Urban</option>
                  </select>
                </div>
              </div>

              {/* Checkboxes: Land, Disability, BPL, Minority */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                gap: '14px',
                marginBottom: '28px',
                padding: '16px',
                background: 'rgba(255, 255, 255, 0.02)',
                borderRadius: '8px'
              }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    name="landOwnership"
                    checked={memberFormData.landOwnership}
                    onChange={handleMemberFormChange}
                  />
                  <span>Owns Agricultural Land</span>
                </label>

                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    name="disabilityStatus"
                    checked={memberFormData.disabilityStatus}
                    onChange={handleMemberFormChange}
                  />
                  <span>Person with Disability (PwD)</span>
                </label>

                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    name="bplStatus"
                    checked={memberFormData.bplStatus}
                    onChange={handleMemberFormChange}
                  />
                  <span>BPL Household Member</span>
                </label>

                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    name="minorityStatus"
                    checked={memberFormData.minorityStatus}
                    onChange={handleMemberFormChange}
                  />
                  <span>Minority Community</span>
                </label>
              </div>

              <div style={{ display: 'flex', gap: '12px' }}>
                <button type="submit" disabled={memberSaving} className="btn btn-primary">
                  <Save size={16} /> {memberSaving ? 'Saving Member...' : editingMemberId ? 'Update Member' : 'Save Member'}
                </button>
                <button
                  type="button"
                  onClick={() => setShowMemberForm(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
              </div>
            </form>
          ) : (
            /* Member List */
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '12px' }}>
                <div>
                  <h3 style={{ fontSize: '1.25rem', color: '#fff', marginBottom: '4px' }}>
                    Family & Household Profiles ({householdMembers.length})
                  </h3>
                  <p style={{ fontSize: '0.84rem', color: 'var(--text-muted)' }}>
                    Each household member's profile enables instant evaluation of individual government schemes like scholarships, pensions, and maternal benefits.
                  </p>
                </div>
                <button onClick={handleOpenAddMember} className="btn btn-primary btn-sm">
                  <Plus size={16} /> Add Household Member
                </button>
              </div>

              {loadingHousehold ? (
                <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
                  Loading household members...
                </div>
              ) : householdMembers.length === 0 ? (
                <div className="glass-card" style={{ padding: '60px 24px', textAlign: 'center' }}>
                  <Users size={40} color="var(--primary)" style={{ marginBottom: '14px' }} />
                  <h3 style={{ fontSize: '1.25rem', marginBottom: '8px' }}>No household members added yet</h3>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', maxWidth: '440px', margin: '0 auto 20px' }}>
                    Add family members (spouse, children, elderly parents) to evaluate customized welfare schemes, student scholarships, and healthcare benefits for each person.
                  </p>
                  <button onClick={handleOpenAddMember} className="btn btn-primary btn-sm">
                    <Plus size={16} /> Add First Household Member
                  </button>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {householdMembers.map((member) => (
                    <div
                      key={member.id}
                      className="glass-card"
                      style={{ padding: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}
                    >
                      <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                        <div style={{
                          width: '48px',
                          height: '48px',
                          borderRadius: '14px',
                          background: 'linear-gradient(135deg, #06b6d4 0%, #10b981 100%)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontSize: '1.2rem',
                          fontWeight: 700,
                          color: '#fff',
                          flexShrink: 0
                        }}>
                          {member.fullName?.charAt(0) || 'M'}
                        </div>

                        <div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                            <h4 style={{ fontSize: '1.15rem', color: '#fff' }}>{member.fullName}</h4>
                            <span className="badge badge-secondary" style={{ fontSize: '0.72rem' }}>
                              {member.relationship}
                            </span>
                            {member.gender && (
                              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                                • {member.gender}{member.age ? `, ${member.age} yrs` : ''}
                              </span>
                            )}
                          </div>

                          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                            {member.occupation && <span>Occupation: {member.occupation}</span>}
                            {member.category && <span>Category: {member.category}</span>}
                            {member.annualIncome !== null && member.annualIncome !== undefined && (
                              <span>Income: ₹{Number(member.annualIncome).toLocaleString('en-IN')}/yr</span>
                            )}
                            {member.disabilityStatus && <span style={{ color: '#38bdf8' }}>• PwD</span>}
                            {member.bplStatus && <span style={{ color: '#34d399' }}>• BPL</span>}
                          </div>
                        </div>
                      </div>

                      <div style={{ display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
                        <Link to={`/eligibility?memberId=${member.id}`} className="btn btn-primary btn-sm">
                          <Sparkles size={14} /> Check Schemes
                        </Link>
                        <button
                          type="button"
                          onClick={() => handleOpenEditMember(member)}
                          className="btn btn-secondary btn-sm"
                          title="Edit member"
                        >
                          <Edit2 size={14} /> Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => setMemberToDelete(member)}
                          className="btn btn-secondary btn-sm"
                          style={{ color: '#ef4444' }}
                          title="Delete member"
                        >
                          <Trash2 size={14} /> Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
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
                    <div style={{ display: 'flex', gap: '8px', marginBottom: '6px', alignItems: 'center', flexWrap: 'wrap' }}>
                      <span className="badge badge-primary">{scheme.benefitCategory}</span>
                      <span className="badge badge-secondary">{scheme.scope}</span>
                      <SchemeVerificationBadge status={scheme.verificationStatus} />
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

      {/* Tab: Tracked Applications */}
      {activeTab === 'APPLICATIONS' && (
        <div>
          {/* Summary Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '16px', marginBottom: '24px' }}>
            <div className="glass-card" style={{ padding: '20px' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)', fontWeight: 600, textTransform: 'uppercase', marginBottom: '6px' }}>
                Total Tracked
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#fff' }}>
                {appSummary?.totalApplications ?? applications.length}
              </div>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                Citizen-managed records
              </div>
            </div>

            <div className="glass-card" style={{ padding: '20px' }}>
              <div style={{ fontSize: '0.8rem', color: '#fbbf24', fontWeight: 600, textTransform: 'uppercase', marginBottom: '6px' }}>
                In Preparation
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#fbbf24' }}>
                {(appSummary?.planningCount || 0) + (appSummary?.readyToApplyCount || 0)}
              </div>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                Planning & Ready to Apply
              </div>
            </div>

            <div className="glass-card" style={{ padding: '20px' }}>
              <div style={{ fontSize: '0.8rem', color: '#38bdf8', fontWeight: 600, textTransform: 'uppercase', marginBottom: '6px' }}>
                With Department
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#38bdf8' }}>
                {(appSummary?.submittedCount || 0) + (appSummary?.underReviewCount || 0) + (appSummary?.actionRequiredCount || 0)}
              </div>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                Submitted, Review & Actions
              </div>
            </div>

            <div className="glass-card" style={{ padding: '20px' }}>
              <div style={{ fontSize: '0.8rem', color: '#34d399', fontWeight: 600, textTransform: 'uppercase', marginBottom: '6px' }}>
                Approved & Completed
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#34d399' }}>
                {(appSummary?.approvedCount || 0) + (appSummary?.completedCount || 0)}
              </div>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                Successful sanctions
              </div>
            </div>
          </div>

          {/* Filter Bar */}
          <div className="glass-card" style={{ padding: '16px 20px', marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '14px' }}>
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center' }}>
              <div>
                <label style={{ fontSize: '0.78rem', color: 'var(--text-dim)', marginRight: '8px', fontWeight: 600 }}>
                  Status:
                </label>
                <select
                  value={appStatusFilter}
                  onChange={(e) => setAppStatusFilter(e.target.value)}
                  style={{ padding: '8px 12px', borderRadius: '8px', background: 'var(--surface)', color: 'var(--text-main)', border: '1px solid var(--border-subtle)', fontSize: '0.85rem' }}
                >
                  <option value="ALL">All Statuses ({applications.length})</option>
                  <option value="PLANNING">Planning to Apply</option>
                  <option value="READY_TO_APPLY">Ready to Apply</option>
                  <option value="SUBMITTED">Submitted</option>
                  <option value="UNDER_REVIEW">Under Review</option>
                  <option value="ACTION_REQUIRED">Action Required</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>

              <div>
                <label style={{ fontSize: '0.78rem', color: 'var(--text-dim)', marginRight: '8px', fontWeight: 600 }}>
                  Applicant:
                </label>
                <select
                  value={appApplicantFilter}
                  onChange={(e) => setAppApplicantFilter(e.target.value)}
                  style={{ padding: '8px 12px', borderRadius: '8px', background: 'var(--surface)', color: 'var(--text-main)', border: '1px solid var(--border-subtle)', fontSize: '0.85rem' }}
                >
                  <option value="ALL">All Household Members</option>
                  <option value="SELF">Myself (Account Holder)</option>
                  {householdMembers.map(m => (
                    <option key={m.id} value={m.fullName}>{m.fullName} ({m.relationship})</option>
                  ))}
                </select>
              </div>
            </div>

            <Link to="/schemes" className="btn btn-secondary btn-sm" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Plus size={15} /> Track Another Scheme
            </Link>
          </div>

          {/* Applications List */}
          {loadingApplications ? (
            <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
              Loading tracked applications...
            </div>
          ) : applications.length === 0 ? (
            <div className="glass-card" style={{ padding: '60px 24px', textAlign: 'center' }}>
              <Clock size={40} color="var(--primary)" style={{ marginBottom: '14px' }} />
              <h3 style={{ fontSize: '1.25rem', marginBottom: '8px' }}>No Tracked Scheme Applications Yet</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', maxWidth: '450px', margin: '0 auto 20px', lineHeight: 1.5 }}>
                Stay organized throughout your welfare journey. Track applications for yourself or family members, record acknowledgement numbers, and follow progress step-by-step.
              </p>
              <Link to="/schemes" className="btn btn-primary btn-sm">
                Explore Schemes Directory
              </Link>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {applications
                .filter(app => {
                  if (appStatusFilter !== 'ALL' && app.status !== appStatusFilter) return false;
                  if (appApplicantFilter === 'SELF' && app.applicantType !== 'SELF') return false;
                  if (appApplicantFilter !== 'ALL' && appApplicantFilter !== 'SELF' && !app.applicantName?.includes(appApplicantFilter)) return false;
                  return true;
                })
                .map((app) => {
                  const meta = getStatusMeta(app.status);
                  return (
                    <div key={app.id} className="glass-card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px' }}>
                        <div>
                          <div style={{ display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap', marginBottom: '6px' }}>
                            <span style={{
                              padding: '4px 10px',
                              borderRadius: '12px',
                              fontSize: '0.75rem',
                              fontWeight: 700,
                              background: meta.bg,
                              color: meta.color,
                              border: `1px solid ${meta.border}`
                            }}>
                              {app.statusDisplayName || meta.label}
                            </span>
                            <span className="badge badge-secondary" style={{ fontSize: '0.75rem' }}>
                              {app.schemeCategory || 'Welfare'}
                            </span>
                            <span style={{
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '4px',
                              fontSize: '0.75rem',
                              color: 'var(--text-dim)',
                              background: 'rgba(255, 255, 255, 0.05)',
                              padding: '3px 8px',
                              borderRadius: '6px'
                            }}>
                              <User size={12} /> {app.applicantName}
                            </span>
                          </div>

                          <h3 style={{ fontSize: '1.25rem', color: '#fff', marginBottom: '4px' }}>
                            <Link to={`/schemes/${app.schemeId}`} style={{ color: 'inherit', textDecoration: 'none' }}>
                              {app.schemeName}
                            </Link>
                          </h3>
                          <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                            {app.governmentAuthority || 'Government Scheme'}
                          </div>
                        </div>

                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                          <button
                            onClick={() => handleOpenAppDetails(app, 'VIEW')}
                            className="btn btn-secondary btn-sm"
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                          >
                            <History size={14} /> Timeline & Details
                          </button>
                          <button
                            onClick={() => handleOpenAppDetails(app, 'UPDATE')}
                            className="btn btn-primary btn-sm"
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                          >
                            <Edit2 size={14} /> Update Status
                          </button>
                          <button
                            onClick={() => setAppToDelete(app)}
                            className="btn btn-secondary btn-sm"
                            style={{ color: '#ef4444' }}
                            title="Delete tracking record"
                          >
                            <Trash2 size={15} />
                          </button>
                        </div>
                      </div>

                      {/* Info Chips */}
                      <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap', fontSize: '0.82rem', color: 'var(--text-dim)' }}>
                        {app.applicationReference && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#38bdf8' }}>
                            <FileText size={14} /> <strong>Ref:</strong> {app.applicationReference}
                          </div>
                        )}
                        {app.appliedDate && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Calendar size={14} /> <strong>Submitted:</strong> {app.appliedDate}
                          </div>
                        )}
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <Clock size={14} /> <strong>Last Updated:</strong> {new Date(app.lastStatusUpdate || app.updatedAt).toLocaleDateString()}
                        </div>
                      </div>

                      {/* Next Action Box */}
                      {(app.nextActionDate || app.nextActionNotes) && (
                        <div style={{
                          padding: '10px 14px',
                          borderRadius: '8px',
                          background: 'rgba(245, 158, 11, 0.08)',
                          border: '1px solid rgba(245, 158, 11, 0.25)',
                          fontSize: '0.82rem',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '10px'
                        }}>
                          <AlertTriangle size={16} color="#fbbf24" style={{ flexShrink: 0 }} />
                          <div>
                            <span style={{ fontWeight: 700, color: '#fbbf24', marginRight: '6px' }}>
                              Next Step {app.nextActionDate ? `(Target: ${app.nextActionDate}):` : ':'}
                            </span>
                            <span style={{ color: 'var(--text-main)' }}>
                              {app.nextActionNotes || 'Follow up on application status.'}
                            </span>
                          </div>
                        </div>
                      )}

                      {app.notes && (
                        <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
                          "{app.notes}"
                        </div>
                      )}
                    </div>
                  );
                })}
            </div>
          )}
        </div>
      )}

      {/* Details & Update Modal */}
      {selectedApp && (
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
            maxWidth: '680px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            padding: '28px',
            borderRadius: '16px',
            position: 'relative'
          }}>
            <button
              onClick={() => setSelectedApp(null)}
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

            <div style={{ marginBottom: '16px' }}>
              <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '6px' }}>
                <span style={{
                  padding: '3px 10px',
                  borderRadius: '12px',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  ...getStatusMeta(selectedApp.status)
                }}>
                  {selectedApp.statusDisplayName || selectedApp.status}
                </span>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                  Applicant: <strong>{selectedApp.applicantName}</strong>
                </span>
              </div>
              <h2 style={{ fontSize: '1.4rem', color: '#fff', marginBottom: '4px' }}>
                {selectedApp.schemeName}
              </h2>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                {selectedApp.governmentAuthority}
              </div>
            </div>

            {/* Modal Tabs */}
            <div style={{ display: 'flex', gap: '8px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '12px', marginBottom: '20px' }}>
              <button
                onClick={() => setAppModalMode('VIEW')}
                className={`btn btn-sm ${appModalMode === 'VIEW' ? 'btn-primary' : 'btn-secondary'}`}
                style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
              >
                <History size={14} /> Progress Timeline ({selectedApp.history?.length || 0})
              </button>
              <button
                onClick={() => setAppModalMode('UPDATE')}
                className={`btn btn-sm ${appModalMode === 'UPDATE' ? 'btn-primary' : 'btn-secondary'}`}
                style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
              >
                <Edit2 size={14} /> Update Status & Details
              </button>
            </div>

            {appMessage && (
              <div style={{
                background: appMessage.type === 'success' ? 'rgba(16, 185, 129, 0.12)' : 'rgba(239, 68, 68, 0.12)',
                border: `1px solid ${appMessage.type === 'success' ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`,
                color: appMessage.type === 'success' ? '#34d399' : '#f87171',
                padding: '10px 14px',
                borderRadius: '8px',
                fontSize: '0.85rem',
                marginBottom: '16px'
              }}>
                {appMessage.text}
              </div>
            )}

            {appModalMode === 'VIEW' ? (
              <div>
                {/* Application Metadata Overview */}
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))',
                  gap: '12px',
                  padding: '14px',
                  borderRadius: '10px',
                  background: 'rgba(255, 255, 255, 0.03)',
                  border: '1px solid var(--border-subtle)',
                  marginBottom: '20px',
                  fontSize: '0.82rem'
                }}>
                  <div>
                    <div style={{ color: 'var(--text-dim)', marginBottom: '2px' }}>Reference Number</div>
                    <div style={{ fontWeight: 700, color: '#38bdf8' }}>{selectedApp.applicationReference || 'Not recorded'}</div>
                  </div>
                  <div>
                    <div style={{ color: 'var(--text-dim)', marginBottom: '2px' }}>Submitted Date</div>
                    <div style={{ fontWeight: 600 }}>{selectedApp.appliedDate || 'Not yet submitted'}</div>
                  </div>
                  <div>
                    <div style={{ color: 'var(--text-dim)', marginBottom: '2px' }}>Next Action Target</div>
                    <div style={{ fontWeight: 600, color: selectedApp.nextActionDate ? '#fbbf24' : 'inherit' }}>
                      {selectedApp.nextActionDate || 'None set'}
                    </div>
                  </div>
                </div>

                {selectedApp.nextActionNotes && (
                  <div style={{
                    padding: '10px 14px',
                    borderRadius: '8px',
                    background: 'rgba(245, 158, 11, 0.08)',
                    border: '1px solid rgba(245, 158, 11, 0.25)',
                    marginBottom: '20px',
                    fontSize: '0.85rem'
                  }}>
                    <strong>Next Action Note:</strong> {selectedApp.nextActionNotes}
                  </div>
                )}

                {/* Audit Timeline */}
                <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <History size={16} /> Chronological Audit Timeline
                </h3>

                {(!selectedApp.history || selectedApp.history.length === 0) ? (
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No status history recorded yet.</p>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', position: 'relative', paddingLeft: '20px', borderLeft: '2px solid rgba(255, 255, 255, 0.1)' }}>
                    {selectedApp.history.map((h) => {
                      const meta = getStatusMeta(h.toStatus);
                      return (
                        <div key={h.id} style={{ position: 'relative' }}>
                          <div style={{
                            position: 'absolute',
                            left: '-27px',
                            top: '4px',
                            width: '12px',
                            height: '12px',
                            borderRadius: '50%',
                            background: meta.color,
                            boxShadow: `0 0 8px ${meta.color}`
                          }} />
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', flexWrap: 'wrap', gap: '6px' }}>
                            <span style={{ fontWeight: 700, fontSize: '0.88rem', color: meta.color }}>
                              {h.fromStatus ? `${h.fromStatusDisplayName || h.fromStatus} → ${h.toStatusDisplayName || h.toStatus}` : (h.toStatusDisplayName || h.toStatus)}
                            </span>
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                              {new Date(h.createdAt).toLocaleString()} • {h.initiatedBy}
                            </span>
                          </div>
                          {h.notes && (
                            <p style={{ margin: '4px 0 0', fontSize: '0.82rem', color: 'var(--text-main)', lineHeight: 1.4 }}>
                              {h.notes}
                            </p>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}

                <div style={{
                  marginTop: '24px',
                  padding: '10px 14px',
                  borderRadius: '8px',
                  background: 'rgba(255, 255, 255, 0.03)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '0.78rem',
                  color: 'var(--text-dim)'
                }}>
                  <Info size={14} style={{ display: 'inline', marginRight: '4px' }} />
                  This history is an append-only audit trail recorded by the citizen.
                </div>
              </div>
            ) : (
              <form onSubmit={handleSaveAppUpdate} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                    Application Status
                  </label>
                  <select
                    value={updateAppForm.status}
                    onChange={(e) => setUpdateAppForm(prev => ({ ...prev, status: e.target.value }))}
                    style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', background: 'var(--surface)', color: 'var(--text-main)', border: '1px solid var(--border-subtle)' }}
                  >
                    <option value={selectedApp.status}>
                      Current: {selectedApp.statusDisplayName || selectedApp.status} (No Status Change)
                    </option>
                    {getPermittedNextStatuses(selectedApp.status).map(s => {
                      const m = getStatusMeta(s);
                      return (
                        <option key={s} value={s}>
                          Transition to: {m.label}
                        </option>
                      );
                    })}
                  </select>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', marginTop: '4px', display: 'block' }}>
                    Only valid lifecycle transitions are selectable to maintain audit consistency.
                  </span>
                </div>

                {updateAppForm.status !== selectedApp.status && (
                  <div>
                    <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                      Status Change Explanation / Note
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Scrutiny cleared, awaiting physical verification from MRO"
                      value={updateAppForm.statusChangeNote}
                      onChange={(e) => setUpdateAppForm(prev => ({ ...prev, statusChangeNote: e.target.value }))}
                      style={{ width: '100%', padding: '10px 12px', borderRadius: '8px' }}
                    />
                  </div>
                )}

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                      Application Reference ID
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. ACK-2026-987654"
                      value={updateAppForm.applicationReference}
                      onChange={(e) => setUpdateAppForm(prev => ({ ...prev, applicationReference: e.target.value }))}
                      style={{ width: '100%', padding: '10px 12px', borderRadius: '8px' }}
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                      Date Submitted
                    </label>
                    <input
                      type="date"
                      value={updateAppForm.appliedDate}
                      onChange={(e) => setUpdateAppForm(prev => ({ ...prev, appliedDate: e.target.value }))}
                      style={{ width: '100%', padding: '10px 12px', borderRadius: '8px' }}
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                      Next Action Date
                    </label>
                    <input
                      type="date"
                      value={updateAppForm.nextActionDate}
                      onChange={(e) => setUpdateAppForm(prev => ({ ...prev, nextActionDate: e.target.value }))}
                      style={{ width: '100%', padding: '10px 12px', borderRadius: '8px' }}
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                      Next Step Reminder Note
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Submit physical documents at district office"
                      value={updateAppForm.nextActionNotes}
                      onChange={(e) => setUpdateAppForm(prev => ({ ...prev, nextActionNotes: e.target.value }))}
                      style={{ width: '100%', padding: '10px 12px', borderRadius: '8px' }}
                    />
                  </div>
                </div>

                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                    Notes & Record Details
                  </label>
                  <textarea
                    rows={3}
                    placeholder="Enter any notes, documents submitted, or contact numbers..."
                    value={updateAppForm.notes}
                    onChange={(e) => setUpdateAppForm(prev => ({ ...prev, notes: e.target.value }))}
                    style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', resize: 'vertical' }}
                  />
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
                  <button
                    type="button"
                    onClick={() => setAppModalMode('VIEW')}
                    className="btn btn-secondary btn-sm"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={updatingApp}
                    className="btn btn-primary btn-sm"
                  >
                    {updatingApp ? 'Saving Changes...' : 'Save Updates'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {/* Delete Application Confirmation Modal */}
      {appToDelete && (
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
          zIndex: 1100,
          padding: '20px'
        }}>
          <div className="glass-card" style={{
            maxWidth: '460px',
            width: '100%',
            padding: '28px',
            borderRadius: '16px',
            textAlign: 'center'
          }}>
            <AlertCircle size={44} color="#ef4444" style={{ margin: '0 auto 14px' }} />
            <h3 style={{ fontSize: '1.25rem', marginBottom: '8px', color: '#fff' }}>
              Delete Application Tracking Record?
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', marginBottom: '22px', lineHeight: 1.5 }}>
              Are you sure you want to remove the tracking record for <strong>{appToDelete.schemeName}</strong> ({appToDelete.applicantName})? This will also remove the complete status audit trail. This action cannot be undone.
            </p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
              <button
                type="button"
                onClick={() => setAppToDelete(null)}
                className="btn btn-secondary btn-sm"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleDeleteApp}
                className="btn btn-sm"
                style={{ background: '#ef4444', color: '#fff', border: 'none' }}
              >
                Delete Record
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
