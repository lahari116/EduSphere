import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { courseService } from '../../services/courseService'
import { departmentService } from '../../services/departmentService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Modal from '../../components/common/Modal'
import Badge from '../../components/common/Badge'
import { Search, Upload, Link, BookOpen, Building2, Clock, CheckCircle2 } from 'lucide-react'
import toast from 'react-hot-toast'
import clsx from 'clsx'

function isPastCourse(course) {
  if (course.completionDeadline) {
    return new Date(course.completionDeadline) < new Date()
  }
  return false
}

export default function CourseManagement() {
  const [search, setSearch] = useState('')
  const [tab, setTab] = useState('active')
  const [syllabusModal, setSyllabusModal] = useState(null)
  const [deptModal, setDeptModal] = useState(null)
  const [selectedDeptId, setSelectedDeptId] = useState('')
  const [file, setFile] = useState(null)

  const { data: coursesRes, isLoading } = useQuery({
    queryKey: ['all-courses'],
    queryFn: () => courseService.getAll(),
  })

  const { data: deptsRes } = useQuery({
    queryKey: ['departments'],
    queryFn: () => departmentService.getAll(),
  })

  const uploadSyllabusMutation = useMutation({
    mutationFn: ({ courseId, file }) => courseService.uploadSyllabus(courseId, file),
    onSuccess: () => {
      toast.success('Syllabus uploaded!')
      setSyllabusModal(null)
      setFile(null)
    },
    onError: (err) => toast.error(err?.response?.data?.message || 'Upload failed'),
  })

  const linkDeptMutation = useMutation({
    mutationFn: ({ courseId, deptId }) => courseService.linkDepartment(courseId, deptId),
    onSuccess: () => {
      toast.success('Department linked!')
      setDeptModal(null)
      setSelectedDeptId('')
    },
    onError: (err) => toast.error(err?.response?.data?.message || 'Link failed'),
  })

  if (isLoading) return <PageLoader />

  const departments = deptsRes?.data?.data || []
  const deptMap = departments.reduce((acc, d) => { acc[d.departmentId] = d; return acc }, {})

  const allCourses = coursesRes?.data?.data || []
  const activeCourses = allCourses.filter((c) => !isPastCourse(c))
  const pastCourses = allCourses.filter((c) => isPastCourse(c))

  const filterFn = (c) =>
    c.courseName?.toLowerCase().includes(search.toLowerCase()) ||
    c.courseCode?.toLowerCase().includes(search.toLowerCase())

  const courses = (tab === 'active' ? activeCourses : pastCourses).filter(filterFn)

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-title">Course Management</h1>
          <p className="text-slate-500 text-sm mt-0.5">Upload syllabi and link courses to departments</p>
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

      {/* Tabs */}
      <div className="flex gap-1 bg-slate-100 p-1 rounded-xl w-fit">
        {[
          { key: 'active', label: `Active (${activeCourses.length})`, icon: CheckCircle2 },
          { key: 'past',   label: `Past (${pastCourses.length})`,     icon: Clock },
        ].map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={clsx(
              'px-4 py-1.5 rounded-lg text-sm font-medium transition-all flex items-center gap-1.5',
              tab === t.key ? 'bg-white text-primary-700 shadow-sm' : 'text-slate-500 hover:text-slate-700'
            )}
          >
            <t.icon size={14} />
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'past' && pastCourses.length > 0 && (
        <div className="flex items-center gap-2 bg-amber-50 border border-amber-100 rounded-xl px-4 py-3 text-sm text-amber-700">
          <Clock size={15} className="flex-shrink-0" />
          Past courses have passed their completion deadline. Students can no longer make progress.
        </div>
      )}

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Course</th>
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Code</th>
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Linked Departments</th>
                {tab === 'past' && <th className="text-left py-3 px-4 text-slate-500 font-medium">Deadline</th>}
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {courses.map((c) => {
                const linkedDepts = (c.departmentIds || []).map((id) => deptMap[id]).filter(Boolean)
                return (
                  <tr key={c.courseId} className={clsx('border-b border-slate-50 hover:bg-slate-50 transition-colors', tab === 'past' && 'opacity-75')}>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <div className={clsx('w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0', tab === 'past' ? 'bg-slate-100' : 'bg-amber-100')}>
                          <BookOpen size={14} className={tab === 'past' ? 'text-slate-400' : 'text-amber-600'} />
                        </div>
                        <span className="font-medium text-slate-800">{c.courseName}</span>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <Badge variant={tab === 'past' ? 'slate' : 'purple'}>{c.courseCode}</Badge>
                    </td>
                    <td className="py-3 px-4">
                      {linkedDepts.length > 0 ? (
                        <div className="flex flex-wrap gap-1">
                          {linkedDepts.map((d) => (
                            <span key={d.departmentId} className="inline-flex items-center gap-1 text-xs px-2 py-0.5 bg-primary-50 text-primary-700 rounded-lg font-medium">
                              <Building2 size={10} /> {d.departmentCode}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-xs text-slate-400 italic">None</span>
                      )}
                    </td>
                    {tab === 'past' && (
                      <td className="py-3 px-4 text-xs text-slate-500">
                        {c.completionDeadline ? new Date(c.completionDeadline).toLocaleDateString() : '—'}
                      </td>
                    )}
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => { setSyllabusModal(c); setFile(null) }}
                          className="flex items-center gap-1 text-xs px-2.5 py-1 bg-primary-50 hover:bg-primary-100 text-primary-700 rounded-lg transition-colors font-medium"
                        >
                          <Upload size={12} /> Syllabus
                        </button>
                        {tab === 'active' && (
                          <button
                            onClick={() => { setDeptModal(c); setSelectedDeptId('') }}
                            className="flex items-center gap-1 text-xs px-2.5 py-1 bg-amber-50 hover:bg-amber-100 text-amber-700 rounded-lg transition-colors font-medium"
                          >
                            <Link size={12} /> Link Dept
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                )
              })}
              {courses.length === 0 && (
                <tr>
                  <td colSpan={tab === 'past' ? 5 : 4} className="py-10 text-center text-slate-400">
                    {tab === 'past' ? 'No past courses found.' : 'No active courses found.'}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Syllabus upload modal */}
      <Modal open={!!syllabusModal} onClose={() => setSyllabusModal(null)} title={`Upload Syllabus — ${syllabusModal?.courseName}`}>
        <div className="space-y-4">
          <div className="border-2 border-dashed border-primary-200 rounded-xl p-6 text-center">
            <Upload size={28} className="mx-auto text-primary-300 mb-2" />
            <p className="text-sm text-slate-500">Select a PDF file</p>
            <input
              type="file"
              accept=".pdf"
              onChange={(e) => setFile(e.target.files[0])}
              className="mt-3 text-sm text-slate-600 file:mr-3 file:py-1.5 file:px-4 file:rounded-lg file:border-0 file:bg-primary-100 file:text-primary-700 file:font-medium cursor-pointer"
            />
          </div>
          {file && <p className="text-sm text-emerald-600 font-medium">✓ {file.name}</p>}
          <div className="flex gap-3">
            <button onClick={() => setSyllabusModal(null)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => uploadSyllabusMutation.mutate({ courseId: syllabusModal?.courseId, file })}
              disabled={!file || uploadSyllabusMutation.isPending}
              className="btn-primary flex-1"
            >
              {uploadSyllabusMutation.isPending ? 'Uploading…' : 'Upload'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Department link modal */}
      <Modal open={!!deptModal} onClose={() => setDeptModal(null)} title={`Link Department — ${deptModal?.courseName}`}>
        <div className="space-y-4">
          {(deptModal?.departmentIds || []).length > 0 && (
            <div className="bg-slate-50 rounded-xl p-3">
              <p className="text-xs text-slate-500 font-medium mb-2">Currently linked:</p>
              <div className="flex flex-wrap gap-1.5">
                {(deptModal.departmentIds || []).map((id) => {
                  const d = deptMap[id]
                  return d ? (
                    <span key={id} className="inline-flex items-center gap-1 text-xs px-2 py-1 bg-primary-100 text-primary-700 rounded-lg font-medium">
                      <Building2 size={10} /> {d.departmentName} ({d.departmentCode})
                    </span>
                  ) : null
                })}
              </div>
            </div>
          )}
          <div>
            <label className="label">Select Department to Link</label>
            <select value={selectedDeptId} onChange={(e) => setSelectedDeptId(e.target.value)} className="input">
              <option value="">— Choose a department —</option>
              {departments
                .filter((d) => !(deptModal?.departmentIds || []).includes(d.departmentId))
                .map((d) => (
                  <option key={d.departmentId} value={d.departmentId}>
                    {d.departmentName} ({d.departmentCode})
                  </option>
                ))}
            </select>
          </div>
          <div className="flex gap-3">
            <button onClick={() => setDeptModal(null)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => linkDeptMutation.mutate({ courseId: deptModal?.courseId, deptId: selectedDeptId })}
              disabled={!selectedDeptId || linkDeptMutation.isPending}
              className="btn-primary flex-1 flex items-center justify-center gap-1"
            >
              <Link size={15} /> {linkDeptMutation.isPending ? 'Linking…' : 'Link'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
