package tasks

import contributors.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val allUsers = mutableListOf<User>()

    for ((index, repo) in repos.withIndex()) {
        val users = service
            .getRepoContributorsCall(req.org, repo.name)
            .execute() // Executes request and blocks the current thread
            .also { logUsers(repo, it) }
            .bodyList()
        allUsers += users
        allUsers.aggregate()
        val isCompleted = index == repos.lastIndex
        updateResults(allUsers, isCompleted)
    }
}
