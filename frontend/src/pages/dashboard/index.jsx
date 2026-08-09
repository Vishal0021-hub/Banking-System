import React, { useEffect, useState } from 'react';
import { Card, Button, cn } from '../../components/ui';
import {
  TrendingUp,
  TrendingDown,
  ArrowUpRight,
  Plus,
  ArrowRight,
  Clock,
  ShieldAlert
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';
import api from '../../api/axios';
import toast from 'react-hot-toast';

const data = [
  { name: 'Mon', value: 4000 },
  { name: 'Tue', value: 3000 },
  { name: 'Wed', value: 5000 },
  { name: 'Thu', value: 2780 },
  { name: 'Fri', value: 1890 },
  { name: 'Sat', value: 2390 },
  { name: 'Sun', value: 3490 },
];

export const Dashboard = () => {
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        let fetchedAccounts = [];
        try {
          const accRes = await api.get('/accounts');
          fetchedAccounts = accRes.data?.data || [];
        } catch (err) {
          console.error('Failed to load accounts:', err);
        }
        setAccounts(fetchedAccounts);

        let fetchedTransactions = [];
        try {
          const txnRes = await api.get('/transactions?size=5');
          fetchedTransactions = txnRes.data?.data?.content || [];
        } catch (err) {
          console.error('Failed to load transactions:', err);
        }
        setTransactions(fetchedTransactions);
      } catch (err) {
        toast.error('Failed to load dashboard data');
        setAccounts([]);
        setTransactions([]);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const totalBalance = accounts.reduce((sum, acc) => sum + Number(acc.balance), 0);

  return (
    <div className="space-y-8 animate-in fade-in duration-700">
      {/* Top Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="premium-gradient text-white border-none relative overflow-hidden">
          <div className="absolute -right-8 -top-8 w-32 h-32 bg-white/10 rounded-full blur-2xl" />
          <div className="relative z-10 space-y-4">
            <p className="text-sky-100/80 font-medium">Total Balance</p>
            <h3 className="text-4xl font-bold">₹{totalBalance.toLocaleString()}</h3>
            <div className="flex items-center gap-2 text-sky-100 text-sm">
              <TrendingUp size={16} />
              <span>+2.5% from last month</span>
            </div>
          </div>
        </Card>

        <Card className="flex flex-col justify-between">
          <div className="space-y-1">
            <p className="text-slate-400 text-sm font-medium">Active Accounts</p>
            <h3 className="text-2xl font-bold">{accounts.length}</h3>
          </div>
          <div className="flex -space-x-2 mt-4">
            {accounts.slice(0, 3).map((_, i) => (
              <div key={i} className="w-8 h-8 rounded-full border-2 border-slate-900 premium-gradient" />
            ))}
            {accounts.length > 3 && (
              <div className="w-8 h-8 rounded-full border-2 border-slate-900 bg-slate-800 flex items-center justify-center text-[10px] font-bold">
                +{accounts.length - 3}
              </div>
            )}
          </div>
        </Card>

        <Card className="flex flex-col justify-between">
          <div className="space-y-1">
            <p className="text-slate-400 text-sm font-medium">Transactions (24h)</p>
            <h3 className="text-2xl font-bold">{transactions.length}</h3>
          </div>
          <div className="text-emerald-500 text-sm font-medium flex items-center gap-1">
            <ShieldAlert size={14} /> Secure & Audited
          </div>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Chart */}
        <Card className="lg:col-span-2 space-y-6">
          <div className="flex items-center justify-between">
            <h4 className="text-lg font-semibold">Spending Analysis</h4>
            <select className="bg-slate-800 border-none rounded-md text-sm px-2 py-1 outline-none">
              <option>Last 7 Days</option>
              <option>Last 30 Days</option>
            </select>
          </div>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={data}>
                <defs>
                  <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0ea5e9" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#0ea5e9" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#1e293b" />
                <XAxis dataKey="name" stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(v) => `₹${v}`} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', border: '1px solid #1e293b', borderRadius: '8px' }}
                  itemStyle={{ color: '#0ea5e9' }}
                />
                <Area type="monotone" dataKey="value" stroke="#0ea5e9" strokeWidth={3} fillOpacity={1} fill="url(#colorValue)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Recent Transactions */}
        <Card className="space-y-6">
          <div className="flex items-center justify-between">
            <h4 className="text-lg font-semibold">Recent Activity</h4>
            <Button variant="ghost" size="sm" className="text-primary hover:text-primary/80">View All</Button>
          </div>
          <div className="space-y-4">
            {transactions.length === 0 && !loading && (
              <p className="text-slate-500 text-sm text-center py-10">No recent transactions</p>
            )}
            {transactions.map((txn) => (
              <div key={txn.id} className="flex items-center justify-between group cursor-pointer hover:bg-white/5 p-2 rounded-lg transition-colors">
                <div className="flex items-center gap-3">
                  <div className={cn(
                    "w-10 h-10 rounded-full flex items-center justify-center",
                    txn.transactionType === 'DEPOSIT' ? "bg-emerald-500/10 text-emerald-500" : "bg-red-500/10 text-red-500"
                  )}>
                    {txn.transactionType === 'DEPOSIT' ? <TrendingUp size={20} /> : <TrendingDown size={20} />}
                  </div>
                  <div>
                    <p className="font-medium text-sm text-slate-200">{txn.description}</p>
                    <p className="text-xs text-slate-500 flex items-center gap-1">
                      <Clock size={10} /> {new Date(txn.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className={cn(
                    "font-bold text-sm",
                    txn.transactionType === 'DEPOSIT' ? "text-emerald-500" : "text-slate-200"
                  )}>
                    {txn.transactionType === 'DEPOSIT' ? '+' : '-'}₹{Number(txn.amount).toLocaleString()}
                  </p>
                  {txn.status === 'SUCCESS' && <span className="text-[10px] text-emerald-500/80 bg-emerald-500/10 px-1.5 rounded uppercase tracking-wider font-bold">Cleared</span>}
                </div>
              </div>
            ))}
          </div>
          <Button className="w-full gap-2 mt-4" variant="outline">
            Initiate Transfer <ArrowUpRight size={18} />
          </Button>
        </Card>
      </div>
    </div>
  );
};
