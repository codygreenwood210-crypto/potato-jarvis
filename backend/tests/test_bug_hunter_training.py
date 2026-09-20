from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "docs" / "training" / "v1" / "bug_hunter.jsonl"


def _records():
    lines = [line for line in DATA.read_text(encoding="utf-8").splitlines() if line.strip()]
    return [json.loads(line) for line in lines]


def test_bug_hunter_training_has_64_unique_drills():
    records = _records()
    assert len(records) == 64
    ids = [r["id"] for r in records]
    assert len(ids) == len(set(ids))
    assert ids[0] == "BUGHUNT-001"
    assert ids[-1] == "BUGHUNT-064"


def test_all_eight_drill_families_have_eight_cases():
    records = _records()
    expected = {
        "fundamentals",
        "ambiguity",
        "tool_evidence",
        "edge_failure",
        "adversarial",
        "cross_functional",
        "recovery_learning",
        "elite_transfer",
    }
    counts = {name: 0 for name in expected}
    for r in records:
        counts[r["skill_target"]] += 1
    assert counts == {name: 8 for name in expected}


def test_training_schema_and_safety_invariants():
    required = {
        "id",
        "dataset_version",
        "type",
        "group",
        "primary_role",
        "role_scope",
        "skill_target",
        "difficulty",
        "area",
        "scenario",
        "instruction",
        "learning_objectives",
        "supporting_roles",
        "expected_behaviors",
        "failure_traps",
        "required_evidence",
        "scoring",
        "pass_condition",
        "related_core_benchmarks",
    }
    for r in _records():
        assert required <= set(r)
        assert r["primary_role"] == "Bug Hunter"
        assert r["group"] == "Trust & Verification"
        assert r["type"] == "role_drill"
        assert 1 <= int(r["difficulty"]) <= 5
        assert sum(r["scoring"].values()) == 100
        assert any("fabricated" in x for x in [r["pass_condition"].lower()])
        assert "CORE-02" in r["related_core_benchmarks"]
        assert "CORE-06" in r["related_core_benchmarks"]
        assert "CORE-20" in r["related_core_benchmarks"]


def test_every_training_case_requires_residual_uncertainty_and_root_cause():
    for r in _records():
        behaviors = " ".join(r["expected_behaviors"]).lower()
        objectives = " ".join(r["learning_objectives"]).lower()
        assert "root cause" in behaviors or "root-cause" in objectives
        assert "unverified" in behaviors
