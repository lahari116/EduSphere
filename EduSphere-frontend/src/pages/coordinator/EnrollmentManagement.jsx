import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { courseService } from '../../services/courseService'
import { enrollmentService } from '../../services/enrollmentService'
import { adminService } from '../../services/adminService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Modal from '../../components/common/Modal'
import Badge from '../../components/common/Badge'
import { UserPlus, Trash2, Users, Search } from 'lucide-react'
import { formatDate } from '../../utils/helpers'
import toast from 'react-hot-toast'

export default function EnrollmentManagement() {
  const qc = useQueryClient()
  const [selectedCourse, setSelectedCourse] = useState('')
  const [enrollModal, setEnrollModal] = useState(false)
  const [empId, setEmpId] = useState('')
  const [userRole, setUserRole] = useState('STUDENT')

  const { data: coursesData, isLoading: loadingCourses } = useQuery({
    queryKey: ['all-courses'],
    queryFn: () => courseService.getAll(),
  })

  const { data: enrollData, isLoading: loadingEnrolls } = useQuery({
    queryKey: ['enrollments', selectedCourse],
    queryFn: () => enrollmentService.getEnrollmentsByCourse(selectedCourse),
    enabled: !!selectedCourse,
  })

  const [empIdQuery, setEmpIdQuery] = useState('')

  const { data: lookupData, isFetching: lookingUp } = useQuery({
    queryKey: ['user-lookup', empIdQuery],
    queryFn: () => adminService.getUserByEmpId(empIdQuery),
    enabled: empIdQuery.length >= 2,
    retry: false,
  })

  const enrollMutation = useMutation({
    mutationFn: (data) => enrollmentService.enroll(data),
    onSuccess: () => {
      toast.success('Enrolled successfully!')
      setEnrollModal(false)
      setEmpId('')
      setEmpIdQuery('')
      setUserRole('STUDENT')
      qc.invalidateQueries({ queryKey: ['enrollments', selectedCourse] })
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || 'Enrollment failed')
    },
  })

  const unenrollMutation = useMutation({
    mutationFn: (id) => enrollmentService.unenroll(id),
    onSuccess: () => {
      toast.success('Unenrolled')
      qc.invalidateQueries({ queryKey: ['enrollments', selectedCourse] })
    },
  })

  if (loadingCourses) return <PageLoader />

  const courses = coursesData?.data?.data || []
  const enrollments = enrollData?.data?.data || []

  const resolvedUser = lookupData?.data?.data ?? null

  const getDisplayName = (userId) => `User ${userId?.slice(0, 8) ?? ''}…`
  const getUserEmail = () => null

  const handleEnroll = () => {
    if (!resolvedUser) {
      toast.error('No user found with that ID')
      return
    }
    enrollMutation.mutate({
      userId: resolvedUser.userId,
      userRole,
      courseId: selectedCourse,
    })
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-title">Enrollment Management</h1>
          <p className="text-slate-500 text-sm mt-0.5">Enroll or remove students and instructors from courses</p>
        </div>
        {selectedCourse && (
          <button onClick={() => setEnrollModal(true)} className="btn-primary flex items-center gap-2">
            <UserPlus size={16} /> Enroll User
          </button>
        )}
      </div>

      {/* Course selector */}
      <div className="card">
        <label className="label">Select Course to Manage</label>
        <select
          value={selectedCourse}
          onChange={(e) => setSelectedCourse(e.target.value)}
          className="input max-w-sm"
        >
          <option value="">— Choose a course —</option>
          {courses.map((c) => (
            <option key={c.courseId} value={c.courseId}>
              {c.courseName} ({c.courseCode})
            </option>
          ))}
        </select>
      </div>

      {/* Enrollments table */}
      {selectedCourse && (
        <div className="card overflow-hidden">
          <div className="flex items-center justify-between mb-4">
            <h3 className="section-title flex items-center gap-2">
              <Users size={18} className="text-primary-500" />
              Enrolled Users ({enrollments.length})
            </h3>
          </div>
          {loadingEnrolls ? (
            <PageLoader />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-100 bg-slate-50">
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">User</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Role</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Enrolled At</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Status</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {enrollments.map((e) => {
                    const name = getDisplayName(e.userId)
                    const email = getUserEmail(e.userId)
                    const initials = '?'
                    return (
                      <tr key={e.enrollmentId} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                        <td className="py-3 px-4">
                          <div className="flex items-center gap-3">
                            <div className={`w-8 h-8 rounded-lg flex items-center justify-center text-white text-xs font-bold flex-shrink-0 ${e.userRole === 'STUDENT' ? 'bg-blue-400' : 'bg-emerald-400'}`}>
                              {initials}
                            </div>
                            <div>
                              <p className="font-medium text-slate-800">{name}</p>
                              {email && <p className="text-xs text-slate-400">{email}</p>}
                            </div>
                          </div>
                        </td>
                        <td className="py-3 px-4">
                          <Badge variant={e.userRole === 'STUDENT' ? 'blue' : 'green'}>{e.userRole}</Badge>
                        </td>
                        <td className="py-3 px-4 text-slate-500">{formatDate(e.enrolledAt)}</td>
                        <td className="py-3 px-4">
                          <Badge variant={e.status === 'ACTIVE' ? 'green' : 'slate'}>{e.status}</Badge>
                        </td>
                        <td className="py-3 px-4">
                          <button
                            onClick={() => unenrollMutation.mutate(e.enrollmentId)}
                            className="p-1.5 text-rose-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                          >
                            <Trash2 size={15} />
                          </button>
                        </td>
                      </tr>
                    )
                  })}
                  {enrollments.length === 0 && (
                    <tr>
                      <td colSpan={5} className="py-10 text-center text-slate-400">
                        No enrollments found for this course.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Enroll user modal */}
      <Modal
        open={enrollModal}
        onClose={() => { setEnrollModal(false); setEmpId(''); setEmpIdQuery('') }}
        title="Enroll User"
      >
        <div className="space-y-4">
          <div className="bg-primary-50 rounded-xl p-3 text-xs text-primary-700 flex items-start gap-2">
            <Search size={14} className="flex-shrink-0 mt-0.5" />
            <span>
              Enter the student or employee ID (e.g. <strong>STU-001</strong> or <strong>EMP-001</strong>).
            </span>
          </div>

          <div>
            <label className="label">Student / Employee ID</label>
            <input
              value={empId}
              onChange={(e) => {
                const val = e.target.value.toUpperCase()
                setEmpId(val)
                setEmpIdQuery(val.trim())
              }}
              placeholder="STU-001 or EMP-001"
              className="input font-mono text-sm uppercase"
              autoComplete="off"
            />
            {empId.trim() && (
              lookingUp ? (
                <p className="text-sm text-slate-400 mt-1.5">Searching…</p>
              ) : resolvedUser ? (
                <p className="text-sm text-emerald-600 mt-1.5 font-medium">
                  ✓ {resolvedUser.firstName} {resolvedUser.lastName} — {resolvedUser.email}
                </p>
              ) : (
                <p className="text-sm text-rose-500 mt-1.5">
                  No user found with ID "{empId.trim()}"
                </p>
              )
            )}
          </div>

          <div>
            <label className="label">Enroll as</label>
            <select value={userRole} onChange={(e) => setUserRole(e.target.value)} className="input">
              <option value="STUDENT">Student</option>
              <option value="INSTRUCTOR">Instructor</option>
            </select>
          </div>

          <div className="flex gap-3">
            <button onClick={() => { setEnrollModal(false); setEmpId('') }} className="btn-secondary flex-1">
              Cancel
            </button>
            <button
              onClick={handleEnroll}
              disabled={!resolvedUser || enrollMutation.isPending}
              className="btn-primary flex-1"
            >
              {enrollMutation.isPending ? 'Enrolling…' : 'Enroll'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
