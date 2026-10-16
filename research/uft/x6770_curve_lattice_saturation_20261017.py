#!/usr/bin/env python3
"""Exact quotient curve-lattice audit for Oxford X6770 Model 1.
Source: Constantin et al. arXiv:2507.03076v1. No physical-vacuum claim.
"""
from itertools import combinations, permutations
from functools import reduce
from math import gcd
P=(1,1,1,2,0); R=(1,1,1,0,2)
K=((1,-2,0,0,0),(0,1,1,-2,0),(0,1,0,0,-1),(0,0,-1,1,1),(-1,0,0,1,0))
assert all(sum(v[i] for v in K)==0 for i in range(5))
def mul(A,B):
    out={}
    for ia,ca in A.items():
        for ib,cb in B.items():
            if ia&ib: continue
            m=ia|ib
            out[m]=out.get(m,0)+ca*cb
    return {m:v for m,v in out.items() if v}
def linear(v):return {1<<i:x for i,x in enumerate(v) if x}
def pair(poly):
    PR=mul(linear(P),linear(R)); top=31
    return tuple(mul(mul(poly,linear([int(j==i) for j in range(5)])),PR).get(top,0) for i in range(5))
c2TA={((1<<i)|(1<<j)):4 for i,j in combinations(range(5),2)}
PR=mul(linear(P),linear(R))
c2T={m:c2TA[m]-PR.get(m,0) for m in c2TA}
c2V={m:0 for m in c2TA}
for a,b in combinations(range(5),2):
    for m,x in mul(linear(K[a]),linear(K[b])).items():c2V[m]+=x
res={m:c2T[m]-c2V[m] for m in c2TA}
cover=pair(res)
assert cover==(16,16,10,18,10)
quot=tuple(x//2 for x in cover)
def d(i,j,k):
    if len({i,j,k})<3:return 0
    return 4 if {i,j,k}=={0,1,2} else 2
pairs=list(combinations(range(5),2))
B=[[d(i,j,k)//2 for i,j in pairs] for k in range(5)]
def det(A):
    n=len(A); M=[r[:] for r in A]; sign=1;prev=1
    for k in range(n-1):
        if M[k][k]==0:
            swap=next((j for j in range(k+1,n) if M[j][k]),None)
            if swap is None:return 0
            M[k],M[swap]=M[swap],M[k];sign=-sign
        pivot=M[k][k]
        for i in range(k+1,n):
            for j in range(k+1,n):
                numer=M[i][j]*pivot-M[i][k]*M[k][j]
                assert numer%prev==0
                M[i][j]=numer//prev
        for i in range(k+1,n):M[i][k]=0
        prev=pivot
    return sign*M[-1][-1]
def det_leibniz(A):
    n=len(A);total=0
    for p in permutations(range(n)):
        term=(-1)**sum(p[i]>p[j] for i in range(n) for j in range(i+1,n))
        for i in range(n):term*=A[i][p[i]]
        total+=term
    return total
cols=(0,2,6,7,9)
cert=[[B[i][c] for c in cols] for i in range(5)]
assert det(cert)==det_leibniz(cert)==-1
minors=[det([[B[i][c] for c in cs] for i in range(5)]) for cs in combinations(range(10),5)]
assert reduce(gcd,map(abs,minors))==1
assert reduce(gcd,(abs(32*x) for x in minors))==32
witness={(0,2):3,(0,4):1,(1,2):2,(1,4):3,(3,4):1}
w=[witness.get(p,0) for p in pairs]
assert tuple(sum(B[i][c]*w[c] for c in range(10)) for i in range(5))==quot
print('Cover residual:',cover)
print('Quotient residual:',quot)
print('Unimodular minor columns:',[pairs[c] for c in cols])
print('Unimodular minor determinant:',det(cert))
print('Maximal-minor gcd:',reduce(gcd,map(abs,minors)))
print('Effective witness:',w)
print('ALL EXACT CHECKS PASSED')
