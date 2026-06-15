import React, { useEffect, useState } from 'react';
import { Card, Button } from '../../components/ui';
import { 
  Users, 
  CreditCard, 
  ShieldAlert, 
  Search, 
  Filter, 
  Lock, 
  Unlock, 
  CheckCircle2, 
  XCircle,
  Activity,
  ArrowUpRight
} from 'lucide-react';
import api from '../../api/axios';
import toast from 'react-hot-toast';

export const Admin = () => {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [flagged, setFlagged] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        const [statsRes, usersRes, flaggedRes] = await Promise.all([
          api.get('/admin/dashboard-stats'),
          api.get('/admin/users'),
          api.get('/admin/transactions/flagged')
        ]);
        setStats(statsRes.data.data);
        setUsers(usersRes.data.data);
        setFlagged(flaggedRes.data.data);
      } catch (err) {
        toast.error('Unauthorized or failed to load admin data');
      } finally {
        setLoading(false);
      }
    };
    fetchAdminData();
  }, []);

  const handleToggleLock = async (userId, currentLock) => {
    try {
      await api.patch(`/admin/users/${userId}/lock?lock=${!currentLock}`);
      toast.success(currentLock ? 'User unlocked' : 'User locked');
      // Refresh user list
      const usersRes = await api.get('/admin/users');
      setUsers(usersRes.data.data);
    } catch (err) {
      toast.error('Action failed');
    }
  };

  const handleUpdateKyc = async (userId, status) => {
    try {
      await api.patch(`/admin/users/${userId}/kyc?status=${status}`);
      toast.success('KYC Status updated');
      // Refresh
      const usersRes = await api.get('/admin/users');
      setUsers(usersRes.data.data);
    } catch (err) {}
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white flex items-center gap-3">
            <ShieldAlert className="text-primary" size={32} /> Central Command
          </h1>
          <p className="text-slate-400">Enterprise banking administration and oversight</p>
        </div>
        <div className="flex gap-4">
           <Button variant="outline" size="sm" className="gap-2"><Activity size={16} /> System Logs</Button>
           <Button size="sm" className="gap-2">Export Data</Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {[
          { label: 'Total Users', value: stats?.totalUsers, icon: Users, color: 'text-sky-500' },
          { label: 'Active Accounts', value: stats?.activeAccounts, icon: CreditCard, color: 'text-emerald-500' },
          { label: 'Fraud Alerts', value: stats?.flaggedToday, icon: ShieldAlert, color: 'text-red-500' },
          { label: 'Pending Loans', value: stats?.pendingLoans, icon: Activity, color: 'text-amber-500' },
        ].map((s, i) => (
          <Card key={i} className="flex items-center gap-4">
            <div className={`p-3 rounded-xl bg-slate-900 border border-white/5 ${s.color}`}>
              <s.icon size={24} />
            </div>
            <div>
              <p className="text-xs text-slate-500 uppercase tracking-widest">{s.label}</p>
              <h3 className="text-xl font-bold text-white">{s.value || '...'}</h3>
            </div>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* User Management Table */}
        <Card className="lg:col-span-2 overflow-hidden">
          <div className="flex items-center justify-between mb-6 px-2">
             <h4 className="font-bold text-lg">User Directory</h4>
             <div className="flex gap-2">
               <div className="relative">
                 <Search className="absolute left-2.5 top-2.5 text-slate-500" size={14} />
                 <input placeholder="Search users..." className="pl-9 pr-4 py-1.5 bg-slate-900 border border-white/5 rounded-md text-xs outline-none focus:border-primary/50" />
               </div>
               <Button variant="ghost" size="sm"><Filter size={16} /></Button>
             </div>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-white/5 text-slate-500 text-[10px] uppercase tracking-widest font-bold">
                  <th className="pb-4 px-4">User</th>
                  <th className="pb-4 px-4">KYC Status</th>
                  <th className="pb-4 px-4">Account Status</th>
                  <th className="pb-4 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {users.map((user) => (
                  <tr key={user.id} className="group hover:bg-white/[0.02] transition-colors">
                    <td className="py-4 px-4">
                       <div className="flex items-center gap-3">
                         <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-[10px] font-bold text-primary">{user.fullName.charAt(0)}</div>
                         <div>
                           <p className="text-sm font-medium text-slate-200">{user.fullName}</p>
                           <p className="text-[10px] text-slate-500">{user.email}</p>
                         </div>
                       </div>
                    </td>
                    <td className="py-4 px-4">
                       <span className={cn(
                         "px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider",
                         user.kycStatus === 'VERIFIED' ? "bg-emerald-500/10 text-emerald-500" :
                         user.kycStatus === 'PENDING' ? "bg-amber-500/10 text-amber-500" : "bg-red-500/10 text-red-500"
                       )}>
                         {user.kycStatus}
                       </span>
                    </td>
                    <td className="py-4 px-4">
                       <span className={cn(
                         "flex items-center gap-1 text-[10px] font-bold uppercase tracking-wider",
                         !user.accountLocked ? "text-emerald-500" : "text-red-500"
                       )}>
                         {!user.accountLocked ? <CheckCircle2 size={10} /> : <XCircle size={10} />}
                         {!user.accountLocked ? 'Active' : 'Locked'}
                       </span>
                    </td>
                    <td className="py-4 px-4 text-right space-x-2">
                       {user.kycStatus === 'PENDING' && (
                         <Button variant="ghost" size="sm" onClick={() => handleUpdateKyc(user.id, 'VERIFIED')} className="text-emerald-500 h-7 text-xs">Verify</Button>
                       )}
                       <Button 
                        variant="ghost" 
                        size="sm" 
                        onClick={() => handleToggleLock(user.id, user.accountLocked)}
                        className={cn("h-7 text-xs", user.accountLocked ? "text-emerald-500" : "text-red-500")}
                       >
                         {user.accountLocked ? <Unlock size={14} /> : <Lock size={14} />}
                       </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        {/* Fraud Monitoring Feed */}
        <Card className="space-y-6">
          <div className="flex items-center justify-between">
             <h4 className="font-bold text-lg">Fraud Alerts</h4>
             <span className="text-red-500 animate-pulse flex items-center gap-1 text-xs font-bold">
               <Activity size={14} /> Live
             </span>
          </div>
          <div className="space-y-4">
            {flagged.map((txn, i) => (
              <div key={i} className="p-4 rounded-xl bg-red-500/5 border border-red-500/20 space-y-3">
                <div className="flex justify-between items-start">
                  <div className="flex items-center gap-2 text-red-400">
                    <ShieldAlert size={16} />
                    <span className="text-xs font-bold uppercase tracking-wider">High Risk</span>
                  </div>
                  <span className="text-[10px] text-slate-500 font-mono">{txn.referenceNumber.substring(0,8)}</span>
                </div>
                <p className="text-sm font-medium text-slate-200">₹{Number(txn.amount).toLocaleString()} Transfer</p>
                <p className="text-xs text-slate-500">Reason: {txn.fraudReason || 'Velocity check failed'}</p>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" className="h-7 text-[10px] border-red-500/50 text-red-400 hover:bg-red-500/10 w-full">Block Account</Button>
                  <Button variant="ghost" size="sm" className="h-7 text-[10px] w-full">Dismiss</Button>
                </div>
              </div>
            ))}
            {flagged.length === 0 && (
              <div className="py-10 text-center text-slate-600 space-y-2">
                <CheckCircle2 size={32} className="mx-auto text-emerald-500/30" />
                <p className="text-sm italic">No flagged transactions today</p>
              </div>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
};
