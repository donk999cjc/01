/**
 * 教师端页面逻辑
 * 文件：js/teacher.js
 * 描述：教师工作台的Vue应用实例和业务逻辑
 */

new Vue({
    el: '#app',
    data() {
        return {
            currentTab: 'review',

            // 批改相关
            selAid: null,
            previewUrl: '',
            reviewing: false,
            reviewResult: null,

            // 学生提交相关（Agent批改用）
            selStudentId: null,
            studentSubmission: null,
            studentImageUrl: '',

            // 列表
            assignments: [],
            submissions: [],
            students: [],
            teachers: [],
            loading: false,

            // 新建/编辑作业
            showNewDlg: false,
            creating: false,
            editingId: null,
            af: {
                title: '',
                courseId: '',
                dueDate: '',
                totalScore: 100,
                description: '',

                // 新增作业扩展字段（比赛版）
                homeworkType: 'WRITTEN',
                gradingStandard: '',
                allowLate: false,
                latePenalty: 10,
                fileList: [],
                attachmentUrl: ''
            },
            showAdvanced: false,  // 控制高级设置展开

            // 图表实例
            lc: null, bc: null,

            // 分析统计
            stats: { students: 0, assignments: 0, avgScore: 0, trend: 0 },
            ranking: [],
            warnings: [],  // 学情预警列表
            deletedWarnings: [],  // 已删除的预警ID列表（用于持久化）

            // 新增：点击选择器弹窗
            showAssignmentPicker: false,
            showStudentPicker: false,

            // 新增：刷新状态
            refreshing: false,
        }
    },
    computed: {
        tabTitle() {
            return { review: 'AI 批改作业', assignments: '作业管理', analytics: '数据分析', students: '学生管理', teachers: '教师信息' }[this.currentTab]
        },
        selAssignment() { return this.assignments.find(a => a.id === this.selAid) },
        selStudentName() {
            const s = this.students.find(st => st.studentId === this.selStudentId)
            return s ? s.realName : '未知学生'
        },
        fbList() {
            if (!this.reviewResult || !this.reviewResult.feedback) return []
            return this.reviewResult.feedback.split('\n').filter(s => s.trim())
        }
    },
    created() {
        this.loadAll()
        // 从localStorage加载已删除的预警记录
        const saved = localStorage.getItem('deletedWarnings')
        if (saved) {
            try {
                this.deletedWarnings = JSON.parse(saved)
            } catch (e) {
                this.deletedWarnings = []
            }
        }
    },
    mounted() {
        window.addEventListener('resize', () => {
            if (this.lc) this.lc.resize()
            if (this.bc) this.bc.resize()
        })
    },
    watch: {
        currentTab(v) {
            if (v === 'analytics') {
                this.$nextTick(() => {
                    // 切换到数据分析标签时，再初始化图表
                    if (!this.lc) {
                        this.lc = echarts.init(document.getElementById('lineChart'));
                    }
                    if (!this.bc) {
                        this.bc = echarts.init(document.getElementById('barChart'));
                    }
                    this.renderCharts();
                });
            }
        }
    },
    methods: {
        handleLogout() {
            this.$confirm('确定要退出登录吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 清除本地存储的用户信息
                localStorage.removeItem('currentUser')
                sessionStorage.removeItem('currentUser')

                // 跳转到登录页
                window.location.href = '/'
            }).catch(() => {
                // 用户取消退出
                console.log('取消退出登录')
            })
        },

        // 格式化AI反馈内容（纯答案比对模式）
        formatFeedback(feedback) {
            if (!feedback) return ''

            // 尝试解析JSON格式
            if (feedback.includes('{') && feedback.includes('}')) {
                try {
                    const jsonMatch = feedback.match(/\{[\s\S]*\}/)
                    if (jsonMatch) {
                        const jsonObj = JSON.parse(jsonMatch[0])

                        let html = ''

                        // 新格式：questions 数组
                        if (jsonObj.questions && Array.isArray(jsonObj.questions)) {
                            html += '<div style="margin-bottom:12px">'
                            jsonObj.questions.forEach((q, idx) => {
                                const isCorrect = q.is_correct === true
                                const color = isCorrect ? '#67c23a' : '#F56C6C'
                                const bg = isCorrect ? '#f0f9eb' : '#fef0f0'

                                html += `<div style="margin-bottom:8px;padding:10px;background:${bg};border-radius:6px;border-left:4px solid ${color}">
                                    <strong>第${q.num || (idx + 1)}题</strong>
                                    <span style="float:right;color:${color};font-weight:bold">${isCorrect ? '✓ 正确' : '✗ 错误'} (${q.score || 0}分)</span>`

                                if (q.question) {
                                    html += `<div style="margin-top:5px;color:#606266;font-size:12px">题目: ${q.question}</div>`
                                }
                                if (q.student_answer) {
                                    html += `<div style="margin-top:3px;color:#909399;font-size:12px">学生答案: ${q.student_answer}</div>`
                                }
                                if (q.correct_answer) {
                                    html += `<div style="margin-top:3px;color:#409EFF;font-size:12px">正确答案: ${q.correct_answer}</div>`
                                }
                                if (!isCorrect && q.reason) {
                                    html += `<div style="margin-top:5px;color:#F56C6C;font-size:12px">错误原因: ${q.reason}</div>`
                                }

                                // 错题举一反三（重点！）
                                if (!isCorrect && (q.knowledge_point || q.similar_question || q.solution_guide)) {
                                    html += '<div style="margin-top:10px;padding:10px;background:#fff;border:2px dashed #E6A23C;border-radius:6px">'

                                    if (q.knowledge_point) {
                                        html += `<div style="color:#E6A23C;font-weight:600;margin-bottom:5px">
                                            知识点: ${q.knowledge_point}
                                        </div>`
                                    }

                                    if (q.similar_question) {
                                        html += `<div style="background:#fdf6ec;padding:8px;border-radius:4px;margin:8px 0">
                                            <div style="color:#E6A23C;font-weight:600;font-size:12px;margin-bottom:5px">
                                                推荐练习题:
                                            </div>
                                            <div style="color:#303133;line-height:1.6">${q.similar_question}</div>
                                        </div>`
                                    }

                                    if (q.solution_guide) {
                                        html += `<div style="background:#ecf5ff;padding:8px;border-radius:4px">
                                            <div style="color:#409EFF;font-weight:600;font-size:12px;margin-bottom:5px">
                                                解题思路:
                                            </div>
                                            <div style="color:#606266;line-height:1.6">${q.solution_guide}</div>
                                        </div>`
                                    }

                                    html += '</div>'
                                }

                                html += '</div>'
                            })
                            html += '</div>'
                        }

                        // 错题总结
                        if (jsonObj.error_summary) {
                            html += `<div style="margin-top:16px;padding:14px;background:#fef0f0;border-left:4px solid #F56C6C;border-radius:6px">
                                <h4 style="margin-bottom:8px;color:#F56C6C;font-size:14px;font-weight:600">错题总结与学习建议</h4>
                                <div style="line-height:1.7;color:#606266;font-size:13px">${jsonObj.error_summary}</div>
                            </div>`
                        }

                        // 总结
                        if (jsonObj.summary) {
                            html += `<div style="padding:12px;background:#f5f7fa;border-radius:6px;color:#303133;font-weight:600">
                                ${jsonObj.summary}
                            </div>`
                        }

                        // 旧格式兼容：detailed_analysis
                        if (jsonObj.detailed_analysis && Array.isArray(jsonObj.detailed_analysis)) {
                            html += '<div style="margin-bottom:12px">'
                            jsonObj.detailed_analysis.forEach((q, idx) => {
                                const isCorrect = q.is_correct === true || q.isCorrect === true
                                const color = isCorrect ? '#67c23a' : '#F56C6C'

                                html += `<div style="margin-bottom:8px;padding:8px;background:${isCorrect ? '#f0f9eb' : '#fef0f0'};border-radius:4px;border-left:3px solid ${color}">
                                    <strong>第${q.question_number || (idx + 1)}题</strong> - 
                                    <span style="color:${color};font-weight:600">${isCorrect ? '正确' : '错误'}</span>
                                    ${q.score != null ? ` (${q.score}分)` : ''}
                                    ${!isCorrect && q.error_analysis ? `<br><span style="color:#909399;font-size:12px">原因: ${q.error_analysis}</span>` : ''}
                                </div>`
                            })
                            html += '</div>'
                        }

                        if (html) return html
                    }
                } catch (e) { }
            }

            // 纯文本处理：只保留核心信息
            let text = feedback

            // 删除所有关于字迹、格式的废话
            const uselessPhrases = [
                /字迹[是否工整潦草清晰][，。！？]*/g,
                /书写[是否规范整洁][，。！？]*/g,
                /格式[是否规范标准][，。！？]*/g,
                /建议.*注意书写/g,
                /建议.*注意格式/g,
                /建议.*注意字迹/g,
                /卷面.*整洁/g,
                /书写.*认真/g,
                /整体.*工整/g,
            ]

            uselessPhrases.forEach(regex => {
                text = text.replace(regex, '')
            })

            // 去除emoji
            text = text.replace(/[📋📝🔍🎯🤖⚠️❌✅💡📊📈🌟👍✏️📝]/g, '')
            text = text.replace(/\*\*/g, '')

            return text
        },

        formatRecognizedContent(content) {
            if (!content) return ''
            if ((content.startsWith('{') || content.startsWith('"')) && content.length > 500) {
                return content.substring(0, 500) + '\n\n... (内容过长，已省略)'
            }
            return content.replace(/[📋📝🔍🎯🤖⚠️❌✅💡📊📈🌟👍]/g, '')
        },

        async loadAll() {
            this.loading = true
            try {
                const [r1, r2, r3, r4] = await Promise.all([
                    fetch('/api/assignments'), fetch('/api/submissions'),
                    fetch('/api/students'), fetch('/api/teachers')
                ])
                this.assignments = (await r1.json()) || []
                this.submissions = (await r2.json()) || []
                this.students = (await r3.json()) || []
                this.teachers = (await r4.json()) || []
                this.calcStats()
                if (this.currentTab === 'analytics') this.$nextTick(() => this.renderCharts())
            } catch (e) { console.error(e) } finally { this.loading = false }
        },
        async refreshAll() {
            this.refreshing = true
            await this.loadAll()
            setTimeout(() => { this.refreshing = false }, 800)
            this.$message.success('数据已刷新')
        },

        // ---- 批改 ----
        onFileChange(e) { const f = e.target.files[0]; if (f) this.readImg(f) },
        onDrop(e) { const f = e.dataTransfer.files[0]; if (f && f.type.startsWith('image/')) this.readImg(f) },
        readImg(file) {
            this.previewUrl = URL.createObjectURL(file)
            this.reviewResult = null
        },
        clearImg() { this.previewUrl = ''; this.reviewResult = null; this.$refs.fileInput.value = '' },

        // Agent模块批改相关方法
        onSelectAssignment() {
            this.studentSubmission = null
            this.studentImageUrl = ''
            if (this.selStudentId) {
                this.loadStudentSubmission()
            }
        },

        // 新增：点击选择作业
        selectAssignment(assignment) {
            this.selAid = assignment.id
            this.onSelectAssignment()
            this.$message.success(`已选择作业: ${assignment.title}`)
        },

        // 新增：点击选择学生
        async selectStudent(student) {
            this.selStudentId = student.studentId
            await this.loadStudentSubmission()
            this.$message.success(`已选择学生: ${student.realName}`)
        },

        async loadStudentSubmission() {
            if (!this.selAid || !this.selStudentId) {
                this.studentSubmission = null
                this.studentImageUrl = ''
                return
            }

            try {
                // 获取选中作业的正确assignmentId
                const selectedAssignment = this.assignments.find(a => a.id === this.selAid)
                const queryAssignmentId = selectedAssignment ? selectedAssignment.assignmentId : String(this.selAid)

                console.log('查询学生提交:', {
                    studentId: this.selStudentId,
                    assignmentId: queryAssignmentId,
                    selAid: this.selAid
                })

                // 方式1：使用assignmentId查询
                let res = await fetch(`/api/submissions/student/${this.selStudentId}/assignment/${queryAssignmentId}`)
                let data = null

                if (res.ok) {
                    data = await res.json()
                    console.log('方式1查询结果:', data)
                }

                // 如果方式1没找到，尝试用数字ID查询
                if (!data || !data.id) {
                    console.log('方式1未找到，尝试方式2...')
                    res = await fetch(`/api/submissions/student/${this.selStudentId}/assignment/${this.selAid}`)
                    if (res.ok) {
                        data = await res.json()
                        console.log('方式2查询结果:', data)
                    }
                }

                // 如果还没找到，尝试从所有提交中筛选
                if (!data || !data.id) {
                    console.log('方式2未找到，尝试方式3...')
                    res = await fetch(`/api/submissions/student/${this.selStudentId}`)
                    if (res.ok) {
                        const allSubmissions = await res.json()
                        console.log('该学生所有提交:', allSubmissions)

                        // 查找匹配的提交（通过assignment_id或数字ID）
                        data = allSubmissions.find(s =>
                            s.assignmentId === queryAssignmentId ||
                            s.assignmentId === String(this.selAid) ||
                            String(s.assignmentId) === String(this.selAid)
                        )
                        console.log('方式3筛选结果:', data)
                    }
                }

                if (data && data.id) {
                    this.studentSubmission = data
                    // 使用imageUrl显示图片
                    this.studentImageUrl = data.imageUrl || ''

                    console.log('找到学生提交:', {
                        id: data.id,
                        hasImage: !!this.studentImageUrl,
                        imageUrl: data.imageUrl
                    })

                    if (data.status === 'graded') {
                        this.reviewResult = {
                            score: data.score,
                            feedback: data.feedback,
                            recognizedContent: data.imageAnalysis,
                            learningSummary: data.learningSummary,  // 学习总结
                            submission: data
                        }
                        this.$message.info(`该学生已批改，得分：${data.score}分`)
                    } else {
                        this.reviewResult = null
                        if (this.studentImageUrl) {
                            this.$message.success(`已加载学生提交的作业图片`)
                        } else {
                            this.$message.warning('找到提交记录，但图片数据为空')
                        }
                    }
                } else {
                    this.studentSubmission = null
                    this.studentImageUrl = ''
                    console.log('未找到任何提交记录')
                }
            } catch (e) {
                console.error('加载学生提交失败:', e)
                this.studentSubmission = null
                this.studentImageUrl = ''
                this.$message.error('加载学生提交失败：' + e.message)
            }
        },

        async doReviewWithAgent() {
            if (!this.selAid) { this.$message.warning('请选择作业'); return }
            if (!this.selStudentId) { this.$message.warning('请选择学生'); return }
            if (!this.studentImageUrl) { this.$message.warning('该学生尚未提交作业'); return }

            this.reviewing = true
            this.$message.info('正在调用 Agent 模块进行智能分析...')

            try {
                const assignmentInfo = this.selAssignment ? `${this.selAssignment.title}\n课程：${this.selAssignment.courseId}\n要求：${this.selAssignment.description || '无'}` : ''

                // 使用filePath（从submission对象获取）
                const filePath = this.studentSubmission ? this.studentSubmission.filePath : null

                const res = await fetch(`/api/assignments/${this.selAid}/review-image`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        studentId: this.selStudentId,
                        filePath: filePath,
                        assignmentInfo: assignmentInfo
                    })
                })

                const d = await res.json()

                if (d.success) {
                    this.reviewResult = d
                    this.$message.success(`AI智能批改完成！得分：${d.score}分`)

                    // 刷新数据
                    await this.loadAll()
                    // 重新加载当前学生的提交状态
                    await this.loadStudentSubmission()
                } else {
                    this.$message.error(d.message || '批改失败')
                }
            } catch (e) {
                console.error('Agent批改错误:', e)
                this.$message.error('批改过程出错：' + e.message)
            } finally {
                this.reviewing = false
            }
        },
        goReview(a) { this.selAid = a.id; this.currentTab = 'review' },

        // ---- 作业CRUD ----
        statusText(s) { return { draft: '草稿', published: '已发布', closed: '已截止' }[s] || s },
        statusType(s) { return { draft: 'info', published: 'success', closed: 'danger' }[s] || 'info' },
        getSubCount(aid) {
            // 多种方式匹配assignmentId（兼容不同格式）
            return this.submissions.filter(s => {
                const sId = String(s.assignmentId || '')
                const targetId = String(aid)
                // 方式1：直接匹配
                if (sId === targetId) return true
                // 方式2：匹配assignment对象的id
                const assignment = this.assignments.find(a => a.id === Number(targetId))
                if (assignment && sId === String(assignment.assignmentId)) return true
                // 方式3：反向查找
                if (this.assignments.find(a => a.assignmentId === sId && a.id === Number(targetId))) return true
                return false
            }).length
        },

        // 删除提交记录
        async deleteSubmission(submission) {
            try {
                await this.$confirm('确定要删除这条提交记录吗？', '提示', {
                    type: 'warning'
                })

                const res = await fetch(`/api/submissions/${submission.id}`, { method: 'DELETE' })
                if (res.ok) {
                    this.$message.success('删除成功')
                    await this.loadAll()
                    this.renderCharts()
                } else {
                    this.$message.error('删除失败')
                }
            } catch (e) {
                console.error(e)
            }
        },
        createAssign() {
            this.editingId = null
            this.af = { title: '', courseId: '', dueDate: '', totalScore: 100, description: '' }
            this.showNewDlg = true
        },
        editAssign(a) {
            this.editingId = a.id
            console.log('✏️ 编辑作业，原始数据:', JSON.stringify(a))

            Object.assign(this.af, {
                title: a.title || '',
                courseId: a.courseId || '',
                dueDate: a.dueDate ? (a.dueDate instanceof Date ? a.dueDate.toISOString().slice(0, 16).replace('T', ' ') : a.dueDate.slice(0, 16)) : '',
                totalScore: a.totalScore || 100,
                description: a.description || '',

                // 新增：回显所有字段（解决编辑时不显示的问题）
                homeworkType: a.homeworkType || 'WRITTEN',
                gradingStandard: a.gradingStandard || '',

                // 文件路径和文件名（关键！）
                filePath: a.filePath || null,
                fileName: a.fileName || null,

                // 文件列表（用于 el-upload 组件显示已上传文件）
                fileList: (a.fileName && a.filePath) ? [{
                    name: a.fileName,
                    url: a.filePath,
                    status: 'success'
                }] : []
            })

            // 如果有附件，提示用户
            if (a.fileName) {
                console.log('📎 已有附件:', a.fileName, '路径:', a.filePath)
            }

            this.showNewDlg = true
        },
        saveAssign() {
            if (!this.af.title || !this.af.courseId) {
                this.$message.warning('请填写必填项')
                return
            }
            this.creating = true

            const url = this.editingId ? `/api/assignments/${this.editingId}` : '/api/assignments'
            const method = this.editingId ? 'PUT' : 'POST'

            fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.af) })
                .then(() => {
                    this.$message.success(this.editingId ? '已保存' : '已创建')
                    this.showNewDlg = false
                    this.af = { title: '', courseId: '', dueDate: '', totalScore: 100, description: '' }
                    this.editingId = null
                    this.loadAll()
                })
                .catch(() => this.$message.error('操作失败'))
                .finally(() => this.creating = false)
        },
        delAssign(a) {
            this.$confirm('确定删除？', '', { type: 'warning' }).then(() => {
                fetch(`/api/assignments/${a.id}`, { method: 'DELETE' }).then(() => { this.$message.success('已删'); this.loadAll() })
            })
        },

        // ---- 学生CRUD ----
        editStudent(s) {
            this.$prompt('修改班级', '编辑学生', { inputValue: s.classes || '', inputPlaceholder: '输入新班级' }).then(({ value }) => {
                fetch(`/api/students/${s.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...s, classes: value }) })
                    .then(() => { this.$message.success('已修改'); this.loadAll() })
            }).catch(() => { })
        },
        delStudent(s) {
            this.$confirm('确定删除该学生？', '', { type: 'warning' }).then(() => {
                fetch(`/api/students/${s.id}`, { method: 'DELETE' }).then(() => { this.$message.success('已删'); this.loadAll() })
            })
        },
        deleteWarning(index) {
            const warning = this.warnings[index]
            this.$confirm('确定删除该预警记录？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }).then(() => {
                // 从数组中删除
                this.warnings.splice(index, 1)
                // 添加到已删除列表并持久化
                if (warning && warning.id) {
                    this.deletedWarnings.push(warning.id)
                    localStorage.setItem('deletedWarnings', JSON.stringify(this.deletedWarnings))
                    console.log('预警已删除并持久化保存:', warning.id)
                    console.log('当前已删除的预警列表:', this.deletedWarnings)
                }
                this.$message.success('预警已删除（刷新后仍保持删除状态）')
            }).catch(() => { })
        },

        // ---- 数据分析 ----
        calcStats() {
            const g = this.submissions.filter(s => s.score != null && s.score !== '')
            this.stats = { students: this.students.length, assignments: this.assignments.length, avgScore: g.length ? (g.reduce((a, b) => a + Number(b.score), 0) / g.length).toFixed(1) : 0, trend: 0 }
            if (g.length >= 4) {
                const m = Math.floor(g.length / 2)
                const a1 = g.slice(0, m).reduce((a, i) => a + Number(i.score), 0) / m
                const a2 = g.slice(m).reduce((a, i) => a + Number(i.score), 0) / g.slice(m).length
                this.stats.trend = (a2 - a1).toFixed(1)
            }

            // 排行榜
            const map = {}
            g.forEach(s => {
                if (!map[s.studentId]) map[s.studentId] = { scores: [] }
                map[s.studentId].scores.push(Number(s.score))
            })
            this.ranking = Object.entries(map).map(([sid, d]) => {
                const st = this.students.find(s => s.studentId === sid) || {}
                const sc = [...d.scores]
                let tr = 0
                if (sc.length >= 4) { const m2 = Math.floor(sc.length / 2); tr = ((sc.slice(m2).reduce((a, b) => a + b, 0) / sc.slice(m2).length) - (sc.slice(0, m2).reduce((a, b) => a + b, 0) / m2)).toFixed(1) }
                return { sid, name: st.realName || sid, avg: (sc.reduce((a, b) => a + b, 0) / sc.length).toFixed(1), max: Math.max(...sc), cnt: sc.length, trend: Number(tr), recent: sc.slice(-5) }
            }).sort((a, b) => Number(b.avg) - Number(a.avg))

            // 学情预警分析
            this.analyzeWarnings()
        },

        analyzeWarnings() {
            this.warnings = []

            this.students.forEach(student => {
                const studentSubmissions = this.submissions.filter(s => s.studentId === student.studentId && s.score != null)

                // 1. 成绩下滑预警（最近3次平均分比前3次低10分以上）
                if (studentSubmissions.length >= 6) {
                    const recent3 = studentSubmissions.slice(-3).map(s => Number(s.score))
                    const prev3 = studentSubmissions.slice(-6, -3).map(s => Number(s.score))

                    const recentAvg = recent3.reduce((a, b) => a + b, 0) / 3
                    const prevAvg = prev3.reduce((a, b) => a + b, 0) / 3

                    if (prevAvg - recentAvg >= 10) {
                        const warningId = `${student.studentId}_score_decline`
                        if (!this.deletedWarnings.includes(warningId)) {
                            this.warnings.push({
                                id: warningId,
                                type: 'score_decline',
                                level: prevAvg - recentAvg >= 20 ? 'high' : 'medium',
                                studentName: student.realName,
                                studentId: student.studentId,
                                message: `成绩明显下滑！前3次均分${prevAvg.toFixed(1)}分 → 最近3次${recentAvg.toFixed(1)}分（下降${(prevAvg - recentAvg).toFixed(1)}分）`,
                                suggestion: '需要关注'
                            })
                        }
                    }
                }

                // 2. 低分预警（有作业低于60分）
                const lowScores = studentSubmissions.filter(s => Number(s.score) < 60)
                if (lowScores.length > 0) {
                    const latestLow = lowScores[lowScores.length - 1]
                    const warningId = `${student.studentId}_low_score`
                    if (!this.deletedWarnings.includes(warningId)) {
                        this.warnings.push({
                            id: warningId,
                            type: 'low_score',
                            level: latestLow.score < 40 ? 'high' : 'medium',
                            studentName: student.realName,
                            studentId: student.studentId,
                            message: `存在不及格作业！最近一次仅得${latestLow.score}分`,
                            suggestion: '需辅导'
                        })
                    }
                }

                // 3. 缺交作业预警
                const submittedAssignmentIds = new Set(studentSubmissions.map(s => String(s.assignmentId)))
                const unsubmittedAssignments = this.assignments.filter(a => !submittedAssignmentIds.has(String(a.id)) && !submittedAssignmentIds.has(a.assignmentId))

                if (unsubmittedAssignments.length > 0) {
                    const warningId = `${student.studentId}_no_submit`
                    if (!this.deletedWarnings.includes(warningId)) {
                        this.warnings.push({
                            id: warningId,
                            type: 'no_submit',
                            level: unsubmittedAssignments.length >= 2 ? 'high' : 'medium',
                            studentName: student.realName,
                            studentId: student.studentId,
                            message: `缺交${unsubmittedAssignments.length}次作业：${unsubmittedAssignments.map(a => a.title || `作业${a.id}`).join('、')}`,
                            suggestion: '请督促'
                        })
                    }
                }

                // 4. 连续低分预警（连续2次低于70分）
                if (studentSubmissions.length >= 2) {
                    const lastTwo = studentSubmissions.slice(-2)
                    if (Number(lastTwo[0].score) < 70 && Number(lastTwo[1].score) < 70) {
                        const warningId = `${student.studentId}_continuous_low`
                        if (!this.deletedWarnings.includes(warningId)) {
                            this.warnings.push({
                                id: warningId,
                                type: 'continuous_low',
                                level: 'medium',
                                studentName: student.realName,
                                studentId: student.studentId,
                                message: `连续2次作业低于70分（${lastTwo[0].score}分、${lastTwo[1].score}分）`,
                                suggestion: '建议谈话'
                            })
                        }
                    }
                }
            })

            // 按严重程度排序：高危 > 中等 > 轻微
            this.warnings.sort((a, b) => {
                const levelOrder = { high: 0, medium: 1, low: 2 }
                return levelOrder[a.level] - levelOrder[b.level]
            })
        },

        renderCharts() {
            this.calcStats();

            // 确保图表实例已初始化
            if (!this.lc) {
                this.lc = echarts.init(document.getElementById('lineChart'));
            }
            if (!this.bc) {
                this.bc = echarts.init(document.getElementById('barChart'));
            }

            // 排序作业
            const sortedAs = [...this.assignments].sort((a, b) => new Date(a.createdAt || 0) - new Date(b.createdAt || 0));
            const xData = sortedAs.map(a => a.title || `作业${a.id}`);
            const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#00CED1'];

            // ==================== 折线图：每个学生成绩曲线 ====================
            const series = [];
            let ci = 0;
            this.students.forEach(st => {
                const scores = sortedAs.map(a => {
                    const sub = this.submissions.find(s =>
                        s.studentId === st.studentId &&
                        (String(s.assignmentId) === String(a.id) || String(s.assignmentId) === String(a.assignmentId))
                    );
                    return sub && sub.score != null ? Number(sub.score) : null;
                });

                if (scores.some(s => s !== null)) {
                    series.push({
                        name: st.realName,
                        type: 'line',
                        smooth: true,
                        data: scores,
                        lineStyle: { width: 2 },
                        itemStyle: { color: colors[ci++ % colors.length] }
                    });
                }
            });

            if (series.length === 0) {
                this.lc.setOption({ title: { text: '暂无批改数据', textStyle: { color: '#909399' } } });
            } else {
                this.lc.setOption({
                    tooltip: { trigger: 'axis' },
                    legend: { bottom: 0, type: 'scroll' },
                    grid: { left: '3%', right: '4%', bottom: '12%', top: '8%' },
                    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 25 } },
                    yAxis: { type: 'value', min: 0, max: 100, name: '分数' },
                    series: series
                }, true);
            }

            // ==================== 柱状图：各作业分数分布 ====================
            const ranges = ['<60', '60-69', '70-79', '80-89', '90-100'];
            const barColors = ['#f56c6c', '#e6a23c', '#409EFF', '#67c23a', '#67c23a'];
            const barSeries = ranges.map(r => ({ name: r, type: 'bar', stack: 'total', data: [] }));

            sortedAs.forEach(a => {
                const subs = this.submissions.filter(s => {
                    const aid = String(s.assignmentId || '');
                    return (aid === String(a.id) || aid === String(a.assignmentId)) && s.score != null;
                });

                const c = [0, 0, 0, 0, 0];
                subs.forEach(s => {
                    const sc = Number(s.score);
                    if (sc < 60) c[0]++;
                    else if (sc < 70) c[1]++;
                    else if (sc < 80) c[2]++;
                    else if (sc < 90) c[3]++;
                    else c[4]++;
                });
                barSeries.forEach((s, i) => s.data.push(c[i]));
            });

            if (sortedAs.length === 0 || barSeries.every(s => s.data.every(d => d === 0))) {
                this.bc.setOption({ title: { text: '暂无分布数据', textStyle: { color: '#909399' } } });
            } else {
                this.bc.setOption({
                    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
                    legend: { bottom: 0 },
                    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%' },
                    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 25 } },
                    yAxis: { type: 'value', name: '人数' },
                    series: barSeries.map((s, i) => ({ ...s, itemStyle: { color: barColors[i] } }))
                }, true);
            }
        },
        handleFileChange(file, fileList) {
            this.af.fileList = fileList;
        },

        // 自定义上传方法（使用正确的后端接口）
        async customUpload(options) {
            const { file, onSuccess, onError } = options;

            // 如果还没有创建作业，先提示
            if (!this.editingId && !this.af.title) {
                this.$message.warning('请先填写作业标题后再上传附件');
                onError(new Error('请先填写作业信息'));
                return;
            }

            try {
                const formData = new FormData();
                formData.append('file', file);

                let uploadUrl;
                if (this.editingId) {
                    // 编辑模式：直接上传到已有作业
                    uploadUrl = `/api/assignments/${this.editingId}/upload-attachment`;
                } else {
                    // 新建模式：先创建作业，再上传
                    const createRes = await fetch('/api/assignments', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            title: this.af.title || '临时作业',
                            courseId: this.af.courseId || 'TEMP',
                            totalScore: this.af.totalScore || 100,
                            description: this.af.description || ''
                        })
                    });
                    const createdData = await createRes.json();
                    this.editingId = createdData.id;
                    uploadUrl = `/api/assignments/${createdData.id}/upload-attachment`;
                }

                const response = await fetch(uploadUrl, {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    const result = await response.text();
                    console.log('上传成功:', result);
                    
                    // 更新 af 对象的文件路径（关键！）
                    this.$set(this.af, 'filePath', '/uploads/' + file.name);
                    this.$set(this.af, 'fileName', file.name);
                    
                    onSuccess({ url: this.af.filePath }, file);
                    this.$message.success(`✅ 附件 "${file.name}" 上传成功！`);
                } else {
                    const errorText = await response.text();
                    console.error('上传失败:', errorText);
                    onError(new Error(errorText || '上传失败'));
                    this.$message.error('❌ 附件上传失败：' + (errorText || '服务器错误'));
                }
            } catch (error) {
                console.error('上传异常:', error);
                onError(error);
                this.$message.error('❌ 上传异常：' + error.message);
            }
        },

        handleUploadSuccess(response, file, fileList) {
            console.log('handleUploadSuccess:', response, file);
            // filePath 和 fileName 已经在 customUpload 中设置了
            if (response && response.url) {
                this.af.attachmentUrl = response.url;
            }
        },
        handleUploadError(err) {
            console.error('handleUploadError:', err);
            this.$message.error('❌ 附件上传失败，请检查文件格式或网络。');
        }
    }
})
