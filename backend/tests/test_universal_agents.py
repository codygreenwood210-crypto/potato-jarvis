from __future__ import annotations

import pytest

from backend.universal_agents import (
    AGENTS,
    CANDIDATES,
    choose_sro,
    get_agent,
    normalize_identifier,
    roster_invariants,
    select_team,
)


def test_exactly_80_certified_agents():
    assert len(AGENTS) == 80
    assert len(set(AGENTS)) == 80
    assert roster_invariants()["certified_primary_agents"] == 80


def test_expected_department_counts():
    assert roster_invariants()["departments"] == {
        "Executive Core": 6,
        "Commercial & Growth": 18,
        "Decision Intelligence": 3,
        "Product, Engineering & AI": 16,
        "Trust & Verification": 8,
        "Creative & Media Studio": 14,
        "Game Studio": 7,
        "Master Story Room": 8,
    }


def test_candidates_are_not_routable_as_certified_agents():
    assert set(CANDIDATES) == {"bug_hunter", "proof"}
    with pytest.raises(KeyError):
        normalize_identifier("Bug Hunter")
    assert normalize_identifier("Bug Hunter", allow_candidates=True) == "bug_hunter"


def test_embedded_aliases_resolve_to_primary_agent():
    assert normalize_identifier("SRE") == "devops"
    assert normalize_identifier("EnemyAI") == "combat"
    assert normalize_identifier("Crypto") == "identity"
    assert normalize_identifier("UX") == "ui"


def test_automatic_routing_selects_relevant_specialists():
    roles = select_team(
        "Audit the FastAPI backend for authorization bugs, database races, regressions and security failures",
        max_roles=8,
    )
    assert "backend" in roles
    assert "qa_11" in roles
    assert any(role in roles for role in ("guard", "shield", "identity"))
    assert len(roles) <= 8
    assert "judge" not in roles


def test_explicit_roster_request_is_honored_and_deduplicated():
    roles = select_team("anything", ["Forge", "Kernel", "Forge", "QA-11"], max_roles=8)
    assert roles == ["forge", "kernel", "qa_11"]


def test_judge_cannot_be_implementation_sro():
    assert choose_sro(["judge", "backend", "qa_11"]) == "backend"
    assert choose_sro(["judge"]) == "atlas"


def test_every_agent_prompt_preserves_nova_control_boundary():
    for spec in AGENTS.values():
        prompt = spec.prompt.lower()
        assert "nova" in prompt
        assert "do not execute tools" in prompt
        assert "never invent" in prompt


def test_nova_is_not_counted_as_team_seat():
    assert roster_invariants()["nova_is_manager_not_team_seat"] is True
    assert "nova" not in AGENTS
