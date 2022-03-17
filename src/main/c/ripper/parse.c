/* A Bison parser, made by GNU Bison 3.5.1.  */

/* Bison implementation for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2015, 2018-2020 Free Software Foundation,
   Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

/* C LALR(1) parser skeleton written by Richard Stallman, by
   simplifying the original so-called "semantic" parser.  */

/* All symbols defined below should begin with yy or YY, to avoid
   infringing on user name space.  This should be done even for local
   variables, as they might otherwise be expanded by user macros.
   There are some unavoidable exceptions within include files to
   define necessary library symbols; they are noted "INFRINGES ON
   USER NAME SPACE" below.  */

/* Undocumented macros, especially those whose name start with YY_,
   are private implementation details.  Do not rely on them.  */

/* Identify Bison output.  */
#define YYBISON 1

/* Bison version.  */
#define YYBISON_VERSION "3.5.1"

/* Skeleton name.  */
#define YYSKELETON_NAME "yacc.c"

/* Pure parsers.  */
#define YYPURE 1

/* Push parsers.  */
#define YYPUSH 0

/* Pull parsers.  */
#define YYPULL 1




/* First part of user prologue.  */
#line 12 "parse.y"


#if !YYPURE
# error needs pure parser
#endif
#define YYDEBUG 1
#define YYERROR_VERBOSE 1
#define YYSTACK_USE_ALLOCA 0
#define YYLTYPE rb_code_location_t
#define YYLTYPE_IS_DECLARED 1

#include "ruby/internal/config.h"

#include <ctype.h>
#include <errno.h>
#include <stdio.h>

struct lex_context;

#include "internal.h"
#include "internal/compile.h"
#include "internal/compilers.h"
#include "internal/complex.h"
#include "internal/error.h"
#include "internal/hash.h"
#include "internal/imemo.h"
#include "internal/io.h"
#include "internal/numeric.h"
#include "internal/parse.h"
#include "internal/rational.h"
#include "internal/re.h"
#include "internal/symbol.h"
#include "internal/thread.h"
#include "internal/util.h"
#include "internal/variable.h"
#include "node.h"
#include "probes.h"
#include "regenc.h"
#include "ruby/encoding.h"
#include "ruby/regex.h"
#include "ruby/ruby.h"
#include "ruby/st.h"
#include "ruby/util.h"
#include "ruby/ractor.h"
#include "symbol.h"

enum shareability {
    shareable_none,
    shareable_literal,
    shareable_copy,
    shareable_everything,
};

struct lex_context {
    unsigned int in_defined: 1;
    unsigned int in_kwarg: 1;
    unsigned int in_def: 1;
    unsigned int in_class: 1;
    BITFIELD(enum shareability, shareable_constant_value, 2);
};

#include "parse.h"

#define NO_LEX_CTXT (struct lex_context){0}

#define AREF(ary, i) RARRAY_AREF(ary, i)

#ifndef WARN_PAST_SCOPE
# define WARN_PAST_SCOPE 0
#endif

#define TAB_WIDTH 8

#define yydebug (p->debug)	/* disable the global variable definition */

#define YYMALLOC(size)		rb_parser_malloc(p, (size))
#define YYREALLOC(ptr, size)	rb_parser_realloc(p, (ptr), (size))
#define YYCALLOC(nelem, size)	rb_parser_calloc(p, (nelem), (size))
#define YYFREE(ptr)		rb_parser_free(p, (ptr))
#define YYFPRINTF		rb_parser_printf
#define YY_LOCATION_PRINT(File, loc) \
     rb_parser_printf(p, "%d.%d-%d.%d", \
		      (loc).beg_pos.lineno, (loc).beg_pos.column,\
		      (loc).end_pos.lineno, (loc).end_pos.column)
#define YYLLOC_DEFAULT(Current, Rhs, N)					\
    do									\
      if (N)								\
	{								\
	  (Current).beg_pos = YYRHSLOC(Rhs, 1).beg_pos;			\
	  (Current).end_pos = YYRHSLOC(Rhs, N).end_pos;			\
	}								\
      else								\
        {                                                               \
          (Current).beg_pos = YYRHSLOC(Rhs, 0).end_pos;                 \
          (Current).end_pos = YYRHSLOC(Rhs, 0).end_pos;                 \
        }                                                               \
    while (0)

#define RUBY_SET_YYLLOC_FROM_STRTERM_HEREDOC(Current)			\
    rb_parser_set_location_from_strterm_heredoc(p, &p->lex.strterm->u.heredoc, &(Current))
#define RUBY_SET_YYLLOC_OF_NONE(Current)					\
    rb_parser_set_location_of_none(p, &(Current))
#define RUBY_SET_YYLLOC(Current)					\
    rb_parser_set_location(p, &(Current))
#define RUBY_INIT_YYLLOC() \
    { \
	{p->ruby_sourceline, (int)(p->lex.ptok - p->lex.pbeg)}, \
	{p->ruby_sourceline, (int)(p->lex.pcur - p->lex.pbeg)}, \
    }

enum lex_state_bits {
    EXPR_BEG_bit,		/* ignore newline, +/- is a sign. */
    EXPR_END_bit,		/* newline significant, +/- is an operator. */
    EXPR_ENDARG_bit,		/* ditto, and unbound braces. */
    EXPR_ENDFN_bit,		/* ditto, and unbound braces. */
    EXPR_ARG_bit,		/* newline significant, +/- is an operator. */
    EXPR_CMDARG_bit,		/* newline significant, +/- is an operator. */
    EXPR_MID_bit,		/* newline significant, +/- is an operator. */
    EXPR_FNAME_bit,		/* ignore newline, no reserved words. */
    EXPR_DOT_bit,		/* right after `.' or `::', no reserved words. */
    EXPR_CLASS_bit,		/* immediate after `class', no here document. */
    EXPR_LABEL_bit,		/* flag bit, label is allowed. */
    EXPR_LABELED_bit,		/* flag bit, just after a label. */
    EXPR_FITEM_bit,		/* symbol literal as FNAME. */
    EXPR_MAX_STATE
};
/* examine combinations */
enum lex_state_e {
#define DEF_EXPR(n) EXPR_##n = (1 << EXPR_##n##_bit)
    DEF_EXPR(BEG),
    DEF_EXPR(END),
    DEF_EXPR(ENDARG),
    DEF_EXPR(ENDFN),
    DEF_EXPR(ARG),
    DEF_EXPR(CMDARG),
    DEF_EXPR(MID),
    DEF_EXPR(FNAME),
    DEF_EXPR(DOT),
    DEF_EXPR(CLASS),
    DEF_EXPR(LABEL),
    DEF_EXPR(LABELED),
    DEF_EXPR(FITEM),
    EXPR_VALUE = EXPR_BEG,
    EXPR_BEG_ANY  =  (EXPR_BEG | EXPR_MID | EXPR_CLASS),
    EXPR_ARG_ANY  =  (EXPR_ARG | EXPR_CMDARG),
    EXPR_END_ANY  =  (EXPR_END | EXPR_ENDARG | EXPR_ENDFN),
    EXPR_NONE = 0
};
#define IS_lex_state_for(x, ls)	((x) & (ls))
#define IS_lex_state_all_for(x, ls) (((x) & (ls)) == (ls))
#define IS_lex_state(ls)	IS_lex_state_for(p->lex.state, (ls))
#define IS_lex_state_all(ls)	IS_lex_state_all_for(p->lex.state, (ls))

# define SET_LEX_STATE(ls) \
    (p->lex.state = \
     (p->debug ? \
      rb_parser_trace_lex_state(p, p->lex.state, (ls), __LINE__) : \
      (enum lex_state_e)(ls)))

typedef VALUE stack_type;

static const rb_code_location_t NULL_LOC = { {0, -1}, {0, -1} };

# define SHOW_BITSTACK(stack, name) (p->debug ? rb_parser_show_bitstack(p, stack, name, __LINE__) : (void)0)
# define BITSTACK_PUSH(stack, n) (((p->stack) = ((p->stack)<<1)|((n)&1)), SHOW_BITSTACK(p->stack, #stack"(push)"))
# define BITSTACK_POP(stack)	 (((p->stack) = (p->stack) >> 1), SHOW_BITSTACK(p->stack, #stack"(pop)"))
# define BITSTACK_SET_P(stack)	 (SHOW_BITSTACK(p->stack, #stack), (p->stack)&1)
# define BITSTACK_SET(stack, n)	 ((p->stack)=(n), SHOW_BITSTACK(p->stack, #stack"(set)"))

/* A flag to identify keyword_do_cond, "do" keyword after condition expression.
   Examples: `while ... do`, `until ... do`, and `for ... in ... do` */
#define COND_PUSH(n)	BITSTACK_PUSH(cond_stack, (n))
#define COND_POP()	BITSTACK_POP(cond_stack)
#define COND_P()	BITSTACK_SET_P(cond_stack)
#define COND_SET(n)	BITSTACK_SET(cond_stack, (n))

/* A flag to identify keyword_do_block; "do" keyword after command_call.
   Example: `foo 1, 2 do`. */
#define CMDARG_PUSH(n)	BITSTACK_PUSH(cmdarg_stack, (n))
#define CMDARG_POP()	BITSTACK_POP(cmdarg_stack)
#define CMDARG_P()	BITSTACK_SET_P(cmdarg_stack)
#define CMDARG_SET(n)	BITSTACK_SET(cmdarg_stack, (n))

struct vtable {
    ID *tbl;
    int pos;
    int capa;
    struct vtable *prev;
};

struct local_vars {
    struct vtable *args;
    struct vtable *vars;
    struct vtable *used;
# if WARN_PAST_SCOPE
    struct vtable *past;
# endif
    struct local_vars *prev;
# ifndef RIPPER
    struct {
	NODE *outer, *inner, *current;
    } numparam;
# endif
};

enum {
    ORDINAL_PARAM = -1,
    NO_PARAM = 0,
    NUMPARAM_MAX = 9,
};

#define NUMPARAM_ID_P(id) numparam_id_p(id)
#define NUMPARAM_ID_TO_IDX(id) (unsigned int)(((id) >> ID_SCOPE_SHIFT) - tNUMPARAM_1 + 1)
#define NUMPARAM_IDX_TO_ID(idx) TOKEN2LOCALID((tNUMPARAM_1 + (idx) - 1))
static int
numparam_id_p(ID id)
{
    if (!is_local_id(id)) return 0;
    unsigned int idx = NUMPARAM_ID_TO_IDX(id);
    return idx > 0 && idx <= NUMPARAM_MAX;
}
static void numparam_name(struct parser_params *p, ID id);

#define DVARS_INHERIT ((void*)1)
#define DVARS_TOPSCOPE NULL
#define DVARS_TERMINAL_P(tbl) ((tbl) == DVARS_INHERIT || (tbl) == DVARS_TOPSCOPE)

typedef struct token_info {
    const char *token;
    rb_code_position_t beg;
    int indent;
    int nonspc;
    struct token_info *next;
} token_info;

typedef struct rb_strterm_struct rb_strterm_t;

/*
    Structure of Lexer Buffer:

 lex.pbeg     lex.ptok     lex.pcur     lex.pend
    |            |            |            |
    |------------+------------+------------|
                 |<---------->|
                     token
*/
struct parser_params {
    rb_imemo_tmpbuf_t *heap;

    YYSTYPE *lval;

    struct {
	rb_strterm_t *strterm;
	VALUE (*gets)(struct parser_params*,VALUE);
	VALUE input;
	VALUE prevline;
	VALUE lastline;
	VALUE nextline;
	const char *pbeg;
	const char *pcur;
	const char *pend;
	const char *ptok;
	union {
	    long ptr;
	    VALUE (*call)(VALUE, int);
	} gets_;
	enum lex_state_e state;
	/* track the nest level of any parens "()[]{}" */
	int paren_nest;
	/* keep p->lex.paren_nest at the beginning of lambda "->" to detect tLAMBEG and keyword_do_LAMBDA */
	int lpar_beg;
	/* track the nest level of only braces "{}" */
	int brace_nest;
    } lex;
    stack_type cond_stack;
    stack_type cmdarg_stack;
    int tokidx;
    int toksiz;
    int tokline;
    int heredoc_end;
    int heredoc_indent;
    int heredoc_line_indent;
    char *tokenbuf;
    struct local_vars *lvtbl;
    st_table *pvtbl;
    st_table *pktbl;
    int line_count;
    int ruby_sourceline;	/* current line no. */
    const char *ruby_sourcefile; /* current source file */
    VALUE ruby_sourcefile_string;
    rb_encoding *enc;
    token_info *token_info;
    VALUE case_labels;
    VALUE compile_option;

    VALUE debug_buffer;
    VALUE debug_output;

    ID cur_arg;

    rb_ast_t *ast;
    int node_id;

    int max_numparam;

    struct lex_context ctxt;

    unsigned int command_start:1;
    unsigned int eofp: 1;
    unsigned int ruby__end__seen: 1;
    unsigned int debug: 1;
    unsigned int has_shebang: 1;
    unsigned int token_seen: 1;
    unsigned int token_info_enabled: 1;
# if WARN_PAST_SCOPE
    unsigned int past_scope_enabled: 1;
# endif
    unsigned int error_p: 1;
    unsigned int cr_seen: 1;

#ifndef RIPPER
    /* Ruby core only */

    unsigned int do_print: 1;
    unsigned int do_loop: 1;
    unsigned int do_chomp: 1;
    unsigned int do_split: 1;

    NODE *eval_tree_begin;
    NODE *eval_tree;
    VALUE error_buffer;
    VALUE debug_lines;
    const struct rb_iseq_struct *parent_iseq;
#else
    /* Ripper only */

    struct {
	VALUE token;
	int line;
	int col;
    } delayed;

    VALUE value;
    VALUE result;
    VALUE parsing_thread;
#endif
};

#define intern_cstr(n,l,en) rb_intern3(n,l,en)

#define STR_NEW(ptr,len) rb_enc_str_new((ptr),(len),p->enc)
#define STR_NEW0() rb_enc_str_new(0,0,p->enc)
#define STR_NEW2(ptr) rb_enc_str_new((ptr),strlen(ptr),p->enc)
#define STR_NEW3(ptr,len,e,func) parser_str_new((ptr),(len),(e),(func),p->enc)
#define TOK_INTERN() intern_cstr(tok(p), toklen(p), p->enc)

static st_table *
push_pvtbl(struct parser_params *p)
{
    st_table *tbl = p->pvtbl;
    p->pvtbl = st_init_numtable();
    return tbl;
}

static void
pop_pvtbl(struct parser_params *p, st_table *tbl)
{
    st_free_table(p->pvtbl);
    p->pvtbl = tbl;
}

static st_table *
push_pktbl(struct parser_params *p)
{
    st_table *tbl = p->pktbl;
    p->pktbl = 0;
    return tbl;
}

static void
pop_pktbl(struct parser_params *p, st_table *tbl)
{
    if (p->pktbl) st_free_table(p->pktbl);
    p->pktbl = tbl;
}

static int parser_yyerror(struct parser_params*, const YYLTYPE *yylloc, const char*);
#define yyerror0(msg) parser_yyerror(p, NULL, (msg))
#define yyerror1(loc, msg) parser_yyerror(p, (loc), (msg))
#define yyerror(yylloc, p, msg) parser_yyerror(p, yylloc, msg)
#define token_flush(ptr) ((ptr)->lex.ptok = (ptr)->lex.pcur)

static void token_info_setup(token_info *ptinfo, const char *ptr, const rb_code_location_t *loc);
static void token_info_push(struct parser_params*, const char *token, const rb_code_location_t *loc);
static void token_info_pop(struct parser_params*, const char *token, const rb_code_location_t *loc);
static void token_info_warn(struct parser_params *p, const char *token, token_info *ptinfo_beg, int same, const rb_code_location_t *loc);
static void token_info_drop(struct parser_params *p, const char *token, rb_code_position_t beg_pos);

#ifdef RIPPER
#define compile_for_eval	(0)
#else
#define compile_for_eval	(p->parent_iseq != 0)
#endif

#define token_column		((int)(p->lex.ptok - p->lex.pbeg))

#define CALL_Q_P(q) ((q) == TOKEN2VAL(tANDDOT))
#define NODE_CALL_Q(q) (CALL_Q_P(q) ? NODE_QCALL : NODE_CALL)
#define NEW_QCALL(q,r,m,a,loc) NEW_NODE(NODE_CALL_Q(q),r,m,a,loc)

#define lambda_beginning_p() (p->lex.lpar_beg == p->lex.paren_nest)

static enum yytokentype yylex(YYSTYPE*, YYLTYPE*, struct parser_params*);

#ifndef RIPPER
static inline void
rb_discard_node(struct parser_params *p, NODE *n)
{
    rb_ast_delete_node(p->ast, n);
}
#endif

#ifdef RIPPER
static inline VALUE
add_mark_object(struct parser_params *p, VALUE obj)
{
    if (!SPECIAL_CONST_P(obj)
	&& !RB_TYPE_P(obj, T_NODE) /* Ripper jumbles NODE objects and other objects... */
    ) {
	rb_ast_add_mark_object(p->ast, obj);
    }
    return obj;
}
#else
static NODE* node_newnode_with_locals(struct parser_params *, enum node_type, VALUE, VALUE, const rb_code_location_t*);
#endif

static NODE* node_newnode(struct parser_params *, enum node_type, VALUE, VALUE, VALUE, const rb_code_location_t*);
#define rb_node_newnode(type, a1, a2, a3, loc) node_newnode(p, (type), (a1), (a2), (a3), (loc))

static NODE *nd_set_loc(NODE *nd, const YYLTYPE *loc);

static int
parser_get_node_id(struct parser_params *p)
{
    int node_id = p->node_id;
    p->node_id++;
    return node_id;
}

#ifndef RIPPER
static inline void
set_line_body(NODE *body, int line)
{
    if (!body) return;
    switch (nd_type(body)) {
      case NODE_RESCUE:
      case NODE_ENSURE:
	nd_set_line(body, line);
    }
}

#define yyparse ruby_yyparse

static NODE* cond(struct parser_params *p, NODE *node, const YYLTYPE *loc);
static NODE* method_cond(struct parser_params *p, NODE *node, const YYLTYPE *loc);
#define new_nil(loc) NEW_NIL(loc)
static NODE *new_nil_at(struct parser_params *p, const rb_code_position_t *pos);
static NODE *new_if(struct parser_params*,NODE*,NODE*,NODE*,const YYLTYPE*);
static NODE *new_unless(struct parser_params*,NODE*,NODE*,NODE*,const YYLTYPE*);
static NODE *logop(struct parser_params*,ID,NODE*,NODE*,const YYLTYPE*,const YYLTYPE*);

static NODE *newline_node(NODE*);
static void fixpos(NODE*,NODE*);

static int value_expr_gen(struct parser_params*,NODE*);
static void void_expr(struct parser_params*,NODE*);
static NODE *remove_begin(NODE*);
static NODE *remove_begin_all(NODE*);
#define value_expr(node) value_expr_gen(p, (node))
static NODE *void_stmts(struct parser_params*,NODE*);
static void reduce_nodes(struct parser_params*,NODE**);
static void block_dup_check(struct parser_params*,NODE*,NODE*);

static NODE *block_append(struct parser_params*,NODE*,NODE*);
static NODE *list_append(struct parser_params*,NODE*,NODE*);
static NODE *list_concat(NODE*,NODE*);
static NODE *arg_append(struct parser_params*,NODE*,NODE*,const YYLTYPE*);
static NODE *last_arg_append(struct parser_params *p, NODE *args, NODE *last_arg, const YYLTYPE *loc);
static NODE *rest_arg_append(struct parser_params *p, NODE *args, NODE *rest_arg, const YYLTYPE *loc);
static NODE *literal_concat(struct parser_params*,NODE*,NODE*,const YYLTYPE*);
static NODE *new_evstr(struct parser_params*,NODE*,const YYLTYPE*);
static NODE *new_dstr(struct parser_params*,NODE*,const YYLTYPE*);
static NODE *evstr2dstr(struct parser_params*,NODE*);
static NODE *splat_array(NODE*);
static void mark_lvar_used(struct parser_params *p, NODE *rhs);

static NODE *call_bin_op(struct parser_params*,NODE*,ID,NODE*,const YYLTYPE*,const YYLTYPE*);
static NODE *call_uni_op(struct parser_params*,NODE*,ID,const YYLTYPE*,const YYLTYPE*);
static NODE *new_qcall(struct parser_params* p, ID atype, NODE *recv, ID mid, NODE *args, const YYLTYPE *op_loc, const YYLTYPE *loc);
static NODE *new_command_qcall(struct parser_params* p, ID atype, NODE *recv, ID mid, NODE *args, NODE *block, const YYLTYPE *op_loc, const YYLTYPE *loc);
static NODE *method_add_block(struct parser_params*p, NODE *m, NODE *b, const YYLTYPE *loc) {b->nd_iter = m; b->nd_loc = *loc; return b;}

static bool args_info_empty_p(struct rb_args_info *args);
static NODE *new_args(struct parser_params*,NODE*,NODE*,ID,NODE*,NODE*,const YYLTYPE*);
static NODE *new_args_tail(struct parser_params*,NODE*,ID,ID,const YYLTYPE*);
static NODE *new_array_pattern(struct parser_params *p, NODE *constant, NODE *pre_arg, NODE *aryptn, const YYLTYPE *loc);
static NODE *new_array_pattern_tail(struct parser_params *p, NODE *pre_args, int has_rest, ID rest_arg, NODE *post_args, const YYLTYPE *loc);
static NODE *new_find_pattern(struct parser_params *p, NODE *constant, NODE *fndptn, const YYLTYPE *loc);
static NODE *new_find_pattern_tail(struct parser_params *p, ID pre_rest_arg, NODE *args, ID post_rest_arg, const YYLTYPE *loc);
static NODE *new_hash_pattern(struct parser_params *p, NODE *constant, NODE *hshptn, const YYLTYPE *loc);
static NODE *new_hash_pattern_tail(struct parser_params *p, NODE *kw_args, ID kw_rest_arg, const YYLTYPE *loc);
static void warn_one_line_pattern_matching(struct parser_params *p, NODE *node, NODE *pattern, bool right_assign);

static NODE *new_kw_arg(struct parser_params *p, NODE *k, const YYLTYPE *loc);
static NODE *args_with_numbered(struct parser_params*,NODE*,int);

static VALUE negate_lit(struct parser_params*, VALUE);
static NODE *ret_args(struct parser_params*,NODE*);
static NODE *arg_blk_pass(NODE*,NODE*);
static NODE *new_yield(struct parser_params*,NODE*,const YYLTYPE*);
static NODE *dsym_node(struct parser_params*,NODE*,const YYLTYPE*);

static NODE *gettable(struct parser_params*,ID,const YYLTYPE*);
static NODE *assignable(struct parser_params*,ID,NODE*,const YYLTYPE*);

static NODE *aryset(struct parser_params*,NODE*,NODE*,const YYLTYPE*);
static NODE *attrset(struct parser_params*,NODE*,ID,ID,const YYLTYPE*);

static void rb_backref_error(struct parser_params*,NODE*);
static NODE *node_assign(struct parser_params*,NODE*,NODE*,struct lex_context,const YYLTYPE*);

static NODE *new_op_assign(struct parser_params *p, NODE *lhs, ID op, NODE *rhs, struct lex_context, const YYLTYPE *loc);
static NODE *new_ary_op_assign(struct parser_params *p, NODE *ary, NODE *args, ID op, NODE *rhs, const YYLTYPE *args_loc, const YYLTYPE *loc);
static NODE *new_attr_op_assign(struct parser_params *p, NODE *lhs, ID atype, ID attr, ID op, NODE *rhs, const YYLTYPE *loc);
static NODE *new_const_op_assign(struct parser_params *p, NODE *lhs, ID op, NODE *rhs, struct lex_context, const YYLTYPE *loc);
static NODE *new_bodystmt(struct parser_params *p, NODE *head, NODE *rescue, NODE *rescue_else, NODE *ensure, const YYLTYPE *loc);

static NODE *const_decl(struct parser_params *p, NODE* path, const YYLTYPE *loc);

static NODE *opt_arg_append(NODE*, NODE*);
static NODE *kwd_append(NODE*, NODE*);

static NODE *new_hash(struct parser_params *p, NODE *hash, const YYLTYPE *loc);
static NODE *new_unique_key_hash(struct parser_params *p, NODE *hash, const YYLTYPE *loc);

static NODE *new_defined(struct parser_params *p, NODE *expr, const YYLTYPE *loc);

static NODE *new_regexp(struct parser_params *, NODE *, int, const YYLTYPE *);

#define make_list(list, loc) ((list) ? (nd_set_loc(list, loc), list) : NEW_ZLIST(loc))

static NODE *new_xstring(struct parser_params *, NODE *, const YYLTYPE *loc);

static NODE *symbol_append(struct parser_params *p, NODE *symbols, NODE *symbol);

static NODE *match_op(struct parser_params*,NODE*,NODE*,const YYLTYPE*,const YYLTYPE*);

static ID  *local_tbl(struct parser_params*);

static VALUE reg_compile(struct parser_params*, VALUE, int);
static void reg_fragment_setenc(struct parser_params*, VALUE, int);
static int reg_fragment_check(struct parser_params*, VALUE, int);
static NODE *reg_named_capture_assign(struct parser_params* p, VALUE regexp, const YYLTYPE *loc);

static int literal_concat0(struct parser_params *p, VALUE head, VALUE tail);
static NODE *heredoc_dedent(struct parser_params*,NODE*);

static void check_literal_when(struct parser_params *p, NODE *args, const YYLTYPE *loc);

#define get_id(id) (id)
#define get_value(val) (val)
#define get_num(num) (num)
#else  /* RIPPER */
#define NODE_RIPPER NODE_CDECL
#define NEW_RIPPER(a,b,c,loc) (VALUE)NEW_CDECL(a,b,c,loc)

static inline int ripper_is_node_yylval(VALUE n);

static inline VALUE
ripper_new_yylval(struct parser_params *p, ID a, VALUE b, VALUE c)
{
    if (ripper_is_node_yylval(c)) c = RNODE(c)->nd_cval;
    add_mark_object(p, b);
    add_mark_object(p, c);
    return NEW_RIPPER(a, b, c, &NULL_LOC);
}

static inline int
ripper_is_node_yylval(VALUE n)
{
    return RB_TYPE_P(n, T_NODE) && nd_type(RNODE(n)) == NODE_RIPPER;
}

#define value_expr(node) ((void)(node))
#define remove_begin(node) (node)
#define void_stmts(p,x) (x)
#define rb_dvar_defined(id, base) 0
#define rb_local_defined(id, base) 0
static ID ripper_get_id(VALUE);
#define get_id(id) ripper_get_id(id)
static VALUE ripper_get_value(VALUE);
#define get_value(val) ripper_get_value(val)
#define get_num(num) (int)get_id(num)
static VALUE assignable(struct parser_params*,VALUE);
static int id_is_var(struct parser_params *p, ID id);

#define method_cond(p,node,loc) (node)
#define call_bin_op(p, recv,id,arg1,op_loc,loc) dispatch3(binary, (recv), STATIC_ID2SYM(id), (arg1))
#define match_op(p,node1,node2,op_loc,loc) call_bin_op(0, (node1), idEqTilde, (node2), op_loc, loc)
#define call_uni_op(p, recv,id,op_loc,loc) dispatch2(unary, STATIC_ID2SYM(id), (recv))
#define logop(p,id,node1,node2,op_loc,loc) call_bin_op(0, (node1), (id), (node2), op_loc, loc)

#define new_nil(loc) Qnil

static VALUE new_regexp(struct parser_params *, VALUE, VALUE, const YYLTYPE *);

static VALUE const_decl(struct parser_params *p, VALUE path);

static VALUE var_field(struct parser_params *p, VALUE a);
static VALUE assign_error(struct parser_params *p, const char *mesg, VALUE a);

static VALUE parser_reg_compile(struct parser_params*, VALUE, int, VALUE *);

static VALUE backref_error(struct parser_params*, NODE *, VALUE);
#endif /* !RIPPER */

/* forward declaration */
typedef struct rb_strterm_heredoc_struct rb_strterm_heredoc_t;

RUBY_SYMBOL_EXPORT_BEGIN
VALUE rb_parser_reg_compile(struct parser_params* p, VALUE str, int options);
int rb_reg_fragment_setenc(struct parser_params*, VALUE, int);
enum lex_state_e rb_parser_trace_lex_state(struct parser_params *, enum lex_state_e, enum lex_state_e, int);
VALUE rb_parser_lex_state_name(enum lex_state_e state);
void rb_parser_show_bitstack(struct parser_params *, stack_type, const char *, int);
PRINTF_ARGS(void rb_parser_fatal(struct parser_params *p, const char *fmt, ...), 2, 3);
YYLTYPE *rb_parser_set_location_from_strterm_heredoc(struct parser_params *p, rb_strterm_heredoc_t *here, YYLTYPE *yylloc);
YYLTYPE *rb_parser_set_location_of_none(struct parser_params *p, YYLTYPE *yylloc);
YYLTYPE *rb_parser_set_location(struct parser_params *p, YYLTYPE *yylloc);
RUBY_SYMBOL_EXPORT_END

static void error_duplicate_pattern_variable(struct parser_params *p, ID id, const YYLTYPE *loc);
static void error_duplicate_pattern_key(struct parser_params *p, ID id, const YYLTYPE *loc);
#ifndef RIPPER
static ID formal_argument(struct parser_params*, ID);
#else
static ID formal_argument(struct parser_params*, VALUE);
#endif
static ID shadowing_lvar(struct parser_params*,ID);
static void new_bv(struct parser_params*,ID);

static void local_push(struct parser_params*,int);
static void local_pop(struct parser_params*);
static void local_var(struct parser_params*, ID);
static void arg_var(struct parser_params*, ID);
static int  local_id(struct parser_params *p, ID id);
static int  local_id_ref(struct parser_params*, ID, ID **);
#ifndef RIPPER
static ID   internal_id(struct parser_params*);
static NODE *new_args_forward_call(struct parser_params*, NODE*, const YYLTYPE*, const YYLTYPE*);
static NODE *new_args_forward_def(struct parser_params*, NODE*, const YYLTYPE*);
#endif
static int check_forwarding_args(struct parser_params*);
static void add_forwarding_args(struct parser_params *p);

static const struct vtable *dyna_push(struct parser_params *);
static void dyna_pop(struct parser_params*, const struct vtable *);
static int dyna_in_block(struct parser_params*);
#define dyna_var(p, id) local_var(p, id)
static int dvar_defined(struct parser_params*, ID);
static int dvar_defined_ref(struct parser_params*, ID, ID**);
static int dvar_curr(struct parser_params*,ID);

static int lvar_defined(struct parser_params*, ID);

static NODE *numparam_push(struct parser_params *p);
static void numparam_pop(struct parser_params *p, NODE *prev_inner);

#ifdef RIPPER
# define METHOD_NOT idNOT
#else
# define METHOD_NOT '!'
#endif

#define idFWD_REST   '*'
#ifdef RUBY3_KEYWORDS
#define idFWD_KWREST idPow /* Use simple "**", as tDSTAR is "**arg" */
#else
#define idFWD_KWREST 0
#endif
#define idFWD_BLOCK  '&'

#define RE_OPTION_ONCE (1<<16)
#define RE_OPTION_ENCODING_SHIFT 8
#define RE_OPTION_ENCODING(e) (((e)&0xff)<<RE_OPTION_ENCODING_SHIFT)
#define RE_OPTION_ENCODING_IDX(o) (((o)>>RE_OPTION_ENCODING_SHIFT)&0xff)
#define RE_OPTION_ENCODING_NONE(o) ((o)&RE_OPTION_ARG_ENCODING_NONE)
#define RE_OPTION_MASK  0xff
#define RE_OPTION_ARG_ENCODING_NONE 32

/* structs for managing terminator of string literal and heredocment */
typedef struct rb_strterm_literal_struct {
    union {
	VALUE dummy;
	long nest;
    } u0;
    union {
	VALUE dummy;
	long func;	    /* STR_FUNC_* (e.g., STR_FUNC_ESCAPE and STR_FUNC_EXPAND) */
    } u1;
    union {
	VALUE dummy;
	long paren;	    /* '(' of `%q(...)` */
    } u2;
    union {
	VALUE dummy;
	long term;	    /* ')' of `%q(...)` */
    } u3;
} rb_strterm_literal_t;

#define HERETERM_LENGTH_BITS ((SIZEOF_VALUE - 1) * CHAR_BIT - 1)

struct rb_strterm_heredoc_struct {
    VALUE lastline;	/* the string of line that contains `<<"END"` */
    long offset;	/* the column of END in `<<"END"` */
    int sourceline;	/* lineno of the line that contains `<<"END"` */
    unsigned length	/* the length of END in `<<"END"` */
#if HERETERM_LENGTH_BITS < SIZEOF_INT * CHAR_BIT
    : HERETERM_LENGTH_BITS
# define HERETERM_LENGTH_MAX ((1U << HERETERM_LENGTH_BITS) - 1)
#else
# define HERETERM_LENGTH_MAX UINT_MAX
#endif
    ;
#if HERETERM_LENGTH_BITS < SIZEOF_INT * CHAR_BIT
    unsigned quote: 1;
    unsigned func: 8;
#else
    uint8_t quote;
    uint8_t func;
#endif
};
STATIC_ASSERT(rb_strterm_heredoc_t, sizeof(rb_strterm_heredoc_t) <= 4 * SIZEOF_VALUE);

#define STRTERM_HEREDOC IMEMO_FL_USER0

struct rb_strterm_struct {
    VALUE flags;
    union {
	rb_strterm_literal_t literal;
	rb_strterm_heredoc_t heredoc;
    } u;
};

#ifndef RIPPER
void
rb_strterm_mark(VALUE obj)
{
    rb_strterm_t *strterm = (rb_strterm_t*)obj;
    if (RBASIC(obj)->flags & STRTERM_HEREDOC) {
	rb_strterm_heredoc_t *heredoc = &strterm->u.heredoc;
	rb_gc_mark(heredoc->lastline);
    }
}
#endif

#define yytnamerr(yyres, yystr) (YYSIZE_T)rb_yytnamerr(p, yyres, yystr)
size_t rb_yytnamerr(struct parser_params *p, char *yyres, const char *yystr);

#define TOKEN2ID(tok) ( \
    tTOKEN_LOCAL_BEGIN<(tok)&&(tok)<tTOKEN_LOCAL_END ? TOKEN2LOCALID(tok) : \
    tTOKEN_INSTANCE_BEGIN<(tok)&&(tok)<tTOKEN_INSTANCE_END ? TOKEN2INSTANCEID(tok) : \
    tTOKEN_GLOBAL_BEGIN<(tok)&&(tok)<tTOKEN_GLOBAL_END ? TOKEN2GLOBALID(tok) : \
    tTOKEN_CONST_BEGIN<(tok)&&(tok)<tTOKEN_CONST_END ? TOKEN2CONSTID(tok) : \
    tTOKEN_CLASS_BEGIN<(tok)&&(tok)<tTOKEN_CLASS_END ? TOKEN2CLASSID(tok) : \
    tTOKEN_ATTRSET_BEGIN<(tok)&&(tok)<tTOKEN_ATTRSET_END ? TOKEN2ATTRSETID(tok) : \
    ((tok) / ((tok)<tPRESERVED_ID_END && ((tok)>=128 || rb_ispunct(tok)))))

/****** Ripper *******/

#ifdef RIPPER
#define RIPPER_VERSION "0.1.0"

static inline VALUE intern_sym(const char *name);

#include "eventids1.c"
#include "eventids2.c"

static VALUE ripper_dispatch0(struct parser_params*,ID);
static VALUE ripper_dispatch1(struct parser_params*,ID,VALUE);
static VALUE ripper_dispatch2(struct parser_params*,ID,VALUE,VALUE);
static VALUE ripper_dispatch3(struct parser_params*,ID,VALUE,VALUE,VALUE);
static VALUE ripper_dispatch4(struct parser_params*,ID,VALUE,VALUE,VALUE,VALUE);
static VALUE ripper_dispatch5(struct parser_params*,ID,VALUE,VALUE,VALUE,VALUE,VALUE);
static VALUE ripper_dispatch7(struct parser_params*,ID,VALUE,VALUE,VALUE,VALUE,VALUE,VALUE,VALUE);
static void ripper_error(struct parser_params *p);

#define dispatch0(n)            ripper_dispatch0(p, TOKEN_PASTE(ripper_id_, n))
#define dispatch1(n,a)          ripper_dispatch1(p, TOKEN_PASTE(ripper_id_, n), (a))
#define dispatch2(n,a,b)        ripper_dispatch2(p, TOKEN_PASTE(ripper_id_, n), (a), (b))
#define dispatch3(n,a,b,c)      ripper_dispatch3(p, TOKEN_PASTE(ripper_id_, n), (a), (b), (c))
#define dispatch4(n,a,b,c,d)    ripper_dispatch4(p, TOKEN_PASTE(ripper_id_, n), (a), (b), (c), (d))
#define dispatch5(n,a,b,c,d,e)  ripper_dispatch5(p, TOKEN_PASTE(ripper_id_, n), (a), (b), (c), (d), (e))
#define dispatch7(n,a,b,c,d,e,f,g) ripper_dispatch7(p, TOKEN_PASTE(ripper_id_, n), (a), (b), (c), (d), (e), (f), (g))

#define yyparse ripper_yyparse

#define ID2VAL(id) STATIC_ID2SYM(id)
#define TOKEN2VAL(t) ID2VAL(TOKEN2ID(t))
#define KWD2EID(t, v) ripper_new_yylval(p, keyword_##t, get_value(v), 0)

#define params_new(pars, opts, rest, pars2, kws, kwrest, blk) \
        dispatch7(params, (pars), (opts), (rest), (pars2), (kws), (kwrest), (blk))

#define escape_Qundef(x) ((x)==Qundef ? Qnil : (x))

static inline VALUE
new_args(struct parser_params *p, VALUE pre_args, VALUE opt_args, VALUE rest_arg, VALUE post_args, VALUE tail, YYLTYPE *loc)
{
    NODE *t = (NODE *)tail;
    VALUE kw_args = t->u1.value, kw_rest_arg = t->u2.value, block = t->u3.value;
    return params_new(pre_args, opt_args, rest_arg, post_args, kw_args, kw_rest_arg, escape_Qundef(block));
}

static inline VALUE
new_args_tail(struct parser_params *p, VALUE kw_args, VALUE kw_rest_arg, VALUE block, YYLTYPE *loc)
{
    NODE *t = rb_node_newnode(NODE_ARGS_AUX, kw_args, kw_rest_arg, block, &NULL_LOC);
    add_mark_object(p, kw_args);
    add_mark_object(p, kw_rest_arg);
    add_mark_object(p, block);
    return (VALUE)t;
}

static inline VALUE
args_with_numbered(struct parser_params *p, VALUE args, int max_numparam)
{
    return args;
}

static VALUE
new_array_pattern(struct parser_params *p, VALUE constant, VALUE pre_arg, VALUE aryptn, const YYLTYPE *loc)
{
    NODE *t = (NODE *)aryptn;
    VALUE pre_args = t->u1.value, rest_arg = t->u2.value, post_args = t->u3.value;

    if (!NIL_P(pre_arg)) {
	if (!NIL_P(pre_args)) {
	    rb_ary_unshift(pre_args, pre_arg);
	}
	else {
	    pre_args = rb_ary_new_from_args(1, pre_arg);
	}
    }
    return dispatch4(aryptn, constant, pre_args, rest_arg, post_args);
}

static VALUE
new_array_pattern_tail(struct parser_params *p, VALUE pre_args, VALUE has_rest, VALUE rest_arg, VALUE post_args, const YYLTYPE *loc)
{
    NODE *t;

    if (has_rest) {
	rest_arg = dispatch1(var_field, rest_arg ? rest_arg : Qnil);
    }
    else {
	rest_arg = Qnil;
    }

    t = rb_node_newnode(NODE_ARYPTN, pre_args, rest_arg, post_args, &NULL_LOC);
    add_mark_object(p, pre_args);
    add_mark_object(p, rest_arg);
    add_mark_object(p, post_args);
    return (VALUE)t;
}

static VALUE
new_find_pattern(struct parser_params *p, VALUE constant, VALUE fndptn, const YYLTYPE *loc)
{
    NODE *t = (NODE *)fndptn;
    VALUE pre_rest_arg = t->u1.value, args = t->u2.value, post_rest_arg = t->u3.value;

    return dispatch4(fndptn, constant, pre_rest_arg, args, post_rest_arg);
}

static VALUE
new_find_pattern_tail(struct parser_params *p, VALUE pre_rest_arg, VALUE args, VALUE post_rest_arg, const YYLTYPE *loc)
{
    NODE *t;

    pre_rest_arg = dispatch1(var_field, pre_rest_arg ? pre_rest_arg : Qnil);
    post_rest_arg = dispatch1(var_field, post_rest_arg ? post_rest_arg : Qnil);

    t = rb_node_newnode(NODE_FNDPTN, pre_rest_arg, args, post_rest_arg, &NULL_LOC);
    add_mark_object(p, pre_rest_arg);
    add_mark_object(p, args);
    add_mark_object(p, post_rest_arg);
    return (VALUE)t;
}

#define new_hash(p,h,l) rb_ary_new_from_args(0)

static VALUE
new_unique_key_hash(struct parser_params *p, VALUE ary, const YYLTYPE *loc)
{
    return ary;
}

static VALUE
new_hash_pattern(struct parser_params *p, VALUE constant, VALUE hshptn, const YYLTYPE *loc)
{
    NODE *t = (NODE *)hshptn;
    VALUE kw_args = t->u1.value, kw_rest_arg = t->u2.value;
    return dispatch3(hshptn, constant, kw_args, kw_rest_arg);
}

static VALUE
new_hash_pattern_tail(struct parser_params *p, VALUE kw_args, VALUE kw_rest_arg, const YYLTYPE *loc)
{
    NODE *t;
    if (kw_rest_arg) {
	kw_rest_arg = dispatch1(var_field, kw_rest_arg);
    }
    else {
	kw_rest_arg = Qnil;
    }
    t = rb_node_newnode(NODE_HSHPTN, kw_args, kw_rest_arg, 0, &NULL_LOC);

    add_mark_object(p, kw_args);
    add_mark_object(p, kw_rest_arg);
    return (VALUE)t;
}

#define new_defined(p,expr,loc) dispatch1(defined, (expr))

static VALUE heredoc_dedent(struct parser_params*,VALUE);

#else
#define ID2VAL(id) (id)
#define TOKEN2VAL(t) ID2VAL(t)
#define KWD2EID(t, v) keyword_##t

static NODE *
set_defun_body(struct parser_params *p, NODE *n, NODE *args, NODE *body, const YYLTYPE *loc)
{
    body = remove_begin(body);
    reduce_nodes(p, &body);
    n->nd_defn = NEW_SCOPE(args, body, loc);
    n->nd_loc = *loc;
    nd_set_line(n->nd_defn, loc->end_pos.lineno);
    set_line_body(body, loc->beg_pos.lineno);
    return n;
}

static NODE *
rescued_expr(struct parser_params *p, NODE *arg, NODE *rescue,
	     const YYLTYPE *arg_loc, const YYLTYPE *mod_loc, const YYLTYPE *res_loc)
{
    YYLTYPE loc = code_loc_gen(mod_loc, res_loc);
    rescue = NEW_RESBODY(0, remove_begin(rescue), 0, &loc);
    loc.beg_pos = arg_loc->beg_pos;
    return NEW_RESCUE(arg, rescue, 0, &loc);
}

#endif /* RIPPER */

static void
restore_defun(struct parser_params *p, NODE *name)
{
    YYSTYPE c = {.val = name->nd_cval};
    p->cur_arg = name->nd_vid;
    p->ctxt.in_def = c.ctxt.in_def;
    p->ctxt.shareable_constant_value = c.ctxt.shareable_constant_value;
}

static void
endless_method_name(struct parser_params *p, NODE *defn, const YYLTYPE *loc)
{
#ifdef RIPPER
    defn = defn->nd_defn;
#endif
    ID mid = defn->nd_mid;
    if (is_attrset_id(mid)) {
	yyerror1(loc, "setter method cannot be defined in an endless method definition");
    }
    token_info_drop(p, "def", loc->beg_pos);
}

#ifndef RIPPER
# define Qnone 0
# define Qnull 0
# define ifndef_ripper(x) (x)
#else
# define Qnone Qnil
# define Qnull Qundef
# define ifndef_ripper(x)
#endif

# define rb_warn0(fmt)         WARN_CALL(WARN_ARGS(fmt, 1))
# define rb_warn1(fmt,a)       WARN_CALL(WARN_ARGS(fmt, 2), (a))
# define rb_warn2(fmt,a,b)     WARN_CALL(WARN_ARGS(fmt, 3), (a), (b))
# define rb_warn3(fmt,a,b,c)   WARN_CALL(WARN_ARGS(fmt, 4), (a), (b), (c))
# define rb_warn4(fmt,a,b,c,d) WARN_CALL(WARN_ARGS(fmt, 5), (a), (b), (c), (d))
# define rb_warning0(fmt)         WARNING_CALL(WARNING_ARGS(fmt, 1))
# define rb_warning1(fmt,a)       WARNING_CALL(WARNING_ARGS(fmt, 2), (a))
# define rb_warning2(fmt,a,b)     WARNING_CALL(WARNING_ARGS(fmt, 3), (a), (b))
# define rb_warning3(fmt,a,b,c)   WARNING_CALL(WARNING_ARGS(fmt, 4), (a), (b), (c))
# define rb_warning4(fmt,a,b,c,d) WARNING_CALL(WARNING_ARGS(fmt, 5), (a), (b), (c), (d))
# define rb_warn0L(l,fmt)         WARN_CALL(WARN_ARGS_L(l, fmt, 1))
# define rb_warn1L(l,fmt,a)       WARN_CALL(WARN_ARGS_L(l, fmt, 2), (a))
# define rb_warn2L(l,fmt,a,b)     WARN_CALL(WARN_ARGS_L(l, fmt, 3), (a), (b))
# define rb_warn3L(l,fmt,a,b,c)   WARN_CALL(WARN_ARGS_L(l, fmt, 4), (a), (b), (c))
# define rb_warn4L(l,fmt,a,b,c,d) WARN_CALL(WARN_ARGS_L(l, fmt, 5), (a), (b), (c), (d))
# define rb_warning0L(l,fmt)         WARNING_CALL(WARNING_ARGS_L(l, fmt, 1))
# define rb_warning1L(l,fmt,a)       WARNING_CALL(WARNING_ARGS_L(l, fmt, 2), (a))
# define rb_warning2L(l,fmt,a,b)     WARNING_CALL(WARNING_ARGS_L(l, fmt, 3), (a), (b))
# define rb_warning3L(l,fmt,a,b,c)   WARNING_CALL(WARNING_ARGS_L(l, fmt, 4), (a), (b), (c))
# define rb_warning4L(l,fmt,a,b,c,d) WARNING_CALL(WARNING_ARGS_L(l, fmt, 5), (a), (b), (c), (d))
#ifdef RIPPER
static ID id_warn, id_warning, id_gets, id_assoc;
# define ERR_MESG() STR_NEW2(mesg) /* to bypass Ripper DSL */
# define WARN_S_L(s,l) STR_NEW(s,l)
# define WARN_S(s) STR_NEW2(s)
# define WARN_I(i) INT2NUM(i)
# define WARN_ID(i) rb_id2str(i)
# define WARN_IVAL(i) i
# define PRIsWARN "s"
# define rb_warn0L_experimental(l,fmt)         WARN_CALL(WARN_ARGS_L(l, fmt, 1))
# define WARN_ARGS(fmt,n) p->value, id_warn, n, rb_usascii_str_new_lit(fmt)
# define WARN_ARGS_L(l,fmt,n) WARN_ARGS(fmt,n)
# ifdef HAVE_VA_ARGS_MACRO
# define WARN_CALL(...) rb_funcall(__VA_ARGS__)
# else
# define WARN_CALL rb_funcall
# endif
# define WARNING_ARGS(fmt,n) p->value, id_warning, n, rb_usascii_str_new_lit(fmt)
# define WARNING_ARGS_L(l, fmt,n) WARNING_ARGS(fmt,n)
# ifdef HAVE_VA_ARGS_MACRO
# define WARNING_CALL(...) rb_funcall(__VA_ARGS__)
# else
# define WARNING_CALL rb_funcall
# endif
PRINTF_ARGS(static void ripper_compile_error(struct parser_params*, const char *fmt, ...), 2, 3);
# define compile_error ripper_compile_error
#else
# define WARN_S_L(s,l) s
# define WARN_S(s) s
# define WARN_I(i) i
# define WARN_ID(i) rb_id2name(i)
# define WARN_IVAL(i) NUM2INT(i)
# define PRIsWARN PRIsVALUE
# define WARN_ARGS(fmt,n) WARN_ARGS_L(p->ruby_sourceline,fmt,n)
# define WARN_ARGS_L(l,fmt,n) p->ruby_sourcefile, (l), (fmt)
# define WARN_CALL rb_compile_warn
# define rb_warn0L_experimental(l,fmt) rb_category_compile_warn(RB_WARN_CATEGORY_EXPERIMENTAL, WARN_ARGS_L(l, fmt, 1))
# define WARNING_ARGS(fmt,n) WARN_ARGS(fmt,n)
# define WARNING_ARGS_L(l,fmt,n) WARN_ARGS_L(l,fmt,n)
# define WARNING_CALL rb_compile_warning
PRINTF_ARGS(static void parser_compile_error(struct parser_params*, const char *fmt, ...), 2, 3);
# define compile_error parser_compile_error
#endif

#define WARN_EOL(tok) \
    (looking_at_eol_p(p) ? \
     (void)rb_warning0("`" tok "' at the end of line without an expression") : \
     (void)0)
static int looking_at_eol_p(struct parser_params *p);

#line 1140 "parse.c"

# ifndef YY_CAST
#  ifdef __cplusplus
#   define YY_CAST(Type, Val) static_cast<Type> (Val)
#   define YY_REINTERPRET_CAST(Type, Val) reinterpret_cast<Type> (Val)
#  else
#   define YY_CAST(Type, Val) ((Type) (Val))
#   define YY_REINTERPRET_CAST(Type, Val) ((Type) (Val))
#  endif
# endif
# ifndef YY_NULLPTR
#  if defined __cplusplus
#   if 201103L <= __cplusplus
#    define YY_NULLPTR nullptr
#   else
#    define YY_NULLPTR 0
#   endif
#  else
#   define YY_NULLPTR ((void*)0)
#  endif
# endif

/* Enabling verbose error messages.  */
#ifdef YYERROR_VERBOSE
# undef YYERROR_VERBOSE
# define YYERROR_VERBOSE 1
#else
# define YYERROR_VERBOSE 1
#endif

/* Use api.header.include to #include this header
   instead of duplicating it here.  */
#ifndef YY_YY_Y_TAB_H_INCLUDED
# define YY_YY_Y_TAB_H_INCLUDED
/* Debug traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif
#if YYDEBUG
#ifndef yydebug
extern int yydebug;
#endif
#endif

/* Token type.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
  enum yytokentype
  {
    END_OF_INPUT = 0,
    keyword_class = 258,
    keyword_module = 259,
    keyword_def = 260,
    keyword_undef = 261,
    keyword_begin = 262,
    keyword_rescue = 263,
    keyword_ensure = 264,
    keyword_end = 265,
    keyword_if = 266,
    keyword_unless = 267,
    keyword_then = 268,
    keyword_elsif = 269,
    keyword_else = 270,
    keyword_case = 271,
    keyword_when = 272,
    keyword_while = 273,
    keyword_until = 274,
    keyword_for = 275,
    keyword_break = 276,
    keyword_next = 277,
    keyword_redo = 278,
    keyword_retry = 279,
    keyword_in = 280,
    keyword_do = 281,
    keyword_do_cond = 282,
    keyword_do_block = 283,
    keyword_do_LAMBDA = 284,
    keyword_return = 285,
    keyword_yield = 286,
    keyword_super = 287,
    keyword_self = 288,
    keyword_nil = 289,
    keyword_true = 290,
    keyword_false = 291,
    keyword_and = 292,
    keyword_or = 293,
    keyword_not = 294,
    modifier_if = 295,
    modifier_unless = 296,
    modifier_while = 297,
    modifier_until = 298,
    modifier_rescue = 299,
    keyword_alias = 300,
    keyword_defined = 301,
    keyword_BEGIN = 302,
    keyword_END = 303,
    keyword__LINE__ = 304,
    keyword__FILE__ = 305,
    keyword__ENCODING__ = 306,
    tIDENTIFIER = 307,
    tFID = 308,
    tGVAR = 309,
    tIVAR = 310,
    tCONSTANT = 311,
    tCVAR = 312,
    tLABEL = 313,
    tINTEGER = 314,
    tFLOAT = 315,
    tRATIONAL = 316,
    tIMAGINARY = 317,
    tCHAR = 318,
    tNTH_REF = 319,
    tBACK_REF = 320,
    tSTRING_CONTENT = 321,
    tREGEXP_END = 322,
    tSP = 323,
    tUPLUS = 132,
    tUMINUS = 133,
    tPOW = 134,
    tCMP = 135,
    tEQ = 140,
    tEQQ = 141,
    tNEQ = 142,
    tGEQ = 139,
    tLEQ = 138,
    tANDOP = 148,
    tOROP = 149,
    tMATCH = 143,
    tNMATCH = 144,
    tDOT2 = 128,
    tDOT3 = 129,
    tBDOT2 = 130,
    tBDOT3 = 131,
    tAREF = 145,
    tASET = 146,
    tLSHFT = 136,
    tRSHFT = 137,
    tANDDOT = 150,
    tCOLON2 = 147,
    tCOLON3 = 324,
    tOP_ASGN = 325,
    tASSOC = 326,
    tLPAREN = 327,
    tLPAREN_ARG = 328,
    tRPAREN = 329,
    tLBRACK = 330,
    tLBRACE = 331,
    tLBRACE_ARG = 332,
    tSTAR = 333,
    tDSTAR = 334,
    tAMPER = 335,
    tLAMBDA = 336,
    tSYMBEG = 337,
    tSTRING_BEG = 338,
    tXSTRING_BEG = 339,
    tREGEXP_BEG = 340,
    tWORDS_BEG = 341,
    tQWORDS_BEG = 342,
    tSYMBOLS_BEG = 343,
    tQSYMBOLS_BEG = 344,
    tSTRING_END = 345,
    tSTRING_DEND = 346,
    tSTRING_DBEG = 347,
    tSTRING_DVAR = 348,
    tLAMBEG = 349,
    tLABEL_END = 350,
    tLOWEST = 351,
    tUMINUS_NUM = 352,
    tLAST_TOKEN = 353
  };
#endif

/* Value type.  */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
union YYSTYPE
{
#line 1121 "parse.y"

    VALUE val;
    NODE *node;
    ID id;
    int num;
    st_table *tbl;
    const struct vtable *vars;
    struct rb_strterm_struct *strterm;
    struct lex_context ctxt;

#line 1326 "parse.c"

};
typedef union YYSTYPE YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define YYSTYPE_IS_DECLARED 1
#endif

/* Location type.  */
#if ! defined YYLTYPE && ! defined YYLTYPE_IS_DECLARED
typedef struct YYLTYPE YYLTYPE;
struct YYLTYPE
{
  int first_line;
  int first_column;
  int last_line;
  int last_column;
};
# define YYLTYPE_IS_DECLARED 1
# define YYLTYPE_IS_TRIVIAL 1
#endif



int yyparse (struct parser_params *p);

#endif /* !YY_YY_Y_TAB_H_INCLUDED  */



#ifdef short
# undef short
#endif

/* On compilers that do not define __PTRDIFF_MAX__ etc., make sure
   <limits.h> and (if available) <stdint.h> are included
   so that the code can choose integer types of a good width.  */

#ifndef __PTRDIFF_MAX__
# include <limits.h> /* INFRINGES ON USER NAME SPACE */
# if defined __STDC_VERSION__ && 199901 <= __STDC_VERSION__
#  include <stdint.h> /* INFRINGES ON USER NAME SPACE */
#  define YY_STDINT_H
# endif
#endif

/* Narrow types that promote to a signed type and that can represent a
   signed or unsigned integer of at least N bits.  In tables they can
   save space and decrease cache pressure.  Promoting to a signed type
   helps avoid bugs in integer arithmetic.  */

#ifdef __INT_LEAST8_MAX__
typedef __INT_LEAST8_TYPE__ yytype_int8;
#elif defined YY_STDINT_H
typedef int_least8_t yytype_int8;
#else
typedef signed char yytype_int8;
#endif

#ifdef __INT_LEAST16_MAX__
typedef __INT_LEAST16_TYPE__ yytype_int16;
#elif defined YY_STDINT_H
typedef int_least16_t yytype_int16;
#else
typedef short yytype_int16;
#endif

#if defined __UINT_LEAST8_MAX__ && __UINT_LEAST8_MAX__ <= __INT_MAX__
typedef __UINT_LEAST8_TYPE__ yytype_uint8;
#elif (!defined __UINT_LEAST8_MAX__ && defined YY_STDINT_H \
       && UINT_LEAST8_MAX <= INT_MAX)
typedef uint_least8_t yytype_uint8;
#elif !defined __UINT_LEAST8_MAX__ && UCHAR_MAX <= INT_MAX
typedef unsigned char yytype_uint8;
#else
typedef short yytype_uint8;
#endif

#if defined __UINT_LEAST16_MAX__ && __UINT_LEAST16_MAX__ <= __INT_MAX__
typedef __UINT_LEAST16_TYPE__ yytype_uint16;
#elif (!defined __UINT_LEAST16_MAX__ && defined YY_STDINT_H \
       && UINT_LEAST16_MAX <= INT_MAX)
typedef uint_least16_t yytype_uint16;
#elif !defined __UINT_LEAST16_MAX__ && USHRT_MAX <= INT_MAX
typedef unsigned short yytype_uint16;
#else
typedef int yytype_uint16;
#endif

#ifndef YYPTRDIFF_T
# if defined __PTRDIFF_TYPE__ && defined __PTRDIFF_MAX__
#  define YYPTRDIFF_T __PTRDIFF_TYPE__
#  define YYPTRDIFF_MAXIMUM __PTRDIFF_MAX__
# elif defined PTRDIFF_MAX
#  ifndef ptrdiff_t
#   include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  endif
#  define YYPTRDIFF_T ptrdiff_t
#  define YYPTRDIFF_MAXIMUM PTRDIFF_MAX
# else
#  define YYPTRDIFF_T long
#  define YYPTRDIFF_MAXIMUM LONG_MAX
# endif
#endif

#ifndef YYSIZE_T
# ifdef __SIZE_TYPE__
#  define YYSIZE_T __SIZE_TYPE__
# elif defined size_t
#  define YYSIZE_T size_t
# elif defined __STDC_VERSION__ && 199901 <= __STDC_VERSION__
#  include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  define YYSIZE_T size_t
# else
#  define YYSIZE_T unsigned
# endif
#endif

#define YYSIZE_MAXIMUM                                  \
  YY_CAST (YYPTRDIFF_T,                                 \
           (YYPTRDIFF_MAXIMUM < YY_CAST (YYSIZE_T, -1)  \
            ? YYPTRDIFF_MAXIMUM                         \
            : YY_CAST (YYSIZE_T, -1)))

#define YYSIZEOF(X) YY_CAST (YYPTRDIFF_T, sizeof (X))

/* Stored state numbers (used for stacks). */
typedef yytype_int16 yy_state_t;

/* State numbers in computations.  */
typedef int yy_state_fast_t;

#ifndef YY_
# if defined YYENABLE_NLS && YYENABLE_NLS
#  if ENABLE_NLS
#   include <libintl.h> /* INFRINGES ON USER NAME SPACE */
#   define YY_(Msgid) dgettext ("bison-runtime", Msgid)
#  endif
# endif
# ifndef YY_
#  define YY_(Msgid) Msgid
# endif
#endif

#ifndef YY_ATTRIBUTE_PURE
# if defined __GNUC__ && 2 < __GNUC__ + (96 <= __GNUC_MINOR__)
#  define YY_ATTRIBUTE_PURE __attribute__ ((__pure__))
# else
#  define YY_ATTRIBUTE_PURE
# endif
#endif

#ifndef YY_ATTRIBUTE_UNUSED
# if defined __GNUC__ && 2 < __GNUC__ + (7 <= __GNUC_MINOR__)
#  define YY_ATTRIBUTE_UNUSED __attribute__ ((__unused__))
# else
#  define YY_ATTRIBUTE_UNUSED
# endif
#endif

/* Suppress unused-variable warnings by "using" E.  */
#if ! defined lint || defined __GNUC__
# define YYUSE(E) ((void) (E))
#else
# define YYUSE(E) /* empty */
#endif

#if defined __GNUC__ && ! defined __ICC && 407 <= __GNUC__ * 100 + __GNUC_MINOR__
/* Suppress an incorrect diagnostic about yylval being uninitialized.  */
# define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN                            \
    _Pragma ("GCC diagnostic push")                                     \
    _Pragma ("GCC diagnostic ignored \"-Wuninitialized\"")              \
    _Pragma ("GCC diagnostic ignored \"-Wmaybe-uninitialized\"")
# define YY_IGNORE_MAYBE_UNINITIALIZED_END      \
    _Pragma ("GCC diagnostic pop")
#else
# define YY_INITIAL_VALUE(Value) Value
#endif
#ifndef YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# define YY_IGNORE_MAYBE_UNINITIALIZED_END
#endif
#ifndef YY_INITIAL_VALUE
# define YY_INITIAL_VALUE(Value) /* Nothing. */
#endif

#if defined __cplusplus && defined __GNUC__ && ! defined __ICC && 6 <= __GNUC__
# define YY_IGNORE_USELESS_CAST_BEGIN                          \
    _Pragma ("GCC diagnostic push")                            \
    _Pragma ("GCC diagnostic ignored \"-Wuseless-cast\"")
# define YY_IGNORE_USELESS_CAST_END            \
    _Pragma ("GCC diagnostic pop")
#endif
#ifndef YY_IGNORE_USELESS_CAST_BEGIN
# define YY_IGNORE_USELESS_CAST_BEGIN
# define YY_IGNORE_USELESS_CAST_END
#endif


#define YY_ASSERT(E) ((void) (0 && (E)))

#if ! defined yyoverflow || YYERROR_VERBOSE

/* The parser invokes alloca or malloc; define the necessary symbols.  */

# ifdef YYSTACK_USE_ALLOCA
#  if YYSTACK_USE_ALLOCA
#   ifdef __GNUC__
#    define YYSTACK_ALLOC __builtin_alloca
#   elif defined __BUILTIN_VA_ARG_INCR
#    include <alloca.h> /* INFRINGES ON USER NAME SPACE */
#   elif defined _AIX
#    define YYSTACK_ALLOC __alloca
#   elif defined _MSC_VER
#    include <malloc.h> /* INFRINGES ON USER NAME SPACE */
#    define alloca _alloca
#   else
#    define YYSTACK_ALLOC alloca
#    if ! defined _ALLOCA_H && ! defined EXIT_SUCCESS
#     include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
      /* Use EXIT_SUCCESS as a witness for stdlib.h.  */
#     ifndef EXIT_SUCCESS
#      define EXIT_SUCCESS 0
#     endif
#    endif
#   endif
#  endif
# endif

# ifdef YYSTACK_ALLOC
   /* Pacify GCC's 'empty if-body' warning.  */
#  define YYSTACK_FREE(Ptr) do { /* empty */; } while (0)
#  ifndef YYSTACK_ALLOC_MAXIMUM
    /* The OS might guarantee only one guard page at the bottom of the stack,
       and a page size can be as small as 4096 bytes.  So we cannot safely
       invoke alloca (N) if N exceeds 4096.  Use a slightly smaller number
       to allow for a few compiler-allocated temporary stack slots.  */
#   define YYSTACK_ALLOC_MAXIMUM 4032 /* reasonable circa 2006 */
#  endif
# else
#  define YYSTACK_ALLOC YYMALLOC
#  define YYSTACK_FREE YYFREE
#  ifndef YYSTACK_ALLOC_MAXIMUM
#   define YYSTACK_ALLOC_MAXIMUM YYSIZE_MAXIMUM
#  endif
#  if (defined __cplusplus && ! defined EXIT_SUCCESS \
       && ! ((defined YYMALLOC || defined malloc) \
             && (defined YYFREE || defined free)))
#   include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
#   ifndef EXIT_SUCCESS
#    define EXIT_SUCCESS 0
#   endif
#  endif
#  ifndef YYMALLOC
#   define YYMALLOC malloc
#   if ! defined malloc && ! defined EXIT_SUCCESS
void *malloc (YYSIZE_T); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
#  ifndef YYFREE
#   define YYFREE free
#   if ! defined free && ! defined EXIT_SUCCESS
void free (void *); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
# endif
#endif /* ! defined yyoverflow || YYERROR_VERBOSE */


#if (! defined yyoverflow \
     && (! defined __cplusplus \
         || (defined YYLTYPE_IS_TRIVIAL && YYLTYPE_IS_TRIVIAL \
             && defined YYSTYPE_IS_TRIVIAL && YYSTYPE_IS_TRIVIAL)))

/* A type that is properly aligned for any stack member.  */
union yyalloc
{
  yy_state_t yyss_alloc;
  YYSTYPE yyvs_alloc;
  YYLTYPE yyls_alloc;
};

/* The size of the maximum gap between one aligned stack and the next.  */
# define YYSTACK_GAP_MAXIMUM (YYSIZEOF (union yyalloc) - 1)

/* The size of an array large to enough to hold all stacks, each with
   N elements.  */
# define YYSTACK_BYTES(N) \
     ((N) * (YYSIZEOF (yy_state_t) + YYSIZEOF (YYSTYPE) \
             + YYSIZEOF (YYLTYPE)) \
      + 2 * YYSTACK_GAP_MAXIMUM)

# define YYCOPY_NEEDED 1

/* Relocate STACK from its old location to the new one.  The
   local variables YYSIZE and YYSTACKSIZE give the old and new number of
   elements in the stack, and YYPTR gives the new location of the
   stack.  Advance YYPTR to a properly aligned location for the next
   stack.  */
# define YYSTACK_RELOCATE(Stack_alloc, Stack)                           \
    do                                                                  \
      {                                                                 \
        YYPTRDIFF_T yynewbytes;                                         \
        YYCOPY (&yyptr->Stack_alloc, Stack, yysize);                    \
        Stack = &yyptr->Stack_alloc;                                    \
        yynewbytes = yystacksize * YYSIZEOF (*Stack) + YYSTACK_GAP_MAXIMUM; \
        yyptr += yynewbytes / YYSIZEOF (*yyptr);                        \
      }                                                                 \
    while (0)

#endif

#if defined YYCOPY_NEEDED && YYCOPY_NEEDED
/* Copy COUNT objects from SRC to DST.  The source and destination do
   not overlap.  */
# ifndef YYCOPY
#  if defined __GNUC__ && 1 < __GNUC__
#   define YYCOPY(Dst, Src, Count) \
      __builtin_memcpy (Dst, Src, YY_CAST (YYSIZE_T, (Count)) * sizeof (*(Src)))
#  else
#   define YYCOPY(Dst, Src, Count)              \
      do                                        \
        {                                       \
          YYPTRDIFF_T yyi;                      \
          for (yyi = 0; yyi < (Count); yyi++)   \
            (Dst)[yyi] = (Src)[yyi];            \
        }                                       \
      while (0)
#  endif
# endif
#endif /* !YYCOPY_NEEDED */

/* YYFINAL -- State number of the termination state.  */
#define YYFINAL  3
/* YYLAST -- Last index in YYTABLE.  */
#define YYLAST   14332

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  154
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  270
/* YYNRULES -- Number of rules.  */
#define YYNRULES  773
/* YYNSTATES -- Number of states.  */
#define YYNSTATES  1294

#define YYUNDEFTOK  2
#define YYMAXUTOK   353


/* YYTRANSLATE(TOKEN-NUM) -- Symbol number corresponding to TOKEN-NUM
   as returned by yylex, with out-of-bounds checking.  */
#define YYTRANSLATE(YYX)                                                \
  (0 <= (YYX) && (YYX) <= YYMAXUTOK ? yytranslate[YYX] : YYUNDEFTOK)

/* YYTRANSLATE[TOKEN-NUM] -- Symbol number corresponding to TOKEN-NUM
   as returned by yylex.  */
static const yytype_uint8 yytranslate[] =
{
       0,     2,     2,     2,     2,     2,     2,     2,     2,    71,
     153,    74,    72,    73,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,   152,   140,     2,     2,     2,   138,   133,     2,
     148,   149,   136,   134,   146,   135,    68,   137,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,   128,   151,
     130,   126,   129,   127,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,   145,    69,   150,   132,     2,   147,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,   143,   131,   144,   141,     2,    88,    89,
      90,    91,    75,    76,    77,    78,    94,    95,    83,    82,
      79,    80,    81,    86,    87,    92,    93,    97,    84,    85,
      96,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59,    60,    61,    62,    63,    64,
      65,    66,    67,    70,    98,    99,   100,   101,   102,   103,
     104,   105,   106,   107,   108,   109,   110,   111,   112,   113,
     114,   115,   116,   117,   118,   119,   120,   121,   122,   123,
     124,   125,   139,   142
};

#if YYDEBUG
  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
static const yytype_int16 yyrline[] =
{
       0,  1322,  1322,  1322,  1348,  1354,  1361,  1368,  1375,  1381,
    1382,  1388,  1401,  1399,  1410,  1421,  1427,  1434,  1441,  1448,
    1454,  1459,  1458,  1468,  1468,  1475,  1482,  1492,  1501,  1508,
    1516,  1524,  1536,  1548,  1558,  1572,  1573,  1581,  1589,  1598,
    1605,  1608,  1615,  1622,  1630,  1637,  1644,  1652,  1659,  1669,
    1674,  1683,  1686,  1687,  1691,  1695,  1699,  1704,  1711,  1713,
    1703,  1723,  1730,  1732,  1722,  1741,  1744,  1761,  1770,  1770,
    1784,  1791,  1791,  1791,  1797,  1798,  1801,  1802,  1811,  1821,
    1831,  1840,  1851,  1858,  1865,  1872,  1879,  1887,  1895,  1902,
    1909,  1918,  1919,  1928,  1929,  1938,  1945,  1952,  1959,  1966,
    1973,  1980,  1987,  1994,  2001,  2010,  2011,  2020,  2027,  2036,
    2043,  2052,  2059,  2066,  2073,  2083,  2090,  2100,  2107,  2114,
    2124,  2131,  2138,  2145,  2152,  2159,  2166,  2173,  2180,  2190,
    2198,  2201,  2208,  2215,  2224,  2225,  2226,  2227,  2232,  2235,
    2242,  2245,  2252,  2252,  2262,  2263,  2264,  2265,  2266,  2267,
    2268,  2269,  2270,  2271,  2272,  2273,  2274,  2275,  2276,  2277,
    2278,  2279,  2280,  2281,  2282,  2283,  2284,  2285,  2286,  2287,
    2288,  2289,  2290,  2291,  2294,  2294,  2294,  2295,  2295,  2296,
    2296,  2296,  2297,  2297,  2297,  2297,  2298,  2298,  2298,  2298,
    2299,  2299,  2299,  2300,  2300,  2300,  2300,  2301,  2301,  2301,
    2301,  2302,  2302,  2302,  2302,  2303,  2303,  2303,  2303,  2304,
    2304,  2304,  2304,  2305,  2305,  2308,  2315,  2322,  2329,  2336,
    2343,  2350,  2358,  2365,  2373,  2382,  2391,  2399,  2407,  2415,
    2423,  2427,  2431,  2435,  2439,  2443,  2447,  2451,  2455,  2459,
    2463,  2467,  2471,  2475,  2476,  2480,  2484,  2488,  2492,  2496,
    2500,  2504,  2508,  2512,  2516,  2520,  2520,  2525,  2534,  2544,
    2555,  2567,  2580,  2586,  2587,  2588,  2589,  2592,  2596,  2603,
    2607,  2613,  2620,  2621,  2625,  2632,  2641,  2646,  2656,  2663,
    2675,  2689,  2690,  2693,  2694,  2695,  2699,  2706,  2715,  2723,
    2730,  2738,  2746,  2750,  2750,  2787,  2796,  2800,  2806,  2813,
    2820,  2827,  2836,  2837,  2840,  2847,  2854,  2863,  2864,  2865,
    2866,  2867,  2868,  2869,  2870,  2871,  2872,  2873,  2881,  2880,
    2895,  2895,  2902,  2902,  2910,  2918,  2925,  2932,  2939,  2947,
    2954,  2961,  2968,  2975,  2975,  2980,  2984,  2988,  2995,  2996,
    3004,  3005,  3016,  3027,  3037,  3048,  3047,  3064,  3063,  3078,
    3087,  3132,  3131,  3155,  3154,  3177,  3176,  3199,  3211,  3225,
    3232,  3239,  3246,  3255,  3262,  3268,  3285,  3291,  3297,  3303,
    3309,  3315,  3322,  3329,  3335,  3341,  3347,  3353,  3359,  3365,
    3380,  3387,  3393,  3400,  3401,  3402,  3405,  3406,  3409,  3410,
    3422,  3423,  3432,  3433,  3436,  3444,  3453,  3460,  3469,  3476,
    3483,  3490,  3497,  3506,  3514,  3523,  3524,  3527,  3531,  3535,
    3539,  3545,  3550,  3555,  3565,  3569,  3573,  3577,  3581,  3585,
    3590,  3594,  3598,  3602,  3606,  3610,  3614,  3618,  3622,  3628,
    3629,  3635,  3644,  3656,  3660,  3669,  3671,  3675,  3680,  3687,
    3693,  3697,  3701,  3686,  3726,  3734,  3744,  3749,  3755,  3765,
    3779,  3786,  3793,  3802,  3811,  3819,  3827,  3834,  3842,  3850,
    3857,  3864,  3877,  3885,  3895,  3896,  3900,  3895,  3917,  3918,
    3922,  3917,  3941,  3949,  3956,  3964,  3973,  3985,  3986,  3990,
    3997,  4001,  3989,  4016,  4017,  4020,  4021,  4029,  4039,  4040,
    4045,  4053,  4057,  4061,  4067,  4070,  4079,  4082,  4089,  4092,
    4093,  4095,  4096,  4105,  4114,  4123,  4128,  4137,  4146,  4155,
    4160,  4164,  4168,  4174,  4173,  4185,  4190,  4190,  4197,  4206,
    4210,  4219,  4223,  4227,  4231,  4235,  4238,  4242,  4251,  4255,
    4261,  4271,  4275,  4281,  4282,  4291,  4300,  4304,  4308,  4312,
    4318,  4320,  4329,  4337,  4351,  4352,  4375,  4379,  4385,  4391,
    4392,  4395,  4396,  4405,  4414,  4422,  4430,  4431,  4432,  4433,
    4441,  4451,  4452,  4453,  4454,  4455,  4456,  4457,  4458,  4459,
    4466,  4469,  4479,  4492,  4499,  4506,  4515,  4527,  4530,  4537,
    4544,  4547,  4551,  4554,  4561,  4564,  4565,  4568,  4585,  4586,
    4587,  4596,  4606,  4615,  4621,  4631,  4637,  4646,  4648,  4657,
    4667,  4673,  4682,  4691,  4701,  4707,  4717,  4723,  4733,  4743,
    4762,  4768,  4778,  4788,  4829,  4832,  4831,  4848,  4852,  4857,
    4861,  4865,  4847,  4886,  4893,  4900,  4907,  4910,  4911,  4914,
    4924,  4925,  4926,  4927,  4930,  4940,  4941,  4951,  4952,  4953,
    4954,  4957,  4958,  4959,  4960,  4961,  4964,  4965,  4966,  4967,
    4968,  4969,  4970,  4973,  4986,  4995,  5002,  5011,  5012,  5016,
    5015,  5025,  5033,  5033,  5035,  5044,  5054,  5066,  5067,  5067,
    5081,  5085,  5089,  5093,  5099,  5104,  5109,  5113,  5117,  5121,
    5125,  5129,  5133,  5137,  5141,  5145,  5149,  5153,  5157,  5161,
    5166,  5172,  5181,  5190,  5199,  5208,  5219,  5220,  5228,  5237,
    5245,  5266,  5268,  5281,  5290,  5298,  5308,  5315,  5324,  5331,
    5341,  5348,  5357,  5358,  5361,  5369,  5377,  5387,  5397,  5407,
    5414,  5423,  5430,  5439,  5440,  5443,  5451,  5461,  5462,  5465,
    5475,  5479,  5485,  5490,  5490,  5514,  5515,  5524,  5526,  5549,
    5560,  5567,  5575,  5594,  5595,  5596,  5599,  5600,  5601,  5602,
    5605,  5606,  5607,  5610,  5611,  5614,  5615,  5618,  5619,  5622,
    5623,  5626,  5627,  5630,  5633,  5636,  5639,  5640,  5641,  5644,
    5645,  5648,  5649,  5653
};
#endif

#if YYDEBUG || YYERROR_VERBOSE || 1
/* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
   First, the terminals, then, starting at YYNTOKENS, nonterminals.  */
static const char *const yytname[] =
{
  "\"end-of-input\"", "error", "$undefined", "\"`class'\"",
  "\"`module'\"", "\"`def'\"", "\"`undef'\"", "\"`begin'\"",
  "\"`rescue'\"", "\"`ensure'\"", "\"`end'\"", "\"`if'\"", "\"`unless'\"",
  "\"`then'\"", "\"`elsif'\"", "\"`else'\"", "\"`case'\"", "\"`when'\"",
  "\"`while'\"", "\"`until'\"", "\"`for'\"", "\"`break'\"", "\"`next'\"",
  "\"`redo'\"", "\"`retry'\"", "\"`in'\"", "\"`do'\"",
  "\"`do' for condition\"", "\"`do' for block\"", "\"`do' for lambda\"",
  "\"`return'\"", "\"`yield'\"", "\"`super'\"", "\"`self'\"", "\"`nil'\"",
  "\"`true'\"", "\"`false'\"", "\"`and'\"", "\"`or'\"", "\"`not'\"",
  "\"`if' modifier\"", "\"`unless' modifier\"", "\"`while' modifier\"",
  "\"`until' modifier\"", "\"`rescue' modifier\"", "\"`alias'\"",
  "\"`defined?'\"", "\"`BEGIN'\"", "\"`END'\"", "\"`__LINE__'\"",
  "\"`__FILE__'\"", "\"`__ENCODING__'\"", "\"local variable or method\"",
  "\"method\"", "\"global variable\"", "\"instance variable\"",
  "\"constant\"", "\"class variable\"", "\"label\"", "\"integer literal\"",
  "\"float literal\"", "\"rational literal\"", "\"imaginary literal\"",
  "\"char literal\"", "\"numbered reference\"", "\"back reference\"",
  "\"literal content\"", "tREGEXP_END", "'.'", "\"backslash\"",
  "\"escaped space\"", "\"escaped horizontal tab\"",
  "\"escaped form feed\"", "\"escaped carriage return\"",
  "\"escaped vertical tab\"", "\"unary+\"", "\"unary-\"", "\"**\"",
  "\"<=>\"", "\"==\"", "\"===\"", "\"!=\"", "\">=\"", "\"<=\"", "\"&&\"",
  "\"||\"", "\"=~\"", "\"!~\"", "\"..\"", "\"...\"", "\"(..\"", "\"(...\"",
  "\"[]\"", "\"[]=\"", "\"<<\"", "\">>\"", "\"&.\"", "\"::\"",
  "\":: at EXPR_BEG\"", "\"operator-assignment\"", "\"=>\"", "\"(\"",
  "\"( arg\"", "\")\"", "\"[\"", "\"{\"", "\"{ arg\"", "\"*\"",
  "\"**arg\"", "\"&\"", "\"->\"", "\"symbol literal\"",
  "\"string literal\"", "\"backtick literal\"", "\"regexp literal\"",
  "\"word list\"", "\"verbatim word list\"", "\"symbol list\"",
  "\"verbatim symbol list\"", "\"terminator\"", "\"'}'\"", "tSTRING_DBEG",
  "tSTRING_DVAR", "tLAMBEG", "tLABEL_END", "tLOWEST", "'='", "'?'", "':'",
  "'>'", "'<'", "'|'", "'^'", "'&'", "'+'", "'-'", "'*'", "'/'", "'%'",
  "tUMINUS_NUM", "'!'", "'~'", "tLAST_TOKEN", "'{'", "'}'", "'['", "','",
  "'`'", "'('", "')'", "']'", "';'", "' '", "'\\n'", "$accept", "program",
  "$@1", "top_compstmt", "top_stmts", "top_stmt", "begin_block",
  "bodystmt", "$@2", "compstmt", "stmts", "stmt_or_begin", "$@3", "stmt",
  "$@4", "command_asgn", "command_rhs", "expr", "@5", "@6", "$@7", "@8",
  "@9", "$@10", "def_name", "defn_head", "defs_head", "$@11", "expr_value",
  "expr_value_do", "$@12", "$@13", "command_call", "block_command",
  "cmd_brace_block", "fcall", "command", "mlhs", "mlhs_inner",
  "mlhs_basic", "mlhs_item", "mlhs_head", "mlhs_post", "mlhs_node", "lhs",
  "cname", "cpath", "fname", "fitem", "undef_list", "$@14", "op",
  "reswords", "arg", "$@15", "relop", "rel_expr", "lex_ctxt", "arg_value",
  "aref_args", "arg_rhs", "paren_args", "opt_paren_args", "opt_call_args",
  "call_args", "command_args", "$@16", "block_arg", "opt_block_arg",
  "args", "mrhs_arg", "mrhs", "primary", "$@17", "$@18", "$@19", "$@20",
  "@21", "@22", "$@23", "$@24", "$@25", "primary_value", "k_begin", "k_if",
  "k_unless", "k_while", "k_until", "k_case", "k_for", "k_class",
  "k_module", "k_def", "k_do", "k_do_block", "k_rescue", "k_ensure",
  "k_when", "k_else", "k_elsif", "k_end", "k_return", "then", "do",
  "if_tail", "opt_else", "for_var", "f_marg", "f_marg_list", "f_margs",
  "f_rest_marg", "f_any_kwrest", "block_args_tail", "opt_block_args_tail",
  "excessed_comma", "block_param", "opt_block_param", "block_param_def",
  "opt_bv_decl", "bv_decls", "bvar", "lambda", "@26", "@27", "@28", "$@29",
  "f_larglist", "lambda_body", "do_block", "block_call", "method_call",
  "brace_block", "brace_body", "@30", "@31", "@32", "do_body", "@33",
  "@34", "@35", "case_args", "case_body", "cases", "p_case_body", "@36",
  "@37", "$@38", "p_cases", "p_top_expr", "p_top_expr_body", "p_expr",
  "p_as", "p_alt", "p_lparen", "p_lbracket", "p_expr_basic", "@39", "@40",
  "p_args", "p_args_head", "p_args_tail", "p_find", "p_rest",
  "p_args_post", "p_arg", "p_kwargs", "p_kwarg", "p_kw", "p_kw_label",
  "p_kwrest", "p_kwnorest", "p_any_kwrest", "p_value", "p_primitive",
  "p_variable", "p_var_ref", "p_const", "opt_rescue", "exc_list",
  "exc_var", "opt_ensure", "literal", "strings", "string", "string1",
  "xstring", "regexp", "words", "word_list", "word", "symbols",
  "symbol_list", "qwords", "qsymbols", "qword_list", "qsym_list",
  "string_contents", "xstring_contents", "regexp_contents",
  "string_content", "@41", "$@42", "@43", "@44", "@45", "@46",
  "string_dvar", "symbol", "ssym", "sym", "dsym", "numeric",
  "simple_numeric", "user_variable", "keyword_variable", "var_ref",
  "var_lhs", "backref", "superclass", "$@47", "f_opt_paren_args",
  "f_paren_args", "f_arglist", "@48", "args_tail", "opt_args_tail",
  "f_args", "args_forward", "f_bad_arg", "f_norm_arg", "f_arg_asgn",
  "f_arg_item", "f_arg", "f_label", "f_kw", "f_block_kw", "f_block_kwarg",
  "f_kwarg", "kwrest_mark", "f_no_kwarg", "f_kwrest", "f_opt",
  "f_block_opt", "f_block_optarg", "f_optarg", "restarg_mark",
  "f_rest_arg", "blkarg_mark", "f_block_arg", "opt_f_block_arg",
  "singleton", "$@49", "assoc_list", "assocs", "assoc", "operation",
  "operation2", "operation3", "dot_or_colon", "call_op", "call_op2",
  "opt_terms", "opt_nl", "rparen", "rbracket", "rbrace", "trailer", "term",
  "terms", "none", YY_NULLPTR
};
#endif

# ifdef YYPRINT
/* YYTOKNUM[NUM] -- (External) token number corresponding to the
   (internal) symbol number NUM (which must be that of a token).  */
static const yytype_int16 yytoknum[] =
{
       0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
     275,   276,   277,   278,   279,   280,   281,   282,   283,   284,
     285,   286,   287,   288,   289,   290,   291,   292,   293,   294,
     295,   296,   297,   298,   299,   300,   301,   302,   303,   304,
     305,   306,   307,   308,   309,   310,   311,   312,   313,   314,
     315,   316,   317,   318,   319,   320,   321,   322,    46,    92,
     323,     9,    12,    13,    11,   132,   133,   134,   135,   140,
     141,   142,   139,   138,   148,   149,   143,   144,   128,   129,
     130,   131,   145,   146,   136,   137,   150,   147,   324,   325,
     326,   327,   328,   329,   330,   331,   332,   333,   334,   335,
     336,   337,   338,   339,   340,   341,   342,   343,   344,   345,
     346,   347,   348,   349,   350,   351,    61,    63,    58,    62,
      60,   124,    94,    38,    43,    45,    42,    47,    37,   352,
      33,   126,   353,   123,   125,    91,    44,    96,    40,    41,
      93,    59,    32,    10
};
# endif

#define YYPACT_NINF (-1080)

#define yypact_value_is_default(Yyn) \
  ((Yyn) == YYPACT_NINF)

#define YYTABLE_NINF (-774)

#define yytable_value_is_error(Yyn) \
  ((Yyn) == YYTABLE_NINF)

  /* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
     STATE-NUM.  */
static const yytype_int16 yypact[] =
{
   -1080,   155,  4053, -1080,  9649, -1080, -1080, -1080,  9107, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080,  9775,  9775, -1080, -1080,
   -1080,  5567,  5126, -1080, -1080, -1080, -1080,   353,  8962,   127,
      41,    83, -1080, -1080, -1080,  4391,  5273, -1080, -1080,  4538,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, 11539, 11539,
   11539, 11539,   112,  7162,  9901, 10405, 10783,  9391, -1080,  8817,
   -1080, -1080, -1080,   164,   217,   257,   289,  1108, 11665, 11539,
   -1080,   512, -1080,  1512, -1080,   728,   227,   227, -1080, -1080,
      66,   425,   347, -1080,   341, 11917, -1080,   377,  2218,   632,
     499,   745, -1080, 11791, 11791, -1080, -1080,  8144, 12039, 12161,
   12283,  8671,  9775, -1080,   444,    81, -1080, -1080,   491, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080,   316,   445, -1080,   558,   451, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080,   520, -1080, -1080, -1080,   539,
   11539,   642,  7313, 11539, 11539, 11539, -1080, 11539, -1080,   586,
    4957,   617, -1080, -1080,   597,   770,    30,    31,   647,   350,
     611, -1080, -1080,  8018, -1080,  9775, 10027, -1080, -1080,  8270,
   -1080, 11791,   613, -1080,   619,  7464, -1080,  7615, -1080, -1080,
     631,   633,    66, -1080,   788, -1080,   755,  5104,  5104,   453,
    9901, -1080,  7162,   654,   512, -1080,  1512,   127,   694, -1080,
    1512,   127,   690,   244,   708, -1080,   617,   706,   708, -1080,
     127,   778,  1108, 12405,   704,   704,   709, -1080,   827,   900,
     926,   932, -1080, -1080, -1080, -1080, -1080,    61, -1080,   400,
     629,   509, -1080, -1080, -1080, -1080,   781, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080,  8396, 11791, 11791, 11791, 11791,  9901,
   11791, 11791,  1279,   737,   754,  6306,  1694, -1080,   765,  6306,
   -1080, -1080, -1080,   791, -1080, -1080, -1080, -1080, -1080,   834,
   -1080,  7162,  9520,   768,   834, -1080, 11539, 11539, 11539, 11539,
   11539, -1080, -1080, 11539, 11539, 11539, 11539, 11539, 11539, 11539,
   11539, -1080, 11539, -1080, -1080, 11539, 11539, 11539, 11539, 11539,
   11539, 11539, 11539, 11539, 11539, -1080, -1080, 12835,  9775, 12925,
    6306,   728,    96,    96,  7766, 11791,  7766,   512, -1080,   775,
     880, -1080, -1080,   956,   929,    90,    93,   106,   726,   861,
   11791,   473, -1080,   826,   990, -1080, -1080, -1080, -1080,    45,
      64,   382,   465,   493,   514,   537,   545,   573, -1080, -1080,
   -1080, -1080,   637, -1080, -1080, -1080, 14185, -1080, -1080,   834,
     834, -1080, -1080,   362, -1080, -1080, -1080,   751,   834, 11539,
   10153, -1080, -1080, 13015,  9775, 13105,   834,   834, 10531, -1080,
     127,   814, -1080, -1080, 11539,   127, -1080,   820,   127,   832,
   -1080,   137, -1080, -1080, -1080, -1080, -1080,  9107, -1080, 11539,
     845,   847, 13015, 13105,   834,  1512,    41,   127, -1080, -1080,
    8522,   852,   127, -1080, -1080, 10657, -1080, -1080, 10783, -1080,
   -1080, -1080,   619,   995, -1080, -1080,   865, -1080, 12405, 13195,
    9775, 13285, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080,   743,   110,   946,   191, 11539, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080,  1307, -1080, -1080, -1080, -1080,
   -1080,   868, -1080,   127,   127, -1080, -1080,   867, -1080,   881,
   11539, -1080,   888,   391, -1080, -1080, -1080,   890,   989,   896,
     999, -1080, 11539,  1037,  1046,   512,   909, 11539,  1037,   917,
   -1080, -1080, -1080,  1037, -1080,  1037, -1080, 10909, -1080,   127,
   12405,   923, -1080, 10909, -1080,   755,  3708,  3708,  3708,  3708,
    5251,  2121,  3708,  3708,  5104,  5104,   592,   592, -1080,  5689,
    1165,  1165,  1456,   447,   447,   755,   755,   755,  1451,  1451,
    5714,  4685,  6008,  4832, -1080,   633, -1080,   127,   924,   489,
   -1080,   623, -1080, -1080,  5420,  1037, -1080,  6457,  1062,  6910,
    1037,    38,  1037,  1053,  1066,   118, 13375,  9775, 13465, -1080,
     728, -1080,   995, -1080, -1080, -1080, 13555,  9775, 13645,  6306,
   11791, -1080, -1080, -1080, -1080, -1080,  3013, 11665, 11665,  9107,
   11539, 11539, -1080, 11539,   617, -1080,   611,  4239,  4979,   127,
     481,   648, 11539, 11539, -1080, -1080, -1080, -1080, 10279, -1080,
   10531, -1080, -1080, 11791,  4957, -1080, -1080,   633,   633, 11539,
   -1080,   285, -1080, -1080,   708, 12405,   865,   280,   763,   127,
     587,   672,  1622, -1080,   953, -1080,   296, -1080,   938, -1080,
   -1080,   299,   941, -1080,   755,  1307,   930, -1080,   951,   127,
     952, -1080,    37, -1080, -1080, -1080, -1080, 11539,  1279, -1080,
   -1080,   630, -1080, -1080, -1080,  1694, -1080, -1080,  1440, -1080,
   -1080,  4516, -1080, -1080, -1080, 11035,   583, -1080, -1080,  1694,
    4663, -1080, -1080, -1080,   940, -1080, -1080, -1080, 11539, -1080,
     957,   958,  1058, -1080, -1080,   865, 12405, -1080, -1080,  1065,
     980,  4369, -1080, -1080, -1080,  1089,   656,  2593,  2593,   987,
     834,   834, -1080,   791,   967,   646, 10153,   834,   834, -1080,
   -1080,   791, -1080, -1080,   862, -1080,  1104, -1080, -1080, -1080,
   -1080, -1080, -1080,  1066,  1037, -1080, 11161,  1037,    87,   284,
     127,   133,   156,  7766,   512, 11791,  6306,  1085,   763, -1080,
     127,  1037,   137,  9252,    81,   425, -1080,  4810, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080,   834,   834,   661,   834,   834,
     127,   977,   137, -1080, -1080, -1080,   684,  1694, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080,   127, -1080,  1307, -1080,  1392, -1080, -1080, -1080,   127,
   -1080,   979,   984, -1080,  1074,   868,   992, -1080,   996, -1080,
     992, 11539,   957, -1080,  1033, -1080, -1080, -1080,  7766, -1080,
   -1080, -1080, 11539,  1005, -1080,  1005,   998, 11287,  9901,   865,
    9901,   834, 11539, 13735,  9775, 13825, -1080, -1080,  2007,  2007,
     568, -1080,  3718,   470,  1096, -1080,  1041, -1080,  1014, -1080,
   -1080,   800, -1080, -1080,   231, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080,  1041, 11539, 11665, 11665, -1080, -1080,
     834, 11665, 11665, -1080, -1080,  7766, 11791,  1037, -1080, -1080,
    1037, -1080, -1080,  1037, -1080, 11539, -1080,    67, -1080,   179,
    1037,  6306,   512,  1037, -1080, -1080, -1080, -1080, -1080, -1080,
   11539, 11539,   834, 11539, 11539, -1080, 10531, -1080,   127,   170,
   -1080, -1080, -1080,  1003,  1009, -1080,  1694, -1080,  1440, -1080,
   -1080,  1440, -1080,  1440, -1080, -1080,  4957, 12527,    96, -1080,
   -1080,  7036,  4957,  1808,  7615, -1080, -1080,  6306, 11539,  1010,
   -1080, -1080, 11665,  4957,  5861,  6155,   127,   679,   725, -1080,
   -1080, -1080, -1080,  2593,  1107,   165,   127, 12682, -1080,   127,
    1017,  1018, -1080,   387,  1029, -1080, -1080,  1124, -1080,  2593,
    2007,  2007,   568,   180,   526,  3895,  3895, -1080,  4957, -1080,
   -1080, -1080, -1080, 11665, -1080, -1080, -1080, -1080, -1080,    96,
   -1080, -1080,  3895, -1080, -1080, 11413,  6608, -1080,  1037, -1080,
   -1080, 11539,  1038,  1027,  6306,  7615, -1080, -1080,  1392,  1392,
     992,  1054,   992,   992,  1143, -1080,  1093,   103,   132,   173,
    6306,  1192,   868, -1080,   127,  1072,  1079,  1071, 12649, -1080,
    1076, -1080,  1077,  1080, -1080, -1080, -1080,  1081,   727,    62,
   -1080, -1080,    73,  1041,  1083, -1080,  2593, -1080, -1080, -1080,
     127,  1094, -1080,  2593, -1080, -1080, -1080,   639, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080,   127,   127,   127,
     127,   127,   127, -1080, -1080,  6457,    96,   901,   243, -1080,
   -1080, -1080, 11539, -1080,   720, -1080, -1080,  1317,  1037,  1064,
    7892,  1009, -1080,  1440, -1080, -1080, -1080,   183, 13915,  9775,
   14005,  1046, -1080, -1080,  1116, -1080, 12649,  1694, -1080, -1080,
    1155,  1148,   630, -1080,  1694, -1080,  1440, -1080, -1080,  1103,
    2593, -1080,  1110, -1080,   415, -1080,   387,  1041, -1080, -1080,
     816, -1080, -1080, -1080, -1080, -1080, -1080,   862, -1080, 11791,
   11791, 12768, -1080, -1080, -1080, -1080, -1080,   767, -1080, -1080,
   -1080, -1080,  1092,   992,    77,    89,   127,   198,   210, -1080,
   -1080,  1148, -1080,  1112,  1118, -1080, 14095, -1080,   868,  1120,
   -1080,  1121,  1120,  2593,  1122, 12768, -1080, -1080, -1080, -1080,
    6759, -1080, -1080, -1080,  1126,  1317, -1080, -1080, -1080,   221,
    1694, -1080,  1440, -1080,  1102,  1105, -1080,  1440, -1080,  1440,
   -1080, -1080,  1122,  2593, -1080, -1080,   370,  2593, -1080,  1120,
    1127,  1120,  1120, -1080, -1080, -1080,  1122, -1080,  1440, -1080,
   -1080, -1080,  1120, -1080
};

  /* YYDEFACT[STATE-NUM] -- Default reduction number in state STATE-NUM.
     Performed when YYTABLE does not specify something else to do.  Zero
     means the default is an error.  */
static const yytype_int16 yydefact[] =
{
       2,     0,     0,     1,     0,   371,   372,   373,     0,   364,
     365,   366,   369,   367,   368,   370,   359,   360,   361,   362,
     382,   293,   293,   647,   646,   648,   649,   761,     0,   761,
       0,     0,   651,   650,   652,   743,   745,   643,   642,   744,
     645,   637,   638,   639,   640,   588,   657,   658,     0,     0,
       0,     0,     0,     0,   320,   773,   773,   103,   439,   608,
     608,   610,   612,     0,     0,     0,     0,     0,     0,     0,
       3,   759,     6,     9,    35,    40,   668,   668,    52,    75,
     293,    74,     0,    91,     0,    95,   105,     0,    65,   243,
     262,     0,   318,     0,     0,    71,    71,   759,     0,     0,
       0,     0,   329,   340,    76,   338,   307,   308,   587,   589,
     309,   310,   311,   313,   312,   314,   586,   627,   628,   585,
     635,   653,   654,   315,     0,   316,    79,     5,     8,   184,
     195,   185,   208,   181,   201,   191,   190,   211,   212,   206,
     189,   188,   183,   209,   213,   214,   193,   182,   196,   200,
     202,   194,   187,   203,   210,   205,   204,   197,   207,   192,
     180,   199,   198,   179,   186,   177,   178,   174,   175,   176,
     134,   136,   135,   169,   170,   165,   147,   148,   149,   156,
     153,   155,   150,   151,   171,   172,   157,   158,   162,   166,
     152,   154,   144,   145,   146,   159,   160,   161,   163,   164,
     167,   168,   173,   139,   141,    28,   137,   138,   140,     0,
       0,     0,     0,     0,     0,     0,   608,     0,   288,     0,
     271,   298,    89,   292,   773,     0,   653,   654,     0,   316,
     773,   737,    90,   761,    87,     0,   773,   459,    86,   761,
     762,     0,     0,    23,   255,     0,    10,     0,   359,   360,
     332,   460,     0,   237,     0,   329,   238,   228,   229,   326,
       0,    21,     0,     0,   759,    17,    20,   761,    93,    16,
     322,   761,     0,   766,   766,   272,     0,     0,   766,   735,
     761,     0,     0,     0,   668,   668,   101,   363,     0,   111,
     112,   119,   440,   632,   631,   633,   630,     0,   629,     0,
       0,     0,   595,   604,   600,   606,   636,    56,   249,   250,
     769,   770,     4,   771,   760,     0,     0,     0,     0,     0,
       0,     0,   690,     0,   667,     0,   690,   663,     0,     0,
     374,   464,   453,    80,   468,   337,   375,   468,   449,   773,
     107,     0,    99,    96,   773,    61,     0,     0,     0,     0,
       0,   265,   266,     0,     0,     0,     0,   226,   227,     0,
       0,    57,     0,   263,   264,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   755,   756,     0,   773,     0,
       0,    70,     0,     0,     0,     0,     0,   759,   347,   760,
       0,   393,   392,     0,     0,   653,   654,   316,   129,   130,
       0,     0,   132,   661,     0,   653,   654,   316,   355,   204,
     197,   207,   192,   174,   175,   176,   134,   135,   733,    67,
      66,   732,     0,    88,   758,   757,     0,   339,   590,   773,
     773,   142,   740,   326,   299,   742,   295,     0,   773,     0,
       0,   289,   297,     0,   773,     0,   773,   773,     0,   290,
     761,     0,   331,   294,   691,   761,   284,   773,   761,   773,
     283,   761,   336,    55,    25,    27,    26,     0,   333,     0,
       0,     0,     0,     0,   773,    19,     0,   761,   324,    15,
     760,    92,   761,   321,   327,   768,   767,   273,   768,   275,
     328,   736,     0,   118,   636,   109,   104,   667,     0,     0,
     773,     0,   441,   614,   634,   617,   615,   609,   591,   592,
     611,   593,   613,     0,     0,     0,     0,     0,   772,     7,
      29,    30,    31,    32,    33,    53,    54,   697,   694,   693,
     692,   695,   703,   712,   691,     0,   724,   713,   728,   727,
     723,   773,   689,   761,   761,   696,   698,   699,   701,   675,
     705,   710,   773,   716,   406,   405,   721,   675,   726,   675,
       0,   673,     0,     0,   773,     0,   675,     0,     0,     0,
     465,   464,    81,     0,   469,     0,   269,     0,   270,   761,
       0,    97,   108,     0,    62,   235,   242,   244,   245,   246,
     253,   254,   247,   248,   224,   225,   251,   252,    58,   761,
     239,   240,   241,   230,   231,   232,   233,   234,   267,   268,
     746,   748,   747,   749,   458,   293,   456,   761,   773,   746,
     748,   747,   749,   457,   293,     0,   384,     0,   383,     0,
       0,     0,     0,   345,     0,   326,     0,   773,     0,    71,
     353,   129,   130,   131,   659,   351,     0,   773,     0,     0,
       0,   753,   754,    68,   746,   747,   293,     0,     0,     0,
       0,     0,   739,     0,   300,   296,   773,   746,   747,   761,
     746,   747,     0,     0,   738,   330,   763,   278,   285,   280,
     287,   335,    24,     0,   256,    11,    34,     0,   773,     0,
      22,    94,    18,   323,   766,     0,   102,   750,   117,   761,
     746,   747,   690,   618,     0,   594,     0,   597,     0,   602,
     599,     0,     0,   603,   236,     0,   404,   396,   398,   761,
     401,   394,     0,   672,   731,   664,   666,     0,     0,   682,
     704,     0,   671,   714,   715,     0,   685,   725,     0,   687,
     729,   258,   381,   357,   376,   773,   773,   577,   669,     0,
     260,   358,   462,   466,     0,   463,   470,   448,     0,    36,
     303,     0,    39,   302,   106,   100,     0,    51,    41,    49,
       0,   276,   298,   215,    37,     0,   316,     0,     0,     0,
     773,   773,   455,    84,     0,   461,   285,   773,   773,   282,
     454,    82,   281,   319,   773,   385,   773,   343,   387,    72,
     386,   344,   479,     0,     0,   378,     0,     0,   750,   325,
     761,   746,   747,     0,     0,     0,     0,   129,   130,   133,
     761,     0,   761,     0,   450,    77,    42,   276,   216,    48,
     223,   143,   741,   301,   291,   773,   773,   461,   773,   773,
     761,   773,   761,   222,   274,   110,   461,   690,   442,   445,
     619,   623,   624,   625,   616,   626,   596,   598,   605,   601,
     607,   761,   403,     0,   700,     0,   730,   717,   674,   761,
     702,   675,   675,   711,   716,   773,   675,   722,   675,   699,
     675,     0,   578,   579,   773,   580,   377,   379,     0,    12,
      14,   584,     0,   773,    78,   773,   306,     0,     0,    98,
       0,   773,     0,     0,   773,     0,   571,   575,     0,     0,
       0,   516,   761,   513,     0,   570,    63,   494,   496,   498,
     501,   551,   556,   557,   558,   561,   562,   563,   564,   565,
     567,   566,   568,   569,    59,     0,     0,     0,    85,   764,
     773,     0,     0,    83,   380,     0,     0,     0,   388,   390,
       0,    73,   480,     0,   349,     0,   472,     0,   348,   461,
       0,     0,     0,     0,   461,   356,   734,    69,   451,   452,
       0,     0,   773,     0,     0,   279,   286,   334,   761,     0,
     620,   395,   397,   399,   402,   665,     0,   678,     0,   680,
     670,     0,   686,     0,   683,   688,   259,     0,     0,   582,
     583,     0,   261,   761,     0,   430,   429,     0,     0,   304,
      38,    50,     0,   277,   746,   747,   761,   746,   747,   559,
     560,   130,   573,     0,   532,   518,   761,   519,   525,   761,
     528,     0,   512,     0,     0,   515,   572,     0,    64,     0,
     554,   555,     0,   500,   499,     0,     0,    60,   257,    47,
     220,    46,   221,     0,    44,   218,    45,   219,   391,     0,
     341,   342,     0,   346,   473,     0,     0,   350,     0,   660,
     352,     0,     0,   433,     0,     0,   443,   621,     0,     0,
     675,   675,   675,   675,     0,   581,     0,   653,   654,   316,
       0,   773,   773,   428,   761,     0,   699,   412,   707,   708,
     773,   719,   412,   412,   410,   467,   471,   305,   461,   761,
     531,   510,   523,   535,   520,   511,     0,   526,   544,   608,
     761,   537,   540,   543,   549,   550,   539,   547,   765,   495,
     497,   552,   553,   574,   509,   505,   608,   761,   761,   761,
     761,   761,   761,    43,   217,     0,     0,   485,   488,   492,
     491,   493,     0,   474,   773,   354,   444,     0,     0,     0,
       0,   400,   679,     0,   676,   681,   684,   326,     0,   773,
       0,   773,    13,   409,     0,   431,     0,   413,   421,   419,
       0,   706,     0,   408,     0,   424,     0,   426,   517,   521,
       0,   527,   529,   533,     0,   514,   538,   542,   548,   546,
       0,   502,   503,   504,   506,   507,   508,   773,   481,     0,
       0,   489,   475,   477,   478,   476,   437,   761,   435,   438,
     447,   446,     0,   675,   750,   325,   761,   746,   747,   576,
     432,   718,   411,   412,   412,   326,     0,   709,   773,   412,
     720,   412,   412,     0,   524,     0,   545,   541,   536,   389,
       0,   486,   487,   490,   528,     0,   434,   622,   677,   461,
       0,   416,     0,   418,   750,   325,   407,     0,   425,     0,
     422,   427,   522,     0,   530,   534,   773,     0,   436,   412,
     412,   412,   412,   483,   484,   482,   529,   417,     0,   414,
     420,   423,   412,   415
};

  /* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
   -1080, -1080, -1080,  1025, -1080,    23,   748,  -318, -1080,   -40,
   -1080,   774, -1080,    69, -1080,   295,  -537,   -44, -1080, -1080,
   -1080, -1080, -1080, -1080,   455,   -42,   -24, -1080,    42,   -88,
   -1080, -1080,   -20, -1080,  -298,  1078,    15,  1176,  -140,    36,
     -62, -1080,  -428,   -27,  2306,  -384,  1179,   -55,   -23, -1080,
   -1080,    13, -1080,  3243, -1080,  1187, -1080,   602,  2959, -1080,
     671,    44,   624,  -350,   189,    25, -1080,  -277,  -191,    71,
   -1080,  -514,    49, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080,   877, -1080, -1080, -1080, -1080, -1080, -1080, -1080,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080, -1080,   540, -1080,
    -440,  1527,  -376, -1080,    78,  -784, -1080,  -804,  -786,   574,
     428,  -714,   291,   194, -1080, -1080,   397, -1080,  -795, -1080,
      40,   242, -1080, -1080, -1080, -1080, -1080, -1080,   480, -1080,
   -1080,  -104,   735, -1080, -1080, -1080,   971, -1080, -1080, -1080,
   -1080,  -767, -1080,    33, -1080, -1080, -1080, -1080, -1080, -1080,
    -343, -1080, -1080, -1080, -1080,   271, -1080, -1080,  -945, -1080,
     250,  -217, -1051, -1079,  -743,  -118, -1080,   119, -1080, -1080,
   -1080,   120, -1080,  -721,   281, -1080, -1080,   149, -1080, -1080,
     233,   443,   759, -1080,  1214,  1373,  1700,  1769, -1080,   811,
    2085, -1080,  2192,  2445, -1080, -1080,   -57, -1080, -1080,  -147,
   -1080, -1080, -1080, -1080, -1080, -1080, -1080,     1, -1080, -1080,
   -1080, -1080,     0,  2191,    -2,  1226,  2488,  1900, -1080, -1080,
    1253,   666,     9, -1080,  -304,  -464,  -289,  -268, -1053,  -394,
    -245,  -646,  -203,  -461,   601,   157, -1080, -1080,  -691, -1080,
    -705,  -700,  -991,   168,  -549, -1080,  -584, -1080,  -476,  -527,
   -1080, -1080, -1080,   161,  -410, -1080,  -335, -1080, -1080,   -85,
   -1080,   -52,   -13,   968,  -532,   222,  -254,   -37,    -3,     8
};

  /* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
      -1,     1,     2,    70,    71,    72,   246,   563,  1001,   564,
     264,   265,   476,   266,   467,    74,   768,    75,   598,   778,
    1047,   584,   777,  1038,   419,    76,    77,   823,   382,   384,
     385,   951,    78,    79,   572,   252,    81,    82,   267,    83,
      84,    85,   496,    86,   219,   402,   403,   203,   204,   205,
     659,   613,   207,    88,   469,   373,    89,   577,   221,   272,
     773,   614,   790,   455,   456,   234,   235,   223,   441,   618,
     762,   763,    90,   380,   271,   482,   683,   803,   634,   816,
     814,   649,   254,    92,    93,    94,    95,    96,    97,    98,
      99,   100,   101,   334,   337,   745,   888,   806,   945,   946,
     743,   255,   627,   799,   947,   948,   394,   717,   718,   719,
     720,   541,  1232,  1178,  1179,  1094,  1004,  1005,  1072,  1217,
    1218,   103,   292,   502,   702,   979,   848,  1076,   338,   104,
     105,   335,   569,   570,   753,   893,   573,   574,   756,   895,
     957,   807,  1215,   804,   952,  1062,  1250,  1285,  1146,  1147,
    1113,   917,   918,  1045,  1046,   919,  1033,  1023,  1026,  1027,
    1028,  1029,  1030,  1192,  1031,  1120,  1121,  1122,  1123,  1124,
    1125,  1126,   920,   921,   922,   923,   924,   746,   884,   998,
     890,   106,   107,   108,   109,   110,   111,   112,   513,   706,
     113,   515,   114,   115,   514,   516,   297,   300,   301,   507,
     704,   703,   850,   980,  1077,  1160,   854,   116,   117,   298,
     118,   119,   120,   226,   227,   123,   228,   229,   645,   815,
     323,   324,   325,   326,   868,   729,   543,   458,   545,   546,
     879,   548,   566,   550,   551,  1099,  1100,   552,   553,   554,
     555,   556,  1101,  1102,   557,   558,   559,   560,   561,   723,
     422,   650,   277,   459,   231,   126,   687,   616,   653,   648,
     426,   312,   451,   452,   785,  1035,   487,   628,   389,   269
};

  /* YYTABLE[YYPACT[STATE-NUM]] -- What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule whose
     number is the opposite.  If YYTABLE_NINF, syntax error.  */
static const yytype_int16 yytable[] =
{
     122,   427,   122,   299,   296,   243,   379,   629,   386,   208,
     127,   568,   950,   263,   241,   284,   244,   643,   542,   425,
     489,   206,   542,   343,   491,   732,   875,   128,   617,   208,
     286,   218,   218,   285,   313,   877,   953,   565,   674,   449,
     874,   206,   615,   284,   624,   388,   420,   238,   307,   381,
     381,   122,   122,   381,   544,   290,   284,   284,   284,   982,
     313,   285,   625,   275,   279,   798,   237,   306,   314,   774,
     696,    73,   206,    73,   285,   285,   285,   547,   674,   984,
     626,   547,   870,   290,   327,   327,   329,   224,   224,   268,
    -124,   656,   330,   736,   669,   739,   396,   406,   406,   406,
    1137,  1140,  -126,   870,  1219,   333,   287,   330,   615,   626,
     624,  1244,  -124,  -647,   206,  -120,  -120,   218,  -121,   549,
     826,   829,   477,   270,   332,  1189,   273,   503,   751,  -655,
    -656,  -128,  -646,   755,   287,   757,   383,   837,   688,   387,
     445,   721,  -647,  -127,   872,  -121,   538,   287,   287,   287,
     699,   878,   765,   510,   512,     3,  -120,  -121,  -123,   437,
    1254,  -646,  1037,   665,  1272,   872,   688,   846,   259,   473,
     539,   665,   263,   224,   320,   321,   708,   230,   230,   871,
     504,  -125,   505,   506,   245,   793,  -128,  1019,  1020,   310,
     797,   311,   801,  1240,  1274,   461,  -127,   463,  1286,  1074,
     871,   579,  1219,   501,  -122,   222,   232,   471,  1095,   331,
     122,  -123,   479,  1065,   236,   240,   274,   278,   310,  1190,
     311,   495,   263,  -125,   331,  -746,   247,   313,  -124,   709,
    -124,   883,   442,  -115,  -122,  -746,  -111,  -747,   442,  -112,
    -126,   284,  -126,   122,   460,   122,   866,   310,   218,   311,
     218,   218,  -119,   127,  -120,   866,  -120,   712,   122,   285,
     122,   480,   819,   230,  -118,  1037,  1253,   492,   449,  1240,
     674,   381,   381,   381,   381,   982,   525,   526,   959,  -114,
     240,   290,   494,  -121,  1114,  -121,   877,   810,   964,  1092,
     240,   423,  1161,  1075,   329,   237,   332,   820,   268,  1174,
     284,   263,  -116,   688,   224,  -115,   224,   457,   638,  -126,
     713,  -535,   122,   688,    73,   581,   302,   122,   285,  1131,
    1132,   721,   862,   122,  -128,  -113,  -128,   122,  1042,   475,
    -761,   821,   287,   240,  -127,   633,  -127,   519,   899,   122,
     290,   381,  1127,  1037,   630,   870,   632,   578,   990,  -123,
     313,  -123,   578,  -773,  1127,  1127,   640,   520,   521,   522,
     523,  -125,   503,  -125,   954,   503,   707,   958,   707,   303,
    1213,  1127,  -122,  1193,  -122,   322,  1043,   268,   122,  1044,
    1032,   965,   122,    73,   122,   887,   460,  1214,   524,  1211,
     485,   287,   622,   218,   230,   802,   230,   486,   542,  1049,
    1051,   665,  1081,   665,  1054,  1056,  -115,   987,   989,   304,
     840,   -92,   992,   849,   994,  -655,   995,   505,   506,  1103,
     505,   506,   450,   623,   453,   733,  -115,   631,  -746,  -115,
    -117,  -106,  -747,  -115,   916,   934,   495,   578,   578,   622,
     844,   305,  -120,   734,   682,  1118,   578,  1193,   856,   447,
    -648,   859,   460,   336,   578,   578,   284,   547,   622,   218,
     869,   474,  -111,  1092,   533,   442,   503,   442,   208,   721,
    1092,   721,  1092,   339,   285,   834,  -128,  1238,   122,  -648,
     206,   503,   578,   547,  1108,   938,   622,   340,  -127,   623,
     547,   874,  1283,   943,  1111,   537,   290,  1115,   963,  1119,
    1193,   239,  1275,   344,   547,  1127,   240,  1060,   460,   665,
    1061,  1134,   375,  1063,   622,   218,  1143,   623,   495,   508,
    1067,   505,   506,  1070,   346,   641,  1022,  1104,   748,   642,
    1275,   870,   876,  -649,  1193,   880,   505,   506,   284,  1246,
     376,   424,  1098,   542,  -656,   623,  1092,   287,  1092,   724,
     430,   813,   474,  1092,  1016,  1092,   285,   759,   978,   857,
     724,  -651,  -649,   769,   857,  1173,   674,  -363,   615,  1025,
     624,  -121,   747,  1183,  1092,   503,   511,  -128,   290,  -127,
     838,  1066,  -650,   370,   371,   372,   779,   794,   787,   796,
    -651,  -112,   886,  1234,   800,  -363,  -363,  -119,   887,  -118,
    1241,   666,   547,    60,   784,  -652,   822,  -123,  1204,  1205,
    1206,  -650,  -114,  -641,  -761,  -123,  1162,  1164,  1165,  1166,
     641,   870,  1090,   240,  1021,   122,   442,   122,  1155,   287,
     505,   506,   792,   845,  -652,  -114,   831,   769,   769,   842,
     783,  -644,  -641,  1068,  -363,   460,   694,   122,   761,   791,
     834,   622,   218,   284,   761,   460,   784,   429,  1133,   782,
     208,   622,   218,   310,   792,   311,   431,   464,   789,   346,
    -644,   285,   206,  1198,   442,  -761,  1280,   465,   466,   240,
    1109,   825,   623,  1145,   721,   721,   784,   239,   532,  1106,
     905,  1199,   623,   290,  1259,   503,   792,  -116,   433,   665,
     789,  1104,  1025,  1025,   495,   651,   866,   533,  1104,  -113,
    1104,  1266,   438,  -114,   351,   352,  1098,   439,  1220,  1148,
     968,  1098,   788,  1098,   284,  1098,   368,   369,   370,   371,
     372,   782,   789,  -114,   652,   887,  -114,   805,   537,   538,
    -114,   547,   285,   440,   287,   940,   446,   839,   509,  -125,
     505,   506,  -743,   885,   891,   430,  1158,   448,  1096,  1258,
     972,   363,   364,   539,   290,   320,   321,   468,   420,  -116,
    1208,   381,  -122,   960,  -125,   933,   933,   961,   787,   233,
    1197,   236,  -128,  1080,  1104,  1082,  1104,  -122,   578,   578,
    1083,  1104,  -113,  1104,  -641,   578,   578,   784,  -116,  1098,
    1097,  1098,   949,   478,   949,  -123,  1098,   784,  1098,   503,
    -113,   122,  1104,   375,   122,   287,   761,   503,  -116,  1226,
     -91,  -116,  -641,  -641,   788,  -116,   940,  1098,  1138,  1141,
    -113,  -325,   346,  -113,   493,   688,   206,  -113,   375,   841,
     484,   376,   377,   578,   578,  1150,   578,   578,  1000,   442,
     490,  -125,   322,  -122,   488,   498,   375,   962,   517,  -325,
    -325,   486,   705,   562,   505,   506,   376,   443,  1025,  -743,
     508,  -641,   505,   506,  -743,   660,   944,   887,   767,    91,
    -662,    91,   503,   724,   376,   472,   122,  -744,  1040,  1041,
     378,   567,   999,   225,   225,   375,   122,   571,   122,   784,
    1034,  1006,   381,  1006,   576,  1058,   933,   933,  -325,   578,
     933,  -747,   460,  1255,   582,   444,   769,   769,   622,   218,
     240,   769,   769,   376,   499,  1069,   518,  1139,  1142,  -644,
      91,    91,  1096,   444,   288,   508,   635,   505,   506,  1096,
    1246,  1209,  1210,   122,  1151,   225,   583,   841,   578,   623,
     497,   497,   767,   767,   639,   284,   644,  -644,  -644,   122,
    1223,  1091,   288,   676,  1105,  1073,   678,  1010,  -653,  1011,
     225,   225,   500,   285,   225,   393,   404,   404,   680,   225,
     578,  1239,   527,  1242,   528,   529,   530,   531,  1059,   685,
    1073,   686,   769,   727,  -654,  1088,  -653,  -653,  -106,   122,
    -316,  1170,   122,   784,  -744,   122,  -644,   851,   852,  -744,
     853,   695,   503,   784,   722,  1096,   784,    46,    47,   915,
     915,   933,  -654,  -654,   375,   933,  1154,   728,  -316,  -316,
     784,   657,   658,   769,   731,  1159,   735,   933,   933,   933,
     661,   737,   738,   933,   933,  -653,   287,   742,   672,   673,
    1171,   740,   376,   636,   744,   749,   284,  1279,   375,  1281,
     933,   752,  1194,  -326,   122,   710,  1282,   505,   506,   766,
     786,  -654,   122,   122,   285,   795,   689,  -316,   802,  1200,
      80,  1073,    80,   805,   894,  1292,   376,   646,   122,    91,
     858,  -326,  -326,   860,    80,    80,   406,   863,   865,   891,
     724,   637,   898,  -298,   897,  1207,   901,  1034,   724,   900,
     225,  -750,   225,   225,   933,   935,   225,   939,   225,   887,
    1222,   933,    91,   976,    91,   986,   734,   784,   784,   784,
     988,    80,    80,   997,   284,   647,  1003,    91,   991,    91,
    -326,  1037,   993,   122,  -299,  1039,    80,   287,  1036,  1078,
     915,   915,   285,  -750,   915,  1079,  -300,   375,   122,  1110,
     288,   375,   949,  1116,  1117,   381,   381,    41,    42,    43,
      44,    80,    80,  1128,   406,    80,   906,   460,  1157,   747,
      80,  -750,  -750,   622,   218,   376,   903,  1156,   933,   376,
    1168,    91,   225,   225,   225,   225,    91,   225,   225,  1167,
    1163,   886,    91,  1175,  1256,  1176,    91,   462,  1221,   933,
    1276,  1235,  1257,   784,   623,   949,   375,  1177,    91,   288,
     925,   925,  1182,  1184,   690,   287,  1186,  -301,  -750,  1191,
    -750,   767,   767,  -746,   904,   481,   767,   767,  1169,   483,
    1196,   933,   346,   933,   376,  1236,   724,  1230,   122,  1243,
    -746,  1251,  1252,  -747,   692,   225,  1245,    91,  1260,   359,
     360,    91,   225,    91,  1262,   915,  1267,  1269,  1273,   915,
     470,   933,  1277,  1288,   391,   933,   374,   225,   967,   408,
     824,   915,   915,   915,   949,  1249,   889,   915,   915,   861,
      80,   983,  1007,   647,  1093,  1278,  1185,  1187,   367,   368,
     369,   370,   371,   372,   915,   969,   754,   767,   575,  1284,
    1130,    80,  1149,    80,    80,  1247,  1248,    80,  1129,    80,
    1229,   225,   428,    80,  1172,    80,   711,   421,   828,   830,
     328,   527,   873,   528,   529,   530,   531,   532,    80,  1237,
      80,     0,  1195,   828,   830,  1233,     0,     0,   767,     0,
       0,   925,   925,     0,     0,   925,   533,    91,   915,   527,
     843,   528,   529,   530,   531,   915,     0,     0,     0,  1216,
     534,   528,   529,   530,   531,   288,     0,   225,     0,     0,
     535,     0,   936,   937,     0,     0,   536,   537,   538,   941,
     942,     0,    80,    80,    80,    80,    80,    80,    80,    80,
       0,     0,     0,    80,     0,     0,     0,    80,   715,     0,
       0,     0,   539,     0,   716,   540,     0,     0,   675,    80,
       0,     0,     0,   677,     0,     0,   679,  1261,  1263,   681,
       0,     0,   915,  1268,     0,  1270,  1271,   970,   971,     0,
     973,   974,     0,     0,   527,   691,   528,   529,   530,   531,
     693,     0,     0,   915,   225,     0,    80,   288,    80,     0,
     775,     0,    80,    80,    80,     0,   925,     0,     0,     0,
     925,     0,     0,  1287,  1289,  1290,  1291,     0,    80,     0,
       0,     0,   925,   925,   925,   915,  1293,   915,   925,   925,
       0,     0,   527,   715,   528,   529,   530,   531,   532,     0,
       0,     0,     0,  1012,    91,   925,    91,     0,     0,     0,
       0,   725,   726,     0,   225,   915,     0,   533,     0,   915,
       0,     0,    80,     0,   225,     0,    91,   225,   346,   102,
       0,   102,     0,   346,   775,   775,   926,   926,     0,     0,
       0,   535,  1053,   102,   102,   359,   360,   764,   537,   538,
     359,   360,   315,   316,   317,   318,   319,     0,    80,   925,
     225,     0,     0,     0,     0,     0,   925,     0,     0,     0,
       0,     0,   288,   539,  1071,     0,     0,     0,    80,     0,
     102,   102,   365,   366,   367,   368,   369,   370,   371,   372,
     368,   369,   370,   371,   372,   102,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  1050,  1052,     0,
       0,     0,  1055,  1057,     0,     0,     0,     0,     0,     0,
     102,   102,     0,     0,   102,     0,     0,     0,     0,   102,
       0,     0,     0,   925,     0,     0,     0,     0,     0,     0,
       0,  1050,  1052,   288,  1055,  1057,     0,     0,     0,     0,
       0,     0,     0,     0,   925,    80,     0,     0,     0,     0,
       0,    80,     0,     0,     0,     0,     0,   926,   926,     0,
       0,   926,     0,     0,   527,     0,   528,   529,   530,   531,
     532,     0,     0,     0,     0,     0,   925,   864,   925,     0,
      91,     0,   225,    91,     0,     0,     0,     0,     0,   533,
       0,     0,     0,     0,     0,    80,     0,    80,     0,     0,
       0,     0,     0,     0,     0,    80,   925,     0,     0,     0,
     925,     0,     0,   535,  1144,    80,     0,    80,    80,   536,
     537,   538,     0,     0,     0,    80,    80,     0,     0,   102,
       0,     0,  1144,     0,     0,     0,   527,     0,   528,   529,
     530,   531,   532,     0,     0,   539,     0,     0,   540,     0,
     102,    80,   102,   102,     0,    91,   102,     0,   102,     0,
     847,   533,   102,     0,   102,    91,     0,    91,     0,     0,
       0,   225,   926,     0,     0,     0,   926,   102,     0,   102,
     966,     0,     0,     0,     0,   535,     0,     0,   926,   926,
     926,   536,   537,   538,   926,   926,     0,     0,   975,     0,
     977,     0,     0,   775,   775,     0,     0,     0,   775,   775,
       0,   926,    91,   225,     0,     0,     0,   539,     0,   981,
     540,     0,     0,     0,     0,     0,     0,   985,    91,     0,
       0,   102,   102,   102,   102,   102,   102,   102,   102,     0,
       0,     0,   102,     0,     0,     0,   102,     0,     0,     0,
     527,     0,   528,   529,   530,   531,   532,     0,   102,     0,
       0,     0,     0,     0,  1086,   926,     0,     0,    91,     0,
       0,    91,   926,     0,    91,   533,     0,     0,     0,   775,
       0,    80,     0,    80,    80,     0,     0,     0,     0,     0,
       0,     0,   125,     0,   125,   102,     0,   102,     0,   535,
       0,   102,   102,   102,     0,   536,   537,   538,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   102,     0,     0,
     775,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   539,     0,    91,   540,     0,     0,     0,     0,   926,
       0,    91,    91,   125,   125,     0,     0,   291,     0,     0,
       0,   240,     0,     0,     0,     0,    80,    91,     0,     0,
     926,   102,     0,     0,     0,  1181,    80,     0,    80,     0,
       0,     0,    80,     0,     0,   291,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   397,   407,
     407,     0,   926,     0,   926,     0,     0,   102,     0,     0,
       0,     0,  1135,     0,    80,    80,     0,     0,     0,    80,
      80,     0,    91,    80,    80,     0,     0,   102,     0,     0,
       0,     0,   926,     0,     0,     0,   926,    91,     0,    80,
      23,    24,    25,    26,     0,     0,   225,     0,     0,     0,
       0,     0,     0,  1231,     0,     0,    32,    33,    34,     0,
       0,     0,     0,     0,     0,     0,    41,    42,    43,    44,
      45,     0,     0,     0,     0,     0,     0,  1188,     0,    80,
       0,     0,    80,     0,     0,    80,   225,   225,     0,     0,
      80,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   102,  1201,  1202,  1203,     0,     0,
     102,     0,   125,     0,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,    91,     0,     0,
       0,    80,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    80,   125,   282,   125,     0,     0,
     927,   927,    80,    80,   102,     0,   102,     0,     0,     0,
     125,     0,   125,     0,   102,     0,     0,     0,    80,     0,
       0,     0,     0,     0,   102,     0,   102,   102,     0,     0,
       0,     0,     0,   291,   102,   102,     0,     0,     0,     0,
       0,     0,     0,   121,     0,   121,     0,     0,   346,   347,
     348,   349,   350,   351,   352,   353,     0,   355,   356,     0,
     102,     0,     0,     0,   125,   359,   360,     0,     0,   125,
       0,     0,     0,    80,     0,   125,     0,     0,     0,   125,
       0,     0,     0,     0,     0,     0,     0,     0,    80,     0,
       0,   125,   291,   345,   121,   121,     0,    80,   289,     0,
     363,   364,   365,   366,   367,   368,   369,   370,   371,   372,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   289,     0,     0,     0,
     125,   927,   927,     0,   125,   927,   125,    80,    80,   395,
     405,   405,   405,     0,     0,   346,   347,   348,   349,   350,
     351,   352,   353,   354,   355,   356,   357,   358,    87,     0,
      87,     0,   359,   360,     0,     0,     0,     0,   361,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    80,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     102,     0,   102,   102,     0,   362,     0,   363,   364,   365,
     366,   367,   368,   369,   370,   371,   372,     0,     0,    87,
      87,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     125,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   927,     0,   291,     0,
     927,     0,     0,   121,   392,     0,     0,     0,     0,     0,
       0,     0,   927,   927,   927,   102,     0,     0,   927,   927,
       0,     0,     0,     0,     0,   102,     0,   102,     0,     0,
       0,   102,     0,     0,     0,   927,   121,     0,   121,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   121,     0,   121,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   102,   102,     0,     0,     0,   102,   102,
       0,     0,   102,   102,   289,     0,     0,   928,   928,     0,
     291,     0,     0,   776,     0,     0,     0,     0,   102,   927,
     124,     0,   124,     0,     0,     0,   927,     0,     0,     0,
       0,     0,     0,     0,     0,   121,     0,     0,     0,     0,
     121,     0,     0,     0,     0,     0,   121,     0,    87,     0,
     121,     0,     0,     0,     0,     0,     0,   125,   102,   125,
       0,   102,   121,   289,   102,     0,     0,     0,     0,   102,
       0,   124,   124,     0,     0,     0,   929,   929,     0,   125,
       0,    87,     0,    87,     0,     0,     0,   776,   776,     0,
       0,     0,     0,   927,     0,     0,    87,     0,    87,     0,
       0,   121,     0,     0,     0,   121,     0,   121,     0,     0,
     102,     0,     0,     0,   927,     0,     0,     0,     0,     0,
       0,     0,     0,   102,     0,   291,     0,     0,     0,     0,
       0,   102,   102,     0,   855,     0,     0,     0,   928,   928,
       0,     0,   928,     0,     0,     0,   927,   102,   927,     0,
      87,     0,     0,     0,     0,    87,    23,    24,    25,    26,
       0,    87,     0,     0,     0,    87,     0,     0,     0,     0,
       0,     0,    32,    33,    34,   906,   927,    87,     0,   907,
     927,     0,    41,    42,    43,    44,    45,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   291,     0,     0,     0,
       0,   121,   102,     0,     0,     0,     0,   929,   929,     0,
       0,   929,     0,   908,   909,     0,    87,   102,     0,   289,
      87,   910,    87,     0,   911,     0,   102,   912,   913,     0,
     124,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,   125,     0,     0,   125,     0,     0,     0,
       0,     0,     0,   928,     0,   914,     0,   928,     0,     0,
       0,     0,   282,   124,     0,   124,   102,   102,     0,   928,
     928,   928,     0,     0,     0,   928,   928,     0,   124,     0,
     124,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   928,     0,     0,     0,     0,     0,     0,     0,
       0,   289,     0,     0,     0,     0,     0,   102,     0,     0,
       0,     0,     0,     0,     0,     0,    87,     0,   125,     0,
       0,     0,   929,     0,     0,     0,   929,     0,   125,     0,
     125,     0,   124,     0,     0,     0,     0,   124,   929,   929,
     929,     0,     0,   124,   929,   929,   928,   124,   121,     0,
     121,     0,     0,   928,     0,     0,     0,     0,     0,   124,
       0,   929,     0,     0,     0,     0,   776,   776,     0,     0,
     121,   776,   776,     0,     0,   125,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   125,   930,   930,     0,     0,     0,     0,   124,     0,
       0,     0,   124,     0,   124,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   929,   289,     0,     0,   770,
     928,     0,   929,     0,     0,     0,     0,  1089,     0,     0,
       0,   125,     0,     0,   125,     0,     0,   125,     0,     0,
       0,   928,   776,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    87,     0,    87,     0,     0,     0,     0,
       0,     0,     0,   928,     0,   928,     0,     0,     0,     0,
       0,     0,     0,   776,     0,    87,     0,   289,     0,   929,
       0,     0,     0,   770,   770,     0,   125,     0,   124,   931,
     931,     0,     0,   928,   125,   125,     0,   928,     0,     0,
     929,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     125,     0,     0,   930,   930,     0,     0,   930,   407,     0,
       0,     0,     0,     0,   121,     0,     0,   121,     0,     0,
       0,     0,   929,  -773,   929,   276,     0,     0,     0,     0,
       0,  -773,  -773,  -773,     0,     0,  -773,  -773,  -773,     0,
    -773,     0,     0,     0,     0,     0,     0,     0,  -773,  -773,
    -773,     0,   929,     0,     0,   125,   929,     0,     0,     0,
    -773,  -773,     0,  -773,  -773,  -773,  -773,  -773,     0,     0,
     125,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   124,     0,     0,     0,     0,   407,     0,     0,   121,
       0,  -773,     0,     0,     0,     0,     0,     0,     0,   121,
       0,   121,     0,     0,     0,     0,     0,     0,     0,     0,
     931,   931,     0,     0,   931,     0,     0,     0,   930,  -773,
    -773,     0,   930,     0,     0,   124,     0,   124,     0,    87,
       0,     0,    87,     0,   930,   930,   930,     0,     0,     0,
     930,   930,     0,  -773,     0,     0,   121,   124,     0,     0,
       0,     0,     0,     0,     0,   124,   124,   930,     0,     0,
     125,     0,   121,     0,     0,     0,  -773,  -773,     0,     0,
       0,   236,  -773,     0,  -773,     0,  -773,     0,     0,   432,
       0,     0,   434,   435,   436,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  1087,     0,
       0,     0,   121,     0,    87,   121,     0,     0,   121,     0,
       0,   930,     0,     0,    87,     0,    87,     0,   930,     0,
       0,     0,     0,     0,     0,   931,     0,     0,     0,   931,
       0,     0,   932,   932,     0,     0,     0,     0,     0,     0,
       0,   931,   931,   931,     0,     0,     0,   931,   931,     0,
       0,     0,   770,   770,     0,     0,     0,   770,   770,     0,
       0,    87,     0,     0,   931,     0,     0,   121,     0,   220,
     220,     0,     0,     0,     0,   121,   121,    87,     0,     0,
       0,     0,     0,     0,     0,   930,     0,     0,     0,     0,
       0,   121,     0,     0,     0,     0,     0,     0,     0,   405,
       0,   253,   256,   257,   258,     0,   930,     0,   220,   220,
       0,   124,     0,  1085,   124,     0,     0,    87,   931,     0,
      87,   308,   309,    87,     0,   931,     0,     0,   770,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   930,     0,
     930,     0,     0,     0,     0,     0,   121,     0,     0,     0,
       0,     0,     0,     0,     0,   220,     0,     0,     0,     0,
       0,   121,     0,   932,   932,     0,     0,   932,   930,   770,
       0,     0,   930,     0,     0,     0,     0,   405,     0,     0,
       0,     0,    87,     0,     0,     0,   124,     0,     0,     0,
      87,    87,   931,     0,     0,     0,   124,     0,   124,     0,
       0,     0,     0,     0,     0,     0,    87,     0,   662,   664,
       0,     0,     0,   931,     0,     0,     0,   276,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   124,   124,     0,     0,     0,   124,
     124,     0,     0,   124,     0,   931,     0,   931,     0,     0,
       0,   121,     0,     0,   664,     0,     0,   276,     0,   124,
       0,    87,     0,   220,     0,     0,   220,   220,   220,     0,
     308,     0,     0,     0,     0,   931,    87,     0,   932,   931,
       0,     0,   932,     0,     0,     0,   220,     0,   220,   220,
       0,     0,     0,     0,   932,   932,   932,     0,     0,   124,
     932,   932,   124,     0,     0,   124,     0,     0,     0,     0,
     124,     0,     0,     0,     0,     0,     0,   932,     0,   730,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   760,     0,     0,     0,
       0,   124,   772,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   124,     0,    87,     0,     0,     0,
       0,   932,   124,   124,     0,     0,     0,     0,   932,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   124,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   585,
     586,   587,   588,   589,     0,     0,   590,   591,   592,   593,
     594,   595,   596,   597,     0,   599,     0,     0,   600,   601,
     602,   603,   604,   605,   606,   607,   608,   609,     0,   832,
       0,   220,   833,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   124,     0,   932,     0,   664,     0,   276,
       0,     0,     0,     0,     0,     0,     0,     0,   124,     0,
       0,     0,     0,     0,     0,     0,   932,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   220,   220,     0,     0,   867,   220,   932,     0,
     932,   220,     0,     0,     0,     0,     0,   258,     0,     0,
       0,     0,     0,     0,   882,     0,     0,     0,     0,     0,
       0,     0,   684,     0,     0,     0,     0,   896,   932,     0,
       0,     0,   932,     0,     0,     0,     0,     0,   220,     0,
       0,   220,     0,     0,     0,     0,     0,     0,   124,     0,
       0,     0,     0,   220,     0,   664,     0,     0,     0,     0,
       0,    23,    24,    25,    26,     0,     0,     0,     0,     0,
     714,     0,     0,     0,     0,   956,     0,    32,    33,    34,
     906,     0,     0,     0,   907,     0,     0,    41,    42,    43,
      44,    45,     0,     0,     0,   346,  -774,  -774,  -774,  -774,
     351,   352,     0,   220,  -774,  -774,     0,     0,     0,     0,
       0,     0,   359,   360,     0,   741,     0,     0,   908,   909,
     750,     0,     0,     0,     0,     0,   910,     0,     0,   911,
     220,     0,   912,   913,     0,  1024,   771,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,   363,   364,   365,
     366,   367,   368,   369,   370,   371,   372,     0,     0,     0,
     914,     0,     0,     0,     0,     0,  1009,   282,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   240,     0,     0,     0,     0,     0,     0,     0,     0,
     220,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     220,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     827,   827,     0,   220,   827,     0,   220,     0,     0,     0,
       0,     0,     0,     0,  1064,   827,   827,     0,     0,     0,
       0,   220,     0,   220,     0,     0,     0,     0,    23,    24,
      25,    26,   827,     0,     0,   276,     0,     0,     0,     0,
       0,     0,     0,     0,    32,    33,    34,   906,     0,     0,
       0,   907,     0,  1118,    41,    42,    43,    44,    45,     0,
       0,     0,     0,     0,     0,     0,     0,  1107,     0,     0,
     220,     0,   533,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   908,   909,     0,   220,     0,
       0,     0,     0,   910,     0,     0,   911,     0,     0,   912,
     913,   220,  1024,   537,     0,    58,    59,  1136,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  1153,     0,     0,   914,     0,   220,
       0,     0,     0,     0,   282,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   220,
       0,     0,     0,  -773,     4,     0,     5,     6,     7,     8,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
       0,     0,     0,    20,    21,    22,    23,    24,    25,    26,
       0,     0,    27,     0,     0,     0,     0,     0,    28,    29,
      30,    31,    32,    33,    34,    35,    36,    37,    38,    39,
      40,  1212,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,   996,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,  1002,     0,     0,     0,     0,
     220,     0,     0,    50,    51,  1013,     0,   220,     0,     0,
       0,    52,     0,     0,    53,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,  1048,   827,
     827,     0,     0,     0,   827,   827,     0,     0,     0,     0,
       0,     0,    67,    68,    69,     0,     0,     0,   220,     0,
       0,     0,     0,     0,  -773,     0,  -773,     0,     0,     0,
       0,     0,     0,   827,   827,     0,   827,   827,     0,   220,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -750,
       0,     0,     0,     0,     0,     0,     0,  -750,  -750,  -750,
       0,   220,  -750,  -750,  -750,   827,  -750,     0,     0,     0,
       0,     0,     0,     0,  -750,  -750,  -750,  -750,  -750,     0,
       0,     0,     0,     0,     0,     0,  -750,  -750,     0,  -750,
    -750,  -750,  -750,  -750,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   827,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -750,   220,     0,
       0,     0,     0,     0,   827,     0,  -750,  -750,  -750,  -750,
    -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,     0,
       0,     0,     0,  -750,  -750,  -750,  -750,     0,   835,  -750,
       0,     0,     0,     0,     0,  -750,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -750,
       0,     0,  -750,     0,     0,  -124,  -750,  -750,  -750,  -750,
    -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,     0,     0,
       0,     0,  -750,  -750,  -750,  -750,     0,     0,  -750,  -750,
    -750,  -641,  -750,     0,     0,   220,     0,     0,     0,  -641,
    -641,  -641,     0,     0,  -641,  -641,  -641,     0,  -641,     0,
       0,     0,   220,   902,     0,     0,  -641,     0,  -641,  -641,
    -641,     0,     0,     0,     0,     0,     0,     0,  -641,  -641,
       0,  -641,  -641,  -641,  -641,  -641,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   346,   347,   348,   349,
     350,   351,   352,   353,   354,   355,   356,   357,   358,  -641,
       0,     0,     0,   359,   360,     0,     0,     0,  -641,  -641,
    -641,  -641,  -641,  -641,  -641,  -641,  -641,  -641,  -641,  -641,
    -641,     0,     0,     0,     0,  -641,  -641,  -641,  -641,     0,
    -641,  -641,     0,     0,     0,     0,   362,  -641,   363,   364,
     365,   366,   367,   368,   369,   370,   371,   372,     0,     0,
       0,  -641,     0,     0,  -641,  -271,     0,  -641,  -641,  -641,
    -641,  -641,  -641,  -641,  -641,  -641,  -641,  -641,  -641,  -641,
       0,     0,     0,     0,     0,  -641,  -641,  -641,  -644,     0,
    -641,  -641,  -641,     0,  -641,     0,  -644,  -644,  -644,     0,
       0,  -644,  -644,  -644,     0,  -644,     0,     0,     0,     0,
     881,     0,     0,  -644,     0,  -644,  -644,  -644,     0,     0,
       0,     0,     0,     0,     0,  -644,  -644,     0,  -644,  -644,
    -644,  -644,  -644,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   346,   347,   348,   349,   350,   351,   352,
     353,   354,   355,   356,   357,   358,  -644,     0,     0,     0,
     359,   360,     0,     0,     0,  -644,  -644,  -644,  -644,  -644,
    -644,  -644,  -644,  -644,  -644,  -644,  -644,  -644,     0,     0,
       0,     0,  -644,  -644,  -644,  -644,     0,  -644,  -644,     0,
       0,     0,     0,   362,  -644,   363,   364,   365,   366,   367,
     368,   369,   370,   371,   372,     0,     0,     0,  -644,     0,
       0,  -644,     0,     0,  -644,  -644,  -644,  -644,  -644,  -644,
    -644,  -644,  -644,  -644,  -644,  -644,  -644,     0,     0,     0,
       0,     0,  -644,  -644,  -644,  -751,     0,  -644,  -644,  -644,
       0,  -644,     0,  -751,  -751,  -751,     0,     0,  -751,  -751,
    -751,     0,  -751,     0,     0,     0,     0,   892,     0,     0,
    -751,  -751,  -751,  -751,  -751,     0,     0,     0,     0,     0,
       0,     0,  -751,  -751,     0,  -751,  -751,  -751,  -751,  -751,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     346,   347,   348,   349,   350,   351,   352,   353,   354,   355,
     356,   357,   358,  -751,     0,     0,     0,   359,   360,     0,
       0,     0,  -751,  -751,  -751,  -751,  -751,  -751,  -751,  -751,
    -751,  -751,  -751,  -751,  -751,     0,     0,     0,     0,  -751,
    -751,  -751,  -751,     0,     0,  -751,     0,     0,     0,     0,
     362,  -751,   363,   364,   365,   366,   367,   368,   369,   370,
     371,   372,     0,     0,     0,  -751,     0,     0,  -751,     0,
       0,     0,  -751,  -751,  -751,  -751,  -751,  -751,  -751,  -751,
    -751,  -751,  -751,  -751,     0,     0,     0,     0,  -751,  -751,
    -751,  -751,  -752,     0,  -751,  -751,  -751,     0,  -751,     0,
    -752,  -752,  -752,     0,     0,  -752,  -752,  -752,     0,  -752,
       0,     0,     0,     0,   902,     0,     0,  -752,  -752,  -752,
    -752,  -752,     0,     0,     0,     0,     0,     0,     0,  -752,
    -752,     0,  -752,  -752,  -752,  -752,  -752,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   346,   347,   348,
     349,   350,   351,   352,   353,   354,   355,   356,   357,   358,
    -752,     0,     0,     0,   359,   360,     0,     0,     0,  -752,
    -752,  -752,  -752,  -752,  -752,  -752,  -752,  -752,  -752,  -752,
    -752,  -752,     0,     0,     0,     0,  -752,  -752,  -752,  -752,
       0,     0,  -752,     0,     0,     0,     0,   362,  -752,   363,
     364,   365,   366,   367,   368,   369,   370,   371,   372,     0,
       0,     0,  -752,     0,     0,  -752,     0,     0,     0,  -752,
    -752,  -752,  -752,  -752,  -752,  -752,  -752,  -752,  -752,  -752,
    -752,     0,     0,     0,     0,  -752,  -752,  -752,  -752,  -325,
       0,  -752,  -752,  -752,     0,  -752,     0,  -325,  -325,  -325,
       0,     0,  -325,  -325,  -325,     0,  -325,     0,     0,     0,
       0,     0,     0,     0,  -325,     0,  -325,  -325,  -325,     0,
       0,     0,     0,     0,     0,     0,  -325,  -325,     0,  -325,
    -325,  -325,  -325,  -325,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   346,   347,   348,   349,   350,   351,
     352,   353,   354,   355,   356,   357,   358,  -325,     0,     0,
       0,   359,   360,     0,     0,     0,  -325,  -325,  -325,  -325,
    -325,  -325,  -325,  -325,  -325,  -325,  -325,  -325,  -325,     0,
       0,     0,     0,  -325,  -325,  -325,  -325,     0,   836,  -325,
       0,     0,     0,     0,   362,  -325,   363,   364,   365,   366,
     367,   368,   369,   370,   371,   372,     0,     0,     0,  -325,
       0,     0,  -325,     0,     0,  -126,  -325,  -325,  -325,  -325,
    -325,  -325,  -325,  -325,  -325,  -325,  -325,  -325,     0,     0,
       0,     0,     0,  -325,  -325,  -325,  -460,     0,  -325,  -325,
    -325,     0,  -325,     0,  -460,  -460,  -460,     0,     0,  -460,
    -460,  -460,     0,  -460,     0,     0,     0,     0,     0,     0,
       0,  -460,  -460,  -460,  -460,     0,     0,     0,     0,     0,
       0,     0,     0,  -460,  -460,     0,  -460,  -460,  -460,  -460,
    -460,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   346,   347,   348,   349,   350,   351,   352,   353,   354,
     355,   356,  -774,  -774,  -460,     0,     0,     0,   359,   360,
       0,     0,     0,  -460,  -460,  -460,  -460,  -460,  -460,  -460,
    -460,  -460,  -460,  -460,  -460,  -460,     0,     0,     0,     0,
    -460,  -460,  -460,  -460,     0,     0,  -460,     0,     0,     0,
       0,     0,  -460,   363,   364,   365,   366,   367,   368,   369,
     370,   371,   372,     0,     0,     0,  -460,     0,     0,     0,
       0,     0,     0,  -460,     0,  -460,  -460,  -460,  -460,  -460,
    -460,  -460,  -460,  -460,  -460,     0,     0,     0,     0,  -460,
    -460,  -460,  -460,  -317,   236,  -460,  -460,  -460,     0,  -460,
       0,  -317,  -317,  -317,     0,     0,  -317,  -317,  -317,     0,
    -317,     0,     0,     0,     0,     0,     0,     0,  -317,     0,
    -317,  -317,  -317,     0,     0,     0,     0,     0,     0,     0,
    -317,  -317,     0,  -317,  -317,  -317,  -317,  -317,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   346,   347,
     348,   349,   350,   351,   352,     0,     0,   355,   356,     0,
       0,  -317,     0,     0,     0,   359,   360,     0,     0,     0,
    -317,  -317,  -317,  -317,  -317,  -317,  -317,  -317,  -317,  -317,
    -317,  -317,  -317,     0,     0,     0,     0,  -317,  -317,  -317,
    -317,     0,     0,  -317,     0,     0,     0,     0,     0,  -317,
     363,   364,   365,   366,   367,   368,   369,   370,   371,   372,
       0,     0,     0,  -317,     0,     0,  -317,     0,     0,     0,
    -317,  -317,  -317,  -317,  -317,  -317,  -317,  -317,  -317,  -317,
    -317,  -317,     0,     0,     0,     0,     0,  -317,  -317,  -317,
    -773,     0,  -317,  -317,  -317,     0,  -317,     0,  -773,  -773,
    -773,     0,     0,  -773,  -773,  -773,     0,  -773,     0,     0,
       0,     0,     0,     0,     0,  -773,  -773,  -773,  -773,     0,
       0,     0,     0,     0,     0,     0,     0,  -773,  -773,     0,
    -773,  -773,  -773,  -773,  -773,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -773,     0,
       0,     0,     0,     0,     0,     0,     0,  -773,  -773,  -773,
    -773,  -773,  -773,  -773,  -773,  -773,  -773,  -773,  -773,  -773,
       0,     0,     0,     0,  -773,  -773,  -773,  -773,     0,     0,
    -773,     0,     0,     0,     0,     0,  -773,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -773,     0,     0,     0,     0,     0,     0,  -773,     0,  -773,
    -773,  -773,  -773,  -773,  -773,  -773,  -773,  -773,  -773,     0,
       0,     0,     0,  -773,  -773,  -773,  -773,  -332,   236,  -773,
    -773,  -773,     0,  -773,     0,  -332,  -332,  -332,     0,     0,
    -332,  -332,  -332,     0,  -332,     0,     0,     0,     0,     0,
       0,     0,  -332,     0,  -332,  -332,     0,     0,     0,     0,
       0,     0,     0,     0,  -332,  -332,     0,  -332,  -332,  -332,
    -332,  -332,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -332,     0,     0,     0,     0,
       0,     0,     0,     0,  -332,  -332,  -332,  -332,  -332,  -332,
    -332,  -332,  -332,  -332,  -332,  -332,  -332,     0,     0,     0,
       0,  -332,  -332,  -332,  -332,     0,     0,  -332,     0,     0,
       0,     0,     0,  -332,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -332,     0,     0,
       0,     0,     0,     0,  -332,     0,  -332,  -332,  -332,  -332,
    -332,  -332,  -332,  -332,  -332,  -332,     0,     0,     0,     0,
       0,  -332,  -332,  -332,  -750,   233,  -332,  -332,  -332,     0,
    -332,     0,  -750,  -750,  -750,     0,     0,     0,  -750,  -750,
       0,  -750,     0,     0,     0,     0,     0,     0,     0,  -750,
    -750,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -750,  -750,     0,  -750,  -750,  -750,  -750,  -750,     0,
       0,     0,     0,     0,     0,     0,   346,   347,   348,   349,
     350,   351,   352,   353,   354,   355,   356,   357,   358,     0,
       0,     0,  -750,   359,   360,     0,     0,     0,     0,     0,
       0,  -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,
    -750,  -750,  -750,  -750,     0,     0,     0,     0,  -750,  -750,
    -750,  -750,     0,   780,  -750,     0,   362,     0,   363,   364,
     365,   366,   367,   368,   369,   370,   371,   372,     0,     0,
       0,     0,     0,     0,  -750,     0,     0,     0,     0,     0,
    -124,  -750,   240,  -750,  -750,  -750,  -750,  -750,  -750,  -750,
    -750,  -750,  -750,     0,     0,     0,     0,  -750,  -750,  -750,
    -115,  -750,     0,  -750,     0,  -750,     0,  -750,     0,  -750,
    -750,  -750,     0,     0,     0,  -750,  -750,     0,  -750,     0,
       0,     0,     0,     0,     0,     0,  -750,  -750,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -750,  -750,
       0,  -750,  -750,  -750,  -750,  -750,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -750,
       0,     0,     0,     0,     0,     0,     0,     0,  -750,  -750,
    -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,
    -750,     0,     0,     0,     0,  -750,  -750,  -750,  -750,     0,
     780,  -750,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -750,     0,     0,     0,     0,     0,  -124,  -750,     0,
    -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,  -750,
       0,     0,     0,     0,  -750,  -750,  -750,  -750,  -325,     0,
    -750,     0,  -750,     0,  -750,     0,  -325,  -325,  -325,     0,
       0,     0,  -325,  -325,     0,  -325,     0,     0,     0,     0,
       0,     0,     0,  -325,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -325,  -325,     0,  -325,  -325,
    -325,  -325,  -325,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  -325,     0,     0,     0,
       0,     0,     0,     0,     0,  -325,  -325,  -325,  -325,  -325,
    -325,  -325,  -325,  -325,  -325,  -325,  -325,  -325,     0,     0,
       0,     0,  -325,  -325,  -325,  -325,     0,   781,  -325,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -325,     0,
       0,     0,     0,     0,  -126,  -325,     0,  -325,  -325,  -325,
    -325,  -325,  -325,  -325,  -325,  -325,  -325,     0,     0,     0,
       0,     0,  -325,  -325,  -117,  -325,     0,  -325,     0,  -325,
       0,  -325,     0,  -325,  -325,  -325,     0,     0,     0,  -325,
    -325,     0,  -325,     0,     0,     0,     0,     0,     0,     0,
    -325,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,  -325,  -325,     0,  -325,  -325,  -325,  -325,  -325,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -325,     0,     0,     0,     0,     0,     0,
       0,     0,  -325,  -325,  -325,  -325,  -325,  -325,  -325,  -325,
    -325,  -325,  -325,  -325,  -325,     0,     0,     0,     0,  -325,
    -325,  -325,  -325,     0,   781,  -325,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -325,     0,     0,     0,     0,
       0,  -126,  -325,     0,  -325,  -325,  -325,  -325,  -325,  -325,
    -325,  -325,  -325,  -325,     0,     0,     0,     0,     0,  -325,
    -325,  -325,     0,     0,  -325,     0,  -325,   260,  -325,     5,
       6,     7,     8,     9,  -773,  -773,  -773,    10,    11,     0,
       0,  -773,    12,     0,    13,    14,    15,    16,    17,    18,
      19,     0,     0,     0,     0,     0,    20,    21,    22,    23,
      24,    25,    26,     0,     0,    27,     0,     0,     0,     0,
       0,    28,    29,   261,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,    52,     0,     0,    53,    54,     0,
      55,    56,     0,    57,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    67,    68,    69,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -773,   260,  -773,
       5,     6,     7,     8,     9,     0,     0,  -773,    10,    11,
       0,  -773,  -773,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,    27,     0,     0,     0,
       0,     0,    28,    29,   261,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,    52,     0,     0,    53,    54,
       0,    55,    56,     0,    57,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    67,    68,    69,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -773,   260,
    -773,     5,     6,     7,     8,     9,     0,     0,  -773,    10,
      11,     0,     0,  -773,    12,  -773,    13,    14,    15,    16,
      17,    18,    19,     0,     0,     0,     0,     0,    20,    21,
      22,    23,    24,    25,    26,     0,     0,    27,     0,     0,
       0,     0,     0,    28,    29,   261,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,    52,     0,     0,    53,
      54,     0,    55,    56,     0,    57,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    67,    68,    69,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -773,
     260,  -773,     5,     6,     7,     8,     9,     0,     0,  -773,
      10,    11,     0,     0,  -773,    12,     0,    13,    14,    15,
      16,    17,    18,    19,  -773,     0,     0,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,    27,     0,
       0,     0,     0,     0,    28,    29,   261,    31,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,    52,     0,     0,
      53,    54,     0,    55,    56,     0,    57,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    67,    68,
      69,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -773,   260,  -773,     5,     6,     7,     8,     9,     0,     0,
    -773,    10,    11,     0,     0,  -773,    12,     0,    13,    14,
      15,    16,    17,    18,    19,     0,     0,     0,     0,     0,
      20,    21,    22,    23,    24,    25,    26,     0,     0,    27,
       0,     0,     0,     0,     0,    28,    29,   261,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    48,    49,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      50,    51,     0,     0,     0,     0,     0,     0,    52,     0,
       0,    53,    54,     0,    55,    56,     0,    57,     0,     0,
      58,    59,    60,    61,    62,    63,    64,    65,    66,     0,
       0,     0,     0,     0,     0,     0,     0,   260,     0,     5,
       6,     7,     8,     9,     0,  -773,  -773,    10,    11,    67,
      68,    69,    12,     0,    13,    14,    15,    16,    17,    18,
      19,  -773,     0,  -773,     0,     0,    20,    21,    22,    23,
      24,    25,    26,     0,     0,    27,     0,     0,     0,     0,
       0,    28,    29,   261,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,    52,     0,     0,    53,    54,     0,
      55,    56,     0,    57,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,   260,     0,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,    67,    68,    69,    12,     0,
      13,    14,    15,    16,    17,    18,    19,  -773,     0,  -773,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   261,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    50,    51,     0,     0,     0,     0,     0,     0,
      52,     0,     0,   262,    54,     0,    55,    56,     0,    57,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    67,    68,    69,     0,     0,     0,     0,     0,     0,
       0,  -773,     0,  -773,   260,  -773,     5,     6,     7,     8,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
       0,     0,     0,    20,    21,    22,    23,    24,    25,    26,
       0,     0,    27,     0,     0,     0,     0,     0,    28,    29,
     261,    31,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,    52,     0,     0,    53,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    67,    68,    69,     0,     0,     0,     0,     0,
       0,     0,  -773,     0,  -773,     4,  -773,     5,     6,     7,
       8,     9,     0,     0,     0,    10,    11,     0,     0,     0,
      12,     0,    13,    14,    15,    16,    17,    18,    19,     0,
       0,     0,     0,     0,    20,    21,    22,    23,    24,    25,
      26,     0,     0,    27,     0,     0,     0,     0,     0,    28,
      29,    30,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    50,    51,     0,     0,     0,     0,
       0,     0,    52,     0,     0,    53,    54,     0,    55,    56,
       0,    57,     0,     0,    58,    59,    60,    61,    62,    63,
      64,    65,    66,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    67,    68,    69,     0,     0,  -773,     0,
       0,     0,     0,     0,     0,  -773,   260,  -773,     5,     6,
       7,     8,     9,     0,     0,     0,    10,    11,     0,     0,
       0,    12,     0,    13,    14,    15,    16,    17,    18,    19,
       0,     0,     0,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,    27,     0,     0,     0,     0,     0,
      28,    29,   261,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,    52,     0,     0,    53,    54,     0,    55,
      56,     0,    57,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    67,    68,    69,     0,     0,  -773,
       0,     0,     0,     0,     0,     0,  -773,   260,  -773,     5,
       6,     7,     8,     9,     0,     0,  -773,    10,    11,     0,
       0,     0,    12,     0,    13,    14,    15,    16,    17,    18,
      19,     0,     0,     0,     0,     0,    20,    21,    22,    23,
      24,    25,    26,     0,     0,    27,     0,     0,     0,     0,
       0,    28,    29,   261,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,    52,     0,     0,    53,    54,     0,
      55,    56,     0,    57,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,   260,     0,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,    67,    68,    69,    12,     0,
      13,    14,    15,    16,    17,    18,    19,  -773,     0,  -773,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   261,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    50,    51,     0,     0,     0,     0,     0,     0,
      52,     0,     0,    53,    54,     0,    55,    56,     0,    57,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,     0,  -773,     0,     0,     0,     0,     0,     0,     0,
       0,     5,     6,     7,     0,     9,     0,     0,     0,    10,
      11,    67,    68,    69,    12,     0,    13,    14,    15,    16,
      17,    18,    19,  -773,     0,  -773,     0,     0,    20,    21,
      22,    23,    24,    25,    26,     0,     0,   209,     0,     0,
       0,     0,     0,     0,    29,     0,     0,    32,    33,    34,
      35,    36,    37,    38,    39,    40,   210,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,   211,     0,     0,   212,
      54,     0,    55,    56,     0,   213,   214,   215,    58,    59,
     216,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     5,     6,     7,
       0,     9,     0,     0,     0,    10,    11,    67,   217,    69,
      12,     0,    13,    14,    15,    16,    17,    18,    19,     0,
       0,   240,     0,     0,    20,    21,    22,    23,    24,    25,
      26,     0,     0,    27,     0,     0,     0,     0,     0,     0,
      29,     0,     0,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    50,    51,     0,     0,     0,     0,
       0,     0,   211,     0,     0,   212,    54,     0,    55,    56,
       0,     0,     0,     0,    58,    59,    60,    61,    62,    63,
      64,    65,    66,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     5,     6,     7,     0,     9,     0,     0,
       0,    10,    11,    67,    68,    69,    12,     0,    13,    14,
      15,    16,    17,    18,    19,   310,     0,   311,     0,     0,
      20,    21,    22,    23,    24,    25,    26,     0,     0,    27,
       0,     0,     0,     0,     0,     0,    29,     0,     0,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    48,    49,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      50,    51,     0,     0,     0,     0,     0,     0,   211,     0,
       0,   212,    54,     0,    55,    56,     0,     0,     0,     0,
      58,    59,    60,    61,    62,    63,    64,    65,    66,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     5,
       6,     7,     8,     9,     0,     0,     0,    10,    11,    67,
      68,    69,    12,     0,    13,    14,    15,    16,    17,    18,
      19,     0,     0,   240,     0,     0,    20,    21,    22,    23,
      24,    25,    26,     0,     0,    27,     0,     0,     0,     0,
       0,    28,    29,    30,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,    52,     0,     0,    53,    54,     0,
      55,    56,     0,    57,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,    67,    68,    69,    12,     0,
      13,    14,    15,    16,    17,    18,    19,   518,     0,     0,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   261,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    50,    51,     0,     0,     0,     0,     0,     0,
      52,     0,     0,    53,    54,     0,    55,    56,     0,    57,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    67,    68,    69,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   518,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,   139,   140,   141,   142,   143,   144,
     145,   146,   147,   148,   149,   150,   151,   152,     0,     0,
       0,   153,   154,   155,   409,   410,   411,   412,   160,   161,
     162,     0,     0,     0,     0,     0,   163,   164,   165,   166,
     413,   414,   415,   416,   171,    37,    38,   417,    40,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   173,   174,   175,   176,
     177,   178,   179,   180,   181,     0,     0,   182,   183,     0,
       0,     0,     0,   184,   185,   186,   187,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   188,   189,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     190,   191,   192,   193,   194,   195,   196,   197,   198,   199,
       0,   200,   201,     0,     0,     0,     0,     0,   202,   418,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
     139,   140,   141,   142,   143,   144,   145,   146,   147,   148,
     149,   150,   151,   152,     0,     0,     0,   153,   154,   155,
     156,   157,   158,   159,   160,   161,   162,     0,     0,     0,
       0,     0,   163,   164,   165,   166,   167,   168,   169,   170,
     171,   293,   294,   172,   295,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   173,   174,   175,   176,   177,   178,   179,   180,
     181,     0,     0,   182,   183,     0,     0,     0,     0,   184,
     185,   186,   187,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   188,   189,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   190,   191,   192,   193,
     194,   195,   196,   197,   198,   199,     0,   200,   201,     0,
       0,     0,     0,     0,   202,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,   139,   140,   141,   142,   143,
     144,   145,   146,   147,   148,   149,   150,   151,   152,     0,
       0,     0,   153,   154,   155,   156,   157,   158,   159,   160,
     161,   162,     0,     0,     0,     0,     0,   163,   164,   165,
     166,   167,   168,   169,   170,   171,   242,     0,   172,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   173,   174,   175,
     176,   177,   178,   179,   180,   181,     0,     0,   182,   183,
       0,     0,     0,     0,   184,   185,   186,   187,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   188,
     189,     0,     0,    59,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   190,   191,   192,   193,   194,   195,   196,   197,   198,
     199,     0,   200,   201,     0,     0,     0,     0,     0,   202,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
     139,   140,   141,   142,   143,   144,   145,   146,   147,   148,
     149,   150,   151,   152,     0,     0,     0,   153,   154,   155,
     156,   157,   158,   159,   160,   161,   162,     0,     0,     0,
       0,     0,   163,   164,   165,   166,   167,   168,   169,   170,
     171,     0,     0,   172,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   173,   174,   175,   176,   177,   178,   179,   180,
     181,     0,     0,   182,   183,     0,     0,     0,     0,   184,
     185,   186,   187,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   188,   189,     0,     0,    59,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   190,   191,   192,   193,
     194,   195,   196,   197,   198,   199,     0,   200,   201,     0,
       0,     0,     0,     0,   202,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,   139,   140,   141,   142,   143,
     144,   145,   146,   147,   148,   149,   150,   151,   152,     0,
       0,     0,   153,   154,   155,   156,   157,   158,   159,   160,
     161,   162,     0,     0,     0,     0,     0,   163,   164,   165,
     166,   167,   168,   169,   170,   171,     0,     0,   172,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   173,   174,   175,
     176,   177,   178,   179,   180,   181,     0,     0,   182,   183,
       0,     0,     0,     0,   184,   185,   186,   187,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   188,
     189,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   190,   191,   192,   193,   194,   195,   196,   197,   198,
     199,     0,   200,   201,     5,     6,     7,     0,     9,   202,
       0,     0,    10,    11,     0,     0,     0,    12,     0,    13,
      14,    15,   248,   249,    18,    19,     0,     0,     0,     0,
       0,    20,   250,   251,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,   280,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   281,
       0,     0,   212,    54,     0,    55,    56,     0,     0,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     5,     6,     7,     0,     9,     0,     0,
     282,    10,    11,     0,     0,     0,    12,   283,    13,    14,
      15,   248,   249,    18,    19,     0,     0,     0,     0,     0,
      20,   250,   251,    23,    24,    25,    26,     0,     0,   209,
       0,     0,     0,     0,     0,     0,   280,     0,     0,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   281,     0,
       0,   212,    54,     0,    55,    56,     0,     0,     0,     0,
      58,    59,    60,    61,    62,    63,    64,    65,    66,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     8,     9,     0,     0,   282,
      10,    11,     0,     0,     0,    12,   580,    13,    14,    15,
      16,    17,    18,    19,     0,     0,     0,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,    27,     0,
       0,     0,     0,     0,    28,    29,    30,    31,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,    52,     0,     0,
      53,    54,     0,    55,    56,     0,    57,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    67,    68,
      69,    12,     0,    13,    14,    15,    16,    17,    18,    19,
       0,     0,     0,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,   210,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,   211,     0,     0,   212,    54,     0,    55,
      56,     0,   213,   214,   215,    58,    59,   216,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     8,     9,     0,
       0,     0,    10,    11,    67,   217,    69,    12,     0,    13,
      14,    15,    16,    17,    18,    19,     0,     0,     0,     0,
       0,    20,    21,    22,    23,    24,    25,    26,     0,     0,
      27,     0,     0,     0,     0,     0,    28,    29,     0,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,    52,
       0,     0,    53,    54,     0,    55,    56,     0,    57,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      67,    68,    69,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,   210,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,   454,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,   213,   214,   215,    58,    59,   216,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    67,   217,    69,    12,
       0,    13,    14,    15,   248,   249,    18,    19,     0,     0,
       0,     0,     0,    20,   250,   251,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,   210,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
     663,   214,   215,    58,    59,   216,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    67,   217,    69,    12,     0,    13,    14,    15,
     248,   249,    18,    19,     0,     0,     0,     0,     0,    20,
     250,   251,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,   210,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
     454,     0,     0,     0,     0,     0,     0,   211,     0,     0,
     212,    54,     0,    55,    56,     0,   663,   214,   215,    58,
      59,   216,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    67,   217,
      69,    12,     0,    13,    14,    15,   248,   249,    18,    19,
       0,     0,     0,     0,     0,    20,   250,   251,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,   210,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,   211,     0,     0,   212,    54,     0,    55,
      56,     0,   213,   214,     0,    58,    59,   216,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,    67,   217,    69,    12,     0,    13,
      14,    15,   248,   249,    18,    19,     0,     0,     0,     0,
       0,    20,   250,   251,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,   210,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,   211,
       0,     0,   212,    54,     0,    55,    56,     0,     0,   214,
     215,    58,    59,   216,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      67,   217,    69,    12,     0,    13,    14,    15,   248,   249,
      18,    19,     0,     0,     0,     0,     0,    20,   250,   251,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,   210,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,   663,   214,     0,    58,    59,   216,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    67,   217,    69,    12,
       0,    13,    14,    15,   248,   249,    18,    19,     0,     0,
       0,     0,     0,    20,   250,   251,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,   210,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
       0,   214,     0,    58,    59,   216,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    67,   217,    69,    12,     0,    13,    14,    15,
      16,    17,    18,    19,     0,     0,     0,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,   211,     0,     0,
     212,    54,     0,    55,    56,     0,   758,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    67,   217,
      69,    12,     0,    13,    14,    15,   248,   249,    18,    19,
       0,     0,     0,     0,     0,    20,   250,   251,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,   211,     0,     0,   212,    54,     0,    55,
      56,     0,   758,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,    67,   217,    69,    12,     0,    13,
      14,    15,   248,   249,    18,    19,     0,     0,     0,     0,
       0,    20,   250,   251,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,   211,
       0,     0,   212,    54,     0,    55,    56,     0,   955,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      67,   217,    69,    12,     0,    13,    14,    15,   248,   249,
      18,    19,     0,     0,     0,     0,     0,    20,   250,   251,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,  1008,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    67,   217,    69,    12,
       0,    13,    14,    15,   248,   249,    18,    19,     0,     0,
       0,     0,     0,    20,   250,   251,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
    1152,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    67,   217,    69,    12,     0,    13,    14,    15,
     248,   249,    18,    19,     0,     0,     0,     0,     0,    20,
     250,   251,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,   211,     0,     0,
     212,    54,     0,    55,    56,     0,     0,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    67,   217,
      69,    12,     0,    13,    14,    15,    16,    17,    18,    19,
       0,     0,     0,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,   211,     0,     0,   212,    54,     0,    55,
      56,     0,     0,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,    67,   217,    69,    12,     0,    13,
      14,    15,    16,    17,    18,    19,     0,     0,     0,     0,
       0,    20,    21,    22,    23,    24,    25,    26,     0,     0,
      27,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,   211,
       0,     0,   212,    54,     0,    55,    56,     0,     0,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      67,    68,    69,    12,     0,    13,    14,    15,   248,   249,
      18,    19,     0,     0,     0,     0,     0,    20,   250,   251,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,   280,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   281,     0,     0,   341,    54,
       0,    55,    56,     0,   342,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,     0,     0,     0,    12,   282,    13,    14,    15,
     248,   249,    18,    19,     0,     0,     0,     0,     0,    20,
     250,   251,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,   280,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   390,     0,     0,
      53,    54,     0,    55,    56,     0,    57,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,     0,     0,     0,    12,   282,    13,
      14,    15,   248,   249,    18,    19,     0,     0,     0,     0,
       0,    20,   250,   251,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,   280,     0,     0,
      32,    33,    34,   398,    36,    37,    38,   399,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   400,     0,     0,     0,   401,
       0,     0,   212,    54,     0,    55,    56,     0,     0,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
     282,    13,    14,    15,   248,   249,    18,    19,     0,     0,
       0,     0,     0,    20,   250,   251,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,   280,
       0,     0,    32,    33,    34,   398,    36,    37,    38,   399,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   401,     0,     0,   212,    54,     0,    55,    56,     0,
       0,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,     0,     0,
       0,    12,   282,    13,    14,    15,   248,   249,    18,    19,
       0,     0,     0,     0,     0,    20,   250,   251,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,   280,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   281,     0,     0,   341,    54,     0,    55,
      56,     0,     0,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
       0,     0,     0,    12,   282,    13,    14,    15,   248,   249,
      18,    19,     0,     0,     0,     0,     0,    20,   250,   251,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,   280,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  1084,     0,     0,   212,    54,
       0,    55,    56,     0,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,     0,     0,     0,    12,   282,    13,    14,    15,
     248,   249,    18,    19,     0,     0,     0,     0,     0,    20,
     250,   251,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,   280,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,    23,    24,    25,    26,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    32,    33,    34,   906,     0,     0,     0,   907,     0,
       0,    41,    42,    43,    44,    45,     0,  1180,     0,     0,
     212,    54,     0,    55,    56,     0,     0,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,   908,   909,     0,     0,     0,     0,     0,     0,
     910,     0,     0,   911,     0,     0,   912,   913,   282,  1112,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,    23,    24,    25,    26,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   914,     0,     0,    32,    33,    34,
     906,   282,     0,     0,   907,     0,     0,    41,    42,    43,
      44,    45,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   908,   909,
       0,     0,     0,     0,     0,     0,   910,     0,     0,   911,
       0,     0,   912,   913,     0,  1024,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,   610,   611,     0,
       0,   612,     0,     0,     0,     0,     0,     0,     0,     0,
     914,     0,     0,     0,     0,     0,     0,   282,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   619,   620,     0,
       0,   621,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   667,   611,     0,
       0,   668,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   670,   620,     0,
       0,   671,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   697,   611,     0,
       0,   698,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   700,   620,     0,
       0,   701,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   808,   611,     0,
       0,   809,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   811,   620,     0,
       0,   812,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   817,   611,     0,
       0,   818,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   654,   620,     0,
       0,   655,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,  1014,   611,     0,
       0,  1015,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,  1017,   620,     0,
       0,  1018,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,  1224,   611,     0,
       0,  1225,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,  1227,   620,     0,
       0,  1228,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,  1264,   611,     0,
       0,  1265,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   654,   620,     0,
       0,   655,   202,   236,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,     0,     0,     0,
       0,     0,   202
};

static const yytype_int16 yycheck[] =
{
       2,   105,     4,    60,    59,    28,    91,   383,    96,     8,
       2,   329,   796,    53,    27,    57,    29,   401,   322,   104,
     274,     8,   326,    85,   278,   552,   731,     4,   378,    28,
      57,    16,    17,    57,    71,   735,   803,   326,   448,   230,
     731,    28,   377,    85,   379,    97,   101,    22,    68,    93,
      94,    53,    54,    97,   322,    57,    98,    99,   100,   863,
      97,    85,   380,    55,    56,    27,    22,    67,    71,   583,
     498,     2,    59,     4,    98,    99,   100,   322,   488,   865,
      13,   326,   728,    85,    76,    77,    77,    16,    17,    53,
      13,   426,    26,   557,   444,   559,    98,    99,   100,   101,
    1045,  1046,    13,   749,  1157,    80,    57,    26,   443,    13,
     445,  1190,    25,    68,   101,    25,    13,   102,    25,   322,
     657,   658,   262,    54,    80,    52,    55,    66,   568,    99,
      99,    25,    68,   573,    85,   575,    94,   669,   473,    97,
     225,   535,    97,    25,   728,    13,   109,    98,    99,   100,
     500,   735,   580,   300,   301,     0,   126,   126,    25,   216,
    1211,    97,   100,   440,  1243,   749,   501,   699,    56,   254,
     133,   448,   212,   102,    37,    38,    66,    16,    17,   728,
     119,    25,   121,   122,   143,   625,    13,   908,   909,   151,
     630,   153,   632,  1184,  1245,   239,    13,   241,  1277,    29,
     749,   341,  1255,   288,    25,    16,    17,   247,  1003,   143,
     212,    13,   264,   146,   148,   153,    55,    56,   151,   146,
     153,   283,   262,    13,   143,   148,   143,   264,   151,   119,
     153,   745,   224,   146,    13,   148,   146,   148,   230,   146,
     151,   283,   153,   245,   236,   247,   722,   151,   233,   153,
     235,   236,   146,   245,   151,   731,   153,    66,   260,   283,
     262,   264,   646,   102,   146,   100,  1211,   280,   459,  1260,
     680,   315,   316,   317,   318,  1079,   320,   321,   810,   146,
     153,   283,   282,   151,  1027,   153,   986,   637,   820,  1003,
     153,   102,  1078,   123,   285,   251,   252,   647,   262,  1094,
     342,   341,   146,   638,   233,    25,   235,   236,   393,    25,
     119,   146,   314,   648,   245,   342,   152,   319,   342,  1040,
    1041,   715,   716,   325,   151,   146,   153,   329,    97,   260,
     150,   649,   283,   153,   151,   387,   153,   314,   766,   341,
     342,   385,  1033,   100,   384,   991,   386,   339,   875,   151,
     387,   153,   344,   126,  1045,  1046,   400,   315,   316,   317,
     318,   151,    66,   153,   804,    66,   513,   807,   515,   152,
    1154,  1062,   151,  1116,   153,   148,   145,   341,   380,   148,
     912,   821,   384,   314,   386,    15,   378,  1154,   319,   146,
     146,   342,   379,   378,   233,    25,   235,   153,   702,   936,
     937,   678,   986,   680,   941,   942,   126,   871,   872,   152,
     678,   126,   876,   702,   878,    99,   880,   121,   122,  1003,
     121,   122,   233,   379,   235,    34,   146,   385,   148,   149,
     146,   146,   148,   153,   777,   778,   498,   429,   430,   426,
     694,   152,   126,    52,   467,    58,   438,  1190,   152,    99,
      68,   152,   444,    28,   446,   447,   498,   702,   445,   444,
     728,    99,   146,  1177,    77,   457,    66,   459,   467,   863,
    1184,   865,  1186,   126,   498,   666,   126,  1182,   480,    97,
     467,    66,   474,   728,  1016,   783,   473,   146,   126,   445,
     735,  1182,  1276,   791,  1026,   108,   498,  1029,   816,   112,
    1243,   148,  1245,   126,   749,  1196,   153,   947,   500,   786,
     950,  1043,    68,   953,   501,   500,  1053,   473,   580,   119,
     960,   121,   122,   963,    77,    52,   910,  1003,   565,    56,
    1273,  1177,   735,    68,  1277,   738,   121,   122,   580,   124,
      96,    97,  1003,   847,    99,   501,  1260,   498,  1262,   541,
      99,   639,    99,  1267,   904,  1269,   580,   577,   847,   706,
     552,    68,    97,   583,   711,  1092,   976,    68,   903,   912,
     905,   126,   564,  1100,  1288,    66,    67,   126,   580,   126,
      99,   957,    68,   136,   137,   138,   599,   627,    99,   629,
      97,   146,     9,  1177,   631,    96,    97,   146,    15,   146,
    1184,   440,   847,   112,   617,    68,   650,   126,  1140,  1141,
    1142,    97,    25,    68,   144,   126,  1080,  1081,  1082,  1083,
      52,  1267,   998,   153,    56,   627,   618,   629,  1068,   580,
     121,   122,   624,   695,    97,   146,   659,   657,   658,   683,
     615,    68,    97,   961,   145,   637,   485,   649,   577,   624,
     841,   638,   637,   695,   583,   647,   669,    99,  1042,   615,
     659,   648,   647,   151,   656,   153,   146,    54,   624,    77,
      97,   695,   659,    34,   666,   149,  1260,    64,    65,   153,
    1023,   656,   638,  1059,  1078,  1079,   699,   148,    58,  1007,
     775,    52,   648,   695,  1226,    66,   688,    25,    56,   976,
     656,  1177,  1045,  1046,   766,    68,  1182,    77,  1184,    25,
    1186,  1238,   126,   126,    82,    83,  1177,   100,  1158,  1062,
     824,  1182,    99,  1184,   766,  1186,   134,   135,   136,   137,
     138,   687,   688,   146,    97,    15,   149,    17,   108,   109,
     153,   986,   766,   146,   695,    99,    99,    99,   119,   126,
     121,   122,    26,   745,   746,    99,  1074,   146,  1003,  1223,
      99,   129,   130,   133,   766,    37,    38,   148,   823,   146,
    1146,   815,   126,   813,   126,   777,   778,   814,    99,   148,
    1123,   148,   126,   986,  1260,   988,  1262,   126,   780,   781,
     993,  1267,   146,  1269,    68,   787,   788,   810,   126,  1260,
    1003,  1262,   794,   149,   796,   126,  1267,   820,  1269,    66,
     126,   813,  1288,    68,   816,   766,   745,    66,   146,  1169,
     126,   149,    96,    97,    99,   153,    99,  1288,  1045,  1046,
     146,    68,    77,   149,    56,  1170,   823,   153,    68,   678,
     150,    96,    97,   835,   836,  1062,   838,   839,   888,   841,
     144,   126,   148,   126,   146,   146,    68,   815,    77,    96,
      97,   153,   119,   126,   121,   122,    96,    97,  1211,   143,
     119,   145,   121,   122,   148,   124,    14,    15,   583,     2,
     126,     4,    66,   875,    96,    97,   888,    26,    88,    89,
     145,   126,   884,    16,    17,    68,   898,   106,   900,   912,
     913,   893,   946,   895,    70,   945,   908,   909,   145,   901,
     912,   148,   904,   146,   146,   145,   936,   937,   905,   904,
     153,   941,   942,    96,    97,   962,   151,  1045,  1046,    68,
      53,    54,  1177,   145,    57,   119,    56,   121,   122,  1184,
     124,    40,    41,   945,  1062,    68,   344,   786,   940,   905,
     284,   285,   657,   658,    25,   997,   130,    96,    97,   961,
    1163,  1001,    85,   149,  1004,   978,   146,   898,    68,   900,
      93,    94,   145,   997,    97,    98,    99,   100,   146,   102,
     972,  1184,    52,  1186,    54,    55,    56,    57,   946,   144,
    1003,   144,  1012,   126,    68,   997,    96,    97,   146,  1001,
      68,  1086,  1004,  1016,   143,  1007,   145,    54,    55,   148,
      57,   146,    66,  1026,   146,  1260,  1029,    64,    65,   777,
     778,  1023,    96,    97,    68,  1027,  1066,   146,    96,    97,
    1043,   429,   430,  1053,   146,  1075,   146,  1039,  1040,  1041,
     438,    52,   146,  1045,  1046,   145,   997,    10,   446,   447,
    1090,    52,    96,    97,     8,   146,  1098,  1260,    68,  1262,
    1062,   144,  1119,    68,  1066,   119,  1269,   121,   122,   146,
     146,   145,  1074,  1075,  1098,    13,   474,   145,    25,  1136,
       2,  1094,     4,    17,   144,  1288,    96,    97,  1090,   212,
     152,    96,    97,   152,    16,    17,  1098,   146,   146,  1091,
    1092,   145,    44,   146,   146,  1145,   126,  1120,  1100,    44,
     233,    26,   235,   236,  1116,   128,   239,   150,   241,    15,
    1160,  1123,   245,   146,   247,   146,    52,  1140,  1141,  1142,
     146,    53,    54,   100,  1176,   145,   131,   260,   146,   262,
     145,   100,   146,  1145,   146,   131,    68,  1098,    52,   146,
     908,   909,  1176,    68,   912,   146,   146,    68,  1160,    52,
     283,    68,  1154,   146,   146,  1209,  1210,    59,    60,    61,
      62,    93,    94,   144,  1176,    97,    52,  1169,   151,  1171,
     102,    96,    97,  1170,  1169,    96,    97,   149,  1190,    96,
      97,   314,   315,   316,   317,   318,   319,   320,   321,    56,
     146,     9,   325,   131,  1217,   126,   329,   239,   144,  1211,
    1250,    56,   120,  1226,  1170,  1207,    68,   146,   341,   342,
     777,   778,   146,   146,   476,  1176,   146,   146,   143,   146,
     145,   936,   937,   148,   145,   267,   941,   942,   145,   271,
     146,  1243,    77,  1245,    96,    97,  1238,   131,  1250,   146,
     148,  1209,  1210,   148,   480,   378,   146,   380,   146,    94,
      95,   384,   385,   386,   146,  1023,   146,   146,   146,  1027,
     245,  1273,   146,   146,    98,  1277,    89,   400,   823,   100,
     656,  1039,  1040,  1041,  1276,  1207,   746,  1045,  1046,   715,
     212,   863,   895,   145,  1003,  1255,  1102,  1103,   133,   134,
     135,   136,   137,   138,  1062,   825,   571,  1012,   337,  1276,
    1039,   233,  1062,   235,   236,  1196,  1196,   239,  1037,   241,
    1171,   444,   108,   245,  1091,   247,   515,   101,   657,   658,
      77,    52,   731,    54,    55,    56,    57,    58,   260,  1182,
     262,    -1,  1120,   672,   673,  1177,    -1,    -1,  1053,    -1,
      -1,   908,   909,    -1,    -1,   912,    77,   480,  1116,    52,
     689,    54,    55,    56,    57,  1123,    -1,    -1,    -1,    52,
      91,    54,    55,    56,    57,   498,    -1,   500,    -1,    -1,
     101,    -1,   780,   781,    -1,    -1,   107,   108,   109,   787,
     788,    -1,   314,   315,   316,   317,   318,   319,   320,   321,
      -1,    -1,    -1,   325,    -1,    -1,    -1,   329,   101,    -1,
      -1,    -1,   133,    -1,   107,   136,    -1,    -1,   450,   341,
      -1,    -1,    -1,   455,    -1,    -1,   458,  1233,  1234,   461,
      -1,    -1,  1190,  1239,    -1,  1241,  1242,   835,   836,    -1,
     838,   839,    -1,    -1,    52,   477,    54,    55,    56,    57,
     482,    -1,    -1,  1211,   577,    -1,   378,   580,   380,    -1,
     583,    -1,   384,   385,   386,    -1,  1023,    -1,    -1,    -1,
    1027,    -1,    -1,  1279,  1280,  1281,  1282,    -1,   400,    -1,
      -1,    -1,  1039,  1040,  1041,  1243,  1292,  1245,  1045,  1046,
      -1,    -1,    52,   101,    54,    55,    56,    57,    58,    -1,
      -1,    -1,    -1,   901,   627,  1062,   629,    -1,    -1,    -1,
      -1,   543,   544,    -1,   637,  1273,    -1,    77,    -1,  1277,
      -1,    -1,   444,    -1,   647,    -1,   649,   650,    77,     2,
      -1,     4,    -1,    77,   657,   658,   777,   778,    -1,    -1,
      -1,   101,   940,    16,    17,    94,    95,   579,   108,   109,
      94,    95,    40,    41,    42,    43,    44,    -1,   480,  1116,
     683,    -1,    -1,    -1,    -1,    -1,  1123,    -1,    -1,    -1,
      -1,    -1,   695,   133,   972,    -1,    -1,    -1,   500,    -1,
      53,    54,   131,   132,   133,   134,   135,   136,   137,   138,
     134,   135,   136,   137,   138,    68,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   936,   937,    -1,
      -1,    -1,   941,   942,    -1,    -1,    -1,    -1,    -1,    -1,
      93,    94,    -1,    -1,    97,    -1,    -1,    -1,    -1,   102,
      -1,    -1,    -1,  1190,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   970,   971,   766,   973,   974,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1211,   577,    -1,    -1,    -1,    -1,
      -1,   583,    -1,    -1,    -1,    -1,    -1,   908,   909,    -1,
      -1,   912,    -1,    -1,    52,    -1,    54,    55,    56,    57,
      58,    -1,    -1,    -1,    -1,    -1,  1243,   719,  1245,    -1,
     813,    -1,   815,   816,    -1,    -1,    -1,    -1,    -1,    77,
      -1,    -1,    -1,    -1,    -1,   627,    -1,   629,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   637,  1273,    -1,    -1,    -1,
    1277,    -1,    -1,   101,  1053,   647,    -1,   649,   650,   107,
     108,   109,    -1,    -1,    -1,   657,   658,    -1,    -1,   212,
      -1,    -1,  1071,    -1,    -1,    -1,    52,    -1,    54,    55,
      56,    57,    58,    -1,    -1,   133,    -1,    -1,   136,    -1,
     233,   683,   235,   236,    -1,   888,   239,    -1,   241,    -1,
     148,    77,   245,    -1,   247,   898,    -1,   900,    -1,    -1,
      -1,   904,  1023,    -1,    -1,    -1,  1027,   260,    -1,   262,
     822,    -1,    -1,    -1,    -1,   101,    -1,    -1,  1039,  1040,
    1041,   107,   108,   109,  1045,  1046,    -1,    -1,   840,    -1,
     842,    -1,    -1,   936,   937,    -1,    -1,    -1,   941,   942,
      -1,  1062,   945,   946,    -1,    -1,    -1,   133,    -1,   861,
     136,    -1,    -1,    -1,    -1,    -1,    -1,   869,   961,    -1,
      -1,   314,   315,   316,   317,   318,   319,   320,   321,    -1,
      -1,    -1,   325,    -1,    -1,    -1,   329,    -1,    -1,    -1,
      52,    -1,    54,    55,    56,    57,    58,    -1,   341,    -1,
      -1,    -1,    -1,    -1,   997,  1116,    -1,    -1,  1001,    -1,
      -1,  1004,  1123,    -1,  1007,    77,    -1,    -1,    -1,  1012,
      -1,   813,    -1,   815,   816,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     2,    -1,     4,   378,    -1,   380,    -1,   101,
      -1,   384,   385,   386,    -1,   107,   108,   109,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   400,    -1,    -1,
    1053,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   133,    -1,  1066,   136,    -1,    -1,    -1,    -1,  1190,
      -1,  1074,  1075,    53,    54,    -1,    -1,    57,    -1,    -1,
      -1,   153,    -1,    -1,    -1,    -1,   888,  1090,    -1,    -1,
    1211,   444,    -1,    -1,    -1,  1098,   898,    -1,   900,    -1,
      -1,    -1,   904,    -1,    -1,    85,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    98,    99,
     100,    -1,  1243,    -1,  1245,    -1,    -1,   480,    -1,    -1,
      -1,    -1,  1044,    -1,   936,   937,    -1,    -1,    -1,   941,
     942,    -1,  1145,   945,   946,    -1,    -1,   500,    -1,    -1,
      -1,    -1,  1273,    -1,    -1,    -1,  1277,  1160,    -1,   961,
      33,    34,    35,    36,    -1,    -1,  1169,    -1,    -1,    -1,
      -1,    -1,    -1,  1176,    -1,    -1,    49,    50,    51,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    59,    60,    61,    62,
      63,    -1,    -1,    -1,    -1,    -1,    -1,  1109,    -1,  1001,
      -1,    -1,  1004,    -1,    -1,  1007,  1209,  1210,    -1,    -1,
    1012,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   577,  1137,  1138,  1139,    -1,    -1,
     583,    -1,   212,    -1,    -1,    -1,    -1,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    -1,  1250,    -1,    -1,
      -1,  1053,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1066,   245,   139,   247,    -1,    -1,
     777,   778,  1074,  1075,   627,    -1,   629,    -1,    -1,    -1,
     260,    -1,   262,    -1,   637,    -1,    -1,    -1,  1090,    -1,
      -1,    -1,    -1,    -1,   647,    -1,   649,   650,    -1,    -1,
      -1,    -1,    -1,   283,   657,   658,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,     2,    -1,     4,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    -1,    86,    87,    -1,
     683,    -1,    -1,    -1,   314,    94,    95,    -1,    -1,   319,
      -1,    -1,    -1,  1145,    -1,   325,    -1,    -1,    -1,   329,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1160,    -1,
      -1,   341,   342,    25,    53,    54,    -1,  1169,    57,    -1,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    85,    -1,    -1,    -1,
     380,   908,   909,    -1,   384,   912,   386,  1209,  1210,    98,
      99,   100,   101,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,     2,    -1,
       4,    -1,    94,    95,    -1,    -1,    -1,    -1,   100,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1250,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     813,    -1,   815,   816,    -1,   127,    -1,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,    -1,    53,
      54,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     480,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1023,    -1,   498,    -1,
    1027,    -1,    -1,   212,    98,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,  1039,  1040,  1041,   888,    -1,    -1,  1045,  1046,
      -1,    -1,    -1,    -1,    -1,   898,    -1,   900,    -1,    -1,
      -1,   904,    -1,    -1,    -1,  1062,   245,    -1,   247,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   260,    -1,   262,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   936,   937,    -1,    -1,    -1,   941,   942,
      -1,    -1,   945,   946,   283,    -1,    -1,   777,   778,    -1,
     580,    -1,    -1,   583,    -1,    -1,    -1,    -1,   961,  1116,
       2,    -1,     4,    -1,    -1,    -1,  1123,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   314,    -1,    -1,    -1,    -1,
     319,    -1,    -1,    -1,    -1,    -1,   325,    -1,   212,    -1,
     329,    -1,    -1,    -1,    -1,    -1,    -1,   627,  1001,   629,
      -1,  1004,   341,   342,  1007,    -1,    -1,    -1,    -1,  1012,
      -1,    53,    54,    -1,    -1,    -1,   777,   778,    -1,   649,
      -1,   245,    -1,   247,    -1,    -1,    -1,   657,   658,    -1,
      -1,    -1,    -1,  1190,    -1,    -1,   260,    -1,   262,    -1,
      -1,   380,    -1,    -1,    -1,   384,    -1,   386,    -1,    -1,
    1053,    -1,    -1,    -1,  1211,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1066,    -1,   695,    -1,    -1,    -1,    -1,
      -1,  1074,  1075,    -1,   704,    -1,    -1,    -1,   908,   909,
      -1,    -1,   912,    -1,    -1,    -1,  1243,  1090,  1245,    -1,
     314,    -1,    -1,    -1,    -1,   319,    33,    34,    35,    36,
      -1,   325,    -1,    -1,    -1,   329,    -1,    -1,    -1,    -1,
      -1,    -1,    49,    50,    51,    52,  1273,   341,    -1,    56,
    1277,    -1,    59,    60,    61,    62,    63,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   766,    -1,    -1,    -1,
      -1,   480,  1145,    -1,    -1,    -1,    -1,   908,   909,    -1,
      -1,   912,    -1,    90,    91,    -1,   380,  1160,    -1,   498,
     384,    98,   386,    -1,   101,    -1,  1169,   104,   105,    -1,
     212,    -1,    -1,   110,   111,   112,   113,   114,   115,   116,
     117,   118,    -1,   813,    -1,    -1,   816,    -1,    -1,    -1,
      -1,    -1,    -1,  1023,    -1,   132,    -1,  1027,    -1,    -1,
      -1,    -1,   139,   245,    -1,   247,  1209,  1210,    -1,  1039,
    1040,  1041,    -1,    -1,    -1,  1045,  1046,    -1,   260,    -1,
     262,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,  1062,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   580,    -1,    -1,    -1,    -1,    -1,  1250,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   480,    -1,   888,    -1,
      -1,    -1,  1023,    -1,    -1,    -1,  1027,    -1,   898,    -1,
     900,    -1,   314,    -1,    -1,    -1,    -1,   319,  1039,  1040,
    1041,    -1,    -1,   325,  1045,  1046,  1116,   329,   627,    -1,
     629,    -1,    -1,  1123,    -1,    -1,    -1,    -1,    -1,   341,
      -1,  1062,    -1,    -1,    -1,    -1,   936,   937,    -1,    -1,
     649,   941,   942,    -1,    -1,   945,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   961,   777,   778,    -1,    -1,    -1,    -1,   380,    -1,
      -1,    -1,   384,    -1,   386,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,  1116,   695,    -1,    -1,   583,
    1190,    -1,  1123,    -1,    -1,    -1,    -1,   997,    -1,    -1,
      -1,  1001,    -1,    -1,  1004,    -1,    -1,  1007,    -1,    -1,
      -1,  1211,  1012,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   627,    -1,   629,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1243,    -1,  1245,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1053,    -1,   649,    -1,   766,    -1,  1190,
      -1,    -1,    -1,   657,   658,    -1,  1066,    -1,   480,   777,
     778,    -1,    -1,  1273,  1074,  1075,    -1,  1277,    -1,    -1,
    1211,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1090,    -1,    -1,   908,   909,    -1,    -1,   912,  1098,    -1,
      -1,    -1,    -1,    -1,   813,    -1,    -1,   816,    -1,    -1,
      -1,    -1,  1243,     0,  1245,    56,    -1,    -1,    -1,    -1,
      -1,     8,     9,    10,    -1,    -1,    13,    14,    15,    -1,
      17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,    26,
      27,    -1,  1273,    -1,    -1,  1145,  1277,    -1,    -1,    -1,
      37,    38,    -1,    40,    41,    42,    43,    44,    -1,    -1,
    1160,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   583,    -1,    -1,    -1,    -1,  1176,    -1,    -1,   888,
      -1,    68,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   898,
      -1,   900,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     908,   909,    -1,    -1,   912,    -1,    -1,    -1,  1023,    96,
      97,    -1,  1027,    -1,    -1,   627,    -1,   629,    -1,   813,
      -1,    -1,   816,    -1,  1039,  1040,  1041,    -1,    -1,    -1,
    1045,  1046,    -1,   120,    -1,    -1,   945,   649,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   657,   658,  1062,    -1,    -1,
    1250,    -1,   961,    -1,    -1,    -1,   143,   144,    -1,    -1,
      -1,   148,   149,    -1,   151,    -1,   153,    -1,    -1,   210,
      -1,    -1,   213,   214,   215,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   997,    -1,
      -1,    -1,  1001,    -1,   888,  1004,    -1,    -1,  1007,    -1,
      -1,  1116,    -1,    -1,   898,    -1,   900,    -1,  1123,    -1,
      -1,    -1,    -1,    -1,    -1,  1023,    -1,    -1,    -1,  1027,
      -1,    -1,   777,   778,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,  1039,  1040,  1041,    -1,    -1,    -1,  1045,  1046,    -1,
      -1,    -1,   936,   937,    -1,    -1,    -1,   941,   942,    -1,
      -1,   945,    -1,    -1,  1062,    -1,    -1,  1066,    -1,    16,
      17,    -1,    -1,    -1,    -1,  1074,  1075,   961,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,  1190,    -1,    -1,    -1,    -1,
      -1,  1090,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1098,
      -1,    48,    49,    50,    51,    -1,  1211,    -1,    55,    56,
      -1,   813,    -1,   997,   816,    -1,    -1,  1001,  1116,    -1,
    1004,    68,    69,  1007,    -1,  1123,    -1,    -1,  1012,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1243,    -1,
    1245,    -1,    -1,    -1,    -1,    -1,  1145,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   102,    -1,    -1,    -1,    -1,
      -1,  1160,    -1,   908,   909,    -1,    -1,   912,  1273,  1053,
      -1,    -1,  1277,    -1,    -1,    -1,    -1,  1176,    -1,    -1,
      -1,    -1,  1066,    -1,    -1,    -1,   888,    -1,    -1,    -1,
    1074,  1075,  1190,    -1,    -1,    -1,   898,    -1,   900,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1090,    -1,   439,   440,
      -1,    -1,    -1,  1211,    -1,    -1,    -1,   448,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   936,   937,    -1,    -1,    -1,   941,
     942,    -1,    -1,   945,    -1,  1243,    -1,  1245,    -1,    -1,
      -1,  1250,    -1,    -1,   485,    -1,    -1,   488,    -1,   961,
      -1,  1145,    -1,   210,    -1,    -1,   213,   214,   215,    -1,
     217,    -1,    -1,    -1,    -1,  1273,  1160,    -1,  1023,  1277,
      -1,    -1,  1027,    -1,    -1,    -1,   233,    -1,   235,   236,
      -1,    -1,    -1,    -1,  1039,  1040,  1041,    -1,    -1,  1001,
    1045,  1046,  1004,    -1,    -1,  1007,    -1,    -1,    -1,    -1,
    1012,    -1,    -1,    -1,    -1,    -1,    -1,  1062,    -1,   550,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   577,    -1,    -1,    -1,
      -1,  1053,   583,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1066,    -1,  1250,    -1,    -1,    -1,
      -1,  1116,  1074,  1075,    -1,    -1,    -1,    -1,  1123,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1090,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   346,
     347,   348,   349,   350,    -1,    -1,   353,   354,   355,   356,
     357,   358,   359,   360,    -1,   362,    -1,    -1,   365,   366,
     367,   368,   369,   370,   371,   372,   373,   374,    -1,   660,
      -1,   378,   663,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1145,    -1,  1190,    -1,   678,    -1,   680,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1160,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1211,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   439,   440,    -1,    -1,   727,   444,  1243,    -1,
    1245,   448,    -1,    -1,    -1,    -1,    -1,   454,    -1,    -1,
      -1,    -1,    -1,    -1,   745,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   469,    -1,    -1,    -1,    -1,   758,  1273,    -1,
      -1,    -1,  1277,    -1,    -1,    -1,    -1,    -1,   485,    -1,
      -1,   488,    -1,    -1,    -1,    -1,    -1,    -1,  1250,    -1,
      -1,    -1,    -1,   500,    -1,   786,    -1,    -1,    -1,    -1,
      -1,    33,    34,    35,    36,    -1,    -1,    -1,    -1,    -1,
     517,    -1,    -1,    -1,    -1,   806,    -1,    49,    50,    51,
      52,    -1,    -1,    -1,    56,    -1,    -1,    59,    60,    61,
      62,    63,    -1,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    -1,   550,    86,    87,    -1,    -1,    -1,    -1,
      -1,    -1,    94,    95,    -1,   562,    -1,    -1,    90,    91,
     567,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,
     577,    -1,   104,   105,    -1,   107,   583,    -1,   110,   111,
     112,   113,   114,   115,   116,   117,   118,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,    -1,    -1,
     132,    -1,    -1,    -1,    -1,    -1,   897,   139,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   153,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     637,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     647,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     657,   658,    -1,   660,   661,    -1,   663,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   955,   672,   673,    -1,    -1,    -1,
      -1,   678,    -1,   680,    -1,    -1,    -1,    -1,    33,    34,
      35,    36,   689,    -1,    -1,   976,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    49,    50,    51,    52,    -1,    -1,
      -1,    56,    -1,    58,    59,    60,    61,    62,    63,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1008,    -1,    -1,
     727,    -1,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    90,    91,    -1,   745,    -1,
      -1,    -1,    -1,    98,    -1,    -1,   101,    -1,    -1,   104,
     105,   758,   107,   108,    -1,   110,   111,   112,   113,   114,
     115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1065,    -1,    -1,   132,    -1,   786,
      -1,    -1,    -1,    -1,   139,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   806,
      -1,    -1,    -1,     0,     1,    -1,     3,     4,     5,     6,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,  1152,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,   881,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,   892,    -1,    -1,    -1,    -1,
     897,    -1,    -1,    90,    91,   902,    -1,   904,    -1,    -1,
      -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,
     107,    -1,    -1,   110,   111,   112,   113,   114,   115,   116,
     117,   118,    -1,    -1,    -1,    -1,    -1,    -1,   935,   936,
     937,    -1,    -1,    -1,   941,   942,    -1,    -1,    -1,    -1,
      -1,    -1,   139,   140,   141,    -1,    -1,    -1,   955,    -1,
      -1,    -1,    -1,    -1,   151,    -1,   153,    -1,    -1,    -1,
      -1,    -1,    -1,   970,   971,    -1,   973,   974,    -1,   976,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     0,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,     8,     9,    10,
      -1,  1008,    13,    14,    15,  1012,    17,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    25,    26,    27,    28,    29,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,
      41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1053,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,  1065,    -1,
      -1,    -1,    -1,    -1,  1071,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    -1,
      -1,    -1,    -1,    94,    95,    96,    97,    -1,    99,   100,
      -1,    -1,    -1,    -1,    -1,   106,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   120,
      -1,    -1,   123,    -1,    -1,   126,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,    -1,   143,   144,   145,   146,    -1,    -1,   149,   150,
     151,     0,   153,    -1,    -1,  1152,    -1,    -1,    -1,     8,
       9,    10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,
      -1,    -1,  1169,    44,    -1,    -1,    25,    -1,    27,    28,
      29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    68,
      -1,    -1,    -1,    94,    95,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    -1,    -1,    94,    95,    96,    97,    -1,
      99,   100,    -1,    -1,    -1,    -1,   127,   106,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,   120,    -1,    -1,   123,   146,    -1,   126,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
      -1,    -1,    -1,    -1,    -1,   144,   145,   146,     0,    -1,
     149,   150,   151,    -1,   153,    -1,     8,     9,    10,    -1,
      -1,    13,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,
      44,    -1,    -1,    25,    -1,    27,    28,    29,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,
      42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    68,    -1,    -1,    -1,
      94,    95,    -1,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    -1,    -1,
      -1,    -1,    94,    95,    96,    97,    -1,    99,   100,    -1,
      -1,    -1,    -1,   127,   106,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,    -1,    -1,   120,    -1,
      -1,   123,    -1,    -1,   126,   127,   128,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,    -1,    -1,
      -1,    -1,   144,   145,   146,     0,    -1,   149,   150,   151,
      -1,   153,    -1,     8,     9,    10,    -1,    -1,    13,    14,
      15,    -1,    17,    -1,    -1,    -1,    -1,    44,    -1,    -1,
      25,    26,    27,    28,    29,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    37,    38,    -1,    40,    41,    42,    43,    44,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      77,    78,    79,    80,    81,    82,    83,    84,    85,    86,
      87,    88,    89,    68,    -1,    -1,    -1,    94,    95,    -1,
      -1,    -1,    77,    78,    79,    80,    81,    82,    83,    84,
      85,    86,    87,    88,    89,    -1,    -1,    -1,    -1,    94,
      95,    96,    97,    -1,    -1,   100,    -1,    -1,    -1,    -1,
     127,   106,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,    -1,    -1,    -1,   120,    -1,    -1,   123,    -1,
      -1,    -1,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,    -1,    -1,    -1,   143,   144,
     145,   146,     0,    -1,   149,   150,   151,    -1,   153,    -1,
       8,     9,    10,    -1,    -1,    13,    14,    15,    -1,    17,
      -1,    -1,    -1,    -1,    44,    -1,    -1,    25,    26,    27,
      28,    29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,
      38,    -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,
      80,    81,    82,    83,    84,    85,    86,    87,    88,    89,
      68,    -1,    -1,    -1,    94,    95,    -1,    -1,    -1,    77,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    -1,    -1,    -1,    -1,    94,    95,    96,    97,
      -1,    -1,   100,    -1,    -1,    -1,    -1,   127,   106,   129,
     130,   131,   132,   133,   134,   135,   136,   137,   138,    -1,
      -1,    -1,   120,    -1,    -1,   123,    -1,    -1,    -1,   127,
     128,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,    -1,    -1,    -1,    -1,   143,   144,   145,   146,     0,
      -1,   149,   150,   151,    -1,   153,    -1,     8,     9,    10,
      -1,    -1,    13,    14,    15,    -1,    17,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    25,    -1,    27,    28,    29,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,
      41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    77,    78,    79,    80,    81,    82,
      83,    84,    85,    86,    87,    88,    89,    68,    -1,    -1,
      -1,    94,    95,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    -1,
      -1,    -1,    -1,    94,    95,    96,    97,    -1,    99,   100,
      -1,    -1,    -1,    -1,   127,   106,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,    -1,    -1,    -1,   120,
      -1,    -1,   123,    -1,    -1,   126,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,    -1,    -1,   144,   145,   146,     0,    -1,   149,   150,
     151,    -1,   153,    -1,     8,     9,    10,    -1,    -1,    13,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    25,    26,    27,    28,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    77,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    68,    -1,    -1,    -1,    94,    95,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    -1,    -1,    -1,    -1,
      94,    95,    96,    97,    -1,    -1,   100,    -1,    -1,    -1,
      -1,    -1,   106,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,    -1,    -1,   120,    -1,    -1,    -1,
      -1,    -1,    -1,   127,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,    -1,    -1,    -1,   143,
     144,   145,   146,     0,   148,   149,   150,   151,    -1,   153,
      -1,     8,     9,    10,    -1,    -1,    13,    14,    15,    -1,
      17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,    -1,
      27,    28,    29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      37,    38,    -1,    40,    41,    42,    43,    44,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    -1,    -1,    86,    87,    -1,
      -1,    68,    -1,    -1,    -1,    94,    95,    -1,    -1,    -1,
      77,    78,    79,    80,    81,    82,    83,    84,    85,    86,
      87,    88,    89,    -1,    -1,    -1,    -1,    94,    95,    96,
      97,    -1,    -1,   100,    -1,    -1,    -1,    -1,    -1,   106,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
      -1,    -1,    -1,   120,    -1,    -1,   123,    -1,    -1,    -1,
     127,   128,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,    -1,    -1,    -1,    -1,    -1,   144,   145,   146,
       0,    -1,   149,   150,   151,    -1,   153,    -1,     8,     9,
      10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    25,    26,    27,    28,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,
      40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,
      80,    81,    82,    83,    84,    85,    86,    87,    88,    89,
      -1,    -1,    -1,    -1,    94,    95,    96,    97,    -1,    -1,
     100,    -1,    -1,    -1,    -1,    -1,   106,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     120,    -1,    -1,    -1,    -1,    -1,    -1,   127,    -1,   129,
     130,   131,   132,   133,   134,   135,   136,   137,   138,    -1,
      -1,    -1,    -1,   143,   144,   145,   146,     0,   148,   149,
     150,   151,    -1,   153,    -1,     8,     9,    10,    -1,    -1,
      13,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    25,    -1,    27,    28,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,
      43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    77,    78,    79,    80,    81,    82,
      83,    84,    85,    86,    87,    88,    89,    -1,    -1,    -1,
      -1,    94,    95,    96,    97,    -1,    -1,   100,    -1,    -1,
      -1,    -1,    -1,   106,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   120,    -1,    -1,
      -1,    -1,    -1,    -1,   127,    -1,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,    -1,    -1,    -1,    -1,
      -1,   144,   145,   146,     0,   148,   149,   150,   151,    -1,
     153,    -1,     8,     9,    10,    -1,    -1,    -1,    14,    15,
      -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,
      26,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    37,    38,    -1,    40,    41,    42,    43,    44,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    -1,
      -1,    -1,    68,    94,    95,    -1,    -1,    -1,    -1,    -1,
      -1,    77,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    -1,    -1,    -1,    -1,    94,    95,
      96,    97,    -1,    99,   100,    -1,   127,    -1,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,    -1,    -1,    -1,   120,    -1,    -1,    -1,    -1,    -1,
     126,   127,   153,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,    -1,    -1,    -1,   143,   144,   145,
     146,     0,    -1,   149,    -1,   151,    -1,   153,    -1,     8,
       9,    10,    -1,    -1,    -1,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    25,    26,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    -1,    -1,    94,    95,    96,    97,    -1,
      99,   100,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   120,    -1,    -1,    -1,    -1,    -1,   126,   127,    -1,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
      -1,    -1,    -1,    -1,   143,   144,   145,   146,     0,    -1,
     149,    -1,   151,    -1,   153,    -1,     8,     9,    10,    -1,
      -1,    -1,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    25,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,
      42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    68,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    -1,    -1,
      -1,    -1,    94,    95,    96,    97,    -1,    99,   100,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   120,    -1,
      -1,    -1,    -1,    -1,   126,   127,    -1,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,    -1,    -1,
      -1,    -1,   144,   145,   146,     0,    -1,   149,    -1,   151,
      -1,   153,    -1,     8,     9,    10,    -1,    -1,    -1,    14,
      15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      25,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    37,    38,    -1,    40,    41,    42,    43,    44,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    77,    78,    79,    80,    81,    82,    83,    84,
      85,    86,    87,    88,    89,    -1,    -1,    -1,    -1,    94,
      95,    96,    97,    -1,    99,   100,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   120,    -1,    -1,    -1,    -1,
      -1,   126,   127,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,    -1,    -1,    -1,    -1,   144,
     145,   146,    -1,    -1,   149,    -1,   151,     1,   153,     3,
       4,     5,     6,     7,     8,     9,    10,    11,    12,    -1,
      -1,    15,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    45,    46,    47,    48,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,
      -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,
     104,   105,    -1,   107,    -1,    -1,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   139,   140,   141,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   151,     1,   153,
       3,     4,     5,     6,     7,    -1,    -1,    10,    11,    12,
      -1,    14,    15,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    45,    46,    47,    48,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,
      -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,
      -1,   104,   105,    -1,   107,    -1,    -1,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   139,   140,   141,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   151,     1,
     153,     3,     4,     5,     6,     7,    -1,    -1,    10,    11,
      12,    -1,    -1,    15,    16,    17,    18,    19,    20,    21,
      22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,
      -1,    -1,    -1,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    -1,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,
      -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,
     102,    -1,   104,   105,    -1,   107,    -1,    -1,   110,   111,
     112,   113,   114,   115,   116,   117,   118,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   139,   140,   141,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   151,
       1,   153,     3,     4,     5,     6,     7,    -1,    -1,    10,
      11,    12,    -1,    -1,    15,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    25,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,
      91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,
     101,   102,    -1,   104,   105,    -1,   107,    -1,    -1,   110,
     111,   112,   113,   114,   115,   116,   117,   118,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   139,   140,
     141,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     151,     1,   153,     3,     4,     5,     6,     7,    -1,    -1,
      10,    11,    12,    -1,    -1,    15,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,
      30,    31,    32,    33,    34,    35,    36,    -1,    -1,    39,
      -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,
      -1,   101,   102,    -1,   104,   105,    -1,   107,    -1,    -1,
     110,   111,   112,   113,   114,   115,   116,   117,   118,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,     1,    -1,     3,
       4,     5,     6,     7,    -1,     9,    10,    11,    12,   139,
     140,   141,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,   151,    -1,   153,    -1,    -1,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    45,    46,    47,    48,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,
      -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,
     104,   105,    -1,   107,    -1,    -1,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,     1,    -1,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,   139,   140,   141,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,   151,    -1,   153,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,
      98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,   107,
      -1,    -1,   110,   111,   112,   113,   114,   115,   116,   117,
     118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   139,   140,   141,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   149,    -1,   151,     1,   153,     3,     4,     5,     6,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,
      -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,
     107,    -1,    -1,   110,   111,   112,   113,   114,   115,   116,
     117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   139,   140,   141,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   149,    -1,   151,     1,   153,     3,     4,     5,
       6,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    -1,
      -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    45,
      46,    47,    48,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,
      -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,
      -1,   107,    -1,    -1,   110,   111,   112,   113,   114,   115,
     116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   139,   140,   141,    -1,    -1,   144,    -1,
      -1,    -1,    -1,    -1,    -1,   151,     1,   153,     3,     4,
       5,     6,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,
      -1,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,
      -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,
     105,    -1,   107,    -1,    -1,   110,   111,   112,   113,   114,
     115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   139,   140,   141,    -1,    -1,   144,
      -1,    -1,    -1,    -1,    -1,    -1,   151,     1,   153,     3,
       4,     5,     6,     7,    -1,    -1,    10,    11,    12,    -1,
      -1,    -1,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    45,    46,    47,    48,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,
      -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,
     104,   105,    -1,   107,    -1,    -1,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,     1,    -1,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,   139,   140,   141,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,   151,    -1,   153,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,
      98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,   107,
      -1,    -1,   110,   111,   112,   113,   114,   115,   116,   117,
     118,    -1,   120,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,
      12,   139,   140,   141,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,   151,    -1,   153,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,
      -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    58,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,
      -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,
     102,    -1,   104,   105,    -1,   107,   108,   109,   110,   111,
     112,   113,   114,   115,   116,   117,   118,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,
      -1,     7,    -1,    -1,    -1,    11,    12,   139,   140,   141,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    -1,
      -1,   153,    -1,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,
      46,    -1,    -1,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,
      -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,
      -1,    -1,    -1,    -1,   110,   111,   112,   113,   114,   115,
     116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,
      -1,    11,    12,   139,   140,   141,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,   151,    -1,   153,    -1,    -1,
      30,    31,    32,    33,    34,    35,    36,    -1,    -1,    39,
      -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,
      -1,   101,   102,    -1,   104,   105,    -1,    -1,    -1,    -1,
     110,   111,   112,   113,   114,   115,   116,   117,   118,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,
       4,     5,     6,     7,    -1,    -1,    -1,    11,    12,   139,
     140,   141,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,    -1,    -1,   153,    -1,    -1,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    45,    46,    47,    48,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,
      -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,
     104,   105,    -1,   107,    -1,    -1,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,   139,   140,   141,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,   151,    -1,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,
      98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,   107,
      -1,    -1,   110,   111,   112,   113,   114,   115,   116,   117,
     118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   139,   140,   141,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   151,     3,     4,     5,     6,     7,     8,
       9,    10,    11,    12,    13,    14,    15,    16,    17,    18,
      19,    20,    21,    22,    23,    24,    25,    26,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    77,    78,
      79,    80,    81,    82,    83,    -1,    -1,    86,    87,    -1,
      -1,    -1,    -1,    92,    93,    94,    95,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   107,   108,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
      -1,   140,   141,    -1,    -1,    -1,    -1,    -1,   147,   148,
       3,     4,     5,     6,     7,     8,     9,    10,    11,    12,
      13,    14,    15,    16,    17,    18,    19,    20,    21,    22,
      23,    24,    25,    26,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    -1,    -1,    -1,
      -1,    -1,    45,    46,    47,    48,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    77,    78,    79,    80,    81,    82,
      83,    -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,
      93,    94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,    -1,   140,   141,    -1,
      -1,    -1,    -1,    -1,   147,     3,     4,     5,     6,     7,
       8,     9,    10,    11,    12,    13,    14,    15,    16,    17,
      18,    19,    20,    21,    22,    23,    24,    25,    26,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    -1,    56,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    77,
      78,    79,    80,    81,    82,    83,    -1,    -1,    86,    87,
      -1,    -1,    -1,    -1,    92,    93,    94,    95,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   107,
     108,    -1,    -1,   111,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,    -1,   140,   141,    -1,    -1,    -1,    -1,    -1,   147,
       3,     4,     5,     6,     7,     8,     9,    10,    11,    12,
      13,    14,    15,    16,    17,    18,    19,    20,    21,    22,
      23,    24,    25,    26,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    -1,    -1,    -1,
      -1,    -1,    45,    46,    47,    48,    49,    50,    51,    52,
      53,    -1,    -1,    56,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    77,    78,    79,    80,    81,    82,
      83,    -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,
      93,    94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   107,   108,    -1,    -1,   111,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,    -1,   140,   141,    -1,
      -1,    -1,    -1,    -1,   147,     3,     4,     5,     6,     7,
       8,     9,    10,    11,    12,    13,    14,    15,    16,    17,
      18,    19,    20,    21,    22,    23,    24,    25,    26,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    -1,    -1,    56,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    77,
      78,    79,    80,    81,    82,    83,    -1,    -1,    86,    87,
      -1,    -1,    -1,    -1,    92,    93,    94,    95,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   107,
     108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,    -1,   140,   141,     3,     4,     5,    -1,     7,   147,
      -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    63,    64,    65,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    98,
      -1,    -1,   101,   102,    -1,   104,   105,    -1,    -1,    -1,
      -1,   110,   111,   112,   113,   114,   115,   116,   117,   118,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,
     139,    11,    12,    -1,    -1,    -1,    16,   146,    18,    19,
      20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,
      30,    31,    32,    33,    34,    35,    36,    -1,    -1,    39,
      -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,
      -1,   101,   102,    -1,   104,   105,    -1,    -1,    -1,    -1,
     110,   111,   112,   113,   114,   115,   116,   117,   118,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,     6,     7,    -1,    -1,   139,
      11,    12,    -1,    -1,    -1,    16,   146,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,
      91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,
     101,   102,    -1,   104,   105,    -1,   107,    -1,    -1,   110,
     111,   112,   113,   114,   115,   116,   117,   118,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,   139,   140,
     141,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,
      -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,
     105,    -1,   107,   108,   109,   110,   111,   112,   113,   114,
     115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,     6,     7,    -1,
      -1,    -1,    11,    12,   139,   140,   141,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    45,    46,    -1,    48,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    63,    64,    65,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,
      -1,    -1,   101,   102,    -1,   104,   105,    -1,   107,    -1,
      -1,   110,   111,   112,   113,   114,   115,   116,   117,   118,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
     139,   140,   141,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    58,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,
      -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,
      -1,   104,   105,    -1,   107,   108,   109,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,   139,   140,   141,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    58,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,
      -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,
     107,   108,   109,   110,   111,   112,   113,   114,   115,   116,
     117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,   139,   140,   141,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    58,    59,    60,
      61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,
      91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,
     101,   102,    -1,   104,   105,    -1,   107,   108,   109,   110,
     111,   112,   113,   114,   115,   116,   117,   118,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,   139,   140,
     141,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,
      -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,
     105,    -1,   107,   108,    -1,   110,   111,   112,   113,   114,
     115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,
      -1,    -1,    11,    12,   139,   140,   141,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    58,
      59,    60,    61,    62,    63,    64,    65,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,
      -1,    -1,   101,   102,    -1,   104,   105,    -1,    -1,   108,
     109,   110,   111,   112,   113,   114,   115,   116,   117,   118,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
     139,   140,   141,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    58,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,
      -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,
      -1,   104,   105,    -1,   107,   108,    -1,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,   139,   140,   141,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    58,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,
      -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,
      -1,   108,    -1,   110,   111,   112,   113,   114,   115,   116,
     117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,   139,   140,   141,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,
      91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,
     101,   102,    -1,   104,   105,    -1,   107,    -1,    -1,   110,
     111,   112,   113,   114,   115,   116,   117,   118,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,   139,   140,
     141,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,
      -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,
     105,    -1,   107,    -1,    -1,   110,   111,   112,   113,   114,
     115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,
      -1,    -1,    11,    12,   139,   140,   141,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    63,    64,    65,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,
      -1,    -1,   101,   102,    -1,   104,   105,    -1,   107,    -1,
      -1,   110,   111,   112,   113,   114,   115,   116,   117,   118,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
     139,   140,   141,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,
      -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,
      -1,   104,   105,    -1,   107,    -1,    -1,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,   139,   140,   141,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,
      -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,
     107,    -1,    -1,   110,   111,   112,   113,   114,   115,   116,
     117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,   139,   140,   141,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,
      91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,
     101,   102,    -1,   104,   105,    -1,    -1,    -1,    -1,   110,
     111,   112,   113,   114,   115,   116,   117,   118,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,   139,   140,
     141,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,
      -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,
     105,    -1,    -1,    -1,    -1,   110,   111,   112,   113,   114,
     115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,
      -1,    -1,    11,    12,   139,   140,   141,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    63,    64,    65,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,
      -1,    -1,   101,   102,    -1,   104,   105,    -1,    -1,    -1,
      -1,   110,   111,   112,   113,   114,   115,   116,   117,   118,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
     139,   140,   141,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,
      -1,   104,   105,    -1,   107,    -1,    -1,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,    -1,    -1,    -1,    16,   139,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,
     101,   102,    -1,   104,   105,    -1,   107,    -1,    -1,   110,
     111,   112,   113,   114,   115,   116,   117,   118,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,
      -1,    -1,    11,    12,    -1,    -1,    -1,    16,   139,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    63,    64,    65,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    94,    -1,    -1,    -1,    98,
      -1,    -1,   101,   102,    -1,   104,   105,    -1,    -1,    -1,
      -1,   110,   111,   112,   113,   114,   115,   116,   117,   118,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
     139,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,
      -1,    -1,    -1,   110,   111,   112,   113,   114,   115,   116,
     117,   118,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,
      -1,    16,   139,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,
     105,    -1,    -1,    -1,    -1,   110,   111,   112,   113,   114,
     115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
      -1,    -1,    -1,    16,   139,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,
      -1,   104,   105,    -1,    -1,    -1,    -1,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,    -1,    -1,    -1,    16,   139,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    63,    64,    65,    33,    34,    35,    36,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    49,    50,    51,    52,    -1,    -1,    -1,    56,    -1,
      -1,    59,    60,    61,    62,    63,    -1,    98,    -1,    -1,
     101,   102,    -1,   104,   105,    -1,    -1,    -1,    -1,   110,
     111,   112,   113,   114,   115,   116,   117,   118,    -1,    -1,
      -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,
      98,    -1,    -1,   101,    -1,    -1,   104,   105,   139,   107,
      -1,    -1,   110,   111,   112,   113,   114,   115,   116,   117,
     118,    33,    34,    35,    36,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   132,    -1,    -1,    49,    50,    51,
      52,   139,    -1,    -1,    56,    -1,    -1,    59,    60,    61,
      62,    63,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,
      -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,
      -1,    -1,   104,   105,    -1,   107,    -1,    -1,   110,   111,
     112,   113,   114,   115,   116,   117,   118,    52,    53,    -1,
      -1,    56,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     132,    -1,    -1,    -1,    -1,    -1,    -1,   139,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    52,    53,    -1,
      -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    -1,    -1,    -1,
      -1,    -1,   147
};

  /* YYSTOS[STATE-NUM] -- The (internal number of the) accessing
     symbol of state STATE-NUM.  */
static const yytype_int16 yystos[] =
{
       0,   155,   156,     0,     1,     3,     4,     5,     6,     7,
      11,    12,    16,    18,    19,    20,    21,    22,    23,    24,
      30,    31,    32,    33,    34,    35,    36,    39,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    59,    60,    61,    62,    63,    64,    65,    75,    76,
      90,    91,    98,   101,   102,   104,   105,   107,   110,   111,
     112,   113,   114,   115,   116,   117,   118,   139,   140,   141,
     157,   158,   159,   167,   169,   171,   179,   180,   186,   187,
     189,   190,   191,   193,   194,   195,   197,   198,   207,   210,
     226,   236,   237,   238,   239,   240,   241,   242,   243,   244,
     245,   246,   255,   275,   283,   284,   335,   336,   337,   338,
     339,   340,   341,   344,   346,   347,   361,   362,   364,   365,
     366,   367,   368,   369,   370,   371,   409,   423,   159,     3,
       4,     5,     6,     7,     8,     9,    10,    11,    12,    13,
      14,    15,    16,    17,    18,    19,    20,    21,    22,    23,
      24,    25,    26,    30,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    56,    75,    76,    77,    78,    79,    80,    81,
      82,    83,    86,    87,    92,    93,    94,    95,   107,   108,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
     140,   141,   147,   201,   202,   203,   205,   206,   361,    39,
      58,    98,   101,   107,   108,   109,   112,   140,   190,   198,
     207,   212,   218,   221,   223,   236,   367,   368,   370,   371,
     407,   408,   218,   148,   219,   220,   148,   215,   219,   148,
     153,   416,    54,   202,   416,   143,   160,   143,    21,    22,
      31,    32,   189,   207,   236,   255,   207,   207,   207,    56,
       1,    47,   101,   163,   164,   165,   167,   192,   193,   423,
     167,   228,   213,   223,   407,   423,   212,   406,   407,   423,
      46,    98,   139,   146,   179,   180,   197,   226,   236,   367,
     368,   371,   276,    54,    55,    57,   201,   350,   363,   350,
     351,   352,   152,   152,   152,   152,   366,   186,   207,   207,
     151,   153,   415,   421,   422,    40,    41,    42,    43,    44,
      37,    38,   148,   374,   375,   376,   377,   423,   374,   376,
      26,   143,   215,   219,   247,   285,    28,   248,   282,   126,
     146,   101,   107,   194,   126,    25,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    94,
      95,   100,   127,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,   209,   209,    68,    96,    97,   145,   413,
     227,   171,   182,   182,   183,   184,   183,   182,   415,   422,
      98,   191,   198,   236,   260,   367,   368,   371,    52,    56,
      94,    98,   199,   200,   236,   367,   368,   371,   200,    33,
      34,    35,    36,    49,    50,    51,    52,    56,   148,   178,
     201,   369,   404,   218,    97,   413,   414,   285,   338,    99,
      99,   146,   212,    56,   212,   212,   212,   350,   126,   100,
     146,   222,   423,    97,   145,   413,    99,    99,   146,   222,
     218,   416,   417,   218,    91,   217,   218,   223,   381,   407,
     423,   171,   417,   171,    54,    64,    65,   168,   148,   208,
     157,   163,    97,   413,    99,   167,   166,   192,   149,   415,
     422,   417,   229,   417,   150,   146,   153,   420,   146,   420,
     144,   420,   416,    56,   366,   194,   196,   375,   146,    97,
     145,   413,   277,    66,   119,   121,   122,   353,   119,   119,
     353,    67,   353,   342,   348,   345,   349,    77,   151,   159,
     182,   182,   182,   182,   167,   171,   171,    52,    54,    55,
      56,    57,    58,    77,    91,   101,   107,   108,   109,   133,
     136,   265,   378,   380,   381,   382,   383,   384,   385,   386,
     387,   388,   391,   392,   393,   394,   395,   398,   399,   400,
     401,   402,   126,   161,   163,   380,   386,   126,   161,   286,
     287,   106,   188,   290,   291,   290,    70,   211,   423,   192,
     146,   197,   146,   211,   175,   207,   207,   207,   207,   207,
     207,   207,   207,   207,   207,   207,   207,   207,   172,   207,
     207,   207,   207,   207,   207,   207,   207,   207,   207,   207,
      52,    53,    56,   205,   215,   410,   411,   217,   223,    52,
      53,    56,   205,   215,   410,   161,    13,   256,   421,   256,
     163,   182,   163,   415,   232,    56,    97,   145,   413,    25,
     171,    52,    56,   199,   130,   372,    97,   145,   413,   235,
     405,    68,    97,   412,    52,    56,   410,   211,   211,   204,
     124,   211,   212,   107,   212,   221,   407,    52,    56,   217,
      52,    56,   211,   211,   408,   417,   149,   417,   146,   417,
     146,   417,   202,   230,   207,   144,   144,   410,   410,   211,
     160,   417,   165,   417,   407,   146,   196,    52,    56,   217,
      52,    56,   278,   355,   354,   119,   343,   353,    66,   119,
     119,   343,    66,   119,   207,   101,   107,   261,   262,   263,
     264,   383,   146,   403,   423,   417,   417,   126,   146,   379,
     212,   146,   403,    34,    52,   146,   379,    52,   146,   379,
      52,   207,    10,   254,     8,   249,   331,   423,   421,   146,
     207,   254,   144,   288,   286,   254,   292,   254,   107,   186,
     212,   223,   224,   225,   417,   196,   146,   169,   170,   186,
     198,   207,   212,   214,   225,   236,   371,   176,   173,   416,
      99,    99,   215,   219,   416,   418,   146,    99,    99,   215,
     216,   219,   423,   254,   163,    13,   163,   254,    27,   257,
     421,   254,    25,   231,   297,    17,   251,   295,    52,    56,
     217,    52,    56,   183,   234,   373,   233,    52,    56,   199,
     217,   161,   171,   181,   216,   219,   170,   207,   214,   170,
     214,   202,   212,   212,   222,    99,    99,   418,    99,    99,
     381,   407,   171,   214,   420,   194,   418,   148,   280,   380,
     356,    54,    55,    57,   360,   371,   152,   353,   152,   152,
     152,   263,   383,   146,   417,   146,   402,   212,   378,   381,
     385,   398,   400,   388,   392,   394,   386,   395,   400,   384,
     386,    44,   212,   225,   332,   423,     9,    15,   250,   252,
     334,   423,    44,   289,   144,   293,   212,   146,    44,   196,
      44,   126,    44,    97,   145,   413,    52,    56,    90,    91,
      98,   101,   104,   105,   132,   275,   304,   305,   306,   309,
     326,   327,   328,   329,   330,   335,   336,   339,   340,   341,
     344,   346,   347,   368,   304,   128,   211,   211,   188,   150,
      99,   211,   211,   188,    14,   252,   253,   258,   259,   423,
     259,   185,   298,   295,   254,   107,   212,   294,   254,   418,
     163,   421,   182,   161,   418,   254,   417,   178,   285,   282,
     211,   211,    99,   211,   211,   417,   146,   417,   380,   279,
     357,   417,   261,   264,   262,   417,   146,   379,   146,   379,
     403,   146,   379,   146,   379,   379,   207,   100,   333,   423,
     163,   162,   207,   131,   270,   271,   423,   270,   107,   212,
     167,   167,   211,   207,    52,    56,   217,    52,    56,   327,
     327,    56,   199,   311,   107,   304,   312,   313,   314,   315,
     316,   318,   418,   310,   416,   419,    52,   100,   177,   131,
      88,    89,    97,   145,   148,   307,   308,   174,   207,   170,
     214,   170,   214,   211,   170,   214,   170,   214,   163,   182,
     254,   254,   299,   254,   212,   146,   256,   254,   161,   421,
     254,   211,   272,   416,    29,   123,   281,   358,   146,   146,
     386,   400,   386,   386,    98,   198,   236,   367,   368,   371,
     256,   163,   265,   266,   269,   272,   384,   386,   387,   389,
     390,   396,   397,   400,   402,   163,   161,   212,   418,   304,
      52,   418,   107,   304,   318,   418,   146,   146,    58,   112,
     319,   320,   321,   322,   323,   324,   325,   392,   144,   328,
     309,   327,   327,   199,   418,   417,   112,   312,   315,   319,
     312,   315,   319,   170,   214,   256,   302,   303,   304,   314,
     315,   319,   107,   212,   163,   254,   149,   151,   161,   163,
     359,   262,   379,   146,   379,   379,   379,    56,    97,   145,
     413,   163,   334,   403,   272,   131,   126,   146,   267,   268,
      98,   236,   146,   403,   146,   267,   146,   267,   417,    52,
     146,   146,   317,   318,   350,   419,   146,   304,    34,    52,
     350,   417,   417,   417,   418,   418,   418,   163,   256,    40,
      41,   146,   212,   259,   295,   296,    52,   273,   274,   382,
     254,   144,   163,   386,    52,    56,   217,    52,    56,   331,
     131,   236,   266,   397,   400,    56,    97,   389,   394,   386,
     396,   400,   386,   146,   317,   146,   124,   321,   325,   258,
     300,   182,   182,   312,   316,   146,   416,   120,   379,   418,
     146,   267,   146,   267,    52,    56,   403,   146,   267,   146,
     267,   267,   317,   146,   316,   318,   163,   146,   274,   386,
     400,   386,   386,   259,   297,   301,   317,   267,   146,   267,
     267,   267,   386,   267
};

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
static const yytype_int16 yyr1[] =
{
       0,   154,   156,   155,   157,   158,   158,   158,   158,   159,
     159,   160,   162,   161,   161,   163,   164,   164,   164,   164,
     165,   166,   165,   168,   167,   167,   167,   167,   167,   167,
     167,   167,   167,   167,   167,   167,   167,   167,   167,   167,
     167,   169,   169,   169,   169,   169,   169,   169,   169,   170,
     170,   170,   171,   171,   171,   171,   171,   172,   173,   174,
     171,   175,   176,   177,   171,   171,   178,   179,   181,   180,
     182,   184,   185,   183,   186,   186,   187,   187,   188,   189,
     190,   190,   190,   190,   190,   190,   190,   190,   190,   190,
     190,   191,   191,   192,   192,   193,   193,   193,   193,   193,
     193,   193,   193,   193,   193,   194,   194,   195,   195,   196,
     196,   197,   197,   197,   197,   197,   197,   197,   197,   197,
     198,   198,   198,   198,   198,   198,   198,   198,   198,   199,
     199,   200,   200,   200,   201,   201,   201,   201,   201,   202,
     202,   203,   204,   203,   205,   205,   205,   205,   205,   205,
     205,   205,   205,   205,   205,   205,   205,   205,   205,   205,
     205,   205,   205,   205,   205,   205,   205,   205,   205,   205,
     205,   205,   205,   205,   206,   206,   206,   206,   206,   206,
     206,   206,   206,   206,   206,   206,   206,   206,   206,   206,
     206,   206,   206,   206,   206,   206,   206,   206,   206,   206,
     206,   206,   206,   206,   206,   206,   206,   206,   206,   206,
     206,   206,   206,   206,   206,   207,   207,   207,   207,   207,
     207,   207,   207,   207,   207,   207,   207,   207,   207,   207,
     207,   207,   207,   207,   207,   207,   207,   207,   207,   207,
     207,   207,   207,   207,   207,   207,   207,   207,   207,   207,
     207,   207,   207,   207,   207,   208,   207,   207,   207,   207,
     207,   207,   207,   209,   209,   209,   209,   210,   210,   211,
     211,   212,   213,   213,   213,   213,   214,   214,   215,   215,
     215,   216,   216,   217,   217,   217,   217,   217,   218,   218,
     218,   218,   218,   220,   219,   221,   222,   222,   223,   223,
     223,   223,   224,   224,   225,   225,   225,   226,   226,   226,
     226,   226,   226,   226,   226,   226,   226,   226,   227,   226,
     228,   226,   229,   226,   226,   226,   226,   226,   226,   226,
     226,   226,   226,   230,   226,   226,   226,   226,   226,   226,
     226,   226,   226,   226,   226,   231,   226,   232,   226,   226,
     226,   233,   226,   234,   226,   235,   226,   226,   226,   226,
     226,   226,   226,   236,   237,   238,   239,   240,   241,   242,
     243,   244,   245,   246,   247,   248,   249,   250,   251,   252,
     253,   254,   255,   256,   256,   256,   257,   257,   258,   258,
     259,   259,   260,   260,   261,   261,   262,   262,   263,   263,
     263,   263,   263,   264,   264,   265,   265,   266,   266,   266,
     266,   267,   267,   268,   269,   269,   269,   269,   269,   269,
     269,   269,   269,   269,   269,   269,   269,   269,   269,   270,
     270,   271,   271,   272,   272,   273,   273,   274,   274,   276,
     277,   278,   279,   275,   280,   280,   281,   281,   282,   283,
     283,   283,   283,   284,   284,   284,   284,   284,   284,   284,
     284,   284,   285,   285,   287,   288,   289,   286,   291,   292,
     293,   290,   294,   294,   294,   294,   295,   296,   296,   298,
     299,   300,   297,   301,   301,   302,   302,   302,   303,   303,
     303,   303,   303,   303,   304,   305,   305,   306,   306,   307,
     308,   309,   309,   309,   309,   309,   309,   309,   309,   309,
     309,   309,   309,   310,   309,   309,   311,   309,   312,   312,
     312,   312,   312,   312,   312,   312,   313,   313,   314,   314,
     315,   316,   316,   317,   317,   318,   319,   319,   319,   319,
     320,   320,   321,   321,   322,   322,   323,   323,   324,   325,
     325,   326,   326,   326,   326,   326,   326,   326,   326,   326,
     326,   327,   327,   327,   327,   327,   327,   327,   327,   327,
     327,   328,   329,   330,   330,   330,   331,   331,   332,   332,
     332,   333,   333,   334,   334,   335,   335,   336,   337,   337,
     337,   338,   339,   340,   341,   342,   342,   343,   343,   344,
     345,   345,   346,   347,   348,   348,   349,   349,   350,   350,
     351,   351,   352,   352,   353,   354,   353,   355,   356,   357,
     358,   359,   353,   360,   360,   360,   360,   361,   361,   362,
     363,   363,   363,   363,   364,   365,   365,   366,   366,   366,
     366,   367,   367,   367,   367,   367,   368,   368,   368,   368,
     368,   368,   368,   369,   369,   370,   370,   371,   371,   373,
     372,   372,   374,   374,   375,   375,   375,   376,   377,   376,
     378,   378,   378,   378,   379,   379,   380,   380,   380,   380,
     380,   380,   380,   380,   380,   380,   380,   380,   380,   380,
     380,   381,   382,   382,   382,   382,   383,   383,   384,   385,
     385,   386,   386,   387,   388,   388,   389,   389,   390,   390,
     391,   391,   392,   392,   393,   394,   394,   395,   396,   397,
     397,   398,   398,   399,   399,   400,   400,   401,   401,   402,
     403,   403,   404,   405,   404,   406,   406,   407,   407,   408,
     408,   408,   408,   409,   409,   409,   410,   410,   410,   410,
     411,   411,   411,   412,   412,   413,   413,   414,   414,   415,
     415,   416,   416,   417,   418,   419,   420,   420,   420,   421,
     421,   422,   422,   423
};

  /* YYR2[YYN] -- Number of symbols on the right hand side of rule YYN.  */
static const yytype_int8 yyr2[] =
{
       0,     2,     0,     2,     2,     1,     1,     3,     2,     1,
       2,     3,     0,     6,     3,     2,     1,     1,     3,     2,
       1,     0,     3,     0,     4,     3,     3,     3,     2,     3,
       3,     3,     3,     3,     4,     1,     4,     4,     6,     4,
       1,     4,     4,     7,     6,     6,     6,     6,     4,     1,
       3,     1,     1,     3,     3,     3,     2,     0,     0,     0,
       6,     0,     0,     0,     6,     1,     1,     2,     0,     5,
       1,     0,     0,     4,     1,     1,     1,     4,     3,     1,
       2,     3,     4,     5,     4,     5,     2,     2,     2,     2,
       2,     1,     3,     1,     3,     1,     2,     3,     5,     2,
       4,     2,     4,     1,     3,     1,     3,     2,     3,     1,
       3,     1,     1,     4,     3,     3,     3,     3,     2,     1,
       1,     1,     4,     3,     3,     3,     3,     2,     1,     1,
       1,     2,     1,     3,     1,     1,     1,     1,     1,     1,
       1,     1,     0,     4,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     4,     4,     7,     6,     6,
       6,     6,     5,     4,     3,     3,     2,     2,     2,     2,
       3,     3,     3,     3,     3,     3,     4,     2,     2,     3,
       3,     3,     3,     1,     3,     3,     3,     3,     3,     2,
       2,     3,     3,     3,     3,     0,     4,     6,     4,     6,
       4,     6,     1,     1,     1,     1,     1,     3,     3,     1,
       1,     1,     1,     2,     4,     2,     1,     3,     3,     5,
       3,     1,     1,     1,     1,     2,     4,     2,     1,     2,
       2,     4,     1,     0,     2,     2,     2,     1,     1,     2,
       3,     4,     1,     1,     3,     4,     2,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     0,     4,
       0,     3,     0,     4,     3,     3,     2,     3,     3,     1,
       4,     3,     1,     0,     6,     4,     3,     2,     1,     2,
       1,     6,     6,     4,     4,     0,     6,     0,     5,     5,
       6,     0,     6,     0,     7,     0,     5,     4,     4,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     2,     1,     1,     1,     5,
       1,     2,     1,     1,     1,     3,     1,     3,     1,     3,
       5,     1,     3,     2,     1,     1,     1,     4,     2,     2,
       1,     2,     0,     1,     6,     8,     4,     6,     4,     2,
       6,     2,     4,     6,     2,     4,     2,     4,     1,     1,
       1,     3,     4,     1,     4,     1,     3,     1,     1,     0,
       0,     0,     0,     7,     4,     1,     3,     3,     3,     2,
       4,     5,     5,     2,     4,     4,     3,     3,     3,     2,
       1,     4,     3,     3,     0,     0,     0,     5,     0,     0,
       0,     5,     1,     2,     3,     4,     5,     1,     1,     0,
       0,     0,     8,     1,     1,     1,     3,     3,     1,     2,
       3,     1,     1,     1,     1,     3,     1,     3,     1,     1,
       1,     1,     4,     4,     4,     3,     4,     4,     4,     3,
       3,     3,     2,     0,     4,     2,     0,     4,     1,     1,
       2,     3,     5,     2,     4,     1,     2,     3,     1,     3,
       5,     2,     1,     1,     3,     1,     3,     1,     2,     1,
       1,     3,     2,     1,     1,     3,     2,     1,     2,     1,
       1,     1,     3,     3,     2,     2,     1,     1,     1,     2,
       2,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     2,     2,     3,     1,     6,     1,     1,     1,
       1,     2,     1,     2,     1,     1,     1,     1,     1,     1,
       2,     3,     3,     3,     4,     0,     3,     1,     2,     4,
       0,     3,     4,     4,     0,     3,     0,     3,     0,     2,
       0,     2,     0,     2,     1,     0,     3,     0,     0,     0,
       0,     0,     8,     1,     1,     1,     1,     1,     1,     2,
       1,     1,     1,     1,     3,     1,     2,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     0,
       4,     0,     1,     1,     3,     5,     3,     1,     0,     3,
       4,     2,     2,     1,     2,     0,     6,     8,     4,     6,
       4,     6,     2,     4,     6,     2,     4,     2,     4,     1,
       0,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       3,     1,     3,     1,     2,     1,     2,     1,     1,     3,
       1,     3,     1,     1,     2,     2,     1,     3,     3,     1,
       3,     1,     3,     1,     1,     2,     1,     1,     1,     2,
       2,     1,     1,     0,     4,     1,     2,     1,     3,     3,
       2,     4,     2,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     0,
       1,     0,     1,     2,     2,     2,     0,     1,     1,     1,
       1,     1,     2,     0
};


#define yyerrok         (yyerrstatus = 0)
#define yyclearin       (yychar = YYEMPTY)
#define YYEMPTY         (-2)
#define YYEOF           0

#define YYACCEPT        goto yyacceptlab
#define YYABORT         goto yyabortlab
#define YYERROR         goto yyerrorlab


#define YYRECOVERING()  (!!yyerrstatus)

#define YYBACKUP(Token, Value)                                    \
  do                                                              \
    if (yychar == YYEMPTY)                                        \
      {                                                           \
        yychar = (Token);                                         \
        yylval = (Value);                                         \
        YYPOPSTACK (yylen);                                       \
        yystate = *yyssp;                                         \
        goto yybackup;                                            \
      }                                                           \
    else                                                          \
      {                                                           \
        yyerror (&yylloc, p, YY_("syntax error: cannot back up")); \
        YYERROR;                                                  \
      }                                                           \
  while (0)

/* Error token number */
#define YYTERROR        1
#define YYERRCODE       256


/* YYLLOC_DEFAULT -- Set CURRENT to span from RHS[1] to RHS[N].
   If N is 0, then set CURRENT to the empty location which ends
   the previous symbol: RHS[0] (always defined).  */

#ifndef YYLLOC_DEFAULT
# define YYLLOC_DEFAULT(Current, Rhs, N)                                \
    do                                                                  \
      if (N)                                                            \
        {                                                               \
          (Current).first_line   = YYRHSLOC (Rhs, 1).first_line;        \
          (Current).first_column = YYRHSLOC (Rhs, 1).first_column;      \
          (Current).last_line    = YYRHSLOC (Rhs, N).last_line;         \
          (Current).last_column  = YYRHSLOC (Rhs, N).last_column;       \
        }                                                               \
      else                                                              \
        {                                                               \
          (Current).first_line   = (Current).last_line   =              \
            YYRHSLOC (Rhs, 0).last_line;                                \
          (Current).first_column = (Current).last_column =              \
            YYRHSLOC (Rhs, 0).last_column;                              \
        }                                                               \
    while (0)
#endif

#define YYRHSLOC(Rhs, K) ((Rhs)[K])


/* Enable debugging if requested.  */
#if YYDEBUG

# ifndef YYFPRINTF
#  include <stdio.h> /* INFRINGES ON USER NAME SPACE */
#  define YYFPRINTF fprintf
# endif

# define YYDPRINTF(Args)                        \
do {                                            \
  if (yydebug)                                  \
    YYFPRINTF Args;                             \
} while (0)


/* YY_LOCATION_PRINT -- Print the location on the stream.
   This macro was not mandated originally: define only if we know
   we won't break user code: when these are the locations we know.  */

#ifndef YY_LOCATION_PRINT
# if defined YYLTYPE_IS_TRIVIAL && YYLTYPE_IS_TRIVIAL

/* Print *YYLOCP on YYO.  Private, do not rely on its existence. */

YY_ATTRIBUTE_UNUSED
static int
yy_location_print_ (FILE *yyo, YYLTYPE const * const yylocp)
{
  int res = 0;
  int end_col = 0 != yylocp->last_column ? yylocp->last_column - 1 : 0;
  if (0 <= yylocp->first_line)
    {
      res += YYFPRINTF (p, "%d", yylocp->first_line);
      if (0 <= yylocp->first_column)
        res += YYFPRINTF (p, ".%d", yylocp->first_column);
    }
  if (0 <= yylocp->last_line)
    {
      if (yylocp->first_line < yylocp->last_line)
        {
          res += YYFPRINTF (p, "-%d", yylocp->last_line);
          if (0 <= end_col)
            res += YYFPRINTF (p, ".%d", end_col);
        }
      else if (0 <= end_col && yylocp->first_column < end_col)
        res += YYFPRINTF (p, "-%d", end_col);
    }
  return res;
 }

#  define YY_LOCATION_PRINT(File, Loc)          \
  yy_location_print_ (File, &(Loc))

# else
#  define YY_LOCATION_PRINT(File, Loc) ((void) 0)
# endif
#endif


# define YY_SYMBOL_PRINT(Title, Type, Value, Location)                    \
do {                                                                      \
  if (yydebug)                                                            \
    {                                                                     \
      YYFPRINTF (p, "%s ", Title);                                   \
      yy_symbol_print (stderr,                                            \
                  Type, Value, Location, p); \
      YYFPRINTF (p, "\n");                                           \
    }                                                                     \
} while (0)


/*-----------------------------------.
| Print this symbol's value on YYO.  |
`-----------------------------------*/

static void
yy_symbol_value_print (FILE *yyo, int yytype, YYSTYPE const * const yyvaluep, YYLTYPE const * const yylocationp, struct parser_params *p)
{
  FILE *yyoutput = yyo;
  YYUSE (yyoutput);
  YYUSE (yylocationp);
  YYUSE (p);
  if (!yyvaluep)
    return;
# ifdef YYPRINT
  if (yytype < YYNTOKENS)
    YYPRINT (yyo, yytoknum[yytype], *yyvaluep);
# endif
  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  switch (yytype)
    {
    case 52: /* "local variable or method"  */
#line 1085 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 5611 "parse.c"
        break;

    case 53: /* "method"  */
#line 1085 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 5623 "parse.c"
        break;

    case 54: /* "global variable"  */
#line 1085 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 5635 "parse.c"
        break;

    case 55: /* "instance variable"  */
#line 1085 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 5647 "parse.c"
        break;

    case 56: /* "constant"  */
#line 1085 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 5659 "parse.c"
        break;

    case 57: /* "class variable"  */
#line 1085 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 5671 "parse.c"
        break;

    case 58: /* "label"  */
#line 1085 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 5683 "parse.c"
        break;

    case 59: /* "integer literal"  */
#line 1092 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 5695 "parse.c"
        break;

    case 60: /* "float literal"  */
#line 1092 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 5707 "parse.c"
        break;

    case 61: /* "rational literal"  */
#line 1092 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 5719 "parse.c"
        break;

    case 62: /* "imaginary literal"  */
#line 1092 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 5731 "parse.c"
        break;

    case 63: /* "char literal"  */
#line 1092 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 5743 "parse.c"
        break;

    case 64: /* "numbered reference"  */
#line 1099 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "$%ld", ((*yyvaluep).node)->nd_nth);
#else
    rb_parser_printf(p, "%"PRIsVALUE, ((*yyvaluep).node));
#endif
}
#line 5755 "parse.c"
        break;

    case 65: /* "back reference"  */
#line 1106 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "$%c", (int)((*yyvaluep).node)->nd_nth);
#else
    rb_parser_printf(p, "%"PRIsVALUE, ((*yyvaluep).node));
#endif
}
#line 5767 "parse.c"
        break;

    case 66: /* "literal content"  */
#line 1092 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 5779 "parse.c"
        break;

    case 99: /* "operator-assignment"  */
#line 1085 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 5791 "parse.c"
        break;

      default:
        break;
    }
  YY_IGNORE_MAYBE_UNINITIALIZED_END
}


/*---------------------------.
| Print this symbol on YYO.  |
`---------------------------*/

static void
yy_symbol_print (FILE *yyo, int yytype, YYSTYPE const * const yyvaluep, YYLTYPE const * const yylocationp, struct parser_params *p)
{
  YYFPRINTF (p, "%s %s (",
             yytype < YYNTOKENS ? "token" : "nterm", yytname[yytype]);

  YY_LOCATION_PRINT (yyo, *yylocationp);
  YYFPRINTF (p, ": ");
  yy_symbol_value_print (yyo, yytype, yyvaluep, yylocationp, p);
  YYFPRINTF (p, ")");
}

/*------------------------------------------------------------------.
| yy_stack_print -- Print the state stack from its BOTTOM up to its |
| TOP (included).                                                   |
`------------------------------------------------------------------*/

static void
ruby_parser_yy_stack_print (yy_state_t *yybottom, yy_state_t *yytop, struct parser_params *p)
#define yy_stack_print(b, t) ruby_parser_yy_stack_print(b, t, p)
{
  YYFPRINTF (p, "Stack now");
  for (; yybottom <= yytop; yybottom++)
    {
      int yybot = *yybottom;
      YYFPRINTF (p, " %d", yybot);
    }
  YYFPRINTF (p, "\n");
}

# define YY_STACK_PRINT(Bottom, Top)                            \
do {                                                            \
  if (yydebug)                                                  \
    yy_stack_print ((Bottom), (Top));                           \
} while (0)


/*------------------------------------------------.
| Report that the YYRULE is going to be reduced.  |
`------------------------------------------------*/

static void
yy_reduce_print (yy_state_t *yyssp, YYSTYPE *yyvsp, YYLTYPE *yylsp, int yyrule, struct parser_params *p)
{
  int yylno = yyrline[yyrule];
  int yynrhs = yyr2[yyrule];
  int yyi;
  YYFPRINTF (p, "Reducing stack by rule %d (line %d):\n",
             yyrule - 1, yylno);
  /* The symbols being reduced.  */
  for (yyi = 0; yyi < yynrhs; yyi++)
    {
      YYFPRINTF (p, "   $%d = ", yyi + 1);
      yy_symbol_print (stderr,
                       yystos[+yyssp[yyi + 1 - yynrhs]],
                       &yyvsp[(yyi + 1) - (yynrhs)]
                       , &(yylsp[(yyi + 1) - (yynrhs)])                       , p);
      YYFPRINTF (p, "\n");
    }
}

# define YY_REDUCE_PRINT(Rule)          \
do {                                    \
  if (yydebug)                          \
    yy_reduce_print (yyssp, yyvsp, yylsp, Rule, p); \
} while (0)

/* Nonzero means print parse trace.  It is left uninitialized so that
   multiple parsers can coexist.  */
#ifndef yydebug
int yydebug;
#endif
#else /* !YYDEBUG */
# define YYDPRINTF(Args)
# define YY_SYMBOL_PRINT(Title, Type, Value, Location)
# define YY_STACK_PRINT(Bottom, Top)
# define YY_REDUCE_PRINT(Rule)
#endif /* !YYDEBUG */


/* YYINITDEPTH -- initial size of the parser's stacks.  */
#ifndef YYINITDEPTH
# define YYINITDEPTH 200
#endif

/* YYMAXDEPTH -- maximum size the stacks can grow to (effective only
   if the built-in stack extension method is used).

   Do not make this value too large; the results are undefined if
   YYSTACK_ALLOC_MAXIMUM < YYSTACK_BYTES (YYMAXDEPTH)
   evaluated with infinite-precision integer arithmetic.  */

#ifndef YYMAXDEPTH
# define YYMAXDEPTH 10000
#endif


#if YYERROR_VERBOSE

# ifndef yystrlen
#  if defined __GLIBC__ && defined _STRING_H
#   define yystrlen(S) (YY_CAST (YYPTRDIFF_T, strlen (S)))
#  else
/* Return the length of YYSTR.  */
static YYPTRDIFF_T
yystrlen (const char *yystr)
{
  YYPTRDIFF_T yylen;
  for (yylen = 0; yystr[yylen]; yylen++)
    continue;
  return yylen;
}
#  endif
# endif

# ifndef yystpcpy
#  if defined __GLIBC__ && defined _STRING_H && defined _GNU_SOURCE
#   define yystpcpy stpcpy
#  else
/* Copy YYSRC to YYDEST, returning the address of the terminating '\0' in
   YYDEST.  */
static char *
yystpcpy (char *yydest, const char *yysrc)
{
  char *yyd = yydest;
  const char *yys = yysrc;

  while ((*yyd++ = *yys++) != '\0')
    continue;

  return yyd - 1;
}
#  endif
# endif

# ifndef yytnamerr
/* Copy to YYRES the contents of YYSTR after stripping away unnecessary
   quotes and backslashes, so that it's suitable for yyerror.  The
   heuristic is that double-quoting is unnecessary unless the string
   contains an apostrophe, a comma, or backslash (other than
   backslash-backslash).  YYSTR is taken from yytname.  If YYRES is
   null, do not copy; instead, return the length of what the result
   would have been.  */
static YYPTRDIFF_T
yytnamerr (char *yyres, const char *yystr)
{
  if (*yystr == '"')
    {
      YYPTRDIFF_T yyn = 0;
      char const *yyp = yystr;

      for (;;)
        switch (*++yyp)
          {
          case '\'':
          case ',':
            goto do_not_strip_quotes;

          case '\\':
            if (*++yyp != '\\')
              goto do_not_strip_quotes;
            else
              goto append;

          append:
          default:
            if (yyres)
              yyres[yyn] = *yyp;
            yyn++;
            break;

          case '"':
            if (yyres)
              yyres[yyn] = '\0';
            return yyn;
          }
    do_not_strip_quotes: ;
    }

  if (yyres)
    return yystpcpy (yyres, yystr) - yyres;
  else
    return yystrlen (yystr);
}
# endif

/* Copy into *YYMSG, which is of size *YYMSG_ALLOC, an error message
   about the unexpected token YYTOKEN for the state stack whose top is
   YYSSP.

   Return 0 if *YYMSG was successfully written.  Return 1 if *YYMSG is
   not large enough to hold the message.  In that case, also set
   *YYMSG_ALLOC to the required number of bytes.  Return 2 if the
   required number of bytes is too large to store.  */
static int
yysyntax_error (struct parser_params *p, YYPTRDIFF_T *yymsg_alloc, char **yymsg,
                yy_state_t *yyssp, int yytoken)
{
  enum { YYERROR_VERBOSE_ARGS_MAXIMUM = 5 };
  /* Internationalized format string. */
  const char *yyformat = YY_NULLPTR;
  /* Arguments of yyformat: reported tokens (one for the "unexpected",
     one per "expected"). */
  char const *yyarg[YYERROR_VERBOSE_ARGS_MAXIMUM];
  /* Actual size of YYARG. */
  int yycount = 0;
  /* Cumulated lengths of YYARG.  */
  YYPTRDIFF_T yysize = 0;

  /* There are many possibilities here to consider:
     - If this state is a consistent state with a default action, then
       the only way this function was invoked is if the default action
       is an error action.  In that case, don't check for expected
       tokens because there are none.
     - The only way there can be no lookahead present (in yychar) is if
       this state is a consistent state with a default action.  Thus,
       detecting the absence of a lookahead is sufficient to determine
       that there is no unexpected or expected token to report.  In that
       case, just report a simple "syntax error".
     - Don't assume there isn't a lookahead just because this state is a
       consistent state with a default action.  There might have been a
       previous inconsistent state, consistent state with a non-default
       action, or user semantic action that manipulated yychar.
     - Of course, the expected token list depends on states to have
       correct lookahead information, and it depends on the parser not
       to perform extra reductions after fetching a lookahead from the
       scanner and before detecting a syntax error.  Thus, state merging
       (from LALR or IELR) and default reductions corrupt the expected
       token list.  However, the list is correct for canonical LR with
       one exception: it will still contain any token that will not be
       accepted due to an error action in a later state.
  */
  if (yytoken != YYEMPTY)
    {
      int yyn = yypact[+*yyssp];
      YYPTRDIFF_T yysize0 = yytnamerr (YY_NULLPTR, yytname[yytoken]);
      yysize = yysize0;
      yyarg[yycount++] = yytname[yytoken];
      if (!yypact_value_is_default (yyn))
        {
          /* Start YYX at -YYN if negative to avoid negative indexes in
             YYCHECK.  In other words, skip the first -YYN actions for
             this state because they are default actions.  */
          int yyxbegin = yyn < 0 ? -yyn : 0;
          /* Stay within bounds of both yycheck and yytname.  */
          int yychecklim = YYLAST - yyn + 1;
          int yyxend = yychecklim < YYNTOKENS ? yychecklim : YYNTOKENS;
          int yyx;

          for (yyx = yyxbegin; yyx < yyxend; ++yyx)
            if (yycheck[yyx + yyn] == yyx && yyx != YYTERROR
                && !yytable_value_is_error (yytable[yyx + yyn]))
              {
                if (yycount == YYERROR_VERBOSE_ARGS_MAXIMUM)
                  {
                    yycount = 1;
                    yysize = yysize0;
                    break;
                  }
                yyarg[yycount++] = yytname[yyx];
                {
                  YYPTRDIFF_T yysize1
                    = yysize + yytnamerr (YY_NULLPTR, yytname[yyx]);
                  if (yysize <= yysize1 && yysize1 <= YYSTACK_ALLOC_MAXIMUM)
                    yysize = yysize1;
                  else
                    return 2;
                }
              }
        }
    }

  switch (yycount)
    {
# define YYCASE_(N, S)                      \
      case N:                               \
        yyformat = S;                       \
      break
    default: /* Avoid compiler warnings. */
      YYCASE_(0, YY_("syntax error"));
      YYCASE_(1, YY_("syntax error, unexpected %s"));
      YYCASE_(2, YY_("syntax error, unexpected %s, expecting %s"));
      YYCASE_(3, YY_("syntax error, unexpected %s, expecting %s or %s"));
      YYCASE_(4, YY_("syntax error, unexpected %s, expecting %s or %s or %s"));
      YYCASE_(5, YY_("syntax error, unexpected %s, expecting %s or %s or %s or %s"));
# undef YYCASE_
    }

  {
    /* Don't count the "%s"s in the final size, but reserve room for
       the terminator.  */
    YYPTRDIFF_T yysize1 = yysize + (yystrlen (yyformat) - 2 * yycount) + 1;
    if (yysize <= yysize1 && yysize1 <= YYSTACK_ALLOC_MAXIMUM)
      yysize = yysize1;
    else
      return 2;
  }

  if (*yymsg_alloc < yysize)
    {
      *yymsg_alloc = 2 * yysize;
      if (! (yysize <= *yymsg_alloc
             && *yymsg_alloc <= YYSTACK_ALLOC_MAXIMUM))
        *yymsg_alloc = YYSTACK_ALLOC_MAXIMUM;
      return 1;
    }

  /* Avoid sprintf, as that infringes on the user's name space.
     Don't have undefined behavior even if the translation
     produced a string with the wrong number of "%s"s.  */
  {
    char *yyp = *yymsg;
    int yyi = 0;
    while ((*yyp = *yyformat) != '\0')
      if (*yyp == '%' && yyformat[1] == 's' && yyi < yycount)
        {
          yyp += yytnamerr (yyp, yyarg[yyi++]);
          yyformat += 2;
        }
      else
        {
          ++yyp;
          ++yyformat;
        }
  }
  return 0;
}
#endif /* YYERROR_VERBOSE */

/*-----------------------------------------------.
| Release the memory associated to this symbol.  |
`-----------------------------------------------*/

static void
yydestruct (const char *yymsg, int yytype, YYSTYPE *yyvaluep, YYLTYPE *yylocationp, struct parser_params *p)
{
  YYUSE (yyvaluep);
  YYUSE (yylocationp);
  YYUSE (p);
  if (!yymsg)
    yymsg = "Deleting";
  YY_SYMBOL_PRINT (yymsg, yytype, yyvaluep, yylocationp);

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  YYUSE (yytype);
  YY_IGNORE_MAYBE_UNINITIALIZED_END
}




/*----------.
| yyparse.  |
`----------*/

int
yyparse (struct parser_params *p)
{
/* The lookahead symbol.  */
int yychar;


/* The semantic value of the lookahead symbol.  */
/* Default value used for initialization, for pacifying older GCCs
   or non-GCC compilers.  */
YY_INITIAL_VALUE (static YYSTYPE yyval_default;)
YYSTYPE yylval YY_INITIAL_VALUE (= yyval_default);

/* Location data for the lookahead symbol.  */
static YYLTYPE yyloc_default
# if defined YYLTYPE_IS_TRIVIAL && YYLTYPE_IS_TRIVIAL
  = { 1, 1, 1, 1 }
# endif
;
YYLTYPE yylloc = yyloc_default;

    /* Number of syntax errors so far.  */
    int yynerrs;

    yy_state_fast_t yystate;
    /* Number of tokens to shift before error messages enabled.  */
    int yyerrstatus;

    /* The stacks and their tools:
       'yyss': related to states.
       'yyvs': related to semantic values.
       'yyls': related to locations.

       Refer to the stacks through separate pointers, to allow yyoverflow
       to reallocate them elsewhere.  */

    /* The state stack.  */
    yy_state_t yyssa[YYINITDEPTH];
    yy_state_t *yyss;
    yy_state_t *yyssp;

    /* The semantic value stack.  */
    YYSTYPE yyvsa[YYINITDEPTH];
    YYSTYPE *yyvs;
    YYSTYPE *yyvsp;

    /* The location stack.  */
    YYLTYPE yylsa[YYINITDEPTH];
    YYLTYPE *yyls;
    YYLTYPE *yylsp;

    /* The locations where the error started and ended.  */
    YYLTYPE yyerror_range[3];

    YYPTRDIFF_T yystacksize;

  int yyn;
  int yyresult;
  /* Lookahead token as an internal (translated) token number.  */
  int yytoken = 0;
  /* The variables used to return semantic value and location from the
     action routines.  */
  YYSTYPE yyval;
  YYLTYPE yyloc;

#if YYERROR_VERBOSE
  /* Buffer for error messages, and its allocated size.  */
  char yymsgbuf[128];
  char *yymsg = yymsgbuf;
  YYPTRDIFF_T yymsg_alloc = sizeof yymsgbuf;
#endif

#define YYPOPSTACK(N)   (yyvsp -= (N), yyssp -= (N), yylsp -= (N))

  /* The number of symbols on the RHS of the reduced rule.
     Keep to zero when no symbol should be popped.  */
  int yylen = 0;

  yyssp = yyss = yyssa;
  yyvsp = yyvs = yyvsa;
  yylsp = yyls = yylsa;
  yystacksize = YYINITDEPTH;

  YYDPRINTF ((p, "Starting parse\n"));

  yystate = 0;
  yyerrstatus = 0;
  yynerrs = 0;
  yychar = YYEMPTY; /* Cause a token to be read.  */

/* User initialization code.  */
#line 1117 "parse.y"
{
    RUBY_SET_YYLLOC_OF_NONE(yylloc);
}

#line 6253 "parse.c"

  yylsp[0] = yylloc;
  goto yysetstate;


/*------------------------------------------------------------.
| yynewstate -- push a new state, which is found in yystate.  |
`------------------------------------------------------------*/
yynewstate:
  /* In all cases, when you get here, the value and location stacks
     have just been pushed.  So pushing a state here evens the stacks.  */
  yyssp++;


/*--------------------------------------------------------------------.
| yysetstate -- set current state (the top of the stack) to yystate.  |
`--------------------------------------------------------------------*/
yysetstate:
  YYDPRINTF ((p, "Entering state %d\n", yystate));
  YY_ASSERT (0 <= yystate && yystate < YYNSTATES);
  YY_IGNORE_USELESS_CAST_BEGIN
  *yyssp = YY_CAST (yy_state_t, yystate);
  YY_IGNORE_USELESS_CAST_END

  if (yyss + yystacksize - 1 <= yyssp)
#if !defined yyoverflow && !defined YYSTACK_RELOCATE
    goto yyexhaustedlab;
#else
    {
      /* Get the current used size of the three stacks, in elements.  */
      YYPTRDIFF_T yysize = yyssp - yyss + 1;

# if defined yyoverflow
      {
        /* Give user a chance to reallocate the stack.  Use copies of
           these so that the &'s don't force the real ones into
           memory.  */
        yy_state_t *yyss1 = yyss;
        YYSTYPE *yyvs1 = yyvs;
        YYLTYPE *yyls1 = yyls;

        /* Each stack pointer address is followed by the size of the
           data in use in that stack, in bytes.  This used to be a
           conditional around just the two extra args, but that might
           be undefined if yyoverflow is a macro.  */
        yyoverflow (YY_("memory exhausted"),
                    &yyss1, yysize * YYSIZEOF (*yyssp),
                    &yyvs1, yysize * YYSIZEOF (*yyvsp),
                    &yyls1, yysize * YYSIZEOF (*yylsp),
                    &yystacksize);
        yyss = yyss1;
        yyvs = yyvs1;
        yyls = yyls1;
      }
# else /* defined YYSTACK_RELOCATE */
      /* Extend the stack our own way.  */
      if (YYMAXDEPTH <= yystacksize)
        goto yyexhaustedlab;
      yystacksize *= 2;
      if (YYMAXDEPTH < yystacksize)
        yystacksize = YYMAXDEPTH;

      {
        yy_state_t *yyss1 = yyss;
        union yyalloc *yyptr =
          YY_CAST (union yyalloc *,
                   YYSTACK_ALLOC (YY_CAST (YYSIZE_T, YYSTACK_BYTES (yystacksize))));
        if (! yyptr)
          goto yyexhaustedlab;
        YYSTACK_RELOCATE (yyss_alloc, yyss);
        YYSTACK_RELOCATE (yyvs_alloc, yyvs);
        YYSTACK_RELOCATE (yyls_alloc, yyls);
# undef YYSTACK_RELOCATE
        if (yyss1 != yyssa)
          YYSTACK_FREE (yyss1);
      }
# endif

      yyssp = yyss + yysize - 1;
      yyvsp = yyvs + yysize - 1;
      yylsp = yyls + yysize - 1;

      YY_IGNORE_USELESS_CAST_BEGIN
      YYDPRINTF ((p, "Stack size increased to %ld\n",
                  YY_CAST (long, yystacksize)));
      YY_IGNORE_USELESS_CAST_END

      if (yyss + yystacksize - 1 <= yyssp)
        YYABORT;
    }
#endif /* !defined yyoverflow && !defined YYSTACK_RELOCATE */

  if (yystate == YYFINAL)
    YYACCEPT;

  goto yybackup;


/*-----------.
| yybackup.  |
`-----------*/
yybackup:
  /* Do appropriate processing given the current state.  Read a
     lookahead token if we need one and don't already have one.  */

  /* First try to decide what to do without reference to lookahead token.  */
  yyn = yypact[yystate];
  if (yypact_value_is_default (yyn))
    goto yydefault;

  /* Not known => get a lookahead token if don't already have one.  */

  /* YYCHAR is either YYEMPTY or YYEOF or a valid lookahead symbol.  */
  if (yychar == YYEMPTY)
    {
      YYDPRINTF ((p, "Reading a token: "));
      yychar = yylex (&yylval, &yylloc, p);
    }

  if (yychar <= YYEOF)
    {
      yychar = yytoken = YYEOF;
      YYDPRINTF ((p, "Now at end of input.\n"));
    }
  else
    {
      yytoken = YYTRANSLATE (yychar);
      YY_SYMBOL_PRINT ("Next token is", yytoken, &yylval, &yylloc);
    }

  /* If the proper action on seeing token YYTOKEN is to reduce or to
     detect an error, take that action.  */
  yyn += yytoken;
  if (yyn < 0 || YYLAST < yyn || yycheck[yyn] != yytoken)
    goto yydefault;
  yyn = yytable[yyn];
  if (yyn <= 0)
    {
      if (yytable_value_is_error (yyn))
        goto yyerrlab;
      yyn = -yyn;
      goto yyreduce;
    }

  /* Count tokens shifted since error; after three, turn off error
     status.  */
  if (yyerrstatus)
    yyerrstatus--;

  /* Shift the lookahead token.  */
  YY_SYMBOL_PRINT ("Shifting", yytoken, &yylval, &yylloc);
  yystate = yyn;
  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END
  *++yylsp = yylloc;

  /* Discard the shifted token.  */
  yychar = YYEMPTY;
  goto yynewstate;


/*-----------------------------------------------------------.
| yydefault -- do the default action for the current state.  |
`-----------------------------------------------------------*/
yydefault:
  yyn = yydefact[yystate];
  if (yyn == 0)
    goto yyerrlab;
  goto yyreduce;


/*-----------------------------.
| yyreduce -- do a reduction.  |
`-----------------------------*/
yyreduce:
  /* yyn is the number of a rule to reduce with.  */
  yylen = yyr2[yyn];

  /* If YYLEN is nonzero, implement the default value of the action:
     '$$ = $1'.

     Otherwise, the following line sets YYVAL to garbage.
     This behavior is undocumented and Bison
     users should not rely upon it.  Assigning to YYVAL
     unconditionally makes the parser a bit smaller, and it avoids a
     GCC warning that YYVAL may be used uninitialized.  */
  yyval = yyvsp[1-yylen];

  /* Default location. */
  YYLLOC_DEFAULT (yyloc, (yylsp - yylen), yylen);
  yyerror_range[1] = yyloc;
  YY_REDUCE_PRINT (yyn);
  switch (yyn)
    {
  case 2:
#line 1322 "parse.y"
                   {
			SET_LEX_STATE(EXPR_BEG);
			local_push(p, ifndef_ripper(1)+0);
		    }
#line 6455 "parse.c"
    break;

  case 3:
#line 1327 "parse.y"
                    {
		    /*%%%*/
			if ((yyvsp[0].node) && !compile_for_eval) {
			    NODE *node = (yyvsp[0].node);
			    /* last expression should not be void */
			    if (nd_type(node) == NODE_BLOCK) {
				while (node->nd_next) {
				    node = node->nd_next;
				}
				node = node->nd_head;
			    }
			    node = remove_begin(node);
			    void_expr(p, node);
			}
			p->eval_tree = NEW_SCOPE(0, block_append(p, p->eval_tree, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper[final]: program!($2) %*/
			local_pop(p);
		    }
#line 6479 "parse.c"
    break;

  case 4:
#line 1349 "parse.y"
                    {
			(yyval.node) = void_stmts(p, (yyvsp[-1].node));
		    }
#line 6487 "parse.c"
    break;

  case 5:
#line 1355 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, void_stmt!) %*/
		    }
#line 6498 "parse.c"
    break;

  case 6:
#line 1362 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = newline_node((yyvsp[0].node));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, $1) %*/
		    }
#line 6509 "parse.c"
    break;

  case 7:
#line 1369 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = block_append(p, (yyvsp[-2].node), newline_node((yyvsp[0].node)));
		    /*% %*/
		    /*% ripper: stmts_add!($1, $3) %*/
		    }
#line 6520 "parse.c"
    break;

  case 8:
#line 1376 "parse.y"
                    {
			(yyval.node) = remove_begin((yyvsp[0].node));
		    }
#line 6528 "parse.c"
    break;

  case 10:
#line 1383 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 6536 "parse.c"
    break;

  case 11:
#line 1389 "parse.y"
                    {
		    /*%%%*/
			p->eval_tree_begin = block_append(p, p->eval_tree_begin,
							  NEW_BEGIN((yyvsp[-1].node), &(yyloc)));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: BEGIN!($2) %*/
		    }
#line 6549 "parse.c"
    break;

  case 12:
#line 1401 "parse.y"
                         {if (!(yyvsp[-1].node)) {yyerror1(&(yylsp[0]), "else without rescue is useless");}}
#line 6555 "parse.c"
    break;

  case 13:
#line 1404 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_bodystmt(p, (yyvsp[-5].node), (yyvsp[-4].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: bodystmt!(escape_Qundef($1), escape_Qundef($2), escape_Qundef($5), escape_Qundef($6)) %*/
		    }
#line 6566 "parse.c"
    break;

  case 14:
#line 1413 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_bodystmt(p, (yyvsp[-2].node), (yyvsp[-1].node), 0, (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: bodystmt!(escape_Qundef($1), escape_Qundef($2), Qnil, escape_Qundef($3)) %*/
		    }
#line 6577 "parse.c"
    break;

  case 15:
#line 1422 "parse.y"
                    {
			(yyval.node) = void_stmts(p, (yyvsp[-1].node));
		    }
#line 6585 "parse.c"
    break;

  case 16:
#line 1428 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, void_stmt!) %*/
		    }
#line 6596 "parse.c"
    break;

  case 17:
#line 1435 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = newline_node((yyvsp[0].node));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, $1) %*/
		    }
#line 6607 "parse.c"
    break;

  case 18:
#line 1442 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = block_append(p, (yyvsp[-2].node), newline_node((yyvsp[0].node)));
		    /*% %*/
		    /*% ripper: stmts_add!($1, $3) %*/
		    }
#line 6618 "parse.c"
    break;

  case 19:
#line 1449 "parse.y"
                    {
			(yyval.node) = remove_begin((yyvsp[0].node));
		    }
#line 6626 "parse.c"
    break;

  case 20:
#line 1455 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 6634 "parse.c"
    break;

  case 21:
#line 1459 "parse.y"
                    {
			yyerror1(&(yylsp[0]), "BEGIN is permitted only at toplevel");
		    }
#line 6642 "parse.c"
    break;

  case 22:
#line 1463 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 6650 "parse.c"
    break;

  case 23:
#line 1468 "parse.y"
                                      {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 6656 "parse.c"
    break;

  case 24:
#line 1469 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_ALIAS((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: alias!($2, $4) %*/
		    }
#line 6667 "parse.c"
    break;

  case 25:
#line 1476 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_VALIAS((yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_alias!($2, $3) %*/
		    }
#line 6678 "parse.c"
    break;

  case 26:
#line 1483 "parse.y"
                    {
		    /*%%%*/
			char buf[2];
			buf[0] = '$';
			buf[1] = (char)(yyvsp[0].node)->nd_nth;
			(yyval.node) = NEW_VALIAS((yyvsp[-1].id), rb_intern2(buf, 2), &(yyloc));
		    /*% %*/
		    /*% ripper: var_alias!($2, $3) %*/
		    }
#line 6692 "parse.c"
    break;

  case 27:
#line 1493 "parse.y"
                    {
			static const char mesg[] = "can't make alias for the number variables";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: alias_error!(ERR_MESG(), $3) %*/
		    }
#line 6705 "parse.c"
    break;

  case 28:
#line 1502 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: undef!($2) %*/
		    }
#line 6716 "parse.c"
    break;

  case 29:
#line 1509 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[0].node), remove_begin((yyvsp[-2].node)), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: if_mod!($3, $1) %*/
		    }
#line 6728 "parse.c"
    break;

  case 30:
#line 1517 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_unless(p, (yyvsp[0].node), remove_begin((yyvsp[-2].node)), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: unless_mod!($3, $1) %*/
		    }
#line 6740 "parse.c"
    break;

  case 31:
#line 1525 "parse.y"
                    {
		    /*%%%*/
			if ((yyvsp[-2].node) && nd_type((yyvsp[-2].node)) == NODE_BEGIN) {
			    (yyval.node) = NEW_WHILE(cond(p, (yyvsp[0].node), &(yylsp[0])), (yyvsp[-2].node)->nd_body, 0, &(yyloc));
			}
			else {
			    (yyval.node) = NEW_WHILE(cond(p, (yyvsp[0].node), &(yylsp[0])), (yyvsp[-2].node), 1, &(yyloc));
			}
		    /*% %*/
		    /*% ripper: while_mod!($3, $1) %*/
		    }
#line 6756 "parse.c"
    break;

  case 32:
#line 1537 "parse.y"
                    {
		    /*%%%*/
			if ((yyvsp[-2].node) && nd_type((yyvsp[-2].node)) == NODE_BEGIN) {
			    (yyval.node) = NEW_UNTIL(cond(p, (yyvsp[0].node), &(yylsp[0])), (yyvsp[-2].node)->nd_body, 0, &(yyloc));
			}
			else {
			    (yyval.node) = NEW_UNTIL(cond(p, (yyvsp[0].node), &(yylsp[0])), (yyvsp[-2].node), 1, &(yyloc));
			}
		    /*% %*/
		    /*% ripper: until_mod!($3, $1) %*/
		    }
#line 6772 "parse.c"
    break;

  case 33:
#line 1549 "parse.y"
                    {
		    /*%%%*/
			NODE *resq;
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			resq = NEW_RESBODY(0, remove_begin((yyvsp[0].node)), 0, &loc);
			(yyval.node) = NEW_RESCUE(remove_begin((yyvsp[-2].node)), resq, 0, &(yyloc));
		    /*% %*/
		    /*% ripper: rescue_mod!($1, $3) %*/
		    }
#line 6786 "parse.c"
    break;

  case 34:
#line 1559 "parse.y"
                    {
			if (p->ctxt.in_def) {
			    rb_warn0("END in method; use at_exit");
			}
		    /*%%%*/
			{
			    NODE *scope = NEW_NODE(
				NODE_SCOPE, 0 /* tbl */, (yyvsp[-1].node) /* body */, 0 /* args */, &(yyloc));
			    (yyval.node) = NEW_POSTEXE(scope, &(yyloc));
			}
		    /*% %*/
		    /*% ripper: END!($3) %*/
		    }
#line 6804 "parse.c"
    break;

  case 36:
#line 1574 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: massign!($1, $4) %*/
		    }
#line 6816 "parse.c"
    break;

  case 37:
#line 1582 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: assign!($1, $4) %*/
		    }
#line 6828 "parse.c"
    break;

  case 38:
#line 1590 "parse.y"
                    {
                    /*%%%*/
                        YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
                        value_expr((yyvsp[-2].node));
			(yyval.node) = node_assign(p, (yyvsp[-5].node), NEW_RESCUE((yyvsp[-2].node), NEW_RESBODY(0, remove_begin((yyvsp[0].node)), 0, &loc), 0, &(yyloc)), (yyvsp[-3].ctxt), &(yyloc));
                    /*% %*/
                    /*% ripper: massign!($1, rescue_mod!($4, $6)) %*/
                    }
#line 6841 "parse.c"
    break;

  case 39:
#line 1599 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: massign!($1, $4) %*/
		    }
#line 6852 "parse.c"
    break;

  case 41:
#line 1609 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: assign!($1, $4) %*/
		    }
#line 6863 "parse.c"
    break;

  case 42:
#line 1616 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_op_assign(p, (yyvsp[-3].node), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!($1, $2, $4) %*/
		    }
#line 6874 "parse.c"
    break;

  case 43:
#line 1623 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_ary_op_assign(p, (yyvsp[-6].node), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[0].node), &(yylsp[-4]), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(aref_field!($1, escape_Qundef($3)), $5, $7) %*/

		    }
#line 6886 "parse.c"
    break;

  case 44:
#line 1631 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 6897 "parse.c"
    break;

  case 45:
#line 1638 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 6908 "parse.c"
    break;

  case 46:
#line 1645 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-5]), &(yylsp[-3]));
			(yyval.node) = new_const_op_assign(p, NEW_COLON2((yyvsp[-5].node), (yyvsp[-3].id), &loc), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(const_path_field!($1, $3), $4, $6) %*/
		    }
#line 6920 "parse.c"
    break;

  case 47:
#line 1653 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), ID2VAL(idCOLON2), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, ID2VAL(idCOLON2), $3), $4, $6) %*/
		    }
#line 6931 "parse.c"
    break;

  case 48:
#line 1660 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[-3].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), assign!(var_field(p, $1), $4)) %*/
		    }
#line 6943 "parse.c"
    break;

  case 49:
#line 1670 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 6952 "parse.c"
    break;

  case 50:
#line 1675 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			value_expr((yyvsp[-2].node));
			(yyval.node) = NEW_RESCUE((yyvsp[-2].node), NEW_RESBODY(0, remove_begin((yyvsp[0].node)), 0, &loc), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: rescue_mod!($1, $3) %*/
		    }
#line 6965 "parse.c"
    break;

  case 53:
#line 1688 "parse.y"
                    {
			(yyval.node) = logop(p, idAND, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 6973 "parse.c"
    break;

  case 54:
#line 1692 "parse.y"
                    {
			(yyval.node) = logop(p, idOR, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 6981 "parse.c"
    break;

  case 55:
#line 1696 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[0].node), &(yylsp[0])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 6989 "parse.c"
    break;

  case 56:
#line 1700 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[0].node), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 6997 "parse.c"
    break;

  case 57:
#line 1704 "parse.y"
                    {
			value_expr((yyvsp[-1].node));
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyval.ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
		    }
#line 7009 "parse.c"
    break;

  case 58:
#line 1711 "parse.y"
                    {(yyval.tbl) = push_pvtbl(p);}
#line 7015 "parse.c"
    break;

  case 59:
#line 1713 "parse.y"
                    {pop_pvtbl(p, (yyvsp[-1].tbl));}
#line 7021 "parse.c"
    break;

  case 60:
#line 1714 "parse.y"
                    {
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
		    /*%%%*/
			(yyval.node) = NEW_CASE3((yyvsp[-5].node), NEW_IN((yyvsp[-1].node), 0, 0, &(yylsp[-1])), &(yyloc));
			warn_one_line_pattern_matching(p, (yyval.node), (yyvsp[-1].node), true);
		    /*% %*/
		    /*% ripper: case!($1, in!($5, Qnil, Qnil)) %*/
		    }
#line 7034 "parse.c"
    break;

  case 61:
#line 1723 "parse.y"
                    {
			value_expr((yyvsp[-1].node));
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyval.ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
		    }
#line 7046 "parse.c"
    break;

  case 62:
#line 1730 "parse.y"
                    {(yyval.tbl) = push_pvtbl(p);}
#line 7052 "parse.c"
    break;

  case 63:
#line 1732 "parse.y"
                    {pop_pvtbl(p, (yyvsp[-1].tbl));}
#line 7058 "parse.c"
    break;

  case 64:
#line 1733 "parse.y"
                    {
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
		    /*%%%*/
			(yyval.node) = NEW_CASE3((yyvsp[-5].node), NEW_IN((yyvsp[-1].node), NEW_TRUE(&(yylsp[-1])), NEW_FALSE(&(yylsp[-1])), &(yylsp[-1])), &(yyloc));
			warn_one_line_pattern_matching(p, (yyval.node), (yyvsp[-1].node), false);
		    /*% %*/
		    /*% ripper: case!($1, in!($5, Qnil, Qnil)) %*/
		    }
#line 7071 "parse.c"
    break;

  case 66:
#line 1745 "parse.y"
                    {
			ID fname = get_id((yyvsp[0].id));
			ID cur_arg = p->cur_arg;
			YYSTYPE c = {.ctxt = p->ctxt};
			numparam_name(p, fname);
			local_push(p, 0);
			p->cur_arg = 0;
			p->ctxt.in_def = 1;
			(yyval.node) = NEW_NODE(NODE_SELF, /*vid*/cur_arg, /*mid*/fname, /*cval*/c.val, &(yyloc));
		    /*%%%*/
		    /*%
			$$ = NEW_RIPPER(fname, get_value($1), $$, &NULL_LOC);
		    %*/
		    }
#line 7090 "parse.c"
    break;

  case 67:
#line 1762 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    /*%%%*/
			(yyval.node) = NEW_NODE(NODE_DEFN, 0, (yyval.node)->nd_mid, (yyval.node), &(yyloc));
		    /*% %*/
		    }
#line 7101 "parse.c"
    break;

  case 68:
#line 1770 "parse.y"
                                               {SET_LEX_STATE(EXPR_FNAME);}
#line 7107 "parse.c"
    break;

  case 69:
#line 1771 "parse.y"
                    {
			SET_LEX_STATE(EXPR_ENDFN|EXPR_LABEL); /* force for args */
			(yyval.node) = (yyvsp[0].node);
		    /*%%%*/
			(yyval.node) = NEW_NODE(NODE_DEFS, (yyvsp[-3].node), (yyval.node)->nd_mid, (yyval.node), &(yyloc));
		    /*%
			VALUE ary = rb_ary_new_from_args(3, $2, $3, get_value($$));
			add_mark_object(p, ary);
			$<node>$->nd_rval = ary;
		    %*/
		    }
#line 7123 "parse.c"
    break;

  case 70:
#line 1785 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 7132 "parse.c"
    break;

  case 71:
#line 1791 "parse.y"
                  {COND_PUSH(1);}
#line 7138 "parse.c"
    break;

  case 72:
#line 1791 "parse.y"
                                                {COND_POP();}
#line 7144 "parse.c"
    break;

  case 73:
#line 1792 "parse.y"
                    {
			(yyval.node) = (yyvsp[-2].node);
		    }
#line 7152 "parse.c"
    break;

  case 77:
#line 1803 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, $2, $3), $4) %*/
		    }
#line 7163 "parse.c"
    break;

  case 78:
#line 1812 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 7175 "parse.c"
    break;

  case 79:
#line 1822 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_FCALL((yyvsp[0].id), 0, &(yyloc));
			nd_set_line((yyval.node), p->tokline);
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 7187 "parse.c"
    break;

  case 80:
#line 1832 "parse.y"
                    {
		    /*%%%*/
			(yyvsp[-1].node)->nd_args = (yyvsp[0].node);
			nd_set_last_loc((yyvsp[-1].node), (yylsp[0]).end_pos);
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: command!($1, $2) %*/
		    }
#line 7200 "parse.c"
    break;

  case 81:
#line 1841 "parse.y"
                    {
		    /*%%%*/
			block_dup_check(p, (yyvsp[-1].node), (yyvsp[0].node));
			(yyvsp[-2].node)->nd_args = (yyvsp[-1].node);
			(yyval.node) = method_add_block(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-2].node));
			nd_set_last_loc((yyvsp[-2].node), (yylsp[-1]).end_pos);
		    /*% %*/
		    /*% ripper: method_add_block!(command!($1, $2), $3) %*/
		    }
#line 7215 "parse.c"
    break;

  case 82:
#line 1852 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), Qnull, &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: command_call!($1, $2, $3, $4) %*/
		    }
#line 7226 "parse.c"
    break;

  case 83:
#line 1859 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-3].id), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(command_call!($1, $2, $3, $4), $5) %*/
		    }
#line 7237 "parse.c"
    break;

  case 84:
#line 1866 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), Qnull, &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: command_call!($1, ID2VAL(idCOLON2), $3, $4) %*/
		    }
#line 7248 "parse.c"
    break;

  case 85:
#line 1873 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(command_call!($1, ID2VAL(idCOLON2), $3, $4), $5) %*/
		   }
#line 7259 "parse.c"
    break;

  case 86:
#line 1880 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SUPER((yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: super!($2) %*/
		    }
#line 7271 "parse.c"
    break;

  case 87:
#line 1888 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_yield(p, (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: yield!($2) %*/
		    }
#line 7283 "parse.c"
    break;

  case 88:
#line 1896 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RETURN(ret_args(p, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: return!($2) %*/
		    }
#line 7294 "parse.c"
    break;

  case 89:
#line 1903 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BREAK(ret_args(p, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: break!($2) %*/
		    }
#line 7305 "parse.c"
    break;

  case 90:
#line 1910 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_NEXT(ret_args(p, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: next!($2) %*/
		    }
#line 7316 "parse.c"
    break;

  case 92:
#line 1920 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 7327 "parse.c"
    break;

  case 94:
#line 1930 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(NEW_LIST((yyvsp[-1].node), &(yyloc)), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 7338 "parse.c"
    break;

  case 95:
#line 1939 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[0].node), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 7349 "parse.c"
    break;

  case 96:
#line 1946 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(list_append(p, (yyvsp[-1].node),(yyvsp[0].node)), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $2) %*/
		    }
#line 7360 "parse.c"
    break;

  case 97:
#line 1953 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!($1, $3) %*/
		    }
#line 7371 "parse.c"
    break;

  case 98:
#line 1960 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-4].node), NEW_POSTARG((yyvsp[-2].node),(yyvsp[0].node),&(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!($1, $3), $5) %*/
		    }
#line 7382 "parse.c"
    break;

  case 99:
#line 1967 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-1].node), NODE_SPECIAL_NO_NAME_REST, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!($1, Qnil) %*/
		    }
#line 7393 "parse.c"
    break;

  case 100:
#line 1974 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-3].node), NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!($1, Qnil), $4) %*/
		    }
#line 7404 "parse.c"
    break;

  case 101:
#line 1981 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!(mlhs_new!, $2) %*/
		    }
#line 7415 "parse.c"
    break;

  case 102:
#line 1988 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NEW_POSTARG((yyvsp[-2].node),(yyvsp[0].node),&(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!(mlhs_new!, $2), $4) %*/
		    }
#line 7426 "parse.c"
    break;

  case 103:
#line 1995 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NODE_SPECIAL_NO_NAME_REST, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!(mlhs_new!, Qnil) %*/
		    }
#line 7437 "parse.c"
    break;

  case 104:
#line 2002 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!(mlhs_new!, Qnil), $3) %*/
		    }
#line 7448 "parse.c"
    break;

  case 106:
#line 2012 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 7459 "parse.c"
    break;

  case 107:
#line 2021 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[-1].node), &(yylsp[-1]));
		    /*% %*/
		    /*% ripper: mlhs_add!(mlhs_new!, $1) %*/
		    }
#line 7470 "parse.c"
    break;

  case 108:
#line 2028 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $2) %*/
		    }
#line 7481 "parse.c"
    break;

  case 109:
#line 2037 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add!(mlhs_new!, $1) %*/
		    }
#line 7492 "parse.c"
    break;

  case 110:
#line 2044 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $3) %*/
		    }
#line 7503 "parse.c"
    break;

  case 111:
#line 2053 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 7514 "parse.c"
    break;

  case 112:
#line 2060 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 7525 "parse.c"
    break;

  case 113:
#line 2067 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = aryset(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: aref_field!($1, escape_Qundef($3)) %*/
		    }
#line 7536 "parse.c"
    break;

  case 114:
#line 2074 "parse.y"
                    {
			if ((yyvsp[-1].id) == tANDDOT) {
			    yyerror1(&(yylsp[-1]), "&. inside multiple assignment destination");
			}
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 7550 "parse.c"
    break;

  case 115:
#line 2084 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), idCOLON2, (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_field!($1, $3) %*/
		    }
#line 7561 "parse.c"
    break;

  case 116:
#line 2091 "parse.y"
                    {
			if ((yyvsp[-1].id) == tANDDOT) {
			    yyerror1(&(yylsp[-1]), "&. inside multiple assignment destination");
			}
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 7575 "parse.c"
    break;

  case 117:
#line 2101 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, const_path_field!($1, $3)) %*/
		    }
#line 7586 "parse.c"
    break;

  case 118:
#line 2108 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON3((yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, top_const_field!($2)) %*/
		    }
#line 7597 "parse.c"
    break;

  case 119:
#line 2115 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[0].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), var_field(p, $1)) %*/
		    }
#line 7609 "parse.c"
    break;

  case 120:
#line 2125 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 7620 "parse.c"
    break;

  case 121:
#line 2132 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 7631 "parse.c"
    break;

  case 122:
#line 2139 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = aryset(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: aref_field!($1, escape_Qundef($3)) %*/
		    }
#line 7642 "parse.c"
    break;

  case 123:
#line 2146 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 7653 "parse.c"
    break;

  case 124:
#line 2153 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), idCOLON2, (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, ID2VAL(idCOLON2), $3) %*/
		    }
#line 7664 "parse.c"
    break;

  case 125:
#line 2160 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 7675 "parse.c"
    break;

  case 126:
#line 2167 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, const_path_field!($1, $3)) %*/
		    }
#line 7686 "parse.c"
    break;

  case 127:
#line 2174 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON3((yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, top_const_field!($2)) %*/
		    }
#line 7697 "parse.c"
    break;

  case 128:
#line 2181 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[0].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), var_field(p, $1)) %*/
		    }
#line 7709 "parse.c"
    break;

  case 129:
#line 2191 "parse.y"
                    {
			static const char mesg[] = "class/module name must be CONSTANT";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
		    /*% %*/
		    /*% ripper[error]: class_name_error!(ERR_MESG(), $1) %*/
		    }
#line 7721 "parse.c"
    break;

  case 131:
#line 2202 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON3((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: top_const_ref!($2) %*/
		    }
#line 7732 "parse.c"
    break;

  case 132:
#line 2209 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2(0, (yyval.node), &(yyloc));
		    /*% %*/
		    /*% ripper: const_ref!($1) %*/
		    }
#line 7743 "parse.c"
    break;

  case 133:
#line 2216 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_ref!($1, $3) %*/
		    }
#line 7754 "parse.c"
    break;

  case 137:
#line 2228 "parse.y"
                    {
			SET_LEX_STATE(EXPR_ENDFN);
			(yyval.id) = (yyvsp[0].id);
		    }
#line 7763 "parse.c"
    break;

  case 139:
#line 2236 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIT(ID2SYM((yyvsp[0].id)), &(yyloc));
		    /*% %*/
		    /*% ripper: symbol_literal!($1) %*/
		    }
#line 7774 "parse.c"
    break;

  case 141:
#line 2246 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_UNDEF((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 7785 "parse.c"
    break;

  case 142:
#line 2252 "parse.y"
                                 {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 7791 "parse.c"
    break;

  case 143:
#line 2253 "parse.y"
                    {
		    /*%%%*/
			NODE *undef = NEW_UNDEF((yyvsp[0].node), &(yylsp[0]));
			(yyval.node) = block_append(p, (yyvsp[-3].node), undef);
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($4)) %*/
		    }
#line 7803 "parse.c"
    break;

  case 144:
#line 2262 "parse.y"
                                { ifndef_ripper((yyval.id) = '|'); }
#line 7809 "parse.c"
    break;

  case 145:
#line 2263 "parse.y"
                                { ifndef_ripper((yyval.id) = '^'); }
#line 7815 "parse.c"
    break;

  case 146:
#line 2264 "parse.y"
                                { ifndef_ripper((yyval.id) = '&'); }
#line 7821 "parse.c"
    break;

  case 147:
#line 2265 "parse.y"
                                { ifndef_ripper((yyval.id) = tCMP); }
#line 7827 "parse.c"
    break;

  case 148:
#line 2266 "parse.y"
                                { ifndef_ripper((yyval.id) = tEQ); }
#line 7833 "parse.c"
    break;

  case 149:
#line 2267 "parse.y"
                                { ifndef_ripper((yyval.id) = tEQQ); }
#line 7839 "parse.c"
    break;

  case 150:
#line 2268 "parse.y"
                                { ifndef_ripper((yyval.id) = tMATCH); }
#line 7845 "parse.c"
    break;

  case 151:
#line 2269 "parse.y"
                                { ifndef_ripper((yyval.id) = tNMATCH); }
#line 7851 "parse.c"
    break;

  case 152:
#line 2270 "parse.y"
                                { ifndef_ripper((yyval.id) = '>'); }
#line 7857 "parse.c"
    break;

  case 153:
#line 2271 "parse.y"
                                { ifndef_ripper((yyval.id) = tGEQ); }
#line 7863 "parse.c"
    break;

  case 154:
#line 2272 "parse.y"
                                { ifndef_ripper((yyval.id) = '<'); }
#line 7869 "parse.c"
    break;

  case 155:
#line 2273 "parse.y"
                                { ifndef_ripper((yyval.id) = tLEQ); }
#line 7875 "parse.c"
    break;

  case 156:
#line 2274 "parse.y"
                                { ifndef_ripper((yyval.id) = tNEQ); }
#line 7881 "parse.c"
    break;

  case 157:
#line 2275 "parse.y"
                                { ifndef_ripper((yyval.id) = tLSHFT); }
#line 7887 "parse.c"
    break;

  case 158:
#line 2276 "parse.y"
                                { ifndef_ripper((yyval.id) = tRSHFT); }
#line 7893 "parse.c"
    break;

  case 159:
#line 2277 "parse.y"
                                { ifndef_ripper((yyval.id) = '+'); }
#line 7899 "parse.c"
    break;

  case 160:
#line 2278 "parse.y"
                                { ifndef_ripper((yyval.id) = '-'); }
#line 7905 "parse.c"
    break;

  case 161:
#line 2279 "parse.y"
                                { ifndef_ripper((yyval.id) = '*'); }
#line 7911 "parse.c"
    break;

  case 162:
#line 2280 "parse.y"
                                { ifndef_ripper((yyval.id) = '*'); }
#line 7917 "parse.c"
    break;

  case 163:
#line 2281 "parse.y"
                                { ifndef_ripper((yyval.id) = '/'); }
#line 7923 "parse.c"
    break;

  case 164:
#line 2282 "parse.y"
                                { ifndef_ripper((yyval.id) = '%'); }
#line 7929 "parse.c"
    break;

  case 165:
#line 2283 "parse.y"
                                { ifndef_ripper((yyval.id) = tPOW); }
#line 7935 "parse.c"
    break;

  case 166:
#line 2284 "parse.y"
                                { ifndef_ripper((yyval.id) = tDSTAR); }
#line 7941 "parse.c"
    break;

  case 167:
#line 2285 "parse.y"
                                { ifndef_ripper((yyval.id) = '!'); }
#line 7947 "parse.c"
    break;

  case 168:
#line 2286 "parse.y"
                                { ifndef_ripper((yyval.id) = '~'); }
#line 7953 "parse.c"
    break;

  case 169:
#line 2287 "parse.y"
                                { ifndef_ripper((yyval.id) = tUPLUS); }
#line 7959 "parse.c"
    break;

  case 170:
#line 2288 "parse.y"
                                { ifndef_ripper((yyval.id) = tUMINUS); }
#line 7965 "parse.c"
    break;

  case 171:
#line 2289 "parse.y"
                                { ifndef_ripper((yyval.id) = tAREF); }
#line 7971 "parse.c"
    break;

  case 172:
#line 2290 "parse.y"
                                { ifndef_ripper((yyval.id) = tASET); }
#line 7977 "parse.c"
    break;

  case 173:
#line 2291 "parse.y"
                                { ifndef_ripper((yyval.id) = '`'); }
#line 7983 "parse.c"
    break;

  case 215:
#line 2309 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: assign!($1, $4) %*/
		    }
#line 7994 "parse.c"
    break;

  case 216:
#line 2316 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_op_assign(p, (yyvsp[-3].node), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!($1, $2, $4) %*/
		    }
#line 8005 "parse.c"
    break;

  case 217:
#line 2323 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_ary_op_assign(p, (yyvsp[-6].node), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[0].node), &(yylsp[-4]), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(aref_field!($1, escape_Qundef($3)), $5, $7) %*/
		    }
#line 8016 "parse.c"
    break;

  case 218:
#line 2330 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 8027 "parse.c"
    break;

  case 219:
#line 2337 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 8038 "parse.c"
    break;

  case 220:
#line 2344 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), ID2VAL(idCOLON2), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, ID2VAL(idCOLON2), $3), $4, $6) %*/
		    }
#line 8049 "parse.c"
    break;

  case 221:
#line 2351 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-5]), &(yylsp[-3]));
			(yyval.node) = new_const_op_assign(p, NEW_COLON2((yyvsp[-5].node), (yyvsp[-3].id), &loc), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(const_path_field!($1, $3), $4, $6) %*/
		    }
#line 8061 "parse.c"
    break;

  case 222:
#line 2359 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_const_op_assign(p, NEW_COLON3((yyvsp[-3].id), &(yyloc)), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(top_const_field!($2), $3, $5) %*/
		    }
#line 8072 "parse.c"
    break;

  case 223:
#line 2366 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[-3].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), opassign!(var_field(p, $1), $2, $4)) %*/
		    }
#line 8084 "parse.c"
    break;

  case 224:
#line 2374 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, $3) %*/
		    }
#line 8097 "parse.c"
    break;

  case 225:
#line 2383 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, $3) %*/
		    }
#line 8110 "parse.c"
    break;

  case 226:
#line 2392 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT2((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, Qnil) %*/
		    }
#line 8122 "parse.c"
    break;

  case 227:
#line 2400 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT3((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, Qnil) %*/
		    }
#line 8134 "parse.c"
    break;

  case 228:
#line 2408 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!(Qnil, $2) %*/
		    }
#line 8146 "parse.c"
    break;

  case 229:
#line 2416 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!(Qnil, $2) %*/
		    }
#line 8158 "parse.c"
    break;

  case 230:
#line 2424 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '+', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8166 "parse.c"
    break;

  case 231:
#line 2428 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '-', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8174 "parse.c"
    break;

  case 232:
#line 2432 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '*', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8182 "parse.c"
    break;

  case 233:
#line 2436 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '/', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8190 "parse.c"
    break;

  case 234:
#line 2440 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '%', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8198 "parse.c"
    break;

  case 235:
#line 2444 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idPow, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8206 "parse.c"
    break;

  case 236:
#line 2448 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, call_bin_op(p, (yyvsp[-2].node), idPow, (yyvsp[0].node), &(yylsp[-2]), &(yyloc)), idUMinus, &(yylsp[-3]), &(yyloc));
		    }
#line 8214 "parse.c"
    break;

  case 237:
#line 2452 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, (yyvsp[0].node), idUPlus, &(yylsp[-1]), &(yyloc));
		    }
#line 8222 "parse.c"
    break;

  case 238:
#line 2456 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, (yyvsp[0].node), idUMinus, &(yylsp[-1]), &(yyloc));
		    }
#line 8230 "parse.c"
    break;

  case 239:
#line 2460 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '|', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8238 "parse.c"
    break;

  case 240:
#line 2464 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '^', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8246 "parse.c"
    break;

  case 241:
#line 2468 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '&', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8254 "parse.c"
    break;

  case 242:
#line 2472 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idCmp, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8262 "parse.c"
    break;

  case 244:
#line 2477 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idEq, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8270 "parse.c"
    break;

  case 245:
#line 2481 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idEqq, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8278 "parse.c"
    break;

  case 246:
#line 2485 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idNeq, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8286 "parse.c"
    break;

  case 247:
#line 2489 "parse.y"
                    {
			(yyval.node) = match_op(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8294 "parse.c"
    break;

  case 248:
#line 2493 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idNeqTilde, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8302 "parse.c"
    break;

  case 249:
#line 2497 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[0].node), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 8310 "parse.c"
    break;

  case 250:
#line 2501 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, (yyvsp[0].node), '~', &(yylsp[-1]), &(yyloc));
		    }
#line 8318 "parse.c"
    break;

  case 251:
#line 2505 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idLTLT, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8326 "parse.c"
    break;

  case 252:
#line 2509 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idGTGT, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8334 "parse.c"
    break;

  case 253:
#line 2513 "parse.y"
                    {
			(yyval.node) = logop(p, idANDOP, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8342 "parse.c"
    break;

  case 254:
#line 2517 "parse.y"
                    {
			(yyval.node) = logop(p, idOROP, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8350 "parse.c"
    break;

  case 255:
#line 2520 "parse.y"
                                         {p->ctxt.in_defined = 1;}
#line 8356 "parse.c"
    break;

  case 256:
#line 2521 "parse.y"
                    {
			p->ctxt.in_defined = 0;
			(yyval.node) = new_defined(p, (yyvsp[0].node), &(yyloc));
		    }
#line 8365 "parse.c"
    break;

  case 257:
#line 2526 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-5].node));
			(yyval.node) = new_if(p, (yyvsp[-5].node), (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-5].node));
		    /*% %*/
		    /*% ripper: ifop!($1, $3, $6) %*/
		    }
#line 8378 "parse.c"
    break;

  case 258:
#line 2535 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-3].node), (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: def!(get_value($1), $2, $4) %*/
			local_pop(p);
		    }
#line 8392 "parse.c"
    break;

  case 259:
#line 2545 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
		    /*%%%*/
			(yyvsp[-2].node) = rescued_expr(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.node) = set_defun_body(p, (yyvsp[-5].node), (yyvsp[-4].node), (yyvsp[-2].node), &(yyloc));
		    /*% %*/
		    /*% ripper: def!(get_value($1), $2, rescue_mod!($4, $6)) %*/
			local_pop(p);
		    }
#line 8407 "parse.c"
    break;

  case 260:
#line 2556 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-3].node), (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*%
			$1 = get_value($1);
		    %*/
		    /*% ripper: defs!(AREF($1, 0), AREF($1, 1), AREF($1, 2), $2, $4) %*/
			local_pop(p);
		    }
#line 8423 "parse.c"
    break;

  case 261:
#line 2568 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
		    /*%%%*/
			(yyvsp[-2].node) = rescued_expr(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.node) = set_defun_body(p, (yyvsp[-5].node), (yyvsp[-4].node), (yyvsp[-2].node), &(yyloc));
		    /*%
			$1 = get_value($1);
		    %*/
		    /*% ripper: defs!(AREF($1, 0), AREF($1, 1), AREF($1, 2), $2, rescue_mod!($4, $6)) %*/
			local_pop(p);
		    }
#line 8440 "parse.c"
    break;

  case 262:
#line 2581 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 8448 "parse.c"
    break;

  case 263:
#line 2586 "parse.y"
                       {(yyval.id) = '>';}
#line 8454 "parse.c"
    break;

  case 264:
#line 2587 "parse.y"
                       {(yyval.id) = '<';}
#line 8460 "parse.c"
    break;

  case 265:
#line 2588 "parse.y"
                       {(yyval.id) = idGE;}
#line 8466 "parse.c"
    break;

  case 266:
#line 2589 "parse.y"
                       {(yyval.id) = idLE;}
#line 8472 "parse.c"
    break;

  case 267:
#line 2593 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8480 "parse.c"
    break;

  case 268:
#line 2597 "parse.y"
                    {
			rb_warning1("comparison '%s' after comparison", WARN_ID((yyvsp[-1].id)));
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8489 "parse.c"
    break;

  case 269:
#line 2604 "parse.y"
                    {
			(yyval.ctxt) = p->ctxt;
		    }
#line 8497 "parse.c"
    break;

  case 270:
#line 2608 "parse.y"
                    {
			(yyval.ctxt) = p->ctxt;
		    }
#line 8505 "parse.c"
    break;

  case 271:
#line 2614 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 8514 "parse.c"
    break;

  case 273:
#line 2622 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 8522 "parse.c"
    break;

  case 274:
#line 2626 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? arg_append(p, (yyvsp[-3].node), new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].node);
		    /*% %*/
		    /*% ripper: args_add!($1, bare_assoc_hash!($3)) %*/
		    }
#line 8533 "parse.c"
    break;

  case 275:
#line 2633 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? NEW_LIST(new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : 0;
		    /*% %*/
		    /*% ripper: args_add!(args_new!, bare_assoc_hash!($1)) %*/
		    }
#line 8544 "parse.c"
    break;

  case 276:
#line 2642 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 8553 "parse.c"
    break;

  case 277:
#line 2647 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			(yyval.node) = rescued_expr(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
		    /*% %*/
		    /*% ripper: rescue_mod!($1, $3) %*/
		    }
#line 8565 "parse.c"
    break;

  case 278:
#line 2657 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: arg_paren!(escape_Qundef($2)) %*/
		    }
#line 8576 "parse.c"
    break;

  case 279:
#line 2664 "parse.y"
                    {
			if (!check_forwarding_args(p)) {
			    (yyval.node) = Qnone;
			}
			else {
			/*%%%*/
			    (yyval.node) = new_args_forward_call(p, (yyvsp[-3].node), &(yylsp[-1]), &(yyloc));
			/*% %*/
			/*% ripper: arg_paren!(args_add!($2, $4)) %*/
			}
		    }
#line 8592 "parse.c"
    break;

  case 280:
#line 2676 "parse.y"
                    {
			if (!check_forwarding_args(p)) {
			    (yyval.node) = Qnone;
			}
			else {
			/*%%%*/
			    (yyval.node) = new_args_forward_call(p, 0, &(yylsp[-1]), &(yyloc));
			/*% %*/
			/*% ripper: arg_paren!($2) %*/
			}
		    }
#line 8608 "parse.c"
    break;

  case 285:
#line 2696 "parse.y"
                    {
		      (yyval.node) = (yyvsp[-1].node);
		    }
#line 8616 "parse.c"
    break;

  case 286:
#line 2700 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? arg_append(p, (yyvsp[-3].node), new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].node);
		    /*% %*/
		    /*% ripper: args_add!($1, bare_assoc_hash!($3)) %*/
		    }
#line 8627 "parse.c"
    break;

  case 287:
#line 2707 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? NEW_LIST(new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yylsp[-1])) : 0;
		    /*% %*/
		    /*% ripper: args_add!(args_new!, bare_assoc_hash!($1)) %*/
		    }
#line 8638 "parse.c"
    break;

  case 288:
#line 2716 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!(args_new!, $1) %*/
		    }
#line 8650 "parse.c"
    break;

  case 289:
#line 2724 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = arg_blk_pass((yyvsp[-1].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: args_add_block!($1, $2) %*/
		    }
#line 8661 "parse.c"
    break;

  case 290:
#line 2731 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? NEW_LIST(new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yylsp[-1])) : 0;
			(yyval.node) = arg_blk_pass((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: args_add_block!(args_add!(args_new!, bare_assoc_hash!($1)), $2) %*/
		    }
#line 8673 "parse.c"
    break;

  case 291:
#line 2739 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? arg_append(p, (yyvsp[-3].node), new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].node);
			(yyval.node) = arg_blk_pass((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: args_add_block!(args_add!($1, bare_assoc_hash!($3)), $4) %*/
		    }
#line 8685 "parse.c"
    break;

  case 293:
#line 2750 "parse.y"
                    {
			/* If call_args starts with a open paren '(' or '[',
			 * look-ahead reading of the letters calls CMDARG_PUSH(0),
			 * but the push must be done after CMDARG_PUSH(1).
			 * So this code makes them consistent by first cancelling
			 * the premature CMDARG_PUSH(0), doing CMDARG_PUSH(1),
			 * and finally redoing CMDARG_PUSH(0).
			 */
			int lookahead = 0;
			switch (yychar) {
			  case '(': case tLPAREN: case tLPAREN_ARG: case '[': case tLBRACK:
			    lookahead = 1;
			}
			if (lookahead) CMDARG_POP();
			CMDARG_PUSH(1);
			if (lookahead) CMDARG_PUSH(0);
		    }
#line 8707 "parse.c"
    break;

  case 294:
#line 2768 "parse.y"
                    {
			/* call_args can be followed by tLBRACE_ARG (that does CMDARG_PUSH(0) in the lexer)
			 * but the push must be done after CMDARG_POP() in the parser.
			 * So this code does CMDARG_POP() to pop 0 pushed by tLBRACE_ARG,
			 * CMDARG_POP() to pop 1 pushed by command_args,
			 * and CMDARG_PUSH(0) to restore back the flag set by tLBRACE_ARG.
			 */
			int lookahead = 0;
			switch (yychar) {
			  case tLBRACE_ARG:
			    lookahead = 1;
			}
			if (lookahead) CMDARG_POP();
			CMDARG_POP();
			if (lookahead) CMDARG_PUSH(0);
			(yyval.node) = (yyvsp[0].node);
		    }
#line 8729 "parse.c"
    break;

  case 295:
#line 2788 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BLOCK_PASS((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: $2 %*/
		    }
#line 8740 "parse.c"
    break;

  case 296:
#line 2797 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 8748 "parse.c"
    break;

  case 297:
#line 2801 "parse.y"
                    {
			(yyval.node) = 0;
		    }
#line 8756 "parse.c"
    break;

  case 298:
#line 2807 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!(args_new!, $1) %*/
		    }
#line 8767 "parse.c"
    break;

  case 299:
#line 2814 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SPLAT((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!(args_new!, $2) %*/
		    }
#line 8778 "parse.c"
    break;

  case 300:
#line 2821 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = last_arg_append(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!($1, $3) %*/
		    }
#line 8789 "parse.c"
    break;

  case 301:
#line 2828 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!($1, $4) %*/
		    }
#line 8800 "parse.c"
    break;

  case 304:
#line 2841 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = last_arg_append(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mrhs_add!(mrhs_new_from_args!($1), $3) %*/
		    }
#line 8811 "parse.c"
    break;

  case 305:
#line 2848 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mrhs_add_star!(mrhs_new_from_args!($1), $4) %*/
		    }
#line 8822 "parse.c"
    break;

  case 306:
#line 2855 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SPLAT((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mrhs_add_star!(mrhs_new!, $2) %*/
		    }
#line 8833 "parse.c"
    break;

  case 317:
#line 2874 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_FCALL((yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_arg!(fcall!($1), args_new!) %*/
		    }
#line 8844 "parse.c"
    break;

  case 318:
#line 2881 "parse.y"
                    {
			CMDARG_PUSH(0);
		    }
#line 8852 "parse.c"
    break;

  case 319:
#line 2886 "parse.y"
                    {
			CMDARG_POP();
		    /*%%%*/
			set_line_body((yyvsp[-1].node), (yylsp[-3]).end_pos.lineno);
			(yyval.node) = NEW_BEGIN((yyvsp[-1].node), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-3]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: begin!($3) %*/
		    }
#line 8866 "parse.c"
    break;

  case 320:
#line 2895 "parse.y"
                              {SET_LEX_STATE(EXPR_ENDARG);}
#line 8872 "parse.c"
    break;

  case 321:
#line 2896 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: paren!(0) %*/
		    }
#line 8883 "parse.c"
    break;

  case 322:
#line 2902 "parse.y"
                                   {SET_LEX_STATE(EXPR_ENDARG);}
#line 8889 "parse.c"
    break;

  case 323:
#line 2903 "parse.y"
                    {
		    /*%%%*/
			if (nd_type((yyvsp[-2].node)) == NODE_SELF) (yyvsp[-2].node)->nd_state = 0;
			(yyval.node) = (yyvsp[-2].node);
		    /*% %*/
		    /*% ripper: paren!($2) %*/
		    }
#line 8901 "parse.c"
    break;

  case 324:
#line 2911 "parse.y"
                    {
		    /*%%%*/
			if (nd_type((yyvsp[-1].node)) == NODE_SELF) (yyvsp[-1].node)->nd_state = 0;
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: paren!($2) %*/
		    }
#line 8913 "parse.c"
    break;

  case 325:
#line 2919 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_ref!($1, $3) %*/
		    }
#line 8924 "parse.c"
    break;

  case 326:
#line 2926 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON3((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: top_const_ref!($2) %*/
		    }
#line 8935 "parse.c"
    break;

  case 327:
#line 2933 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!(escape_Qundef($2)) %*/
		    }
#line 8946 "parse.c"
    break;

  case 328:
#line 2940 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_hash(p, (yyvsp[-1].node), &(yyloc));
			(yyval.node)->nd_brace = TRUE;
		    /*% %*/
		    /*% ripper: hash!(escape_Qundef($2)) %*/
		    }
#line 8958 "parse.c"
    break;

  case 329:
#line 2948 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RETURN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: return0! %*/
		    }
#line 8969 "parse.c"
    break;

  case 330:
#line 2955 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_yield(p, (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: yield!(paren!($3)) %*/
		    }
#line 8980 "parse.c"
    break;

  case 331:
#line 2962 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_YIELD(0, &(yyloc));
		    /*% %*/
		    /*% ripper: yield!(paren!(args_new!)) %*/
		    }
#line 8991 "parse.c"
    break;

  case 332:
#line 2969 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_YIELD(0, &(yyloc));
		    /*% %*/
		    /*% ripper: yield0! %*/
		    }
#line 9002 "parse.c"
    break;

  case 333:
#line 2975 "parse.y"
                                             {p->ctxt.in_defined = 1;}
#line 9008 "parse.c"
    break;

  case 334:
#line 2976 "parse.y"
                    {
			p->ctxt.in_defined = 0;
			(yyval.node) = new_defined(p, (yyvsp[-1].node), &(yyloc));
		    }
#line 9017 "parse.c"
    break;

  case 335:
#line 2981 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[-1].node), &(yylsp[-1])), METHOD_NOT, &(yylsp[-3]), &(yyloc));
		    }
#line 9025 "parse.c"
    break;

  case 336:
#line 2985 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, new_nil(&(yylsp[-1])), &(yylsp[-1])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 9033 "parse.c"
    break;

  case 337:
#line 2989 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = method_add_block(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(method_add_arg!(fcall!($1), args_new!), $2) %*/
		    }
#line 9044 "parse.c"
    break;

  case 339:
#line 2997 "parse.y"
                    {
		    /*%%%*/
			block_dup_check(p, (yyvsp[-1].node)->nd_args, (yyvsp[0].node));
			(yyval.node) = method_add_block(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!($1, $2) %*/
		    }
#line 9056 "parse.c"
    break;

  case 341:
#line 3009 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[-4].node), (yyvsp[-2].node), (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: if!($2, $4, escape_Qundef($5)) %*/
		    }
#line 9068 "parse.c"
    break;

  case 342:
#line 3020 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_unless(p, (yyvsp[-4].node), (yyvsp[-2].node), (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: unless!($2, $4, escape_Qundef($5)) %*/
		    }
#line 9080 "parse.c"
    break;

  case 343:
#line 3030 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_WHILE(cond(p, (yyvsp[-2].node), &(yylsp[-2])), (yyvsp[-1].node), 1, &(yyloc));
			fixpos((yyval.node), (yyvsp[-2].node));
		    /*% %*/
		    /*% ripper: while!($2, $3) %*/
		    }
#line 9092 "parse.c"
    break;

  case 344:
#line 3040 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_UNTIL(cond(p, (yyvsp[-2].node), &(yylsp[-2])), (yyvsp[-1].node), 1, &(yyloc));
			fixpos((yyval.node), (yyvsp[-2].node));
		    /*% %*/
		    /*% ripper: until!($2, $3) %*/
		    }
#line 9104 "parse.c"
    break;

  case 345:
#line 3048 "parse.y"
                    {
			(yyval.val) = p->case_labels;
			p->case_labels = Qnil;
		    }
#line 9113 "parse.c"
    break;

  case 346:
#line 3054 "parse.y"
                    {
			if (RTEST(p->case_labels)) rb_hash_clear(p->case_labels);
			p->case_labels = (yyvsp[-2].val);
		    /*%%%*/
			(yyval.node) = NEW_CASE((yyvsp[-4].node), (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: case!($2, $5) %*/
		    }
#line 9127 "parse.c"
    break;

  case 347:
#line 3064 "parse.y"
                    {
			(yyval.val) = p->case_labels;
			p->case_labels = 0;
		    }
#line 9136 "parse.c"
    break;

  case 348:
#line 3070 "parse.y"
                    {
			if (RTEST(p->case_labels)) rb_hash_clear(p->case_labels);
			p->case_labels = (yyvsp[-2].val);
		    /*%%%*/
			(yyval.node) = NEW_CASE2((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: case!(Qnil, $4) %*/
		    }
#line 9149 "parse.c"
    break;

  case 349:
#line 3081 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_CASE3((yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: case!($2, $4) %*/
		    }
#line 9160 "parse.c"
    break;

  case 350:
#line 3090 "parse.y"
                    {
		    /*%%%*/
			/*
			 *  for a, b, c in e
			 *  #=>
			 *  e.each{|*x| a, b, c = x}
			 *
			 *  for a in e
			 *  #=>
			 *  e.each{|x| a, = x}
			 */
			ID id = internal_id(p);
			NODE *m = NEW_ARGS_AUX(0, 0, &NULL_LOC);
			NODE *args, *scope, *internal_var = NEW_DVAR(id, &(yylsp[-4]));
			ID *tbl = ALLOC_N(ID, 3);
			tbl[0] = 1 /* length of local var table */; tbl[1] = id /* internal id */;
                        rb_ast_add_local_table(p->ast, tbl);

			switch (nd_type((yyvsp[-4].node))) {
			  case NODE_LASGN:
			  case NODE_DASGN:
			  case NODE_DASGN_CURR: /* e.each {|internal_var| a = internal_var; ... } */
			    (yyvsp[-4].node)->nd_value = internal_var;
			    id = 0;
			    m->nd_plen = 1;
			    m->nd_next = (yyvsp[-4].node);
			    break;
			  case NODE_MASGN: /* e.each {|*internal_var| a, b, c = (internal_var.length == 1 && Array === (tmp = internal_var[0]) ? tmp : internal_var); ... } */
			    m->nd_next = node_assign(p, (yyvsp[-4].node), NEW_FOR_MASGN(internal_var, &(yylsp[-4])), NO_LEX_CTXT, &(yylsp[-4]));
			    break;
			  default: /* e.each {|*internal_var| @a, B, c[1], d.attr = internal_val; ... } */
			    m->nd_next = node_assign(p, NEW_MASGN(NEW_LIST((yyvsp[-4].node), &(yylsp[-4])), 0, &(yylsp[-4])), internal_var, NO_LEX_CTXT, &(yylsp[-4]));
			}
			/* {|*internal_id| <m> = internal_id; ... } */
			args = new_args(p, m, 0, id, 0, new_args_tail(p, 0, 0, 0, &(yylsp[-4])), &(yylsp[-4]));
			scope = NEW_NODE(NODE_SCOPE, tbl, (yyvsp[-1].node), args, &(yyloc));
			(yyval.node) = NEW_FOR((yyvsp[-2].node), scope, &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: for!($2, $4, $5) %*/
		    }
#line 9206 "parse.c"
    break;

  case 351:
#line 3132 "parse.y"
                    {
			if (p->ctxt.in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[-1]));
			    yyerror1(&loc, "class definition in method body");
			}
			p->ctxt.in_class = 1;
			local_push(p, 0);
		    }
#line 9219 "parse.c"
    break;

  case 352:
#line 3142 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_CLASS((yyvsp[-4].node), (yyvsp[-1].node), (yyvsp[-3].node), &(yyloc));
			nd_set_line((yyval.node)->nd_body, (yylsp[0]).end_pos.lineno);
			set_line_body((yyvsp[-1].node), (yylsp[-3]).end_pos.lineno);
			nd_set_line((yyval.node), (yylsp[-3]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: class!($2, $3, $5) %*/
			local_pop(p);
			p->ctxt.in_class = (yyvsp[-5].ctxt).in_class;
			p->ctxt.shareable_constant_value = (yyvsp[-5].ctxt).shareable_constant_value;
		    }
#line 9236 "parse.c"
    break;

  case 353:
#line 3155 "parse.y"
                    {
			p->ctxt.in_def = 0;
			p->ctxt.in_class = 0;
			local_push(p, 0);
		    }
#line 9246 "parse.c"
    break;

  case 354:
#line 3163 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SCLASS((yyvsp[-4].node), (yyvsp[-1].node), &(yyloc));
			nd_set_line((yyval.node)->nd_body, (yylsp[0]).end_pos.lineno);
			set_line_body((yyvsp[-1].node), nd_line((yyvsp[-4].node)));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: sclass!($3, $6) %*/
			local_pop(p);
			p->ctxt.in_def = (yyvsp[-6].ctxt).in_def;
			p->ctxt.in_class = (yyvsp[-6].ctxt).in_class;
			p->ctxt.shareable_constant_value = (yyvsp[-6].ctxt).shareable_constant_value;
		    }
#line 9264 "parse.c"
    break;

  case 355:
#line 3177 "parse.y"
                    {
			if (p->ctxt.in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			    yyerror1(&loc, "module definition in method body");
			}
			p->ctxt.in_class = 1;
			local_push(p, 0);
		    }
#line 9277 "parse.c"
    break;

  case 356:
#line 3187 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MODULE((yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
			nd_set_line((yyval.node)->nd_body, (yylsp[0]).end_pos.lineno);
			set_line_body((yyvsp[-1].node), (yylsp[-3]).end_pos.lineno);
			nd_set_line((yyval.node), (yylsp[-3]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: module!($2, $4) %*/
			local_pop(p);
			p->ctxt.in_class = (yyvsp[-4].ctxt).in_class;
			p->ctxt.shareable_constant_value = (yyvsp[-4].ctxt).shareable_constant_value;
		    }
#line 9294 "parse.c"
    break;

  case 357:
#line 3203 "parse.y"
                    {
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-3].node), (yyvsp[-2].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: def!(get_value($1), $2, $3) %*/
			local_pop(p);
		    }
#line 9307 "parse.c"
    break;

  case 358:
#line 3215 "parse.y"
                    {
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-3].node), (yyvsp[-2].node), (yyvsp[-1].node), &(yyloc));
		    /*%
			$1 = get_value($1);
		    %*/
		    /*% ripper: defs!(AREF($1, 0), AREF($1, 1), AREF($1, 2), $2, $3) %*/
			local_pop(p);
		    }
#line 9322 "parse.c"
    break;

  case 359:
#line 3226 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BREAK(0, &(yyloc));
		    /*% %*/
		    /*% ripper: break!(args_new!) %*/
		    }
#line 9333 "parse.c"
    break;

  case 360:
#line 3233 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_NEXT(0, &(yyloc));
		    /*% %*/
		    /*% ripper: next!(args_new!) %*/
		    }
#line 9344 "parse.c"
    break;

  case 361:
#line 3240 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_REDO(&(yyloc));
		    /*% %*/
		    /*% ripper: redo! %*/
		    }
#line 9355 "parse.c"
    break;

  case 362:
#line 3247 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RETRY(&(yyloc));
		    /*% %*/
		    /*% ripper: retry! %*/
		    }
#line 9366 "parse.c"
    break;

  case 363:
#line 3256 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9375 "parse.c"
    break;

  case 364:
#line 3263 "parse.y"
                    {
			token_info_push(p, "begin", &(yyloc));
		    }
#line 9383 "parse.c"
    break;

  case 365:
#line 3269 "parse.y"
                    {
			WARN_EOL("if");
			token_info_push(p, "if", &(yyloc));
			if (p->token_info && p->token_info->nonspc &&
			    p->token_info->next && !strcmp(p->token_info->next->token, "else")) {
			    const char *tok = p->lex.ptok;
			    const char *beg = p->lex.pbeg + p->token_info->next->beg.column;
			    beg += rb_strlen_lit("else");
			    while (beg < tok && ISSPACE(*beg)) beg++;
			    if (beg == tok) {
				p->token_info->nonspc = 0;
			    }
			}
		    }
#line 9402 "parse.c"
    break;

  case 366:
#line 3286 "parse.y"
                    {
			token_info_push(p, "unless", &(yyloc));
		    }
#line 9410 "parse.c"
    break;

  case 367:
#line 3292 "parse.y"
                    {
			token_info_push(p, "while", &(yyloc));
		    }
#line 9418 "parse.c"
    break;

  case 368:
#line 3298 "parse.y"
                    {
			token_info_push(p, "until", &(yyloc));
		    }
#line 9426 "parse.c"
    break;

  case 369:
#line 3304 "parse.y"
                    {
			token_info_push(p, "case", &(yyloc));
		    }
#line 9434 "parse.c"
    break;

  case 370:
#line 3310 "parse.y"
                    {
			token_info_push(p, "for", &(yyloc));
		    }
#line 9442 "parse.c"
    break;

  case 371:
#line 3316 "parse.y"
                    {
			token_info_push(p, "class", &(yyloc));
			(yyval.ctxt) = p->ctxt;
		    }
#line 9451 "parse.c"
    break;

  case 372:
#line 3323 "parse.y"
                    {
			token_info_push(p, "module", &(yyloc));
			(yyval.ctxt) = p->ctxt;
		    }
#line 9460 "parse.c"
    break;

  case 373:
#line 3330 "parse.y"
                    {
			token_info_push(p, "def", &(yyloc));
		    }
#line 9468 "parse.c"
    break;

  case 374:
#line 3336 "parse.y"
                    {
			token_info_push(p, "do", &(yyloc));
		    }
#line 9476 "parse.c"
    break;

  case 375:
#line 3342 "parse.y"
                    {
			token_info_push(p, "do", &(yyloc));
		    }
#line 9484 "parse.c"
    break;

  case 376:
#line 3348 "parse.y"
                    {
			token_info_warn(p, "rescue", p->token_info, 1, &(yyloc));
		    }
#line 9492 "parse.c"
    break;

  case 377:
#line 3354 "parse.y"
                    {
			token_info_warn(p, "ensure", p->token_info, 1, &(yyloc));
		    }
#line 9500 "parse.c"
    break;

  case 378:
#line 3360 "parse.y"
                    {
			token_info_warn(p, "when", p->token_info, 0, &(yyloc));
		    }
#line 9508 "parse.c"
    break;

  case 379:
#line 3366 "parse.y"
                    {
			token_info *ptinfo_beg = p->token_info;
			int same = ptinfo_beg && strcmp(ptinfo_beg->token, "case") != 0;
			token_info_warn(p, "else", p->token_info, same, &(yyloc));
			if (same) {
			    token_info e;
			    e.next = ptinfo_beg->next;
			    e.token = "else";
			    token_info_setup(&e, p->lex.pbeg, &(yyloc));
			    if (!e.nonspc) *ptinfo_beg = e;
			}
		    }
#line 9525 "parse.c"
    break;

  case 380:
#line 3381 "parse.y"
                    {
			WARN_EOL("elsif");
			token_info_warn(p, "elsif", p->token_info, 1, &(yyloc));
		    }
#line 9534 "parse.c"
    break;

  case 381:
#line 3388 "parse.y"
                    {
			token_info_pop(p, "end", &(yyloc));
		    }
#line 9542 "parse.c"
    break;

  case 382:
#line 3394 "parse.y"
                    {
			if (p->ctxt.in_class && !p->ctxt.in_def && !dyna_in_block(p))
			    yyerror1(&(yylsp[0]), "Invalid return in class/module body");
		    }
#line 9551 "parse.c"
    break;

  case 389:
#line 3413 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[-3].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-3].node));
		    /*% %*/
		    /*% ripper: elsif!($2, $4, escape_Qundef($5)) %*/
		    }
#line 9563 "parse.c"
    break;

  case 391:
#line 3424 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: else!($2) %*/
		    }
#line 9574 "parse.c"
    break;

  case 394:
#line 3437 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
			mark_lvar_used(p, (yyval.node));
		    /*% %*/
		    /*% ripper: assignable(p, $1) %*/
		    }
#line 9586 "parse.c"
    break;

  case 395:
#line 3445 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 9597 "parse.c"
    break;

  case 396:
#line 3454 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add!(mlhs_new!, $1) %*/
		    }
#line 9608 "parse.c"
    break;

  case 397:
#line 3461 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $3) %*/
		    }
#line 9619 "parse.c"
    break;

  case 398:
#line 3470 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[0].node), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 9630 "parse.c"
    break;

  case 399:
#line 3477 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!($1, $3) %*/
		    }
#line 9641 "parse.c"
    break;

  case 400:
#line 3484 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-4].node), NEW_POSTARG((yyvsp[-2].node), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!($1, $3), $5) %*/
		    }
#line 9652 "parse.c"
    break;

  case 401:
#line 3491 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!(mlhs_new!, $1) %*/
		    }
#line 9663 "parse.c"
    break;

  case 402:
#line 3498 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NEW_POSTARG((yyvsp[-2].node), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!(mlhs_new!, $1), $3) %*/
		    }
#line 9674 "parse.c"
    break;

  case 403:
#line 3507 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
			mark_lvar_used(p, (yyval.node));
		    /*% %*/
		    /*% ripper: assignable(p, $2) %*/
		    }
#line 9686 "parse.c"
    break;

  case 404:
#line 3515 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NODE_SPECIAL_NO_NAME_REST;
		    /*% %*/
		    /*% ripper: Qnil %*/
		    }
#line 9697 "parse.c"
    break;

  case 406:
#line 3524 "parse.y"
                             {(yyval.id) = ID2VAL(idNil);}
#line 9703 "parse.c"
    break;

  case 407:
#line 3528 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 9711 "parse.c"
    break;

  case 408:
#line 3532 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-1].node), Qnone, (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 9719 "parse.c"
    break;

  case 409:
#line 3536 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 9727 "parse.c"
    break;

  case 410:
#line 3540 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].id), &(yylsp[0]));
		    }
#line 9735 "parse.c"
    break;

  case 411:
#line 3546 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9743 "parse.c"
    break;

  case 412:
#line 3550 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 9751 "parse.c"
    break;

  case 413:
#line 3556 "parse.y"
                    {
			/* magic number for rest_id in iseq_set_arguments() */
		    /*%%%*/
			(yyval.id) = NODE_SPECIAL_EXCESSIVE_COMMA;
		    /*% %*/
		    /*% ripper: excessed_comma! %*/
		    }
#line 9763 "parse.c"
    break;

  case 414:
#line 3566 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 9771 "parse.c"
    break;

  case 415:
#line 3570 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-7].node), (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 9779 "parse.c"
    break;

  case 416:
#line 3574 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 9787 "parse.c"
    break;

  case 417:
#line 3578 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 9795 "parse.c"
    break;

  case 418:
#line 3582 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 9803 "parse.c"
    break;

  case 419:
#line 3586 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
			(yyval.node) = new_args(p, (yyvsp[-1].node), Qnone, (yyvsp[0].id), Qnone, (yyval.node), &(yyloc));
		    }
#line 9812 "parse.c"
    break;

  case 420:
#line 3591 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 9820 "parse.c"
    break;

  case 421:
#line 3595 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-1].node), Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 9828 "parse.c"
    break;

  case 422:
#line 3599 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 9836 "parse.c"
    break;

  case 423:
#line 3603 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 9844 "parse.c"
    break;

  case 424:
#line 3607 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 9852 "parse.c"
    break;

  case 425:
#line 3611 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 9860 "parse.c"
    break;

  case 426:
#line 3615 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 9868 "parse.c"
    break;

  case 427:
#line 3619 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 9876 "parse.c"
    break;

  case 428:
#line 3623 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 9884 "parse.c"
    break;

  case 430:
#line 3630 "parse.y"
                    {
			p->command_start = TRUE;
		    }
#line 9892 "parse.c"
    break;

  case 431:
#line 3636 "parse.y"
                    {
			p->cur_arg = 0;
			p->max_numparam = ORDINAL_PARAM;
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: block_var!(params!(Qnil,Qnil,Qnil,Qnil,Qnil,Qnil,Qnil), escape_Qundef($2)) %*/
		    }
#line 9905 "parse.c"
    break;

  case 432:
#line 3645 "parse.y"
                    {
			p->cur_arg = 0;
			p->max_numparam = ORDINAL_PARAM;
		    /*%%%*/
			(yyval.node) = (yyvsp[-2].node);
		    /*% %*/
		    /*% ripper: block_var!(escape_Qundef($2), escape_Qundef($3)) %*/
		    }
#line 9918 "parse.c"
    break;

  case 433:
#line 3657 "parse.y"
                    {
		      (yyval.node) = 0;
		    }
#line 9926 "parse.c"
    break;

  case 434:
#line 3661 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: $3 %*/
		    }
#line 9937 "parse.c"
    break;

  case 437:
#line 3676 "parse.y"
                    {
			new_bv(p, get_id((yyvsp[0].id)));
		    /*% ripper: get_value($1) %*/
		    }
#line 9946 "parse.c"
    break;

  case 438:
#line 3681 "parse.y"
                    {
			(yyval.node) = 0;
		    }
#line 9954 "parse.c"
    break;

  case 439:
#line 3687 "parse.y"
                    {
			token_info_push(p, "->", &(yylsp[0]));
			(yyvsp[0].vars) = dyna_push(p);
			(yyval.num) = p->lex.lpar_beg;
			p->lex.lpar_beg = p->lex.paren_nest;
		    }
#line 9965 "parse.c"
    break;

  case 440:
#line 3693 "parse.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 9974 "parse.c"
    break;

  case 441:
#line 3697 "parse.y"
                    {
			(yyval.node) = numparam_push(p);
		    }
#line 9982 "parse.c"
    break;

  case 442:
#line 3701 "parse.y"
                    {
			CMDARG_PUSH(0);
		    }
#line 9990 "parse.c"
    break;

  case 443:
#line 3705 "parse.y"
                    {
			int max_numparam = p->max_numparam;
			p->lex.lpar_beg = (yyvsp[-5].num);
			p->max_numparam = (yyvsp[-4].num);
			CMDARG_POP();
			(yyvsp[-2].node) = args_with_numbered(p, (yyvsp[-2].node), max_numparam);
		    /*%%%*/
                        {
                            YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
                            (yyval.node) = NEW_LAMBDA((yyvsp[-2].node), (yyvsp[0].node), &loc);
                            nd_set_line((yyval.node)->nd_body, (yylsp[0]).end_pos.lineno);
                            nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
			    nd_set_first_loc((yyval.node), (yylsp[-6]).beg_pos);
                        }
		    /*% %*/
		    /*% ripper: lambda!($5, $7) %*/
			numparam_pop(p, (yyvsp[-3].node));
			dyna_pop(p, (yyvsp[-6].vars));
		    }
#line 10014 "parse.c"
    break;

  case 444:
#line 3727 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-2].node);
			p->max_numparam = ORDINAL_PARAM;
		    /*% %*/
		    /*% ripper: paren!($2) %*/
		    }
#line 10026 "parse.c"
    break;

  case 445:
#line 3735 "parse.y"
                    {
		    /*%%%*/
			if (!args_info_empty_p((yyvsp[0].node)->nd_ainfo))
			    p->max_numparam = ORDINAL_PARAM;
		    /*% %*/
			(yyval.node) = (yyvsp[0].node);
		    }
#line 10038 "parse.c"
    break;

  case 446:
#line 3745 "parse.y"
                    {
			token_info_pop(p, "}", &(yylsp[0]));
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 10047 "parse.c"
    break;

  case 447:
#line 3750 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 10055 "parse.c"
    break;

  case 448:
#line 3756 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 10067 "parse.c"
    break;

  case 449:
#line 3766 "parse.y"
                    {
		    /*%%%*/
			if (nd_type((yyvsp[-1].node)) == NODE_YIELD) {
			    compile_error(p, "block given to yield");
			}
			else {
			    block_dup_check(p, (yyvsp[-1].node)->nd_args, (yyvsp[0].node));
			}
			(yyval.node) = method_add_block(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: method_add_block!($1, $2) %*/
		    }
#line 10085 "parse.c"
    break;

  case 450:
#line 3780 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: opt_event(:method_add_arg!, call!($1, $2, $3), $4) %*/
		    }
#line 10096 "parse.c"
    break;

  case 451:
#line 3787 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-3].id), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: opt_event(:method_add_block!, command_call!($1, $2, $3, $4), $5) %*/
		    }
#line 10107 "parse.c"
    break;

  case 452:
#line 3794 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-3].id), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(command_call!($1, $2, $3, $4), $5) %*/
		    }
#line 10118 "parse.c"
    break;

  case 453:
#line 3803 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
			(yyval.node)->nd_args = (yyvsp[0].node);
			nd_set_last_loc((yyvsp[-1].node), (yylsp[0]).end_pos);
		    /*% %*/
		    /*% ripper: method_add_arg!(fcall!($1), $2) %*/
		    }
#line 10131 "parse.c"
    break;

  case 454:
#line 3812 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: opt_event(:method_add_arg!, call!($1, $2, $3), $4) %*/
		    }
#line 10143 "parse.c"
    break;

  case 455:
#line 3820 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, ID2VAL(idCOLON2), $3), $4) %*/
		    }
#line 10155 "parse.c"
    break;

  case 456:
#line 3828 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].node), (yyvsp[0].id), Qnull, &(yylsp[0]), &(yyloc));
		    /*% %*/
		    /*% ripper: call!($1, ID2VAL(idCOLON2), $3) %*/
		    }
#line 10166 "parse.c"
    break;

  case 457:
#line 3835 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-1].id), (yyvsp[-2].node), ID2VAL(idCall), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, $2, ID2VAL(idCall)), $3) %*/
		    }
#line 10178 "parse.c"
    break;

  case 458:
#line 3843 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].node), ID2VAL(idCall), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, ID2VAL(idCOLON2), ID2VAL(idCall)), $3) %*/
		    }
#line 10190 "parse.c"
    break;

  case 459:
#line 3851 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SUPER((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: super!($2) %*/
		    }
#line 10201 "parse.c"
    break;

  case 460:
#line 3858 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_ZSUPER(&(yyloc));
		    /*% %*/
		    /*% ripper: zsuper! %*/
		    }
#line 10212 "parse.c"
    break;

  case 461:
#line 3865 "parse.y"
                    {
		    /*%%%*/
			if ((yyvsp[-3].node) && nd_type((yyvsp[-3].node)) == NODE_SELF)
			    (yyval.node) = NEW_FCALL(tAREF, (yyvsp[-1].node), &(yyloc));
			else
			    (yyval.node) = NEW_CALL((yyvsp[-3].node), tAREF, (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-3].node));
		    /*% %*/
		    /*% ripper: aref!($1, escape_Qundef($3)) %*/
		    }
#line 10227 "parse.c"
    break;

  case 462:
#line 3878 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 10239 "parse.c"
    break;

  case 463:
#line 3886 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 10251 "parse.c"
    break;

  case 464:
#line 3895 "parse.y"
                  {(yyval.vars) = dyna_push(p);}
#line 10257 "parse.c"
    break;

  case 465:
#line 3896 "parse.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 10266 "parse.c"
    break;

  case 466:
#line 3900 "parse.y"
                    {
			(yyval.node) = numparam_push(p);
		    }
#line 10274 "parse.c"
    break;

  case 467:
#line 3904 "parse.y"
                    {
			int max_numparam = p->max_numparam;
			p->max_numparam = (yyvsp[-3].num);
			(yyvsp[-1].node) = args_with_numbered(p, (yyvsp[-1].node), max_numparam);
		    /*%%%*/
			(yyval.node) = NEW_ITER((yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: brace_block!(escape_Qundef($4), $5) %*/
			numparam_pop(p, (yyvsp[-2].node));
			dyna_pop(p, (yyvsp[-4].vars));
		    }
#line 10290 "parse.c"
    break;

  case 468:
#line 3917 "parse.y"
                  {(yyval.vars) = dyna_push(p);}
#line 10296 "parse.c"
    break;

  case 469:
#line 3918 "parse.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 10305 "parse.c"
    break;

  case 470:
#line 3922 "parse.y"
                    {
			(yyval.node) = numparam_push(p);
			CMDARG_PUSH(0);
		    }
#line 10314 "parse.c"
    break;

  case 471:
#line 3927 "parse.y"
                    {
			int max_numparam = p->max_numparam;
			p->max_numparam = (yyvsp[-3].num);
			(yyvsp[-1].node) = args_with_numbered(p, (yyvsp[-1].node), max_numparam);
		    /*%%%*/
			(yyval.node) = NEW_ITER((yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: do_block!(escape_Qundef($4), $5) %*/
			CMDARG_POP();
			numparam_pop(p, (yyvsp[-2].node));
			dyna_pop(p, (yyvsp[-4].vars));
		    }
#line 10331 "parse.c"
    break;

  case 472:
#line 3942 "parse.y"
                    {
		    /*%%%*/
			check_literal_when(p, (yyvsp[0].node), &(yylsp[0]));
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!(args_new!, $1) %*/
		    }
#line 10343 "parse.c"
    break;

  case 473:
#line 3950 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SPLAT((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!(args_new!, $2) %*/
		    }
#line 10354 "parse.c"
    break;

  case 474:
#line 3957 "parse.y"
                    {
		    /*%%%*/
			check_literal_when(p, (yyvsp[0].node), &(yylsp[0]));
			(yyval.node) = last_arg_append(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!($1, $3) %*/
		    }
#line 10366 "parse.c"
    break;

  case 475:
#line 3965 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!($1, $4) %*/
		    }
#line 10377 "parse.c"
    break;

  case 476:
#line 3976 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_WHEN((yyvsp[-3].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-3].node));
		    /*% %*/
		    /*% ripper: when!($2, $4, escape_Qundef($5)) %*/
		    }
#line 10389 "parse.c"
    break;

  case 479:
#line 3990 "parse.y"
                    {
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 10401 "parse.c"
    break;

  case 480:
#line 3997 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
		    }
#line 10409 "parse.c"
    break;

  case 481:
#line 4001 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			pop_pvtbl(p, (yyvsp[-3].tbl));
			p->ctxt.in_kwarg = (yyvsp[-4].ctxt).in_kwarg;
		    }
#line 10419 "parse.c"
    break;

  case 482:
#line 4008 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_IN((yyvsp[-4].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: in!($4, $7, escape_Qundef($8)) %*/
		    }
#line 10430 "parse.c"
    break;

  case 486:
#line 4022 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[0].node), remove_begin((yyvsp[-2].node)), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: if_mod!($3, $1) %*/
		    }
#line 10442 "parse.c"
    break;

  case 487:
#line 4030 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_unless(p, (yyvsp[0].node), remove_begin((yyvsp[-2].node)), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: unless_mod!($3, $1) %*/
		    }
#line 10454 "parse.c"
    break;

  case 489:
#line 4041 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 1, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, Qnone, get_value((yyvsp[-1].node)), (yyval.node), &(yyloc));
		    }
#line 10463 "parse.c"
    break;

  case 490:
#line 4046 "parse.y"
                    {
			(yyval.node) = new_array_pattern(p, Qnone, get_value((yyvsp[-2].node)), (yyvsp[0].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-2]).beg_pos);
		    /*%
		    %*/
		    }
#line 10475 "parse.c"
    break;

  case 491:
#line 4054 "parse.y"
                    {
			(yyval.node) = new_find_pattern(p, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10483 "parse.c"
    break;

  case 492:
#line 4058 "parse.y"
                    {
			(yyval.node) = new_array_pattern(p, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10491 "parse.c"
    break;

  case 493:
#line 4062 "parse.y"
                    {
			(yyval.node) = new_hash_pattern(p, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10499 "parse.c"
    break;

  case 495:
#line 4071 "parse.y"
                    {
		    /*%%%*/
			NODE *n = NEW_LIST((yyvsp[-2].node), &(yyloc));
			n = list_append(p, n, (yyvsp[0].node));
			(yyval.node) = new_hash(p, n, &(yyloc));
		    /*% %*/
		    /*% ripper: binary!($1, STATIC_ID2SYM((id_assoc)), $3) %*/
		    }
#line 10512 "parse.c"
    break;

  case 497:
#line 4083 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_NODE(NODE_OR, (yyvsp[-2].node), (yyvsp[0].node), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: binary!($1, STATIC_ID2SYM(idOr), $3) %*/
		    }
#line 10523 "parse.c"
    break;

  case 499:
#line 4092 "parse.y"
                      {(yyval.tbl) = push_pktbl(p);}
#line 10529 "parse.c"
    break;

  case 500:
#line 4093 "parse.y"
                      {(yyval.tbl) = push_pktbl(p);}
#line 10535 "parse.c"
    break;

  case 502:
#line 4097 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_array_pattern(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 10548 "parse.c"
    break;

  case 503:
#line 4106 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_find_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 10561 "parse.c"
    break;

  case 504:
#line 4115 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_hash_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 10574 "parse.c"
    break;

  case 505:
#line 4124 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, (yyvsp[-2].node), Qnone, (yyval.node), &(yyloc));
		    }
#line 10583 "parse.c"
    break;

  case 506:
#line 4129 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_array_pattern(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 10596 "parse.c"
    break;

  case 507:
#line 4138 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_find_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 10609 "parse.c"
    break;

  case 508:
#line 4147 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_hash_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 10622 "parse.c"
    break;

  case 509:
#line 4156 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, (yyvsp[-2].node), Qnone, (yyval.node), &(yyloc));
		    }
#line 10631 "parse.c"
    break;

  case 510:
#line 4161 "parse.y"
                    {
			(yyval.node) = new_array_pattern(p, Qnone, Qnone, (yyvsp[-1].node), &(yyloc));
		    }
#line 10639 "parse.c"
    break;

  case 511:
#line 4165 "parse.y"
                    {
			(yyval.node) = new_find_pattern(p, Qnone, (yyvsp[-1].node), &(yyloc));
		    }
#line 10647 "parse.c"
    break;

  case 512:
#line 4169 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, Qnone, Qnone, (yyval.node), &(yyloc));
		    }
#line 10656 "parse.c"
    break;

  case 513:
#line 4174 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 0;
		    }
#line 10666 "parse.c"
    break;

  case 514:
#line 4180 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
			(yyval.node) = new_hash_pattern(p, Qnone, (yyvsp[-1].node), &(yyloc));
		    }
#line 10676 "parse.c"
    break;

  case 515:
#line 4186 "parse.y"
                    {
			(yyval.node) = new_hash_pattern_tail(p, Qnone, 0, &(yyloc));
			(yyval.node) = new_hash_pattern(p, Qnone, (yyval.node), &(yyloc));
		    }
#line 10685 "parse.c"
    break;

  case 516:
#line 4190 "parse.y"
                          {(yyval.tbl) = push_pktbl(p);}
#line 10691 "parse.c"
    break;

  case 517:
#line 4191 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 10700 "parse.c"
    break;

  case 518:
#line 4198 "parse.y"
                    {
		    /*%%%*/
			NODE *pre_args = NEW_LIST((yyvsp[0].node), &(yyloc));
			(yyval.node) = new_array_pattern_tail(p, pre_args, 0, 0, Qnone, &(yyloc));
		    /*%
			$$ = new_array_pattern_tail(p, rb_ary_new_from_args(1, get_value($1)), 0, 0, Qnone, &@$);
		    %*/
		    }
#line 10713 "parse.c"
    break;

  case 519:
#line 4207 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[0].node), 1, 0, Qnone, &(yyloc));
		    }
#line 10721 "parse.c"
    break;

  case 520:
#line 4211 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_array_pattern_tail(p, list_concat((yyvsp[-1].node), (yyvsp[0].node)), 0, 0, Qnone, &(yyloc));
		    /*%
			VALUE pre_args = rb_ary_concat($1, get_value($2));
			$$ = new_array_pattern_tail(p, pre_args, 0, 0, Qnone, &@$);
		    %*/
		    }
#line 10734 "parse.c"
    break;

  case 521:
#line 4220 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-2].node), 1, (yyvsp[0].id), Qnone, &(yyloc));
		    }
#line 10742 "parse.c"
    break;

  case 522:
#line 4224 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-4].node), 1, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    }
#line 10750 "parse.c"
    break;

  case 523:
#line 4228 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-1].node), 1, 0, Qnone, &(yyloc));
		    }
#line 10758 "parse.c"
    break;

  case 524:
#line 4232 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-3].node), 1, 0, (yyvsp[0].node), &(yyloc));
		    }
#line 10766 "parse.c"
    break;

  case 526:
#line 4239 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 10774 "parse.c"
    break;

  case 527:
#line 4243 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_concat((yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: rb_ary_concat($1, get_value($2)) %*/
		    }
#line 10785 "parse.c"
    break;

  case 528:
#line 4252 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 1, (yyvsp[0].id), Qnone, &(yyloc));
		    }
#line 10793 "parse.c"
    break;

  case 529:
#line 4256 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 1, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    }
#line 10801 "parse.c"
    break;

  case 530:
#line 4262 "parse.y"
                    {
			(yyval.node) = new_find_pattern_tail(p, (yyvsp[-4].id), (yyvsp[-2].node), (yyvsp[0].id), &(yyloc));

			if (rb_warning_category_enabled_p(RB_WARN_CATEGORY_EXPERIMENTAL))
			    rb_warn0L_experimental(nd_line((yyval.node)), "Find pattern is experimental, and the behavior may change in future versions of Ruby!");
		    }
#line 10812 "parse.c"
    break;

  case 531:
#line 4272 "parse.y"
                    {
			(yyval.id) = (yyvsp[0].id);
		    }
#line 10820 "parse.c"
    break;

  case 532:
#line 4276 "parse.y"
                    {
			(yyval.id) = 0;
		    }
#line 10828 "parse.c"
    break;

  case 534:
#line 4283 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_concat((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_concat($1, get_value($3)) %*/
		    }
#line 10839 "parse.c"
    break;

  case 535:
#line 4292 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_ary_new_from_args(1, get_value($1)) %*/
		    }
#line 10850 "parse.c"
    break;

  case 536:
#line 4301 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[-2].node), &(yyloc)), (yyvsp[0].id), &(yyloc));
		    }
#line 10858 "parse.c"
    break;

  case 537:
#line 4305 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[0].node), &(yyloc)), 0, &(yyloc));
		    }
#line 10866 "parse.c"
    break;

  case 538:
#line 4309 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[-1].node), &(yyloc)), 0, &(yyloc));
		    }
#line 10874 "parse.c"
    break;

  case 539:
#line 4313 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_hash(p, Qnone, &(yyloc)), (yyvsp[0].id), &(yyloc));
		    }
#line 10882 "parse.c"
    break;

  case 541:
#line 4321 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_concat((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, $3) %*/
		    }
#line 10893 "parse.c"
    break;

  case 542:
#line 4330 "parse.y"
                    {
			error_duplicate_pattern_key(p, get_id((yyvsp[-1].id)), &(yylsp[-1]));
		    /*%%%*/
			(yyval.node) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[-1].id)), &(yyloc)), &(yyloc)), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_new_from_args(2, get_value($1), get_value($2)) %*/
		    }
#line 10905 "parse.c"
    break;

  case 543:
#line 4338 "parse.y"
                    {
			error_duplicate_pattern_key(p, get_id((yyvsp[0].id)), &(yylsp[0]));
			if ((yyvsp[0].id) && !is_local_id(get_id((yyvsp[0].id)))) {
			    yyerror1(&(yylsp[0]), "key must be valid as local variables");
			}
			error_duplicate_pattern_variable(p, get_id((yyvsp[0].id)), &(yylsp[0]));
		    /*%%%*/
			(yyval.node) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[0].id)), &(yyloc)), &(yyloc)), assignable(p, (yyvsp[0].id), 0, &(yyloc)));
		    /*% %*/
		    /*% ripper: rb_ary_new_from_args(2, get_value($1), Qnil) %*/
		    }
#line 10921 "parse.c"
    break;

  case 545:
#line 4353 "parse.y"
                    {
			YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
		    /*%%%*/
			if (!(yyvsp[-1].node) || nd_type((yyvsp[-1].node)) == NODE_STR) {
			    NODE *node = dsym_node(p, (yyvsp[-1].node), &loc);
			    (yyval.id) = SYM2ID(node->nd_lit);
			}
		    /*%
			if (ripper_is_node_yylval($2) && RNODE($2)->nd_cval) {
			    VALUE label = RNODE($2)->nd_cval;
			    VALUE rval = RNODE($2)->nd_rval;
			    $$ = ripper_new_yylval(p, rb_intern_str(label), rval, label);
			    RNODE($$)->nd_loc = loc;
			}
		    %*/
			else {
			    yyerror1(&loc, "symbol literal with interpolation is not allowed");
			    (yyval.id) = 0;
			}
		    }
#line 10946 "parse.c"
    break;

  case 546:
#line 4376 "parse.y"
                    {
		        (yyval.id) = (yyvsp[0].id);
		    }
#line 10954 "parse.c"
    break;

  case 547:
#line 4380 "parse.y"
                    {
		        (yyval.id) = 0;
		    }
#line 10962 "parse.c"
    break;

  case 548:
#line 4386 "parse.y"
                    {
		        (yyval.id) = 0;
		    }
#line 10970 "parse.c"
    break;

  case 550:
#line 4392 "parse.y"
                             {(yyval.id) = ID2VAL(idNil);}
#line 10976 "parse.c"
    break;

  case 552:
#line 4397 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, $3) %*/
		    }
#line 10989 "parse.c"
    break;

  case 553:
#line 4406 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, $3) %*/
		    }
#line 11002 "parse.c"
    break;

  case 554:
#line 4415 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT2((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, Qnil) %*/
		    }
#line 11014 "parse.c"
    break;

  case 555:
#line 4423 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT3((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, Qnil) %*/
		    }
#line 11026 "parse.c"
    break;

  case 559:
#line 4434 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!(Qnil, $2) %*/
		    }
#line 11038 "parse.c"
    break;

  case 560:
#line 4442 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!(Qnil, $2) %*/
		    }
#line 11050 "parse.c"
    break;

  case 569:
#line 4460 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc)))) (yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 11061 "parse.c"
    break;

  case 571:
#line 4470 "parse.y"
                    {
		    /*%%%*/
			error_duplicate_pattern_variable(p, (yyvsp[0].id), &(yylsp[0]));
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 11073 "parse.c"
    break;

  case 572:
#line 4480 "parse.y"
                    {
		    /*%%%*/
			NODE *n = gettable(p, (yyvsp[0].id), &(yyloc));
			if (!(nd_type(n) == NODE_LVAR || nd_type(n) == NODE_DVAR)) {
			    compile_error(p, "%"PRIsVALUE": no such local variable", rb_id2str((yyvsp[0].id)));
			}
			(yyval.node) = n;
		    /*% %*/
		    /*% ripper: var_ref!($2) %*/
		    }
#line 11088 "parse.c"
    break;

  case 573:
#line 4493 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON3((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: top_const_ref!($2) %*/
		    }
#line 11099 "parse.c"
    break;

  case 574:
#line 4500 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_ref!($1, $3) %*/
		    }
#line 11110 "parse.c"
    break;

  case 575:
#line 4507 "parse.y"
                   {
		    /*%%%*/
			(yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		   }
#line 11121 "parse.c"
    break;

  case 576:
#line 4518 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RESBODY((yyvsp[-4].node),
					 (yyvsp[-3].node) ? block_append(p, node_assign(p, (yyvsp[-3].node), NEW_ERRINFO(&(yylsp[-3])), NO_LEX_CTXT, &(yylsp[-3])), (yyvsp[-1].node)) : (yyvsp[-1].node),
					 (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node)?(yyvsp[-4].node):(yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: rescue!(escape_Qundef($2), escape_Qundef($3), escape_Qundef($5), escape_Qundef($6)) %*/
		    }
#line 11135 "parse.c"
    break;

  case 578:
#line 4531 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 11146 "parse.c"
    break;

  case 579:
#line 4538 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = splat_array((yyvsp[0].node)))) (yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 11157 "parse.c"
    break;

  case 581:
#line 4548 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 11165 "parse.c"
    break;

  case 583:
#line 4555 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: ensure!($2) %*/
		    }
#line 11176 "parse.c"
    break;

  case 587:
#line 4569 "parse.y"
                    {
		    /*%%%*/
			NODE *node = (yyvsp[0].node);
			if (!node) {
			    node = NEW_STR(STR_NEW0(), &(yyloc));
                            RB_OBJ_WRITTEN(p->ast, Qnil, node->nd_lit);
			}
			else {
			    node = evstr2dstr(p, node);
			}
			(yyval.node) = node;
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 11195 "parse.c"
    break;

  case 590:
#line 4588 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: string_concat!($1, $2) %*/
		    }
#line 11206 "parse.c"
    break;

  case 591:
#line 4597 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = heredoc_dedent(p, (yyvsp[-1].node));
			if ((yyval.node)) nd_set_loc((yyval.node), &(yyloc));
		    /*% %*/
		    /*% ripper: string_literal!(heredoc_dedent(p, $2)) %*/
		    }
#line 11218 "parse.c"
    break;

  case 592:
#line 4607 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_xstring(p, heredoc_dedent(p, (yyvsp[-1].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: xstring_literal!(heredoc_dedent(p, $2)) %*/
		    }
#line 11229 "parse.c"
    break;

  case 593:
#line 4616 "parse.y"
                    {
			(yyval.node) = new_regexp(p, (yyvsp[-1].node), (yyvsp[0].num), &(yyloc));
		    }
#line 11237 "parse.c"
    break;

  case 594:
#line 4622 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 11248 "parse.c"
    break;

  case 595:
#line 4631 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: words_new! %*/
		    }
#line 11259 "parse.c"
    break;

  case 596:
#line 4638 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), evstr2dstr(p, (yyvsp[-1].node)));
		    /*% %*/
		    /*% ripper: words_add!($1, $2) %*/
		    }
#line 11270 "parse.c"
    break;

  case 598:
#line 4649 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: word_add!($1, $2) %*/
		    }
#line 11281 "parse.c"
    break;

  case 599:
#line 4658 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 11292 "parse.c"
    break;

  case 600:
#line 4667 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: symbols_new! %*/
		    }
#line 11303 "parse.c"
    break;

  case 601:
#line 4674 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = symbol_append(p, (yyvsp[-2].node), evstr2dstr(p, (yyvsp[-1].node)));
		    /*% %*/
		    /*% ripper: symbols_add!($1, $2) %*/
		    }
#line 11314 "parse.c"
    break;

  case 602:
#line 4683 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 11325 "parse.c"
    break;

  case 603:
#line 4692 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 11336 "parse.c"
    break;

  case 604:
#line 4701 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: qwords_new! %*/
		    }
#line 11347 "parse.c"
    break;

  case 605:
#line 4708 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: qwords_add!($1, $2) %*/
		    }
#line 11358 "parse.c"
    break;

  case 606:
#line 4717 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: qsymbols_new! %*/
		    }
#line 11369 "parse.c"
    break;

  case 607:
#line 4724 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = symbol_append(p, (yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: qsymbols_add!($1, $2) %*/
		    }
#line 11380 "parse.c"
    break;

  case 608:
#line 4733 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: string_content! %*/
		    /*%%%*/
		    /*%
			$$ = ripper_new_yylval(p, 0, $$, 0);
		    %*/
		    }
#line 11395 "parse.c"
    break;

  case 609:
#line 4744 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: string_add!($1, $2) %*/
		    /*%%%*/
		    /*%
			if (ripper_is_node_yylval($1) && ripper_is_node_yylval($2) &&
			    !RNODE($1)->nd_cval) {
			    RNODE($1)->nd_cval = RNODE($2)->nd_cval;
			    RNODE($1)->nd_rval = add_mark_object(p, $$);
			    $$ = $1;
			}
		    %*/
		    }
#line 11415 "parse.c"
    break;

  case 610:
#line 4762 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: xstring_new! %*/
		    }
#line 11426 "parse.c"
    break;

  case 611:
#line 4769 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: xstring_add!($1, $2) %*/
		    }
#line 11437 "parse.c"
    break;

  case 612:
#line 4778 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: regexp_new! %*/
		    /*%%%*/
		    /*%
			$$ = ripper_new_yylval(p, 0, $$, 0);
		    %*/
		    }
#line 11452 "parse.c"
    break;

  case 613:
#line 4789 "parse.y"
                    {
		    /*%%%*/
			NODE *head = (yyvsp[-1].node), *tail = (yyvsp[0].node);
			if (!head) {
			    (yyval.node) = tail;
			}
			else if (!tail) {
			    (yyval.node) = head;
			}
			else {
			    switch (nd_type(head)) {
			      case NODE_STR:
				nd_set_type(head, NODE_DSTR);
				break;
			      case NODE_DSTR:
				break;
			      default:
				head = list_append(p, NEW_DSTR(Qnil, &(yyloc)), head);
				break;
			    }
			    (yyval.node) = list_append(p, head, tail);
			}
		    /*%
			VALUE s1 = 1, s2 = 0, n1 = $1, n2 = $2;
			if (ripper_is_node_yylval(n1)) {
			    s1 = RNODE(n1)->nd_cval;
			    n1 = RNODE(n1)->nd_rval;
			}
			if (ripper_is_node_yylval(n2)) {
			    s2 = RNODE(n2)->nd_cval;
			    n2 = RNODE(n2)->nd_rval;
			}
			$$ = dispatch2(regexp_add, n1, n2);
			if (!s1 && s2) {
			    $$ = ripper_new_yylval(p, 0, $$, s2);
			}
		    %*/
		    }
#line 11495 "parse.c"
    break;

  case 615:
#line 4832 "parse.y"
                    {
			/* need to backup p->lex.strterm so that a string literal `%&foo,#$&,bar&` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 11506 "parse.c"
    break;

  case 616:
#line 4839 "parse.y"
                    {
			p->lex.strterm = (yyvsp[-1].strterm);
		    /*%%%*/
			(yyval.node) = NEW_EVSTR((yyvsp[0].node), &(yyloc));
			nd_set_line((yyval.node), (yylsp[0]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: string_dvar!($3) %*/
		    }
#line 11519 "parse.c"
    break;

  case 617:
#line 4848 "parse.y"
                    {
			CMDARG_PUSH(0);
			COND_PUSH(0);
		    }
#line 11528 "parse.c"
    break;

  case 618:
#line 4852 "parse.y"
                    {
			/* need to backup p->lex.strterm so that a string literal `%!foo,#{ !0 },bar!` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
		    }
#line 11538 "parse.c"
    break;

  case 619:
#line 4857 "parse.y"
                    {
			(yyval.num) = p->lex.state;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 11547 "parse.c"
    break;

  case 620:
#line 4861 "parse.y"
                    {
			(yyval.num) = p->lex.brace_nest;
			p->lex.brace_nest = 0;
		    }
#line 11556 "parse.c"
    break;

  case 621:
#line 4865 "parse.y"
                    {
			(yyval.num) = p->heredoc_indent;
			p->heredoc_indent = 0;
		    }
#line 11565 "parse.c"
    break;

  case 622:
#line 4870 "parse.y"
                    {
			COND_POP();
			CMDARG_POP();
			p->lex.strterm = (yyvsp[-5].strterm);
			SET_LEX_STATE((yyvsp[-4].num));
			p->lex.brace_nest = (yyvsp[-3].num);
			p->heredoc_indent = (yyvsp[-2].num);
			p->heredoc_line_indent = -1;
		    /*%%%*/
			if ((yyvsp[-1].node)) (yyvsp[-1].node)->flags &= ~NODE_FL_NEWLINE;
			(yyval.node) = new_evstr(p, (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: string_embexpr!($7) %*/
		    }
#line 11584 "parse.c"
    break;

  case 623:
#line 4887 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_GVAR((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 11595 "parse.c"
    break;

  case 624:
#line 4894 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_IVAR((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 11606 "parse.c"
    break;

  case 625:
#line 4901 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_CVAR((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 11617 "parse.c"
    break;

  case 629:
#line 4915 "parse.y"
                    {
			SET_LEX_STATE(EXPR_END);
		    /*%%%*/
			(yyval.node) = NEW_LIT(ID2SYM((yyvsp[0].id)), &(yyloc));
		    /*% %*/
		    /*% ripper: symbol_literal!(symbol!($2)) %*/
		    }
#line 11629 "parse.c"
    break;

  case 634:
#line 4931 "parse.y"
                    {
			SET_LEX_STATE(EXPR_END);
		    /*%%%*/
			(yyval.node) = dsym_node(p, (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dyna_symbol!($2) %*/
		    }
#line 11641 "parse.c"
    break;

  case 636:
#line 4942 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
			RB_OBJ_WRITE(p->ast, &(yyval.node)->nd_lit, negate_lit(p, (yyval.node)->nd_lit));
		    /*% %*/
		    /*% ripper: unary!(ID2VAL(idUMinus), $2) %*/
		    }
#line 11653 "parse.c"
    break;

  case 646:
#line 4964 "parse.y"
                              {(yyval.id) = KWD2EID(nil, (yyvsp[0].id));}
#line 11659 "parse.c"
    break;

  case 647:
#line 4965 "parse.y"
                               {(yyval.id) = KWD2EID(self, (yyvsp[0].id));}
#line 11665 "parse.c"
    break;

  case 648:
#line 4966 "parse.y"
                               {(yyval.id) = KWD2EID(true, (yyvsp[0].id));}
#line 11671 "parse.c"
    break;

  case 649:
#line 4967 "parse.y"
                                {(yyval.id) = KWD2EID(false, (yyvsp[0].id));}
#line 11677 "parse.c"
    break;

  case 650:
#line 4968 "parse.y"
                                  {(yyval.id) = KWD2EID(_FILE__, (yyvsp[0].id));}
#line 11683 "parse.c"
    break;

  case 651:
#line 4969 "parse.y"
                                  {(yyval.id) = KWD2EID(_LINE__, (yyvsp[0].id));}
#line 11689 "parse.c"
    break;

  case 652:
#line 4970 "parse.y"
                                      {(yyval.id) = KWD2EID(_ENCODING__, (yyvsp[0].id));}
#line 11695 "parse.c"
    break;

  case 653:
#line 4974 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc)))) (yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*%
			if (id_is_var(p, get_id($1))) {
			    $$ = dispatch1(var_ref, $1);
			}
			else {
			    $$ = dispatch1(vcall, $1);
			}
		    %*/
		    }
#line 11712 "parse.c"
    break;

  case 654:
#line 4987 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc)))) (yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 11723 "parse.c"
    break;

  case 655:
#line 4996 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 11734 "parse.c"
    break;

  case 656:
#line 5003 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 11745 "parse.c"
    break;

  case 659:
#line 5016 "parse.y"
                    {
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 11754 "parse.c"
    break;

  case 660:
#line 5021 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 11762 "parse.c"
    break;

  case 661:
#line 5025 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: Qnil %*/
		    }
#line 11773 "parse.c"
    break;

  case 664:
#line 5036 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: paren!($2) %*/
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 11786 "parse.c"
    break;

  case 665:
#line 5045 "parse.y"
                    {
			add_forwarding_args(p);
		    /*%%%*/
			(yyval.node) = new_args_forward_def(p, (yyvsp[-3].node), &(yyloc));
		    /*% %*/
		    /*% ripper: paren!(params!($2, Qnone, $4, Qnone, Qnone, Qnone, Qnone)) %*/
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 11800 "parse.c"
    break;

  case 666:
#line 5055 "parse.y"
                    {
			add_forwarding_args(p);
		    /*%%%*/
			(yyval.node) = new_args_forward_def(p, 0, &(yyloc));
		    /*% %*/
		    /*% ripper: paren!(params!(Qnone, Qnone, $2, Qnone, Qnone, Qnone, Qnone)) %*/
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 11814 "parse.c"
    break;

  case 668:
#line 5067 "parse.y"
                    {
			(yyval.ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			SET_LEX_STATE(p->lex.state|EXPR_LABEL); /* force for args */
		    }
#line 11824 "parse.c"
    break;

  case 669:
#line 5073 "parse.y"
                    {
			p->ctxt.in_kwarg = (yyvsp[-2].ctxt).in_kwarg;
			(yyval.node) = (yyvsp[-1].node);
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 11835 "parse.c"
    break;

  case 670:
#line 5082 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 11843 "parse.c"
    break;

  case 671:
#line 5086 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-1].node), Qnone, (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 11851 "parse.c"
    break;

  case 672:
#line 5090 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 11859 "parse.c"
    break;

  case 673:
#line 5094 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].id), &(yylsp[0]));
		    }
#line 11867 "parse.c"
    break;

  case 674:
#line 5100 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 11875 "parse.c"
    break;

  case 675:
#line 5104 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 11883 "parse.c"
    break;

  case 676:
#line 5110 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11891 "parse.c"
    break;

  case 677:
#line 5114 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-7].node), (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 11899 "parse.c"
    break;

  case 678:
#line 5118 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11907 "parse.c"
    break;

  case 679:
#line 5122 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 11915 "parse.c"
    break;

  case 680:
#line 5126 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11923 "parse.c"
    break;

  case 681:
#line 5130 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 11931 "parse.c"
    break;

  case 682:
#line 5134 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-1].node), Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11939 "parse.c"
    break;

  case 683:
#line 5138 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11947 "parse.c"
    break;

  case 684:
#line 5142 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 11955 "parse.c"
    break;

  case 685:
#line 5146 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11963 "parse.c"
    break;

  case 686:
#line 5150 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 11971 "parse.c"
    break;

  case 687:
#line 5154 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11979 "parse.c"
    break;

  case 688:
#line 5158 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 11987 "parse.c"
    break;

  case 689:
#line 5162 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11995 "parse.c"
    break;

  case 690:
#line 5166 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyval.node), &(yylsp[0]));
		    }
#line 12004 "parse.c"
    break;

  case 691:
#line 5173 "parse.y"
                    {
		    /*%%%*/
			(yyval.id) = idDot3;
		    /*% %*/
		    /*% ripper: args_forward! %*/
		    }
#line 12015 "parse.c"
    break;

  case 692:
#line 5182 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be a constant";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 12028 "parse.c"
    break;

  case 693:
#line 5191 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be an instance variable";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 12041 "parse.c"
    break;

  case 694:
#line 5200 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be a global variable";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 12054 "parse.c"
    break;

  case 695:
#line 5209 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be a class variable";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 12067 "parse.c"
    break;

  case 697:
#line 5221 "parse.y"
                    {
			formal_argument(p, (yyvsp[0].id));
			p->max_numparam = ORDINAL_PARAM;
			(yyval.id) = (yyvsp[0].id);
		    }
#line 12077 "parse.c"
    break;

  case 698:
#line 5229 "parse.y"
                    {
			ID id = get_id((yyvsp[0].id));
			arg_var(p, id);
			p->cur_arg = id;
			(yyval.id) = (yyvsp[0].id);
		    }
#line 12088 "parse.c"
    break;

  case 699:
#line 5238 "parse.y"
                    {
			p->cur_arg = 0;
		    /*%%%*/
			(yyval.node) = NEW_ARGS_AUX((yyvsp[0].id), 1, &NULL_LOC);
		    /*% %*/
		    /*% ripper: get_value($1) %*/
		    }
#line 12100 "parse.c"
    break;

  case 700:
#line 5246 "parse.y"
                    {
		    /*%%%*/
			ID tid = internal_id(p);
			YYLTYPE loc;
			loc.beg_pos = (yylsp[-1]).beg_pos;
			loc.end_pos = (yylsp[-1]).beg_pos;
			arg_var(p, tid);
			if (dyna_in_block(p)) {
			    (yyvsp[-1].node)->nd_value = NEW_DVAR(tid, &loc);
			}
			else {
			    (yyvsp[-1].node)->nd_value = NEW_LVAR(tid, &loc);
			}
			(yyval.node) = NEW_ARGS_AUX(tid, 1, &NULL_LOC);
			(yyval.node)->nd_next = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 12123 "parse.c"
    break;

  case 702:
#line 5269 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-2].node);
			(yyval.node)->nd_plen++;
			(yyval.node)->nd_next = block_append(p, (yyval.node)->nd_next, (yyvsp[0].node)->nd_next);
			rb_discard_node(p, (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 12137 "parse.c"
    break;

  case 703:
#line 5282 "parse.y"
                    {
			arg_var(p, formal_argument(p, (yyvsp[0].id)));
			p->cur_arg = get_id((yyvsp[0].id));
			p->max_numparam = ORDINAL_PARAM;
			(yyval.id) = (yyvsp[0].id);
		    }
#line 12148 "parse.c"
    break;

  case 704:
#line 5291 "parse.y"
                    {
			p->cur_arg = 0;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[-1].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($2)) %*/
		    }
#line 12160 "parse.c"
    break;

  case 705:
#line 5299 "parse.y"
                    {
			p->cur_arg = 0;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[0].id), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), 0) %*/
		    }
#line 12172 "parse.c"
    break;

  case 706:
#line 5309 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[-1].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($2)) %*/
		    }
#line 12183 "parse.c"
    break;

  case 707:
#line 5316 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[0].id), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), 0) %*/
		    }
#line 12194 "parse.c"
    break;

  case 708:
#line 5325 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 12205 "parse.c"
    break;

  case 709:
#line 5332 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = kwd_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 12216 "parse.c"
    break;

  case 710:
#line 5342 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 12227 "parse.c"
    break;

  case 711:
#line 5349 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = kwd_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 12238 "parse.c"
    break;

  case 714:
#line 5362 "parse.y"
                    {
		    /*%%%*/
		    /*% %*/
		    /*% ripper: nokw_param!(Qnil) %*/
		    }
#line 12248 "parse.c"
    break;

  case 715:
#line 5370 "parse.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].id))));
		    /*%%%*/
			(yyval.id) = (yyvsp[0].id);
		    /*% %*/
		    /*% ripper: kwrest_param!($2) %*/
		    }
#line 12260 "parse.c"
    break;

  case 716:
#line 5378 "parse.y"
                    {
		    /*%%%*/
			(yyval.id) = internal_id(p);
			arg_var(p, (yyval.id));
		    /*% %*/
		    /*% ripper: kwrest_param!(Qnil) %*/
		    }
#line 12272 "parse.c"
    break;

  case 717:
#line 5388 "parse.y"
                    {
			p->cur_arg = 0;
		    /*%%%*/
			(yyval.node) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($3)) %*/
		    }
#line 12284 "parse.c"
    break;

  case 718:
#line 5398 "parse.y"
                    {
			p->cur_arg = 0;
		    /*%%%*/
			(yyval.node) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($3)) %*/
		    }
#line 12296 "parse.c"
    break;

  case 719:
#line 5408 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 12307 "parse.c"
    break;

  case 720:
#line 5415 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = opt_arg_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 12318 "parse.c"
    break;

  case 721:
#line 5424 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 12329 "parse.c"
    break;

  case 722:
#line 5431 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = opt_arg_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 12340 "parse.c"
    break;

  case 725:
#line 5444 "parse.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].id))));
		    /*%%%*/
			(yyval.id) = (yyvsp[0].id);
		    /*% %*/
		    /*% ripper: rest_param!($2) %*/
		    }
#line 12352 "parse.c"
    break;

  case 726:
#line 5452 "parse.y"
                    {
		    /*%%%*/
			(yyval.id) = internal_id(p);
			arg_var(p, (yyval.id));
		    /*% %*/
		    /*% ripper: rest_param!(Qnil) %*/
		    }
#line 12364 "parse.c"
    break;

  case 729:
#line 5466 "parse.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].id))));
		    /*%%%*/
			(yyval.id) = (yyvsp[0].id);
		    /*% %*/
		    /*% ripper: blockarg!($2) %*/
		    }
#line 12376 "parse.c"
    break;

  case 730:
#line 5476 "parse.y"
                    {
			(yyval.id) = (yyvsp[0].id);
		    }
#line 12384 "parse.c"
    break;

  case 731:
#line 5480 "parse.y"
                    {
			(yyval.id) = Qnull;
		    }
#line 12392 "parse.c"
    break;

  case 732:
#line 5486 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 12401 "parse.c"
    break;

  case 733:
#line 5490 "parse.y"
                      {SET_LEX_STATE(EXPR_BEG);}
#line 12407 "parse.c"
    break;

  case 734:
#line 5491 "parse.y"
                    {
		    /*%%%*/
			switch (nd_type((yyvsp[-1].node))) {
			  case NODE_STR:
			  case NODE_DSTR:
			  case NODE_XSTR:
			  case NODE_DXSTR:
			  case NODE_DREGX:
			  case NODE_LIT:
			  case NODE_LIST:
			  case NODE_ZLIST:
			    yyerror1(&(yylsp[-1]), "can't define singleton method for literals");
			    break;
			  default:
			    value_expr((yyvsp[-1].node));
			    break;
			}
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: paren!($3) %*/
		    }
#line 12433 "parse.c"
    break;

  case 736:
#line 5516 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: assoclist_from_args!($1) %*/
		    }
#line 12444 "parse.c"
    break;

  case 738:
#line 5527 "parse.y"
                    {
		    /*%%%*/
			NODE *assocs = (yyvsp[-2].node);
			NODE *tail = (yyvsp[0].node);
			if (!assocs) {
			    assocs = tail;
			}
			else if (tail) {
                            if (assocs->nd_head &&
                                !tail->nd_head && nd_type(tail->nd_next) == NODE_LIST &&
                                nd_type(tail->nd_next->nd_head) == NODE_HASH) {
                                /* DSTAR */
                                tail = tail->nd_next->nd_head->nd_head;
                            }
			    assocs = list_concat(assocs, tail);
			}
			(yyval.node) = assocs;
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 12469 "parse.c"
    break;

  case 739:
#line 5550 "parse.y"
                    {
		    /*%%%*/
			if (nd_type((yyvsp[-2].node)) == NODE_STR) {
			    nd_set_type((yyvsp[-2].node), NODE_LIT);
			    RB_OBJ_WRITE(p->ast, &(yyvsp[-2].node)->nd_lit, rb_fstring((yyvsp[-2].node)->nd_lit));
			}
			(yyval.node) = list_append(p, NEW_LIST((yyvsp[-2].node), &(yyloc)), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: assoc_new!($1, $3) %*/
		    }
#line 12484 "parse.c"
    break;

  case 740:
#line 5561 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[-1].id)), &(yylsp[-1])), &(yyloc)), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: assoc_new!($1, $2) %*/
		    }
#line 12495 "parse.c"
    break;

  case 741:
#line 5568 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-3]), &(yylsp[-1]));
			(yyval.node) = list_append(p, NEW_LIST(dsym_node(p, (yyvsp[-2].node), &loc), &loc), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: assoc_new!(dyna_symbol!($2), $4) %*/
		    }
#line 12507 "parse.c"
    break;

  case 742:
#line 5576 "parse.y"
                    {
		    /*%%%*/
                        if (nd_type((yyvsp[0].node)) == NODE_HASH &&
                            !((yyvsp[0].node)->nd_head && (yyvsp[0].node)->nd_head->nd_alen)) {
                            static VALUE empty_hash;
                            if (!empty_hash) {
                                empty_hash = rb_obj_freeze(rb_hash_new());
                                rb_gc_register_mark_object(empty_hash);
                            }
                            (yyval.node) = list_append(p, NEW_LIST(0, &(yyloc)), NEW_LIT(empty_hash, &(yyloc)));
                        }
                        else
                            (yyval.node) = list_append(p, NEW_LIST(0, &(yyloc)), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: assoc_splat!($2) %*/
		    }
#line 12528 "parse.c"
    break;

  case 769:
#line 5644 "parse.y"
                      {yyerrok;token_flush(p);}
#line 12534 "parse.c"
    break;

  case 770:
#line 5645 "parse.y"
                       {token_flush(p);}
#line 12540 "parse.c"
    break;

  case 772:
#line 5649 "parse.y"
                            {yyerrok;}
#line 12546 "parse.c"
    break;

  case 773:
#line 5653 "parse.y"
                    {
			(yyval.node) = Qnull;
		    }
#line 12554 "parse.c"
    break;


#line 12558 "parse.c"

      default: break;
    }
  /* User semantic actions sometimes alter yychar, and that requires
     that yytoken be updated with the new translation.  We take the
     approach of translating immediately before every use of yytoken.
     One alternative is translating here after every semantic action,
     but that translation would be missed if the semantic action invokes
     YYABORT, YYACCEPT, or YYERROR immediately after altering yychar or
     if it invokes YYBACKUP.  In the case of YYABORT or YYACCEPT, an
     incorrect destructor might then be invoked immediately.  In the
     case of YYERROR or YYBACKUP, subsequent parser actions might lead
     to an incorrect destructor call or verbose syntax error message
     before the lookahead is translated.  */
  YY_SYMBOL_PRINT ("-> $$ =", yyr1[yyn], &yyval, &yyloc);

  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);

  *++yyvsp = yyval;
  *++yylsp = yyloc;

  /* Now 'shift' the result of the reduction.  Determine what state
     that goes to, based on the state we popped back to and the rule
     number reduced by.  */
  {
    const int yylhs = yyr1[yyn] - YYNTOKENS;
    const int yyi = yypgoto[yylhs] + *yyssp;
    yystate = (0 <= yyi && yyi <= YYLAST && yycheck[yyi] == *yyssp
               ? yytable[yyi]
               : yydefgoto[yylhs]);
  }

  goto yynewstate;


/*--------------------------------------.
| yyerrlab -- here on detecting error.  |
`--------------------------------------*/
yyerrlab:
  /* Make sure we have latest lookahead translation.  See comments at
     user semantic actions for why this is necessary.  */
  yytoken = yychar == YYEMPTY ? YYEMPTY : YYTRANSLATE (yychar);

  /* If not already recovering from an error, report this error.  */
  if (!yyerrstatus)
    {
      ++yynerrs;
#if ! YYERROR_VERBOSE
      yyerror (&yylloc, p, YY_("syntax error"));
#else
# define YYSYNTAX_ERROR yysyntax_error (p, &yymsg_alloc, &yymsg, \
                                        yyssp, yytoken)
      {
        char const *yymsgp = YY_("syntax error");
        int yysyntax_error_status;
        yysyntax_error_status = YYSYNTAX_ERROR;
        if (yysyntax_error_status == 0)
          yymsgp = yymsg;
        else if (yysyntax_error_status == 1)
          {
            if (yymsg != yymsgbuf)
              YYSTACK_FREE (yymsg);
            yymsg = YY_CAST (char *, YYSTACK_ALLOC (YY_CAST (YYSIZE_T, yymsg_alloc)));
            if (!yymsg)
              {
                yymsg = yymsgbuf;
                yymsg_alloc = sizeof yymsgbuf;
                yysyntax_error_status = 2;
              }
            else
              {
                yysyntax_error_status = YYSYNTAX_ERROR;
                yymsgp = yymsg;
              }
          }
        yyerror (&yylloc, p, yymsgp);
        if (yysyntax_error_status == 2)
          goto yyexhaustedlab;
      }
# undef YYSYNTAX_ERROR
#endif
    }

  yyerror_range[1] = yylloc;

  if (yyerrstatus == 3)
    {
      /* If just tried and failed to reuse lookahead token after an
         error, discard it.  */

      if (yychar <= YYEOF)
        {
          /* Return failure if at end of input.  */
          if (yychar == YYEOF)
            YYABORT;
        }
      else
        {
          yydestruct ("Error: discarding",
                      yytoken, &yylval, &yylloc, p);
          yychar = YYEMPTY;
        }
    }

  /* Else will try to reuse lookahead token after shifting the error
     token.  */
  goto yyerrlab1;


/*---------------------------------------------------.
| yyerrorlab -- error raised explicitly by YYERROR.  |
`---------------------------------------------------*/
yyerrorlab:
  /* Pacify compilers when the user code never invokes YYERROR and the
     label yyerrorlab therefore never appears in user code.  */
  if (0)
    YYERROR;

  /* Do not reclaim the symbols of the rule whose action triggered
     this YYERROR.  */
  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);
  yystate = *yyssp;
  goto yyerrlab1;


/*-------------------------------------------------------------.
| yyerrlab1 -- common code for both syntax error and YYERROR.  |
`-------------------------------------------------------------*/
yyerrlab1:
  yyerrstatus = 3;      /* Each real token shifted decrements this.  */

  for (;;)
    {
      yyn = yypact[yystate];
      if (!yypact_value_is_default (yyn))
        {
          yyn += YYTERROR;
          if (0 <= yyn && yyn <= YYLAST && yycheck[yyn] == YYTERROR)
            {
              yyn = yytable[yyn];
              if (0 < yyn)
                break;
            }
        }

      /* Pop the current state because it cannot handle the error token.  */
      if (yyssp == yyss)
        YYABORT;

      yyerror_range[1] = *yylsp;
      yydestruct ("Error: popping",
                  yystos[yystate], yyvsp, yylsp, p);
      YYPOPSTACK (1);
      yystate = *yyssp;
      YY_STACK_PRINT (yyss, yyssp);
    }

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END

  yyerror_range[2] = yylloc;
  /* Using YYLLOC is tempting, but would change the location of
     the lookahead.  YYLOC is available though.  */
  YYLLOC_DEFAULT (yyloc, yyerror_range, 2);
  *++yylsp = yyloc;

  /* Shift the error token.  */
  YY_SYMBOL_PRINT ("Shifting", yystos[yyn], yyvsp, yylsp);

  yystate = yyn;
  goto yynewstate;


/*-------------------------------------.
| yyacceptlab -- YYACCEPT comes here.  |
`-------------------------------------*/
yyacceptlab:
  yyresult = 0;
  goto yyreturn;


/*-----------------------------------.
| yyabortlab -- YYABORT comes here.  |
`-----------------------------------*/
yyabortlab:
  yyresult = 1;
  goto yyreturn;


#if !defined yyoverflow || YYERROR_VERBOSE
/*-------------------------------------------------.
| yyexhaustedlab -- memory exhaustion comes here.  |
`-------------------------------------------------*/
yyexhaustedlab:
  yyerror (&yylloc, p, YY_("memory exhausted"));
  yyresult = 2;
  /* Fall through.  */
#endif


/*-----------------------------------------------------.
| yyreturn -- parsing is finished, return the result.  |
`-----------------------------------------------------*/
yyreturn:
  if (yychar != YYEMPTY)
    {
      /* Make sure we have latest lookahead translation.  See comments at
         user semantic actions for why this is necessary.  */
      yytoken = YYTRANSLATE (yychar);
      yydestruct ("Cleanup: discarding lookahead",
                  yytoken, &yylval, &yylloc, p);
    }
  /* Do not reclaim the symbols of the rule whose action triggered
     this YYABORT or YYACCEPT.  */
  YYPOPSTACK (yylen);
  YY_STACK_PRINT (yyss, yyssp);
  while (yyssp != yyss)
    {
      yydestruct ("Cleanup: popping",
                  yystos[+*yyssp], yyvsp, yylsp, p);
      YYPOPSTACK (1);
    }
#ifndef yyoverflow
  if (yyss != yyssa)
    YYSTACK_FREE (yyss);
#endif
#if YYERROR_VERBOSE
  if (yymsg != yymsgbuf)
    YYSTACK_FREE (yymsg);
#endif
  return yyresult;
}
#line 5657 "parse.y"

# undef p
# undef yylex
# undef yylval
# define yylval  (*p->lval)

static int regx_options(struct parser_params*);
static int tokadd_string(struct parser_params*,int,int,int,long*,rb_encoding**,rb_encoding**);
static void tokaddmbc(struct parser_params *p, int c, rb_encoding *enc);
static enum yytokentype parse_string(struct parser_params*,rb_strterm_literal_t*);
static enum yytokentype here_document(struct parser_params*,rb_strterm_heredoc_t*);

#ifndef RIPPER
# define set_yylval_node(x) {				\
  YYLTYPE _cur_loc;					\
  rb_parser_set_location(p, &_cur_loc);			\
  yylval.node = (x);					\
}
# define set_yylval_str(x) \
do { \
  set_yylval_node(NEW_STR(x, &_cur_loc)); \
  RB_OBJ_WRITTEN(p->ast, Qnil, x); \
} while(0)
# define set_yylval_literal(x) \
do { \
  set_yylval_node(NEW_LIT(x, &_cur_loc)); \
  RB_OBJ_WRITTEN(p->ast, Qnil, x); \
} while(0)
# define set_yylval_num(x) (yylval.num = (x))
# define set_yylval_id(x)  (yylval.id = (x))
# define set_yylval_name(x)  (yylval.id = (x))
# define yylval_id() (yylval.id)
#else
static inline VALUE
ripper_yylval_id(struct parser_params *p, ID x)
{
    return ripper_new_yylval(p, x, ID2SYM(x), 0);
}
# define set_yylval_str(x) (yylval.val = add_mark_object(p, (x)))
# define set_yylval_num(x) (yylval.val = ripper_new_yylval(p, (x), 0, 0))
# define set_yylval_id(x)  (void)(x)
# define set_yylval_name(x) (void)(yylval.val = ripper_yylval_id(p, x))
# define set_yylval_literal(x) add_mark_object(p, (x))
# define set_yylval_node(x) (yylval.val = ripper_new_yylval(p, 0, 0, STR_NEW(p->lex.ptok, p->lex.pcur-p->lex.ptok)))
# define yylval_id() yylval.id
# define _cur_loc NULL_LOC /* dummy */
#endif

#define set_yylval_noname() set_yylval_id(keyword_nil)

#ifndef RIPPER
#define literal_flush(p, ptr) ((p)->lex.ptok = (ptr))
#define dispatch_scan_event(p, t) ((void)0)
#define dispatch_delayed_token(p, t) ((void)0)
#define has_delayed_token(p) (0)
#else
#define literal_flush(p, ptr) ((void)(ptr))

#define yylval_rval (*(RB_TYPE_P(yylval.val, T_NODE) ? &yylval.node->nd_rval : &yylval.val))

static inline VALUE
intern_sym(const char *name)
{
    ID id = rb_intern_const(name);
    return ID2SYM(id);
}

static int
ripper_has_scan_event(struct parser_params *p)
{
    if (p->lex.pcur < p->lex.ptok) rb_raise(rb_eRuntimeError, "lex.pcur < lex.ptok");
    return p->lex.pcur > p->lex.ptok;
}

static VALUE
ripper_scan_event_val(struct parser_params *p, enum yytokentype t)
{
    VALUE str = STR_NEW(p->lex.ptok, p->lex.pcur - p->lex.ptok);
    VALUE rval = ripper_dispatch1(p, ripper_token2eventid(t), str);
    token_flush(p);
    return rval;
}

static void
ripper_dispatch_scan_event(struct parser_params *p, enum yytokentype t)
{
    if (!ripper_has_scan_event(p)) return;
    add_mark_object(p, yylval_rval = ripper_scan_event_val(p, t));
}
#define dispatch_scan_event(p, t) ripper_dispatch_scan_event(p, t)

static void
ripper_dispatch_delayed_token(struct parser_params *p, enum yytokentype t)
{
    int saved_line = p->ruby_sourceline;
    const char *saved_tokp = p->lex.ptok;

    if (NIL_P(p->delayed.token)) return;
    p->ruby_sourceline = p->delayed.line;
    p->lex.ptok = p->lex.pbeg + p->delayed.col;
    add_mark_object(p, yylval_rval = ripper_dispatch1(p, ripper_token2eventid(t), p->delayed.token));
    p->delayed.token = Qnil;
    p->ruby_sourceline = saved_line;
    p->lex.ptok = saved_tokp;
}
#define dispatch_delayed_token(p, t) ripper_dispatch_delayed_token(p, t)
#define has_delayed_token(p) (!NIL_P(p->delayed.token))
#endif /* RIPPER */

static inline int
is_identchar(const char *ptr, const char *MAYBE_UNUSED(ptr_end), rb_encoding *enc)
{
    return rb_enc_isalnum((unsigned char)*ptr, enc) || *ptr == '_' || !ISASCII(*ptr);
}

static inline int
parser_is_identchar(struct parser_params *p)
{
    return !(p)->eofp && is_identchar(p->lex.pcur-1, p->lex.pend, p->enc);
}

static inline int
parser_isascii(struct parser_params *p)
{
    return ISASCII(*(p->lex.pcur-1));
}

static void
token_info_setup(token_info *ptinfo, const char *ptr, const rb_code_location_t *loc)
{
    int column = 1, nonspc = 0, i;
    for (i = 0; i < loc->beg_pos.column; i++, ptr++) {
	if (*ptr == '\t') {
	    column = (((column - 1) / TAB_WIDTH) + 1) * TAB_WIDTH;
	}
	column++;
	if (*ptr != ' ' && *ptr != '\t') {
	    nonspc = 1;
	}
    }

    ptinfo->beg = loc->beg_pos;
    ptinfo->indent = column;
    ptinfo->nonspc = nonspc;
}

static void
token_info_push(struct parser_params *p, const char *token, const rb_code_location_t *loc)
{
    token_info *ptinfo;

    if (!p->token_info_enabled) return;
    ptinfo = ALLOC(token_info);
    ptinfo->token = token;
    ptinfo->next = p->token_info;
    token_info_setup(ptinfo, p->lex.pbeg, loc);

    p->token_info = ptinfo;
}

static void
token_info_pop(struct parser_params *p, const char *token, const rb_code_location_t *loc)
{
    token_info *ptinfo_beg = p->token_info;

    if (!ptinfo_beg) return;
    p->token_info = ptinfo_beg->next;

    /* indentation check of matched keywords (begin..end, if..end, etc.) */
    token_info_warn(p, token, ptinfo_beg, 1, loc);
    ruby_sized_xfree(ptinfo_beg, sizeof(*ptinfo_beg));
}

static void
token_info_drop(struct parser_params *p, const char *token, rb_code_position_t beg_pos)
{
    token_info *ptinfo_beg = p->token_info;

    if (!ptinfo_beg) return;
    p->token_info = ptinfo_beg->next;

    if (ptinfo_beg->beg.lineno != beg_pos.lineno ||
	ptinfo_beg->beg.column != beg_pos.column ||
	strcmp(ptinfo_beg->token, token)) {
	compile_error(p, "token position mismatch: %d:%d:%s expected but %d:%d:%s",
		      beg_pos.lineno, beg_pos.column, token,
		      ptinfo_beg->beg.lineno, ptinfo_beg->beg.column,
		      ptinfo_beg->token);
    }

    ruby_sized_xfree(ptinfo_beg, sizeof(*ptinfo_beg));
}

static void
token_info_warn(struct parser_params *p, const char *token, token_info *ptinfo_beg, int same, const rb_code_location_t *loc)
{
    token_info ptinfo_end_body, *ptinfo_end = &ptinfo_end_body;
    if (!p->token_info_enabled) return;
    if (!ptinfo_beg) return;
    token_info_setup(ptinfo_end, p->lex.pbeg, loc);
    if (ptinfo_beg->beg.lineno == ptinfo_end->beg.lineno) return; /* ignore one-line block */
    if (ptinfo_beg->nonspc || ptinfo_end->nonspc) return; /* ignore keyword in the middle of a line */
    if (ptinfo_beg->indent == ptinfo_end->indent) return; /* the indents are matched */
    if (!same && ptinfo_beg->indent < ptinfo_end->indent) return;
    rb_warn3L(ptinfo_end->beg.lineno,
	      "mismatched indentations at '%s' with '%s' at %d",
	      WARN_S(token), WARN_S(ptinfo_beg->token), WARN_I(ptinfo_beg->beg.lineno));
}

static int
parser_precise_mbclen(struct parser_params *p, const char *ptr)
{
    int len = rb_enc_precise_mbclen(ptr, p->lex.pend, p->enc);
    if (!MBCLEN_CHARFOUND_P(len)) {
	compile_error(p, "invalid multibyte char (%s)", rb_enc_name(p->enc));
	return -1;
    }
    return len;
}

#ifndef RIPPER
static void ruby_show_error_line(VALUE errbuf, const YYLTYPE *yylloc, int lineno, VALUE str);

static inline void
parser_show_error_line(struct parser_params *p, const YYLTYPE *yylloc)
{
    VALUE str;
    int lineno = p->ruby_sourceline;
    if (!yylloc) {
	return;
    }
    else if (yylloc->beg_pos.lineno == lineno) {
	str = p->lex.lastline;
    }
    else {
	return;
    }
    ruby_show_error_line(p->error_buffer, yylloc, lineno, str);
}

static int
parser_yyerror(struct parser_params *p, const YYLTYPE *yylloc, const char *msg)
{
    YYLTYPE current;

    if (!yylloc) {
	yylloc = RUBY_SET_YYLLOC(current);
    }
    else if ((p->ruby_sourceline != yylloc->beg_pos.lineno &&
	      p->ruby_sourceline != yylloc->end_pos.lineno) ||
	     (yylloc->beg_pos.lineno == yylloc->end_pos.lineno &&
	      yylloc->beg_pos.column == yylloc->end_pos.column)) {
	yylloc = 0;
    }
    compile_error(p, "%s", msg);
    parser_show_error_line(p, yylloc);
    return 0;
}

static void
ruby_show_error_line(VALUE errbuf, const YYLTYPE *yylloc, int lineno, VALUE str)
{
    VALUE mesg;
    const int max_line_margin = 30;
    const char *ptr, *ptr_end, *pt, *pb;
    const char *pre = "", *post = "", *pend;
    const char *code = "", *caret = "";
    const char *lim;
    const char *const pbeg = RSTRING_PTR(str);
    char *buf;
    long len;
    int i;

    if (!yylloc) return;
    pend = RSTRING_END(str);
    if (pend > pbeg && pend[-1] == '\n') {
	if (--pend > pbeg && pend[-1] == '\r') --pend;
    }

    pt = pend;
    if (lineno == yylloc->end_pos.lineno &&
	(pend - pbeg) > yylloc->end_pos.column) {
	pt = pbeg + yylloc->end_pos.column;
    }

    ptr = ptr_end = pt;
    lim = ptr - pbeg > max_line_margin ? ptr - max_line_margin : pbeg;
    while ((lim < ptr) && (*(ptr-1) != '\n')) ptr--;

    lim = pend - ptr_end > max_line_margin ? ptr_end + max_line_margin : pend;
    while ((ptr_end < lim) && (*ptr_end != '\n') && (*ptr_end != '\r')) ptr_end++;

    len = ptr_end - ptr;
    if (len > 4) {
	if (ptr > pbeg) {
	    ptr = rb_enc_prev_char(pbeg, ptr, pt, rb_enc_get(str));
	    if (ptr > pbeg) pre = "...";
	}
	if (ptr_end < pend) {
	    ptr_end = rb_enc_prev_char(pt, ptr_end, pend, rb_enc_get(str));
	    if (ptr_end < pend) post = "...";
	}
    }
    pb = pbeg;
    if (lineno == yylloc->beg_pos.lineno) {
	pb += yylloc->beg_pos.column;
	if (pb > pt) pb = pt;
    }
    if (pb < ptr) pb = ptr;
    if (len <= 4 && yylloc->beg_pos.lineno == yylloc->end_pos.lineno) {
	return;
    }
    if (RTEST(errbuf)) {
	mesg = rb_attr_get(errbuf, idMesg);
	if (RSTRING_LEN(mesg) > 0 && *(RSTRING_END(mesg)-1) != '\n')
	    rb_str_cat_cstr(mesg, "\n");
    }
    else {
	mesg = rb_enc_str_new(0, 0, rb_enc_get(str));
    }
    if (!errbuf && rb_stderr_tty_p()) {
#define CSI_BEGIN "\033["
#define CSI_SGR "m"
	rb_str_catf(mesg,
		    CSI_BEGIN""CSI_SGR"%s" /* pre */
		    CSI_BEGIN"1"CSI_SGR"%.*s"
		    CSI_BEGIN"1;4"CSI_SGR"%.*s"
		    CSI_BEGIN";1"CSI_SGR"%.*s"
		    CSI_BEGIN""CSI_SGR"%s" /* post */
		    "\n",
		    pre,
		    (int)(pb - ptr), ptr,
		    (int)(pt - pb), pb,
		    (int)(ptr_end - pt), pt,
		    post);
    }
    else {
	char *p2;

	len = ptr_end - ptr;
	lim = pt < pend ? pt : pend;
	i = (int)(lim - ptr);
	buf = ALLOCA_N(char, i+2);
	code = ptr;
	caret = p2 = buf;
	if (ptr <= pb) {
	    while (ptr < pb) {
		*p2++ = *ptr++ == '\t' ? '\t' : ' ';
	    }
	    *p2++ = '^';
	    ptr++;
	}
	if (lim > ptr) {
	    memset(p2, '~', (lim - ptr));
	    p2 += (lim - ptr);
	}
	*p2 = '\0';
	rb_str_catf(mesg, "%s%.*s%s\n""%s%s\n",
		    pre, (int)len, code, post,
		    pre, caret);
    }
    if (!errbuf) rb_write_error_str(mesg);
}
#else
static int
parser_yyerror(struct parser_params *p, const YYLTYPE *yylloc, const char *msg)
{
    const char *pcur = 0, *ptok = 0;
    if (yylloc &&
	p->ruby_sourceline == yylloc->beg_pos.lineno &&
	p->ruby_sourceline == yylloc->end_pos.lineno) {
	pcur = p->lex.pcur;
	ptok = p->lex.ptok;
	p->lex.ptok = p->lex.pbeg + yylloc->beg_pos.column;
	p->lex.pcur = p->lex.pbeg + yylloc->end_pos.column;
    }
    dispatch1(parse_error, STR_NEW2(msg));
    ripper_error(p);
    if (pcur) {
	p->lex.ptok = ptok;
	p->lex.pcur = pcur;
    }
    return 0;
}

static inline void
parser_show_error_line(struct parser_params *p, const YYLTYPE *yylloc)
{
}
#endif /* !RIPPER */

#ifndef RIPPER
static int
vtable_size(const struct vtable *tbl)
{
    if (!DVARS_TERMINAL_P(tbl)) {
	return tbl->pos;
    }
    else {
	return 0;
    }
}
#endif

static struct vtable *
vtable_alloc_gen(struct parser_params *p, int line, struct vtable *prev)
{
    struct vtable *tbl = ALLOC(struct vtable);
    tbl->pos = 0;
    tbl->capa = 8;
    tbl->tbl = ALLOC_N(ID, tbl->capa);
    tbl->prev = prev;
#ifndef RIPPER
    if (p->debug) {
	rb_parser_printf(p, "vtable_alloc:%d: %p\n", line, (void *)tbl);
    }
#endif
    return tbl;
}
#define vtable_alloc(prev) vtable_alloc_gen(p, __LINE__, prev)

static void
vtable_free_gen(struct parser_params *p, int line, const char *name,
		struct vtable *tbl)
{
#ifndef RIPPER
    if (p->debug) {
	rb_parser_printf(p, "vtable_free:%d: %s(%p)\n", line, name, (void *)tbl);
    }
#endif
    if (!DVARS_TERMINAL_P(tbl)) {
	if (tbl->tbl) {
	    ruby_sized_xfree(tbl->tbl, tbl->capa * sizeof(ID));
	}
	ruby_sized_xfree(tbl, sizeof(*tbl));
    }
}
#define vtable_free(tbl) vtable_free_gen(p, __LINE__, #tbl, tbl)

static void
vtable_add_gen(struct parser_params *p, int line, const char *name,
	       struct vtable *tbl, ID id)
{
#ifndef RIPPER
    if (p->debug) {
	rb_parser_printf(p, "vtable_add:%d: %s(%p), %s\n",
			 line, name, (void *)tbl, rb_id2name(id));
    }
#endif
    if (DVARS_TERMINAL_P(tbl)) {
	rb_parser_fatal(p, "vtable_add: vtable is not allocated (%p)", (void *)tbl);
	return;
    }
    if (tbl->pos == tbl->capa) {
	tbl->capa = tbl->capa * 2;
	SIZED_REALLOC_N(tbl->tbl, ID, tbl->capa, tbl->pos);
    }
    tbl->tbl[tbl->pos++] = id;
}
#define vtable_add(tbl, id) vtable_add_gen(p, __LINE__, #tbl, tbl, id)

#ifndef RIPPER
static void
vtable_pop_gen(struct parser_params *p, int line, const char *name,
	       struct vtable *tbl, int n)
{
    if (p->debug) {
	rb_parser_printf(p, "vtable_pop:%d: %s(%p), %d\n",
			 line, name, (void *)tbl, n);
    }
    if (tbl->pos < n) {
	rb_parser_fatal(p, "vtable_pop: unreachable (%d < %d)", tbl->pos, n);
	return;
    }
    tbl->pos -= n;
}
#define vtable_pop(tbl, n) vtable_pop_gen(p, __LINE__, #tbl, tbl, n)
#endif

static int
vtable_included(const struct vtable * tbl, ID id)
{
    int i;

    if (!DVARS_TERMINAL_P(tbl)) {
	for (i = 0; i < tbl->pos; i++) {
	    if (tbl->tbl[i] == id) {
		return i+1;
	    }
	}
    }
    return 0;
}

static void parser_prepare(struct parser_params *p);

#ifndef RIPPER
static NODE *parser_append_options(struct parser_params *p, NODE *node);

static VALUE
debug_lines(VALUE fname)
{
    ID script_lines;
    CONST_ID(script_lines, "SCRIPT_LINES__");
    if (rb_const_defined_at(rb_cObject, script_lines)) {
	VALUE hash = rb_const_get_at(rb_cObject, script_lines);
	if (RB_TYPE_P(hash, T_HASH)) {
	    VALUE lines = rb_ary_new();
	    rb_hash_aset(hash, fname, lines);
	    return lines;
	}
    }
    return 0;
}

static int
e_option_supplied(struct parser_params *p)
{
    return strcmp(p->ruby_sourcefile, "-e") == 0;
}

static VALUE
yycompile0(VALUE arg)
{
    int n;
    NODE *tree;
    struct parser_params *p = (struct parser_params *)arg;
    VALUE cov = Qfalse;

    if (!compile_for_eval && !NIL_P(p->ruby_sourcefile_string)) {
	p->debug_lines = debug_lines(p->ruby_sourcefile_string);
	if (p->debug_lines && p->ruby_sourceline > 0) {
	    VALUE str = rb_default_rs;
	    n = p->ruby_sourceline;
	    do {
		rb_ary_push(p->debug_lines, str);
	    } while (--n);
	}

	if (!e_option_supplied(p)) {
	    cov = Qtrue;
	}
    }

    parser_prepare(p);
#define RUBY_DTRACE_PARSE_HOOK(name) \
    if (RUBY_DTRACE_PARSE_##name##_ENABLED()) { \
	RUBY_DTRACE_PARSE_##name(p->ruby_sourcefile, p->ruby_sourceline); \
    }
    RUBY_DTRACE_PARSE_HOOK(BEGIN);
    n = yyparse(p);
    RUBY_DTRACE_PARSE_HOOK(END);
    p->debug_lines = 0;

    p->lex.strterm = 0;
    p->lex.pcur = p->lex.pbeg = p->lex.pend = 0;
    p->lex.prevline = p->lex.lastline = p->lex.nextline = 0;
    if (n || p->error_p) {
	VALUE mesg = p->error_buffer;
	if (!mesg) {
	    mesg = rb_class_new_instance(0, 0, rb_eSyntaxError);
	}
	rb_set_errinfo(mesg);
	return FALSE;
    }
    tree = p->eval_tree;
    if (!tree) {
	tree = NEW_NIL(&NULL_LOC);
    }
    else {
	VALUE opt = p->compile_option;
	NODE *prelude;
	NODE *body = parser_append_options(p, tree->nd_body);
	if (!opt) opt = rb_obj_hide(rb_ident_hash_new());
	rb_hash_aset(opt, rb_sym_intern_ascii_cstr("coverage_enabled"), cov);
	prelude = block_append(p, p->eval_tree_begin, body);
	tree->nd_body = prelude;
        RB_OBJ_WRITE(p->ast, &p->ast->body.compile_option, opt);
    }
    p->ast->body.root = tree;
    p->ast->body.line_count = p->line_count;
    return TRUE;
}

static rb_ast_t *
yycompile(VALUE vparser, struct parser_params *p, VALUE fname, int line)
{
    rb_ast_t *ast;
    if (NIL_P(fname)) {
	p->ruby_sourcefile_string = Qnil;
	p->ruby_sourcefile = "(none)";
    }
    else {
	p->ruby_sourcefile_string = rb_fstring(fname);
	p->ruby_sourcefile = StringValueCStr(fname);
    }
    p->ruby_sourceline = line - 1;

    p->ast = ast = rb_ast_new();
    rb_suppress_tracing(yycompile0, (VALUE)p);
    p->ast = 0;
    RB_GC_GUARD(vparser); /* prohibit tail call optimization */

    return ast;
}
#endif /* !RIPPER */

static rb_encoding *
must_be_ascii_compatible(VALUE s)
{
    rb_encoding *enc = rb_enc_get(s);
    if (!rb_enc_asciicompat(enc)) {
	rb_raise(rb_eArgError, "invalid source encoding");
    }
    return enc;
}

static VALUE
lex_get_str(struct parser_params *p, VALUE s)
{
    char *beg, *end, *start;
    long len;

    beg = RSTRING_PTR(s);
    len = RSTRING_LEN(s);
    start = beg;
    if (p->lex.gets_.ptr) {
	if (len == p->lex.gets_.ptr) return Qnil;
	beg += p->lex.gets_.ptr;
	len -= p->lex.gets_.ptr;
    }
    end = memchr(beg, '\n', len);
    if (end) len = ++end - beg;
    p->lex.gets_.ptr += len;
    return rb_str_subseq(s, beg - start, len);
}

static VALUE
lex_getline(struct parser_params *p)
{
    VALUE line = (*p->lex.gets)(p, p->lex.input);
    if (NIL_P(line)) return line;
    must_be_ascii_compatible(line);
#ifndef RIPPER
    if (p->debug_lines) {
	rb_enc_associate(line, p->enc);
	rb_ary_push(p->debug_lines, line);
    }
#endif
    p->line_count++;
    return line;
}

static const rb_data_type_t parser_data_type;

#ifndef RIPPER
static rb_ast_t*
parser_compile_string(VALUE vparser, VALUE fname, VALUE s, int line)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);

    p->lex.gets = lex_get_str;
    p->lex.gets_.ptr = 0;
    p->lex.input = rb_str_new_frozen(s);
    p->lex.pbeg = p->lex.pcur = p->lex.pend = 0;

    return yycompile(vparser, p, fname, line);
}

rb_ast_t*
rb_parser_compile_string(VALUE vparser, const char *f, VALUE s, int line)
{
    return rb_parser_compile_string_path(vparser, rb_filesystem_str_new_cstr(f), s, line);
}

rb_ast_t*
rb_parser_compile_string_path(VALUE vparser, VALUE f, VALUE s, int line)
{
    must_be_ascii_compatible(s);
    return parser_compile_string(vparser, f, s, line);
}

VALUE rb_io_gets_internal(VALUE io);

static VALUE
lex_io_gets(struct parser_params *p, VALUE io)
{
    return rb_io_gets_internal(io);
}

rb_ast_t*
rb_parser_compile_file_path(VALUE vparser, VALUE fname, VALUE file, int start)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);

    p->lex.gets = lex_io_gets;
    p->lex.input = file;
    p->lex.pbeg = p->lex.pcur = p->lex.pend = 0;

    return yycompile(vparser, p, fname, start);
}

static VALUE
lex_generic_gets(struct parser_params *p, VALUE input)
{
    return (*p->lex.gets_.call)(input, p->line_count);
}

rb_ast_t*
rb_parser_compile_generic(VALUE vparser, VALUE (*lex_gets)(VALUE, int), VALUE fname, VALUE input, int start)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);

    p->lex.gets = lex_generic_gets;
    p->lex.gets_.call = lex_gets;
    p->lex.input = input;
    p->lex.pbeg = p->lex.pcur = p->lex.pend = 0;

    return yycompile(vparser, p, fname, start);
}
#endif  /* !RIPPER */

#define STR_FUNC_ESCAPE 0x01
#define STR_FUNC_EXPAND 0x02
#define STR_FUNC_REGEXP 0x04
#define STR_FUNC_QWORDS 0x08
#define STR_FUNC_SYMBOL 0x10
#define STR_FUNC_INDENT 0x20
#define STR_FUNC_LABEL  0x40
#define STR_FUNC_LIST   0x4000
#define STR_FUNC_TERM   0x8000

enum string_type {
    str_label  = STR_FUNC_LABEL,
    str_squote = (0),
    str_dquote = (STR_FUNC_EXPAND),
    str_xquote = (STR_FUNC_EXPAND),
    str_regexp = (STR_FUNC_REGEXP|STR_FUNC_ESCAPE|STR_FUNC_EXPAND),
    str_sword  = (STR_FUNC_QWORDS|STR_FUNC_LIST),
    str_dword  = (STR_FUNC_QWORDS|STR_FUNC_EXPAND|STR_FUNC_LIST),
    str_ssym   = (STR_FUNC_SYMBOL),
    str_dsym   = (STR_FUNC_SYMBOL|STR_FUNC_EXPAND)
};

static VALUE
parser_str_new(const char *ptr, long len, rb_encoding *enc, int func, rb_encoding *enc0)
{
    VALUE str;

    str = rb_enc_str_new(ptr, len, enc);
    if (!(func & STR_FUNC_REGEXP) && rb_enc_asciicompat(enc)) {
	if (rb_enc_str_coderange(str) == ENC_CODERANGE_7BIT) {
	}
	else if (enc0 == rb_usascii_encoding() && enc != rb_utf8_encoding()) {
	    rb_enc_associate(str, rb_ascii8bit_encoding());
	}
    }

    return str;
}

#define lex_goto_eol(p) ((p)->lex.pcur = (p)->lex.pend)
#define lex_eol_p(p) ((p)->lex.pcur >= (p)->lex.pend)
#define lex_eol_n_p(p,n) ((p)->lex.pcur+(n) >= (p)->lex.pend)
#define peek(p,c) peek_n(p, (c), 0)
#define peek_n(p,c,n) (!lex_eol_n_p(p, n) && (c) == (unsigned char)(p)->lex.pcur[n])
#define peekc(p) peekc_n(p, 0)
#define peekc_n(p,n) (lex_eol_n_p(p, n) ? -1 : (unsigned char)(p)->lex.pcur[n])

#ifdef RIPPER
static void
add_delayed_token(struct parser_params *p, const char *tok, const char *end)
{
    if (tok < end) {
	if (!has_delayed_token(p)) {
	    p->delayed.token = rb_str_buf_new(end - tok);
	    rb_enc_associate(p->delayed.token, p->enc);
	    p->delayed.line = p->ruby_sourceline;
	    p->delayed.col = rb_long2int(tok - p->lex.pbeg);
	}
	rb_str_buf_cat(p->delayed.token, tok, end - tok);
	p->lex.ptok = end;
    }
}
#else
#define add_delayed_token(p, tok, end) ((void)(tok), (void)(end))
#endif

static int
nextline(struct parser_params *p)
{
    VALUE v = p->lex.nextline;
    p->lex.nextline = 0;
    if (!v) {
	if (p->eofp)
	    return -1;

	if (p->lex.pend > p->lex.pbeg && *(p->lex.pend-1) != '\n') {
	    goto end_of_input;
	}

	if (!p->lex.input || NIL_P(v = lex_getline(p))) {
	  end_of_input:
	    p->eofp = 1;
	    lex_goto_eol(p);
	    return -1;
	}
	p->cr_seen = FALSE;
    }
    else if (NIL_P(v)) {
	/* after here-document without terminator */
	goto end_of_input;
    }
    add_delayed_token(p, p->lex.ptok, p->lex.pend);
    if (p->heredoc_end > 0) {
	p->ruby_sourceline = p->heredoc_end;
	p->heredoc_end = 0;
    }
    p->ruby_sourceline++;
    p->lex.pbeg = p->lex.pcur = RSTRING_PTR(v);
    p->lex.pend = p->lex.pcur + RSTRING_LEN(v);
    token_flush(p);
    p->lex.prevline = p->lex.lastline;
    p->lex.lastline = v;
    return 0;
}

static int
parser_cr(struct parser_params *p, int c)
{
    if (peek(p, '\n')) {
	p->lex.pcur++;
	c = '\n';
    }
    return c;
}

static inline int
nextc(struct parser_params *p)
{
    int c;

    if (UNLIKELY((p->lex.pcur == p->lex.pend) || p->eofp || RTEST(p->lex.nextline))) {
	if (nextline(p)) return -1;
    }
    c = (unsigned char)*p->lex.pcur++;
    if (UNLIKELY(c == '\r')) {
	c = parser_cr(p, c);
    }

    return c;
}

static void
pushback(struct parser_params *p, int c)
{
    if (c == -1) return;
    p->lex.pcur--;
    if (p->lex.pcur > p->lex.pbeg && p->lex.pcur[0] == '\n' && p->lex.pcur[-1] == '\r') {
	p->lex.pcur--;
    }
}

#define was_bol(p) ((p)->lex.pcur == (p)->lex.pbeg + 1)

#define tokfix(p) ((p)->tokenbuf[(p)->tokidx]='\0')
#define tok(p) (p)->tokenbuf
#define toklen(p) (p)->tokidx

static int
looking_at_eol_p(struct parser_params *p)
{
    const char *ptr = p->lex.pcur;
    while (ptr < p->lex.pend) {
	int c = (unsigned char)*ptr++;
	int eol = (c == '\n' || c == '#');
	if (eol || !ISSPACE(c)) {
	    return eol;
	}
    }
    return TRUE;
}

static char*
newtok(struct parser_params *p)
{
    p->tokidx = 0;
    p->tokline = p->ruby_sourceline;
    if (!p->tokenbuf) {
	p->toksiz = 60;
	p->tokenbuf = ALLOC_N(char, 60);
    }
    if (p->toksiz > 4096) {
	p->toksiz = 60;
	REALLOC_N(p->tokenbuf, char, 60);
    }
    return p->tokenbuf;
}

static char *
tokspace(struct parser_params *p, int n)
{
    p->tokidx += n;

    if (p->tokidx >= p->toksiz) {
	do {p->toksiz *= 2;} while (p->toksiz < p->tokidx);
	REALLOC_N(p->tokenbuf, char, p->toksiz);
    }
    return &p->tokenbuf[p->tokidx-n];
}

static void
tokadd(struct parser_params *p, int c)
{
    p->tokenbuf[p->tokidx++] = (char)c;
    if (p->tokidx >= p->toksiz) {
	p->toksiz *= 2;
	REALLOC_N(p->tokenbuf, char, p->toksiz);
    }
}

static int
tok_hex(struct parser_params *p, size_t *numlen)
{
    int c;

    c = scan_hex(p->lex.pcur, 2, numlen);
    if (!*numlen) {
	yyerror0("invalid hex escape");
	token_flush(p);
	return 0;
    }
    p->lex.pcur += *numlen;
    return c;
}

#define tokcopy(p, n) memcpy(tokspace(p, n), (p)->lex.pcur - (n), (n))

static int
escaped_control_code(int c)
{
    int c2 = 0;
    switch (c) {
      case ' ':
	c2 = 's';
	break;
      case '\n':
	c2 = 'n';
	break;
      case '\t':
	c2 = 't';
	break;
      case '\v':
	c2 = 'v';
	break;
      case '\r':
	c2 = 'r';
	break;
      case '\f':
	c2 = 'f';
	break;
    }
    return c2;
}

#define WARN_SPACE_CHAR(c, prefix) \
    rb_warn1("invalid character syntax; use "prefix"\\%c", WARN_I(c2))

static int
tokadd_codepoint(struct parser_params *p, rb_encoding **encp,
		 int regexp_literal, int wide)
{
    size_t numlen;
    int codepoint = scan_hex(p->lex.pcur, wide ? p->lex.pend - p->lex.pcur : 4, &numlen);
    literal_flush(p, p->lex.pcur);
    p->lex.pcur += numlen;
    if (wide ? (numlen == 0 || numlen > 6) : (numlen < 4))  {
	yyerror0("invalid Unicode escape");
	return wide && numlen > 0;
    }
    if (codepoint > 0x10ffff) {
	yyerror0("invalid Unicode codepoint (too large)");
	return wide;
    }
    if ((codepoint & 0xfffff800) == 0xd800) {
	yyerror0("invalid Unicode codepoint");
	return wide;
    }
    if (regexp_literal) {
	tokcopy(p, (int)numlen);
    }
    else if (codepoint >= 0x80) {
	rb_encoding *utf8 = rb_utf8_encoding();
	if (*encp && utf8 != *encp) {
	    YYLTYPE loc = RUBY_INIT_YYLLOC();
	    compile_error(p, "UTF-8 mixed within %s source", rb_enc_name(*encp));
	    parser_show_error_line(p, &loc);
	    return wide;
	}
	*encp = utf8;
	tokaddmbc(p, codepoint, *encp);
    }
    else {
	tokadd(p, codepoint);
    }
    return TRUE;
}

/* return value is for ?\u3042 */
static void
tokadd_utf8(struct parser_params *p, rb_encoding **encp,
	    int term, int symbol_literal, int regexp_literal)
{
    /*
     * If `term` is not -1, then we allow multiple codepoints in \u{}
     * upto `term` byte, otherwise we're parsing a character literal.
     * And then add the codepoints to the current token.
     */
    static const char multiple_codepoints[] = "Multiple codepoints at single character literal";

    const int open_brace = '{', close_brace = '}';

    if (regexp_literal) { tokadd(p, '\\'); tokadd(p, 'u'); }

    if (peek(p, open_brace)) {  /* handle \u{...} form */
	const char *second = NULL;
	int c, last = nextc(p);
	if (p->lex.pcur >= p->lex.pend) goto unterminated;
	while (ISSPACE(c = *p->lex.pcur) && ++p->lex.pcur < p->lex.pend);
	while (c != close_brace) {
	    if (c == term) goto unterminated;
	    if (second == multiple_codepoints)
		second = p->lex.pcur;
	    if (regexp_literal) tokadd(p, last);
	    if (!tokadd_codepoint(p, encp, regexp_literal, TRUE)) {
		break;
	    }
	    while (ISSPACE(c = *p->lex.pcur)) {
		if (++p->lex.pcur >= p->lex.pend) goto unterminated;
		last = c;
	    }
	    if (term == -1 && !second)
		second = multiple_codepoints;
	}

	if (c != close_brace) {
	  unterminated:
	    token_flush(p);
	    yyerror0("unterminated Unicode escape");
	    return;
	}
	if (second && second != multiple_codepoints) {
	    const char *pcur = p->lex.pcur;
	    p->lex.pcur = second;
	    dispatch_scan_event(p, tSTRING_CONTENT);
	    token_flush(p);
	    p->lex.pcur = pcur;
	    yyerror0(multiple_codepoints);
	    token_flush(p);
	}

	if (regexp_literal) tokadd(p, close_brace);
	nextc(p);
    }
    else {			/* handle \uxxxx form */
	if (!tokadd_codepoint(p, encp, regexp_literal, FALSE)) {
	    token_flush(p);
	    return;
	}
    }
}

#define ESCAPE_CONTROL 1
#define ESCAPE_META    2

static int
read_escape(struct parser_params *p, int flags, rb_encoding **encp)
{
    int c;
    size_t numlen;

    switch (c = nextc(p)) {
      case '\\':	/* Backslash */
	return c;

      case 'n':	/* newline */
	return '\n';

      case 't':	/* horizontal tab */
	return '\t';

      case 'r':	/* carriage-return */
	return '\r';

      case 'f':	/* form-feed */
	return '\f';

      case 'v':	/* vertical tab */
	return '\13';

      case 'a':	/* alarm(bell) */
	return '\007';

      case 'e':	/* escape */
	return 033;

      case '0': case '1': case '2': case '3': /* octal constant */
      case '4': case '5': case '6': case '7':
	pushback(p, c);
	c = scan_oct(p->lex.pcur, 3, &numlen);
	p->lex.pcur += numlen;
	return c;

      case 'x':	/* hex constant */
	c = tok_hex(p, &numlen);
	if (numlen == 0) return 0;
	return c;

      case 'b':	/* backspace */
	return '\010';

      case 's':	/* space */
	return ' ';

      case 'M':
	if (flags & ESCAPE_META) goto eof;
	if ((c = nextc(p)) != '-') {
	    goto eof;
	}
	if ((c = nextc(p)) == '\\') {
	    switch (peekc(p)) {
	      case 'u': case 'U':
		nextc(p);
		goto eof;
	    }
	    return read_escape(p, flags|ESCAPE_META, encp) | 0x80;
	}
	else if (c == -1 || !ISASCII(c)) goto eof;
	else {
	    int c2 = escaped_control_code(c);
	    if (c2) {
		if (ISCNTRL(c) || !(flags & ESCAPE_CONTROL)) {
		    WARN_SPACE_CHAR(c2, "\\M-");
		}
		else {
		    WARN_SPACE_CHAR(c2, "\\C-\\M-");
		}
	    }
	    else if (ISCNTRL(c)) goto eof;
	    return ((c & 0xff) | 0x80);
	}

      case 'C':
	if ((c = nextc(p)) != '-') {
	    goto eof;
	}
      case 'c':
	if (flags & ESCAPE_CONTROL) goto eof;
	if ((c = nextc(p))== '\\') {
	    switch (peekc(p)) {
	      case 'u': case 'U':
		nextc(p);
		goto eof;
	    }
	    c = read_escape(p, flags|ESCAPE_CONTROL, encp);
	}
	else if (c == '?')
	    return 0177;
	else if (c == -1 || !ISASCII(c)) goto eof;
	else {
	    int c2 = escaped_control_code(c);
	    if (c2) {
		if (ISCNTRL(c)) {
		    if (flags & ESCAPE_META) {
			WARN_SPACE_CHAR(c2, "\\M-");
		    }
		    else {
			WARN_SPACE_CHAR(c2, "");
		    }
		}
		else {
		    if (flags & ESCAPE_META) {
			WARN_SPACE_CHAR(c2, "\\M-\\C-");
		    }
		    else {
			WARN_SPACE_CHAR(c2, "\\C-");
		    }
		}
	    }
	    else if (ISCNTRL(c)) goto eof;
	}
	return c & 0x9f;

      eof:
      case -1:
        yyerror0("Invalid escape character syntax");
	token_flush(p);
	return '\0';

      default:
	return c;
    }
}

static void
tokaddmbc(struct parser_params *p, int c, rb_encoding *enc)
{
    int len = rb_enc_codelen(c, enc);
    rb_enc_mbcput(c, tokspace(p, len), enc);
}

static int
tokadd_escape(struct parser_params *p, rb_encoding **encp)
{
    int c;
    int flags = 0;
    size_t numlen;

  first:
    switch (c = nextc(p)) {
      case '\n':
	return 0;		/* just ignore */

      case '0': case '1': case '2': case '3': /* octal constant */
      case '4': case '5': case '6': case '7':
	{
	    ruby_scan_oct(--p->lex.pcur, 3, &numlen);
	    if (numlen == 0) goto eof;
	    p->lex.pcur += numlen;
	    tokcopy(p, (int)numlen + 1);
	}
	return 0;

      case 'x':	/* hex constant */
	{
	    tok_hex(p, &numlen);
	    if (numlen == 0) return -1;
	    tokcopy(p, (int)numlen + 2);
	}
	return 0;

      case 'M':
	if (flags & ESCAPE_META) goto eof;
	if ((c = nextc(p)) != '-') {
	    pushback(p, c);
	    goto eof;
	}
	tokcopy(p, 3);
	flags |= ESCAPE_META;
	goto escaped;

      case 'C':
	if (flags & ESCAPE_CONTROL) goto eof;
	if ((c = nextc(p)) != '-') {
	    pushback(p, c);
	    goto eof;
	}
	tokcopy(p, 3);
	goto escaped;

      case 'c':
	if (flags & ESCAPE_CONTROL) goto eof;
	tokcopy(p, 2);
	flags |= ESCAPE_CONTROL;
      escaped:
	if ((c = nextc(p)) == '\\') {
	    goto first;
	}
	else if (c == -1) goto eof;
	tokadd(p, c);
	return 0;

      eof:
      case -1:
        yyerror0("Invalid escape character syntax");
	token_flush(p);
	return -1;

      default:
	tokadd(p, '\\');
	tokadd(p, c);
    }
    return 0;
}

static int
regx_options(struct parser_params *p)
{
    int kcode = 0;
    int kopt = 0;
    int options = 0;
    int c, opt, kc;

    newtok(p);
    while (c = nextc(p), ISALPHA(c)) {
        if (c == 'o') {
            options |= RE_OPTION_ONCE;
        }
        else if (rb_char_to_option_kcode(c, &opt, &kc)) {
	    if (kc >= 0) {
		if (kc != rb_ascii8bit_encindex()) kcode = c;
		kopt = opt;
	    }
	    else {
		options |= opt;
	    }
        }
        else {
	    tokadd(p, c);
        }
    }
    options |= kopt;
    pushback(p, c);
    if (toklen(p)) {
	YYLTYPE loc = RUBY_INIT_YYLLOC();
	tokfix(p);
	compile_error(p, "unknown regexp option%s - %*s",
		      toklen(p) > 1 ? "s" : "", toklen(p), tok(p));
	parser_show_error_line(p, &loc);
    }
    return options | RE_OPTION_ENCODING(kcode);
}

static int
tokadd_mbchar(struct parser_params *p, int c)
{
    int len = parser_precise_mbclen(p, p->lex.pcur-1);
    if (len < 0) return -1;
    tokadd(p, c);
    p->lex.pcur += --len;
    if (len > 0) tokcopy(p, len);
    return c;
}

static inline int
simple_re_meta(int c)
{
    switch (c) {
      case '$': case '*': case '+': case '.':
      case '?': case '^': case '|':
      case ')': case ']': case '}': case '>':
	return TRUE;
      default:
	return FALSE;
    }
}

static int
parser_update_heredoc_indent(struct parser_params *p, int c)
{
    if (p->heredoc_line_indent == -1) {
	if (c == '\n') p->heredoc_line_indent = 0;
    }
    else {
	if (c == ' ') {
	    p->heredoc_line_indent++;
	    return TRUE;
	}
	else if (c == '\t') {
	    int w = (p->heredoc_line_indent / TAB_WIDTH) + 1;
	    p->heredoc_line_indent = w * TAB_WIDTH;
	    return TRUE;
	}
	else if (c != '\n') {
	    if (p->heredoc_indent > p->heredoc_line_indent) {
		p->heredoc_indent = p->heredoc_line_indent;
	    }
	    p->heredoc_line_indent = -1;
	}
    }
    return FALSE;
}

static void
parser_mixed_error(struct parser_params *p, rb_encoding *enc1, rb_encoding *enc2)
{
    YYLTYPE loc = RUBY_INIT_YYLLOC();
    const char *n1 = rb_enc_name(enc1), *n2 = rb_enc_name(enc2);
    compile_error(p, "%s mixed within %s source", n1, n2);
    parser_show_error_line(p, &loc);
}

static void
parser_mixed_escape(struct parser_params *p, const char *beg, rb_encoding *enc1, rb_encoding *enc2)
{
    const char *pos = p->lex.pcur;
    p->lex.pcur = beg;
    parser_mixed_error(p, enc1, enc2);
    p->lex.pcur = pos;
}

static int
tokadd_string(struct parser_params *p,
	      int func, int term, int paren, long *nest,
	      rb_encoding **encp, rb_encoding **enc)
{
    int c;
    bool erred = false;

#define mixed_error(enc1, enc2) \
    (void)(erred || (parser_mixed_error(p, enc1, enc2), erred = true))
#define mixed_escape(beg, enc1, enc2) \
    (void)(erred || (parser_mixed_escape(p, beg, enc1, enc2), erred = true))

    while ((c = nextc(p)) != -1) {
	if (p->heredoc_indent > 0) {
	    parser_update_heredoc_indent(p, c);
	}

	if (paren && c == paren) {
	    ++*nest;
	}
	else if (c == term) {
	    if (!nest || !*nest) {
		pushback(p, c);
		break;
	    }
	    --*nest;
	}
	else if ((func & STR_FUNC_EXPAND) && c == '#' && p->lex.pcur < p->lex.pend) {
	    int c2 = *p->lex.pcur;
	    if (c2 == '$' || c2 == '@' || c2 == '{') {
		pushback(p, c);
		break;
	    }
	}
	else if (c == '\\') {
	    literal_flush(p, p->lex.pcur - 1);
	    c = nextc(p);
	    switch (c) {
	      case '\n':
		if (func & STR_FUNC_QWORDS) break;
		if (func & STR_FUNC_EXPAND) {
		    if (!(func & STR_FUNC_INDENT) || (p->heredoc_indent < 0))
			continue;
		    if (c == term) {
			c = '\\';
			goto terminate;
		    }
		}
		tokadd(p, '\\');
		break;

	      case '\\':
		if (func & STR_FUNC_ESCAPE) tokadd(p, c);
		break;

	      case 'u':
		if ((func & STR_FUNC_EXPAND) == 0) {
		    tokadd(p, '\\');
		    break;
		}
		tokadd_utf8(p, enc, term,
			    func & STR_FUNC_SYMBOL,
			    func & STR_FUNC_REGEXP);
		continue;

	      default:
		if (c == -1) return -1;
		if (!ISASCII(c)) {
		    if ((func & STR_FUNC_EXPAND) == 0) tokadd(p, '\\');
		    goto non_ascii;
		}
		if (func & STR_FUNC_REGEXP) {
		    if (c == term && !simple_re_meta(c)) {
			tokadd(p, c);
			continue;
		    }
		    pushback(p, c);
		    if ((c = tokadd_escape(p, enc)) < 0)
			return -1;
		    if (*enc && *enc != *encp) {
			mixed_escape(p->lex.ptok+2, *enc, *encp);
		    }
		    continue;
		}
		else if (func & STR_FUNC_EXPAND) {
		    pushback(p, c);
		    if (func & STR_FUNC_ESCAPE) tokadd(p, '\\');
		    c = read_escape(p, 0, enc);
		}
		else if ((func & STR_FUNC_QWORDS) && ISSPACE(c)) {
		    /* ignore backslashed spaces in %w */
		}
		else if (c != term && !(paren && c == paren)) {
		    tokadd(p, '\\');
		    pushback(p, c);
		    continue;
		}
	    }
	}
	else if (!parser_isascii(p)) {
	  non_ascii:
	    if (!*enc) {
		*enc = *encp;
	    }
	    else if (*enc != *encp) {
		mixed_error(*enc, *encp);
		continue;
	    }
	    if (tokadd_mbchar(p, c) == -1) return -1;
	    continue;
	}
	else if ((func & STR_FUNC_QWORDS) && ISSPACE(c)) {
	    pushback(p, c);
	    break;
	}
        if (c & 0x80) {
	    if (!*enc) {
		*enc = *encp;
	    }
	    else if (*enc != *encp) {
		mixed_error(*enc, *encp);
		continue;
	    }
        }
	tokadd(p, c);
    }
  terminate:
    if (*enc) *encp = *enc;
    return c;
}

static inline rb_strterm_t *
new_strterm(VALUE v1, VALUE v2, VALUE v3, VALUE v0)
{
    return (rb_strterm_t*)rb_imemo_new(imemo_parser_strterm, v1, v2, v3, v0);
}

/* imemo_parser_strterm for literal */
#define NEW_STRTERM(func, term, paren) \
    new_strterm((VALUE)(func), (VALUE)(paren), (VALUE)(term), 0)

#ifdef RIPPER
static void
flush_string_content(struct parser_params *p, rb_encoding *enc)
{
    VALUE content = yylval.val;
    if (!ripper_is_node_yylval(content))
	content = ripper_new_yylval(p, 0, 0, content);
    if (has_delayed_token(p)) {
	ptrdiff_t len = p->lex.pcur - p->lex.ptok;
	if (len > 0) {
	    rb_enc_str_buf_cat(p->delayed.token, p->lex.ptok, len, enc);
	}
	dispatch_delayed_token(p, tSTRING_CONTENT);
	p->lex.ptok = p->lex.pcur;
	RNODE(content)->nd_rval = yylval.val;
    }
    dispatch_scan_event(p, tSTRING_CONTENT);
    if (yylval.val != content)
	RNODE(content)->nd_rval = yylval.val;
    yylval.val = content;
}
#else
#define flush_string_content(p, enc) ((void)(enc))
#endif

RUBY_FUNC_EXPORTED const unsigned int ruby_global_name_punct_bits[(0x7e - 0x20 + 31) / 32];
/* this can be shared with ripper, since it's independent from struct
 * parser_params. */
#ifndef RIPPER
#define BIT(c, idx) (((c) / 32 - 1 == idx) ? (1U << ((c) % 32)) : 0)
#define SPECIAL_PUNCT(idx) ( \
	BIT('~', idx) | BIT('*', idx) | BIT('$', idx) | BIT('?', idx) | \
	BIT('!', idx) | BIT('@', idx) | BIT('/', idx) | BIT('\\', idx) | \
	BIT(';', idx) | BIT(',', idx) | BIT('.', idx) | BIT('=', idx) | \
	BIT(':', idx) | BIT('<', idx) | BIT('>', idx) | BIT('\"', idx) | \
	BIT('&', idx) | BIT('`', idx) | BIT('\'', idx) | BIT('+', idx) | \
	BIT('0', idx))
const unsigned int ruby_global_name_punct_bits[] = {
    SPECIAL_PUNCT(0),
    SPECIAL_PUNCT(1),
    SPECIAL_PUNCT(2),
};
#undef BIT
#undef SPECIAL_PUNCT
#endif

static enum yytokentype
parser_peek_variable_name(struct parser_params *p)
{
    int c;
    const char *ptr = p->lex.pcur;

    if (ptr + 1 >= p->lex.pend) return 0;
    c = *ptr++;
    switch (c) {
      case '$':
	if ((c = *ptr) == '-') {
	    if (++ptr >= p->lex.pend) return 0;
	    c = *ptr;
	}
	else if (is_global_name_punct(c) || ISDIGIT(c)) {
	    return tSTRING_DVAR;
	}
	break;
      case '@':
	if ((c = *ptr) == '@') {
	    if (++ptr >= p->lex.pend) return 0;
	    c = *ptr;
	}
	break;
      case '{':
	p->lex.pcur = ptr;
	p->command_start = TRUE;
	return tSTRING_DBEG;
      default:
	return 0;
    }
    if (!ISASCII(c) || c == '_' || ISALPHA(c))
	return tSTRING_DVAR;
    return 0;
}

#define IS_ARG() IS_lex_state(EXPR_ARG_ANY)
#define IS_END() IS_lex_state(EXPR_END_ANY)
#define IS_BEG() (IS_lex_state(EXPR_BEG_ANY) || IS_lex_state_all(EXPR_ARG|EXPR_LABELED))
#define IS_SPCARG(c) (IS_ARG() && space_seen && !ISSPACE(c))
#define IS_LABEL_POSSIBLE() (\
	(IS_lex_state(EXPR_LABEL|EXPR_ENDFN) && !cmd_state) || \
	IS_ARG())
#define IS_LABEL_SUFFIX(n) (peek_n(p, ':',(n)) && !peek_n(p, ':', (n)+1))
#define IS_AFTER_OPERATOR() IS_lex_state(EXPR_FNAME | EXPR_DOT)

static inline enum yytokentype
parser_string_term(struct parser_params *p, int func)
{
    p->lex.strterm = 0;
    if (func & STR_FUNC_REGEXP) {
	set_yylval_num(regx_options(p));
	dispatch_scan_event(p, tREGEXP_END);
	SET_LEX_STATE(EXPR_END);
	return tREGEXP_END;
    }
    if ((func & STR_FUNC_LABEL) && IS_LABEL_SUFFIX(0)) {
	nextc(p);
	SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
	return tLABEL_END;
    }
    SET_LEX_STATE(EXPR_END);
    return tSTRING_END;
}

static enum yytokentype
parse_string(struct parser_params *p, rb_strterm_literal_t *quote)
{
    int func = (int)quote->u1.func;
    int term = (int)quote->u3.term;
    int paren = (int)quote->u2.paren;
    int c, space = 0;
    rb_encoding *enc = p->enc;
    rb_encoding *base_enc = 0;
    VALUE lit;

    if (func & STR_FUNC_TERM) {
	if (func & STR_FUNC_QWORDS) nextc(p); /* delayed term */
	SET_LEX_STATE(EXPR_END);
	p->lex.strterm = 0;
	return func & STR_FUNC_REGEXP ? tREGEXP_END : tSTRING_END;
    }
    c = nextc(p);
    if ((func & STR_FUNC_QWORDS) && ISSPACE(c)) {
	do {c = nextc(p);} while (ISSPACE(c));
	space = 1;
    }
    if (func & STR_FUNC_LIST) {
	quote->u1.func &= ~STR_FUNC_LIST;
	space = 1;
    }
    if (c == term && !quote->u0.nest) {
	if (func & STR_FUNC_QWORDS) {
	    quote->u1.func |= STR_FUNC_TERM;
	    pushback(p, c); /* dispatch the term at tSTRING_END */
	    add_delayed_token(p, p->lex.ptok, p->lex.pcur);
	    return ' ';
	}
	return parser_string_term(p, func);
    }
    if (space) {
	pushback(p, c);
	add_delayed_token(p, p->lex.ptok, p->lex.pcur);
	return ' ';
    }
    newtok(p);
    if ((func & STR_FUNC_EXPAND) && c == '#') {
	int t = parser_peek_variable_name(p);
	if (t) return t;
	tokadd(p, '#');
	c = nextc(p);
    }
    pushback(p, c);
    if (tokadd_string(p, func, term, paren, &quote->u0.nest,
		      &enc, &base_enc) == -1) {
	if (p->eofp) {
#ifndef RIPPER
# define unterminated_literal(mesg) yyerror0(mesg)
#else
# define unterminated_literal(mesg) compile_error(p,  mesg)
#endif
	    literal_flush(p, p->lex.pcur);
	    if (func & STR_FUNC_QWORDS) {
		/* no content to add, bailing out here */
		unterminated_literal("unterminated list meets end of file");
		p->lex.strterm = 0;
		return tSTRING_END;
	    }
	    if (func & STR_FUNC_REGEXP) {
		unterminated_literal("unterminated regexp meets end of file");
	    }
	    else {
		unterminated_literal("unterminated string meets end of file");
	    }
	    quote->u1.func |= STR_FUNC_TERM;
	}
    }

    tokfix(p);
    lit = STR_NEW3(tok(p), toklen(p), enc, func);
    set_yylval_str(lit);
    flush_string_content(p, enc);

    return tSTRING_CONTENT;
}

static enum yytokentype
heredoc_identifier(struct parser_params *p)
{
    /*
     * term_len is length of `<<"END"` except `END`,
     * in this case term_len is 4 (<, <, " and ").
     */
    long len, offset = p->lex.pcur - p->lex.pbeg;
    int c = nextc(p), term, func = 0, quote = 0;
    enum yytokentype token = tSTRING_BEG;
    int indent = 0;

    if (c == '-') {
	c = nextc(p);
	func = STR_FUNC_INDENT;
	offset++;
    }
    else if (c == '~') {
	c = nextc(p);
	func = STR_FUNC_INDENT;
	offset++;
	indent = INT_MAX;
    }
    switch (c) {
      case '\'':
	func |= str_squote; goto quoted;
      case '"':
	func |= str_dquote; goto quoted;
      case '`':
	token = tXSTRING_BEG;
	func |= str_xquote; goto quoted;

      quoted:
	quote++;
	offset++;
	term = c;
	len = 0;
	while ((c = nextc(p)) != term) {
	    if (c == -1 || c == '\r' || c == '\n') {
		yyerror(NULL, p, "unterminated here document identifier");
		return -1;
	    }
	}
	break;

      default:
	if (!parser_is_identchar(p)) {
	    pushback(p, c);
	    if (func & STR_FUNC_INDENT) {
		pushback(p, indent > 0 ? '~' : '-');
	    }
	    return 0;
	}
	func |= str_dquote;
	do {
	    int n = parser_precise_mbclen(p, p->lex.pcur-1);
	    if (n < 0) return 0;
	    p->lex.pcur += --n;
	} while ((c = nextc(p)) != -1 && parser_is_identchar(p));
	pushback(p, c);
	break;
    }

    len = p->lex.pcur - (p->lex.pbeg + offset) - quote;
    if ((unsigned long)len >= HERETERM_LENGTH_MAX)
	yyerror(NULL, p, "too long here document identifier");
    dispatch_scan_event(p, tHEREDOC_BEG);
    lex_goto_eol(p);

    p->lex.strterm = new_strterm(0, 0, 0, p->lex.lastline);
    p->lex.strterm->flags |= STRTERM_HEREDOC;
    rb_strterm_heredoc_t *here = &p->lex.strterm->u.heredoc;
    here->offset = offset;
    here->sourceline = p->ruby_sourceline;
    here->length = (int)len;
    here->quote = quote;
    here->func = func;

    token_flush(p);
    p->heredoc_indent = indent;
    p->heredoc_line_indent = 0;
    return token;
}

static void
heredoc_restore(struct parser_params *p, rb_strterm_heredoc_t *here)
{
    VALUE line;

    p->lex.strterm = 0;
    line = here->lastline;
    p->lex.lastline = line;
    p->lex.pbeg = RSTRING_PTR(line);
    p->lex.pend = p->lex.pbeg + RSTRING_LEN(line);
    p->lex.pcur = p->lex.pbeg + here->offset + here->length + here->quote;
    p->lex.ptok = p->lex.pbeg + here->offset - here->quote;
    p->heredoc_end = p->ruby_sourceline;
    p->ruby_sourceline = (int)here->sourceline;
    if (p->eofp) p->lex.nextline = Qnil;
    p->eofp = 0;
}

static int
dedent_string(VALUE string, int width)
{
    char *str;
    long len;
    int i, col = 0;

    RSTRING_GETMEM(string, str, len);
    for (i = 0; i < len && col < width; i++) {
	if (str[i] == ' ') {
	    col++;
	}
	else if (str[i] == '\t') {
	    int n = TAB_WIDTH * (col / TAB_WIDTH + 1);
	    if (n > width) break;
	    col = n;
	}
	else {
	    break;
	}
    }
    if (!i) return 0;
    rb_str_modify(string);
    str = RSTRING_PTR(string);
    if (RSTRING_LEN(string) != len)
	rb_fatal("literal string changed: %+"PRIsVALUE, string);
    MEMMOVE(str, str + i, char, len - i);
    rb_str_set_len(string, len - i);
    return i;
}

#ifndef RIPPER
static NODE *
heredoc_dedent(struct parser_params *p, NODE *root)
{
    NODE *node, *str_node, *prev_node;
    int indent = p->heredoc_indent;
    VALUE prev_lit = 0;

    if (indent <= 0) return root;
    p->heredoc_indent = 0;
    if (!root) return root;

    prev_node = node = str_node = root;
    if (nd_type(root) == NODE_LIST) str_node = root->nd_head;

    while (str_node) {
	VALUE lit = str_node->nd_lit;
	if (str_node->flags & NODE_FL_NEWLINE) {
	    dedent_string(lit, indent);
	}
	if (!prev_lit) {
	    prev_lit = lit;
	}
	else if (!literal_concat0(p, prev_lit, lit)) {
	    return 0;
	}
	else {
	    NODE *end = node->nd_end;
	    node = prev_node->nd_next = node->nd_next;
	    if (!node) {
		if (nd_type(prev_node) == NODE_DSTR)
		    nd_set_type(prev_node, NODE_STR);
		break;
	    }
	    node->nd_end = end;
	    goto next_str;
	}

	str_node = 0;
	while ((node = (prev_node = node)->nd_next) != 0) {
	  next_str:
	    if (nd_type(node) != NODE_LIST) break;
	    if ((str_node = node->nd_head) != 0) {
		enum node_type type = nd_type(str_node);
		if (type == NODE_STR || type == NODE_DSTR) break;
		prev_lit = 0;
		str_node = 0;
	    }
	}
    }
    return root;
}
#else /* RIPPER */
static VALUE
heredoc_dedent(struct parser_params *p, VALUE array)
{
    int indent = p->heredoc_indent;

    if (indent <= 0) return array;
    p->heredoc_indent = 0;
    dispatch2(heredoc_dedent, array, INT2NUM(indent));
    return array;
}

/*
 *  call-seq:
 *    Ripper.dedent_string(input, width)   -> Integer
 *
 *  USE OF RIPPER LIBRARY ONLY.
 *
 *  Strips up to +width+ leading whitespaces from +input+,
 *  and returns the stripped column width.
 */
static VALUE
parser_dedent_string(VALUE self, VALUE input, VALUE width)
{
    int wid, col;

    StringValue(input);
    wid = NUM2UINT(width);
    col = dedent_string(input, wid);
    return INT2NUM(col);
}
#endif

static int
whole_match_p(struct parser_params *p, const char *eos, long len, int indent)
{
    const char *ptr = p->lex.pbeg;
    long n;

    if (indent) {
	while (*ptr && ISSPACE(*ptr)) ptr++;
    }
    n = p->lex.pend - (ptr + len);
    if (n < 0) return FALSE;
    if (n > 0 && ptr[len] != '\n') {
	if (ptr[len] != '\r') return FALSE;
	if (n <= 1 || ptr[len+1] != '\n') return FALSE;
    }
    return strncmp(eos, ptr, len) == 0;
}

static int
word_match_p(struct parser_params *p, const char *word, long len)
{
    if (strncmp(p->lex.pcur, word, len)) return 0;
    if (p->lex.pcur + len == p->lex.pend) return 1;
    int c = (unsigned char)p->lex.pcur[len];
    if (ISSPACE(c)) return 1;
    switch (c) {
      case '\0': case '\004': case '\032': return 1;
    }
    return 0;
}

#define NUM_SUFFIX_R   (1<<0)
#define NUM_SUFFIX_I   (1<<1)
#define NUM_SUFFIX_ALL 3

static int
number_literal_suffix(struct parser_params *p, int mask)
{
    int c, result = 0;
    const char *lastp = p->lex.pcur;

    while ((c = nextc(p)) != -1) {
	if ((mask & NUM_SUFFIX_I) && c == 'i') {
	    result |= (mask & NUM_SUFFIX_I);
	    mask &= ~NUM_SUFFIX_I;
	    /* r after i, rational of complex is disallowed */
	    mask &= ~NUM_SUFFIX_R;
	    continue;
	}
	if ((mask & NUM_SUFFIX_R) && c == 'r') {
	    result |= (mask & NUM_SUFFIX_R);
	    mask &= ~NUM_SUFFIX_R;
	    continue;
	}
	if (!ISASCII(c) || ISALPHA(c) || c == '_') {
	    p->lex.pcur = lastp;
	    literal_flush(p, p->lex.pcur);
	    return 0;
	}
	pushback(p, c);
	break;
    }
    return result;
}

static enum yytokentype
set_number_literal(struct parser_params *p, VALUE v,
		   enum yytokentype type, int suffix)
{
    if (suffix & NUM_SUFFIX_I) {
	v = rb_complex_raw(INT2FIX(0), v);
	type = tIMAGINARY;
    }
    set_yylval_literal(v);
    SET_LEX_STATE(EXPR_END);
    return type;
}

static enum yytokentype
set_integer_literal(struct parser_params *p, VALUE v, int suffix)
{
    enum yytokentype type = tINTEGER;
    if (suffix & NUM_SUFFIX_R) {
	v = rb_rational_raw1(v);
	type = tRATIONAL;
    }
    return set_number_literal(p, v, type, suffix);
}

#ifdef RIPPER
static void
dispatch_heredoc_end(struct parser_params *p)
{
    VALUE str;
    if (has_delayed_token(p))
	dispatch_delayed_token(p, tSTRING_CONTENT);
    str = STR_NEW(p->lex.ptok, p->lex.pend - p->lex.ptok);
    ripper_dispatch1(p, ripper_token2eventid(tHEREDOC_END), str);
    lex_goto_eol(p);
    token_flush(p);
}

#else
#define dispatch_heredoc_end(p) ((void)0)
#endif

static enum yytokentype
here_document(struct parser_params *p, rb_strterm_heredoc_t *here)
{
    int c, func, indent = 0;
    const char *eos, *ptr, *ptr_end;
    long len;
    VALUE str = 0;
    rb_encoding *enc = p->enc;
    rb_encoding *base_enc = 0;
    int bol;

    eos = RSTRING_PTR(here->lastline) + here->offset;
    len = here->length;
    indent = (func = here->func) & STR_FUNC_INDENT;

    if ((c = nextc(p)) == -1) {
      error:
#ifdef RIPPER
	if (!has_delayed_token(p)) {
	    dispatch_scan_event(p, tSTRING_CONTENT);
	}
	else {
	    if ((len = p->lex.pcur - p->lex.ptok) > 0) {
		if (!(func & STR_FUNC_REGEXP) && rb_enc_asciicompat(enc)) {
		    int cr = ENC_CODERANGE_UNKNOWN;
		    rb_str_coderange_scan_restartable(p->lex.ptok, p->lex.pcur, enc, &cr);
		    if (cr != ENC_CODERANGE_7BIT &&
			p->enc == rb_usascii_encoding() &&
			enc != rb_utf8_encoding()) {
			enc = rb_ascii8bit_encoding();
		    }
		}
		rb_enc_str_buf_cat(p->delayed.token, p->lex.ptok, len, enc);
	    }
	    dispatch_delayed_token(p, tSTRING_CONTENT);
	}
	lex_goto_eol(p);
#endif
	heredoc_restore(p, &p->lex.strterm->u.heredoc);
	compile_error(p, "can't find string \"%.*s\" anywhere before EOF",
		      (int)len, eos);
	token_flush(p);
	p->lex.strterm = 0;
	SET_LEX_STATE(EXPR_END);
	return tSTRING_END;
    }
    bol = was_bol(p);
    if (!bol) {
	/* not beginning of line, cannot be the terminator */
    }
    else if (p->heredoc_line_indent == -1) {
	/* `heredoc_line_indent == -1` means
	 * - "after an interpolation in the same line", or
	 * - "in a continuing line"
	 */
	p->heredoc_line_indent = 0;
    }
    else if (whole_match_p(p, eos, len, indent)) {
	dispatch_heredoc_end(p);
      restore:
	heredoc_restore(p, &p->lex.strterm->u.heredoc);
	token_flush(p);
	p->lex.strterm = 0;
	SET_LEX_STATE(EXPR_END);
	return tSTRING_END;
    }

    if (!(func & STR_FUNC_EXPAND)) {
	do {
	    ptr = RSTRING_PTR(p->lex.lastline);
	    ptr_end = p->lex.pend;
	    if (ptr_end > ptr) {
		switch (ptr_end[-1]) {
		  case '\n':
		    if (--ptr_end == ptr || ptr_end[-1] != '\r') {
			ptr_end++;
			break;
		    }
		  case '\r':
		    --ptr_end;
		}
	    }

	    if (p->heredoc_indent > 0) {
		long i = 0;
		while (ptr + i < ptr_end && parser_update_heredoc_indent(p, ptr[i]))
		    i++;
		p->heredoc_line_indent = 0;
	    }

	    if (str)
		rb_str_cat(str, ptr, ptr_end - ptr);
	    else
		str = STR_NEW(ptr, ptr_end - ptr);
	    if (ptr_end < p->lex.pend) rb_str_cat(str, "\n", 1);
	    lex_goto_eol(p);
	    if (p->heredoc_indent > 0) {
		goto flush_str;
	    }
	    if (nextc(p) == -1) {
		if (str) {
		    str = 0;
		}
		goto error;
	    }
	} while (!whole_match_p(p, eos, len, indent));
    }
    else {
	/*	int mb = ENC_CODERANGE_7BIT, *mbp = &mb;*/
	newtok(p);
	if (c == '#') {
	    int t = parser_peek_variable_name(p);
	    if (p->heredoc_line_indent != -1) {
		if (p->heredoc_indent > p->heredoc_line_indent) {
		    p->heredoc_indent = p->heredoc_line_indent;
		}
		p->heredoc_line_indent = -1;
	    }
	    if (t) return t;
	    tokadd(p, '#');
	    c = nextc(p);
	}
	do {
	    pushback(p, c);
	    enc = p->enc;
	    if ((c = tokadd_string(p, func, '\n', 0, NULL, &enc, &base_enc)) == -1) {
		if (p->eofp) goto error;
		goto restore;
	    }
	    if (c != '\n') {
		if (c == '\\') p->heredoc_line_indent = -1;
	      flush:
		str = STR_NEW3(tok(p), toklen(p), enc, func);
	      flush_str:
		set_yylval_str(str);
#ifndef RIPPER
		if (bol) yylval.node->flags |= NODE_FL_NEWLINE;
#endif
		flush_string_content(p, enc);
		return tSTRING_CONTENT;
	    }
	    tokadd(p, nextc(p));
	    if (p->heredoc_indent > 0) {
		lex_goto_eol(p);
		goto flush;
	    }
	    /*	    if (mbp && mb == ENC_CODERANGE_UNKNOWN) mbp = 0;*/
	    if ((c = nextc(p)) == -1) goto error;
	} while (!whole_match_p(p, eos, len, indent));
	str = STR_NEW3(tok(p), toklen(p), enc, func);
    }
    dispatch_heredoc_end(p);
#ifdef RIPPER
    str = ripper_new_yylval(p, ripper_token2eventid(tSTRING_CONTENT),
			    yylval.val, str);
#endif
    heredoc_restore(p, &p->lex.strterm->u.heredoc);
    token_flush(p);
    p->lex.strterm = NEW_STRTERM(func | STR_FUNC_TERM, 0, 0);
    set_yylval_str(str);
#ifndef RIPPER
    if (bol) yylval.node->flags |= NODE_FL_NEWLINE;
#endif
    return tSTRING_CONTENT;
}

#include "lex.c"

static int
arg_ambiguous(struct parser_params *p, char c)
{
#ifndef RIPPER
    if (c == '/') {
        rb_warning1("ambiguity between regexp and two divisions: wrap regexp in parentheses or add a space after `%c' operator", WARN_I(c));
    }
    else {
        rb_warning1("ambiguous first argument; put parentheses or a space even after `%c' operator", WARN_I(c));
    }
#else
    dispatch1(arg_ambiguous, rb_usascii_str_new(&c, 1));
#endif
    return TRUE;
}

static ID
#ifndef RIPPER
formal_argument(struct parser_params *p, ID lhs)
#else
formal_argument(struct parser_params *p, VALUE lhs)
#endif
{
    switch (id_type(get_id(lhs))) {
      case ID_LOCAL:
	break;
#ifndef RIPPER
# define ERR(mesg) yyerror0(mesg)
#else
# define ERR(mesg) (dispatch2(param_error, WARN_S(mesg), lhs), ripper_error(p))
#endif
      case ID_CONST:
	ERR("formal argument cannot be a constant");
	return 0;
      case ID_INSTANCE:
	ERR("formal argument cannot be an instance variable");
	return 0;
      case ID_GLOBAL:
	ERR("formal argument cannot be a global variable");
	return 0;
      case ID_CLASS:
	ERR("formal argument cannot be a class variable");
	return 0;
      default:
	ERR("formal argument must be local variable");
	return 0;
#undef ERR
    }
    shadowing_lvar(p, lhs);
    return lhs;
}

static int
lvar_defined(struct parser_params *p, ID id)
{
    return (dyna_in_block(p) && dvar_defined(p, id)) || local_id(p, id);
}

/* emacsen -*- hack */
static long
parser_encode_length(struct parser_params *p, const char *name, long len)
{
    long nlen;

    if (len > 5 && name[nlen = len - 5] == '-') {
	if (rb_memcicmp(name + nlen + 1, "unix", 4) == 0)
	    return nlen;
    }
    if (len > 4 && name[nlen = len - 4] == '-') {
	if (rb_memcicmp(name + nlen + 1, "dos", 3) == 0)
	    return nlen;
	if (rb_memcicmp(name + nlen + 1, "mac", 3) == 0 &&
	    !(len == 8 && rb_memcicmp(name, "utf8-mac", len) == 0))
	    /* exclude UTF8-MAC because the encoding named "UTF8" doesn't exist in Ruby */
	    return nlen;
    }
    return len;
}

static void
parser_set_encode(struct parser_params *p, const char *name)
{
    int idx = rb_enc_find_index(name);
    rb_encoding *enc;
    VALUE excargs[3];

    if (idx < 0) {
	excargs[1] = rb_sprintf("unknown encoding name: %s", name);
      error:
	excargs[0] = rb_eArgError;
	excargs[2] = rb_make_backtrace();
	rb_ary_unshift(excargs[2], rb_sprintf("%"PRIsVALUE":%d", p->ruby_sourcefile_string, p->ruby_sourceline));
	rb_exc_raise(rb_make_exception(3, excargs));
    }
    enc = rb_enc_from_index(idx);
    if (!rb_enc_asciicompat(enc)) {
	excargs[1] = rb_sprintf("%s is not ASCII compatible", rb_enc_name(enc));
	goto error;
    }
    p->enc = enc;
#ifndef RIPPER
    if (p->debug_lines) {
	VALUE lines = p->debug_lines;
	long i, n = RARRAY_LEN(lines);
	for (i = 0; i < n; ++i) {
	    rb_enc_associate_index(RARRAY_AREF(lines, i), idx);
	}
    }
#endif
}

static int
comment_at_top(struct parser_params *p)
{
    const char *ptr = p->lex.pbeg, *ptr_end = p->lex.pcur - 1;
    if (p->line_count != (p->has_shebang ? 2 : 1)) return 0;
    while (ptr < ptr_end) {
	if (!ISSPACE(*ptr)) return 0;
	ptr++;
    }
    return 1;
}

typedef long (*rb_magic_comment_length_t)(struct parser_params *p, const char *name, long len);
typedef void (*rb_magic_comment_setter_t)(struct parser_params *p, const char *name, const char *val);

static int parser_invalid_pragma_value(struct parser_params *p, const char *name, const char *val);

static void
magic_comment_encoding(struct parser_params *p, const char *name, const char *val)
{
    if (!comment_at_top(p)) {
	return;
    }
    parser_set_encode(p, val);
}

static int
parser_get_bool(struct parser_params *p, const char *name, const char *val)
{
    switch (*val) {
      case 't': case 'T':
	if (STRCASECMP(val, "true") == 0) {
	    return TRUE;
	}
	break;
      case 'f': case 'F':
	if (STRCASECMP(val, "false") == 0) {
	    return FALSE;
	}
	break;
    }
    return parser_invalid_pragma_value(p, name, val);
}

static int
parser_invalid_pragma_value(struct parser_params *p, const char *name, const char *val)
{
    rb_warning2("invalid value for %s: %s", WARN_S(name), WARN_S(val));
    return -1;
}

static void
parser_set_token_info(struct parser_params *p, const char *name, const char *val)
{
    int b = parser_get_bool(p, name, val);
    if (b >= 0) p->token_info_enabled = b;
}

static void
parser_set_compile_option_flag(struct parser_params *p, const char *name, const char *val)
{
    int b;

    if (p->token_seen) {
	rb_warning1("`%s' is ignored after any tokens", WARN_S(name));
	return;
    }

    b = parser_get_bool(p, name, val);
    if (b < 0) return;

    if (!p->compile_option)
	p->compile_option = rb_obj_hide(rb_ident_hash_new());
    rb_hash_aset(p->compile_option, ID2SYM(rb_intern(name)),
		 (b ? Qtrue : Qfalse));
}

static void
parser_set_shareable_constant_value(struct parser_params *p, const char *name, const char *val)
{
    for (const char *s = p->lex.pbeg, *e = p->lex.pcur; s < e; ++s) {
	if (*s == ' ' || *s == '\t') continue;
	if (*s == '#') break;
	rb_warning1("`%s' is ignored unless in comment-only line", WARN_S(name));
	return;
    }

    switch (*val) {
      case 'n': case 'N':
	if (STRCASECMP(val, "none") == 0) {
	    p->ctxt.shareable_constant_value = shareable_none;
	    return;
	}
	break;
      case 'l': case 'L':
	if (STRCASECMP(val, "literal") == 0) {
	    p->ctxt.shareable_constant_value = shareable_literal;
	    return;
	}
	break;
      case 'e': case 'E':
	if (STRCASECMP(val, "experimental_copy") == 0) {
	    p->ctxt.shareable_constant_value = shareable_copy;
	    return;
	}
	if (STRCASECMP(val, "experimental_everything") == 0) {
	    p->ctxt.shareable_constant_value = shareable_everything;
	    return;
	}
	break;
    }
    parser_invalid_pragma_value(p, name, val);
}

# if WARN_PAST_SCOPE
static void
parser_set_past_scope(struct parser_params *p, const char *name, const char *val)
{
    int b = parser_get_bool(p, name, val);
    if (b >= 0) p->past_scope_enabled = b;
}
# endif

struct magic_comment {
    const char *name;
    rb_magic_comment_setter_t func;
    rb_magic_comment_length_t length;
};

static const struct magic_comment magic_comments[] = {
    {"coding", magic_comment_encoding, parser_encode_length},
    {"encoding", magic_comment_encoding, parser_encode_length},
    {"frozen_string_literal", parser_set_compile_option_flag},
    {"shareable_constant_value", parser_set_shareable_constant_value},
    {"warn_indent", parser_set_token_info},
# if WARN_PAST_SCOPE
    {"warn_past_scope", parser_set_past_scope},
# endif
};

static const char *
magic_comment_marker(const char *str, long len)
{
    long i = 2;

    while (i < len) {
	switch (str[i]) {
	  case '-':
	    if (str[i-1] == '*' && str[i-2] == '-') {
		return str + i + 1;
	    }
	    i += 2;
	    break;
	  case '*':
	    if (i + 1 >= len) return 0;
	    if (str[i+1] != '-') {
		i += 4;
	    }
	    else if (str[i-1] != '-') {
		i += 2;
	    }
	    else {
		return str + i + 2;
	    }
	    break;
	  default:
	    i += 3;
	    break;
	}
    }
    return 0;
}

static int
parser_magic_comment(struct parser_params *p, const char *str, long len)
{
    int indicator = 0;
    VALUE name = 0, val = 0;
    const char *beg, *end, *vbeg, *vend;
#define str_copy(_s, _p, _n) ((_s) \
	? (void)(rb_str_resize((_s), (_n)), \
	   MEMCPY(RSTRING_PTR(_s), (_p), char, (_n)), (_s)) \
	: (void)((_s) = STR_NEW((_p), (_n))))

    if (len <= 7) return FALSE;
    if (!!(beg = magic_comment_marker(str, len))) {
	if (!(end = magic_comment_marker(beg, str + len - beg)))
	    return FALSE;
	indicator = TRUE;
	str = beg;
	len = end - beg - 3;
    }

    /* %r"([^\\s\'\":;]+)\\s*:\\s*(\"(?:\\\\.|[^\"])*\"|[^\"\\s;]+)[\\s;]*" */
    while (len > 0) {
	const struct magic_comment *mc = magic_comments;
	char *s;
	int i;
	long n = 0;

	for (; len > 0 && *str; str++, --len) {
	    switch (*str) {
	      case '\'': case '"': case ':': case ';':
		continue;
	    }
	    if (!ISSPACE(*str)) break;
	}
	for (beg = str; len > 0; str++, --len) {
	    switch (*str) {
	      case '\'': case '"': case ':': case ';':
		break;
	      default:
		if (ISSPACE(*str)) break;
		continue;
	    }
	    break;
	}
	for (end = str; len > 0 && ISSPACE(*str); str++, --len);
	if (!len) break;
	if (*str != ':') {
	    if (!indicator) return FALSE;
	    continue;
	}

	do str++; while (--len > 0 && ISSPACE(*str));
	if (!len) break;
	if (*str == '"') {
	    for (vbeg = ++str; --len > 0 && *str != '"'; str++) {
		if (*str == '\\') {
		    --len;
		    ++str;
		}
	    }
	    vend = str;
	    if (len) {
		--len;
		++str;
	    }
	}
	else {
	    for (vbeg = str; len > 0 && *str != '"' && *str != ';' && !ISSPACE(*str); --len, str++);
	    vend = str;
	}
	if (indicator) {
	    while (len > 0 && (*str == ';' || ISSPACE(*str))) --len, str++;
	}
	else {
	    while (len > 0 && (ISSPACE(*str))) --len, str++;
	    if (len) return FALSE;
	}

	n = end - beg;
	str_copy(name, beg, n);
	s = RSTRING_PTR(name);
	for (i = 0; i < n; ++i) {
	    if (s[i] == '-') s[i] = '_';
	}
	do {
	    if (STRNCASECMP(mc->name, s, n) == 0 && !mc->name[n]) {
		n = vend - vbeg;
		if (mc->length) {
		    n = (*mc->length)(p, vbeg, n);
		}
		str_copy(val, vbeg, n);
		(*mc->func)(p, mc->name, RSTRING_PTR(val));
		break;
	    }
	} while (++mc < magic_comments + numberof(magic_comments));
#ifdef RIPPER
	str_copy(val, vbeg, vend - vbeg);
	dispatch2(magic_comment, name, val);
#endif
    }

    return TRUE;
}

static void
set_file_encoding(struct parser_params *p, const char *str, const char *send)
{
    int sep = 0;
    const char *beg = str;
    VALUE s;

    for (;;) {
	if (send - str <= 6) return;
	switch (str[6]) {
	  case 'C': case 'c': str += 6; continue;
	  case 'O': case 'o': str += 5; continue;
	  case 'D': case 'd': str += 4; continue;
	  case 'I': case 'i': str += 3; continue;
	  case 'N': case 'n': str += 2; continue;
	  case 'G': case 'g': str += 1; continue;
	  case '=': case ':':
	    sep = 1;
	    str += 6;
	    break;
	  default:
	    str += 6;
	    if (ISSPACE(*str)) break;
	    continue;
	}
	if (STRNCASECMP(str-6, "coding", 6) == 0) break;
    }
    for (;;) {
	do {
	    if (++str >= send) return;
	} while (ISSPACE(*str));
	if (sep) break;
	if (*str != '=' && *str != ':') return;
	sep = 1;
	str++;
    }
    beg = str;
    while ((*str == '-' || *str == '_' || ISALNUM(*str)) && ++str < send);
    s = rb_str_new(beg, parser_encode_length(p, beg, str - beg));
    parser_set_encode(p, RSTRING_PTR(s));
    rb_str_resize(s, 0);
}

static void
parser_prepare(struct parser_params *p)
{
    int c = nextc(p);
    p->token_info_enabled = !compile_for_eval && RTEST(ruby_verbose);
    switch (c) {
      case '#':
	if (peek(p, '!')) p->has_shebang = 1;
	break;
      case 0xef:		/* UTF-8 BOM marker */
	if (p->lex.pend - p->lex.pcur >= 2 &&
	    (unsigned char)p->lex.pcur[0] == 0xbb &&
	    (unsigned char)p->lex.pcur[1] == 0xbf) {
	    p->enc = rb_utf8_encoding();
	    p->lex.pcur += 2;
	    p->lex.pbeg = p->lex.pcur;
	    return;
	}
	break;
      case EOF:
	return;
    }
    pushback(p, c);
    p->enc = rb_enc_get(p->lex.lastline);
}

#ifndef RIPPER
#define ambiguous_operator(tok, op, syn) ( \
    rb_warning0("`"op"' after local variable or literal is interpreted as binary operator"), \
    rb_warning0("even though it seems like "syn""))
#else
#define ambiguous_operator(tok, op, syn) \
    dispatch2(operator_ambiguous, TOKEN2VAL(tok), rb_str_new_cstr(syn))
#endif
#define warn_balanced(tok, op, syn) ((void) \
    (!IS_lex_state_for(last_state, EXPR_CLASS|EXPR_DOT|EXPR_FNAME|EXPR_ENDFN) && \
     space_seen && !ISSPACE(c) && \
     (ambiguous_operator(tok, op, syn), 0)), \
     (enum yytokentype)(tok))

static VALUE
parse_rational(struct parser_params *p, char *str, int len, int seen_point)
{
    VALUE v;
    char *point = &str[seen_point];
    size_t fraclen = len-seen_point-1;
    memmove(point, point+1, fraclen+1);
    v = rb_cstr_to_inum(str, 10, FALSE);
    return rb_rational_new(v, rb_int_positive_pow(10, fraclen));
}

static enum yytokentype
no_digits(struct parser_params *p)
{
    yyerror0("numeric literal without digits");
    if (peek(p, '_')) nextc(p);
    /* dummy 0, for tUMINUS_NUM at numeric */
    return set_integer_literal(p, INT2FIX(0), 0);
}

static enum yytokentype
parse_numeric(struct parser_params *p, int c)
{
    int is_float, seen_point, seen_e, nondigit;
    int suffix;

    is_float = seen_point = seen_e = nondigit = 0;
    SET_LEX_STATE(EXPR_END);
    newtok(p);
    if (c == '-' || c == '+') {
	tokadd(p, c);
	c = nextc(p);
    }
    if (c == '0') {
	int start = toklen(p);
	c = nextc(p);
	if (c == 'x' || c == 'X') {
	    /* hexadecimal */
	    c = nextc(p);
	    if (c != -1 && ISXDIGIT(c)) {
		do {
		    if (c == '_') {
			if (nondigit) break;
			nondigit = c;
			continue;
		    }
		    if (!ISXDIGIT(c)) break;
		    nondigit = 0;
		    tokadd(p, c);
		} while ((c = nextc(p)) != -1);
	    }
	    pushback(p, c);
	    tokfix(p);
	    if (toklen(p) == start) {
		return no_digits(p);
	    }
	    else if (nondigit) goto trailing_uc;
	    suffix = number_literal_suffix(p, NUM_SUFFIX_ALL);
	    return set_integer_literal(p, rb_cstr_to_inum(tok(p), 16, FALSE), suffix);
	}
	if (c == 'b' || c == 'B') {
	    /* binary */
	    c = nextc(p);
	    if (c == '0' || c == '1') {
		do {
		    if (c == '_') {
			if (nondigit) break;
			nondigit = c;
			continue;
		    }
		    if (c != '0' && c != '1') break;
		    nondigit = 0;
		    tokadd(p, c);
		} while ((c = nextc(p)) != -1);
	    }
	    pushback(p, c);
	    tokfix(p);
	    if (toklen(p) == start) {
		return no_digits(p);
	    }
	    else if (nondigit) goto trailing_uc;
	    suffix = number_literal_suffix(p, NUM_SUFFIX_ALL);
	    return set_integer_literal(p, rb_cstr_to_inum(tok(p), 2, FALSE), suffix);
	}
	if (c == 'd' || c == 'D') {
	    /* decimal */
	    c = nextc(p);
	    if (c != -1 && ISDIGIT(c)) {
		do {
		    if (c == '_') {
			if (nondigit) break;
			nondigit = c;
			continue;
		    }
		    if (!ISDIGIT(c)) break;
		    nondigit = 0;
		    tokadd(p, c);
		} while ((c = nextc(p)) != -1);
	    }
	    pushback(p, c);
	    tokfix(p);
	    if (toklen(p) == start) {
		return no_digits(p);
	    }
	    else if (nondigit) goto trailing_uc;
	    suffix = number_literal_suffix(p, NUM_SUFFIX_ALL);
	    return set_integer_literal(p, rb_cstr_to_inum(tok(p), 10, FALSE), suffix);
	}
	if (c == '_') {
	    /* 0_0 */
	    goto octal_number;
	}
	if (c == 'o' || c == 'O') {
	    /* prefixed octal */
	    c = nextc(p);
	    if (c == -1 || c == '_' || !ISDIGIT(c)) {
		return no_digits(p);
	    }
	}
	if (c >= '0' && c <= '7') {
	    /* octal */
	  octal_number:
	    do {
		if (c == '_') {
		    if (nondigit) break;
		    nondigit = c;
		    continue;
		}
		if (c < '0' || c > '9') break;
		if (c > '7') goto invalid_octal;
		nondigit = 0;
		tokadd(p, c);
	    } while ((c = nextc(p)) != -1);
	    if (toklen(p) > start) {
		pushback(p, c);
		tokfix(p);
		if (nondigit) goto trailing_uc;
		suffix = number_literal_suffix(p, NUM_SUFFIX_ALL);
		return set_integer_literal(p, rb_cstr_to_inum(tok(p), 8, FALSE), suffix);
	    }
	    if (nondigit) {
		pushback(p, c);
		goto trailing_uc;
	    }
	}
	if (c > '7' && c <= '9') {
	  invalid_octal:
	    yyerror0("Invalid octal digit");
	}
	else if (c == '.' || c == 'e' || c == 'E') {
	    tokadd(p, '0');
	}
	else {
	    pushback(p, c);
	    suffix = number_literal_suffix(p, NUM_SUFFIX_ALL);
	    return set_integer_literal(p, INT2FIX(0), suffix);
	}
    }

    for (;;) {
	switch (c) {
	  case '0': case '1': case '2': case '3': case '4':
	  case '5': case '6': case '7': case '8': case '9':
	    nondigit = 0;
	    tokadd(p, c);
	    break;

	  case '.':
	    if (nondigit) goto trailing_uc;
	    if (seen_point || seen_e) {
		goto decode_num;
	    }
	    else {
		int c0 = nextc(p);
		if (c0 == -1 || !ISDIGIT(c0)) {
		    pushback(p, c0);
		    goto decode_num;
		}
		c = c0;
	    }
	    seen_point = toklen(p);
	    tokadd(p, '.');
	    tokadd(p, c);
	    is_float++;
	    nondigit = 0;
	    break;

	  case 'e':
	  case 'E':
	    if (nondigit) {
		pushback(p, c);
		c = nondigit;
		goto decode_num;
	    }
	    if (seen_e) {
		goto decode_num;
	    }
	    nondigit = c;
	    c = nextc(p);
	    if (c != '-' && c != '+' && !ISDIGIT(c)) {
		pushback(p, c);
		nondigit = 0;
		goto decode_num;
	    }
	    tokadd(p, nondigit);
	    seen_e++;
	    is_float++;
	    tokadd(p, c);
	    nondigit = (c == '-' || c == '+') ? c : 0;
	    break;

	  case '_':	/* `_' in number just ignored */
	    if (nondigit) goto decode_num;
	    nondigit = c;
	    break;

	  default:
	    goto decode_num;
	}
	c = nextc(p);
    }

  decode_num:
    pushback(p, c);
    if (nondigit) {
      trailing_uc:
	literal_flush(p, p->lex.pcur - 1);
	YYLTYPE loc = RUBY_INIT_YYLLOC();
	compile_error(p, "trailing `%c' in number", nondigit);
	parser_show_error_line(p, &loc);
    }
    tokfix(p);
    if (is_float) {
	enum yytokentype type = tFLOAT;
	VALUE v;

	suffix = number_literal_suffix(p, seen_e ? NUM_SUFFIX_I : NUM_SUFFIX_ALL);
	if (suffix & NUM_SUFFIX_R) {
	    type = tRATIONAL;
	    v = parse_rational(p, tok(p), toklen(p), seen_point);
	}
	else {
	    double d = strtod(tok(p), 0);
	    if (errno == ERANGE) {
		rb_warning1("Float %s out of range", WARN_S(tok(p)));
		errno = 0;
	    }
	    v = DBL2NUM(d);
	}
	return set_number_literal(p, v, type, suffix);
    }
    suffix = number_literal_suffix(p, NUM_SUFFIX_ALL);
    return set_integer_literal(p, rb_cstr_to_inum(tok(p), 10, FALSE), suffix);
}

static enum yytokentype
parse_qmark(struct parser_params *p, int space_seen)
{
    rb_encoding *enc;
    register int c;
    VALUE lit;

    if (IS_END()) {
	SET_LEX_STATE(EXPR_VALUE);
	return '?';
    }
    c = nextc(p);
    if (c == -1) {
	compile_error(p, "incomplete character syntax");
	return 0;
    }
    if (rb_enc_isspace(c, p->enc)) {
	if (!IS_ARG()) {
	    int c2 = escaped_control_code(c);
	    if (c2) {
		WARN_SPACE_CHAR(c2, "?");
	    }
	}
      ternary:
	pushback(p, c);
	SET_LEX_STATE(EXPR_VALUE);
	return '?';
    }
    newtok(p);
    enc = p->enc;
    if (!parser_isascii(p)) {
	if (tokadd_mbchar(p, c) == -1) return 0;
    }
    else if ((rb_enc_isalnum(c, p->enc) || c == '_') &&
	     p->lex.pcur < p->lex.pend && is_identchar(p->lex.pcur, p->lex.pend, p->enc)) {
	if (space_seen) {
	    const char *start = p->lex.pcur - 1, *ptr = start;
	    do {
		int n = parser_precise_mbclen(p, ptr);
		if (n < 0) return -1;
		ptr += n;
	    } while (ptr < p->lex.pend && is_identchar(ptr, p->lex.pend, p->enc));
	    rb_warn2("`?' just followed by `%.*s' is interpreted as" \
		     " a conditional operator, put a space after `?'",
		     WARN_I((int)(ptr - start)), WARN_S_L(start, (ptr - start)));
	}
	goto ternary;
    }
    else if (c == '\\') {
	if (peek(p, 'u')) {
	    nextc(p);
	    enc = rb_utf8_encoding();
	    tokadd_utf8(p, &enc, -1, 0, 0);
	}
	else if (!lex_eol_p(p) && !(c = *p->lex.pcur, ISASCII(c))) {
	    nextc(p);
	    if (tokadd_mbchar(p, c) == -1) return 0;
	}
	else {
	    c = read_escape(p, 0, &enc);
	    tokadd(p, c);
	}
    }
    else {
	tokadd(p, c);
    }
    tokfix(p);
    lit = STR_NEW3(tok(p), toklen(p), enc, 0);
    set_yylval_str(lit);
    SET_LEX_STATE(EXPR_END);
    return tCHAR;
}

static enum yytokentype
parse_percent(struct parser_params *p, const int space_seen, const enum lex_state_e last_state)
{
    register int c;
    const char *ptok = p->lex.pcur;

    if (IS_BEG()) {
	int term;
	int paren;

	c = nextc(p);
      quotation:
	if (c == -1 || !ISALNUM(c)) {
	    term = c;
	    if (!ISASCII(c)) goto unknown;
	    c = 'Q';
	}
	else {
	    term = nextc(p);
	    if (rb_enc_isalnum(term, p->enc) || !parser_isascii(p)) {
	      unknown:
		pushback(p, term);
		c = parser_precise_mbclen(p, p->lex.pcur);
		if (c < 0) return 0;
		p->lex.pcur += c;
		yyerror0("unknown type of %string");
		return 0;
	    }
	}
	if (term == -1) {
	    compile_error(p, "unterminated quoted string meets end of file");
	    return 0;
	}
	paren = term;
	if (term == '(') term = ')';
	else if (term == '[') term = ']';
	else if (term == '{') term = '}';
	else if (term == '<') term = '>';
	else paren = 0;

	p->lex.ptok = ptok-1;
	switch (c) {
	  case 'Q':
	    p->lex.strterm = NEW_STRTERM(str_dquote, term, paren);
	    return tSTRING_BEG;

	  case 'q':
	    p->lex.strterm = NEW_STRTERM(str_squote, term, paren);
	    return tSTRING_BEG;

	  case 'W':
	    p->lex.strterm = NEW_STRTERM(str_dword, term, paren);
	    return tWORDS_BEG;

	  case 'w':
	    p->lex.strterm = NEW_STRTERM(str_sword, term, paren);
	    return tQWORDS_BEG;

	  case 'I':
	    p->lex.strterm = NEW_STRTERM(str_dword, term, paren);
	    return tSYMBOLS_BEG;

	  case 'i':
	    p->lex.strterm = NEW_STRTERM(str_sword, term, paren);
	    return tQSYMBOLS_BEG;

	  case 'x':
	    p->lex.strterm = NEW_STRTERM(str_xquote, term, paren);
	    return tXSTRING_BEG;

	  case 'r':
	    p->lex.strterm = NEW_STRTERM(str_regexp, term, paren);
	    return tREGEXP_BEG;

	  case 's':
	    p->lex.strterm = NEW_STRTERM(str_ssym, term, paren);
	    SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);
	    return tSYMBEG;

	  default:
	    yyerror0("unknown type of %string");
	    return 0;
	}
    }
    if ((c = nextc(p)) == '=') {
	set_yylval_id('%');
	SET_LEX_STATE(EXPR_BEG);
	return tOP_ASGN;
    }
    if (IS_SPCARG(c) || (IS_lex_state(EXPR_FITEM) && c == 's')) {
	goto quotation;
    }
    SET_LEX_STATE(IS_AFTER_OPERATOR() ? EXPR_ARG : EXPR_BEG);
    pushback(p, c);
    return warn_balanced('%', "%%", "string literal");
}

static int
tokadd_ident(struct parser_params *p, int c)
{
    do {
	if (tokadd_mbchar(p, c) == -1) return -1;
	c = nextc(p);
    } while (parser_is_identchar(p));
    pushback(p, c);
    return 0;
}

static ID
tokenize_ident(struct parser_params *p, const enum lex_state_e last_state)
{
    ID ident = TOK_INTERN();

    set_yylval_name(ident);

    return ident;
}

static int
parse_numvar(struct parser_params *p)
{
    size_t len;
    int overflow;
    unsigned long n = ruby_scan_digits(tok(p)+1, toklen(p)-1, 10, &len, &overflow);
    const unsigned long nth_ref_max =
	((FIXNUM_MAX < INT_MAX) ? FIXNUM_MAX : INT_MAX) >> 1;
    /* NTH_REF is left-shifted to be ORed with back-ref flag and
     * turned into a Fixnum, in compile.c */

    if (overflow || n > nth_ref_max) {
	/* compile_error()? */
	rb_warn1("`%s' is too big for a number variable, always nil", WARN_S(tok(p)));
	return 0;		/* $0 is $PROGRAM_NAME, not NTH_REF */
    }
    else {
	return (int)n;
    }
}

static enum yytokentype
parse_gvar(struct parser_params *p, const enum lex_state_e last_state)
{
    const char *ptr = p->lex.pcur;
    register int c;

    SET_LEX_STATE(EXPR_END);
    p->lex.ptok = ptr - 1; /* from '$' */
    newtok(p);
    c = nextc(p);
    switch (c) {
      case '_':		/* $_: last read line string */
	c = nextc(p);
	if (parser_is_identchar(p)) {
	    tokadd(p, '$');
	    tokadd(p, '_');
	    break;
	}
	pushback(p, c);
	c = '_';
	/* fall through */
      case '~':		/* $~: match-data */
      case '*':		/* $*: argv */
      case '$':		/* $$: pid */
      case '?':		/* $?: last status */
      case '!':		/* $!: error string */
      case '@':		/* $@: error position */
      case '/':		/* $/: input record separator */
      case '\\':		/* $\: output record separator */
      case ';':		/* $;: field separator */
      case ',':		/* $,: output field separator */
      case '.':		/* $.: last read line number */
      case '=':		/* $=: ignorecase */
      case ':':		/* $:: load path */
      case '<':		/* $<: reading filename */
      case '>':		/* $>: default output handle */
      case '\"':		/* $": already loaded files */
	tokadd(p, '$');
	tokadd(p, c);
	goto gvar;

      case '-':
	tokadd(p, '$');
	tokadd(p, c);
	c = nextc(p);
	if (parser_is_identchar(p)) {
	    if (tokadd_mbchar(p, c) == -1) return 0;
	}
	else {
	    pushback(p, c);
	    pushback(p, '-');
	    return '$';
	}
      gvar:
	set_yylval_name(TOK_INTERN());
	return tGVAR;

      case '&':		/* $&: last match */
      case '`':		/* $`: string before last match */
      case '\'':		/* $': string after last match */
      case '+':		/* $+: string matches last paren. */
	if (IS_lex_state_for(last_state, EXPR_FNAME)) {
	    tokadd(p, '$');
	    tokadd(p, c);
	    goto gvar;
	}
	set_yylval_node(NEW_BACK_REF(c, &_cur_loc));
	return tBACK_REF;

      case '1': case '2': case '3':
      case '4': case '5': case '6':
      case '7': case '8': case '9':
	tokadd(p, '$');
	do {
	    tokadd(p, c);
	    c = nextc(p);
	} while (c != -1 && ISDIGIT(c));
	pushback(p, c);
	if (IS_lex_state_for(last_state, EXPR_FNAME)) goto gvar;
	tokfix(p);
	c = parse_numvar(p);
	set_yylval_node(NEW_NTH_REF(c, &_cur_loc));
	return tNTH_REF;

      default:
	if (!parser_is_identchar(p)) {
	    YYLTYPE loc = RUBY_INIT_YYLLOC();
	    if (c == -1 || ISSPACE(c)) {
		compile_error(p, "`$' without identifiers is not allowed as a global variable name");
	    }
	    else {
		pushback(p, c);
		compile_error(p, "`$%c' is not allowed as a global variable name", c);
	    }
	    parser_show_error_line(p, &loc);
	    set_yylval_noname();
	    return tGVAR;
	}
	/* fall through */
      case '0':
	tokadd(p, '$');
    }

    if (tokadd_ident(p, c)) return 0;
    SET_LEX_STATE(EXPR_END);
    tokenize_ident(p, last_state);
    return tGVAR;
}

#ifndef RIPPER
static bool
parser_numbered_param(struct parser_params *p, int n)
{
    if (n < 0) return false;

    if (DVARS_TERMINAL_P(p->lvtbl->args) || DVARS_TERMINAL_P(p->lvtbl->args->prev)) {
	return false;
    }
    if (p->max_numparam == ORDINAL_PARAM) {
	compile_error(p, "ordinary parameter is defined");
	return false;
    }
    struct vtable *args = p->lvtbl->args;
    if (p->max_numparam < n) {
	p->max_numparam = n;
    }
    while (n > args->pos) {
	vtable_add(args, NUMPARAM_IDX_TO_ID(args->pos+1));
    }
    return true;
}
#endif

static enum yytokentype
parse_atmark(struct parser_params *p, const enum lex_state_e last_state)
{
    const char *ptr = p->lex.pcur;
    enum yytokentype result = tIVAR;
    register int c = nextc(p);
    YYLTYPE loc;

    p->lex.ptok = ptr - 1; /* from '@' */
    newtok(p);
    tokadd(p, '@');
    if (c == '@') {
	result = tCVAR;
	tokadd(p, '@');
	c = nextc(p);
    }
    SET_LEX_STATE(IS_lex_state_for(last_state, EXPR_FNAME) ? EXPR_ENDFN : EXPR_END);
    if (c == -1 || !parser_is_identchar(p)) {
	pushback(p, c);
	RUBY_SET_YYLLOC(loc);
	if (result == tIVAR) {
	    compile_error(p, "`@' without identifiers is not allowed as an instance variable name");
	}
	else {
	    compile_error(p, "`@@' without identifiers is not allowed as a class variable name");
	}
	parser_show_error_line(p, &loc);
	set_yylval_noname();
	SET_LEX_STATE(EXPR_END);
	return result;
    }
    else if (ISDIGIT(c)) {
	pushback(p, c);
	RUBY_SET_YYLLOC(loc);
	if (result == tIVAR) {
	    compile_error(p, "`@%c' is not allowed as an instance variable name", c);
	}
	else {
	    compile_error(p, "`@@%c' is not allowed as a class variable name", c);
	}
	parser_show_error_line(p, &loc);
	set_yylval_noname();
	SET_LEX_STATE(EXPR_END);
	return result;
    }

    if (tokadd_ident(p, c)) return 0;
    tokenize_ident(p, last_state);
    return result;
}

static enum yytokentype
parse_ident(struct parser_params *p, int c, int cmd_state)
{
    enum yytokentype result;
    int mb = ENC_CODERANGE_7BIT;
    const enum lex_state_e last_state = p->lex.state;
    ID ident;

    do {
	if (!ISASCII(c)) mb = ENC_CODERANGE_UNKNOWN;
	if (tokadd_mbchar(p, c) == -1) return 0;
	c = nextc(p);
    } while (parser_is_identchar(p));
    if ((c == '!' || c == '?') && !peek(p, '=')) {
	result = tFID;
	tokadd(p, c);
    }
    else if (c == '=' && IS_lex_state(EXPR_FNAME) &&
	     (!peek(p, '~') && !peek(p, '>') && (!peek(p, '=') || (peek_n(p, '>', 1))))) {
	result = tIDENTIFIER;
	tokadd(p, c);
    }
    else {
	result = tCONSTANT;	/* assume provisionally */
	pushback(p, c);
    }
    tokfix(p);

    if (IS_LABEL_POSSIBLE()) {
	if (IS_LABEL_SUFFIX(0)) {
	    SET_LEX_STATE(EXPR_ARG|EXPR_LABELED);
	    nextc(p);
	    set_yylval_name(TOK_INTERN());
	    return tLABEL;
	}
    }
    if (mb == ENC_CODERANGE_7BIT && !IS_lex_state(EXPR_DOT)) {
	const struct kwtable *kw;

	/* See if it is a reserved word.  */
	kw = rb_reserved_word(tok(p), toklen(p));
	if (kw) {
	    enum lex_state_e state = p->lex.state;
	    if (IS_lex_state_for(state, EXPR_FNAME)) {
		SET_LEX_STATE(EXPR_ENDFN);
		set_yylval_name(rb_intern2(tok(p), toklen(p)));
		return kw->id[0];
	    }
	    SET_LEX_STATE(kw->state);
	    if (IS_lex_state(EXPR_BEG)) {
		p->command_start = TRUE;
	    }
	    if (kw->id[0] == keyword_do) {
		if (lambda_beginning_p()) {
		    p->lex.lpar_beg = -1; /* make lambda_beginning_p() == FALSE in the body of "-> do ... end" */
		    return keyword_do_LAMBDA;
		}
		if (COND_P()) return keyword_do_cond;
		if (CMDARG_P() && !IS_lex_state_for(state, EXPR_CMDARG))
		    return keyword_do_block;
		return keyword_do;
	    }
	    if (IS_lex_state_for(state, (EXPR_BEG | EXPR_LABELED)))
		return kw->id[0];
	    else {
		if (kw->id[0] != kw->id[1])
		    SET_LEX_STATE(EXPR_BEG | EXPR_LABEL);
		return kw->id[1];
	    }
	}
    }

    if (IS_lex_state(EXPR_BEG_ANY | EXPR_ARG_ANY | EXPR_DOT)) {
	if (cmd_state) {
	    SET_LEX_STATE(EXPR_CMDARG);
	}
	else {
	    SET_LEX_STATE(EXPR_ARG);
	}
    }
    else if (p->lex.state == EXPR_FNAME) {
	SET_LEX_STATE(EXPR_ENDFN);
    }
    else {
	SET_LEX_STATE(EXPR_END);
    }

    ident = tokenize_ident(p, last_state);
    if (result == tCONSTANT && is_local_id(ident)) result = tIDENTIFIER;
    if (!IS_lex_state_for(last_state, EXPR_DOT|EXPR_FNAME) &&
	(result == tIDENTIFIER) && /* not EXPR_FNAME, not attrasgn */
	lvar_defined(p, ident)) {
	SET_LEX_STATE(EXPR_END|EXPR_LABEL);
    }
    return result;
}

static enum yytokentype
parser_yylex(struct parser_params *p)
{
    register int c;
    int space_seen = 0;
    int cmd_state;
    int label;
    enum lex_state_e last_state;
    int fallthru = FALSE;
    int token_seen = p->token_seen;

    if (p->lex.strterm) {
	if (p->lex.strterm->flags & STRTERM_HEREDOC) {
	    return here_document(p, &p->lex.strterm->u.heredoc);
	}
	else {
	    token_flush(p);
	    return parse_string(p, &p->lex.strterm->u.literal);
	}
    }
    cmd_state = p->command_start;
    p->command_start = FALSE;
    p->token_seen = TRUE;
  retry:
    last_state = p->lex.state;
#ifndef RIPPER
    token_flush(p);
#endif
    switch (c = nextc(p)) {
      case '\0':		/* NUL */
      case '\004':		/* ^D */
      case '\032':		/* ^Z */
      case -1:			/* end of script. */
	return 0;

	/* white spaces */
      case '\r':
	if (!p->cr_seen) {
	    p->cr_seen = TRUE;
	    /* carried over with p->lex.nextline for nextc() */
	    rb_warn0("encountered \\r in middle of line, treated as a mere space");
	}
	/* fall through */
      case ' ': case '\t': case '\f':
      case '\13': /* '\v' */
	space_seen = 1;
#ifdef RIPPER
	while ((c = nextc(p))) {
	    switch (c) {
	      case ' ': case '\t': case '\f': case '\r':
	      case '\13': /* '\v' */
		break;
	      default:
		goto outofloop;
	    }
	}
      outofloop:
	pushback(p, c);
	dispatch_scan_event(p, tSP);
#endif
	goto retry;

      case '#':		/* it's a comment */
	p->token_seen = token_seen;
	/* no magic_comment in shebang line */
	if (!parser_magic_comment(p, p->lex.pcur, p->lex.pend - p->lex.pcur)) {
	    if (comment_at_top(p)) {
		set_file_encoding(p, p->lex.pcur, p->lex.pend);
	    }
	}
	lex_goto_eol(p);
        dispatch_scan_event(p, tCOMMENT);
        fallthru = TRUE;
	/* fall through */
      case '\n':
	p->token_seen = token_seen;
	c = (IS_lex_state(EXPR_BEG|EXPR_CLASS|EXPR_FNAME|EXPR_DOT) &&
	     !IS_lex_state(EXPR_LABELED));
	if (c || IS_lex_state_all(EXPR_ARG|EXPR_LABELED)) {
            if (!fallthru) {
                dispatch_scan_event(p, tIGNORED_NL);
            }
            fallthru = FALSE;
	    if (!c && p->ctxt.in_kwarg) {
		goto normal_newline;
	    }
	    goto retry;
	}
	while (1) {
	    switch (c = nextc(p)) {
	      case ' ': case '\t': case '\f': case '\r':
	      case '\13': /* '\v' */
		space_seen = 1;
		break;
	      case '#':
		pushback(p, c);
		if (space_seen) dispatch_scan_event(p, tSP);
		goto retry;
	      case '&':
	      case '.': {
		dispatch_delayed_token(p, tIGNORED_NL);
		if (peek(p, '.') == (c == '&')) {
		    pushback(p, c);
		    dispatch_scan_event(p, tSP);
		    goto retry;
		}
	      }
	      default:
		p->ruby_sourceline--;
		p->lex.nextline = p->lex.lastline;
	      case -1:		/* EOF no decrement*/
#ifndef RIPPER
		if (p->lex.prevline && !p->eofp) p->lex.lastline = p->lex.prevline;
		p->lex.pbeg = RSTRING_PTR(p->lex.lastline);
		p->lex.pend = p->lex.pcur = p->lex.pbeg + RSTRING_LEN(p->lex.lastline);
		pushback(p, 1); /* always pushback */
		p->lex.ptok = p->lex.pcur;
#else
		lex_goto_eol(p);
		if (c != -1) {
		    p->lex.ptok = p->lex.pcur;
		}
#endif
		goto normal_newline;
	    }
	}
      normal_newline:
	p->command_start = TRUE;
	SET_LEX_STATE(EXPR_BEG);
	return '\n';

      case '*':
	if ((c = nextc(p)) == '*') {
	    if ((c = nextc(p)) == '=') {
		set_yylval_id(idPow);
		SET_LEX_STATE(EXPR_BEG);
		return tOP_ASGN;
	    }
	    pushback(p, c);
	    if (IS_SPCARG(c)) {
		rb_warning0("`**' interpreted as argument prefix");
		c = tDSTAR;
	    }
	    else if (IS_BEG()) {
		c = tDSTAR;
	    }
	    else {
		c = warn_balanced((enum ruby_method_ids)tPOW, "**", "argument prefix");
	    }
	}
	else {
	    if (c == '=') {
                set_yylval_id('*');
		SET_LEX_STATE(EXPR_BEG);
		return tOP_ASGN;
	    }
	    pushback(p, c);
	    if (IS_SPCARG(c)) {
		rb_warning0("`*' interpreted as argument prefix");
		c = tSTAR;
	    }
	    else if (IS_BEG()) {
		c = tSTAR;
	    }
	    else {
		c = warn_balanced('*', "*", "argument prefix");
	    }
	}
	SET_LEX_STATE(IS_AFTER_OPERATOR() ? EXPR_ARG : EXPR_BEG);
	return c;

      case '!':
	c = nextc(p);
	if (IS_AFTER_OPERATOR()) {
	    SET_LEX_STATE(EXPR_ARG);
	    if (c == '@') {
		return '!';
	    }
	}
	else {
	    SET_LEX_STATE(EXPR_BEG);
	}
	if (c == '=') {
	    return tNEQ;
	}
	if (c == '~') {
	    return tNMATCH;
	}
	pushback(p, c);
	return '!';

      case '=':
	if (was_bol(p)) {
	    /* skip embedded rd document */
	    if (word_match_p(p, "begin", 5)) {
		int first_p = TRUE;

		lex_goto_eol(p);
		dispatch_scan_event(p, tEMBDOC_BEG);
		for (;;) {
		    lex_goto_eol(p);
		    if (!first_p) {
			dispatch_scan_event(p, tEMBDOC);
		    }
		    first_p = FALSE;
		    c = nextc(p);
		    if (c == -1) {
			compile_error(p, "embedded document meets end of file");
			return 0;
		    }
		    if (c == '=' && word_match_p(p, "end", 3)) {
			break;
		    }
		    pushback(p, c);
		}
		lex_goto_eol(p);
		dispatch_scan_event(p, tEMBDOC_END);
		goto retry;
	    }
	}

	SET_LEX_STATE(IS_AFTER_OPERATOR() ? EXPR_ARG : EXPR_BEG);
	if ((c = nextc(p)) == '=') {
	    if ((c = nextc(p)) == '=') {
		return tEQQ;
	    }
	    pushback(p, c);
	    return tEQ;
	}
	if (c == '~') {
	    return tMATCH;
	}
	else if (c == '>') {
	    return tASSOC;
	}
	pushback(p, c);
	return '=';

      case '<':
	c = nextc(p);
	if (c == '<' &&
	    !IS_lex_state(EXPR_DOT | EXPR_CLASS) &&
	    !IS_END() &&
	    (!IS_ARG() || IS_lex_state(EXPR_LABELED) || space_seen)) {
	    int token = heredoc_identifier(p);
	    if (token) return token < 0 ? 0 : token;
	}
	if (IS_AFTER_OPERATOR()) {
	    SET_LEX_STATE(EXPR_ARG);
	}
	else {
	    if (IS_lex_state(EXPR_CLASS))
		p->command_start = TRUE;
	    SET_LEX_STATE(EXPR_BEG);
	}
	if (c == '=') {
	    if ((c = nextc(p)) == '>') {
		return tCMP;
	    }
	    pushback(p, c);
	    return tLEQ;
	}
	if (c == '<') {
	    if ((c = nextc(p)) == '=') {
		set_yylval_id(idLTLT);
		SET_LEX_STATE(EXPR_BEG);
		return tOP_ASGN;
	    }
	    pushback(p, c);
	    return warn_balanced((enum ruby_method_ids)tLSHFT, "<<", "here document");
	}
	pushback(p, c);
	return '<';

      case '>':
	SET_LEX_STATE(IS_AFTER_OPERATOR() ? EXPR_ARG : EXPR_BEG);
	if ((c = nextc(p)) == '=') {
	    return tGEQ;
	}
	if (c == '>') {
	    if ((c = nextc(p)) == '=') {
		set_yylval_id(idGTGT);
		SET_LEX_STATE(EXPR_BEG);
		return tOP_ASGN;
	    }
	    pushback(p, c);
	    return tRSHFT;
	}
	pushback(p, c);
	return '>';

      case '"':
	label = (IS_LABEL_POSSIBLE() ? str_label : 0);
	p->lex.strterm = NEW_STRTERM(str_dquote | label, '"', 0);
	p->lex.ptok = p->lex.pcur-1;
	return tSTRING_BEG;

      case '`':
	if (IS_lex_state(EXPR_FNAME)) {
	    SET_LEX_STATE(EXPR_ENDFN);
	    return c;
	}
	if (IS_lex_state(EXPR_DOT)) {
	    if (cmd_state)
		SET_LEX_STATE(EXPR_CMDARG);
	    else
		SET_LEX_STATE(EXPR_ARG);
	    return c;
	}
	p->lex.strterm = NEW_STRTERM(str_xquote, '`', 0);
	return tXSTRING_BEG;

      case '\'':
	label = (IS_LABEL_POSSIBLE() ? str_label : 0);
	p->lex.strterm = NEW_STRTERM(str_squote | label, '\'', 0);
	p->lex.ptok = p->lex.pcur-1;
	return tSTRING_BEG;

      case '?':
	return parse_qmark(p, space_seen);

      case '&':
	if ((c = nextc(p)) == '&') {
	    SET_LEX_STATE(EXPR_BEG);
	    if ((c = nextc(p)) == '=') {
                set_yylval_id(idANDOP);
		SET_LEX_STATE(EXPR_BEG);
		return tOP_ASGN;
	    }
	    pushback(p, c);
	    return tANDOP;
	}
	else if (c == '=') {
            set_yylval_id('&');
	    SET_LEX_STATE(EXPR_BEG);
	    return tOP_ASGN;
	}
	else if (c == '.') {
	    set_yylval_id(idANDDOT);
	    SET_LEX_STATE(EXPR_DOT);
	    return tANDDOT;
	}
	pushback(p, c);
	if (IS_SPCARG(c)) {
	    if ((c != ':') ||
		(c = peekc_n(p, 1)) == -1 ||
		!(c == '\'' || c == '"' ||
		  is_identchar((p->lex.pcur+1), p->lex.pend, p->enc))) {
		rb_warning0("`&' interpreted as argument prefix");
	    }
	    c = tAMPER;
	}
	else if (IS_BEG()) {
	    c = tAMPER;
	}
	else {
	    c = warn_balanced('&', "&", "argument prefix");
	}
	SET_LEX_STATE(IS_AFTER_OPERATOR() ? EXPR_ARG : EXPR_BEG);
	return c;

      case '|':
	if ((c = nextc(p)) == '|') {
	    SET_LEX_STATE(EXPR_BEG);
	    if ((c = nextc(p)) == '=') {
                set_yylval_id(idOROP);
		SET_LEX_STATE(EXPR_BEG);
		return tOP_ASGN;
	    }
	    pushback(p, c);
	    if (IS_lex_state_for(last_state, EXPR_BEG)) {
		c = '|';
		pushback(p, '|');
		return c;
	    }
	    return tOROP;
	}
	if (c == '=') {
            set_yylval_id('|');
	    SET_LEX_STATE(EXPR_BEG);
	    return tOP_ASGN;
	}
	SET_LEX_STATE(IS_AFTER_OPERATOR() ? EXPR_ARG : EXPR_BEG|EXPR_LABEL);
	pushback(p, c);
	return '|';

      case '+':
	c = nextc(p);
	if (IS_AFTER_OPERATOR()) {
	    SET_LEX_STATE(EXPR_ARG);
	    if (c == '@') {
		return tUPLUS;
	    }
	    pushback(p, c);
	    return '+';
	}
	if (c == '=') {
            set_yylval_id('+');
	    SET_LEX_STATE(EXPR_BEG);
	    return tOP_ASGN;
	}
	if (IS_BEG() || (IS_SPCARG(c) && arg_ambiguous(p, '+'))) {
	    SET_LEX_STATE(EXPR_BEG);
	    pushback(p, c);
	    if (c != -1 && ISDIGIT(c)) {
		return parse_numeric(p, '+');
	    }
	    return tUPLUS;
	}
	SET_LEX_STATE(EXPR_BEG);
	pushback(p, c);
	return warn_balanced('+', "+", "unary operator");

      case '-':
	c = nextc(p);
	if (IS_AFTER_OPERATOR()) {
	    SET_LEX_STATE(EXPR_ARG);
	    if (c == '@') {
		return tUMINUS;
	    }
	    pushback(p, c);
	    return '-';
	}
	if (c == '=') {
            set_yylval_id('-');
	    SET_LEX_STATE(EXPR_BEG);
	    return tOP_ASGN;
	}
	if (c == '>') {
	    SET_LEX_STATE(EXPR_ENDFN);
	    return tLAMBDA;
	}
	if (IS_BEG() || (IS_SPCARG(c) && arg_ambiguous(p, '-'))) {
	    SET_LEX_STATE(EXPR_BEG);
	    pushback(p, c);
	    if (c != -1 && ISDIGIT(c)) {
		return tUMINUS_NUM;
	    }
	    return tUMINUS;
	}
	SET_LEX_STATE(EXPR_BEG);
	pushback(p, c);
	return warn_balanced('-', "-", "unary operator");

      case '.': {
        int is_beg = IS_BEG();
	SET_LEX_STATE(EXPR_BEG);
	if ((c = nextc(p)) == '.') {
	    if ((c = nextc(p)) == '.') {
		if (p->lex.paren_nest == 0 && looking_at_eol_p(p)) {
		    rb_warn0("... at EOL, should be parenthesized?");
		}
		else if (p->lex.lpar_beg >= 0 && p->lex.lpar_beg+1 == p->lex.paren_nest) {
		    if (IS_lex_state_for(last_state, EXPR_LABEL))
			return tDOT3;
		}
		return is_beg ? tBDOT3 : tDOT3;
	    }
	    pushback(p, c);
	    return is_beg ? tBDOT2 : tDOT2;
	}
	pushback(p, c);
	if (c != -1 && ISDIGIT(c)) {
	    char prev = p->lex.pcur-1 > p->lex.pbeg ? *(p->lex.pcur-2) : 0;
	    parse_numeric(p, '.');
	    if (ISDIGIT(prev)) {
		yyerror0("unexpected fraction part after numeric literal");
	    }
	    else {
		yyerror0("no .<digit> floating literal anymore; put 0 before dot");
	    }
	    SET_LEX_STATE(EXPR_END);
	    p->lex.ptok = p->lex.pcur;
	    goto retry;
	}
	set_yylval_id('.');
	SET_LEX_STATE(EXPR_DOT);
	return '.';
      }

      case '0': case '1': case '2': case '3': case '4':
      case '5': case '6': case '7': case '8': case '9':
	return parse_numeric(p, c);

      case ')':
	COND_POP();
	CMDARG_POP();
	SET_LEX_STATE(EXPR_ENDFN);
	p->lex.paren_nest--;
	return c;

      case ']':
	COND_POP();
	CMDARG_POP();
	SET_LEX_STATE(EXPR_END);
	p->lex.paren_nest--;
	return c;

      case '}':
	/* tSTRING_DEND does COND_POP and CMDARG_POP in the yacc's rule */
	if (!p->lex.brace_nest--) return tSTRING_DEND;
	COND_POP();
	CMDARG_POP();
	SET_LEX_STATE(EXPR_END);
	p->lex.paren_nest--;
	return c;

      case ':':
	c = nextc(p);
	if (c == ':') {
	    if (IS_BEG() || IS_lex_state(EXPR_CLASS) || IS_SPCARG(-1)) {
		SET_LEX_STATE(EXPR_BEG);
		return tCOLON3;
	    }
	    set_yylval_id(idCOLON2);
	    SET_LEX_STATE(EXPR_DOT);
	    return tCOLON2;
	}
	if (IS_END() || ISSPACE(c) || c == '#') {
	    pushback(p, c);
	    c = warn_balanced(':', ":", "symbol literal");
	    SET_LEX_STATE(EXPR_BEG);
	    return c;
	}
	switch (c) {
	  case '\'':
	    p->lex.strterm = NEW_STRTERM(str_ssym, c, 0);
	    break;
	  case '"':
	    p->lex.strterm = NEW_STRTERM(str_dsym, c, 0);
	    break;
	  default:
	    pushback(p, c);
	    break;
	}
	SET_LEX_STATE(EXPR_FNAME);
	return tSYMBEG;

      case '/':
	if (IS_BEG()) {
	    p->lex.strterm = NEW_STRTERM(str_regexp, '/', 0);
	    return tREGEXP_BEG;
	}
	if ((c = nextc(p)) == '=') {
            set_yylval_id('/');
	    SET_LEX_STATE(EXPR_BEG);
	    return tOP_ASGN;
	}
	pushback(p, c);
	if (IS_SPCARG(c)) {
	    arg_ambiguous(p, '/');
	    p->lex.strterm = NEW_STRTERM(str_regexp, '/', 0);
	    return tREGEXP_BEG;
	}
	SET_LEX_STATE(IS_AFTER_OPERATOR() ? EXPR_ARG : EXPR_BEG);
	return warn_balanced('/', "/", "regexp literal");

      case '^':
	if ((c = nextc(p)) == '=') {
            set_yylval_id('^');
	    SET_LEX_STATE(EXPR_BEG);
	    return tOP_ASGN;
	}
	SET_LEX_STATE(IS_AFTER_OPERATOR() ? EXPR_ARG : EXPR_BEG);
	pushback(p, c);
	return '^';

      case ';':
	SET_LEX_STATE(EXPR_BEG);
	p->command_start = TRUE;
	return ';';

      case ',':
	SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
	return ',';

      case '~':
	if (IS_AFTER_OPERATOR()) {
	    if ((c = nextc(p)) != '@') {
		pushback(p, c);
	    }
	    SET_LEX_STATE(EXPR_ARG);
	}
	else {
	    SET_LEX_STATE(EXPR_BEG);
	}
	return '~';

      case '(':
	if (IS_BEG()) {
	    c = tLPAREN;
	}
	else if (!space_seen) {
	    /* foo( ... ) => method call, no ambiguity */
	}
	else if (IS_ARG() || IS_lex_state_all(EXPR_END|EXPR_LABEL)) {
	    c = tLPAREN_ARG;
	}
	else if (IS_lex_state(EXPR_ENDFN) && !lambda_beginning_p()) {
	    rb_warning0("parentheses after method name is interpreted as "
			"an argument list, not a decomposed argument");
	}
	p->lex.paren_nest++;
	COND_PUSH(0);
	CMDARG_PUSH(0);
	SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
	return c;

      case '[':
	p->lex.paren_nest++;
	if (IS_AFTER_OPERATOR()) {
	    if ((c = nextc(p)) == ']') {
		p->lex.paren_nest--;
		SET_LEX_STATE(EXPR_ARG);
		if ((c = nextc(p)) == '=') {
		    return tASET;
		}
		pushback(p, c);
		return tAREF;
	    }
	    pushback(p, c);
	    SET_LEX_STATE(EXPR_ARG|EXPR_LABEL);
	    return '[';
	}
	else if (IS_BEG()) {
	    c = tLBRACK;
	}
	else if (IS_ARG() && (space_seen || IS_lex_state(EXPR_LABELED))) {
	    c = tLBRACK;
	}
	SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
	COND_PUSH(0);
	CMDARG_PUSH(0);
	return c;

      case '{':
	++p->lex.brace_nest;
	if (lambda_beginning_p())
	    c = tLAMBEG;
	else if (IS_lex_state(EXPR_LABELED))
	    c = tLBRACE;      /* hash */
	else if (IS_lex_state(EXPR_ARG_ANY | EXPR_END | EXPR_ENDFN))
	    c = '{';          /* block (primary) */
	else if (IS_lex_state(EXPR_ENDARG))
	    c = tLBRACE_ARG;  /* block (expr) */
	else
	    c = tLBRACE;      /* hash */
	if (c != tLBRACE) {
	    p->command_start = TRUE;
	    SET_LEX_STATE(EXPR_BEG);
	}
	else {
	    SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
	}
	++p->lex.paren_nest;  /* after lambda_beginning_p() */
	COND_PUSH(0);
	CMDARG_PUSH(0);
	return c;

      case '\\':
	c = nextc(p);
	if (c == '\n') {
	    space_seen = 1;
	    dispatch_scan_event(p, tSP);
	    goto retry; /* skip \\n */
	}
	if (c == ' ') return tSP;
	if (ISSPACE(c)) return c;
	pushback(p, c);
	return '\\';

      case '%':
	return parse_percent(p, space_seen, last_state);

      case '$':
	return parse_gvar(p, last_state);

      case '@':
	return parse_atmark(p, last_state);

      case '_':
	if (was_bol(p) && whole_match_p(p, "__END__", 7, 0)) {
	    p->ruby__end__seen = 1;
	    p->eofp = 1;
#ifndef RIPPER
	    return -1;
#else
            lex_goto_eol(p);
            dispatch_scan_event(p, k__END__);
            return 0;
#endif
	}
	newtok(p);
	break;

      default:
	if (!parser_is_identchar(p)) {
	    compile_error(p, "Invalid char `\\x%02X' in expression", c);
            token_flush(p);
	    goto retry;
	}

	newtok(p);
	break;
    }

    return parse_ident(p, c, cmd_state);
}

static enum yytokentype
yylex(YYSTYPE *lval, YYLTYPE *yylloc, struct parser_params *p)
{
    enum yytokentype t;

    p->lval = lval;
    lval->val = Qundef;
    t = parser_yylex(p);

    if (p->lex.strterm && (p->lex.strterm->flags & STRTERM_HEREDOC))
	RUBY_SET_YYLLOC_FROM_STRTERM_HEREDOC(*yylloc);
    else
	RUBY_SET_YYLLOC(*yylloc);

    if (has_delayed_token(p))
	dispatch_delayed_token(p, t);
    else if (t != 0)
	dispatch_scan_event(p, t);

    return t;
}

#define LVAR_USED ((ID)1 << (sizeof(ID) * CHAR_BIT - 1))

static NODE*
node_newnode(struct parser_params *p, enum node_type type, VALUE a0, VALUE a1, VALUE a2, const rb_code_location_t *loc)
{
    NODE *n = rb_ast_newnode(p->ast, type);

    rb_node_init(n, type, a0, a1, a2);

    nd_set_loc(n, loc);
    nd_set_node_id(n, parser_get_node_id(p));
    return n;
}

static NODE *
nd_set_loc(NODE *nd, const YYLTYPE *loc)
{
    nd->nd_loc = *loc;
    nd_set_line(nd, loc->beg_pos.lineno);
    return nd;
}

#ifndef RIPPER
static enum node_type
nodetype(NODE *node)			/* for debug */
{
    return (enum node_type)nd_type(node);
}

static int
nodeline(NODE *node)
{
    return nd_line(node);
}

static NODE*
newline_node(NODE *node)
{
    if (node) {
	node = remove_begin(node);
	node->flags |= NODE_FL_NEWLINE;
    }
    return node;
}

static void
fixpos(NODE *node, NODE *orig)
{
    if (!node) return;
    if (!orig) return;
    nd_set_line(node, nd_line(orig));
}

static void
parser_warning(struct parser_params *p, NODE *node, const char *mesg)
{
    rb_compile_warning(p->ruby_sourcefile, nd_line(node), "%s", mesg);
}

static void
parser_warn(struct parser_params *p, NODE *node, const char *mesg)
{
    rb_compile_warn(p->ruby_sourcefile, nd_line(node), "%s", mesg);
}

static NODE*
block_append(struct parser_params *p, NODE *head, NODE *tail)
{
    NODE *end, *h = head, *nd;

    if (tail == 0) return head;

    if (h == 0) return tail;
    switch (nd_type(h)) {
      case NODE_LIT:
      case NODE_STR:
      case NODE_SELF:
      case NODE_TRUE:
      case NODE_FALSE:
      case NODE_NIL:
	parser_warning(p, h, "unused literal ignored");
	return tail;
      default:
	h = end = NEW_BLOCK(head, &head->nd_loc);
	end->nd_end = end;
	head = end;
	break;
      case NODE_BLOCK:
	end = h->nd_end;
	break;
    }

    nd = end->nd_head;
    switch (nd_type(nd)) {
      case NODE_RETURN:
      case NODE_BREAK:
      case NODE_NEXT:
      case NODE_REDO:
      case NODE_RETRY:
	if (RTEST(ruby_verbose)) {
	    parser_warning(p, tail, "statement not reached");
	}
	break;

      default:
	break;
    }

    if (nd_type(tail) != NODE_BLOCK) {
	tail = NEW_BLOCK(tail, &tail->nd_loc);
	tail->nd_end = tail;
    }
    end->nd_next = tail;
    h->nd_end = tail->nd_end;
    nd_set_last_loc(head, nd_last_loc(tail));
    return head;
}

/* append item to the list */
static NODE*
list_append(struct parser_params *p, NODE *list, NODE *item)
{
    NODE *last;

    if (list == 0) return NEW_LIST(item, &item->nd_loc);
    if (list->nd_next) {
	last = list->nd_next->nd_end;
    }
    else {
	last = list;
    }

    list->nd_alen += 1;
    last->nd_next = NEW_LIST(item, &item->nd_loc);
    list->nd_next->nd_end = last->nd_next;

    nd_set_last_loc(list, nd_last_loc(item));

    return list;
}

/* concat two lists */
static NODE*
list_concat(NODE *head, NODE *tail)
{
    NODE *last;

    if (head->nd_next) {
	last = head->nd_next->nd_end;
    }
    else {
	last = head;
    }

    head->nd_alen += tail->nd_alen;
    last->nd_next = tail;
    if (tail->nd_next) {
	head->nd_next->nd_end = tail->nd_next->nd_end;
    }
    else {
	head->nd_next->nd_end = tail;
    }

    nd_set_last_loc(head, nd_last_loc(tail));

    return head;
}

static int
literal_concat0(struct parser_params *p, VALUE head, VALUE tail)
{
    if (NIL_P(tail)) return 1;
    if (!rb_enc_compatible(head, tail)) {
	compile_error(p, "string literal encodings differ (%s / %s)",
		      rb_enc_name(rb_enc_get(head)),
		      rb_enc_name(rb_enc_get(tail)));
	rb_str_resize(head, 0);
	rb_str_resize(tail, 0);
	return 0;
    }
    rb_str_buf_append(head, tail);
    return 1;
}

static VALUE
string_literal_head(enum node_type htype, NODE *head)
{
    if (htype != NODE_DSTR) return Qfalse;
    if (head->nd_next) {
	head = head->nd_next->nd_end->nd_head;
	if (!head || nd_type(head) != NODE_STR) return Qfalse;
    }
    const VALUE lit = head->nd_lit;
    ASSUME(lit != Qfalse);
    return lit;
}

/* concat two string literals */
static NODE *
literal_concat(struct parser_params *p, NODE *head, NODE *tail, const YYLTYPE *loc)
{
    enum node_type htype;
    VALUE lit;

    if (!head) return tail;
    if (!tail) return head;

    htype = nd_type(head);
    if (htype == NODE_EVSTR) {
	head = new_dstr(p, head, loc);
	htype = NODE_DSTR;
    }
    if (p->heredoc_indent > 0) {
	switch (htype) {
	  case NODE_STR:
	    nd_set_type(head, NODE_DSTR);
	  case NODE_DSTR:
	    return list_append(p, head, tail);
	  default:
	    break;
	}
    }
    switch (nd_type(tail)) {
      case NODE_STR:
	if ((lit = string_literal_head(htype, head)) != Qfalse) {
	    htype = NODE_STR;
	}
	else {
	    lit = head->nd_lit;
	}
	if (htype == NODE_STR) {
	    if (!literal_concat0(p, lit, tail->nd_lit)) {
	      error:
		rb_discard_node(p, head);
		rb_discard_node(p, tail);
		return 0;
	    }
	    rb_discard_node(p, tail);
	}
	else {
	    list_append(p, head, tail);
	}
	break;

      case NODE_DSTR:
	if (htype == NODE_STR) {
	    if (!literal_concat0(p, head->nd_lit, tail->nd_lit))
		goto error;
	    tail->nd_lit = head->nd_lit;
	    rb_discard_node(p, head);
	    head = tail;
	}
	else if (NIL_P(tail->nd_lit)) {
	  append:
	    head->nd_alen += tail->nd_alen - 1;
	    if (!head->nd_next) {
		head->nd_next = tail->nd_next;
	    }
	    else if (tail->nd_next) {
		head->nd_next->nd_end->nd_next = tail->nd_next;
		head->nd_next->nd_end = tail->nd_next->nd_end;
	    }
	    rb_discard_node(p, tail);
	}
	else if ((lit = string_literal_head(htype, head)) != Qfalse) {
	    if (!literal_concat0(p, lit, tail->nd_lit))
		goto error;
	    tail->nd_lit = Qnil;
	    goto append;
	}
	else {
	    list_concat(head, NEW_NODE(NODE_LIST, NEW_STR(tail->nd_lit, loc), tail->nd_alen, tail->nd_next, loc));
	}
	break;

      case NODE_EVSTR:
	if (htype == NODE_STR) {
	    nd_set_type(head, NODE_DSTR);
	    head->nd_alen = 1;
	}
	list_append(p, head, tail);
	break;
    }
    return head;
}

static NODE *
evstr2dstr(struct parser_params *p, NODE *node)
{
    if (nd_type(node) == NODE_EVSTR) {
	node = new_dstr(p, node, &node->nd_loc);
    }
    return node;
}

static NODE *
new_evstr(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    NODE *head = node;

    if (node) {
	switch (nd_type(node)) {
	  case NODE_STR:
	    nd_set_type(node, NODE_DSTR);
	  case NODE_DSTR: case NODE_EVSTR:
	    return node;
	}
    }
    return NEW_EVSTR(head, loc);
}

static NODE *
new_dstr(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    VALUE lit = STR_NEW0();
    NODE *dstr = NEW_DSTR(lit, loc);
    RB_OBJ_WRITTEN(p->ast, Qnil, lit);
    return list_append(p, dstr, node);
}

static NODE *
call_bin_op(struct parser_params *p, NODE *recv, ID id, NODE *arg1,
		const YYLTYPE *op_loc, const YYLTYPE *loc)
{
    NODE *expr;
    value_expr(recv);
    value_expr(arg1);
    expr = NEW_OPCALL(recv, id, NEW_LIST(arg1, &arg1->nd_loc), loc);
    nd_set_line(expr, op_loc->beg_pos.lineno);
    return expr;
}

static NODE *
call_uni_op(struct parser_params *p, NODE *recv, ID id, const YYLTYPE *op_loc, const YYLTYPE *loc)
{
    NODE *opcall;
    value_expr(recv);
    opcall = NEW_OPCALL(recv, id, 0, loc);
    nd_set_line(opcall, op_loc->beg_pos.lineno);
    return opcall;
}

static NODE *
new_qcall(struct parser_params* p, ID atype, NODE *recv, ID mid, NODE *args, const YYLTYPE *op_loc, const YYLTYPE *loc)
{
    NODE *qcall = NEW_QCALL(atype, recv, mid, args, loc);
    nd_set_line(qcall, op_loc->beg_pos.lineno);
    return qcall;
}

static NODE*
new_command_qcall(struct parser_params* p, ID atype, NODE *recv, ID mid, NODE *args, NODE *block, const YYLTYPE *op_loc, const YYLTYPE *loc)
{
    NODE *ret;
    if (block) block_dup_check(p, args, block);
    ret = new_qcall(p, atype, recv, mid, args, op_loc, loc);
    if (block) ret = method_add_block(p, ret, block, loc);
    fixpos(ret, recv);
    return ret;
}

#define nd_once_body(node) (nd_type(node) == NODE_ONCE ? (node)->nd_body : node)
static NODE*
match_op(struct parser_params *p, NODE *node1, NODE *node2, const YYLTYPE *op_loc, const YYLTYPE *loc)
{
    NODE *n;
    int line = op_loc->beg_pos.lineno;

    value_expr(node1);
    value_expr(node2);
    if (node1 && (n = nd_once_body(node1)) != 0) {
	switch (nd_type(n)) {
	  case NODE_DREGX:
	    {
		NODE *match = NEW_MATCH2(node1, node2, loc);
		nd_set_line(match, line);
		return match;
	    }

	  case NODE_LIT:
	    if (RB_TYPE_P(n->nd_lit, T_REGEXP)) {
		const VALUE lit = n->nd_lit;
		NODE *match = NEW_MATCH2(node1, node2, loc);
		match->nd_args = reg_named_capture_assign(p, lit, loc);
		nd_set_line(match, line);
		return match;
	    }
	}
    }

    if (node2 && (n = nd_once_body(node2)) != 0) {
        NODE *match3;

	switch (nd_type(n)) {
	  case NODE_LIT:
	    if (!RB_TYPE_P(n->nd_lit, T_REGEXP)) break;
	    /* fallthru */
	  case NODE_DREGX:
	    match3 = NEW_MATCH3(node2, node1, loc);
	    return match3;
	}
    }

    n = NEW_CALL(node1, tMATCH, NEW_LIST(node2, &node2->nd_loc), loc);
    nd_set_line(n, line);
    return n;
}

# if WARN_PAST_SCOPE
static int
past_dvar_p(struct parser_params *p, ID id)
{
    struct vtable *past = p->lvtbl->past;
    while (past) {
	if (vtable_included(past, id)) return 1;
	past = past->prev;
    }
    return 0;
}
# endif

static int
numparam_nested_p(struct parser_params *p)
{
    struct local_vars *local = p->lvtbl;
    NODE *outer = local->numparam.outer;
    NODE *inner = local->numparam.inner;
    if (outer || inner) {
	NODE *used = outer ? outer : inner;
	compile_error(p, "numbered parameter is already used in\n"
		      "%s:%d: %s block here",
		      p->ruby_sourcefile, nd_line(used),
		      outer ? "outer" : "inner");
	parser_show_error_line(p, &used->nd_loc);
	return 1;
    }
    return 0;
}

static NODE*
gettable(struct parser_params *p, ID id, const YYLTYPE *loc)
{
    ID *vidp = NULL;
    NODE *node;
    switch (id) {
      case keyword_self:
	return NEW_SELF(loc);
      case keyword_nil:
	return NEW_NIL(loc);
      case keyword_true:
	return NEW_TRUE(loc);
      case keyword_false:
	return NEW_FALSE(loc);
      case keyword__FILE__:
	{
	    VALUE file = p->ruby_sourcefile_string;
	    if (NIL_P(file))
		file = rb_str_new(0, 0);
	    else
		file = rb_str_dup(file);
	    node = NEW_STR(file, loc);
            RB_OBJ_WRITTEN(p->ast, Qnil, file);
	}
	return node;
      case keyword__LINE__:
	return NEW_LIT(INT2FIX(p->tokline), loc);
      case keyword__ENCODING__:
        node = NEW_LIT(rb_enc_from_encoding(p->enc), loc);
        RB_OBJ_WRITTEN(p->ast, Qnil, node->nd_lit);
        return node;

    }
    switch (id_type(id)) {
      case ID_LOCAL:
	if (dyna_in_block(p) && dvar_defined_ref(p, id, &vidp)) {
	    if (NUMPARAM_ID_P(id) && numparam_nested_p(p)) return 0;
	    if (id == p->cur_arg) {
                compile_error(p, "circular argument reference - %"PRIsWARN, rb_id2str(id));
                return 0;
	    }
	    if (vidp) *vidp |= LVAR_USED;
	    node = NEW_DVAR(id, loc);
	    return node;
	}
	if (local_id_ref(p, id, &vidp)) {
	    if (id == p->cur_arg) {
                compile_error(p, "circular argument reference - %"PRIsWARN, rb_id2str(id));
                return 0;
	    }
	    if (vidp) *vidp |= LVAR_USED;
	    node = NEW_LVAR(id, loc);
	    return node;
	}
	if (dyna_in_block(p) && NUMPARAM_ID_P(id) &&
	    parser_numbered_param(p, NUMPARAM_ID_TO_IDX(id))) {
	    if (numparam_nested_p(p)) return 0;
	    node = NEW_DVAR(id, loc);
	    struct local_vars *local = p->lvtbl;
	    if (!local->numparam.current) local->numparam.current = node;
	    return node;
	}
# if WARN_PAST_SCOPE
	if (!p->ctxt.in_defined && RTEST(ruby_verbose) && past_dvar_p(p, id)) {
	    rb_warning1("possible reference to past scope - %"PRIsWARN, rb_id2str(id));
	}
# endif
	/* method call without arguments */
	return NEW_VCALL(id, loc);
      case ID_GLOBAL:
	return NEW_GVAR(id, loc);
      case ID_INSTANCE:
	return NEW_IVAR(id, loc);
      case ID_CONST:
	return NEW_CONST(id, loc);
      case ID_CLASS:
	return NEW_CVAR(id, loc);
    }
    compile_error(p, "identifier %"PRIsVALUE" is not valid to get", rb_id2str(id));
    return 0;
}

static NODE *
opt_arg_append(NODE *opt_list, NODE *opt)
{
    NODE *opts = opt_list;
    opts->nd_loc.end_pos = opt->nd_loc.end_pos;

    while (opts->nd_next) {
	opts = opts->nd_next;
	opts->nd_loc.end_pos = opt->nd_loc.end_pos;
    }
    opts->nd_next = opt;

    return opt_list;
}

static NODE *
kwd_append(NODE *kwlist, NODE *kw)
{
    if (kwlist) {
	NODE *kws = kwlist;
	kws->nd_loc.end_pos = kw->nd_loc.end_pos;
	while (kws->nd_next) {
	    kws = kws->nd_next;
	    kws->nd_loc.end_pos = kw->nd_loc.end_pos;
	}
	kws->nd_next = kw;
    }
    return kwlist;
}

static NODE *
new_defined(struct parser_params *p, NODE *expr, const YYLTYPE *loc)
{
    return NEW_DEFINED(remove_begin_all(expr), loc);
}

static NODE*
symbol_append(struct parser_params *p, NODE *symbols, NODE *symbol)
{
    enum node_type type = nd_type(symbol);
    switch (type) {
      case NODE_DSTR:
	nd_set_type(symbol, NODE_DSYM);
	break;
      case NODE_STR:
	nd_set_type(symbol, NODE_LIT);
	RB_OBJ_WRITTEN(p->ast, Qnil, symbol->nd_lit = rb_str_intern(symbol->nd_lit));
	break;
      default:
	compile_error(p, "unexpected node as symbol: %s", ruby_node_name(type));
    }
    return list_append(p, symbols, symbol);
}

static NODE *
new_regexp(struct parser_params *p, NODE *node, int options, const YYLTYPE *loc)
{
    NODE *list, *prev;
    VALUE lit;

    if (!node) {
	node = NEW_LIT(reg_compile(p, STR_NEW0(), options), loc);
	RB_OBJ_WRITTEN(p->ast, Qnil, node->nd_lit);
        return node;
    }
    switch (nd_type(node)) {
      case NODE_STR:
	{
	    VALUE src = node->nd_lit;
	    nd_set_type(node, NODE_LIT);
	    nd_set_loc(node, loc);
	    RB_OBJ_WRITTEN(p->ast, Qnil, node->nd_lit = reg_compile(p, src, options));
	}
	break;
      default:
	lit = STR_NEW0();
	node = NEW_NODE(NODE_DSTR, lit, 1, NEW_LIST(node, loc), loc);
        RB_OBJ_WRITTEN(p->ast, Qnil, lit);
	/* fall through */
      case NODE_DSTR:
	nd_set_type(node, NODE_DREGX);
	nd_set_loc(node, loc);
	node->nd_cflag = options & RE_OPTION_MASK;
	if (!NIL_P(node->nd_lit)) reg_fragment_check(p, node->nd_lit, options);
	for (list = (prev = node)->nd_next; list; list = list->nd_next) {
	    NODE *frag = list->nd_head;
	    enum node_type type = nd_type(frag);
	    if (type == NODE_STR || (type == NODE_DSTR && !frag->nd_next)) {
		VALUE tail = frag->nd_lit;
		if (reg_fragment_check(p, tail, options) && prev && !NIL_P(prev->nd_lit)) {
		    VALUE lit = prev == node ? prev->nd_lit : prev->nd_head->nd_lit;
		    if (!literal_concat0(p, lit, tail)) {
			return NEW_NIL(loc); /* dummy node on error */
		    }
		    rb_str_resize(tail, 0);
		    prev->nd_next = list->nd_next;
		    rb_discard_node(p, list->nd_head);
		    rb_discard_node(p, list);
		    list = prev;
		}
		else {
		    prev = list;
		}
	    }
	    else {
		prev = 0;
	    }
	}
	if (!node->nd_next) {
	    VALUE src = node->nd_lit;
	    nd_set_type(node, NODE_LIT);
	    RB_OBJ_WRITTEN(p->ast, Qnil, node->nd_lit = reg_compile(p, src, options));
	}
	if (options & RE_OPTION_ONCE) {
	    node = NEW_NODE(NODE_ONCE, 0, node, 0, loc);
	}
	break;
    }
    return node;
}

static NODE *
new_kw_arg(struct parser_params *p, NODE *k, const YYLTYPE *loc)
{
    if (!k) return 0;
    return NEW_KW_ARG(0, (k), loc);
}

static NODE *
new_xstring(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    if (!node) {
	VALUE lit = STR_NEW0();
	NODE *xstr = NEW_XSTR(lit, loc);
	RB_OBJ_WRITTEN(p->ast, Qnil, lit);
	return xstr;
    }
    switch (nd_type(node)) {
      case NODE_STR:
	nd_set_type(node, NODE_XSTR);
	nd_set_loc(node, loc);
	break;
      case NODE_DSTR:
	nd_set_type(node, NODE_DXSTR);
	nd_set_loc(node, loc);
	break;
      default:
	node = NEW_NODE(NODE_DXSTR, Qnil, 1, NEW_LIST(node, loc), loc);
	break;
    }
    return node;
}

static void
check_literal_when(struct parser_params *p, NODE *arg, const YYLTYPE *loc)
{
    VALUE lit;

    if (!arg || !p->case_labels) return;

    lit = rb_node_case_when_optimizable_literal(arg);
    if (lit == Qundef) return;
    if (nd_type(arg) == NODE_STR) {
	RB_OBJ_WRITTEN(p->ast, Qnil, arg->nd_lit = lit);
    }

    if (NIL_P(p->case_labels)) {
	p->case_labels = rb_obj_hide(rb_hash_new());
    }
    else {
	VALUE line = rb_hash_lookup(p->case_labels, lit);
	if (!NIL_P(line)) {
	    rb_warning1("duplicated `when' clause with line %d is ignored",
			WARN_IVAL(line));
	    return;
	}
    }
    rb_hash_aset(p->case_labels, lit, INT2NUM(p->ruby_sourceline));
}

#else  /* !RIPPER */
static int
id_is_var(struct parser_params *p, ID id)
{
    if (is_notop_id(id)) {
	switch (id & ID_SCOPE_MASK) {
	  case ID_GLOBAL: case ID_INSTANCE: case ID_CONST: case ID_CLASS:
	    return 1;
	  case ID_LOCAL:
	    if (dyna_in_block(p)) {
		if (NUMPARAM_ID_P(id) || dvar_defined(p, id)) return 1;
	    }
	    if (local_id(p, id)) return 1;
	    /* method call without arguments */
	    return 0;
	}
    }
    compile_error(p, "identifier %"PRIsVALUE" is not valid to get", rb_id2str(id));
    return 0;
}

static VALUE
new_regexp(struct parser_params *p, VALUE re, VALUE opt, const YYLTYPE *loc)
{
    VALUE src = 0, err;
    int options = 0;
    if (ripper_is_node_yylval(re)) {
	src = RNODE(re)->nd_cval;
	re = RNODE(re)->nd_rval;
    }
    if (ripper_is_node_yylval(opt)) {
	options = (int)RNODE(opt)->nd_tag;
	opt = RNODE(opt)->nd_rval;
    }
    if (src && NIL_P(parser_reg_compile(p, src, options, &err))) {
	compile_error(p, "%"PRIsVALUE, err);
    }
    return dispatch2(regexp_literal, re, opt);
}
#endif /* !RIPPER */


#ifndef RIPPER
static const char rb_parser_lex_state_names[][8] = {
    "BEG",    "END",    "ENDARG", "ENDFN",  "ARG",
    "CMDARG", "MID",    "FNAME",  "DOT",    "CLASS",
    "LABEL",  "LABELED","FITEM",
};

static VALUE
append_lex_state_name(enum lex_state_e state, VALUE buf)
{
    int i, sep = 0;
    unsigned int mask = 1;
    static const char none[] = "NONE";

    for (i = 0; i < EXPR_MAX_STATE; ++i, mask <<= 1) {
	if ((unsigned)state & mask) {
	    if (sep) {
		rb_str_cat(buf, "|", 1);
	    }
	    sep = 1;
	    rb_str_cat_cstr(buf, rb_parser_lex_state_names[i]);
	}
    }
    if (!sep) {
	rb_str_cat(buf, none, sizeof(none)-1);
    }
    return buf;
}

static void
flush_debug_buffer(struct parser_params *p, VALUE out, VALUE str)
{
    VALUE mesg = p->debug_buffer;

    if (!NIL_P(mesg) && RSTRING_LEN(mesg)) {
	p->debug_buffer = Qnil;
	rb_io_puts(1, &mesg, out);
    }
    if (!NIL_P(str) && RSTRING_LEN(str)) {
	rb_io_write(p->debug_output, str);
    }
}

enum lex_state_e
rb_parser_trace_lex_state(struct parser_params *p, enum lex_state_e from,
			  enum lex_state_e to, int line)
{
    VALUE mesg;
    mesg = rb_str_new_cstr("lex_state: ");
    append_lex_state_name(from, mesg);
    rb_str_cat_cstr(mesg, " -> ");
    append_lex_state_name(to, mesg);
    rb_str_catf(mesg, " at line %d\n", line);
    flush_debug_buffer(p, p->debug_output, mesg);
    return to;
}

VALUE
rb_parser_lex_state_name(enum lex_state_e state)
{
    return rb_fstring(append_lex_state_name(state, rb_str_new(0, 0)));
}

static void
append_bitstack_value(stack_type stack, VALUE mesg)
{
    if (stack == 0) {
	rb_str_cat_cstr(mesg, "0");
    }
    else {
	stack_type mask = (stack_type)1U << (CHAR_BIT * sizeof(stack_type) - 1);
	for (; mask && !(stack & mask); mask >>= 1) continue;
	for (; mask; mask >>= 1) rb_str_cat(mesg, stack & mask ? "1" : "0", 1);
    }
}

void
rb_parser_show_bitstack(struct parser_params *p, stack_type stack,
			const char *name, int line)
{
    VALUE mesg = rb_sprintf("%s: ", name);
    append_bitstack_value(stack, mesg);
    rb_str_catf(mesg, " at line %d\n", line);
    flush_debug_buffer(p, p->debug_output, mesg);
}

void
rb_parser_fatal(struct parser_params *p, const char *fmt, ...)
{
    va_list ap;
    VALUE mesg = rb_str_new_cstr("internal parser error: ");

    va_start(ap, fmt);
    rb_str_vcatf(mesg, fmt, ap);
    va_end(ap);
    parser_yyerror(p, NULL, RSTRING_PTR(mesg));
    RB_GC_GUARD(mesg);

    mesg = rb_str_new(0, 0);
    append_lex_state_name(p->lex.state, mesg);
    compile_error(p, "lex.state: %"PRIsVALUE, mesg);
    rb_str_resize(mesg, 0);
    append_bitstack_value(p->cond_stack, mesg);
    compile_error(p, "cond_stack: %"PRIsVALUE, mesg);
    rb_str_resize(mesg, 0);
    append_bitstack_value(p->cmdarg_stack, mesg);
    compile_error(p, "cmdarg_stack: %"PRIsVALUE, mesg);
    if (p->debug_output == rb_ractor_stdout())
	p->debug_output = rb_ractor_stderr();
    p->debug = TRUE;
}

YYLTYPE *
rb_parser_set_location_from_strterm_heredoc(struct parser_params *p, rb_strterm_heredoc_t *here, YYLTYPE *yylloc)
{
    int sourceline = here->sourceline;
    int beg_pos = (int)here->offset - here->quote
	- (rb_strlen_lit("<<-") - !(here->func & STR_FUNC_INDENT));
    int end_pos = (int)here->offset + here->length + here->quote;

    yylloc->beg_pos.lineno = sourceline;
    yylloc->beg_pos.column = beg_pos;
    yylloc->end_pos.lineno = sourceline;
    yylloc->end_pos.column = end_pos;
    return yylloc;
}

YYLTYPE *
rb_parser_set_location_of_none(struct parser_params *p, YYLTYPE *yylloc)
{
    yylloc->beg_pos.lineno = p->ruby_sourceline;
    yylloc->beg_pos.column = (int)(p->lex.ptok - p->lex.pbeg);
    yylloc->end_pos.lineno = p->ruby_sourceline;
    yylloc->end_pos.column = (int)(p->lex.ptok - p->lex.pbeg);
    return yylloc;
}

YYLTYPE *
rb_parser_set_location(struct parser_params *p, YYLTYPE *yylloc)
{
    yylloc->beg_pos.lineno = p->ruby_sourceline;
    yylloc->beg_pos.column = (int)(p->lex.ptok - p->lex.pbeg);
    yylloc->end_pos.lineno = p->ruby_sourceline;
    yylloc->end_pos.column = (int)(p->lex.pcur - p->lex.pbeg);
    return yylloc;
}
#endif /* !RIPPER */

static void
parser_token_value_print(struct parser_params *p, enum yytokentype type, const YYSTYPE *valp)
{
    VALUE v;

    switch (type) {
      case tIDENTIFIER: case tFID: case tGVAR: case tIVAR:
      case tCONSTANT: case tCVAR: case tLABEL: case tOP_ASGN:
#ifndef RIPPER
	v = rb_id2str(valp->id);
#else
	v = valp->node->nd_rval;
#endif
	rb_parser_printf(p, "%"PRIsVALUE, v);
	break;
      case tINTEGER: case tFLOAT: case tRATIONAL: case tIMAGINARY:
      case tSTRING_CONTENT: case tCHAR:
#ifndef RIPPER
	v = valp->node->nd_lit;
#else
	v = valp->val;
#endif
	rb_parser_printf(p, "%+"PRIsVALUE, v);
	break;
      case tNTH_REF:
#ifndef RIPPER
	rb_parser_printf(p, "$%ld", valp->node->nd_nth);
#else
	rb_parser_printf(p, "%"PRIsVALUE, valp->val);
#endif
	break;
      case tBACK_REF:
#ifndef RIPPER
	rb_parser_printf(p, "$%c", (int)valp->node->nd_nth);
#else
	rb_parser_printf(p, "%"PRIsVALUE, valp->val);
#endif
	break;
      default:
	break;
    }
}

static int
assignable0(struct parser_params *p, ID id, const char **err)
{
    if (!id) return -1;
    switch (id) {
      case keyword_self:
	*err = "Can't change the value of self";
	return -1;
      case keyword_nil:
	*err = "Can't assign to nil";
	return -1;
      case keyword_true:
	*err = "Can't assign to true";
	return -1;
      case keyword_false:
	*err = "Can't assign to false";
	return -1;
      case keyword__FILE__:
	*err = "Can't assign to __FILE__";
	return -1;
      case keyword__LINE__:
	*err = "Can't assign to __LINE__";
	return -1;
      case keyword__ENCODING__:
	*err = "Can't assign to __ENCODING__";
	return -1;
    }
    switch (id_type(id)) {
      case ID_LOCAL:
	if (dyna_in_block(p)) {
	    if (p->max_numparam > NO_PARAM && NUMPARAM_ID_P(id)) {
		compile_error(p, "Can't assign to numbered parameter _%d",
			      NUMPARAM_ID_TO_IDX(id));
		return -1;
	    }
	    if (dvar_curr(p, id)) return NODE_DASGN_CURR;
	    if (dvar_defined(p, id)) return NODE_DASGN;
	    if (local_id(p, id)) return NODE_LASGN;
	    dyna_var(p, id);
	    return NODE_DASGN_CURR;
	}
	else {
	    if (!local_id(p, id)) local_var(p, id);
	    return NODE_LASGN;
	}
	break;
      case ID_GLOBAL: return NODE_GASGN;
      case ID_INSTANCE: return NODE_IASGN;
      case ID_CONST:
	if (!p->ctxt.in_def) return NODE_CDECL;
	*err = "dynamic constant assignment";
	return -1;
      case ID_CLASS: return NODE_CVASGN;
      default:
	compile_error(p, "identifier %"PRIsVALUE" is not valid to set", rb_id2str(id));
    }
    return -1;
}

#ifndef RIPPER
static NODE*
assignable(struct parser_params *p, ID id, NODE *val, const YYLTYPE *loc)
{
    const char *err = 0;
    int node_type = assignable0(p, id, &err);
    switch (node_type) {
      case NODE_DASGN_CURR: return NEW_DASGN_CURR(id, val, loc);
      case NODE_DASGN: return NEW_DASGN(id, val, loc);
      case NODE_LASGN: return NEW_LASGN(id, val, loc);
      case NODE_GASGN: return NEW_GASGN(id, val, loc);
      case NODE_IASGN: return NEW_IASGN(id, val, loc);
      case NODE_CDECL: return NEW_CDECL(id, val, 0, loc);
      case NODE_CVASGN: return NEW_CVASGN(id, val, loc);
    }
    if (err) yyerror1(loc, err);
    return NEW_BEGIN(0, loc);
}
#else
static VALUE
assignable(struct parser_params *p, VALUE lhs)
{
    const char *err = 0;
    assignable0(p, get_id(lhs), &err);
    if (err) lhs = assign_error(p, err, lhs);
    return lhs;
}
#endif

static int
is_private_local_id(ID name)
{
    VALUE s;
    if (name == idUScore) return 1;
    if (!is_local_id(name)) return 0;
    s = rb_id2str(name);
    if (!s) return 0;
    return RSTRING_PTR(s)[0] == '_';
}

static int
shadowing_lvar_0(struct parser_params *p, ID name)
{
    if (is_private_local_id(name)) return 1;
    if (dyna_in_block(p)) {
	if (dvar_curr(p, name)) {
	    yyerror0("duplicated argument name");
	}
	else if (dvar_defined(p, name) || local_id(p, name)) {
	    vtable_add(p->lvtbl->vars, name);
	    if (p->lvtbl->used) {
		vtable_add(p->lvtbl->used, (ID)p->ruby_sourceline | LVAR_USED);
	    }
	    return 0;
	}
    }
    else {
	if (local_id(p, name)) {
	    yyerror0("duplicated argument name");
	}
    }
    return 1;
}

static ID
shadowing_lvar(struct parser_params *p, ID name)
{
    shadowing_lvar_0(p, name);
    return name;
}

static void
new_bv(struct parser_params *p, ID name)
{
    if (!name) return;
    if (!is_local_id(name)) {
	compile_error(p, "invalid local variable - %"PRIsVALUE,
		      rb_id2str(name));
	return;
    }
    if (!shadowing_lvar_0(p, name)) return;
    dyna_var(p, name);
}

#ifndef RIPPER
static NODE *
aryset(struct parser_params *p, NODE *recv, NODE *idx, const YYLTYPE *loc)
{
    return NEW_ATTRASGN(recv, tASET, idx, loc);
}

static void
block_dup_check(struct parser_params *p, NODE *node1, NODE *node2)
{
    if (node2 && node1 && nd_type(node1) == NODE_BLOCK_PASS) {
	compile_error(p, "both block arg and actual block given");
    }
}

static NODE *
attrset(struct parser_params *p, NODE *recv, ID atype, ID id, const YYLTYPE *loc)
{
    if (!CALL_Q_P(atype)) id = rb_id_attrset(id);
    return NEW_ATTRASGN(recv, id, 0, loc);
}

static void
rb_backref_error(struct parser_params *p, NODE *node)
{
    switch (nd_type(node)) {
      case NODE_NTH_REF:
	compile_error(p, "Can't set variable $%ld", node->nd_nth);
	break;
      case NODE_BACK_REF:
	compile_error(p, "Can't set variable $%c", (int)node->nd_nth);
	break;
    }
}
#else
static VALUE
backref_error(struct parser_params *p, NODE *ref, VALUE expr)
{
    VALUE mesg = rb_str_new_cstr("Can't set variable ");
    rb_str_append(mesg, ref->nd_cval);
    return dispatch2(assign_error, mesg, expr);
}
#endif

#ifndef RIPPER
static NODE *
arg_append(struct parser_params *p, NODE *node1, NODE *node2, const YYLTYPE *loc)
{
    if (!node1) return NEW_LIST(node2, &node2->nd_loc);
    switch (nd_type(node1))  {
      case NODE_LIST:
	return list_append(p, node1, node2);
      case NODE_BLOCK_PASS:
	node1->nd_head = arg_append(p, node1->nd_head, node2, loc);
	node1->nd_loc.end_pos = node1->nd_head->nd_loc.end_pos;
	return node1;
      case NODE_ARGSPUSH:
	node1->nd_body = list_append(p, NEW_LIST(node1->nd_body, &node1->nd_body->nd_loc), node2);
	node1->nd_loc.end_pos = node1->nd_body->nd_loc.end_pos;
	nd_set_type(node1, NODE_ARGSCAT);
	return node1;
      case NODE_ARGSCAT:
        if (nd_type(node1->nd_body) != NODE_LIST) break;
        node1->nd_body = list_append(p, node1->nd_body, node2);
        node1->nd_loc.end_pos = node1->nd_body->nd_loc.end_pos;
        return node1;
    }
    return NEW_ARGSPUSH(node1, node2, loc);
}

static NODE *
arg_concat(struct parser_params *p, NODE *node1, NODE *node2, const YYLTYPE *loc)
{
    if (!node2) return node1;
    switch (nd_type(node1)) {
      case NODE_BLOCK_PASS:
	if (node1->nd_head)
	    node1->nd_head = arg_concat(p, node1->nd_head, node2, loc);
	else
	    node1->nd_head = NEW_LIST(node2, loc);
	return node1;
      case NODE_ARGSPUSH:
	if (nd_type(node2) != NODE_LIST) break;
	node1->nd_body = list_concat(NEW_LIST(node1->nd_body, loc), node2);
	nd_set_type(node1, NODE_ARGSCAT);
	return node1;
      case NODE_ARGSCAT:
	if (nd_type(node2) != NODE_LIST ||
	    nd_type(node1->nd_body) != NODE_LIST) break;
	node1->nd_body = list_concat(node1->nd_body, node2);
	return node1;
    }
    return NEW_ARGSCAT(node1, node2, loc);
}

static NODE *
last_arg_append(struct parser_params *p, NODE *args, NODE *last_arg, const YYLTYPE *loc)
{
    NODE *n1;
    if ((n1 = splat_array(args)) != 0) {
	return list_append(p, n1, last_arg);
    }
    return arg_append(p, args, last_arg, loc);
}

static NODE *
rest_arg_append(struct parser_params *p, NODE *args, NODE *rest_arg, const YYLTYPE *loc)
{
    NODE *n1;
    if ((nd_type(rest_arg) == NODE_LIST) && (n1 = splat_array(args)) != 0) {
	return list_concat(n1, rest_arg);
    }
    return arg_concat(p, args, rest_arg, loc);
}

static NODE *
splat_array(NODE* node)
{
    if (nd_type(node) == NODE_SPLAT) node = node->nd_head;
    if (nd_type(node) == NODE_LIST) return node;
    return 0;
}

static void
mark_lvar_used(struct parser_params *p, NODE *rhs)
{
    ID *vidp = NULL;
    if (!rhs) return;
    switch (nd_type(rhs)) {
      case NODE_LASGN:
	if (local_id_ref(p, rhs->nd_vid, &vidp)) {
	    if (vidp) *vidp |= LVAR_USED;
	}
	break;
      case NODE_DASGN:
      case NODE_DASGN_CURR:
	if (dvar_defined_ref(p, rhs->nd_vid, &vidp)) {
	    if (vidp) *vidp |= LVAR_USED;
	}
	break;
#if 0
      case NODE_MASGN:
	for (rhs = rhs->nd_head; rhs; rhs = rhs->nd_next) {
	    mark_lvar_used(p, rhs->nd_head);
	}
	break;
#endif
    }
}

static NODE *
const_decl_path(struct parser_params *p, NODE **dest)
{
    NODE *n = *dest;
    if (nd_type(n) != NODE_CALL) {
	const YYLTYPE *loc = &n->nd_loc;
	VALUE path;
	if (n->nd_vid) {
	     path = rb_id2str(n->nd_vid);
	}
	else {
	    n = n->nd_else;
	    path = rb_ary_new();
	    for (; n && nd_type(n) == NODE_COLON2; n = n->nd_head) {
		rb_ary_push(path, rb_id2str(n->nd_mid));
	    }
	    if (n && nd_type(n) == NODE_CONST) {
		// Const::Name
		rb_ary_push(path, rb_id2str(n->nd_vid));
	    }
	    else if (n && nd_type(n) == NODE_COLON3) {
		// ::Const::Name
		rb_ary_push(path, rb_str_new(0, 0));
	    }
	    else {
		// expression::Name
		rb_ary_push(path, rb_str_new_cstr("..."));
	    }
	    path = rb_ary_join(rb_ary_reverse(path), rb_str_new_cstr("::"));
	    path = rb_fstring(path);
	}
	*dest = n = NEW_LIT(path, loc);
    }
    return n;
}

extern VALUE rb_mRubyVMFrozenCore;

static NODE *
make_shareable_node(struct parser_params *p, NODE *value, bool copy, const YYLTYPE *loc)
{
    NODE *fcore = NEW_LIT(rb_mRubyVMFrozenCore, loc);

    if (copy) {
        return NEW_CALL(fcore, rb_intern("make_shareable_copy"),
                        NEW_LIST(value, loc), loc);
    }
    else {
        return NEW_CALL(fcore, rb_intern("make_shareable"),
                        NEW_LIST(value, loc), loc);
    }
}

static NODE *
ensure_shareable_node(struct parser_params *p, NODE **dest, NODE *value, const YYLTYPE *loc)
{
    NODE *fcore = NEW_LIT(rb_mRubyVMFrozenCore, loc);
    NODE *args = NEW_LIST(value, loc);
    args = list_append(p, args, const_decl_path(p, dest));
    return NEW_CALL(fcore, rb_intern("ensure_shareable"), args, loc);
}

static int is_static_content(NODE *node);

static VALUE
shareable_literal_value(NODE *node)
{
    if (!node) return Qnil;
    enum node_type type = nd_type(node);
    switch (type) {
      case NODE_TRUE:
	return Qtrue;
      case NODE_FALSE:
	return Qfalse;
      case NODE_NIL:
	return Qnil;
      case NODE_LIT:
	return node->nd_lit;
      default:
	return Qundef;
    }
}

#ifndef SHAREABLE_BARE_EXPRESSION
#define SHAREABLE_BARE_EXPRESSION 1
#endif

static NODE *
shareable_literal_constant(struct parser_params *p, enum shareability shareable,
			   NODE **dest, NODE *value, const YYLTYPE *loc, size_t level)
{
# define shareable_literal_constant_next(n) \
    shareable_literal_constant(p, shareable, dest, (n), &(n)->nd_loc, level+1)
    VALUE lit = Qnil;

    if (!value) return 0;
    enum node_type type = nd_type(value);
    switch (type) {
      case NODE_TRUE:
      case NODE_FALSE:
      case NODE_NIL:
      case NODE_LIT:
	return value;

      case NODE_DSTR:
	if (shareable == shareable_literal) {
	    value = NEW_CALL(value, idUMinus, 0, loc);
	}
	return value;

      case NODE_STR:
	lit = rb_fstring(value->nd_lit);
	nd_set_type(value, NODE_LIT);
	RB_OBJ_WRITE(p->ast, &value->nd_lit, lit);
	return value;

      case NODE_ZLIST:
	lit = rb_ary_new();
	OBJ_FREEZE_RAW(lit);
	return NEW_LIT(lit, loc);

      case NODE_LIST:
	lit = rb_ary_new();
	for (NODE *n = value; n; n = n->nd_next) {
	    NODE *elt = n->nd_head;
	    if (elt) {
		elt = shareable_literal_constant_next(elt);
		if (elt) {
		    n->nd_head = elt;
		}
		else if (RTEST(lit)) {
		    rb_ary_clear(lit);
		    lit = Qfalse;
		}
	    }
	    if (RTEST(lit)) {
		VALUE e = shareable_literal_value(elt);
		if (e != Qundef) {
		    rb_ary_push(lit, e);
		}
		else {
		    rb_ary_clear(lit);
		    lit = Qnil;	/* make shareable at runtime */
		}
	    }
	}
	break;

      case NODE_HASH:
	if (!value->nd_brace) return 0;
	lit = rb_hash_new();
	for (NODE *n = value->nd_head; n; n = n->nd_next->nd_next) {
	    NODE *key = n->nd_head;
	    NODE *val = n->nd_next->nd_head;
	    if (key) {
		key = shareable_literal_constant_next(key);
		if (key) {
		    n->nd_head = key;
		}
		else if (RTEST(lit)) {
		    rb_hash_clear(lit);
		    lit = Qfalse;
		}
	    }
	    if (val) {
		val = shareable_literal_constant_next(val);
		if (val) {
		    n->nd_next->nd_head = val;
		}
		else if (RTEST(lit)) {
		    rb_hash_clear(lit);
		    lit = Qfalse;
		}
	    }
	    if (RTEST(lit)) {
		VALUE k = shareable_literal_value(key);
		VALUE v = shareable_literal_value(val);
		if (k != Qundef && v != Qundef) {
		    rb_hash_aset(lit, k, v);
		}
		else {
		    rb_hash_clear(lit);
		    lit = Qnil;	/* make shareable at runtime */
		}
	    }
	}
	break;

      default:
	if (shareable == shareable_literal &&
	    (SHAREABLE_BARE_EXPRESSION || level > 0)) {
	    return ensure_shareable_node(p, dest, value, loc);
	}
	return 0;
    }

    /* Array or Hash */
    if (!lit) return 0;
    if (NIL_P(lit)) {
	// if shareable_literal, all elements should have been ensured
	// as shareable
	value = make_shareable_node(p, value, false, loc);
    }
    else {
	value = NEW_LIT(rb_ractor_make_shareable(lit), loc);
    }

    return value;
# undef shareable_literal_constant_next
}

static NODE *
shareable_constant_value(struct parser_params *p, enum shareability shareable,
			 NODE *lhs, NODE *value, const YYLTYPE *loc)
{
    if (!value) return 0;
    switch (shareable) {
      case shareable_none:
	return value;

      case shareable_literal:
	{
	    NODE *lit = shareable_literal_constant(p, shareable, &lhs, value, loc, 0);
	    if (lit) return lit;
	    return value;
	}
	break;

      case shareable_copy:
      case shareable_everything:
	{
	    NODE *lit = shareable_literal_constant(p, shareable, &lhs, value, loc, 0);
	    if (lit) return lit;
	    return make_shareable_node(p, value, shareable == shareable_copy, loc);
	}
	break;

      default:
	UNREACHABLE_RETURN(0);
    }
}

static NODE *
node_assign(struct parser_params *p, NODE *lhs, NODE *rhs, struct lex_context ctxt, const YYLTYPE *loc)
{
    if (!lhs) return 0;

    switch (nd_type(lhs)) {
      case NODE_CDECL:
	rhs = shareable_constant_value(p, ctxt.shareable_constant_value, lhs, rhs, loc);
	/* fallthru */

      case NODE_GASGN:
      case NODE_IASGN:
      case NODE_LASGN:
      case NODE_DASGN:
      case NODE_DASGN_CURR:
      case NODE_MASGN:
      case NODE_CVASGN:
	lhs->nd_value = rhs;
	nd_set_loc(lhs, loc);
	break;

      case NODE_ATTRASGN:
	lhs->nd_args = arg_append(p, lhs->nd_args, rhs, loc);
	nd_set_loc(lhs, loc);
	break;

      default:
	/* should not happen */
	break;
    }

    return lhs;
}

static NODE *
value_expr_check(struct parser_params *p, NODE *node)
{
    NODE *void_node = 0, *vn;

    if (!node) {
	rb_warning0("empty expression");
    }
    while (node) {
	switch (nd_type(node)) {
	  case NODE_RETURN:
	  case NODE_BREAK:
	  case NODE_NEXT:
	  case NODE_REDO:
	  case NODE_RETRY:
	    return void_node ? void_node : node;

	  case NODE_CASE3:
	    if (!node->nd_body || nd_type(node->nd_body) != NODE_IN) {
		compile_error(p, "unexpected node");
		return NULL;
	    }
	    if (node->nd_body->nd_body) {
		return NULL;
	    }
	    /* single line pattern matching */
	    return void_node ? void_node : node;

	  case NODE_BLOCK:
	    while (node->nd_next) {
		node = node->nd_next;
	    }
	    node = node->nd_head;
	    break;

	  case NODE_BEGIN:
	    node = node->nd_body;
	    break;

	  case NODE_IF:
	  case NODE_UNLESS:
	    if (!node->nd_body) {
		return NULL;
	    }
	    else if (!node->nd_else) {
		return NULL;
	    }
	    vn = value_expr_check(p, node->nd_body);
	    if (!vn) return NULL;
	    if (!void_node) void_node = vn;
	    node = node->nd_else;
	    break;

	  case NODE_AND:
	  case NODE_OR:
	    node = node->nd_1st;
	    break;

	  case NODE_LASGN:
	  case NODE_DASGN:
	  case NODE_DASGN_CURR:
	  case NODE_MASGN:
	    mark_lvar_used(p, node);
	    return NULL;

	  default:
	    return NULL;
	}
    }

    return NULL;
}

static int
value_expr_gen(struct parser_params *p, NODE *node)
{
    NODE *void_node = value_expr_check(p, node);
    if (void_node) {
	yyerror1(&void_node->nd_loc, "void value expression");
	/* or "control never reach"? */
	return FALSE;
    }
    return TRUE;
}
static void
void_expr(struct parser_params *p, NODE *node)
{
    const char *useless = 0;

    if (!RTEST(ruby_verbose)) return;

    if (!node || !(node = nd_once_body(node))) return;
    switch (nd_type(node)) {
      case NODE_OPCALL:
	switch (node->nd_mid) {
	  case '+':
	  case '-':
	  case '*':
	  case '/':
	  case '%':
	  case tPOW:
	  case tUPLUS:
	  case tUMINUS:
	  case '|':
	  case '^':
	  case '&':
	  case tCMP:
	  case '>':
	  case tGEQ:
	  case '<':
	  case tLEQ:
	  case tEQ:
	  case tNEQ:
	    useless = rb_id2name(node->nd_mid);
	    break;
	}
	break;

      case NODE_LVAR:
      case NODE_DVAR:
      case NODE_GVAR:
      case NODE_IVAR:
      case NODE_CVAR:
      case NODE_NTH_REF:
      case NODE_BACK_REF:
	useless = "a variable";
	break;
      case NODE_CONST:
	useless = "a constant";
	break;
      case NODE_LIT:
      case NODE_STR:
      case NODE_DSTR:
      case NODE_DREGX:
	useless = "a literal";
	break;
      case NODE_COLON2:
      case NODE_COLON3:
	useless = "::";
	break;
      case NODE_DOT2:
	useless = "..";
	break;
      case NODE_DOT3:
	useless = "...";
	break;
      case NODE_SELF:
	useless = "self";
	break;
      case NODE_NIL:
	useless = "nil";
	break;
      case NODE_TRUE:
	useless = "true";
	break;
      case NODE_FALSE:
	useless = "false";
	break;
      case NODE_DEFINED:
	useless = "defined?";
	break;
    }

    if (useless) {
	rb_warn1L(nd_line(node), "possibly useless use of %s in void context", WARN_S(useless));
    }
}

static NODE *
void_stmts(struct parser_params *p, NODE *node)
{
    NODE *const n = node;
    if (!RTEST(ruby_verbose)) return n;
    if (!node) return n;
    if (nd_type(node) != NODE_BLOCK) return n;

    while (node->nd_next) {
	void_expr(p, node->nd_head);
	node = node->nd_next;
    }
    return n;
}

static NODE *
remove_begin(NODE *node)
{
    NODE **n = &node, *n1 = node;
    while (n1 && nd_type(n1) == NODE_BEGIN && n1->nd_body) {
	*n = n1 = n1->nd_body;
    }
    return node;
}

static NODE *
remove_begin_all(NODE *node)
{
    NODE **n = &node, *n1 = node;
    while (n1 && nd_type(n1) == NODE_BEGIN) {
	*n = n1 = n1->nd_body;
    }
    return node;
}

static void
reduce_nodes(struct parser_params *p, NODE **body)
{
    NODE *node = *body;

    if (!node) {
	*body = NEW_NIL(&NULL_LOC);
	return;
    }
#define subnodes(n1, n2) \
    ((!node->n1) ? (node->n2 ? (body = &node->n2, 1) : 0) : \
     (!node->n2) ? (body = &node->n1, 1) : \
     (reduce_nodes(p, &node->n1), body = &node->n2, 1))

    while (node) {
	int newline = (int)(node->flags & NODE_FL_NEWLINE);
	switch (nd_type(node)) {
	  end:
	  case NODE_NIL:
	    *body = 0;
	    return;
	  case NODE_RETURN:
	    *body = node = node->nd_stts;
	    if (newline && node) node->flags |= NODE_FL_NEWLINE;
	    continue;
	  case NODE_BEGIN:
	    *body = node = node->nd_body;
	    if (newline && node) node->flags |= NODE_FL_NEWLINE;
	    continue;
	  case NODE_BLOCK:
	    body = &node->nd_end->nd_head;
	    break;
	  case NODE_IF:
	  case NODE_UNLESS:
	    if (subnodes(nd_body, nd_else)) break;
	    return;
	  case NODE_CASE:
	    body = &node->nd_body;
	    break;
	  case NODE_WHEN:
	    if (!subnodes(nd_body, nd_next)) goto end;
	    break;
	  case NODE_ENSURE:
	    if (!subnodes(nd_head, nd_resq)) goto end;
	    break;
	  case NODE_RESCUE:
	    if (node->nd_else) {
		body = &node->nd_resq;
		break;
	    }
	    if (!subnodes(nd_head, nd_resq)) goto end;
	    break;
	  default:
	    return;
	}
	node = *body;
	if (newline && node) node->flags |= NODE_FL_NEWLINE;
    }

#undef subnodes
}

static int
is_static_content(NODE *node)
{
    if (!node) return 1;
    switch (nd_type(node)) {
      case NODE_HASH:
	if (!(node = node->nd_head)) break;
      case NODE_LIST:
	do {
	    if (!is_static_content(node->nd_head)) return 0;
	} while ((node = node->nd_next) != 0);
      case NODE_LIT:
      case NODE_STR:
      case NODE_NIL:
      case NODE_TRUE:
      case NODE_FALSE:
      case NODE_ZLIST:
	break;
      default:
	return 0;
    }
    return 1;
}

static int
assign_in_cond(struct parser_params *p, NODE *node)
{
    switch (nd_type(node)) {
      case NODE_MASGN:
      case NODE_LASGN:
      case NODE_DASGN:
      case NODE_DASGN_CURR:
      case NODE_GASGN:
      case NODE_IASGN:
	break;

      default:
	return 0;
    }

    if (!node->nd_value) return 1;
    if (is_static_content(node->nd_value)) {
	/* reports always */
	parser_warn(p, node->nd_value, "found `= literal' in conditional, should be ==");
    }
    return 1;
}

enum cond_type {
    COND_IN_OP,
    COND_IN_COND,
    COND_IN_FF
};

#define SWITCH_BY_COND_TYPE(t, w, arg) \
    switch (t) { \
      case COND_IN_OP: break; \
      case COND_IN_COND: rb_##w##0(arg "literal in condition"); break; \
      case COND_IN_FF: rb_##w##0(arg "literal in flip-flop"); break; \
    }

static NODE *cond0(struct parser_params*,NODE*,enum cond_type,const YYLTYPE*);

static NODE*
range_op(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    enum node_type type;

    if (node == 0) return 0;

    type = nd_type(node);
    value_expr(node);
    if (type == NODE_LIT && FIXNUM_P(node->nd_lit)) {
	if (!e_option_supplied(p)) parser_warn(p, node, "integer literal in flip-flop");
	ID lineno = rb_intern("$.");
	return NEW_CALL(node, tEQ, NEW_LIST(NEW_GVAR(lineno, loc), loc), loc);
    }
    return cond0(p, node, COND_IN_FF, loc);
}

static NODE*
cond0(struct parser_params *p, NODE *node, enum cond_type type, const YYLTYPE *loc)
{
    if (node == 0) return 0;
    if (!(node = nd_once_body(node))) return 0;
    assign_in_cond(p, node);

    switch (nd_type(node)) {
      case NODE_DSTR:
      case NODE_EVSTR:
      case NODE_STR:
	SWITCH_BY_COND_TYPE(type, warn, "string ")
	break;

      case NODE_DREGX:
	if (!e_option_supplied(p)) SWITCH_BY_COND_TYPE(type, warning, "regex ")

	return NEW_MATCH2(node, NEW_GVAR(idLASTLINE, loc), loc);

      case NODE_AND:
      case NODE_OR:
	node->nd_1st = cond0(p, node->nd_1st, COND_IN_COND, loc);
	node->nd_2nd = cond0(p, node->nd_2nd, COND_IN_COND, loc);
	break;

      case NODE_DOT2:
      case NODE_DOT3:
	node->nd_beg = range_op(p, node->nd_beg, loc);
	node->nd_end = range_op(p, node->nd_end, loc);
	if (nd_type(node) == NODE_DOT2) nd_set_type(node,NODE_FLIP2);
	else if (nd_type(node) == NODE_DOT3) nd_set_type(node, NODE_FLIP3);
	break;

      case NODE_DSYM:
	SWITCH_BY_COND_TYPE(type, warning, "string ")
	break;

      case NODE_LIT:
	if (RB_TYPE_P(node->nd_lit, T_REGEXP)) {
	    if (!e_option_supplied(p)) SWITCH_BY_COND_TYPE(type, warn, "regex ")
	    nd_set_type(node, NODE_MATCH);
	}
	else if (node->nd_lit == Qtrue ||
		 node->nd_lit == Qfalse) {
	    /* booleans are OK, e.g., while true */
	}
	else {
	    SWITCH_BY_COND_TYPE(type, warning, "")
	}
      default:
	break;
    }
    return node;
}

static NODE*
cond(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    if (node == 0) return 0;
    return cond0(p, node, COND_IN_COND, loc);
}

static NODE*
method_cond(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    if (node == 0) return 0;
    return cond0(p, node, COND_IN_OP, loc);
}

static NODE*
new_nil_at(struct parser_params *p, const rb_code_position_t *pos)
{
    YYLTYPE loc = {*pos, *pos};
    return NEW_NIL(&loc);
}

static NODE*
new_if(struct parser_params *p, NODE *cc, NODE *left, NODE *right, const YYLTYPE *loc)
{
    if (!cc) return right;
    cc = cond0(p, cc, COND_IN_COND, loc);
    return newline_node(NEW_IF(cc, left, right, loc));
}

static NODE*
new_unless(struct parser_params *p, NODE *cc, NODE *left, NODE *right, const YYLTYPE *loc)
{
    if (!cc) return right;
    cc = cond0(p, cc, COND_IN_COND, loc);
    return newline_node(NEW_UNLESS(cc, left, right, loc));
}

static NODE*
logop(struct parser_params *p, ID id, NODE *left, NODE *right,
	  const YYLTYPE *op_loc, const YYLTYPE *loc)
{
    enum node_type type = id == idAND || id == idANDOP ? NODE_AND : NODE_OR;
    NODE *op;
    value_expr(left);
    if (left && (enum node_type)nd_type(left) == type) {
	NODE *node = left, *second;
	while ((second = node->nd_2nd) != 0 && (enum node_type)nd_type(second) == type) {
	    node = second;
	}
	node->nd_2nd = NEW_NODE(type, second, right, 0, loc);
	nd_set_line(node->nd_2nd, op_loc->beg_pos.lineno);
	left->nd_loc.end_pos = loc->end_pos;
	return left;
    }
    op = NEW_NODE(type, left, right, 0, loc);
    nd_set_line(op, op_loc->beg_pos.lineno);
    return op;
}

static void
no_blockarg(struct parser_params *p, NODE *node)
{
    if (node && nd_type(node) == NODE_BLOCK_PASS) {
	compile_error(p, "block argument should not be given");
    }
}

static NODE *
ret_args(struct parser_params *p, NODE *node)
{
    if (node) {
	no_blockarg(p, node);
	if (nd_type(node) == NODE_LIST) {
	    if (node->nd_next == 0) {
		node = node->nd_head;
	    }
	    else {
		nd_set_type(node, NODE_VALUES);
	    }
	}
    }
    return node;
}

static NODE *
new_yield(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    if (node) no_blockarg(p, node);

    return NEW_YIELD(node, loc);
}

static VALUE
negate_lit(struct parser_params *p, VALUE lit)
{
    if (FIXNUM_P(lit)) {
	return LONG2FIX(-FIX2LONG(lit));
    }
    if (SPECIAL_CONST_P(lit)) {
#if USE_FLONUM
	if (FLONUM_P(lit)) {
	    return DBL2NUM(-RFLOAT_VALUE(lit));
	}
#endif
	goto unknown;
    }
    switch (BUILTIN_TYPE(lit)) {
      case T_BIGNUM:
	BIGNUM_NEGATE(lit);
	lit = rb_big_norm(lit);
	break;
      case T_RATIONAL:
	RATIONAL_SET_NUM(lit, negate_lit(p, RRATIONAL(lit)->num));
	break;
      case T_COMPLEX:
	RCOMPLEX_SET_REAL(lit, negate_lit(p, RCOMPLEX(lit)->real));
	RCOMPLEX_SET_IMAG(lit, negate_lit(p, RCOMPLEX(lit)->imag));
	break;
      case T_FLOAT:
	RFLOAT(lit)->float_value = -RFLOAT_VALUE(lit);
	break;
      unknown:
      default:
	rb_parser_fatal(p, "unknown literal type (%s) passed to negate_lit",
			rb_builtin_class_name(lit));
	break;
    }
    return lit;
}

static NODE *
arg_blk_pass(NODE *node1, NODE *node2)
{
    if (node2) {
        if (!node1) return node2;
	node2->nd_head = node1;
	nd_set_first_lineno(node2, nd_first_lineno(node1));
	nd_set_first_column(node2, nd_first_column(node1));
	return node2;
    }
    return node1;
}

static bool
args_info_empty_p(struct rb_args_info *args)
{
    if (args->pre_args_num) return false;
    if (args->post_args_num) return false;
    if (args->rest_arg) return false;
    if (args->opt_args) return false;
    if (args->block_arg) return false;
    if (args->kw_args) return false;
    if (args->kw_rest_arg) return false;
    return true;
}

static NODE*
new_args(struct parser_params *p, NODE *pre_args, NODE *opt_args, ID rest_arg, NODE *post_args, NODE *tail, const YYLTYPE *loc)
{
    int saved_line = p->ruby_sourceline;
    struct rb_args_info *args = tail->nd_ainfo;

    args->pre_args_num   = pre_args ? rb_long2int(pre_args->nd_plen) : 0;
    args->pre_init       = pre_args ? pre_args->nd_next : 0;

    args->post_args_num  = post_args ? rb_long2int(post_args->nd_plen) : 0;
    args->post_init      = post_args ? post_args->nd_next : 0;
    args->first_post_arg = post_args ? post_args->nd_pid : 0;

    args->rest_arg       = rest_arg;

    args->opt_args       = opt_args;

    args->ruby2_keywords = rest_arg == idFWD_REST;

    p->ruby_sourceline = saved_line;
    nd_set_loc(tail, loc);

    return tail;
}

static NODE*
new_args_tail(struct parser_params *p, NODE *kw_args, ID kw_rest_arg, ID block, const YYLTYPE *kw_rest_loc)
{
    int saved_line = p->ruby_sourceline;
    NODE *node;
    VALUE tmpbuf = rb_imemo_tmpbuf_auto_free_pointer();
    struct rb_args_info *args = ZALLOC(struct rb_args_info);
    rb_imemo_tmpbuf_set_ptr(tmpbuf, args);
    args->imemo = tmpbuf;
    node = NEW_NODE(NODE_ARGS, 0, 0, args, &NULL_LOC);
    RB_OBJ_WRITTEN(p->ast, Qnil, tmpbuf);
    if (p->error_p) return node;

    args->block_arg      = block;
    args->kw_args        = kw_args;

    if (kw_args) {
	/*
	 * def foo(k1: 1, kr1:, k2: 2, **krest, &b)
	 * variable order: k1, kr1, k2, &b, internal_id, krest
	 * #=> <reorder>
	 * variable order: kr1, k1, k2, internal_id, krest, &b
	 */
	ID kw_bits = internal_id(p), *required_kw_vars, *kw_vars;
	struct vtable *vtargs = p->lvtbl->args;
	NODE *kwn = kw_args;

	vtable_pop(vtargs, !!block + !!kw_rest_arg);
	required_kw_vars = kw_vars = &vtargs->tbl[vtargs->pos];
	while (kwn) {
	    if (!NODE_REQUIRED_KEYWORD_P(kwn->nd_body))
		--kw_vars;
	    --required_kw_vars;
	    kwn = kwn->nd_next;
	}

	for (kwn = kw_args; kwn; kwn = kwn->nd_next) {
	    ID vid = kwn->nd_body->nd_vid;
	    if (NODE_REQUIRED_KEYWORD_P(kwn->nd_body)) {
		*required_kw_vars++ = vid;
	    }
	    else {
		*kw_vars++ = vid;
	    }
	}

	arg_var(p, kw_bits);
	if (kw_rest_arg) arg_var(p, kw_rest_arg);
	if (block) arg_var(p, block);

	args->kw_rest_arg = NEW_DVAR(kw_rest_arg, kw_rest_loc);
	args->kw_rest_arg->nd_cflag = kw_bits;
    }
    else if (kw_rest_arg == idNil) {
	args->no_kwarg = 1;
    }
    else if (kw_rest_arg) {
	args->kw_rest_arg = NEW_DVAR(kw_rest_arg, kw_rest_loc);
    }

    p->ruby_sourceline = saved_line;
    return node;
}

static NODE *
args_with_numbered(struct parser_params *p, NODE *args, int max_numparam)
{
    if (max_numparam > NO_PARAM) {
	if (!args) {
	    YYLTYPE loc = RUBY_INIT_YYLLOC();
	    args = new_args_tail(p, 0, 0, 0, 0);
	    nd_set_loc(args, &loc);
	}
	args->nd_ainfo->pre_args_num = max_numparam;
    }
    return args;
}

static NODE*
new_array_pattern(struct parser_params *p, NODE *constant, NODE *pre_arg, NODE *aryptn, const YYLTYPE *loc)
{
    struct rb_ary_pattern_info *apinfo = aryptn->nd_apinfo;

    aryptn->nd_pconst = constant;

    if (pre_arg) {
	NODE *pre_args = NEW_LIST(pre_arg, loc);
	if (apinfo->pre_args) {
	    apinfo->pre_args = list_concat(pre_args, apinfo->pre_args);
	}
	else {
	    apinfo->pre_args = pre_args;
	}
    }
    return aryptn;
}

static NODE*
new_array_pattern_tail(struct parser_params *p, NODE *pre_args, int has_rest, ID rest_arg, NODE *post_args, const YYLTYPE *loc)
{
    int saved_line = p->ruby_sourceline;
    NODE *node;
    VALUE tmpbuf = rb_imemo_tmpbuf_auto_free_pointer();
    struct rb_ary_pattern_info *apinfo = ZALLOC(struct rb_ary_pattern_info);
    rb_imemo_tmpbuf_set_ptr(tmpbuf, apinfo);
    node = NEW_NODE(NODE_ARYPTN, 0, tmpbuf, apinfo, loc);
    RB_OBJ_WRITTEN(p->ast, Qnil, tmpbuf);

    apinfo->pre_args = pre_args;

    if (has_rest) {
	if (rest_arg) {
	    apinfo->rest_arg = assignable(p, rest_arg, 0, loc);
	}
	else {
	    apinfo->rest_arg = NODE_SPECIAL_NO_NAME_REST;
	}
    }
    else {
	apinfo->rest_arg = NULL;
    }

    apinfo->post_args = post_args;

    p->ruby_sourceline = saved_line;
    return node;
}

static NODE*
new_find_pattern(struct parser_params *p, NODE *constant, NODE *fndptn, const YYLTYPE *loc)
{
    fndptn->nd_pconst = constant;

    return fndptn;
}

static NODE*
new_find_pattern_tail(struct parser_params *p, ID pre_rest_arg, NODE *args, ID post_rest_arg, const YYLTYPE *loc)
{
    int saved_line = p->ruby_sourceline;
    NODE *node;
    VALUE tmpbuf = rb_imemo_tmpbuf_auto_free_pointer();
    struct rb_fnd_pattern_info *fpinfo = ZALLOC(struct rb_fnd_pattern_info);
    rb_imemo_tmpbuf_set_ptr(tmpbuf, fpinfo);
    node = NEW_NODE(NODE_FNDPTN, 0, tmpbuf, fpinfo, loc);
    RB_OBJ_WRITTEN(p->ast, Qnil, tmpbuf);

    fpinfo->pre_rest_arg = pre_rest_arg ? assignable(p, pre_rest_arg, 0, loc) : NODE_SPECIAL_NO_NAME_REST;
    fpinfo->args = args;
    fpinfo->post_rest_arg = post_rest_arg ? assignable(p, post_rest_arg, 0, loc) : NODE_SPECIAL_NO_NAME_REST;

    p->ruby_sourceline = saved_line;
    return node;
}

static NODE*
new_hash_pattern(struct parser_params *p, NODE *constant, NODE *hshptn, const YYLTYPE *loc)
{
    hshptn->nd_pconst = constant;
    return hshptn;
}

static NODE*
new_hash_pattern_tail(struct parser_params *p, NODE *kw_args, ID kw_rest_arg, const YYLTYPE *loc)
{
    int saved_line = p->ruby_sourceline;
    NODE *node, *kw_rest_arg_node;

    if (kw_rest_arg == idNil) {
	kw_rest_arg_node = NODE_SPECIAL_NO_REST_KEYWORD;
    }
    else if (kw_rest_arg) {
	kw_rest_arg_node = assignable(p, kw_rest_arg, 0, loc);
    }
    else {
	kw_rest_arg_node = NULL;
    }

    node = NEW_NODE(NODE_HSHPTN, 0, kw_args, kw_rest_arg_node, loc);

    p->ruby_sourceline = saved_line;
    return node;
}

static void
warn_one_line_pattern_matching(struct parser_params *p, NODE *node, NODE *pattern, bool right_assign)
{
    enum node_type type;
    type = nd_type(pattern);

    if (rb_warning_category_enabled_p(RB_WARN_CATEGORY_EXPERIMENTAL) &&
	!(right_assign && (type == NODE_LASGN || type == NODE_DASGN || type == NODE_DASGN_CURR)))
	rb_warn0L_experimental(nd_line(node), "One-line pattern matching is experimental, and the behavior may change in future versions of Ruby!");
}

static NODE*
dsym_node(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    VALUE lit;

    if (!node) {
	return NEW_LIT(ID2SYM(idNULL), loc);
    }

    switch (nd_type(node)) {
      case NODE_DSTR:
	nd_set_type(node, NODE_DSYM);
	nd_set_loc(node, loc);
	break;
      case NODE_STR:
	lit = node->nd_lit;
	RB_OBJ_WRITTEN(p->ast, Qnil, node->nd_lit = ID2SYM(rb_intern_str(lit)));
	nd_set_type(node, NODE_LIT);
	nd_set_loc(node, loc);
	break;
      default:
	node = NEW_NODE(NODE_DSYM, Qnil, 1, NEW_LIST(node, loc), loc);
	break;
    }
    return node;
}

static int
append_literal_keys(st_data_t k, st_data_t v, st_data_t h)
{
    NODE *node = (NODE *)v;
    NODE **result = (NODE **)h;
    node->nd_alen = 2;
    node->nd_next->nd_end = node->nd_next;
    node->nd_next->nd_next = 0;
    if (*result)
	list_concat(*result, node);
    else
	*result = node;
    return ST_CONTINUE;
}

static NODE *
remove_duplicate_keys(struct parser_params *p, NODE *hash)
{
    st_table *literal_keys = st_init_numtable_with_size(hash->nd_alen / 2);
    NODE *result = 0;
    rb_code_location_t loc = hash->nd_loc;
    while (hash && hash->nd_head && hash->nd_next) {
	NODE *head = hash->nd_head;
	NODE *value = hash->nd_next;
	NODE *next = value->nd_next;
	VALUE key = (VALUE)head;
	st_data_t data;
	if (nd_type(head) == NODE_LIT &&
	    st_lookup(literal_keys, (key = head->nd_lit), &data)) {
	    rb_compile_warn(p->ruby_sourcefile, nd_line((NODE *)data),
			    "key %+"PRIsVALUE" is duplicated and overwritten on line %d",
			    head->nd_lit, nd_line(head));
	    head = ((NODE *)data)->nd_next;
	    head->nd_head = block_append(p, head->nd_head, value->nd_head);
	}
	else {
	    st_insert(literal_keys, (st_data_t)key, (st_data_t)hash);
	}
	hash = next;
    }
    st_foreach(literal_keys, append_literal_keys, (st_data_t)&result);
    st_free_table(literal_keys);
    if (hash) {
	if (!result) result = hash;
	else list_concat(result, hash);
    }
    result->nd_loc = loc;
    return result;
}

static NODE *
new_hash(struct parser_params *p, NODE *hash, const YYLTYPE *loc)
{
    if (hash) hash = remove_duplicate_keys(p, hash);
    return NEW_HASH(hash, loc);
}
#endif

static void
error_duplicate_pattern_variable(struct parser_params *p, ID id, const YYLTYPE *loc)
{
    if (is_private_local_id(id)) {
	return;
    }
    if (st_is_member(p->pvtbl, id)) {
	yyerror1(loc, "duplicated variable name");
    }
    else {
	st_insert(p->pvtbl, (st_data_t)id, 0);
    }
}

static void
error_duplicate_pattern_key(struct parser_params *p, VALUE key, const YYLTYPE *loc)
{
    if (!p->pktbl) {
	p->pktbl = st_init_numtable();
    }
    else if (st_is_member(p->pktbl, key)) {
	yyerror1(loc, "duplicated key name");
	return;
    }
    st_insert(p->pktbl, (st_data_t)key, 0);
}

#ifndef RIPPER
static NODE *
new_unique_key_hash(struct parser_params *p, NODE *hash, const YYLTYPE *loc)
{
    return NEW_HASH(hash, loc);
}
#endif /* !RIPPER */

#ifndef RIPPER
static NODE *
new_op_assign(struct parser_params *p, NODE *lhs, ID op, NODE *rhs, struct lex_context ctxt, const YYLTYPE *loc)
{
    NODE *asgn;

    if (lhs) {
	ID vid = lhs->nd_vid;
	YYLTYPE lhs_loc = lhs->nd_loc;
	int shareable = ctxt.shareable_constant_value;
	if (shareable) {
	    switch (nd_type(lhs)) {
	      case NODE_CDECL:
	      case NODE_COLON2:
	      case NODE_COLON3:
		break;
	      default:
		shareable = 0;
		break;
	    }
	}
	if (op == tOROP) {
	    rhs = shareable_constant_value(p, shareable, lhs, rhs, &rhs->nd_loc);
	    lhs->nd_value = rhs;
	    nd_set_loc(lhs, loc);
	    asgn = NEW_OP_ASGN_OR(gettable(p, vid, &lhs_loc), lhs, loc);
	    if (is_notop_id(vid)) {
		switch (id_type(vid)) {
		  case ID_GLOBAL:
		  case ID_INSTANCE:
		  case ID_CLASS:
		    asgn->nd_aid = vid;
		}
	    }
	}
	else if (op == tANDOP) {
	    if (shareable) {
		rhs = shareable_constant_value(p, shareable, lhs, rhs, &rhs->nd_loc);
	    }
	    lhs->nd_value = rhs;
	    nd_set_loc(lhs, loc);
	    asgn = NEW_OP_ASGN_AND(gettable(p, vid, &lhs_loc), lhs, loc);
	}
	else {
	    asgn = lhs;
	    rhs = NEW_CALL(gettable(p, vid, &lhs_loc), op, NEW_LIST(rhs, &rhs->nd_loc), loc);
	    if (shareable) {
		rhs = shareable_constant_value(p, shareable, lhs, rhs, &rhs->nd_loc);
	    }
	    asgn->nd_value = rhs;
	    nd_set_loc(asgn, loc);
	}
    }
    else {
	asgn = NEW_BEGIN(0, loc);
    }
    return asgn;
}

static NODE *
new_ary_op_assign(struct parser_params *p, NODE *ary,
		  NODE *args, ID op, NODE *rhs, const YYLTYPE *args_loc, const YYLTYPE *loc)
{
    NODE *asgn;

    args = make_list(args, args_loc);
    if (nd_type(args) == NODE_BLOCK_PASS) {
	args = NEW_ARGSCAT(args, rhs, loc);
    }
    else {
	args = arg_concat(p, args, rhs, loc);
    }
    asgn = NEW_OP_ASGN1(ary, op, args, loc);
    fixpos(asgn, ary);
    return asgn;
}

static NODE *
new_attr_op_assign(struct parser_params *p, NODE *lhs,
		   ID atype, ID attr, ID op, NODE *rhs, const YYLTYPE *loc)
{
    NODE *asgn;

    asgn = NEW_OP_ASGN2(lhs, CALL_Q_P(atype), attr, op, rhs, loc);
    fixpos(asgn, lhs);
    return asgn;
}

static NODE *
new_const_op_assign(struct parser_params *p, NODE *lhs, ID op, NODE *rhs, struct lex_context ctxt, const YYLTYPE *loc)
{
    NODE *asgn;

    if (lhs) {
	rhs = shareable_constant_value(p, ctxt.shareable_constant_value, lhs, rhs, loc);
	asgn = NEW_OP_CDECL(lhs, op, rhs, loc);
    }
    else {
	asgn = NEW_BEGIN(0, loc);
    }
    fixpos(asgn, lhs);
    return asgn;
}

static NODE *
const_decl(struct parser_params *p, NODE *path, const YYLTYPE *loc)
{
    if (p->ctxt.in_def) {
	yyerror1(loc, "dynamic constant assignment");
    }
    return NEW_CDECL(0, 0, (path), loc);
}
#else
static VALUE
const_decl(struct parser_params *p, VALUE path)
{
    if (p->ctxt.in_def) {
	path = assign_error(p, "dynamic constant assignment", path);
    }
    return path;
}

static VALUE
assign_error(struct parser_params *p, const char *mesg, VALUE a)
{
    a = dispatch2(assign_error, ERR_MESG(), a);
    ripper_error(p);
    return a;
}

static VALUE
var_field(struct parser_params *p, VALUE a)
{
    return ripper_new_yylval(p, get_id(a), dispatch1(var_field, a), 0);
}
#endif

#ifndef RIPPER
static NODE *
new_bodystmt(struct parser_params *p, NODE *head, NODE *rescue, NODE *rescue_else, NODE *ensure, const YYLTYPE *loc)
{
    NODE *result = head;
    if (rescue) {
        NODE *tmp = rescue_else ? rescue_else : rescue;
        YYLTYPE rescue_loc = code_loc_gen(&head->nd_loc, &tmp->nd_loc);

        result = NEW_RESCUE(head, rescue, rescue_else, &rescue_loc);
        nd_set_line(result, rescue->nd_loc.beg_pos.lineno);
    }
    else if (rescue_else) {
        result = block_append(p, result, rescue_else);
    }
    if (ensure) {
        result = NEW_ENSURE(result, ensure, loc);
    }
    fixpos(result, head);
    return result;
}
#endif

static void
warn_unused_var(struct parser_params *p, struct local_vars *local)
{
    int cnt;

    if (!local->used) return;
    cnt = local->used->pos;
    if (cnt != local->vars->pos) {
	rb_parser_fatal(p, "local->used->pos != local->vars->pos");
    }
#ifndef RIPPER
    ID *v = local->vars->tbl;
    ID *u = local->used->tbl;
    for (int i = 0; i < cnt; ++i) {
	if (!v[i] || (u[i] & LVAR_USED)) continue;
	if (is_private_local_id(v[i])) continue;
	rb_warn1L((int)u[i], "assigned but unused variable - %"PRIsWARN, rb_id2str(v[i]));
    }
#endif
}

static void
local_push(struct parser_params *p, int toplevel_scope)
{
    struct local_vars *local;
    int inherits_dvars = toplevel_scope && compile_for_eval;
    int warn_unused_vars = RTEST(ruby_verbose);

    local = ALLOC(struct local_vars);
    local->prev = p->lvtbl;
    local->args = vtable_alloc(0);
    local->vars = vtable_alloc(inherits_dvars ? DVARS_INHERIT : DVARS_TOPSCOPE);
#ifndef RIPPER
    if (toplevel_scope && compile_for_eval) warn_unused_vars = 0;
    if (toplevel_scope && e_option_supplied(p)) warn_unused_vars = 0;
    local->numparam.outer = 0;
    local->numparam.inner = 0;
    local->numparam.current = 0;
#endif
    local->used = warn_unused_vars ? vtable_alloc(0) : 0;

# if WARN_PAST_SCOPE
    local->past = 0;
# endif
    CMDARG_PUSH(0);
    COND_PUSH(0);
    p->lvtbl = local;
}

static void
local_pop(struct parser_params *p)
{
    struct local_vars *local = p->lvtbl->prev;
    if (p->lvtbl->used) {
	warn_unused_var(p, p->lvtbl);
	vtable_free(p->lvtbl->used);
    }
# if WARN_PAST_SCOPE
    while (p->lvtbl->past) {
	struct vtable *past = p->lvtbl->past;
	p->lvtbl->past = past->prev;
	vtable_free(past);
    }
# endif
    vtable_free(p->lvtbl->args);
    vtable_free(p->lvtbl->vars);
    CMDARG_POP();
    COND_POP();
    ruby_sized_xfree(p->lvtbl, sizeof(*p->lvtbl));
    p->lvtbl = local;
}

#ifndef RIPPER
static ID*
local_tbl(struct parser_params *p)
{
    int cnt_args = vtable_size(p->lvtbl->args);
    int cnt_vars = vtable_size(p->lvtbl->vars);
    int cnt = cnt_args + cnt_vars;
    int i, j;
    ID *buf;

    if (cnt <= 0) return 0;
    buf = ALLOC_N(ID, cnt + 2);
    MEMCPY(buf+1, p->lvtbl->args->tbl, ID, cnt_args);
    /* remove IDs duplicated to warn shadowing */
    for (i = 0, j = cnt_args+1; i < cnt_vars; ++i) {
	ID id = p->lvtbl->vars->tbl[i];
	if (!vtable_included(p->lvtbl->args, id)) {
	    buf[j++] = id;
	}
    }
    if (--j < cnt) {
	REALLOC_N(buf, ID, (cnt = j) + 2);
    }
    buf[0] = cnt;
    rb_ast_add_local_table(p->ast, buf);

    return buf;
}

static NODE*
node_newnode_with_locals(struct parser_params *p, enum node_type type, VALUE a1, VALUE a2, const rb_code_location_t *loc)
{
    ID *a0;
    NODE *n;

    a0 = local_tbl(p);
    n = NEW_NODE(type, a0, a1, a2, loc);
    return n;
}

#endif

static void
numparam_name(struct parser_params *p, ID id)
{
    if (!NUMPARAM_ID_P(id)) return;
    compile_error(p, "_%d is reserved for numbered parameter",
        NUMPARAM_ID_TO_IDX(id));
}

static void
arg_var(struct parser_params *p, ID id)
{
    numparam_name(p, id);
    vtable_add(p->lvtbl->args, id);
}

static void
local_var(struct parser_params *p, ID id)
{
    numparam_name(p, id);
    vtable_add(p->lvtbl->vars, id);
    if (p->lvtbl->used) {
	vtable_add(p->lvtbl->used, (ID)p->ruby_sourceline);
    }
}

static int
local_id_ref(struct parser_params *p, ID id, ID **vidrefp)
{
    struct vtable *vars, *args, *used;

    vars = p->lvtbl->vars;
    args = p->lvtbl->args;
    used = p->lvtbl->used;

    while (vars && !DVARS_TERMINAL_P(vars->prev)) {
	vars = vars->prev;
	args = args->prev;
	if (used) used = used->prev;
    }

    if (vars && vars->prev == DVARS_INHERIT) {
	return rb_local_defined(id, p->parent_iseq);
    }
    else if (vtable_included(args, id)) {
	return 1;
    }
    else {
	int i = vtable_included(vars, id);
	if (i && used && vidrefp) *vidrefp = &used->tbl[i-1];
	return i != 0;
    }
}

static int
local_id(struct parser_params *p, ID id)
{
    return local_id_ref(p, id, NULL);
}

static int
check_forwarding_args(struct parser_params *p)
{
    if (local_id(p, idFWD_REST) &&
#if idFWD_KWREST
        local_id(p, idFWD_KWREST) &&
#endif
        local_id(p, idFWD_BLOCK)) return TRUE;
    compile_error(p, "unexpected ...");
    return FALSE;
}

static void
add_forwarding_args(struct parser_params *p)
{
    arg_var(p, idFWD_REST);
#if idFWD_KWREST
    arg_var(p, idFWD_KWREST);
#endif
    arg_var(p, idFWD_BLOCK);
}

#ifndef RIPPER
static NODE *
new_args_forward_call(struct parser_params *p, NODE *leading, const YYLTYPE *loc, const YYLTYPE *argsloc)
{
    NODE *splat = NEW_SPLAT(NEW_LVAR(idFWD_REST, loc), loc);
#if idFWD_KWREST
    NODE *kwrest = list_append(p, NEW_LIST(0, loc), NEW_LVAR(idFWD_KWREST, loc));
#endif
    NODE *block = NEW_BLOCK_PASS(NEW_LVAR(idFWD_BLOCK, loc), loc);
    NODE *args = leading ? rest_arg_append(p, leading, splat, argsloc) : splat;
#if idFWD_KWREST
    args = arg_append(p, splat, new_hash(p, kwrest, loc), loc);
#endif
    return arg_blk_pass(args, block);
}

static NODE *
new_args_forward_def(struct parser_params *p, NODE *leading, const YYLTYPE *loc)
{
    NODE *n = new_args_tail(p, Qnone, idFWD_KWREST, idFWD_BLOCK, loc);
    return new_args(p, leading, Qnone, idFWD_REST, Qnone, n, loc);
}
#endif

static NODE *
numparam_push(struct parser_params *p)
{
#ifndef RIPPER
    struct local_vars *local = p->lvtbl;
    NODE *inner = local->numparam.inner;
    if (!local->numparam.outer) {
	local->numparam.outer = local->numparam.current;
    }
    local->numparam.inner = 0;
    local->numparam.current = 0;
    return inner;
#else
    return 0;
#endif
}

static void
numparam_pop(struct parser_params *p, NODE *prev_inner)
{
#ifndef RIPPER
    struct local_vars *local = p->lvtbl;
    if (prev_inner) {
	/* prefer first one */
	local->numparam.inner = prev_inner;
    }
    else if (local->numparam.current) {
	/* current and inner are exclusive */
	local->numparam.inner = local->numparam.current;
    }
    if (p->max_numparam > NO_PARAM) {
	/* current and outer are exclusive */
	local->numparam.current = local->numparam.outer;
	local->numparam.outer = 0;
    }
    else {
	/* no numbered parameter */
	local->numparam.current = 0;
    }
#endif
}

static const struct vtable *
dyna_push(struct parser_params *p)
{
    p->lvtbl->args = vtable_alloc(p->lvtbl->args);
    p->lvtbl->vars = vtable_alloc(p->lvtbl->vars);
    if (p->lvtbl->used) {
	p->lvtbl->used = vtable_alloc(p->lvtbl->used);
    }
    return p->lvtbl->args;
}

static void
dyna_pop_vtable(struct parser_params *p, struct vtable **vtblp)
{
    struct vtable *tmp = *vtblp;
    *vtblp = tmp->prev;
# if WARN_PAST_SCOPE
    if (p->past_scope_enabled) {
	tmp->prev = p->lvtbl->past;
	p->lvtbl->past = tmp;
	return;
    }
# endif
    vtable_free(tmp);
}

static void
dyna_pop_1(struct parser_params *p)
{
    struct vtable *tmp;

    if ((tmp = p->lvtbl->used) != 0) {
	warn_unused_var(p, p->lvtbl);
	p->lvtbl->used = p->lvtbl->used->prev;
	vtable_free(tmp);
    }
    dyna_pop_vtable(p, &p->lvtbl->args);
    dyna_pop_vtable(p, &p->lvtbl->vars);
}

static void
dyna_pop(struct parser_params *p, const struct vtable *lvargs)
{
    while (p->lvtbl->args != lvargs) {
	dyna_pop_1(p);
	if (!p->lvtbl->args) {
	    struct local_vars *local = p->lvtbl->prev;
	    ruby_sized_xfree(p->lvtbl, sizeof(*p->lvtbl));
	    p->lvtbl = local;
	}
    }
    dyna_pop_1(p);
}

static int
dyna_in_block(struct parser_params *p)
{
    return !DVARS_TERMINAL_P(p->lvtbl->vars) && p->lvtbl->vars->prev != DVARS_TOPSCOPE;
}

static int
dvar_defined_ref(struct parser_params *p, ID id, ID **vidrefp)
{
    struct vtable *vars, *args, *used;
    int i;

    args = p->lvtbl->args;
    vars = p->lvtbl->vars;
    used = p->lvtbl->used;

    while (!DVARS_TERMINAL_P(vars)) {
	if (vtable_included(args, id)) {
	    return 1;
	}
	if ((i = vtable_included(vars, id)) != 0) {
	    if (used && vidrefp) *vidrefp = &used->tbl[i-1];
	    return 1;
	}
	args = args->prev;
	vars = vars->prev;
	if (!vidrefp) used = 0;
	if (used) used = used->prev;
    }

    if (vars == DVARS_INHERIT && !NUMPARAM_ID_P(id)) {
        return rb_dvar_defined(id, p->parent_iseq);
    }

    return 0;
}

static int
dvar_defined(struct parser_params *p, ID id)
{
    return dvar_defined_ref(p, id, NULL);
}

static int
dvar_curr(struct parser_params *p, ID id)
{
    return (vtable_included(p->lvtbl->args, id) ||
	    vtable_included(p->lvtbl->vars, id));
}

static void
reg_fragment_enc_error(struct parser_params* p, VALUE str, int c)
{
    compile_error(p,
        "regexp encoding option '%c' differs from source encoding '%s'",
        c, rb_enc_name(rb_enc_get(str)));
}

#ifndef RIPPER
int
rb_reg_fragment_setenc(struct parser_params* p, VALUE str, int options)
{
    int c = RE_OPTION_ENCODING_IDX(options);

    if (c) {
	int opt, idx;
	rb_char_to_option_kcode(c, &opt, &idx);
	if (idx != ENCODING_GET(str) &&
	    rb_enc_str_coderange(str) != ENC_CODERANGE_7BIT) {
            goto error;
	}
	ENCODING_SET(str, idx);
    }
    else if (RE_OPTION_ENCODING_NONE(options)) {
        if (!ENCODING_IS_ASCII8BIT(str) &&
            rb_enc_str_coderange(str) != ENC_CODERANGE_7BIT) {
            c = 'n';
            goto error;
        }
	rb_enc_associate(str, rb_ascii8bit_encoding());
    }
    else if (p->enc == rb_usascii_encoding()) {
	if (rb_enc_str_coderange(str) != ENC_CODERANGE_7BIT) {
	    /* raise in re.c */
	    rb_enc_associate(str, rb_usascii_encoding());
	}
	else {
	    rb_enc_associate(str, rb_ascii8bit_encoding());
	}
    }
    return 0;

  error:
    return c;
}

static void
reg_fragment_setenc(struct parser_params* p, VALUE str, int options)
{
    int c = rb_reg_fragment_setenc(p, str, options);
    if (c) reg_fragment_enc_error(p, str, c);
}

static int
reg_fragment_check(struct parser_params* p, VALUE str, int options)
{
    VALUE err;
    reg_fragment_setenc(p, str, options);
    err = rb_reg_check_preprocess(str);
    if (err != Qnil) {
        err = rb_obj_as_string(err);
        compile_error(p, "%"PRIsVALUE, err);
	return 0;
    }
    return 1;
}

typedef struct {
    struct parser_params* parser;
    rb_encoding *enc;
    NODE *succ_block;
    const YYLTYPE *loc;
} reg_named_capture_assign_t;

static int
reg_named_capture_assign_iter(const OnigUChar *name, const OnigUChar *name_end,
          int back_num, int *back_refs, OnigRegex regex, void *arg0)
{
    reg_named_capture_assign_t *arg = (reg_named_capture_assign_t*)arg0;
    struct parser_params* p = arg->parser;
    rb_encoding *enc = arg->enc;
    long len = name_end - name;
    const char *s = (const char *)name;
    ID var;
    NODE *node, *succ;

    if (!len) return ST_CONTINUE;
    if (rb_enc_symname_type(s, len, enc, (1U<<ID_LOCAL)) != ID_LOCAL)
        return ST_CONTINUE;

    var = intern_cstr(s, len, enc);
    if (len < MAX_WORD_LENGTH && rb_reserved_word(s, (int)len)) {
	if (!lvar_defined(p, var)) return ST_CONTINUE;
    }
    node = node_assign(p, assignable(p, var, 0, arg->loc), NEW_LIT(ID2SYM(var), arg->loc), NO_LEX_CTXT, arg->loc);
    succ = arg->succ_block;
    if (!succ) succ = NEW_BEGIN(0, arg->loc);
    succ = block_append(p, succ, node);
    arg->succ_block = succ;
    return ST_CONTINUE;
}

static NODE *
reg_named_capture_assign(struct parser_params* p, VALUE regexp, const YYLTYPE *loc)
{
    reg_named_capture_assign_t arg;

    arg.parser = p;
    arg.enc = rb_enc_get(regexp);
    arg.succ_block = 0;
    arg.loc = loc;
    onig_foreach_name(RREGEXP_PTR(regexp), reg_named_capture_assign_iter, &arg);

    if (!arg.succ_block) return 0;
    return arg.succ_block->nd_next;
}

static VALUE
parser_reg_compile(struct parser_params* p, VALUE str, int options)
{
    reg_fragment_setenc(p, str, options);
    return rb_parser_reg_compile(p, str, options);
}

VALUE
rb_parser_reg_compile(struct parser_params* p, VALUE str, int options)
{
    return rb_reg_compile(str, options & RE_OPTION_MASK, p->ruby_sourcefile, p->ruby_sourceline);
}

static VALUE
reg_compile(struct parser_params* p, VALUE str, int options)
{
    VALUE re;
    VALUE err;

    err = rb_errinfo();
    re = parser_reg_compile(p, str, options);
    if (NIL_P(re)) {
	VALUE m = rb_attr_get(rb_errinfo(), idMesg);
	rb_set_errinfo(err);
	compile_error(p, "%"PRIsVALUE, m);
	return Qnil;
    }
    return re;
}
#else
static VALUE
parser_reg_compile(struct parser_params* p, VALUE str, int options, VALUE *errmsg)
{
    VALUE err = rb_errinfo();
    VALUE re;
    str = ripper_is_node_yylval(str) ? RNODE(str)->nd_cval : str;
    int c = rb_reg_fragment_setenc(p, str, options);
    if (c) reg_fragment_enc_error(p, str, c);
    re = rb_parser_reg_compile(p, str, options);
    if (NIL_P(re)) {
	*errmsg = rb_attr_get(rb_errinfo(), idMesg);
	rb_set_errinfo(err);
    }
    return re;
}
#endif

#ifndef RIPPER
void
rb_parser_set_options(VALUE vparser, int print, int loop, int chomp, int split)
{
    struct parser_params *p;
    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    p->do_print = print;
    p->do_loop = loop;
    p->do_chomp = chomp;
    p->do_split = split;
}

static NODE *
parser_append_options(struct parser_params *p, NODE *node)
{
    static const YYLTYPE default_location = {{1, 0}, {1, 0}};
    const YYLTYPE *const LOC = &default_location;

    if (p->do_print) {
	NODE *print = NEW_FCALL(rb_intern("print"),
				NEW_LIST(NEW_GVAR(idLASTLINE, LOC), LOC),
				LOC);
	node = block_append(p, node, print);
    }

    if (p->do_loop) {
	if (p->do_split) {
	    ID ifs = rb_intern("$;");
	    ID fields = rb_intern("$F");
	    NODE *args = NEW_LIST(NEW_GVAR(ifs, LOC), LOC);
	    NODE *split = NEW_GASGN(fields,
				    NEW_CALL(NEW_GVAR(idLASTLINE, LOC),
					     rb_intern("split"), args, LOC),
				    LOC);
	    node = block_append(p, split, node);
	}
	if (p->do_chomp) {
	    NODE *chomp = NEW_CALL(NEW_GVAR(idLASTLINE, LOC),
				   rb_intern("chomp!"), 0, LOC);
	    node = block_append(p, chomp, node);
	}

	node = NEW_WHILE(NEW_VCALL(idGets, LOC), node, 1, LOC);
    }

    return node;
}

void
rb_init_parse(void)
{
    /* just to suppress unused-function warnings */
    (void)nodetype;
    (void)nodeline;
}

static ID
internal_id(struct parser_params *p)
{
    const ID max_id = RB_ID_SERIAL_MAX & ~0xffff;
    ID id = (ID)vtable_size(p->lvtbl->args) + (ID)vtable_size(p->lvtbl->vars);
    id = max_id - id;
    return ID_STATIC_SYM | ID_INTERNAL | (id << ID_SCOPE_SHIFT);
}
#endif /* !RIPPER */

static void
parser_initialize(struct parser_params *p)
{
    /* note: we rely on TypedData_Make_Struct to set most fields to 0 */
    p->command_start = TRUE;
    p->ruby_sourcefile_string = Qnil;
    p->lex.lpar_beg = -1; /* make lambda_beginning_p() == FALSE at first */
    p->node_id = 0;
#ifdef RIPPER
    p->delayed.token = Qnil;
    p->result = Qnil;
    p->parsing_thread = Qnil;
#else
    p->error_buffer = Qfalse;
#endif
    p->debug_buffer = Qnil;
    p->debug_output = rb_ractor_stdout();
    p->enc = rb_utf8_encoding();
}

#ifdef RIPPER
#define parser_mark ripper_parser_mark
#define parser_free ripper_parser_free
#endif

static void
parser_mark(void *ptr)
{
    struct parser_params *p = (struct parser_params*)ptr;

    rb_gc_mark(p->lex.input);
    rb_gc_mark(p->lex.prevline);
    rb_gc_mark(p->lex.lastline);
    rb_gc_mark(p->lex.nextline);
    rb_gc_mark(p->ruby_sourcefile_string);
    rb_gc_mark((VALUE)p->lex.strterm);
    rb_gc_mark((VALUE)p->ast);
    rb_gc_mark(p->case_labels);
#ifndef RIPPER
    rb_gc_mark(p->debug_lines);
    rb_gc_mark(p->compile_option);
    rb_gc_mark(p->error_buffer);
#else
    rb_gc_mark(p->delayed.token);
    rb_gc_mark(p->value);
    rb_gc_mark(p->result);
    rb_gc_mark(p->parsing_thread);
#endif
    rb_gc_mark(p->debug_buffer);
    rb_gc_mark(p->debug_output);
#ifdef YYMALLOC
    rb_gc_mark((VALUE)p->heap);
#endif
}

static void
parser_free(void *ptr)
{
    struct parser_params *p = (struct parser_params*)ptr;
    struct local_vars *local, *prev;

    if (p->tokenbuf) {
        ruby_sized_xfree(p->tokenbuf, p->toksiz);
    }
    for (local = p->lvtbl; local; local = prev) {
	if (local->vars) xfree(local->vars);
	prev = local->prev;
	xfree(local);
    }
    {
	token_info *ptinfo;
	while ((ptinfo = p->token_info) != 0) {
	    p->token_info = ptinfo->next;
	    xfree(ptinfo);
	}
    }
    xfree(ptr);
}

static size_t
parser_memsize(const void *ptr)
{
    struct parser_params *p = (struct parser_params*)ptr;
    struct local_vars *local;
    size_t size = sizeof(*p);

    size += p->toksiz;
    for (local = p->lvtbl; local; local = local->prev) {
	size += sizeof(*local);
	if (local->vars) size += local->vars->capa * sizeof(ID);
    }
    return size;
}

static const rb_data_type_t parser_data_type = {
#ifndef RIPPER
    "parser",
#else
    "ripper",
#endif
    {
	parser_mark,
	parser_free,
	parser_memsize,
    },
    0, 0, RUBY_TYPED_FREE_IMMEDIATELY
};

#ifndef RIPPER
#undef rb_reserved_word

const struct kwtable *
rb_reserved_word(const char *str, unsigned int len)
{
    return reserved_word(str, len);
}

VALUE
rb_parser_new(void)
{
    struct parser_params *p;
    VALUE parser = TypedData_Make_Struct(0, struct parser_params,
					 &parser_data_type, p);
    parser_initialize(p);
    return parser;
}

VALUE
rb_parser_set_context(VALUE vparser, const struct rb_iseq_struct *base, int main)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    p->error_buffer = main ? Qfalse : Qnil;
    p->parent_iseq = base;
    return vparser;
}
#endif

#ifdef RIPPER
#define rb_parser_end_seen_p ripper_parser_end_seen_p
#define rb_parser_encoding ripper_parser_encoding
#define rb_parser_get_yydebug ripper_parser_get_yydebug
#define rb_parser_set_yydebug ripper_parser_set_yydebug
#define rb_parser_get_debug_output ripper_parser_get_debug_output
#define rb_parser_set_debug_output ripper_parser_set_debug_output
static VALUE ripper_parser_end_seen_p(VALUE vparser);
static VALUE ripper_parser_encoding(VALUE vparser);
static VALUE ripper_parser_get_yydebug(VALUE self);
static VALUE ripper_parser_set_yydebug(VALUE self, VALUE flag);
static VALUE ripper_parser_get_debug_output(VALUE self);
static VALUE ripper_parser_set_debug_output(VALUE self, VALUE output);

/*
 *  call-seq:
 *    ripper.error?   -> Boolean
 *
 *  Return true if parsed source has errors.
 */
static VALUE
ripper_error_p(VALUE vparser)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    return p->error_p ? Qtrue : Qfalse;
}
#endif

/*
 *  call-seq:
 *    ripper.end_seen?   -> Boolean
 *
 *  Return true if parsed source ended by +\_\_END\_\_+.
 */
VALUE
rb_parser_end_seen_p(VALUE vparser)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    return p->ruby__end__seen ? Qtrue : Qfalse;
}

/*
 *  call-seq:
 *    ripper.encoding   -> encoding
 *
 *  Return encoding of the source.
 */
VALUE
rb_parser_encoding(VALUE vparser)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    return rb_enc_from_encoding(p->enc);
}

#ifdef RIPPER
/*
 *  call-seq:
 *    ripper.yydebug   -> true or false
 *
 *  Get yydebug.
 */
VALUE
rb_parser_get_yydebug(VALUE self)
{
    struct parser_params *p;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    return p->debug ? Qtrue : Qfalse;
}
#endif

/*
 *  call-seq:
 *    ripper.yydebug = flag
 *
 *  Set yydebug.
 */
VALUE
rb_parser_set_yydebug(VALUE self, VALUE flag)
{
    struct parser_params *p;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    p->debug = RTEST(flag);
    return flag;
}

/*
 *  call-seq:
 *    ripper.debug_output   -> obj
 *
 *  Get debug output.
 */
VALUE
rb_parser_get_debug_output(VALUE self)
{
    struct parser_params *p;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    return p->debug_output;
}

/*
 *  call-seq:
 *    ripper.debug_output = obj
 *
 *  Set debug output.
 */
VALUE
rb_parser_set_debug_output(VALUE self, VALUE output)
{
    struct parser_params *p;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    return p->debug_output = output;
}

#ifndef RIPPER
#ifdef YYMALLOC
#define HEAPCNT(n, size) ((n) * (size) / sizeof(YYSTYPE))
/* Keep the order; NEWHEAP then xmalloc and ADD2HEAP to get rid of
 * potential memory leak */
#define NEWHEAP() rb_imemo_tmpbuf_parser_heap(0, p->heap, 0)
#define ADD2HEAP(new, cnt, ptr) ((p->heap = (new))->ptr = (ptr), \
			   (new)->cnt = (cnt), (ptr))

void *
rb_parser_malloc(struct parser_params *p, size_t size)
{
    size_t cnt = HEAPCNT(1, size);
    rb_imemo_tmpbuf_t *n = NEWHEAP();
    void *ptr = xmalloc(size);

    return ADD2HEAP(n, cnt, ptr);
}

void *
rb_parser_calloc(struct parser_params *p, size_t nelem, size_t size)
{
    size_t cnt = HEAPCNT(nelem, size);
    rb_imemo_tmpbuf_t *n = NEWHEAP();
    void *ptr = xcalloc(nelem, size);

    return ADD2HEAP(n, cnt, ptr);
}

void *
rb_parser_realloc(struct parser_params *p, void *ptr, size_t size)
{
    rb_imemo_tmpbuf_t *n;
    size_t cnt = HEAPCNT(1, size);

    if (ptr && (n = p->heap) != NULL) {
	do {
	    if (n->ptr == ptr) {
		n->ptr = ptr = xrealloc(ptr, size);
		if (n->cnt) n->cnt = cnt;
		return ptr;
	    }
	} while ((n = n->next) != NULL);
    }
    n = NEWHEAP();
    ptr = xrealloc(ptr, size);
    return ADD2HEAP(n, cnt, ptr);
}

void
rb_parser_free(struct parser_params *p, void *ptr)
{
    rb_imemo_tmpbuf_t **prev = &p->heap, *n;

    while ((n = *prev) != NULL) {
	if (n->ptr == ptr) {
	    *prev = n->next;
	    rb_gc_force_recycle((VALUE)n);
	    break;
	}
	prev = &n->next;
    }
    xfree(ptr);
}
#endif

void
rb_parser_printf(struct parser_params *p, const char *fmt, ...)
{
    va_list ap;
    VALUE mesg = p->debug_buffer;

    if (NIL_P(mesg)) p->debug_buffer = mesg = rb_str_new(0, 0);
    va_start(ap, fmt);
    rb_str_vcatf(mesg, fmt, ap);
    va_end(ap);
    if (RSTRING_END(mesg)[-1] == '\n') {
	rb_io_write(p->debug_output, mesg);
	p->debug_buffer = Qnil;
    }
}

static void
parser_compile_error(struct parser_params *p, const char *fmt, ...)
{
    va_list ap;

    rb_io_flush(p->debug_output);
    p->error_p = 1;
    va_start(ap, fmt);
    p->error_buffer =
	rb_syntax_error_append(p->error_buffer,
			       p->ruby_sourcefile_string,
			       p->ruby_sourceline,
			       rb_long2int(p->lex.pcur - p->lex.pbeg),
			       p->enc, fmt, ap);
    va_end(ap);
}

static size_t
count_char(const char *str, int c)
{
    int n = 0;
    while (str[n] == c) ++n;
    return n;
}

/*
 * strip enclosing double-quotes, same as the default yytnamerr except
 * for that single-quotes matching back-quotes do not stop stripping.
 *
 *  "\"`class' keyword\"" => "`class' keyword"
 */
RUBY_FUNC_EXPORTED size_t
rb_yytnamerr(struct parser_params *p, char *yyres, const char *yystr)
{
    if (*yystr == '"') {
	size_t yyn = 0, bquote = 0;
	const char *yyp = yystr;

	while (*++yyp) {
	    switch (*yyp) {
	      case '`':
		if (!bquote) {
		    bquote = count_char(yyp+1, '`') + 1;
		    if (yyres) memcpy(&yyres[yyn], yyp, bquote);
		    yyn += bquote;
		    yyp += bquote - 1;
		    break;
		}
		goto default_char;

	      case '\'':
		if (bquote && count_char(yyp+1, '\'') + 1 == bquote) {
		    if (yyres) memcpy(yyres + yyn, yyp, bquote);
		    yyn += bquote;
		    yyp += bquote - 1;
		    bquote = 0;
		    break;
		}
		if (yyp[1] && yyp[1] != '\'' && yyp[2] == '\'') {
		    if (yyres) memcpy(yyres + yyn, yyp, 3);
		    yyn += 3;
		    yyp += 2;
		    break;
		}
		goto do_not_strip_quotes;

	      case ',':
		goto do_not_strip_quotes;

	      case '\\':
		if (*++yyp != '\\')
		    goto do_not_strip_quotes;
		/* Fall through.  */
	      default_char:
	      default:
		if (yyres)
		    yyres[yyn] = *yyp;
		yyn++;
		break;

	      case '"':
	      case '\0':
		if (yyres)
		    yyres[yyn] = '\0';
		return yyn;
	    }
	}
      do_not_strip_quotes: ;
    }

    if (!yyres) return strlen(yystr);

    return (YYSIZE_T)(yystpcpy(yyres, yystr) - yyres);
}
#endif

#ifdef RIPPER
#ifdef RIPPER_DEBUG
/* :nodoc: */
static VALUE
ripper_validate_object(VALUE self, VALUE x)
{
    if (x == Qfalse) return x;
    if (x == Qtrue) return x;
    if (x == Qnil) return x;
    if (x == Qundef)
	rb_raise(rb_eArgError, "Qundef given");
    if (FIXNUM_P(x)) return x;
    if (SYMBOL_P(x)) return x;
    switch (BUILTIN_TYPE(x)) {
      case T_STRING:
      case T_OBJECT:
      case T_ARRAY:
      case T_BIGNUM:
      case T_FLOAT:
      case T_COMPLEX:
      case T_RATIONAL:
	break;
      case T_NODE:
	if (nd_type((NODE *)x) != NODE_RIPPER) {
	    rb_raise(rb_eArgError, "NODE given: %p", (void *)x);
	}
	x = ((NODE *)x)->nd_rval;
	break;
      default:
	rb_raise(rb_eArgError, "wrong type of ruby object: %p (%s)",
		 (void *)x, rb_obj_classname(x));
    }
    if (!RBASIC_CLASS(x)) {
	rb_raise(rb_eArgError, "hidden ruby object: %p (%s)",
		 (void *)x, rb_builtin_type_name(TYPE(x)));
    }
    return x;
}
#endif

#define validate(x) ((x) = get_value(x))

static VALUE
ripper_dispatch0(struct parser_params *p, ID mid)
{
    return rb_funcall(p->value, mid, 0);
}

static VALUE
ripper_dispatch1(struct parser_params *p, ID mid, VALUE a)
{
    validate(a);
    return rb_funcall(p->value, mid, 1, a);
}

static VALUE
ripper_dispatch2(struct parser_params *p, ID mid, VALUE a, VALUE b)
{
    validate(a);
    validate(b);
    return rb_funcall(p->value, mid, 2, a, b);
}

static VALUE
ripper_dispatch3(struct parser_params *p, ID mid, VALUE a, VALUE b, VALUE c)
{
    validate(a);
    validate(b);
    validate(c);
    return rb_funcall(p->value, mid, 3, a, b, c);
}

static VALUE
ripper_dispatch4(struct parser_params *p, ID mid, VALUE a, VALUE b, VALUE c, VALUE d)
{
    validate(a);
    validate(b);
    validate(c);
    validate(d);
    return rb_funcall(p->value, mid, 4, a, b, c, d);
}

static VALUE
ripper_dispatch5(struct parser_params *p, ID mid, VALUE a, VALUE b, VALUE c, VALUE d, VALUE e)
{
    validate(a);
    validate(b);
    validate(c);
    validate(d);
    validate(e);
    return rb_funcall(p->value, mid, 5, a, b, c, d, e);
}

static VALUE
ripper_dispatch7(struct parser_params *p, ID mid, VALUE a, VALUE b, VALUE c, VALUE d, VALUE e, VALUE f, VALUE g)
{
    validate(a);
    validate(b);
    validate(c);
    validate(d);
    validate(e);
    validate(f);
    validate(g);
    return rb_funcall(p->value, mid, 7, a, b, c, d, e, f, g);
}

static ID
ripper_get_id(VALUE v)
{
    NODE *nd;
    if (!RB_TYPE_P(v, T_NODE)) return 0;
    nd = (NODE *)v;
    if (nd_type(nd) != NODE_RIPPER) return 0;
    return nd->nd_vid;
}

static VALUE
ripper_get_value(VALUE v)
{
    NODE *nd;
    if (v == Qundef) return Qnil;
    if (!RB_TYPE_P(v, T_NODE)) return v;
    nd = (NODE *)v;
    if (nd_type(nd) != NODE_RIPPER) return Qnil;
    return nd->nd_rval;
}

static void
ripper_error(struct parser_params *p)
{
    p->error_p = TRUE;
}

static void
ripper_compile_error(struct parser_params *p, const char *fmt, ...)
{
    VALUE str;
    va_list args;

    va_start(args, fmt);
    str = rb_vsprintf(fmt, args);
    va_end(args);
    rb_funcall(p->value, rb_intern("compile_error"), 1, str);
    ripper_error(p);
}

static VALUE
ripper_lex_get_generic(struct parser_params *p, VALUE src)
{
    VALUE line = rb_funcallv_public(src, id_gets, 0, 0);
    if (!NIL_P(line) && !RB_TYPE_P(line, T_STRING)) {
	rb_raise(rb_eTypeError,
		 "gets returned %"PRIsVALUE" (expected String or nil)",
		 rb_obj_class(line));
    }
    return line;
}

static VALUE
ripper_lex_io_get(struct parser_params *p, VALUE src)
{
    return rb_io_gets(src);
}

static VALUE
ripper_s_allocate(VALUE klass)
{
    struct parser_params *p;
    VALUE self = TypedData_Make_Struct(klass, struct parser_params,
				       &parser_data_type, p);
    p->value = self;
    return self;
}

#define ripper_initialized_p(r) ((r)->lex.input != 0)

/*
 *  call-seq:
 *    Ripper.new(src, filename="(ripper)", lineno=1) -> ripper
 *
 *  Create a new Ripper object.
 *  _src_ must be a String, an IO, or an Object which has #gets method.
 *
 *  This method does not starts parsing.
 *  See also Ripper#parse and Ripper.parse.
 */
static VALUE
ripper_initialize(int argc, VALUE *argv, VALUE self)
{
    struct parser_params *p;
    VALUE src, fname, lineno;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    rb_scan_args(argc, argv, "12", &src, &fname, &lineno);
    if (RB_TYPE_P(src, T_FILE)) {
        p->lex.gets = ripper_lex_io_get;
    }
    else if (rb_respond_to(src, id_gets)) {
        p->lex.gets = ripper_lex_get_generic;
    }
    else {
        StringValue(src);
        p->lex.gets = lex_get_str;
    }
    p->lex.input = src;
    p->eofp = 0;
    if (NIL_P(fname)) {
        fname = STR_NEW2("(ripper)");
	OBJ_FREEZE(fname);
    }
    else {
	StringValueCStr(fname);
	fname = rb_str_new_frozen(fname);
    }
    parser_initialize(p);

    p->ruby_sourcefile_string = fname;
    p->ruby_sourcefile = RSTRING_PTR(fname);
    p->ruby_sourceline = NIL_P(lineno) ? 0 : NUM2INT(lineno) - 1;

    return Qnil;
}

static VALUE
ripper_parse0(VALUE parser_v)
{
    struct parser_params *p;

    TypedData_Get_Struct(parser_v, struct parser_params, &parser_data_type, p);
    parser_prepare(p);
    p->ast = rb_ast_new();
    ripper_yyparse((void*)p);
    rb_ast_dispose(p->ast);
    p->ast = 0;
    return p->result;
}

static VALUE
ripper_ensure(VALUE parser_v)
{
    struct parser_params *p;

    TypedData_Get_Struct(parser_v, struct parser_params, &parser_data_type, p);
    p->parsing_thread = Qnil;
    return Qnil;
}

/*
 *  call-seq:
 *    ripper.parse
 *
 *  Start parsing and returns the value of the root action.
 */
static VALUE
ripper_parse(VALUE self)
{
    struct parser_params *p;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    if (!ripper_initialized_p(p)) {
        rb_raise(rb_eArgError, "method called for uninitialized object");
    }
    if (!NIL_P(p->parsing_thread)) {
        if (p->parsing_thread == rb_thread_current())
            rb_raise(rb_eArgError, "Ripper#parse is not reentrant");
        else
            rb_raise(rb_eArgError, "Ripper#parse is not multithread-safe");
    }
    p->parsing_thread = rb_thread_current();
    rb_ensure(ripper_parse0, self, ripper_ensure, self);

    return p->result;
}

/*
 *  call-seq:
 *    ripper.column   -> Integer
 *
 *  Return column number of current parsing line.
 *  This number starts from 0.
 */
static VALUE
ripper_column(VALUE self)
{
    struct parser_params *p;
    long col;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    if (!ripper_initialized_p(p)) {
        rb_raise(rb_eArgError, "method called for uninitialized object");
    }
    if (NIL_P(p->parsing_thread)) return Qnil;
    col = p->lex.ptok - p->lex.pbeg;
    return LONG2NUM(col);
}

/*
 *  call-seq:
 *    ripper.filename   -> String
 *
 *  Return current parsing filename.
 */
static VALUE
ripper_filename(VALUE self)
{
    struct parser_params *p;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    if (!ripper_initialized_p(p)) {
        rb_raise(rb_eArgError, "method called for uninitialized object");
    }
    return p->ruby_sourcefile_string;
}

/*
 *  call-seq:
 *    ripper.lineno   -> Integer
 *
 *  Return line number of current parsing line.
 *  This number starts from 1.
 */
static VALUE
ripper_lineno(VALUE self)
{
    struct parser_params *p;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    if (!ripper_initialized_p(p)) {
        rb_raise(rb_eArgError, "method called for uninitialized object");
    }
    if (NIL_P(p->parsing_thread)) return Qnil;
    return INT2NUM(p->ruby_sourceline);
}

/*
 *  call-seq:
 *    ripper.state   -> Integer
 *
 *  Return scanner state of current token.
 */
static VALUE
ripper_state(VALUE self)
{
    struct parser_params *p;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    if (!ripper_initialized_p(p)) {
	rb_raise(rb_eArgError, "method called for uninitialized object");
    }
    if (NIL_P(p->parsing_thread)) return Qnil;
    return INT2NUM(p->lex.state);
}

/*
 *  call-seq:
 *    ripper.token   -> String
 *
 *  Return the current token string.
 */
static VALUE
ripper_token(VALUE self)
{
    struct parser_params *p;
    long pos, len;

    TypedData_Get_Struct(self, struct parser_params, &parser_data_type, p);
    if (!ripper_initialized_p(p)) {
        rb_raise(rb_eArgError, "method called for uninitialized object");
    }
    if (NIL_P(p->parsing_thread)) return Qnil;
    pos = p->lex.ptok - p->lex.pbeg;
    len = p->lex.pcur - p->lex.ptok;
    return rb_str_subseq(p->lex.lastline, pos, len);
}

#ifdef RIPPER_DEBUG
/* :nodoc: */
static VALUE
ripper_assert_Qundef(VALUE self, VALUE obj, VALUE msg)
{
    StringValue(msg);
    if (obj == Qundef) {
        rb_raise(rb_eArgError, "%"PRIsVALUE, msg);
    }
    return Qnil;
}

/* :nodoc: */
static VALUE
ripper_value(VALUE self, VALUE obj)
{
    return ULONG2NUM(obj);
}
#endif

/*
 *  call-seq:
 *    Ripper.lex_state_name(integer)   -> string
 *
 *  Returns a string representation of lex_state.
 */
static VALUE
ripper_lex_state_name(VALUE self, VALUE state)
{
    return rb_parser_lex_state_name(NUM2INT(state));
}

void
Init_ripper(void)
{
    ripper_init_eventids1();
    ripper_init_eventids2();
    id_warn = rb_intern_const("warn");
    id_warning = rb_intern_const("warning");
    id_gets = rb_intern_const("gets");
    id_assoc = rb_intern_const("=>");

    (void)yystpcpy; /* may not used in newer bison */

    InitVM(ripper);
}

void
InitVM_ripper(void)
{
    VALUE Ripper;

    Ripper = rb_define_class("Ripper", rb_cObject);
    /* version of Ripper */
    rb_define_const(Ripper, "Version", rb_usascii_str_new2(RIPPER_VERSION));
    rb_define_alloc_func(Ripper, ripper_s_allocate);
    rb_define_method(Ripper, "initialize", ripper_initialize, -1);
    rb_define_method(Ripper, "parse", ripper_parse, 0);
    rb_define_method(Ripper, "column", ripper_column, 0);
    rb_define_method(Ripper, "filename", ripper_filename, 0);
    rb_define_method(Ripper, "lineno", ripper_lineno, 0);
    rb_define_method(Ripper, "state", ripper_state, 0);
    rb_define_method(Ripper, "token", ripper_token, 0);
    rb_define_method(Ripper, "end_seen?", rb_parser_end_seen_p, 0);
    rb_define_method(Ripper, "encoding", rb_parser_encoding, 0);
    rb_define_method(Ripper, "yydebug", rb_parser_get_yydebug, 0);
    rb_define_method(Ripper, "yydebug=", rb_parser_set_yydebug, 1);
    rb_define_method(Ripper, "debug_output", rb_parser_get_debug_output, 0);
    rb_define_method(Ripper, "debug_output=", rb_parser_set_debug_output, 1);
    rb_define_method(Ripper, "error?", ripper_error_p, 0);
#ifdef RIPPER_DEBUG
    rb_define_method(Ripper, "assert_Qundef", ripper_assert_Qundef, 2);
    rb_define_method(Ripper, "rawVALUE", ripper_value, 1);
    rb_define_method(Ripper, "validate_object", ripper_validate_object, 1);
#endif

    rb_define_singleton_method(Ripper, "dedent_string", parser_dedent_string, 2);
    rb_define_private_method(Ripper, "dedent_string", parser_dedent_string, 2);

    rb_define_singleton_method(Ripper, "lex_state_name", ripper_lex_state_name, 1);

<% @exprs.each do |expr, desc| -%>
    /* <%=desc%> */
    rb_define_const(Ripper, "<%=expr%>", INT2NUM(<%=expr%>));
<% end %>
    ripper_init_eventids1_table(Ripper);
    ripper_init_eventids2_table(Ripper);

# if 0
    /* Hack to let RDoc document SCRIPT_LINES__ */

    /*
     * When a Hash is assigned to +SCRIPT_LINES__+ the contents of files loaded
     * after the assignment will be added as an Array of lines with the file
     * name as the key.
     */
    rb_define_global_const("SCRIPT_LINES__", Qnil);
#endif

}
#endif /* RIPPER */

/*
 * Local variables:
 * mode: c
 * c-file-style: "ruby"
 * End:
 */
