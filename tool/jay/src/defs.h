/*
 * Copyright (c) 1989 The Regents of the University of California.
 * All rights reserved.
 *
 * This code is derived from software contributed to Berkeley by
 * Robert Paul Corbett.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by the University of
 *	California, Berkeley and its contributors.
 * 4. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *	@(#)defs.h	5.6 (Berkeley) 5/24/93
 */

#include <assert.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>


/*  machine-dependent definitions			*/
/*  the following definitions are for the Tahoe		*/
/*  they might have to be changed for other machines	*/

/*  MAXCHAR is the largest unsigned character value	*/
/*  MAXSHORT is the largest value of a C short		*/
/*  MINSHORT is the most negative value of a C short	*/
/*  MAXTABLE is the maximum table size			*/
/*  BITS_PER_WORD is the number of bits in a C unsigned	*/
/*  WORDSIZE computes the number of words needed to	*/
/*	store n bits					*/
/*  BIT returns the value of the n-th bit starting	*/
/*	from r (0-indexed)				*/
/*  SETBIT sets the n-th bit starting from r		*/

#define	MAXCHAR		255
#define	MAXSHORT	32767
#define MINSHORT	-32768
#define MAXTABLE	32500
#define BITS_PER_WORD	32
#define	WORDSIZE(n)	(((n)+(BITS_PER_WORD-1))/BITS_PER_WORD)
#define	BIT(r, n)	((((r)[(n)>>5])>>((n)&31))&1)
#define	SETBIT(r, n)	((r)[(n)>>5]|=((unsigned)1<<((n)&31)))


/*  character names  */

#define	NUL		'\0'    /*  the null character  */
#define	NEWLINE		'\n'    /*  line feed  */
#define	SP		' '     /*  space  */
#define	BS		'\b'    /*  backspace  */
#define	HT		'\t'    /*  horizontal tab  */
#define	VT		'\013'  /*  vertical tab  */
#define	CR		'\r'    /*  carriage return  */
#define	FF		'\f'    /*  form feed  */
#define	QUOTE		'\''    /*  single quote  */
#define	DOUBLE_QUOTE	'\"'    /*  double quote  */
#define	BACKSLASH	'\\'    /*  backslash  */


/* defines for constructing filenames */

#define CODE_SUFFIX	".code.c"
#define	DEFINES_SUFFIX	".tab.h"
#define	OUTPUT_SUFFIX	".tab.c"
#define	VERBOSE_SUFFIX	".output"


/* keyword codes */

#define TOKEN 0
#define LEFT 1
#define RIGHT 2
#define NONASSOC 3
#define MARK 4
#define TEXT 5
#define TYPE 6
#define START 7


/*  symbol classes  */

#define UNKNOWN 0
#define TERM 1
#define NONTERM 2


/*  the undefined value  */

#define UNDEFINED (-1)


/*  action codes  */

#define SHIFT 1
#define REDUCE 2


/*  character macros  */

#define IS_IDENT(c)	(isalnum(c) || (c) == '_' || (c) == '.' || (c) == '$')
#define	IS_OCTAL(c)	((c) >= '0' && (c) <= '7')
#define	NUMERIC_VALUE(c)	((c) - '0')


/*  symbol macros  */

#define ISTOKEN(s)	((s) < start_symbol)
#define ISVAR(s)	((s) >= start_symbol)


/*  storage allocation macros  */

#define CALLOC(k,n)	(calloc((unsigned)(k),(unsigned)(n)))
#define	FREE(x)		(free((char*)(x)))
#define MALLOC(n)	(malloc((unsigned)(n)))
#define	NEW(t)		((t*)allocate(sizeof(t)))
#define	NEW2(n,t)	((t*)allocate((unsigned)((n)*sizeof(t))))
#define REALLOC(p,n)	(realloc((char*)(p),(unsigned)(n)))


/*  the structure of a symbol table entry  */

typedef struct bucket bucket;
struct bucket
{
    struct bucket *link;
    struct bucket *next;
    char *name;
    char *tag;
    short value;
    short index;
    short prec;
    char class;
    char assoc;
};


/*  the structure of the LR(0) state machine  */

typedef struct core core;
struct core
{
    struct core *next;
    struct core *link;
    short number;
    short accessing_symbol;
    short nitems;
    short items[1];
};


/*  the structure used to record shifts  */

typedef struct shifts shifts;
struct shifts
{
    struct shifts *next;
    short number;
    short nshifts;
    short shift[1];
};


/*  the structure used to store reductions  */

typedef struct reductions reductions;
struct reductions
{
    struct reductions *next;
    short number;
    short nreds;
    short rules[1];
};


/*  the structure used to represent parser actions  */

typedef struct action action;
struct action
{
    struct action *next;
    short symbol;
    short number;
    short prec;
    char action_code;
    char assoc;
    char suppressed;
};


/* global variables */

extern char tflag;
extern char vflag;

extern char *myname;
extern char *cptr;
extern char *line;
extern int lineno;
extern int outline;

extern char *action_file_name;
extern char *input_file_name;
extern char *prolog_file_name;
extern char *local_file_name;
extern char *verbose_file_name;

