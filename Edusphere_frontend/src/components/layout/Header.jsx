import { useNavigate } from 'react-router-dom'
import { Bell } from 'lucide-react'
import { useNotifications } from '../../context/NotificationContext'
import { useEffect } from 'react'
import clsx from 'clsx'

export default function Header({ title }) {
  const navigate = useNavigate()
  const notif = useNotifications()

  useEffect(() => { notif?.fetchCount() }, [])

  return (
    <header className="sticky top-0 z-20 bg-white/80 backdrop-blur border-b border-slate-100 px-6 py-3 flex items-center justify-between gap-4">
      <h1 className="text-lg font-semibold text-slate-800">{title}</h1>

      <button
        onClick={() => navigate('/notifications')}
        className="relative p-2 rounded-xl hover:bg-primary-50 transition-colors"
      >
        <Bell size={20} className="text-slate-600" />
        {notif?.unreadCount > 0 && (
          <span className={clsx(
            'absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] px-1 rounded-full text-white text-[10px] font-bold flex items-center justify-center',
            'bg-rose-500'
          )}>
            {notif.unreadCount > 99 ? '99+' : notif.unreadCount}
          </span>
        )}
      </button>
    </header>
  )
}
