import { useEffect, useState } from 'react';
import axios from 'axios';
import { Home, Plus, Trash2, List, Edit } from 'lucide-react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000';

type Pg = {
  id: string;
  name: string;
  address: string;
  rent: number;
  deposit: number;
  verified: boolean;
};

type Booking = {
  id: string;
  status: string;
  roomType: string;
  createdAt: string;
  user?: { phone: string };
  pg?: { name: string };
};

const OwnerDashboard = () => {
  const [pgs, setPgs] = useState<Pg[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [activeTab, setActiveTab] = useState<'pgs' | 'bookings'>('pgs');
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    name: '', address: '', description: '', lat: '', lng: '',
    rent: '', deposit: ''
  });

  const token = localStorage.getItem('token');
  const headers = { Authorization: `Bearer ${token}` };

  const fetchPgs = async () => {
    try {
      const res = await axios.get(`${API_URL}/api/owner/pgs`, { headers });
      setPgs(res.data);
    } catch (err) { console.error(err); }
  };

  const fetchBookings = async () => {
    try {
      const res = await axios.get(`${API_URL}/api/owner/bookings`, { headers });
      setBookings(res.data);
    } catch (err) { console.error(err); }
  };

  useEffect(() => {
    setLoading(true);
    Promise.all([fetchPgs(), fetchBookings()]).finally(() => setLoading(false));
  }, []);

  const handleCreatePg = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await axios.post(`${API_URL}/api/owner/pgs`, {
        ...form,
        lat: parseFloat(form.lat),
        lng: parseFloat(form.lng),
        rent: parseInt(form.rent),
        deposit: parseInt(form.deposit),
        rooms: [
          { type: 'SINGLE', total: 5, price: parseInt(form.rent) },
          { type: 'DOUBLE', total: 5, price: parseInt(form.rent) - 2000 },
        ]
      }, { headers });
      setShowForm(false);
      setForm({ name: '', address: '', description: '', lat: '', lng: '', rent: '', deposit: '' });
      fetchPgs();
    } catch (err) { console.error(err); }
  };

  const handleDeletePg = async (id: string) => {
    if (!confirm('Delete this PG?')) return;
    try {
      await axios.delete(`${API_URL}/api/owner/pgs/${id}`, { headers });
      fetchPgs();
    } catch (err) { console.error(err); }
  };

  return (
    <div className="dashboard-layout">
      <aside className="sidebar">
        <div className="sidebar-links">
          <button
            className={`sidebar-link ${activeTab === 'pgs' ? 'active' : ''}`}
            onClick={() => setActiveTab('pgs')}
          >
            <Home size={20} /> My PGs
          </button>
          <button
            className={`sidebar-link ${activeTab === 'bookings' ? 'active' : ''}`}
            onClick={() => setActiveTab('bookings')}
          >
            <List size={20} /> Bookings
          </button>
        </div>
      </aside>

      <main className="dashboard-content">
        {loading && <p className="muted">Loading...</p>}

        {activeTab === 'pgs' && (
          <div>
            <header className="dashboard-header">
              <h1>My PG Listings</h1>
              <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
                <Plus size={16} /> Add PG
              </button>
            </header>

            {showForm && (
              <form className="card mt-2" onSubmit={handleCreatePg} style={{ padding: '1.5rem', display: 'grid', gap: '1rem' }}>
                <h3>New PG</h3>
                {(['name', 'address', 'description', 'lat', 'lng', 'rent', 'deposit'] as const).map(f => (
                  <div key={f} className="form-group">
                    <label style={{ textTransform: 'capitalize', fontWeight: 600 }}>{f}</label>
                    <input
                      className="form-control"
                      placeholder={f}
                      value={form[f]}
                      onChange={e => setForm({ ...form, [f]: e.target.value })}
                      required={f !== 'description'}
                    />
                  </div>
                ))}
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <button type="submit" className="btn btn-primary">Create</button>
                  <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
                </div>
              </form>
            )}

            {pgs.length === 0 && !loading ? (
              <p className="muted" style={{ marginTop: '2rem' }}>No PGs yet. Click "Add PG" to create one.</p>
            ) : (
              <div style={{ display: 'grid', gap: '1rem', marginTop: '1.5rem' }}>
                {pgs.map(pg => (
                  <div key={pg.id} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 1.5rem' }}>
                    <div>
                      <strong>{pg.name}</strong>
                      <p className="muted" style={{ margin: '0.25rem 0 0' }}>{pg.address}</p>
                      <small className="muted">₹{pg.rent}/mo · Deposit ₹{pg.deposit} · {pg.verified ? '✓ Verified' : 'Pending'}</small>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button className="btn btn-secondary btn-sm" onClick={() => handleDeletePg(pg.id)}>
                        <Trash2 size={14} /> Delete
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'bookings' && (
          <div>
            <header className="dashboard-header">
              <h1>All Bookings</h1>
            </header>
            {bookings.length === 0 && !loading ? (
              <p className="muted" style={{ marginTop: '2rem' }}>No bookings yet.</p>
            ) : (
              <div style={{ display: 'grid', gap: '0.75rem', marginTop: '1.5rem' }}>
                {bookings.map(b => (
                  <div key={b.id} className="card" style={{ padding: '1rem 1.5rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <div>
                        <strong>{b.pg?.name || 'PG'}</strong> — {b.roomType}
                        <p className="muted" style={{ margin: '0.25rem 0 0' }}>Tenant: {b.user?.phone || 'N/A'}</p>
                      </div>
                      <span className={`status-badge ${b.status === 'CONFIRMED' ? 'active' : 'paid'}`}>
                        {b.status}
                      </span>
                    </div>
                    <small className="muted">{b.createdAt?.split('T')[0]}</small>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
};

export default OwnerDashboard;
