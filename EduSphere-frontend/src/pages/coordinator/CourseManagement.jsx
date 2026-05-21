import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { courseService } from '../../services/courseService'
import { departmentService } from '../../services/departmentService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Modal from '../../components/common/Modal'
import Badge from '../../components/common/Badge'
import { Search, Upload, Link, BookOpen, Plus } from 'lucide-react'
import toast from 'react-hot-toast'

export default function CourseManagement() {
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [syllabusModal, setSyllabusModal] = useState(null)
  const [deptModal, setDeptModal] = useState(null)
  const [selectedDeptId, setSelectedDeptId] = useState('')
  const [file, setFile] = useState(null)
  const [createDeptModal, setCreateDeptModal] = useState(false)
  const [newDept, setNewDept] = useState({ departmentName: '', departmentCode: '', description: '' })

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

  const createDeptMutation = useMutation({
    mutationFn: (data) => departmentService.create(data),
    onSuccess: () => {
      toast.success('Department created!')
      setCreateDeptModal(false)
      setNewDept({ departmentName: '', departmentCode: '', description: '' })
      qc.invalidateQueries({ queryKey: ['departments'] })
    },
    onError: (err) => toast.error(err?.response?.data?.message || 'Failed to create department'),
  })

  if (isLoading) return <PageLoader />

  const departments = deptsRes?.data?.data || []
  const courses = (coursesRes?.data?.data || []).filter((c) =>
    c.courseName?.toLowerCase().includes(search.toLowerCase()) ||
    c.courseCode?.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-title">Course Management</h1>
          <p className="text-slate-500 text-sm mt-0.5">
            Upload syllabi and link courses to departments
          </p>
        </div>
        <div className="flex items-center gap-2">
          <div className="relative max-w-xs w-full">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search coursesâ€¦"
              className="input pl-9"
            />
          </div>
          <button
            onClick={() => setCreateDeptModal(true)}
            className="btn-primary flex items-center gap-1.5 whitespace-nowrap"
          >
            <Plus size={15} /> New Department
          </button>
        </div>
      </div>

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Course</th>
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Code</th>
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Description</th>
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {courses.map((c) => (
                <tr key={c.courseId} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-2">
                      <div className="w-8 h-8 rounded-lg bg-amber-100 flex items-center justify-center flex-shrink-0">
                        <BookOpen size={14} className="text-amber-600" />
                      </div>
                      <span className="font-medium text-slate-800">{c.courseName}</span>
                    </div>
                  </td>
                  <td className="py-3 px-4">
                    <Badge variant="purple">{c.courseCode}</Badge>
                  </td>
                  <td className="py-3 px-4 text-slate-500 text-xs max-w-xs truncate">
                    {c.description || 'â€”'}
                  </td>
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => { setSyllabusModal(c); setFile(null) }}
                        className="flex items-center gap-1 text-xs px-2.5 py-1 bg-primary-50 hover:bg-primary-100 text-primary-700 rounded-lg transition-colors font-medium"
                      >
                        <Upload size={12} /> Syllabus
                      </button>
                      <button
                        onClick={() => { setDeptModal(c); setSelectedDeptId('') }}
                        className="flex items-center gap-1 text-xs px-2.5 py-1 bg-amber-50 hover:bg-amber-100 text-amber-700 rounded-lg transition-colors font-medium"
                      >
                        <Link size={12} /> Dept
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {courses.length === 0 && (
                <tr>
                  <td colSpan={4} className="py-10 text-center text-slate-400">
                    No courses found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Syllabus upload modal */}
      <Modal
        open={!!syllabusModal}
        onClose={() => setSyllabusModal(null)}
        title={`Upload Syllabus â€” ${syllabusModal?.courseName}`}
      >
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
          {file && <p className="text-sm text-emerald-600 font-medium">âœ“ {file.name}</p>}
          <div className="flex gap-3">
            <button onClick={() => setSyllabusModal(null)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => uploadSyllabusMutation.mutate({ courseId: syllabusModal?.courseId, file })}
              disabled={!file || uploadSyllabusMutation.isPending}
              className="btn-primary flex-1"
            >
              {uploadSyllabusMutation.isPending ? 'Uploadingâ€¦' : 'Upload'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Create department modal */}
      <Modal
        open={createDeptModal}
        onClose={() => setCreateDeptModal(false)}
        title="Create New Department"
      >
        <div className="space-y-4">
          <div>
            <label className="label">Department Name</label>
            <input
              value={newDept.departmentName}
              onChange={(e) => setNewDept((p) => ({ ...p, departmentName: e.target.value }))}
              placeholder="e.g. Computer Science"
              className="input"
            />
          </div>
          <div>
            <label className="label">Department Code</label>
            <input
              value={newDept.departmentCode}
              onChange={(e) => setNewDept((p) => ({ ...p, departmentCode: e.target.value.toUpperCase() }))}
              placeholder="e.g. CS"
              className="input"
            />
          </div>
          <div>
            <label className="label">Description <span className="text-slate-400 font-normal">(optional)</span></label>
            <textarea
              value={newDept.description}
              onChange={(e) => setNewDept((p) => ({ ...p, description: e.target.value }))}
              placeholder="Brief descriptionâ€¦"
              rows={3}
              className="input resize-none"
            />
          </div>
          <div className="flex gap-3">
            <button onClick={() => setCreateDeptModal(false)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => createDeptMutation.mutate(newDept)}
              disabled={!newDept.departmentName || !newDept.departmentCode || createDeptMutation.isPending}
              className="btn-primary flex-1"
            >
              {createDeptMutation.isPending ? 'Creatingâ€¦' : 'Create'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Department link modal */}
      <Modal
        open={!!deptModal}
        onClose={() => setDeptModal(null)}
        title={`Link Department â€” ${deptModal?.courseName}`}
      >
        <div className="space-y-4">
          <div>
            <label className="label">Select Department</label>
            <select
              value={selectedDeptId}
              onChange={(e) => setSelectedDeptId(e.target.value)}
              className="input"
            >
              <option value="">â€” Choose a department â€”</option>
              {departments.map((d) => (
                <option key={d.departmentId} value={d.departmentId}>
                  {d.departmentName} ({d.departmentCode})
                </option>
              ))}
            </select>
            {departments.length === 0 && (
              <p className="text-xs text-amber-600 mt-1">
                No departments found. Use the "New Department" button above to create one first.
              </p>
            )}
          </div>
          <div className="flex gap-3">
            <button onClick={() => setDeptModal(null)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => linkDeptMutation.mutate({ courseId: deptModal?.courseId, deptId: selectedDeptId })}
              disabled={!selectedDeptId || linkDeptMutation.isPending}
              className="btn-primary flex-1 flex items-center justify-center gap-1"
            >
              <Link size={15} /> {linkDeptMutation.isPending ? 'Linkingâ€¦' : 'Link'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

