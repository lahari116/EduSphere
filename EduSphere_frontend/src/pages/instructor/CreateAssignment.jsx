import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { assignmentService } from '../../services/assignmentService'
import { ArrowLeft, Plus, Trash2, CheckCircle } from 'lucide-react'
import toast from 'react-hot-toast'
import clsx from 'clsx'

const OPTION_KEYS = ['A', 'B', 'C', 'D']
const emptyQ = () => ({ questionText: '', optionA: '', optionB: '', optionC: '', optionD: '', correctOption: '' })

export default function CreateAssignment() {
  const { courseId } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    title: '',
    instructions: '',
    timeLimitMinutes: 30,
    submissionDeadline: '',
  })
  const [questions, setQuestions] = useState([emptyQ()])

  const createMutation = useMutation({
    mutationFn: (data) => assignmentService.create(courseId, data),
    onSuccess: () => {
      toast.success('Assignment created!')
      navigate(-1)
    },
  })

  const addQuestion = () => setQuestions([...questions, emptyQ()])
  const removeQuestion = (i) => setQuestions(questions.filter((_, qi) => qi !== i))

  const updateQ = (i, field, val) => {
    const q = [...questions]
    q[i] = { ...q[i], [field]: val }
    setQuestions(q)
  }

  const submit = (e) => {
    e.preventDefault()
    for (let i = 0; i < questions.length; i++) {
      const q = questions[i]
      if (!q.correctOption) {
        toast.error(`Please select the correct answer for Question ${i + 1}`)
        return
      }
      if (!q.optionA || !q.optionB || !q.optionC || !q.optionD) {
        toast.error(`Please fill in all 4 options for Question ${i + 1}`)
        return
      }
    }
    const payload = {
      ...form,
      questions: questions.map((q, idx) => ({
        questionText: q.questionText,
        optionA: q.optionA,
        optionB: q.optionB,
        optionC: q.optionC,
        optionD: q.optionD,
        correctOption: q.correctOption,   // already "A"|"B"|"C"|"D"
        sequenceNumber: idx + 1,
      })),
    }
    createMutation.mutate(payload)
  }

  return (
    <form onSubmit={submit} className="space-y-6 animate-fade-in max-w-3xl">
      <button type="button" onClick={() => navigate(-1)} className="btn-ghost -ml-2">
        <ArrowLeft size={16} /> Back
      </button>

      <div>
        <h1 className="page-title">Create Assignment</h1>
        <p className="text-slate-500 text-sm mt-0.5">Add MCQ questions with auto-grading</p>
      </div>

      {/* Assignment details */}
      <div className="card space-y-4">
        <h3 className="section-title">Assignment Details</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="sm:col-span-2">
            <label className="label">Title</label>
            <input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="Quiz 1: Introduction" className="input" />
          </div>
          <div className="sm:col-span-2">
            <label className="label">Instructions</label>
            <textarea value={form.instructions} onChange={(e) => setForm({ ...form, instructions: e.target.value })} rows={2} placeholder="Answer all questions carefully…" className="input resize-none" />
          </div>
          <div>
            <label className="label">Time Limit (minutes)</label>
            <input type="number" min={1} value={form.timeLimitMinutes} onChange={(e) => setForm({ ...form, timeLimitMinutes: +e.target.value })} className="input" />
          </div>
          <div>
            <label className="label">Submission Deadline</label>
            <input type="datetime-local" required value={form.submissionDeadline} onChange={(e) => setForm({ ...form, submissionDeadline: e.target.value })} className="input" />
          </div>
        </div>
      </div>

      {/* Questions */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="section-title">Questions ({questions.length})</h3>
          <button type="button" onClick={addQuestion} className="btn-secondary">
            <Plus size={16} /> Add Question
          </button>
        </div>

        {questions.map((q, qi) => (
          <div key={qi} className="card space-y-4 border-l-4 border-l-primary-300">
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-center gap-2">
                <span className="w-7 h-7 rounded-lg bg-primary-100 text-primary-700 text-sm font-bold flex items-center justify-center">
                  {qi + 1}
                </span>
                <span className="text-sm font-medium text-slate-600">Question {qi + 1}</span>
              </div>
              {questions.length > 1 && (
                <button type="button" onClick={() => removeQuestion(qi)} className="p-1.5 text-rose-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors">
                  <Trash2 size={15} />
                </button>
              )}
            </div>

            <div>
              <label className="label">Question Text</label>
              <textarea
                required
                value={q.questionText}
                onChange={(e) => updateQ(qi, 'questionText', e.target.value)}
                rows={2}
                placeholder="Enter your question…"
                className="input resize-none"
              />
            </div>

            <div>
              <label className="label">
                Options — <span className="text-emerald-600 font-normal">click the circle to mark the correct answer</span>
              </label>
              <div className="space-y-2">
                {OPTION_KEYS.map((letter) => {
                  const fieldKey = `option${letter}`
                  const isCorrect = q.correctOption === letter
                  return (
                    <div key={letter} className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => updateQ(qi, 'correctOption', letter)}
                        className={clsx(
                          'w-7 h-7 rounded-full border-2 flex items-center justify-center flex-shrink-0 transition-all',
                          isCorrect
                            ? 'border-emerald-500 bg-emerald-500 text-white'
                            : 'border-slate-200 hover:border-emerald-300'
                        )}
                        title={`Mark ${letter} as correct`}
                      >
                        {isCorrect && <CheckCircle size={14} />}
                      </button>
                      <span className="w-6 text-xs font-bold text-slate-400">{letter}.</span>
                      <input
                        required
                        value={q[fieldKey]}
                        onChange={(e) => updateQ(qi, fieldKey, e.target.value)}
                        placeholder={`Option ${letter}`}
                        className="input"
                      />
                    </div>
                  )
                })}
              </div>
              {!q.correctOption && (
                <p className="text-xs text-amber-600 mt-1.5">⚠ Click the circle next to the correct answer</p>
              )}
            </div>
          </div>
        ))}
      </div>

      <div className="flex gap-3 pb-6">
        <button type="button" onClick={() => navigate(-1)} className="btn-secondary">Cancel</button>
        <button type="submit" disabled={createMutation.isPending} className="btn-primary flex-1">
          {createMutation.isPending ? 'Creating…' : 'Create Assignment'}
        </button>
      </div>
    </form>
  )
}
