import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { Layout } from './components/layout';
import { Login } from './pages/auth/Login';
import { Signup } from './pages/auth/Signup';
import { Dashboard } from './pages/dashboard';
import { Accounts } from './pages/accounts';
import { Transfer } from './pages/transfer';
import { Beneficiaries } from './pages/beneficiaries';
import { Loans } from './pages/loans';
import { Notifications } from './pages/notifications';
import { Admin } from './pages/admin';
import useAuthStore from './store/authStore';
import api from './api/axios';

const ProtectedRoute = ({ children, adminOnly = false }) => {
  const { isAuthenticated, user } = useAuthStore();
  if (!isAuthenticated) return <Navigate to="/login" />;
  if (adminOnly && user?.role !== 'ADMIN') return <Navigate to="/dashboard" />;
  return <Layout>{children}</Layout>;
};

const PublicRoute = ({ children }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  return !isAuthenticated ? children : <Navigate to="/dashboard" />;
};

function App() {
  React.useEffect(() => {
    const checkSession = async () => {
      const { isAuthenticated, logout } = useAuthStore.getState();
      if (isAuthenticated) {
        try {
          await api.get('/users/me');
        } catch (err) {
          logout();
        }
      }
    };
    checkSession();
  }, []);

  return (
    <BrowserRouter>
      <Toaster 
        position="top-right"
        toastOptions={{
          style: {
            background: '#0f172a',
            color: '#fff',
            border: '1px solid rgba(255,255,255,0.1)',
            backdropFilter: 'blur(8px)',
          },
        }} 
      />
      <Routes>
        <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
        <Route path="/signup" element={<PublicRoute><Signup /></PublicRoute>} />
        
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/accounts" element={<ProtectedRoute><Accounts /></ProtectedRoute>} />
        <Route path="/transfer" element={<ProtectedRoute><Transfer /></ProtectedRoute>} />
        <Route path="/beneficiaries" element={<ProtectedRoute><Beneficiaries /></ProtectedRoute>} />
        <Route path="/loans" element={<ProtectedRoute><Loans /></ProtectedRoute>} />
        <Route path="/notifications" element={<ProtectedRoute><Notifications /></ProtectedRoute>} />
        <Route path="/admin" element={<ProtectedRoute adminOnly><Admin /></ProtectedRoute>} />
        
        <Route path="/" element={<Navigate to="/dashboard" />} />
        <Route path="*" element={<div className="flex items-center justify-center h-screen text-slate-500">404 | Page Not Found</div>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
