import { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { AnimatePresence, LayoutGroup, motion } from 'framer-motion';
import { 
  Search, MapPin, Star, Home, DollarSign, 
  User, Phone, BedDouble, Utensils, ShieldCheck,
  Bus, Train, Building2, List, Grid2X2, LocateFixed, Loader2,
  Check, X
} from 'lucide-react';
import PropertyComparison from '../components/PropertyComparison';

type RoomType = {
  type: 'Single' | 'Double' | 'Triple';
  totalRooms: number;
  occupiedRooms: number;
  availableRooms: number;
};

type ReviewSummary = {
  rating: number;
  reviewCount: number;
  topReviews: {
    authorName: string;
    rating: number;
    time: string;
    text: string;
  }[];
};

type NearbyPlace = {
  name: string;
  address: string;
  distanceMeters: number;
};

type Infrastructure = {
  busStand: NearbyPlace | null;
  metroStation: NearbyPlace | null;
  railwayStation: NearbyPlace | null;
  collegeOrSchool: NearbyPlace | null;
};

type PG = {
  id: string;
  name: string;
  location: string;
  latitude?: number | null;
  longitude?: number | null;
  monthlyRent: number;
  depositAmount: number;
  distance: number;
  totalRooms: number;
  occupiedRooms: number;
  availableRooms: number;
  occupancyRate: number;
  overallRating: number;
  foodSafetyRating?: number;
  cleaningRating?: number;
  ownerName?: string | null;
  managerPhone?: string | null;
  summary?: string | null;
  nearCollege?: string | null;
  nearMetro?: string | null;
  nearBusStand?: string | null;
  placeId?: string | null;
  googlePlaceId?: string | null;
  roomTypes?: RoomType[];
  averageNearbyRent?: number;
  priceDifference?: number;
  priceComparison?: 'Above average' | 'Below average' | 'Near average';
};

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000';

const demoPGs: PG[] = [
  {
    id: 'demo-1',
    name: 'Urban Nest PG',
    location: 'Indiranagar, Bengaluru',
    latitude: 12.9784,
    longitude: 77.6408,
    monthlyRent: 14200,
    depositAmount: 28000,
    distance: 0.8,
    totalRooms: 24,
    occupiedRooms: 19,
    availableRooms: 5,
    occupancyRate: 79.2,
    overallRating: 4.5,
    foodSafetyRating: 4.2,
    cleaningRating: 4.6,
    ownerName: 'Ramesh Kumar',
    managerPhone: '+91 98765 43210',
    summary: 'Furnished student PG with meals, Wi-Fi, laundry, and biometric entry.',
    nearMetro: '500m from metro',
    nearBusStand: '300m from bus stand',
    nearCollege: '1.2km from college',
    roomTypes: [
      { type: 'Single', totalRooms: 8, occupiedRooms: 7, availableRooms: 1 },
      { type: 'Double', totalRooms: 10, occupiedRooms: 8, availableRooms: 2 },
      { type: 'Triple', totalRooms: 6, occupiedRooms: 4, availableRooms: 2 }
    ],
    averageNearbyRent: 13200,
    priceDifference: 1000,
    priceComparison: 'Above average'
  },
  {
    id: 'demo-2',
    name: 'Metro Stay Homes',
    location: 'Koramangala, Bengaluru',
    latitude: 12.9352,
    longitude: 77.6245,
    monthlyRent: 11800,
    depositAmount: 22000,
    distance: 1.4,
    totalRooms: 18,
    occupiedRooms: 18,
    availableRooms: 0,
    occupancyRate: 100,
    overallRating: 4.1,
    foodSafetyRating: 3.9,
    cleaningRating: 4.3,
    ownerName: 'Anita Sharma',
    managerPhone: '+91 99887 76655',
    summary: 'Budget PG close to colleges, bus routes, grocery stores, and food courts.',
    nearMetro: '1.8km from metro',
    nearBusStand: '450m from bus stand',
    nearCollege: '700m from college',
    roomTypes: [
      { type: 'Single', totalRooms: 4, occupiedRooms: 4, availableRooms: 0 },
      { type: 'Double', totalRooms: 8, occupiedRooms: 8, availableRooms: 0 },
      { type: 'Triple', totalRooms: 6, occupiedRooms: 6, availableRooms: 0 }
    ],
    averageNearbyRent: 13200,
    priceDifference: -1400,
    priceComparison: 'Below average'
  }
];

const formatCurrency = (value = 0) =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0
  }).format(value);

