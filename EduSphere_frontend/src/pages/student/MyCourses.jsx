import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { courseService } from '../../services/courseService'
import { enrollmentService } from '../../services/enrollmentService'
import { useAuth } from '../../context/AuthContext'
import CourseCard from '../../components/dashboard/CourseCard'
import { PageLoader } from '../../components/common/LoadingSpinner'
import { Search, BookOpen, Sparkles, CheckCircle2 } from 'lucide-react'
import toast from 'react-hot-toast'
import clsx from 'clsx'

export default function MyCourses() {
  const { user } = useAuth()
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [tab, setTab] = useState('enrolled')

  const { data: allCoursesRes, isLoading } = useQuery({
    queryKey: ['all-courses'],
    queryFn: () => courseService.getAll(),
  })

  const { data: enrollRes } = useQuery({
    queryKey: ['student-enrollments', user?.userId],
    queryFn: () => enrollmentService.getStudentEnrollments(user.userId),
    enabled: !!user?.userId,
  })

  const enrollMutation = useMutation({
    mutationFn: (courseId) => enrollmentService.selfEnroll(courseId),
    onSuccess: (_, courseId) => {
      toast.success('Enrolled successfully!')
      qc.invalidateQueries({ queryKey: ['student-enrollments', user?.userId] })
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || 'Enrollment failed')
    },
  })

  if (isLoading) return <PageLoader />

  const allCourses = allCoursesRes?.data?.data || []
  const myEnrollments = enrollRes?.data?.data || []
  const enrolledCourseIds = new Set(myEnrollments.map((e) => e.courseId))

  const searchFilter = (c) =>
    c.courseName?.toLowerCase().includes(search.toLowerCase()) ||
    c.courseCode?.toLowerCase().includes(search.toLowerCase())

  const displayCourses =
    tab === 'enrolled'
      ? allCourses.filter((c) => enrolledCourseIds.has(c.courseId) && searchFilter(c))
      : allCourses.filter(searchFilter)

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-title">My Courses</h1>
          <p className="text-slate-500 text-sm mt-0.5">{allCourses.length} courses on the platform</p>
        </div>
        <div className="relative max-w-xs w-full">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search courses…"
            className="input pl-9"
          />
        </div>
      </div>

      {/* Self-enroll banner */}
      {tab === 'enrolled' && (
        <div className="flex items-center justify-between gap-4 bg-gradient-to-r from-primary-50 to-purple-50 border border-primary-100 rounded-2xl px-5 py-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center flex-shrink-0">
              <Sparkles size={18} className="text-primary-600" />
            </div>
            <div>
              <p className="font-semibold text-slate-800 text-sm">Looking for new courses?</p>
              <p className="text-xs text-slate-500 mt-0.5">Browse the full catalog and self-enroll instantly.</p>
            </div>
          </div>
          <button onClick={() => setTab('browse')} className="btn-primary flex-shrink-0 text-sm">
            Browse &amp; Enroll
          </button>
        </div>
      )}

      {/* Tabs */}
      <div className="flex gap-1 bg-slate-100 p-1 rounded-xl w-fit">
        {[
          { key: 'enrolled', label: `Enrolled (${enrolledCourseIds.size})` },
          { key: 'browse',   label: '✨ Browse All' },
        ].map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={clsx(
              'px-4 py-1.5 rounded-lg text-sm font-medium transition-all',
              tab === t.key ? 'bg-white text-primary-700 shadow-sm' : 'text-slate-500 hover:text-slate-700'
            )}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'browse' && (
        <div className="flex items-center gap-2 text-sm text-slate-500 bg-amber-50 border border-amber-100 rounded-xl px-4 py-3">
          <CheckCircle2 size={16} className="text-amber-500 flex-shrink-0" />
          <span>Click <strong>+ Enroll</strong> on any course to join immediately.</span>
        </div>
      )}

      {displayCourses.length === 0 ? (
        <div className="card text-center py-16 text-slate-400">
          <BookOpen size={48} className="mx-auto mb-4 text-slate-200" />
          <p className="text-base font-medium">
            {tab === 'enrolled' ? 'You are not enrolled in any courses yet.' : 'No courses found.'}
          </p>
          {tab === 'enrolled' && (
            <button onClick={() => setTab('browse')} className="btn-primary mt-4 mx-auto">
              Browse &amp; Enroll Now
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {displayCourses.map((c, i) => (
            <div key={c.courseId} className="relative">
              <CourseCard course={c} index={i} navigateTo={`/student/courses/${c.courseId}`} />
              {tab === 'browse' && !enrolledCourseIds.has(c.courseId) && (
                <button
                  onClick={(e) => {
                    e.preventDefault()
                    e.stopPropagation()
                    enrollMutation.mutate(c.courseId)
                  }}
                  disabled={enrollMutation.isPending}
                  className="absolute top-4 right-4 btn-primary text-xs py-1.5 px-3 shadow-md"
                >
                  {enrollMutation.isPending ? '…' : '+ Enroll'}
                </button>
              )}
              {tab === 'browse' && enrolledCourseIds.has(c.courseId) && (
                <span className="absolute top-4 right-4 bg-emerald-500 text-white text-xs px-2 py-1 rounded-lg shadow">
                  Enrolled ✓
                </span>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
