import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { GraduationCap, Mail, Lock, User, Eye, EyeOff, ArrowRight, IdCard } from 'lucide-react'
import api from '../../services/api'
import toast from 'react-hot-toast'

const ROLES = [
  { value: 'STUDENT',     label: 'Student',     prefix: 'STU' },
  { value: 'INSTRUCTOR',  label: 'Instructor',  prefix: 'EMP' },
  { value: 'COORDINATOR', label: 'Coordinator', prefix: 'EMP' },
]

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '',
    password: '', confirmPassword: '',
    role: 'STUDENT', studentOrEmployeeId: '',
  })
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handle = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match')
      return
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }
    setLoading(true)
    try {
      const { confirmPassword, ...payload } = form
      await api.post('/auth/register', payload)
      toast.success('Account created! Please sign in.')
      navigate('/login')
    } catch (err) {
      setError(err?.response?.data?.message || 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const selectedRole = ROLES.find((r) => r.value === form.role)

  return (
    <div className="min-h-screen flex">
      {/* Left panel */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-gradient-to-br from-primary-600 via-purple-600 to-indigo-700 flex-col items-center justify-center p-12 overflow-hidden">
        <div className="absolute top-[-80px] left-[-80px] w-72 h-72 rounded-full bg-white/5" />
        <div className="absolute bottom-[-60px] right-[-60px] w-56 h-56 rounded-full bg-white/5" />
        <div className="absolute top-1/2 right-[-120px] w-80 h-80 rounded-full bg-white/5" />

        <div className="relative z-10 text-center text-white space-y-8 max-w-sm">
          <div className="flex justify-center">
            <div className="w-20 h-20 bg-white/20 backdrop-blur rounded-3xl flex items-center justify-center ring-4 ring-white/30">
              <GraduationCap size={40} className="text-white" />
            </div>
          </div>
          <div>
            <h1 className="text-4xl font-bold leading-tight">Join EduSphere</h1>
            <p className="mt-3 text-lg text-white/70 leading-relaxed">Start your learning journey today with a free account.</p>
          </div>
          <div className="space-y-3">
            {[
              { icon: '🎓', text: 'Access courses designed by expert instructors' },
              { icon: '📝', text: 'Submit assignments and track your grades in real-time' },
              { icon: '📊', text: 'Monitor your progress with detailed analytics' },
              { icon: '🔔', text: 'Get instant notifications for updates' },
            ].map((f) => (
              <div key={f.text} className="flex items-start gap-3 bg-white/10 backdrop-blur rounded-2xl px-4 py-3">
                <span className="text-lg flex-shrink-0">{f.icon}</span>
                <p className="text-sm text-white/80 leading-relaxed">{f.text}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right panel */}
      <div className="flex-1 flex items-center justify-center p-6 bg-slate-50 overflow-y-auto">
        <div className="w-full max-w-md py-8">
          {/* Mobile logo */}
          <div className="flex lg:hidden items-center gap-3 mb-8">
            <div className="w-10 h-10 bg-gradient-to-br from-primary-500 to-purple-600 rounded-xl flex items-center justify-center">
              <GraduationCap size={22} className="text-white" />
            </div>
            <span className="text-xl font-bold text-slate-900">EduSphere</span>
          </div>

          <div className="bg-white rounded-3xl shadow-soft border border-slate-100 p-8 space-y-6">
            <div>
              <h2 className="text-2xl font-bold text-slate-900">Create an account</h2>
              <p className="text-sm text-slate-500 mt-1">Fill in your details to get started</p>
            </div>

            {error && (
              <div className="flex items-center gap-2 bg-rose-50 border border-rose-200 text-rose-700 text-sm px-4 py-3 rounded-xl">
                <span className="w-5 h-5 bg-rose-500 text-white rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0">!</span>
                {error}
              </div>
            )}

            <form onSubmit={submit} className="space-y-4">
              {/* Name row */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="label">First Name</label>
                  <div className="relative">
                    <User size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                    <input name="firstName" required value={form.firstName} onChange={handle} placeholder="John" className="input pl-9" />
                  </div>
                </div>
                <div>
                  <label className="label">Last Name</label>
                  <input name="lastName" required value={form.lastName} onChange={handle} placeholder="Doe" className="input" />
                </div>
              </div>

              {/* Email */}
              <div>
                <label className="label">Email address</label>
                <div className="relative">
                  <Mail size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input name="email" type="email" required value={form.email} onChange={handle} placeholder="you@university.edu" className="input pl-9" />
                </div>
              </div>

              {/* Role */}
              <div>
                <label className="label">Role</label>
                <select name="role" value={form.role} onChange={handle} className="input">
                  {ROLES.map((r) => (
                    <option key={r.value} value={r.value}>{r.label}</option>
                  ))}
                </select>
              </div>

              {/* Student / Employee ID */}
              <div>
                <label className="label">
                  {selectedRole?.value === 'STUDENT' ? 'Student ID' : 'Employee ID'}
                </label>
                <div className="relative">
                  <IdCard size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    name="studentOrEmployeeId"
                    required
                    value={form.studentOrEmployeeId}
                    onChange={handle}
                    placeholder={selectedRole?.value === 'STUDENT' ? 'STU-001' : 'EMP-001'}
                    className="input pl-9 uppercase font-mono"
                  />
                </div>
                <p className="text-xs text-slate-400 mt-1">
                  Format: {selectedRole?.prefix}-001, {selectedRole?.prefix}-002, etc.
                </p>
              </div>

              {/* Password */}
              <div>
                <label className="label">Password</label>
                <div className="relative">
                  <Lock size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    name="password"
                    type={showPw ? 'text' : 'password'}
                    required
                    value={form.password}
                    onChange={handle}
                    placeholder="Min. 6 characters"
                    className="input pl-9 pr-10"
                  />
                  <button type="button" onClick={() => setShowPw(!showPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
                    {showPw ? <EyeOff size={15} /> : <Eye size={15} />}
                  </button>
                </div>
              </div>

              {/* Confirm password */}
              <div>
                <label className="label">Confirm Password</label>
                <div className="relative">
                  <Lock size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    name="confirmPassword"
                    type={showPw ? 'text' : 'password'}
                    required
                    value={form.confirmPassword}
                    onChange={handle}
                    placeholder="Re-enter password"
                    className="input pl-9"
                  />
                </div>
              </div>

              <button type="submit" disabled={loading} className="btn-primary w-full py-3 text-base mt-2">
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Creating account…
                  </span>
                ) : (
                  <span className="flex items-center justify-center gap-2">
                    Create Account <ArrowRight size={18} />
                  </span>
                )}
              </button>
            </form>

            <p className="text-sm text-center text-slate-500">
              Already have an account?{' '}
              <Link to="/login" className="text-primary-600 font-medium hover:underline">Sign in</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