const formatDistance = (meters: number) =>
  meters < 1000 ? `${meters}m` : `${(meters / 1000).toFixed(1)}km`;

const getRoomTypes = (pg: PG): RoomType[] =>
  pg.roomTypes?.length
    ? pg.roomTypes
    : [
        { type: 'Single', totalRooms: 0, occupiedRooms: 0, availableRooms: 0 },
        { type: 'Double', totalRooms: 0, occupiedRooms: 0, availableRooms: 0 },
        { type: 'Triple', totalRooms: 0, occupiedRooms: 0, availableRooms: 0 }
      ];

const StudentPGSearch = () => {
  const [pgs, setPgs] = useState<PG[]>(demoPGs);
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState('Use your current location to find PGs within 10 km.');
  const [error, setError] = useState('');
  const [reviewsByPg, setReviewsByPg] = useState<Record<string, ReviewSummary>>({});
  const [infrastructureByPg, setInfrastructureByPg] = useState<Record<string, Infrastructure>>({});
  
  // New States
  const [favorites, setFavorites] = useState<string[]>([]);
  const [selectedForComparison, setSelectedForComparison] = useState<PG[]>([]);
  const [isComparing, setIsComparing] = useState(false);
  const [filters, setFilters] = useState({
    priceRange: [0, 30000],
    minRating: 0,
    hasFood: false,
    amenities: [] as string[]
  });
  const [selectedBookingPg, setSelectedBookingPg] = useState<PG | null>(null);
  const [bookingStep, setBookingStep] = useState(1);

  const averageRent = useMemo(() => {
    if (!pgs.length) return 0;
    return Math.round(pgs.reduce((sum, pg) => sum + pg.monthlyRent, 0) / pgs.length);
  }, [pgs]);

  const filteredPgs = useMemo(() => {
    return pgs.filter(pg => {
      const withinPrice = pg.monthlyRent >= filters.priceRange[0] && pg.monthlyRent <= filters.priceRange[1];
      const aboveRating = pg.overallRating >= filters.minRating;
      const foodMatch = !filters.hasFood || (pg.foodSafetyRating && pg.foodSafetyRating > 0);
      return withinPrice && aboveRating && foodMatch;
    });
  }, [pgs, filters]);

  useEffect(() => {
    pgs.forEach((pg) => {
      const placeId = pg.placeId || pg.googlePlaceId;

      if (placeId && !reviewsByPg[pg.id]) {
        axios
          .get<ReviewSummary>(`${API_URL}/api/google/places/${placeId}/reviews`)
          .then((res) => setReviewsByPg((current) => ({ ...current, [pg.id]: res.data })))
          .catch(() => undefined);
      }

      if (pg.latitude && pg.longitude && !infrastructureByPg[pg.id]) {
        axios
          .get<Infrastructure>(`${API_URL}/api/google/nearby-infrastructure`, {
            params: { lat: pg.latitude, lng: pg.longitude, radius: 3000 }
          })
          .then((res) => setInfrastructureByPg((current) => ({ ...current, [pg.id]: res.data })))
          .catch(() => undefined);
      }
    });
  }, [infrastructureByPg, pgs, reviewsByPg]);

  const fetchNearbyPGs = async () => {
    if (!navigator.geolocation) {
      setError('Location services are not available in this browser.');
      return;
    }

    setLoading(true);
    setError('');
    setStatus('Waiting for location permission...');

    navigator.geolocation.getCurrentPosition(
      async ({ coords }) => {
        try {
          setStatus('Finding nearby PGs...');
          const res = await axios.get(`${API_URL}/api/pgs/nearby`, {
            params: { lat: coords.latitude, lng: coords.longitude }
          });
          const nearby = Array.isArray(res.data) ? res.data : res.data.pgs;
          setPgs(nearby?.length ? nearby : []);
          setStatus(
            nearby?.length
              ? `${nearby.length} PG${nearby.length === 1 ? '' : 's'} found within 10 km.`
              : 'No PGs found within 10 km of your location.'
          );
        } catch (err) {
          const message = axios.isAxiosError(err)
            ? err.response?.data?.message || 'Unable to fetch nearby PGs.'
            : 'Unable to fetch nearby PGs.';
          setError(message);
        } finally {
          setLoading(false);
        }
      },
      () => {
        setLoading(false);
        setError('Location permission is required to search nearby PGs.');
        setStatus('Location access was not granted.');
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    );
  };

  const toggleFavorite = (id: string) => {
    setFavorites(prev => prev.includes(id) ? prev.filter(f => f !== id) : [...prev, id]);
  };

  const toggleComparison = (pg: PG) => {
    setSelectedForComparison(prev => 
      prev.find(p => p.id === pg.id) ? prev.filter(p => p.id !== pg.id) : [...prev, pg].slice(0, 3)
    );
  };

  return (
    <div className="pg-search-page">
      <section className="pg-toolbar">
        <div>
          <span className="section-kicker">Student PG Finder</span>
          <h1>Find your perfect stay</h1>
          <p>{status}</p>
        </div>
        <div className="toolbar-actions">
          <div className="view-toggle" aria-label="Toggle listing layout">
            <button
              className={viewMode === 'list' ? 'active' : ''}
              onClick={() => setViewMode('list')}
              title="List view"
              type="button"
            >
              <List size={18} />
            </button>
            <button
              className={viewMode === 'grid' ? 'active' : ''}
              onClick={() => setViewMode('grid')}
              title="Grid view"
              type="button"
            >
              <Grid2X2 size={18} />
            </button>
          </div>
          <button className="btn btn-primary" onClick={fetchNearbyPGs} disabled={loading}>
            {loading ? <Loader2 className="animate-spin" size={18} /> : <LocateFixed size={18} />}
            Find PGs
          </button>
        </div>
      </section>

      <section className="filter-bar">
        <div className="filter-group">
          <label>Price Range</label>
          <input 
            type="range" 
            min="5000" 
            max="30000" 
            step="1000" 
            value={filters.priceRange[1]}
            onChange={(e) => setFilters({...filters, priceRange: [filters.priceRange[0], parseInt(e.target.value)]})}
          />
          <span>Up to {formatCurrency(filters.priceRange[1])}</span>
        </div>
        <div className="filter-group">
          <label>Min Rating</label>
          <div className="rating-selector">
            {[3, 3.5, 4, 4.5].map(r => (
              <button 
                key={r}
                className={filters.minRating === r ? 'active' : ''}
                onClick={() => setFilters({...filters, minRating: r})}
              >
                {r}+ <Star size={12} fill={filters.minRating === r ? 'white' : 'currentColor'} />
              </button>
            ))}
          </div>
        </div>
        <div className="filter-group checkbox">
          <input 
            type="checkbox" 
            id="food-filter"
            checked={filters.hasFood}
            onChange={(e) => setFilters({...filters, hasFood: e.target.checked})}
          />
          <label htmlFor="food-filter">Food Included</label>
        </div>
        {selectedForComparison.length > 0 && (
          <button className="btn btn-secondary compare-btn" onClick={() => setIsComparing(true)}>
            Compare ({selectedForComparison.length})
          </button>
        )}
      </section>

      <section className="comparison-strip">
        <div>
          <span>Nearby average</span>
          <strong>{formatCurrency(averageRent)}</strong>
        </div>
        <div>
          <span>PGs found</span>
          <strong>{filteredPgs.length}</strong>
        </div>
        <div>
          <span>Favorites</span>
          <strong>{favorites.length} saved</strong>
        </div>
      </section>

      {error && <div className="error-alert">{error}</div>}

      <LayoutGroup>
        <motion.section className={`pg-results ${viewMode}`} layout>
          <AnimatePresence mode="popLayout">
            {filteredPgs.map((pg) => {
              const googleReviews = reviewsByPg[pg.id];
              const infrastructure = infrastructureByPg[pg.id];
              const rating = googleReviews?.rating || pg.overallRating || 0;
              const safetyRating = pg.cleaningRating || pg.overallRating || 0;
              const foodRating = pg.foodSafetyRating || pg.overallRating || 0;
              const isAvailable = pg.availableRooms > 0;
              const comparison = pg.priceComparison || (
                Math.abs(pg.monthlyRent - averageRent) < averageRent * 0.05
                  ? 'Near average'
                  : pg.monthlyRent > averageRent
                    ? 'Above average'
                    : 'Below average'
              );

              return (
                <motion.article
                  className={`pg-card ${!isAvailable ? 'not-available' : ''}`}
                  key={pg.id}
                  layout
                  initial={{ opacity: 0, y: 18, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: -10, scale: 0.98 }}
                  transition={{ type: 'spring', stiffness: 320, damping: 30 }}
                >
                  <div className="pg-card-main">
                    <div className="pg-card-top">
                      <div className="pg-icon">
                        <Home size={22} />
                      </div>
                      <div>
                        <h2>{pg.name}</h2>
                        <p><MapPin size={15} /> {pg.location}</p>
                      </div>
                      <div className="card-actions-top">
                        <button 
                          className={`icon-btn ${favorites.includes(pg.id) ? 'active' : ''}`}
                          onClick={() => toggleFavorite(pg.id)}
                        >
                          <Star size={18} fill={favorites.includes(pg.id) ? '#fbbf24' : 'none'} />
                        </button>
                        <span className={isAvailable ? 'availability-pill' : 'availability-pill danger'}>
                          {isAvailable ? `${pg.availableRooms} Available` : 'Not Available'}
                        </span>
                      </div>
                    </div>

                    <p className="pg-summary">
                      {pg.summary || 'Managed PG with verified owner details, room availability, and nearby access points.'}
                    </p>

                    <div className="owner-row">
                      <span><User size={15} /> {pg.ownerName || 'Owner details pending'}</span>
                      <span><Phone size={15} /> {pg.managerPhone || 'Phone not added'}</span>
                    </div>

                    <div className="rating-row">
                      <span><Star size={16} /> {rating.toFixed(1)} / 5</span>
                      <span>{googleReviews ? `${googleReviews.reviewCount} reviews` : 'Google reviews pending'}</span>
                      <span>{pg.distance.toFixed(1)} km away</span>
                    </div>

                    <div className="card-footer">
                      <button 
                        className={`btn btn-outline btn-sm ${selectedForComparison.find(p => p.id === pg.id) ? 'active' : ''}`}
                        onClick={() => toggleComparison(pg)}
                      >
                        {selectedForComparison.find(p => p.id === pg.id) ? 'Comparing' : 'Compare'}
                      </button>
                      <button 
                        className="btn btn-primary btn-sm" 
                        disabled={!isAvailable}
                        onClick={() => { setSelectedBookingPg(pg); setBookingStep(1); }}
                      >
                        Book Now
                      </button>
                    </div>
                  </div>

                  <div className="pg-card-side">
                    <div className="price-panel">
                      <span>Monthly rent</span>
                      <strong>{formatCurrency(pg.monthlyRent)}</strong>
                      <small>Deposit {formatCurrency(pg.depositAmount)}</small>
                      <b className={`comparison-badge ${comparison === 'Above average' ? 'high' : comparison === 'Below average' ? 'low' : ''}`}>
                        {comparison}
                      </b>
                    </div>

                    <div className="room-panel">
                      <div className="room-header">
                        <span><BedDouble size={16} /> Occupancy</span>
                        <strong>{pg.occupancyRate.toFixed(0)}%</strong>
                      </div>
                      {getRoomTypes(pg).map((room) => (
                        <div className="room-type" key={`${pg.id}-${room.type}`}>
                          <span>{room.type}</span>
                          <strong>{room.availableRooms}/{room.totalRooms}</strong>
                        </div>
                      ))}
                    </div>
                  </div>
                </motion.article>
              );
            })}
          </AnimatePresence>
        </motion.section>
      </LayoutGroup>

      {/* Comparison Overlay */}
      <AnimatePresence>
        {isComparing && (
          <PropertyComparison 
            pgs={selectedForComparison} 
            onClose={() => setIsComparing(false)}
            onBook={(pg) => { setSelectedBookingPg(pg); setIsComparing(false); }}
          />
        )}
      </AnimatePresence>

      {/* Booking Modal */}
      <AnimatePresence>
        {selectedBookingPg && (
          <motion.div 
            className="booking-modal-overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.div 
              className="booking-modal glass"
              initial={{ y: 50, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
            >
              <div className="booking-header">
                <h2>Book {selectedBookingPg.name}</h2>
                <button onClick={() => setSelectedBookingPg(null)}>×</button>
              </div>
              <div className="booking-steps">
                <div className={`step ${bookingStep >= 1 ? 'active' : ''}`}>1. Details</div>
                <div className={`step ${bookingStep >= 2 ? 'active' : ''}`}>2. Review</div>
                <div className={`step ${bookingStep >= 3 ? 'active' : ''}`}>3. Payment</div>
              </div>
              <div className="booking-body">
                {bookingStep === 1 && (
                  <div className="step-content">
                    <h3>Select Room Type</h3>
                    <select className="form-select">
                      {getRoomTypes(selectedBookingPg).filter(r => r.availableRooms > 0).map(r => (
                        <option key={r.type}>{r.type} - {formatCurrency(selectedBookingPg.monthlyRent)}</option>
                      ))}
                    </select>
                    <button className="btn btn-primary" onClick={() => setBookingStep(2)}>Next</button>
                  </div>
                )}
                {bookingStep === 2 && (
                  <div className="step-content">
                    <h3>Review Booking</h3>
                    <p>Total Amount: {formatCurrency(selectedBookingPg.monthlyRent + selectedBookingPg.depositAmount)}</p>
                    <button className="btn btn-primary" onClick={() => setBookingStep(3)}>Proceed to Payment</button>
                  </div>
                )}
                {bookingStep === 3 && (
                  <div className="step-content text-center">
                    <h3>Payment Integrated</h3>
                    <p>Redirecting to Razorpay...</p>
                    <button className="btn btn-primary" onClick={() => setSelectedBookingPg(null)}>Complete (Demo)</button>
                  </div>
                )}
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <style>{`
        .pg-search-page {
          max-width: 1180px;
          margin: 0 auto;
          padding: 2rem 1rem 4rem;
        }
        .filter-bar {
          display: flex;
          align-items: center;
          gap: 2rem;
          background: white;
          border: 1px solid var(--border);
          padding: 1rem 1.5rem;
          border-radius: 12px;
          margin-bottom: 1.5rem;
          box-shadow: var(--shadow-sm);
          flex-wrap: wrap;
        }
        .filter-group {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
        }
        .filter-group.checkbox {
          flex-direction: row;
          align-items: center;
          margin-top: 1.2rem;
        }
        .filter-group label {
          font-size: 0.75rem;
          font-weight: 700;
          text-transform: uppercase;
          color: var(--text-muted);
        }
        .rating-selector {
          display: flex;
          gap: 0.5rem;
        }
        .rating-selector button {
          padding: 0.3rem 0.6rem;
          border-radius: 6px;
          border: 1px solid var(--border);
          background: white;
          font-size: 0.85rem;
          display: flex;
          align-items: center;
          gap: 0.3rem;
        }
        .rating-selector button.active {
          background: var(--primary);
          color: white;
          border-color: var(--primary);
        }
        .compare-btn {
          margin-left: auto;
        }
        .card-actions-top {
          display: flex;
          gap: 0.5rem;
          align-items: center;
        }
        .icon-btn {
          width: 32px;
          height: 32px;
          display: grid;
          place-items: center;
          border-radius: 50%;
          border: 1px solid var(--border);
          background: white;
          color: var(--text-muted);
        }
        .icon-btn.active {
          border-color: #fbbf24;
          color: #fbbf24;
        }
        .card-footer {
          display: flex;
          gap: 0.75rem;
          margin-top: 1.2rem;
        }
        .comparison-overlay, .booking-modal-overlay {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0,0,0,0.4);
          display: grid;
          place-items: center;
          z-index: 1000;
          padding: 1rem;
        }
        .glass {
          background: rgba(255, 255, 255, 0.85);
          backdrop-filter: blur(12px);
          border: 1px solid rgba(255, 255, 255, 0.3);
          box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25);
        }
        .comparison-content, .booking-modal {
          width: 100%;
          max-width: 800px;
          border-radius: 20px;
          padding: 2rem;
        }
        .comparison-header, .booking-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 2rem;
        }
        .comparison-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 1.5rem;
        }
        .comparison-col {
          padding: 1.5rem;
          background: white;
          border-radius: 12px;
          border: 1px solid var(--border);
        }
        .comp-item {
          margin: 0.75rem 0;
          font-size: 0.9rem;
        }
        .booking-steps {
          display: flex;
          gap: 1rem;
          margin-bottom: 2rem;
        }
        .step {
          flex: 1;
          height: 4px;
          background: var(--border);
          border-radius: 2px;
          font-size: 0.7rem;
          font-weight: 700;
          padding-top: 0.5rem;
          color: var(--text-muted);
        }
        .step.active {
          background: var(--primary);
          color: var(--primary);
        }
        .step-content {
          min-height: 200px;
          display: flex;
          flex-direction: column;
          gap: 1.5rem;
        }
        .form-select {
          padding: 0.75rem;
          border-radius: 8px;
          border: 1px solid var(--border);
        }
        .section-kicker {
          color: var(--primary);
          font-size: 0.82rem;
          font-weight: 800;
          text-transform: uppercase;
        }
        .pg-toolbar {
          display: flex;
          align-items: flex-end;
          justify-content: space-between;
          gap: 1rem;
          margin-bottom: 1rem;
        }
        .pg-toolbar h1 {
          max-width: 760px;
          font-size: clamp(1.7rem, 4vw, 2.6rem);
          line-height: 1.1;
          margin: 0.3rem 0;
        }
        .pg-toolbar p,
        .muted {
          color: var(--text-muted);
        }
        .toolbar-actions,
        .view-toggle {
          display: flex;
          align-items: center;
          gap: 0.65rem;
        }
        .view-toggle {
          background: white;
          border: 1px solid var(--border);
          border-radius: 8px;
          padding: 0.25rem;
        }
        .view-toggle button {
          width: 38px;
          height: 38px;
          border-radius: 7px;
          display: grid;
          place-items: center;
          color: var(--text-muted);
          background: transparent;
        }
        .view-toggle button.active {
          background: var(--primary);
          color: white;
        }
        .comparison-strip {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 0.75rem;
          margin-bottom: 1rem;
        }
        .comparison-strip div {
          background: white;
          border: 1px solid var(--border);
          border-radius: 8px;
          padding: 0.85rem 1rem;
        }
        .comparison-strip span,
        .price-panel span {
          display: block;
          color: var(--text-muted);
          font-size: 0.78rem;
          font-weight: 700;
          text-transform: uppercase;
        }
        .comparison-strip strong {
          display: block;
          margin-top: 0.2rem;
          font-size: 1.1rem;
        }
        .error-alert {
          border: 1px solid #fecaca;
          background: #fef2f2;
          color: #b91c1c;
          border-radius: 8px;
          padding: 0.8rem 1rem;
          margin-bottom: 1rem;
        }
        .pg-results {
          display: grid;
          gap: 1rem;
        }
        .pg-results.grid {
          grid-template-columns: repeat(2, minmax(0, 1fr));
        }
        .pg-results.list {
          grid-template-columns: 1fr;
        }
        .pg-card {
          display: grid;
          grid-template-columns: minmax(0, 1fr) 260px;
          gap: 1rem;
          background: white;
          border: 1px solid var(--border);
          border-radius: 8px;
          padding: 1rem;
          box-shadow: var(--shadow-sm);
        }
        .pg-results.grid .pg-card {
          grid-template-columns: 1fr;
        }
        .pg-card.not-available {
          border-color: #fecaca;
        }
        .pg-card-top {
          display: grid;
          grid-template-columns: auto 1fr auto;
          gap: 0.75rem;
          align-items: start;
        }
        .pg-icon {
          width: 44px;
          height: 44px;
          border-radius: 8px;
          display: grid;
          place-items: center;
          background: #eef2ff;
          color: var(--primary);
        }
        .pg-card h2 {
          font-size: 1.12rem;
          line-height: 1.2;
          margin-bottom: 0.25rem;
        }
        .pg-card h3 {
          font-size: 0.92rem;
          margin-bottom: 0.5rem;
        }
        .pg-card p,
        .pg-card-top p,
        .owner-row span,
        .rating-row span,
        .nearby-grid span,
        .score-grid span,
        .room-header span {
          display: flex;
          gap: 0.35rem;
          align-items: center;
        }
        .pg-card-top p,
        .owner-row,
        .rating-row,
        .nearby-grid,
        .pg-summary {
          color: var(--text-muted);
          font-size: 0.9rem;
        }
        .pg-summary {
          margin: 0.85rem 0;
        }
        .availability-pill,
        .comparison-badge {
          white-space: nowrap;
          border-radius: 999px;
          background: #ecfdf5;
          color: #047857;
          font-weight: 800;
          font-size: 0.76rem;
          padding: 0.32rem 0.58rem;
        }
        .availability-pill.danger,
        .comparison-badge.high {
          background: #fef2f2;
          color: #b91c1c;
        }
        .comparison-badge.low {
          background: #eff6ff;
          color: #1d4ed8;
        }
        .owner-row,
        .rating-row {
          display: flex;
          flex-wrap: wrap;
          gap: 0.75rem;
          margin-top: 0.7rem;
        }
        .score-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 0.5rem;
          margin-top: 0.9rem;
        }
        .score-grid span,
        .room-type {
          border: 1px solid var(--border);
          border-radius: 8px;
          padding: 0.55rem;
          font-weight: 700;
          font-size: 0.83rem;
        }
        .pg-card-side {
          display: grid;
          gap: 0.75rem;
        }
        .price-panel,
        .room-panel,
        .nearby-panel,
        .reviews-panel {
          border: 1px solid var(--border);
          border-radius: 8px;
          padding: 0.85rem;
          background: #fbfdff;
        }
        .price-panel strong {
          display: block;
          font-size: 1.45rem;
          margin: 0.15rem 0;
        }
        .price-panel small {
          display: block;
          color: var(--text-muted);
          margin-top: 0.35rem;
        }
        .comparison-badge {
          display: inline-flex;
          margin-top: 0.7rem;
        }
        .room-header,
        .room-type {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        .room-type {
          margin-top: 0.45rem;
          background: white;
        }
        .nearby-panel,
        .reviews-panel {
          grid-column: 1 / -1;
        }
        .nearby-grid {
          display: grid;
          grid-template-columns: repeat(4, minmax(0, 1fr));
          gap: 0.55rem;
        }
        .nearby-grid span {
          border: 1px solid var(--border);
          border-radius: 8px;
          min-height: 44px;
          padding: 0.5rem;
          background: white;
        }
        blockquote {
          border-left: 3px solid var(--primary);
          padding-left: 0.75rem;
          margin-top: 0.6rem;
          color: var(--text-muted);
        }
        blockquote strong {
          color: var(--text-main);
          font-size: 0.88rem;
        }
        blockquote p {
          margin-top: 0.25rem;
          font-size: 0.88rem;
        }
        .animate-spin {
          animation: spin 0.8s linear infinite;
        }
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
        @media (min-width: 900px) {
          .pg-results.grid .nearby-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
          }
        }
        @media (max-width: 780px) {
          .pg-search-page {
            padding-top: 1rem;
          }
          .pg-toolbar {
            align-items: stretch;
            flex-direction: column;
          }
          .toolbar-actions,
          .toolbar-actions .btn {
            width: 100%;
          }
          .toolbar-actions .btn {
            justify-content: center;
          }
          .view-toggle {
            flex: 1;
            justify-content: center;
          }
          .comparison-strip,
          .pg-results.grid,
          .pg-results.list,
          .score-grid,
          .nearby-grid {
            grid-template-columns: 1fr;
          }
          .pg-card {
            grid-template-columns: 1fr;
          }
          .pg-card-top {
            grid-template-columns: auto 1fr;
          }
          .availability-pill {
            grid-column: 1 / -1;
            width: fit-content;
          }
        }
      `}</style>
    </div>
  );
};

export default StudentPGSearch;
