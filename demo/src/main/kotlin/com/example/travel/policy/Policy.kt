package com.example.travel.policy

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/** A travel policy is just text - the agent has to interpret it. */
@Entity
class Policy(
    var title: String,
    @Column(length = 4000)
    var text: String,
    @Id @GeneratedValue
    var id: Long? = null,
)

interface PolicyRepository : JpaRepository<Policy, Long>

data class PolicyRequest(val title: String, val text: String)

@RestController
@RequestMapping("/api/policies")
class PolicyController(private val policies: PolicyRepository) {

    @GetMapping
    fun all(): List<Policy> = policies.findAll()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Policy = find(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody r: PolicyRequest): Policy = policies.save(Policy(r.title, r.text))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody r: PolicyRequest): Policy =
        policies.save(find(id).apply { title = r.title; text = r.text })

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = policies.delete(find(id))

    private fun find(id: Long) =
        policies.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Policy $id not found")
}
