import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import LandingPage from './pages/LandingPage';
import Login from './pages/Login';
import Register from './pages/Register';
import AdminDashboard from './pages/AdminDashboard';
import TenantDashboard from './pages/TenantDashboard';
import StudentOTPLogin from './pages/StudentOTPLogin';
import StudentPGSearch from './pages/StudentPGSearch';
import ProtectedRoute from './components/ProtectedRoute';
import OwnerDashboard from './pages/OwnerDashboard';

function App() {
  return (
    <Router>
      <div className="app-container">
        <Navbar />
        <main>
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<Login />} />
            <Route path="/student/login" element={<StudentOTPLogin />} />
            <Route path="/register" element={<Register />} />
            
            <Route 
              path="/admin/*" 
              element={
                <ProtectedRoute role={['ADMIN', 'OWNER']}>
                  <AdminDashboard />
                </ProtectedRoute>
              } 
            />
            
            <Route 
              path="/tenant/*" 
              element={
                <ProtectedRoute role="TENANT">
                  <TenantDashboard />
                </ProtectedRoute>
              } 
            />

            <Route 
              path="/student/search" 
              element={
                <ProtectedRoute role="STUDENT">
                  <StudentPGSearch />
                </ProtectedRoute>
              } 
            />

            <Route
              path="/owner/*"
              element={
                <ProtectedRoute role="OWNER">
                  <OwnerDashboard />
                </ProtectedRoute>
              }
            />

            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
