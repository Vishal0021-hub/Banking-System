import React, { useEffect, useState } from 'react';
import { Card, Button, Input, cn } from '../../components/ui';
import { 
  ArrowRightLeft, 
  Search, 
  User, 
  ArrowRight, 
  History, 
  ShieldCheck, 
  Info,
  ChevronRight
} from 'lucide-react';
import api from '../../api/axios';
import toast from 'react-hot-toast';

export const Transfer = () => {
  const [accounts, setAccounts] = useState([]);
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    fromAccountNumber: '',
    toAccountNumber: '',
    amount: '',
    description: ''
  });

  const fetchData = async () => {
    try {
      const [accRes, benRes] = await Promise.all([
        api.get('/accounts'),
        api.get('/beneficiaries')
      ]);
      setAccounts(accRes.data.data);
      setBeneficiaries(benRes.data.data);
      return accRes.data.data;
    } catch (err) {
      toast.error('Failed to load data');
      return [];
    }
  };

  useEffect(() => {
    fetchData().then((accs) => {
      if (accs && accs.length > 0) {
        setFormData(prev => ({ ...prev, fromAccountNumber: accs[0].accountNumber }));
      }
    });
  }, []);

  const handleTransfer = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await api.post('/transactions/transfer', formData);
      const txn = res.data.data;
      
      if (txn.status === 'FLAGGED') {
        toast.error(`Transfer FLAGGED for Fraud: ${txn.fraudReason}`, { duration: 6000 });
      } else {
        toast.success('Transfer initiated successfully!');
        setFormData(prev => ({ ...prev, toAccountNumber: '', amount: '', description: '' }));
      }
      await fetchData();
    } catch (err) {
      if (err.response?.data?.message) {
        toast.error(err.response.data.message);
      } else {
        toast.error('Transfer failed');
      }
    } finally {
      setLoading(false);
    }
  };

  const selectedAccount = accounts.find(a => a.accountNumber === formData.fromAccountNumber);

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-in fade-in slide-in-from-top-4 duration-700">
      <div className="text-center space-y-2">
        <h1 className="text-3xl font-bold text-white">Fund Transfer</h1>
        <p className="text-slate-400">Transfer money securely between accounts or to beneficiaries</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* Transfer Form */}
        <Card className="md:col-span-2 p-8 space-y-8">
          <form onSubmit={handleTransfer} className="space-y-6">
            <div className="space-y-4">
              <label className="text-sm font-medium text-slate-300">From Account</label>
              <div className="grid grid-cols-1 gap-3">
                {accounts.map((acc) => (
                  <div 
                    key={acc.id}
                    onClick={() => setFormData({ ...formData, fromAccountNumber: acc.accountNumber })}
                    className={cn(
                      "flex items-center justify-between p-4 rounded-xl border cursor-pointer transition-all",
                      formData.fromAccountNumber === acc.accountNumber 
                        ? "bg-primary/10 border-primary ring-1 ring-primary/50 shadow-lg shadow-primary/10" 
                        : "bg-slate-900/50 border-white/5 hover:border-white/10"
                    )}
                  >
                    <div className="flex items-center gap-3">
                      <div className={cn("p-2 rounded-lg", formData.fromAccountNumber === acc.accountNumber ? "bg-primary text-white" : "bg-slate-800 text-slate-400")}>
                        <ShieldCheck size={20} />
                      </div>
                      <div>
                        <p className="font-medium text-slate-100">{acc.accountType} Account</p>
                        <p className="text-xs text-slate-500">{acc.accountNumber}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-slate-100">₹{Number(acc.balance).toLocaleString()}</p>
                      <p className="text-[10px] text-slate-500 uppercase tracking-widest">Available</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Input
                label="Destination Account Number"
                placeholder="Enter 16-digit account number"
                required
                value={formData.toAccountNumber}
                onChange={(e) => setFormData({ ...formData, toAccountNumber: e.target.value })}
              />
              <Input
                label="Amount (INR)"
                type="number"
                placeholder="0.00"
                required
                min="1"
                value={formData.amount}
                onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              />
            </div>

            <Input
              label="Description (Optional)"
              placeholder="e.g. Monthly Rent, Food, etc."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />

            <div className="bg-slate-900/80 rounded-xl p-4 border border-white/5 flex items-start gap-3">
              <Info className="text-primary mt-0.5" size={18} />
              <div className="text-xs text-slate-400 leading-relaxed">
                <p className="font-medium text-slate-200 mb-1">Important Note:</p>
                Transfers are subject to fraud detection rules. High-value transactions may be flagged for manual review. Ensure the recipient details are correct before proceeding.
              </div>
            </div>

            <Button type="submit" className="w-full h-12 gap-2" disabled={loading || !formData.toAccountNumber || !formData.amount}>
              {loading ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <>
                  Confirm Transfer <ArrowRight size={18} />
                </>
              )}
            </Button>
          </form>
        </Card>

        {/* Sidebar / Beneficiaries */}
        <div className="space-y-6">
          <Card className="p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h4 className="font-semibold text-slate-100">Quick Select</h4>
              <Button variant="ghost" size="sm" className="text-xs text-primary">Manage</Button>
            </div>
            <div className="space-y-3">
              {beneficiaries.length === 0 && (
                <p className="text-xs text-slate-500 text-center py-4 italic">No beneficiaries added yet</p>
              )}
              {beneficiaries.map((ben) => (
                <div 
                  key={ben.id}
                  onClick={() => setFormData({ ...formData, toAccountNumber: ben.accountNumber })}
                  className="flex items-center gap-3 p-3 rounded-lg bg-slate-900/50 border border-white/5 hover:border-primary/30 cursor-pointer transition-all group"
                >
                  <div className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center text-primary font-bold group-hover:bg-primary group-hover:text-white transition-colors">
                    {ben.name.charAt(0)}
                  </div>
                  <div className="flex-1 overflow-hidden">
                    <p className="text-sm font-medium text-slate-200 truncate">{ben.name}</p>
                    <p className="text-[10px] text-slate-500 font-mono">{ben.accountNumber}</p>
                  </div>
                  <ChevronRight size={14} className="text-slate-600 group-hover:text-primary transition-colors" />
                </div>
              ))}
            </div>
          </Card>

          <Card className="p-6 bg-gradient-to-br from-indigo-950/40 to-slate-950/40 border-indigo-500/20">
            <h4 className="font-semibold text-slate-100 mb-2">Transfer Limits</h4>
            <div className="space-y-3">
              <div className="space-y-1">
                <div className="flex justify-between text-[10px] uppercase tracking-wider text-slate-500 font-bold">
                  <span>Daily Limit</span>
                  <span>₹{selectedAccount ? Number(selectedAccount.dailyTransferLimit).toLocaleString() : '50,000'}</span>
                </div>
                <div className="h-1.5 w-full bg-slate-800 rounded-full overflow-hidden">
                   <div 
                    className="h-full bg-primary" 
                    style={{ width: selectedAccount ? `${(Number(selectedAccount.transferredToday) / Number(selectedAccount.dailyTransferLimit)) * 100}%` : '0%' }}
                   />
                </div>
                <p className="text-[10px] text-slate-400">Used: ₹{selectedAccount ? Number(selectedAccount.transferredToday).toLocaleString() : '0'}</p>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};
