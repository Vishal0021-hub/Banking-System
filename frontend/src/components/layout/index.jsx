import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { 
  LayoutDashboard, 
  CreditCard, 
  ArrowRightLeft, 
  Users, 
  PiggyBank, 
  Bell, 
  ShieldCheck, 
  LogOut,
  Menu,
  X,
  ChevronRight
} from 'lucide-react';
import useAuthStore from '../../store/authStore';
import { cn } from '../ui';

const SidebarItem = ({ to, icon: Icon, label, collapsed }) => (
  <NavLink
    to={to}
    className={({ isActive }) => cn(
      "flex items-center gap-3 px-4 py-3 rounded-lg transition-all group relative",
      isActive 
        ? "bg-primary/10 text-primary border-r-4 border-primary" 
        : "text-slate-400 hover:bg-slate-800/50 hover:text-slate-100"
    )}
  >
    <Icon size={22} className={cn("shrink-0", !collapsed && "transition-transform group-hover:scale-110")} />
    {!collapsed && <span className="font-medium">{label}</span>}
    {collapsed && (
      <div className="absolute left-full ml-2 px-2 py-1 bg-slate-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity whitespace-nowrap z-50">
        {label}
      </div>
    )}
  </NavLink>
);

export const Layout = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  React.useEffect(() => {
    let ctrl;
    const connectSSE = async () => {
      const token = localStorage.getItem('accessToken');
      if (!token) return;

      ctrl = new AbortController();
      const streamUrl = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8088/api/v1'}/notifications/stream`;
      try {
        await fetchEventSource(streamUrl, {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`
          },
          signal: ctrl.signal,
          onmessage(msg) {
            if (msg.event === 'notification') {
              const data = JSON.parse(msg.data);
              toast.success(`${data.title}\n${data.message}`, { duration: 5000 });
            }
          },
          onerror(err) {
            console.error('SSE Error:', err);
            throw err; // triggers reconnect
          }
        });
      } catch (err) {
        // fetchEventSource handles reconnects
      }
    };

    connectSSE();

    return () => {
      if (ctrl) {
        ctrl.abort();
      }
    };
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const menuItems = [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/accounts', icon: CreditCard, label: 'Accounts' },
    { to: '/transfer', icon: ArrowRightLeft, label: 'Transfer' },
    { to: '/beneficiaries', icon: Users, label: 'Beneficiaries' },
    { to: '/loans', icon: PiggyBank, label: 'Loans' },
    { to: '/notifications', icon: Bell, label: 'Notifications' },
  ];

  if (user?.role === 'ADMIN') {
    menuItems.push({ to: '/admin', icon: ShieldCheck, label: 'Admin Panel' });
  }

  return (
    <div className="min-h-screen bg-background flex overflow-hidden">
      {/* Sidebar */}
      <aside className={cn(
        "bg-slate-950 border-r border-white/5 transition-all duration-300 flex flex-col z-40",
        collapsed ? "w-20" : "w-64"
      )}>
        <div className="p-6 flex items-center justify-between">
          {!collapsed && (
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg premium-gradient flex items-center justify-center">
                <ShieldCheck className="text-white" size={20} />
              </div>
              <span className="font-bold text-xl tracking-tight uppercase">Enterpri<span className="text-primary">se</span></span>
            </div>
          )}
          <button 
            onClick={() => setCollapsed(!collapsed)}
            className="p-1.5 rounded-md hover:bg-slate-800 text-slate-400"
          >
            {collapsed ? <ChevronRight size={20} /> : <Menu size={20} />}
          </button>
        </div>

        <nav className="flex-1 px-3 space-y-1 mt-4">
          {menuItems.map(item => (
            <SidebarItem key={item.to} {...item} collapsed={collapsed} />
          ))}
        </nav>

        <div className="p-4 border-t border-white/5">
          <button 
            onClick={handleLogout}
            className={cn(
              "flex items-center gap-3 w-full px-4 py-3 rounded-lg text-slate-400 hover:bg-red-500/10 hover:text-red-400 transition-all group",
              collapsed && "justify-center"
            )}
          >
            <LogOut size={22} />
            {!collapsed && <span className="font-medium">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col overflow-hidden relative">
        {/* Top Navbar */}
        <header className="h-16 border-b border-white/5 bg-slate-950/50 backdrop-blur-md flex items-center justify-between px-8 z-30">
          <h2 className="text-lg font-semibold text-slate-200">
            Welcome back, <span className="text-primary">{user?.fullName?.split(' ')[0]}</span>
          </h2>
          <div className="flex items-center gap-4">
            <div className="flex flex-col items-end mr-2">
              <span className="text-sm font-medium text-slate-200">{user?.fullName}</span>
              <span className="text-xs text-slate-500">{user?.role} Account</span>
            </div>
            <div className="w-10 h-10 rounded-full bg-slate-800 border border-white/10 flex items-center justify-center text-primary font-bold overflow-hidden">
               {user?.fullName?.charAt(0)}
            </div>
          </div>
        </header>

        {/* Page Content */}
        <div className="flex-1 overflow-y-auto p-8 custom-scrollbar">
          {children}
        </div>
      </main>
    </div>
  );
};
