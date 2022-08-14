package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.DialogWrapper;
import com.shuzijun.leetcode.platform.model.Config;
import com.shuzijun.leetcode.platform.model.Question;
import com.shuzijun.leetcode.platform.model.Submission;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;
import com.shuzijun.leetcode.plugin.window.dialog.SubmissionsPanel;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author shuzijun
 */
public class SubmissionsAction extends AbstractTreeAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {

        List<Submission> submissionList = RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getSubmissionService().getSubmissionService(question.getTitleSlug());
        if (submissionList == null || submissionList.isEmpty()) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            SubmissionsPanel.TableModel tableModel = new SubmissionsPanel.TableModel(submissionList);
            SubmissionsPanel dialog = new SubmissionsPanel(anActionEvent.getProject(), tableModel);
            dialog.setTitle(question.getFormTitle() + " Submissions");
            dialog.addTableMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                        int row = dialog.getSelectedRow();
                        openSubmission(anActionEvent, config, question, submissionList, row);
                        dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
                    }
                }
            });
            dialog.addTableKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                        int row = dialog.getSelectedRow();
                        openSubmission(anActionEvent, config, question, submissionList, row);
                        dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
                    }

                }
            });
            dialog.show();
        });
    }

    private void openSubmission(AnActionEvent anActionEvent, Config config, Question question, List<Submission> submissionList, int row) {
        Submission submission = submissionList.get(row);

        ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(), anActionEvent.getActionManager().getId(this), false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getSubmissionService().openSubmission(submission, question.getTitleSlug(), true);
            }
        });
    }

}
