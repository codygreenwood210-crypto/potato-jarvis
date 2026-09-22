"""Nova Universal Team agent registry and router.

The Universal Team agents are bounded specialists controlled by Nova.
They do not independently acquire authority, execute tools, approve actions,
or mutate persistent state. Nova remains the manager/controller and the
application security gateway remains authoritative.

This module defines the 80 currently certified primary operators. Candidate
roles are represented separately and are not routable as certified agents.
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from typing import Iterable


@dataclass(frozen=True)
class AgentSpec:
    slug: str
    name: str
    department: str
    role: str
    specialties: tuple[str, ...] = ()
    keywords: tuple[str, ...] = ()
    status: str = "active"

    @property
    def prompt(self) -> str:
        specialty_text = ", ".join(self.specialties) if self.specialties else "none"
        return (
            f"You are {self.name}, a bounded specialist in Nova's Universal Team. "
            f"Department: {self.department}. Role: {self.role}. "
            f"Embedded specialties/aliases: {specialty_text}. "
            "Work only within your expertise and explicitly request another specialist when needed. "
            "Separate verified evidence, inference, assumptions, unknowns, and conflicts. "
            "Never invent tool access, execution, test results, files, consensus, or completion. "
            "Do not execute tools, approve actions, change permissions, or mutate persistent state yourself; "
            "send recommendations and evidence to Nova, which controls orchestration and any authorized action path. "
            "Preserve user intent, reversibility, provenance, privacy, least privilege, and the Protected Trust Core. "
            "Challenge weak assumptions and identify failure modes. "
            "When the task is complete, state what evidence supports completion and what remains uncertain."
        )


_RAW_SPECS = [
  {
    "slug": "atlas",
    "name": "Atlas",
    "department": "Executive Core",
    "role": "Executive Mission Director / front-door router",
    "specialties": [
      "Roster",
      "Compass"
    ],
    "keywords": [
      "mission",
      "route",
      "router",
      "owner",
      "sro",
      "priority",
      "coordination",
      "team"
    ],
    "status": "active"
  },
  {
    "slug": "judge",
    "name": "Judge",
    "department": "Executive Core",
    "role": "Independent Completion & Quality Arbiter",
    "specialties": [
      "Score"
    ],
    "keywords": [
      "judge",
      "pass",
      "fail",
      "certify",
      "completion",
      "quality",
      "evidence",
      "acceptance"
    ],
    "status": "active"
  },
  {
    "slug": "archivist",
    "name": "Archivist",
    "department": "Executive Core",
    "role": "Continuity, memory, provenance & handoff",
    "specialties": [],
    "keywords": [
      "archive",
      "continuity",
      "memory",
      "provenance",
      "handoff",
      "history",
      "record"
    ],
    "status": "active"
  },
  {
    "slug": "scout",
    "name": "Scout",
    "department": "Executive Core",
    "role": "Current research & external evidence",
    "specialties": [
      "Fact",
      "Intel",
      "Trend",
      "Library"
    ],
    "keywords": [
      "research",
      "current",
      "latest",
      "sources",
      "external",
      "fact",
      "trend",
      "evidence"
    ],
    "status": "active"
  },
  {
    "slug": "mentor",
    "name": "Mentor",
    "department": "Executive Core",
    "role": "Skills Trainer / operational retraining",
    "specialties": [
      "Gap"
    ],
    "keywords": [
      "train",
      "training",
      "skill",
      "retrain",
      "education",
      "curriculum",
      "gap"
    ],
    "status": "active"
  },
  {
    "slug": "redline",
    "name": "Redline",
    "department": "Executive Core",
    "role": "Red-team, premortem & assumption challenger",
    "specialties": [],
    "keywords": [
      "red team",
      "premortem",
      "assumption",
      "challenge",
      "risk",
      "failure",
      "stress test"
    ],
    "status": "active"
  },
  {
    "slug": "venture",
    "name": "Venture",
    "department": "Commercial & Growth",
    "role": "Venture strategy & opportunity selection",
    "specialties": [
      "Model",
      "Grant"
    ],
    "keywords": [
      "business idea",
      "venture",
      "opportunity",
      "strategy",
      "startup",
      "grant",
      "business model"
    ],
    "status": "active"
  },
  {
    "slug": "vault",
    "name": "Vault",
    "department": "Commercial & Growth",
    "role": "Finance/CFO controls & financial stewardship",
    "specialties": [
      "Runway",
      "Procure"
    ],
    "keywords": [
      "finance",
      "cfo",
      "budget",
      "cash",
      "runway",
      "procure",
      "cost",
      "financial"
    ],
    "status": "active"
  },
  {
    "slug": "margin",
    "name": "Margin",
    "department": "Commercial & Growth",
    "role": "Unit economics, contribution margin & profitability",
    "specialties": [
      "Price"
    ],
    "keywords": [
      "margin",
      "profit",
      "profitability",
      "unit economics",
      "pricing",
      "price",
      "cost"
    ],
    "status": "active"
  },
  {
    "slug": "yield",
    "name": "Yield",
    "department": "Commercial & Growth",
    "role": "Monetization and game/digital economy design",
    "specialties": [],
    "keywords": [
      "monetization",
      "economy",
      "revenue",
      "iap",
      "subscription",
      "offers"
    ],
    "status": "active"
  },
  {
    "slug": "deal",
    "name": "Deal",
    "department": "Commercial & Growth",
    "role": "Business development, partnerships & commercial negotiation",
    "specialties": [],
    "keywords": [
      "deal",
      "partnership",
      "business development",
      "negotiation",
      "contract terms"
    ],
    "status": "active"
  },
  {
    "slug": "launch",
    "name": "Launch",
    "department": "Commercial & Growth",
    "role": "Launch, release & go-to-market orchestration",
    "specialties": [],
    "keywords": [
      "launch",
      "release",
      "publish",
      "rollout",
      "go to market",
      "gtm"
    ],
    "status": "active"
  },
  {
    "slug": "metric",
    "name": "Metric",
    "department": "Commercial & Growth",
    "role": "Product KPI, instrumentation & performance analytics",
    "specialties": [],
    "keywords": [
      "kpi",
      "metric",
      "analytics",
      "instrumentation",
      "performance",
      "dashboard"
    ],
    "status": "active"
  },
  {
    "slug": "closer",
    "name": "Closer",
    "department": "Commercial & Growth",
    "role": "Sales strategy & conversion",
    "specialties": [
      "Prospect",
      "Demo",
      "Proposal"
    ],
    "keywords": [
      "sales",
      "prospect",
      "demo",
      "proposal",
      "conversion",
      "close"
    ],
    "status": "active"
  },
  {
    "slug": "success",
    "name": "Success",
    "department": "Commercial & Growth",
    "role": "Customer success, expansion & churn prevention",
    "specialties": [
      "Support"
    ],
    "keywords": [
      "customer success",
      "support",
      "churn",
      "retention",
      "expansion",
      "customer"
    ],
    "status": "active"
  },
  {
    "slug": "beacon",
    "name": "Beacon",
    "department": "Commercial & Growth",
    "role": "Marketing Director / strategy & routing",
    "specialties": [
      "Position"
    ],
    "keywords": [
      "marketing",
      "positioning",
      "campaign",
      "go to market",
      "brand strategy"
    ],
    "status": "active"
  },
  {
    "slug": "radar",
    "name": "Radar",
    "department": "Commercial & Growth",
    "role": "Market, customer, competitor, trend, demand & keyword research",
    "specialties": [],
    "keywords": [
      "market research",
      "competitor",
      "customer research",
      "demand",
      "keyword",
      "trend"
    ],
    "status": "active"
  },
  {
    "slug": "signal_marketing",
    "name": "Signal (Marketing)",
    "department": "Commercial & Growth",
    "role": "Content strategy, creative direction & brand consistency",
    "specialties": [
      "Hook"
    ],
    "keywords": [
      "content",
      "copy",
      "creative",
      "brand",
      "hook",
      "social"
    ],
    "status": "active"
  },
  {
    "slug": "reach",
    "name": "Reach",
    "department": "Commercial & Growth",
    "role": "SEO, AI-search visibility, organic discovery & community growth",
    "specialties": [
      "Shelf",
      "Signal (Core)"
    ],
    "keywords": [
      "seo",
      "organic",
      "search visibility",
      "community",
      "discovery",
      "ai search"
    ],
    "status": "active"
  },
  {
    "slug": "boost",
    "name": "Boost",
    "department": "Commercial & Growth",
    "role": "Paid acquisition, media buying, retargeting, CAC & ROAS",
    "specialties": [],
    "keywords": [
      "ads",
      "paid",
      "media buying",
      "retargeting",
      "cac",
      "roas",
      "acquisition"
    ],
    "status": "active"
  },
  {
    "slug": "bridge",
    "name": "Bridge",
    "department": "Commercial & Growth",
    "role": "Creators, influencers, affiliates, PR, outreach & partnerships",
    "specialties": [],
    "keywords": [
      "influencer",
      "creator",
      "affiliate",
      "pr",
      "outreach",
      "partnership"
    ],
    "status": "active"
  },
  {
    "slug": "loop_marketing",
    "name": "Loop (Marketing)",
    "department": "Commercial & Growth",
    "role": "Lifecycle, CRM, retention, referrals, loyalty & LTV",
    "specialties": [
      "Relay",
      "Pulse"
    ],
    "keywords": [
      "crm",
      "lifecycle",
      "retention",
      "referral",
      "loyalty",
      "ltv",
      "email"
    ],
    "status": "active"
  },
  {
    "slug": "lab",
    "name": "Lab",
    "department": "Commercial & Growth",
    "role": "Growth experiments, CRO, A/B testing & funnel diagnostics",
    "specialties": [
      "Ledger (Marketing)"
    ],
    "keywords": [
      "cro",
      "a/b",
      "experiment",
      "funnel",
      "growth test",
      "conversion rate"
    ],
    "status": "active"
  },
  {
    "slug": "trust",
    "name": "Trust",
    "department": "Commercial & Growth",
    "role": "Marketing claims, privacy/spam compliance, disclosures & brand safety",
    "specialties": [],
    "keywords": [
      "marketing claim",
      "spam",
      "disclosure",
      "brand safety",
      "compliance",
      "advertising"
    ],
    "status": "active"
  },
  {
    "slug": "stat",
    "name": "Stat",
    "department": "Decision Intelligence",
    "role": "Statistics, uncertainty & quantitative reasoning",
    "specialties": [],
    "keywords": [
      "statistics",
      "probability",
      "uncertainty",
      "confidence interval",
      "significance",
      "quantitative"
    ],
    "status": "active"
  },
  {
    "slug": "analyst",
    "name": "Analyst",
    "department": "Decision Intelligence",
    "role": "Data analysis, dashboards & business intelligence",
    "specialties": [],
    "keywords": [
      "data analysis",
      "dashboard",
      "business intelligence",
      "bi",
      "dataset",
      "analysis"
    ],
    "status": "active"
  },
  {
    "slug": "experiment",
    "name": "Experiment",
    "department": "Decision Intelligence",
    "role": "Experimental design, causal thinking & measurement plans",
    "specialties": [
      "Survey"
    ],
    "keywords": [
      "experiment",
      "causal",
      "randomized",
      "survey",
      "measurement",
      "ab test"
    ],
    "status": "active"
  },
  {
    "slug": "forge",
    "name": "Forge",
    "department": "Product, Engineering & AI",
    "role": "Product build & execution lead",
    "specialties": [],
    "keywords": [
      "build",
      "product",
      "implementation",
      "engineering",
      "ship",
      "execution"
    ],
    "status": "active"
  },
  {
    "slug": "kernel",
    "name": "Kernel",
    "department": "Product, Engineering & AI",
    "role": "Principal software architecture & systems integration",
    "specialties": [
      "Contract",
      "Migration"
    ],
    "keywords": [
      "architecture",
      "system design",
      "integration",
      "migration",
      "contract",
      "refactor"
    ],
    "status": "active"
  },
  {
    "slug": "backend",
    "name": "Backend",
    "department": "Product, Engineering & AI",
    "role": "Backend services, APIs & server-side engineering",
    "specialties": [],
    "keywords": [
      "backend",
      "api",
      "server",
      "endpoint",
      "service",
      "fastapi"
    ],
    "status": "active"
  },
  {
    "slug": "frontend",
    "name": "Frontend",
    "department": "Product, Engineering & AI",
    "role": "Frontend application engineering",
    "specialties": [
      "Web"
    ],
    "keywords": [
      "frontend",
      "web",
      "ui code",
      "browser",
      "react",
      "javascript",
      "typescript"
    ],
    "status": "active"
  },
  {
    "slug": "android",
    "name": "Android",
    "department": "Product, Engineering & AI",
    "role": "Android/Kotlin/Jetpack Compose engineering",
    "specialties": [],
    "keywords": [
      "android",
      "kotlin",
      "jetpack",
      "compose",
      "apk"
    ],
    "status": "active"
  },
  {
    "slug": "apple",
    "name": "Apple",
    "department": "Product, Engineering & AI",
    "role": "iOS/macOS/Swift engineering",
    "specialties": [],
    "keywords": [
      "ios",
      "macos",
      "swift",
      "apple",
      "xcode"
    ],
    "status": "active"
  },
  {
    "slug": "flutter",
    "name": "Flutter",
    "department": "Product, Engineering & AI",
    "role": "Flutter/Dart cross-platform engineering",
    "specialties": [],
    "keywords": [
      "flutter",
      "dart",
      "cross platform"
    ],
    "status": "active"
  },
  {
    "slug": "database",
    "name": "Database",
    "department": "Product, Engineering & AI",
    "role": "Database design, persistence, migrations & data integrity",
    "specialties": [],
    "keywords": [
      "database",
      "sql",
      "sqlite",
      "schema",
      "migration",
      "persistence",
      "data integrity"
    ],
    "status": "active"
  },
  {
    "slug": "devops",
    "name": "DevOps",
    "department": "Product, Engineering & AI",
    "role": "CI/CD, infrastructure automation & delivery pipelines",
    "specialties": [
      "Cloud",
      "SRE",
      "Build",
      "Linux",
      "Network"
    ],
    "keywords": [
      "ci",
      "cd",
      "devops",
      "github actions",
      "pipeline",
      "linux",
      "network",
      "cloud",
      "sre",
      "build"
    ],
    "status": "active"
  },
  {
    "slug": "automate",
    "name": "Automate",
    "department": "Product, Engineering & AI",
    "role": "Workflow automation, scripts & orchestration",
    "specialties": [
      "Integrate",
      "API",
      "IoT"
    ],
    "keywords": [
      "automation",
      "workflow",
      "script",
      "orchestration",
      "integration",
      "iot"
    ],
    "status": "active"
  },
  {
    "slug": "cortex",
    "name": "Cortex",
    "department": "Product, Engineering & AI",
    "role": "AI/LLM architecture & capability design",
    "specialties": [
      "Prompt",
      "ModelOps"
    ],
    "keywords": [
      "ai",
      "llm",
      "model",
      "prompt",
      "modelops",
      "inference"
    ],
    "status": "active"
  },
  {
    "slug": "agent",
    "name": "Agent",
    "department": "Product, Engineering & AI",
    "role": "Agentic workflows, planning, memory & tool use",
    "specialties": [],
    "keywords": [
      "agent",
      "agentic",
      "tool use",
      "planner",
      "memory",
      "autonomy"
    ],
    "status": "active"
  },
  {
    "slug": "rag",
    "name": "RAG",
    "department": "Product, Engineering & AI",
    "role": "Retrieval-augmented generation & knowledge systems",
    "specialties": [
      "Index"
    ],
    "keywords": [
      "rag",
      "retrieval",
      "embedding",
      "index",
      "knowledge base",
      "vector"
    ],
    "status": "active"
  },
  {
    "slug": "eval",
    "name": "Eval",
    "department": "Product, Engineering & AI",
    "role": "AI evaluations, benchmarks, red-team cases & quality gates",
    "specialties": [
      "Synthetic"
    ],
    "keywords": [
      "evaluation",
      "eval",
      "benchmark",
      "synthetic",
      "red team",
      "quality gate"
    ],
    "status": "active"
  },
  {
    "slug": "scientist",
    "name": "Scientist",
    "department": "Product, Engineering & AI",
    "role": "Machine learning, data science & predictive modelling",
    "specialties": [
      "DataPipe"
    ],
    "keywords": [
      "machine learning",
      "ml",
      "data science",
      "model training",
      "prediction",
      "pipeline"
    ],
    "status": "active"
  },
  {
    "slug": "toolsmith",
    "name": "Toolsmith",
    "department": "Product, Engineering & AI",
    "role": "Tool schemas, function calling & action interfaces",
    "specialties": [
      "Gateway"
    ],
    "keywords": [
      "tool schema",
      "function calling",
      "tool",
      "gateway",
      "action interface",
      "connector"
    ],
    "status": "active"
  },
  {
    "slug": "guard",
    "name": "Guard",
    "department": "Trust & Verification",
    "role": "Security lead, threat-aware architecture & security governance",
    "specialties": [],
    "keywords": [
      "security",
      "threat model",
      "security governance",
      "risk",
      "attack surface"
    ],
    "status": "active"
  },
  {
    "slug": "shield",
    "name": "Shield",
    "department": "Trust & Verification",
    "role": "Application security, secure coding & vulnerability review",
    "specialties": [
      "MobileSec",
      "CloudSec",
      "Threat"
    ],
    "keywords": [
      "vulnerability",
      "secure code",
      "appsec",
      "injection",
      "exploit",
      "threat",
      "security review"
    ],
    "status": "active"
  },
  {
    "slug": "identity",
    "name": "Identity",
    "department": "Trust & Verification",
    "role": "Authentication, authorization, sessions & access control",
    "specialties": [
      "Crypto"
    ],
    "keywords": [
      "authentication",
      "authorization",
      "auth",
      "session",
      "access control",
      "crypto",
      "permission"
    ],
    "status": "active"
  },
  {
    "slug": "privacy",
    "name": "Privacy",
    "department": "Trust & Verification",
    "role": "Privacy engineering, data minimization & consent design",
    "specialties": [],
    "keywords": [
      "privacy",
      "consent",
      "data minimization",
      "personal data",
      "pii"
    ],
    "status": "active"
  },
  {
    "slug": "counsel",
    "name": "Counsel",
    "department": "Trust & Verification",
    "role": "Legal research & issue spotting; escalates licensed-lawyer matters",
    "specialties": [
      "Pact",
      "Regula"
    ],
    "keywords": [
      "legal",
      "law",
      "contract",
      "regulation",
      "compliance",
      "terms"
    ],
    "status": "active"
  },
  {
    "slug": "rights",
    "name": "Rights",
    "department": "Trust & Verification",
    "role": "Copyright, trademark, licensing, provenance & IP controls",
    "specialties": [],
    "keywords": [
      "copyright",
      "trademark",
      "license",
      "licensing",
      "ip",
      "intellectual property",
      "provenance"
    ],
    "status": "active"
  },
  {
    "slug": "qa_11",
    "name": "QA-11",
    "department": "Trust & Verification",
    "role": "Independent product QA, acceptance testing & risk-based verification",
    "specialties": [
      "Verifier",
      "Regression",
      "Chaos",
      "Observe",
      "Incident",
      "Gate"
    ],
    "keywords": [
      "qa",
      "quality assurance",
      "test",
      "testing",
      "regression",
      "acceptance",
      "chaos",
      "incident",
      "verification"
    ],
    "status": "active"
  },
  {
    "slug": "device",
    "name": "Device",
    "department": "Trust & Verification",
    "role": "Physical-device, emulator & environment verification",
    "specialties": [],
    "keywords": [
      "device",
      "emulator",
      "physical device",
      "runtime",
      "environment",
      "hardware"
    ],
    "status": "active"
  },
  {
    "slug": "vision",
    "name": "Vision",
    "department": "Creative & Media Studio",
    "role": "Creative director / visual strategy",
    "specialties": [
      "Brand",
      "Layout"
    ],
    "keywords": [
      "creative direction",
      "visual",
      "brand",
      "layout",
      "art direction"
    ],
    "status": "active"
  },
  {
    "slug": "pixel",
    "name": "Pixel",
    "department": "Creative & Media Studio",
    "role": "Pixel-art specialist",
    "specialties": [],
    "keywords": [
      "pixel art",
      "pixel",
      "sprite",
      "tileset"
    ],
    "status": "active"
  },
  {
    "slug": "motion",
    "name": "Motion",
    "department": "Creative & Media Studio",
    "role": "2D animation & motion design",
    "specialties": [
      "Rig"
    ],
    "keywords": [
      "animation",
      "motion",
      "rig",
      "2d animation"
    ],
    "status": "active"
  },
  {
    "slug": "vector",
    "name": "Vector",
    "department": "Creative & Media Studio",
    "role": "Vector art, icons & scalable assets",
    "specialties": [
      "Icon"
    ],
    "keywords": [
      "vector",
      "svg",
      "icon",
      "scalable"
    ],
    "status": "active"
  },
  {
    "slug": "concept",
    "name": "Concept",
    "department": "Creative & Media Studio",
    "role": "Concept art & visual exploration",
    "specialties": [
      "Character",
      "Environment",
      "Illustrate",
      "GenArt"
    ],
    "keywords": [
      "concept art",
      "character art",
      "environment art",
      "illustration",
      "genart"
    ],
    "status": "active"
  },
  {
    "slug": "ui",
    "name": "UI",
    "department": "Creative & Media Studio",
    "role": "UI visual design",
    "specialties": [
      "UX",
      "System",
      "Type",
      "Palette",
      "AccessVisual"
    ],
    "keywords": [
      "ui",
      "ux",
      "interface design",
      "typography",
      "palette",
      "accessibility",
      "design system"
    ],
    "status": "active"
  },
  {
    "slug": "techart",
    "name": "TechArt",
    "department": "Creative & Media Studio",
    "role": "Technical art, shaders, export pipelines & engine integration",
    "specialties": [
      "Model3D",
      "VFX",
      "Texture",
      "Light"
    ],
    "keywords": [
      "technical art",
      "shader",
      "vfx",
      "texture",
      "lighting",
      "3d",
      "export"
    ],
    "status": "active"
  },
  {
    "slug": "storeart",
    "name": "StoreArt",
    "department": "Creative & Media Studio",
    "role": "Store screenshots, promo boards & listing visual conversion",
    "specialties": [
      "Graphic",
      "Package",
      "Thumbnail"
    ],
    "keywords": [
      "store art",
      "screenshot",
      "promo",
      "thumbnail",
      "package",
      "listing"
    ],
    "status": "active"
  },
  {
    "slug": "imageqa",
    "name": "ImageQA",
    "department": "Creative & Media Studio",
    "role": "Visual QA, consistency, artifact detection & spec compliance",
    "specialties": [
      "Asset"
    ],
    "keywords": [
      "image qa",
      "visual qa",
      "artifact",
      "asset consistency",
      "spec"
    ],
    "status": "active"
  },
  {
    "slug": "dataviz",
    "name": "DataViz",
    "department": "Creative & Media Studio",
    "role": "Charts, diagrams, infographics & information visualization",
    "specialties": [
      "Print"
    ],
    "keywords": [
      "chart",
      "diagram",
      "infographic",
      "data visualization",
      "dataviz"
    ],
    "status": "active"
  },
  {
    "slug": "composer",
    "name": "Composer",
    "department": "Creative & Media Studio",
    "role": "Music composition, themes & adaptive-music planning",
    "specialties": [
      "SFX",
      "Mix"
    ],
    "keywords": [
      "music",
      "compose",
      "sfx",
      "sound",
      "mix",
      "audio"
    ],
    "status": "active"
  },
  {
    "slug": "voice",
    "name": "Voice",
    "department": "Creative & Media Studio",
    "role": "Voice direction, TTS/VO preparation & performance briefs",
    "specialties": [],
    "keywords": [
      "voice",
      "tts",
      "voiceover",
      "vo",
      "pronunciation"
    ],
    "status": "active"
  },
  {
    "slug": "video",
    "name": "Video",
    "department": "Creative & Media Studio",
    "role": "Video production, shot planning & cinematography",
    "specialties": [
      "Edit",
      "Script"
    ],
    "keywords": [
      "video",
      "cinematography",
      "shot",
      "edit",
      "trailer",
      "film"
    ],
    "status": "active"
  },
  {
    "slug": "docs",
    "name": "Docs",
    "department": "Creative & Media Studio",
    "role": "Technical writing, manuals, onboarding & documentation",
    "specialties": [
      "Localize",
      "Caption"
    ],
    "keywords": [
      "documentation",
      "docs",
      "manual",
      "onboarding",
      "localize",
      "caption"
    ],
    "status": "active"
  },
  {
    "slug": "gamedirector",
    "name": "GameDirector",
    "department": "Game Studio",
    "role": "Game direction, pillars & production cohesion",
    "specialties": [],
    "keywords": [
      "game direction",
      "game pillars",
      "production",
      "game vision"
    ],
    "status": "active"
  },
  {
    "slug": "gamedesign",
    "name": "GameDesign",
    "department": "Game Studio",
    "role": "Core mechanics, rules, feel & player experience",
    "specialties": [],
    "keywords": [
      "game design",
      "mechanic",
      "game feel",
      "player experience",
      "core loop"
    ],
    "status": "active"
  },
  {
    "slug": "systems",
    "name": "Systems",
    "department": "Game Studio",
    "role": "Progression, inventory, abilities & systemic design",
    "specialties": [
      "Quest"
    ],
    "keywords": [
      "progression",
      "inventory",
      "ability",
      "quest",
      "game system"
    ],
    "status": "active"
  },
  {
    "slug": "level",
    "name": "Level",
    "department": "Game Studio",
    "role": "Level design, encounter flow & spatial pacing",
    "specialties": [
      "Loop (Core)"
    ],
    "keywords": [
      "level design",
      "encounter",
      "spatial",
      "pacing",
      "map"
    ],
    "status": "active"
  },
  {
    "slug": "combat",
    "name": "Combat",
    "department": "Game Studio",
    "role": "Combat mechanics, feel, balance & feedback",
    "specialties": [
      "EnemyAI",
      "Boss"
    ],
    "keywords": [
      "combat",
      "enemy ai",
      "boss",
      "damage",
      "balance",
      "fight"
    ],
    "status": "active"
  },
  {
    "slug": "controls",
    "name": "Controls",
    "department": "Game Studio",
    "role": "Input, touch/controller mapping & control usability",
    "specialties": [],
    "keywords": [
      "controls",
      "input",
      "touch",
      "controller",
      "keyboard",
      "remap"
    ],
    "status": "active"
  },
  {
    "slug": "godot",
    "name": "Godot",
    "department": "Game Studio",
    "role": "Godot 4.x/GDScript engineering & optimization",
    "specialties": [
      "Unity",
      "Unreal"
    ],
    "keywords": [
      "godot",
      "gdscript",
      "unity",
      "unreal",
      "game engine"
    ],
    "status": "active"
  },
  {
    "slug": "quill",
    "name": "Quill",
    "department": "Master Story Room",
    "role": "Story Director",
    "specialties": [
      "Seed"
    ],
    "keywords": [
      "story",
      "narrative",
      "story direction",
      "seed",
      "writing"
    ],
    "status": "active"
  },
  {
    "slug": "realm",
    "name": "Realm",
    "department": "Master Story Room",
    "role": "World Architect",
    "specialties": [],
    "keywords": [
      "worldbuilding",
      "world",
      "lore",
      "setting",
      "geography",
      "culture"
    ],
    "status": "active"
  },
  {
    "slug": "persona",
    "name": "Persona",
    "department": "Master Story Room",
    "role": "Character Architect",
    "specialties": [
      "Bond",
      "Nemesis"
    ],
    "keywords": [
      "character",
      "character arc",
      "relationship",
      "bond",
      "nemesis"
    ],
    "status": "active"
  },
  {
    "slug": "plot",
    "name": "Plot",
    "department": "Master Story Room",
    "role": "Plot & Structure Architect",
    "specialties": [
      "Thread",
      "Echo",
      "Twist"
    ],
    "keywords": [
      "plot",
      "structure",
      "twist",
      "thread",
      "setup payoff"
    ],
    "status": "active"
  },
  {
    "slug": "theme",
    "name": "Theme",
    "department": "Master Story Room",
    "role": "Theme & Meaning specialist",
    "specialties": [
      "Heart"
    ],
    "keywords": [
      "theme",
      "meaning",
      "message",
      "heart",
      "motif"
    ],
    "status": "active"
  },
  {
    "slug": "scene",
    "name": "Scene",
    "department": "Master Story Room",
    "role": "Scene Architect",
    "specialties": [
      "Banter",
      "Atmosphere",
      "Tempo"
    ],
    "keywords": [
      "scene",
      "dialogue",
      "banter",
      "atmosphere",
      "tempo",
      "pacing"
    ],
    "status": "active"
  },
  {
    "slug": "critic",
    "name": "Critic",
    "department": "Master Story Room",
    "role": "Story critic / structural stress tester",
    "specialties": [
      "Spark",
      "Audience"
    ],
    "keywords": [
      "story critique",
      "critic",
      "audience",
      "structure",
      "predictability"
    ],
    "status": "active"
  },
  {
    "slug": "canon",
    "name": "Canon",
    "department": "Master Story Room",
    "role": "Canon Keeper / Story Bible Archivist",
    "specialties": [
      "Continuity",
      "Consequence"
    ],
    "keywords": [
      "canon",
      "continuity",
      "story bible",
      "timeline",
      "consequence",
      "retcon"
    ],
    "status": "active"
  }
]
AGENTS: dict[str, AgentSpec] = {
    item["slug"]: AgentSpec(
        slug=item["slug"],
        name=item["name"],
        department=item["department"],
        role=item["role"],
        specialties=tuple(item.get("specialties", ())),
        keywords=tuple(item.get("keywords", ())),
        status=item.get("status", "active"),
    )
    for item in _RAW_SPECS
}

CANDIDATES: dict[str, AgentSpec] = {
    "bug_hunter": AgentSpec(
        slug="bug_hunter",
        name="Bug Hunter",
        department="Trust & Verification",
        role="Adversarial Software Defect Investigator & Root-Cause Specialist",
        specialties=("Undertaker", "Root Cause", "Regression"),
        keywords=("bug hunt", "adversarial test", "root cause", "fuzz", "mutation test"),
        status="candidate",
    ),
    "proof": AgentSpec(
        slug="proof",
        name="Proof",
        department="Executive Core",
        role="Final Response Integrity Reviewer",
        specialties=("Response Integrity",),
        keywords=("proofread", "final response", "copy paste", "response integrity"),
        status="candidate",
    ),
}

DEPARTMENT_HEADS = {
    "Executive Core": "atlas",
    "Commercial & Growth": "venture",
    "Decision Intelligence": "scout",
    "Product, Engineering & AI": "forge",
    "Trust & Verification": "guard",
    "Creative & Media Studio": "vision",
    "Game Studio": "gamedirector",
    "Master Story Room": "quill",
}

# Judge must remain independent from implementation ownership.
NON_IMPLEMENTATION_SRO = {"judge"}

# Names and embedded aliases may be used in API requests.
_ALIAS_TO_SLUG: dict[str, str] = {}
for slug, spec in AGENTS.items():
    for alias in (slug, spec.name, *spec.specialties):
        key = re.sub(r"[^a-z0-9]+", "_", alias.lower()).strip("_")
        _ALIAS_TO_SLUG[key] = slug

for slug, spec in CANDIDATES.items():
    for alias in (slug, spec.name, *spec.specialties):
        key = re.sub(r"[^a-z0-9]+", "_", alias.lower()).strip("_")
        _ALIAS_TO_SLUG[key] = slug


def normalize_identifier(value: str, *, allow_candidates: bool = False) -> str:
    key = re.sub(r"[^a-z0-9]+", "_", str(value).lower()).strip("_")
    slug = _ALIAS_TO_SLUG.get(key, key)
    if slug in AGENTS:
        return slug
    if allow_candidates and slug in CANDIDATES:
        return slug
    raise KeyError(f"Unknown or uncertified Universal Team role: {value}")


def get_agent(value: str, *, allow_candidates: bool = False) -> AgentSpec:
    slug = normalize_identifier(value, allow_candidates=allow_candidates)
    if slug in AGENTS:
        return AGENTS[slug]
    return CANDIDATES[slug]


def active_agents() -> tuple[AgentSpec, ...]:
    return tuple(AGENTS.values())


def roster_rows() -> list[dict[str, object]]:
    return [
        {
            "slug": spec.slug,
            "name": spec.name,
            "department": spec.department,
            "role": spec.role,
            "specialties": list(spec.specialties),
            "status": spec.status,
        }
        for spec in AGENTS.values()
    ]


def candidate_rows() -> list[dict[str, object]]:
    return [
        {
            "slug": spec.slug,
            "name": spec.name,
            "department": spec.department,
            "role": spec.role,
            "specialties": list(spec.specialties),
            "status": spec.status,
        }
        for spec in CANDIDATES.values()
    ]


def seed_rows() -> list[tuple[str, str, str]]:
    """Rows for the existing SQLite agents table: role, name, description."""
    return [(spec.slug, spec.name, spec.role) for spec in AGENTS.values()]


def _score(spec: AgentSpec, request: str) -> int:
    clean = " ".join(request.lower().split())
    tokens = set(re.findall(r"[a-z0-9]+", clean))
    score = 0

    # Explicit name/slug references dominate.
    if spec.name.lower() in clean or spec.slug.replace("_", " ") in clean:
        score += 30

    terms = list(spec.keywords) + [spec.role] + list(spec.specialties)
    for term in terms:
        t = term.lower().strip()
        if not t:
            continue
        if " " in t:
            if t in clean:
                score += 5
        elif t in tokens:
            score += 3

    # Department-level hints.
    dep = spec.department.lower()
    if "story" in dep and any(x in tokens for x in {"story", "character", "plot", "narrative"}):
        score += 2
    if "engineering" in dep and any(x in tokens for x in {"code", "software", "build", "api", "app"}):
        score += 2
    if "verification" in dep and any(x in tokens for x in {"test", "security", "verify", "audit"}):
        score += 2
    if "creative" in dep and any(x in tokens for x in {"art", "image", "design", "video", "audio"}):
        score += 2
    if "commercial" in dep and any(x in tokens for x in {"money", "business", "marketing", "sales", "revenue"}):
        score += 2
    if "game" in dep and any(x in tokens for x in {"game", "level", "combat", "godot"}):
        score += 2
    return score


def select_team(
    request: str,
    requested: Iterable[str] | None = None,
    *,
    max_roles: int = 8,
) -> list[str]:
    """Select a small mission pod of certified agents.

    Explicit requested roles are honored when certified. Automatic routing
    scores all 80 agents and returns the strongest relevant pod. Judge is
    never automatically selected as an implementation specialist.
    """
    limit = max(1, min(int(max_roles), 12))
    if requested:
        selected: list[str] = []
        for value in requested:
            slug = normalize_identifier(value)
            if slug not in selected:
                selected.append(slug)
        return selected[:limit]

    scored = [(_score(spec, request), slug) for slug, spec in AGENTS.items()]
    scored = [(score, slug) for score, slug in scored if score > 0 and slug not in NON_IMPLEMENTATION_SRO]
    scored.sort(key=lambda x: (-x[0], x[1]))

    chosen = [slug for _, slug in scored[:limit]]
    if not chosen:
        chosen = ["atlas"]

    lower = request.lower()
    # High-value conditional reviewers. They remain advisory unless Nova
    # explicitly sends them a task.
    conditional = []
    if any(x in lower for x in ("test", "verify", "audit", "bug", "regression")):
        conditional.append("qa_11")
    if any(x in lower for x in ("security", "permission", "auth", "secret", "privacy", "vulnerability")):
        conditional.append("guard")
    if any(x in lower for x in ("latest", "current", "today", "research", "source")):
        conditional.append("scout")
    if any(x in lower for x in ("memory", "archive", "handoff", "continuity")):
        conditional.append("archivist")

    for slug in conditional:
        if slug not in chosen and len(chosen) < limit:
            chosen.append(slug)
    return chosen[:limit]


def choose_sro(roles: Iterable[str]) -> str:
    """Choose the accountable implementation owner from a routed pod."""
    normalized = [normalize_identifier(role) for role in roles]
    for slug in normalized:
        if slug not in NON_IMPLEMENTATION_SRO:
            return slug
    return "atlas"


def role_prompt(role: str) -> str:
    return get_agent(role).prompt


def roster_invariants() -> dict[str, object]:
    departments: dict[str, int] = {}
    for spec in AGENTS.values():
        departments[spec.department] = departments.get(spec.department, 0) + 1
    return {
        "certified_primary_agents": len(AGENTS),
        "candidate_agents": len(CANDIDATES),
        "departments": departments,
        "unique_slugs": len(set(AGENTS)),
        "nova_is_manager_not_team_seat": True,
    }
