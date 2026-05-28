import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import {
  LayoutDashboard, BookOpen, ClipboardList, BarChart2,
  Users, Bell, LogOut, GraduationCap,
  UserCheck, FolderKanban, ShieldCheck, UserCircle, Building2,
} from 'lucide-react'
import clsx from 'clsx'
import toast from 'react-hot-toast'

const NAV = {
  STUDENT: [
    { to: '/student/dashboard',   icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/student/courses',     icon: BookOpen,         label: 'My Courses' },
    { to: '/student/enrollments', icon: ClipboardList,    label: 'My Enrollments' },
    { to: '/student/progress',    icon: BarChart2,        label: 'My Progress' },
    { to: '/notifications',       icon: Bell,             label: 'Notifications' },
    { to: '/profile',             icon: UserCircle,       label: 'My Profile' },
  ],
  INSTRUCTOR: [
    { to: '/instructor/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/notifications',        icon: Bell,            label: 'Notifications' },
    { to: '/profile',              icon: UserCircle,      label: 'My Profile' },
  ],
  COORDINATOR: [
    { to: '/coordinator/dashboard',   icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/coordinator/courses',     icon: FolderKanban,    label: 'Courses' },
    { to: '/coordinator/enrollments', icon: UserCheck,       label: 'Enrollments' },
    { to: '/notifications',           icon: Bell,            label: 'Notifications' },
    { to: '/profile',                 icon: UserCircle,      label: 'My Profile' },
  ],
  ADMIN: [
    { to: '/admin/dashboard',   icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/admin/users',       icon: Users,            label: 'Users' },
    { to: '/admin/departments', icon: Building2,        label: 'Departments' },
    { to: '/admin/courses',     icon: BookOpen,         label: 'Courses' },
    { to: '/admin/audit',       icon: ShieldCheck,      label: 'Audit Logs' },
    { to: '/notifications',     icon: Bell,             label: 'Notifications' },
    { to: '/profile',           icon: UserCircle,       label: 'My Profile' },
  ],
}

const ROLE_GRADIENT = {
  STUDENT:     'from-blue-500 to-indigo-600',
  INSTRUCTOR:  'from-emerald-500 to-teal-600',
  COORDINATOR: 'from-amber-500 to-orange-500',
  ADMIN:       'from-rose-500 to-pink-600',
}

export default function Sidebar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const links = NAV[user?.role] || []

  const handleLogout = async () => {
    await logout()
    toast.success('Logged out successfully')
    navigate('/login')
  }

  return (
    <aside className="flex flex-col w-64 min-h-screen bg-white border-r border-slate-100 shadow-soft">
      {/* Logo */}
      <div className="flex items-center gap-3 px-5 py-5 border-b border-slate-100">
        <div className={clsx('w-9 h-9 rounded-xl flex items-center justify-center bg-gradient-to-br', ROLE_GRADIENT[user?.role])}>
          <GraduationCap size={20} className="text-white" />
        </div>
        <div>
          <p className="text-base font-bold text-slate-900 leading-none">EduSphere</p>
          <p className="text-xs text-slate-400 mt-0.5">
            {user?.role ? (user.role.charAt(0) + user.role.slice(1).toLowerCase()) : ''} Portal
          </p>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        {links.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => clsx('sidebar-link', isActive && 'active')}
          >
            <Icon size={18} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      {/* User profile */}
      <div className="px-3 py-4 border-t border-slate-100 space-y-0.5">
        <div className="flex items-center gap-3 px-3 py-2.5 rounded-xl bg-primary-50">
          <div className={clsx('w-8 h-8 rounded-lg flex items-center justify-center text-white text-xs font-bold bg-gradient-to-br flex-shrink-0', ROLE_GRADIENT[user?.role])}>
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </div>
          <div className="overflow-hidden">
            <p className="text-sm font-semibold text-slate-800 truncate">{user?.firstName} {user?.lastName}</p>
            <p className="text-xs text-slate-400 truncate">{user?.email}</p>
          </div>
        </div>
        <button onClick={handleLogout} className="sidebar-link w-full text-rose-500 hover:bg-rose-50 hover:text-rose-600">
          <LogOut size={18} />
          <span>Log Out</span>
        </button>
      </div>
    </aside>
  )
}
