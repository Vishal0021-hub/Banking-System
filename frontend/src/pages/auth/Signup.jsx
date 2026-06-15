import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldCheck, ArrowRight, User, Mail, Phone, Lock } from 'lucide-react';
import { Button, Input, Card } from '../../components/ui';
import api from '../../api/axios';
import toast from 'react-hot-toast';

export const Signup = () => {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
    panNumber: '',
    aadharNumber: ''
  });
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/auth/register', formData);
      toast.success('Registration successful! Please login.');
      navigate('/login');
    } catch (err) {
      if (err.response?.data?.data && typeof err.response.data.data === 'object') {
        // Display specific validation errors
        Object.values(err.response.data.data).forEach(msg => toast.error(msg));
      } else {
        toast.error(err.response?.data?.message || 'Registration failed');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-6 relative overflow-hidden">
      <div className="absolute top-0 -right-4 w-96 h-96 bg-primary/20 rounded-full blur-[128px] -z-10" />
      <div className="absolute bottom-0 -left-4 w-96 h-96 bg-emerald-500/10 rounded-full blur-[128px] -z-10" />

      <div className="w-full max-w-2xl space-y-8">
        <div className="text-center space-y-2">
          <div className="inline-flex w-14 h-14 rounded-2xl premium-gradient items-center justify-center shadow-xl mb-4">
            <ShieldCheck className="text-white" size={28} />
          </div>
          <h1 className="text-3xl font-bold tracking-tight text-white">Join Enterprise <span className="text-primary">Bank</span></h1>
          <p className="text-slate-400">Complete your profile to start your journey</p>
        </div>

        <Card className="p-8">
          <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Input
              label="Full Name"
              placeholder="John Doe"
              required
              value={formData.fullName}
              onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
            />
            <Input
              label="Email Address"
              type="email"
              placeholder="john@example.com"
              required
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
            <Input
              label="Phone Number"
              placeholder="+91 9876543210"
              required
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            />
            <Input
              label="Password"
              type="password"
              placeholder="••••••••"
              required
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            />
            <div className="md:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-6 border-t border-white/5 pt-6">
               <Input
                label="PAN Number (Optional)"
                placeholder="ABCDE1234F"
                value={formData.panNumber}
                onChange={(e) => setFormData({ ...formData, panNumber: e.target.value })}
              />
              <Input
                label="Aadhar Number (Optional)"
                placeholder="1234 5678 9012"
                value={formData.aadharNumber}
                onChange={(e) => setFormData({ ...formData, aadharNumber: e.target.value })}
              />
            </div>

            <div className="md:col-span-2">
              <Button type="submit" className="w-full h-12 gap-2 mt-2" disabled={loading}>
                {loading ? (
                  <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                ) : (
                  <>
                    Create Account <ArrowRight size={18} />
                  </>
                )}
              </Button>
            </div>
          </form>

          <div className="mt-8 text-center border-t border-white/5 pt-6">
            <p className="text-slate-400 text-sm">
              Already have an account?{' '}
              <Link to="/login" className="text-primary hover:underline font-bold">
                Login here
              </Link>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
};
