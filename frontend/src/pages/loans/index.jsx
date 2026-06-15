import React, { useEffect, useState } from 'react';
import { Card, Button, Input } from '../../components/ui';
import { 
  PiggyBank, 
  Plus, 
  Calculator, 
  Clock, 
  ArrowRight,
  TrendingUp,
  History,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';
import api from '../../api/axios';
import toast from 'react-hot-toast';

export const Loans = () => {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showApply, setShowApply] = useState(false);
  const [showCalc, setShowCalc] = useState(false);
  
  const [calcData, setCalcData] = useState({ principalAmount: '', annualInterestRate: '10', tenureMonths: '12' });
  const [calcResult, setCalcResult] = useState(null);

  const [loanApp, setLoanApp] = useState({ 
    loanType: 'PERSONAL', 
    accountNumber: '', 
    principalAmount: '', 
    tenureMonths: '12', 
    purpose: '' 
  });

  const fetchLoans = async () => {
    try {
      const res = await api.get('/loans/my-loans');
      setLoans(res.data.data);
    } catch (err) {
      toast.error('Failed to load loans');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLoans();
  }, []);

  const handleCalculate = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post('/loans/calculate-emi', calcData);
      setCalcResult(res.data.data);
    } catch (err) {
      toast.error('Calculation failed');
    }
  };

  const handleApply = async (e) => {
    e.preventDefault();
    try {
      await api.post('/loans/apply', loanApp);
      toast.success('Loan application submitted!');
      setShowApply(false);
      fetchLoans();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Application failed');
    }
  };

  return (
    <div className="space-y-8 animate-in slide-in-from-left-4 duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white">Loans & Credit</h1>
          <p className="text-slate-400">Finance your dreams with our flexible loan options</p>
        </div>
        <div className="flex gap-4">
          <Button variant="outline" onClick={() => setShowCalc(!showCalc)} className="gap-2">
            <Calculator size={18} /> EMI Calculator
          </Button>
          <Button onClick={() => setShowApply(true)} className="gap-2">
            <Plus size={18} /> Apply for Loan
          </Button>
        </div>
      </div>

      {/* EMI Calculator */}
      {showCalc && (
        <Card className="p-8 border-primary/20 bg-primary/5 animate-in slide-in-from-top-2 duration-300">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
            <form onSubmit={handleCalculate} className="space-y-4">
              <h4 className="font-bold text-lg mb-4 flex items-center gap-2"><Calculator className="text-primary" /> Instant Estimator</h4>
              <Input 
                label="Principal Amount (₹)" 
                type="number" 
                value={calcData.principalAmount} 
                onChange={(e) => setCalcData({...calcData, principalAmount: e.target.value})}
              />
              <div className="grid grid-cols-2 gap-4">
                <Input 
                  label="Interest Rate (%)" 
                  type="number" 
                  value={calcData.annualInterestRate} 
                  onChange={(e) => setCalcData({...calcData, annualInterestRate: e.target.value})}
                />
                <Input 
                  label="Tenure (Months)" 
                  type="number" 
                  value={calcData.tenureMonths} 
                  onChange={(e) => setCalcData({...calcData, tenureMonths: e.target.value})}
                />
              </div>
              <Button type="submit" className="w-full">Calculate EMI</Button>
            </form>
            {calcResult ? (
              <div className="flex flex-col justify-center space-y-6">
                <div className="text-center p-6 bg-slate-900/50 rounded-2xl border border-white/5">
                  <p className="text-slate-400 text-sm uppercase tracking-widest mb-2">Monthly EMI</p>
                  <h3 className="text-4xl font-bold text-primary">₹{Number(calcResult.monthlyEmi).toLocaleString()}</h3>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-4 bg-slate-900/50 rounded-xl border border-white/5">
                    <p className="text-slate-500 text-xs mb-1">Total Interest</p>
                    <p className="font-bold">₹{Number(calcResult.totalInterest).toLocaleString()}</p>
                  </div>
                  <div className="p-4 bg-slate-900/50 rounded-xl border border-white/5">
                    <p className="text-slate-500 text-xs mb-1">Total Payable</p>
                    <p className="font-bold">₹{Number(calcResult.totalPayable).toLocaleString()}</p>
                  </div>
                </div>
              </div>
            ) : (
              <div className="flex items-center justify-center border-2 border-dashed border-white/5 rounded-2xl text-slate-600 italic">
                Enter details to see estimation
              </div>
            )}
          </div>
        </Card>
      )}

      {/* Loan Application Modal-like form */}
      {showApply && (
        <Card className="p-8 border-primary animate-in zoom-in-95 duration-300">
           <form onSubmit={handleApply} className="space-y-6">
            <h3 className="text-xl font-bold mb-4">Loan Application Form</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-slate-300">Loan Type</label>
                <select 
                  className="flex h-10 w-full rounded-md border border-input bg-slate-900/50 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary/50"
                  value={loanApp.loanType}
                  onChange={(e) => setLoanApp({ ...loanApp, loanType: e.target.value })}
                >
                  <option value="PERSONAL">Personal Loan (12.5%)</option>
                  <option value="HOME">Home Loan (8.5%)</option>
                  <option value="VEHICLE">Vehicle Loan (9.0%)</option>
                  <option value="EDUCATION">Education Loan (7.5%)</option>
                </select>
              </div>
              <Input
                label="Disbursement Account Number"
                placeholder="Enter your account number"
                required
                value={loanApp.accountNumber}
                onChange={(e) => setLoanApp({ ...loanApp, accountNumber: e.target.value })}
              />
              <Input
                label="Required Amount (₹)"
                type="number"
                placeholder="Min ₹10,000"
                required
                value={loanApp.principalAmount}
                onChange={(e) => setLoanApp({ ...loanApp, principalAmount: e.target.value })}
              />
              <Input
                label="Tenure (Months)"
                type="number"
                placeholder="e.g. 24"
                required
                value={loanApp.tenureMonths}
                onChange={(e) => setLoanApp({ ...loanApp, tenureMonths: e.target.value })}
              />
            </div>
            <Input
              label="Purpose of Loan"
              placeholder="e.g. Home Renovation"
              value={loanApp.purpose}
              onChange={(e) => setLoanApp({ ...loanApp, purpose: e.target.value })}
            />
            <div className="flex gap-4">
              <Button type="submit" className="flex-1">Submit Application</Button>
              <Button type="button" variant="ghost" onClick={() => setShowApply(false)}>Cancel</Button>
            </div>
          </form>
        </Card>
      )}

      {/* Active Loans */}
      <div className="space-y-4">
        <h4 className="text-lg font-semibold flex items-center gap-2"><History size={20} className="text-primary" /> Loan History</h4>
        <div className="grid grid-cols-1 gap-4">
          {loans.map((loan) => (
            <Card key={loan.id} className="flex flex-col md:flex-row items-center justify-between gap-6 hover:bg-white/5 transition-colors">
              <div className="flex items-center gap-4 w-full md:w-auto">
                <div className="w-12 h-12 rounded-xl bg-slate-800 flex items-center justify-center text-primary">
                  <PiggyBank size={24} />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h5 className="font-bold text-slate-100">{loan.loanType} LOAN</h5>
                    <span className="text-[10px] text-slate-500 font-mono tracking-tighter bg-slate-900 px-1.5 py-0.5 rounded">{loan.loanNumber}</span>
                  </div>
                  <p className="text-xs text-slate-500">Principal: ₹{Number(loan.principalAmount).toLocaleString()} @ {loan.interestRate}%</p>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-8 flex-1 px-8 text-center md:text-left">
                <div>
                  <p className="text-[10px] text-slate-500 uppercase tracking-widest">Monthly EMI</p>
                  <p className="font-bold text-primary">₹{Number(loan.emiAmount).toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-[10px] text-slate-500 uppercase tracking-widest">Outstanding</p>
                  <p className="font-bold text-white">₹{Number(loan.outstandingAmount).toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-[10px] text-slate-500 uppercase tracking-widest">Next Due</p>
                  <p className="font-bold text-slate-300">{loan.nextEmiDate || 'N/A'}</p>
                </div>
              </div>

              <div className="flex items-center gap-3 w-full md:w-auto justify-end">
                <div className={cn(
                  "px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest flex items-center gap-1",
                  loan.status === 'ACTIVE' || loan.status === 'DISBURSED' ? "bg-emerald-500/10 text-emerald-500" :
                  loan.status === 'PENDING' ? "bg-amber-500/10 text-amber-500" : "bg-red-500/10 text-red-500"
                )}>
                  {loan.status === 'PENDING' ? <Clock size={12} /> : loan.status === 'ACTIVE' ? <CheckCircle2 size={12} /> : <AlertCircle size={12} />}
                  {loan.status}
                </div>
                <Button variant="ghost" size="sm" className="text-slate-400"><ArrowRight size={18} /></Button>
              </div>
            </Card>
          ))}

          {loans.length === 0 && !loading && (
             <div className="py-20 text-center space-y-4 border-2 border-dashed border-white/5 rounded-2xl">
               <PiggyBank size={48} className="mx-auto text-slate-800" />
               <p className="text-slate-500">No active or past loans found.</p>
               <Button variant="outline" onClick={() => setShowApply(true)}>Apply for a Loan</Button>
             </div>
          )}
        </div>
      </div>
    </div>
  );
};
