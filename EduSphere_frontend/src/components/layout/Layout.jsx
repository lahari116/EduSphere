import { Outlet, useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'
import Header from './Header'

const TITLES = {
  '/student/dashboard':    'Dashboard',
  '/student/courses':      'My Courses',
  '/student/enrollments':  'My Enrollments',
  '/student/progress':     'My Progress',
  '/instructor/dashboard': 'Dashboard',
  '/coordinator/dashboard':   'Dashboard',
  '/coordinator/courses':     'Course Management',
  '/coordinator/enrollments': 'Enrollment Management',
  '/admin/dashboard': 'Dashboard',
  '/admin/users':     'User Management',
  '/admin/courses':   'Course Management',
  '/admin/audit':     'Audit Logs',
  '/notifications':   'Notifications',
  '/profile':         'My Profile',
}

export default function Layout() {
  const { pathname } = useLocation()
  const title = TITLES[pathname] ?? 'EduSphere'

  return (
    <div className="flex min-h-screen bg-primary-50">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Header title={title} />
        <main className="flex-1 p-6 animate-fade-in">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
