import { useNavigate } from 'react-router-dom'
import { Bell, Search } from 'lucide-react'
import { useNotifications } from '../../context/NotificationContext'
import { useAuth } from '../../context/AuthContext'
import { useEffect, useState } from 'react'
import clsx from 'clsx'

const ROLE_PLACEHOLDER = {
  STUDENT:     'Search courses, assignments…',
  INSTRUCTOR:  'Search courses, students…',
  COORDINATOR: 'Search courses, enrollments…',
  ADMIN:       'Search users, courses, logs…',
}

const ROLE_SEARCH_PATH = {
  STUDENT:     '/student/courses',
  INSTRUCTOR:  '/instructor/dashboard',
  COORDINATOR: '/coordinator/courses',
  ADMIN:       '/admin/users',
}

export default function Header({ title }) {
  const navigate = useNavigate()
  const { user } = useAuth()
  const notif = useNotifications()
  const [search, setSearch] = useState('')

  useEffect(() => { notif?.fetchCount() }, [])

  const placeholder = ROLE_PLACEHOLDER[user?.role] || 'Search…'

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && search.trim()) {
      const path = ROLE_SEARCH_PATH[user?.role] || '/'
      navigate(`${path}?q=${encodeURIComponent(search.trim())}`)
      setSearch('')
    }
  }

  return (
    <header className="sticky top-0 z-20 bg-white/80 backdrop-blur border-b border-slate-100 px-6 py-3 flex items-center justify-between gap-4">
      <h1 className="text-lg font-semibold text-slate-800 hidden sm:block">{title}</h1>

      <div className="flex-1 max-w-xs">
        <div className="relative">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            className="w-full pl-8 pr-3 py-2 text-sm rounded-xl bg-slate-50 border border-slate-200 focus:outline-none focus:ring-2 focus:ring-primary-300 focus:border-primary-400 transition-all"
          />
        </div>
      </div>

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
