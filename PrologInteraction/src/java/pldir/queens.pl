% File   : queens.pl
% Updated: 14 February 2008
% Purpose: N-Queen Puzzle (posed by Franz Nauch, 1850)

main(Listener) :-
	java_method(Listener, write('N-Queen Puzzle (posed by Franz Nauch, 1850) '), _), java_method(Listener, nl, _),
	java_method(Listener, write('N = '), _),
	java_method(Listener, flush(), _),
	java_method(Listener, read(), N),
	N >= 4,
	read_yn('All solutions (y/n)? ', All, Listener),
	read_yn('Output (y/n)? ', Output, Listener),
	statistics(runtime, _),
	queen_solve(N, all(All), output(Output), Listener),
	statistics(runtime, [_,T]),
	java_method(Listener, write('CPU time = '), _), java_method(Listener, write(T), _), java_method(Listener, write(' msec'), _), java_method(Listener, nl, _).

read_yn(Message, YN, Listener) :-
	java_method(Listener, write(Message), _),
	java_method(Listener, flush(), _),
	java_method(Listener, read(), X),
	(X == 'y' -> YN = yes; YN = no).

queen_solve(N, all(X), output(Y), Listener) :-
	queens(N, Q),
	(Y == yes -> java_method(Listener, write(Q), _), java_method(Listener, nl, _); true),
	X == no,
	!.
queen_solve(_,_,_).

queens(N,Qs) :-
	range(1,N,Ns),
	queens(Ns,[],Qs).

queens([],Qs,Qs).
queens(UnplacedQs,SafeQs,Qs) :-
	select(UnplacedQs,UnplacedQs1,Q),
	not_attack(SafeQs,Q),
	queens(UnplacedQs1,[Q|SafeQs],Qs).

not_attack(Xs,X) :-
	not_attack(Xs,X,1).

not_attack([],_,_) :- !.
not_attack([Y|Ys],X,N) :-
	X =\= Y+N, X =\= Y-N,
	N1 is N+1,
	not_attack(Ys,X,N1).

select([X|Xs],Xs,X).
select([Y|Ys],[Y|Zs],X) :- select(Ys,Zs,X).

range(N,N,[N]) :- !.
range(M,N,[M|Ns]) :-
	M < N,
	M1 is M+1,
	range(M1,N,Ns).