extern FILE *action_file;
extern FILE *input_file;
extern FILE *prolog_file;
extern FILE *local_file;
extern FILE *verbose_file;

extern int nitems;
extern int nrules;
extern int nsyms;
extern int ntokens;
extern int nvars;
extern int ntags;

extern char *line_format;

extern int   start_symbol;
extern char  **symbol_name;
extern short *symbol_value;
extern short *symbol_prec;
extern char  *symbol_assoc;

extern short *ritem;
extern short *rlhs;
extern short *rrhs;
extern short *rprec;
extern char  *rassoc;

extern short **derives;
extern char *nullable;

extern bucket *first_symbol;
extern bucket *last_symbol;

extern int nstates;
extern core *first_state;
extern shifts *first_shift;
extern reductions *first_reduction;
extern short *accessing_symbol;
extern core **state_table;
extern shifts **shift_table;
extern reductions **reduction_table;
extern unsigned *LA;
extern short *LAruleno;
extern short *lookaheads;
extern short *goto_map;
extern short *from_state;
extern short *to_state;

extern action **parser;
extern int SRtotal;
extern int RRtotal;
extern short *SRconflicts;
extern short *RRconflicts;
extern short *defred;
extern short *rules_used;
extern short nunused;
extern short final_state;

/* global functions */

extern char *allocate();
extern bucket *lookup();
extern bucket *make_bucket();


/* system variables */

extern int errno;

/* error.c functions */
void fatal(char *msg);
void no_space();
void open_error(char *filename);
void unexpected_EOF();
void print_pos(char *st_line, char *st_cptr);
void syntax_error(int st_lineno, char *st_line, char *st_cptr);
void unterminated_comment(int c_lineno, char *c_line, char *c_cptr);
void unterminated_string(int s_lineno, char *s_line, char *s_cptr);
void unterminated_text(int t_lineno, char *t_line, char *t_cptr);
void illegal_tag(int t_lineno, char *t_line, char *t_cptr);
void illegal_character(char *c_cptr);
void used_reserved(char *s);
void tokenized_start(char *s);
void retyped_warning(char *s);
void reprec_warning(char *s);
void revalued_warning(char *s);
void terminal_start(char *s);
void restarted_warning();
void no_grammar();
void terminal_lhs(int s_lineno);
void prec_redeclared();
void unterminated_action(int a_lineno, char *a_line, char *a_cptr);
void dollar_warning(int a_lineno, int i);
void dollar_error(int a_lineno, char *a_line, char *a_cptr);
void untyped_lhs();
void untyped_rhs(int i, char *s);
void unknown_rhs(int i);
void default_action_warning();
void undefined_goal(char *s);
void undefined_symbol_warning(char *s);

/* from symtab.c */
int hash(char* name);
bucket* make_bucket(char* name);
bucket* lookup(char *name);
void create_symbol_table();
void free_symbol_table();
void free_symbols();

/* from mkpar.c */
void make_parser();
void find_final_state();
void unused_rules();
void remove_conflicts();
void total_conflicts();
void defreds();
void free_action_row(action* p);
void free_parser();

/* from main.c */
void done(int k);
void onintr(int signo);
void set_signals();
void usage();
void getargs(int argc, char **argv);
void create_file_names();
void open_files();

/* from output.c */
void output ();
int default_goto(int symbol);
int matching_vector(int vector);
int pack_vector(int vector);
int is_C_identifier(char *name);

/* from lalr.c */
void lalr();
void set_state_table();
void set_accessing_symbol();
void set_shift_table();
void set_reduction_table();
void set_maxrhs();
void initialize_LA();
void set_goto_map();
void initialize_F();
void build_relations();
void add_lookback_edge(int stateno, int ruleno, int gotono);
void compute_FOLLOWS();
void compute_lookaheads();
void digraph(short **relation);
void traverse(int i);

/* from reader.c */
void cachec(int c);
void get_line();
void skip_comment();
void copy_text(FILE *f);
void declare_tokens(int assoc);
void declare_types();
void declare_start();
void read_declarations();
void initialize_grammar();
void expand_items();
void expand_rules();
void start_rule(bucket* bp, int s_lineno);
void advance_to_start();
void end_rule();
void insert_empty_rule();
void add_symbol();
void copy_action();
void read_grammar();
void free_tags();
void pack_names();
void check_symbols();
void pack_symbols();
void pack_grammar();
void print_grammar();
void reader();

/* from lr0.c */
void allocate_itemsets();
void allocate_storage();
void append_states();
void free_storage();
void generate_states();
void initialize_states();
void new_itemsets();
void show_cores();
void show_ritems();
void show_rrhs();
void show_shifts();
void save_shifts();
void save_reductions();
void set_derives();
void free_derives();
void print_derives();
void set_nullable();
void free_nullable();
void lr0();

/* from closure.c */
void set_EFF();
void set_first_derives();
void closure(short* nucleus, int n);
void finalize_closure();
void print_closure(int n);
void print_EFF();
void print_first_derives();

/* from warshall.c */
void reflexive_transitive_closure(unsigned* R, int n);


/* from verbose.c */
void verbose();
