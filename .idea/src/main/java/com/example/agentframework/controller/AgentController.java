package com.example.agentframework.controller;

import com.example.agentframework.entity.Agent;
import com.example.agentframework.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin
public class AgentController {

    @Autowired
    private AgentService agentService;

    @PostMapping
    public ResponseEntity<Agent> createAgent(@RequestBody Agent agent) {
        Agent createdAgent = agentService.createAgent(agent);
        return new ResponseEntity<>(createdAgent, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agent> getAgentById(@PathVariable Long id) {
        return agentService.getAgentById(id)
                .map(agent -> new ResponseEntity<>(agent, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Agent>> getAgentsByCourseId(@PathVariable String courseId) {
        List<Agent> agents = agentService.getAgentsByCourseId(courseId);
        return new ResponseEntity<>(agents, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Agent>> getAllAgents() {
        List<Agent> agents = agentService.getAllAgents();
        return new ResponseEntity<>(agents, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agent> updateAgent(@PathVariable Long id, @RequestBody Agent agent) {
        Agent updatedAgent = agentService.updateAgent(id, agent);
        if (updatedAgent != null) {
            return new ResponseEntity<>(updatedAgent, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/chat")
    public ResponseEntity<Map<String, Object>> chatWithAgent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> chatRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String message = (String) chatRequest.get("message");
            String aiResponse = agentService.chatWithAgent(id, message);
            
            response.put("success", true);
            response.put("response", aiResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("response", "抱歉，我暂时无法回答这个问题。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
