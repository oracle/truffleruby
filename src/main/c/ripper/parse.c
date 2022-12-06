/* A Bison parser, made by GNU Bison 3.8.2.  */

/* Bison implementation for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2015, 2018-2021 Free Software Foundation,
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
   along with this program.  If not, see <https://www.gnu.org/licenses/>.  */

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

/* DO NOT RELY ON FEATURES THAT ARE NOT DOCUMENTED in the manual,
   especially those whose name start with YY_ or yy_.  They are
   private implementation details that can be changed or removed.  */

/* All symbols defined below should begin with yy or YY, to avoid
   infringing on user name space.  This should be done even for local
   variables, as they might otherwise be expanded by user macros.
   There are some unavoidable exceptions within include files to
   define necessary library symbols; they are noted "INFRINGES ON
   USER NAME SPACE" below.  */

/* Identify Bison output, and Bison version.  */
#define YYBISON 30802

/* Bison version string.  */
#define YYBISON_VERSION "3.8.2"

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
    unsigned int in_argdef: 1;
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
#define YY_(Msgid) \
    (((Msgid)[0] == 'm') && (strcmp((Msgid), "memory exhausted") == 0) ? \
     "nesting too deep" : (Msgid))

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
    parser_set_lex_state(p, ls, __LINE__)
static inline enum lex_state_e parser_set_lex_state(struct parser_params *p, enum lex_state_e ls, int line);

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
    unsigned int keep_script_lines: 1;

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

RBIMPL_ATTR_NONNULL((1, 2, 3))
static int parser_yyerror(struct parser_params*, const YYLTYPE *yylloc, const char*);
RBIMPL_ATTR_NONNULL((1, 2))
static int parser_yyerror0(struct parser_params*, const char*);
#define yyerror0(msg) parser_yyerror0(p, (msg))
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

#define ANON_BLOCK_ID '&'

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

static rb_ast_id_table_t *local_tbl(struct parser_params*);

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
    return RB_TYPE_P(n, T_NODE) && nd_type_p(RNODE(n), NODE_RIPPER);
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

#line 1146 "parse.c"

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

/* Token kinds.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
  enum yytokentype
  {
    YYEMPTY = -2,
    END_OF_INPUT = 0,              /* "end-of-input"  */
    YYerror = 256,                 /* error  */
    YYUNDEF = 257,                 /* "invalid token"  */
    keyword_class = 258,           /* "`class'"  */
    keyword_module = 259,          /* "`module'"  */
    keyword_def = 260,             /* "`def'"  */
    keyword_undef = 261,           /* "`undef'"  */
    keyword_begin = 262,           /* "`begin'"  */
    keyword_rescue = 263,          /* "`rescue'"  */
    keyword_ensure = 264,          /* "`ensure'"  */
    keyword_end = 265,             /* "`end'"  */
    keyword_if = 266,              /* "`if'"  */
    keyword_unless = 267,          /* "`unless'"  */
    keyword_then = 268,            /* "`then'"  */
    keyword_elsif = 269,           /* "`elsif'"  */
    keyword_else = 270,            /* "`else'"  */
    keyword_case = 271,            /* "`case'"  */
    keyword_when = 272,            /* "`when'"  */
    keyword_while = 273,           /* "`while'"  */
    keyword_until = 274,           /* "`until'"  */
    keyword_for = 275,             /* "`for'"  */
    keyword_break = 276,           /* "`break'"  */
    keyword_next = 277,            /* "`next'"  */
    keyword_redo = 278,            /* "`redo'"  */
    keyword_retry = 279,           /* "`retry'"  */
    keyword_in = 280,              /* "`in'"  */
    keyword_do = 281,              /* "`do'"  */
    keyword_do_cond = 282,         /* "`do' for condition"  */
    keyword_do_block = 283,        /* "`do' for block"  */
    keyword_do_LAMBDA = 284,       /* "`do' for lambda"  */
    keyword_return = 285,          /* "`return'"  */
    keyword_yield = 286,           /* "`yield'"  */
    keyword_super = 287,           /* "`super'"  */
    keyword_self = 288,            /* "`self'"  */
    keyword_nil = 289,             /* "`nil'"  */
    keyword_true = 290,            /* "`true'"  */
    keyword_false = 291,           /* "`false'"  */
    keyword_and = 292,             /* "`and'"  */
    keyword_or = 293,              /* "`or'"  */
    keyword_not = 294,             /* "`not'"  */
    modifier_if = 295,             /* "`if' modifier"  */
    modifier_unless = 296,         /* "`unless' modifier"  */
    modifier_while = 297,          /* "`while' modifier"  */
    modifier_until = 298,          /* "`until' modifier"  */
    modifier_rescue = 299,         /* "`rescue' modifier"  */
    keyword_alias = 300,           /* "`alias'"  */
    keyword_defined = 301,         /* "`defined?'"  */
    keyword_BEGIN = 302,           /* "`BEGIN'"  */
    keyword_END = 303,             /* "`END'"  */
    keyword__LINE__ = 304,         /* "`__LINE__'"  */
    keyword__FILE__ = 305,         /* "`__FILE__'"  */
    keyword__ENCODING__ = 306,     /* "`__ENCODING__'"  */
    tIDENTIFIER = 307,             /* "local variable or method"  */
    tFID = 308,                    /* "method"  */
    tGVAR = 309,                   /* "global variable"  */
    tIVAR = 310,                   /* "instance variable"  */
    tCONSTANT = 311,               /* "constant"  */
    tCVAR = 312,                   /* "class variable"  */
    tLABEL = 313,                  /* "label"  */
    tINTEGER = 314,                /* "integer literal"  */
    tFLOAT = 315,                  /* "float literal"  */
    tRATIONAL = 316,               /* "rational literal"  */
    tIMAGINARY = 317,              /* "imaginary literal"  */
    tCHAR = 318,                   /* "char literal"  */
    tNTH_REF = 319,                /* "numbered reference"  */
    tBACK_REF = 320,               /* "back reference"  */
    tSTRING_CONTENT = 321,         /* "literal content"  */
    tREGEXP_END = 322,             /* tREGEXP_END  */
    tSP = 323,                     /* "escaped space"  */
    tUPLUS = 132,                  /* "unary+"  */
    tUMINUS = 133,                 /* "unary-"  */
    tPOW = 134,                    /* "**"  */
    tCMP = 135,                    /* "<=>"  */
    tEQ = 140,                     /* "=="  */
    tEQQ = 141,                    /* "==="  */
    tNEQ = 142,                    /* "!="  */
    tGEQ = 139,                    /* ">="  */
    tLEQ = 138,                    /* "<="  */
    tANDOP = 148,                  /* "&&"  */
    tOROP = 149,                   /* "||"  */
    tMATCH = 143,                  /* "=~"  */
    tNMATCH = 144,                 /* "!~"  */
    tDOT2 = 128,                   /* ".."  */
    tDOT3 = 129,                   /* "..."  */
    tBDOT2 = 130,                  /* "(.."  */
    tBDOT3 = 131,                  /* "(..."  */
    tAREF = 145,                   /* "[]"  */
    tASET = 146,                   /* "[]="  */
    tLSHFT = 136,                  /* "<<"  */
    tRSHFT = 137,                  /* ">>"  */
    tANDDOT = 150,                 /* "&."  */
    tCOLON2 = 147,                 /* "::"  */
    tCOLON3 = 324,                 /* ":: at EXPR_BEG"  */
    tOP_ASGN = 325,                /* "operator-assignment"  */
    tASSOC = 326,                  /* "=>"  */
    tLPAREN = 327,                 /* "("  */
    tLPAREN_ARG = 328,             /* "( arg"  */
    tRPAREN = 329,                 /* ")"  */
    tLBRACK = 330,                 /* "["  */
    tLBRACE = 331,                 /* "{"  */
    tLBRACE_ARG = 332,             /* "{ arg"  */
    tSTAR = 333,                   /* "*"  */
    tDSTAR = 334,                  /* "**arg"  */
    tAMPER = 335,                  /* "&"  */
    tLAMBDA = 336,                 /* "->"  */
    tSYMBEG = 337,                 /* "symbol literal"  */
    tSTRING_BEG = 338,             /* "string literal"  */
    tXSTRING_BEG = 339,            /* "backtick literal"  */
    tREGEXP_BEG = 340,             /* "regexp literal"  */
    tWORDS_BEG = 341,              /* "word list"  */
    tQWORDS_BEG = 342,             /* "verbatim word list"  */
    tSYMBOLS_BEG = 343,            /* "symbol list"  */
    tQSYMBOLS_BEG = 344,           /* "verbatim symbol list"  */
    tSTRING_END = 345,             /* "terminator"  */
    tSTRING_DEND = 346,            /* "'}'"  */
    tSTRING_DBEG = 347,            /* tSTRING_DBEG  */
    tSTRING_DVAR = 348,            /* tSTRING_DVAR  */
    tLAMBEG = 349,                 /* tLAMBEG  */
    tLABEL_END = 350,              /* tLABEL_END  */
    tLOWEST = 351,                 /* tLOWEST  */
    tUMINUS_NUM = 352,             /* tUMINUS_NUM  */
    tLAST_TOKEN = 353              /* tLAST_TOKEN  */
  };
  typedef enum yytokentype yytoken_kind_t;
#endif

/* Value type.  */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
union YYSTYPE
{
#line 1126 "parse.y"

    VALUE val;
    NODE *node;
    ID id;
    int num;
    st_table *tbl;
    const struct vtable *vars;
    struct rb_strterm_struct *strterm;
    struct lex_context ctxt;

#line 1328 "parse.c"

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
/* Symbol kind.  */
enum yysymbol_kind_t
{
  YYSYMBOL_YYEMPTY = -2,
  YYSYMBOL_YYEOF = 0,                      /* "end-of-input"  */
  YYSYMBOL_YYerror = 1,                    /* error  */
  YYSYMBOL_YYUNDEF = 2,                    /* "invalid token"  */
  YYSYMBOL_keyword_class = 3,              /* "`class'"  */
  YYSYMBOL_keyword_module = 4,             /* "`module'"  */
  YYSYMBOL_keyword_def = 5,                /* "`def'"  */
  YYSYMBOL_keyword_undef = 6,              /* "`undef'"  */
  YYSYMBOL_keyword_begin = 7,              /* "`begin'"  */
  YYSYMBOL_keyword_rescue = 8,             /* "`rescue'"  */
  YYSYMBOL_keyword_ensure = 9,             /* "`ensure'"  */
  YYSYMBOL_keyword_end = 10,               /* "`end'"  */
  YYSYMBOL_keyword_if = 11,                /* "`if'"  */
  YYSYMBOL_keyword_unless = 12,            /* "`unless'"  */
  YYSYMBOL_keyword_then = 13,              /* "`then'"  */
  YYSYMBOL_keyword_elsif = 14,             /* "`elsif'"  */
  YYSYMBOL_keyword_else = 15,              /* "`else'"  */
  YYSYMBOL_keyword_case = 16,              /* "`case'"  */
  YYSYMBOL_keyword_when = 17,              /* "`when'"  */
  YYSYMBOL_keyword_while = 18,             /* "`while'"  */
  YYSYMBOL_keyword_until = 19,             /* "`until'"  */
  YYSYMBOL_keyword_for = 20,               /* "`for'"  */
  YYSYMBOL_keyword_break = 21,             /* "`break'"  */
  YYSYMBOL_keyword_next = 22,              /* "`next'"  */
  YYSYMBOL_keyword_redo = 23,              /* "`redo'"  */
  YYSYMBOL_keyword_retry = 24,             /* "`retry'"  */
  YYSYMBOL_keyword_in = 25,                /* "`in'"  */
  YYSYMBOL_keyword_do = 26,                /* "`do'"  */
  YYSYMBOL_keyword_do_cond = 27,           /* "`do' for condition"  */
  YYSYMBOL_keyword_do_block = 28,          /* "`do' for block"  */
  YYSYMBOL_keyword_do_LAMBDA = 29,         /* "`do' for lambda"  */
  YYSYMBOL_keyword_return = 30,            /* "`return'"  */
  YYSYMBOL_keyword_yield = 31,             /* "`yield'"  */
  YYSYMBOL_keyword_super = 32,             /* "`super'"  */
  YYSYMBOL_keyword_self = 33,              /* "`self'"  */
  YYSYMBOL_keyword_nil = 34,               /* "`nil'"  */
  YYSYMBOL_keyword_true = 35,              /* "`true'"  */
  YYSYMBOL_keyword_false = 36,             /* "`false'"  */
  YYSYMBOL_keyword_and = 37,               /* "`and'"  */
  YYSYMBOL_keyword_or = 38,                /* "`or'"  */
  YYSYMBOL_keyword_not = 39,               /* "`not'"  */
  YYSYMBOL_modifier_if = 40,               /* "`if' modifier"  */
  YYSYMBOL_modifier_unless = 41,           /* "`unless' modifier"  */
  YYSYMBOL_modifier_while = 42,            /* "`while' modifier"  */
  YYSYMBOL_modifier_until = 43,            /* "`until' modifier"  */
  YYSYMBOL_modifier_rescue = 44,           /* "`rescue' modifier"  */
  YYSYMBOL_keyword_alias = 45,             /* "`alias'"  */
  YYSYMBOL_keyword_defined = 46,           /* "`defined?'"  */
  YYSYMBOL_keyword_BEGIN = 47,             /* "`BEGIN'"  */
  YYSYMBOL_keyword_END = 48,               /* "`END'"  */
  YYSYMBOL_keyword__LINE__ = 49,           /* "`__LINE__'"  */
  YYSYMBOL_keyword__FILE__ = 50,           /* "`__FILE__'"  */
  YYSYMBOL_keyword__ENCODING__ = 51,       /* "`__ENCODING__'"  */
  YYSYMBOL_tIDENTIFIER = 52,               /* "local variable or method"  */
  YYSYMBOL_tFID = 53,                      /* "method"  */
  YYSYMBOL_tGVAR = 54,                     /* "global variable"  */
  YYSYMBOL_tIVAR = 55,                     /* "instance variable"  */
  YYSYMBOL_tCONSTANT = 56,                 /* "constant"  */
  YYSYMBOL_tCVAR = 57,                     /* "class variable"  */
  YYSYMBOL_tLABEL = 58,                    /* "label"  */
  YYSYMBOL_tINTEGER = 59,                  /* "integer literal"  */
  YYSYMBOL_tFLOAT = 60,                    /* "float literal"  */
  YYSYMBOL_tRATIONAL = 61,                 /* "rational literal"  */
  YYSYMBOL_tIMAGINARY = 62,                /* "imaginary literal"  */
  YYSYMBOL_tCHAR = 63,                     /* "char literal"  */
  YYSYMBOL_tNTH_REF = 64,                  /* "numbered reference"  */
  YYSYMBOL_tBACK_REF = 65,                 /* "back reference"  */
  YYSYMBOL_tSTRING_CONTENT = 66,           /* "literal content"  */
  YYSYMBOL_tREGEXP_END = 67,               /* tREGEXP_END  */
  YYSYMBOL_68_ = 68,                       /* '.'  */
  YYSYMBOL_69_backslash_ = 69,             /* "backslash"  */
  YYSYMBOL_tSP = 70,                       /* "escaped space"  */
  YYSYMBOL_71_escaped_horizontal_tab_ = 71, /* "escaped horizontal tab"  */
  YYSYMBOL_72_escaped_form_feed_ = 72,     /* "escaped form feed"  */
  YYSYMBOL_73_escaped_carriage_return_ = 73, /* "escaped carriage return"  */
  YYSYMBOL_74_escaped_vertical_tab_ = 74,  /* "escaped vertical tab"  */
  YYSYMBOL_tUPLUS = 75,                    /* "unary+"  */
  YYSYMBOL_tUMINUS = 76,                   /* "unary-"  */
  YYSYMBOL_tPOW = 77,                      /* "**"  */
  YYSYMBOL_tCMP = 78,                      /* "<=>"  */
  YYSYMBOL_tEQ = 79,                       /* "=="  */
  YYSYMBOL_tEQQ = 80,                      /* "==="  */
  YYSYMBOL_tNEQ = 81,                      /* "!="  */
  YYSYMBOL_tGEQ = 82,                      /* ">="  */
  YYSYMBOL_tLEQ = 83,                      /* "<="  */
  YYSYMBOL_tANDOP = 84,                    /* "&&"  */
  YYSYMBOL_tOROP = 85,                     /* "||"  */
  YYSYMBOL_tMATCH = 86,                    /* "=~"  */
  YYSYMBOL_tNMATCH = 87,                   /* "!~"  */
  YYSYMBOL_tDOT2 = 88,                     /* ".."  */
  YYSYMBOL_tDOT3 = 89,                     /* "..."  */
  YYSYMBOL_tBDOT2 = 90,                    /* "(.."  */
  YYSYMBOL_tBDOT3 = 91,                    /* "(..."  */
  YYSYMBOL_tAREF = 92,                     /* "[]"  */
  YYSYMBOL_tASET = 93,                     /* "[]="  */
  YYSYMBOL_tLSHFT = 94,                    /* "<<"  */
  YYSYMBOL_tRSHFT = 95,                    /* ">>"  */
  YYSYMBOL_tANDDOT = 96,                   /* "&."  */
  YYSYMBOL_tCOLON2 = 97,                   /* "::"  */
  YYSYMBOL_tCOLON3 = 98,                   /* ":: at EXPR_BEG"  */
  YYSYMBOL_tOP_ASGN = 99,                  /* "operator-assignment"  */
  YYSYMBOL_tASSOC = 100,                   /* "=>"  */
  YYSYMBOL_tLPAREN = 101,                  /* "("  */
  YYSYMBOL_tLPAREN_ARG = 102,              /* "( arg"  */
  YYSYMBOL_tRPAREN = 103,                  /* ")"  */
  YYSYMBOL_tLBRACK = 104,                  /* "["  */
  YYSYMBOL_tLBRACE = 105,                  /* "{"  */
  YYSYMBOL_tLBRACE_ARG = 106,              /* "{ arg"  */
  YYSYMBOL_tSTAR = 107,                    /* "*"  */
  YYSYMBOL_tDSTAR = 108,                   /* "**arg"  */
  YYSYMBOL_tAMPER = 109,                   /* "&"  */
  YYSYMBOL_tLAMBDA = 110,                  /* "->"  */
  YYSYMBOL_tSYMBEG = 111,                  /* "symbol literal"  */
  YYSYMBOL_tSTRING_BEG = 112,              /* "string literal"  */
  YYSYMBOL_tXSTRING_BEG = 113,             /* "backtick literal"  */
  YYSYMBOL_tREGEXP_BEG = 114,              /* "regexp literal"  */
  YYSYMBOL_tWORDS_BEG = 115,               /* "word list"  */
  YYSYMBOL_tQWORDS_BEG = 116,              /* "verbatim word list"  */
  YYSYMBOL_tSYMBOLS_BEG = 117,             /* "symbol list"  */
  YYSYMBOL_tQSYMBOLS_BEG = 118,            /* "verbatim symbol list"  */
  YYSYMBOL_tSTRING_END = 119,              /* "terminator"  */
  YYSYMBOL_tSTRING_DEND = 120,             /* "'}'"  */
  YYSYMBOL_tSTRING_DBEG = 121,             /* tSTRING_DBEG  */
  YYSYMBOL_tSTRING_DVAR = 122,             /* tSTRING_DVAR  */
  YYSYMBOL_tLAMBEG = 123,                  /* tLAMBEG  */
  YYSYMBOL_tLABEL_END = 124,               /* tLABEL_END  */
  YYSYMBOL_tLOWEST = 125,                  /* tLOWEST  */
  YYSYMBOL_126_ = 126,                     /* '='  */
  YYSYMBOL_127_ = 127,                     /* '?'  */
  YYSYMBOL_128_ = 128,                     /* ':'  */
  YYSYMBOL_129_ = 129,                     /* '>'  */
  YYSYMBOL_130_ = 130,                     /* '<'  */
  YYSYMBOL_131_ = 131,                     /* '|'  */
  YYSYMBOL_132_ = 132,                     /* '^'  */
  YYSYMBOL_133_ = 133,                     /* '&'  */
  YYSYMBOL_134_ = 134,                     /* '+'  */
  YYSYMBOL_135_ = 135,                     /* '-'  */
  YYSYMBOL_136_ = 136,                     /* '*'  */
  YYSYMBOL_137_ = 137,                     /* '/'  */
  YYSYMBOL_138_ = 138,                     /* '%'  */
  YYSYMBOL_tUMINUS_NUM = 139,              /* tUMINUS_NUM  */
  YYSYMBOL_140_ = 140,                     /* '!'  */
  YYSYMBOL_141_ = 141,                     /* '~'  */
  YYSYMBOL_tLAST_TOKEN = 142,              /* tLAST_TOKEN  */
  YYSYMBOL_143_ = 143,                     /* '{'  */
  YYSYMBOL_144_ = 144,                     /* '}'  */
  YYSYMBOL_145_ = 145,                     /* '['  */
  YYSYMBOL_146_ = 146,                     /* ','  */
  YYSYMBOL_147_ = 147,                     /* '`'  */
  YYSYMBOL_148_ = 148,                     /* '('  */
  YYSYMBOL_149_ = 149,                     /* ')'  */
  YYSYMBOL_150_ = 150,                     /* ']'  */
  YYSYMBOL_151_ = 151,                     /* ';'  */
  YYSYMBOL_152_ = 152,                     /* ' '  */
  YYSYMBOL_153_n_ = 153,                   /* '\n'  */
  YYSYMBOL_YYACCEPT = 154,                 /* $accept  */
  YYSYMBOL_program = 155,                  /* program  */
  YYSYMBOL_156_1 = 156,                    /* $@1  */
  YYSYMBOL_top_compstmt = 157,             /* top_compstmt  */
  YYSYMBOL_top_stmts = 158,                /* top_stmts  */
  YYSYMBOL_top_stmt = 159,                 /* top_stmt  */
  YYSYMBOL_begin_block = 160,              /* begin_block  */
  YYSYMBOL_bodystmt = 161,                 /* bodystmt  */
  YYSYMBOL_162_2 = 162,                    /* $@2  */
  YYSYMBOL_compstmt = 163,                 /* compstmt  */
  YYSYMBOL_stmts = 164,                    /* stmts  */
  YYSYMBOL_stmt_or_begin = 165,            /* stmt_or_begin  */
  YYSYMBOL_166_3 = 166,                    /* $@3  */
  YYSYMBOL_stmt = 167,                     /* stmt  */
  YYSYMBOL_168_4 = 168,                    /* $@4  */
  YYSYMBOL_command_asgn = 169,             /* command_asgn  */
  YYSYMBOL_command_rhs = 170,              /* command_rhs  */
  YYSYMBOL_expr = 171,                     /* expr  */
  YYSYMBOL_172_5 = 172,                    /* @5  */
  YYSYMBOL_173_6 = 173,                    /* @6  */
  YYSYMBOL_174_7 = 174,                    /* @7  */
  YYSYMBOL_175_8 = 175,                    /* @8  */
  YYSYMBOL_def_name = 176,                 /* def_name  */
  YYSYMBOL_defn_head = 177,                /* defn_head  */
  YYSYMBOL_defs_head = 178,                /* defs_head  */
  YYSYMBOL_179_9 = 179,                    /* $@9  */
  YYSYMBOL_expr_value = 180,               /* expr_value  */
  YYSYMBOL_expr_value_do = 181,            /* expr_value_do  */
  YYSYMBOL_182_10 = 182,                   /* $@10  */
  YYSYMBOL_183_11 = 183,                   /* $@11  */
  YYSYMBOL_command_call = 184,             /* command_call  */
  YYSYMBOL_block_command = 185,            /* block_command  */
  YYSYMBOL_cmd_brace_block = 186,          /* cmd_brace_block  */
  YYSYMBOL_fcall = 187,                    /* fcall  */
  YYSYMBOL_command = 188,                  /* command  */
  YYSYMBOL_mlhs = 189,                     /* mlhs  */
  YYSYMBOL_mlhs_inner = 190,               /* mlhs_inner  */
  YYSYMBOL_mlhs_basic = 191,               /* mlhs_basic  */
  YYSYMBOL_mlhs_item = 192,                /* mlhs_item  */
  YYSYMBOL_mlhs_head = 193,                /* mlhs_head  */
  YYSYMBOL_mlhs_post = 194,                /* mlhs_post  */
  YYSYMBOL_mlhs_node = 195,                /* mlhs_node  */
  YYSYMBOL_lhs = 196,                      /* lhs  */
  YYSYMBOL_cname = 197,                    /* cname  */
  YYSYMBOL_cpath = 198,                    /* cpath  */
  YYSYMBOL_fname = 199,                    /* fname  */
  YYSYMBOL_fitem = 200,                    /* fitem  */
  YYSYMBOL_undef_list = 201,               /* undef_list  */
  YYSYMBOL_202_12 = 202,                   /* $@12  */
  YYSYMBOL_op = 203,                       /* op  */
  YYSYMBOL_reswords = 204,                 /* reswords  */
  YYSYMBOL_arg = 205,                      /* arg  */
  YYSYMBOL_206_13 = 206,                   /* $@13  */
  YYSYMBOL_relop = 207,                    /* relop  */
  YYSYMBOL_rel_expr = 208,                 /* rel_expr  */
  YYSYMBOL_lex_ctxt = 209,                 /* lex_ctxt  */
  YYSYMBOL_arg_value = 210,                /* arg_value  */
  YYSYMBOL_aref_args = 211,                /* aref_args  */
  YYSYMBOL_arg_rhs = 212,                  /* arg_rhs  */
  YYSYMBOL_paren_args = 213,               /* paren_args  */
  YYSYMBOL_opt_paren_args = 214,           /* opt_paren_args  */
  YYSYMBOL_opt_call_args = 215,            /* opt_call_args  */
  YYSYMBOL_call_args = 216,                /* call_args  */
  YYSYMBOL_command_args = 217,             /* command_args  */
  YYSYMBOL_218_14 = 218,                   /* $@14  */
  YYSYMBOL_block_arg = 219,                /* block_arg  */
  YYSYMBOL_opt_block_arg = 220,            /* opt_block_arg  */
  YYSYMBOL_args = 221,                     /* args  */
  YYSYMBOL_mrhs_arg = 222,                 /* mrhs_arg  */
  YYSYMBOL_mrhs = 223,                     /* mrhs  */
  YYSYMBOL_primary = 224,                  /* primary  */
  YYSYMBOL_225_15 = 225,                   /* $@15  */
  YYSYMBOL_226_16 = 226,                   /* $@16  */
  YYSYMBOL_227_17 = 227,                   /* $@17  */
  YYSYMBOL_228_18 = 228,                   /* $@18  */
  YYSYMBOL_229_19 = 229,                   /* @19  */
  YYSYMBOL_230_20 = 230,                   /* @20  */
  YYSYMBOL_231_21 = 231,                   /* $@21  */
  YYSYMBOL_232_22 = 232,                   /* $@22  */
  YYSYMBOL_233_23 = 233,                   /* $@23  */
  YYSYMBOL_primary_value = 234,            /* primary_value  */
  YYSYMBOL_k_begin = 235,                  /* k_begin  */
  YYSYMBOL_k_if = 236,                     /* k_if  */
  YYSYMBOL_k_unless = 237,                 /* k_unless  */
  YYSYMBOL_k_while = 238,                  /* k_while  */
  YYSYMBOL_k_until = 239,                  /* k_until  */
  YYSYMBOL_k_case = 240,                   /* k_case  */
  YYSYMBOL_k_for = 241,                    /* k_for  */
  YYSYMBOL_k_class = 242,                  /* k_class  */
  YYSYMBOL_k_module = 243,                 /* k_module  */
  YYSYMBOL_k_def = 244,                    /* k_def  */
  YYSYMBOL_k_do = 245,                     /* k_do  */
  YYSYMBOL_k_do_block = 246,               /* k_do_block  */
  YYSYMBOL_k_rescue = 247,                 /* k_rescue  */
  YYSYMBOL_k_ensure = 248,                 /* k_ensure  */
  YYSYMBOL_k_when = 249,                   /* k_when  */
  YYSYMBOL_k_else = 250,                   /* k_else  */
  YYSYMBOL_k_elsif = 251,                  /* k_elsif  */
  YYSYMBOL_k_end = 252,                    /* k_end  */
  YYSYMBOL_k_return = 253,                 /* k_return  */
  YYSYMBOL_then = 254,                     /* then  */
  YYSYMBOL_do = 255,                       /* do  */
  YYSYMBOL_if_tail = 256,                  /* if_tail  */
  YYSYMBOL_opt_else = 257,                 /* opt_else  */
  YYSYMBOL_for_var = 258,                  /* for_var  */
  YYSYMBOL_f_marg = 259,                   /* f_marg  */
  YYSYMBOL_f_marg_list = 260,              /* f_marg_list  */
  YYSYMBOL_f_margs = 261,                  /* f_margs  */
  YYSYMBOL_f_rest_marg = 262,              /* f_rest_marg  */
  YYSYMBOL_f_any_kwrest = 263,             /* f_any_kwrest  */
  YYSYMBOL_f_eq = 264,                     /* f_eq  */
  YYSYMBOL_265_24 = 265,                   /* $@24  */
  YYSYMBOL_block_args_tail = 266,          /* block_args_tail  */
  YYSYMBOL_opt_block_args_tail = 267,      /* opt_block_args_tail  */
  YYSYMBOL_excessed_comma = 268,           /* excessed_comma  */
  YYSYMBOL_block_param = 269,              /* block_param  */
  YYSYMBOL_opt_block_param = 270,          /* opt_block_param  */
  YYSYMBOL_block_param_def = 271,          /* block_param_def  */
  YYSYMBOL_opt_bv_decl = 272,              /* opt_bv_decl  */
  YYSYMBOL_bv_decls = 273,                 /* bv_decls  */
  YYSYMBOL_bvar = 274,                     /* bvar  */
  YYSYMBOL_lambda = 275,                   /* lambda  */
  YYSYMBOL_276_25 = 276,                   /* @25  */
  YYSYMBOL_277_26 = 277,                   /* @26  */
  YYSYMBOL_278_27 = 278,                   /* @27  */
  YYSYMBOL_279_28 = 279,                   /* $@28  */
  YYSYMBOL_f_larglist = 280,               /* f_larglist  */
  YYSYMBOL_lambda_body = 281,              /* lambda_body  */
  YYSYMBOL_do_block = 282,                 /* do_block  */
  YYSYMBOL_block_call = 283,               /* block_call  */
  YYSYMBOL_method_call = 284,              /* method_call  */
  YYSYMBOL_brace_block = 285,              /* brace_block  */
  YYSYMBOL_brace_body = 286,               /* brace_body  */
  YYSYMBOL_287_29 = 287,                   /* @29  */
  YYSYMBOL_288_30 = 288,                   /* @30  */
  YYSYMBOL_289_31 = 289,                   /* @31  */
  YYSYMBOL_do_body = 290,                  /* do_body  */
  YYSYMBOL_291_32 = 291,                   /* @32  */
  YYSYMBOL_292_33 = 292,                   /* @33  */
  YYSYMBOL_293_34 = 293,                   /* @34  */
  YYSYMBOL_case_args = 294,                /* case_args  */
  YYSYMBOL_case_body = 295,                /* case_body  */
  YYSYMBOL_cases = 296,                    /* cases  */
  YYSYMBOL_p_case_body = 297,              /* p_case_body  */
  YYSYMBOL_298_35 = 298,                   /* @35  */
  YYSYMBOL_299_36 = 299,                   /* @36  */
  YYSYMBOL_300_37 = 300,                   /* $@37  */
  YYSYMBOL_p_cases = 301,                  /* p_cases  */
  YYSYMBOL_p_top_expr = 302,               /* p_top_expr  */
  YYSYMBOL_p_top_expr_body = 303,          /* p_top_expr_body  */
  YYSYMBOL_p_expr = 304,                   /* p_expr  */
  YYSYMBOL_p_as = 305,                     /* p_as  */
  YYSYMBOL_p_alt = 306,                    /* p_alt  */
  YYSYMBOL_p_lparen = 307,                 /* p_lparen  */
  YYSYMBOL_p_lbracket = 308,               /* p_lbracket  */
  YYSYMBOL_p_expr_basic = 309,             /* p_expr_basic  */
  YYSYMBOL_310_38 = 310,                   /* @38  */
  YYSYMBOL_311_39 = 311,                   /* @39  */
  YYSYMBOL_p_args = 312,                   /* p_args  */
  YYSYMBOL_p_args_head = 313,              /* p_args_head  */
  YYSYMBOL_p_args_tail = 314,              /* p_args_tail  */
  YYSYMBOL_p_find = 315,                   /* p_find  */
  YYSYMBOL_p_rest = 316,                   /* p_rest  */
  YYSYMBOL_p_args_post = 317,              /* p_args_post  */
  YYSYMBOL_p_arg = 318,                    /* p_arg  */
  YYSYMBOL_p_kwargs = 319,                 /* p_kwargs  */
  YYSYMBOL_p_kwarg = 320,                  /* p_kwarg  */
  YYSYMBOL_p_kw = 321,                     /* p_kw  */
  YYSYMBOL_p_kw_label = 322,               /* p_kw_label  */
  YYSYMBOL_p_kwrest = 323,                 /* p_kwrest  */
  YYSYMBOL_p_kwnorest = 324,               /* p_kwnorest  */
  YYSYMBOL_p_any_kwrest = 325,             /* p_any_kwrest  */
  YYSYMBOL_p_value = 326,                  /* p_value  */
  YYSYMBOL_p_primitive = 327,              /* p_primitive  */
  YYSYMBOL_p_variable = 328,               /* p_variable  */
  YYSYMBOL_p_var_ref = 329,                /* p_var_ref  */
  YYSYMBOL_p_expr_ref = 330,               /* p_expr_ref  */
  YYSYMBOL_p_const = 331,                  /* p_const  */
  YYSYMBOL_opt_rescue = 332,               /* opt_rescue  */
  YYSYMBOL_exc_list = 333,                 /* exc_list  */
  YYSYMBOL_exc_var = 334,                  /* exc_var  */
  YYSYMBOL_opt_ensure = 335,               /* opt_ensure  */
  YYSYMBOL_literal = 336,                  /* literal  */
  YYSYMBOL_strings = 337,                  /* strings  */
  YYSYMBOL_string = 338,                   /* string  */
  YYSYMBOL_string1 = 339,                  /* string1  */
  YYSYMBOL_xstring = 340,                  /* xstring  */
  YYSYMBOL_regexp = 341,                   /* regexp  */
  YYSYMBOL_words = 342,                    /* words  */
  YYSYMBOL_word_list = 343,                /* word_list  */
  YYSYMBOL_word = 344,                     /* word  */
  YYSYMBOL_symbols = 345,                  /* symbols  */
  YYSYMBOL_symbol_list = 346,              /* symbol_list  */
  YYSYMBOL_qwords = 347,                   /* qwords  */
  YYSYMBOL_qsymbols = 348,                 /* qsymbols  */
  YYSYMBOL_qword_list = 349,               /* qword_list  */
  YYSYMBOL_qsym_list = 350,                /* qsym_list  */
  YYSYMBOL_string_contents = 351,          /* string_contents  */
  YYSYMBOL_xstring_contents = 352,         /* xstring_contents  */
  YYSYMBOL_regexp_contents = 353,          /* regexp_contents  */
  YYSYMBOL_string_content = 354,           /* string_content  */
  YYSYMBOL_355_40 = 355,                   /* @40  */
  YYSYMBOL_356_41 = 356,                   /* $@41  */
  YYSYMBOL_357_42 = 357,                   /* @42  */
  YYSYMBOL_358_43 = 358,                   /* @43  */
  YYSYMBOL_359_44 = 359,                   /* @44  */
  YYSYMBOL_360_45 = 360,                   /* @45  */
  YYSYMBOL_string_dvar = 361,              /* string_dvar  */
  YYSYMBOL_symbol = 362,                   /* symbol  */
  YYSYMBOL_ssym = 363,                     /* ssym  */
  YYSYMBOL_sym = 364,                      /* sym  */
  YYSYMBOL_dsym = 365,                     /* dsym  */
  YYSYMBOL_numeric = 366,                  /* numeric  */
  YYSYMBOL_simple_numeric = 367,           /* simple_numeric  */
  YYSYMBOL_nonlocal_var = 368,             /* nonlocal_var  */
  YYSYMBOL_user_variable = 369,            /* user_variable  */
  YYSYMBOL_keyword_variable = 370,         /* keyword_variable  */
  YYSYMBOL_var_ref = 371,                  /* var_ref  */
  YYSYMBOL_var_lhs = 372,                  /* var_lhs  */
  YYSYMBOL_backref = 373,                  /* backref  */
  YYSYMBOL_superclass = 374,               /* superclass  */
  YYSYMBOL_375_46 = 375,                   /* $@46  */
  YYSYMBOL_f_opt_paren_args = 376,         /* f_opt_paren_args  */
  YYSYMBOL_f_paren_args = 377,             /* f_paren_args  */
  YYSYMBOL_f_arglist = 378,                /* f_arglist  */
  YYSYMBOL_379_47 = 379,                   /* @47  */
  YYSYMBOL_args_tail = 380,                /* args_tail  */
  YYSYMBOL_opt_args_tail = 381,            /* opt_args_tail  */
  YYSYMBOL_f_args = 382,                   /* f_args  */
  YYSYMBOL_args_forward = 383,             /* args_forward  */
  YYSYMBOL_f_bad_arg = 384,                /* f_bad_arg  */
  YYSYMBOL_f_norm_arg = 385,               /* f_norm_arg  */
  YYSYMBOL_f_arg_asgn = 386,               /* f_arg_asgn  */
  YYSYMBOL_f_arg_item = 387,               /* f_arg_item  */
  YYSYMBOL_f_arg = 388,                    /* f_arg  */
  YYSYMBOL_f_label = 389,                  /* f_label  */
  YYSYMBOL_f_kw = 390,                     /* f_kw  */
  YYSYMBOL_f_block_kw = 391,               /* f_block_kw  */
  YYSYMBOL_f_block_kwarg = 392,            /* f_block_kwarg  */
  YYSYMBOL_f_kwarg = 393,                  /* f_kwarg  */
  YYSYMBOL_kwrest_mark = 394,              /* kwrest_mark  */
  YYSYMBOL_f_no_kwarg = 395,               /* f_no_kwarg  */
  YYSYMBOL_f_kwrest = 396,                 /* f_kwrest  */
  YYSYMBOL_f_opt = 397,                    /* f_opt  */
  YYSYMBOL_f_block_opt = 398,              /* f_block_opt  */
  YYSYMBOL_f_block_optarg = 399,           /* f_block_optarg  */
  YYSYMBOL_f_optarg = 400,                 /* f_optarg  */
  YYSYMBOL_restarg_mark = 401,             /* restarg_mark  */
  YYSYMBOL_f_rest_arg = 402,               /* f_rest_arg  */
  YYSYMBOL_blkarg_mark = 403,              /* blkarg_mark  */
  YYSYMBOL_f_block_arg = 404,              /* f_block_arg  */
  YYSYMBOL_opt_f_block_arg = 405,          /* opt_f_block_arg  */
  YYSYMBOL_singleton = 406,                /* singleton  */
  YYSYMBOL_407_48 = 407,                   /* $@48  */
  YYSYMBOL_assoc_list = 408,               /* assoc_list  */
  YYSYMBOL_assocs = 409,                   /* assocs  */
  YYSYMBOL_assoc = 410,                    /* assoc  */
  YYSYMBOL_operation = 411,                /* operation  */
  YYSYMBOL_operation2 = 412,               /* operation2  */
  YYSYMBOL_operation3 = 413,               /* operation3  */
  YYSYMBOL_dot_or_colon = 414,             /* dot_or_colon  */
  YYSYMBOL_call_op = 415,                  /* call_op  */
  YYSYMBOL_call_op2 = 416,                 /* call_op2  */
  YYSYMBOL_opt_terms = 417,                /* opt_terms  */
  YYSYMBOL_opt_nl = 418,                   /* opt_nl  */
  YYSYMBOL_rparen = 419,                   /* rparen  */
  YYSYMBOL_rbracket = 420,                 /* rbracket  */
  YYSYMBOL_rbrace = 421,                   /* rbrace  */
  YYSYMBOL_trailer = 422,                  /* trailer  */
  YYSYMBOL_term = 423,                     /* term  */
  YYSYMBOL_terms = 424,                    /* terms  */
  YYSYMBOL_none = 425                      /* none  */
};
typedef enum yysymbol_kind_t yysymbol_kind_t;




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

/* Work around bug in HP-UX 11.23, which defines these macros
   incorrectly for preprocessor constants.  This workaround can likely
   be removed in 2023, as HPE has promised support for HP-UX 11.23
   (aka HP-UX 11i v2) only through the end of 2022; see Table 2 of
   <https://h20195.www2.hpe.com/V2/getpdf.aspx/4AA4-7673ENW.pdf>.  */
#ifdef __hpux
# undef UINT_LEAST8_MAX
# undef UINT_LEAST16_MAX
# define UINT_LEAST8_MAX 255
# define UINT_LEAST16_MAX 65535
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
# define YY_USE(E) ((void) (E))
#else
# define YY_USE(E) /* empty */
#endif

/* Suppress an incorrect diagnostic about yylval being uninitialized.  */
#if defined __GNUC__ && ! defined __ICC && 406 <= __GNUC__ * 100 + __GNUC_MINOR__
# if __GNUC__ * 100 + __GNUC_MINOR__ < 407
#  define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN                           \
    _Pragma ("GCC diagnostic push")                                     \
    _Pragma ("GCC diagnostic ignored \"-Wuninitialized\"")
# else
#  define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN                           \
    _Pragma ("GCC diagnostic push")                                     \
    _Pragma ("GCC diagnostic ignored \"-Wuninitialized\"")              \
    _Pragma ("GCC diagnostic ignored \"-Wmaybe-uninitialized\"")
# endif
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

#if 1

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
#endif /* 1 */

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
#define YYLAST   15161

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  154
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  272
/* YYNRULES -- Number of rules.  */
#define YYNRULES  785
/* YYNSTATES -- Number of states.  */
#define YYNSTATES  1309

/* YYMAXUTOK -- Last valid token kind.  */
#define YYMAXUTOK   353


/* YYTRANSLATE(TOKEN-NUM) -- Symbol number corresponding to TOKEN-NUM
   as returned by yylex, with out-of-bounds checking.  */
#define YYTRANSLATE(YYX)                                \
  (0 <= (YYX) && (YYX) <= YYMAXUTOK                     \
   ? YY_CAST (yysymbol_kind_t, yytranslate[YYX])        \
   : YYSYMBOL_YYUNDEF)

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
       0,  1327,  1327,  1327,  1353,  1359,  1366,  1373,  1380,  1386,
    1387,  1393,  1406,  1404,  1415,  1426,  1432,  1439,  1446,  1453,
    1459,  1464,  1463,  1473,  1473,  1480,  1487,  1497,  1506,  1513,
    1521,  1529,  1541,  1553,  1563,  1577,  1578,  1586,  1593,  1601,
    1608,  1611,  1618,  1625,  1633,  1640,  1647,  1655,  1662,  1673,
    1685,  1698,  1712,  1722,  1727,  1736,  1739,  1740,  1744,  1748,
    1752,  1757,  1765,  1756,  1779,  1787,  1778,  1800,  1803,  1820,
    1830,  1829,  1848,  1855,  1855,  1855,  1861,  1862,  1865,  1866,
    1875,  1885,  1895,  1904,  1915,  1922,  1929,  1936,  1943,  1951,
    1959,  1966,  1973,  1982,  1983,  1992,  1993,  2002,  2009,  2016,
    2023,  2030,  2037,  2044,  2051,  2058,  2065,  2074,  2075,  2084,
    2091,  2100,  2107,  2116,  2123,  2130,  2137,  2147,  2154,  2164,
    2171,  2178,  2188,  2195,  2202,  2209,  2216,  2223,  2230,  2237,
    2244,  2254,  2262,  2265,  2272,  2279,  2288,  2289,  2290,  2291,
    2296,  2299,  2306,  2309,  2316,  2316,  2326,  2327,  2328,  2329,
    2330,  2331,  2332,  2333,  2334,  2335,  2336,  2337,  2338,  2339,
    2340,  2341,  2342,  2343,  2344,  2345,  2346,  2347,  2348,  2349,
    2350,  2351,  2352,  2353,  2354,  2355,  2358,  2358,  2358,  2359,
    2359,  2360,  2360,  2360,  2361,  2361,  2361,  2361,  2362,  2362,
    2362,  2362,  2363,  2363,  2363,  2364,  2364,  2364,  2364,  2365,
    2365,  2365,  2365,  2366,  2366,  2366,  2366,  2367,  2367,  2367,
    2367,  2368,  2368,  2368,  2368,  2369,  2369,  2372,  2379,  2386,
    2393,  2400,  2407,  2414,  2422,  2430,  2438,  2447,  2456,  2464,
    2472,  2480,  2488,  2492,  2496,  2500,  2504,  2508,  2512,  2516,
    2520,  2524,  2528,  2532,  2536,  2540,  2541,  2545,  2549,  2553,
    2557,  2561,  2565,  2569,  2573,  2577,  2581,  2585,  2585,  2590,
    2599,  2610,  2622,  2635,  2649,  2655,  2656,  2657,  2658,  2661,
    2665,  2672,  2676,  2682,  2689,  2690,  2694,  2701,  2710,  2715,
    2725,  2732,  2744,  2758,  2759,  2762,  2763,  2764,  2768,  2775,
    2784,  2792,  2799,  2807,  2815,  2819,  2819,  2856,  2863,  2876,
    2880,  2887,  2894,  2901,  2908,  2918,  2919,  2923,  2930,  2937,
    2946,  2947,  2948,  2949,  2950,  2951,  2952,  2953,  2954,  2955,
    2956,  2964,  2963,  2978,  2978,  2985,  2985,  2993,  3001,  3008,
    3015,  3022,  3030,  3037,  3044,  3051,  3058,  3058,  3063,  3067,
    3071,  3078,  3079,  3087,  3088,  3099,  3110,  3120,  3131,  3130,
    3147,  3146,  3161,  3170,  3213,  3212,  3236,  3235,  3258,  3257,
    3280,  3292,  3306,  3313,  3320,  3327,  3336,  3343,  3349,  3366,
    3372,  3378,  3384,  3390,  3396,  3403,  3410,  3417,  3423,  3429,
    3435,  3441,  3447,  3462,  3469,  3475,  3482,  3483,  3484,  3487,
    3488,  3491,  3492,  3504,  3505,  3514,  3515,  3518,  3526,  3535,
    3542,  3551,  3558,  3565,  3572,  3579,  3588,  3596,  3605,  3606,
    3609,  3609,  3611,  3615,  3619,  3623,  3629,  3634,  3639,  3649,
    3653,  3657,  3661,  3665,  3669,  3674,  3678,  3682,  3686,  3690,
    3694,  3698,  3702,  3706,  3712,  3713,  3719,  3729,  3742,  3746,
    3755,  3757,  3761,  3766,  3773,  3779,  3783,  3787,  3772,  3812,
    3821,  3832,  3837,  3843,  3853,  3867,  3874,  3881,  3890,  3899,
    3907,  3915,  3922,  3930,  3938,  3945,  3952,  3965,  3973,  3983,
    3984,  3988,  3983,  4005,  4006,  4010,  4005,  4029,  4037,  4044,
    4052,  4061,  4073,  4074,  4078,  4085,  4089,  4077,  4104,  4105,
    4108,  4109,  4117,  4127,  4128,  4133,  4141,  4145,  4149,  4155,
    4158,  4167,  4170,  4177,  4180,  4181,  4183,  4184,  4185,  4194,
    4203,  4212,  4217,  4226,  4235,  4244,  4249,  4253,  4257,  4263,
    4262,  4274,  4279,  4279,  4286,  4295,  4299,  4308,  4312,  4316,
    4320,  4324,  4327,  4331,  4340,  4344,  4350,  4360,  4364,  4370,
    4371,  4380,  4389,  4393,  4397,  4401,  4407,  4409,  4418,  4426,
    4440,  4441,  4464,  4468,  4474,  4480,  4481,  4484,  4485,  4494,
    4503,  4511,  4519,  4520,  4521,  4522,  4530,  4540,  4541,  4542,
    4543,  4544,  4545,  4546,  4547,  4548,  4555,  4558,  4568,  4579,
    4588,  4597,  4604,  4611,  4620,  4632,  4635,  4642,  4649,  4652,
    4656,  4659,  4666,  4669,  4670,  4673,  4690,  4691,  4692,  4701,
    4711,  4720,  4726,  4736,  4742,  4751,  4753,  4762,  4772,  4778,
    4787,  4796,  4806,  4812,  4822,  4828,  4838,  4848,  4867,  4873,
    4883,  4893,  4934,  4937,  4936,  4953,  4957,  4962,  4966,  4970,
    4952,  4991,  4998,  5005,  5012,  5015,  5016,  5019,  5029,  5030,
    5031,  5032,  5035,  5045,  5046,  5056,  5057,  5058,  5059,  5062,
    5063,  5064,  5067,  5068,  5069,  5070,  5071,  5074,  5075,  5076,
    5077,  5078,  5079,  5080,  5083,  5096,  5105,  5112,  5121,  5122,
    5126,  5125,  5135,  5143,  5144,  5152,  5164,  5165,  5165,  5181,
    5185,  5189,  5193,  5197,  5204,  5209,  5214,  5218,  5222,  5226,
    5230,  5234,  5238,  5242,  5246,  5250,  5254,  5258,  5262,  5266,
    5271,  5277,  5286,  5295,  5304,  5313,  5324,  5325,  5333,  5342,
    5350,  5371,  5373,  5386,  5396,  5405,  5416,  5424,  5434,  5441,
    5451,  5458,  5467,  5468,  5471,  5479,  5487,  5497,  5508,  5519,
    5526,  5535,  5542,  5551,  5552,  5555,  5563,  5573,  5574,  5577,
    5585,  5595,  5599,  5605,  5610,  5610,  5634,  5635,  5644,  5646,
    5669,  5680,  5687,  5696,  5704,  5723,  5724,  5725,  5728,  5729,
    5730,  5731,  5734,  5735,  5736,  5739,  5740,  5743,  5744,  5747,
    5748,  5751,  5752,  5755,  5756,  5759,  5762,  5765,  5768,  5769,
    5770,  5773,  5774,  5777,  5778,  5782
};
#endif

/** Accessing symbol of state STATE.  */
#define YY_ACCESSING_SYMBOL(State) YY_CAST (yysymbol_kind_t, yystos[State])

#if 1
/* The user-facing name of the symbol whose (internal) number is
   YYSYMBOL.  No bounds checking.  */
static const char *yysymbol_name (yysymbol_kind_t yysymbol) YY_ATTRIBUTE_UNUSED;

/* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
   First, the terminals, then, starting at YYNTOKENS, nonterminals.  */
static const char *const yytname[] =
{
  "\"end-of-input\"", "error", "\"invalid token\"", "\"`class'\"",
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
  "$@4", "command_asgn", "command_rhs", "expr", "@5", "@6", "@7", "@8",
  "def_name", "defn_head", "defs_head", "$@9", "expr_value",
  "expr_value_do", "$@10", "$@11", "command_call", "block_command",
  "cmd_brace_block", "fcall", "command", "mlhs", "mlhs_inner",
  "mlhs_basic", "mlhs_item", "mlhs_head", "mlhs_post", "mlhs_node", "lhs",
  "cname", "cpath", "fname", "fitem", "undef_list", "$@12", "op",
  "reswords", "arg", "$@13", "relop", "rel_expr", "lex_ctxt", "arg_value",
  "aref_args", "arg_rhs", "paren_args", "opt_paren_args", "opt_call_args",
  "call_args", "command_args", "$@14", "block_arg", "opt_block_arg",
  "args", "mrhs_arg", "mrhs", "primary", "$@15", "$@16", "$@17", "$@18",
  "@19", "@20", "$@21", "$@22", "$@23", "primary_value", "k_begin", "k_if",
  "k_unless", "k_while", "k_until", "k_case", "k_for", "k_class",
  "k_module", "k_def", "k_do", "k_do_block", "k_rescue", "k_ensure",
  "k_when", "k_else", "k_elsif", "k_end", "k_return", "then", "do",
  "if_tail", "opt_else", "for_var", "f_marg", "f_marg_list", "f_margs",
  "f_rest_marg", "f_any_kwrest", "f_eq", "$@24", "block_args_tail",
  "opt_block_args_tail", "excessed_comma", "block_param",
  "opt_block_param", "block_param_def", "opt_bv_decl", "bv_decls", "bvar",
  "lambda", "@25", "@26", "@27", "$@28", "f_larglist", "lambda_body",
  "do_block", "block_call", "method_call", "brace_block", "brace_body",
  "@29", "@30", "@31", "do_body", "@32", "@33", "@34", "case_args",
  "case_body", "cases", "p_case_body", "@35", "@36", "$@37", "p_cases",
  "p_top_expr", "p_top_expr_body", "p_expr", "p_as", "p_alt", "p_lparen",
  "p_lbracket", "p_expr_basic", "@38", "@39", "p_args", "p_args_head",
  "p_args_tail", "p_find", "p_rest", "p_args_post", "p_arg", "p_kwargs",
  "p_kwarg", "p_kw", "p_kw_label", "p_kwrest", "p_kwnorest",
  "p_any_kwrest", "p_value", "p_primitive", "p_variable", "p_var_ref",
  "p_expr_ref", "p_const", "opt_rescue", "exc_list", "exc_var",
  "opt_ensure", "literal", "strings", "string", "string1", "xstring",
  "regexp", "words", "word_list", "word", "symbols", "symbol_list",
  "qwords", "qsymbols", "qword_list", "qsym_list", "string_contents",
  "xstring_contents", "regexp_contents", "string_content", "@40", "$@41",
  "@42", "@43", "@44", "@45", "string_dvar", "symbol", "ssym", "sym",
  "dsym", "numeric", "simple_numeric", "nonlocal_var", "user_variable",
  "keyword_variable", "var_ref", "var_lhs", "backref", "superclass",
  "$@46", "f_opt_paren_args", "f_paren_args", "f_arglist", "@47",
  "args_tail", "opt_args_tail", "f_args", "args_forward", "f_bad_arg",
  "f_norm_arg", "f_arg_asgn", "f_arg_item", "f_arg", "f_label", "f_kw",
  "f_block_kw", "f_block_kwarg", "f_kwarg", "kwrest_mark", "f_no_kwarg",
  "f_kwrest", "f_opt", "f_block_opt", "f_block_optarg", "f_optarg",
  "restarg_mark", "f_rest_arg", "blkarg_mark", "f_block_arg",
  "opt_f_block_arg", "singleton", "$@48", "assoc_list", "assocs", "assoc",
  "operation", "operation2", "operation3", "dot_or_colon", "call_op",
  "call_op2", "opt_terms", "opt_nl", "rparen", "rbracket", "rbrace",
  "trailer", "term", "terms", "none", YY_NULLPTR
};

static const char *
yysymbol_name (yysymbol_kind_t yysymbol)
{
  return yytname[yysymbol];
}
#endif

#define YYPACT_NINF (-1086)

#define yypact_value_is_default(Yyn) \
  ((Yyn) == YYPACT_NINF)

#define YYTABLE_NINF (-786)

#define yytable_value_is_error(Yyn) \
  ((Yyn) == YYTABLE_NINF)

/* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
   STATE-NUM.  */
static const yytype_int16 yypact[] =
{
   -1086,   168,  4730, -1086, 10445, -1086, -1086, -1086,  9903, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, 10571, 10571, -1086, -1086,
   -1086,  6363,  5922, -1086, -1086, -1086, -1086,   574,  9758,    26,
      48,    92, -1086, -1086, -1086,  5187,  6069, -1086, -1086,  5334,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, 12335, 12335,
   12335, 12335,   273,  7958, 10697, 11201, 11579, 10187, -1086,  9613,
   -1086, -1086, -1086,   196,   239,   259,   302,  1067, 12461, 12335,
   -1086,   418, -1086,  1061, -1086,   774,   345,   345, -1086, -1086,
     195,   441,   364, -1086,   404, 12713, -1086,   405,  3899,   546,
     317,   329, -1086, 12587, 12587, -1086, -1086,  8940, 12835, 12957,
   13079,  9467, 10571, -1086,    70,   122, -1086, -1086,   471, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086,    52,   353, -1086,   518,   678, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086,   486, -1086, -1086, -1086,   505,
   12335,   610,  8109, 12335, 12335, 12335, -1086, 12335,   345,   345,
   -1086,   552,  5753,   602, -1086, -1086,   592,   369,   274,   454,
     641,   464,   615, -1086, -1086,  8814, -1086, 10571, 10823, -1086,
   -1086,  9066, -1086, 12587,   618, -1086,   616,  8260, -1086,  8411,
   -1086, -1086,   621,   651,   195, -1086,   385, -1086,   736,  5900,
    5900,   742, 10697, -1086,  7958,   682,   418, -1086,  1061,    26,
     707, -1086,  1061,    26,   699,    85,   617, -1086,   602,   692,
     617, -1086,    26,   798,  1067, 13201,   713,   713,   719, -1086,
     516,   596,   609,   611, -1086, -1086, -1086, -1086, -1086,    81,
   -1086,   453,   501,   470, -1086, -1086, -1086, -1086,   795, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086,  9192, 12587, 12587, 12587,
   12587, 10697, 12587, 12587,  1785,   748,   754,  7102,  1785, -1086,
     761,  7102, -1086, -1086, -1086,   784, -1086, -1086, -1086, -1086,
   -1086,   823, -1086,  7958, 10316,   749,   823, -1086, 12335, 12335,
   12335, 12335, 12335, -1086, -1086, 12335, 12335, 12335, 12335, 12335,
   12335, 12335, 12335, -1086, 12335, -1086, -1086, 12335, 12335, 12335,
   12335, 12335, 12335, 12335, 12335, 12335, 12335, -1086, -1086, 13664,
   10571, 13754,  7102,   774,   151,   151,  8562, 12587,  8562,   418,
   -1086,   753,   844, -1086, -1086,   622,   884,    91,   121,   129,
     951,  1040, 12587,   379, -1086,   782,   661, -1086, -1086, -1086,
   -1086,    72,   304,   341,   344,   407,   420,   424,   510,   517,
   -1086, -1086, -1086, -1086,   548, -1086, -1086, -1086, 15014, -1086,
   -1086,   823,   823, -1086, -1086,   544, -1086, -1086, -1086,   904,
     804,   806,   823, 12335, 10949, -1086, -1086, 13844, 10571, 13934,
     823,   823, 11327, -1086,    26,   791, -1086, -1086, 12335,    26,
   -1086,   796,    26,   800, -1086,   159, -1086, -1086, -1086, -1086,
   -1086,  9903, -1086, 12335,   808,   813, 13844, 13934,   823,  1061,
      48,    26, -1086, -1086,  9318,   815,    26, -1086, -1086, 11453,
   -1086, -1086, 11579, -1086, -1086, -1086,   616,   669, -1086, -1086,
     821, -1086, 13201, 14024, 10571, 14114, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086,   623,    56,   686,
      68, 12335, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,  1015,
   -1086, -1086, -1086, -1086, -1086,   828, -1086,    26, -1086, -1086,
   -1086,   843, -1086,   830, 12335, -1086,   832,   108, -1086, -1086,
   -1086,   835,   932,   841,   941, -1086, 12461,   987,   992,   418,
   12461,   987,   862, -1086, -1086, -1086,   987, -1086,   987, -1086,
   11705, -1086,    26, 13201,   875, -1086, 11705, -1086,   736,  6047,
    6047,  6047,  6047,  6194,  4208,  6047,  6047,  5900,  5900,   550,
     550, -1086,  6485,  1260,  1260,  1391,   386,   386,   736,   736,
     736,  1376,  1376,  6510,  5481,  6804,  5628, -1086,   651, -1086,
      26,   878,   751, -1086,   768, -1086, -1086,  6216,   987, -1086,
    7253,  1009,  7706,   987,    88,   987,  1007,  1020,   130, 14204,
   10571, 14294, -1086,   774, -1086,   669, -1086, -1086, -1086, 14384,
   10571, 14474,  7102, 12587, -1086, -1086, -1086, -1086, -1086,  3812,
   12461, 12461,  9903, 12335, 12335, 12335, 12335, -1086, 12335,   602,
   -1086,   615,  5039,  5775,    26,   595,   647, 12335, 12335, -1086,
   -1086, -1086, -1086, 11075, -1086, 11327, -1086, -1086, 12587,  5753,
   -1086, -1086,   651,   651, 12335, -1086,   261, -1086, -1086,   617,
   13201,   821,   403,    65,    26,   257,   298,  1528, -1086,   934,
   -1086,    63, -1086,   886, -1086, -1086,    71,   891, -1086,   736,
    1015,  1114, -1086,   890,    26,   900, -1086,   209, -1086, -1086,
   -1086, 12335,   924,  1785, -1086, -1086,   639, -1086, -1086, -1086,
    1785, -1086, -1086,  1696, -1086, -1086,  1011,  5312, -1086, -1086,
   -1086, 11831,   659, -1086, -1086,  1012,  5459, -1086, -1086, -1086,
     913, -1086, -1086, -1086, 12335, -1086,   914,   916,  1021, -1086,
   -1086,   821, 13201, -1086, -1086,  1024,   933,  5165, -1086, -1086,
   -1086,   766,   676,  4880,  4880,   935,   823,   823, -1086,   784,
     923,   797, 10949,   823,   823, -1086, -1086,   784, -1086, -1086,
     801, -1086,  1059, -1086, -1086, -1086, -1086, -1086, -1086,  1020,
     987, -1086, 11957,   987,   105,   297,    26,   138,   147,  8562,
     418, 12587,  7102,  1081,    65, -1086,    26,   987,   159, 10048,
     122,   441, -1086,  5606, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086,   823,   823,   787,   823,   823,    26,   929,   159, -1086,
   -1086, -1086,   433,  1785, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086,    26, -1086,  1015,
   -1086,  1091, -1086, -1086, -1086, -1086, -1086,   936,   940, -1086,
    1029,   828,   944, -1086,   945, -1086,   944, 12335, 12335,   914,
   -1086,   993, -1086, -1086, -1086,  8562, -1086, -1086, -1086, 12335,
   12335,   956, -1086,   956,   949, 12083, 10697,   821, 10697,   823,
   12335, 14564, 10571, 14654, -1086, -1086, -1086,  4593,  4593,   455,
   -1086,  4442,    12,  1045, -1086,   119, -1086, -1086,   215, -1086,
     967, -1086, -1086, -1086,   963, -1086,   971, -1086, 13597, -1086,
   -1086, -1086, -1086,   876, -1086, -1086, -1086,    38, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086,   475, -1086, 12335,
   12461, 12461, -1086, -1086,   823, 12461, 12461, -1086, -1086,  8562,
   12587,   987, -1086, -1086,   987, -1086, -1086,   987, -1086, 12335,
   -1086,   118, -1086,   165,   987,  7102,   418,   987, -1086, -1086,
   -1086, -1086, -1086, -1086, 12335, 12335,   823, 12335, 12335, -1086,
   11327, -1086,    26,    78, -1086, -1086, -1086,   975,   985,  1785,
   -1086,  1696, -1086, -1086,  1696, -1086,  1696, -1086, -1086,  5753,
    5753, 13323,   151, -1086, -1086,  7832,  5753,  5753,  1232,  8411,
   -1086, -1086,  7102, 12335,   989, -1086, -1086, 12461,  5753,  6657,
    6951,    26,   793,   803, -1086, -1086, -1086, -1086, 13597,   286,
      26, 13478, -1086,    26,   994, -1086,   896,   995, -1086, -1086,
     930, -1086, -1086, -1086, -1086, 12587, -1086,  1063, 13564, 13597,
   13597,   896,  1033,  4593,  4593,   455,   465,   634,  4880,  4880,
   -1086, -1086,  5753, -1086, -1086, -1086, -1086, 12461, -1086, -1086,
   -1086, -1086, -1086,   151, -1086, -1086,  4880, -1086, -1086, 12209,
    7404, -1086,   987, -1086, -1086, 12335,  1001,   990,  7102,  8411,
   -1086, -1086,  1091,  1091,   944,  1008,   944,   944,  1082, -1086,
     779,   206,   214,   231,  7102,  1149,   828, -1086,    26,  1031,
     843,  1026, 13445, -1086,  1028, -1086,  1035,  1043, -1086, -1086,
   -1086,  1047,   827,    36, -1086,    60,  1033,  1049, -1086, -1086,
   -1086,    26, -1086, -1086,  1042, -1086, -1086,  1052, -1086,  1056,
   -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,    26,    26,
      26,    26,    26,    26, -1086, -1086,  7253,   151,   969, 12335,
   -1086,   697, -1086, -1086,  1155,   987,  1060,  8688,   985, -1086,
    1696, -1086, -1086, -1086,   283, 14744, 10571, 14834,   992, -1086,
   -1086,  1044, -1086, 13445,  1358, -1086, -1086,  1152,   788,   639,
   -1086,  1358, -1086,  1466, -1086, -1086,  1068, 13597, -1086,   515,
   -1086, -1086, 13597, 13564, -1086, -1086, -1086, -1086, -1086, -1086,
     801, -1086, 12587, 12587, -1086, -1086, -1086, -1086, -1086,   698,
   -1086, -1086, -1086, -1086,  1095,   944,   182,   199,    26,   333,
     338, -1086, -1086,   788, -1086,  1071,  1073, -1086, 14924, -1086,
     828,  1077, -1086,  1079,  1077, 13597,  1096,  1096, -1086, -1086,
   -1086,  7555, -1086, -1086,  1155, -1086, -1086, -1086,   367,  1358,
   -1086,  1466, -1086,  1098,  1102, -1086,  1466, -1086,  1466, -1086,
   -1086,  1096, 13597,   586, -1086,  1077,  1097,  1077,  1077, -1086,
   -1086, -1086, -1086,  1466, -1086, -1086, -1086,  1077, -1086
};

/* YYDEFACT[STATE-NUM] -- Default reduction number in state STATE-NUM.
   Performed when YYTABLE does not specify something else to do.  Zero
   means the default is an error.  */
static const yytype_int16 yydefact[] =
{
       2,     0,     0,     1,     0,   374,   375,   376,     0,   367,
     368,   369,   372,   370,   371,   373,   362,   363,   364,   365,
     385,   295,   295,   658,   657,   659,   660,   773,     0,   773,
       0,     0,   662,   661,   663,   755,   757,   654,   653,   756,
     656,   645,   646,   647,   648,   596,   668,   669,     0,     0,
       0,     0,     0,     0,   323,   785,   785,   105,   444,   616,
     616,   618,   620,     0,     0,     0,     0,     0,     0,     0,
       3,   771,     6,     9,    35,    40,   677,   677,    56,    77,
     295,    76,     0,    93,     0,    97,   107,     0,    67,   245,
     264,     0,   321,     0,     0,    73,    73,   771,     0,     0,
       0,     0,   332,   343,    78,   341,   310,   311,   595,   597,
     312,   313,   314,   316,   315,   317,   594,   635,   636,   593,
     643,   664,   665,   318,     0,   319,    81,     5,     8,   186,
     197,   187,   210,   183,   203,   193,   192,   213,   214,   208,
     191,   190,   185,   211,   215,   216,   195,   184,   198,   202,
     204,   196,   189,   205,   212,   207,   206,   199,   209,   194,
     182,   201,   200,   181,   188,   179,   180,   176,   177,   178,
     136,   138,   137,   171,   172,   167,   149,   150,   151,   158,
     155,   157,   152,   153,   173,   174,   159,   160,   164,   168,
     154,   156,   146,   147,   148,   161,   162,   163,   165,   166,
     169,   170,   175,   141,   143,    28,   139,   140,   142,     0,
     752,     0,     0,     0,     0,   298,   616,     0,   677,   677,
     290,     0,   273,   301,    91,   294,   785,     0,   664,   665,
       0,   319,   785,   748,    92,   773,    89,     0,   785,   464,
      88,   773,   774,     0,     0,    23,   257,     0,    10,     0,
     362,   363,   335,   465,     0,   239,     0,   332,   240,   230,
     231,   329,     0,    21,     0,     0,   771,    17,    20,   773,
      95,    16,   325,   773,     0,   778,   778,   274,     0,     0,
     778,   746,   773,     0,     0,     0,   677,   677,   103,   366,
       0,   113,   114,   121,   445,   640,   639,   641,   638,     0,
     637,     0,     0,     0,   603,   612,   608,   614,   644,    60,
     251,   252,   781,   782,     4,   783,   772,     0,     0,     0,
       0,     0,     0,     0,   700,     0,   676,     0,   700,   674,
       0,     0,   377,   469,   458,    82,   473,   340,   378,   473,
     454,   785,   109,     0,   101,    98,   785,    64,     0,     0,
       0,     0,     0,   267,   268,     0,     0,     0,     0,   228,
     229,     0,     0,    61,     0,   265,   266,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   767,   768,     0,
     785,     0,     0,    72,     0,     0,     0,     0,     0,   771,
     350,   772,     0,   396,   395,     0,     0,   664,   665,   319,
     131,   132,     0,     0,   134,   672,     0,   664,   665,   319,
     358,   206,   199,   209,   194,   176,   177,   178,   136,   137,
     744,    69,    68,   743,     0,    90,   770,   769,     0,   342,
     598,   785,   785,   144,   751,   329,   302,   754,   297,     0,
       0,     0,   785,     0,     0,   291,   300,     0,   785,     0,
     785,   785,     0,   292,   773,     0,   334,   296,   701,   773,
     286,   785,   773,   785,   285,   773,   339,    59,    25,    27,
      26,     0,   336,     0,     0,     0,     0,     0,   785,    19,
       0,   773,   327,    15,   772,    94,   773,   324,   330,   780,
     779,   275,   780,   277,   331,   747,     0,   120,   644,   111,
     106,   676,     0,     0,   785,     0,   446,   622,   642,   625,
     623,   617,   599,   600,   619,   601,   621,     0,     0,     0,
       0,     0,   784,     7,    29,    30,    31,    32,    33,    57,
      58,   707,   704,   703,   702,   705,   713,   722,   701,     0,
     734,   723,   738,   737,   733,   785,   699,   773,   683,   706,
     708,   709,   711,   685,   715,   720,   785,   726,   409,   408,
     731,   685,   736,   685,   740,   682,     0,     0,   785,     0,
       0,     0,     0,   470,   469,    83,     0,   474,     0,   271,
       0,   272,   773,     0,    99,   110,     0,    65,   237,   244,
     246,   247,   248,   255,   256,   249,   250,   226,   227,   253,
     254,    62,   773,   241,   242,   243,   232,   233,   234,   235,
     236,   269,   270,   758,   760,   759,   761,   463,   295,   461,
     773,   785,   758,   760,   759,   761,   462,   295,     0,   387,
       0,   386,     0,     0,     0,     0,   348,     0,   329,     0,
     785,     0,    73,   356,   131,   132,   133,   670,   354,     0,
     785,     0,     0,     0,   765,   766,    70,   758,   759,   295,
       0,     0,     0,     0,     0,     0,     0,   750,     0,   303,
     299,   785,   758,   759,   773,   758,   759,     0,     0,   749,
     333,   775,   280,   287,   282,   289,   338,    24,     0,   258,
      11,    34,     0,   785,     0,    22,    96,    18,   326,   778,
       0,   104,   762,   119,   773,   758,   759,   700,   626,     0,
     602,     0,   605,     0,   610,   607,     0,     0,   611,   238,
       0,   407,   399,   401,   773,   404,   397,     0,   681,   742,
     675,     0,     0,     0,   692,   714,     0,   680,   724,   725,
       0,   695,   735,     0,   697,   739,    48,   260,   384,   360,
     379,   785,   785,   585,   678,    50,   262,   361,   467,   471,
       0,   468,   475,   453,     0,    36,   306,     0,    39,   305,
     108,   102,     0,    55,    41,    53,     0,   278,   301,   217,
      37,     0,   319,     0,     0,     0,   785,   785,   460,    86,
       0,   466,   287,   785,   785,   284,   459,    84,   283,   322,
     785,   388,   785,   346,   390,    74,   389,   347,   484,     0,
       0,   381,     0,     0,   762,   328,   773,   758,   759,     0,
       0,     0,     0,   131,   132,   135,   773,     0,   773,     0,
     455,    79,    42,   278,   218,    52,   225,   145,   753,   304,
     293,   785,   785,   466,   785,   785,   773,   785,   773,   224,
     276,   112,   466,   700,   447,   450,   627,   631,   632,   633,
     624,   634,   604,   606,   613,   609,   615,   773,   406,     0,
     710,     0,   741,   727,   411,   684,   712,   685,   685,   721,
     726,   785,   685,   732,   685,   709,   685,     0,     0,   586,
     587,   785,   588,   380,   382,     0,    12,    14,   592,     0,
       0,   785,    80,   785,   309,     0,     0,   100,     0,   785,
       0,     0,   785,     0,   577,   583,   550,     0,     0,     0,
     522,   773,   519,   538,   616,     0,   576,    66,   493,   499,
     501,   503,   497,   496,   534,   498,   543,   546,   549,   555,
     556,   545,   506,   557,   507,   562,   563,   564,   567,   568,
     569,   570,   571,   573,   572,   574,   575,   553,    63,     0,
       0,     0,    87,   776,   785,     0,     0,    85,   383,     0,
       0,     0,   391,   393,     0,    75,   485,     0,   352,     0,
     477,     0,   351,   466,     0,     0,     0,     0,   466,   359,
     745,    71,   456,   457,     0,     0,   785,     0,     0,   281,
     288,   337,   773,     0,   628,   398,   400,   402,   405,     0,
     688,     0,   690,   679,     0,   696,     0,   693,   698,    49,
     261,     0,     0,   590,   591,     0,    51,   263,   773,     0,
     435,   434,     0,     0,   307,    38,    54,     0,   279,   758,
     759,   773,   758,   759,   565,   566,   132,   581,     0,   524,
     773,   525,   531,   773,     0,   518,     0,     0,   521,   537,
       0,   578,   650,   649,   651,     0,   579,     0,   494,     0,
       0,   544,   548,   560,   561,     0,   505,   504,     0,     0,
     554,   552,   259,    47,   222,    46,   223,     0,    44,   220,
      45,   221,   394,     0,   344,   345,     0,   349,   478,     0,
       0,   353,     0,   671,   355,     0,     0,   438,     0,     0,
     448,   629,     0,     0,   685,   685,   685,   685,     0,   589,
       0,   664,   665,   319,     0,   785,   785,   433,   773,     0,
     709,   417,   717,   718,   785,   729,   417,   417,   415,   472,
     476,   308,   466,   773,   516,   529,   541,   526,   517,   532,
     616,   773,   777,   551,     0,   500,   495,   534,   502,   535,
     539,   547,   542,   558,   559,   582,   515,   511,   773,   773,
     773,   773,   773,   773,    43,   219,     0,     0,   490,     0,
     479,   785,   357,   449,     0,     0,     0,     0,   403,   689,
       0,   686,   691,   694,   329,     0,   785,     0,   785,    13,
     414,     0,   436,     0,   418,   426,   424,     0,   716,     0,
     413,     0,   429,     0,   431,   523,   527,     0,   533,     0,
     520,   580,     0,     0,   508,   509,   510,   512,   513,   514,
     785,   486,     0,     0,   480,   482,   483,   481,   442,   773,
     440,   443,   452,   451,     0,   685,   762,   328,   773,   758,
     759,   584,   437,   728,   416,   417,   417,   329,     0,   719,
     785,   417,   730,   417,   417,     0,   530,   535,   536,   540,
     392,     0,   491,   492,     0,   439,   630,   687,   466,     0,
     421,     0,   423,   762,   328,   412,     0,   430,     0,   427,
     432,   528,     0,   785,   441,   417,   417,   417,   417,   488,
     489,   487,   422,     0,   419,   425,   428,   417,   420
};

/* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
   -1086, -1086, -1086,   997, -1086,    47,   772,  -312, -1086,   -24,
   -1086,   769, -1086,    74, -1086,  -427,  -543,   -32, -1086, -1086,
   -1086, -1086,   426,  2327,  2557, -1086,   -12,   -74, -1086, -1086,
     -10, -1086,  -640,  1214,    -9,  1159,  -125,    34,   -42, -1086,
    -406,    32,  3056,  -372,  1160,   -55,   -15, -1086, -1086,    -7,
   -1086,  4007, -1086,  1170, -1086,   927,   668, -1086,   714,     8,
     604,  -377,    97,     6, -1086,  -404,  -193,    17, -1086,  -483,
     -19, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
     918, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086, -1086,
   -1086, -1086, -1086, -1086, -1086, -1086,   512, -1086,   811,  1895,
    -376, -1086,    35,  -777, -1086,  -768,  -788,   549,   401,  -296,
     141, -1086,   244,  -438, -1086, -1086,   373, -1086,  -896, -1086,
       3,  1019, -1086, -1086, -1086, -1086, -1086, -1086,   447, -1086,
   -1086,   -99,   705, -1086, -1086, -1086,   946, -1086, -1086, -1086,
   -1086,  -762, -1086,   -13, -1086, -1086, -1086, -1086, -1086,  -728,
    -258, -1086, -1086, -1086, -1086,   223, -1086, -1086,   -38, -1086,
    -715,  -829,  -963,  -613,  -750,  -119, -1086,   212, -1086, -1086,
   -1086,   222, -1086,  -774,   230, -1086, -1086, -1086,   101, -1086,
   -1086,   176,  1381,  1652, -1086,  1195,  1974,  2151,  2180, -1086,
     794,  2262, -1086,  2645,  2678, -1086, -1086,   -58, -1086, -1086,
    -261, -1086, -1086, -1086, -1086, -1086, -1086, -1086,     9, -1086,
   -1086, -1086, -1086,    30, -1086,  3022,    10,  1211,  3297,  1722,
   -1086, -1086,    29,   727,    23, -1086,  -308,  -381,  -305,  -206,
   -1060,  -513,  -313,  -698,  -451,  -500,   578,   106, -1086, -1086,
    -663, -1086,  -709,  -696, -1085,   113,   585, -1086,  -649, -1086,
    -433,  -532, -1086, -1086, -1086,    43,  -402, -1086,  -324, -1086,
   -1086,   -86, -1086,   -20,   123,   482,   440,   170,  -231,   -61,
      31,    -2
};

/* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
       0,     1,     2,    70,    71,    72,   248,   567,  1025,   568,
     266,   267,   480,   268,   471,    74,   774,    75,   601,   784,
     587,   783,   421,   218,   219,   829,   384,   386,   387,   975,
      78,    79,   575,   254,    81,    82,   269,    83,    84,    85,
     500,    86,   221,   404,   405,   203,   204,   205,   662,   616,
     207,    88,   473,   375,    89,   580,   223,   274,   779,   617,
     796,   459,   460,   236,   237,   225,   445,   621,   768,   769,
      90,   382,   273,   486,   688,   809,   637,   822,   820,   652,
     256,    92,    93,    94,    95,    96,    97,    98,    99,   100,
     101,   336,   339,   751,   895,   812,   969,   970,   749,   257,
     630,   805,   971,   972,   396,   722,   723,   724,   725,   545,
     731,   732,  1254,  1205,  1206,  1128,  1029,  1030,  1106,  1239,
    1240,   103,   294,   506,   707,  1003,   854,  1110,   340,   104,
     105,   337,   572,   573,   759,   901,   576,   577,   762,   903,
     981,   813,  1237,   810,   976,  1096,  1271,  1301,  1177,   927,
    1146,   929,   930,  1078,  1079,   931,  1056,  1048,  1050,  1051,
    1052,   933,   934,  1159,  1054,   935,   936,   937,   938,   939,
     940,   941,   942,   943,   944,   945,   946,   947,   752,   891,
    1022,   897,   106,   107,   108,   109,   110,   111,   112,   517,
     711,   113,   519,   114,   115,   518,   520,   299,   302,   303,
     511,   709,   708,   856,  1004,  1111,  1187,   860,   116,   117,
     300,   118,   119,   120,  1066,   228,   229,   123,   230,   231,
     648,   821,   325,   326,   327,   328,   875,   734,   547,   548,
     549,   550,   885,   552,   553,   554,   555,  1133,  1134,   556,
     557,   558,   559,   560,  1135,  1136,   561,   562,   563,   564,
     565,   728,   424,   653,   279,   463,   233,   126,   692,   619,
     656,   651,   428,   314,   455,   456,   791,  1058,   491,   631,
     391,   271
};

/* YYTABLE[YYPACT[STATE-NUM]] -- What to do in state STATE-NUM.  If
   positive, shift that token.  If negative, reduce the rule whose
   number is the opposite.  If YYTABLE_NINF, syntax error.  */
static const yytype_int16 yytable[] =
{
     127,   206,   301,   620,   298,   381,   429,   220,   220,   632,
     315,   551,   122,   245,   122,   551,   546,   208,   427,   571,
     546,   206,   388,   569,   737,   974,   726,   881,   240,   265,
     239,   646,   462,   226,   226,   876,   315,   208,   289,   453,
     670,   514,   516,   345,   883,   493,   422,   977,   670,   495,
     679,   128,   206,   277,   281,   618,   958,   627,   309,   232,
     232,   383,   383,   122,   122,   383,   289,   292,   932,   932,
     628,   674,   275,   880,   329,   329,    73,   390,    73,   289,
     289,   289,   385,  1008,   878,   389,   335,   270,   334,   288,
     679,   884,  1053,   220,   206,   292,   701,   308,   276,   280,
     331,  1006,   316,   780,   659,  1157,   330,  1108,   398,   408,
     408,   408,  1216,   224,   234,   804,  -122,   832,   835,   226,
     957,   957,   713,   618,  1241,   627,  1262,   704,   272,   507,
    -126,   629,  1129,  -328,   717,  1075,  1067,   507,   377,   481,
    -658,   449,   738,  1044,  1045,   232,  -123,   507,   332,   962,
     243,  -666,   246,   693,  -130,  -129,  -773,   967,   439,   773,
     739,  -328,  -328,  -125,   629,   242,   378,   426,     3,  -658,
     477,  1061,  -127,  1062,  1063,   714,  1064,   771,  -122,   242,
     741,   693,   744,  1076,   509,   510,  1077,   718,   265,   242,
    -124,   247,   509,   510,  1262,  -126,   322,   323,  -113,   425,
     508,  1109,   509,   510,   505,   315,  1217,   726,   868,   465,
    -328,   467,  -128,  -759,  1241,   862,   329,   329,   582,  -122,
    1065,   332,   122,   865,   446,   475,   220,  -123,   220,   220,
     446,   489,  1201,   773,   773,   249,   464,  -113,   490,   312,
     265,   313,   331,   499,  -130,   127,   483,   440,   441,  1169,
    1172,  -117,   226,  -758,   226,   461,   712,   122,   712,   122,
    1268,   239,   334,   816,  1099,   333,   289,  -114,   890,   312,
     453,   313,   122,   826,   122,  -121,  -120,   825,   232,   670,
     232,   670,  -116,   679,  -116,   383,   383,   383,   383,   882,
     529,   530,   886,  -118,   872,   292,  -129,   484,   270,  1163,
    1164,  1147,   312,   872,   313,   524,   525,   526,   527,   641,
     331,  -115,   242,   883,   498,  1067,   876,   693,   542,   265,
    1160,    73,  -128,  -118,  1188,   289,   122,   693,   315,   261,
    -758,   122,   454,  -126,   457,  -126,   479,   122,   333,   581,
     827,   122,   543,   238,   581,  1006,  -125,  -759,   304,  1013,
    -128,  -127,  -128,   122,   292,   383,   726,  -122,   726,  -122,
    1115,  1068,   633,   523,   635,  -123,   907,  -123,  1178,   636,
     643,   220,  -657,  -666,   625,   634,   584,   270,   464,  1137,
    -124,   932,  -130,  -116,  -130,  -366,  1067,   -94,   670,   626,
      73,   305,   122,   957,   551,   528,   122,   377,   122,   546,
    -122,  -657,   855,  -116,  1235,   496,  -116,  -108,   957,  -659,
    -116,   306,  -660,  -366,  -366,   957,   957,  1083,  1085,  1236,
     551,   625,  1088,  1090,  -118,   378,   379,   551,  -117,   581,
     581,   644,  -541,   957,  -129,   645,  -129,   377,  -659,   220,
     581,  -660,   625,  -119,  -118,  -759,   464,  -118,   581,   581,
     863,  -118,  -667,   377,   307,   863,   687,   626,  -115,   446,
     499,   446,  -366,   348,   206,   378,   447,  1160,   850,   338,
     625,  -785,  1160,  1269,   380,  -662,   581,   846,   840,  -123,
     208,   378,   476,   289,  -125,   626,  -125,   671,  -661,  -127,
     341,  -127,  -663,   324,   122,   220,  1010,  1012,   625,  -114,
    1260,  1015,   464,  1017,  -662,  1018,   876,   644,   754,  1080,
     987,  1046,   292,   626,   448,  1160,  1299,  -661,  -124,   507,
    -124,  -663,   372,   373,   374,   928,   928,  1081,  1132,  -117,
     448,   346,   699,   773,   773,  1041,   507,   515,   773,   773,
     551,   499,  1269,   729,  1174,   546,   880,  1047,  1002,  -117,
     342,  -758,  -117,  -667,   729,  1256,  -117,   746,  1114,  -115,
    1116,   755,  1263,   451,   289,  1117,   753,   507,   819,   312,
     765,   313,   512,   806,   509,   510,   775,  1131,  -652,  -115,
    -123,   507,  -115,    60,   377,  -655,  -115,   618,   876,   627,
    -130,   509,   510,   292,  1200,  1138,   670,   767,   679,   726,
     726,   894,  1210,   767,  1266,  1100,   800,  -652,   802,  1267,
     773,   808,   378,   503,  -655,  -773,   654,   431,   242,   446,
     513,   828,   509,   510,   789,   798,   788,   348,   353,   354,
    1296,   220,   433,   797,   625,   795,   509,   510,   464,  1153,
     122,   220,   122,   478,   625,   655,  1124,   837,   464,   626,
     775,   775,  1291,   241,   840,   206,   848,   798,   851,   626,
     773,   504,   122,  1049,  -664,   831,   435,   795,   893,   446,
    -129,   208,   468,  1102,   894,   365,   366,  -665,   442,  -319,
    1072,   289,   469,   470,   370,   371,   372,   373,   374,   507,
     377,   798,  -664,  -664,   844,   913,   551,   536,  1212,  1214,
     788,   795,   443,  1165,  1132,  -665,  -665,  -319,  -319,  1132,
     292,  1132,   894,  1132,   811,  1130,   537,  1176,   378,   639,
    1140,  -125,   241,   466,   278,   785,   847,   242,  1285,   377,
     499,   992,  1126,  1189,  1191,  1192,  1193,  -329,   444,  1245,
     450,  -664,   710,   790,   509,   510,   845,   541,   542,   892,
     898,   485,   507,   289,  -665,   487,  -319,   378,   649,   985,
    1261,   452,  1264,   492,   472,  -329,  -329,   640,   767,   235,
     490,  1138,   543,  -127,   422,   432,   872,   432,  1138,  1132,
    1138,  1132,   292,  -773,   581,   581,  1132,   242,  1132,   383,
    1143,   581,   581,   956,   956,   984,  1185,   790,   973,   238,
     973,  1231,  -130,  1132,  -130,   715,   650,   509,   510,   986,
    1049,   322,   323,   348,  -329,   968,   894,  1280,  1282,  1248,
    1049,  1049,   206,  1287,  -121,  1289,  1290,   790,  1295,   122,
    1297,   482,   122,   -93,   377,   847,   494,  1298,   928,   581,
     581,   478,   581,   581,  1274,   446,  1138,   377,  1138,   488,
     793,   242,  1307,  1138,   497,  1138,   377,  1302,  1304,  1305,
    1306,   324,   378,   911,  1277,   502,  1060,   794,  -129,  1308,
    1138,  1024,   521,   693,   566,   378,  1195,  -125,   434,   729,
    -673,   436,   437,   438,   378,  1258,   996,   570,  -120,  1023,
     574,  1130,   793,   579,  -127,   585,   964,  -116,  1130,  1031,
     638,  1031,   794,   220,   522,   122,   625,   581,  1126,   642,
     464,   912,   647,  -124,  -118,  1126,   122,  1126,   122,  -125,
      91,   626,    91,  -124,  1196,  1103,   964,   956,   956,  -127,
     664,   956,   665,   650,   227,   227,   680,  1151,   383,   790,
     681,   682,   683,  -115,   684,  1092,   685,   686,   956,   790,
     775,   775,   690,  -124,   916,   775,   775,   691,  1093,  1170,
    1173,  -108,   581,   696,  1073,  1074,  1130,   700,   698,  -410,
     507,    91,    91,   537,   727,   290,   733,  -755,   736,   122,
    1035,   740,  1036,  1126,   742,  1126,   227,   743,   857,   858,
    1126,   859,  1126,   745,   581,   122,   507,   748,    46,    47,
     750,  1125,   289,   290,   541,  1139,   758,  1126,  1150,  1232,
    1233,   227,   227,   501,   501,   227,   395,   406,   406,  -652,
     227,   772,   801,   512,   792,   509,   510,   775,   663,   730,
    1156,  1122,   808,   383,  1197,   122,   869,   811,   864,   122,
    1168,  1171,   122,   866,   790,  1057,   871,  -652,  -652,   512,
     874,   509,   510,  1154,  1153,   887,   899,   902,   956,   909,
    -301,   956,   905,   959,   770,   906,  -756,   531,   908,   532,
     533,   534,   535,   963,   894,  1000,  1181,   775,   956,   956,
     956,   739,  1009,   956,   956,  1186,  1011,  1028,   956,   956,
    1014,  1016,  1219,  1021,  -755,  -302,  -652,  1059,  1069,  -755,
    1198,   317,   318,   319,   320,   321,   956,  -762,  -655,  1070,
     122,   667,   669,   289,   843,   914,   720,  1071,   122,   122,
     278,  1112,   721,   898,   729,  1107,    41,    42,    43,    44,
      91,  1113,   729,  1067,   122,  -303,  -655,  -655,  1194,  1152,
    1149,  1184,   408,   531,   852,   532,   533,   534,   535,  -762,
    1183,  1107,  1230,   227,  1190,   227,   227,   669,   893,   227,
     278,   227,  1202,  1244,   790,    91,   531,    91,   532,   533,
     534,   535,  1204,   790,  1209,  1252,   790,  -762,  -762,   973,
      91,  1211,    91,  -756,   289,  -655,   122,   220,  -756,  1213,
     625,  1221,   720,  -304,   464,  1218,   753,   122,  1222,   790,
     383,   383,  1223,   290,  1243,   626,   870,  1238,  1257,   532,
     533,   534,   535,   408,  1265,  1276,    80,  1279,    80,  1281,
    1272,  1273,   735,  1286,  -762,  1288,  -762,   956,   973,  -758,
      80,    80,   956,   956,    91,   227,   227,   227,   227,    91,
     227,   227,  1292,  1303,   474,    91,  -758,  1293,   766,    91,
    -759,  1107,   695,   697,   778,   991,   983,   393,   729,   376,
     410,    91,   290,   830,   896,  1270,   988,    80,    80,   867,
    1007,  1203,  1127,   586,  1057,   956,  1032,  1294,   993,   760,
    1300,   122,    80,  1161,   531,   578,   532,   533,   534,   535,
     536,   973,  1158,  1162,   790,   790,   790,  1155,   227,  1251,
      91,  1199,   956,   430,    91,   227,    91,    80,    80,   537,
     990,    80,   423,   716,   879,  1259,    80,  1255,   877,     0,
     227,  1220,     0,     0,     0,     0,     0,     0,   999,     0,
    1001,   838,     0,   539,     0,     0,   839,   348,     0,   540,
     541,   542,     0,     0,     0,     0,     0,     0,     0,  1005,
       0,   669,     0,   278,   361,   362,     0,     0,   660,   661,
       0,  1055,  1275,     0,     0,   543,   227,     0,   544,   666,
       0,   790,     0,     0,   834,   836,     0,   677,   678,     0,
       0,     0,   757,     0,     0,   242,     0,   761,     0,   763,
       0,   834,   836,   369,   370,   371,   372,   373,   374,   873,
       0,     0,    91,     0,     0,   694,     0,     0,   849,     0,
     531,     0,   532,   533,   534,   535,   536,     0,     0,   889,
     290,     0,   227,     0,     0,     0,    80,     0,     0,     0,
       0,     0,   904,     0,     0,   537,     0,     0,     0,   799,
       0,     0,     0,     0,   803,     0,   807,     0,     0,    80,
       0,    80,    80,   348,     0,    80,     0,    80,     0,   539,
     669,    80,     0,    80,     0,   540,   541,   542,   348,     0,
     361,   362,     0,     0,     0,     0,    80,     0,    80,     0,
     980,  1142,     0,     0,   227,   361,   362,     0,   227,     0,
    1144,   543,     0,  1148,   544,     0,     0,     0,   227,     0,
       0,   290,     0,     0,   781,     0,     0,   367,   368,   369,
     370,   371,   372,   373,   374,     0,  1166,     0,   531,     0,
     532,   533,   534,   535,   536,   370,   371,   372,   373,   374,
      80,    80,    80,    80,    80,    80,    80,    80,     0,     0,
       0,    80,     0,   537,     0,    80,     0,     0,    91,     0,
      91,     0,     0,     0,     0,     0,     0,    80,   227,  1167,
       0,     0,     0,     0,     0,     0,     0,   539,   227,     0,
      91,   227,     0,  1034,   541,   542,     0,     0,   781,   781,
     531,     0,   532,   533,   534,   535,   536,     0,     0,     0,
       0,     0,     0,     0,    80,     0,    80,     0,     0,   543,
      80,    80,    80,     0,     0,   537,   227,     0,     0,     0,
       0,  1227,  1228,  1229,     0,     0,    80,     0,   290,   538,
       0,   978,     0,     0,   982,  1215,     0,     0,     0,   539,
       0,     0,     0,     0,     0,   540,   541,   542,   989,     0,
       0,     0,     0,     0,     0,     0,     0,  1098,     0,     0,
    1224,  1225,  1226,     0,     0,     0,     0,     0,     0,     0,
       0,   543,    80,     0,   544,     0,     0,     0,   278,     0,
       0,     0,     0,     0,  1084,  1086,   853,     0,     0,  1089,
    1091,     0,     0,     0,     0,     0,     0,     0,  1278,     0,
     290,     0,     0,     0,     0,     0,     0,     0,    80,     0,
       0,  1141,     0,     0,     0,     0,     0,     0,  1084,  1086,
       0,  1089,  1091,   960,   961,     0,     0,     0,    80,     0,
     965,   966,     0,     0,   125,     0,   125,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    91,     0,   227,
      91,     0,     0,     0,     0,     0,     0,     0,   531,     0,
     532,   533,   534,   535,   536,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  1180,   994,   995,
       0,   997,   998,   537,     0,   125,   125,     0,     0,   293,
      80,     0,  1094,     0,    80,  1095,     0,   538,  1097,     0,
       0,     0,     0,     0,    80,  1101,     0,   539,  1104,     0,
      80,  1175,   926,   926,   541,   542,     0,   293,     0,     0,
       0,     0,     0,    91,     0,     0,     0,     0,     0,  1175,
     399,   409,   409,     0,    91,     0,    91,     0,     0,   543,
     227,     0,     0,     0,     0,     0,  1037,   531,     0,   532,
     533,   534,   535,   536,    80,     0,    80,  1234,     0,     0,
       0,     0,     0,     0,    80,     0,     0,     0,     0,     0,
       0,     0,   537,     0,    80,     0,    80,    80,     0,     0,
       0,     0,     0,     0,    80,    80,   538,     0,   781,   781,
       0,     0,     0,   781,   781,     0,   539,    91,   227,     0,
       0,  1087,   540,   541,   542,     0,     0,   102,     0,   102,
       0,     0,    80,    91,     0,     0,     0,     0,     0,     0,
       0,   102,   102,  1182,     0,     0,     0,     0,   543,     0,
       0,   544,     0,  1105,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   125,     0,   926,   926,     0,  1120,
     926,     0,     0,    91,     0,     0,     0,    91,   102,   102,
      91,     0,     0,     0,     0,   781,     0,   926,     0,     0,
       0,     0,     0,   102,     0,     0,     0,     0,     0,   125,
       0,   125,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   227,   125,     0,   125,     0,   102,   102,
       0,     0,   102,     0,     0,     0,  1242,   102,     0,     0,
       0,     0,     0,     0,     0,   781,     0,   293,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    91,     0,
       0,     0,     0,     0,     0,     0,    91,    91,     0,     0,
       0,     0,     0,    80,     0,    80,    80,     0,   125,     0,
       0,     0,    91,   125,     0,     0,     0,     0,     0,   125,
    1208,     0,     0,   125,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   125,   293,   926,     0,     0,
     926,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   926,   926,   926,
       0,     0,   926,   926,    91,     0,     0,   926,   926,     0,
       0,     0,     0,     0,   125,    91,     0,   102,   125,    80,
     125,     0,     0,     0,   227,   926,     0,     0,     0,     0,
      80,  1253,    80,     0,     0,     0,    80,     0,     0,     0,
     102,     0,   102,   102,     0,     0,   102,     0,   102,     0,
       0,     0,   102,     0,   102,     0,     0,     0,     0,     0,
     227,   227,     0,     0,     0,     0,     0,   102,     0,   102,
       0,     0,     0,     0,   948,   948,     0,     0,     0,     0,
       0,     0,     0,     0,    80,    80,     0,     0,     0,    80,
      80,     0,     0,    80,    80,     0,     0,     0,     0,    91,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    80,
       0,     0,     0,     0,     0,     0,   125,     0,     0,     0,
       0,   102,   102,   102,   102,   102,   102,   102,   102,     0,
       0,     0,   102,     0,   293,     0,   102,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   926,     0,   102,    80,
       0,   926,   926,    80,     0,     0,    80,     0,     0,     0,
       0,    80,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   102,     0,   102,     0,    80,
       0,   102,   102,   102,   926,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   102,   948,   948,
       0,    80,   948,     0,     0,   293,     0,     0,   782,     0,
       0,   926,     0,     0,    80,     0,     0,     0,     0,   948,
       0,     0,    80,    80,     0,     0,     0,     0,     0,    76,
       0,    76,     0,     0,     0,     0,     0,     0,    80,     0,
       0,     0,     0,   102,     0,     0,     0,     0,     0,     0,
       0,     0,   125,     0,   125,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   125,     0,     0,     0,     0,   102,
      76,    76,   782,   782,   286,     0,     0,     0,     0,     0,
      80,     0,     0,     0,     0,     0,     0,     0,     0,   102,
       0,    80,     0,     0,     0,     0,     0,     0,     0,     0,
      80,     0,   286,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   293,     0,     0,   286,   286,   286,     0,   948,
       0,   861,   948,     0,     0,   949,   949,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    80,    80,     0,   948,
     948,   948,     0,     0,   948,   948,     0,     0,     0,   948,
     948,   102,     0,     0,     0,   102,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   102,     0,   948,     0,     0,
       0,   102,     0,     0,     0,    80,     0,     0,     0,     0,
       0,     0,     0,     0,   293,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   102,     0,   102,     0,     0,
       0,     0,     0,     0,     0,   102,     0,     0,     0,    76,
       0,   125,     0,     0,   125,   102,     0,   102,   102,     0,
       0,     0,     0,     0,     0,   102,   102,     0,     0,    77,
       0,    77,     0,     0,     0,     0,     0,     0,     0,   949,
     949,     0,     0,   949,    76,     0,    76,     0,     0,     0,
       0,     0,     0,   102,     0,     0,     0,     0,     0,    76,
     949,    76,     0,     0,     0,     0,     0,     0,   948,     0,
       0,     0,     0,   948,   948,     0,     0,     0,     0,     0,
      77,    77,   286,     0,   287,     0,     0,   125,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   125,     0,
     125,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   287,    76,     0,     0,   948,     0,    76,     0,
       0,     0,     0,     0,    76,   287,   287,   287,    76,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      76,   286,     0,   948,     0,     0,     0,     0,     0,     0,
       0,     0,   782,   782,     0,     0,     0,   782,   782,     0,
       0,   125,     0,     0,     0,     0,     0,     0,     0,     0,
     949,     0,     0,   949,     0,     0,     0,   125,     0,    76,
       0,     0,     0,    76,   102,    76,   102,   102,     0,     0,
     949,   949,   949,     0,     0,   949,   949,     0,     0,     0,
     949,   949,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  1123,     0,     0,     0,   125,   949,     0,
       0,   125,     0,     0,   125,     0,     0,   950,   950,   782,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    77,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     102,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   102,     0,   102,    77,     0,    77,   102,     0,   782,
       0,    76,     0,     0,     0,     0,     0,     0,     0,    77,
       0,    77,   125,     0,     0,     0,     0,     0,     0,   286,
     125,   125,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   287,     0,     0,     0,   125,     0,     0,     0,
       0,     0,     0,     0,   409,   102,   102,     0,     0,     0,
     102,   102,     0,     0,   102,   102,     0,     0,     0,   949,
       0,     0,     0,    77,   949,   949,     0,     0,    77,     0,
     102,     0,     0,     0,    77,     0,     0,     0,    77,     0,
       0,   950,   950,     0,     0,   950,     0,     0,   125,     0,
      77,   287,     0,     0,     0,     0,     0,     0,     0,   125,
     286,     0,   950,    76,     0,     0,     0,   949,     0,     0,
     102,     0,     0,     0,   102,   409,     0,   102,     0,     0,
       0,     0,   102,     0,   951,   951,     0,     0,     0,    77,
       0,     0,     0,    77,   949,    77,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    76,     0,    76,
     102,     0,     0,   952,   952,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    76,
       0,     0,   102,     0,     0,     0,     0,    76,    76,     0,
       0,     0,     0,   125,     0,   102,     0,     0,     0,     0,
       0,     0,     0,   102,   102,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   102,
       0,     0,   950,     0,   121,   950,   121,   286,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    77,   950,   950,   950,   953,   953,   950,   950,     0,
       0,     0,   950,   950,     0,     0,     0,     0,    87,   287,
      87,     0,     0,     0,     0,     0,     0,     0,   951,   951,
     950,   102,   951,     0,     0,   121,   121,     0,     0,   291,
       0,     0,   102,     0,     0,     0,     0,     0,     0,   951,
       0,   102,     0,     0,     0,     0,     0,   952,   952,   286,
       0,   952,     0,     0,     0,     0,     0,   291,     0,    87,
      87,     0,     0,     0,     0,     0,     0,     0,   952,     0,
     397,   407,   407,   407,     0,     0,     0,   102,   102,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     287,     0,     0,    77,     0,     0,    76,     0,     0,    76,
       0,     0,     0,     0,   394,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   102,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   953,
     953,     0,     0,   953,     0,     0,     0,    77,     0,    77,
       0,   950,     0,     0,     0,     0,   950,   950,     0,   951,
     953,     0,   951,     0,     0,     0,     0,     0,     0,    77,
       0,     0,     0,     0,     0,     0,     0,    77,    77,   951,
     951,   951,    76,     0,   951,   951,     0,     0,   952,   951,
     951,   952,     0,    76,   121,    76,     0,     0,     0,   950,
       0,     0,     0,     0,     0,     0,     0,   951,   952,   952,
     952,     0,     0,   952,   952,     0,     0,   287,   952,   952,
       0,     0,     0,     0,     0,     0,   950,     0,    87,   121,
       0,   121,     0,     0,     0,     0,   952,     0,     0,     0,
       0,     0,     0,     0,   121,     0,   121,    76,    76,     0,
       0,     0,    76,    76,     0,     0,    76,     0,     0,   124,
       0,   124,     0,    87,     0,    87,     0,   291,     0,     0,
     953,     0,    76,   953,     0,     0,     0,     0,    87,     0,
      87,     0,     0,     0,     0,     0,     0,     0,     0,   287,
     953,   953,   953,     0,     0,   953,   953,     0,   121,     0,
     953,   953,     0,   121,     0,     0,     0,     0,   286,   121,
     124,   124,    76,   121,     0,     0,    76,     0,   953,    76,
       0,     0,     0,     0,    76,   121,   291,     0,   951,     0,
       0,     0,    87,   951,   951,     0,    77,    87,     0,    77,
       0,     0,     0,    87,     0,     0,     0,    87,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   952,     0,    87,
       0,     0,   952,   952,   121,     0,     0,     0,   121,     0,
     121,     0,     0,     0,    76,     0,   951,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    76,   954,   954,
       0,     0,     0,     0,     0,    76,    76,     0,    87,     0,
       0,     0,    87,   951,    87,   952,     0,     0,     0,     0,
       0,    76,    77,     0,     0,     0,     0,     0,     0,   286,
       0,   955,   955,    77,     0,    77,     0,     0,     0,     0,
       0,     0,   952,     0,     0,     0,     0,     0,     0,   953,
       0,     0,     0,     0,   953,   953,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    76,     0,     0,   121,     0,     0,   124,
       0,     0,     0,     0,    76,     0,     0,    77,    77,     0,
       0,     0,    77,    77,   291,     0,    77,   953,     0,     0,
     286,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      87,     0,    77,     0,   124,     0,   124,     0,     0,     0,
       0,     0,     0,     0,   953,     0,     0,     0,     0,   124,
       0,   124,   954,   954,     0,     0,   954,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   287,     0,
       0,     0,    77,   954,     0,     0,    77,     0,     0,    77,
       0,     0,     0,     0,    77,   955,   955,     0,    76,   955,
       0,     0,     0,     0,     0,   291,     0,     0,     0,     0,
       0,     0,     0,   124,     0,     0,   955,     0,   124,     0,
       0,     0,     0,     0,   124,     0,     0,     0,   124,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     124,     0,   776,     0,    77,     0,     0,     0,     0,     0,
       0,     0,   121,     0,   121,     0,     0,    77,     0,     0,
       0,     0,     0,     0,     0,    77,    77,     0,     0,     0,
       0,     0,     0,     0,   121,     0,     0,     0,     0,   124,
       0,    77,     0,   124,     0,   124,    87,     0,    87,   287,
       0,     0,     0,   954,     0,     0,   954,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    87,     0,
       0,     0,     0,   954,   954,   954,   776,   776,   954,   954,
       0,     0,   291,   954,   954,     0,   955,     0,     0,   955,
       0,     0,     0,    77,     0,     0,     0,     0,     0,     0,
       0,   954,     0,     0,    77,     0,   955,   955,   955,     0,
       0,   955,   955,     0,     0,     0,   955,   955,     0,     0,
     287,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   955,     0,     0,     0,     0,     0,
       0,   124,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   291,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,  -785,     0,     0,     0,     0,     0,     0,     0,
    -785,  -785,  -785,     0,     0,  -785,  -785,  -785,    77,  -785,
       0,     0,     0,     0,     0,     0,     0,  -785,  -785,  -785,
       0,   121,     0,     0,   121,     0,     0,     0,     0,  -785,
    -785,     0,  -785,  -785,  -785,  -785,  -785,     0,     0,     0,
       0,     0,   954,     0,     0,     0,     0,   954,   954,     0,
       0,     0,     0,     0,     0,    87,     0,     0,    87,     0,
    -785,     0,     0,   124,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   955,     0,     0,     0,     0,
     955,   955,     0,     0,     0,     0,     0,     0,  -785,  -785,
     954,     0,     0,     0,     0,     0,     0,   121,     0,     0,
       0,     0,     0,     0,   347,     0,     0,   124,   121,   124,
     121,     0,  -785,     0,     0,     0,     0,   954,     0,     0,
       0,     0,     0,   955,     0,     0,     0,     0,     0,   124,
       0,    87,     0,     0,     0,  -785,  -785,   124,   124,     0,
     238,  -785,    87,  -785,    87,  -785,     0,     0,     0,     0,
     955,     0,     0,     0,     0,     0,   348,   349,   350,   351,
     352,   353,   354,   355,   356,   357,   358,   359,   360,     0,
       0,   121,     0,   361,   362,     0,     0,     0,     0,   363,
       0,     0,     0,     0,     0,     0,     0,   121,     0,     0,
       0,     0,     0,     0,     0,     0,   776,   776,     0,     0,
       0,   776,   776,   222,   222,    87,   364,     0,   365,   366,
     367,   368,   369,   370,   371,   372,   373,   374,     0,     0,
       0,    87,     0,  1121,     0,     0,     0,   121,     0,     0,
       0,   121,     0,     0,   121,   255,   258,   259,   260,     0,
       0,     0,   222,   222,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   310,   311,  1119,     0,     0,
       0,    87,     0,     0,     0,    87,     0,     0,    87,     0,
       0,     0,     0,   776,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   222,
       0,     0,     0,     0,     0,     0,   124,     0,     0,   124,
       0,     0,   121,     0,     0,     0,     0,     0,     0,     0,
     121,   121,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   776,     0,     0,   121,     0,     0,     0,
       0,     0,     0,     0,   407,     0,    87,     0,     0,     0,
       0,     0,     0,     0,    87,    87,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      87,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   124,     0,     0,     0,     0,     0,   121,     0,
       0,     0,     0,   124,     0,   124,     0,     0,     0,   121,
       0,     0,     0,     0,     0,     0,     0,   222,     0,     0,
     222,   222,   222,     0,   310,   407,     0,     0,     0,     0,
       0,     0,    87,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   222,    87,   222,   222,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   124,   124,     0,
       0,     0,   124,   124,     0,     0,   124,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   124,     0,     0,   348,   349,   350,   351,   352,
     353,   354,   355,   121,   357,   358,     0,     0,     0,     0,
       0,     0,   361,   362,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   124,     0,     0,     0,   124,    87,     0,   124,
       0,     0,     0,     0,   124,     0,     0,   365,   366,   367,
     368,   369,   370,   371,   372,   373,   374,     0,     0,     0,
       0,     0,     0,     0,     0,   588,   589,   590,   591,   592,
       0,     0,   593,   594,   595,   596,   597,   598,   599,   600,
       0,   602,     0,     0,   603,   604,   605,   606,   607,   608,
     609,   610,   611,   612,   124,     0,     0,   222,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   124,     0,     0,
       0,     0,     0,     0,     0,   124,   124,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   124,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     222,   222,     0,     0,     0,   222,     0,     0,     0,   222,
       0,     0,     0,     0,     0,   260,     0,     0,     0,     0,
       0,     0,     0,   124,     0,    23,    24,    25,    26,     0,
     689,     0,     0,     0,   124,     0,     0,     0,     0,     0,
       0,    32,    33,    34,   914,     0,   222,     0,   915,   222,
       0,    41,    42,    43,    44,    45,     0,     0,     0,     0,
       0,   222,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   719,     0,
       0,     0,   917,   918,     0,     0,     0,     0,     0,     0,
     919,     0,     0,   920,     0,     0,   921,   922,     0,   923,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,   222,     0,     0,     0,     0,     0,     0,   124,     0,
       0,     0,     0,   747,   925,     0,     0,   756,     0,     0,
       0,   284,     0,     0,     0,     0,     0,   222,     0,     0,
       0,     0,     0,   777,     0,   242,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    23,    24,    25,    26,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    32,    33,    34,     0,     0,   222,     0,     0,
       0,     0,    41,    42,    43,    44,    45,   222,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   833,   833,     0,
     222,   747,   756,   833,     0,   222,     0,     0,     0,     0,
       0,     0,     0,     0,   833,   833,     0,     0,     0,     0,
     222,     0,   222,     0,     0,     0,     0,     0,     0,     0,
       0,   833,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -785,     4,   284,     5,     6,     7,     8,     9,   222,     0,
       0,    10,    11,     0,     0,     0,    12,     0,    13,    14,
      15,    16,    17,    18,    19,     0,     0,     0,   222,     0,
      20,    21,    22,    23,    24,    25,    26,     0,     0,    27,
       0,   222,     0,     0,     0,    28,    29,    30,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,   222,
       0,     0,     0,     0,     0,    48,    49,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   222,
      50,    51,     0,     0,     0,     0,     0,     0,    52,     0,
       0,    53,    54,     0,    55,    56,     0,    57,     0,     0,
      58,    59,    60,    61,    62,    63,    64,    65,    66,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    67,
      68,    69,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -785,     0,  -785,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  1019,  1020,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  1026,  1027,     0,     0,
       0,     0,   222,    23,    24,    25,    26,  1038,     0,   222,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    32,
      33,    34,   914,     0,     0,     0,   915,     0,   916,    41,
      42,    43,    44,    45,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   537,     0,     0,
       0,     0,     0,     0,     0,     0,  1082,   833,   833,     0,
     917,   918,   833,   833,     0,     0,     0,     0,   919,     0,
       0,   920,     0,     0,   921,   922,   222,   923,   541,     0,
      58,    59,   924,    61,    62,    63,    64,    65,    66,     0,
       0,   833,   833,     0,   833,   833,     0,   222,     0,     0,
       0,     0,   925,     0,     0,     0,     0,     0,     0,   284,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -762,
     222,     0,     0,     0,   833,     0,     0,  -762,  -762,  -762,
       0,     0,  -762,  -762,  -762,     0,  -762,     0,     0,     0,
       0,     0,     0,     0,  -762,  -762,  -762,  -762,  -762,     0,
       0,     0,     0,     0,     0,     0,  -762,  -762,     0,  -762,
    -762,  -762,  -762,  -762,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   833,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   222,  -762,     0,     0,
       0,     0,   833,     0,     0,     0,  -762,  -762,  -762,  -762,
    -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,     0,
       0,     0,     0,  -762,  -762,  -762,  -762,     0,   841,  -762,
       0,     0,     0,     0,     0,  -762,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -762,
       0,     0,  -762,     0,     0,  -126,  -762,  -762,  -762,  -762,
    -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,     0,     0,
       0,     0,  -762,  -762,  -762,  -762,   222,  -652,  -762,  -762,
    -762,     0,  -762,     0,     0,  -652,  -652,  -652,     0,     0,
    -652,  -652,  -652,   222,  -652,     0,     0,     0,     0,   910,
       0,     0,  -652,     0,  -652,  -652,  -652,     0,     0,     0,
       0,     0,     0,     0,  -652,  -652,     0,  -652,  -652,  -652,
    -652,  -652,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   348,   349,   350,   351,   352,   353,   354,   355,
     356,   357,   358,   359,   360,  -652,     0,     0,     0,   361,
     362,     0,     0,     0,  -652,  -652,  -652,  -652,  -652,  -652,
    -652,  -652,  -652,  -652,  -652,  -652,  -652,     0,     0,     0,
       0,  -652,  -652,  -652,  -652,     0,  -652,  -652,     0,     0,
       0,     0,   364,  -652,   365,   366,   367,   368,   369,   370,
     371,   372,   373,   374,     0,     0,     0,  -652,     0,     0,
    -652,  -273,     0,  -652,  -652,  -652,  -652,  -652,  -652,  -652,
    -652,  -652,  -652,  -652,  -652,  -652,     0,     0,     0,     0,
       0,  -652,  -652,  -652,  -655,     0,  -652,  -652,  -652,     0,
    -652,     0,  -655,  -655,  -655,     0,     0,  -655,  -655,  -655,
       0,  -655,     0,     0,     0,     0,   888,     0,     0,  -655,
       0,  -655,  -655,  -655,     0,     0,     0,     0,     0,     0,
       0,  -655,  -655,     0,  -655,  -655,  -655,  -655,  -655,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   348,
     349,   350,   351,   352,   353,   354,   355,   356,   357,   358,
     359,   360,  -655,     0,     0,     0,   361,   362,     0,     0,
       0,  -655,  -655,  -655,  -655,  -655,  -655,  -655,  -655,  -655,
    -655,  -655,  -655,  -655,     0,     0,     0,     0,  -655,  -655,
    -655,  -655,     0,  -655,  -655,     0,     0,     0,     0,   364,
    -655,   365,   366,   367,   368,   369,   370,   371,   372,   373,
     374,     0,     0,     0,  -655,     0,     0,  -655,     0,     0,
    -655,  -655,  -655,  -655,  -655,  -655,  -655,  -655,  -655,  -655,
    -655,  -655,  -655,     0,     0,     0,     0,     0,  -655,  -655,
    -655,  -763,     0,  -655,  -655,  -655,     0,  -655,     0,  -763,
    -763,  -763,     0,     0,  -763,  -763,  -763,     0,  -763,     0,
       0,     0,     0,   900,     0,     0,  -763,  -763,  -763,  -763,
    -763,     0,     0,     0,     0,     0,     0,     0,  -763,  -763,
       0,  -763,  -763,  -763,  -763,  -763,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   348,   349,   350,   351,
     352,   353,   354,   355,   356,   357,   358,   359,   360,  -763,
       0,     0,     0,   361,   362,     0,     0,     0,  -763,  -763,
    -763,  -763,  -763,  -763,  -763,  -763,  -763,  -763,  -763,  -763,
    -763,     0,     0,     0,     0,  -763,  -763,  -763,  -763,     0,
       0,  -763,     0,     0,     0,     0,   364,  -763,   365,   366,
     367,   368,   369,   370,   371,   372,   373,   374,     0,     0,
       0,  -763,     0,     0,  -763,     0,     0,     0,  -763,  -763,
    -763,  -763,  -763,  -763,  -763,  -763,  -763,  -763,  -763,  -763,
       0,     0,     0,     0,  -763,  -763,  -763,  -763,  -764,     0,
    -763,  -763,  -763,     0,  -763,     0,  -764,  -764,  -764,     0,
       0,  -764,  -764,  -764,     0,  -764,     0,     0,     0,     0,
     910,     0,     0,  -764,  -764,  -764,  -764,  -764,     0,     0,
       0,     0,     0,     0,     0,  -764,  -764,     0,  -764,  -764,
    -764,  -764,  -764,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   348,   349,   350,   351,   352,   353,   354,
     355,   356,   357,   358,   359,   360,  -764,     0,     0,     0,
     361,   362,     0,     0,     0,  -764,  -764,  -764,  -764,  -764,
    -764,  -764,  -764,  -764,  -764,  -764,  -764,  -764,     0,     0,
       0,     0,  -764,  -764,  -764,  -764,     0,     0,  -764,     0,
       0,     0,     0,   364,  -764,   365,   366,   367,   368,   369,
     370,   371,   372,   373,   374,     0,     0,     0,  -764,     0,
       0,  -764,     0,     0,     0,  -764,  -764,  -764,  -764,  -764,
    -764,  -764,  -764,  -764,  -764,  -764,  -764,     0,     0,     0,
       0,  -764,  -764,  -764,  -764,  -328,     0,  -764,  -764,  -764,
       0,  -764,     0,  -328,  -328,  -328,     0,     0,  -328,  -328,
    -328,     0,  -328,     0,     0,     0,     0,     0,     0,     0,
    -328,     0,  -328,  -328,  -328,     0,     0,     0,     0,     0,
       0,     0,  -328,  -328,     0,  -328,  -328,  -328,  -328,  -328,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     348,   349,   350,   351,   352,   353,   354,   355,   356,   357,
     358,   359,   360,  -328,     0,     0,     0,   361,   362,     0,
       0,     0,  -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,
    -328,  -328,  -328,  -328,  -328,     0,     0,     0,     0,  -328,
    -328,  -328,  -328,     0,   842,  -328,     0,     0,     0,     0,
     364,  -328,   365,   366,   367,   368,   369,   370,   371,   372,
     373,   374,     0,     0,     0,  -328,     0,     0,  -328,     0,
       0,  -128,  -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,
    -328,  -328,  -328,  -328,     0,     0,     0,     0,     0,  -328,
    -328,  -328,  -465,     0,  -328,  -328,  -328,     0,  -328,     0,
    -465,  -465,  -465,     0,     0,  -465,  -465,  -465,     0,  -465,
       0,     0,     0,     0,     0,     0,     0,  -465,  -465,  -465,
    -465,     0,     0,     0,     0,     0,     0,     0,     0,  -465,
    -465,     0,  -465,  -465,  -465,  -465,  -465,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   348,   349,   350,
     351,   352,   353,   354,   355,   356,   357,   358,  -786,  -786,
    -465,     0,     0,     0,   361,   362,     0,     0,     0,  -465,
    -465,  -465,  -465,  -465,  -465,  -465,  -465,  -465,  -465,  -465,
    -465,  -465,     0,     0,     0,     0,  -465,  -465,  -465,  -465,
       0,     0,  -465,     0,     0,     0,     0,     0,  -465,   365,
     366,   367,   368,   369,   370,   371,   372,   373,   374,     0,
       0,     0,  -465,     0,     0,     0,     0,     0,     0,  -465,
       0,  -465,  -465,  -465,  -465,  -465,  -465,  -465,  -465,  -465,
    -465,     0,     0,     0,     0,  -465,  -465,  -465,  -465,  -320,
     238,  -465,  -465,  -465,     0,  -465,     0,  -320,  -320,  -320,
       0,     0,  -320,  -320,  -320,     0,  -320,     0,     0,     0,
       0,     0,     0,     0,  -320,     0,  -320,  -320,  -320,     0,
       0,     0,     0,     0,     0,     0,  -320,  -320,     0,  -320,
    -320,  -320,  -320,  -320,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   348,  -786,  -786,  -786,  -786,   353,
     354,     0,     0,  -786,  -786,     0,     0,  -320,     0,     0,
       0,   361,   362,     0,     0,     0,  -320,  -320,  -320,  -320,
    -320,  -320,  -320,  -320,  -320,  -320,  -320,  -320,  -320,     0,
       0,     0,     0,  -320,  -320,  -320,  -320,     0,     0,  -320,
       0,     0,     0,     0,     0,  -320,   365,   366,   367,   368,
     369,   370,   371,   372,   373,   374,     0,     0,     0,  -320,
       0,     0,  -320,     0,     0,     0,  -320,  -320,  -320,  -320,
    -320,  -320,  -320,  -320,  -320,  -320,  -320,  -320,     0,     0,
       0,     0,     0,  -320,  -320,  -320,  -785,     0,  -320,  -320,
    -320,     0,  -320,     0,  -785,  -785,  -785,     0,     0,  -785,
    -785,  -785,     0,  -785,     0,     0,     0,     0,     0,     0,
       0,  -785,  -785,  -785,  -785,     0,     0,     0,     0,     0,
       0,     0,     0,  -785,  -785,     0,  -785,  -785,  -785,  -785,
    -785,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   348,   349,   350,   351,   352,   353,   354,     0,     0,
     357,   358,     0,     0,  -785,     0,     0,     0,   361,   362,
       0,     0,     0,  -785,  -785,  -785,  -785,  -785,  -785,  -785,
    -785,  -785,  -785,  -785,  -785,  -785,     0,     0,     0,     0,
    -785,  -785,  -785,  -785,     0,     0,  -785,     0,     0,     0,
       0,     0,  -785,   365,   366,   367,   368,   369,   370,   371,
     372,   373,   374,     0,     0,     0,  -785,     0,     0,     0,
       0,     0,     0,  -785,     0,  -785,  -785,  -785,  -785,  -785,
    -785,  -785,  -785,  -785,  -785,     0,     0,     0,     0,  -785,
    -785,  -785,  -785,  -335,   238,  -785,  -785,  -785,     0,  -785,
       0,  -335,  -335,  -335,     0,     0,  -335,  -335,  -335,     0,
    -335,     0,     0,     0,     0,     0,     0,     0,  -335,     0,
    -335,  -335,     0,     0,     0,     0,     0,     0,     0,     0,
    -335,  -335,     0,  -335,  -335,  -335,  -335,  -335,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -335,     0,     0,     0,     0,     0,     0,     0,     0,
    -335,  -335,  -335,  -335,  -335,  -335,  -335,  -335,  -335,  -335,
    -335,  -335,  -335,     0,     0,     0,     0,  -335,  -335,  -335,
    -335,     0,     0,  -335,     0,     0,     0,     0,     0,  -335,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -335,     0,     0,     0,     0,     0,     0,
    -335,     0,  -335,  -335,  -335,  -335,  -335,  -335,  -335,  -335,
    -335,  -335,     0,     0,     0,     0,     0,  -335,  -335,  -335,
    -762,   235,  -335,  -335,  -335,     0,  -335,     0,  -762,  -762,
    -762,     0,     0,     0,  -762,  -762,     0,  -762,     0,     0,
       0,     0,     0,     0,     0,  -762,  -762,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -762,  -762,     0,
    -762,  -762,  -762,  -762,  -762,     0,     0,     0,     0,     0,
       0,     0,   348,   349,   350,   351,   352,   353,   354,   355,
     356,   357,   358,   359,   360,     0,     0,     0,  -762,   361,
     362,     0,     0,     0,     0,     0,     0,  -762,  -762,  -762,
    -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,
       0,     0,     0,     0,  -762,  -762,  -762,  -762,     0,   786,
    -762,     0,   364,     0,   365,   366,   367,   368,   369,   370,
     371,   372,   373,   374,     0,     0,     0,     0,     0,     0,
    -762,     0,     0,     0,     0,     0,  -126,  -762,   242,  -762,
    -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,     0,
       0,     0,     0,  -762,  -762,  -762,  -117,  -762,     0,  -762,
       0,  -762,     0,  -762,     0,  -762,  -762,  -762,     0,     0,
       0,  -762,  -762,     0,  -762,     0,     0,     0,     0,     0,
       0,     0,  -762,  -762,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -762,  -762,     0,  -762,  -762,  -762,
    -762,  -762,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -762,     0,     0,     0,     0,
       0,     0,     0,     0,  -762,  -762,  -762,  -762,  -762,  -762,
    -762,  -762,  -762,  -762,  -762,  -762,  -762,     0,     0,     0,
       0,  -762,  -762,  -762,  -762,     0,   786,  -762,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -762,     0,     0,
       0,     0,     0,  -126,  -762,     0,  -762,  -762,  -762,  -762,
    -762,  -762,  -762,  -762,  -762,  -762,     0,     0,     0,     0,
    -762,  -762,  -762,  -762,  -328,     0,  -762,     0,  -762,     0,
    -762,     0,  -328,  -328,  -328,     0,     0,     0,  -328,  -328,
       0,  -328,     0,     0,     0,     0,     0,     0,     0,  -328,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -328,  -328,     0,  -328,  -328,  -328,  -328,  -328,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,  -328,     0,     0,     0,     0,     0,     0,     0,
       0,  -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,
    -328,  -328,  -328,  -328,     0,     0,     0,     0,  -328,  -328,
    -328,  -328,     0,   787,  -328,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -328,     0,     0,     0,     0,     0,
    -128,  -328,     0,  -328,  -328,  -328,  -328,  -328,  -328,  -328,
    -328,  -328,  -328,     0,     0,     0,     0,     0,  -328,  -328,
    -119,  -328,     0,  -328,     0,  -328,     0,  -328,     0,  -328,
    -328,  -328,     0,     0,     0,  -328,  -328,     0,  -328,     0,
       0,     0,     0,     0,     0,     0,  -328,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -328,  -328,
       0,  -328,  -328,  -328,  -328,  -328,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -328,
       0,     0,     0,     0,     0,     0,     0,     0,  -328,  -328,
    -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,
    -328,     0,     0,     0,     0,  -328,  -328,  -328,  -328,     0,
     787,  -328,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -328,     0,     0,     0,     0,     0,  -128,  -328,     0,
    -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,  -328,
       0,     0,     0,     0,     0,  -328,  -328,  -328,     0,     0,
    -328,     0,  -328,   262,  -328,     5,     6,     7,     8,     9,
    -785,  -785,  -785,    10,    11,     0,     0,  -785,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,     0,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   263,
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
       0,     0,     0,  -785,   262,  -785,     5,     6,     7,     8,
       9,     0,     0,  -785,    10,    11,     0,  -785,  -785,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
       0,     0,     0,    20,    21,    22,    23,    24,    25,    26,
       0,     0,    27,     0,     0,     0,     0,     0,    28,    29,
     263,    31,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,    52,     0,     0,    53,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    67,    68,    69,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -785,   262,  -785,     5,     6,     7,
       8,     9,     0,     0,  -785,    10,    11,     0,     0,  -785,
      12,  -785,    13,    14,    15,    16,    17,    18,    19,     0,
       0,     0,     0,     0,    20,    21,    22,    23,    24,    25,
      26,     0,     0,    27,     0,     0,     0,     0,     0,    28,
      29,   263,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    50,    51,     0,     0,     0,     0,
       0,     0,    52,     0,     0,    53,    54,     0,    55,    56,
       0,    57,     0,     0,    58,    59,    60,    61,    62,    63,
      64,    65,    66,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    67,    68,    69,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -785,   262,  -785,     5,     6,
       7,     8,     9,     0,     0,  -785,    10,    11,     0,     0,
    -785,    12,     0,    13,    14,    15,    16,    17,    18,    19,
    -785,     0,     0,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,    27,     0,     0,     0,     0,     0,
      28,    29,   263,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,    52,     0,     0,    53,    54,     0,    55,
      56,     0,    57,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    67,    68,    69,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  -785,   262,  -785,     5,
       6,     7,     8,     9,     0,     0,  -785,    10,    11,     0,
       0,  -785,    12,     0,    13,    14,    15,    16,    17,    18,
      19,     0,     0,     0,     0,     0,    20,    21,    22,    23,
      24,    25,    26,     0,     0,    27,     0,     0,     0,     0,
       0,    28,    29,   263,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,    52,     0,     0,    53,    54,     0,
      55,    56,     0,    57,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,   262,     0,     5,     6,     7,     8,     9,
       0,  -785,  -785,    10,    11,    67,    68,    69,    12,     0,
      13,    14,    15,    16,    17,    18,    19,  -785,     0,  -785,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   263,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    50,    51,     0,     0,     0,     0,     0,     0,
      52,     0,     0,    53,    54,     0,    55,    56,     0,    57,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,     0,     0,     0,     0,     0,     0,     0,     0,   262,
       0,     5,     6,     7,     8,     9,     0,     0,     0,    10,
      11,    67,    68,    69,    12,     0,    13,    14,    15,    16,
      17,    18,    19,  -785,     0,  -785,     0,     0,    20,    21,
      22,    23,    24,    25,    26,     0,     0,    27,     0,     0,
       0,     0,     0,    28,    29,   263,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,    52,     0,     0,   264,
      54,     0,    55,    56,     0,    57,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    67,    68,    69,
       0,     0,     0,     0,     0,     0,     0,  -785,     0,  -785,
     262,  -785,     5,     6,     7,     8,     9,     0,     0,     0,
      10,    11,     0,     0,     0,    12,     0,    13,    14,    15,
      16,    17,    18,    19,     0,     0,     0,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,    27,     0,
       0,     0,     0,     0,    28,    29,   263,    31,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,    52,     0,     0,
      53,    54,     0,    55,    56,     0,    57,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    67,    68,
      69,     0,     0,     0,     0,     0,     0,     0,  -785,     0,
    -785,     4,  -785,     5,     6,     7,     8,     9,     0,     0,
       0,    10,    11,     0,     0,     0,    12,     0,    13,    14,
      15,    16,    17,    18,    19,     0,     0,     0,     0,     0,
      20,    21,    22,    23,    24,    25,    26,     0,     0,    27,
       0,     0,     0,     0,     0,    28,    29,    30,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    48,    49,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      50,    51,     0,     0,     0,     0,     0,     0,    52,     0,
       0,    53,    54,     0,    55,    56,     0,    57,     0,     0,
      58,    59,    60,    61,    62,    63,    64,    65,    66,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    67,
      68,    69,     0,     0,  -785,     0,     0,     0,     0,     0,
       0,  -785,   262,  -785,     5,     6,     7,     8,     9,     0,
       0,     0,    10,    11,     0,     0,     0,    12,     0,    13,
      14,    15,    16,    17,    18,    19,     0,     0,     0,     0,
       0,    20,    21,    22,    23,    24,    25,    26,     0,     0,
      27,     0,     0,     0,     0,     0,    28,    29,   263,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,    52,
       0,     0,    53,    54,     0,    55,    56,     0,    57,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      67,    68,    69,     0,     0,  -785,     0,     0,     0,     0,
       0,     0,  -785,   262,  -785,     5,     6,     7,     8,     9,
       0,     0,  -785,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,     0,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   263,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    50,    51,     0,     0,     0,     0,     0,     0,
      52,     0,     0,    53,    54,     0,    55,    56,     0,    57,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,     0,     0,     0,     0,     0,     0,     0,     0,   262,
       0,     5,     6,     7,     8,     9,     0,     0,     0,    10,
      11,    67,    68,    69,    12,     0,    13,    14,    15,    16,
      17,    18,    19,  -785,     0,  -785,     0,     0,    20,    21,
      22,    23,    24,    25,    26,     0,     0,    27,     0,     0,
       0,     0,     0,    28,    29,   263,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,    52,     0,     0,    53,
      54,     0,    55,    56,     0,    57,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,  -785,     0,
       0,     0,     0,     0,     0,     0,     0,     5,     6,     7,
       0,     9,     0,     0,     0,    10,    11,    67,    68,    69,
      12,     0,    13,    14,    15,    16,    17,    18,    19,  -785,
       0,  -785,     0,     0,    20,    21,    22,    23,    24,    25,
      26,     0,     0,   209,     0,     0,     0,     0,     0,     0,
      29,     0,     0,    32,    33,    34,    35,    36,    37,    38,
      39,    40,   210,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    50,    51,     0,     0,     0,     0,
       0,     0,   211,     0,     0,   212,    54,     0,    55,    56,
       0,   213,   214,   215,    58,    59,   216,    61,    62,    63,
      64,    65,    66,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     5,     6,     7,     0,     9,     0,     0,
       0,    10,    11,    67,   217,    69,    12,     0,    13,    14,
      15,    16,    17,    18,    19,     0,     0,   242,     0,     0,
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
       6,     7,     0,     9,     0,     0,     0,    10,    11,    67,
      68,    69,    12,     0,    13,    14,    15,    16,    17,    18,
      19,   312,     0,   313,     0,     0,    20,    21,    22,    23,
      24,    25,    26,     0,     0,    27,     0,     0,     0,     0,
       0,     0,    29,     0,     0,    32,    33,    34,    35,    36,
      37,    38,    39,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,   211,     0,     0,   212,    54,     0,
      55,    56,     0,     0,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,    67,    68,    69,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,   242,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,    30,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    50,    51,     0,     0,     0,     0,     0,     0,
      52,     0,     0,    53,    54,     0,    55,    56,     0,    57,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     5,     6,     7,     8,     9,     0,     0,     0,    10,
      11,    67,    68,    69,    12,     0,    13,    14,    15,    16,
      17,    18,    19,   522,     0,     0,     0,     0,    20,    21,
      22,    23,    24,    25,    26,     0,     0,    27,     0,     0,
       0,     0,     0,    28,    29,   263,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,    52,     0,     0,    53,
      54,     0,    55,    56,     0,    57,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    67,    68,    69,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   522,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
     139,   140,   141,   142,   143,   144,   145,   146,   147,   148,
     149,   150,   151,   152,     0,     0,     0,   153,   154,   155,
     411,   412,   413,   414,   160,   161,   162,     0,     0,     0,
       0,     0,   163,   164,   165,   166,   415,   416,   417,   418,
     171,    37,    38,   419,    40,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   173,   174,   175,   176,   177,   178,   179,   180,
     181,     0,     0,   182,   183,     0,     0,     0,     0,   184,
     185,   186,   187,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   188,   189,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   190,   191,   192,   193,
     194,   195,   196,   197,   198,   199,     0,   200,   201,     0,
       0,     0,     0,     0,   202,   420,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,   139,   140,   141,   142,
     143,   144,   145,   146,   147,   148,   149,   150,   151,   152,
       0,     0,     0,   153,   154,   155,   156,   157,   158,   159,
     160,   161,   162,     0,     0,     0,     0,     0,   163,   164,
     165,   166,   167,   168,   169,   170,   171,   295,   296,   172,
     297,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   173,   174,
     175,   176,   177,   178,   179,   180,   181,     0,     0,   182,
     183,     0,     0,     0,     0,   184,   185,   186,   187,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     188,   189,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   190,   191,   192,   193,   194,   195,   196,   197,
     198,   199,     0,   200,   201,     0,     0,     0,     0,     0,
     202,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
     148,   149,   150,   151,   152,     0,     0,     0,   153,   154,
     155,   156,   157,   158,   159,   160,   161,   162,     0,     0,
       0,     0,     0,   163,   164,   165,   166,   167,   168,   169,
     170,   171,   244,     0,   172,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   173,   174,   175,   176,   177,   178,   179,
     180,   181,     0,     0,   182,   183,     0,     0,     0,     0,
     184,   185,   186,   187,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   188,   189,     0,     0,    59,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   190,   191,   192,
     193,   194,   195,   196,   197,   198,   199,     0,   200,   201,
       0,     0,     0,     0,     0,   202,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,   139,   140,   141,   142,
     143,   144,   145,   146,   147,   148,   149,   150,   151,   152,
       0,     0,     0,   153,   154,   155,   156,   157,   158,   159,
     160,   161,   162,     0,     0,     0,     0,     0,   163,   164,
     165,   166,   167,   168,   169,   170,   171,     0,     0,   172,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   173,   174,
     175,   176,   177,   178,   179,   180,   181,     0,     0,   182,
     183,     0,     0,     0,     0,   184,   185,   186,   187,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     188,   189,     0,     0,    59,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   190,   191,   192,   193,   194,   195,   196,   197,
     198,   199,     0,   200,   201,     0,     0,     0,     0,     0,
     202,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
     148,   149,   150,   151,   152,     0,     0,     0,   153,   154,
     155,   156,   157,   158,   159,   160,   161,   162,     0,     0,
       0,     0,     0,   163,   164,   165,   166,   167,   168,   169,
     170,   171,     0,     0,   172,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   173,   174,   175,   176,   177,   178,   179,
     180,   181,     0,     0,   182,   183,     0,     0,     0,     0,
     184,   185,   186,   187,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   188,   189,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   190,   191,   192,
     193,   194,   195,   196,   197,   198,   199,     0,   200,   201,
       5,     6,     7,     0,     9,   202,     0,     0,    10,    11,
       0,     0,     0,    12,     0,    13,    14,    15,   250,   251,
      18,    19,     0,     0,     0,     0,     0,    20,   252,   253,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,   282,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   283,     0,     0,   212,    54,
       0,    55,    56,     0,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     5,
       6,     7,     0,     9,     0,     0,   284,    10,    11,     0,
       0,     0,    12,   285,    13,    14,    15,   250,   251,    18,
      19,     0,     0,     0,     0,     0,    20,   252,   253,    23,
      24,    25,    26,     0,     0,   209,     0,     0,     0,     0,
       0,     0,   282,     0,     0,    32,    33,    34,    35,    36,
      37,    38,    39,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   283,     0,     0,   212,    54,     0,
      55,    56,     0,     0,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     8,     9,     0,     0,   284,    10,    11,     0,     0,
       0,    12,   583,    13,    14,    15,    16,    17,    18,    19,
       0,     0,     0,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,    27,     0,     0,     0,     0,     0,
      28,    29,    30,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,    52,     0,     0,    53,    54,     0,    55,
      56,     0,    57,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,    67,    68,    69,    12,     0,    13,
      14,    15,    16,    17,    18,    19,     0,     0,     0,     0,
       0,    20,    21,    22,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,   210,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,   211,
       0,     0,   212,    54,     0,    55,    56,     0,   213,   214,
     215,    58,    59,   216,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     8,     9,     0,     0,     0,    10,    11,
      67,   217,    69,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,    27,     0,     0,     0,
       0,     0,    28,    29,     0,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,    52,     0,     0,    53,    54,
       0,    55,    56,     0,    57,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    67,    68,    69,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
       0,     0,     0,    20,    21,    22,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,   210,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,   458,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
     213,   214,   215,    58,    59,   216,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    67,   217,    69,    12,     0,    13,    14,    15,
     250,   251,    18,    19,     0,     0,     0,     0,     0,    20,
     252,   253,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,   210,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,   211,     0,     0,
     212,    54,     0,    55,    56,     0,   668,   214,   215,    58,
      59,   216,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    67,   217,
      69,    12,     0,    13,    14,    15,   250,   251,    18,    19,
       0,     0,     0,     0,     0,    20,   252,   253,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,   210,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,   458,     0,     0,     0,
       0,     0,     0,   211,     0,     0,   212,    54,     0,    55,
      56,     0,   668,   214,   215,    58,    59,   216,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,    67,   217,    69,    12,     0,    13,
      14,    15,   250,   251,    18,    19,     0,     0,     0,     0,
       0,    20,   252,   253,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,   210,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,   211,
       0,     0,   212,    54,     0,    55,    56,     0,   213,   214,
       0,    58,    59,   216,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      67,   217,    69,    12,     0,    13,    14,    15,   250,   251,
      18,    19,     0,     0,     0,     0,     0,    20,   252,   253,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,   210,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,     0,   214,   215,    58,    59,   216,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    67,   217,    69,    12,
       0,    13,    14,    15,   250,   251,    18,    19,     0,     0,
       0,     0,     0,    20,   252,   253,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,   210,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
     668,   214,     0,    58,    59,   216,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    67,   217,    69,    12,     0,    13,    14,    15,
     250,   251,    18,    19,     0,     0,     0,     0,     0,    20,
     252,   253,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,   210,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,   211,     0,     0,
     212,    54,     0,    55,    56,     0,     0,   214,     0,    58,
      59,   216,    61,    62,    63,    64,    65,    66,     0,     0,
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
      56,     0,   764,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,    67,   217,    69,    12,     0,    13,
      14,    15,   250,   251,    18,    19,     0,     0,     0,     0,
       0,    20,   252,   253,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,   211,
       0,     0,   212,    54,     0,    55,    56,     0,   764,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      67,   217,    69,    12,     0,    13,    14,    15,   250,   251,
      18,    19,     0,     0,     0,     0,     0,    20,   252,   253,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,   979,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    67,   217,    69,    12,
       0,    13,    14,    15,   250,   251,    18,    19,     0,     0,
       0,     0,     0,    20,   252,   253,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
    1033,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    67,   217,    69,    12,     0,    13,    14,    15,
     250,   251,    18,    19,     0,     0,     0,     0,     0,    20,
     252,   253,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,   211,     0,     0,
     212,    54,     0,    55,    56,     0,  1179,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    67,   217,
      69,    12,     0,    13,    14,    15,   250,   251,    18,    19,
       0,     0,     0,     0,     0,    20,   252,   253,    23,    24,
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
     209,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,   211,
       0,     0,   212,    54,     0,    55,    56,     0,     0,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      67,   217,    69,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,    27,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    67,    68,    69,    12,
       0,    13,    14,    15,   250,   251,    18,    19,     0,     0,
       0,     0,     0,    20,   252,   253,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,   282,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   283,     0,     0,   343,    54,     0,    55,    56,     0,
     344,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,     0,     0,
       0,    12,   284,    13,    14,    15,   250,   251,    18,    19,
       0,     0,     0,     0,     0,    20,   252,   253,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,   282,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   392,     0,     0,    53,    54,     0,    55,
      56,     0,    57,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
       0,     0,     0,    12,   284,    13,    14,    15,   250,   251,
      18,    19,     0,     0,     0,     0,     0,    20,   252,   253,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,   282,     0,     0,    32,    33,    34,   400,
      36,    37,    38,   401,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   402,     0,     0,     0,   403,     0,     0,   212,    54,
       0,    55,    56,     0,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,     0,     0,     0,    12,   284,    13,    14,    15,
     250,   251,    18,    19,     0,     0,     0,     0,     0,    20,
     252,   253,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,   282,     0,     0,    32,    33,
      34,   400,    36,    37,    38,   401,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   403,     0,     0,
     212,    54,     0,    55,    56,     0,     0,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,     0,     0,     0,    12,   284,    13,
      14,    15,   250,   251,    18,    19,     0,     0,     0,     0,
       0,    20,   252,   253,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,   282,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   283,
       0,     0,   343,    54,     0,    55,    56,     0,     0,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
     284,    13,    14,    15,   250,   251,    18,    19,     0,     0,
       0,     0,     0,    20,   252,   253,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,   282,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  1118,     0,     0,   212,    54,     0,    55,    56,     0,
       0,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,     0,     0,
       0,    12,   284,    13,    14,    15,   250,   251,    18,    19,
       0,     0,     0,     0,     0,    20,   252,   253,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,   282,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,    23,    24,    25,    26,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    32,    33,    34,
     914,     0,     0,     0,   915,     0,     0,    41,    42,    43,
      44,    45,     0,  1207,     0,     0,   212,    54,     0,    55,
      56,     0,     0,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,   917,   918,
       0,     0,     0,     0,     0,     0,   919,     0,     0,   920,
       0,     0,   921,   922,   284,  1145,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,    23,    24,    25,
      26,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     925,     0,     0,    32,    33,    34,   914,   284,     0,     0,
     915,     0,     0,    41,    42,    43,    44,    45,     0,     0,
      23,    24,    25,    26,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    32,    33,    34,   914,
       0,     0,     0,   915,   917,   918,    41,    42,    43,    44,
      45,     0,   919,     0,     0,   920,     0,     0,   921,   922,
       0,   923,     0,     0,    58,    59,    60,    61,    62,    63,
      64,    65,    66,     0,     0,     0,     0,   917,   918,     0,
       0,     0,     0,     0,     0,   919,   925,     0,   920,     0,
       0,   921,   922,   284,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,   613,   614,     0,     0,
     615,     0,     0,     0,     0,     0,     0,     0,     0,   925,
       0,     0,     0,     0,     0,     0,   284,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   622,   623,     0,     0,
     624,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   672,   614,     0,     0,
     673,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   675,   623,     0,     0,
     676,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   702,   614,     0,     0,
     703,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   705,   623,     0,     0,
     706,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   814,   614,     0,     0,
     815,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   817,   623,     0,     0,
     818,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   823,   614,     0,     0,
     824,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   657,   623,     0,     0,
     658,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,  1039,   614,     0,     0,
    1040,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,  1042,   623,     0,     0,
    1043,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,  1246,   614,     0,     0,
    1247,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,  1249,   623,     0,     0,
    1250,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,  1283,   614,     0,     0,
    1284,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   657,   623,     0,     0,
     658,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,     0,     0,     0,     0,
       0,   202
};

static const yytype_int16 yycheck[] =
{
       2,     8,    60,   380,    59,    91,   105,    16,    17,   385,
      71,   324,     2,    28,     4,   328,   324,     8,   104,   331,
     328,    28,    96,   328,   556,   802,   539,   736,    22,    53,
      22,   403,   238,    16,    17,   733,    97,    28,    57,   232,
     444,   302,   303,    85,   740,   276,   101,   809,   452,   280,
     452,     4,    59,    55,    56,   379,   784,   381,    68,    16,
      17,    93,    94,    53,    54,    97,    85,    57,   783,   784,
     382,   448,    55,   736,    76,    77,     2,    97,     4,    98,
      99,   100,    94,   871,   733,    97,    80,    53,    80,    57,
     492,   740,   921,   102,   101,    85,   502,    67,    55,    56,
      77,   869,    71,   586,   428,  1068,    77,    29,    98,    99,
     100,   101,    52,    16,    17,    27,    25,   660,   661,   102,
     783,   784,    66,   447,  1184,   449,  1211,   504,    54,    66,
      25,    13,  1028,    68,    66,    97,   100,    66,    68,   264,
      68,   227,    34,   917,   918,   102,    25,    66,    26,   789,
      27,    99,    29,   477,    25,    25,   144,   797,   216,   586,
      52,    96,    97,    25,    13,   153,    96,    97,     0,    97,
     256,    52,    25,    54,    55,   119,    57,   583,   126,   153,
     561,   505,   563,   145,   121,   122,   148,   119,   212,   153,
      25,   143,   121,   122,  1279,    13,    37,    38,   146,   102,
     119,   123,   121,   122,   290,   266,   146,   720,   721,   241,
     145,   243,    13,   148,  1274,   152,   218,   219,   343,    13,
     101,    26,   212,   152,   226,   249,   235,    13,   237,   238,
     232,   146,  1128,   660,   661,   143,   238,   146,   153,   151,
     264,   153,   219,   285,    13,   247,   266,   218,   219,  1078,
    1079,   146,   235,   148,   237,   238,   517,   247,   519,   249,
    1223,   253,   254,   640,   146,   143,   285,   146,   751,   151,
     463,   153,   262,   650,   264,   146,   146,   649,   235,   683,
     237,   685,    25,   685,   146,   317,   318,   319,   320,   740,
     322,   323,   743,   146,   727,   285,    13,   266,   264,  1073,
    1074,  1051,   151,   736,   153,   317,   318,   319,   320,   395,
     287,   146,   153,  1009,   284,   100,  1014,   641,   109,   343,
    1070,   247,    25,    25,  1112,   344,   316,   651,   389,    56,
     148,   321,   235,   151,   237,   153,   262,   327,   143,   341,
     652,   331,   133,   148,   346,  1113,    13,   148,   152,   881,
     151,    13,   153,   343,   344,   387,   869,   151,   871,   153,
    1009,   146,   386,   316,   388,   151,   772,   153,  1096,   389,
     402,   380,    68,    99,   381,   387,   344,   343,   380,  1028,
      13,  1096,   151,   126,   153,    68,   100,   126,   792,   381,
     316,   152,   382,  1056,   707,   321,   386,    68,   388,   707,
     126,    97,   707,   146,  1181,   282,   149,   146,  1071,    68,
     153,   152,    68,    96,    97,  1078,  1079,   960,   961,  1181,
     733,   428,   965,   966,   126,    96,    97,   740,    25,   431,
     432,    52,   146,  1096,   151,    56,   153,    68,    97,   448,
     442,    97,   449,   146,   146,   148,   448,   149,   450,   451,
     711,   153,    99,    68,   152,   716,   471,   449,    25,   461,
     502,   463,   145,    77,   471,    96,    97,  1217,   699,    28,
     477,   126,  1222,  1223,   145,    68,   478,   683,   671,   126,
     471,    96,    97,   502,   151,   477,   153,   444,    68,   151,
     126,   153,    68,   148,   484,   504,   877,   878,   505,   146,
    1209,   882,   504,   884,    97,   886,  1204,    52,   569,    34,
     822,    56,   502,   505,   145,  1265,  1293,    97,   151,    66,
     153,    97,   136,   137,   138,   783,   784,    52,  1028,   126,
     145,   126,   489,   960,   961,   912,    66,    67,   965,   966,
     853,   583,  1292,   545,  1087,   853,  1209,   919,   853,   146,
     146,   148,   149,    99,   556,  1204,   153,   566,  1009,   126,
    1011,   570,  1211,    99,   583,  1016,   568,    66,   642,   151,
     580,   153,   119,   634,   121,   122,   586,  1028,    68,   146,
     126,    66,   149,   112,    68,    68,   153,   911,  1286,   913,
     126,   121,   122,   583,  1126,  1028,  1000,   580,  1000,  1112,
    1113,    15,  1134,   586,  1217,   981,   630,    97,   632,  1222,
    1037,    25,    96,    97,    97,   150,    68,    99,   153,   621,
     119,   653,   121,   122,   618,   627,   618,    77,    82,    83,
    1279,   640,   146,   627,   641,   627,   121,   122,   640,   124,
     630,   650,   632,    99,   651,    97,  1022,   662,   650,   641,
     660,   661,  1265,   148,   847,   662,   688,   659,   700,   651,
    1087,   145,   652,   921,    68,   659,    56,   659,     9,   671,
     126,   662,    54,   985,    15,   129,   130,    68,   126,    68,
     938,   700,    64,    65,   134,   135,   136,   137,   138,    66,
      68,   693,    96,    97,    99,   781,  1009,    58,  1136,  1137,
     692,   693,   100,  1075,  1204,    96,    97,    96,    97,  1209,
     700,  1211,    15,  1213,    17,  1028,    77,  1093,    96,    97,
    1032,   126,   148,   241,    56,   602,   683,   153,  1260,    68,
     772,   830,  1028,  1114,  1115,  1116,  1117,    68,   146,  1190,
      99,   145,   119,   620,   121,   122,    99,   108,   109,   751,
     752,   269,    66,   772,   145,   273,   145,    96,    97,   820,
    1211,   146,  1213,   146,   148,    96,    97,   145,   751,   148,
     153,  1204,   133,   126,   829,    99,  1209,    99,  1211,  1279,
    1213,  1281,   772,   149,   786,   787,  1286,   153,  1288,   821,
    1048,   793,   794,   783,   784,   819,  1108,   674,   800,   148,
     802,  1177,   126,  1303,   126,   119,   145,   121,   122,   821,
    1068,    37,    38,    77,   145,    14,    15,  1255,  1256,  1196,
    1078,  1079,   829,  1261,   146,  1263,  1264,   704,  1279,   819,
    1281,   149,   822,   126,    68,   792,   144,  1288,  1096,   841,
     842,    99,   844,   845,   146,   847,  1279,    68,  1281,   150,
      99,   153,  1303,  1286,    56,  1288,    68,  1295,  1296,  1297,
    1298,   148,    96,    97,  1245,   146,   924,    99,   126,  1307,
    1303,   895,    77,  1197,   126,    96,    97,   126,   210,   881,
     126,   213,   214,   215,    96,    97,    99,   126,   146,   891,
     106,  1204,    99,    70,   126,   146,    99,   146,  1211,   901,
      56,   903,    99,   912,   151,   895,   913,   909,  1204,    25,
     912,   145,   130,   126,   146,  1211,   906,  1213,   908,   126,
       2,   913,     4,   126,   145,   986,    99,   917,   918,   126,
     126,   921,   126,   145,    16,    17,   454,  1056,   970,   816,
     149,   459,   146,   146,   462,   969,   146,   465,   938,   826,
     960,   961,   144,   126,    58,   965,   966,   144,   970,  1078,
    1079,   146,   964,   481,    88,    89,  1279,   146,   486,   126,
      66,    53,    54,    77,   146,    57,   146,    26,   146,   969,
     906,   146,   908,  1279,    52,  1281,    68,   146,    54,    55,
    1286,    57,  1288,    52,   996,   985,    66,    10,    64,    65,
       8,  1025,  1021,    85,   108,  1029,   144,  1303,   112,    40,
      41,    93,    94,   286,   287,    97,    98,    99,   100,    68,
     102,   146,    13,   119,   146,   121,   122,  1037,   124,   547,
    1068,  1021,    25,  1065,  1120,  1025,   146,    17,   152,  1029,
    1078,  1079,  1032,   152,   921,   922,   146,    96,    97,   119,
     126,   121,   122,  1065,   124,    44,    44,   144,  1048,   126,
     146,  1051,   146,   128,   582,    44,    26,    52,    44,    54,
      55,    56,    57,   150,    15,   146,  1100,  1087,  1068,  1069,
    1070,    52,   146,  1073,  1074,  1109,   146,   131,  1078,  1079,
     146,   146,  1150,   100,   143,   146,   145,    52,   131,   148,
    1124,    40,    41,    42,    43,    44,  1096,    26,    68,   146,
    1100,   443,   444,  1132,   674,    52,   101,   146,  1108,  1109,
     452,   146,   107,  1125,  1126,  1002,    59,    60,    61,    62,
     212,   146,  1134,   100,  1124,   146,    96,    97,    56,   144,
     146,   151,  1132,    52,   704,    54,    55,    56,    57,    68,
     149,  1028,  1176,   235,   146,   237,   238,   489,     9,   241,
     492,   243,   131,  1187,  1041,   247,    52,   249,    54,    55,
      56,    57,   146,  1050,   146,   131,  1053,    96,    97,  1181,
     262,   146,   264,   143,  1203,   145,  1176,  1196,   148,   146,
    1197,   149,   101,   146,  1196,   146,  1198,  1187,   146,  1076,
    1232,  1233,   146,   285,   144,  1197,   724,    52,    56,    54,
      55,    56,    57,  1203,   146,   120,     2,   146,     4,   146,
    1232,  1233,   554,   146,   143,   146,   145,  1217,  1230,   148,
      16,    17,  1222,  1223,   316,   317,   318,   319,   320,   321,
     322,   323,   146,   146,   247,   327,   148,  1271,   580,   331,
     148,  1128,   480,   484,   586,   829,   816,    98,  1260,    89,
     100,   343,   344,   659,   752,  1230,   826,    53,    54,   720,
     869,  1130,  1028,   346,  1151,  1265,   903,  1274,   831,   574,
    1293,  1271,    68,  1071,    52,   339,    54,    55,    56,    57,
      58,  1293,  1069,  1071,  1171,  1172,  1173,  1067,   380,  1198,
     382,  1125,  1292,   108,   386,   387,   388,    93,    94,    77,
     828,    97,   101,   519,   736,  1209,   102,  1204,   733,    -1,
     402,  1151,    -1,    -1,    -1,    -1,    -1,    -1,   846,    -1,
     848,   663,    -1,   101,    -1,    -1,   668,    77,    -1,   107,
     108,   109,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   867,
      -1,   683,    -1,   685,    94,    95,    -1,    -1,   431,   432,
      -1,   921,  1239,    -1,    -1,   133,   448,    -1,   136,   442,
      -1,  1248,    -1,    -1,   660,   661,    -1,   450,   451,    -1,
      -1,    -1,   571,    -1,    -1,   153,    -1,   576,    -1,   578,
      -1,   677,   678,   133,   134,   135,   136,   137,   138,   731,
      -1,    -1,   484,    -1,    -1,   478,    -1,    -1,   694,    -1,
      52,    -1,    54,    55,    56,    57,    58,    -1,    -1,   751,
     502,    -1,   504,    -1,    -1,    -1,   212,    -1,    -1,    -1,
      -1,    -1,   764,    -1,    -1,    77,    -1,    -1,    -1,   628,
      -1,    -1,    -1,    -1,   633,    -1,   635,    -1,    -1,   235,
      -1,   237,   238,    77,    -1,   241,    -1,   243,    -1,   101,
     792,   247,    -1,   249,    -1,   107,   108,   109,    77,    -1,
      94,    95,    -1,    -1,    -1,    -1,   262,    -1,   264,    -1,
     812,  1041,    -1,    -1,   566,    94,    95,    -1,   570,    -1,
    1050,   133,    -1,  1053,   136,    -1,    -1,    -1,   580,    -1,
      -1,   583,    -1,    -1,   586,    -1,    -1,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,  1076,    -1,    52,    -1,
      54,    55,    56,    57,    58,   134,   135,   136,   137,   138,
     316,   317,   318,   319,   320,   321,   322,   323,    -1,    -1,
      -1,   327,    -1,    77,    -1,   331,    -1,    -1,   630,    -1,
     632,    -1,    -1,    -1,    -1,    -1,    -1,   343,   640,  1077,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   101,   650,    -1,
     652,   653,    -1,   905,   108,   109,    -1,    -1,   660,   661,
      52,    -1,    54,    55,    56,    57,    58,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   380,    -1,   382,    -1,    -1,   133,
     386,   387,   388,    -1,    -1,    77,   688,    -1,    -1,    -1,
      -1,  1171,  1172,  1173,    -1,    -1,   402,    -1,   700,    91,
      -1,   810,    -1,    -1,   813,  1143,    -1,    -1,    -1,   101,
      -1,    -1,    -1,    -1,    -1,   107,   108,   109,   827,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   979,    -1,    -1,
    1168,  1169,  1170,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   133,   448,    -1,   136,    -1,    -1,    -1,  1000,    -1,
      -1,    -1,    -1,    -1,   960,   961,   148,    -1,    -1,   965,
     966,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1248,    -1,
     772,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   484,    -1,
      -1,  1033,    -1,    -1,    -1,    -1,    -1,    -1,   994,   995,
      -1,   997,   998,   786,   787,    -1,    -1,    -1,   504,    -1,
     793,   794,    -1,    -1,     2,    -1,     4,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   819,    -1,   821,
     822,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    52,    -1,
      54,    55,    56,    57,    58,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1099,   841,   842,
      -1,   844,   845,    77,    -1,    53,    54,    -1,    -1,    57,
     566,    -1,   971,    -1,   570,   974,    -1,    91,   977,    -1,
      -1,    -1,    -1,    -1,   580,   984,    -1,   101,   987,    -1,
     586,  1087,   783,   784,   108,   109,    -1,    85,    -1,    -1,
      -1,    -1,    -1,   895,    -1,    -1,    -1,    -1,    -1,  1105,
      98,    99,   100,    -1,   906,    -1,   908,    -1,    -1,   133,
     912,    -1,    -1,    -1,    -1,    -1,   909,    52,    -1,    54,
      55,    56,    57,    58,   630,    -1,   632,  1179,    -1,    -1,
      -1,    -1,    -1,    -1,   640,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    77,    -1,   650,    -1,   652,   653,    -1,    -1,
      -1,    -1,    -1,    -1,   660,   661,    91,    -1,   960,   961,
      -1,    -1,    -1,   965,   966,    -1,   101,   969,   970,    -1,
      -1,   964,   107,   108,   109,    -1,    -1,     2,    -1,     4,
      -1,    -1,   688,   985,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    16,    17,  1102,    -1,    -1,    -1,    -1,   133,    -1,
      -1,   136,    -1,   996,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   212,    -1,   917,   918,    -1,  1021,
     921,    -1,    -1,  1025,    -1,    -1,    -1,  1029,    53,    54,
    1032,    -1,    -1,    -1,    -1,  1037,    -1,   938,    -1,    -1,
      -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,   247,
      -1,   249,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1065,   262,    -1,   264,    -1,    93,    94,
      -1,    -1,    97,    -1,    -1,    -1,  1185,   102,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,  1087,    -1,   285,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1100,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1108,  1109,    -1,    -1,
      -1,    -1,    -1,   819,    -1,   821,   822,    -1,   316,    -1,
      -1,    -1,  1124,   321,    -1,    -1,    -1,    -1,    -1,   327,
    1132,    -1,    -1,   331,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   343,   344,  1048,    -1,    -1,
    1051,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1068,  1069,  1070,
      -1,    -1,  1073,  1074,  1176,    -1,    -1,  1078,  1079,    -1,
      -1,    -1,    -1,    -1,   382,  1187,    -1,   212,   386,   895,
     388,    -1,    -1,    -1,  1196,  1096,    -1,    -1,    -1,    -1,
     906,  1203,   908,    -1,    -1,    -1,   912,    -1,    -1,    -1,
     235,    -1,   237,   238,    -1,    -1,   241,    -1,   243,    -1,
      -1,    -1,   247,    -1,   249,    -1,    -1,    -1,    -1,    -1,
    1232,  1233,    -1,    -1,    -1,    -1,    -1,   262,    -1,   264,
      -1,    -1,    -1,    -1,   783,   784,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   960,   961,    -1,    -1,    -1,   965,
     966,    -1,    -1,   969,   970,    -1,    -1,    -1,    -1,  1271,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   985,
      -1,    -1,    -1,    -1,    -1,    -1,   484,    -1,    -1,    -1,
      -1,   316,   317,   318,   319,   320,   321,   322,   323,    -1,
      -1,    -1,   327,    -1,   502,    -1,   331,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1217,    -1,   343,  1025,
      -1,  1222,  1223,  1029,    -1,    -1,  1032,    -1,    -1,    -1,
      -1,  1037,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   380,    -1,   382,    -1,  1065,
      -1,   386,   387,   388,  1265,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   402,   917,   918,
      -1,  1087,   921,    -1,    -1,   583,    -1,    -1,   586,    -1,
      -1,  1292,    -1,    -1,  1100,    -1,    -1,    -1,    -1,   938,
      -1,    -1,  1108,  1109,    -1,    -1,    -1,    -1,    -1,     2,
      -1,     4,    -1,    -1,    -1,    -1,    -1,    -1,  1124,    -1,
      -1,    -1,    -1,   448,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   630,    -1,   632,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   652,    -1,    -1,    -1,    -1,   484,
      53,    54,   660,   661,    57,    -1,    -1,    -1,    -1,    -1,
    1176,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   504,
      -1,  1187,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1196,    -1,    85,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   700,    -1,    -1,    98,    99,   100,    -1,  1048,
      -1,   709,  1051,    -1,    -1,   783,   784,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1232,  1233,    -1,  1068,
    1069,  1070,    -1,    -1,  1073,  1074,    -1,    -1,    -1,  1078,
    1079,   566,    -1,    -1,    -1,   570,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   580,    -1,  1096,    -1,    -1,
      -1,   586,    -1,    -1,    -1,  1271,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   772,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   630,    -1,   632,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   640,    -1,    -1,    -1,   212,
      -1,   819,    -1,    -1,   822,   650,    -1,   652,   653,    -1,
      -1,    -1,    -1,    -1,    -1,   660,   661,    -1,    -1,     2,
      -1,     4,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   917,
     918,    -1,    -1,   921,   247,    -1,   249,    -1,    -1,    -1,
      -1,    -1,    -1,   688,    -1,    -1,    -1,    -1,    -1,   262,
     938,   264,    -1,    -1,    -1,    -1,    -1,    -1,  1217,    -1,
      -1,    -1,    -1,  1222,  1223,    -1,    -1,    -1,    -1,    -1,
      53,    54,   285,    -1,    57,    -1,    -1,   895,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   906,    -1,
     908,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    85,   316,    -1,    -1,  1265,    -1,   321,    -1,
      -1,    -1,    -1,    -1,   327,    98,    99,   100,   331,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     343,   344,    -1,  1292,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   960,   961,    -1,    -1,    -1,   965,   966,    -1,
      -1,   969,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1048,    -1,    -1,  1051,    -1,    -1,    -1,   985,    -1,   382,
      -1,    -1,    -1,   386,   819,   388,   821,   822,    -1,    -1,
    1068,  1069,  1070,    -1,    -1,  1073,  1074,    -1,    -1,    -1,
    1078,  1079,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1021,    -1,    -1,    -1,  1025,  1096,    -1,
      -1,  1029,    -1,    -1,  1032,    -1,    -1,   783,   784,  1037,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   212,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     895,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   906,    -1,   908,   247,    -1,   249,   912,    -1,  1087,
      -1,   484,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   262,
      -1,   264,  1100,    -1,    -1,    -1,    -1,    -1,    -1,   502,
    1108,  1109,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   285,    -1,    -1,    -1,  1124,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1132,   960,   961,    -1,    -1,    -1,
     965,   966,    -1,    -1,   969,   970,    -1,    -1,    -1,  1217,
      -1,    -1,    -1,   316,  1222,  1223,    -1,    -1,   321,    -1,
     985,    -1,    -1,    -1,   327,    -1,    -1,    -1,   331,    -1,
      -1,   917,   918,    -1,    -1,   921,    -1,    -1,  1176,    -1,
     343,   344,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1187,
     583,    -1,   938,   586,    -1,    -1,    -1,  1265,    -1,    -1,
    1025,    -1,    -1,    -1,  1029,  1203,    -1,  1032,    -1,    -1,
      -1,    -1,  1037,    -1,   783,   784,    -1,    -1,    -1,   382,
      -1,    -1,    -1,   386,  1292,   388,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   630,    -1,   632,
    1065,    -1,    -1,   783,   784,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   652,
      -1,    -1,  1087,    -1,    -1,    -1,    -1,   660,   661,    -1,
      -1,    -1,    -1,  1271,    -1,  1100,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1108,  1109,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1124,
      -1,    -1,  1048,    -1,     2,  1051,     4,   700,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   484,  1068,  1069,  1070,   783,   784,  1073,  1074,    -1,
      -1,    -1,  1078,  1079,    -1,    -1,    -1,    -1,     2,   502,
       4,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   917,   918,
    1096,  1176,   921,    -1,    -1,    53,    54,    -1,    -1,    57,
      -1,    -1,  1187,    -1,    -1,    -1,    -1,    -1,    -1,   938,
      -1,  1196,    -1,    -1,    -1,    -1,    -1,   917,   918,   772,
      -1,   921,    -1,    -1,    -1,    -1,    -1,    85,    -1,    53,
      54,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   938,    -1,
      98,    99,   100,   101,    -1,    -1,    -1,  1232,  1233,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     583,    -1,    -1,   586,    -1,    -1,   819,    -1,    -1,   822,
      -1,    -1,    -1,    -1,    98,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1271,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   917,
     918,    -1,    -1,   921,    -1,    -1,    -1,   630,    -1,   632,
      -1,  1217,    -1,    -1,    -1,    -1,  1222,  1223,    -1,  1048,
     938,    -1,  1051,    -1,    -1,    -1,    -1,    -1,    -1,   652,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   660,   661,  1068,
    1069,  1070,   895,    -1,  1073,  1074,    -1,    -1,  1048,  1078,
    1079,  1051,    -1,   906,   212,   908,    -1,    -1,    -1,  1265,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1096,  1068,  1069,
    1070,    -1,    -1,  1073,  1074,    -1,    -1,   700,  1078,  1079,
      -1,    -1,    -1,    -1,    -1,    -1,  1292,    -1,   212,   247,
      -1,   249,    -1,    -1,    -1,    -1,  1096,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   262,    -1,   264,   960,   961,    -1,
      -1,    -1,   965,   966,    -1,    -1,   969,    -1,    -1,     2,
      -1,     4,    -1,   247,    -1,   249,    -1,   285,    -1,    -1,
    1048,    -1,   985,  1051,    -1,    -1,    -1,    -1,   262,    -1,
     264,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   772,
    1068,  1069,  1070,    -1,    -1,  1073,  1074,    -1,   316,    -1,
    1078,  1079,    -1,   321,    -1,    -1,    -1,    -1,  1021,   327,
      53,    54,  1025,   331,    -1,    -1,  1029,    -1,  1096,  1032,
      -1,    -1,    -1,    -1,  1037,   343,   344,    -1,  1217,    -1,
      -1,    -1,   316,  1222,  1223,    -1,   819,   321,    -1,   822,
      -1,    -1,    -1,   327,    -1,    -1,    -1,   331,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1217,    -1,   343,
      -1,    -1,  1222,  1223,   382,    -1,    -1,    -1,   386,    -1,
     388,    -1,    -1,    -1,  1087,    -1,  1265,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1100,   783,   784,
      -1,    -1,    -1,    -1,    -1,  1108,  1109,    -1,   382,    -1,
      -1,    -1,   386,  1292,   388,  1265,    -1,    -1,    -1,    -1,
      -1,  1124,   895,    -1,    -1,    -1,    -1,    -1,    -1,  1132,
      -1,   783,   784,   906,    -1,   908,    -1,    -1,    -1,    -1,
      -1,    -1,  1292,    -1,    -1,    -1,    -1,    -1,    -1,  1217,
      -1,    -1,    -1,    -1,  1222,  1223,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1176,    -1,    -1,   484,    -1,    -1,   212,
      -1,    -1,    -1,    -1,  1187,    -1,    -1,   960,   961,    -1,
      -1,    -1,   965,   966,   502,    -1,   969,  1265,    -1,    -1,
    1203,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     484,    -1,   985,    -1,   247,    -1,   249,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1292,    -1,    -1,    -1,    -1,   262,
      -1,   264,   917,   918,    -1,    -1,   921,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1021,    -1,
      -1,    -1,  1025,   938,    -1,    -1,  1029,    -1,    -1,  1032,
      -1,    -1,    -1,    -1,  1037,   917,   918,    -1,  1271,   921,
      -1,    -1,    -1,    -1,    -1,   583,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   316,    -1,    -1,   938,    -1,   321,    -1,
      -1,    -1,    -1,    -1,   327,    -1,    -1,    -1,   331,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     343,    -1,   586,    -1,  1087,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   630,    -1,   632,    -1,    -1,  1100,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,  1108,  1109,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   652,    -1,    -1,    -1,    -1,   382,
      -1,  1124,    -1,   386,    -1,   388,   630,    -1,   632,  1132,
      -1,    -1,    -1,  1048,    -1,    -1,  1051,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   652,    -1,
      -1,    -1,    -1,  1068,  1069,  1070,   660,   661,  1073,  1074,
      -1,    -1,   700,  1078,  1079,    -1,  1048,    -1,    -1,  1051,
      -1,    -1,    -1,  1176,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,  1096,    -1,    -1,  1187,    -1,  1068,  1069,  1070,    -1,
      -1,  1073,  1074,    -1,    -1,    -1,  1078,  1079,    -1,    -1,
    1203,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1096,    -1,    -1,    -1,    -1,    -1,
      -1,   484,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   772,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     0,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       8,     9,    10,    -1,    -1,    13,    14,    15,  1271,    17,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,    26,    27,
      -1,   819,    -1,    -1,   822,    -1,    -1,    -1,    -1,    37,
      38,    -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,
      -1,    -1,  1217,    -1,    -1,    -1,    -1,  1222,  1223,    -1,
      -1,    -1,    -1,    -1,    -1,   819,    -1,    -1,   822,    -1,
      68,    -1,    -1,   586,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,  1217,    -1,    -1,    -1,    -1,
    1222,  1223,    -1,    -1,    -1,    -1,    -1,    -1,    96,    97,
    1265,    -1,    -1,    -1,    -1,    -1,    -1,   895,    -1,    -1,
      -1,    -1,    -1,    -1,    25,    -1,    -1,   630,   906,   632,
     908,    -1,   120,    -1,    -1,    -1,    -1,  1292,    -1,    -1,
      -1,    -1,    -1,  1265,    -1,    -1,    -1,    -1,    -1,   652,
      -1,   895,    -1,    -1,    -1,   143,   144,   660,   661,    -1,
     148,   149,   906,   151,   908,   153,    -1,    -1,    -1,    -1,
    1292,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    -1,
      -1,   969,    -1,    94,    95,    -1,    -1,    -1,    -1,   100,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   985,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   960,   961,    -1,    -1,
      -1,   965,   966,    16,    17,   969,   127,    -1,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,   985,    -1,  1021,    -1,    -1,    -1,  1025,    -1,    -1,
      -1,  1029,    -1,    -1,  1032,    48,    49,    50,    51,    -1,
      -1,    -1,    55,    56,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    68,    69,  1021,    -1,    -1,
      -1,  1025,    -1,    -1,    -1,  1029,    -1,    -1,  1032,    -1,
      -1,    -1,    -1,  1037,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   102,
      -1,    -1,    -1,    -1,    -1,    -1,   819,    -1,    -1,   822,
      -1,    -1,  1100,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1108,  1109,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1087,    -1,    -1,  1124,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1132,    -1,  1100,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1108,  1109,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1124,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   895,    -1,    -1,    -1,    -1,    -1,  1176,    -1,
      -1,    -1,    -1,   906,    -1,   908,    -1,    -1,    -1,  1187,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   210,    -1,    -1,
     213,   214,   215,    -1,   217,  1203,    -1,    -1,    -1,    -1,
      -1,    -1,  1176,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   235,  1187,   237,   238,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   960,   961,    -1,
      -1,    -1,   965,   966,    -1,    -1,   969,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   985,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    84,  1271,    86,    87,    -1,    -1,    -1,    -1,
      -1,    -1,    94,    95,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,  1025,    -1,    -1,    -1,  1029,  1271,    -1,  1032,
      -1,    -1,    -1,    -1,  1037,    -1,    -1,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   348,   349,   350,   351,   352,
      -1,    -1,   355,   356,   357,   358,   359,   360,   361,   362,
      -1,   364,    -1,    -1,   367,   368,   369,   370,   371,   372,
     373,   374,   375,   376,  1087,    -1,    -1,   380,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1100,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,  1108,  1109,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,  1124,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     443,   444,    -1,    -1,    -1,   448,    -1,    -1,    -1,   452,
      -1,    -1,    -1,    -1,    -1,   458,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1176,    -1,    33,    34,    35,    36,    -1,
     473,    -1,    -1,    -1,  1187,    -1,    -1,    -1,    -1,    -1,
      -1,    49,    50,    51,    52,    -1,   489,    -1,    56,   492,
      -1,    59,    60,    61,    62,    63,    -1,    -1,    -1,    -1,
      -1,   504,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   521,    -1,
      -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,
      98,    -1,    -1,   101,    -1,    -1,   104,   105,    -1,   107,
      -1,    -1,   110,   111,   112,   113,   114,   115,   116,   117,
     118,   554,    -1,    -1,    -1,    -1,    -1,    -1,  1271,    -1,
      -1,    -1,    -1,   566,   132,    -1,    -1,   570,    -1,    -1,
      -1,   139,    -1,    -1,    -1,    -1,    -1,   580,    -1,    -1,
      -1,    -1,    -1,   586,    -1,   153,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    33,    34,    35,    36,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    49,    50,    51,    -1,    -1,   640,    -1,    -1,
      -1,    -1,    59,    60,    61,    62,    63,   650,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   660,   661,    -1,
     663,   664,   665,   666,    -1,   668,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   677,   678,    -1,    -1,    -1,    -1,
     683,    -1,   685,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   694,    -1,   110,   111,   112,   113,   114,   115,   116,
     117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       0,     1,   139,     3,     4,     5,     6,     7,   731,    -1,
      -1,    11,    12,    -1,    -1,    -1,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,    -1,    -1,    -1,   751,    -1,
      30,    31,    32,    33,    34,    35,    36,    -1,    -1,    39,
      -1,   764,    -1,    -1,    -1,    45,    46,    47,    48,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    63,    64,    65,    -1,    -1,    -1,   792,
      -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   812,
      90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,
      -1,   101,   102,    -1,   104,   105,    -1,   107,    -1,    -1,
     110,   111,   112,   113,   114,   115,   116,   117,   118,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   139,
     140,   141,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   151,    -1,   153,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   887,   888,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   899,   900,    -1,    -1,
      -1,    -1,   905,    33,    34,    35,    36,   910,    -1,   912,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    49,
      50,    51,    52,    -1,    -1,    -1,    56,    -1,    58,    59,
      60,    61,    62,    63,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   959,   960,   961,    -1,
      90,    91,   965,   966,    -1,    -1,    -1,    -1,    98,    -1,
      -1,   101,    -1,    -1,   104,   105,   979,   107,   108,    -1,
     110,   111,   112,   113,   114,   115,   116,   117,   118,    -1,
      -1,   994,   995,    -1,   997,   998,    -1,  1000,    -1,    -1,
      -1,    -1,   132,    -1,    -1,    -1,    -1,    -1,    -1,   139,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     0,
    1033,    -1,    -1,    -1,  1037,    -1,    -1,     8,     9,    10,
      -1,    -1,    13,    14,    15,    -1,    17,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    25,    26,    27,    28,    29,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,
      41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1087,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1099,    68,    -1,    -1,
      -1,    -1,  1105,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    -1,
      -1,    -1,    -1,    94,    95,    96,    97,    -1,    99,   100,
      -1,    -1,    -1,    -1,    -1,   106,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   120,
      -1,    -1,   123,    -1,    -1,   126,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,    -1,   143,   144,   145,   146,  1179,     0,   149,   150,
     151,    -1,   153,    -1,    -1,     8,     9,    10,    -1,    -1,
      13,    14,    15,  1196,    17,    -1,    -1,    -1,    -1,    44,
      -1,    -1,    25,    -1,    27,    28,    29,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,
      43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    77,    78,    79,    80,    81,    82,    83,    84,
      85,    86,    87,    88,    89,    68,    -1,    -1,    -1,    94,
      95,    -1,    -1,    -1,    77,    78,    79,    80,    81,    82,
      83,    84,    85,    86,    87,    88,    89,    -1,    -1,    -1,
      -1,    94,    95,    96,    97,    -1,    99,   100,    -1,    -1,
      -1,    -1,   127,   106,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,    -1,    -1,   120,    -1,    -1,
     123,   146,    -1,   126,   127,   128,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,    -1,    -1,    -1,    -1,
      -1,   144,   145,   146,     0,    -1,   149,   150,   151,    -1,
     153,    -1,     8,     9,    10,    -1,    -1,    13,    14,    15,
      -1,    17,    -1,    -1,    -1,    -1,    44,    -1,    -1,    25,
      -1,    27,    28,    29,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    37,    38,    -1,    40,    41,    42,    43,    44,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    68,    -1,    -1,    -1,    94,    95,    -1,    -1,
      -1,    77,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    -1,    -1,    -1,    -1,    94,    95,
      96,    97,    -1,    99,   100,    -1,    -1,    -1,    -1,   127,
     106,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,    -1,    -1,    -1,   120,    -1,    -1,   123,    -1,    -1,
     126,   127,   128,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,    -1,    -1,    -1,    -1,   144,   145,
     146,     0,    -1,   149,   150,   151,    -1,   153,    -1,     8,
       9,    10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    44,    -1,    -1,    25,    26,    27,    28,
      29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    68,
      -1,    -1,    -1,    94,    95,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    -1,    -1,    94,    95,    96,    97,    -1,
      -1,   100,    -1,    -1,    -1,    -1,   127,   106,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,   120,    -1,    -1,   123,    -1,    -1,    -1,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
      -1,    -1,    -1,    -1,   143,   144,   145,   146,     0,    -1,
     149,   150,   151,    -1,   153,    -1,     8,     9,    10,    -1,
      -1,    13,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,
      44,    -1,    -1,    25,    26,    27,    28,    29,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,
      42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    68,    -1,    -1,    -1,
      94,    95,    -1,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    -1,    -1,
      -1,    -1,    94,    95,    96,    97,    -1,    -1,   100,    -1,
      -1,    -1,    -1,   127,   106,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,    -1,    -1,   120,    -1,
      -1,   123,    -1,    -1,    -1,   127,   128,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,    -1,    -1,
      -1,   143,   144,   145,   146,     0,    -1,   149,   150,   151,
      -1,   153,    -1,     8,     9,    10,    -1,    -1,    13,    14,
      15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      25,    -1,    27,    28,    29,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    37,    38,    -1,    40,    41,    42,    43,    44,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      77,    78,    79,    80,    81,    82,    83,    84,    85,    86,
      87,    88,    89,    68,    -1,    -1,    -1,    94,    95,    -1,
      -1,    -1,    77,    78,    79,    80,    81,    82,    83,    84,
      85,    86,    87,    88,    89,    -1,    -1,    -1,    -1,    94,
      95,    96,    97,    -1,    99,   100,    -1,    -1,    -1,    -1,
     127,   106,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,    -1,    -1,    -1,   120,    -1,    -1,   123,    -1,
      -1,   126,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,    -1,    -1,    -1,    -1,   144,
     145,   146,     0,    -1,   149,   150,   151,    -1,   153,    -1,
       8,     9,    10,    -1,    -1,    13,    14,    15,    -1,    17,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,    26,    27,
      28,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,
      38,    -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,
      80,    81,    82,    83,    84,    85,    86,    87,    88,    89,
      68,    -1,    -1,    -1,    94,    95,    -1,    -1,    -1,    77,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    -1,    -1,    -1,    -1,    94,    95,    96,    97,
      -1,    -1,   100,    -1,    -1,    -1,    -1,    -1,   106,   129,
     130,   131,   132,   133,   134,   135,   136,   137,   138,    -1,
      -1,    -1,   120,    -1,    -1,    -1,    -1,    -1,    -1,   127,
      -1,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,    -1,    -1,    -1,    -1,   143,   144,   145,   146,     0,
     148,   149,   150,   151,    -1,   153,    -1,     8,     9,    10,
      -1,    -1,    13,    14,    15,    -1,    17,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    25,    -1,    27,    28,    29,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,
      41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    77,    78,    79,    80,    81,    82,
      83,    -1,    -1,    86,    87,    -1,    -1,    68,    -1,    -1,
      -1,    94,    95,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    -1,
      -1,    -1,    -1,    94,    95,    96,    97,    -1,    -1,   100,
      -1,    -1,    -1,    -1,    -1,   106,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,    -1,    -1,    -1,   120,
      -1,    -1,   123,    -1,    -1,    -1,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,    -1,    -1,   144,   145,   146,     0,    -1,   149,   150,
     151,    -1,   153,    -1,     8,     9,    10,    -1,    -1,    13,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    25,    26,    27,    28,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    68,    -1,    -1,    -1,    94,    95,
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
      27,    28,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      37,    38,    -1,    40,    41,    42,    43,    44,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    68,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      77,    78,    79,    80,    81,    82,    83,    84,    85,    86,
      87,    88,    89,    -1,    -1,    -1,    -1,    94,    95,    96,
      97,    -1,    -1,   100,    -1,    -1,    -1,    -1,    -1,   106,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   120,    -1,    -1,    -1,    -1,    -1,    -1,
     127,    -1,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,    -1,    -1,    -1,    -1,    -1,   144,   145,   146,
       0,   148,   149,   150,   151,    -1,   153,    -1,     8,     9,
      10,    -1,    -1,    -1,    14,    15,    -1,    17,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    25,    26,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,
      40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    77,    78,    79,    80,    81,    82,    83,    84,
      85,    86,    87,    88,    89,    -1,    -1,    -1,    68,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,
      80,    81,    82,    83,    84,    85,    86,    87,    88,    89,
      -1,    -1,    -1,    -1,    94,    95,    96,    97,    -1,    99,
     100,    -1,   127,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,    -1,    -1,    -1,    -1,    -1,
     120,    -1,    -1,    -1,    -1,    -1,   126,   127,   153,   129,
     130,   131,   132,   133,   134,   135,   136,   137,   138,    -1,
      -1,    -1,    -1,   143,   144,   145,   146,     0,    -1,   149,
      -1,   151,    -1,   153,    -1,     8,     9,    10,    -1,    -1,
      -1,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    25,    26,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,
      43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    77,    78,    79,    80,    81,    82,
      83,    84,    85,    86,    87,    88,    89,    -1,    -1,    -1,
      -1,    94,    95,    96,    97,    -1,    99,   100,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   120,    -1,    -1,
      -1,    -1,    -1,   126,   127,    -1,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,    -1,    -1,    -1,    -1,
     143,   144,   145,   146,     0,    -1,   149,    -1,   151,    -1,
     153,    -1,     8,     9,    10,    -1,    -1,    -1,    14,    15,
      -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    37,    38,    -1,    40,    41,    42,    43,    44,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    77,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    -1,    -1,    -1,    -1,    94,    95,
      96,    97,    -1,    99,   100,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   120,    -1,    -1,    -1,    -1,    -1,
     126,   127,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,    -1,    -1,    -1,    -1,   144,   145,
     146,     0,    -1,   149,    -1,   151,    -1,   153,    -1,     8,
       9,    10,    -1,    -1,    -1,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    25,    -1,    -1,    -1,
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
      -1,    -1,    -1,    -1,    -1,   144,   145,   146,    -1,    -1,
     149,    -1,   151,     1,   153,     3,     4,     5,     6,     7,
       8,     9,    10,    11,    12,    -1,    -1,    15,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
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
      -1,    -1,    -1,   151,     1,   153,     3,     4,     5,     6,
       7,    -1,    -1,    10,    11,    12,    -1,    14,    15,    16,
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
      -1,    -1,    -1,    -1,   151,     1,   153,     3,     4,     5,
       6,     7,    -1,    -1,    10,    11,    12,    -1,    -1,    15,
      16,    17,    18,    19,    20,    21,    22,    23,    24,    -1,
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
      -1,    -1,    -1,   139,   140,   141,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   151,     1,   153,     3,     4,
       5,     6,     7,    -1,    -1,    10,    11,    12,    -1,    -1,
      15,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      25,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
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
      -1,    -1,    -1,    -1,   139,   140,   141,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   151,     1,   153,     3,
       4,     5,     6,     7,    -1,    -1,    10,    11,    12,    -1,
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
      -1,    -1,    -1,     1,    -1,     3,     4,     5,     6,     7,
      -1,     9,    10,    11,    12,   139,   140,   141,    16,    -1,
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
     118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     1,
      -1,     3,     4,     5,     6,     7,    -1,    -1,    -1,    11,
      12,   139,   140,   141,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,   151,    -1,   153,    -1,    -1,    30,    31,
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
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   149,    -1,   151,
       1,   153,     3,     4,     5,     6,     7,    -1,    -1,    -1,
      11,    12,    -1,    -1,    -1,    16,    -1,    18,    19,    20,
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
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   139,   140,
     141,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   149,    -1,
     151,     1,   153,     3,     4,     5,     6,     7,    -1,    -1,
      -1,    11,    12,    -1,    -1,    -1,    16,    -1,    18,    19,
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
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   139,
     140,   141,    -1,    -1,   144,    -1,    -1,    -1,    -1,    -1,
      -1,   151,     1,   153,     3,     4,     5,     6,     7,    -1,
      -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    63,    64,    65,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,
      -1,    -1,   101,   102,    -1,   104,   105,    -1,   107,    -1,
      -1,   110,   111,   112,   113,   114,   115,   116,   117,   118,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     139,   140,   141,    -1,    -1,   144,    -1,    -1,    -1,    -1,
      -1,    -1,   151,     1,   153,     3,     4,     5,     6,     7,
      -1,    -1,    10,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,
      98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,   107,
      -1,    -1,   110,   111,   112,   113,   114,   115,   116,   117,
     118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     1,
      -1,     3,     4,     5,     6,     7,    -1,    -1,    -1,    11,
      12,   139,   140,   141,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,   151,    -1,   153,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,
      -1,    -1,    -1,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    -1,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,
      -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,
     102,    -1,   104,   105,    -1,   107,    -1,    -1,   110,   111,
     112,   113,   114,   115,   116,   117,   118,    -1,   120,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,
      -1,     7,    -1,    -1,    -1,    11,    12,   139,   140,   141,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,   151,
      -1,   153,    -1,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,
      46,    -1,    -1,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    58,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,
      -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,
      -1,   107,   108,   109,   110,   111,   112,   113,   114,   115,
     116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,
      -1,    11,    12,   139,   140,   141,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,    -1,    -1,   153,    -1,    -1,
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
       4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,   139,
     140,   141,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,   151,    -1,   153,    -1,    -1,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,
      -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,
     104,   105,    -1,    -1,    -1,    -1,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,   139,   140,   141,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,   153,
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
      -1,     3,     4,     5,     6,     7,    -1,    -1,    -1,    11,
      12,   139,   140,   141,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,   151,    -1,    -1,    -1,    -1,    30,    31,
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
      -1,    -1,    -1,    -1,   147,   148,     3,     4,     5,     6,
       7,     8,     9,    10,    11,    12,    13,    14,    15,    16,
      17,    18,    19,    20,    21,    22,    23,    24,    25,    26,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      77,    78,    79,    80,    81,    82,    83,    -1,    -1,    86,
      87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,    -1,   140,   141,    -1,    -1,    -1,    -1,    -1,
     147,     3,     4,     5,     6,     7,     8,     9,    10,    11,
      12,    13,    14,    15,    16,    17,    18,    19,    20,    21,
      22,    23,    24,    25,    26,    -1,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    -1,    -1,
      -1,    -1,    -1,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    54,    -1,    56,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    75,    76,    77,    78,    79,    80,    81,
      82,    83,    -1,    -1,    86,    87,    -1,    -1,    -1,    -1,
      92,    93,    94,    95,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   107,   108,    -1,    -1,   111,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,   140,   141,
      -1,    -1,    -1,    -1,    -1,   147,     3,     4,     5,     6,
       7,     8,     9,    10,    11,    12,    13,    14,    15,    16,
      17,    18,    19,    20,    21,    22,    23,    24,    25,    26,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    -1,    -1,    56,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      77,    78,    79,    80,    81,    82,    83,    -1,    -1,    86,
      87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     107,   108,    -1,    -1,   111,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,    -1,   140,   141,    -1,    -1,    -1,    -1,    -1,
     147,     3,     4,     5,     6,     7,     8,     9,    10,    11,
      12,    13,    14,    15,    16,    17,    18,    19,    20,    21,
      22,    23,    24,    25,    26,    -1,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    -1,    -1,
      -1,    -1,    -1,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    -1,    -1,    56,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    75,    76,    77,    78,    79,    80,    81,
      82,    83,    -1,    -1,    86,    87,    -1,    -1,    -1,    -1,
      92,    93,    94,    95,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   107,   108,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,   140,   141,
       3,     4,     5,    -1,     7,   147,    -1,    -1,    11,    12,
      -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,    22,
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
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,
       4,     5,    -1,     7,    -1,    -1,   139,    11,    12,    -1,
      -1,    -1,    16,   146,    18,    19,    20,    21,    22,    23,
      24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,
     104,   105,    -1,    -1,    -1,    -1,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,     6,     7,    -1,    -1,   139,    11,    12,    -1,    -1,
      -1,    16,   146,    18,    19,    20,    21,    22,    23,    24,
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
      -1,    -1,   101,   102,    -1,   104,   105,    -1,   107,   108,
     109,   110,   111,   112,   113,   114,   115,   116,   117,   118,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,     6,     7,    -1,    -1,    -1,    11,    12,
     139,   140,   141,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    45,    46,    -1,    48,    49,    50,    51,    52,
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
     105,    -1,   107,   108,   109,   110,   111,   112,   113,   114,
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
      -1,    -1,   101,   102,    -1,   104,   105,    -1,   107,   108,
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
      -1,   104,   105,    -1,    -1,   108,   109,   110,   111,   112,
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
     107,   108,    -1,   110,   111,   112,   113,   114,   115,   116,
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
     101,   102,    -1,   104,   105,    -1,    -1,   108,    -1,   110,
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
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,
      -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,
      -1,   104,   105,    -1,    -1,    -1,    -1,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,   139,   140,   141,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,
     107,    -1,    -1,   110,   111,   112,   113,   114,   115,   116,
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
     105,    -1,   107,    -1,    -1,   110,   111,   112,   113,   114,
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
      -1,    94,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,
      -1,   104,   105,    -1,    -1,    -1,    -1,   110,   111,   112,
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
     101,   102,    -1,   104,   105,    -1,    -1,    -1,    -1,   110,
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
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    98,
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
      65,    33,    34,    35,    36,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    49,    50,    51,
      52,    -1,    -1,    -1,    56,    -1,    -1,    59,    60,    61,
      62,    63,    -1,    98,    -1,    -1,   101,   102,    -1,   104,
     105,    -1,    -1,    -1,    -1,   110,   111,   112,   113,   114,
     115,   116,   117,   118,    -1,    -1,    -1,    -1,    90,    91,
      -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,
      -1,    -1,   104,   105,   139,   107,    -1,    -1,   110,   111,
     112,   113,   114,   115,   116,   117,   118,    33,    34,    35,
      36,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     132,    -1,    -1,    49,    50,    51,    52,   139,    -1,    -1,
      56,    -1,    -1,    59,    60,    61,    62,    63,    -1,    -1,
      33,    34,    35,    36,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    49,    50,    51,    52,
      -1,    -1,    -1,    56,    90,    91,    59,    60,    61,    62,
      63,    -1,    98,    -1,    -1,   101,    -1,    -1,   104,   105,
      -1,   107,    -1,    -1,   110,   111,   112,   113,   114,   115,
     116,   117,   118,    -1,    -1,    -1,    -1,    90,    91,    -1,
      -1,    -1,    -1,    -1,    -1,    98,   132,    -1,   101,    -1,
      -1,   104,   105,   139,    -1,    -1,    -1,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    52,    53,    -1,    -1,
      56,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   132,
      -1,    -1,    -1,    -1,    -1,    -1,   139,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    52,    53,    -1,    -1,
      56,   147,   148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,   140,   141,    -1,    -1,    -1,    -1,
      -1,   147
};

/* YYSTOS[STATE-NUM] -- The symbol kind of the accessing symbol of
   state STATE-NUM.  */
static const yytype_int16 yystos[] =
{
       0,   155,   156,     0,     1,     3,     4,     5,     6,     7,
      11,    12,    16,    18,    19,    20,    21,    22,    23,    24,
      30,    31,    32,    33,    34,    35,    36,    39,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    59,    60,    61,    62,    63,    64,    65,    75,    76,
      90,    91,    98,   101,   102,   104,   105,   107,   110,   111,
     112,   113,   114,   115,   116,   117,   118,   139,   140,   141,
     157,   158,   159,   167,   169,   171,   177,   178,   184,   185,
     187,   188,   189,   191,   192,   193,   195,   196,   205,   208,
     224,   234,   235,   236,   237,   238,   239,   240,   241,   242,
     243,   244,   253,   275,   283,   284,   336,   337,   338,   339,
     340,   341,   342,   345,   347,   348,   362,   363,   365,   366,
     367,   369,   370,   371,   372,   373,   411,   425,   159,     3,
       4,     5,     6,     7,     8,     9,    10,    11,    12,    13,
      14,    15,    16,    17,    18,    19,    20,    21,    22,    23,
      24,    25,    26,    30,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    56,    75,    76,    77,    78,    79,    80,    81,
      82,    83,    86,    87,    92,    93,    94,    95,   107,   108,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
     140,   141,   147,   199,   200,   201,   203,   204,   362,    39,
      58,    98,   101,   107,   108,   109,   112,   140,   177,   178,
     188,   196,   205,   210,   216,   219,   221,   234,   369,   370,
     372,   373,   409,   410,   216,   148,   217,   218,   148,   213,
     217,   148,   153,   418,    54,   200,   418,   143,   160,   143,
      21,    22,    31,    32,   187,   205,   234,   253,   205,   205,
     205,    56,     1,    47,   101,   163,   164,   165,   167,   190,
     191,   425,   167,   226,   211,   221,   409,   425,   210,   408,
     409,   425,    46,    98,   139,   146,   177,   178,   195,   224,
     234,   369,   370,   373,   276,    54,    55,    57,   199,   351,
     364,   351,   352,   353,   152,   152,   152,   152,   367,   184,
     205,   205,   151,   153,   417,   423,   424,    40,    41,    42,
      43,    44,    37,    38,   148,   376,   377,   378,   379,   425,
     376,   378,    26,   143,   213,   217,   245,   285,    28,   246,
     282,   126,   146,   101,   107,   192,   126,    25,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    94,    95,   100,   127,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,   207,   207,    68,    96,    97,
     145,   415,   225,   171,   180,   180,   181,   182,   181,   180,
     417,   424,    98,   189,   196,   234,   258,   369,   370,   373,
      52,    56,    94,    98,   197,   198,   234,   369,   370,   373,
     198,    33,    34,    35,    36,    49,    50,    51,    52,    56,
     148,   176,   199,   371,   406,   216,    97,   415,   416,   285,
     339,    99,    99,   146,   210,    56,   210,   210,   210,   351,
     376,   376,   126,   100,   146,   220,   425,    97,   145,   415,
      99,    99,   146,   220,   216,   418,   419,   216,    91,   215,
     216,   221,   383,   409,   425,   171,   419,   171,    54,    64,
      65,   168,   148,   206,   157,   163,    97,   415,    99,   167,
     166,   190,   149,   417,   424,   419,   227,   419,   150,   146,
     153,   422,   146,   422,   144,   422,   418,    56,   367,   192,
     194,   377,   146,    97,   145,   415,   277,    66,   119,   121,
     122,   354,   119,   119,   354,    67,   354,   343,   349,   346,
     350,    77,   151,   159,   180,   180,   180,   180,   167,   171,
     171,    52,    54,    55,    56,    57,    58,    77,    91,   101,
     107,   108,   109,   133,   136,   263,   380,   382,   383,   384,
     385,   386,   387,   388,   389,   390,   393,   394,   395,   396,
     397,   400,   401,   402,   403,   404,   126,   161,   163,   382,
     126,   161,   286,   287,   106,   186,   290,   291,   290,    70,
     209,   425,   190,   146,   195,   146,   209,   174,   205,   205,
     205,   205,   205,   205,   205,   205,   205,   205,   205,   205,
     205,   172,   205,   205,   205,   205,   205,   205,   205,   205,
     205,   205,   205,    52,    53,    56,   203,   213,   412,   413,
     215,   221,    52,    53,    56,   203,   213,   412,   161,    13,
     254,   423,   254,   163,   180,   163,   417,   230,    56,    97,
     145,   415,    25,   171,    52,    56,   197,   130,   374,    97,
     145,   415,   233,   407,    68,    97,   414,    52,    56,   412,
     209,   209,   202,   124,   126,   126,   209,   210,   107,   210,
     219,   409,    52,    56,   215,    52,    56,   209,   209,   410,
     419,   149,   419,   146,   419,   146,   419,   200,   228,   205,
     144,   144,   412,   412,   209,   160,   419,   165,   419,   409,
     146,   194,    52,    56,   215,    52,    56,   278,   356,   355,
     119,   344,   354,    66,   119,   119,   344,    66,   119,   205,
     101,   107,   259,   260,   261,   262,   385,   146,   405,   425,
     419,   264,   265,   146,   381,   210,   146,   405,    34,    52,
     146,   381,    52,   146,   381,    52,   188,   205,    10,   252,
       8,   247,   332,   425,   423,   188,   205,   252,   144,   288,
     286,   252,   292,   252,   107,   184,   210,   221,   222,   223,
     419,   194,   146,   169,   170,   184,   196,   205,   210,   212,
     223,   234,   373,   175,   173,   418,    99,    99,   213,   217,
     418,   420,   146,    99,    99,   213,   214,   217,   425,   252,
     163,    13,   163,   252,    27,   255,   423,   252,    25,   229,
     297,    17,   249,   295,    52,    56,   215,    52,    56,   181,
     232,   375,   231,    52,    56,   197,   215,   161,   171,   179,
     214,   217,   170,   205,   212,   170,   212,   200,   210,   210,
     220,    99,    99,   420,    99,    99,   383,   409,   171,   212,
     422,   192,   420,   148,   280,   382,   357,    54,    55,    57,
     361,   373,   152,   354,   152,   152,   152,   261,   385,   146,
     419,   146,   404,   210,   126,   380,   387,   400,   402,   390,
     394,   396,   388,   397,   402,   386,   388,    44,    44,   210,
     223,   333,   425,     9,    15,   248,   250,   335,   425,    44,
      44,   289,   144,   293,   210,   146,    44,   194,    44,   126,
      44,    97,   145,   415,    52,    56,    58,    90,    91,    98,
     101,   104,   105,   107,   112,   132,   275,   303,   304,   305,
     306,   309,   314,   315,   316,   319,   320,   321,   322,   323,
     324,   325,   326,   327,   328,   329,   330,   331,   336,   337,
     340,   341,   342,   345,   347,   348,   370,   394,   303,   128,
     209,   209,   186,   150,    99,   209,   209,   186,    14,   250,
     251,   256,   257,   425,   257,   183,   298,   295,   252,   107,
     210,   294,   252,   420,   163,   423,   180,   161,   420,   252,
     419,   176,   285,   282,   209,   209,    99,   209,   209,   419,
     146,   419,   382,   279,   358,   419,   259,   262,   260,   146,
     381,   146,   381,   405,   146,   381,   146,   381,   381,   205,
     205,   100,   334,   425,   163,   162,   205,   205,   131,   270,
     271,   425,   270,   107,   210,   167,   167,   209,   205,    52,
      56,   215,    52,    56,   327,   327,    56,   197,   311,   304,
     312,   313,   314,   315,   318,   420,   310,   418,   421,    52,
     351,    52,    54,    55,    57,   101,   368,   100,   146,   131,
     146,   146,   304,    88,    89,    97,   145,   148,   307,   308,
      34,    52,   205,   170,   212,   170,   212,   209,   170,   212,
     170,   212,   163,   180,   252,   252,   299,   252,   210,   146,
     254,   252,   161,   423,   252,   209,   272,   418,    29,   123,
     281,   359,   146,   146,   388,   402,   388,   388,    98,   196,
     234,   369,   370,   373,   254,   163,   263,   266,   269,   272,
     386,   388,   389,   391,   392,   398,   399,   402,   404,   163,
     161,   210,   420,   304,   420,   107,   304,   318,   420,   146,
     112,   319,   144,   124,   180,   328,   312,   316,   309,   317,
     318,   321,   325,   327,   327,   197,   420,   419,   312,   315,
     319,   312,   315,   319,   170,   212,   254,   302,   303,   107,
     210,   163,   252,   149,   151,   161,   163,   360,   260,   381,
     146,   381,   381,   381,    56,    97,   145,   415,   163,   335,
     405,   272,   131,   264,   146,   267,   268,    98,   234,   146,
     405,   146,   267,   146,   267,   419,    52,   146,   146,   351,
     421,   149,   146,   146,   419,   419,   419,   420,   420,   420,
     163,   254,    40,    41,   210,   257,   295,   296,    52,   273,
     274,   384,   252,   144,   163,   388,    52,    56,   215,    52,
      56,   332,   131,   234,   266,   399,   402,    56,    97,   391,
     396,   388,   398,   402,   388,   146,   317,   317,   316,   318,
     256,   300,   180,   180,   146,   418,   120,   381,   420,   146,
     267,   146,   267,    52,    56,   405,   146,   267,   146,   267,
     267,   317,   146,   163,   274,   388,   402,   388,   388,   257,
     297,   301,   267,   146,   267,   267,   267,   388,   267
};

/* YYR1[RULE-NUM] -- Symbol kind of the left-hand side of rule RULE-NUM.  */
static const yytype_int16 yyr1[] =
{
       0,   154,   156,   155,   157,   158,   158,   158,   158,   159,
     159,   160,   162,   161,   161,   163,   164,   164,   164,   164,
     165,   166,   165,   168,   167,   167,   167,   167,   167,   167,
     167,   167,   167,   167,   167,   167,   167,   167,   167,   167,
     167,   169,   169,   169,   169,   169,   169,   169,   169,   169,
     169,   169,   169,   170,   170,   170,   171,   171,   171,   171,
     171,   172,   173,   171,   174,   175,   171,   171,   176,   177,
     179,   178,   180,   182,   183,   181,   184,   184,   185,   185,
     186,   187,   188,   188,   188,   188,   188,   188,   188,   188,
     188,   188,   188,   189,   189,   190,   190,   191,   191,   191,
     191,   191,   191,   191,   191,   191,   191,   192,   192,   193,
     193,   194,   194,   195,   195,   195,   195,   195,   195,   195,
     195,   195,   196,   196,   196,   196,   196,   196,   196,   196,
     196,   197,   197,   198,   198,   198,   199,   199,   199,   199,
     199,   200,   200,   201,   202,   201,   203,   203,   203,   203,
     203,   203,   203,   203,   203,   203,   203,   203,   203,   203,
     203,   203,   203,   203,   203,   203,   203,   203,   203,   203,
     203,   203,   203,   203,   203,   203,   204,   204,   204,   204,
     204,   204,   204,   204,   204,   204,   204,   204,   204,   204,
     204,   204,   204,   204,   204,   204,   204,   204,   204,   204,
     204,   204,   204,   204,   204,   204,   204,   204,   204,   204,
     204,   204,   204,   204,   204,   204,   204,   205,   205,   205,
     205,   205,   205,   205,   205,   205,   205,   205,   205,   205,
     205,   205,   205,   205,   205,   205,   205,   205,   205,   205,
     205,   205,   205,   205,   205,   205,   205,   205,   205,   205,
     205,   205,   205,   205,   205,   205,   205,   206,   205,   205,
     205,   205,   205,   205,   205,   207,   207,   207,   207,   208,
     208,   209,   209,   210,   211,   211,   211,   211,   212,   212,
     213,   213,   213,   214,   214,   215,   215,   215,   215,   215,
     216,   216,   216,   216,   216,   218,   217,   219,   219,   220,
     220,   221,   221,   221,   221,   222,   222,   223,   223,   223,
     224,   224,   224,   224,   224,   224,   224,   224,   224,   224,
     224,   225,   224,   226,   224,   227,   224,   224,   224,   224,
     224,   224,   224,   224,   224,   224,   228,   224,   224,   224,
     224,   224,   224,   224,   224,   224,   224,   224,   229,   224,
     230,   224,   224,   224,   231,   224,   232,   224,   233,   224,
     224,   224,   224,   224,   224,   224,   234,   235,   236,   237,
     238,   239,   240,   241,   242,   243,   244,   245,   246,   247,
     248,   249,   250,   251,   252,   253,   254,   254,   254,   255,
     255,   256,   256,   257,   257,   258,   258,   259,   259,   260,
     260,   261,   261,   261,   261,   261,   262,   262,   263,   263,
     265,   264,   266,   266,   266,   266,   267,   267,   268,   269,
     269,   269,   269,   269,   269,   269,   269,   269,   269,   269,
     269,   269,   269,   269,   270,   270,   271,   271,   272,   272,
     273,   273,   274,   274,   276,   277,   278,   279,   275,   280,
     280,   281,   281,   282,   283,   283,   283,   283,   284,   284,
     284,   284,   284,   284,   284,   284,   284,   285,   285,   287,
     288,   289,   286,   291,   292,   293,   290,   294,   294,   294,
     294,   295,   296,   296,   298,   299,   300,   297,   301,   301,
     302,   302,   302,   303,   303,   303,   303,   303,   303,   304,
     305,   305,   306,   306,   307,   308,   309,   309,   309,   309,
     309,   309,   309,   309,   309,   309,   309,   309,   309,   310,
     309,   309,   311,   309,   312,   312,   312,   312,   312,   312,
     312,   312,   313,   313,   314,   314,   315,   316,   316,   317,
     317,   318,   319,   319,   319,   319,   320,   320,   321,   321,
     322,   322,   323,   323,   324,   325,   325,   326,   326,   326,
     326,   326,   326,   326,   326,   326,   326,   327,   327,   327,
     327,   327,   327,   327,   327,   327,   327,   328,   329,   329,
     330,   331,   331,   331,   332,   332,   333,   333,   333,   334,
     334,   335,   335,   336,   336,   337,   338,   338,   338,   339,
     340,   341,   342,   343,   343,   344,   344,   345,   346,   346,
     347,   348,   349,   349,   350,   350,   351,   351,   352,   352,
     353,   353,   354,   355,   354,   356,   357,   358,   359,   360,
     354,   361,   361,   361,   361,   362,   362,   363,   364,   364,
     364,   364,   365,   366,   366,   367,   367,   367,   367,   368,
     368,   368,   369,   369,   369,   369,   369,   370,   370,   370,
     370,   370,   370,   370,   371,   371,   372,   372,   373,   373,
     375,   374,   374,   376,   376,   377,   378,   379,   378,   380,
     380,   380,   380,   380,   381,   381,   382,   382,   382,   382,
     382,   382,   382,   382,   382,   382,   382,   382,   382,   382,
     382,   383,   384,   384,   384,   384,   385,   385,   386,   387,
     387,   388,   388,   389,   390,   390,   391,   391,   392,   392,
     393,   393,   394,   394,   395,   396,   396,   397,   398,   399,
     399,   400,   400,   401,   401,   402,   402,   403,   403,   404,
     404,   405,   405,   406,   407,   406,   408,   408,   409,   409,
     410,   410,   410,   410,   410,   411,   411,   411,   412,   412,
     412,   412,   413,   413,   413,   414,   414,   415,   415,   416,
     416,   417,   417,   418,   418,   419,   420,   421,   422,   422,
     422,   423,   423,   424,   424,   425
};

/* YYR2[RULE-NUM] -- Number of symbols on the right-hand side of rule RULE-NUM.  */
static const yytype_int8 yyr2[] =
{
       0,     2,     0,     2,     2,     1,     1,     3,     2,     1,
       2,     3,     0,     6,     3,     2,     1,     1,     3,     2,
       1,     0,     3,     0,     4,     3,     3,     3,     2,     3,
       3,     3,     3,     3,     4,     1,     4,     4,     6,     4,
       1,     4,     4,     7,     6,     6,     6,     6,     4,     6,
       4,     6,     4,     1,     3,     1,     1,     3,     3,     3,
       2,     0,     0,     5,     0,     0,     5,     1,     1,     2,
       0,     5,     1,     0,     0,     4,     1,     1,     1,     4,
       3,     1,     2,     3,     4,     5,     4,     5,     2,     2,
       2,     2,     2,     1,     3,     1,     3,     1,     2,     3,
       5,     2,     4,     2,     4,     1,     3,     1,     3,     2,
       3,     1,     3,     1,     1,     4,     3,     3,     3,     3,
       2,     1,     1,     1,     4,     3,     3,     3,     3,     2,
       1,     1,     1,     2,     1,     3,     1,     1,     1,     1,
       1,     1,     1,     1,     0,     4,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     4,     4,     7,
       6,     6,     6,     6,     5,     4,     3,     3,     2,     2,
       2,     2,     3,     3,     3,     3,     3,     3,     4,     2,
       2,     3,     3,     3,     3,     1,     3,     3,     3,     3,
       3,     2,     2,     3,     3,     3,     3,     0,     4,     6,
       4,     6,     4,     6,     1,     1,     1,     1,     1,     3,
       3,     1,     1,     1,     1,     2,     4,     2,     1,     3,
       3,     5,     3,     1,     1,     1,     1,     2,     4,     2,
       1,     2,     2,     4,     1,     0,     2,     2,     1,     2,
       1,     1,     2,     3,     4,     1,     1,     3,     4,     2,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     0,     4,     0,     3,     0,     4,     3,     3,     2,
       3,     3,     1,     4,     3,     1,     0,     6,     4,     3,
       2,     1,     2,     1,     6,     6,     4,     4,     0,     6,
       0,     5,     5,     6,     0,     6,     0,     7,     0,     5,
       4,     4,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     2,     1,
       1,     1,     5,     1,     2,     1,     1,     1,     3,     1,
       3,     1,     3,     5,     1,     3,     2,     1,     1,     1,
       0,     2,     4,     2,     2,     1,     2,     0,     1,     6,
       8,     4,     6,     4,     2,     6,     2,     4,     6,     2,
       4,     2,     4,     1,     1,     1,     3,     4,     1,     4,
       1,     3,     1,     1,     0,     0,     0,     0,     7,     4,
       1,     3,     3,     3,     2,     4,     5,     5,     2,     4,
       4,     3,     3,     3,     2,     1,     4,     3,     3,     0,
       0,     0,     5,     0,     0,     0,     5,     1,     2,     3,
       4,     5,     1,     1,     0,     0,     0,     8,     1,     1,
       1,     3,     3,     1,     2,     3,     1,     1,     1,     1,
       3,     1,     3,     1,     1,     1,     1,     1,     4,     4,
       4,     3,     4,     4,     4,     3,     3,     3,     2,     0,
       4,     2,     0,     4,     1,     1,     2,     3,     5,     2,
       4,     1,     2,     3,     1,     3,     5,     2,     1,     1,
       3,     1,     3,     1,     2,     1,     1,     3,     2,     1,
       1,     3,     2,     1,     2,     1,     1,     1,     3,     3,
       2,     2,     1,     1,     1,     2,     2,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     2,     2,
       4,     2,     3,     1,     6,     1,     1,     1,     1,     2,
       1,     2,     1,     1,     1,     1,     1,     1,     2,     3,
       3,     3,     4,     0,     3,     1,     2,     4,     0,     3,
       4,     4,     0,     3,     0,     3,     0,     2,     0,     2,
       0,     2,     1,     0,     3,     0,     0,     0,     0,     0,
       8,     1,     1,     1,     1,     1,     1,     2,     1,     1,
       1,     1,     3,     1,     2,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       0,     4,     0,     1,     1,     3,     1,     0,     3,     4,
       2,     2,     1,     1,     2,     0,     6,     8,     4,     6,
       4,     6,     2,     4,     6,     2,     4,     2,     4,     1,
       0,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       3,     1,     3,     1,     2,     1,     2,     1,     1,     3,
       1,     3,     1,     1,     2,     2,     1,     3,     3,     1,
       3,     1,     3,     1,     1,     2,     1,     1,     1,     2,
       1,     2,     1,     1,     0,     4,     1,     2,     1,     3,
       3,     2,     1,     4,     2,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     0,     1,     0,     1,     2,     2,     2,     0,     1,
       1,     1,     1,     1,     2,     0
};


enum { YYENOMEM = -2 };

#define yyerrok         (yyerrstatus = 0)
#define yyclearin       (yychar = YYEMPTY)

#define YYACCEPT        goto yyacceptlab
#define YYABORT         goto yyabortlab
#define YYERROR         goto yyerrorlab
#define YYNOMEM         goto yyexhaustedlab


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

/* Backward compatibility with an undocumented macro.
   Use YYerror or YYUNDEF. */
#define YYERRCODE YYUNDEF

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


/* YYLOCATION_PRINT -- Print the location on the stream.
   This macro was not mandated originally: define only if we know
   we won't break user code: when these are the locations we know.  */

# ifndef YYLOCATION_PRINT

#  if defined YY_LOCATION_PRINT

   /* Temporary convenience wrapper in case some people defined the
      undocumented and private YY_LOCATION_PRINT macros.  */
#   define YYLOCATION_PRINT(File, Loc)  YY_LOCATION_PRINT(File, *(Loc))

#  elif defined YYLTYPE_IS_TRIVIAL && YYLTYPE_IS_TRIVIAL

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

#   define YYLOCATION_PRINT  yy_location_print_

    /* Temporary convenience wrapper in case some people defined the
       undocumented and private YY_LOCATION_PRINT macros.  */
#   define YY_LOCATION_PRINT(File, Loc)  YYLOCATION_PRINT(File, &(Loc))

#  else

#   define YYLOCATION_PRINT(File, Loc) ((void) 0)
    /* Temporary convenience wrapper in case some people defined the
       undocumented and private YY_LOCATION_PRINT macros.  */
#   define YY_LOCATION_PRINT  YYLOCATION_PRINT

#  endif
# endif /* !defined YYLOCATION_PRINT */


# define YY_SYMBOL_PRINT(Title, Kind, Value, Location)                    \
do {                                                                      \
  if (yydebug)                                                            \
    {                                                                     \
      YYFPRINTF (p, "%s ", Title);                                   \
      yy_symbol_print (stderr,                                            \
                  Kind, Value, Location, p); \
      YYFPRINTF (p, "\n");                                           \
    }                                                                     \
} while (0)


/*-----------------------------------.
| Print this symbol's value on YYO.  |
`-----------------------------------*/

static void
yy_symbol_value_print (FILE *yyo,
                       yysymbol_kind_t yykind, YYSTYPE const * const yyvaluep, YYLTYPE const * const yylocationp, struct parser_params *p)
{
  FILE *yyoutput = yyo;
  YY_USE (yyoutput);
  YY_USE (yylocationp);
  YY_USE (p);
  if (!yyvaluep)
    return;
  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  switch (yykind)
    {
    case YYSYMBOL_tIDENTIFIER: /* "local variable or method"  */
#line 1090 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6244 "parse.c"
        break;

    case YYSYMBOL_tFID: /* "method"  */
#line 1090 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6256 "parse.c"
        break;

    case YYSYMBOL_tGVAR: /* "global variable"  */
#line 1090 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6268 "parse.c"
        break;

    case YYSYMBOL_tIVAR: /* "instance variable"  */
#line 1090 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6280 "parse.c"
        break;

    case YYSYMBOL_tCONSTANT: /* "constant"  */
#line 1090 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6292 "parse.c"
        break;

    case YYSYMBOL_tCVAR: /* "class variable"  */
#line 1090 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6304 "parse.c"
        break;

    case YYSYMBOL_tLABEL: /* "label"  */
#line 1090 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6316 "parse.c"
        break;

    case YYSYMBOL_tINTEGER: /* "integer literal"  */
#line 1097 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6328 "parse.c"
        break;

    case YYSYMBOL_tFLOAT: /* "float literal"  */
#line 1097 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6340 "parse.c"
        break;

    case YYSYMBOL_tRATIONAL: /* "rational literal"  */
#line 1097 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6352 "parse.c"
        break;

    case YYSYMBOL_tIMAGINARY: /* "imaginary literal"  */
#line 1097 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6364 "parse.c"
        break;

    case YYSYMBOL_tCHAR: /* "char literal"  */
#line 1097 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6376 "parse.c"
        break;

    case YYSYMBOL_tNTH_REF: /* "numbered reference"  */
#line 1104 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "$%ld", ((*yyvaluep).node)->nd_nth);
#else
    rb_parser_printf(p, "%"PRIsVALUE, ((*yyvaluep).node));
#endif
}
#line 6388 "parse.c"
        break;

    case YYSYMBOL_tBACK_REF: /* "back reference"  */
#line 1111 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "$%c", (int)((*yyvaluep).node)->nd_nth);
#else
    rb_parser_printf(p, "%"PRIsVALUE, ((*yyvaluep).node));
#endif
}
#line 6400 "parse.c"
        break;

    case YYSYMBOL_tSTRING_CONTENT: /* "literal content"  */
#line 1097 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6412 "parse.c"
        break;

    case YYSYMBOL_tOP_ASGN: /* "operator-assignment"  */
#line 1090 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6424 "parse.c"
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
yy_symbol_print (FILE *yyo,
                 yysymbol_kind_t yykind, YYSTYPE const * const yyvaluep, YYLTYPE const * const yylocationp, struct parser_params *p)
{
  YYFPRINTF (p, "%s %s (",
             yykind < YYNTOKENS ? "token" : "nterm", yysymbol_name (yykind));

  YYLOCATION_PRINT (yyo, yylocationp);
  YYFPRINTF (p, ": ");
  yy_symbol_value_print (yyo, yykind, yyvaluep, yylocationp, p);
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
yy_reduce_print (yy_state_t *yyssp, YYSTYPE *yyvsp, YYLTYPE *yylsp,
                 int yyrule, struct parser_params *p)
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
                       YY_ACCESSING_SYMBOL (+yyssp[yyi + 1 - yynrhs]),
                       &yyvsp[(yyi + 1) - (yynrhs)],
                       &(yylsp[(yyi + 1) - (yynrhs)]), p);
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
# define YYDPRINTF(Args) ((void) 0)
# define YY_SYMBOL_PRINT(Title, Kind, Value, Location)
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


/* Context of a parse error.  */
typedef struct
{
  yy_state_t *yyssp;
  yysymbol_kind_t yytoken;
  YYLTYPE *yylloc;
} yypcontext_t;

/* Put in YYARG at most YYARGN of the expected tokens given the
   current YYCTX, and return the number of tokens stored in YYARG.  If
   YYARG is null, return the number of expected tokens (guaranteed to
   be less than YYNTOKENS).  Return YYENOMEM on memory exhaustion.
   Return 0 if there are more than YYARGN expected tokens, yet fill
   YYARG up to YYARGN. */
static int
yypcontext_expected_tokens (const yypcontext_t *yyctx,
                            yysymbol_kind_t yyarg[], int yyargn)
{
  /* Actual size of YYARG. */
  int yycount = 0;
  int yyn = yypact[+*yyctx->yyssp];
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
        if (yycheck[yyx + yyn] == yyx && yyx != YYSYMBOL_YYerror
            && !yytable_value_is_error (yytable[yyx + yyn]))
          {
            if (!yyarg)
              ++yycount;
            else if (yycount == yyargn)
              return 0;
            else
              yyarg[yycount++] = YY_CAST (yysymbol_kind_t, yyx);
          }
    }
  if (yyarg && yycount == 0 && 0 < yyargn)
    yyarg[0] = YYSYMBOL_YYEMPTY;
  return yycount;
}




#ifndef yystrlen
# if defined __GLIBC__ && defined _STRING_H
#  define yystrlen(S) (YY_CAST (YYPTRDIFF_T, strlen (S)))
# else
/* Return the length of YYSTR.  */
static YYPTRDIFF_T
yystrlen (const char *yystr)
{
  YYPTRDIFF_T yylen;
  for (yylen = 0; yystr[yylen]; yylen++)
    continue;
  return yylen;
}
# endif
#endif

#ifndef yystpcpy
# if defined __GLIBC__ && defined _STRING_H && defined _GNU_SOURCE
#  define yystpcpy stpcpy
# else
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
# endif
#endif

#ifndef yytnamerr
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
#endif


static int
yy_syntax_error_arguments (const yypcontext_t *yyctx,
                           yysymbol_kind_t yyarg[], int yyargn)
{
  /* Actual size of YYARG. */
  int yycount = 0;
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
  if (yyctx->yytoken != YYSYMBOL_YYEMPTY)
    {
      int yyn;
      if (yyarg)
        yyarg[yycount] = yyctx->yytoken;
      ++yycount;
      yyn = yypcontext_expected_tokens (yyctx,
                                        yyarg ? yyarg + 1 : yyarg, yyargn - 1);
      if (yyn == YYENOMEM)
        return YYENOMEM;
      else
        yycount += yyn;
    }
  return yycount;
}

/* Copy into *YYMSG, which is of size *YYMSG_ALLOC, an error message
   about the unexpected token YYTOKEN for the state stack whose top is
   YYSSP.

   Return 0 if *YYMSG was successfully written.  Return -1 if *YYMSG is
   not large enough to hold the message.  In that case, also set
   *YYMSG_ALLOC to the required number of bytes.  Return YYENOMEM if the
   required number of bytes is too large to store.  */
static int
yysyntax_error (struct parser_params *p, YYPTRDIFF_T *yymsg_alloc, char **yymsg,
                const yypcontext_t *yyctx)
{
  enum { YYARGS_MAX = 5 };
  /* Internationalized format string. */
  const char *yyformat = YY_NULLPTR;
  /* Arguments of yyformat: reported tokens (one for the "unexpected",
     one per "expected"). */
  yysymbol_kind_t yyarg[YYARGS_MAX];
  /* Cumulated lengths of YYARG.  */
  YYPTRDIFF_T yysize = 0;

  /* Actual size of YYARG. */
  int yycount = yy_syntax_error_arguments (yyctx, yyarg, YYARGS_MAX);
  if (yycount == YYENOMEM)
    return YYENOMEM;

  switch (yycount)
    {
#define YYCASE_(N, S)                       \
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
#undef YYCASE_
    }

  /* Compute error message size.  Don't count the "%s"s, but reserve
     room for the terminator.  */
  yysize = yystrlen (yyformat) - 2 * yycount + 1;
  {
    int yyi;
    for (yyi = 0; yyi < yycount; ++yyi)
      {
        YYPTRDIFF_T yysize1
          = yysize + yytnamerr (YY_NULLPTR, yytname[yyarg[yyi]]);
        if (yysize <= yysize1 && yysize1 <= YYSTACK_ALLOC_MAXIMUM)
          yysize = yysize1;
        else
          return YYENOMEM;
      }
  }

  if (*yymsg_alloc < yysize)
    {
      *yymsg_alloc = 2 * yysize;
      if (! (yysize <= *yymsg_alloc
             && *yymsg_alloc <= YYSTACK_ALLOC_MAXIMUM))
        *yymsg_alloc = YYSTACK_ALLOC_MAXIMUM;
      return -1;
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
          yyp += yytnamerr (yyp, yytname[yyarg[yyi++]]);
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


/*-----------------------------------------------.
| Release the memory associated to this symbol.  |
`-----------------------------------------------*/

static void
yydestruct (const char *yymsg,
            yysymbol_kind_t yykind, YYSTYPE *yyvaluep, YYLTYPE *yylocationp, struct parser_params *p)
{
  YY_USE (yyvaluep);
  YY_USE (yylocationp);
  YY_USE (p);
  if (!yymsg)
    yymsg = "Deleting";
  YY_SYMBOL_PRINT (yymsg, yykind, yyvaluep, yylocationp);

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  YY_USE (yykind);
  YY_IGNORE_MAYBE_UNINITIALIZED_END
}






/*----------.
| yyparse.  |
`----------*/

int
yyparse (struct parser_params *p)
{
/* Lookahead token kind.  */
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
    int yynerrs = 0;

    yy_state_fast_t yystate = 0;
    /* Number of tokens to shift before error messages enabled.  */
    int yyerrstatus = 0;

    /* Refer to the stacks through separate pointers, to allow yyoverflow
       to reallocate them elsewhere.  */

    /* Their size.  */
    YYPTRDIFF_T yystacksize = YYINITDEPTH;

    /* The state stack: array, bottom, top.  */
    yy_state_t yyssa[YYINITDEPTH];
    yy_state_t *yyss = yyssa;
    yy_state_t *yyssp = yyss;

    /* The semantic value stack: array, bottom, top.  */
    YYSTYPE yyvsa[YYINITDEPTH];
    YYSTYPE *yyvs = yyvsa;
    YYSTYPE *yyvsp = yyvs;

    /* The location stack: array, bottom, top.  */
    YYLTYPE yylsa[YYINITDEPTH];
    YYLTYPE *yyls = yylsa;
    YYLTYPE *yylsp = yyls;

  int yyn;
  /* The return value of yyparse.  */
  int yyresult;
  /* Lookahead symbol kind.  */
  yysymbol_kind_t yytoken = YYSYMBOL_YYEMPTY;
  /* The variables used to return semantic value and location from the
     action routines.  */
  YYSTYPE yyval;
  YYLTYPE yyloc;

  /* The locations where the error started and ended.  */
  YYLTYPE yyerror_range[3];

  /* Buffer for error messages, and its allocated size.  */
  char yymsgbuf[128];
  char *yymsg = yymsgbuf;
  YYPTRDIFF_T yymsg_alloc = sizeof yymsgbuf;

#define YYPOPSTACK(N)   (yyvsp -= (N), yyssp -= (N), yylsp -= (N))

  /* The number of symbols on the RHS of the reduced rule.
     Keep to zero when no symbol should be popped.  */
  int yylen = 0;

  YYDPRINTF ((p, "Starting parse\n"));

  yychar = YYEMPTY; /* Cause a token to be read.  */


/* User initialization code.  */
#line 1122 "parse.y"
{
    RUBY_SET_YYLLOC_OF_NONE(yylloc);
}

#line 6919 "parse.c"

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
  YY_STACK_PRINT (yyss, yyssp);

  if (yyss + yystacksize - 1 <= yyssp)
#if !defined yyoverflow && !defined YYSTACK_RELOCATE
    YYNOMEM;
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
        YYNOMEM;
      yystacksize *= 2;
      if (YYMAXDEPTH < yystacksize)
        yystacksize = YYMAXDEPTH;

      {
        yy_state_t *yyss1 = yyss;
        union yyalloc *yyptr =
          YY_CAST (union yyalloc *,
                   YYSTACK_ALLOC (YY_CAST (YYSIZE_T, YYSTACK_BYTES (yystacksize))));
        if (! yyptr)
          YYNOMEM;
        YYSTACK_RELOCATE (yyss_alloc, yyss);
        YYSTACK_RELOCATE (yyvs_alloc, yyvs);
        YYSTACK_RELOCATE (yyls_alloc, yyls);
#  undef YYSTACK_RELOCATE
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

  /* YYCHAR is either empty, or end-of-input, or a valid lookahead.  */
  if (yychar == YYEMPTY)
    {
      YYDPRINTF ((p, "Reading a token\n"));
      yychar = yylex (&yylval, &yylloc, p);
    }

  if (yychar <= END_OF_INPUT)
    {
      yychar = END_OF_INPUT;
      yytoken = YYSYMBOL_YYEOF;
      YYDPRINTF ((p, "Now at end of input.\n"));
    }
  else if (yychar == YYerror)
    {
      /* The scanner already issued an error message, process directly
         to error recovery.  But do not keep the error token as
         lookahead, it is too special and may lead us to an endless
         loop in error recovery. */
      yychar = YYUNDEF;
      yytoken = YYSYMBOL_YYerror;
      yyerror_range[1] = yylloc;
      goto yyerrlab1;
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
  case 2: /* $@1: %empty  */
#line 1327 "parse.y"
                   {
			SET_LEX_STATE(EXPR_BEG);
			local_push(p, ifndef_ripper(1)+0);
		    }
#line 7135 "parse.c"
    break;

  case 3: /* program: $@1 top_compstmt  */
#line 1332 "parse.y"
                    {
		    /*%%%*/
			if ((yyvsp[0].node) && !compile_for_eval) {
			    NODE *node = (yyvsp[0].node);
			    /* last expression should not be void */
			    if (nd_type_p(node, NODE_BLOCK)) {
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
#line 7159 "parse.c"
    break;

  case 4: /* top_compstmt: top_stmts opt_terms  */
#line 1354 "parse.y"
                    {
			(yyval.node) = void_stmts(p, (yyvsp[-1].node));
		    }
#line 7167 "parse.c"
    break;

  case 5: /* top_stmts: none  */
#line 1360 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, void_stmt!) %*/
		    }
#line 7178 "parse.c"
    break;

  case 6: /* top_stmts: top_stmt  */
#line 1367 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = newline_node((yyvsp[0].node));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, $1) %*/
		    }
#line 7189 "parse.c"
    break;

  case 7: /* top_stmts: top_stmts terms top_stmt  */
#line 1374 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = block_append(p, (yyvsp[-2].node), newline_node((yyvsp[0].node)));
		    /*% %*/
		    /*% ripper: stmts_add!($1, $3) %*/
		    }
#line 7200 "parse.c"
    break;

  case 8: /* top_stmts: error top_stmt  */
#line 1381 "parse.y"
                    {
			(yyval.node) = remove_begin((yyvsp[0].node));
		    }
#line 7208 "parse.c"
    break;

  case 10: /* top_stmt: "`BEGIN'" begin_block  */
#line 1388 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 7216 "parse.c"
    break;

  case 11: /* begin_block: '{' top_compstmt '}'  */
#line 1394 "parse.y"
                    {
		    /*%%%*/
			p->eval_tree_begin = block_append(p, p->eval_tree_begin,
							  NEW_BEGIN((yyvsp[-1].node), &(yyloc)));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: BEGIN!($2) %*/
		    }
#line 7229 "parse.c"
    break;

  case 12: /* $@2: %empty  */
#line 1406 "parse.y"
                         {if (!(yyvsp[-1].node)) {yyerror1(&(yylsp[0]), "else without rescue is useless");}}
#line 7235 "parse.c"
    break;

  case 13: /* bodystmt: compstmt opt_rescue k_else $@2 compstmt opt_ensure  */
#line 1409 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_bodystmt(p, (yyvsp[-5].node), (yyvsp[-4].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: bodystmt!(escape_Qundef($1), escape_Qundef($2), escape_Qundef($5), escape_Qundef($6)) %*/
		    }
#line 7246 "parse.c"
    break;

  case 14: /* bodystmt: compstmt opt_rescue opt_ensure  */
#line 1418 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_bodystmt(p, (yyvsp[-2].node), (yyvsp[-1].node), 0, (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: bodystmt!(escape_Qundef($1), escape_Qundef($2), Qnil, escape_Qundef($3)) %*/
		    }
#line 7257 "parse.c"
    break;

  case 15: /* compstmt: stmts opt_terms  */
#line 1427 "parse.y"
                    {
			(yyval.node) = void_stmts(p, (yyvsp[-1].node));
		    }
#line 7265 "parse.c"
    break;

  case 16: /* stmts: none  */
#line 1433 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, void_stmt!) %*/
		    }
#line 7276 "parse.c"
    break;

  case 17: /* stmts: stmt_or_begin  */
#line 1440 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = newline_node((yyvsp[0].node));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, $1) %*/
		    }
#line 7287 "parse.c"
    break;

  case 18: /* stmts: stmts terms stmt_or_begin  */
#line 1447 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = block_append(p, (yyvsp[-2].node), newline_node((yyvsp[0].node)));
		    /*% %*/
		    /*% ripper: stmts_add!($1, $3) %*/
		    }
#line 7298 "parse.c"
    break;

  case 19: /* stmts: error stmt  */
#line 1454 "parse.y"
                    {
			(yyval.node) = remove_begin((yyvsp[0].node));
		    }
#line 7306 "parse.c"
    break;

  case 20: /* stmt_or_begin: stmt  */
#line 1460 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 7314 "parse.c"
    break;

  case 21: /* $@3: %empty  */
#line 1464 "parse.y"
                    {
			yyerror1(&(yylsp[0]), "BEGIN is permitted only at toplevel");
		    }
#line 7322 "parse.c"
    break;

  case 22: /* stmt_or_begin: "`BEGIN'" $@3 begin_block  */
#line 1468 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 7330 "parse.c"
    break;

  case 23: /* $@4: %empty  */
#line 1473 "parse.y"
                                      {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 7336 "parse.c"
    break;

  case 24: /* stmt: "`alias'" fitem $@4 fitem  */
#line 1474 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_ALIAS((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: alias!($2, $4) %*/
		    }
#line 7347 "parse.c"
    break;

  case 25: /* stmt: "`alias'" "global variable" "global variable"  */
#line 1481 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_VALIAS((yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_alias!($2, $3) %*/
		    }
#line 7358 "parse.c"
    break;

  case 26: /* stmt: "`alias'" "global variable" "back reference"  */
#line 1488 "parse.y"
                    {
		    /*%%%*/
			char buf[2];
			buf[0] = '$';
			buf[1] = (char)(yyvsp[0].node)->nd_nth;
			(yyval.node) = NEW_VALIAS((yyvsp[-1].id), rb_intern2(buf, 2), &(yyloc));
		    /*% %*/
		    /*% ripper: var_alias!($2, $3) %*/
		    }
#line 7372 "parse.c"
    break;

  case 27: /* stmt: "`alias'" "global variable" "numbered reference"  */
#line 1498 "parse.y"
                    {
			static const char mesg[] = "can't make alias for the number variables";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: alias_error!(ERR_MESG(), $3) %*/
		    }
#line 7385 "parse.c"
    break;

  case 28: /* stmt: "`undef'" undef_list  */
#line 1507 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: undef!($2) %*/
		    }
#line 7396 "parse.c"
    break;

  case 29: /* stmt: stmt "`if' modifier" expr_value  */
#line 1514 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[0].node), remove_begin((yyvsp[-2].node)), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: if_mod!($3, $1) %*/
		    }
#line 7408 "parse.c"
    break;

  case 30: /* stmt: stmt "`unless' modifier" expr_value  */
#line 1522 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_unless(p, (yyvsp[0].node), remove_begin((yyvsp[-2].node)), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: unless_mod!($3, $1) %*/
		    }
#line 7420 "parse.c"
    break;

  case 31: /* stmt: stmt "`while' modifier" expr_value  */
#line 1530 "parse.y"
                    {
		    /*%%%*/
			if ((yyvsp[-2].node) && nd_type_p((yyvsp[-2].node), NODE_BEGIN)) {
			    (yyval.node) = NEW_WHILE(cond(p, (yyvsp[0].node), &(yylsp[0])), (yyvsp[-2].node)->nd_body, 0, &(yyloc));
			}
			else {
			    (yyval.node) = NEW_WHILE(cond(p, (yyvsp[0].node), &(yylsp[0])), (yyvsp[-2].node), 1, &(yyloc));
			}
		    /*% %*/
		    /*% ripper: while_mod!($3, $1) %*/
		    }
#line 7436 "parse.c"
    break;

  case 32: /* stmt: stmt "`until' modifier" expr_value  */
#line 1542 "parse.y"
                    {
		    /*%%%*/
			if ((yyvsp[-2].node) && nd_type_p((yyvsp[-2].node), NODE_BEGIN)) {
			    (yyval.node) = NEW_UNTIL(cond(p, (yyvsp[0].node), &(yylsp[0])), (yyvsp[-2].node)->nd_body, 0, &(yyloc));
			}
			else {
			    (yyval.node) = NEW_UNTIL(cond(p, (yyvsp[0].node), &(yylsp[0])), (yyvsp[-2].node), 1, &(yyloc));
			}
		    /*% %*/
		    /*% ripper: until_mod!($3, $1) %*/
		    }
#line 7452 "parse.c"
    break;

  case 33: /* stmt: stmt "`rescue' modifier" stmt  */
#line 1554 "parse.y"
                    {
		    /*%%%*/
			NODE *resq;
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			resq = NEW_RESBODY(0, remove_begin((yyvsp[0].node)), 0, &loc);
			(yyval.node) = NEW_RESCUE(remove_begin((yyvsp[-2].node)), resq, 0, &(yyloc));
		    /*% %*/
		    /*% ripper: rescue_mod!($1, $3) %*/
		    }
#line 7466 "parse.c"
    break;

  case 34: /* stmt: "`END'" '{' compstmt '}'  */
#line 1564 "parse.y"
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
#line 7484 "parse.c"
    break;

  case 36: /* stmt: mlhs '=' lex_ctxt command_call  */
#line 1579 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: massign!($1, $4) %*/
		    }
#line 7496 "parse.c"
    break;

  case 37: /* stmt: lhs '=' lex_ctxt mrhs  */
#line 1587 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: assign!($1, $4) %*/
		    }
#line 7507 "parse.c"
    break;

  case 38: /* stmt: mlhs '=' lex_ctxt mrhs_arg "`rescue' modifier" stmt  */
#line 1594 "parse.y"
                    {
                    /*%%%*/
                        YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			(yyval.node) = node_assign(p, (yyvsp[-5].node), NEW_RESCUE((yyvsp[-2].node), NEW_RESBODY(0, remove_begin((yyvsp[0].node)), 0, &loc), 0, &(yyloc)), (yyvsp[-3].ctxt), &(yyloc));
                    /*% %*/
                    /*% ripper: massign!($1, rescue_mod!($4, $6)) %*/
                    }
#line 7519 "parse.c"
    break;

  case 39: /* stmt: mlhs '=' lex_ctxt mrhs_arg  */
#line 1602 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: massign!($1, $4) %*/
		    }
#line 7530 "parse.c"
    break;

  case 41: /* command_asgn: lhs '=' lex_ctxt command_rhs  */
#line 1612 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: assign!($1, $4) %*/
		    }
#line 7541 "parse.c"
    break;

  case 42: /* command_asgn: var_lhs "operator-assignment" lex_ctxt command_rhs  */
#line 1619 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_op_assign(p, (yyvsp[-3].node), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!($1, $2, $4) %*/
		    }
#line 7552 "parse.c"
    break;

  case 43: /* command_asgn: primary_value '[' opt_call_args rbracket "operator-assignment" lex_ctxt command_rhs  */
#line 1626 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_ary_op_assign(p, (yyvsp[-6].node), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[0].node), &(yylsp[-4]), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(aref_field!($1, escape_Qundef($3)), $5, $7) %*/

		    }
#line 7564 "parse.c"
    break;

  case 44: /* command_asgn: primary_value call_op "local variable or method" "operator-assignment" lex_ctxt command_rhs  */
#line 1634 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 7575 "parse.c"
    break;

  case 45: /* command_asgn: primary_value call_op "constant" "operator-assignment" lex_ctxt command_rhs  */
#line 1641 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 7586 "parse.c"
    break;

  case 46: /* command_asgn: primary_value "::" "constant" "operator-assignment" lex_ctxt command_rhs  */
#line 1648 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-5]), &(yylsp[-3]));
			(yyval.node) = new_const_op_assign(p, NEW_COLON2((yyvsp[-5].node), (yyvsp[-3].id), &loc), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(const_path_field!($1, $3), $4, $6) %*/
		    }
#line 7598 "parse.c"
    break;

  case 47: /* command_asgn: primary_value "::" "local variable or method" "operator-assignment" lex_ctxt command_rhs  */
#line 1656 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), ID2VAL(idCOLON2), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, ID2VAL(idCOLON2), $3), $4, $6) %*/
		    }
#line 7609 "parse.c"
    break;

  case 48: /* command_asgn: defn_head f_opt_paren_args '=' command  */
#line 1663 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-3].node), (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper[$4]: bodystmt!($4, Qnil, Qnil, Qnil) %*/
		    /*% ripper: def!(get_value($1), $2, $4) %*/
			local_pop(p);
		    }
#line 7624 "parse.c"
    break;

  case 49: /* command_asgn: defn_head f_opt_paren_args '=' command "`rescue' modifier" arg  */
#line 1674 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
		    /*%%%*/
			(yyvsp[-2].node) = rescued_expr(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.node) = set_defun_body(p, (yyvsp[-5].node), (yyvsp[-4].node), (yyvsp[-2].node), &(yyloc));
		    /*% %*/
		    /*% ripper[$4]: bodystmt!(rescue_mod!($4, $6), Qnil, Qnil, Qnil) %*/
		    /*% ripper: def!(get_value($1), $2, $4) %*/
			local_pop(p);
		    }
#line 7640 "parse.c"
    break;

  case 50: /* command_asgn: defs_head f_opt_paren_args '=' command  */
#line 1686 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-3].node), (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*%
			$1 = get_value($1);
		    %*/
		    /*% ripper[$4]: bodystmt!($4, Qnil, Qnil, Qnil) %*/
		    /*% ripper: defs!(AREF($1, 0), AREF($1, 1), AREF($1, 2), $2, $4) %*/
			local_pop(p);
		    }
#line 7657 "parse.c"
    break;

  case 51: /* command_asgn: defs_head f_opt_paren_args '=' command "`rescue' modifier" arg  */
#line 1699 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
		    /*%%%*/
			(yyvsp[-2].node) = rescued_expr(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.node) = set_defun_body(p, (yyvsp[-5].node), (yyvsp[-4].node), (yyvsp[-2].node), &(yyloc));
		    /*%
			$1 = get_value($1);
		    %*/
		    /*% ripper[$4]: bodystmt!(rescue_mod!($4, $6), Qnil, Qnil, Qnil) %*/
		    /*% ripper: defs!(AREF($1, 0), AREF($1, 1), AREF($1, 2), $2, $4) %*/
			local_pop(p);
		    }
#line 7675 "parse.c"
    break;

  case 52: /* command_asgn: backref "operator-assignment" lex_ctxt command_rhs  */
#line 1713 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[-3].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), assign!(var_field(p, $1), $4)) %*/
		    }
#line 7687 "parse.c"
    break;

  case 53: /* command_rhs: command_call  */
#line 1723 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 7696 "parse.c"
    break;

  case 54: /* command_rhs: command_call "`rescue' modifier" stmt  */
#line 1728 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			value_expr((yyvsp[-2].node));
			(yyval.node) = NEW_RESCUE((yyvsp[-2].node), NEW_RESBODY(0, remove_begin((yyvsp[0].node)), 0, &loc), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: rescue_mod!($1, $3) %*/
		    }
#line 7709 "parse.c"
    break;

  case 57: /* expr: expr "`and'" expr  */
#line 1741 "parse.y"
                    {
			(yyval.node) = logop(p, idAND, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 7717 "parse.c"
    break;

  case 58: /* expr: expr "`or'" expr  */
#line 1745 "parse.y"
                    {
			(yyval.node) = logop(p, idOR, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 7725 "parse.c"
    break;

  case 59: /* expr: "`not'" opt_nl expr  */
#line 1749 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[0].node), &(yylsp[0])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 7733 "parse.c"
    break;

  case 60: /* expr: '!' command_call  */
#line 1753 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[0].node), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 7741 "parse.c"
    break;

  case 61: /* @5: %empty  */
#line 1757 "parse.y"
                    {
			value_expr((yyvsp[-1].node));
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 7754 "parse.c"
    break;

  case 62: /* @6: %empty  */
#line 1765 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
		    }
#line 7762 "parse.c"
    break;

  case 63: /* expr: arg "=>" @5 @6 p_top_expr_body  */
#line 1769 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-1].tbl));
			pop_pvtbl(p, (yyvsp[-2].tbl));
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
		    /*%%%*/
			(yyval.node) = NEW_CASE3((yyvsp[-4].node), NEW_IN((yyvsp[0].node), 0, 0, &(yylsp[0])), &(yyloc));
		    /*% %*/
		    /*% ripper: case!($1, in!($5, Qnil, Qnil)) %*/
		    }
#line 7776 "parse.c"
    break;

  case 64: /* @7: %empty  */
#line 1779 "parse.y"
                    {
			value_expr((yyvsp[-1].node));
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 7789 "parse.c"
    break;

  case 65: /* @8: %empty  */
#line 1787 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
		    }
#line 7797 "parse.c"
    break;

  case 66: /* expr: arg "`in'" @7 @8 p_top_expr_body  */
#line 1791 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-1].tbl));
			pop_pvtbl(p, (yyvsp[-2].tbl));
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
		    /*%%%*/
			(yyval.node) = NEW_CASE3((yyvsp[-4].node), NEW_IN((yyvsp[0].node), NEW_TRUE(&(yylsp[0])), NEW_FALSE(&(yylsp[0])), &(yylsp[0])), &(yyloc));
		    /*% %*/
		    /*% ripper: case!($1, in!($5, Qnil, Qnil)) %*/
		    }
#line 7811 "parse.c"
    break;

  case 68: /* def_name: fname  */
#line 1804 "parse.y"
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
#line 7830 "parse.c"
    break;

  case 69: /* defn_head: k_def def_name  */
#line 1821 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    /*%%%*/
			(yyval.node) = NEW_NODE(NODE_DEFN, 0, (yyval.node)->nd_mid, (yyval.node), &(yyloc));
		    /*% %*/
		    }
#line 7841 "parse.c"
    break;

  case 70: /* $@9: %empty  */
#line 1830 "parse.y"
                    {
			SET_LEX_STATE(EXPR_FNAME);
			p->ctxt.in_argdef = 1;
		    }
#line 7850 "parse.c"
    break;

  case 71: /* defs_head: k_def singleton dot_or_colon $@9 def_name  */
#line 1835 "parse.y"
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
#line 7866 "parse.c"
    break;

  case 72: /* expr_value: expr  */
#line 1849 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 7875 "parse.c"
    break;

  case 73: /* $@10: %empty  */
#line 1855 "parse.y"
                  {COND_PUSH(1);}
#line 7881 "parse.c"
    break;

  case 74: /* $@11: %empty  */
#line 1855 "parse.y"
                                                {COND_POP();}
#line 7887 "parse.c"
    break;

  case 75: /* expr_value_do: $@10 expr_value do $@11  */
#line 1856 "parse.y"
                    {
			(yyval.node) = (yyvsp[-2].node);
		    }
#line 7895 "parse.c"
    break;

  case 79: /* block_command: block_call call_op2 operation2 command_args  */
#line 1867 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, $2, $3), $4) %*/
		    }
#line 7906 "parse.c"
    break;

  case 80: /* cmd_brace_block: "{ arg" brace_body '}'  */
#line 1876 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 7918 "parse.c"
    break;

  case 81: /* fcall: operation  */
#line 1886 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_FCALL((yyvsp[0].id), 0, &(yyloc));
			nd_set_line((yyval.node), p->tokline);
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 7930 "parse.c"
    break;

  case 82: /* command: fcall command_args  */
#line 1896 "parse.y"
                    {
		    /*%%%*/
			(yyvsp[-1].node)->nd_args = (yyvsp[0].node);
			nd_set_last_loc((yyvsp[-1].node), (yylsp[0]).end_pos);
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: command!($1, $2) %*/
		    }
#line 7943 "parse.c"
    break;

  case 83: /* command: fcall command_args cmd_brace_block  */
#line 1905 "parse.y"
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
#line 7958 "parse.c"
    break;

  case 84: /* command: primary_value call_op operation2 command_args  */
#line 1916 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), Qnull, &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: command_call!($1, $2, $3, $4) %*/
		    }
#line 7969 "parse.c"
    break;

  case 85: /* command: primary_value call_op operation2 command_args cmd_brace_block  */
#line 1923 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-3].id), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(command_call!($1, $2, $3, $4), $5) %*/
		    }
#line 7980 "parse.c"
    break;

  case 86: /* command: primary_value "::" operation2 command_args  */
#line 1930 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), Qnull, &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: command_call!($1, ID2VAL(idCOLON2), $3, $4) %*/
		    }
#line 7991 "parse.c"
    break;

  case 87: /* command: primary_value "::" operation2 command_args cmd_brace_block  */
#line 1937 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(command_call!($1, ID2VAL(idCOLON2), $3, $4), $5) %*/
		   }
#line 8002 "parse.c"
    break;

  case 88: /* command: "`super'" command_args  */
#line 1944 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SUPER((yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: super!($2) %*/
		    }
#line 8014 "parse.c"
    break;

  case 89: /* command: "`yield'" command_args  */
#line 1952 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_yield(p, (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: yield!($2) %*/
		    }
#line 8026 "parse.c"
    break;

  case 90: /* command: k_return call_args  */
#line 1960 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RETURN(ret_args(p, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: return!($2) %*/
		    }
#line 8037 "parse.c"
    break;

  case 91: /* command: "`break'" call_args  */
#line 1967 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BREAK(ret_args(p, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: break!($2) %*/
		    }
#line 8048 "parse.c"
    break;

  case 92: /* command: "`next'" call_args  */
#line 1974 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_NEXT(ret_args(p, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: next!($2) %*/
		    }
#line 8059 "parse.c"
    break;

  case 94: /* mlhs: "(" mlhs_inner rparen  */
#line 1984 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 8070 "parse.c"
    break;

  case 96: /* mlhs_inner: "(" mlhs_inner rparen  */
#line 1994 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(NEW_LIST((yyvsp[-1].node), &(yyloc)), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 8081 "parse.c"
    break;

  case 97: /* mlhs_basic: mlhs_head  */
#line 2003 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[0].node), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 8092 "parse.c"
    break;

  case 98: /* mlhs_basic: mlhs_head mlhs_item  */
#line 2010 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(list_append(p, (yyvsp[-1].node),(yyvsp[0].node)), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $2) %*/
		    }
#line 8103 "parse.c"
    break;

  case 99: /* mlhs_basic: mlhs_head "*" mlhs_node  */
#line 2017 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!($1, $3) %*/
		    }
#line 8114 "parse.c"
    break;

  case 100: /* mlhs_basic: mlhs_head "*" mlhs_node ',' mlhs_post  */
#line 2024 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-4].node), NEW_POSTARG((yyvsp[-2].node),(yyvsp[0].node),&(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!($1, $3), $5) %*/
		    }
#line 8125 "parse.c"
    break;

  case 101: /* mlhs_basic: mlhs_head "*"  */
#line 2031 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-1].node), NODE_SPECIAL_NO_NAME_REST, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!($1, Qnil) %*/
		    }
#line 8136 "parse.c"
    break;

  case 102: /* mlhs_basic: mlhs_head "*" ',' mlhs_post  */
#line 2038 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-3].node), NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!($1, Qnil), $4) %*/
		    }
#line 8147 "parse.c"
    break;

  case 103: /* mlhs_basic: "*" mlhs_node  */
#line 2045 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!(mlhs_new!, $2) %*/
		    }
#line 8158 "parse.c"
    break;

  case 104: /* mlhs_basic: "*" mlhs_node ',' mlhs_post  */
#line 2052 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NEW_POSTARG((yyvsp[-2].node),(yyvsp[0].node),&(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!(mlhs_new!, $2), $4) %*/
		    }
#line 8169 "parse.c"
    break;

  case 105: /* mlhs_basic: "*"  */
#line 2059 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NODE_SPECIAL_NO_NAME_REST, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!(mlhs_new!, Qnil) %*/
		    }
#line 8180 "parse.c"
    break;

  case 106: /* mlhs_basic: "*" ',' mlhs_post  */
#line 2066 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!(mlhs_new!, Qnil), $3) %*/
		    }
#line 8191 "parse.c"
    break;

  case 108: /* mlhs_item: "(" mlhs_inner rparen  */
#line 2076 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 8202 "parse.c"
    break;

  case 109: /* mlhs_head: mlhs_item ','  */
#line 2085 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[-1].node), &(yylsp[-1]));
		    /*% %*/
		    /*% ripper: mlhs_add!(mlhs_new!, $1) %*/
		    }
#line 8213 "parse.c"
    break;

  case 110: /* mlhs_head: mlhs_head mlhs_item ','  */
#line 2092 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $2) %*/
		    }
#line 8224 "parse.c"
    break;

  case 111: /* mlhs_post: mlhs_item  */
#line 2101 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add!(mlhs_new!, $1) %*/
		    }
#line 8235 "parse.c"
    break;

  case 112: /* mlhs_post: mlhs_post ',' mlhs_item  */
#line 2108 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $3) %*/
		    }
#line 8246 "parse.c"
    break;

  case 113: /* mlhs_node: user_variable  */
#line 2117 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 8257 "parse.c"
    break;

  case 114: /* mlhs_node: keyword_variable  */
#line 2124 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 8268 "parse.c"
    break;

  case 115: /* mlhs_node: primary_value '[' opt_call_args rbracket  */
#line 2131 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = aryset(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: aref_field!($1, escape_Qundef($3)) %*/
		    }
#line 8279 "parse.c"
    break;

  case 116: /* mlhs_node: primary_value call_op "local variable or method"  */
#line 2138 "parse.y"
                    {
			if ((yyvsp[-1].id) == tANDDOT) {
			    yyerror1(&(yylsp[-1]), "&. inside multiple assignment destination");
			}
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 8293 "parse.c"
    break;

  case 117: /* mlhs_node: primary_value "::" "local variable or method"  */
#line 2148 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), idCOLON2, (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_field!($1, $3) %*/
		    }
#line 8304 "parse.c"
    break;

  case 118: /* mlhs_node: primary_value call_op "constant"  */
#line 2155 "parse.y"
                    {
			if ((yyvsp[-1].id) == tANDDOT) {
			    yyerror1(&(yylsp[-1]), "&. inside multiple assignment destination");
			}
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 8318 "parse.c"
    break;

  case 119: /* mlhs_node: primary_value "::" "constant"  */
#line 2165 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, const_path_field!($1, $3)) %*/
		    }
#line 8329 "parse.c"
    break;

  case 120: /* mlhs_node: ":: at EXPR_BEG" "constant"  */
#line 2172 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON3((yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, top_const_field!($2)) %*/
		    }
#line 8340 "parse.c"
    break;

  case 121: /* mlhs_node: backref  */
#line 2179 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[0].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), var_field(p, $1)) %*/
		    }
#line 8352 "parse.c"
    break;

  case 122: /* lhs: user_variable  */
#line 2189 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 8363 "parse.c"
    break;

  case 123: /* lhs: keyword_variable  */
#line 2196 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 8374 "parse.c"
    break;

  case 124: /* lhs: primary_value '[' opt_call_args rbracket  */
#line 2203 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = aryset(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: aref_field!($1, escape_Qundef($3)) %*/
		    }
#line 8385 "parse.c"
    break;

  case 125: /* lhs: primary_value call_op "local variable or method"  */
#line 2210 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 8396 "parse.c"
    break;

  case 126: /* lhs: primary_value "::" "local variable or method"  */
#line 2217 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), idCOLON2, (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, ID2VAL(idCOLON2), $3) %*/
		    }
#line 8407 "parse.c"
    break;

  case 127: /* lhs: primary_value call_op "constant"  */
#line 2224 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 8418 "parse.c"
    break;

  case 128: /* lhs: primary_value "::" "constant"  */
#line 2231 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, const_path_field!($1, $3)) %*/
		    }
#line 8429 "parse.c"
    break;

  case 129: /* lhs: ":: at EXPR_BEG" "constant"  */
#line 2238 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON3((yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, top_const_field!($2)) %*/
		    }
#line 8440 "parse.c"
    break;

  case 130: /* lhs: backref  */
#line 2245 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[0].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), var_field(p, $1)) %*/
		    }
#line 8452 "parse.c"
    break;

  case 131: /* cname: "local variable or method"  */
#line 2255 "parse.y"
                    {
			static const char mesg[] = "class/module name must be CONSTANT";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
		    /*% %*/
		    /*% ripper[error]: class_name_error!(ERR_MESG(), $1) %*/
		    }
#line 8464 "parse.c"
    break;

  case 133: /* cpath: ":: at EXPR_BEG" cname  */
#line 2266 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON3((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: top_const_ref!($2) %*/
		    }
#line 8475 "parse.c"
    break;

  case 134: /* cpath: cname  */
#line 2273 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2(0, (yyval.node), &(yyloc));
		    /*% %*/
		    /*% ripper: const_ref!($1) %*/
		    }
#line 8486 "parse.c"
    break;

  case 135: /* cpath: primary_value "::" cname  */
#line 2280 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_ref!($1, $3) %*/
		    }
#line 8497 "parse.c"
    break;

  case 139: /* fname: op  */
#line 2292 "parse.y"
                    {
			SET_LEX_STATE(EXPR_ENDFN);
			(yyval.id) = (yyvsp[0].id);
		    }
#line 8506 "parse.c"
    break;

  case 141: /* fitem: fname  */
#line 2300 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIT(ID2SYM((yyvsp[0].id)), &(yyloc));
		    /*% %*/
		    /*% ripper: symbol_literal!($1) %*/
		    }
#line 8517 "parse.c"
    break;

  case 143: /* undef_list: fitem  */
#line 2310 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_UNDEF((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 8528 "parse.c"
    break;

  case 144: /* $@12: %empty  */
#line 2316 "parse.y"
                                 {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 8534 "parse.c"
    break;

  case 145: /* undef_list: undef_list ',' $@12 fitem  */
#line 2317 "parse.y"
                    {
		    /*%%%*/
			NODE *undef = NEW_UNDEF((yyvsp[0].node), &(yylsp[0]));
			(yyval.node) = block_append(p, (yyvsp[-3].node), undef);
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($4)) %*/
		    }
#line 8546 "parse.c"
    break;

  case 146: /* op: '|'  */
#line 2326 "parse.y"
                                { ifndef_ripper((yyval.id) = '|'); }
#line 8552 "parse.c"
    break;

  case 147: /* op: '^'  */
#line 2327 "parse.y"
                                { ifndef_ripper((yyval.id) = '^'); }
#line 8558 "parse.c"
    break;

  case 148: /* op: '&'  */
#line 2328 "parse.y"
                                { ifndef_ripper((yyval.id) = '&'); }
#line 8564 "parse.c"
    break;

  case 149: /* op: "<=>"  */
#line 2329 "parse.y"
                                { ifndef_ripper((yyval.id) = tCMP); }
#line 8570 "parse.c"
    break;

  case 150: /* op: "=="  */
#line 2330 "parse.y"
                                { ifndef_ripper((yyval.id) = tEQ); }
#line 8576 "parse.c"
    break;

  case 151: /* op: "==="  */
#line 2331 "parse.y"
                                { ifndef_ripper((yyval.id) = tEQQ); }
#line 8582 "parse.c"
    break;

  case 152: /* op: "=~"  */
#line 2332 "parse.y"
                                { ifndef_ripper((yyval.id) = tMATCH); }
#line 8588 "parse.c"
    break;

  case 153: /* op: "!~"  */
#line 2333 "parse.y"
                                { ifndef_ripper((yyval.id) = tNMATCH); }
#line 8594 "parse.c"
    break;

  case 154: /* op: '>'  */
#line 2334 "parse.y"
                                { ifndef_ripper((yyval.id) = '>'); }
#line 8600 "parse.c"
    break;

  case 155: /* op: ">="  */
#line 2335 "parse.y"
                                { ifndef_ripper((yyval.id) = tGEQ); }
#line 8606 "parse.c"
    break;

  case 156: /* op: '<'  */
#line 2336 "parse.y"
                                { ifndef_ripper((yyval.id) = '<'); }
#line 8612 "parse.c"
    break;

  case 157: /* op: "<="  */
#line 2337 "parse.y"
                                { ifndef_ripper((yyval.id) = tLEQ); }
#line 8618 "parse.c"
    break;

  case 158: /* op: "!="  */
#line 2338 "parse.y"
                                { ifndef_ripper((yyval.id) = tNEQ); }
#line 8624 "parse.c"
    break;

  case 159: /* op: "<<"  */
#line 2339 "parse.y"
                                { ifndef_ripper((yyval.id) = tLSHFT); }
#line 8630 "parse.c"
    break;

  case 160: /* op: ">>"  */
#line 2340 "parse.y"
                                { ifndef_ripper((yyval.id) = tRSHFT); }
#line 8636 "parse.c"
    break;

  case 161: /* op: '+'  */
#line 2341 "parse.y"
                                { ifndef_ripper((yyval.id) = '+'); }
#line 8642 "parse.c"
    break;

  case 162: /* op: '-'  */
#line 2342 "parse.y"
                                { ifndef_ripper((yyval.id) = '-'); }
#line 8648 "parse.c"
    break;

  case 163: /* op: '*'  */
#line 2343 "parse.y"
                                { ifndef_ripper((yyval.id) = '*'); }
#line 8654 "parse.c"
    break;

  case 164: /* op: "*"  */
#line 2344 "parse.y"
                                { ifndef_ripper((yyval.id) = '*'); }
#line 8660 "parse.c"
    break;

  case 165: /* op: '/'  */
#line 2345 "parse.y"
                                { ifndef_ripper((yyval.id) = '/'); }
#line 8666 "parse.c"
    break;

  case 166: /* op: '%'  */
#line 2346 "parse.y"
                                { ifndef_ripper((yyval.id) = '%'); }
#line 8672 "parse.c"
    break;

  case 167: /* op: "**"  */
#line 2347 "parse.y"
                                { ifndef_ripper((yyval.id) = tPOW); }
#line 8678 "parse.c"
    break;

  case 168: /* op: "**arg"  */
#line 2348 "parse.y"
                                { ifndef_ripper((yyval.id) = tDSTAR); }
#line 8684 "parse.c"
    break;

  case 169: /* op: '!'  */
#line 2349 "parse.y"
                                { ifndef_ripper((yyval.id) = '!'); }
#line 8690 "parse.c"
    break;

  case 170: /* op: '~'  */
#line 2350 "parse.y"
                                { ifndef_ripper((yyval.id) = '~'); }
#line 8696 "parse.c"
    break;

  case 171: /* op: "unary+"  */
#line 2351 "parse.y"
                                { ifndef_ripper((yyval.id) = tUPLUS); }
#line 8702 "parse.c"
    break;

  case 172: /* op: "unary-"  */
#line 2352 "parse.y"
                                { ifndef_ripper((yyval.id) = tUMINUS); }
#line 8708 "parse.c"
    break;

  case 173: /* op: "[]"  */
#line 2353 "parse.y"
                                { ifndef_ripper((yyval.id) = tAREF); }
#line 8714 "parse.c"
    break;

  case 174: /* op: "[]="  */
#line 2354 "parse.y"
                                { ifndef_ripper((yyval.id) = tASET); }
#line 8720 "parse.c"
    break;

  case 175: /* op: '`'  */
#line 2355 "parse.y"
                                { ifndef_ripper((yyval.id) = '`'); }
#line 8726 "parse.c"
    break;

  case 217: /* arg: lhs '=' lex_ctxt arg_rhs  */
#line 2373 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: assign!($1, $4) %*/
		    }
#line 8737 "parse.c"
    break;

  case 218: /* arg: var_lhs "operator-assignment" lex_ctxt arg_rhs  */
#line 2380 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_op_assign(p, (yyvsp[-3].node), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!($1, $2, $4) %*/
		    }
#line 8748 "parse.c"
    break;

  case 219: /* arg: primary_value '[' opt_call_args rbracket "operator-assignment" lex_ctxt arg_rhs  */
#line 2387 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_ary_op_assign(p, (yyvsp[-6].node), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[0].node), &(yylsp[-4]), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(aref_field!($1, escape_Qundef($3)), $5, $7) %*/
		    }
#line 8759 "parse.c"
    break;

  case 220: /* arg: primary_value call_op "local variable or method" "operator-assignment" lex_ctxt arg_rhs  */
#line 2394 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 8770 "parse.c"
    break;

  case 221: /* arg: primary_value call_op "constant" "operator-assignment" lex_ctxt arg_rhs  */
#line 2401 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 8781 "parse.c"
    break;

  case 222: /* arg: primary_value "::" "local variable or method" "operator-assignment" lex_ctxt arg_rhs  */
#line 2408 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), ID2VAL(idCOLON2), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, ID2VAL(idCOLON2), $3), $4, $6) %*/
		    }
#line 8792 "parse.c"
    break;

  case 223: /* arg: primary_value "::" "constant" "operator-assignment" lex_ctxt arg_rhs  */
#line 2415 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-5]), &(yylsp[-3]));
			(yyval.node) = new_const_op_assign(p, NEW_COLON2((yyvsp[-5].node), (yyvsp[-3].id), &loc), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(const_path_field!($1, $3), $4, $6) %*/
		    }
#line 8804 "parse.c"
    break;

  case 224: /* arg: ":: at EXPR_BEG" "constant" "operator-assignment" lex_ctxt arg_rhs  */
#line 2423 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-4]), &(yylsp[-3]));
			(yyval.node) = new_const_op_assign(p, NEW_COLON3((yyvsp[-3].id), &loc), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(top_const_field!($2), $3, $5) %*/
		    }
#line 8816 "parse.c"
    break;

  case 225: /* arg: backref "operator-assignment" lex_ctxt arg_rhs  */
#line 2431 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[-3].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), opassign!(var_field(p, $1), $2, $4)) %*/
		    }
#line 8828 "parse.c"
    break;

  case 226: /* arg: arg ".." arg  */
#line 2439 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, $3) %*/
		    }
#line 8841 "parse.c"
    break;

  case 227: /* arg: arg "..." arg  */
#line 2448 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, $3) %*/
		    }
#line 8854 "parse.c"
    break;

  case 228: /* arg: arg ".."  */
#line 2457 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT2((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, Qnil) %*/
		    }
#line 8866 "parse.c"
    break;

  case 229: /* arg: arg "..."  */
#line 2465 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT3((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, Qnil) %*/
		    }
#line 8878 "parse.c"
    break;

  case 230: /* arg: "(.." arg  */
#line 2473 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!(Qnil, $2) %*/
		    }
#line 8890 "parse.c"
    break;

  case 231: /* arg: "(..." arg  */
#line 2481 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!(Qnil, $2) %*/
		    }
#line 8902 "parse.c"
    break;

  case 232: /* arg: arg '+' arg  */
#line 2489 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '+', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8910 "parse.c"
    break;

  case 233: /* arg: arg '-' arg  */
#line 2493 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '-', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8918 "parse.c"
    break;

  case 234: /* arg: arg '*' arg  */
#line 2497 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '*', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8926 "parse.c"
    break;

  case 235: /* arg: arg '/' arg  */
#line 2501 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '/', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8934 "parse.c"
    break;

  case 236: /* arg: arg '%' arg  */
#line 2505 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '%', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8942 "parse.c"
    break;

  case 237: /* arg: arg "**" arg  */
#line 2509 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idPow, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8950 "parse.c"
    break;

  case 238: /* arg: tUMINUS_NUM simple_numeric "**" arg  */
#line 2513 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, call_bin_op(p, (yyvsp[-2].node), idPow, (yyvsp[0].node), &(yylsp[-2]), &(yyloc)), idUMinus, &(yylsp[-3]), &(yyloc));
		    }
#line 8958 "parse.c"
    break;

  case 239: /* arg: "unary+" arg  */
#line 2517 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, (yyvsp[0].node), idUPlus, &(yylsp[-1]), &(yyloc));
		    }
#line 8966 "parse.c"
    break;

  case 240: /* arg: "unary-" arg  */
#line 2521 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, (yyvsp[0].node), idUMinus, &(yylsp[-1]), &(yyloc));
		    }
#line 8974 "parse.c"
    break;

  case 241: /* arg: arg '|' arg  */
#line 2525 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '|', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8982 "parse.c"
    break;

  case 242: /* arg: arg '^' arg  */
#line 2529 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '^', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8990 "parse.c"
    break;

  case 243: /* arg: arg '&' arg  */
#line 2533 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '&', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 8998 "parse.c"
    break;

  case 244: /* arg: arg "<=>" arg  */
#line 2537 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idCmp, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9006 "parse.c"
    break;

  case 246: /* arg: arg "==" arg  */
#line 2542 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idEq, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9014 "parse.c"
    break;

  case 247: /* arg: arg "===" arg  */
#line 2546 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idEqq, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9022 "parse.c"
    break;

  case 248: /* arg: arg "!=" arg  */
#line 2550 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idNeq, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9030 "parse.c"
    break;

  case 249: /* arg: arg "=~" arg  */
#line 2554 "parse.y"
                    {
			(yyval.node) = match_op(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9038 "parse.c"
    break;

  case 250: /* arg: arg "!~" arg  */
#line 2558 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idNeqTilde, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9046 "parse.c"
    break;

  case 251: /* arg: '!' arg  */
#line 2562 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[0].node), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 9054 "parse.c"
    break;

  case 252: /* arg: '~' arg  */
#line 2566 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, (yyvsp[0].node), '~', &(yylsp[-1]), &(yyloc));
		    }
#line 9062 "parse.c"
    break;

  case 253: /* arg: arg "<<" arg  */
#line 2570 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idLTLT, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9070 "parse.c"
    break;

  case 254: /* arg: arg ">>" arg  */
#line 2574 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idGTGT, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9078 "parse.c"
    break;

  case 255: /* arg: arg "&&" arg  */
#line 2578 "parse.y"
                    {
			(yyval.node) = logop(p, idANDOP, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9086 "parse.c"
    break;

  case 256: /* arg: arg "||" arg  */
#line 2582 "parse.y"
                    {
			(yyval.node) = logop(p, idOROP, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9094 "parse.c"
    break;

  case 257: /* $@13: %empty  */
#line 2585 "parse.y"
                                         {p->ctxt.in_defined = 1;}
#line 9100 "parse.c"
    break;

  case 258: /* arg: "`defined?'" opt_nl $@13 arg  */
#line 2586 "parse.y"
                    {
			p->ctxt.in_defined = 0;
			(yyval.node) = new_defined(p, (yyvsp[0].node), &(yyloc));
		    }
#line 9109 "parse.c"
    break;

  case 259: /* arg: arg '?' arg opt_nl ':' arg  */
#line 2591 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-5].node));
			(yyval.node) = new_if(p, (yyvsp[-5].node), (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-5].node));
		    /*% %*/
		    /*% ripper: ifop!($1, $3, $6) %*/
		    }
#line 9122 "parse.c"
    break;

  case 260: /* arg: defn_head f_opt_paren_args '=' arg  */
#line 2600 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-3].node), (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper[$4]: bodystmt!($4, Qnil, Qnil, Qnil) %*/
		    /*% ripper: def!(get_value($1), $2, $4) %*/
			local_pop(p);
		    }
#line 9137 "parse.c"
    break;

  case 261: /* arg: defn_head f_opt_paren_args '=' arg "`rescue' modifier" arg  */
#line 2611 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
		    /*%%%*/
			(yyvsp[-2].node) = rescued_expr(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.node) = set_defun_body(p, (yyvsp[-5].node), (yyvsp[-4].node), (yyvsp[-2].node), &(yyloc));
		    /*% %*/
		    /*% ripper[$4]: bodystmt!(rescue_mod!($4, $6), Qnil, Qnil, Qnil) %*/
		    /*% ripper: def!(get_value($1), $2, $4) %*/
			local_pop(p);
		    }
#line 9153 "parse.c"
    break;

  case 262: /* arg: defs_head f_opt_paren_args '=' arg  */
#line 2623 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-3].node), (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*%
			$1 = get_value($1);
		    %*/
		    /*% ripper[$4]: bodystmt!($4, Qnil, Qnil, Qnil) %*/
		    /*% ripper: defs!(AREF($1, 0), AREF($1, 1), AREF($1, 2), $2, $4) %*/
			local_pop(p);
		    }
#line 9170 "parse.c"
    break;

  case 263: /* arg: defs_head f_opt_paren_args '=' arg "`rescue' modifier" arg  */
#line 2636 "parse.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
		    /*%%%*/
			(yyvsp[-2].node) = rescued_expr(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.node) = set_defun_body(p, (yyvsp[-5].node), (yyvsp[-4].node), (yyvsp[-2].node), &(yyloc));
		    /*%
			$1 = get_value($1);
		    %*/
		    /*% ripper[$4]: bodystmt!(rescue_mod!($4, $6), Qnil, Qnil, Qnil) %*/
		    /*% ripper: defs!(AREF($1, 0), AREF($1, 1), AREF($1, 2), $2, $4) %*/
			local_pop(p);
		    }
#line 9188 "parse.c"
    break;

  case 264: /* arg: primary  */
#line 2650 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9196 "parse.c"
    break;

  case 265: /* relop: '>'  */
#line 2655 "parse.y"
                       {(yyval.id) = '>';}
#line 9202 "parse.c"
    break;

  case 266: /* relop: '<'  */
#line 2656 "parse.y"
                       {(yyval.id) = '<';}
#line 9208 "parse.c"
    break;

  case 267: /* relop: ">="  */
#line 2657 "parse.y"
                       {(yyval.id) = idGE;}
#line 9214 "parse.c"
    break;

  case 268: /* relop: "<="  */
#line 2658 "parse.y"
                       {(yyval.id) = idLE;}
#line 9220 "parse.c"
    break;

  case 269: /* rel_expr: arg relop arg  */
#line 2662 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9228 "parse.c"
    break;

  case 270: /* rel_expr: rel_expr relop arg  */
#line 2666 "parse.y"
                    {
			rb_warning1("comparison '%s' after comparison", WARN_ID((yyvsp[-1].id)));
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9237 "parse.c"
    break;

  case 271: /* lex_ctxt: "escaped space"  */
#line 2673 "parse.y"
                    {
			(yyval.ctxt) = p->ctxt;
		    }
#line 9245 "parse.c"
    break;

  case 272: /* lex_ctxt: none  */
#line 2677 "parse.y"
                    {
			(yyval.ctxt) = p->ctxt;
		    }
#line 9253 "parse.c"
    break;

  case 273: /* arg_value: arg  */
#line 2683 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9262 "parse.c"
    break;

  case 275: /* aref_args: args trailer  */
#line 2691 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 9270 "parse.c"
    break;

  case 276: /* aref_args: args ',' assocs trailer  */
#line 2695 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? arg_append(p, (yyvsp[-3].node), new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].node);
		    /*% %*/
		    /*% ripper: args_add!($1, bare_assoc_hash!($3)) %*/
		    }
#line 9281 "parse.c"
    break;

  case 277: /* aref_args: assocs trailer  */
#line 2702 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? NEW_LIST(new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : 0;
		    /*% %*/
		    /*% ripper: args_add!(args_new!, bare_assoc_hash!($1)) %*/
		    }
#line 9292 "parse.c"
    break;

  case 278: /* arg_rhs: arg  */
#line 2711 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9301 "parse.c"
    break;

  case 279: /* arg_rhs: arg "`rescue' modifier" arg  */
#line 2716 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			(yyval.node) = rescued_expr(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
		    /*% %*/
		    /*% ripper: rescue_mod!($1, $3) %*/
		    }
#line 9313 "parse.c"
    break;

  case 280: /* paren_args: '(' opt_call_args rparen  */
#line 2726 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: arg_paren!(escape_Qundef($2)) %*/
		    }
#line 9324 "parse.c"
    break;

  case 281: /* paren_args: '(' args ',' args_forward rparen  */
#line 2733 "parse.y"
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
#line 9340 "parse.c"
    break;

  case 282: /* paren_args: '(' args_forward rparen  */
#line 2745 "parse.y"
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
#line 9356 "parse.c"
    break;

  case 287: /* opt_call_args: args ','  */
#line 2765 "parse.y"
                    {
		      (yyval.node) = (yyvsp[-1].node);
		    }
#line 9364 "parse.c"
    break;

  case 288: /* opt_call_args: args ',' assocs ','  */
#line 2769 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? arg_append(p, (yyvsp[-3].node), new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].node);
		    /*% %*/
		    /*% ripper: args_add!($1, bare_assoc_hash!($3)) %*/
		    }
#line 9375 "parse.c"
    break;

  case 289: /* opt_call_args: assocs ','  */
#line 2776 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? NEW_LIST(new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yylsp[-1])) : 0;
		    /*% %*/
		    /*% ripper: args_add!(args_new!, bare_assoc_hash!($1)) %*/
		    }
#line 9386 "parse.c"
    break;

  case 290: /* call_args: command  */
#line 2785 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!(args_new!, $1) %*/
		    }
#line 9398 "parse.c"
    break;

  case 291: /* call_args: args opt_block_arg  */
#line 2793 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = arg_blk_pass((yyvsp[-1].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: args_add_block!($1, $2) %*/
		    }
#line 9409 "parse.c"
    break;

  case 292: /* call_args: assocs opt_block_arg  */
#line 2800 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? NEW_LIST(new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yylsp[-1])) : 0;
			(yyval.node) = arg_blk_pass((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: args_add_block!(args_add!(args_new!, bare_assoc_hash!($1)), $2) %*/
		    }
#line 9421 "parse.c"
    break;

  case 293: /* call_args: args ',' assocs opt_block_arg  */
#line 2808 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? arg_append(p, (yyvsp[-3].node), new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].node);
			(yyval.node) = arg_blk_pass((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: args_add_block!(args_add!($1, bare_assoc_hash!($3)), $4) %*/
		    }
#line 9433 "parse.c"
    break;

  case 295: /* $@14: %empty  */
#line 2819 "parse.y"
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
#line 9455 "parse.c"
    break;

  case 296: /* command_args: $@14 call_args  */
#line 2837 "parse.y"
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
#line 9477 "parse.c"
    break;

  case 297: /* block_arg: "&" arg_value  */
#line 2857 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BLOCK_PASS((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: $2 %*/
		    }
#line 9488 "parse.c"
    break;

  case 298: /* block_arg: "&"  */
#line 2864 "parse.y"
                    {
                    /*%%%*/
                        if (!local_id(p, ANON_BLOCK_ID)) {
                            compile_error(p, "no anonymous block parameter");
                        }
                        (yyval.node) = NEW_BLOCK_PASS(NEW_LVAR(ANON_BLOCK_ID, &(yylsp[0])), &(yyloc));
                    /*%
                    $$ = Qnil;
                    %*/
                    }
#line 9503 "parse.c"
    break;

  case 299: /* opt_block_arg: ',' block_arg  */
#line 2877 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9511 "parse.c"
    break;

  case 300: /* opt_block_arg: none  */
#line 2881 "parse.y"
                    {
			(yyval.node) = 0;
		    }
#line 9519 "parse.c"
    break;

  case 301: /* args: arg_value  */
#line 2888 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!(args_new!, $1) %*/
		    }
#line 9530 "parse.c"
    break;

  case 302: /* args: "*" arg_value  */
#line 2895 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SPLAT((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!(args_new!, $2) %*/
		    }
#line 9541 "parse.c"
    break;

  case 303: /* args: args ',' arg_value  */
#line 2902 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = last_arg_append(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!($1, $3) %*/
		    }
#line 9552 "parse.c"
    break;

  case 304: /* args: args ',' "*" arg_value  */
#line 2909 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!($1, $4) %*/
		    }
#line 9563 "parse.c"
    break;

  case 307: /* mrhs: args ',' arg_value  */
#line 2924 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = last_arg_append(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mrhs_add!(mrhs_new_from_args!($1), $3) %*/
		    }
#line 9574 "parse.c"
    break;

  case 308: /* mrhs: args ',' "*" arg_value  */
#line 2931 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mrhs_add_star!(mrhs_new_from_args!($1), $4) %*/
		    }
#line 9585 "parse.c"
    break;

  case 309: /* mrhs: "*" arg_value  */
#line 2938 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SPLAT((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mrhs_add_star!(mrhs_new!, $2) %*/
		    }
#line 9596 "parse.c"
    break;

  case 320: /* primary: "method"  */
#line 2957 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_FCALL((yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_arg!(fcall!($1), args_new!) %*/
		    }
#line 9607 "parse.c"
    break;

  case 321: /* $@15: %empty  */
#line 2964 "parse.y"
                    {
			CMDARG_PUSH(0);
		    }
#line 9615 "parse.c"
    break;

  case 322: /* primary: k_begin $@15 bodystmt k_end  */
#line 2969 "parse.y"
                    {
			CMDARG_POP();
		    /*%%%*/
			set_line_body((yyvsp[-1].node), (yylsp[-3]).end_pos.lineno);
			(yyval.node) = NEW_BEGIN((yyvsp[-1].node), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-3]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: begin!($3) %*/
		    }
#line 9629 "parse.c"
    break;

  case 323: /* $@16: %empty  */
#line 2978 "parse.y"
                              {SET_LEX_STATE(EXPR_ENDARG);}
#line 9635 "parse.c"
    break;

  case 324: /* primary: "( arg" $@16 rparen  */
#line 2979 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: paren!(0) %*/
		    }
#line 9646 "parse.c"
    break;

  case 325: /* $@17: %empty  */
#line 2985 "parse.y"
                                   {SET_LEX_STATE(EXPR_ENDARG);}
#line 9652 "parse.c"
    break;

  case 326: /* primary: "( arg" stmt $@17 rparen  */
#line 2986 "parse.y"
                    {
		    /*%%%*/
			if (nd_type_p((yyvsp[-2].node), NODE_SELF)) (yyvsp[-2].node)->nd_state = 0;
			(yyval.node) = (yyvsp[-2].node);
		    /*% %*/
		    /*% ripper: paren!($2) %*/
		    }
#line 9664 "parse.c"
    break;

  case 327: /* primary: "(" compstmt ')'  */
#line 2994 "parse.y"
                    {
		    /*%%%*/
			if (nd_type_p((yyvsp[-1].node), NODE_SELF)) (yyvsp[-1].node)->nd_state = 0;
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: paren!($2) %*/
		    }
#line 9676 "parse.c"
    break;

  case 328: /* primary: primary_value "::" "constant"  */
#line 3002 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_ref!($1, $3) %*/
		    }
#line 9687 "parse.c"
    break;

  case 329: /* primary: ":: at EXPR_BEG" "constant"  */
#line 3009 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON3((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: top_const_ref!($2) %*/
		    }
#line 9698 "parse.c"
    break;

  case 330: /* primary: "[" aref_args ']'  */
#line 3016 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!(escape_Qundef($2)) %*/
		    }
#line 9709 "parse.c"
    break;

  case 331: /* primary: "{" assoc_list '}'  */
#line 3023 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_hash(p, (yyvsp[-1].node), &(yyloc));
			(yyval.node)->nd_brace = TRUE;
		    /*% %*/
		    /*% ripper: hash!(escape_Qundef($2)) %*/
		    }
#line 9721 "parse.c"
    break;

  case 332: /* primary: k_return  */
#line 3031 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RETURN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: return0! %*/
		    }
#line 9732 "parse.c"
    break;

  case 333: /* primary: "`yield'" '(' call_args rparen  */
#line 3038 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_yield(p, (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: yield!(paren!($3)) %*/
		    }
#line 9743 "parse.c"
    break;

  case 334: /* primary: "`yield'" '(' rparen  */
#line 3045 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_YIELD(0, &(yyloc));
		    /*% %*/
		    /*% ripper: yield!(paren!(args_new!)) %*/
		    }
#line 9754 "parse.c"
    break;

  case 335: /* primary: "`yield'"  */
#line 3052 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_YIELD(0, &(yyloc));
		    /*% %*/
		    /*% ripper: yield0! %*/
		    }
#line 9765 "parse.c"
    break;

  case 336: /* $@18: %empty  */
#line 3058 "parse.y"
                                             {p->ctxt.in_defined = 1;}
#line 9771 "parse.c"
    break;

  case 337: /* primary: "`defined?'" opt_nl '(' $@18 expr rparen  */
#line 3059 "parse.y"
                    {
			p->ctxt.in_defined = 0;
			(yyval.node) = new_defined(p, (yyvsp[-1].node), &(yyloc));
		    }
#line 9780 "parse.c"
    break;

  case 338: /* primary: "`not'" '(' expr rparen  */
#line 3064 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[-1].node), &(yylsp[-1])), METHOD_NOT, &(yylsp[-3]), &(yyloc));
		    }
#line 9788 "parse.c"
    break;

  case 339: /* primary: "`not'" '(' rparen  */
#line 3068 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, new_nil(&(yylsp[-1])), &(yylsp[-1])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 9796 "parse.c"
    break;

  case 340: /* primary: fcall brace_block  */
#line 3072 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = method_add_block(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(method_add_arg!(fcall!($1), args_new!), $2) %*/
		    }
#line 9807 "parse.c"
    break;

  case 342: /* primary: method_call brace_block  */
#line 3080 "parse.y"
                    {
		    /*%%%*/
			block_dup_check(p, (yyvsp[-1].node)->nd_args, (yyvsp[0].node));
			(yyval.node) = method_add_block(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!($1, $2) %*/
		    }
#line 9819 "parse.c"
    break;

  case 344: /* primary: k_if expr_value then compstmt if_tail k_end  */
#line 3092 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[-4].node), (yyvsp[-2].node), (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: if!($2, $4, escape_Qundef($5)) %*/
		    }
#line 9831 "parse.c"
    break;

  case 345: /* primary: k_unless expr_value then compstmt opt_else k_end  */
#line 3103 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_unless(p, (yyvsp[-4].node), (yyvsp[-2].node), (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: unless!($2, $4, escape_Qundef($5)) %*/
		    }
#line 9843 "parse.c"
    break;

  case 346: /* primary: k_while expr_value_do compstmt k_end  */
#line 3113 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_WHILE(cond(p, (yyvsp[-2].node), &(yylsp[-2])), (yyvsp[-1].node), 1, &(yyloc));
			fixpos((yyval.node), (yyvsp[-2].node));
		    /*% %*/
		    /*% ripper: while!($2, $3) %*/
		    }
#line 9855 "parse.c"
    break;

  case 347: /* primary: k_until expr_value_do compstmt k_end  */
#line 3123 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_UNTIL(cond(p, (yyvsp[-2].node), &(yylsp[-2])), (yyvsp[-1].node), 1, &(yyloc));
			fixpos((yyval.node), (yyvsp[-2].node));
		    /*% %*/
		    /*% ripper: until!($2, $3) %*/
		    }
#line 9867 "parse.c"
    break;

  case 348: /* @19: %empty  */
#line 3131 "parse.y"
                    {
			(yyval.val) = p->case_labels;
			p->case_labels = Qnil;
		    }
#line 9876 "parse.c"
    break;

  case 349: /* primary: k_case expr_value opt_terms @19 case_body k_end  */
#line 3137 "parse.y"
                    {
			if (RTEST(p->case_labels)) rb_hash_clear(p->case_labels);
			p->case_labels = (yyvsp[-2].val);
		    /*%%%*/
			(yyval.node) = NEW_CASE((yyvsp[-4].node), (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: case!($2, $5) %*/
		    }
#line 9890 "parse.c"
    break;

  case 350: /* @20: %empty  */
#line 3147 "parse.y"
                    {
			(yyval.val) = p->case_labels;
			p->case_labels = 0;
		    }
#line 9899 "parse.c"
    break;

  case 351: /* primary: k_case opt_terms @20 case_body k_end  */
#line 3153 "parse.y"
                    {
			if (RTEST(p->case_labels)) rb_hash_clear(p->case_labels);
			p->case_labels = (yyvsp[-2].val);
		    /*%%%*/
			(yyval.node) = NEW_CASE2((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: case!(Qnil, $4) %*/
		    }
#line 9912 "parse.c"
    break;

  case 352: /* primary: k_case expr_value opt_terms p_case_body k_end  */
#line 3164 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_CASE3((yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: case!($2, $4) %*/
		    }
#line 9923 "parse.c"
    break;

  case 353: /* primary: k_for for_var "`in'" expr_value_do compstmt k_end  */
#line 3173 "parse.y"
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
                        rb_ast_id_table_t *tbl = rb_ast_new_local_table(p->ast, 1);
			tbl->ids[0] = id; /* internal id */

			switch (nd_type((yyvsp[-4].node))) {
			  case NODE_LASGN:
			  case NODE_DASGN: /* e.each {|internal_var| a = internal_var; ... } */
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
#line 9967 "parse.c"
    break;

  case 354: /* $@21: %empty  */
#line 3213 "parse.y"
                    {
			if (p->ctxt.in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[-1]));
			    yyerror1(&loc, "class definition in method body");
			}
			p->ctxt.in_class = 1;
			local_push(p, 0);
		    }
#line 9980 "parse.c"
    break;

  case 355: /* primary: k_class cpath superclass $@21 bodystmt k_end  */
#line 3223 "parse.y"
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
#line 9997 "parse.c"
    break;

  case 356: /* $@22: %empty  */
#line 3236 "parse.y"
                    {
			p->ctxt.in_def = 0;
			p->ctxt.in_class = 0;
			local_push(p, 0);
		    }
#line 10007 "parse.c"
    break;

  case 357: /* primary: k_class "<<" expr $@22 term bodystmt k_end  */
#line 3244 "parse.y"
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
#line 10025 "parse.c"
    break;

  case 358: /* $@23: %empty  */
#line 3258 "parse.y"
                    {
			if (p->ctxt.in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			    yyerror1(&loc, "module definition in method body");
			}
			p->ctxt.in_class = 1;
			local_push(p, 0);
		    }
#line 10038 "parse.c"
    break;

  case 359: /* primary: k_module cpath $@23 bodystmt k_end  */
#line 3268 "parse.y"
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
#line 10055 "parse.c"
    break;

  case 360: /* primary: defn_head f_arglist bodystmt k_end  */
#line 3284 "parse.y"
                    {
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-3].node), (yyvsp[-2].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: def!(get_value($1), $2, $3) %*/
			local_pop(p);
		    }
#line 10068 "parse.c"
    break;

  case 361: /* primary: defs_head f_arglist bodystmt k_end  */
#line 3296 "parse.y"
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
#line 10083 "parse.c"
    break;

  case 362: /* primary: "`break'"  */
#line 3307 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BREAK(0, &(yyloc));
		    /*% %*/
		    /*% ripper: break!(args_new!) %*/
		    }
#line 10094 "parse.c"
    break;

  case 363: /* primary: "`next'"  */
#line 3314 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_NEXT(0, &(yyloc));
		    /*% %*/
		    /*% ripper: next!(args_new!) %*/
		    }
#line 10105 "parse.c"
    break;

  case 364: /* primary: "`redo'"  */
#line 3321 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_REDO(&(yyloc));
		    /*% %*/
		    /*% ripper: redo! %*/
		    }
#line 10116 "parse.c"
    break;

  case 365: /* primary: "`retry'"  */
#line 3328 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RETRY(&(yyloc));
		    /*% %*/
		    /*% ripper: retry! %*/
		    }
#line 10127 "parse.c"
    break;

  case 366: /* primary_value: primary  */
#line 3337 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 10136 "parse.c"
    break;

  case 367: /* k_begin: "`begin'"  */
#line 3344 "parse.y"
                    {
			token_info_push(p, "begin", &(yyloc));
		    }
#line 10144 "parse.c"
    break;

  case 368: /* k_if: "`if'"  */
#line 3350 "parse.y"
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
#line 10163 "parse.c"
    break;

  case 369: /* k_unless: "`unless'"  */
#line 3367 "parse.y"
                    {
			token_info_push(p, "unless", &(yyloc));
		    }
#line 10171 "parse.c"
    break;

  case 370: /* k_while: "`while'"  */
#line 3373 "parse.y"
                    {
			token_info_push(p, "while", &(yyloc));
		    }
#line 10179 "parse.c"
    break;

  case 371: /* k_until: "`until'"  */
#line 3379 "parse.y"
                    {
			token_info_push(p, "until", &(yyloc));
		    }
#line 10187 "parse.c"
    break;

  case 372: /* k_case: "`case'"  */
#line 3385 "parse.y"
                    {
			token_info_push(p, "case", &(yyloc));
		    }
#line 10195 "parse.c"
    break;

  case 373: /* k_for: "`for'"  */
#line 3391 "parse.y"
                    {
			token_info_push(p, "for", &(yyloc));
		    }
#line 10203 "parse.c"
    break;

  case 374: /* k_class: "`class'"  */
#line 3397 "parse.y"
                    {
			token_info_push(p, "class", &(yyloc));
			(yyval.ctxt) = p->ctxt;
		    }
#line 10212 "parse.c"
    break;

  case 375: /* k_module: "`module'"  */
#line 3404 "parse.y"
                    {
			token_info_push(p, "module", &(yyloc));
			(yyval.ctxt) = p->ctxt;
		    }
#line 10221 "parse.c"
    break;

  case 376: /* k_def: "`def'"  */
#line 3411 "parse.y"
                    {
			token_info_push(p, "def", &(yyloc));
			p->ctxt.in_argdef = 1;
		    }
#line 10230 "parse.c"
    break;

  case 377: /* k_do: "`do'"  */
#line 3418 "parse.y"
                    {
			token_info_push(p, "do", &(yyloc));
		    }
#line 10238 "parse.c"
    break;

  case 378: /* k_do_block: "`do' for block"  */
#line 3424 "parse.y"
                    {
			token_info_push(p, "do", &(yyloc));
		    }
#line 10246 "parse.c"
    break;

  case 379: /* k_rescue: "`rescue'"  */
#line 3430 "parse.y"
                    {
			token_info_warn(p, "rescue", p->token_info, 1, &(yyloc));
		    }
#line 10254 "parse.c"
    break;

  case 380: /* k_ensure: "`ensure'"  */
#line 3436 "parse.y"
                    {
			token_info_warn(p, "ensure", p->token_info, 1, &(yyloc));
		    }
#line 10262 "parse.c"
    break;

  case 381: /* k_when: "`when'"  */
#line 3442 "parse.y"
                    {
			token_info_warn(p, "when", p->token_info, 0, &(yyloc));
		    }
#line 10270 "parse.c"
    break;

  case 382: /* k_else: "`else'"  */
#line 3448 "parse.y"
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
#line 10287 "parse.c"
    break;

  case 383: /* k_elsif: "`elsif'"  */
#line 3463 "parse.y"
                    {
			WARN_EOL("elsif");
			token_info_warn(p, "elsif", p->token_info, 1, &(yyloc));
		    }
#line 10296 "parse.c"
    break;

  case 384: /* k_end: "`end'"  */
#line 3470 "parse.y"
                    {
			token_info_pop(p, "end", &(yyloc));
		    }
#line 10304 "parse.c"
    break;

  case 385: /* k_return: "`return'"  */
#line 3476 "parse.y"
                    {
			if (p->ctxt.in_class && !p->ctxt.in_def && !dyna_in_block(p))
			    yyerror1(&(yylsp[0]), "Invalid return in class/module body");
		    }
#line 10313 "parse.c"
    break;

  case 392: /* if_tail: k_elsif expr_value then compstmt if_tail  */
#line 3495 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[-3].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-3].node));
		    /*% %*/
		    /*% ripper: elsif!($2, $4, escape_Qundef($5)) %*/
		    }
#line 10325 "parse.c"
    break;

  case 394: /* opt_else: k_else compstmt  */
#line 3506 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: else!($2) %*/
		    }
#line 10336 "parse.c"
    break;

  case 397: /* f_marg: f_norm_arg  */
#line 3519 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
			mark_lvar_used(p, (yyval.node));
		    /*% %*/
		    /*% ripper: assignable(p, $1) %*/
		    }
#line 10348 "parse.c"
    break;

  case 398: /* f_marg: "(" f_margs rparen  */
#line 3527 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 10359 "parse.c"
    break;

  case 399: /* f_marg_list: f_marg  */
#line 3536 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add!(mlhs_new!, $1) %*/
		    }
#line 10370 "parse.c"
    break;

  case 400: /* f_marg_list: f_marg_list ',' f_marg  */
#line 3543 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $3) %*/
		    }
#line 10381 "parse.c"
    break;

  case 401: /* f_margs: f_marg_list  */
#line 3552 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[0].node), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 10392 "parse.c"
    break;

  case 402: /* f_margs: f_marg_list ',' f_rest_marg  */
#line 3559 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!($1, $3) %*/
		    }
#line 10403 "parse.c"
    break;

  case 403: /* f_margs: f_marg_list ',' f_rest_marg ',' f_marg_list  */
#line 3566 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-4].node), NEW_POSTARG((yyvsp[-2].node), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!($1, $3), $5) %*/
		    }
#line 10414 "parse.c"
    break;

  case 404: /* f_margs: f_rest_marg  */
#line 3573 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!(mlhs_new!, $1) %*/
		    }
#line 10425 "parse.c"
    break;

  case 405: /* f_margs: f_rest_marg ',' f_marg_list  */
#line 3580 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NEW_POSTARG((yyvsp[-2].node), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!(mlhs_new!, $1), $3) %*/
		    }
#line 10436 "parse.c"
    break;

  case 406: /* f_rest_marg: "*" f_norm_arg  */
#line 3589 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
			mark_lvar_used(p, (yyval.node));
		    /*% %*/
		    /*% ripper: assignable(p, $2) %*/
		    }
#line 10448 "parse.c"
    break;

  case 407: /* f_rest_marg: "*"  */
#line 3597 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NODE_SPECIAL_NO_NAME_REST;
		    /*% %*/
		    /*% ripper: Qnil %*/
		    }
#line 10459 "parse.c"
    break;

  case 409: /* f_any_kwrest: f_no_kwarg  */
#line 3606 "parse.y"
                             {(yyval.id) = ID2VAL(idNil);}
#line 10465 "parse.c"
    break;

  case 410: /* $@24: %empty  */
#line 3609 "parse.y"
                  {p->ctxt.in_argdef = 0;}
#line 10471 "parse.c"
    break;

  case 412: /* block_args_tail: f_block_kwarg ',' f_kwrest opt_f_block_arg  */
#line 3612 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 10479 "parse.c"
    break;

  case 413: /* block_args_tail: f_block_kwarg opt_f_block_arg  */
#line 3616 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-1].node), Qnone, (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 10487 "parse.c"
    break;

  case 414: /* block_args_tail: f_any_kwrest opt_f_block_arg  */
#line 3620 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 10495 "parse.c"
    break;

  case 415: /* block_args_tail: f_block_arg  */
#line 3624 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].id), &(yylsp[0]));
		    }
#line 10503 "parse.c"
    break;

  case 416: /* opt_block_args_tail: ',' block_args_tail  */
#line 3630 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 10511 "parse.c"
    break;

  case 417: /* opt_block_args_tail: %empty  */
#line 3634 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 10519 "parse.c"
    break;

  case 418: /* excessed_comma: ','  */
#line 3640 "parse.y"
                    {
			/* magic number for rest_id in iseq_set_arguments() */
		    /*%%%*/
			(yyval.id) = NODE_SPECIAL_EXCESSIVE_COMMA;
		    /*% %*/
		    /*% ripper: excessed_comma! %*/
		    }
#line 10531 "parse.c"
    break;

  case 419: /* block_param: f_arg ',' f_block_optarg ',' f_rest_arg opt_block_args_tail  */
#line 3650 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10539 "parse.c"
    break;

  case 420: /* block_param: f_arg ',' f_block_optarg ',' f_rest_arg ',' f_arg opt_block_args_tail  */
#line 3654 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-7].node), (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 10547 "parse.c"
    break;

  case 421: /* block_param: f_arg ',' f_block_optarg opt_block_args_tail  */
#line 3658 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10555 "parse.c"
    break;

  case 422: /* block_param: f_arg ',' f_block_optarg ',' f_arg opt_block_args_tail  */
#line 3662 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 10563 "parse.c"
    break;

  case 423: /* block_param: f_arg ',' f_rest_arg opt_block_args_tail  */
#line 3666 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10571 "parse.c"
    break;

  case 424: /* block_param: f_arg excessed_comma  */
#line 3670 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
			(yyval.node) = new_args(p, (yyvsp[-1].node), Qnone, (yyvsp[0].id), Qnone, (yyval.node), &(yyloc));
		    }
#line 10580 "parse.c"
    break;

  case 425: /* block_param: f_arg ',' f_rest_arg ',' f_arg opt_block_args_tail  */
#line 3675 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 10588 "parse.c"
    break;

  case 426: /* block_param: f_arg opt_block_args_tail  */
#line 3679 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-1].node), Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10596 "parse.c"
    break;

  case 427: /* block_param: f_block_optarg ',' f_rest_arg opt_block_args_tail  */
#line 3683 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10604 "parse.c"
    break;

  case 428: /* block_param: f_block_optarg ',' f_rest_arg ',' f_arg opt_block_args_tail  */
#line 3687 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 10612 "parse.c"
    break;

  case 429: /* block_param: f_block_optarg opt_block_args_tail  */
#line 3691 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10620 "parse.c"
    break;

  case 430: /* block_param: f_block_optarg ',' f_arg opt_block_args_tail  */
#line 3695 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 10628 "parse.c"
    break;

  case 431: /* block_param: f_rest_arg opt_block_args_tail  */
#line 3699 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10636 "parse.c"
    break;

  case 432: /* block_param: f_rest_arg ',' f_arg opt_block_args_tail  */
#line 3703 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 10644 "parse.c"
    break;

  case 433: /* block_param: block_args_tail  */
#line 3707 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 10652 "parse.c"
    break;

  case 435: /* opt_block_param: block_param_def  */
#line 3714 "parse.y"
                    {
			p->command_start = TRUE;
		    }
#line 10660 "parse.c"
    break;

  case 436: /* block_param_def: '|' opt_bv_decl '|'  */
#line 3720 "parse.y"
                    {
			p->cur_arg = 0;
			p->max_numparam = ORDINAL_PARAM;
			p->ctxt.in_argdef = 0;
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: block_var!(params!(Qnil,Qnil,Qnil,Qnil,Qnil,Qnil,Qnil), escape_Qundef($2)) %*/
		    }
#line 10674 "parse.c"
    break;

  case 437: /* block_param_def: '|' block_param opt_bv_decl '|'  */
#line 3730 "parse.y"
                    {
			p->cur_arg = 0;
			p->max_numparam = ORDINAL_PARAM;
			p->ctxt.in_argdef = 0;
		    /*%%%*/
			(yyval.node) = (yyvsp[-2].node);
		    /*% %*/
		    /*% ripper: block_var!(escape_Qundef($2), escape_Qundef($3)) %*/
		    }
#line 10688 "parse.c"
    break;

  case 438: /* opt_bv_decl: opt_nl  */
#line 3743 "parse.y"
                    {
			(yyval.node) = 0;
		    }
#line 10696 "parse.c"
    break;

  case 439: /* opt_bv_decl: opt_nl ';' bv_decls opt_nl  */
#line 3747 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: $3 %*/
		    }
#line 10707 "parse.c"
    break;

  case 442: /* bvar: "local variable or method"  */
#line 3762 "parse.y"
                    {
			new_bv(p, get_id((yyvsp[0].id)));
		    /*% ripper: get_value($1) %*/
		    }
#line 10716 "parse.c"
    break;

  case 443: /* bvar: f_bad_arg  */
#line 3767 "parse.y"
                    {
			(yyval.node) = 0;
		    }
#line 10724 "parse.c"
    break;

  case 444: /* @25: %empty  */
#line 3773 "parse.y"
                    {
			token_info_push(p, "->", &(yylsp[0]));
			(yyvsp[0].vars) = dyna_push(p);
			(yyval.num) = p->lex.lpar_beg;
			p->lex.lpar_beg = p->lex.paren_nest;
		    }
#line 10735 "parse.c"
    break;

  case 445: /* @26: %empty  */
#line 3779 "parse.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 10744 "parse.c"
    break;

  case 446: /* @27: %empty  */
#line 3783 "parse.y"
                    {
			(yyval.node) = numparam_push(p);
		    }
#line 10752 "parse.c"
    break;

  case 447: /* $@28: %empty  */
#line 3787 "parse.y"
                    {
			CMDARG_PUSH(0);
		    }
#line 10760 "parse.c"
    break;

  case 448: /* lambda: "->" @25 @26 @27 f_larglist $@28 lambda_body  */
#line 3791 "parse.y"
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
#line 10784 "parse.c"
    break;

  case 449: /* f_larglist: '(' f_args opt_bv_decl ')'  */
#line 3813 "parse.y"
                    {
			p->ctxt.in_argdef = 0;
		    /*%%%*/
			(yyval.node) = (yyvsp[-2].node);
			p->max_numparam = ORDINAL_PARAM;
		    /*% %*/
		    /*% ripper: paren!($2) %*/
		    }
#line 10797 "parse.c"
    break;

  case 450: /* f_larglist: f_args  */
#line 3822 "parse.y"
                    {
			p->ctxt.in_argdef = 0;
		    /*%%%*/
			if (!args_info_empty_p((yyvsp[0].node)->nd_ainfo))
			    p->max_numparam = ORDINAL_PARAM;
		    /*% %*/
			(yyval.node) = (yyvsp[0].node);
		    }
#line 10810 "parse.c"
    break;

  case 451: /* lambda_body: tLAMBEG compstmt '}'  */
#line 3833 "parse.y"
                    {
			token_info_pop(p, "}", &(yylsp[0]));
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 10819 "parse.c"
    break;

  case 452: /* lambda_body: "`do' for lambda" bodystmt k_end  */
#line 3838 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 10827 "parse.c"
    break;

  case 453: /* do_block: k_do_block do_body k_end  */
#line 3844 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 10839 "parse.c"
    break;

  case 454: /* block_call: command do_block  */
#line 3854 "parse.y"
                    {
		    /*%%%*/
			if (nd_type_p((yyvsp[-1].node), NODE_YIELD)) {
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
#line 10857 "parse.c"
    break;

  case 455: /* block_call: block_call call_op2 operation2 opt_paren_args  */
#line 3868 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: opt_event(:method_add_arg!, call!($1, $2, $3), $4) %*/
		    }
#line 10868 "parse.c"
    break;

  case 456: /* block_call: block_call call_op2 operation2 opt_paren_args brace_block  */
#line 3875 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-3].id), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: opt_event(:method_add_block!, command_call!($1, $2, $3, $4), $5) %*/
		    }
#line 10879 "parse.c"
    break;

  case 457: /* block_call: block_call call_op2 operation2 command_args do_block  */
#line 3882 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-3].id), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(command_call!($1, $2, $3, $4), $5) %*/
		    }
#line 10890 "parse.c"
    break;

  case 458: /* method_call: fcall paren_args  */
#line 3891 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
			(yyval.node)->nd_args = (yyvsp[0].node);
			nd_set_last_loc((yyvsp[-1].node), (yylsp[0]).end_pos);
		    /*% %*/
		    /*% ripper: method_add_arg!(fcall!($1), $2) %*/
		    }
#line 10903 "parse.c"
    break;

  case 459: /* method_call: primary_value call_op operation2 opt_paren_args  */
#line 3900 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: opt_event(:method_add_arg!, call!($1, $2, $3), $4) %*/
		    }
#line 10915 "parse.c"
    break;

  case 460: /* method_call: primary_value "::" operation2 paren_args  */
#line 3908 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, ID2VAL(idCOLON2), $3), $4) %*/
		    }
#line 10927 "parse.c"
    break;

  case 461: /* method_call: primary_value "::" operation3  */
#line 3916 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].node), (yyvsp[0].id), Qnull, &(yylsp[0]), &(yyloc));
		    /*% %*/
		    /*% ripper: call!($1, ID2VAL(idCOLON2), $3) %*/
		    }
#line 10938 "parse.c"
    break;

  case 462: /* method_call: primary_value call_op paren_args  */
#line 3923 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-1].id), (yyvsp[-2].node), ID2VAL(idCall), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, $2, ID2VAL(idCall)), $3) %*/
		    }
#line 10950 "parse.c"
    break;

  case 463: /* method_call: primary_value "::" paren_args  */
#line 3931 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].node), ID2VAL(idCall), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, ID2VAL(idCOLON2), ID2VAL(idCall)), $3) %*/
		    }
#line 10962 "parse.c"
    break;

  case 464: /* method_call: "`super'" paren_args  */
#line 3939 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SUPER((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: super!($2) %*/
		    }
#line 10973 "parse.c"
    break;

  case 465: /* method_call: "`super'"  */
#line 3946 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_ZSUPER(&(yyloc));
		    /*% %*/
		    /*% ripper: zsuper! %*/
		    }
#line 10984 "parse.c"
    break;

  case 466: /* method_call: primary_value '[' opt_call_args rbracket  */
#line 3953 "parse.y"
                    {
		    /*%%%*/
			if ((yyvsp[-3].node) && nd_type_p((yyvsp[-3].node), NODE_SELF))
			    (yyval.node) = NEW_FCALL(tAREF, (yyvsp[-1].node), &(yyloc));
			else
			    (yyval.node) = NEW_CALL((yyvsp[-3].node), tAREF, (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-3].node));
		    /*% %*/
		    /*% ripper: aref!($1, escape_Qundef($3)) %*/
		    }
#line 10999 "parse.c"
    break;

  case 467: /* brace_block: '{' brace_body '}'  */
#line 3966 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 11011 "parse.c"
    break;

  case 468: /* brace_block: k_do do_body k_end  */
#line 3974 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 11023 "parse.c"
    break;

  case 469: /* @29: %empty  */
#line 3983 "parse.y"
                  {(yyval.vars) = dyna_push(p);}
#line 11029 "parse.c"
    break;

  case 470: /* @30: %empty  */
#line 3984 "parse.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 11038 "parse.c"
    break;

  case 471: /* @31: %empty  */
#line 3988 "parse.y"
                    {
			(yyval.node) = numparam_push(p);
		    }
#line 11046 "parse.c"
    break;

  case 472: /* brace_body: @29 @30 @31 opt_block_param compstmt  */
#line 3992 "parse.y"
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
#line 11062 "parse.c"
    break;

  case 473: /* @32: %empty  */
#line 4005 "parse.y"
                  {(yyval.vars) = dyna_push(p);}
#line 11068 "parse.c"
    break;

  case 474: /* @33: %empty  */
#line 4006 "parse.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 11077 "parse.c"
    break;

  case 475: /* @34: %empty  */
#line 4010 "parse.y"
                    {
			(yyval.node) = numparam_push(p);
			CMDARG_PUSH(0);
		    }
#line 11086 "parse.c"
    break;

  case 476: /* do_body: @32 @33 @34 opt_block_param bodystmt  */
#line 4015 "parse.y"
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
#line 11103 "parse.c"
    break;

  case 477: /* case_args: arg_value  */
#line 4030 "parse.y"
                    {
		    /*%%%*/
			check_literal_when(p, (yyvsp[0].node), &(yylsp[0]));
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!(args_new!, $1) %*/
		    }
#line 11115 "parse.c"
    break;

  case 478: /* case_args: "*" arg_value  */
#line 4038 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SPLAT((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!(args_new!, $2) %*/
		    }
#line 11126 "parse.c"
    break;

  case 479: /* case_args: case_args ',' arg_value  */
#line 4045 "parse.y"
                    {
		    /*%%%*/
			check_literal_when(p, (yyvsp[0].node), &(yylsp[0]));
			(yyval.node) = last_arg_append(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!($1, $3) %*/
		    }
#line 11138 "parse.c"
    break;

  case 480: /* case_args: case_args ',' "*" arg_value  */
#line 4053 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!($1, $4) %*/
		    }
#line 11149 "parse.c"
    break;

  case 481: /* case_body: k_when case_args then compstmt cases  */
#line 4064 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_WHEN((yyvsp[-3].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-3].node));
		    /*% %*/
		    /*% ripper: when!($2, $4, escape_Qundef($5)) %*/
		    }
#line 11161 "parse.c"
    break;

  case 484: /* @35: %empty  */
#line 4078 "parse.y"
                    {
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 11173 "parse.c"
    break;

  case 485: /* @36: %empty  */
#line 4085 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
		    }
#line 11181 "parse.c"
    break;

  case 486: /* $@37: %empty  */
#line 4089 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			pop_pvtbl(p, (yyvsp[-3].tbl));
			p->ctxt.in_kwarg = (yyvsp[-4].ctxt).in_kwarg;
		    }
#line 11191 "parse.c"
    break;

  case 487: /* p_case_body: "`in'" @35 @36 p_top_expr then $@37 compstmt p_cases  */
#line 4096 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_IN((yyvsp[-4].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: in!($4, $7, escape_Qundef($8)) %*/
		    }
#line 11202 "parse.c"
    break;

  case 491: /* p_top_expr: p_top_expr_body "`if' modifier" expr_value  */
#line 4110 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[0].node), (yyvsp[-2].node), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: if_mod!($3, $1) %*/
		    }
#line 11214 "parse.c"
    break;

  case 492: /* p_top_expr: p_top_expr_body "`unless' modifier" expr_value  */
#line 4118 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_unless(p, (yyvsp[0].node), (yyvsp[-2].node), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: unless_mod!($3, $1) %*/
		    }
#line 11226 "parse.c"
    break;

  case 494: /* p_top_expr_body: p_expr ','  */
#line 4129 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 1, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, Qnone, get_value((yyvsp[-1].node)), (yyval.node), &(yyloc));
		    }
#line 11235 "parse.c"
    break;

  case 495: /* p_top_expr_body: p_expr ',' p_args  */
#line 4134 "parse.y"
                    {
			(yyval.node) = new_array_pattern(p, Qnone, get_value((yyvsp[-2].node)), (yyvsp[0].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-2]).beg_pos);
		    /*%
		    %*/
		    }
#line 11247 "parse.c"
    break;

  case 496: /* p_top_expr_body: p_find  */
#line 4142 "parse.y"
                    {
			(yyval.node) = new_find_pattern(p, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11255 "parse.c"
    break;

  case 497: /* p_top_expr_body: p_args_tail  */
#line 4146 "parse.y"
                    {
			(yyval.node) = new_array_pattern(p, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11263 "parse.c"
    break;

  case 498: /* p_top_expr_body: p_kwargs  */
#line 4150 "parse.y"
                    {
			(yyval.node) = new_hash_pattern(p, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 11271 "parse.c"
    break;

  case 500: /* p_as: p_expr "=>" p_variable  */
#line 4159 "parse.y"
                    {
		    /*%%%*/
			NODE *n = NEW_LIST((yyvsp[-2].node), &(yyloc));
			n = list_append(p, n, (yyvsp[0].node));
			(yyval.node) = new_hash(p, n, &(yyloc));
		    /*% %*/
		    /*% ripper: binary!($1, STATIC_ID2SYM((id_assoc)), $3) %*/
		    }
#line 11284 "parse.c"
    break;

  case 502: /* p_alt: p_alt '|' p_expr_basic  */
#line 4171 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_NODE(NODE_OR, (yyvsp[-2].node), (yyvsp[0].node), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: binary!($1, STATIC_ID2SYM(idOr), $3) %*/
		    }
#line 11295 "parse.c"
    break;

  case 504: /* p_lparen: '('  */
#line 4180 "parse.y"
                      {(yyval.tbl) = push_pktbl(p);}
#line 11301 "parse.c"
    break;

  case 505: /* p_lbracket: '['  */
#line 4181 "parse.y"
                      {(yyval.tbl) = push_pktbl(p);}
#line 11307 "parse.c"
    break;

  case 508: /* p_expr_basic: p_const p_lparen p_args rparen  */
#line 4186 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_array_pattern(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 11320 "parse.c"
    break;

  case 509: /* p_expr_basic: p_const p_lparen p_find rparen  */
#line 4195 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_find_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 11333 "parse.c"
    break;

  case 510: /* p_expr_basic: p_const p_lparen p_kwargs rparen  */
#line 4204 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_hash_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 11346 "parse.c"
    break;

  case 511: /* p_expr_basic: p_const '(' rparen  */
#line 4213 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, (yyvsp[-2].node), Qnone, (yyval.node), &(yyloc));
		    }
#line 11355 "parse.c"
    break;

  case 512: /* p_expr_basic: p_const p_lbracket p_args rbracket  */
#line 4218 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_array_pattern(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 11368 "parse.c"
    break;

  case 513: /* p_expr_basic: p_const p_lbracket p_find rbracket  */
#line 4227 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_find_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 11381 "parse.c"
    break;

  case 514: /* p_expr_basic: p_const p_lbracket p_kwargs rbracket  */
#line 4236 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_hash_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 11394 "parse.c"
    break;

  case 515: /* p_expr_basic: p_const '[' rbracket  */
#line 4245 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, (yyvsp[-2].node), Qnone, (yyval.node), &(yyloc));
		    }
#line 11403 "parse.c"
    break;

  case 516: /* p_expr_basic: "[" p_args rbracket  */
#line 4250 "parse.y"
                    {
			(yyval.node) = new_array_pattern(p, Qnone, Qnone, (yyvsp[-1].node), &(yyloc));
		    }
#line 11411 "parse.c"
    break;

  case 517: /* p_expr_basic: "[" p_find rbracket  */
#line 4254 "parse.y"
                    {
			(yyval.node) = new_find_pattern(p, Qnone, (yyvsp[-1].node), &(yyloc));
		    }
#line 11419 "parse.c"
    break;

  case 518: /* p_expr_basic: "[" rbracket  */
#line 4258 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, Qnone, Qnone, (yyval.node), &(yyloc));
		    }
#line 11428 "parse.c"
    break;

  case 519: /* @38: %empty  */
#line 4263 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 0;
		    }
#line 11438 "parse.c"
    break;

  case 520: /* p_expr_basic: "{" @38 p_kwargs rbrace  */
#line 4269 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
			(yyval.node) = new_hash_pattern(p, Qnone, (yyvsp[-1].node), &(yyloc));
		    }
#line 11448 "parse.c"
    break;

  case 521: /* p_expr_basic: "{" rbrace  */
#line 4275 "parse.y"
                    {
			(yyval.node) = new_hash_pattern_tail(p, Qnone, 0, &(yyloc));
			(yyval.node) = new_hash_pattern(p, Qnone, (yyval.node), &(yyloc));
		    }
#line 11457 "parse.c"
    break;

  case 522: /* @39: %empty  */
#line 4279 "parse.y"
                          {(yyval.tbl) = push_pktbl(p);}
#line 11463 "parse.c"
    break;

  case 523: /* p_expr_basic: "(" @39 p_expr rparen  */
#line 4280 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 11472 "parse.c"
    break;

  case 524: /* p_args: p_expr  */
#line 4287 "parse.y"
                    {
		    /*%%%*/
			NODE *pre_args = NEW_LIST((yyvsp[0].node), &(yyloc));
			(yyval.node) = new_array_pattern_tail(p, pre_args, 0, 0, Qnone, &(yyloc));
		    /*%
			$$ = new_array_pattern_tail(p, rb_ary_new_from_args(1, get_value($1)), 0, 0, Qnone, &@$);
		    %*/
		    }
#line 11485 "parse.c"
    break;

  case 525: /* p_args: p_args_head  */
#line 4296 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[0].node), 1, 0, Qnone, &(yyloc));
		    }
#line 11493 "parse.c"
    break;

  case 526: /* p_args: p_args_head p_arg  */
#line 4300 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_array_pattern_tail(p, list_concat((yyvsp[-1].node), (yyvsp[0].node)), 0, 0, Qnone, &(yyloc));
		    /*%
			VALUE pre_args = rb_ary_concat($1, get_value($2));
			$$ = new_array_pattern_tail(p, pre_args, 0, 0, Qnone, &@$);
		    %*/
		    }
#line 11506 "parse.c"
    break;

  case 527: /* p_args: p_args_head "*" "local variable or method"  */
#line 4309 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-2].node), 1, (yyvsp[0].id), Qnone, &(yyloc));
		    }
#line 11514 "parse.c"
    break;

  case 528: /* p_args: p_args_head "*" "local variable or method" ',' p_args_post  */
#line 4313 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-4].node), 1, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    }
#line 11522 "parse.c"
    break;

  case 529: /* p_args: p_args_head "*"  */
#line 4317 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-1].node), 1, 0, Qnone, &(yyloc));
		    }
#line 11530 "parse.c"
    break;

  case 530: /* p_args: p_args_head "*" ',' p_args_post  */
#line 4321 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-3].node), 1, 0, (yyvsp[0].node), &(yyloc));
		    }
#line 11538 "parse.c"
    break;

  case 532: /* p_args_head: p_arg ','  */
#line 4328 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 11546 "parse.c"
    break;

  case 533: /* p_args_head: p_args_head p_arg ','  */
#line 4332 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_concat((yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: rb_ary_concat($1, get_value($2)) %*/
		    }
#line 11557 "parse.c"
    break;

  case 534: /* p_args_tail: p_rest  */
#line 4341 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 1, (yyvsp[0].id), Qnone, &(yyloc));
		    }
#line 11565 "parse.c"
    break;

  case 535: /* p_args_tail: p_rest ',' p_args_post  */
#line 4345 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 1, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    }
#line 11573 "parse.c"
    break;

  case 536: /* p_find: p_rest ',' p_args_post ',' p_rest  */
#line 4351 "parse.y"
                    {
			(yyval.node) = new_find_pattern_tail(p, (yyvsp[-4].id), (yyvsp[-2].node), (yyvsp[0].id), &(yyloc));

			if (rb_warning_category_enabled_p(RB_WARN_CATEGORY_EXPERIMENTAL))
			    rb_warn0L_experimental(nd_line((yyval.node)), "Find pattern is experimental, and the behavior may change in future versions of Ruby!");
		    }
#line 11584 "parse.c"
    break;

  case 537: /* p_rest: "*" "local variable or method"  */
#line 4361 "parse.y"
                    {
			(yyval.id) = (yyvsp[0].id);
		    }
#line 11592 "parse.c"
    break;

  case 538: /* p_rest: "*"  */
#line 4365 "parse.y"
                    {
			(yyval.id) = 0;
		    }
#line 11600 "parse.c"
    break;

  case 540: /* p_args_post: p_args_post ',' p_arg  */
#line 4372 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_concat((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_concat($1, get_value($3)) %*/
		    }
#line 11611 "parse.c"
    break;

  case 541: /* p_arg: p_expr  */
#line 4381 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_ary_new_from_args(1, get_value($1)) %*/
		    }
#line 11622 "parse.c"
    break;

  case 542: /* p_kwargs: p_kwarg ',' p_any_kwrest  */
#line 4390 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[-2].node), &(yyloc)), (yyvsp[0].id), &(yyloc));
		    }
#line 11630 "parse.c"
    break;

  case 543: /* p_kwargs: p_kwarg  */
#line 4394 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[0].node), &(yyloc)), 0, &(yyloc));
		    }
#line 11638 "parse.c"
    break;

  case 544: /* p_kwargs: p_kwarg ','  */
#line 4398 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[-1].node), &(yyloc)), 0, &(yyloc));
		    }
#line 11646 "parse.c"
    break;

  case 545: /* p_kwargs: p_any_kwrest  */
#line 4402 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_hash(p, Qnone, &(yyloc)), (yyvsp[0].id), &(yyloc));
		    }
#line 11654 "parse.c"
    break;

  case 547: /* p_kwarg: p_kwarg ',' p_kw  */
#line 4410 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_concat((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, $3) %*/
		    }
#line 11665 "parse.c"
    break;

  case 548: /* p_kw: p_kw_label p_expr  */
#line 4419 "parse.y"
                    {
			error_duplicate_pattern_key(p, get_id((yyvsp[-1].id)), &(yylsp[-1]));
		    /*%%%*/
			(yyval.node) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[-1].id)), &(yyloc)), &(yyloc)), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_new_from_args(2, get_value($1), get_value($2)) %*/
		    }
#line 11677 "parse.c"
    break;

  case 549: /* p_kw: p_kw_label  */
#line 4427 "parse.y"
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
#line 11693 "parse.c"
    break;

  case 551: /* p_kw_label: "string literal" string_contents tLABEL_END  */
#line 4442 "parse.y"
                    {
			YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
		    /*%%%*/
			if (!(yyvsp[-1].node) || nd_type_p((yyvsp[-1].node), NODE_STR)) {
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
#line 11718 "parse.c"
    break;

  case 552: /* p_kwrest: kwrest_mark "local variable or method"  */
#line 4465 "parse.y"
                    {
		        (yyval.id) = (yyvsp[0].id);
		    }
#line 11726 "parse.c"
    break;

  case 553: /* p_kwrest: kwrest_mark  */
#line 4469 "parse.y"
                    {
		        (yyval.id) = 0;
		    }
#line 11734 "parse.c"
    break;

  case 554: /* p_kwnorest: kwrest_mark "`nil'"  */
#line 4475 "parse.y"
                    {
		        (yyval.id) = 0;
		    }
#line 11742 "parse.c"
    break;

  case 556: /* p_any_kwrest: p_kwnorest  */
#line 4481 "parse.y"
                             {(yyval.id) = ID2VAL(idNil);}
#line 11748 "parse.c"
    break;

  case 558: /* p_value: p_primitive ".." p_primitive  */
#line 4486 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, $3) %*/
		    }
#line 11761 "parse.c"
    break;

  case 559: /* p_value: p_primitive "..." p_primitive  */
#line 4495 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, $3) %*/
		    }
#line 11774 "parse.c"
    break;

  case 560: /* p_value: p_primitive ".."  */
#line 4504 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT2((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, Qnil) %*/
		    }
#line 11786 "parse.c"
    break;

  case 561: /* p_value: p_primitive "..."  */
#line 4512 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT3((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, Qnil) %*/
		    }
#line 11798 "parse.c"
    break;

  case 565: /* p_value: "(.." p_primitive  */
#line 4523 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!(Qnil, $2) %*/
		    }
#line 11810 "parse.c"
    break;

  case 566: /* p_value: "(..." p_primitive  */
#line 4531 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!(Qnil, $2) %*/
		    }
#line 11822 "parse.c"
    break;

  case 575: /* p_primitive: keyword_variable  */
#line 4549 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc)))) (yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 11833 "parse.c"
    break;

  case 577: /* p_variable: "local variable or method"  */
#line 4559 "parse.y"
                    {
		    /*%%%*/
			error_duplicate_pattern_variable(p, (yyvsp[0].id), &(yylsp[0]));
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 11845 "parse.c"
    break;

  case 578: /* p_var_ref: '^' "local variable or method"  */
#line 4569 "parse.y"
                    {
		    /*%%%*/
			NODE *n = gettable(p, (yyvsp[0].id), &(yyloc));
			if (!(nd_type_p(n, NODE_LVAR) || nd_type_p(n, NODE_DVAR))) {
			    compile_error(p, "%"PRIsVALUE": no such local variable", rb_id2str((yyvsp[0].id)));
			}
			(yyval.node) = n;
		    /*% %*/
		    /*% ripper: var_ref!($2) %*/
		    }
#line 11860 "parse.c"
    break;

  case 579: /* p_var_ref: '^' nonlocal_var  */
#line 4580 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc)))) (yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($2) %*/
                    }
#line 11871 "parse.c"
    break;

  case 580: /* p_expr_ref: '^' "(" expr_value ')'  */
#line 4589 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: begin!($3) %*/
		    }
#line 11882 "parse.c"
    break;

  case 581: /* p_const: ":: at EXPR_BEG" cname  */
#line 4598 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON3((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: top_const_ref!($2) %*/
		    }
#line 11893 "parse.c"
    break;

  case 582: /* p_const: p_const "::" cname  */
#line 4605 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_ref!($1, $3) %*/
		    }
#line 11904 "parse.c"
    break;

  case 583: /* p_const: "constant"  */
#line 4612 "parse.y"
                   {
		    /*%%%*/
			(yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		   }
#line 11915 "parse.c"
    break;

  case 584: /* opt_rescue: k_rescue exc_list exc_var then compstmt opt_rescue  */
#line 4623 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RESBODY((yyvsp[-4].node),
					 (yyvsp[-3].node) ? block_append(p, node_assign(p, (yyvsp[-3].node), NEW_ERRINFO(&(yylsp[-3])), NO_LEX_CTXT, &(yylsp[-3])), (yyvsp[-1].node)) : (yyvsp[-1].node),
					 (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node)?(yyvsp[-4].node):(yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: rescue!(escape_Qundef($2), escape_Qundef($3), escape_Qundef($5), escape_Qundef($6)) %*/
		    }
#line 11929 "parse.c"
    break;

  case 586: /* exc_list: arg_value  */
#line 4636 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 11940 "parse.c"
    break;

  case 587: /* exc_list: mrhs  */
#line 4643 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = splat_array((yyvsp[0].node)))) (yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 11951 "parse.c"
    break;

  case 589: /* exc_var: "=>" lhs  */
#line 4653 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 11959 "parse.c"
    break;

  case 591: /* opt_ensure: k_ensure compstmt  */
#line 4660 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: ensure!($2) %*/
		    }
#line 11970 "parse.c"
    break;

  case 595: /* strings: string  */
#line 4674 "parse.y"
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
#line 11989 "parse.c"
    break;

  case 598: /* string: string string1  */
#line 4693 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: string_concat!($1, $2) %*/
		    }
#line 12000 "parse.c"
    break;

  case 599: /* string1: "string literal" string_contents "terminator"  */
#line 4702 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = heredoc_dedent(p, (yyvsp[-1].node));
			if ((yyval.node)) nd_set_loc((yyval.node), &(yyloc));
		    /*% %*/
		    /*% ripper: string_literal!(heredoc_dedent(p, $2)) %*/
		    }
#line 12012 "parse.c"
    break;

  case 600: /* xstring: "backtick literal" xstring_contents "terminator"  */
#line 4712 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_xstring(p, heredoc_dedent(p, (yyvsp[-1].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: xstring_literal!(heredoc_dedent(p, $2)) %*/
		    }
#line 12023 "parse.c"
    break;

  case 601: /* regexp: "regexp literal" regexp_contents tREGEXP_END  */
#line 4721 "parse.y"
                    {
			(yyval.node) = new_regexp(p, (yyvsp[-1].node), (yyvsp[0].num), &(yyloc));
		    }
#line 12031 "parse.c"
    break;

  case 602: /* words: "word list" ' ' word_list "terminator"  */
#line 4727 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 12042 "parse.c"
    break;

  case 603: /* word_list: %empty  */
#line 4736 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: words_new! %*/
		    }
#line 12053 "parse.c"
    break;

  case 604: /* word_list: word_list word ' '  */
#line 4743 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), evstr2dstr(p, (yyvsp[-1].node)));
		    /*% %*/
		    /*% ripper: words_add!($1, $2) %*/
		    }
#line 12064 "parse.c"
    break;

  case 606: /* word: word string_content  */
#line 4754 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: word_add!($1, $2) %*/
		    }
#line 12075 "parse.c"
    break;

  case 607: /* symbols: "symbol list" ' ' symbol_list "terminator"  */
#line 4763 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 12086 "parse.c"
    break;

  case 608: /* symbol_list: %empty  */
#line 4772 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: symbols_new! %*/
		    }
#line 12097 "parse.c"
    break;

  case 609: /* symbol_list: symbol_list word ' '  */
#line 4779 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = symbol_append(p, (yyvsp[-2].node), evstr2dstr(p, (yyvsp[-1].node)));
		    /*% %*/
		    /*% ripper: symbols_add!($1, $2) %*/
		    }
#line 12108 "parse.c"
    break;

  case 610: /* qwords: "verbatim word list" ' ' qword_list "terminator"  */
#line 4788 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 12119 "parse.c"
    break;

  case 611: /* qsymbols: "verbatim symbol list" ' ' qsym_list "terminator"  */
#line 4797 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 12130 "parse.c"
    break;

  case 612: /* qword_list: %empty  */
#line 4806 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: qwords_new! %*/
		    }
#line 12141 "parse.c"
    break;

  case 613: /* qword_list: qword_list "literal content" ' '  */
#line 4813 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: qwords_add!($1, $2) %*/
		    }
#line 12152 "parse.c"
    break;

  case 614: /* qsym_list: %empty  */
#line 4822 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: qsymbols_new! %*/
		    }
#line 12163 "parse.c"
    break;

  case 615: /* qsym_list: qsym_list "literal content" ' '  */
#line 4829 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = symbol_append(p, (yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: qsymbols_add!($1, $2) %*/
		    }
#line 12174 "parse.c"
    break;

  case 616: /* string_contents: %empty  */
#line 4838 "parse.y"
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
#line 12189 "parse.c"
    break;

  case 617: /* string_contents: string_contents string_content  */
#line 4849 "parse.y"
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
#line 12209 "parse.c"
    break;

  case 618: /* xstring_contents: %empty  */
#line 4867 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: xstring_new! %*/
		    }
#line 12220 "parse.c"
    break;

  case 619: /* xstring_contents: xstring_contents string_content  */
#line 4874 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: xstring_add!($1, $2) %*/
		    }
#line 12231 "parse.c"
    break;

  case 620: /* regexp_contents: %empty  */
#line 4883 "parse.y"
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
#line 12246 "parse.c"
    break;

  case 621: /* regexp_contents: regexp_contents string_content  */
#line 4894 "parse.y"
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
#line 12289 "parse.c"
    break;

  case 623: /* @40: %empty  */
#line 4937 "parse.y"
                    {
			/* need to backup p->lex.strterm so that a string literal `%&foo,#$&,bar&` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 12300 "parse.c"
    break;

  case 624: /* string_content: tSTRING_DVAR @40 string_dvar  */
#line 4944 "parse.y"
                    {
			p->lex.strterm = (yyvsp[-1].strterm);
		    /*%%%*/
			(yyval.node) = NEW_EVSTR((yyvsp[0].node), &(yyloc));
			nd_set_line((yyval.node), (yylsp[0]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: string_dvar!($3) %*/
		    }
#line 12313 "parse.c"
    break;

  case 625: /* $@41: %empty  */
#line 4953 "parse.y"
                    {
			CMDARG_PUSH(0);
			COND_PUSH(0);
		    }
#line 12322 "parse.c"
    break;

  case 626: /* @42: %empty  */
#line 4957 "parse.y"
                    {
			/* need to backup p->lex.strterm so that a string literal `%!foo,#{ !0 },bar!` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
		    }
#line 12332 "parse.c"
    break;

  case 627: /* @43: %empty  */
#line 4962 "parse.y"
                    {
			(yyval.num) = p->lex.state;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 12341 "parse.c"
    break;

  case 628: /* @44: %empty  */
#line 4966 "parse.y"
                    {
			(yyval.num) = p->lex.brace_nest;
			p->lex.brace_nest = 0;
		    }
#line 12350 "parse.c"
    break;

  case 629: /* @45: %empty  */
#line 4970 "parse.y"
                    {
			(yyval.num) = p->heredoc_indent;
			p->heredoc_indent = 0;
		    }
#line 12359 "parse.c"
    break;

  case 630: /* string_content: tSTRING_DBEG $@41 @42 @43 @44 @45 compstmt "'}'"  */
#line 4975 "parse.y"
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
#line 12378 "parse.c"
    break;

  case 631: /* string_dvar: "global variable"  */
#line 4992 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_GVAR((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 12389 "parse.c"
    break;

  case 632: /* string_dvar: "instance variable"  */
#line 4999 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_IVAR((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 12400 "parse.c"
    break;

  case 633: /* string_dvar: "class variable"  */
#line 5006 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_CVAR((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 12411 "parse.c"
    break;

  case 637: /* ssym: "symbol literal" sym  */
#line 5020 "parse.y"
                    {
			SET_LEX_STATE(EXPR_END);
		    /*%%%*/
			(yyval.node) = NEW_LIT(ID2SYM((yyvsp[0].id)), &(yyloc));
		    /*% %*/
		    /*% ripper: symbol_literal!(symbol!($2)) %*/
		    }
#line 12423 "parse.c"
    break;

  case 642: /* dsym: "symbol literal" string_contents "terminator"  */
#line 5036 "parse.y"
                    {
			SET_LEX_STATE(EXPR_END);
		    /*%%%*/
			(yyval.node) = dsym_node(p, (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dyna_symbol!($2) %*/
		    }
#line 12435 "parse.c"
    break;

  case 644: /* numeric: tUMINUS_NUM simple_numeric  */
#line 5047 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
			RB_OBJ_WRITE(p->ast, &(yyval.node)->nd_lit, negate_lit(p, (yyval.node)->nd_lit));
		    /*% %*/
		    /*% ripper: unary!(ID2VAL(idUMinus), $2) %*/
		    }
#line 12447 "parse.c"
    break;

  case 657: /* keyword_variable: "`nil'"  */
#line 5074 "parse.y"
                              {(yyval.id) = KWD2EID(nil, (yyvsp[0].id));}
#line 12453 "parse.c"
    break;

  case 658: /* keyword_variable: "`self'"  */
#line 5075 "parse.y"
                               {(yyval.id) = KWD2EID(self, (yyvsp[0].id));}
#line 12459 "parse.c"
    break;

  case 659: /* keyword_variable: "`true'"  */
#line 5076 "parse.y"
                               {(yyval.id) = KWD2EID(true, (yyvsp[0].id));}
#line 12465 "parse.c"
    break;

  case 660: /* keyword_variable: "`false'"  */
#line 5077 "parse.y"
                                {(yyval.id) = KWD2EID(false, (yyvsp[0].id));}
#line 12471 "parse.c"
    break;

  case 661: /* keyword_variable: "`__FILE__'"  */
#line 5078 "parse.y"
                                  {(yyval.id) = KWD2EID(_FILE__, (yyvsp[0].id));}
#line 12477 "parse.c"
    break;

  case 662: /* keyword_variable: "`__LINE__'"  */
#line 5079 "parse.y"
                                  {(yyval.id) = KWD2EID(_LINE__, (yyvsp[0].id));}
#line 12483 "parse.c"
    break;

  case 663: /* keyword_variable: "`__ENCODING__'"  */
#line 5080 "parse.y"
                                      {(yyval.id) = KWD2EID(_ENCODING__, (yyvsp[0].id));}
#line 12489 "parse.c"
    break;

  case 664: /* var_ref: user_variable  */
#line 5084 "parse.y"
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
#line 12506 "parse.c"
    break;

  case 665: /* var_ref: keyword_variable  */
#line 5097 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc)))) (yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 12517 "parse.c"
    break;

  case 666: /* var_lhs: user_variable  */
#line 5106 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 12528 "parse.c"
    break;

  case 667: /* var_lhs: keyword_variable  */
#line 5113 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 12539 "parse.c"
    break;

  case 670: /* $@46: %empty  */
#line 5126 "parse.y"
                    {
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 12548 "parse.c"
    break;

  case 671: /* superclass: '<' $@46 expr_value term  */
#line 5131 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 12556 "parse.c"
    break;

  case 672: /* superclass: %empty  */
#line 5135 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: Qnil %*/
		    }
#line 12567 "parse.c"
    break;

  case 674: /* f_opt_paren_args: none  */
#line 5145 "parse.y"
                    {
			p->ctxt.in_argdef = 0;
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[-1]));
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyval.node), &(yylsp[-1]));
		    }
#line 12577 "parse.c"
    break;

  case 675: /* f_paren_args: '(' f_args rparen  */
#line 5153 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: paren!($2) %*/
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
			p->ctxt.in_argdef = 0;
		    }
#line 12591 "parse.c"
    break;

  case 677: /* @47: %empty  */
#line 5165 "parse.y"
                    {
			(yyval.ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			p->ctxt.in_argdef = 1;
			SET_LEX_STATE(p->lex.state|EXPR_LABEL); /* force for args */
		    }
#line 12602 "parse.c"
    break;

  case 678: /* f_arglist: @47 f_args term  */
#line 5172 "parse.y"
                    {
			p->ctxt.in_kwarg = (yyvsp[-2].ctxt).in_kwarg;
			p->ctxt.in_argdef = 0;
			(yyval.node) = (yyvsp[-1].node);
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 12614 "parse.c"
    break;

  case 679: /* args_tail: f_kwarg ',' f_kwrest opt_f_block_arg  */
#line 5182 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 12622 "parse.c"
    break;

  case 680: /* args_tail: f_kwarg opt_f_block_arg  */
#line 5186 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-1].node), Qnone, (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 12630 "parse.c"
    break;

  case 681: /* args_tail: f_any_kwrest opt_f_block_arg  */
#line 5190 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 12638 "parse.c"
    break;

  case 682: /* args_tail: f_block_arg  */
#line 5194 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].id), &(yylsp[0]));
		    }
#line 12646 "parse.c"
    break;

  case 683: /* args_tail: args_forward  */
#line 5198 "parse.y"
                    {
			add_forwarding_args(p);
			(yyval.node) = new_args_tail(p, Qnone, (yyvsp[0].id), ID2VAL(idFWD_BLOCK), &(yylsp[0]));
		    }
#line 12655 "parse.c"
    break;

  case 684: /* opt_args_tail: ',' args_tail  */
#line 5205 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 12663 "parse.c"
    break;

  case 685: /* opt_args_tail: %empty  */
#line 5209 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 12671 "parse.c"
    break;

  case 686: /* f_args: f_arg ',' f_optarg ',' f_rest_arg opt_args_tail  */
#line 5215 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12679 "parse.c"
    break;

  case 687: /* f_args: f_arg ',' f_optarg ',' f_rest_arg ',' f_arg opt_args_tail  */
#line 5219 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-7].node), (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12687 "parse.c"
    break;

  case 688: /* f_args: f_arg ',' f_optarg opt_args_tail  */
#line 5223 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12695 "parse.c"
    break;

  case 689: /* f_args: f_arg ',' f_optarg ',' f_arg opt_args_tail  */
#line 5227 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12703 "parse.c"
    break;

  case 690: /* f_args: f_arg ',' f_rest_arg opt_args_tail  */
#line 5231 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12711 "parse.c"
    break;

  case 691: /* f_args: f_arg ',' f_rest_arg ',' f_arg opt_args_tail  */
#line 5235 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12719 "parse.c"
    break;

  case 692: /* f_args: f_arg opt_args_tail  */
#line 5239 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-1].node), Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12727 "parse.c"
    break;

  case 693: /* f_args: f_optarg ',' f_rest_arg opt_args_tail  */
#line 5243 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12735 "parse.c"
    break;

  case 694: /* f_args: f_optarg ',' f_rest_arg ',' f_arg opt_args_tail  */
#line 5247 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12743 "parse.c"
    break;

  case 695: /* f_args: f_optarg opt_args_tail  */
#line 5251 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12751 "parse.c"
    break;

  case 696: /* f_args: f_optarg ',' f_arg opt_args_tail  */
#line 5255 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12759 "parse.c"
    break;

  case 697: /* f_args: f_rest_arg opt_args_tail  */
#line 5259 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12767 "parse.c"
    break;

  case 698: /* f_args: f_rest_arg ',' f_arg opt_args_tail  */
#line 5263 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12775 "parse.c"
    break;

  case 699: /* f_args: args_tail  */
#line 5267 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12783 "parse.c"
    break;

  case 700: /* f_args: %empty  */
#line 5271 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyval.node), &(yylsp[0]));
		    }
#line 12792 "parse.c"
    break;

  case 701: /* args_forward: "(..."  */
#line 5278 "parse.y"
                    {
		    /*%%%*/
			(yyval.id) = idFWD_KWREST;
		    /*% %*/
		    /*% ripper: args_forward! %*/
		    }
#line 12803 "parse.c"
    break;

  case 702: /* f_bad_arg: "constant"  */
#line 5287 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be a constant";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 12816 "parse.c"
    break;

  case 703: /* f_bad_arg: "instance variable"  */
#line 5296 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be an instance variable";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 12829 "parse.c"
    break;

  case 704: /* f_bad_arg: "global variable"  */
#line 5305 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be a global variable";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 12842 "parse.c"
    break;

  case 705: /* f_bad_arg: "class variable"  */
#line 5314 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be a class variable";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 12855 "parse.c"
    break;

  case 707: /* f_norm_arg: "local variable or method"  */
#line 5326 "parse.y"
                    {
			formal_argument(p, (yyvsp[0].id));
			p->max_numparam = ORDINAL_PARAM;
			(yyval.id) = (yyvsp[0].id);
		    }
#line 12865 "parse.c"
    break;

  case 708: /* f_arg_asgn: f_norm_arg  */
#line 5334 "parse.y"
                    {
			ID id = get_id((yyvsp[0].id));
			arg_var(p, id);
			p->cur_arg = id;
			(yyval.id) = (yyvsp[0].id);
		    }
#line 12876 "parse.c"
    break;

  case 709: /* f_arg_item: f_arg_asgn  */
#line 5343 "parse.y"
                    {
			p->cur_arg = 0;
		    /*%%%*/
			(yyval.node) = NEW_ARGS_AUX((yyvsp[0].id), 1, &NULL_LOC);
		    /*% %*/
		    /*% ripper: get_value($1) %*/
		    }
#line 12888 "parse.c"
    break;

  case 710: /* f_arg_item: "(" f_margs rparen  */
#line 5351 "parse.y"
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
#line 12911 "parse.c"
    break;

  case 712: /* f_arg: f_arg ',' f_arg_item  */
#line 5374 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-2].node);
			(yyval.node)->nd_plen++;
			(yyval.node)->nd_next = block_append(p, (yyval.node)->nd_next, (yyvsp[0].node)->nd_next);
			rb_discard_node(p, (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 12925 "parse.c"
    break;

  case 713: /* f_label: "label"  */
#line 5387 "parse.y"
                    {
			arg_var(p, formal_argument(p, (yyvsp[0].id)));
			p->cur_arg = get_id((yyvsp[0].id));
			p->max_numparam = ORDINAL_PARAM;
			p->ctxt.in_argdef = 0;
			(yyval.id) = (yyvsp[0].id);
		    }
#line 12937 "parse.c"
    break;

  case 714: /* f_kw: f_label arg_value  */
#line 5397 "parse.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[-1].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($2)) %*/
		    }
#line 12950 "parse.c"
    break;

  case 715: /* f_kw: f_label  */
#line 5406 "parse.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[0].id), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), 0) %*/
		    }
#line 12963 "parse.c"
    break;

  case 716: /* f_block_kw: f_label primary_value  */
#line 5417 "parse.y"
                    {
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[-1].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($2)) %*/
		    }
#line 12975 "parse.c"
    break;

  case 717: /* f_block_kw: f_label  */
#line 5425 "parse.y"
                    {
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[0].id), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), 0) %*/
		    }
#line 12987 "parse.c"
    break;

  case 718: /* f_block_kwarg: f_block_kw  */
#line 5435 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 12998 "parse.c"
    break;

  case 719: /* f_block_kwarg: f_block_kwarg ',' f_block_kw  */
#line 5442 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = kwd_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 13009 "parse.c"
    break;

  case 720: /* f_kwarg: f_kw  */
#line 5452 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 13020 "parse.c"
    break;

  case 721: /* f_kwarg: f_kwarg ',' f_kw  */
#line 5459 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = kwd_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 13031 "parse.c"
    break;

  case 724: /* f_no_kwarg: kwrest_mark "`nil'"  */
#line 5472 "parse.y"
                    {
		    /*%%%*/
		    /*% %*/
		    /*% ripper: nokw_param!(Qnil) %*/
		    }
#line 13041 "parse.c"
    break;

  case 725: /* f_kwrest: kwrest_mark "local variable or method"  */
#line 5480 "parse.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].id))));
		    /*%%%*/
			(yyval.id) = (yyvsp[0].id);
		    /*% %*/
		    /*% ripper: kwrest_param!($2) %*/
		    }
#line 13053 "parse.c"
    break;

  case 726: /* f_kwrest: kwrest_mark  */
#line 5488 "parse.y"
                    {
		    /*%%%*/
			(yyval.id) = internal_id(p);
			arg_var(p, (yyval.id));
		    /*% %*/
		    /*% ripper: kwrest_param!(Qnil) %*/
		    }
#line 13065 "parse.c"
    break;

  case 727: /* f_opt: f_arg_asgn f_eq arg_value  */
#line 5498 "parse.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($3)) %*/
		    }
#line 13078 "parse.c"
    break;

  case 728: /* f_block_opt: f_arg_asgn f_eq primary_value  */
#line 5509 "parse.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($3)) %*/
		    }
#line 13091 "parse.c"
    break;

  case 729: /* f_block_optarg: f_block_opt  */
#line 5520 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 13102 "parse.c"
    break;

  case 730: /* f_block_optarg: f_block_optarg ',' f_block_opt  */
#line 5527 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = opt_arg_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 13113 "parse.c"
    break;

  case 731: /* f_optarg: f_opt  */
#line 5536 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 13124 "parse.c"
    break;

  case 732: /* f_optarg: f_optarg ',' f_opt  */
#line 5543 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = opt_arg_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 13135 "parse.c"
    break;

  case 735: /* f_rest_arg: restarg_mark "local variable or method"  */
#line 5556 "parse.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].id))));
		    /*%%%*/
			(yyval.id) = (yyvsp[0].id);
		    /*% %*/
		    /*% ripper: rest_param!($2) %*/
		    }
#line 13147 "parse.c"
    break;

  case 736: /* f_rest_arg: restarg_mark  */
#line 5564 "parse.y"
                    {
		    /*%%%*/
			(yyval.id) = internal_id(p);
			arg_var(p, (yyval.id));
		    /*% %*/
		    /*% ripper: rest_param!(Qnil) %*/
		    }
#line 13159 "parse.c"
    break;

  case 739: /* f_block_arg: blkarg_mark "local variable or method"  */
#line 5578 "parse.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].id))));
		    /*%%%*/
			(yyval.id) = (yyvsp[0].id);
		    /*% %*/
		    /*% ripper: blockarg!($2) %*/
		    }
#line 13171 "parse.c"
    break;

  case 740: /* f_block_arg: blkarg_mark  */
#line 5586 "parse.y"
                    {
                    /*%%%*/
                        arg_var(p, shadowing_lvar(p, get_id(ANON_BLOCK_ID)));
                    /*%
                    $$ = dispatch1(blockarg, Qnil);
                    %*/
                    }
#line 13183 "parse.c"
    break;

  case 741: /* opt_f_block_arg: ',' f_block_arg  */
#line 5596 "parse.y"
                    {
			(yyval.id) = (yyvsp[0].id);
		    }
#line 13191 "parse.c"
    break;

  case 742: /* opt_f_block_arg: none  */
#line 5600 "parse.y"
                    {
			(yyval.id) = Qnull;
		    }
#line 13199 "parse.c"
    break;

  case 743: /* singleton: var_ref  */
#line 5606 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 13208 "parse.c"
    break;

  case 744: /* $@48: %empty  */
#line 5610 "parse.y"
                      {SET_LEX_STATE(EXPR_BEG);}
#line 13214 "parse.c"
    break;

  case 745: /* singleton: '(' $@48 expr rparen  */
#line 5611 "parse.y"
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
#line 13240 "parse.c"
    break;

  case 747: /* assoc_list: assocs trailer  */
#line 5636 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: assoclist_from_args!($1) %*/
		    }
#line 13251 "parse.c"
    break;

  case 749: /* assocs: assocs ',' assoc  */
#line 5647 "parse.y"
                    {
		    /*%%%*/
			NODE *assocs = (yyvsp[-2].node);
			NODE *tail = (yyvsp[0].node);
			if (!assocs) {
			    assocs = tail;
			}
			else if (tail) {
                            if (assocs->nd_head &&
                                !tail->nd_head && nd_type_p(tail->nd_next, NODE_LIST) &&
                                nd_type_p(tail->nd_next->nd_head, NODE_HASH)) {
                                /* DSTAR */
                                tail = tail->nd_next->nd_head->nd_head;
                            }
			    assocs = list_concat(assocs, tail);
			}
			(yyval.node) = assocs;
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 13276 "parse.c"
    break;

  case 750: /* assoc: arg_value "=>" arg_value  */
#line 5670 "parse.y"
                    {
		    /*%%%*/
			if (nd_type_p((yyvsp[-2].node), NODE_STR)) {
			    nd_set_type((yyvsp[-2].node), NODE_LIT);
			    RB_OBJ_WRITE(p->ast, &(yyvsp[-2].node)->nd_lit, rb_fstring((yyvsp[-2].node)->nd_lit));
			}
			(yyval.node) = list_append(p, NEW_LIST((yyvsp[-2].node), &(yyloc)), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: assoc_new!($1, $3) %*/
		    }
#line 13291 "parse.c"
    break;

  case 751: /* assoc: "label" arg_value  */
#line 5681 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[-1].id)), &(yylsp[-1])), &(yyloc)), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: assoc_new!($1, $2) %*/
		    }
#line 13302 "parse.c"
    break;

  case 752: /* assoc: "label"  */
#line 5688 "parse.y"
                    {
		    /*%%%*/
			NODE *val = gettable(p, (yyvsp[0].id), &(yyloc));
			if (!val) val = NEW_BEGIN(0, &(yyloc));
			(yyval.node) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[0].id)), &(yylsp[0])), &(yyloc)), val);
		    /*% %*/
		    /*% ripper: assoc_new!($1, Qnil) %*/
		    }
#line 13315 "parse.c"
    break;

  case 753: /* assoc: "string literal" string_contents tLABEL_END arg_value  */
#line 5697 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-3]), &(yylsp[-1]));
			(yyval.node) = list_append(p, NEW_LIST(dsym_node(p, (yyvsp[-2].node), &loc), &loc), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: assoc_new!(dyna_symbol!($2), $4) %*/
		    }
#line 13327 "parse.c"
    break;

  case 754: /* assoc: "**arg" arg_value  */
#line 5705 "parse.y"
                    {
		    /*%%%*/
                        if (nd_type_p((yyvsp[0].node), NODE_HASH) &&
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
#line 13348 "parse.c"
    break;

  case 781: /* term: ';'  */
#line 5773 "parse.y"
                      {yyerrok;token_flush(p);}
#line 13354 "parse.c"
    break;

  case 782: /* term: '\n'  */
#line 5774 "parse.y"
                       {token_flush(p);}
#line 13360 "parse.c"
    break;

  case 784: /* terms: terms ';'  */
#line 5778 "parse.y"
                            {yyerrok;}
#line 13366 "parse.c"
    break;

  case 785: /* none: %empty  */
#line 5782 "parse.y"
                    {
			(yyval.node) = Qnull;
		    }
#line 13374 "parse.c"
    break;


#line 13378 "parse.c"

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
  YY_SYMBOL_PRINT ("-> $$ =", YY_CAST (yysymbol_kind_t, yyr1[yyn]), &yyval, &yyloc);

  YYPOPSTACK (yylen);
  yylen = 0;

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
  yytoken = yychar == YYEMPTY ? YYSYMBOL_YYEMPTY : YYTRANSLATE (yychar);
  /* If not already recovering from an error, report this error.  */
  if (!yyerrstatus)
    {
      ++yynerrs;
      {
        yypcontext_t yyctx
          = {yyssp, yytoken, &yylloc};
        char const *yymsgp = YY_("syntax error");
        int yysyntax_error_status;
        yysyntax_error_status = yysyntax_error (p, &yymsg_alloc, &yymsg, &yyctx);
        if (yysyntax_error_status == 0)
          yymsgp = yymsg;
        else if (yysyntax_error_status == -1)
          {
            if (yymsg != yymsgbuf)
              YYSTACK_FREE (yymsg);
            yymsg = YY_CAST (char *,
                             YYSTACK_ALLOC (YY_CAST (YYSIZE_T, yymsg_alloc)));
            if (yymsg)
              {
                yysyntax_error_status
                  = yysyntax_error (p, &yymsg_alloc, &yymsg, &yyctx);
                yymsgp = yymsg;
              }
            else
              {
                yymsg = yymsgbuf;
                yymsg_alloc = sizeof yymsgbuf;
                yysyntax_error_status = YYENOMEM;
              }
          }
        yyerror (&yylloc, p, yymsgp);
        if (yysyntax_error_status == YYENOMEM)
          YYNOMEM;
      }
    }

  yyerror_range[1] = yylloc;
  if (yyerrstatus == 3)
    {
      /* If just tried and failed to reuse lookahead token after an
         error, discard it.  */

      if (yychar <= END_OF_INPUT)
        {
          /* Return failure if at end of input.  */
          if (yychar == END_OF_INPUT)
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
  ++yynerrs;

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

  /* Pop stack until we find a state that shifts the error token.  */
  for (;;)
    {
      yyn = yypact[yystate];
      if (!yypact_value_is_default (yyn))
        {
          yyn += YYSYMBOL_YYerror;
          if (0 <= yyn && yyn <= YYLAST && yycheck[yyn] == YYSYMBOL_YYerror)
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
                  YY_ACCESSING_SYMBOL (yystate), yyvsp, yylsp, p);
      YYPOPSTACK (1);
      yystate = *yyssp;
      YY_STACK_PRINT (yyss, yyssp);
    }

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END

  yyerror_range[2] = yylloc;
  ++yylsp;
  YYLLOC_DEFAULT (*yylsp, yyerror_range, 2);

  /* Shift the error token.  */
  YY_SYMBOL_PRINT ("Shifting", YY_ACCESSING_SYMBOL (yyn), yyvsp, yylsp);

  yystate = yyn;
  goto yynewstate;


/*-------------------------------------.
| yyacceptlab -- YYACCEPT comes here.  |
`-------------------------------------*/
yyacceptlab:
  yyresult = 0;
  goto yyreturnlab;


/*-----------------------------------.
| yyabortlab -- YYABORT comes here.  |
`-----------------------------------*/
yyabortlab:
  yyresult = 1;
  goto yyreturnlab;


/*-----------------------------------------------------------.
| yyexhaustedlab -- YYNOMEM (memory exhaustion) comes here.  |
`-----------------------------------------------------------*/
yyexhaustedlab:
  yyerror (&yylloc, p, YY_("memory exhausted"));
  yyresult = 2;
  goto yyreturnlab;


/*----------------------------------------------------------.
| yyreturnlab -- parsing is finished, clean up and return.  |
`----------------------------------------------------------*/
yyreturnlab:
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
                  YY_ACCESSING_SYMBOL (+*yyssp), yyvsp, yylsp, p);
      YYPOPSTACK (1);
    }
#ifndef yyoverflow
  if (yyss != yyssa)
    YYSTACK_FREE (yyss);
#endif
  if (yymsg != yymsgbuf)
    YYSTACK_FREE (yymsg);
  return yyresult;
}

#line 5786 "parse.y"

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
#if 0
    YYLTYPE current;

    if (!yylloc) {
	yylloc = RUBY_SET_YYLLOC(current);
    }
    else if ((p->ruby_sourceline != yylloc->beg_pos.lineno &&
	      p->ruby_sourceline != yylloc->end_pos.lineno)) {
	yylloc = 0;
    }
#endif
    compile_error(p, "%s", msg);
    parser_show_error_line(p, yylloc);
    return 0;
}

static int
parser_yyerror0(struct parser_params *p, const char *msg)
{
    YYLTYPE current;
    return parser_yyerror(p, RUBY_SET_YYLLOC(current), msg);
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
    if (p->ruby_sourceline == yylloc->beg_pos.lineno &&
	p->ruby_sourceline == yylloc->end_pos.lineno) {
	pcur = p->lex.pcur;
	ptok = p->lex.ptok;
	p->lex.ptok = p->lex.pbeg + yylloc->beg_pos.column;
	p->lex.pcur = p->lex.pbeg + yylloc->end_pos.column;
    }
    parser_yyerror0(p, msg);
    if (pcur) {
	p->lex.ptok = ptok;
	p->lex.pcur = pcur;
    }
    return 0;
}

static int
parser_yyerror0(struct parser_params *p, const char *msg)
{
    dispatch1(parse_error, STR_NEW2(msg));
    ripper_error(p);
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

    if (p->keep_script_lines || ruby_vm_keep_script_lines) {
        if (!p->debug_lines) {
            p->debug_lines = rb_ary_new();
        }

        RB_OBJ_WRITE(p->ast, &p->ast->body.script_lines, p->debug_lines);
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
    if (!p->ast->body.script_lines) p->ast->body.script_lines = INT2FIX(p->line_count);
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

    p->lvtbl = NULL;

    p->ast = ast = rb_ast_new();
    rb_suppress_tracing(yycompile0, (VALUE)p);
    p->ast = 0;
    RB_GC_GUARD(vparser); /* prohibit tail call optimization */

    while (p->lvtbl) {
        local_pop(p);
    }

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
    if (RB_OBJ_FROZEN(line)) line = rb_str_dup(line); // needed for RubyVM::AST.of because script_lines in iseq is deep-frozen
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
    size_t numlen;

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
                    switch (c) {
                      case 'c':
                      case 'C':
                      case 'M': {
                        pushback(p, c);
                        c = read_escape(p, 0, enc);

                        int i;
                        char escbuf[5];
                        snprintf(escbuf, sizeof(escbuf), "\\x%02X", c);
                        for (i = 0; i < 4; i++) {
                            tokadd(p, escbuf[i]);
                        }
                        continue;
                      }
                    }

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
		yyerror0("unterminated here document identifier");
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
	yyerror0("too long here document identifier");
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
    if (nd_type_p(root, NODE_LIST)) str_node = root->nd_head;

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
		if (nd_type_p(prev_node, NODE_DSTR))
		    nd_set_type(prev_node, NODE_STR);
		break;
	    }
	    node->nd_end = end;
	    goto next_str;
	}

	str_node = 0;
	while ((node = (prev_node = node)->nd_next) != 0) {
	  next_str:
	    if (!nd_type_p(node, NODE_LIST)) break;
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
    ID id = get_id(lhs);

    switch (id_type(id)) {
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
    shadowing_lvar(p, id);
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
		 RBOOL(b));
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
	sep = 0;
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
	if (c == -1) goto unterminated;
	if (!ISALNUM(c)) {
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
	  unterminated:
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
		if (p->ctxt.in_argdef) {
		    SET_LEX_STATE(EXPR_ENDARG);
		    return tBDOT3;
		}
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

    if (!nd_type_p(tail, NODE_BLOCK)) {
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
	if (!head || !nd_type_p(head, NODE_STR)) return Qfalse;
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
    if (nd_type_p(node, NODE_EVSTR)) {
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
            return node;
          case NODE_DSTR:
            break;
          case NODE_EVSTR:
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

#define nd_once_body(node) (nd_type_p((node), NODE_ONCE) ? (node)->nd_body : node)
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
    if (nd_type_p(arg, NODE_STR)) {
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

static inline enum lex_state_e
parser_set_lex_state(struct parser_params *p, enum lex_state_e ls, int line)
{
    if (p->debug) {
	ls = rb_parser_trace_lex_state(p, p->lex.state, ls, line);
    }
    return p->lex.state = ls;
}

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
    yyerror0(RSTRING_PTR(mesg));
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

static YYLTYPE *
rb_parser_set_pos(YYLTYPE *yylloc, int sourceline, int beg_pos, int end_pos)
{
    yylloc->beg_pos.lineno = sourceline;
    yylloc->beg_pos.column = beg_pos;
    yylloc->end_pos.lineno = sourceline;
    yylloc->end_pos.column = end_pos;
    return yylloc;
}

YYLTYPE *
rb_parser_set_location_from_strterm_heredoc(struct parser_params *p, rb_strterm_heredoc_t *here, YYLTYPE *yylloc)
{
    int sourceline = here->sourceline;
    int beg_pos = (int)here->offset - here->quote
	- (rb_strlen_lit("<<-") - !(here->func & STR_FUNC_INDENT));
    int end_pos = (int)here->offset + here->length + here->quote;

    return rb_parser_set_pos(yylloc, sourceline, beg_pos, end_pos);
}

YYLTYPE *
rb_parser_set_location_of_none(struct parser_params *p, YYLTYPE *yylloc)
{
    int sourceline = p->ruby_sourceline;
    int beg_pos = (int)(p->lex.ptok - p->lex.pbeg);
    int end_pos = (int)(p->lex.ptok - p->lex.pbeg);
    return rb_parser_set_pos(yylloc, sourceline, beg_pos, end_pos);
}

YYLTYPE *
rb_parser_set_location(struct parser_params *p, YYLTYPE *yylloc)
{
    int sourceline = p->ruby_sourceline;
    int beg_pos = (int)(p->lex.ptok - p->lex.pbeg);
    int end_pos = (int)(p->lex.pcur - p->lex.pbeg);
    return rb_parser_set_pos(yylloc, sourceline, beg_pos, end_pos);
}
#endif /* !RIPPER */

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
	    if (dvar_curr(p, id)) return NODE_DASGN;
	    if (dvar_defined(p, id)) return NODE_DASGN;
	    if (local_id(p, id)) return NODE_LASGN;
	    dyna_var(p, id);
	    return NODE_DASGN;
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
    if (node2 && node1 && nd_type_p(node1, NODE_BLOCK_PASS)) {
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
        if (!nd_type_p(node1->nd_body, NODE_LIST)) break;
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
	if (!nd_type_p(node2, NODE_LIST)) break;
	node1->nd_body = list_concat(NEW_LIST(node1->nd_body, loc), node2);
	nd_set_type(node1, NODE_ARGSCAT);
	return node1;
      case NODE_ARGSCAT:
	if (!nd_type_p(node2, NODE_LIST) ||
	    !nd_type_p(node1->nd_body, NODE_LIST)) break;
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
    if ((nd_type_p(rest_arg, NODE_LIST)) && (n1 = splat_array(args)) != 0) {
	return list_concat(n1, rest_arg);
    }
    return arg_concat(p, args, rest_arg, loc);
}

static NODE *
splat_array(NODE* node)
{
    if (nd_type_p(node, NODE_SPLAT)) node = node->nd_head;
    if (nd_type_p(node, NODE_LIST)) return node;
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
    if (!nd_type_p(n, NODE_CALL)) {
	const YYLTYPE *loc = &n->nd_loc;
	VALUE path;
	if (n->nd_vid) {
	     path = rb_id2str(n->nd_vid);
	}
	else {
	    n = n->nd_else;
	    path = rb_ary_new();
	    for (; n && nd_type_p(n, NODE_COLON2); n = n->nd_head) {
		rb_ary_push(path, rb_id2str(n->nd_mid));
	    }
	    if (n && nd_type_p(n, NODE_CONST)) {
		// Const::Name
		rb_ary_push(path, rb_id2str(n->nd_vid));
	    }
	    else if (n && nd_type_p(n, NODE_COLON3)) {
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
        RB_OBJ_WRITTEN(p->ast, Qnil, n->nd_lit);
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
        NODE *n = NEW_LIT(lit, loc);
        RB_OBJ_WRITTEN(p->ast, Qnil, n->nd_lit);
        return n;

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
        RB_OBJ_WRITTEN(p->ast, Qnil, value->nd_lit);
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
	    if (!node->nd_body || !nd_type_p(node->nd_body, NODE_IN)) {
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
    if (!nd_type_p(node, NODE_BLOCK)) return n;

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
    while (n1 && nd_type_p(n1, NODE_BEGIN) && n1->nd_body) {
	*n = n1 = n1->nd_body;
    }
    return node;
}

static NODE *
remove_begin_all(NODE *node)
{
    NODE **n = &node, *n1 = node;
    while (n1 && nd_type_p(n1, NODE_BEGIN)) {
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
	if (nd_type_p(node, NODE_DOT2)) nd_set_type(node,NODE_FLIP2);
	else if (nd_type_p(node, NODE_DOT3)) nd_set_type(node, NODE_FLIP3);
	break;

      case NODE_DSYM:
      warn_symbol:
	SWITCH_BY_COND_TYPE(type, warning, "symbol ")
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
	else if (SYMBOL_P(node->nd_lit)) {
	    goto warn_symbol;
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
    if (left && nd_type_p(left, type)) {
	NODE *node = left, *second;
	while ((second = node->nd_2nd) != 0 && nd_type_p(second, type)) {
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
    if (node && nd_type_p(node, NODE_BLOCK_PASS)) {
	compile_error(p, "block argument should not be given");
    }
}

static NODE *
ret_args(struct parser_params *p, NODE *node)
{
    if (node) {
	no_blockarg(p, node);
	if (nd_type_p(node, NODE_LIST)) {
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
	lit = DBL2NUM(-RFLOAT_VALUE(lit));
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

    if (args->block_arg == idFWD_BLOCK) {
	if (rest_arg) {
	    yyerror1(&tail->nd_loc, "... after rest argument");
	    return tail;
	}
	rest_arg = idFWD_REST;
    }

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

        if (block) block = vtargs->tbl[vtargs->pos-1];
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

static bool
hash_literal_key_p(VALUE k)
{
    switch (OBJ_BUILTIN_TYPE(k)) {
      case T_NODE:
	return false;
      default:
	return true;
    }
}

static int
literal_cmp(VALUE val, VALUE lit)
{
    if (val == lit) return 0;
    if (!hash_literal_key_p(val) || !hash_literal_key_p(lit)) return -1;
    return rb_iseq_cdhash_cmp(val, lit);
}

static st_index_t
literal_hash(VALUE a)
{
    if (!hash_literal_key_p(a)) return (st_index_t)a;
    return rb_iseq_cdhash_hash(a);
}

static const struct st_hash_type literal_type = {
    literal_cmp,
    literal_hash,
};

static NODE *
remove_duplicate_keys(struct parser_params *p, NODE *hash)
{
    st_table *literal_keys = st_init_table_with_size(&literal_type, hash->nd_alen / 2);
    NODE *result = 0;
    NODE *last_expr = 0;
    rb_code_location_t loc = hash->nd_loc;
    while (hash && hash->nd_head && hash->nd_next) {
	NODE *head = hash->nd_head;
	NODE *value = hash->nd_next;
	NODE *next = value->nd_next;
	st_data_t key = (st_data_t)head;
	st_data_t data;
	value->nd_next = 0;
	if (nd_type_p(head, NODE_LIT) &&
	    st_delete(literal_keys, (key = (st_data_t)head->nd_lit, &key), &data)) {
	    NODE *dup_value = ((NODE *)data)->nd_next;
	    rb_compile_warn(p->ruby_sourcefile, nd_line((NODE *)data),
			    "key %+"PRIsVALUE" is duplicated and overwritten on line %d",
			    head->nd_lit, nd_line(head));
	    if (dup_value == last_expr) {
		value->nd_head = block_append(p, dup_value->nd_head, value->nd_head);
	    }
	    else {
		last_expr->nd_head = block_append(p, dup_value->nd_head, last_expr->nd_head);
	    }
	}
	st_insert(literal_keys, (st_data_t)key, (st_data_t)hash);
	last_expr = nd_type_p(head, NODE_LIT) ? value : head;
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
    if (nd_type_p(args, NODE_BLOCK_PASS)) {
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
static rb_ast_id_table_t *
local_tbl(struct parser_params *p)
{
    int cnt_args = vtable_size(p->lvtbl->args);
    int cnt_vars = vtable_size(p->lvtbl->vars);
    int cnt = cnt_args + cnt_vars;
    int i, j;
    rb_ast_id_table_t *tbl;

    if (cnt <= 0) return 0;
    tbl = rb_ast_new_local_table(p->ast, cnt);
    MEMCPY(tbl->ids, p->lvtbl->args->tbl, ID, cnt_args);
    /* remove IDs duplicated to warn shadowing */
    for (i = 0, j = cnt_args; i < cnt_vars; ++i) {
	ID id = p->lvtbl->vars->tbl[i];
	if (!vtable_included(p->lvtbl->args, id)) {
	    tbl->ids[j++] = id;
	}
    }
    if (j < cnt) {
        tbl = rb_ast_resize_latest_local_table(p->ast, j);
    }

    return tbl;
}

static NODE*
node_newnode_with_locals(struct parser_params *p, enum node_type type, VALUE a1, VALUE a2, const rb_code_location_t *loc)
{
    rb_ast_id_table_t *a0;
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
    return rb_make_temporary_id(vtable_size(p->lvtbl->args) + vtable_size(p->lvtbl->vars));
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

void
rb_parser_keep_script_lines(VALUE vparser)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    p->keep_script_lines = 1;
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
    return RBOOL(p->error_p);
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
    return RBOOL(p->ruby__end__seen);
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
    return RBOOL(p->debug);
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
	    break;
	}
	prev = &n->next;
    }
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
	if (!nd_type_p((NODE *)x, NODE_RIPPER)) {
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
    if (!nd_type_p(nd, NODE_RIPPER)) return 0;
    return nd->nd_vid;
}

static VALUE
ripper_get_value(VALUE v)
{
    NODE *nd;
    if (v == Qundef) return Qnil;
    if (!RB_TYPE_P(v, T_NODE)) return v;
    nd = (NODE *)v;
    if (!nd_type_p(nd, NODE_RIPPER)) return Qnil;
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
