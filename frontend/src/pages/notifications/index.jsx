import React, { useEffect, useState } from 'react';
import { Card, Button } from '../../components/ui';
import { 
  Bell, 
  CheckCheck, 
  Clock, 
  Info, 
  ShieldAlert, 
  CreditCard, 
  ArrowRightLeft,
  Trash2
} from 'lucide-react';
import api from '../../api/axios';
import toast from 'react-hot-toast';

export const Notifications = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = async () => {
    try {
      const res = await api.get('/notifications');
      setNotifications(res.data.data);
    } catch (err) {
      toast.error('Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const markRead = async (id) => {
    try {
      await api.patch(`/notifications/${id}/read`);
      fetchNotifications();
    } catch (err) {}
  };

  const markAllRead = async () => {
    try {
      await api.patch('/notifications/read-all');
      toast.success('All marked as read');
      fetchNotifications();
    } catch (err) {}
  };

  const getIcon = (type) => {
    switch (type) {
      case 'TRANSACTION': return <CreditCard className="text-emerald-500" size={20} />;
      case 'FRAUD_ALERT': return <ShieldAlert className="text-red-500" size={20} />;
      case 'ACCOUNT': return <Info className="text-sky-500" size={20} />;
      default: return <Bell className="text-primary" size={20} />;
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-in fade-in duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white">Notifications</h1>
          <p className="text-slate-400">Stay updated with your account activities</p>
        </div>
        <Button variant="outline" size="sm" onClick={markAllRead} className="gap-2">
          <CheckCheck size={18} /> Mark all as read
        </Button>
      </div>

      <div className="space-y-4">
        {notifications.map((notif) => (
          <Card 
            key={notif.id} 
            className={cn(
              "p-5 transition-all flex gap-4 group",
              !notif.read ? "bg-primary/5 border-primary/20 ring-1 ring-primary/10" : "bg-slate-900/50"
            )}
            onClick={() => !notif.read && markRead(notif.id)}
          >
            <div className={cn(
              "w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 shadow-lg",
              !notif.read ? "bg-primary/20" : "bg-slate-800"
            )}>
              {getIcon(notif.type)}
            </div>

            <div className="flex-1 space-y-1">
              <div className="flex items-center justify-between">
                <h4 className={cn("font-bold", !notif.read ? "text-slate-100" : "text-slate-300")}>
                  {notif.title}
                </h4>
                <span className="text-[10px] text-slate-500 flex items-center gap-1">
                  <Clock size={10} /> {new Date(notif.createdAt).toLocaleString()}
                </span>
              </div>
              <p className="text-sm text-slate-400 leading-relaxed">{notif.message}</p>
              {notif.referenceId && (
                <p className="text-[10px] font-mono text-slate-600 mt-2">REF: {notif.referenceId}</p>
              )}
            </div>

            {!notif.read && (
              <div className="w-2 h-2 rounded-full bg-primary mt-2 shadow-[0_0_8px_rgba(14,165,233,0.5)]" />
            )}
          </Card>
        ))}

        {notifications.length === 0 && !loading && (
          <div className="py-20 text-center space-y-4 border-2 border-dashed border-white/5 rounded-2xl">
            <Bell size={48} className="mx-auto text-slate-800" />
            <p className="text-slate-500">All caught up! No notifications yet.</p>
          </div>
        )}
      </div>
    </div>
  );
};
