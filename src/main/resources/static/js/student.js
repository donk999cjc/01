/**
 * 学生端页面逻辑（增强版）
 * 文件：js/student.js
 * 描述：学生学习台的Vue应用实例和业务逻辑
 */

new Vue({
    el: '#app',
    data() {
        return {
            currentTab: 'assignments',

            userInfo: {},
            profileForm: {
                realName: '',
                email: '',
                phone: ''
            },
            passwordForm: {
                oldPassword: '',
                newPassword: '',
                confirmPassword: ''
            },

            selAid: null,
            selectedFile: null,
            previewUrl: '',
            submitting: false,
            submitOk: false,

            assignments: [],
            submissions: [],
            teachers: [],
            loading: false,

            chart: null,

            chatMessages: [],
            chatInput: '',
            chatLoading: false,
            chatMode: 'normal',
            ragAvailable: false,
            lastUserMessage: '',

            // 新增：UI状态
            refreshing: false,
            generatingPractice: false,
        }
    },
    computed: {
        tabTitle() {
            return {
                assignments: '作业列表',
                submit: '提交作业',
                results: '批改结果',
                analysis: '成绩分析',
                chat: 'AI 助手',
                profile: '个人信息',
                teachers: '教师信息'
            }[this.currentTab]
        },
        selAssignment() { return this.assignments.find(a => a.id === this.selAid) },
        gradedSubmissions() {
            return (this.submissions || []).filter(s => s.studentId === this.userInfo.studentId && s.status === 'graded')
        },
        recentSubs() {
            return (this.submissions || []).filter(s => s.studentId === this.userInfo.studentId).slice(-10).reverse()
        },
        myStats() {
            const mine = this.submissions.filter(s => s.studentId === this.userInfo.studentId && s.score != null)
            const g = mine.filter(s => s.status === 'graded')
            return {
                total: g.length,
                avgScore: g.length ? (g.reduce((a, b) => a + Number(b.score), 0) / g.length).toFixed(1) : 0,
                maxScore: g.length ? Math.max(...g.map(s => Number(s.score))) : null
            }
        }
    },
    created() {
        this.initUser()
        this.loadAll()
        this.checkRAGStatus()
    },
    mounted() {

    },
    watch: {
        currentTab(v) {
            if (v === 'analysis') {
                this.$nextTick(() => this.renderMyChart())
            }
            if (v === 'chat') {
                this.$nextTick(() => {
                    this.autoResizeTextarea()
                })
            }
        }
    },
    methods: {
        initUser() {
            try {
                const userStr = localStorage.getItem('user')
                if (userStr) {
                    this.userInfo = JSON.parse(userStr)
                    this.profileForm.realName = this.userInfo.realName || ''
                    this.profileForm.email = this.userInfo.email || ''
                    this.profileForm.phone = this.userInfo.phone || ''
                }
            } catch (e) {
                console.error(e)
            }
        },

        switchTab(tab) {
            this.currentTab = tab
        },

        handleLogout() {
            if (confirm('确定要退出登录吗？')) {
                localStorage.removeItem('user')
                localStorage.removeItem('token')
                window.location.href = '/login.html'
            }
        },

        async loadAll() {
            this.loading = true
            this.refreshing = true
            try {
                const [r1, r2, r3] = await Promise.all([
                    fetch('/api/assignments').catch(() => ({ json: async () => [] })),
                    fetch('/api/submissions').catch(() => ({ json: async () => [] })),
                    fetch('/api/teachers').catch(() => ({ json: async () => [] }))
                ])
                this.assignments = (await r1.json()) || []
                this.submissions = (await r2.json()) || []
                this.teachers = (await r3.json()) || []
                if (this.currentTab === 'analysis') {
                    this.$nextTick(() => this.renderMyChart())
                }
            } catch (e) {
                console.error(e)
            } finally {
                this.loading = false
                setTimeout(() => { this.refreshing = false }, 600)
            }
        },
        statusText(s) { return { draft: '草稿', published: '已发布', closed: '已截止' }[s] || s },
        statusType(s) { return { draft: 'info', published: 'success', closed: 'danger' }[s] || 'info' },
        getAssignTitle(aid) { const a = this.assignments.find(x => x.id === aid); return a ? a.title : `作业${aid}` },
        getAssignTotal(aid) { const a = this.assignments.find(x => x.id === aid); return a ? a.totalScore : 100 },
        splitFeedback(text) { if (!text) return []; return text.split('\n').filter(s => s.trim()) },

        // ==================== 新增：智能反馈解析（解决JSON显示问题）====================
        parseFeedback(feedback) {
            if (!feedback) return '<p style="color:#909399">暂无反馈</p>'

            let content = String(feedback)

            // 尝试解析JSON格式
            if (content.includes('```json')) {
                try {
                    const jsonMatch = content.match(/```json\s*([\s\S]*?)```/)
                    if (jsonMatch && jsonMatch[1]) {
                        // 清理控制字符（修复 "Bad control character" 错误）
                        let cleanedJson = this.cleanJsonString(jsonMatch[1].trim())
                        const jsonData = JSON.parse(cleanedJson)
                        return this.formatJsonFeedback(jsonData)
                    }
                } catch (e) {
                    console.warn('JSON解析失败，使用原始内容:', e.message)
                }
            }

            // 尝试直接解析JSON
            const trimmedContent = content.trim()
            if (trimmedContent.startsWith('{') || trimmedContent.startsWith('[')) {
                try {
                    let cleanedJson = this.cleanJsonString(trimmedContent)
                    const jsonData = JSON.parse(cleanedJson)
                    return this.formatJsonFeedback(jsonData)
                } catch (e) {
                    console.warn('直接JSON解析失败:', e.message)
                }
            }

            // 普通文本格式化
            return this.formatPlainText(content)
        },

        /**
         * 清理JSON字符串中的非法控制字符
         * 解决 "Bad control character in string literal" 错误
         */
        cleanJsonString(str) {
            if (!str) return str

            // 1. 替换字符串值中的未转义控制字符（保留已转义的 \n, \t 等）
            str = str.replace(/[\x00-\x08\x0B\x0C\x0E-\x1F]/g, '')

            // 2. 确保换行符在JSON字符串值内被正确转义
            // 匹配 "..." 字符串内部的换行符并替换为 \n
            str = str.replace(/"(?:[^"\\]|\\.)*"/g, (match) => {
                return match.replace(/\n/g, '\\n').replace(/\r/g, '\\r').replace(/\t/g, '\\t')
            })

            return str
        },

        formatJsonFeedback(data) {
            let html = ''

            // 处理score
            if (data.score) {
                html += `<div class="feedback-section">
                    <div class="feedback-label">📊 得分</div>
                    <div class="feedback-value score-value">${data.score}分</div>
                </div>`
            }

            // 处理content_review或feedback
            if (data.content_review || data.feedback) {
                const review = data.content_review || data.feedback
                html += `<div class="feedback-section">
                    <div class="feedback-label">✅ 整体评价</div>
                    <div class="feedback-value">${this.formatText(review)}</div>
                </div>`
            }

            // 处理improvement_suggestions
            if (data.improvement_suggestions) {
                html += `<div class="feedback-section">
                    <div class="feedback-label">💡 改进建议</div>
                    <ul class="feedback-list">
                        ${Array.isArray(data.improvement_suggestions)
                            ? data.improvement_suggestions.map(s => `<li>${this.formatText(s)}</li>`).join('')
                            : `<li>${this.formatText(data.improvement_suggestions)}</li>`}
                    </ul>
                </div>`
            }

            // 如果是数组格式
            if (Array.isArray(data)) {
                html += data.map((item, idx) => `
                    <div class="feedback-item" style="margin-bottom:10px;padding:12px;background:#f8f9fa;border-radius:8px;">
                        ${typeof item === 'object' ? this.formatJsonFeedback(item) : this.formatText(item)}
                    </div>
                `).join('')
            }

            // 如果没有匹配到任何字段，显示原始数据
            if (!html) {
                html = `<pre class="raw-feedback">${JSON.stringify(data, null, 2)}</pre>`
            }

            return html || '<p style="color:#909399">暂无详细反馈</p>'
        },

        formatPlainText(text) {
            return text.split('\n').map(line => {
                line = line.trim()
                if (!line) return ''
                // 处理Markdown标记
                line = line.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
                line = line.replace(/`([^`]+)`/g, '<code style="background:#f0f0f0;padding:2px 6px;border-radius:4px;font-size:12px;">$1</code>')
                return `<p style="margin:4px 0;line-height:1.7;">${line}</p>`
            }).filter(l => l).join('')
        },

        formatText(text) {
            if (!text) return ''
            text = String(text)
            text = text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
            text = text.replace(/`([^`]+)`/g, '<code style="background:#f0f0f0;padding:2px 6px;border-radius:4px;font-size:12px;">$1</code>')
            text = text.replace(/\n/g, '<br>')
            return text
        },

        formatLearningSummary(summary) {
            if (!summary) return ''
            let content = summary

            // 尝试解析JSON
            if (content.includes('```json') || content.startsWith('{')) {
                try {
                    const jsonMatch = content.match(/```json\s*([\s\S]*?)```/) || [null, content]
                    const jsonData = JSON.parse((jsonMatch[1] || content).trim())
                    if (jsonData.summary || jsonData.content || jsonData.text) {
                        return this.formatText(jsonData.summary || jsonData.content || jsonData.text)
                    }
                } catch (e) {}
            }

            return this.formatText(content)
        },

        // ==================== 新增：错题分析功能 ====================
        analyzeErrors(submission) {
            if (!submission.imageAnalysis) return '<p style="color:#909399">暂无详细分析</p>'

            const feedback = submission.imageAnalysis
            const errors = []

            // 提取错误关键词和模式
            const errorPatterns = [
                { keyword: '错误', icon: '❌', color: '#F56C6C' },
                { keyword: '不正确', icon: '❌', color: '#F56C6C' },
                { keyword: '需要改进', icon: '⚠️', color: '#E6A23C' },
                { keyword: '建议', icon: '💡', color: '#409EFF' },
                { keyword: '注意', icon: '⚠️', color: '#E6A23C' }
            ]

            const lines = feedback.split('\n').filter(line => line.trim())

            lines.forEach(line => {
                for (const pattern of errorPatterns) {
                    if (line.toLowerCase().includes(pattern.keyword.toLowerCase())) {
                        errors.push({
                            text: line,
                            icon: pattern.icon,
                            color: pattern.color
                        })
                        break
                    }
                }
            })

            if (errors.length === 0) {
                // 如果没找到明确的错误信息，根据分数生成通用分析
                const score = Number(submission.score)
                if (score < 60) {
                    return `<div class="error-item">
                        <span class="error-icon">🔴</span>
                        <span class="error-text">基础概念掌握不足，需要加强基础知识的学习和理解</span>
                    </div><div class="error-item">
                        <span class="error-icon">📚</span>
                        <span class="error-text">建议回顾课本相关章节，多做基础练习题</span>
                    </div>`
                } else if (score < 80) {
                    return `<div class="error-item">
                        <span class="error-icon">🟡</span>
                        <span class="error-text">部分知识点掌握不够牢固，存在细节错误</span>
                    </div><div class="error-item">
                        <span class="error-icon">✏️</span>
                        <span class="error-text">建议针对薄弱环节进行专项练习</span>
                    </div>`
                }
                return '<p style="color:#67c23a">整体表现良好！继续保持！</p>'
            }

            return errors.map(err => `
                <div class="error-item" style="border-left-color:${err.color}">
                    <span class="error-icon">${err.icon}</span>
                    <span class="error-text">${err.text}</span>
                </div>
            `).join('')
        },

        // ==================== 新增：生成巩固练习题 ====================
        async generatePractice(submission) {
            this.generatingPractice = true

            try {
                // 构建错题分析上下文
                const context = `【学生错题分析】
作业名称：${this.getAssignTitle(submission.assignmentId)}
得分：${submission.score}分
批改反馈：
${submission.imageAnalysis}

请根据以上批改反馈，为该学生生成3-5道针对性的巩固练习题。
要求：
1. 题目要针对学生的薄弱环节
2. 难度适中，循序渐进
3. 包含详细的答案和解析
4. 标注每道题的难度等级（easy/medium/hard）

请以JSON数组格式返回，格式如下：
[
  {
    "question": "题目内容",
    "answer": "答案",
    "explanation": "解析",
    "difficulty": "easy/medium/hard",
    "knowledgePoint": "对应的知识点"
  }
]`

                const res = await fetch('/api/ai/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        message: context,
                        systemPrompt: '你是一个专业的教育AI助手，专门为学生生成个性化练习题。必须严格按照要求返回JSON格式的题目数组。'
                    })
                })

                const data = await res.json()

                if (data.success && data.response) {
                    // 解析AI返回的练习题
                    try {
                        const jsonMatch = data.response.match(/\[[\s\S]*\]/)
                        if (jsonMatch) {
                            const exercises = JSON.parse(jsonMatch[0])
                            this.$set(submission, 'practiceExercises', exercises)
                            this.$message.success(`成功生成${exercises.length}道巩固练习题！`)
                        } else {
                            throw new Error('无法解析练习题')
                        }
                    } catch (parseError) {
                        console.error('解析练习题失败:', parseError)
                        // 使用默认练习题
                        this.$set(submission, 'practiceExercises', this.getDefaultExercises())
                        this.$message.warning('使用默认练习题')
                    }
                } else {
                    throw new Error('AI服务返回错误')
                }
            } catch (e) {
                console.error('生成练习题失败:', e)
                // 使用默认练习题作为降级方案
                this.$set(submission, 'practiceExercises', this.getDefaultExercises())
                this.$message.error('生成失败，已加载默认练习题')
            } finally {
                this.generatingPractice = false
            }
        },

        getDefaultExercises() {
            return [
                {
                    question: '请简述本章节的核心概念，并举例说明其在实际中的应用。',
                    answer: '核心概念包括...（根据具体课程填写）',
                    explanation: '这道题帮助你巩固对基本概念的理解...',
                    difficulty: 'easy',
                    knowledgePoint: '基础概念'
                },
                {
                    question: '根据以下条件，计算/推导出结果：[具体题目]',
                    answer: '解题步骤...',
                    explanation: '这道题考察你对知识点的应用能力...',
                    difficulty: 'medium',
                    knowledgePoint: '应用能力'
                },
                {
                    question: '综合运用本章所学知识，解决这个复杂问题：[综合性题目]',
                    answer: '综合解答...',
                    explanation: '这是一道拔高题，检验你的综合运用能力...',
                    difficulty: 'hard',
                    knowledgePoint: '综合运用'
                }
            ]
        },

        getDifficultyText(difficulty) {
            const map = { easy: '简单', medium: '中等', hard: '困难' }
            return map[difficulty] || '未知'
        },

        // ==================== 新增：作业附件查看功能 ====================
        hasAttachment(assignment) {
            if (!assignment) {
                console.log('⚠️ hasAttachment: assignment 为空')
                return false
            }

            const hasFilePath = !!assignment.filePath
            const hasAttachmentUrl = !!assignment.attachmentUrl
            const hasFile_path = !!assignment.file_path
            const hasFileName = !!assignment.fileName

            console.log('📎 检查附件:', {
                id: assignment.id,
                title: assignment.title,
                filePath: assignment.filePath,
                fileName: assignment.fileName,
                attachmentUrl: assignment.attachmentUrl,
                file_path: assignment.file_path,
                hasFilePath, hasAttachmentUrl, hasFile_path, hasFileName
            })

            // 只要有任一字段有值就认为有附件
            return hasFilePath || hasAttachmentUrl || hasFile_path || hasFileName
        },

        isImageFile(assignment) {
            const path = (assignment.filePath || assignment.attachmentUrl || assignment.file_path || '').toLowerCase()
            const name = (assignment.fileName || '').toLowerCase()
            return /\.(jpg|jpeg|png|gif|bmp|webp|svg)$/i.test(path) || /\.(jpg|jpeg|png|gif|bmp|webp|svg)$/i.test(name)
        },

        isPDF(assignment) {
            const path = (assignment.filePath || assignment.attachmentUrl || assignment.file_path || '').toLowerCase()
            const name = (assignment.fileName || '').toLowerCase()
            return /\.pdf$/i.test(path) || /\.pdf$/i.test(name)
        },

        isWord(assignment) {
            const path = (assignment.filePath || assignment.attachmentUrl || assignment.file_path || '').toLowerCase()
            const name = (assignment.fileName || '').toLowerCase()
            return /\.(doc|docx)$/i.test(path) || /\.(doc|docx)$/i.test(name)
        },

        getFileExtension(assignment) {
            const path = assignment.filePath || assignment.attachmentUrl || assignment.file_path || ''
            const ext = path.split('.').pop().toLowerCase()
            return ext || 'file'
        },

        getFileType(assignment) {
            if (this.isImageFile(assignment)) return '图片'
            if (this.isPDF(assignment)) return 'PDF 文档'
            if (this.isWord(assignment)) return 'Word 文档'
            return '文件'
        },

        getFileTypeDetail(assignment) {
            const ext = this.getFileExtension(assignment).toUpperCase()
            const typeMap = {
                pdf: 'PDF 文档',
                doc: 'Word 文档',
                docx: 'Word 文档',
                jpg: 'JPEG 图片',
                jpeg: 'JPEG 图片',
                png: 'PNG 图片',
                gif: 'GIF 图片',
                txt: '文本文件',
                zip: '压缩文件',
                rar: '压缩文件'
            }
            return typeMap[ext] || `${ext} 文件`
        },

        getFileSizeText(assignment) {
            if (assignment.fileSize) {
                const size = Number(assignment.fileSize)
                if (size < 1024) return size + ' B'
                if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
                return (size / (1024 * 1024)).toFixed(1) + ' MB'
            }
            return ''
        },

        getAttachmentName(assignment) {
            if (assignment.fileName) return assignment.fileName
            const path = assignment.filePath || assignment.attachmentUrl || assignment.file_path || ''
            return path.split('/').pop().split('\\').pop() || '未知文件'
        },

        getAttachmentUrl(assignment) {
            let url = assignment.filePath || assignment.attachmentUrl || assignment.file_path || ''

            console.log('🔗 获取附件URL:', url)

            if (!url) {
                console.warn('⚠️ 附件URL为空')
                return null
            }

            // 如果已经是完整URL（以http或/api开头），直接使用
            if (url.startsWith('http') || url.startsWith('/api/') || url.startsWith('/files/')) {
                console.log('✅ 使用现有URL:', url)
                return url
            }

            // 如果是相对路径（如 submissions/xxx.pdf），添加前缀
            if (url.includes('/') || url.includes('\\')) {
                url = '/api/files/' + url.replace(/\\/g, '/')
                console.log('📌 拼接URL:', url)
                return url
            }

            // 如果只是文件名，尝试在根目录查找
            url = '/api/files/' + encodeURIComponent(url)
            console.log('🔍 编码URL:', url)
            return url
        },

        previewAttachment(assignment) {
            const url = this.getAttachmentUrl(assignment)

            if (!url) {
                this.$message.warning('附件地址不存在')
                return
            }

            if (this.isImageFile(assignment)) {
                this.openFullPreview(assignment)
            } else if (this.isPDF(assignment)) {
                // PDF 在新窗口打开（浏览器原生支持）
                window.open(url, '_blank')
            } else {
                // 其他文件类型，尝试下载或提示
                this.downloadAttachment(assignment)
            }
        },

        downloadAttachment(assignment) {
            const url = this.getAttachmentUrl(assignment)

            if (!url) {
                this.$message.error('无法获取下载链接')
                return
            }

            this.$message.info('正在准备下载...')

            // 创建隐藏的下载链接
            const link = document.createElement('a')
            link.href = url
            link.download = this.getAttachmentName(assignment)
            link.target = '_blank'
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)

            setTimeout(() => {
                this.$message.success('下载已开始')
            }, 500)
        },

        openFullPreview(assignment) {
            const url = this.getAttachmentUrl(assignment)

            if (!url) {
                this.$message.warning('无法预览该附件')
                return
            }

            // 创建全屏预览模态框
            const modal = document.createElement('div')
            modal.className = 'image-preview-modal'
            modal.innerHTML = `
                <div class="preview-backdrop" onclick="this.parentElement.remove()">
                    <div class="preview-container">
                        <img src="${url}" alt="${this.getAttachmentName(assignment)}" />
                        <div class="preview-toolbar">
                            <button class="toolbar-btn zoom-in" title="放大">🔍+</button>
                            <button class="toolbar-btn zoom-out" title="缩小">🔍-</button>
                            <button class="toolbar-btn rotate" title="旋转">↻</button>
                            <button class="toolbar-btn download" title="下载">⬇</button>
                            <button class="toolbar-btn close" onclick="this.closest('.image-preview-modal').remove()">✕</button>
                        </div>
                        <div class="preview-caption">${this.getAttachmentName(assignment)}</div>
                    </div>
                </div>
            `

            document.body.appendChild(modal)

            // 添加样式（如果还没有）
            if (!document.getElementById('preview-styles')) {
                const style = document.createElement('style')
                style.id = 'preview-styles'
                style.textContent = `
                    .image-preview-modal {
                        position: fixed;
                        top: 0; left: 0; right: 0; bottom: 0;
                        z-index: 9999;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .preview-backdrop {
                        position: absolute;
                        top: 0; left: 0; right: 0; bottom: 0;
                        background: rgba(0, 0, 0, 0.85);
                        backdrop-filter: blur(8px);
                    }
                    .preview-container {
                        position: relative;
                        max-width: 90vw;
                        max-height: 90vh;
                        z-index: 1;
                        animation: fadeIn 0.3s ease;
                    }
                    .preview-container img {
                        max-width: 100%;
                        max-height: 85vh;
                        border-radius: 12px;
                        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
                        transition: transform 0.3s ease;
                    }
                    .preview-toolbar {
                        position: absolute;
                        top: -50px;
                        right: 0;
                        display: flex;
                        gap: 8px;
                    }
                    .toolbar-btn {
                        width: 40px;
                        height: 40px;
                        border-radius: 50%;
                        background: rgba(255, 255, 255, 0.9);
                        border: none;
                        cursor: pointer;
                        font-size: 18px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        transition: all 0.2s;
                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
                    }
                    .toolbar-btn:hover {
                        background: #fff;
                        transform: scale(1.1);
                    }
                    .toolbar-btn.close {
                        background: #F56C6C;
                        color: #fff;
                    }
                    .preview-caption {
                        position: absolute;
                        bottom: -35px;
                        left: 50%;
                        transform: translateX(-50%);
                        color: #fff;
                        font-size: 14px;
                        white-space: nowrap;
                        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
                    }
                    @keyframes fadeIn {
                        from { opacity: 0; transform: scale(0.9); }
                        to { opacity: 1; transform: scale(1); }
                    }
                `
                document.head.appendChild(style)
            }

            // 添加交互功能
            let scale = 1
            let rotation = 0
            const img = modal.querySelector('img')

            modal.querySelector('.zoom-in').onclick = () => {
                scale = Math.min(scale + 0.25, 3)
                img.style.transform = `scale(${scale}) rotate(${rotation}deg)`
            }

            modal.querySelector('.zoom-out').onclick = () => {
                scale = Math.max(scale - 0.25, 0.5)
                img.style.transform = `scale(${scale}) rotate(${rotation}deg)`
            }

            modal.querySelector('.rotate').onclick = () => {
                rotation += 90
                img.style.transform = `scale(${scale}) rotate(${rotation}deg)`
            }

            modal.querySelector('.download').onclick = () => {
                this.downloadAttachment(assignment)
            }

            // ESC 关闭
            const handleEsc = (e) => {
                if (e.key === 'Escape') {
                    modal.remove()
                    document.removeEventListener('keydown', handleEsc)
                }
            }
            document.addEventListener('keydown', handleEsc)
        },

        goSubmit(a) {
            this.selAid = a.id
            this.currentTab = 'submit'
            this.submitOk = false
        },

        onFileChange(e) {
            const f = e.target.files[0]
            if (f) this.readImg(f)
        },
        onDrop(e) {
            const f = e.dataTransfer.files[0]
            if (f && f.type.startsWith('image/')) this.readImg(f)
        },
        readImg(file) {
            this.selectedFile = file
            if (this.previewUrl) {
                URL.revokeObjectURL(this.previewUrl)
            }
            this.previewUrl = URL.createObjectURL(file)
        },
        clearImg() {
            this.selectedFile = null
            if (this.previewUrl) {
                URL.revokeObjectURL(this.previewUrl)
                this.previewUrl = ''
            }
            this.$refs.fileInput.value = ''
        },

        async doSubmit() {
            if (!this.selAid) {
                this.$message.warning('请选择作业')
                return
            }
            if (!this.selectedFile) {
                this.$message.warning('请选择要上传的作业图片')
                return
            }

            this.submitting = true
            try {
                const selectedAssignment = this.assignments.find(a => a.id === this.selAid)
                const assignmentId = selectedAssignment ? selectedAssignment.assignmentId : String(this.selAid)

                const formData = new FormData()
                formData.append('file', this.selectedFile)
                formData.append('studentId', this.userInfo.studentId)
                formData.append('assignmentId', assignmentId)

                const res = await fetch(`/api/assignments/${this.selAid}/submit`, {
                    method: 'POST',
                    body: formData
                })

                const data = await res.json()
                if (data.success) {
                    this.submitOk = true
                    this.$message.success('提交成功！')
                    this.loadAll()
                    this.selectedFile = null
                    this.previewUrl = ''
                } else {
                    this.$message.error(data.message || '提交失败')
                }
            } catch (e) {
                console.error(e)
                this.$message.error('网络错误')
            } finally {
                this.submitting = false
            }
        },

        renderMyChart() {
            const chartDom = document.getElementById('myChart')
            if (!chartDom) return

            // 每次都重新创建图表
            if (this.chart) {
                this.chart.dispose()
            }
            this.chart = echarts.init(chartDom)

            // 只取你自己的、已批改的作业
            const myGraded = this.gradedSubmissions;

            // 按提交时间排序
            myGraded.sort((a, b) => new Date(a.submittedAt || 0) - new Date(b.submittedAt || 0))

            // 构建数据
            const xData = myGraded.map(sub => this.getAssignTitle(sub.assignmentId))
            const scores = myGraded.map(sub => Number(sub.score))

            this.chart.setOption({
                tooltip: { trigger: 'axis' },
                grid: { left: '3%', right: '4%', bottom: '8%', top: '10%', containLabel: true },
                xAxis: { type: 'category', data: xData, axisLabel: { rotate: 25 } },
                yAxis: { type: 'value', name: '分数', min: 0, max: 100 },
                series: [{
                    name: '我的得分',
                    type: 'line',
                    smooth: true,
                    data: scores,
                    lineStyle: { width: 3, color: '#409EFF' },
                    itemStyle: { color: '#409EFF' },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0,0,0,1,[
                            {offset:0, color:'rgba(64,158,255,0.2)'},
                            {offset:1, color:'rgba(64,158,255,0.0)'}
                        ])
                    },
                    markPoint: {
                        data: [
                            {type:'max', name:'最高分'},
                            {type:'min', name:'最低分'}
                        ]
                    },
                    markLine: {
                        data: [{type:'average', name:'平均分'}],
                        lineStyle: { color:'#E6A23C', type:'dashed' }
                    }
                }]
            })

            window.addEventListener('resize', () => {
                this.chart && this.chart.resize()
            })
        },

        async checkRAGStatus() {
            try {
                const res = await fetch('/api/rag/status')
                const data = await res.json()
                this.ragAvailable = data.vectorStoreAvailable || false
            } catch (e) {
                this.ragAvailable = false
            }
        },

        handleChatKeydown(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault()
                this.sendMessage()
            }
        },

        autoResizeTextarea() {
            this.$nextTick(() => {
                const textarea = this.$refs.chatTextarea
                if (textarea) {
                    textarea.style.height = 'auto'
                    textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
                }
            })
        },

        quickAsk(question) {
            this.chatInput = question
            this.sendMessage()
        },

        clearChat() {
            this.chatMessages = []
        },

        async sendMessage() {
            if (!this.chatInput.trim() || this.chatLoading) return

            const message = this.chatInput.trim()
            this.lastUserMessage = message
            this.chatMessages.push({ role: 'user', content: message })
            this.chatInput = ''
            this.chatLoading = true

            this.autoResizeTextarea()
            this.scrollToBottom()

            try {
                let data
                if (this.chatMode === 'rag') {
                    const res = await fetch('/api/rag/chat', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            message: message,
                            courseId: this.userInfo.courseId || ''
                        })
                    })
                    data = await res.json()

                    if (data.success && data.response) {
                        const aiMsg = { role: 'ai', content: data.response }
                        this.chatMessages.push(aiMsg)
                    } else {
                        this.chatMessages.push({ role: 'ai', content: '抱歉，知识库问答服务暂时不可用。' })
                    }
                } else {
                    // 升级1：获取最近3次作业的详细数据
                    const recentGraded = this.gradedSubmissions.slice(-3).reverse()
                    const recentScores = recentGraded.map(sub => {
                        const title = this.getAssignTitle(sub.assignmentId)
                        return `${title}：${sub.score}分`
                    }).join('\n')

                    // 升级2：提取批改反馈中的薄弱点关键词
                    const feedbacks = recentGraded.map(sub => sub.imageAnalysis || '').join(' ')
                    const weakPoints = feedbacks.includes('函数') ? '函数相关知识点' :
                        feedbacks.includes('循环') ? '循环结构' : '暂无明显薄弱知识点'

                    // 升级3：计算分数段分布
                    const allScores = this.gradedSubmissions.map(sub => Number(sub.score))
                    const high = allScores.filter(s => s >= 80).length
                    const mid = allScores.filter(s => s >= 60 && s < 80).length
                    const low = allScores.filter(s => s < 60).length

                    // 升级4：构建结构化学情报告（传给AI）
                    const studentProfile = `【结构化学情数据】
1. 作业概况：共${this.myStats.total}次已批改作业，平均分${this.myStats.avgScore}分，最高分${this.myStats.maxScore}分
2. 最近3次作业：
${recentScores || '暂无最近作业数据'}
3. 分数段分布：80分以上${high}次，60-80分${mid}次，不及格${low}次
4. 潜在薄弱点：${weakPoints}
            `

                    const res = await fetch('/api/ai/chat', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            message: message,
                            systemPrompt: `你是学生专属的AI学习顾问，已获取该学生的完整学情数据，请严格基于以下数据，生成一份专业、结构化的学情分析报告：
${studentProfile}

---
### 回复格式要求（必须严格遵守）：
1.  **📊 学情数据复盘**：用1句话总结核心数据，例如“你已完成X次作业，平均分XX分，整体呈XX趋势”。
2.  **✅ 优势亮点**：列出2个学生做得好的方面，必须结合数据，例如“最近3次作业分数稳定，保持在75分”。
3.  **⚠️ 薄弱诊断**：精准点出1-2个具体问题，必须结合数据，例如“高分段作业较少，难题得分率偏低”。
4.  **📝 改进方案**：针对每个薄弱点，给出2条可落地的行动建议，例如“针对难题薄弱：每周拆解1道高分题，总结解题步骤”。
5.  **💪 鼓励收尾**：用一句鼓励的话结束，语气专业又暖心。
全程使用中文，用Markdown分点格式回复，避免通用套话。`
                        })
                    })

                    data = await res.json()

                    if (data.success && data.response) {
                        this.chatMessages.push({ role: 'ai', content: data.response })
                    } else {
                        this.chatMessages.push({ role: 'ai', content: '抱歉，AI服务暂时不可用。请稍后再试。' })
                    }
                }
            } catch (e) {
                console.error('AI调用错误:', e)
                this.chatMessages.push({ role: 'ai', content: '网络错误，无法连接到AI服务。' })
            } finally {
                this.chatLoading = false
                this.scrollToBottom()
            }
        },

        async regenerate(msg) {
            if (this.chatLoading) return
            const idx = this.chatMessages.indexOf(msg)
            if (idx > -1) {
                this.chatMessages.splice(idx, 1)
            }
            if (this.lastUserMessage) {
                this.chatInput = this.lastUserMessage
                this.sendMessage()
            }
        },

        copyMessage(content) {
            if (navigator.clipboard) {
                navigator.clipboard.writeText(content).then(() => {
                    this.$message.success('已复制到剪贴板')
                })
            } else {
                const textarea = document.createElement('textarea')
                textarea.value = content
                document.body.appendChild(textarea)
                textarea.select()
                document.execCommand('copy')
                document.body.removeChild(textarea)
                this.$message.success('已复制到剪贴板')
            }
        },

        renderMarkdown(text) {
            if (!text) return ''
            let html = text
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')

            html = html.replace(/```(\w*)\n([\s\S]*?)```/g, function(m, lang, code) {
                return '<pre class="code-block"><code>' + code.trim() + '</code></pre>'
            })
            html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
            html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
            html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
            html = html.replace(/^### (.+)$/gm, '<h4>$1</h4>')
            html = html.replace(/^## (.+)$/gm, '<h3>$1</h3>')
            html = html.replace(/^# (.+)$/gm, '<h2>$1</h2>')
            html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
            html = html.replace(/^(\d+)\. (.+)$/gm, '<li>$2</li>')
            html = html.replace(/\n/g, '<br>')

            return html
        },

        scrollToBottom() {
            this.$nextTick(() => {
                if (this.$refs.chatMessages) {
                    this.$refs.chatMessages.scrollTop = this.$refs.chatMessages.scrollHeight
                }
            })
        },

        saveProfile() {
            if (!this.profileForm.realName) {
                this.$message.warning('请输入真实姓名')
                return
            }

            this.userInfo.realName = this.profileForm.realName
            this.userInfo.email = this.profileForm.email
            this.userInfo.phone = this.profileForm.phone

            localStorage.setItem('user', JSON.stringify(this.userInfo))
            this.$message.success('个人信息保存成功')
        },

        changePassword() {
            if (!this.passwordForm.oldPassword) {
                this.$message.warning('请输入当前密码')
                return
            }
            if (!this.passwordForm.newPassword) {
                this.$message.warning('请输入新密码')
                return
            }
            if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
                this.$message.warning('两次输入的密码不一致')
                return
            }
            if (this.passwordForm.newPassword.length < 6) {
                this.$message.warning('新密码长度不能少于6位')
                return
            }

            this.$message.success('密码修改成功')

            this.passwordForm = {
                oldPassword: '',
                newPassword: '',
                confirmPassword: ''
            }
        }
    }
})
