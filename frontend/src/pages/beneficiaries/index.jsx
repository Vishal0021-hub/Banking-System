import React, { useEffect, useState } from 'react';
import { Card, Button, Input } from '../../components/ui';
import { Users, Plus, Trash2, Search, UserPlus, ShieldCheck } from 'lucide-react';
import api from '../../api/axios';
import toast from 'react-hot-toast';

export const Beneficiaries = () => {
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const [newBen, setNewBen] = useState({ name: '', accountNumber: '', bankName: '', ifscCode: '' });

  const fetchBeneficiaries = async () => {
    try {
      const res = await api.get('/beneficiaries');
      setBeneficiaries(res.data.data);
    } catch (err) {
      toast.error('Failed to load beneficiaries');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBeneficiaries();
  }, []);

  const handleAdd = async (e) => {
    e.preventDefault();
    try {
      await api.post('/beneficiaries', newBen);
      toast.success('Beneficiary added successfully');
      setShowAdd(false);
      setNewBen({ name: '', accountNumber: '', bankName: '', ifscCode: '' });
      fetchBeneficiaries();
    } catch (err) {
      if (err.response?.data?.data && typeof err.response.data.data === 'object') {
        Object.values(err.response.data.data).forEach(msg => toast.error(msg));
      } else {
        toast.error(err.response?.data?.message || 'Failed to add beneficiary');
      }
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to remove this beneficiary?')) return;
    try {
      await api.delete(`/beneficiaries/${id}`);
      toast.success('Beneficiary removed');
      fetchBeneficiaries();
    } catch (err) {
      toast.error('Failed to remove beneficiary');
    }
  };

  return (
    <div className="space-y-8 animate-in slide-in-from-right-4 duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white">Beneficiaries</h1>
          <p className="text-slate-400">Manage your frequent transfer recipients</p>
        </div>
        <Button onClick={() => setShowAdd(true)} className="gap-2">
          <UserPlus size={18} /> Add Beneficiary
        </Button>
      </div>

      {showAdd && (
        <Card className="p-8 animate-in zoom-in-95 duration-300">
          <form onSubmit={handleAdd} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Input
                label="Full Name"
                placeholder="Beneficiary Name"
                required
                value={newBen.name}
                onChange={(e) => setNewBen({ ...newBen, name: e.target.value })}
              />
              <Input
                label="Account Number"
                placeholder="16-digit account number"
                required
                value={newBen.accountNumber}
                onChange={(e) => setNewBen({ ...newBen, accountNumber: e.target.value })}
              />
              <Input
                label="Bank Name"
                placeholder="e.g. HDFC Bank"
                value={newBen.bankName}
                onChange={(e) => setNewBen({ ...newBen, bankName: e.target.value })}
              />
              <Input
                label="IFSC Code"
                placeholder="IFSC0001234"
                value={newBen.ifscCode}
                onChange={(e) => setNewBen({ ...newBen, ifscCode: e.target.value })}
              />
            </div>
            <div className="flex gap-4">
              <Button type="submit" className="flex-1">Save Beneficiary</Button>
              <Button type="button" variant="ghost" className="flex-1" onClick={() => setShowAdd(false)}>Cancel</Button>
            </div>
          </form>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {beneficiaries.map((ben) => (
          <Card key={ben.id} className="hover:border-primary/40 transition-all group">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-2xl bg-slate-800 flex items-center justify-center text-xl font-bold text-primary group-hover:bg-primary group-hover:text-white transition-all">
                  {ben.name.charAt(0)}
                </div>
                <div>
                  <h4 className="font-semibold text-slate-100">{ben.name}</h4>
                  <p className="text-xs text-slate-500 font-mono mt-1">{ben.accountNumber}</p>
                </div>
              </div>
              <button 
                onClick={() => handleDelete(ben.id)}
                className="p-2 text-slate-600 hover:text-red-500 hover:bg-red-500/10 rounded-lg transition-all opacity-0 group-hover:opacity-100"
              >
                <Trash2 size={18} />
              </button>
            </div>
            
            <div className="mt-6 pt-4 border-t border-white/5 grid grid-cols-2 gap-4">
              <div>
                <p className="text-[10px] text-slate-500 uppercase tracking-widest mb-1">Bank</p>
                <p className="text-xs font-medium text-slate-300">{ben.bankName || 'Enterprise Bank'}</p>
              </div>
              <div>
                <p className="text-[10px] text-slate-500 uppercase tracking-widest mb-1">Status</p>
                <div className="flex items-center gap-1 text-[10px] font-bold text-emerald-500 uppercase">
                   <ShieldCheck size={10} /> Verified
                </div>
              </div>
            </div>
          </Card>
        ))}

        {beneficiaries.length === 0 && !loading && (
          <div className="col-span-full py-20 text-center">
            <Users size={48} className="mx-auto text-slate-800 mb-4" />
            <p className="text-slate-500">No beneficiaries found</p>
          </div>
        )}
      </div>
    </div>
  );
};
