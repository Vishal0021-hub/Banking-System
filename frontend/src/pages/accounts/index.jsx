import React, { useEffect, useState } from 'react';
import { Card, Button, Input } from '../../components/ui';
import { 
  CreditCard, 
  Plus, 
  MoreVertical, 
  ExternalLink, 
  Copy, 
  CheckCircle2, 
  AlertCircle 
} from 'lucide-react';
import api from '../../api/axios';
import toast from 'react-hot-toast';

export const Accounts = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [newAccount, setNewAccount] = useState({ accountType: 'SAVINGS', currency: 'INR' });

  const fetchAccounts = async () => {
    try {
      const res = await api.get('/accounts');
      setAccounts(res.data.data);
    } catch (err) {
      toast.error('Failed to fetch accounts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.post('/accounts', newAccount);
      toast.success('Account created successfully');
      setShowCreate(false);
      fetchAccounts();
    } catch (err) {
      if (err.response?.data?.data && typeof err.response.data.data === 'object') {
        Object.values(err.response.data.data).forEach(msg => toast.error(msg));
      } else {
        toast.error(err.response?.data?.message || 'Failed to create account');
      }
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    toast.success('Account number copied!');
  };

  return (
    <div className="space-y-8 animate-in slide-in-from-bottom-4 duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white">Your Accounts</h1>
          <p className="text-slate-400">Manage and view all your active banking accounts</p>
        </div>
        <Button onClick={() => setShowCreate(true)} className="gap-2">
          <Plus size={18} /> Open New Account
        </Button>
      </div>

      {showCreate && (
        <Card className="border-primary/30 animate-in zoom-in-95 duration-300">
          <form onSubmit={handleCreate} className="grid grid-cols-1 md:grid-cols-3 gap-6 items-end">
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-300">Account Type</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input bg-slate-900/50 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary/50"
                value={newAccount.accountType}
                onChange={(e) => setNewAccount({ ...newAccount, accountType: e.target.value })}
              >
                <option value="SAVINGS">Savings Account</option>
                <option value="CURRENT">Current Account</option>
                <option value="FIXED_DEPOSIT">Fixed Deposit</option>
              </select>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-300">Currency</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input bg-slate-900/50 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary/50"
                value={newAccount.currency}
                onChange={(e) => setNewAccount({ ...newAccount, currency: e.target.value })}
              >
                <option value="INR">Indian Rupee (INR)</option>
                <option value="USD">US Dollar (USD)</option>
                <option value="EUR">Euro (EUR)</option>
              </select>
            </div>
            <div className="flex gap-2">
              <Button type="submit" className="flex-1">Create</Button>
              <Button type="button" variant="ghost" onClick={() => setShowCreate(false)}>Cancel</Button>
            </div>
          </form>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {loading && [1, 2, 3].map(i => (
          <Card key={i} className="h-64 animate-shimmer" />
        ))}
        
        {accounts.map((acc) => (
          <Card key={acc.id} className="relative group overflow-hidden hover:border-primary/50 transition-all">
            <div className="absolute top-0 right-0 w-32 h-32 bg-primary/5 rounded-full -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-700" />
            
            <div className="space-y-6 relative z-10">
              <div className="flex items-center justify-between">
                <div className="p-2 bg-primary/10 rounded-lg text-primary">
                  <CreditCard size={24} />
                </div>
                <div className="flex items-center gap-1">
                   {acc.status === 'ACTIVE' ? (
                     <span className="flex items-center gap-1 text-[10px] font-bold text-emerald-500 bg-emerald-500/10 px-2 py-0.5 rounded-full uppercase tracking-wider">
                       <CheckCircle2 size={10} /> Active
                     </span>
                   ) : (
                     <span className="flex items-center gap-1 text-[10px] font-bold text-amber-500 bg-amber-500/10 px-2 py-0.5 rounded-full uppercase tracking-wider">
                       <AlertCircle size={10} /> Frozen
                     </span>
                   )}
                   <button className="text-slate-500 hover:text-slate-300"><MoreVertical size={18} /></button>
                </div>
              </div>

              <div>
                <p className="text-slate-400 text-sm font-medium">{acc.accountType} Account</p>
                <div className="flex items-center gap-2 mt-1">
                  <h3 className="text-xl font-mono text-slate-100 tracking-wider">
                    {acc.accountNumber.replace(/(.{4})/g, '$1 ')}
                  </h3>
                  <button onClick={() => copyToClipboard(acc.accountNumber)} className="text-slate-600 hover:text-primary transition-colors">
                    <Copy size={14} />
                  </button>
                </div>
              </div>

              <div className="pt-4 border-t border-white/5 flex items-end justify-between">
                <div className="space-y-0.5">
                   <p className="text-slate-500 text-[10px] uppercase tracking-widest">Available Balance</p>
                   <p className="text-3xl font-bold text-white">
                     <span className="text-primary text-xl mr-1">₹</span>
                     {Number(acc.balance).toLocaleString()}
                   </p>
                </div>
                <Button variant="ghost" size="sm" className="text-primary group/btn">
                  View Details <ExternalLink size={14} className="ml-1 group-hover/btn:translate-x-0.5 group-hover/btn:-translate-y-0.5 transition-transform" />
                </Button>
              </div>
            </div>
          </Card>
        ))}

        {accounts.length === 0 && !loading && (
          <div className="col-span-full py-20 text-center space-y-4">
             <div className="w-16 h-16 bg-slate-900 rounded-full flex items-center justify-center mx-auto text-slate-600">
               <CreditCard size={32} />
             </div>
             <p className="text-slate-500">You don't have any bank accounts yet.</p>
             <Button variant="outline" onClick={() => setShowCreate(true)}>Create your first account</Button>
          </div>
        )}
      </div>
    </div>
  );
};
