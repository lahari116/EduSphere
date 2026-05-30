import { useNavigate } from 'react-router-dom'
import { Bell, Sun, Moon, Flame } from 'lucide-react'
import { useNotifications } from '../../context/NotificationContext'
import { useTheme } from '../../context/ThemeContext'
import { useAuth } from '../../context/AuthContext'
import { useEffect } from 'react'
import { getGreeting } from '../../utils/helpers'
import clsx from 'clsx'

export default function Header({ title }) {
  const navigate = useNavigate()
  const notif = useNotifications()
  const { isDark, toggle } = useTheme()
  const { user } = useAuth()

  useEffect(() => { notif?.fetchCount() }, [])

  const greeting = getGreeting()
  const streak = user?.streakDays ?? 0

  return (
    <header
      className="sticky top-0 z-20 border-b px-6 py-3 flex items-center justify-between gap-4 backdrop-blur"
      style={{ backgroundColor: 'color-mix(in srgb, var(--bg-sidebar) 85%, transparent)', borderColor: 'var(--border)' }}
    >
      <div className="flex flex-col">
        <h1 className="text-lg font-semibold" style={{ color: 'var(--text-primary)' }}>{title}</h1>
        <p className="text-xs hidden sm:block" style={{ color: 'var(--text-muted)' }}>
          {greeting}, {user?.firstName}!
        </p>
      </div>

      <div className="flex items-center gap-2">
        {/* Streak badge */}
        {streak > 0 && (
          <div className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-50 dark:bg-amber-900/30 border border-amber-100 dark:border-amber-800">
            <Flame size={14} className="text-amber-500" />
            <span className="text-xs font-semibold text-amber-700 dark:text-amber-300">{streak} day streak</span>
          </div>
        )}

        {/* Theme toggle */}
        <button
          onClick={toggle}
          className="p-2 rounded-xl transition-colors hover:bg-primary-50 dark:hover:bg-primary-900/30"
          title={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {isDark
            ? <Sun size={18} className="text-amber-400" />
            : <Moon size={18} className="text-slate-500" />
          }
        </button>

        {/* Notifications */}
        <button
          onClick={() => navigate('/notifications')}
          className="relative p-2 rounded-xl hover:bg-primary-50 dark:hover:bg-primary-900/30 transition-colors"
        >
          <Bell size={18} style={{ color: 'var(--text-secondary)' }} />
          {notif?.unreadCount > 0 && (
            <span className={clsx(
              'absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] px-1 rounded-full text-white text-[10px] font-bold flex items-center justify-center',
              'bg-rose-500 animate-pulse-soft'
            )}>
              {notif.unreadCount > 99 ? '99+' : notif.unreadCount}
            </span>
          )}
        </button>
      </div>
    </header>
  )
}
