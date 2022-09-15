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
#line 12 "ripper.y"


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

#line 1145 "ripper.c"

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


/* Debug traces.  */
#ifndef YYDEBUG
# define YYDEBUG 1
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
#line 1126 "ripper.y"

    VALUE val;
    NODE *node;
    ID id;
    int num;
    st_table *tbl;
    const struct vtable *vars;
    struct rb_strterm_struct *strterm;
    struct lex_context ctxt;

#line 1328 "ripper.c"

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
#define YYLAST   14773

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  154
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  270
/* YYNRULES -- Number of rules.  */
#define YYNRULES  783
/* YYNSTATES -- Number of states.  */
#define YYNSTATES  1307

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
       0,  1327,  1327,  1327,  1353,  1359,  1366,  1373,  1380,  1386,
    1387,  1393,  1406,  1404,  1415,  1426,  1432,  1439,  1446,  1453,
    1459,  1464,  1463,  1473,  1473,  1480,  1487,  1497,  1506,  1513,
    1521,  1529,  1541,  1553,  1563,  1577,  1578,  1586,  1593,  1601,
    1608,  1611,  1618,  1625,  1633,  1640,  1647,  1655,  1662,  1673,
    1685,  1698,  1712,  1722,  1727,  1736,  1739,  1740,  1744,  1748,
    1752,  1757,  1756,  1775,  1774,  1792,  1795,  1812,  1822,  1821,
    1840,  1847,  1847,  1847,  1853,  1854,  1857,  1858,  1867,  1877,
    1887,  1896,  1907,  1914,  1921,  1928,  1935,  1943,  1951,  1958,
    1965,  1974,  1975,  1984,  1985,  1994,  2001,  2008,  2015,  2022,
    2029,  2036,  2043,  2050,  2057,  2066,  2067,  2076,  2083,  2092,
    2099,  2108,  2115,  2122,  2129,  2139,  2146,  2156,  2163,  2170,
    2180,  2187,  2194,  2201,  2208,  2215,  2222,  2229,  2236,  2246,
    2254,  2257,  2264,  2271,  2280,  2281,  2282,  2283,  2288,  2291,
    2298,  2301,  2308,  2308,  2318,  2319,  2320,  2321,  2322,  2323,
    2324,  2325,  2326,  2327,  2328,  2329,  2330,  2331,  2332,  2333,
    2334,  2335,  2336,  2337,  2338,  2339,  2340,  2341,  2342,  2343,
    2344,  2345,  2346,  2347,  2350,  2350,  2350,  2351,  2351,  2352,
    2352,  2352,  2353,  2353,  2353,  2353,  2354,  2354,  2354,  2354,
    2355,  2355,  2355,  2356,  2356,  2356,  2356,  2357,  2357,  2357,
    2357,  2358,  2358,  2358,  2358,  2359,  2359,  2359,  2359,  2360,
    2360,  2360,  2360,  2361,  2361,  2364,  2371,  2378,  2385,  2392,
    2399,  2406,  2414,  2422,  2430,  2439,  2448,  2456,  2464,  2472,
    2480,  2484,  2488,  2492,  2496,  2500,  2504,  2508,  2512,  2516,
    2520,  2524,  2528,  2532,  2533,  2537,  2541,  2545,  2549,  2553,
    2557,  2561,  2565,  2569,  2573,  2577,  2577,  2582,  2591,  2602,
    2614,  2627,  2641,  2647,  2648,  2649,  2650,  2653,  2657,  2664,
    2668,  2674,  2681,  2682,  2686,  2693,  2702,  2707,  2717,  2724,
    2736,  2750,  2751,  2754,  2755,  2756,  2760,  2767,  2776,  2784,
    2791,  2799,  2807,  2811,  2811,  2848,  2855,  2868,  2872,  2879,
    2886,  2893,  2900,  2910,  2911,  2915,  2922,  2929,  2938,  2939,
    2940,  2941,  2942,  2943,  2944,  2945,  2946,  2947,  2948,  2956,
    2955,  2970,  2970,  2977,  2977,  2985,  2993,  3000,  3007,  3014,
    3022,  3029,  3036,  3043,  3050,  3050,  3055,  3059,  3063,  3070,
    3071,  3079,  3080,  3091,  3102,  3112,  3123,  3122,  3139,  3138,
    3153,  3162,  3205,  3204,  3228,  3227,  3250,  3249,  3272,  3284,
    3298,  3305,  3312,  3319,  3328,  3335,  3341,  3358,  3364,  3370,
    3376,  3382,  3388,  3395,  3402,  3409,  3415,  3421,  3427,  3433,
    3439,  3454,  3461,  3467,  3474,  3475,  3476,  3479,  3480,  3483,
    3484,  3496,  3497,  3506,  3507,  3510,  3518,  3527,  3534,  3543,
    3550,  3557,  3564,  3571,  3580,  3588,  3597,  3598,  3601,  3601,
    3603,  3607,  3611,  3615,  3621,  3626,  3631,  3641,  3645,  3649,
    3653,  3657,  3661,  3666,  3670,  3674,  3678,  3682,  3686,  3690,
    3694,  3698,  3704,  3705,  3711,  3721,  3734,  3738,  3747,  3749,
    3753,  3758,  3765,  3771,  3775,  3779,  3764,  3804,  3813,  3824,
    3829,  3835,  3845,  3859,  3866,  3873,  3882,  3891,  3899,  3907,
    3914,  3922,  3930,  3937,  3944,  3957,  3965,  3975,  3976,  3980,
    3975,  3997,  3998,  4002,  3997,  4021,  4029,  4036,  4044,  4053,
    4065,  4066,  4070,  4077,  4081,  4069,  4096,  4097,  4100,  4101,
    4109,  4119,  4120,  4125,  4133,  4137,  4141,  4147,  4150,  4159,
    4162,  4169,  4172,  4173,  4175,  4176,  4177,  4186,  4195,  4204,
    4209,  4218,  4227,  4236,  4241,  4245,  4249,  4255,  4254,  4266,
    4271,  4271,  4278,  4287,  4291,  4300,  4304,  4308,  4312,  4316,
    4319,  4323,  4332,  4336,  4342,  4352,  4356,  4362,  4363,  4372,
    4381,  4385,  4389,  4393,  4399,  4401,  4410,  4418,  4432,  4433,
    4456,  4460,  4466,  4472,  4473,  4476,  4477,  4486,  4495,  4503,
    4511,  4512,  4513,  4514,  4522,  4532,  4533,  4534,  4535,  4536,
    4537,  4538,  4539,  4540,  4547,  4550,  4560,  4571,  4580,  4589,
    4596,  4603,  4612,  4624,  4627,  4634,  4641,  4644,  4648,  4651,
    4658,  4661,  4662,  4665,  4682,  4683,  4684,  4693,  4703,  4712,
    4718,  4728,  4734,  4743,  4745,  4754,  4764,  4770,  4779,  4788,
    4798,  4804,  4814,  4820,  4830,  4840,  4859,  4865,  4875,  4885,
    4926,  4929,  4928,  4945,  4949,  4954,  4958,  4962,  4944,  4983,
    4990,  4997,  5004,  5007,  5008,  5011,  5021,  5022,  5023,  5024,
    5027,  5037,  5038,  5048,  5049,  5050,  5051,  5054,  5055,  5056,
    5059,  5060,  5061,  5062,  5063,  5066,  5067,  5068,  5069,  5070,
    5071,  5072,  5075,  5088,  5097,  5104,  5113,  5114,  5118,  5117,
    5127,  5135,  5136,  5144,  5156,  5157,  5157,  5173,  5177,  5181,
    5185,  5189,  5196,  5201,  5206,  5210,  5214,  5218,  5222,  5226,
    5230,  5234,  5238,  5242,  5246,  5250,  5254,  5258,  5263,  5269,
    5278,  5287,  5296,  5305,  5316,  5317,  5325,  5334,  5342,  5363,
    5365,  5378,  5388,  5397,  5408,  5416,  5426,  5433,  5443,  5450,
    5459,  5460,  5463,  5471,  5479,  5489,  5500,  5511,  5518,  5527,
    5534,  5543,  5544,  5547,  5555,  5565,  5566,  5569,  5577,  5587,
    5591,  5597,  5602,  5602,  5626,  5627,  5636,  5638,  5661,  5672,
    5679,  5688,  5696,  5715,  5716,  5717,  5720,  5721,  5722,  5723,
    5726,  5727,  5728,  5731,  5732,  5735,  5736,  5739,  5740,  5743,
    5744,  5747,  5748,  5751,  5754,  5757,  5760,  5761,  5762,  5765,
    5766,  5769,  5770,  5774
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
  "$@4", "command_asgn", "command_rhs", "expr", "@5", "@6", "def_name",
  "defn_head", "defs_head", "$@7", "expr_value", "expr_value_do", "$@8",
  "$@9", "command_call", "block_command", "cmd_brace_block", "fcall",
  "command", "mlhs", "mlhs_inner", "mlhs_basic", "mlhs_item", "mlhs_head",
  "mlhs_post", "mlhs_node", "lhs", "cname", "cpath", "fname", "fitem",
  "undef_list", "$@10", "op", "reswords", "arg", "$@11", "relop",
  "rel_expr", "lex_ctxt", "arg_value", "aref_args", "arg_rhs",
  "paren_args", "opt_paren_args", "opt_call_args", "call_args",
  "command_args", "$@12", "block_arg", "opt_block_arg", "args", "mrhs_arg",
  "mrhs", "primary", "$@13", "$@14", "$@15", "$@16", "@17", "@18", "$@19",
  "$@20", "$@21", "primary_value", "k_begin", "k_if", "k_unless",
  "k_while", "k_until", "k_case", "k_for", "k_class", "k_module", "k_def",
  "k_do", "k_do_block", "k_rescue", "k_ensure", "k_when", "k_else",
  "k_elsif", "k_end", "k_return", "then", "do", "if_tail", "opt_else",
  "for_var", "f_marg", "f_marg_list", "f_margs", "f_rest_marg",
  "f_any_kwrest", "f_eq", "$@22", "block_args_tail", "opt_block_args_tail",
  "excessed_comma", "block_param", "opt_block_param", "block_param_def",
  "opt_bv_decl", "bv_decls", "bvar", "lambda", "@23", "@24", "@25", "$@26",
  "f_larglist", "lambda_body", "do_block", "block_call", "method_call",
  "brace_block", "brace_body", "@27", "@28", "@29", "do_body", "@30",
  "@31", "@32", "case_args", "case_body", "cases", "p_case_body", "@33",
  "@34", "$@35", "p_cases", "p_top_expr", "p_top_expr_body", "p_expr",
  "p_as", "p_alt", "p_lparen", "p_lbracket", "p_expr_basic", "@36", "@37",
  "p_args", "p_args_head", "p_args_tail", "p_find", "p_rest",
  "p_args_post", "p_arg", "p_kwargs", "p_kwarg", "p_kw", "p_kw_label",
  "p_kwrest", "p_kwnorest", "p_any_kwrest", "p_value", "p_primitive",
  "p_variable", "p_var_ref", "p_expr_ref", "p_const", "opt_rescue",
  "exc_list", "exc_var", "opt_ensure", "literal", "strings", "string",
  "string1", "xstring", "regexp", "words", "word_list", "word", "symbols",
  "symbol_list", "qwords", "qsymbols", "qword_list", "qsym_list",
  "string_contents", "xstring_contents", "regexp_contents",
  "string_content", "@38", "$@39", "@40", "@41", "@42", "@43",
  "string_dvar", "symbol", "ssym", "sym", "dsym", "numeric",
  "simple_numeric", "nonlocal_var", "user_variable", "keyword_variable",
  "var_ref", "var_lhs", "backref", "superclass", "$@44",
  "f_opt_paren_args", "f_paren_args", "f_arglist", "@45", "args_tail",
  "opt_args_tail", "f_args", "args_forward", "f_bad_arg", "f_norm_arg",
  "f_arg_asgn", "f_arg_item", "f_arg", "f_label", "f_kw", "f_block_kw",
  "f_block_kwarg", "f_kwarg", "kwrest_mark", "f_no_kwarg", "f_kwrest",
  "f_opt", "f_block_opt", "f_block_optarg", "f_optarg", "restarg_mark",
  "f_rest_arg", "blkarg_mark", "f_block_arg", "opt_f_block_arg",
  "singleton", "$@46", "assoc_list", "assocs", "assoc", "operation",
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

#define YYPACT_NINF (-1111)

#define yypact_value_is_default(Yyn) \
  ((Yyn) == YYPACT_NINF)

#define YYTABLE_NINF (-784)

#define yytable_value_is_error(Yyn) \
  ((Yyn) == YYTABLE_NINF)

  /* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
     STATE-NUM.  */
static const yytype_int16 yypact[] =
{
   -1111,   129,  4523, -1111, 10057, -1111, -1111, -1111,  9515, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, 10183, 10183, -1111, -1111,
   -1111,  5975,  5534, -1111, -1111, -1111, -1111,   515,  9370,    18,
     120,   143, -1111, -1111, -1111,  4799,  5681, -1111, -1111,  4946,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, 11947, 11947,
   11947, 11947,   145,  7570, 10309, 10813, 11191,  9799, -1111,  9225,
   -1111, -1111, -1111,   165,   188,   259,   283,  1165, 12073, 11947,
   -1111,   213, -1111,  1330, -1111,   531,   186,   186, -1111, -1111,
     128,   194,   225, -1111,   306, 12325, -1111,   354,  4248,    81,
      64,   599, -1111, 12199, 12199, -1111, -1111,  8552, 12447, 12569,
   12691,  9079, 10183, -1111,   670,   136, -1111, -1111,   361, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111,    68,   287, -1111,   404,   365, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111,   343, -1111, -1111, -1111,   381,
   11947,   480,  7721, 11947, 11947, 11947, -1111, 11947,   186,   186,
   -1111,   450,  5512,   501, -1111, -1111,   437,   614,   483,   528,
     525,   545,   503, -1111, -1111,  8426, -1111, 10183, 10435, -1111,
   -1111,  8678, -1111, 12199,   768, -1111,   507,  7872, -1111,  8023,
   -1111, -1111,   551,   573,   128, -1111,   665, -1111,   596,  3223,
    3223,   406, 10309, -1111,  7570,   537,   213, -1111,  1330,    18,
     597, -1111,  1330,    18,   581,    20,   239, -1111,   501,   602,
     239, -1111,    18,   696,  1165, 12813,   622,   622,   639, -1111,
     695,   709,   753,   771, -1111, -1111, -1111, -1111, -1111,   973,
   -1111,  1098,  1132,   474, -1111, -1111, -1111, -1111,   711, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111,  8804, 12199, 12199, 12199,
   12199, 10309, 12199, 12199,  2195,   681,   716,  6714,  2195, -1111,
     729,  6714, -1111, -1111, -1111,   707, -1111, -1111, -1111, -1111,
   -1111,   790, -1111,  7570,  9928,   720,   790, -1111, 11947, 11947,
   11947, 11947, 11947, -1111, -1111, 11947, 11947, 11947, 11947, 11947,
   11947, 11947, 11947, -1111, 11947, -1111, -1111, 11947, 11947, 11947,
   11947, 11947, 11947, 11947, 11947, 11947, 11947, -1111, -1111, 13276,
   10183, 13366,  6714,   531,    88,    88,  8174, 12199,  8174,   213,
   -1111,   718,   806, -1111, -1111,   824,   845,   105,   121,   124,
     793,   869, 12199,   398, -1111,   742,   888, -1111, -1111, -1111,
   -1111,    83,   108,   328,   347,   371,   390,   422,   481,   491,
   -1111, -1111, -1111, -1111,   524, -1111, -1111, -1111, 14626, -1111,
   -1111,   790,   790, -1111, -1111,   606, -1111, -1111, -1111,  1081,
     748,   755,   790, 11947, 10561, -1111, -1111, 13456, 10183, 13546,
     790,   790, 10939, -1111,    18,   735, -1111, -1111, 11947,    18,
   -1111,   745,    18,   750, -1111,    84, -1111, -1111, -1111, -1111,
   -1111,  9515, -1111, 11947,   762,   765, 13456, 13546,   790,  1330,
     120,    18, -1111, -1111,  8930,   751,    18, -1111, -1111, 11065,
   -1111, -1111, 11191, -1111, -1111, -1111,   507,   890, -1111, -1111,
     767, -1111, 12813, 13636, 10183, 13726, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111,  1138,    70,  1169,
      79, 11947, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,  1311,
   -1111, -1111, -1111, -1111, -1111,   777, -1111,    18, -1111, -1111,
   -1111,   785, -1111,   778, 11947, -1111,   780,   131, -1111, -1111,
   -1111,   781,   842,   787,   865, -1111, 12073,   904,   923,   213,
   12073,   904,   791, -1111, -1111, -1111,   904, -1111,   904, -1111,
   11317, -1111,    18, 12813,   788, -1111, 11317,  4646,   596,  2834,
    2834,  2834,  2834,  4514,  2588,  2834,  2834,  3223,  3223,   710,
     710,  4646,  6097,  1077,  1077,  1300,   346,   346,   596,   596,
     596,  1186,  1186,  6122,  5093,  6416,  5240, -1111,   573, -1111,
      18,   794,   476, -1111,   519, -1111, -1111,  5828,   904, -1111,
    6865,   926,  7318,   904,   104,   904,   918,   930,   170, 13816,
   10183, 13906, -1111,   531, -1111,   890, -1111, -1111, -1111, 13996,
   10183, 14086,  6714, 12199, -1111, -1111, -1111, -1111, -1111,  3520,
   12073, 12073,  9515, 11947, 11947, 11947, 11947, -1111, 11947,   501,
   -1111,   503,  4169,  5387,    18,   646,   656, 11947, 11947, -1111,
   -1111, -1111, -1111, 10687, -1111, 10939, -1111, -1111, 12199,  5512,
   -1111, -1111,   573,   573, 11947, -1111,     9, -1111, -1111,   239,
   12813,   767,   513,   375,    18,   288,   321,  2098, -1111,  1057,
   -1111,   387, -1111,   796, -1111, -1111,   396,   797, -1111,   596,
    1311,   945, -1111,   805,    18,   811, -1111,    69, -1111, -1111,
   -1111, 11947,   833,  2195, -1111, -1111,   576, -1111, -1111, -1111,
    2195, -1111, -1111,  1944, -1111, -1111,   927,  5071, -1111, -1111,
   -1111, 11443,   291, -1111, -1111,   932,  5218, -1111, -1111, -1111,
     818, -1111, -1111, -1111, 11947, -1111,   828,   834,   935, -1111,
   -1111,   767, 12813, -1111, -1111,   946,   844,  4924, -1111, -1111,
   -1111,   928,   676, -1111, -1111, -1111,  2049,  2049,   538, -1111,
    3956,   256,   940, -1111,   624, -1111, -1111,    44, -1111,   877,
   -1111, -1111, -1111,   847, -1111,   870, -1111, 13209, -1111, -1111,
   -1111, -1111,   527, -1111, -1111, -1111,   273, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111,   342, -1111,   883,   790,
     790, -1111,   707,   873,   561, 10561,   790,   790, -1111, -1111,
     707, -1111, -1111,   593, -1111,  1000, -1111, -1111, -1111, -1111,
   -1111, -1111,   930,   904, -1111, 11569,   904,    77,   134,    18,
     208,   234,  8174,   213, 12199,  6714,   886,   375, -1111,    18,
     904,    84,  9660,   136,   194, -1111,  5365, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111,   790,   790,   677,   790,   790,    18,
     881,    84, -1111, -1111, -1111,   625,  2195, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
      18, -1111,  1311, -1111,  1278, -1111, -1111, -1111, -1111, -1111,
     892,   894, -1111,   996,   777,   905, -1111,   907, -1111,   905,
   11947, 11947,   828, -1111,   950, -1111, -1111, -1111,  8174, -1111,
   -1111, -1111, 11947, 11947,   925, -1111,   925,   912, 11695, 10309,
     767, 10309,   790, 11947, 14176, 10183, 14266, -1111, -1111, -1111,
   -1111, 13209,    50,    18, 13090, -1111,    18,   913, -1111,   438,
     916, -1111, -1111,  1117, -1111, -1111, -1111, -1111, 12199, -1111,
    1014, 13176, 13209, 13209,   438,   968,  2049,  2049,   538,   633,
     604,  4646,  4646, -1111, -1111, 11947, 12073, 12073, -1111, -1111,
     790, 12073, 12073, -1111, -1111,  8174, 12199,   904, -1111, -1111,
     904, -1111, -1111,   904, -1111, 11947, -1111,    97, -1111,   235,
     904,  6714,   213,   904, -1111, -1111, -1111, -1111, -1111, -1111,
   11947, 11947,   790, 11947, 11947, -1111, 10939, -1111,    18,    58,
   -1111, -1111, -1111,   931,   934,  2195, -1111,  1944, -1111, -1111,
    1944, -1111,  1944, -1111, -1111,  5512,  5512, 12935,    88, -1111,
   -1111,  7444,  5512,  5512,  1546,  8023, -1111, -1111,  6714, 11947,
     937, -1111, -1111, 12073,  5512,  6269,  6563,    18,   712,   725,
      87, -1111,    54,   968,   939, -1111, -1111, -1111,    18, -1111,
   -1111,   922, -1111, -1111,   952, -1111,   954, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111, -1111,    18,    18,    18,    18,    18,
      18,  5512, -1111, -1111, -1111, -1111, 12073, -1111, -1111, -1111,
   -1111, -1111,    88, -1111, -1111,  4646, -1111, -1111, 11821,  7016,
   -1111,   904, -1111, -1111, 11947,   929,   921,  6714,  8023, -1111,
   -1111,  1278,  1278,   905,   956,   905,   905,  1020, -1111,   958,
     139,   140,   171,  6714,  1095,   777, -1111,    18,   975,   785,
     964, 13057, -1111,   970, -1111,   977,   980, -1111, -1111, -1111,
     982,   726, -1111,   986, 13209, -1111,   898, -1111, -1111, 13209,
   13176, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,  6865,
      88,   724, 11947, -1111,   520, -1111, -1111,  1367,   904,   990,
    8300,   934, -1111,  1944, -1111, -1111, -1111,   180, 14356, 10183,
   14446,   923, -1111, -1111,  1004, -1111, 13057,  1858, -1111, -1111,
    1084,   994,   576, -1111,  1858, -1111,  1292, -1111, 13209,  1002,
    1002, -1111, -1111,   593, -1111, 12199, 12199, -1111, -1111, -1111,
   -1111, -1111,   310, -1111, -1111, -1111, -1111,  1022,   905,   175,
     184,    18,   190,   199, -1111, -1111,   994, -1111,  1003,  1005,
   -1111, 14536, -1111,   777,  1009, -1111,  1011,  1009,  1002, 13209,
   -1111,  7167, -1111, -1111,  1367, -1111, -1111, -1111,   252,  1858,
   -1111,  1292, -1111,   998,  1012, -1111,  1292, -1111,  1292, -1111,
   -1111,   330, -1111,  1009,  1024,  1009,  1009, -1111, -1111, -1111,
   -1111,  1292, -1111, -1111, -1111,  1009, -1111
};

  /* YYDEFACT[STATE-NUM] -- Default reduction number in state STATE-NUM.
     Performed when YYTABLE does not specify something else to do.  Zero
     means the default is an error.  */
static const yytype_int16 yydefact[] =
{
       2,     0,     0,     1,     0,   372,   373,   374,     0,   365,
     366,   367,   370,   368,   369,   371,   360,   361,   362,   363,
     383,   293,   293,   656,   655,   657,   658,   771,     0,   771,
       0,     0,   660,   659,   661,   753,   755,   652,   651,   754,
     654,   643,   644,   645,   646,   594,   666,   667,     0,     0,
       0,     0,     0,     0,   321,   783,   783,   103,   442,   614,
     614,   616,   618,     0,     0,     0,     0,     0,     0,     0,
       3,   769,     6,     9,    35,    40,   675,   675,    56,    75,
     293,    74,     0,    91,     0,    95,   105,     0,    65,   243,
     262,     0,   319,     0,     0,    71,    71,   769,     0,     0,
       0,     0,   330,   341,    76,   339,   308,   309,   593,   595,
     310,   311,   312,   314,   313,   315,   592,   633,   634,   591,
     641,   662,   663,   316,     0,   317,    79,     5,     8,   184,
     195,   185,   208,   181,   201,   191,   190,   211,   212,   206,
     189,   188,   183,   209,   213,   214,   193,   182,   196,   200,
     202,   194,   187,   203,   210,   205,   204,   197,   207,   192,
     180,   199,   198,   179,   186,   177,   178,   174,   175,   176,
     134,   136,   135,   169,   170,   165,   147,   148,   149,   156,
     153,   155,   150,   151,   171,   172,   157,   158,   162,   166,
     152,   154,   144,   145,   146,   159,   160,   161,   163,   164,
     167,   168,   173,   139,   141,    28,   137,   138,   140,     0,
     750,     0,     0,     0,     0,   296,   614,     0,   675,   675,
     288,     0,   271,   299,    89,   292,   783,     0,   662,   663,
       0,   317,   783,   746,    90,   771,    87,     0,   783,   462,
      86,   771,   772,     0,     0,    23,   255,     0,    10,     0,
     360,   361,   333,   463,     0,   237,     0,   330,   238,   228,
     229,   327,     0,    21,     0,     0,   769,    17,    20,   771,
      93,    16,   323,   771,     0,   776,   776,   272,     0,     0,
     776,   744,   771,     0,     0,     0,   675,   675,   101,   364,
       0,   111,   112,   119,   443,   638,   637,   639,   636,     0,
     635,     0,     0,     0,   601,   610,   606,   612,   642,    60,
     249,   250,   779,   780,     4,   781,   770,     0,     0,     0,
       0,     0,     0,     0,   698,     0,   674,     0,   698,   672,
       0,     0,   375,   467,   456,    80,   471,   338,   376,   471,
     452,   783,   107,     0,    99,    96,   783,    63,     0,     0,
       0,     0,     0,   265,   266,     0,     0,     0,     0,   226,
     227,     0,     0,    61,     0,   263,   264,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   765,   766,     0,
     783,     0,     0,    70,     0,     0,     0,     0,     0,   769,
     348,   770,     0,   394,   393,     0,     0,   662,   663,   317,
     129,   130,     0,     0,   132,   670,     0,   662,   663,   317,
     356,   204,   197,   207,   192,   174,   175,   176,   134,   135,
     742,    67,    66,   741,     0,    88,   768,   767,     0,   340,
     596,   783,   783,   142,   749,   327,   300,   752,   295,     0,
       0,     0,   783,     0,     0,   289,   298,     0,   783,     0,
     783,   783,     0,   290,   771,     0,   332,   294,   699,   771,
     284,   783,   771,   783,   283,   771,   337,    59,    25,    27,
      26,     0,   334,     0,     0,     0,     0,     0,   783,    19,
       0,   771,   325,    15,   770,    92,   771,   322,   328,   778,
     777,   273,   778,   275,   329,   745,     0,   118,   642,   109,
     104,   674,     0,     0,   783,     0,   444,   620,   640,   623,
     621,   615,   597,   598,   617,   599,   619,     0,     0,     0,
       0,     0,   782,     7,    29,    30,    31,    32,    33,    57,
      58,   705,   702,   701,   700,   703,   711,   720,   699,     0,
     732,   721,   736,   735,   731,   783,   697,   771,   681,   704,
     706,   707,   709,   683,   713,   718,   783,   724,   407,   406,
     729,   683,   734,   683,   738,   680,     0,     0,   783,     0,
       0,     0,     0,   468,   467,    81,     0,   472,     0,   269,
       0,   270,   771,     0,    97,   108,     0,     0,   235,   242,
     244,   245,   246,   253,   254,   247,   248,   224,   225,   251,
     252,     0,   771,   239,   240,   241,   230,   231,   232,   233,
     234,   267,   268,   756,   758,   757,   759,   461,   293,   459,
     771,   783,   756,   758,   757,   759,   460,   293,     0,   385,
       0,   384,     0,     0,     0,     0,   346,     0,   327,     0,
     783,     0,    71,   354,   129,   130,   131,   668,   352,     0,
     783,     0,     0,     0,   763,   764,    68,   756,   757,   293,
       0,     0,     0,     0,     0,     0,     0,   748,     0,   301,
     297,   783,   756,   757,   771,   756,   757,     0,     0,   747,
     331,   773,   278,   285,   280,   287,   336,    24,     0,   256,
      11,    34,     0,   783,     0,    22,    94,    18,   324,   776,
       0,   102,   760,   117,   771,   756,   757,   698,   624,     0,
     600,     0,   603,     0,   608,   605,     0,     0,   609,   236,
       0,   405,   397,   399,   771,   402,   395,     0,   679,   740,
     673,     0,     0,     0,   690,   712,     0,   678,   722,   723,
       0,   693,   733,     0,   695,   737,    48,   258,   382,   358,
     377,   783,   783,   583,   676,    50,   260,   359,   465,   469,
       0,   466,   473,   451,     0,    36,   304,     0,    39,   303,
     106,   100,     0,    55,    41,    53,     0,   276,   299,   215,
      37,     0,   317,   575,   581,   548,     0,     0,     0,   520,
     771,   517,   536,   614,     0,   574,    64,   491,   497,   499,
     501,   495,   494,   532,   496,   541,   544,   547,   553,   554,
     543,   504,   555,   505,   560,   561,   562,   565,   566,   567,
     568,   569,   571,   570,   572,   573,   551,    62,     0,   783,
     783,   458,    84,     0,   464,   285,   783,   783,   282,   457,
      82,   281,   320,   783,   386,   783,   344,   388,    72,   387,
     345,   482,     0,     0,   379,     0,     0,   760,   326,   771,
     756,   757,     0,     0,     0,     0,   129,   130,   133,   771,
       0,   771,     0,   453,    77,    42,   276,   216,    52,   223,
     143,   751,   302,   291,   783,   783,   464,   783,   783,   771,
     783,   771,   222,   274,   110,   464,   698,   445,   448,   625,
     629,   630,   631,   622,   632,   602,   604,   611,   607,   613,
     771,   404,     0,   708,     0,   739,   725,   409,   682,   710,
     683,   683,   719,   724,   783,   683,   730,   683,   707,   683,
       0,     0,   584,   585,   783,   586,   378,   380,     0,    12,
      14,   590,     0,     0,   783,    78,   783,   307,     0,     0,
      98,     0,   783,     0,     0,   783,     0,   563,   564,   130,
     579,     0,   522,   771,   523,   529,   771,     0,   516,     0,
       0,   519,   535,     0,   576,   648,   647,   649,     0,   577,
       0,   492,     0,     0,   542,   546,   558,   559,     0,   503,
     502,     0,     0,   552,   550,     0,     0,     0,    85,   774,
     783,     0,     0,    83,   381,     0,     0,     0,   389,   391,
       0,    73,   483,     0,   350,     0,   475,     0,   349,   464,
       0,     0,     0,     0,   464,   357,   743,    69,   454,   455,
       0,     0,   783,     0,     0,   279,   286,   335,   771,     0,
     626,   396,   398,   400,   403,     0,   686,     0,   688,   677,
       0,   694,     0,   691,   696,    49,   259,     0,     0,   588,
     589,     0,    51,   261,   771,     0,   433,   432,     0,     0,
     305,    38,    54,     0,   277,   756,   757,   771,   756,   757,
     771,   514,   527,   539,   524,   515,   530,   614,   771,   775,
     549,     0,   498,   493,   532,   500,   533,   537,   545,   540,
     556,   557,   580,   513,   509,   771,   771,   771,   771,   771,
     771,   257,    47,   220,    46,   221,     0,    44,   218,    45,
     219,   392,     0,   342,   343,     0,   347,   476,     0,     0,
     351,     0,   669,   353,     0,     0,   436,     0,     0,   446,
     627,     0,     0,   683,   683,   683,   683,     0,   587,     0,
     662,   663,   317,     0,   783,   783,   431,   771,     0,   707,
     415,   715,   716,   783,   727,   415,   415,   413,   470,   474,
     306,   464,   521,   525,     0,   531,     0,   518,   578,     0,
       0,   506,   507,   508,   510,   511,   512,    43,   217,     0,
       0,   488,     0,   477,   783,   355,   447,     0,     0,     0,
       0,   401,   687,     0,   684,   689,   692,   327,     0,   783,
       0,   783,    13,   412,     0,   434,     0,   416,   424,   422,
       0,   714,     0,   411,     0,   427,     0,   429,     0,   528,
     533,   534,   538,   783,   484,     0,     0,   478,   480,   481,
     479,   440,   771,   438,   441,   450,   449,     0,   683,   760,
     326,   771,   756,   757,   582,   435,   726,   414,   415,   415,
     327,     0,   717,   783,   415,   728,   415,   415,   526,     0,
     390,     0,   489,   490,     0,   437,   628,   685,   464,     0,
     419,     0,   421,   760,   326,   410,     0,   428,     0,   425,
     430,   783,   439,   415,   415,   415,   415,   486,   487,   485,
     420,     0,   417,   423,   426,   415,   418
};

  /* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
   -1111, -1111, -1111,   919, -1111,    56,   688,  -279, -1111,   126,
   -1111,   689, -1111,    26, -1111,  -475,  -441,   687, -1111, -1111,
     303,  2130,  2522, -1111,   -91,   -78, -1111, -1111,   -29, -1111,
    -453,   477,    -3,  1078,  -173,    31,   -70, -1111,  -436,     4,
    2761,  -352,  1079,   -55,   -11, -1111, -1111,    57, -1111,  3787,
   -1111,  1093, -1111,   468,  1189, -1111,   903,    76,   526,  -378,
      66,    95, -1111,  -419,  -216,    72, -1111,  -490,   -36, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,    40, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111, -1111,
   -1111, -1111, -1111, -1111,   432, -1111,   508,  1065,  -375, -1111,
     -47,  -787, -1111,  -843,  -836,   469,   276,  -408,    38, -1111,
     135,   685, -1111, -1111,   244, -1111,  -936, -1111,   -66,   862,
   -1111, -1111, -1111, -1111, -1111, -1111,   344, -1111, -1111,   -94,
     647, -1111, -1111, -1111,   895, -1111, -1111, -1111, -1111,  -812,
   -1111,   -58, -1111, -1111, -1111, -1111, -1111,  -567,  -388, -1111,
   -1111, -1111, -1111,   255, -1111, -1111,  -155, -1111,  -560,  -723,
    -895,  -997,  -859,  -494, -1111,   258, -1111, -1111, -1111,   260,
   -1111,  -668,   266, -1111, -1111, -1111,    36, -1111, -1111,    94,
     901,  1505, -1111,  1142,  1571,  1613,  2225, -1111,   739,  2283,
   -1111,  2352,  2451, -1111, -1111,   -59, -1111, -1111,  -111, -1111,
   -1111, -1111, -1111, -1111, -1111, -1111,    15, -1111, -1111, -1111,
   -1111,    14, -1111,  2793,  1473,  1161,  2779,  1707, -1111, -1111,
      35,   543,    27, -1111,  -256,  -532,  -309,  -188, -1056,  -513,
    -316,  -701,   530,     6,   529,    46, -1111, -1111,  -492, -1111,
    -689,  -692, -1110,    49,   536, -1111,  -633, -1111,   267,  -520,
   -1111, -1111, -1111,    21,  -447, -1111,  -357, -1111, -1111,   -84,
   -1111,   -24,    86,   634,   548,   183,  -221,   -62,     8,    -2
};

  /* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
      -1,     1,     2,    70,    71,    72,   248,   567,  1061,   568,
     266,   267,   480,   268,   471,    74,   774,    75,   601,   587,
     421,   218,   219,   872,   384,   386,   387,  1011,    78,    79,
     575,   254,    81,    82,   269,    83,    84,    85,   500,    86,
     221,   404,   405,   203,   204,   205,   662,   616,   207,    88,
     473,   375,    89,   580,   223,   274,   779,   617,   839,   459,
     460,   236,   237,   225,   445,   621,   768,   769,    90,   382,
     273,   486,   688,   852,   637,   865,   863,   652,   256,    92,
      93,    94,    95,    96,    97,    98,    99,   100,   101,   336,
     339,   751,   938,   855,  1005,  1006,   749,   257,   630,   848,
    1007,  1008,   396,   722,   723,   724,   725,   545,   731,   732,
    1257,  1218,  1219,  1157,  1065,  1066,  1135,  1242,  1243,   103,
     294,   506,   707,  1039,   897,  1139,   340,   104,   105,   337,
     572,   573,   759,   944,   576,   577,   762,   946,  1017,   856,
    1240,   853,  1012,  1125,  1271,  1299,  1190,   796,  1083,   798,
     799,   991,   992,   800,   969,   961,   963,   964,   965,   802,
     803,  1096,   967,   804,   805,   806,   807,   808,   809,   810,
     811,   812,   813,   814,   815,   816,   752,   934,  1058,   940,
     106,   107,   108,   109,   110,   111,   112,   517,   711,   113,
     519,   114,   115,   518,   520,   299,   302,   303,   511,   709,
     708,   899,  1040,  1140,  1200,   903,   116,   117,   300,   118,
     119,   120,   979,   228,   229,   123,   230,   231,   648,   864,
     325,   326,   327,   328,   918,   734,   547,   548,   549,   550,
     928,   552,   553,   554,   555,  1162,  1163,   556,   557,   558,
     559,   560,  1164,  1165,   561,   562,   563,   564,   565,   728,
     424,   653,   279,   463,   233,   126,   692,   619,   656,   651,
     428,   314,   455,   456,   834,   971,   491,   631,   391,   271
};

  /* YYTABLE[YYPACT[STATE-NUM]] -- What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule whose
     number is the opposite.  If YYTABLE_NINF, syntax error.  */
static const yytype_int16 yytable[] =
{
     127,   301,   620,   385,   298,   679,   389,   381,   551,   315,
     632,   429,   551,   220,   220,   345,   453,   245,   388,   569,
     427,   289,   618,   208,   627,   670,   726,   801,    73,   741,
      73,   744,   919,   670,   827,   315,   737,   232,   232,   309,
    1013,   801,    91,   208,    91,   679,   422,   924,   926,   289,
     462,   646,   571,   277,   281,   493,   227,   227,  1010,   495,
     128,   288,   289,   289,   289,   206,   701,   966,   546,  1042,
     674,   659,   546,   390,   329,   329,   276,   280,  1044,   316,
     272,   308,   224,   234,   270,   206,  1094,  1137,   226,   226,
     618,   481,   627,    91,    91,   826,   780,   290,   239,   220,
     921,   629,  -124,   628,   331,  1084,  1173,   927,   227,   826,
     629,   773,   330,   243,  1265,   246,   206,   240,   957,   958,
     693,   322,   323,   232,  1097,   290,   704,   275,  1158,     3,
    -120,   847,  -364,   227,   227,   -92,   713,   227,   395,   406,
     406,  1244,   227,   449,   980,   717,  -121,   771,   693,  -128,
     980,  -656,  -120,  -121,   332,  -106,   334,   439,   206,  -126,
    -364,  -364,   332,   353,   354,   738,   489,  -664,   425,  1265,
     582,   242,   477,   490,   226,   335,  -655,  1229,   542,   265,
    -656,  1138,  1230,   739,  -128,   773,   773,   980,  -124,   714,
     981,   514,   516,  -127,  -120,  -127,  -539,  -126,   718,   797,
    1174,   261,   543,  -123,   315,  -655,   505,   726,   911,  -364,
     365,   366,  -125,   797,  -111,   499,   329,   329,  1244,   875,
     878,  1214,   338,  -115,   446,  -756,   524,   525,   526,   527,
     446,  1268,   220,  -123,   220,   220,   464,   242,   679,   312,
     242,   313,   483,  1128,   923,   127,   331,   453,   312,   289,
     313,  -111,    91,   440,   441,   312,   232,   313,   232,  -125,
    -122,   933,   859,   247,   670,  -122,   670,  -112,  1106,  1109,
    -119,   333,   869,    73,   484,   227,   238,   227,   227,   333,
    -117,   227,  -757,   227,   693,  1231,   249,    91,   479,    91,
    -120,  -121,  -120,  -121,   693,   270,   634,   868,   498,  1042,
     936,   454,    91,   457,    91,  1201,   937,   226,   289,   226,
     461,   641,  -783,  -114,   331,  1097,  -118,   304,  1100,  1101,
    1097,  1232,  -128,  -756,  -128,   290,  -124,   315,  -124,   239,
     334,  -127,  -757,  -127,   324,  -126,   950,  -126,   265,   581,
     305,  -123,    73,  -123,   581,   937,  -116,   528,   584,   919,
    -125,   341,  -125,   926,  -114,   851,    91,   227,   227,   227,
     227,    91,   227,   227,   312,   636,   313,    91,   496,  1097,
     988,    91,   523,   870,   270,   475,   993,   220,   464,   998,
    -116,  -113,  1239,    91,   290,   492,  -665,  1003,  1046,  1048,
     265,   551,   490,  1051,   994,  1053,  -657,  1054,   898,   726,
    -771,   726,   962,  -122,  1049,  -122,   712,  1238,   712,   242,
    1232,   306,  1144,  -121,  -114,  -658,   670,   551,   989,   985,
     227,   990,    91,   348,   551,  -657,    91,   227,    91,   581,
     581,  1166,   499,  -112,  -114,   307,   960,  -114,   625,  -660,
     581,  -114,   227,  -326,  -658,   220,   464,  -116,   581,   581,
     644,   546,   342,   507,   645,   883,  1274,   626,  -659,   446,
     687,   446,   507,   242,   432,   671,   289,  -116,  -660,   265,
    -116,  -326,  -326,    60,  -116,  1088,   581,   826,   893,    80,
     346,    80,   372,   373,   374,   625,   208,  -659,   227,   433,
    -661,  -128,   826,    80,    80,   889,   785,  1107,  1110,   826,
     826,   220,   464,   431,  1297,   478,   625,   754,   509,   510,
     699,  -119,   633,   499,   635,   537,   919,   509,   510,  -661,
    -326,   773,   773,  -757,    91,   626,   773,   773,   206,   241,
      80,    80,  -127,  1263,   625,   937,   435,   854,  -115,   905,
     507,   515,   290,   729,   227,    80,   541,   289,   908,  -650,
    1087,   765,  -118,   626,   729,  1112,  1114,   775,  1191,  -653,
    1117,  1119,   625,   746,   862,   801,   753,   755,   322,   323,
      80,    80,   849,  1080,    80,   836,   442,  1077,  -650,    80,
     551,   626,  -664,   444,  1259,   919,  1023,  1038,  -653,   679,
     644,  1266,   654,   962,   959,   509,   510,   618,   773,   627,
     906,   443,  -123,   962,   962,   906,   227,  1004,   937,  -120,
     227,  1202,  1204,  1205,  1206,   986,   987,   670,   837,   446,
     227,   655,  -114,   290,   450,   841,   781,  -665,   726,   726,
     894,   775,   775,   826,   536,  1213,  1102,   220,   464,  -115,
     546,   773,  1129,  1223,   451,  -125,  1294,   220,   464,   452,
    -113,   880,   767,   537,  -121,   472,  1155,   841,   767,  -115,
    1000,  -756,  -115,   241,   289,  -116,  -115,   377,   242,   446,
      91,  -128,    91,   348,   883,  1187,   974,   208,   975,   976,
     227,   977,   377,  1153,   541,   542,   482,  -122,   828,    80,
     227,   841,    91,   227,   831,   378,   379,   956,   625,   235,
     781,   781,   499,   838,   890,   478,   833,  -113,   625,   543,
     378,   447,    80,   832,    80,    80,  1277,   626,    80,   206,
      80,   238,   840,   -91,    80,   978,    80,   626,   227,   551,
     923,   488,  -127,   377,   973,   838,   289,   797,   377,    80,
     290,    80,  1131,  1285,   380,   887,   494,  1189,  1159,   935,
     941,  -113,   497,  -771,   874,   888,   843,   242,   845,   448,
     833,   378,   476,   377,  1235,  1236,   378,   426,   831,   838,
     324,  -113,  -123,  1022,  -113,   432,  1032,  -662,  -113,  1028,
     383,   383,  -125,  -771,   383,   502,   242,   348,   521,  1169,
     833,   378,   503,    80,    80,    80,    80,    80,    80,    80,
      80,  1021,  -128,  -122,    80,  -662,  -662,   566,    80,  1155,
     448,   836,   290,   574,   586,  1234,  1155,   422,  1155,  -753,
      80,  -663,   468,   767,   837,  1000,  1093,   581,   581,   501,
     501,  1251,   469,   470,   581,   581,  1105,  1108,  -123,  -317,
     504,  1009,  -671,  1009,   370,   371,   372,   373,   374,  -663,
    -663,  -125,  -122,   693,  -662,   570,   890,    80,  1198,    80,
     579,  -650,   638,    80,    80,    80,   585,  -317,  -317,   522,
     642,  1155,   647,  1155,   664,   466,   833,   970,  1155,    80,
    1155,   665,   581,   581,   681,   581,   581,  1091,   446,  -650,
    -650,   683,   377,  1155,   742,  -754,   685,  -106,  -663,   660,
     661,  1159,    91,   485,   227,    91,   690,   487,  1159,   691,
     666,  -408,  -760,   700,   748,  1122,  -317,   745,   677,   678,
     378,   639,   729,   727,   733,    80,   736,   740,   465,   206,
     467,   750,  1059,   743,   772,   758,  -753,  -653,  -650,   844,
     835,  -753,  1067,   851,  1067,   833,   694,   854,   907,   909,
     581,   912,   220,   464,  -760,   833,   377,   914,  -327,   917,
    1132,    80,   945,  1159,   507,  -653,  -653,   775,   775,   640,
     952,   930,   775,   775,  -299,  1071,   942,  1072,    91,   949,
     948,    80,  -760,  -760,   378,   649,  -327,  -327,  1020,    91,
     951,    91,   972,   983,   915,   227,   377,   531,   581,   532,
     533,   534,   535,   915,   383,   383,   383,   383,   982,   529,
     530,   995,  -754,   625,  -653,   937,   984,  -754,   227,   509,
     510,   289,  1090,   999,   378,   954,   377,  1036,  1176,  -760,
     581,  -760,   626,   650,  -756,  -327,   781,   781,  1045,   507,
    1047,   781,   781,    80,   775,    91,   227,    80,   739,   833,
    1057,  1050,   833,  1052,   378,  1208,  1064,    80,  -300,  1086,
    1089,    91,   377,    80,  1060,  1210,   783,   102,   980,   102,
    1161,  1178,  1197,   955,   383,   833,  1207,  1141,  1196,   757,
    1142,   102,   102,  -301,   761,  1175,   763,   775,   680,   643,
     378,  1261,   508,   682,   509,   510,   684,  1149,  1179,   686,
    1180,    91,  1203,  1209,   936,    91,  1215,    80,    91,    80,
    1217,   900,   901,   781,   902,   696,  1222,    80,   102,   102,
     698,    46,    47,  1224,  1136,   289,  1226,    80,  -302,    80,
      80,  1121,  1228,   102,  1246,  1255,   842,    80,    80,   650,
    1260,   846,  1276,   850,  1272,  1273,  -756,   507,  1269,  1279,
    1136,  1281,   941,   729,   348,  1286,   781,  1288,   102,   102,
    -757,   729,   102,   833,   507,    80,   474,   102,   695,    91,
    1301,   361,   362,   697,   970,  1027,   393,    91,    91,   410,
     289,   730,   376,   507,   939,   873,  1270,  1154,  1043,   910,
    1068,  1168,  1009,    91,   833,   833,   833,  1216,   507,  1156,
     512,  1221,   509,   510,   507,   663,   220,   464,  1292,   753,
     369,   370,   371,   372,   373,   374,   770,   512,  1029,   509,
     510,   760,   886,  1161,    41,    42,    43,    44,  1161,    91,
    1161,  1009,  1161,  1298,   578,   507,   512,  1095,   509,   510,
      91,  1090,  1098,  1136,  1099,   278,  1092,  1254,  1212,   227,
     430,   513,   895,   509,   510,  1194,  1256,   710,   716,   509,
     510,   729,   423,   348,  1199,   922,  1258,   625,  1262,   920,
     925,  1177,     0,   929,     0,   227,   227,   102,     0,  1211,
     361,   362,     0,     0,     0,  1161,   626,  1161,   715,  1009,
     509,   510,  1161,     0,  1161,     0,     0,   996,   997,     0,
     102,     0,   102,   102,  1001,  1002,   102,  1161,   102,     0,
       0,    91,   102,     0,   102,  1233,     0,   367,   368,   369,
     370,   371,   372,   373,   374,     0,  1247,   102,  1275,   102,
     531,  1167,   532,   533,   534,   535,     0,   833,   968,    80,
     871,    80,    80,     0,   531,     0,   532,   533,   534,   535,
     536,     0,  1030,  1031,     0,  1033,  1034,     0,   913,     0,
       0,  1014,     0,   531,  1018,   532,   533,   534,   535,   537,
     317,   318,   319,   320,   321,   891,     0,   348,  1025,   720,
       0,   102,   102,   102,   102,   102,   102,   102,   102,     0,
       0,     0,   102,   539,   361,   362,   102,  1291,     0,   434,
     541,   542,   436,   437,   438,     0,     0,  1019,   102,     0,
       0,     0,   720,     0,     0,    80,     0,  1024,   721,  1241,
    1073,   532,   533,   534,   535,   543,    80,     0,    80,     0,
       0,     0,    80,     0,   370,   371,   372,   373,   374,     0,
       0,     0,     0,     0,     0,   102,     0,   102,     0,   795,
       0,   102,   102,   102,     0,    80,     0,     0,     0,     0,
       0,     0,     0,   795,     0,     0,     0,   102,  1116,     0,
       0,     0,     0,    80,    80,   122,     0,   122,    80,    80,
       0,     0,    80,    80,  1167,     0,     0,     0,   817,   915,
       0,  1167,     0,  1167,     0,     0,     0,     0,    80,     0,
    1134,     0,   817,     0,     0,  1026,     0,     0,     0,     0,
       0,  1081,     0,   102,  1085,  1123,     0,     0,  1124,     0,
       0,  1126,     0,  1035,     0,  1037,   122,   122,  1130,     0,
     292,  1133,     0,     0,     0,     0,     0,  1103,    80,     0,
       0,     0,    80,     0,  1041,    80,  1167,     0,  1167,   102,
      80,   383,     0,  1167,     0,  1167,     0,     0,   292,     0,
       0,     0,     0,   877,   879,     0,     0,     0,  1167,   102,
       0,   398,   408,   408,   408,  1143,     0,  1145,     0,     0,
     877,   879,  1146,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    80,  1160,     0,     0,   892,   531,     0,
     532,   533,   534,   535,   536,     0,    80,     0,     0,     0,
       0,     0,     0,     0,    80,    80,     0,     0,     0,     0,
       0,     0,     0,   537,  1104,  1171,     0,     0,     0,     0,
      80,   102,   667,   669,     0,   102,     0,     0,     0,  1195,
       0,   278,     0,     0,     0,   102,     0,   539,   795,   795,
       0,   102,   795,   540,   541,   542,  1184,  1185,  1186,     0,
       0,     0,     0,     0,     0,   383,    80,     0,     0,   795,
       0,     0,     0,     0,     0,     0,     0,    80,   669,   543,
       0,   278,   544,     0,     0,   122,    80,   817,   817,     0,
       0,   817,     0,   383,     0,   102,     0,   102,     0,   242,
       0,     0,     0,     0,     0,   102,  1245,     0,   817,   125,
       0,   125,    80,    80,  1172,   102,     0,   102,   102,     0,
     122,     0,   122,     0,     0,   102,   102,     0,     0,     0,
       0,     0,     0,  1248,     0,   122,     0,   122,     0,  1181,
    1182,  1183,     0,   735,     0,     0,     0,     0,    80,     0,
       0,     0,     0,   102,  1264,     0,  1267,     0,   292,     0,
     125,   125,     0,     0,   293,     0,     0,     0,     0,   766,
       0,     0,     0,     0,     0,   778,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   122,
       0,     0,   293,     0,   122,     0,     0,     0,     0,  1278,
     122,     0,     0,     0,   122,   399,   409,   409,     0,  1293,
       0,  1295,     0,     0,     0,     0,   122,   292,  1296,     0,
       0,     0,     0,   795,     0,     0,   795,     0,     0,     0,
       0,  1305,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   795,   795,   795,     0,     0,   795,   795,
    1225,  1227,   881,   795,   795,   122,     0,   882,     0,   122,
       0,   122,   817,     0,     0,   817,     0,     0,     0,     0,
       0,     0,   669,     0,   278,     0,     0,     0,     0,     0,
       0,     0,   817,   817,   817,     0,     0,   817,   817,     0,
       0,     0,   817,   817,     0,     0,     0,     0,     0,  1113,
    1115,     0,     0,     0,  1118,  1120,     0,     0,     0,     0,
     531,     0,   532,   533,   534,   535,   536,     0,     0,   125,
     916,     0,   383,   383,     0,     0,     0,   102,     0,   102,
     102,     0,     0,  1113,  1115,   537,  1118,  1120,     0,     0,
     932,     0,     0,  1280,  1282,     0,     0,     0,     0,  1287,
       0,  1289,  1290,   947,   125,     0,   125,   122,     0,   539,
       0,     0,     0,     0,     0,   540,   541,   542,     0,   125,
       0,   125,     0,     0,     0,   292,     0,     0,  1300,  1302,
    1303,  1304,     0,     0,     0,     0,     0,   795,     0,     0,
    1306,   543,   293,     0,   544,     0,   531,     0,   532,   533,
     534,   535,   536,   102,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   102,     0,   102,     0,     0,  1188,
     102,   537,     0,   125,   669,     0,   817,     0,   125,     0,
       0,     0,     0,     0,   125,   538,   795,  1188,   125,     0,
       0,   795,   795,   102,  1016,   539,     0,     0,     0,     0,
     125,   293,   541,   542,     0,     0,   292,     0,     0,     0,
     825,   102,   102,     0,     0,     0,   102,   102,     0,     0,
     102,   102,     0,     0,   825,   817,     0,   543,     0,     0,
     817,   817,    23,    24,    25,    26,   102,     0,     0,   125,
     795,     0,   818,   125,     0,   125,     0,     0,    32,    33,
      34,     0,     0,   122,     0,   122,   818,     0,    41,    42,
      43,    44,    45,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   122,   102,     0,     0,   817,
     102,   795,    76,   102,    76,     0,     0,  1070,   102,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     531,     0,   532,   533,   534,   535,   536,     0,   819,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
     817,     0,   819,   292,     0,   537,     0,     0,     0,     0,
       0,   102,     0,    76,    76,     0,     0,   286,   284,   538,
       0,   125,     0,     0,   102,     0,     0,     0,     0,   539,
     820,     0,   102,   102,  1127,   540,   541,   542,     0,   293,
       0,     0,     0,     0,   820,   286,     0,     0,   102,     0,
       0,     0,     0,     0,     0,   278,     0,     0,   286,   286,
     286,   543,     0,     0,   544,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   292,   896,   531,     0,   532,
     533,   534,   535,   536,   102,     0,     0,     0,  1170,   825,
     825,     0,     0,   825,     0,   102,     0,     0,     0,     0,
       0,     0,   537,     0,   102,     0,     0,     0,     0,     0,
     825,     0,     0,     0,     0,     0,   538,     0,     0,     0,
     293,   818,   818,   782,     0,   818,   539,     0,     0,     0,
     102,   102,   540,   541,   542,     0,     0,     0,     0,     0,
       0,     0,   818,     0,     0,     0,     0,  1193,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   543,     0,
       0,   544,     0,     0,     0,   122,   102,   125,   122,   125,
       0,     0,    76,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   819,   819,   125,
       0,   819,     0,     0,     0,     0,     0,   782,   782,     0,
       0,     0,     0,     0,     0,     0,     0,    76,   819,    76,
       0,  1237,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    76,     0,    76,     0,     0,     0,     0,   820,
     820,     0,     0,   820,     0,     0,     0,   293,     0,     0,
       0,   122,     0,     0,     0,   286,   904,     0,     0,     0,
     820,     0,   122,     0,   122,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   825,     0,     0,   825,     0,     0,
       0,     0,     0,     0,     0,     0,    76,     0,     0,     0,
       0,    76,     0,     0,   825,   825,   825,    76,     0,   825,
     825,    76,     0,     0,   825,   825,   818,     0,     0,   818,
       0,     0,     0,    76,   286,     0,     0,     0,   122,   293,
       0,     0,     0,     0,     0,     0,   818,   818,   818,     0,
       0,   818,   818,     0,   122,     0,   818,   818,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    76,     0,     0,     0,    76,     0,    76,     0,
       0,     0,     0,     0,    77,     0,    77,     0,     0,     0,
    1151,     0,   819,     0,   122,   819,     0,     0,   122,     0,
       0,   122,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   819,   819,   819,     0,     0,   819,   819,     0,
       0,     0,   819,   819,     0,     0,     0,     0,     0,   125,
       0,     0,   125,     0,   820,    77,    77,   820,     0,   287,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   820,   820,   820,     0,   825,   820,
     820,     0,   122,     0,   820,   820,     0,   287,     0,     0,
     122,   122,     0,     0,    76,     0,     0,     0,     0,     0,
     287,   287,   287,     0,     0,     0,   122,     0,     0,     0,
     818,     0,   286,     0,   408,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   125,     0,   825,     0,     0,
       0,     0,   825,   825,     0,     0,   125,     0,   125,     0,
       0,     0,   122,     0,     0,   348,   349,   350,   351,   352,
     353,   354,   355,   122,   357,   358,     0,     0,     0,   818,
       0,     0,   361,   362,   818,   818,     0,     0,     0,   408,
       0,     0,     0,     0,     0,     0,   819,     0,     0,     0,
       0,   825,     0,   782,   782,     0,     0,     0,   782,   782,
       0,     0,   125,   286,     0,     0,    76,   365,   366,   367,
     368,   369,   370,   371,   372,   373,   374,     0,   125,     0,
       0,     0,     0,   818,    77,     0,     0,     0,   820,     0,
       0,     0,   825,     0,   122,   819,     0,     0,     0,     0,
     819,   819,     0,     0,     0,     0,     0,     0,     0,     0,
      76,     0,    76,    87,  1152,    87,     0,     0,   125,    77,
       0,    77,   125,     0,   818,   125,     0,     0,     0,     0,
     782,   124,    76,   124,    77,     0,    77,   820,     0,     0,
      76,    76,   820,   820,     0,   121,     0,   121,     0,   819,
       0,     0,     0,     0,     0,     0,     0,   287,     0,     0,
       0,     0,   821,     0,    87,    87,     0,     0,     0,     0,
       0,     0,     0,   782,     0,     0,   821,     0,     0,     0,
     286,     0,   124,   124,     0,     0,   125,     0,    77,     0,
     819,   820,     0,    77,   125,   125,   121,   121,     0,    77,
     291,     0,     0,    77,     0,     0,     0,     0,     0,   394,
     125,     0,     0,     0,     0,    77,   287,     0,   409,     0,
     822,     0,     0,     0,     0,     0,     0,     0,   291,     0,
       0,     0,   820,     0,   822,     0,     0,     0,     0,     0,
       0,   397,   407,   407,   407,     0,   125,     0,     0,     0,
       0,     0,   286,     0,    77,     0,     0,   125,    77,     0,
      77,   348,  -784,  -784,  -784,  -784,   353,   354,     0,     0,
    -784,  -784,     0,   409,     0,     0,     0,     0,   361,   362,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   823,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   823,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   365,   366,   367,   368,   369,   370,   371,
     372,   373,   374,    87,     0,     0,     0,     0,   125,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   124,    76,     0,     0,    76,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   121,    77,     0,    87,     0,
      87,   821,   821,     0,     0,   821,     0,     0,     0,     0,
       0,     0,     0,    87,   287,    87,   124,     0,   124,     0,
       0,     0,   821,     0,     0,     0,     0,     0,   824,     0,
     121,   124,   121,   124,     0,     0,     0,     0,     0,     0,
       0,     0,   824,     0,     0,   121,     0,   121,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    76,   822,
     822,     0,     0,   822,     0,     0,     0,    87,   291,    76,
       0,    76,    87,     0,     0,     0,     0,     0,    87,     0,
     822,     0,    87,     0,     0,   124,     0,     0,     0,     0,
     124,     0,     0,     0,    87,   287,   124,     0,    77,   121,
     124,     0,     0,     0,   121,     0,     0,     0,     0,     0,
     121,     0,   124,     0,   121,     0,    76,    76,     0,     0,
       0,    76,    76,     0,     0,    76,   121,   291,   823,   823,
       0,     0,   823,    87,     0,     0,     0,    87,     0,    87,
       0,    76,    77,     0,    77,     0,     0,     0,     0,   823,
       0,   124,     0,     0,     0,   124,     0,   124,     0,     0,
       0,     0,     0,     0,    77,   121,     0,     0,     0,   121,
       0,   121,    77,    77,     0,     0,   821,   286,     0,   821,
       0,    76,     0,     0,     0,    76,     0,     0,    76,     0,
       0,     0,     0,    76,     0,     0,   821,   821,   821,     0,
       0,   821,   821,     0,     0,     0,   821,   821,     0,     0,
       0,     0,   287,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   824,   824,     0,
       0,   824,     0,     0,   822,    87,    76,   822,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   824,    76,
       0,     0,     0,   124,   822,   822,   822,    76,    76,   822,
     822,     0,     0,     0,   822,   822,     0,   121,     0,     0,
       0,     0,     0,    76,     0,     0,     0,     0,     0,     0,
       0,   286,     0,     0,   287,   291,     0,     0,     0,     0,
     348,   349,   350,   351,   352,   353,   354,   355,   356,   357,
     358,  -784,  -784,   823,     0,     0,   823,   361,   362,    76,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      76,     0,     0,   823,   823,   823,     0,     0,   823,   823,
       0,     0,     0,   823,   823,     0,   286,   776,     0,     0,
     821,     0,   365,   366,   367,   368,   369,   370,   371,   372,
     373,   374,     0,     0,     0,   124,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   291,     0,     0,     0,
       0,     0,     0,     0,    77,     0,     0,    77,     0,     0,
       0,    87,     0,    87,     0,     0,     0,     0,     0,   821,
       0,    76,     0,     0,   821,   821,     0,     0,   822,   124,
       0,   124,   824,    87,     0,   824,     0,     0,     0,     0,
       0,   776,   776,   121,     0,   121,     0,     0,     0,     0,
       0,   124,   824,   824,   824,     0,     0,   824,   824,   124,
     124,     0,   824,   824,     0,   121,     0,     0,     0,     0,
       0,     0,     0,   821,     0,     0,     0,   822,     0,     0,
      77,     0,   822,   822,     0,     0,     0,     0,     0,     0,
       0,    77,     0,    77,     0,     0,     0,   823,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   291,   821,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   822,     0,     0,     0,     0,     0,     0,    77,    77,
    -783,     0,     0,    77,    77,     0,   823,    77,  -783,  -783,
    -783,   823,   823,  -783,  -783,  -783,     0,  -783,     0,     0,
       0,     0,     0,    77,     0,  -783,  -783,  -783,     0,     0,
       0,     0,   822,     0,     0,     0,     0,  -783,  -783,     0,
    -783,  -783,  -783,  -783,  -783,   291,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   824,     0,     0,   287,
     823,     0,     0,    77,     0,     0,     0,    77,  -783,     0,
      77,     0,     0,     0,     0,    77,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  -783,  -783,     0,     0,
       0,   823,     0,    87,     0,   824,    87,     0,     0,     0,
     824,   824,     0,     0,     0,     0,     0,     0,    77,     0,
    -783,   124,     0,     0,   124,     0,     0,     0,     0,     0,
       0,    77,     0,     0,     0,   121,     0,     0,   121,    77,
      77,     0,     0,  -783,  -783,     0,     0,     0,   238,  -783,
       0,  -783,     0,  -783,     0,    77,     0,     0,     0,   824,
       0,     0,     0,   287,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    87,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      87,    77,    87,     0,     0,     0,     0,   124,     0,     0,
     824,     0,    77,     0,     0,     0,     0,     0,   124,     0,
     124,   121,     0,     0,     0,     0,     0,     0,   287,     0,
       0,     0,   121,     0,   121,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   776,   776,     0,
       0,     0,   776,   776,     0,     0,    87,     0,     0,     0,
       0,     0,     0,     0,     0,   124,   124,     0,     0,     0,
     124,   124,    87,     0,   124,     0,     0,     0,     0,     0,
       0,     0,     0,    77,     0,     0,     0,     0,   121,     0,
     124,     0,     0,   222,   222,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   121,     0,     0,     0,  1148,     0,
       0,     0,    87,     0,     0,     0,    87,     0,     0,    87,
       0,     0,     0,     0,   776,   255,   258,   259,   260,     0,
     124,     0,   222,   222,   124,     0,     0,   124,     0,     0,
    1150,     0,   124,     0,   121,   310,   311,     0,   121,     0,
       0,   121,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   776,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   222,
      87,     0,     0,     0,     0,   124,     0,     0,    87,    87,
       0,     0,     0,     0,     0,     0,     0,     0,   124,     0,
       0,     0,     0,     0,    87,     0,   124,   124,     0,     0,
       0,     0,   121,     0,     0,     0,     0,     0,     0,     0,
     121,   121,   124,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   121,     0,     0,     0,
      87,     0,     0,     0,   407,     0,     0,     0,     0,     0,
       0,    87,     0,     0,     0,     0,     0,     0,   124,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   124,
       0,     0,   121,     0,     0,     0,     0,     0,     0,    23,
      24,    25,    26,   121,     0,     0,     0,   222,     0,     0,
     222,   222,   222,     0,   310,    32,    33,    34,   783,   407,
       0,     0,   784,     0,     0,    41,    42,    43,    44,    45,
       0,     0,   222,     0,   222,   222,     0,     0,     0,     0,
       0,     0,    87,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   786,   787,     0,     0,
     124,     0,     0,     0,   788,     0,     0,   789,     0,     0,
     790,   791,     0,   792,   121,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   794,     0,
       0,     0,     0,     0,     0,   284,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   242,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   588,   589,   590,   591,   592,
       0,     0,   593,   594,   595,   596,   597,   598,   599,   600,
       0,   602,     0,     0,   603,   604,   605,   606,   607,   608,
     609,   610,   611,   612,     0,     0,     0,   222,     0,  -760,
       0,     0,     0,     0,     0,     0,     0,  -760,  -760,  -760,
       0,     0,  -760,  -760,  -760,     0,  -760,     0,     0,     0,
       0,     0,     0,     0,  -760,  -760,  -760,  -760,  -760,     0,
       0,     0,     0,     0,     0,     0,  -760,  -760,     0,  -760,
    -760,  -760,  -760,  -760,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     222,   222,     0,     0,     0,   222,     0,  -760,     0,   222,
       0,     0,     0,     0,     0,   260,  -760,  -760,  -760,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,     0,
     689,     0,     0,  -760,  -760,  -760,  -760,     0,   884,  -760,
       0,     0,     0,   347,     0,  -760,   222,     0,     0,   222,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -760,
       0,   222,  -760,     0,     0,  -124,  -760,  -760,  -760,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,   719,     0,
       0,     0,  -760,  -760,  -760,  -760,     0,     0,  -760,  -760,
    -760,     0,  -760,     0,     0,   348,   349,   350,   351,   352,
     353,   354,   355,   356,   357,   358,   359,   360,     0,     0,
       0,   222,   361,   362,     0,     0,     0,     0,   363,     0,
       0,     0,     0,   747,     0,     0,     0,   756,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   222,     0,     0,
       0,     0,     0,   777,     0,   364,     0,   365,   366,   367,
     368,   369,   370,   371,   372,   373,   374,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   222,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   222,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   876,   876,     0,
     222,   747,   756,   876,     0,   222,     0,     0,     0,     0,
       0,     0,     0,     0,   876,   876,     0,     0,     0,     0,
     222,     0,   222,     0,     0,     0,     0,     0,     0,     0,
       0,   876,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   222,     0,
       0,     0,     0,  -783,     4,     0,     5,     6,     7,     8,
       9,     0,     0,     0,    10,    11,     0,     0,   222,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
       0,   222,     0,    20,    21,    22,    23,    24,    25,    26,
       0,     0,    27,     0,     0,     0,     0,     0,    28,    29,
      30,    31,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,   348,   349,   350,   351,   352,   353,   354,    48,    49,
     357,   358,     0,     0,     0,     0,     0,     0,   361,   362,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,    52,   222,     0,    53,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,   222,   365,   366,   367,   368,   369,   370,   371,
     372,   373,   374,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    67,    68,    69,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -783,     0,  -783,     0,     0,    23,
      24,    25,    26,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    32,    33,    34,   783,     0,
       0,     0,   784,     0,   785,    41,    42,    43,    44,    45,
       0,     0,     0,     0,     0,     0,     0,  1055,  1056,     0,
       0,     0,     0,   537,     0,     0,     0,     0,     0,  1062,
    1063,     0,     0,     0,     0,   222,   786,   787,     0,     0,
    1074,     0,   222,     0,   788,     0,     0,   789,     0,     0,
     790,   791,     0,   792,   541,     0,    58,    59,   793,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   794,     0,
       0,     0,  1111,   876,   876,   284,     0,     0,   876,   876,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -650,
       0,     0,   222,     0,     0,     0,     0,  -650,  -650,  -650,
       0,     0,  -650,  -650,  -650,     0,  -650,   876,   876,     0,
     876,   876,     0,   222,  -650,     0,  -650,  -650,  -650,     0,
       0,     0,     0,     0,     0,     0,  -650,  -650,     0,  -650,
    -650,  -650,  -650,  -650,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   222,     0,     0,     0,
     876,     0,     0,     0,     0,     0,     0,  -650,     0,     0,
       0,     0,     0,     0,     0,     0,  -650,  -650,  -650,  -650,
    -650,  -650,  -650,  -650,  -650,  -650,  -650,  -650,  -650,     0,
       0,     0,     0,  -650,  -650,  -650,  -650,     0,  -650,  -650,
       0,     0,     0,   876,     0,  -650,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   222,     0,     0,     0,  -650,
       0,   876,  -650,     0,     0,  -650,  -650,  -650,  -650,  -650,
    -650,  -650,  -650,  -650,  -650,  -650,  -650,  -650,     0,     0,
       0,     0,     0,  -650,  -650,  -650,  -653,     0,  -650,  -650,
    -650,     0,  -650,     0,  -653,  -653,  -653,     0,     0,  -653,
    -653,  -653,     0,  -653,     0,     0,     0,     0,   953,     0,
       0,  -653,     0,  -653,  -653,  -653,     0,     0,     0,   222,
       0,     0,     0,  -653,  -653,     0,  -653,  -653,  -653,  -653,
    -653,     0,     0,     0,     0,     0,   222,     0,     0,     0,
       0,   348,   349,   350,   351,   352,   353,   354,   355,   356,
     357,   358,   359,   360,  -653,     0,     0,     0,   361,   362,
       0,     0,     0,  -653,  -653,  -653,  -653,  -653,  -653,  -653,
    -653,  -653,  -653,  -653,  -653,  -653,     0,     0,     0,     0,
    -653,  -653,  -653,  -653,     0,  -653,  -653,     0,     0,     0,
       0,   364,  -653,   365,   366,   367,   368,   369,   370,   371,
     372,   373,   374,     0,     0,     0,  -653,     0,     0,  -653,
    -271,     0,  -653,  -653,  -653,  -653,  -653,  -653,  -653,  -653,
    -653,  -653,  -653,  -653,  -653,     0,     0,     0,     0,     0,
    -653,  -653,  -653,  -761,     0,  -653,  -653,  -653,     0,  -653,
       0,  -761,  -761,  -761,     0,     0,  -761,  -761,  -761,     0,
    -761,     0,     0,     0,     0,   931,     0,     0,  -761,  -761,
    -761,  -761,  -761,     0,     0,     0,     0,     0,     0,     0,
    -761,  -761,     0,  -761,  -761,  -761,  -761,  -761,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   348,   349,
     350,   351,   352,   353,   354,   355,   356,   357,   358,   359,
     360,  -761,     0,     0,     0,   361,   362,     0,     0,     0,
    -761,  -761,  -761,  -761,  -761,  -761,  -761,  -761,  -761,  -761,
    -761,  -761,  -761,     0,     0,     0,     0,  -761,  -761,  -761,
    -761,     0,     0,  -761,     0,     0,     0,     0,   364,  -761,
     365,   366,   367,   368,   369,   370,   371,   372,   373,   374,
       0,     0,     0,  -761,     0,     0,  -761,     0,     0,     0,
    -761,  -761,  -761,  -761,  -761,  -761,  -761,  -761,  -761,  -761,
    -761,  -761,     0,     0,     0,     0,  -761,  -761,  -761,  -761,
    -762,     0,  -761,  -761,  -761,     0,  -761,     0,  -762,  -762,
    -762,     0,     0,  -762,  -762,  -762,     0,  -762,     0,     0,
       0,     0,   943,     0,     0,  -762,  -762,  -762,  -762,  -762,
       0,     0,     0,     0,     0,     0,     0,  -762,  -762,     0,
    -762,  -762,  -762,  -762,  -762,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   348,   349,   350,   351,   352,
     353,   354,   355,   356,   357,   358,   359,   360,  -762,     0,
       0,     0,   361,   362,     0,     0,     0,  -762,  -762,  -762,
    -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,
       0,     0,     0,     0,  -762,  -762,  -762,  -762,     0,     0,
    -762,     0,     0,     0,     0,   364,  -762,   365,   366,   367,
     368,   369,   370,   371,   372,   373,   374,     0,     0,     0,
    -762,     0,     0,  -762,     0,     0,     0,  -762,  -762,  -762,
    -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,     0,
       0,     0,     0,  -762,  -762,  -762,  -762,  -326,     0,  -762,
    -762,  -762,     0,  -762,     0,  -326,  -326,  -326,     0,     0,
    -326,  -326,  -326,     0,  -326,     0,     0,     0,     0,   953,
       0,     0,  -326,     0,  -326,  -326,  -326,     0,     0,     0,
       0,     0,     0,     0,  -326,  -326,     0,  -326,  -326,  -326,
    -326,  -326,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   348,   349,   350,   351,   352,   353,   354,   355,
     356,   357,   358,   359,   360,  -326,     0,     0,     0,   361,
     362,     0,     0,     0,  -326,  -326,  -326,  -326,  -326,  -326,
    -326,  -326,  -326,  -326,  -326,  -326,  -326,     0,     0,     0,
       0,  -326,  -326,  -326,  -326,     0,   885,  -326,     0,     0,
       0,     0,   364,  -326,   365,   366,   367,   368,   369,   370,
     371,   372,   373,   374,     0,     0,     0,  -326,     0,     0,
    -326,     0,     0,  -126,  -326,  -326,  -326,  -326,  -326,  -326,
    -326,  -326,  -326,  -326,  -326,  -326,     0,     0,     0,     0,
       0,  -326,  -326,  -326,  -463,     0,  -326,  -326,  -326,     0,
    -326,     0,  -463,  -463,  -463,     0,     0,  -463,  -463,  -463,
       0,  -463,     0,     0,     0,     0,     0,     0,     0,  -463,
    -463,  -463,  -463,     0,     0,     0,     0,     0,     0,     0,
       0,  -463,  -463,     0,  -463,  -463,  -463,  -463,  -463,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   348,
     349,   350,   351,   352,   353,   354,   355,   356,   357,   358,
     359,   360,  -463,     0,     0,     0,   361,   362,     0,     0,
       0,  -463,  -463,  -463,  -463,  -463,  -463,  -463,  -463,  -463,
    -463,  -463,  -463,  -463,     0,     0,     0,     0,  -463,  -463,
    -463,  -463,     0,     0,  -463,     0,     0,     0,     0,   364,
    -463,   365,   366,   367,   368,   369,   370,   371,   372,   373,
     374,     0,     0,     0,  -463,     0,     0,     0,     0,     0,
       0,  -463,     0,  -463,  -463,  -463,  -463,  -463,  -463,  -463,
    -463,  -463,  -463,     0,     0,     0,     0,  -463,  -463,  -463,
    -463,  -318,   238,  -463,  -463,  -463,     0,  -463,     0,  -318,
    -318,  -318,     0,     0,  -318,  -318,  -318,     0,  -318,     0,
       0,     0,     0,     0,     0,     0,  -318,     0,  -318,  -318,
    -318,     0,     0,     0,     0,     0,     0,     0,  -318,  -318,
       0,  -318,  -318,  -318,  -318,  -318,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -318,
       0,     0,     0,     0,     0,     0,     0,     0,  -318,  -318,
    -318,  -318,  -318,  -318,  -318,  -318,  -318,  -318,  -318,  -318,
    -318,     0,     0,     0,     0,  -318,  -318,  -318,  -318,     0,
       0,  -318,     0,     0,     0,     0,     0,  -318,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -318,     0,     0,  -318,     0,     0,     0,  -318,  -318,
    -318,  -318,  -318,  -318,  -318,  -318,  -318,  -318,  -318,  -318,
       0,     0,     0,     0,     0,  -318,  -318,  -318,  -783,     0,
    -318,  -318,  -318,     0,  -318,     0,  -783,  -783,  -783,     0,
       0,  -783,  -783,  -783,     0,  -783,     0,     0,     0,     0,
       0,     0,     0,  -783,  -783,  -783,  -783,     0,     0,     0,
       0,     0,     0,     0,     0,  -783,  -783,     0,  -783,  -783,
    -783,  -783,  -783,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  -783,     0,     0,     0,
       0,     0,     0,     0,     0,  -783,  -783,  -783,  -783,  -783,
    -783,  -783,  -783,  -783,  -783,  -783,  -783,  -783,     0,     0,
       0,     0,  -783,  -783,  -783,  -783,     0,     0,  -783,     0,
       0,     0,     0,     0,  -783,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -783,     0,
       0,     0,     0,     0,     0,  -783,     0,  -783,  -783,  -783,
    -783,  -783,  -783,  -783,  -783,  -783,  -783,     0,     0,     0,
       0,  -783,  -783,  -783,  -783,  -333,   238,  -783,  -783,  -783,
       0,  -783,     0,  -333,  -333,  -333,     0,     0,  -333,  -333,
    -333,     0,  -333,     0,     0,     0,     0,     0,     0,     0,
    -333,     0,  -333,  -333,     0,     0,     0,     0,     0,     0,
       0,     0,  -333,  -333,     0,  -333,  -333,  -333,  -333,  -333,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -333,     0,     0,     0,     0,     0,     0,
       0,     0,  -333,  -333,  -333,  -333,  -333,  -333,  -333,  -333,
    -333,  -333,  -333,  -333,  -333,     0,     0,     0,     0,  -333,
    -333,  -333,  -333,     0,     0,  -333,     0,     0,     0,     0,
       0,  -333,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -333,     0,     0,     0,     0,
       0,     0,  -333,     0,  -333,  -333,  -333,  -333,  -333,  -333,
    -333,  -333,  -333,  -333,     0,     0,     0,     0,     0,  -333,
    -333,  -333,  -760,   235,  -333,  -333,  -333,     0,  -333,     0,
    -760,  -760,  -760,     0,     0,     0,  -760,  -760,     0,  -760,
       0,     0,     0,     0,     0,     0,     0,  -760,  -760,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -760,
    -760,     0,  -760,  -760,  -760,  -760,  -760,     0,     0,     0,
       0,     0,     0,     0,   348,   349,   350,   351,   352,   353,
     354,   355,   356,   357,   358,   359,   360,     0,     0,     0,
    -760,   361,   362,     0,     0,     0,     0,     0,     0,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,
    -760,  -760,     0,     0,     0,     0,  -760,  -760,  -760,  -760,
       0,   829,  -760,     0,   364,     0,   365,   366,   367,   368,
     369,   370,   371,   372,   373,   374,     0,     0,     0,     0,
       0,     0,  -760,     0,     0,     0,     0,     0,  -124,  -760,
     242,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,
    -760,     0,     0,     0,     0,  -760,  -760,  -760,  -115,  -760,
       0,  -760,     0,  -760,     0,  -760,     0,  -760,  -760,  -760,
       0,     0,     0,  -760,  -760,     0,  -760,     0,     0,     0,
       0,     0,     0,     0,  -760,  -760,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  -760,  -760,     0,  -760,
    -760,  -760,  -760,  -760,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -760,     0,     0,
       0,     0,     0,     0,     0,     0,  -760,  -760,  -760,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,     0,
       0,     0,     0,  -760,  -760,  -760,  -760,     0,   829,  -760,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -760,
       0,     0,     0,     0,     0,  -124,  -760,     0,  -760,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,     0,     0,
       0,     0,  -760,  -760,  -760,  -760,  -326,     0,  -760,     0,
    -760,     0,  -760,     0,  -326,  -326,  -326,     0,     0,     0,
    -326,  -326,     0,  -326,     0,     0,     0,     0,     0,     0,
       0,  -326,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -326,  -326,     0,  -326,  -326,  -326,  -326,
    -326,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -326,     0,     0,     0,     0,     0,
       0,     0,     0,  -326,  -326,  -326,  -326,  -326,  -326,  -326,
    -326,  -326,  -326,  -326,  -326,  -326,     0,     0,     0,     0,
    -326,  -326,  -326,  -326,     0,   830,  -326,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  -326,     0,     0,     0,
       0,     0,  -126,  -326,     0,  -326,  -326,  -326,  -326,  -326,
    -326,  -326,  -326,  -326,  -326,     0,     0,     0,     0,     0,
    -326,  -326,  -117,  -326,     0,  -326,     0,  -326,     0,  -326,
       0,  -326,  -326,  -326,     0,     0,     0,  -326,  -326,     0,
    -326,     0,     0,     0,     0,     0,     0,     0,  -326,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -326,  -326,     0,  -326,  -326,  -326,  -326,  -326,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -326,     0,     0,     0,     0,     0,     0,     0,     0,
    -326,  -326,  -326,  -326,  -326,  -326,  -326,  -326,  -326,  -326,
    -326,  -326,  -326,     0,     0,     0,     0,  -326,  -326,  -326,
    -326,     0,   830,  -326,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -326,     0,     0,     0,     0,     0,  -126,
    -326,     0,  -326,  -326,  -326,  -326,  -326,  -326,  -326,  -326,
    -326,  -326,     0,     0,     0,     0,     0,  -326,  -326,  -326,
       0,     0,  -326,     0,  -326,   262,  -326,     5,     6,     7,
       8,     9,  -783,  -783,  -783,    10,    11,     0,     0,  -783,
      12,     0,    13,    14,    15,    16,    17,    18,    19,     0,
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
       0,     0,     0,     0,     0,  -783,   262,  -783,     5,     6,
       7,     8,     9,     0,     0,  -783,    10,    11,     0,  -783,
    -783,    12,     0,    13,    14,    15,    16,    17,    18,    19,
       0,     0,     0,     0,     0,    20,    21,    22,    23,    24,
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
       0,     0,     0,     0,     0,     0,  -783,   262,  -783,     5,
       6,     7,     8,     9,     0,     0,  -783,    10,    11,     0,
       0,  -783,    12,  -783,    13,    14,    15,    16,    17,    18,
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
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    67,    68,    69,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -783,   262,  -783,
       5,     6,     7,     8,     9,     0,     0,  -783,    10,    11,
       0,     0,  -783,    12,     0,    13,    14,    15,    16,    17,
      18,    19,  -783,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,    27,     0,     0,     0,
       0,     0,    28,    29,   263,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,    52,     0,     0,    53,    54,
       0,    55,    56,     0,    57,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    67,    68,    69,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -783,   262,
    -783,     5,     6,     7,     8,     9,     0,     0,  -783,    10,
      11,     0,     0,  -783,    12,     0,    13,    14,    15,    16,
      17,    18,    19,     0,     0,     0,     0,     0,    20,    21,
      22,    23,    24,    25,    26,     0,     0,    27,     0,     0,
       0,     0,     0,    28,    29,   263,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,    52,     0,     0,    53,
      54,     0,    55,    56,     0,    57,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     0,     0,     0,     0,   262,     0,     5,     6,     7,
       8,     9,     0,  -783,  -783,    10,    11,    67,    68,    69,
      12,     0,    13,    14,    15,    16,    17,    18,    19,  -783,
       0,  -783,     0,     0,    20,    21,    22,    23,    24,    25,
      26,     0,     0,    27,     0,     0,     0,     0,     0,    28,
      29,   263,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    50,    51,     0,     0,     0,     0,
       0,     0,    52,     0,     0,    53,    54,     0,    55,    56,
       0,    57,     0,     0,    58,    59,    60,    61,    62,    63,
      64,    65,    66,     0,     0,     0,     0,     0,     0,     0,
       0,   262,     0,     5,     6,     7,     8,     9,     0,     0,
       0,    10,    11,    67,    68,    69,    12,     0,    13,    14,
      15,    16,    17,    18,    19,  -783,     0,  -783,     0,     0,
      20,    21,    22,    23,    24,    25,    26,     0,     0,    27,
       0,     0,     0,     0,     0,    28,    29,   263,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    48,    49,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      50,    51,     0,     0,     0,     0,     0,     0,    52,     0,
       0,   264,    54,     0,    55,    56,     0,    57,     0,     0,
      58,    59,    60,    61,    62,    63,    64,    65,    66,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    67,
      68,    69,     0,     0,     0,     0,     0,     0,     0,  -783,
       0,  -783,   262,  -783,     5,     6,     7,     8,     9,     0,
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
      67,    68,    69,     0,     0,     0,     0,     0,     0,     0,
    -783,     0,  -783,     4,  -783,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,     0,
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
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    67,    68,    69,     0,     0,  -783,     0,     0,     0,
       0,     0,     0,  -783,   262,  -783,     5,     6,     7,     8,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
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
       0,     0,    67,    68,    69,     0,     0,  -783,     0,     0,
       0,     0,     0,     0,  -783,   262,  -783,     5,     6,     7,
       8,     9,     0,     0,  -783,    10,    11,     0,     0,     0,
      12,     0,    13,    14,    15,    16,    17,    18,    19,     0,
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
       0,   262,     0,     5,     6,     7,     8,     9,     0,     0,
       0,    10,    11,    67,    68,    69,    12,     0,    13,    14,
      15,    16,    17,    18,    19,  -783,     0,  -783,     0,     0,
      20,    21,    22,    23,    24,    25,    26,     0,     0,    27,
       0,     0,     0,     0,     0,    28,    29,   263,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    48,    49,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      50,    51,     0,     0,     0,     0,     0,     0,    52,     0,
       0,    53,    54,     0,    55,    56,     0,    57,     0,     0,
      58,    59,    60,    61,    62,    63,    64,    65,    66,     0,
    -783,     0,     0,     0,     0,     0,     0,     0,     0,     5,
       6,     7,     0,     9,     0,     0,     0,    10,    11,    67,
      68,    69,    12,     0,    13,    14,    15,    16,    17,    18,
      19,  -783,     0,  -783,     0,     0,    20,    21,    22,    23,
      24,    25,    26,     0,     0,   209,     0,     0,     0,     0,
       0,     0,    29,     0,     0,    32,    33,    34,    35,    36,
      37,    38,    39,    40,   210,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,   211,     0,     0,   212,    54,     0,
      55,    56,     0,   213,   214,   215,    58,    59,   216,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     5,     6,     7,     0,     9,
       0,     0,     0,    10,    11,    67,   217,    69,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,   242,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,     0,    29,     0,
       0,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    50,    51,     0,     0,     0,     0,     0,     0,
     211,     0,     0,   212,    54,     0,    55,    56,     0,     0,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     5,     6,     7,     0,     9,     0,     0,     0,    10,
      11,    67,    68,    69,    12,     0,    13,    14,    15,    16,
      17,    18,    19,   312,     0,   313,     0,     0,    20,    21,
      22,    23,    24,    25,    26,     0,     0,    27,     0,     0,
       0,     0,     0,     0,    29,     0,     0,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,   211,     0,     0,   212,
      54,     0,    55,    56,     0,     0,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     5,     6,     7,
       8,     9,     0,     0,     0,    10,    11,    67,    68,    69,
      12,     0,    13,    14,    15,    16,    17,    18,    19,     0,
       0,   242,     0,     0,    20,    21,    22,    23,    24,    25,
      26,     0,     0,    27,     0,     0,     0,     0,     0,    28,
      29,    30,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    50,    51,     0,     0,     0,     0,
       0,     0,    52,     0,     0,    53,    54,     0,    55,    56,
       0,    57,     0,     0,    58,    59,    60,    61,    62,    63,
      64,    65,    66,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     5,     6,     7,     8,     9,     0,     0,
       0,    10,    11,    67,    68,    69,    12,     0,    13,    14,
      15,    16,    17,    18,    19,   522,     0,     0,     0,     0,
      20,    21,    22,    23,    24,    25,    26,     0,     0,    27,
       0,     0,     0,     0,     0,    28,    29,   263,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    48,    49,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      50,    51,     0,     0,     0,     0,     0,     0,    52,     0,
       0,    53,    54,     0,    55,    56,     0,    57,     0,     0,
      58,    59,    60,    61,    62,    63,    64,    65,    66,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    67,
      68,    69,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   522,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,   139,   140,   141,   142,   143,   144,   145,   146,
     147,   148,   149,   150,   151,   152,     0,     0,     0,   153,
     154,   155,   411,   412,   413,   414,   160,   161,   162,     0,
       0,     0,     0,     0,   163,   164,   165,   166,   415,   416,
     417,   418,   171,    37,    38,   419,    40,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   173,   174,   175,   176,   177,   178,
     179,   180,   181,     0,     0,   182,   183,     0,     0,     0,
       0,   184,   185,   186,   187,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   188,   189,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   190,   191,
     192,   193,   194,   195,   196,   197,   198,   199,     0,   200,
     201,     0,     0,     0,     0,     0,   202,   420,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,   139,   140,
     141,   142,   143,   144,   145,   146,   147,   148,   149,   150,
     151,   152,     0,     0,     0,   153,   154,   155,   156,   157,
     158,   159,   160,   161,   162,     0,     0,     0,     0,     0,
     163,   164,   165,   166,   167,   168,   169,   170,   171,   295,
     296,   172,   297,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,     0,     0,     0,
       0,     0,   202,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,   148,   149,   150,   151,   152,     0,     0,     0,
     153,   154,   155,   156,   157,   158,   159,   160,   161,   162,
       0,     0,     0,     0,     0,   163,   164,   165,   166,   167,
     168,   169,   170,   171,   244,     0,   172,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   173,   174,   175,   176,   177,
     178,   179,   180,   181,     0,     0,   182,   183,     0,     0,
       0,     0,   184,   185,   186,   187,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   188,   189,     0,
       0,    59,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   190,
     191,   192,   193,   194,   195,   196,   197,   198,   199,     0,
     200,   201,     0,     0,     0,     0,     0,   202,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,   139,   140,
     141,   142,   143,   144,   145,   146,   147,   148,   149,   150,
     151,   152,     0,     0,     0,   153,   154,   155,   156,   157,
     158,   159,   160,   161,   162,     0,     0,     0,     0,     0,
     163,   164,   165,   166,   167,   168,   169,   170,   171,     0,
       0,   172,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,    59,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,     0,     0,     0,
       0,     0,   202,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,   148,   149,   150,   151,   152,     0,     0,     0,
     153,   154,   155,   156,   157,   158,   159,   160,   161,   162,
       0,     0,     0,     0,     0,   163,   164,   165,   166,   167,
     168,   169,   170,   171,     0,     0,   172,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   173,   174,   175,   176,   177,
     178,   179,   180,   181,     0,     0,   182,   183,     0,     0,
       0,     0,   184,   185,   186,   187,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   188,   189,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   190,
     191,   192,   193,   194,   195,   196,   197,   198,   199,     0,
     200,   201,     5,     6,     7,     0,     9,   202,     0,     0,
      10,    11,     0,     0,     0,    12,     0,    13,    14,    15,
     250,   251,    18,    19,     0,     0,     0,     0,     0,    20,
     252,   253,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,   282,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   283,     0,     0,
     212,    54,     0,    55,    56,     0,     0,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     5,     6,     7,     0,     9,     0,     0,   284,    10,
      11,     0,     0,     0,    12,   285,    13,    14,    15,   250,
     251,    18,    19,     0,     0,     0,     0,     0,    20,   252,
     253,    23,    24,    25,    26,     0,     0,   209,     0,     0,
       0,     0,     0,     0,   282,     0,     0,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   283,     0,     0,   212,
      54,     0,    55,    56,     0,     0,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     8,     9,     0,     0,   284,    10,    11,
       0,     0,     0,    12,   583,    13,    14,    15,    16,    17,
      18,    19,     0,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,    27,     0,     0,     0,
       0,     0,    28,    29,    30,    31,    32,    33,    34,    35,
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
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
     213,   214,   215,    58,    59,   216,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     8,     9,     0,     0,     0,
      10,    11,    67,   217,    69,    12,     0,    13,    14,    15,
      16,    17,    18,    19,     0,     0,     0,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,    27,     0,
       0,     0,     0,     0,    28,    29,     0,    31,    32,    33,
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
       0,     0,     0,     0,     0,    50,   458,     0,     0,     0,
       0,     0,     0,   211,     0,     0,   212,    54,     0,    55,
      56,     0,   213,   214,   215,    58,    59,   216,    61,    62,
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
       0,     0,   212,    54,     0,    55,    56,     0,   668,   214,
     215,    58,    59,   216,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      67,   217,    69,    12,     0,    13,    14,    15,   250,   251,
      18,    19,     0,     0,     0,     0,     0,    20,   252,   253,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,   210,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,   458,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,   668,   214,   215,    58,    59,   216,
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
     213,   214,     0,    58,    59,   216,    61,    62,    63,    64,
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
     212,    54,     0,    55,    56,     0,     0,   214,   215,    58,
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
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,   211,     0,     0,   212,    54,     0,    55,
      56,     0,   668,   214,     0,    58,    59,   216,    61,    62,
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
       0,     0,   212,    54,     0,    55,    56,     0,     0,   214,
       0,    58,    59,   216,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      67,   217,    69,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,   764,     0,     0,    58,    59,    60,
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
     764,     0,     0,    58,    59,    60,    61,    62,    63,    64,
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
     212,    54,     0,    55,    56,     0,  1015,     0,     0,    58,
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
      56,     0,  1069,     0,     0,    58,    59,    60,    61,    62,
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
       0,     0,   212,    54,     0,    55,    56,     0,  1192,     0,
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
       0,    55,    56,     0,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    67,   217,    69,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
       0,     0,     0,    20,    21,    22,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
       0,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    67,   217,    69,    12,     0,    13,    14,    15,
      16,    17,    18,    19,     0,     0,     0,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,    27,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,   211,     0,     0,
     212,    54,     0,    55,    56,     0,     0,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    67,    68,
      69,    12,     0,    13,    14,    15,   250,   251,    18,    19,
       0,     0,     0,     0,     0,    20,   252,   253,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,   282,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   283,     0,     0,   343,    54,     0,    55,
      56,     0,   344,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
       0,     0,     0,    12,   284,    13,    14,    15,   250,   251,
      18,    19,     0,     0,     0,     0,     0,    20,   252,   253,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,   282,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   392,     0,     0,    53,    54,
       0,    55,    56,     0,    57,     0,     0,    58,    59,    60,
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
       0,     0,     0,   402,     0,     0,     0,   403,     0,     0,
     212,    54,     0,    55,    56,     0,     0,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,     0,     0,     0,    12,   284,    13,
      14,    15,   250,   251,    18,    19,     0,     0,     0,     0,
       0,    20,   252,   253,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,   282,     0,     0,
      32,    33,    34,   400,    36,    37,    38,   401,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   403,
       0,     0,   212,    54,     0,    55,    56,     0,     0,     0,
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
       0,   283,     0,     0,   343,    54,     0,    55,    56,     0,
       0,     0,     0,    58,    59,    60,    61,    62,    63,    64,
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
       0,     0,     0,  1147,     0,     0,   212,    54,     0,    55,
      56,     0,     0,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
       0,     0,     0,    12,   284,    13,    14,    15,   250,   251,
      18,    19,     0,     0,     0,     0,     0,    20,   252,   253,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,   282,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,    23,    24,    25,    26,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    32,
      33,    34,   783,     0,     0,     0,   784,     0,     0,    41,
      42,    43,    44,    45,     0,  1220,     0,     0,   212,    54,
       0,    55,    56,     0,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
     786,   787,     0,     0,     0,     0,     0,     0,   788,     0,
       0,   789,     0,     0,   790,   791,   284,  1082,     0,     0,
      58,    59,    60,    61,    62,    63,    64,    65,    66,    23,
      24,    25,    26,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   794,     0,     0,    32,    33,    34,   783,   284,
       0,     0,   784,     0,     0,    41,    42,    43,    44,    45,
       0,     0,    23,    24,    25,    26,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    32,    33,
      34,   783,     0,     0,     0,   784,   786,   787,    41,    42,
      43,    44,    45,     0,   788,     0,     0,   789,     0,     0,
     790,   791,     0,   792,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,   786,
     787,     0,     0,     0,     0,     0,     0,   788,   794,     0,
     789,     0,     0,   790,   791,   284,     0,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,   613,   614,
       0,     0,   615,     0,     0,     0,     0,     0,     0,     0,
       0,   794,     0,     0,     0,     0,     0,     0,   284,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   622,   623,
       0,     0,   624,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   672,   614,
       0,     0,   673,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   675,   623,
       0,     0,   676,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   702,   614,
       0,     0,   703,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   705,   623,
       0,     0,   706,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   857,   614,
       0,     0,   858,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   860,   623,
       0,     0,   861,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   866,   614,
       0,     0,   867,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   657,   623,
       0,     0,   658,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,  1075,   614,
       0,     0,  1076,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,  1078,   623,
       0,     0,  1079,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,  1249,   614,
       0,     0,  1250,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,  1252,   623,
       0,     0,  1253,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,  1283,   614,
       0,     0,  1284,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   657,   623,
       0,     0,   658,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,     0,     0,
       0,     0,     0,   202
};

static const yytype_int16 yycheck[] =
{
       2,    60,   380,    94,    59,   452,    97,    91,   324,    71,
     385,   105,   328,    16,    17,    85,   232,    28,    96,   328,
     104,    57,   379,     8,   381,   444,   539,   587,     2,   561,
       4,   563,   733,   452,   601,    97,   556,    16,    17,    68,
     852,   601,     2,    28,     4,   492,   101,   736,   740,    85,
     238,   403,   331,    55,    56,   276,    16,    17,   845,   280,
       4,    57,    98,    99,   100,     8,   502,   790,   324,   912,
     448,   428,   328,    97,    76,    77,    55,    56,   914,    71,
      54,    67,    16,    17,    53,    28,   981,    29,    16,    17,
     447,   264,   449,    53,    54,   587,   586,    57,    22,   102,
     733,    13,    25,   382,    77,   964,    52,   740,    68,   601,
      13,   586,    77,    27,  1224,    29,    59,    22,   786,   787,
     477,    37,    38,   102,   983,    85,   504,    55,  1064,     0,
      25,    27,    68,    93,    94,   126,    66,    97,    98,    99,
     100,  1197,   102,   227,   100,    66,    25,   583,   505,    25,
     100,    68,    13,    13,    26,   146,    80,   216,   101,    25,
      96,    97,    26,    82,    83,    34,   146,    99,   102,  1279,
     343,   153,   256,   153,   102,    80,    68,  1174,   109,    53,
      97,   123,  1179,    52,    13,   660,   661,   100,    13,   119,
     146,   302,   303,    13,   126,    25,   146,    13,   119,   587,
     146,    56,   133,    13,   266,    97,   290,   720,   721,   145,
     129,   130,    13,   601,   146,   285,   218,   219,  1274,   660,
     661,  1157,    28,   146,   226,   148,   317,   318,   319,   320,
     232,  1228,   235,    25,   237,   238,   238,   153,   685,   151,
     153,   153,   266,   146,   736,   247,   219,   463,   151,   285,
     153,   146,   212,   218,   219,   151,   235,   153,   237,    25,
      25,   751,   640,   143,   683,    13,   685,   146,   991,   992,
     146,   143,   650,   247,   266,   235,   148,   237,   238,   143,
     146,   241,   148,   243,   641,  1180,   143,   247,   262,   249,
     151,   151,   153,   153,   651,   264,   387,   649,   284,  1142,
       9,   235,   262,   237,   264,  1141,    15,   235,   344,   237,
     238,   395,   126,    25,   287,  1174,   146,   152,   986,   987,
    1179,  1180,   151,   148,   153,   285,   151,   389,   153,   253,
     254,   151,   148,   153,   148,   151,   772,   153,   212,   341,
     152,   151,   316,   153,   346,    15,    25,   321,   344,  1050,
     151,   126,   153,  1045,   146,    25,   316,   317,   318,   319,
     320,   321,   322,   323,   151,   389,   153,   327,   282,  1228,
      97,   331,   316,   652,   343,   249,    34,   380,   380,   832,
     146,   146,  1194,   343,   344,   146,    99,   840,   920,   921,
     264,   707,   153,   925,    52,   927,    68,   929,   707,   912,
     144,   914,   790,   151,   924,   153,   517,  1194,   519,   153,
    1269,   152,  1045,   126,   126,    68,   835,   733,   145,   807,
     380,   148,   382,    77,   740,    97,   386,   387,   388,   431,
     432,  1064,   502,   146,   146,   152,   788,   149,   381,    68,
     442,   153,   402,    68,    97,   448,   448,   126,   450,   451,
      52,   707,   146,    66,    56,   671,   146,   381,    68,   461,
     471,   463,    66,   153,    99,   444,   502,   146,    97,   343,
     149,    96,    97,   112,   153,   969,   478,   969,   699,     2,
     126,     4,   136,   137,   138,   428,   471,    97,   448,   146,
      68,   126,   984,    16,    17,   683,    58,   991,   992,   991,
     992,   504,   504,    99,  1291,    99,   449,   569,   121,   122,
     489,   146,   386,   583,   388,    77,  1217,   121,   122,    97,
     145,   996,   997,   148,   484,   449,  1001,  1002,   471,   148,
      53,    54,   126,  1222,   477,    15,    56,    17,    25,   152,
      66,    67,   502,   545,   504,    68,   108,   583,   152,    68,
     112,   580,   146,   477,   556,   996,   997,   586,  1125,    68,
    1001,  1002,   505,   566,   642,  1125,   568,   570,    37,    38,
      93,    94,   634,   961,    97,    99,   126,   955,    97,   102,
     896,   505,    99,   146,  1217,  1286,   865,   896,    97,  1036,
      52,  1224,    68,   981,    56,   121,   122,   954,  1073,   956,
     711,   100,   126,   991,   992,   716,   566,    14,    15,   126,
     570,  1143,  1144,  1145,  1146,    88,    89,  1036,    99,   621,
     580,    97,   146,   583,    99,   627,   586,    99,  1141,  1142,
     700,   660,   661,  1125,    58,  1155,   988,   640,   640,   126,
     896,  1116,  1017,  1163,    99,   126,  1279,   650,   650,   146,
      25,   662,   580,    77,   126,   148,  1064,   659,   586,   146,
      99,   148,   149,   148,   700,   146,   153,    68,   153,   671,
     630,   126,   632,    77,   890,  1116,    52,   662,    54,    55,
     640,    57,    68,  1058,   108,   109,   149,   126,   602,   212,
     650,   693,   652,   653,   618,    96,    97,   781,   641,   148,
     660,   661,   772,   627,   683,    99,   620,   146,   651,   133,
      96,    97,   235,   618,   237,   238,  1248,   641,   241,   662,
     243,   148,   627,   126,   247,   101,   249,   651,   688,  1045,
    1222,   150,   126,    68,   793,   659,   772,  1125,    68,   262,
     700,   264,  1021,  1263,   145,    99,   144,  1122,  1064,   751,
     752,   126,    56,   149,   659,    99,   630,   153,   632,   145,
     674,    96,    97,    68,    40,    41,    96,    97,   692,   693,
     148,   146,   126,   864,   149,    99,    99,    68,   153,   873,
      93,    94,   126,   150,    97,   146,   153,    77,    77,  1068,
     704,    96,    97,   316,   317,   318,   319,   320,   321,   322,
     323,   863,   126,   126,   327,    96,    97,   126,   331,  1217,
     145,    99,   772,   106,   346,  1190,  1224,   872,  1226,    26,
     343,    68,    54,   751,    99,    99,   981,   829,   830,   286,
     287,  1209,    64,    65,   836,   837,   991,   992,   126,    68,
     145,   843,   126,   845,   134,   135,   136,   137,   138,    96,
      97,   126,   126,  1210,   145,   126,   835,   380,  1137,   382,
      70,    68,    56,   386,   387,   388,   146,    96,    97,   151,
      25,  1279,   130,  1281,   126,   241,   790,   791,  1286,   402,
    1288,   126,   884,   885,   149,   887,   888,   978,   890,    96,
      97,   146,    68,  1301,    52,    26,   146,   146,   145,   431,
     432,  1217,   862,   269,   864,   865,   144,   273,  1224,   144,
     442,   126,    26,   146,    10,  1006,   145,    52,   450,   451,
      96,    97,   924,   146,   146,   448,   146,   146,   241,   872,
     243,     8,   934,   146,   146,   144,   143,    68,   145,    13,
     146,   148,   944,    25,   946,   859,   478,    17,   152,   152,
     952,   146,   955,   955,    68,   869,    68,   146,    68,   126,
    1022,   484,   144,  1279,    66,    96,    97,   996,   997,   145,
     126,    44,  1001,  1002,   146,   949,    44,   951,   938,    44,
     146,   504,    96,    97,    96,    97,    96,    97,   862,   949,
      44,   951,    52,   146,   727,   955,    68,    52,  1000,    54,
      55,    56,    57,   736,   317,   318,   319,   320,   131,   322,
     323,   128,   143,   956,   145,    15,   146,   148,   978,   121,
     122,  1057,   124,   150,    96,    97,    68,   146,  1087,   143,
    1032,   145,   956,   145,   148,   145,   996,   997,   146,    66,
     146,  1001,  1002,   566,  1073,  1005,  1006,   570,    52,   963,
     100,   146,   966,   146,    96,    97,   131,   580,   146,   146,
     144,  1021,    68,   586,   938,  1149,    52,     2,   100,     4,
    1064,   149,   151,   145,   387,   989,    56,   146,   149,   571,
     146,    16,    17,   146,   576,   146,   578,  1116,   454,   402,
      96,    97,   119,   459,   121,   122,   462,  1057,   146,   465,
     146,  1061,   146,   145,     9,  1065,   131,   630,  1068,   632,
     146,    54,    55,  1073,    57,   481,   146,   640,    53,    54,
     486,    64,    65,   146,  1038,  1161,   146,   650,   146,   652,
     653,  1005,   146,    68,   144,   131,   628,   660,   661,   145,
      56,   633,   120,   635,  1235,  1236,   148,    66,   146,   146,
    1064,   146,  1154,  1155,    77,   146,  1116,   146,    93,    94,
     148,  1163,    97,  1077,    66,   688,   247,   102,   480,  1129,
     146,    94,    95,   484,  1088,   872,    98,  1137,  1138,   100,
    1216,   547,    89,    66,   752,   659,  1233,  1061,   912,   720,
     946,  1065,  1194,  1153,  1108,  1109,  1110,  1159,    66,  1064,
     119,  1161,   121,   122,    66,   124,  1209,  1209,  1274,  1211,
     133,   134,   135,   136,   137,   138,   582,   119,   874,   121,
     122,   574,   674,  1217,    59,    60,    61,    62,  1222,  1189,
    1224,  1233,  1226,  1291,   339,    66,   119,   982,   121,   122,
    1200,   124,   984,  1157,   984,    56,   980,  1211,  1154,  1209,
     108,   119,   704,   121,   122,  1129,  1216,   119,   519,   121,
     122,  1263,   101,    77,  1138,   736,  1217,  1210,  1222,   733,
     740,  1088,    -1,   743,    -1,  1235,  1236,   212,    -1,  1153,
      94,    95,    -1,    -1,    -1,  1279,  1210,  1281,   119,  1291,
     121,   122,  1286,    -1,  1288,    -1,    -1,   829,   830,    -1,
     235,    -1,   237,   238,   836,   837,   241,  1301,   243,    -1,
      -1,  1271,   247,    -1,   249,  1189,    -1,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,  1200,   262,  1242,   264,
      52,  1064,    54,    55,    56,    57,    -1,  1251,   790,   862,
     653,   864,   865,    -1,    52,    -1,    54,    55,    56,    57,
      58,    -1,   884,   885,    -1,   887,   888,    -1,   724,    -1,
      -1,   853,    -1,    52,   856,    54,    55,    56,    57,    77,
      40,    41,    42,    43,    44,   688,    -1,    77,   870,   101,
      -1,   316,   317,   318,   319,   320,   321,   322,   323,    -1,
      -1,    -1,   327,   101,    94,    95,   331,  1271,    -1,   210,
     108,   109,   213,   214,   215,    -1,    -1,   859,   343,    -1,
      -1,    -1,   101,    -1,    -1,   938,    -1,   869,   107,    52,
     952,    54,    55,    56,    57,   133,   949,    -1,   951,    -1,
      -1,    -1,   955,    -1,   134,   135,   136,   137,   138,    -1,
      -1,    -1,    -1,    -1,    -1,   380,    -1,   382,    -1,   587,
      -1,   386,   387,   388,    -1,   978,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   601,    -1,    -1,    -1,   402,  1000,    -1,
      -1,    -1,    -1,   996,   997,     2,    -1,     4,  1001,  1002,
      -1,    -1,  1005,  1006,  1217,    -1,    -1,    -1,   587,  1222,
      -1,  1224,    -1,  1226,    -1,    -1,    -1,    -1,  1021,    -1,
    1032,    -1,   601,    -1,    -1,   871,    -1,    -1,    -1,    -1,
      -1,   963,    -1,   448,   966,  1007,    -1,    -1,  1010,    -1,
      -1,  1013,    -1,   889,    -1,   891,    53,    54,  1020,    -1,
      57,  1023,    -1,    -1,    -1,    -1,    -1,   989,  1061,    -1,
      -1,    -1,  1065,    -1,   910,  1068,  1279,    -1,  1281,   484,
    1073,   864,    -1,  1286,    -1,  1288,    -1,    -1,    85,    -1,
      -1,    -1,    -1,   660,   661,    -1,    -1,    -1,  1301,   504,
      -1,    98,    99,   100,   101,  1045,    -1,  1047,    -1,    -1,
     677,   678,  1052,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1116,  1064,    -1,    -1,   694,    52,    -1,
      54,    55,    56,    57,    58,    -1,  1129,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1137,  1138,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    77,   990,  1077,    -1,    -1,    -1,    -1,
    1153,   566,   443,   444,    -1,   570,    -1,    -1,    -1,  1131,
      -1,   452,    -1,    -1,    -1,   580,    -1,   101,   786,   787,
      -1,   586,   790,   107,   108,   109,  1108,  1109,  1110,    -1,
      -1,    -1,    -1,    -1,    -1,   978,  1189,    -1,    -1,   807,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1200,   489,   133,
      -1,   492,   136,    -1,    -1,   212,  1209,   786,   787,    -1,
      -1,   790,    -1,  1006,    -1,   630,    -1,   632,    -1,   153,
      -1,    -1,    -1,    -1,    -1,   640,  1198,    -1,   807,     2,
      -1,     4,  1235,  1236,  1080,   650,    -1,   652,   653,    -1,
     247,    -1,   249,    -1,    -1,   660,   661,    -1,    -1,    -1,
      -1,    -1,    -1,  1203,    -1,   262,    -1,   264,    -1,  1105,
    1106,  1107,    -1,   554,    -1,    -1,    -1,    -1,  1271,    -1,
      -1,    -1,    -1,   688,  1224,    -1,  1226,    -1,   285,    -1,
      53,    54,    -1,    -1,    57,    -1,    -1,    -1,    -1,   580,
      -1,    -1,    -1,    -1,    -1,   586,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   316,
      -1,    -1,    85,    -1,   321,    -1,    -1,    -1,    -1,  1251,
     327,    -1,    -1,    -1,   331,    98,    99,   100,    -1,  1279,
      -1,  1281,    -1,    -1,    -1,    -1,   343,   344,  1288,    -1,
      -1,    -1,    -1,   961,    -1,    -1,   964,    -1,    -1,    -1,
      -1,  1301,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   981,   982,   983,    -1,    -1,   986,   987,
    1165,  1166,   663,   991,   992,   382,    -1,   668,    -1,   386,
      -1,   388,   961,    -1,    -1,   964,    -1,    -1,    -1,    -1,
      -1,    -1,   683,    -1,   685,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   981,   982,   983,    -1,    -1,   986,   987,    -1,
      -1,    -1,   991,   992,    -1,    -1,    -1,    -1,    -1,   996,
     997,    -1,    -1,    -1,  1001,  1002,    -1,    -1,    -1,    -1,
      52,    -1,    54,    55,    56,    57,    58,    -1,    -1,   212,
     731,    -1,  1235,  1236,    -1,    -1,    -1,   862,    -1,   864,
     865,    -1,    -1,  1030,  1031,    77,  1033,  1034,    -1,    -1,
     751,    -1,    -1,  1258,  1259,    -1,    -1,    -1,    -1,  1264,
      -1,  1266,  1267,   764,   247,    -1,   249,   484,    -1,   101,
      -1,    -1,    -1,    -1,    -1,   107,   108,   109,    -1,   262,
      -1,   264,    -1,    -1,    -1,   502,    -1,    -1,  1293,  1294,
    1295,  1296,    -1,    -1,    -1,    -1,    -1,  1125,    -1,    -1,
    1305,   133,   285,    -1,   136,    -1,    52,    -1,    54,    55,
      56,    57,    58,   938,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   949,    -1,   951,    -1,    -1,  1116,
     955,    77,    -1,   316,   835,    -1,  1125,    -1,   321,    -1,
      -1,    -1,    -1,    -1,   327,    91,  1174,  1134,   331,    -1,
      -1,  1179,  1180,   978,   855,   101,    -1,    -1,    -1,    -1,
     343,   344,   108,   109,    -1,    -1,   583,    -1,    -1,    -1,
     587,   996,   997,    -1,    -1,    -1,  1001,  1002,    -1,    -1,
    1005,  1006,    -1,    -1,   601,  1174,    -1,   133,    -1,    -1,
    1179,  1180,    33,    34,    35,    36,  1021,    -1,    -1,   382,
    1228,    -1,   587,   386,    -1,   388,    -1,    -1,    49,    50,
      51,    -1,    -1,   630,    -1,   632,   601,    -1,    59,    60,
      61,    62,    63,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   652,  1061,    -1,    -1,  1228,
    1065,  1269,     2,  1068,     4,    -1,    -1,   948,  1073,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      52,    -1,    54,    55,    56,    57,    58,    -1,   587,   110,
     111,   112,   113,   114,   115,   116,   117,   118,    -1,    -1,
    1269,    -1,   601,   700,    -1,    77,    -1,    -1,    -1,    -1,
      -1,  1116,    -1,    53,    54,    -1,    -1,    57,   139,    91,
      -1,   484,    -1,    -1,  1129,    -1,    -1,    -1,    -1,   101,
     587,    -1,  1137,  1138,  1015,   107,   108,   109,    -1,   502,
      -1,    -1,    -1,    -1,   601,    85,    -1,    -1,  1153,    -1,
      -1,    -1,    -1,    -1,    -1,  1036,    -1,    -1,    98,    99,
     100,   133,    -1,    -1,   136,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   772,   148,    52,    -1,    54,
      55,    56,    57,    58,  1189,    -1,    -1,    -1,  1069,   786,
     787,    -1,    -1,   790,    -1,  1200,    -1,    -1,    -1,    -1,
      -1,    -1,    77,    -1,  1209,    -1,    -1,    -1,    -1,    -1,
     807,    -1,    -1,    -1,    -1,    -1,    91,    -1,    -1,    -1,
     583,   786,   787,   586,    -1,   790,   101,    -1,    -1,    -1,
    1235,  1236,   107,   108,   109,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   807,    -1,    -1,    -1,    -1,  1128,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   133,    -1,
      -1,   136,    -1,    -1,    -1,   862,  1271,   630,   865,   632,
      -1,    -1,   212,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   786,   787,   652,
      -1,   790,    -1,    -1,    -1,    -1,    -1,   660,   661,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   247,   807,   249,
      -1,  1192,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   262,    -1,   264,    -1,    -1,    -1,    -1,   786,
     787,    -1,    -1,   790,    -1,    -1,    -1,   700,    -1,    -1,
      -1,   938,    -1,    -1,    -1,   285,   709,    -1,    -1,    -1,
     807,    -1,   949,    -1,   951,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   961,    -1,    -1,   964,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   316,    -1,    -1,    -1,
      -1,   321,    -1,    -1,   981,   982,   983,   327,    -1,   986,
     987,   331,    -1,    -1,   991,   992,   961,    -1,    -1,   964,
      -1,    -1,    -1,   343,   344,    -1,    -1,    -1,  1005,   772,
      -1,    -1,    -1,    -1,    -1,    -1,   981,   982,   983,    -1,
      -1,   986,   987,    -1,  1021,    -1,   991,   992,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   382,    -1,    -1,    -1,   386,    -1,   388,    -1,
      -1,    -1,    -1,    -1,     2,    -1,     4,    -1,    -1,    -1,
    1057,    -1,   961,    -1,  1061,   964,    -1,    -1,  1065,    -1,
      -1,  1068,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   981,   982,   983,    -1,    -1,   986,   987,    -1,
      -1,    -1,   991,   992,    -1,    -1,    -1,    -1,    -1,   862,
      -1,    -1,   865,    -1,   961,    53,    54,   964,    -1,    57,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   981,   982,   983,    -1,  1125,   986,
     987,    -1,  1129,    -1,   991,   992,    -1,    85,    -1,    -1,
    1137,  1138,    -1,    -1,   484,    -1,    -1,    -1,    -1,    -1,
      98,    99,   100,    -1,    -1,    -1,  1153,    -1,    -1,    -1,
    1125,    -1,   502,    -1,  1161,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   938,    -1,  1174,    -1,    -1,
      -1,    -1,  1179,  1180,    -1,    -1,   949,    -1,   951,    -1,
      -1,    -1,  1189,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    84,  1200,    86,    87,    -1,    -1,    -1,  1174,
      -1,    -1,    94,    95,  1179,  1180,    -1,    -1,    -1,  1216,
      -1,    -1,    -1,    -1,    -1,    -1,  1125,    -1,    -1,    -1,
      -1,  1228,    -1,   996,   997,    -1,    -1,    -1,  1001,  1002,
      -1,    -1,  1005,   583,    -1,    -1,   586,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,  1021,    -1,
      -1,    -1,    -1,  1228,   212,    -1,    -1,    -1,  1125,    -1,
      -1,    -1,  1269,    -1,  1271,  1174,    -1,    -1,    -1,    -1,
    1179,  1180,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     630,    -1,   632,     2,  1057,     4,    -1,    -1,  1061,   247,
      -1,   249,  1065,    -1,  1269,  1068,    -1,    -1,    -1,    -1,
    1073,     2,   652,     4,   262,    -1,   264,  1174,    -1,    -1,
     660,   661,  1179,  1180,    -1,     2,    -1,     4,    -1,  1228,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   285,    -1,    -1,
      -1,    -1,   587,    -1,    53,    54,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1116,    -1,    -1,   601,    -1,    -1,    -1,
     700,    -1,    53,    54,    -1,    -1,  1129,    -1,   316,    -1,
    1269,  1228,    -1,   321,  1137,  1138,    53,    54,    -1,   327,
      57,    -1,    -1,   331,    -1,    -1,    -1,    -1,    -1,    98,
    1153,    -1,    -1,    -1,    -1,   343,   344,    -1,  1161,    -1,
     587,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    85,    -1,
      -1,    -1,  1269,    -1,   601,    -1,    -1,    -1,    -1,    -1,
      -1,    98,    99,   100,   101,    -1,  1189,    -1,    -1,    -1,
      -1,    -1,   772,    -1,   382,    -1,    -1,  1200,   386,    -1,
     388,    77,    78,    79,    80,    81,    82,    83,    -1,    -1,
      86,    87,    -1,  1216,    -1,    -1,    -1,    -1,    94,    95,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   587,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   601,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,   212,    -1,    -1,    -1,    -1,  1271,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   212,   862,    -1,    -1,   865,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   212,   484,    -1,   247,    -1,
     249,   786,   787,    -1,    -1,   790,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   262,   502,   264,   247,    -1,   249,    -1,
      -1,    -1,   807,    -1,    -1,    -1,    -1,    -1,   587,    -1,
     247,   262,   249,   264,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   601,    -1,    -1,   262,    -1,   264,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   938,   786,
     787,    -1,    -1,   790,    -1,    -1,    -1,   316,   285,   949,
      -1,   951,   321,    -1,    -1,    -1,    -1,    -1,   327,    -1,
     807,    -1,   331,    -1,    -1,   316,    -1,    -1,    -1,    -1,
     321,    -1,    -1,    -1,   343,   583,   327,    -1,   586,   316,
     331,    -1,    -1,    -1,   321,    -1,    -1,    -1,    -1,    -1,
     327,    -1,   343,    -1,   331,    -1,   996,   997,    -1,    -1,
      -1,  1001,  1002,    -1,    -1,  1005,   343,   344,   786,   787,
      -1,    -1,   790,   382,    -1,    -1,    -1,   386,    -1,   388,
      -1,  1021,   630,    -1,   632,    -1,    -1,    -1,    -1,   807,
      -1,   382,    -1,    -1,    -1,   386,    -1,   388,    -1,    -1,
      -1,    -1,    -1,    -1,   652,   382,    -1,    -1,    -1,   386,
      -1,   388,   660,   661,    -1,    -1,   961,  1057,    -1,   964,
      -1,  1061,    -1,    -1,    -1,  1065,    -1,    -1,  1068,    -1,
      -1,    -1,    -1,  1073,    -1,    -1,   981,   982,   983,    -1,
      -1,   986,   987,    -1,    -1,    -1,   991,   992,    -1,    -1,
      -1,    -1,   700,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   786,   787,    -1,
      -1,   790,    -1,    -1,   961,   484,  1116,   964,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   807,  1129,
      -1,    -1,    -1,   484,   981,   982,   983,  1137,  1138,   986,
     987,    -1,    -1,    -1,   991,   992,    -1,   484,    -1,    -1,
      -1,    -1,    -1,  1153,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,  1161,    -1,    -1,   772,   502,    -1,    -1,    -1,    -1,
      77,    78,    79,    80,    81,    82,    83,    84,    85,    86,
      87,    88,    89,   961,    -1,    -1,   964,    94,    95,  1189,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1200,    -1,    -1,   981,   982,   983,    -1,    -1,   986,   987,
      -1,    -1,    -1,   991,   992,    -1,  1216,   586,    -1,    -1,
    1125,    -1,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,    -1,    -1,    -1,   586,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   583,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   862,    -1,    -1,   865,    -1,    -1,
      -1,   630,    -1,   632,    -1,    -1,    -1,    -1,    -1,  1174,
      -1,  1271,    -1,    -1,  1179,  1180,    -1,    -1,  1125,   630,
      -1,   632,   961,   652,    -1,   964,    -1,    -1,    -1,    -1,
      -1,   660,   661,   630,    -1,   632,    -1,    -1,    -1,    -1,
      -1,   652,   981,   982,   983,    -1,    -1,   986,   987,   660,
     661,    -1,   991,   992,    -1,   652,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1228,    -1,    -1,    -1,  1174,    -1,    -1,
     938,    -1,  1179,  1180,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   949,    -1,   951,    -1,    -1,    -1,  1125,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   700,  1269,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,  1228,    -1,    -1,    -1,    -1,    -1,    -1,   996,   997,
       0,    -1,    -1,  1001,  1002,    -1,  1174,  1005,     8,     9,
      10,  1179,  1180,    13,    14,    15,    -1,    17,    -1,    -1,
      -1,    -1,    -1,  1021,    -1,    25,    26,    27,    -1,    -1,
      -1,    -1,  1269,    -1,    -1,    -1,    -1,    37,    38,    -1,
      40,    41,    42,    43,    44,   772,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1125,    -1,    -1,  1057,
    1228,    -1,    -1,  1061,    -1,    -1,    -1,  1065,    68,    -1,
    1068,    -1,    -1,    -1,    -1,  1073,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    96,    97,    -1,    -1,
      -1,  1269,    -1,   862,    -1,  1174,   865,    -1,    -1,    -1,
    1179,  1180,    -1,    -1,    -1,    -1,    -1,    -1,  1116,    -1,
     120,   862,    -1,    -1,   865,    -1,    -1,    -1,    -1,    -1,
      -1,  1129,    -1,    -1,    -1,   862,    -1,    -1,   865,  1137,
    1138,    -1,    -1,   143,   144,    -1,    -1,    -1,   148,   149,
      -1,   151,    -1,   153,    -1,  1153,    -1,    -1,    -1,  1228,
      -1,    -1,    -1,  1161,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   938,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     949,  1189,   951,    -1,    -1,    -1,    -1,   938,    -1,    -1,
    1269,    -1,  1200,    -1,    -1,    -1,    -1,    -1,   949,    -1,
     951,   938,    -1,    -1,    -1,    -1,    -1,    -1,  1216,    -1,
      -1,    -1,   949,    -1,   951,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   996,   997,    -1,
      -1,    -1,  1001,  1002,    -1,    -1,  1005,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   996,   997,    -1,    -1,    -1,
    1001,  1002,  1021,    -1,  1005,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1271,    -1,    -1,    -1,    -1,  1005,    -1,
    1021,    -1,    -1,    16,    17,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1021,    -1,    -1,    -1,  1057,    -1,
      -1,    -1,  1061,    -1,    -1,    -1,  1065,    -1,    -1,  1068,
      -1,    -1,    -1,    -1,  1073,    48,    49,    50,    51,    -1,
    1061,    -1,    55,    56,  1065,    -1,    -1,  1068,    -1,    -1,
    1057,    -1,  1073,    -1,  1061,    68,    69,    -1,  1065,    -1,
      -1,  1068,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1116,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   102,
    1129,    -1,    -1,    -1,    -1,  1116,    -1,    -1,  1137,  1138,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1129,    -1,
      -1,    -1,    -1,    -1,  1153,    -1,  1137,  1138,    -1,    -1,
      -1,    -1,  1129,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1137,  1138,  1153,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1153,    -1,    -1,    -1,
    1189,    -1,    -1,    -1,  1161,    -1,    -1,    -1,    -1,    -1,
      -1,  1200,    -1,    -1,    -1,    -1,    -1,    -1,  1189,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1200,
      -1,    -1,  1189,    -1,    -1,    -1,    -1,    -1,    -1,    33,
      34,    35,    36,  1200,    -1,    -1,    -1,   210,    -1,    -1,
     213,   214,   215,    -1,   217,    49,    50,    51,    52,  1216,
      -1,    -1,    56,    -1,    -1,    59,    60,    61,    62,    63,
      -1,    -1,   235,    -1,   237,   238,    -1,    -1,    -1,    -1,
      -1,    -1,  1271,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,
    1271,    -1,    -1,    -1,    98,    -1,    -1,   101,    -1,    -1,
     104,   105,    -1,   107,  1271,    -1,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   132,    -1,
      -1,    -1,    -1,    -1,    -1,   139,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   153,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   348,   349,   350,   351,   352,
      -1,    -1,   355,   356,   357,   358,   359,   360,   361,   362,
      -1,   364,    -1,    -1,   367,   368,   369,   370,   371,   372,
     373,   374,   375,   376,    -1,    -1,    -1,   380,    -1,     0,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,     8,     9,    10,
      -1,    -1,    13,    14,    15,    -1,    17,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    25,    26,    27,    28,    29,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,
      41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     443,   444,    -1,    -1,    -1,   448,    -1,    68,    -1,   452,
      -1,    -1,    -1,    -1,    -1,   458,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    -1,
     473,    -1,    -1,    94,    95,    96,    97,    -1,    99,   100,
      -1,    -1,    -1,    25,    -1,   106,   489,    -1,    -1,   492,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   120,
      -1,   504,   123,    -1,    -1,   126,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,   521,    -1,
      -1,    -1,   143,   144,   145,   146,    -1,    -1,   149,   150,
     151,    -1,   153,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    -1,    -1,
      -1,   554,    94,    95,    -1,    -1,    -1,    -1,   100,    -1,
      -1,    -1,    -1,   566,    -1,    -1,    -1,   570,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   580,    -1,    -1,
      -1,    -1,    -1,   586,    -1,   127,    -1,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   640,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   650,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   660,   661,    -1,
     663,   664,   665,   666,    -1,   668,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   677,   678,    -1,    -1,    -1,    -1,
     683,    -1,   685,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   694,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   731,    -1,
      -1,    -1,    -1,     0,     1,    -1,     3,     4,     5,     6,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,   751,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,   764,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    77,    78,    79,    80,    81,    82,    83,    75,    76,
      86,    87,    -1,    -1,    -1,    -1,    -1,    -1,    94,    95,
      -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,
      -1,    98,   835,    -1,   101,   102,    -1,   104,   105,    -1,
     107,    -1,    -1,   110,   111,   112,   113,   114,   115,   116,
     117,   118,   855,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   139,   140,   141,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   151,    -1,   153,    -1,    -1,    33,
      34,    35,    36,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    49,    50,    51,    52,    -1,
      -1,    -1,    56,    -1,    58,    59,    60,    61,    62,    63,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   930,   931,    -1,
      -1,    -1,    -1,    77,    -1,    -1,    -1,    -1,    -1,   942,
     943,    -1,    -1,    -1,    -1,   948,    90,    91,    -1,    -1,
     953,    -1,   955,    -1,    98,    -1,    -1,   101,    -1,    -1,
     104,   105,    -1,   107,   108,    -1,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   132,    -1,
      -1,    -1,   995,   996,   997,   139,    -1,    -1,  1001,  1002,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     0,
      -1,    -1,  1015,    -1,    -1,    -1,    -1,     8,     9,    10,
      -1,    -1,    13,    14,    15,    -1,    17,  1030,  1031,    -1,
    1033,  1034,    -1,  1036,    25,    -1,    27,    28,    29,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,
      41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1069,    -1,    -1,    -1,
    1073,    -1,    -1,    -1,    -1,    -1,    -1,    68,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    -1,
      -1,    -1,    -1,    94,    95,    96,    97,    -1,    99,   100,
      -1,    -1,    -1,  1116,    -1,   106,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,  1128,    -1,    -1,    -1,   120,
      -1,  1134,   123,    -1,    -1,   126,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,    -1,    -1,   144,   145,   146,     0,    -1,   149,   150,
     151,    -1,   153,    -1,     8,     9,    10,    -1,    -1,    13,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    44,    -1,
      -1,    25,    -1,    27,    28,    29,    -1,    -1,    -1,  1192,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,  1209,    -1,    -1,    -1,
      -1,    77,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    68,    -1,    -1,    -1,    94,    95,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    -1,    -1,    -1,    -1,
      94,    95,    96,    97,    -1,    99,   100,    -1,    -1,    -1,
      -1,   127,   106,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,    -1,    -1,   120,    -1,    -1,   123,
     146,    -1,   126,   127,   128,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,    -1,    -1,    -1,    -1,
     144,   145,   146,     0,    -1,   149,   150,   151,    -1,   153,
      -1,     8,     9,    10,    -1,    -1,    13,    14,    15,    -1,
      17,    -1,    -1,    -1,    -1,    44,    -1,    -1,    25,    26,
      27,    28,    29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      37,    38,    -1,    40,    41,    42,    43,    44,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    68,    -1,    -1,    -1,    94,    95,    -1,    -1,    -1,
      77,    78,    79,    80,    81,    82,    83,    84,    85,    86,
      87,    88,    89,    -1,    -1,    -1,    -1,    94,    95,    96,
      97,    -1,    -1,   100,    -1,    -1,    -1,    -1,   127,   106,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
      -1,    -1,    -1,   120,    -1,    -1,   123,    -1,    -1,    -1,
     127,   128,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,    -1,    -1,    -1,    -1,   143,   144,   145,   146,
       0,    -1,   149,   150,   151,    -1,   153,    -1,     8,     9,
      10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,    -1,
      -1,    -1,    44,    -1,    -1,    25,    26,    27,    28,    29,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,
      40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    68,    -1,
      -1,    -1,    94,    95,    -1,    -1,    -1,    77,    78,    79,
      80,    81,    82,    83,    84,    85,    86,    87,    88,    89,
      -1,    -1,    -1,    -1,    94,    95,    96,    97,    -1,    -1,
     100,    -1,    -1,    -1,    -1,   127,   106,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,    -1,    -1,
     120,    -1,    -1,   123,    -1,    -1,    -1,   127,   128,   129,
     130,   131,   132,   133,   134,   135,   136,   137,   138,    -1,
      -1,    -1,    -1,   143,   144,   145,   146,     0,    -1,   149,
     150,   151,    -1,   153,    -1,     8,     9,    10,    -1,    -1,
      13,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,    44,
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
     123,    -1,    -1,   126,   127,   128,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,    -1,    -1,    -1,    -1,
      -1,   144,   145,   146,     0,    -1,   149,   150,   151,    -1,
     153,    -1,     8,     9,    10,    -1,    -1,    13,    14,    15,
      -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,
      26,    27,    28,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    37,    38,    -1,    40,    41,    42,    43,    44,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    68,    -1,    -1,    -1,    94,    95,    -1,    -1,
      -1,    77,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    -1,    -1,    -1,    -1,    94,    95,
      96,    97,    -1,    -1,   100,    -1,    -1,    -1,    -1,   127,
     106,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,    -1,    -1,    -1,   120,    -1,    -1,    -1,    -1,    -1,
      -1,   127,    -1,   129,   130,   131,   132,   133,   134,   135,
     136,   137,   138,    -1,    -1,    -1,    -1,   143,   144,   145,
     146,     0,   148,   149,   150,   151,    -1,   153,    -1,     8,
       9,    10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    25,    -1,    27,    28,
      29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    -1,    -1,    94,    95,    96,    97,    -1,
      -1,   100,    -1,    -1,    -1,    -1,    -1,   106,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   120,    -1,    -1,   123,    -1,    -1,    -1,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
      -1,    -1,    -1,    -1,    -1,   144,   145,   146,     0,    -1,
     149,   150,   151,    -1,   153,    -1,     8,     9,    10,    -1,
      -1,    13,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    25,    26,    27,    28,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,
      42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    68,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    -1,    -1,
      -1,    -1,    94,    95,    96,    97,    -1,    -1,   100,    -1,
      -1,    -1,    -1,    -1,   106,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   120,    -1,
      -1,    -1,    -1,    -1,    -1,   127,    -1,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,    -1,    -1,    -1,
      -1,   143,   144,   145,   146,     0,   148,   149,   150,   151,
      -1,   153,    -1,     8,     9,    10,    -1,    -1,    13,    14,
      15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      25,    -1,    27,    28,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    37,    38,    -1,    40,    41,    42,    43,    44,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    77,    78,    79,    80,    81,    82,    83,    84,
      85,    86,    87,    88,    89,    -1,    -1,    -1,    -1,    94,
      95,    96,    97,    -1,    -1,   100,    -1,    -1,    -1,    -1,
      -1,   106,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   120,    -1,    -1,    -1,    -1,
      -1,    -1,   127,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,    -1,    -1,    -1,    -1,   144,
     145,   146,     0,   148,   149,   150,   151,    -1,   153,    -1,
       8,     9,    10,    -1,    -1,    -1,    14,    15,    -1,    17,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,    26,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,
      38,    -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    77,    78,    79,    80,    81,    82,
      83,    84,    85,    86,    87,    88,    89,    -1,    -1,    -1,
      68,    94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    77,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    -1,    -1,    -1,    -1,    94,    95,    96,    97,
      -1,    99,   100,    -1,   127,    -1,   129,   130,   131,   132,
     133,   134,   135,   136,   137,   138,    -1,    -1,    -1,    -1,
      -1,    -1,   120,    -1,    -1,    -1,    -1,    -1,   126,   127,
     153,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,    -1,    -1,    -1,    -1,   143,   144,   145,   146,     0,
      -1,   149,    -1,   151,    -1,   153,    -1,     8,     9,    10,
      -1,    -1,    -1,    14,    15,    -1,    17,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    25,    26,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,
      41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    -1,
      -1,    -1,    -1,    94,    95,    96,    97,    -1,    99,   100,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   120,
      -1,    -1,    -1,    -1,    -1,   126,   127,    -1,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,    -1,
      -1,    -1,   143,   144,   145,   146,     0,    -1,   149,    -1,
     151,    -1,   153,    -1,     8,     9,    10,    -1,    -1,    -1,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    25,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    -1,    -1,    -1,    -1,
      94,    95,    96,    97,    -1,    99,   100,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   120,    -1,    -1,    -1,
      -1,    -1,   126,   127,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,    -1,    -1,    -1,    -1,
     144,   145,   146,     0,    -1,   149,    -1,   151,    -1,   153,
      -1,     8,     9,    10,    -1,    -1,    -1,    14,    15,    -1,
      17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      37,    38,    -1,    40,    41,    42,    43,    44,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    68,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      77,    78,    79,    80,    81,    82,    83,    84,    85,    86,
      87,    88,    89,    -1,    -1,    -1,    -1,    94,    95,    96,
      97,    -1,    99,   100,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   120,    -1,    -1,    -1,    -1,    -1,   126,
     127,    -1,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,    -1,    -1,    -1,    -1,    -1,   144,   145,   146,
      -1,    -1,   149,    -1,   151,     1,   153,     3,     4,     5,
       6,     7,     8,     9,    10,    11,    12,    -1,    -1,    15,
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
      -1,    -1,    -1,   139,   140,   141,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   151,     1,   153,     3,     4,
       5,     6,     7,    -1,    -1,    10,    11,    12,    -1,    14,
      15,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
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
      -1,    -1,    -1,    -1,   139,   140,   141,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   151,     1,   153,     3,
       4,     5,     6,     7,    -1,    -1,    10,    11,    12,    -1,
      -1,    15,    16,    17,    18,    19,    20,    21,    22,    23,
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
      -1,    -1,    15,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    25,    -1,    -1,    -1,    -1,    30,    31,    32,
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
      12,    -1,    -1,    15,    16,    -1,    18,    19,    20,    21,
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
      -1,    -1,    -1,    -1,    -1,     1,    -1,     3,     4,     5,
       6,     7,    -1,     9,    10,    11,    12,   139,   140,   141,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,   151,
      -1,   153,    -1,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    45,
      46,    47,    48,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,
      -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,
      -1,   107,    -1,    -1,   110,   111,   112,   113,   114,   115,
     116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,     1,    -1,     3,     4,     5,     6,     7,    -1,    -1,
      -1,    11,    12,   139,   140,   141,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,   151,    -1,   153,    -1,    -1,
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
     140,   141,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   149,
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
     139,   140,   141,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     149,    -1,   151,     1,   153,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
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
      -1,   139,   140,   141,    -1,    -1,   144,    -1,    -1,    -1,
      -1,    -1,    -1,   151,     1,   153,     3,     4,     5,     6,
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
      -1,    -1,   139,   140,   141,    -1,    -1,   144,    -1,    -1,
      -1,    -1,    -1,    -1,   151,     1,   153,     3,     4,     5,
       6,     7,    -1,    -1,    10,    11,    12,    -1,    -1,    -1,
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
      -1,     1,    -1,     3,     4,     5,     6,     7,    -1,    -1,
      -1,    11,    12,   139,   140,   141,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,   151,    -1,   153,    -1,    -1,
      30,    31,    32,    33,    34,    35,    36,    -1,    -1,    39,
      -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,
      -1,   101,   102,    -1,   104,   105,    -1,   107,    -1,    -1,
     110,   111,   112,   113,   114,   115,   116,   117,   118,    -1,
     120,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,
       4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,   139,
     140,   141,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,   151,    -1,   153,    -1,    -1,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    58,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    90,    91,    -1,    -1,
      -1,    -1,    -1,    -1,    98,    -1,    -1,   101,   102,    -1,
     104,   105,    -1,   107,   108,   109,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,
      -1,    -1,    -1,    11,    12,   139,   140,   141,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,   153,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,
      -1,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    90,    91,    -1,    -1,    -1,    -1,    -1,    -1,
      98,    -1,    -1,   101,   102,    -1,   104,   105,    -1,    -1,
      -1,    -1,   110,   111,   112,   113,   114,   115,   116,   117,
     118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,
      12,   139,   140,   141,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,   151,    -1,   153,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,
      -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    -1,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    90,    91,
      -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,
     102,    -1,   104,   105,    -1,    -1,    -1,    -1,   110,   111,
     112,   113,   114,   115,   116,   117,   118,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,
       6,     7,    -1,    -1,    -1,    11,    12,   139,   140,   141,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    -1,
      -1,   153,    -1,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    45,
      46,    47,    48,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    90,    91,    -1,    -1,    -1,    -1,
      -1,    -1,    98,    -1,    -1,   101,   102,    -1,   104,   105,
      -1,   107,    -1,    -1,   110,   111,   112,   113,   114,   115,
     116,   117,   118,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,     3,     4,     5,     6,     7,    -1,    -1,
      -1,    11,    12,   139,   140,   141,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,   151,    -1,    -1,    -1,    -1,
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
     140,   141,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   151,     3,     4,     5,     6,     7,     8,     9,    10,
      11,    12,    13,    14,    15,    16,    17,    18,    19,    20,
      21,    22,    23,    24,    25,    26,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    -1,
      -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    77,    78,    79,    80,
      81,    82,    83,    -1,    -1,    86,    87,    -1,    -1,    -1,
      -1,    92,    93,    94,    95,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   107,   108,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,    -1,   140,
     141,    -1,    -1,    -1,    -1,    -1,   147,   148,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    -1,    -1,    -1,    -1,    -1,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    -1,    -1,    -1,
      -1,    -1,   147,     3,     4,     5,     6,     7,     8,     9,
      10,    11,    12,    13,    14,    15,    16,    17,    18,    19,
      20,    21,    22,    23,    24,    25,    26,    -1,    -1,    -1,
      30,    31,    32,    33,    34,    35,    36,    37,    38,    39,
      -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,
      50,    51,    52,    53,    54,    -1,    56,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    75,    76,    77,    78,    79,
      80,    81,    82,    83,    -1,    -1,    86,    87,    -1,    -1,
      -1,    -1,    92,    93,    94,    95,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   107,   108,    -1,
      -1,   111,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   129,
     130,   131,   132,   133,   134,   135,   136,   137,   138,    -1,
     140,   141,    -1,    -1,    -1,    -1,    -1,   147,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    -1,    -1,    -1,    -1,    -1,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    -1,
      -1,    56,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,    94,
      95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   107,   108,    -1,    -1,   111,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,    -1,   140,   141,    -1,    -1,    -1,
      -1,    -1,   147,     3,     4,     5,     6,     7,     8,     9,
      10,    11,    12,    13,    14,    15,    16,    17,    18,    19,
      20,    21,    22,    23,    24,    25,    26,    -1,    -1,    -1,
      30,    31,    32,    33,    34,    35,    36,    37,    38,    39,
      -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,
      50,    51,    52,    53,    -1,    -1,    56,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    75,    76,    77,    78,    79,
      80,    81,    82,    83,    -1,    -1,    86,    87,    -1,    -1,
      -1,    -1,    92,    93,    94,    95,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   107,   108,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   129,
     130,   131,   132,   133,   134,   135,   136,   137,   138,    -1,
     140,   141,     3,     4,     5,    -1,     7,   147,    -1,    -1,
      11,    12,    -1,    -1,    -1,    16,    -1,    18,    19,    20,
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
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,     3,     4,     5,    -1,     7,    -1,    -1,   139,    11,
      12,    -1,    -1,    -1,    16,   146,    18,    19,    20,    21,
      22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,
      -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    -1,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,   101,
     102,    -1,   104,   105,    -1,    -1,    -1,    -1,   110,   111,
     112,   113,   114,   115,   116,   117,   118,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,     6,     7,    -1,    -1,   139,    11,    12,
      -1,    -1,    -1,    16,   146,    18,    19,    20,    21,    22,
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
      -1,    -1,     3,     4,     5,     6,     7,    -1,    -1,    -1,
      11,    12,   139,   140,   141,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    45,    46,    -1,    48,    49,    50,
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
     101,   102,    -1,   104,   105,    -1,    -1,   108,   109,   110,
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
      -1,   104,   105,    -1,    -1,    -1,    -1,   110,   111,   112,
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
      -1,    -1,    -1,   110,   111,   112,   113,   114,   115,   116,
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
      -1,    -1,    -1,    94,    -1,    -1,    -1,    98,    -1,    -1,
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
      63,    64,    65,    33,    34,    35,    36,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    49,
      50,    51,    52,    -1,    -1,    -1,    56,    -1,    -1,    59,
      60,    61,    62,    63,    -1,    98,    -1,    -1,   101,   102,
      -1,   104,   105,    -1,    -1,    -1,    -1,   110,   111,   112,
     113,   114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,
      90,    91,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,
      -1,   101,    -1,    -1,   104,   105,   139,   107,    -1,    -1,
     110,   111,   112,   113,   114,   115,   116,   117,   118,    33,
      34,    35,    36,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   132,    -1,    -1,    49,    50,    51,    52,   139,
      -1,    -1,    56,    -1,    -1,    59,    60,    61,    62,    63,
      -1,    -1,    33,    34,    35,    36,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    49,    50,
      51,    52,    -1,    -1,    -1,    56,    90,    91,    59,    60,
      61,    62,    63,    -1,    98,    -1,    -1,   101,    -1,    -1,
     104,   105,    -1,   107,    -1,    -1,   110,   111,   112,   113,
     114,   115,   116,   117,   118,    -1,    -1,    -1,    -1,    90,
      91,    -1,    -1,    -1,    -1,    -1,    -1,    98,   132,    -1,
     101,    -1,    -1,   104,   105,   139,    -1,    -1,    -1,   110,
     111,   112,   113,   114,   115,   116,   117,   118,    52,    53,
      -1,    -1,    56,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   132,    -1,    -1,    -1,    -1,    -1,    -1,   139,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    52,    53,
      -1,    -1,    56,   147,   148,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   107,   108,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,    -1,   140,   141,    -1,    -1,
      -1,    -1,    -1,   147
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
     157,   158,   159,   167,   169,   171,   175,   176,   182,   183,
     185,   186,   187,   189,   190,   191,   193,   194,   203,   206,
     222,   232,   233,   234,   235,   236,   237,   238,   239,   240,
     241,   242,   251,   273,   281,   282,   334,   335,   336,   337,
     338,   339,   340,   343,   345,   346,   360,   361,   363,   364,
     365,   367,   368,   369,   370,   371,   409,   423,   159,     3,
       4,     5,     6,     7,     8,     9,    10,    11,    12,    13,
      14,    15,    16,    17,    18,    19,    20,    21,    22,    23,
      24,    25,    26,    30,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    56,    75,    76,    77,    78,    79,    80,    81,
      82,    83,    86,    87,    92,    93,    94,    95,   107,   108,
     129,   130,   131,   132,   133,   134,   135,   136,   137,   138,
     140,   141,   147,   197,   198,   199,   201,   202,   360,    39,
      58,    98,   101,   107,   108,   109,   112,   140,   175,   176,
     186,   194,   203,   208,   214,   217,   219,   232,   367,   368,
     370,   371,   407,   408,   214,   148,   215,   216,   148,   211,
     215,   148,   153,   416,    54,   198,   416,   143,   160,   143,
      21,    22,    31,    32,   185,   203,   232,   251,   203,   203,
     203,    56,     1,    47,   101,   163,   164,   165,   167,   188,
     189,   423,   167,   224,   209,   219,   407,   423,   208,   406,
     407,   423,    46,    98,   139,   146,   175,   176,   193,   222,
     232,   367,   368,   371,   274,    54,    55,    57,   197,   349,
     362,   349,   350,   351,   152,   152,   152,   152,   365,   182,
     203,   203,   151,   153,   415,   421,   422,    40,    41,    42,
      43,    44,    37,    38,   148,   374,   375,   376,   377,   423,
     374,   376,    26,   143,   211,   215,   243,   283,    28,   244,
     280,   126,   146,   101,   107,   190,   126,    25,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    94,    95,   100,   127,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,   205,   205,    68,    96,    97,
     145,   413,   223,   171,   178,   178,   179,   180,   179,   178,
     415,   422,    98,   187,   194,   232,   256,   367,   368,   371,
      52,    56,    94,    98,   195,   196,   232,   367,   368,   371,
     196,    33,    34,    35,    36,    49,    50,    51,    52,    56,
     148,   174,   197,   369,   404,   214,    97,   413,   414,   283,
     337,    99,    99,   146,   208,    56,   208,   208,   208,   349,
     374,   374,   126,   100,   146,   218,   423,    97,   145,   413,
      99,    99,   146,   218,   214,   416,   417,   214,    91,   213,
     214,   219,   381,   407,   423,   171,   417,   171,    54,    64,
      65,   168,   148,   204,   157,   163,    97,   413,    99,   167,
     166,   188,   149,   415,   422,   417,   225,   417,   150,   146,
     153,   420,   146,   420,   144,   420,   416,    56,   365,   190,
     192,   375,   146,    97,   145,   413,   275,    66,   119,   121,
     122,   352,   119,   119,   352,    67,   352,   341,   347,   344,
     348,    77,   151,   159,   178,   178,   178,   178,   167,   171,
     171,    52,    54,    55,    56,    57,    58,    77,    91,   101,
     107,   108,   109,   133,   136,   261,   378,   380,   381,   382,
     383,   384,   385,   386,   387,   388,   391,   392,   393,   394,
     395,   398,   399,   400,   401,   402,   126,   161,   163,   380,
     126,   161,   284,   285,   106,   184,   288,   289,   288,    70,
     207,   423,   188,   146,   193,   146,   207,   173,   203,   203,
     203,   203,   203,   203,   203,   203,   203,   203,   203,   203,
     203,   172,   203,   203,   203,   203,   203,   203,   203,   203,
     203,   203,   203,    52,    53,    56,   201,   211,   410,   411,
     213,   219,    52,    53,    56,   201,   211,   410,   161,    13,
     252,   421,   252,   163,   178,   163,   415,   228,    56,    97,
     145,   413,    25,   171,    52,    56,   195,   130,   372,    97,
     145,   413,   231,   405,    68,    97,   412,    52,    56,   410,
     207,   207,   200,   124,   126,   126,   207,   208,   107,   208,
     217,   407,    52,    56,   213,    52,    56,   207,   207,   408,
     417,   149,   417,   146,   417,   146,   417,   198,   226,   203,
     144,   144,   410,   410,   207,   160,   417,   165,   417,   407,
     146,   192,    52,    56,   213,    52,    56,   276,   354,   353,
     119,   342,   352,    66,   119,   119,   342,    66,   119,   203,
     101,   107,   257,   258,   259,   260,   383,   146,   403,   423,
     417,   262,   263,   146,   379,   208,   146,   403,    34,    52,
     146,   379,    52,   146,   379,    52,   186,   203,    10,   250,
       8,   245,   330,   423,   421,   186,   203,   250,   144,   286,
     284,   250,   290,   250,   107,   182,   208,   219,   220,   221,
     417,   192,   146,   169,   170,   182,   194,   203,   208,   210,
     221,   232,   371,    52,    56,    58,    90,    91,    98,   101,
     104,   105,   107,   112,   132,   273,   301,   302,   303,   304,
     307,   312,   313,   314,   317,   318,   319,   320,   321,   322,
     323,   324,   325,   326,   327,   328,   329,   334,   335,   338,
     339,   340,   343,   345,   346,   368,   392,   301,   416,    99,
      99,   211,   215,   416,   418,   146,    99,    99,   211,   212,
     215,   423,   250,   163,    13,   163,   250,    27,   253,   421,
     250,    25,   227,   295,    17,   247,   293,    52,    56,   213,
      52,    56,   179,   230,   373,   229,    52,    56,   195,   213,
     161,   171,   177,   212,   215,   170,   203,   210,   170,   210,
     198,   208,   208,   218,    99,    99,   418,    99,    99,   381,
     407,   171,   210,   420,   190,   418,   148,   278,   380,   355,
      54,    55,    57,   359,   371,   152,   352,   152,   152,   152,
     259,   383,   146,   417,   146,   402,   208,   126,   378,   385,
     398,   400,   388,   392,   394,   386,   395,   400,   384,   386,
      44,    44,   208,   221,   331,   423,     9,    15,   246,   248,
     333,   423,    44,    44,   287,   144,   291,   208,   146,    44,
     192,    44,   126,    44,    97,   145,   413,   325,   325,    56,
     195,   309,   302,   310,   311,   312,   313,   316,   418,   308,
     416,   419,    52,   349,    52,    54,    55,    57,   101,   366,
     100,   146,   131,   146,   146,   302,    88,    89,    97,   145,
     148,   305,   306,    34,    52,   128,   207,   207,   184,   150,
      99,   207,   207,   184,    14,   248,   249,   254,   255,   423,
     255,   181,   296,   293,   250,   107,   208,   292,   250,   418,
     163,   421,   178,   161,   418,   250,   417,   174,   283,   280,
     207,   207,    99,   207,   207,   417,   146,   417,   380,   277,
     356,   417,   257,   260,   258,   146,   379,   146,   379,   403,
     146,   379,   146,   379,   379,   203,   203,   100,   332,   423,
     163,   162,   203,   203,   131,   268,   269,   423,   268,   107,
     208,   167,   167,   207,   203,    52,    56,   213,    52,    56,
     302,   418,   107,   302,   316,   418,   146,   112,   317,   144,
     124,   178,   326,   310,   314,   307,   315,   316,   319,   323,
     325,   325,   195,   418,   417,   310,   313,   317,   310,   313,
     317,   203,   170,   210,   170,   210,   207,   170,   210,   170,
     210,   163,   178,   250,   250,   297,   250,   208,   146,   252,
     250,   161,   421,   250,   207,   270,   416,    29,   123,   279,
     357,   146,   146,   386,   400,   386,   386,    98,   194,   232,
     367,   368,   371,   252,   163,   261,   264,   267,   270,   384,
     386,   387,   389,   390,   396,   397,   400,   402,   163,   161,
     208,   418,   417,    52,   146,   146,   349,   419,   149,   146,
     146,   417,   417,   417,   418,   418,   418,   170,   210,   252,
     300,   301,   107,   208,   163,   250,   149,   151,   161,   163,
     358,   258,   379,   146,   379,   379,   379,    56,    97,   145,
     413,   163,   333,   403,   270,   131,   262,   146,   265,   266,
      98,   232,   146,   403,   146,   265,   146,   265,   146,   315,
     315,   314,   316,   163,   252,    40,    41,   208,   255,   293,
     294,    52,   271,   272,   382,   250,   144,   163,   386,    52,
      56,   213,    52,    56,   330,   131,   232,   264,   397,   400,
      56,    97,   389,   394,   386,   396,   400,   386,   315,   146,
     254,   298,   178,   178,   146,   416,   120,   379,   418,   146,
     265,   146,   265,    52,    56,   403,   146,   265,   146,   265,
     265,   163,   272,   386,   400,   386,   386,   255,   295,   299,
     265,   146,   265,   265,   265,   386,   265
};

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
static const yytype_int16 yyr1[] =
{
       0,   154,   156,   155,   157,   158,   158,   158,   158,   159,
     159,   160,   162,   161,   161,   163,   164,   164,   164,   164,
     165,   166,   165,   168,   167,   167,   167,   167,   167,   167,
     167,   167,   167,   167,   167,   167,   167,   167,   167,   167,
     167,   169,   169,   169,   169,   169,   169,   169,   169,   169,
     169,   169,   169,   170,   170,   170,   171,   171,   171,   171,
     171,   172,   171,   173,   171,   171,   174,   175,   177,   176,
     178,   180,   181,   179,   182,   182,   183,   183,   184,   185,
     186,   186,   186,   186,   186,   186,   186,   186,   186,   186,
     186,   187,   187,   188,   188,   189,   189,   189,   189,   189,
     189,   189,   189,   189,   189,   190,   190,   191,   191,   192,
     192,   193,   193,   193,   193,   193,   193,   193,   193,   193,
     194,   194,   194,   194,   194,   194,   194,   194,   194,   195,
     195,   196,   196,   196,   197,   197,   197,   197,   197,   198,
     198,   199,   200,   199,   201,   201,   201,   201,   201,   201,
     201,   201,   201,   201,   201,   201,   201,   201,   201,   201,
     201,   201,   201,   201,   201,   201,   201,   201,   201,   201,
     201,   201,   201,   201,   202,   202,   202,   202,   202,   202,
     202,   202,   202,   202,   202,   202,   202,   202,   202,   202,
     202,   202,   202,   202,   202,   202,   202,   202,   202,   202,
     202,   202,   202,   202,   202,   202,   202,   202,   202,   202,
     202,   202,   202,   202,   202,   203,   203,   203,   203,   203,
     203,   203,   203,   203,   203,   203,   203,   203,   203,   203,
     203,   203,   203,   203,   203,   203,   203,   203,   203,   203,
     203,   203,   203,   203,   203,   203,   203,   203,   203,   203,
     203,   203,   203,   203,   203,   204,   203,   203,   203,   203,
     203,   203,   203,   205,   205,   205,   205,   206,   206,   207,
     207,   208,   209,   209,   209,   209,   210,   210,   211,   211,
     211,   212,   212,   213,   213,   213,   213,   213,   214,   214,
     214,   214,   214,   216,   215,   217,   217,   218,   218,   219,
     219,   219,   219,   220,   220,   221,   221,   221,   222,   222,
     222,   222,   222,   222,   222,   222,   222,   222,   222,   223,
     222,   224,   222,   225,   222,   222,   222,   222,   222,   222,
     222,   222,   222,   222,   226,   222,   222,   222,   222,   222,
     222,   222,   222,   222,   222,   222,   227,   222,   228,   222,
     222,   222,   229,   222,   230,   222,   231,   222,   222,   222,
     222,   222,   222,   222,   232,   233,   234,   235,   236,   237,
     238,   239,   240,   241,   242,   243,   244,   245,   246,   247,
     248,   249,   250,   251,   252,   252,   252,   253,   253,   254,
     254,   255,   255,   256,   256,   257,   257,   258,   258,   259,
     259,   259,   259,   259,   260,   260,   261,   261,   263,   262,
     264,   264,   264,   264,   265,   265,   266,   267,   267,   267,
     267,   267,   267,   267,   267,   267,   267,   267,   267,   267,
     267,   267,   268,   268,   269,   269,   270,   270,   271,   271,
     272,   272,   274,   275,   276,   277,   273,   278,   278,   279,
     279,   280,   281,   281,   281,   281,   282,   282,   282,   282,
     282,   282,   282,   282,   282,   283,   283,   285,   286,   287,
     284,   289,   290,   291,   288,   292,   292,   292,   292,   293,
     294,   294,   296,   297,   298,   295,   299,   299,   300,   300,
     300,   301,   301,   301,   301,   301,   301,   302,   303,   303,
     304,   304,   305,   306,   307,   307,   307,   307,   307,   307,
     307,   307,   307,   307,   307,   307,   307,   308,   307,   307,
     309,   307,   310,   310,   310,   310,   310,   310,   310,   310,
     311,   311,   312,   312,   313,   314,   314,   315,   315,   316,
     317,   317,   317,   317,   318,   318,   319,   319,   320,   320,
     321,   321,   322,   323,   323,   324,   324,   324,   324,   324,
     324,   324,   324,   324,   324,   325,   325,   325,   325,   325,
     325,   325,   325,   325,   325,   326,   327,   327,   328,   329,
     329,   329,   330,   330,   331,   331,   331,   332,   332,   333,
     333,   334,   334,   335,   336,   336,   336,   337,   338,   339,
     340,   341,   341,   342,   342,   343,   344,   344,   345,   346,
     347,   347,   348,   348,   349,   349,   350,   350,   351,   351,
     352,   353,   352,   354,   355,   356,   357,   358,   352,   359,
     359,   359,   359,   360,   360,   361,   362,   362,   362,   362,
     363,   364,   364,   365,   365,   365,   365,   366,   366,   366,
     367,   367,   367,   367,   367,   368,   368,   368,   368,   368,
     368,   368,   369,   369,   370,   370,   371,   371,   373,   372,
     372,   374,   374,   375,   376,   377,   376,   378,   378,   378,
     378,   378,   379,   379,   380,   380,   380,   380,   380,   380,
     380,   380,   380,   380,   380,   380,   380,   380,   380,   381,
     382,   382,   382,   382,   383,   383,   384,   385,   385,   386,
     386,   387,   388,   388,   389,   389,   390,   390,   391,   391,
     392,   392,   393,   394,   394,   395,   396,   397,   397,   398,
     398,   399,   399,   400,   400,   401,   401,   402,   402,   403,
     403,   404,   405,   404,   406,   406,   407,   407,   408,   408,
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
       1,     4,     4,     7,     6,     6,     6,     6,     4,     6,
       4,     6,     4,     1,     3,     1,     1,     3,     3,     3,
       2,     0,     4,     0,     4,     1,     1,     2,     0,     5,
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
       2,     4,     1,     0,     2,     2,     1,     2,     1,     1,
       2,     3,     4,     1,     1,     3,     4,     2,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     0,
       4,     0,     3,     0,     4,     3,     3,     2,     3,     3,
       1,     4,     3,     1,     0,     6,     4,     3,     2,     1,
       2,     1,     6,     6,     4,     4,     0,     6,     0,     5,
       5,     6,     0,     6,     0,     7,     0,     5,     4,     4,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     2,     1,     1,     1,
       5,     1,     2,     1,     1,     1,     3,     1,     3,     1,
       3,     5,     1,     3,     2,     1,     1,     1,     0,     2,
       4,     2,     2,     1,     2,     0,     1,     6,     8,     4,
       6,     4,     2,     6,     2,     4,     6,     2,     4,     2,
       4,     1,     1,     1,     3,     4,     1,     4,     1,     3,
       1,     1,     0,     0,     0,     0,     7,     4,     1,     3,
       3,     3,     2,     4,     5,     5,     2,     4,     4,     3,
       3,     3,     2,     1,     4,     3,     3,     0,     0,     0,
       5,     0,     0,     0,     5,     1,     2,     3,     4,     5,
       1,     1,     0,     0,     0,     8,     1,     1,     1,     3,
       3,     1,     2,     3,     1,     1,     1,     1,     3,     1,
       3,     1,     1,     1,     1,     1,     4,     4,     4,     3,
       4,     4,     4,     3,     3,     3,     2,     0,     4,     2,
       0,     4,     1,     1,     2,     3,     5,     2,     4,     1,
       2,     3,     1,     3,     5,     2,     1,     1,     3,     1,
       3,     1,     2,     1,     1,     3,     2,     1,     1,     3,
       2,     1,     2,     1,     1,     1,     3,     3,     2,     2,
       1,     1,     1,     2,     2,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     2,     2,     4,     2,
       3,     1,     6,     1,     1,     1,     1,     2,     1,     2,
       1,     1,     1,     1,     1,     1,     2,     3,     3,     3,
       4,     0,     3,     1,     2,     4,     0,     3,     4,     4,
       0,     3,     0,     3,     0,     2,     0,     2,     0,     2,
       1,     0,     3,     0,     0,     0,     0,     0,     8,     1,
       1,     1,     1,     1,     1,     2,     1,     1,     1,     1,
       3,     1,     2,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     0,     4,
       0,     1,     1,     3,     1,     0,     3,     4,     2,     2,
       1,     1,     2,     0,     6,     8,     4,     6,     4,     6,
       2,     4,     6,     2,     4,     2,     4,     1,     0,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     3,     1,
       3,     1,     2,     1,     2,     1,     1,     3,     1,     3,
       1,     1,     2,     2,     1,     3,     3,     1,     3,     1,
       3,     1,     1,     2,     1,     1,     1,     2,     1,     2,
       1,     1,     0,     4,     1,     2,     1,     3,     3,     2,
       1,     4,     2,     1,     1,     1,     1,     1,     1,     1,
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
#line 1090 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).val)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).val))->nd_rval);
#endif
}
#line 5707 "ripper.c"
        break;

    case 53: /* "method"  */
#line 1090 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).val)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).val))->nd_rval);
#endif
}
#line 5719 "ripper.c"
        break;

    case 54: /* "global variable"  */
#line 1090 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).val)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).val))->nd_rval);
#endif
}
#line 5731 "ripper.c"
        break;

    case 55: /* "instance variable"  */
#line 1090 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).val)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).val))->nd_rval);
#endif
}
#line 5743 "ripper.c"
        break;

    case 56: /* "constant"  */
#line 1090 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).val)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).val))->nd_rval);
#endif
}
#line 5755 "ripper.c"
        break;

    case 57: /* "class variable"  */
#line 1090 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).val)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).val))->nd_rval);
#endif
}
#line 5767 "ripper.c"
        break;

    case 58: /* "label"  */
#line 1090 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).val)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).val))->nd_rval);
#endif
}
#line 5779 "ripper.c"
        break;

    case 59: /* "integer literal"  */
#line 1097 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).val)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).val)));
#endif
}
#line 5791 "ripper.c"
        break;

    case 60: /* "float literal"  */
#line 1097 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).val)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).val)));
#endif
}
#line 5803 "ripper.c"
        break;

    case 61: /* "rational literal"  */
#line 1097 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).val)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).val)));
#endif
}
#line 5815 "ripper.c"
        break;

    case 62: /* "imaginary literal"  */
#line 1097 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).val)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).val)));
#endif
}
#line 5827 "ripper.c"
        break;

    case 63: /* "char literal"  */
#line 1097 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).val)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).val)));
#endif
}
#line 5839 "ripper.c"
        break;

    case 64: /* "numbered reference"  */
#line 1104 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "$%ld", ((*yyvaluep).val)->nd_nth);
#else
    rb_parser_printf(p, "%"PRIsVALUE, ((*yyvaluep).val));
#endif
}
#line 5851 "ripper.c"
        break;

    case 65: /* "back reference"  */
#line 1111 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "$%c", (int)((*yyvaluep).val)->nd_nth);
#else
    rb_parser_printf(p, "%"PRIsVALUE, ((*yyvaluep).val));
#endif
}
#line 5863 "ripper.c"
        break;

    case 66: /* "literal content"  */
#line 1097 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).val)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).val)));
#endif
}
#line 5875 "ripper.c"
        break;

    case 99: /* "operator-assignment"  */
#line 1090 "ripper.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).val)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).val))->nd_rval);
#endif
}
#line 5887 "ripper.c"
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
#line 1122 "ripper.y"
{
    RUBY_SET_YYLLOC_OF_NONE(yylloc);
}

#line 6349 "ripper.c"

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
#line 1327 "ripper.y"
                   {
			SET_LEX_STATE(EXPR_BEG);
			local_push(p, ifndef_ripper(1)+0);
		    }
#line 6551 "ripper.c"
    break;

  case 3:
#line 1332 "ripper.y"
                    {
#if 0
			if ((yyvsp[0].val) && !compile_for_eval) {
			    NODE *node = (yyvsp[0].val);
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
			p->eval_tree = NEW_SCOPE(0, block_append(p, p->eval_tree, (yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(program,v1);p->result=v2;}
			local_pop(p);
		    }
#line 6575 "ripper.c"
    break;

  case 4:
#line 1354 "ripper.y"
                    {
			(yyval.val) = void_stmts(p, (yyvsp[-1].val));
		    }
#line 6583 "ripper.c"
    break;

  case 5:
#line 1360 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=dispatch0(stmts_new);v2=dispatch0(void_stmt);v3=v1;v4=v2;v5=dispatch2(stmts_add,v3,v4);(yyval.val)=v5;}
		    }
#line 6594 "ripper.c"
    break;

  case 6:
#line 1367 "ripper.y"
                    {
#if 0
			(yyval.val) = newline_node((yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(stmts_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(stmts_add,v2,v3);(yyval.val)=v4;}
		    }
#line 6605 "ripper.c"
    break;

  case 7:
#line 1374 "ripper.y"
                    {
#if 0
			(yyval.val) = block_append(p, (yyvsp[-2].val), newline_node((yyvsp[0].val)));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(stmts_add,v1,v2);(yyval.val)=v3;}
		    }
#line 6616 "ripper.c"
    break;

  case 8:
#line 1381 "ripper.y"
                    {
			(yyval.val) = remove_begin((yyvsp[0].val));
		    }
#line 6624 "ripper.c"
    break;

  case 10:
#line 1388 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 6632 "ripper.c"
    break;

  case 11:
#line 1394 "ripper.y"
                    {
#if 0
			p->eval_tree_begin = block_append(p, p->eval_tree_begin,
							  NEW_BEGIN((yyvsp[-1].val), &(yyloc)));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(BEGIN,v1);(yyval.val)=v2;}
		    }
#line 6645 "ripper.c"
    break;

  case 12:
#line 1406 "ripper.y"
                         {if (!(yyvsp[-1].val)) {yyerror1(&(yylsp[0]), "else without rescue is useless");}}
#line 6651 "ripper.c"
    break;

  case 13:
#line 1409 "ripper.y"
                    {
#if 0
			(yyval.val) = new_bodystmt(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=escape_Qundef((yyvsp[-5].val));v2=escape_Qundef((yyvsp[-4].val));v3=escape_Qundef((yyvsp[-1].val));v4=escape_Qundef((yyvsp[0].val));v5=dispatch4(bodystmt,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 6662 "ripper.c"
    break;

  case 14:
#line 1418 "ripper.y"
                    {
#if 0
			(yyval.val) = new_bodystmt(p, (yyvsp[-2].val), (yyvsp[-1].val), 0, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=escape_Qundef((yyvsp[-2].val));v2=escape_Qundef((yyvsp[-1].val));v3=Qnil;v4=escape_Qundef((yyvsp[0].val));v5=dispatch4(bodystmt,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 6673 "ripper.c"
    break;

  case 15:
#line 1427 "ripper.y"
                    {
			(yyval.val) = void_stmts(p, (yyvsp[-1].val));
		    }
#line 6681 "ripper.c"
    break;

  case 16:
#line 1433 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=dispatch0(stmts_new);v2=dispatch0(void_stmt);v3=v1;v4=v2;v5=dispatch2(stmts_add,v3,v4);(yyval.val)=v5;}
		    }
#line 6692 "ripper.c"
    break;

  case 17:
#line 1440 "ripper.y"
                    {
#if 0
			(yyval.val) = newline_node((yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(stmts_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(stmts_add,v2,v3);(yyval.val)=v4;}
		    }
#line 6703 "ripper.c"
    break;

  case 18:
#line 1447 "ripper.y"
                    {
#if 0
			(yyval.val) = block_append(p, (yyvsp[-2].val), newline_node((yyvsp[0].val)));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(stmts_add,v1,v2);(yyval.val)=v3;}
		    }
#line 6714 "ripper.c"
    break;

  case 19:
#line 1454 "ripper.y"
                    {
			(yyval.val) = remove_begin((yyvsp[0].val));
		    }
#line 6722 "ripper.c"
    break;

  case 20:
#line 1460 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 6730 "ripper.c"
    break;

  case 21:
#line 1464 "ripper.y"
                    {
			yyerror1(&(yylsp[0]), "BEGIN is permitted only at toplevel");
		    }
#line 6738 "ripper.c"
    break;

  case 22:
#line 1468 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 6746 "ripper.c"
    break;

  case 23:
#line 1473 "ripper.y"
                                      {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 6752 "ripper.c"
    break;

  case 24:
#line 1474 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_ALIAS((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(alias,v1,v2);(yyval.val)=v3;}
		    }
#line 6763 "ripper.c"
    break;

  case 25:
#line 1481 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_VALIAS((yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(var_alias,v1,v2);(yyval.val)=v3;}
		    }
#line 6774 "ripper.c"
    break;

  case 26:
#line 1488 "ripper.y"
                    {
#if 0
			char buf[2];
			buf[0] = '$';
			buf[1] = (char)(yyvsp[0].val)->nd_nth;
			(yyval.val) = NEW_VALIAS((yyvsp[-1].val), rb_intern2(buf, 2), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(var_alias,v1,v2);(yyval.val)=v3;}
		    }
#line 6788 "ripper.c"
    break;

  case 27:
#line 1498 "ripper.y"
                    {
			static const char mesg[] = "can't make alias for the number variables";
#if 0
			yyerror1(&(yylsp[0]), mesg);
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=ERR_MESG();v2=(yyvsp[0].val);v3=dispatch2(alias_error,v1,v2);(yyval.val)=v3;}ripper_error(p);
		    }
#line 6801 "ripper.c"
    break;

  case 28:
#line 1507 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(undef,v1);(yyval.val)=v2;}
		    }
#line 6812 "ripper.c"
    break;

  case 29:
#line 1514 "ripper.y"
                    {
#if 0
			(yyval.val) = new_if(p, (yyvsp[0].val), remove_begin((yyvsp[-2].val)), 0, &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(if_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 6824 "ripper.c"
    break;

  case 30:
#line 1522 "ripper.y"
                    {
#if 0
			(yyval.val) = new_unless(p, (yyvsp[0].val), remove_begin((yyvsp[-2].val)), 0, &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(unless_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 6836 "ripper.c"
    break;

  case 31:
#line 1530 "ripper.y"
                    {
#if 0
			if ((yyvsp[-2].val) && nd_type_p((yyvsp[-2].val), NODE_BEGIN)) {
			    (yyval.val) = NEW_WHILE(cond(p, (yyvsp[0].val), &(yylsp[0])), (yyvsp[-2].val)->nd_body, 0, &(yyloc));
			}
			else {
			    (yyval.val) = NEW_WHILE(cond(p, (yyvsp[0].val), &(yylsp[0])), (yyvsp[-2].val), 1, &(yyloc));
			}
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(while_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 6852 "ripper.c"
    break;

  case 32:
#line 1542 "ripper.y"
                    {
#if 0
			if ((yyvsp[-2].val) && nd_type_p((yyvsp[-2].val), NODE_BEGIN)) {
			    (yyval.val) = NEW_UNTIL(cond(p, (yyvsp[0].val), &(yylsp[0])), (yyvsp[-2].val)->nd_body, 0, &(yyloc));
			}
			else {
			    (yyval.val) = NEW_UNTIL(cond(p, (yyvsp[0].val), &(yylsp[0])), (yyvsp[-2].val), 1, &(yyloc));
			}
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(until_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 6868 "ripper.c"
    break;

  case 33:
#line 1554 "ripper.y"
                    {
#if 0
			NODE *resq;
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			resq = NEW_RESBODY(0, remove_begin((yyvsp[0].val)), 0, &loc);
			(yyval.val) = NEW_RESCUE(remove_begin((yyvsp[-2].val)), resq, 0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 6882 "ripper.c"
    break;

  case 34:
#line 1564 "ripper.y"
                    {
			if (p->ctxt.in_def) {
			    rb_warn0("END in method; use at_exit");
			}
#if 0
			{
			    NODE *scope = NEW_NODE(
				NODE_SCOPE, 0 /* tbl */, (yyvsp[-1].val) /* body */, 0 /* args */, &(yyloc));
			    (yyval.val) = NEW_POSTEXE(scope, &(yyloc));
			}
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(END,v1);(yyval.val)=v2;}
		    }
#line 6900 "ripper.c"
    break;

  case 36:
#line 1579 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = node_assign(p, (yyvsp[-3].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[0].val);v3=dispatch2(massign,v1,v2);(yyval.val)=v3;}
		    }
#line 6912 "ripper.c"
    break;

  case 37:
#line 1587 "ripper.y"
                    {
#if 0
			(yyval.val) = node_assign(p, (yyvsp[-3].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[0].val);v3=dispatch2(assign,v1,v2);(yyval.val)=v3;}
		    }
#line 6923 "ripper.c"
    break;

  case 38:
#line 1594 "ripper.y"
                    {
#if 0
                        YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			(yyval.val) = node_assign(p, (yyvsp[-5].val), NEW_RESCUE((yyvsp[-2].val), NEW_RESBODY(0, remove_begin((yyvsp[0].val)), 0, &loc), 0, &(yyloc)), (yyvsp[-3].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);v4=(yyvsp[-5].val);v5=v3;v6=dispatch2(massign,v4,v5);(yyval.val)=v6;}
                    }
#line 6935 "ripper.c"
    break;

  case 39:
#line 1602 "ripper.y"
                    {
#if 0
			(yyval.val) = node_assign(p, (yyvsp[-3].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[0].val);v3=dispatch2(massign,v1,v2);(yyval.val)=v3;}
		    }
#line 6946 "ripper.c"
    break;

  case 41:
#line 1612 "ripper.y"
                    {
#if 0
			(yyval.val) = node_assign(p, (yyvsp[-3].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[0].val);v3=dispatch2(assign,v1,v2);(yyval.val)=v3;}
		    }
#line 6957 "ripper.c"
    break;

  case 42:
#line 1619 "ripper.y"
                    {
#if 0
			(yyval.val) = new_op_assign(p, (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[0].val);v4=dispatch3(opassign,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 6968 "ripper.c"
    break;

  case 43:
#line 1626 "ripper.y"
                    {
#if 0
			(yyval.val) = new_ary_op_assign(p, (yyvsp[-6].val), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-4]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-6].val);v2=escape_Qundef((yyvsp[-4].val));v3=dispatch2(aref_field,v1,v2);v4=v3;v5=(yyvsp[-2].val);v6=(yyvsp[0].val);v7=dispatch3(opassign,v4,v5,v6);(yyval.val)=v7;}

		    }
#line 6980 "ripper.c"
    break;

  case 44:
#line 1634 "ripper.y"
                    {
#if 0
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-5].val);v2=(yyvsp[-4].val);v3=(yyvsp[-3].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-2].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 6991 "ripper.c"
    break;

  case 45:
#line 1641 "ripper.y"
                    {
#if 0
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-5].val);v2=(yyvsp[-4].val);v3=(yyvsp[-3].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-2].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 7002 "ripper.c"
    break;

  case 46:
#line 1648 "ripper.y"
                    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-5]), &(yylsp[-3]));
			(yyval.val) = new_const_op_assign(p, NEW_COLON2((yyvsp[-5].val), (yyvsp[-3].val), &loc), (yyvsp[-2].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-5].val);v2=(yyvsp[-3].val);v3=dispatch2(const_path_field,v1,v2);v4=v3;v5=(yyvsp[-2].val);v6=(yyvsp[0].val);v7=dispatch3(opassign,v4,v5,v6);(yyval.val)=v7;}
		    }
#line 7014 "ripper.c"
    break;

  case 47:
#line 1656 "ripper.y"
                    {
#if 0
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-5].val), ID2VAL(idCOLON2), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-5].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-3].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-2].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 7025 "ripper.c"
    break;

  case 48:
#line 1663 "ripper.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
#if 0
			(yyval.val) = set_defun_body(p, (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[0].val);v2=Qnil;v3=Qnil;v4=Qnil;v5=dispatch4(bodystmt,v1,v2,v3,v4);(yyvsp[0].val)=v5;}
			{VALUE v1,v2,v3,v4;v1=get_value((yyvsp[-3].val));v2=(yyvsp[-2].val);v3=(yyvsp[0].val);v4=dispatch3(def,v1,v2,v3);(yyval.val)=v4;}
			local_pop(p);
		    }
#line 7040 "ripper.c"
    break;

  case 49:
#line 1674 "ripper.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
#if 0
			(yyvsp[-2].val) = rescued_expr(p, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.val) = set_defun_body(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-2].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);v4=v3;v5=Qnil;v6=Qnil;v7=Qnil;v8=dispatch4(bodystmt,v4,v5,v6,v7);(yyvsp[-2].val)=v8;}
			{VALUE v1,v2,v3,v4;v1=get_value((yyvsp[-5].val));v2=(yyvsp[-4].val);v3=(yyvsp[-2].val);v4=dispatch3(def,v1,v2,v3);(yyval.val)=v4;}
			local_pop(p);
		    }
#line 7056 "ripper.c"
    break;

  case 50:
#line 1686 "ripper.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
#if 0
			(yyval.val) = set_defun_body(p, (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			(yyvsp[-3].val) = get_value((yyvsp[-3].val));

			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[0].val);v2=Qnil;v3=Qnil;v4=Qnil;v5=dispatch4(bodystmt,v1,v2,v3,v4);(yyvsp[0].val)=v5;}
			{VALUE v1,v2,v3,v4,v5,v6;v1=AREF((yyvsp[-3].val), 0);v2=AREF((yyvsp[-3].val), 1);v3=AREF((yyvsp[-3].val), 2);v4=(yyvsp[-2].val);v5=(yyvsp[0].val);v6=dispatch5(defs,v1,v2,v3,v4,v5);(yyval.val)=v6;}
			local_pop(p);
		    }
#line 7073 "ripper.c"
    break;

  case 51:
#line 1699 "ripper.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
#if 0
			(yyvsp[-2].val) = rescued_expr(p, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.val) = set_defun_body(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-2].val), &(yyloc));
#endif
			(yyvsp[-5].val) = get_value((yyvsp[-5].val));

			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);v4=v3;v5=Qnil;v6=Qnil;v7=Qnil;v8=dispatch4(bodystmt,v4,v5,v6,v7);(yyvsp[-2].val)=v8;}
			{VALUE v1,v2,v3,v4,v5,v6;v1=AREF((yyvsp[-5].val), 0);v2=AREF((yyvsp[-5].val), 1);v3=AREF((yyvsp[-5].val), 2);v4=(yyvsp[-4].val);v5=(yyvsp[-2].val);v6=dispatch5(defs,v1,v2,v3,v4,v5);(yyval.val)=v6;}
			local_pop(p);
		    }
#line 7091 "ripper.c"
    break;

  case 52:
#line 1713 "ripper.y"
                    {
#if 0
			rb_backref_error(p, (yyvsp[-3].val));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=var_field(p, (yyvsp[-3].val));v2=(yyvsp[0].val);v3=dispatch2(assign,v1,v2);(yyval.val)=backref_error(p, RNODE((yyvsp[-3].val)), v3);}ripper_error(p);
		    }
#line 7103 "ripper.c"
    break;

  case 53:
#line 1723 "ripper.y"
                    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 7112 "ripper.c"
    break;

  case 54:
#line 1728 "ripper.y"
                    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			value_expr((yyvsp[-2].val));
			(yyval.val) = NEW_RESCUE((yyvsp[-2].val), NEW_RESBODY(0, remove_begin((yyvsp[0].val)), 0, &loc), 0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 7125 "ripper.c"
    break;

  case 57:
#line 1741 "ripper.y"
                    {
			(yyval.val) = logop(p, idAND, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7133 "ripper.c"
    break;

  case 58:
#line 1745 "ripper.y"
                    {
			(yyval.val) = logop(p, idOR, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7141 "ripper.c"
    break;

  case 59:
#line 1749 "ripper.y"
                    {
			(yyval.val) = call_uni_op(p, method_cond(p, (yyvsp[0].val), &(yylsp[0])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 7149 "ripper.c"
    break;

  case 60:
#line 1753 "ripper.y"
                    {
			(yyval.val) = call_uni_op(p, method_cond(p, (yyvsp[0].val), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 7157 "ripper.c"
    break;

  case 61:
#line 1757 "ripper.y"
                    {
			value_expr((yyvsp[-1].val));
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 7170 "ripper.c"
    break;

  case 62:
#line 1766 "ripper.y"
                    {
			pop_pvtbl(p, (yyvsp[-1].tbl));
			p->ctxt.in_kwarg = (yyvsp[-2].ctxt).in_kwarg;
#if 0
			(yyval.val) = NEW_CASE3((yyvsp[-3].val), NEW_IN((yyvsp[0].val), 0, 0, &(yylsp[0])), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[0].val);v2=Qnil;v3=Qnil;v4=dispatch3(in,v1,v2,v3);v5=(yyvsp[-3].val);v6=v4;v7=dispatch2(case,v5,v6);(yyval.val)=v7;}
		    }
#line 7183 "ripper.c"
    break;

  case 63:
#line 1775 "ripper.y"
                    {
			value_expr((yyvsp[-1].val));
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 7196 "ripper.c"
    break;

  case 64:
#line 1784 "ripper.y"
                    {
			pop_pvtbl(p, (yyvsp[-1].tbl));
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
#if 0
			(yyval.val) = NEW_CASE3((yyvsp[-3].val), NEW_IN((yyvsp[0].val), NEW_TRUE(&(yylsp[0])), NEW_FALSE(&(yylsp[0])), &(yylsp[0])), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[0].val);v2=Qnil;v3=Qnil;v4=dispatch3(in,v1,v2,v3);v5=(yyvsp[-3].val);v6=v4;v7=dispatch2(case,v5,v6);(yyval.val)=v7;}
		    }
#line 7209 "ripper.c"
    break;

  case 66:
#line 1796 "ripper.y"
                    {
			ID fname = get_id((yyvsp[0].val));
			ID cur_arg = p->cur_arg;
			YYSTYPE c = {.ctxt = p->ctxt};
			numparam_name(p, fname);
			local_push(p, 0);
			p->cur_arg = 0;
			p->ctxt.in_def = 1;
			(yyval.node) = NEW_NODE(NODE_SELF, /*vid*/cur_arg, /*mid*/fname, /*cval*/c.val, &(yyloc));
#if 0
#endif
			(yyval.val) = NEW_RIPPER(fname, get_value((yyvsp[0].val)), (yyval.val), &NULL_LOC);

		    }
#line 7228 "ripper.c"
    break;

  case 67:
#line 1813 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
#if 0
			(yyval.val) = NEW_NODE(NODE_DEFN, 0, (yyval.val)->nd_mid, (yyval.val), &(yyloc));
#endif
		    }
#line 7239 "ripper.c"
    break;

  case 68:
#line 1822 "ripper.y"
                    {
			SET_LEX_STATE(EXPR_FNAME);
			p->ctxt.in_argdef = 1;
		    }
#line 7248 "ripper.c"
    break;

  case 69:
#line 1827 "ripper.y"
                    {
			SET_LEX_STATE(EXPR_ENDFN|EXPR_LABEL); /* force for args */
			(yyval.val) = (yyvsp[0].val);
#if 0
			(yyval.val) = NEW_NODE(NODE_DEFS, (yyvsp[-3].val), (yyval.val)->nd_mid, (yyval.val), &(yyloc));
#endif
			VALUE ary = rb_ary_new_from_args(3, (yyvsp[-3].val), (yyvsp[-2].val), get_value((yyval.val)));
			add_mark_object(p, ary);
			(yyval.node)->nd_rval = ary;

		    }
#line 7264 "ripper.c"
    break;

  case 70:
#line 1841 "ripper.y"
                    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 7273 "ripper.c"
    break;

  case 71:
#line 1847 "ripper.y"
                  {COND_PUSH(1);}
#line 7279 "ripper.c"
    break;

  case 72:
#line 1847 "ripper.y"
                                                {COND_POP();}
#line 7285 "ripper.c"
    break;

  case 73:
#line 1848 "ripper.y"
                    {
			(yyval.val) = (yyvsp[-2].val);
		    }
#line 7293 "ripper.c"
    break;

  case 77:
#line 1859 "ripper.y"
                    {
#if 0
			(yyval.val) = new_qcall(p, (yyvsp[-2].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 7304 "ripper.c"
    break;

  case 78:
#line 1868 "ripper.y"
                    {
			(yyval.val) = (yyvsp[-1].val);
#if 0
			(yyval.val)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
#endif
		    }
#line 7316 "ripper.c"
    break;

  case 79:
#line 1878 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_FCALL((yyvsp[0].val), 0, &(yyloc));
			nd_set_line((yyval.val), p->tokline);
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 7328 "ripper.c"
    break;

  case 80:
#line 1888 "ripper.y"
                    {
#if 0
			(yyvsp[-1].val)->nd_args = (yyvsp[0].val);
			nd_set_last_loc((yyvsp[-1].val), (yylsp[0]).end_pos);
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(command,v1,v2);(yyval.val)=v3;}
		    }
#line 7341 "ripper.c"
    break;

  case 81:
#line 1897 "ripper.y"
                    {
#if 0
			block_dup_check(p, (yyvsp[-1].val), (yyvsp[0].val));
			(yyvsp[-2].val)->nd_args = (yyvsp[-1].val);
			(yyval.val) = method_add_block(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-2].val));
			nd_set_last_loc((yyvsp[-2].val), (yylsp[-1]).end_pos);
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(command,v1,v2);v4=v3;v5=(yyvsp[0].val);v6=dispatch2(method_add_block,v4,v5);(yyval.val)=v6;}
		    }
#line 7356 "ripper.c"
    break;

  case 82:
#line 1908 "ripper.y"
                    {
#if 0
			(yyval.val) = new_command_qcall(p, (yyvsp[-2].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), Qnull, &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=(yyvsp[0].val);v5=dispatch4(command_call,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 7367 "ripper.c"
    break;

  case 83:
#line 1915 "ripper.y"
                    {
#if 0
			(yyval.val) = new_command_qcall(p, (yyvsp[-3].val), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-2]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=(yyvsp[-1].val);v5=dispatch4(command_call,v1,v2,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=dispatch2(method_add_block,v6,v7);(yyval.val)=v8;}
		    }
#line 7378 "ripper.c"
    break;

  case 84:
#line 1922 "ripper.y"
                    {
#if 0
			(yyval.val) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), Qnull, &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-3].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-1].val);v4=(yyvsp[0].val);v5=dispatch4(command_call,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 7389 "ripper.c"
    break;

  case 85:
#line 1929 "ripper.y"
                    {
#if 0
			(yyval.val) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-2]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-2].val);v4=(yyvsp[-1].val);v5=dispatch4(command_call,v1,v2,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=dispatch2(method_add_block,v6,v7);(yyval.val)=v8;}
		   }
#line 7400 "ripper.c"
    break;

  case 86:
#line 1936 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_SUPER((yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(super,v1);(yyval.val)=v2;}
		    }
#line 7412 "ripper.c"
    break;

  case 87:
#line 1944 "ripper.y"
                    {
#if 0
			(yyval.val) = new_yield(p, (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(yield,v1);(yyval.val)=v2;}
		    }
#line 7424 "ripper.c"
    break;

  case 88:
#line 1952 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_RETURN(ret_args(p, (yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(return,v1);(yyval.val)=v2;}
		    }
#line 7435 "ripper.c"
    break;

  case 89:
#line 1959 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_BREAK(ret_args(p, (yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(break,v1);(yyval.val)=v2;}
		    }
#line 7446 "ripper.c"
    break;

  case 90:
#line 1966 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_NEXT(ret_args(p, (yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(next,v1);(yyval.val)=v2;}
		    }
#line 7457 "ripper.c"
    break;

  case 92:
#line 1976 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(mlhs_paren,v1);(yyval.val)=v2;}
		    }
#line 7468 "ripper.c"
    break;

  case 94:
#line 1986 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN(NEW_LIST((yyvsp[-1].val), &(yyloc)), 0, &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(mlhs_paren,v1);(yyval.val)=v2;}
		    }
#line 7479 "ripper.c"
    break;

  case 95:
#line 1995 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 7490 "ripper.c"
    break;

  case 96:
#line 2002 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN(list_append(p, (yyvsp[-1].val),(yyvsp[0].val)), 0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(mlhs_add,v1,v2);(yyval.val)=v3;}
		    }
#line 7501 "ripper.c"
    break;

  case 97:
#line 2009 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(mlhs_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 7512 "ripper.c"
    break;

  case 98:
#line 2016 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-4].val), NEW_POSTARG((yyvsp[-2].val),(yyvsp[0].val),&(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=dispatch2(mlhs_add_star,v1,v2);v4=v3;v5=(yyvsp[0].val);v6=dispatch2(mlhs_add_post,v4,v5);(yyval.val)=v6;}
		    }
#line 7523 "ripper.c"
    break;

  case 99:
#line 2023 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-1].val), NODE_SPECIAL_NO_NAME_REST, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=Qnil;v3=dispatch2(mlhs_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 7534 "ripper.c"
    break;

  case 100:
#line 2030 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-3].val), NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-3].val);v2=Qnil;v3=dispatch2(mlhs_add_star,v1,v2);v4=v3;v5=(yyvsp[0].val);v6=dispatch2(mlhs_add_post,v4,v5);(yyval.val)=v6;}
		    }
#line 7545 "ripper.c"
    break;

  case 101:
#line 2037 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN(0, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(mlhs_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 7556 "ripper.c"
    break;

  case 102:
#line 2044 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN(0, NEW_POSTARG((yyvsp[-2].val),(yyvsp[0].val),&(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[-2].val);v4=dispatch2(mlhs_add_star,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(mlhs_add_post,v5,v6);(yyval.val)=v7;}
		    }
#line 7567 "ripper.c"
    break;

  case 103:
#line 2051 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN(0, NODE_SPECIAL_NO_NAME_REST, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=Qnil;v4=dispatch2(mlhs_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 7578 "ripper.c"
    break;

  case 104:
#line 2058 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN(0, NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=dispatch0(mlhs_new);v2=v1;v3=Qnil;v4=dispatch2(mlhs_add_star,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(mlhs_add_post,v5,v6);(yyval.val)=v7;}
		    }
#line 7589 "ripper.c"
    break;

  case 106:
#line 2068 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(mlhs_paren,v1);(yyval.val)=v2;}
		    }
#line 7600 "ripper.c"
    break;

  case 107:
#line 2077 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[-1].val), &(yylsp[-1]));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[-1].val);v4=dispatch2(mlhs_add,v2,v3);(yyval.val)=v4;}
		    }
#line 7611 "ripper.c"
    break;

  case 108:
#line 2084 "ripper.y"
                    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), (yyvsp[-1].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(mlhs_add,v1,v2);(yyval.val)=v3;}
		    }
#line 7622 "ripper.c"
    break;

  case 109:
#line 2093 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(mlhs_add,v2,v3);(yyval.val)=v4;}
		    }
#line 7633 "ripper.c"
    break;

  case 110:
#line 2100 "ripper.y"
                    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(mlhs_add,v1,v2);(yyval.val)=v3;}
		    }
#line 7644 "ripper.c"
    break;

  case 111:
#line 2109 "ripper.y"
                    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 7655 "ripper.c"
    break;

  case 112:
#line 2116 "ripper.y"
                    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 7666 "ripper.c"
    break;

  case 113:
#line 2123 "ripper.y"
                    {
#if 0
			(yyval.val) = aryset(p, (yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=escape_Qundef((yyvsp[-1].val));v3=dispatch2(aref_field,v1,v2);(yyval.val)=v3;}
		    }
#line 7677 "ripper.c"
    break;

  case 114:
#line 2130 "ripper.y"
                    {
			if ((yyvsp[-1].val) == tANDDOT) {
			    yyerror1(&(yylsp[-1]), "&. inside multiple assignment destination");
			}
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 7691 "ripper.c"
    break;

  case 115:
#line 2140 "ripper.y"
                    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), idCOLON2, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_field,v1,v2);(yyval.val)=v3;}
		    }
#line 7702 "ripper.c"
    break;

  case 116:
#line 2147 "ripper.y"
                    {
			if ((yyvsp[-1].val) == tANDDOT) {
			    yyerror1(&(yylsp[-1]), "&. inside multiple assignment destination");
			}
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 7716 "ripper.c"
    break;

  case 117:
#line 2157 "ripper.y"
                    {
#if 0
			(yyval.val) = const_decl(p, NEW_COLON2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_field,v1,v2);(yyval.val)=const_decl(p, v3);}
		    }
#line 7727 "ripper.c"
    break;

  case 118:
#line 2164 "ripper.y"
                    {
#if 0
			(yyval.val) = const_decl(p, NEW_COLON3((yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(top_const_field,v1);(yyval.val)=const_decl(p, v2);}
		    }
#line 7738 "ripper.c"
    break;

  case 119:
#line 2171 "ripper.y"
                    {
#if 0
			rb_backref_error(p, (yyvsp[0].val));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			(yyval.val)=backref_error(p, RNODE((yyvsp[0].val)), var_field(p, (yyvsp[0].val)));ripper_error(p);
		    }
#line 7750 "ripper.c"
    break;

  case 120:
#line 2181 "ripper.y"
                    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 7761 "ripper.c"
    break;

  case 121:
#line 2188 "ripper.y"
                    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 7772 "ripper.c"
    break;

  case 122:
#line 2195 "ripper.y"
                    {
#if 0
			(yyval.val) = aryset(p, (yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=escape_Qundef((yyvsp[-1].val));v3=dispatch2(aref_field,v1,v2);(yyval.val)=v3;}
		    }
#line 7783 "ripper.c"
    break;

  case 123:
#line 2202 "ripper.y"
                    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 7794 "ripper.c"
    break;

  case 124:
#line 2209 "ripper.y"
                    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), idCOLON2, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 7805 "ripper.c"
    break;

  case 125:
#line 2216 "ripper.y"
                    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 7816 "ripper.c"
    break;

  case 126:
#line 2223 "ripper.y"
                    {
#if 0
			(yyval.val) = const_decl(p, NEW_COLON2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_field,v1,v2);(yyval.val)=const_decl(p, v3);}
		    }
#line 7827 "ripper.c"
    break;

  case 127:
#line 2230 "ripper.y"
                    {
#if 0
			(yyval.val) = const_decl(p, NEW_COLON3((yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(top_const_field,v1);(yyval.val)=const_decl(p, v2);}
		    }
#line 7838 "ripper.c"
    break;

  case 128:
#line 2237 "ripper.y"
                    {
#if 0
			rb_backref_error(p, (yyvsp[0].val));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			(yyval.val)=backref_error(p, RNODE((yyvsp[0].val)), var_field(p, (yyvsp[0].val)));ripper_error(p);
		    }
#line 7850 "ripper.c"
    break;

  case 129:
#line 2247 "ripper.y"
                    {
			static const char mesg[] = "class/module name must be CONSTANT";
#if 0
			yyerror1(&(yylsp[0]), mesg);
#endif
			{VALUE v1,v2,v3;v1=ERR_MESG();v2=(yyvsp[0].val);v3=dispatch2(class_name_error,v1,v2);(yyval.val)=v3;}ripper_error(p);
		    }
#line 7862 "ripper.c"
    break;

  case 131:
#line 2258 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_COLON3((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(top_const_ref,v1);(yyval.val)=v2;}
		    }
#line 7873 "ripper.c"
    break;

  case 132:
#line 2265 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_COLON2(0, (yyval.val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(const_ref,v1);(yyval.val)=v2;}
		    }
#line 7884 "ripper.c"
    break;

  case 133:
#line 2272 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_COLON2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_ref,v1,v2);(yyval.val)=v3;}
		    }
#line 7895 "ripper.c"
    break;

  case 137:
#line 2284 "ripper.y"
                    {
			SET_LEX_STATE(EXPR_ENDFN);
			(yyval.val) = (yyvsp[0].val);
		    }
#line 7904 "ripper.c"
    break;

  case 139:
#line 2292 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_LIT(ID2SYM((yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(symbol_literal,v1);(yyval.val)=v2;}
		    }
#line 7915 "ripper.c"
    break;

  case 141:
#line 2302 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_UNDEF((yyvsp[0].val), &(yyloc));
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 7926 "ripper.c"
    break;

  case 142:
#line 2308 "ripper.y"
                                 {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 7932 "ripper.c"
    break;

  case 143:
#line 2309 "ripper.y"
                    {
#if 0
			NODE *undef = NEW_UNDEF((yyvsp[0].val), &(yylsp[0]));
			(yyval.val) = block_append(p, (yyvsp[-3].val), undef);
#endif
			(yyval.val)=rb_ary_push((yyvsp[-3].val), get_value((yyvsp[0].val)));
		    }
#line 7944 "ripper.c"
    break;

  case 144:
#line 2318 "ripper.y"
                                { ifndef_ripper((yyval.val) = '|'); }
#line 7950 "ripper.c"
    break;

  case 145:
#line 2319 "ripper.y"
                                { ifndef_ripper((yyval.val) = '^'); }
#line 7956 "ripper.c"
    break;

  case 146:
#line 2320 "ripper.y"
                                { ifndef_ripper((yyval.val) = '&'); }
#line 7962 "ripper.c"
    break;

  case 147:
#line 2321 "ripper.y"
                                { ifndef_ripper((yyval.val) = tCMP); }
#line 7968 "ripper.c"
    break;

  case 148:
#line 2322 "ripper.y"
                                { ifndef_ripper((yyval.val) = tEQ); }
#line 7974 "ripper.c"
    break;

  case 149:
#line 2323 "ripper.y"
                                { ifndef_ripper((yyval.val) = tEQQ); }
#line 7980 "ripper.c"
    break;

  case 150:
#line 2324 "ripper.y"
                                { ifndef_ripper((yyval.val) = tMATCH); }
#line 7986 "ripper.c"
    break;

  case 151:
#line 2325 "ripper.y"
                                { ifndef_ripper((yyval.val) = tNMATCH); }
#line 7992 "ripper.c"
    break;

  case 152:
#line 2326 "ripper.y"
                                { ifndef_ripper((yyval.val) = '>'); }
#line 7998 "ripper.c"
    break;

  case 153:
#line 2327 "ripper.y"
                                { ifndef_ripper((yyval.val) = tGEQ); }
#line 8004 "ripper.c"
    break;

  case 154:
#line 2328 "ripper.y"
                                { ifndef_ripper((yyval.val) = '<'); }
#line 8010 "ripper.c"
    break;

  case 155:
#line 2329 "ripper.y"
                                { ifndef_ripper((yyval.val) = tLEQ); }
#line 8016 "ripper.c"
    break;

  case 156:
#line 2330 "ripper.y"
                                { ifndef_ripper((yyval.val) = tNEQ); }
#line 8022 "ripper.c"
    break;

  case 157:
#line 2331 "ripper.y"
                                { ifndef_ripper((yyval.val) = tLSHFT); }
#line 8028 "ripper.c"
    break;

  case 158:
#line 2332 "ripper.y"
                                { ifndef_ripper((yyval.val) = tRSHFT); }
#line 8034 "ripper.c"
    break;

  case 159:
#line 2333 "ripper.y"
                                { ifndef_ripper((yyval.val) = '+'); }
#line 8040 "ripper.c"
    break;

  case 160:
#line 2334 "ripper.y"
                                { ifndef_ripper((yyval.val) = '-'); }
#line 8046 "ripper.c"
    break;

  case 161:
#line 2335 "ripper.y"
                                { ifndef_ripper((yyval.val) = '*'); }
#line 8052 "ripper.c"
    break;

  case 162:
#line 2336 "ripper.y"
                                { ifndef_ripper((yyval.val) = '*'); }
#line 8058 "ripper.c"
    break;

  case 163:
#line 2337 "ripper.y"
                                { ifndef_ripper((yyval.val) = '/'); }
#line 8064 "ripper.c"
    break;

  case 164:
#line 2338 "ripper.y"
                                { ifndef_ripper((yyval.val) = '%'); }
#line 8070 "ripper.c"
    break;

  case 165:
#line 2339 "ripper.y"
                                { ifndef_ripper((yyval.val) = tPOW); }
#line 8076 "ripper.c"
    break;

  case 166:
#line 2340 "ripper.y"
                                { ifndef_ripper((yyval.val) = tDSTAR); }
#line 8082 "ripper.c"
    break;

  case 167:
#line 2341 "ripper.y"
                                { ifndef_ripper((yyval.val) = '!'); }
#line 8088 "ripper.c"
    break;

  case 168:
#line 2342 "ripper.y"
                                { ifndef_ripper((yyval.val) = '~'); }
#line 8094 "ripper.c"
    break;

  case 169:
#line 2343 "ripper.y"
                                { ifndef_ripper((yyval.val) = tUPLUS); }
#line 8100 "ripper.c"
    break;

  case 170:
#line 2344 "ripper.y"
                                { ifndef_ripper((yyval.val) = tUMINUS); }
#line 8106 "ripper.c"
    break;

  case 171:
#line 2345 "ripper.y"
                                { ifndef_ripper((yyval.val) = tAREF); }
#line 8112 "ripper.c"
    break;

  case 172:
#line 2346 "ripper.y"
                                { ifndef_ripper((yyval.val) = tASET); }
#line 8118 "ripper.c"
    break;

  case 173:
#line 2347 "ripper.y"
                                { ifndef_ripper((yyval.val) = '`'); }
#line 8124 "ripper.c"
    break;

  case 215:
#line 2365 "ripper.y"
                    {
#if 0
			(yyval.val) = node_assign(p, (yyvsp[-3].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[0].val);v3=dispatch2(assign,v1,v2);(yyval.val)=v3;}
		    }
#line 8135 "ripper.c"
    break;

  case 216:
#line 2372 "ripper.y"
                    {
#if 0
			(yyval.val) = new_op_assign(p, (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[0].val);v4=dispatch3(opassign,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 8146 "ripper.c"
    break;

  case 217:
#line 2379 "ripper.y"
                    {
#if 0
			(yyval.val) = new_ary_op_assign(p, (yyvsp[-6].val), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-4]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-6].val);v2=escape_Qundef((yyvsp[-4].val));v3=dispatch2(aref_field,v1,v2);v4=v3;v5=(yyvsp[-2].val);v6=(yyvsp[0].val);v7=dispatch3(opassign,v4,v5,v6);(yyval.val)=v7;}
		    }
#line 8157 "ripper.c"
    break;

  case 218:
#line 2386 "ripper.y"
                    {
#if 0
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-5].val);v2=(yyvsp[-4].val);v3=(yyvsp[-3].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-2].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 8168 "ripper.c"
    break;

  case 219:
#line 2393 "ripper.y"
                    {
#if 0
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-5].val);v2=(yyvsp[-4].val);v3=(yyvsp[-3].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-2].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 8179 "ripper.c"
    break;

  case 220:
#line 2400 "ripper.y"
                    {
#if 0
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-5].val), ID2VAL(idCOLON2), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-5].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-3].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-2].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 8190 "ripper.c"
    break;

  case 221:
#line 2407 "ripper.y"
                    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-5]), &(yylsp[-3]));
			(yyval.val) = new_const_op_assign(p, NEW_COLON2((yyvsp[-5].val), (yyvsp[-3].val), &loc), (yyvsp[-2].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-5].val);v2=(yyvsp[-3].val);v3=dispatch2(const_path_field,v1,v2);v4=v3;v5=(yyvsp[-2].val);v6=(yyvsp[0].val);v7=dispatch3(opassign,v4,v5,v6);(yyval.val)=v7;}
		    }
#line 8202 "ripper.c"
    break;

  case 222:
#line 2415 "ripper.y"
                    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-4]), &(yylsp[-3]));
			(yyval.val) = new_const_op_assign(p, NEW_COLON3((yyvsp[-3].val), &loc), (yyvsp[-2].val), (yyvsp[0].val), (yyvsp[-1].ctxt), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-3].val);v2=dispatch1(top_const_field,v1);v3=v2;v4=(yyvsp[-2].val);v5=(yyvsp[0].val);v6=dispatch3(opassign,v3,v4,v5);(yyval.val)=v6;}
		    }
#line 8214 "ripper.c"
    break;

  case 223:
#line 2423 "ripper.y"
                    {
#if 0
			rb_backref_error(p, (yyvsp[-3].val));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=var_field(p, (yyvsp[-3].val));v2=(yyvsp[-2].val);v3=(yyvsp[0].val);v4=dispatch3(opassign,v1,v2,v3);(yyval.val)=backref_error(p, RNODE((yyvsp[-3].val)), v4);}ripper_error(p);
		    }
#line 8226 "ripper.c"
    break;

  case 224:
#line 2431 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-2].val));
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(dot2,v1,v2);(yyval.val)=v3;}
		    }
#line 8239 "ripper.c"
    break;

  case 225:
#line 2440 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-2].val));
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT3((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(dot3,v1,v2);(yyval.val)=v3;}
		    }
#line 8252 "ripper.c"
    break;

  case 226:
#line 2449 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-1].val));
			(yyval.val) = NEW_DOT2((yyvsp[-1].val), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=Qnil;v3=dispatch2(dot2,v1,v2);(yyval.val)=v3;}
		    }
#line 8264 "ripper.c"
    break;

  case 227:
#line 2457 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-1].val));
			(yyval.val) = NEW_DOT3((yyvsp[-1].val), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=Qnil;v3=dispatch2(dot3,v1,v2);(yyval.val)=v3;}
		    }
#line 8276 "ripper.c"
    break;

  case 228:
#line 2465 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT2(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=Qnil;v2=(yyvsp[0].val);v3=dispatch2(dot2,v1,v2);(yyval.val)=v3;}
		    }
#line 8288 "ripper.c"
    break;

  case 229:
#line 2473 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT3(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=Qnil;v2=(yyvsp[0].val);v3=dispatch2(dot3,v1,v2);(yyval.val)=v3;}
		    }
#line 8300 "ripper.c"
    break;

  case 230:
#line 2481 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '+', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8308 "ripper.c"
    break;

  case 231:
#line 2485 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '-', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8316 "ripper.c"
    break;

  case 232:
#line 2489 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '*', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8324 "ripper.c"
    break;

  case 233:
#line 2493 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '/', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8332 "ripper.c"
    break;

  case 234:
#line 2497 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '%', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8340 "ripper.c"
    break;

  case 235:
#line 2501 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idPow, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8348 "ripper.c"
    break;

  case 236:
#line 2505 "ripper.y"
                    {
			(yyval.val) = call_uni_op(p, call_bin_op(p, (yyvsp[-2].val), idPow, (yyvsp[0].val), &(yylsp[-2]), &(yyloc)), idUMinus, &(yylsp[-3]), &(yyloc));
		    }
#line 8356 "ripper.c"
    break;

  case 237:
#line 2509 "ripper.y"
                    {
			(yyval.val) = call_uni_op(p, (yyvsp[0].val), idUPlus, &(yylsp[-1]), &(yyloc));
		    }
#line 8364 "ripper.c"
    break;

  case 238:
#line 2513 "ripper.y"
                    {
			(yyval.val) = call_uni_op(p, (yyvsp[0].val), idUMinus, &(yylsp[-1]), &(yyloc));
		    }
#line 8372 "ripper.c"
    break;

  case 239:
#line 2517 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '|', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8380 "ripper.c"
    break;

  case 240:
#line 2521 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '^', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8388 "ripper.c"
    break;

  case 241:
#line 2525 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '&', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8396 "ripper.c"
    break;

  case 242:
#line 2529 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idCmp, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8404 "ripper.c"
    break;

  case 244:
#line 2534 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idEq, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8412 "ripper.c"
    break;

  case 245:
#line 2538 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idEqq, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8420 "ripper.c"
    break;

  case 246:
#line 2542 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idNeq, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8428 "ripper.c"
    break;

  case 247:
#line 2546 "ripper.y"
                    {
			(yyval.val) = match_op(p, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8436 "ripper.c"
    break;

  case 248:
#line 2550 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idNeqTilde, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8444 "ripper.c"
    break;

  case 249:
#line 2554 "ripper.y"
                    {
			(yyval.val) = call_uni_op(p, method_cond(p, (yyvsp[0].val), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 8452 "ripper.c"
    break;

  case 250:
#line 2558 "ripper.y"
                    {
			(yyval.val) = call_uni_op(p, (yyvsp[0].val), '~', &(yylsp[-1]), &(yyloc));
		    }
#line 8460 "ripper.c"
    break;

  case 251:
#line 2562 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idLTLT, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8468 "ripper.c"
    break;

  case 252:
#line 2566 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idGTGT, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8476 "ripper.c"
    break;

  case 253:
#line 2570 "ripper.y"
                    {
			(yyval.val) = logop(p, idANDOP, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8484 "ripper.c"
    break;

  case 254:
#line 2574 "ripper.y"
                    {
			(yyval.val) = logop(p, idOROP, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8492 "ripper.c"
    break;

  case 255:
#line 2577 "ripper.y"
                                         {p->ctxt.in_defined = 1;}
#line 8498 "ripper.c"
    break;

  case 256:
#line 2578 "ripper.y"
                    {
			p->ctxt.in_defined = 0;
			(yyval.val) = new_defined(p, (yyvsp[0].val), &(yyloc));
		    }
#line 8507 "ripper.c"
    break;

  case 257:
#line 2583 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-5].val));
			(yyval.val) = new_if(p, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-5].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-5].val);v2=(yyvsp[-3].val);v3=(yyvsp[0].val);v4=dispatch3(ifop,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 8520 "ripper.c"
    break;

  case 258:
#line 2592 "ripper.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
#if 0
			(yyval.val) = set_defun_body(p, (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[0].val);v2=Qnil;v3=Qnil;v4=Qnil;v5=dispatch4(bodystmt,v1,v2,v3,v4);(yyvsp[0].val)=v5;}
			{VALUE v1,v2,v3,v4;v1=get_value((yyvsp[-3].val));v2=(yyvsp[-2].val);v3=(yyvsp[0].val);v4=dispatch3(def,v1,v2,v3);(yyval.val)=v4;}
			local_pop(p);
		    }
#line 8535 "ripper.c"
    break;

  case 259:
#line 2603 "ripper.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
#if 0
			(yyvsp[-2].val) = rescued_expr(p, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.val) = set_defun_body(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-2].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);v4=v3;v5=Qnil;v6=Qnil;v7=Qnil;v8=dispatch4(bodystmt,v4,v5,v6,v7);(yyvsp[-2].val)=v8;}
			{VALUE v1,v2,v3,v4;v1=get_value((yyvsp[-5].val));v2=(yyvsp[-4].val);v3=(yyvsp[-2].val);v4=dispatch3(def,v1,v2,v3);(yyval.val)=v4;}
			local_pop(p);
		    }
#line 8551 "ripper.c"
    break;

  case 260:
#line 2615 "ripper.y"
                    {
			endless_method_name(p, (yyvsp[-3].node), &(yylsp[-3]));
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
#if 0
			(yyval.val) = set_defun_body(p, (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			(yyvsp[-3].val) = get_value((yyvsp[-3].val));

			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[0].val);v2=Qnil;v3=Qnil;v4=Qnil;v5=dispatch4(bodystmt,v1,v2,v3,v4);(yyvsp[0].val)=v5;}
			{VALUE v1,v2,v3,v4,v5,v6;v1=AREF((yyvsp[-3].val), 0);v2=AREF((yyvsp[-3].val), 1);v3=AREF((yyvsp[-3].val), 2);v4=(yyvsp[-2].val);v5=(yyvsp[0].val);v6=dispatch5(defs,v1,v2,v3,v4,v5);(yyval.val)=v6;}
			local_pop(p);
		    }
#line 8568 "ripper.c"
    break;

  case 261:
#line 2628 "ripper.y"
                    {
			endless_method_name(p, (yyvsp[-5].node), &(yylsp[-5]));
			restore_defun(p, (yyvsp[-5].node)->nd_defn);
#if 0
			(yyvsp[-2].val) = rescued_expr(p, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
			(yyval.val) = set_defun_body(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-2].val), &(yyloc));
#endif
			(yyvsp[-5].val) = get_value((yyvsp[-5].val));

			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);v4=v3;v5=Qnil;v6=Qnil;v7=Qnil;v8=dispatch4(bodystmt,v4,v5,v6,v7);(yyvsp[-2].val)=v8;}
			{VALUE v1,v2,v3,v4,v5,v6;v1=AREF((yyvsp[-5].val), 0);v2=AREF((yyvsp[-5].val), 1);v3=AREF((yyvsp[-5].val), 2);v4=(yyvsp[-4].val);v5=(yyvsp[-2].val);v6=dispatch5(defs,v1,v2,v3,v4,v5);(yyval.val)=v6;}
			local_pop(p);
		    }
#line 8586 "ripper.c"
    break;

  case 262:
#line 2642 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 8594 "ripper.c"
    break;

  case 263:
#line 2647 "ripper.y"
                       {(yyval.val) = '>';}
#line 8600 "ripper.c"
    break;

  case 264:
#line 2648 "ripper.y"
                       {(yyval.val) = '<';}
#line 8606 "ripper.c"
    break;

  case 265:
#line 2649 "ripper.y"
                       {(yyval.val) = idGE;}
#line 8612 "ripper.c"
    break;

  case 266:
#line 2650 "ripper.y"
                       {(yyval.val) = idLE;}
#line 8618 "ripper.c"
    break;

  case 267:
#line 2654 "ripper.y"
                    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8626 "ripper.c"
    break;

  case 268:
#line 2658 "ripper.y"
                    {
			rb_warning1("comparison '%s' after comparison", WARN_ID((yyvsp[-1].val)));
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 8635 "ripper.c"
    break;

  case 269:
#line 2665 "ripper.y"
                    {
			(yyval.ctxt) = p->ctxt;
		    }
#line 8643 "ripper.c"
    break;

  case 270:
#line 2669 "ripper.y"
                    {
			(yyval.ctxt) = p->ctxt;
		    }
#line 8651 "ripper.c"
    break;

  case 271:
#line 2675 "ripper.y"
                    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 8660 "ripper.c"
    break;

  case 273:
#line 2683 "ripper.y"
                    {
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 8668 "ripper.c"
    break;

  case 274:
#line 2687 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? arg_append(p, (yyvsp[-3].val), new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].val);
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-1].val);v2=dispatch1(bare_assoc_hash,v1);v3=(yyvsp[-3].val);v4=v2;v5=dispatch2(args_add,v3,v4);(yyval.val)=v5;}
		    }
#line 8679 "ripper.c"
    break;

  case 275:
#line 2694 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? NEW_LIST(new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yyloc)) : 0;
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=dispatch0(args_new);v2=(yyvsp[-1].val);v3=dispatch1(bare_assoc_hash,v2);v4=v1;v5=v3;v6=dispatch2(args_add,v4,v5);(yyval.val)=v6;}
		    }
#line 8690 "ripper.c"
    break;

  case 276:
#line 2703 "ripper.y"
                    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 8699 "ripper.c"
    break;

  case 277:
#line 2708 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-2].val));
			(yyval.val) = rescued_expr(p, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 8711 "ripper.c"
    break;

  case 278:
#line 2718 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=escape_Qundef((yyvsp[-1].val));v2=dispatch1(arg_paren,v1);(yyval.val)=v2;}
		    }
#line 8722 "ripper.c"
    break;

  case 279:
#line 2725 "ripper.y"
                    {
			if (!check_forwarding_args(p)) {
			    (yyval.val) = Qnone;
			}
			else {
#if 0
			    (yyval.val) = new_args_forward_call(p, (yyvsp[-3].val), &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-3].val);v2=(yyvsp[-1].val);v3=dispatch2(args_add,v1,v2);v4=v3;v5=dispatch1(arg_paren,v4);(yyval.val)=v5;}
			}
		    }
#line 8738 "ripper.c"
    break;

  case 280:
#line 2737 "ripper.y"
                    {
			if (!check_forwarding_args(p)) {
			    (yyval.val) = Qnone;
			}
			else {
#if 0
			    (yyval.val) = new_args_forward_call(p, 0, &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(arg_paren,v1);(yyval.val)=v2;}
			}
		    }
#line 8754 "ripper.c"
    break;

  case 285:
#line 2757 "ripper.y"
                    {
		      (yyval.val) = (yyvsp[-1].val);
		    }
#line 8762 "ripper.c"
    break;

  case 286:
#line 2761 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? arg_append(p, (yyvsp[-3].val), new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].val);
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-1].val);v2=dispatch1(bare_assoc_hash,v1);v3=(yyvsp[-3].val);v4=v2;v5=dispatch2(args_add,v3,v4);(yyval.val)=v5;}
		    }
#line 8773 "ripper.c"
    break;

  case 287:
#line 2768 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? NEW_LIST(new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yylsp[-1])) : 0;
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=dispatch0(args_new);v2=(yyvsp[-1].val);v3=dispatch1(bare_assoc_hash,v2);v4=v1;v5=v3;v6=dispatch2(args_add,v4,v5);(yyval.val)=v6;}
		    }
#line 8784 "ripper.c"
    break;

  case 288:
#line 2777 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add,v2,v3);(yyval.val)=v4;}
		    }
#line 8796 "ripper.c"
    break;

  case 289:
#line 2785 "ripper.y"
                    {
#if 0
			(yyval.val) = arg_blk_pass((yyvsp[-1].val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(args_add_block,v1,v2);(yyval.val)=v3;}
		    }
#line 8807 "ripper.c"
    break;

  case 290:
#line 2792 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? NEW_LIST(new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yylsp[-1])) : 0;
			(yyval.val) = arg_blk_pass((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8,v9;v1=dispatch0(args_new);v2=(yyvsp[-1].val);v3=dispatch1(bare_assoc_hash,v2);v4=v1;v5=v3;v6=dispatch2(args_add,v4,v5);v7=v6;v8=(yyvsp[0].val);v9=dispatch2(args_add_block,v7,v8);(yyval.val)=v9;}
		    }
#line 8819 "ripper.c"
    break;

  case 291:
#line 2800 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? arg_append(p, (yyvsp[-3].val), new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].val);
			(yyval.val) = arg_blk_pass((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-1].val);v2=dispatch1(bare_assoc_hash,v1);v3=(yyvsp[-3].val);v4=v2;v5=dispatch2(args_add,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=dispatch2(args_add_block,v6,v7);(yyval.val)=v8;}
		    }
#line 8831 "ripper.c"
    break;

  case 292:
#line 2808 "ripper.y"
                        {{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add_block,v2,v3);(yyval.val)=v4;}}
#line 8837 "ripper.c"
    break;

  case 293:
#line 2811 "ripper.y"
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
#line 8859 "ripper.c"
    break;

  case 294:
#line 2829 "ripper.y"
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
			(yyval.val) = (yyvsp[0].val);
		    }
#line 8881 "ripper.c"
    break;

  case 295:
#line 2849 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_BLOCK_PASS((yyvsp[0].val), &(yyloc));
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 8892 "ripper.c"
    break;

  case 296:
#line 2856 "ripper.y"
                    {
#if 0
                        if (!local_id(p, ANON_BLOCK_ID)) {
                            compile_error(p, "no anonymous block parameter");
                        }
                        (yyval.val) = NEW_BLOCK_PASS(NEW_LVAR(ANON_BLOCK_ID, &(yylsp[0])), &(yyloc));
#endif
                    (yyval.val) = Qnil;

                    }
#line 8907 "ripper.c"
    break;

  case 297:
#line 2869 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 8915 "ripper.c"
    break;

  case 298:
#line 2873 "ripper.y"
                    {
			(yyval.val) = 0;
		    }
#line 8923 "ripper.c"
    break;

  case 299:
#line 2880 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add,v2,v3);(yyval.val)=v4;}
		    }
#line 8934 "ripper.c"
    break;

  case 300:
#line 2887 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_SPLAT((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 8945 "ripper.c"
    break;

  case 301:
#line 2894 "ripper.y"
                    {
#if 0
			(yyval.val) = last_arg_append(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(args_add,v1,v2);(yyval.val)=v3;}
		    }
#line 8956 "ripper.c"
    break;

  case 302:
#line 2901 "ripper.y"
                    {
#if 0
			(yyval.val) = rest_arg_append(p, (yyvsp[-3].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[0].val);v3=dispatch2(args_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 8967 "ripper.c"
    break;

  case 305:
#line 2916 "ripper.y"
                    {
#if 0
			(yyval.val) = last_arg_append(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-2].val);v2=dispatch1(mrhs_new_from_args,v1);v3=v2;v4=(yyvsp[0].val);v5=dispatch2(mrhs_add,v3,v4);(yyval.val)=v5;}
		    }
#line 8978 "ripper.c"
    break;

  case 306:
#line 2923 "ripper.y"
                    {
#if 0
			(yyval.val) = rest_arg_append(p, (yyvsp[-3].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-3].val);v2=dispatch1(mrhs_new_from_args,v1);v3=v2;v4=(yyvsp[0].val);v5=dispatch2(mrhs_add_star,v3,v4);(yyval.val)=v5;}
		    }
#line 8989 "ripper.c"
    break;

  case 307:
#line 2930 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_SPLAT((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mrhs_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(mrhs_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 9000 "ripper.c"
    break;

  case 318:
#line 2949 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_FCALL((yyvsp[0].val), 0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[0].val);v2=dispatch1(fcall,v1);v3=dispatch0(args_new);v4=v2;v5=v3;v6=dispatch2(method_add_arg,v4,v5);(yyval.val)=v6;}
		    }
#line 9011 "ripper.c"
    break;

  case 319:
#line 2956 "ripper.y"
                    {
			CMDARG_PUSH(0);
		    }
#line 9019 "ripper.c"
    break;

  case 320:
#line 2961 "ripper.y"
                    {
			CMDARG_POP();
#if 0
			set_line_body((yyvsp[-1].val), (yylsp[-3]).end_pos.lineno);
			(yyval.val) = NEW_BEGIN((yyvsp[-1].val), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-3]).end_pos.lineno);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(begin,v1);(yyval.val)=v2;}
		    }
#line 9033 "ripper.c"
    break;

  case 321:
#line 2970 "ripper.y"
                              {SET_LEX_STATE(EXPR_ENDARG);}
#line 9039 "ripper.c"
    break;

  case 322:
#line 2971 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=0;v2=dispatch1(paren,v1);(yyval.val)=v2;}
		    }
#line 9050 "ripper.c"
    break;

  case 323:
#line 2977 "ripper.y"
                                   {SET_LEX_STATE(EXPR_ENDARG);}
#line 9056 "ripper.c"
    break;

  case 324:
#line 2978 "ripper.y"
                    {
#if 0
			if (nd_type_p((yyvsp[-2].val), NODE_SELF)) (yyvsp[-2].val)->nd_state = 0;
			(yyval.val) = (yyvsp[-2].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-2].val);v2=dispatch1(paren,v1);(yyval.val)=v2;}
		    }
#line 9068 "ripper.c"
    break;

  case 325:
#line 2986 "ripper.y"
                    {
#if 0
			if (nd_type_p((yyvsp[-1].val), NODE_SELF)) (yyvsp[-1].val)->nd_state = 0;
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(paren,v1);(yyval.val)=v2;}
		    }
#line 9080 "ripper.c"
    break;

  case 326:
#line 2994 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_COLON2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_ref,v1,v2);(yyval.val)=v3;}
		    }
#line 9091 "ripper.c"
    break;

  case 327:
#line 3001 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_COLON3((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(top_const_ref,v1);(yyval.val)=v2;}
		    }
#line 9102 "ripper.c"
    break;

  case 328:
#line 3008 "ripper.y"
                    {
#if 0
			(yyval.val) = make_list((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=escape_Qundef((yyvsp[-1].val));v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 9113 "ripper.c"
    break;

  case 329:
#line 3015 "ripper.y"
                    {
#if 0
			(yyval.val) = new_hash(p, (yyvsp[-1].val), &(yyloc));
			(yyval.val)->nd_brace = TRUE;
#endif
			{VALUE v1,v2;v1=escape_Qundef((yyvsp[-1].val));v2=dispatch1(hash,v1);(yyval.val)=v2;}
		    }
#line 9125 "ripper.c"
    break;

  case 330:
#line 3023 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_RETURN(0, &(yyloc));
#endif
			{VALUE v1;v1=dispatch0(return0);(yyval.val)=v1;}
		    }
#line 9136 "ripper.c"
    break;

  case 331:
#line 3030 "ripper.y"
                    {
#if 0
			(yyval.val) = new_yield(p, (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-1].val);v2=dispatch1(paren,v1);v3=v2;v4=dispatch1(yield,v3);(yyval.val)=v4;}
		    }
#line 9147 "ripper.c"
    break;

  case 332:
#line 3037 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_YIELD(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=dispatch0(args_new);v2=v1;v3=dispatch1(paren,v2);v4=v3;v5=dispatch1(yield,v4);(yyval.val)=v5;}
		    }
#line 9158 "ripper.c"
    break;

  case 333:
#line 3044 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_YIELD(0, &(yyloc));
#endif
			{VALUE v1;v1=dispatch0(yield0);(yyval.val)=v1;}
		    }
#line 9169 "ripper.c"
    break;

  case 334:
#line 3050 "ripper.y"
                                             {p->ctxt.in_defined = 1;}
#line 9175 "ripper.c"
    break;

  case 335:
#line 3051 "ripper.y"
                    {
			p->ctxt.in_defined = 0;
			(yyval.val) = new_defined(p, (yyvsp[-1].val), &(yyloc));
		    }
#line 9184 "ripper.c"
    break;

  case 336:
#line 3056 "ripper.y"
                    {
			(yyval.val) = call_uni_op(p, method_cond(p, (yyvsp[-1].val), &(yylsp[-1])), METHOD_NOT, &(yylsp[-3]), &(yyloc));
		    }
#line 9192 "ripper.c"
    break;

  case 337:
#line 3060 "ripper.y"
                    {
			(yyval.val) = call_uni_op(p, method_cond(p, new_nil(&(yylsp[-1])), &(yylsp[-1])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 9200 "ripper.c"
    break;

  case 338:
#line 3064 "ripper.y"
                    {
#if 0
			(yyval.val) = method_add_block(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8,v9;v1=(yyvsp[-1].val);v2=dispatch1(fcall,v1);v3=dispatch0(args_new);v4=v2;v5=v3;v6=dispatch2(method_add_arg,v4,v5);v7=v6;v8=(yyvsp[0].val);v9=dispatch2(method_add_block,v7,v8);(yyval.val)=v9;}
		    }
#line 9211 "ripper.c"
    break;

  case 340:
#line 3072 "ripper.y"
                    {
#if 0
			block_dup_check(p, (yyvsp[-1].val)->nd_args, (yyvsp[0].val));
			(yyval.val) = method_add_block(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(method_add_block,v1,v2);(yyval.val)=v3;}
		    }
#line 9223 "ripper.c"
    break;

  case 342:
#line 3084 "ripper.y"
                    {
#if 0
			(yyval.val) = new_if(p, (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-4].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=escape_Qundef((yyvsp[-1].val));v4=dispatch3(if,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 9235 "ripper.c"
    break;

  case 343:
#line 3095 "ripper.y"
                    {
#if 0
			(yyval.val) = new_unless(p, (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-4].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=escape_Qundef((yyvsp[-1].val));v4=dispatch3(unless,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 9247 "ripper.c"
    break;

  case 344:
#line 3105 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_WHILE(cond(p, (yyvsp[-2].val), &(yylsp[-2])), (yyvsp[-1].val), 1, &(yyloc));
			fixpos((yyval.val), (yyvsp[-2].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(while,v1,v2);(yyval.val)=v3;}
		    }
#line 9259 "ripper.c"
    break;

  case 345:
#line 3115 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_UNTIL(cond(p, (yyvsp[-2].val), &(yylsp[-2])), (yyvsp[-1].val), 1, &(yyloc));
			fixpos((yyval.val), (yyvsp[-2].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(until,v1,v2);(yyval.val)=v3;}
		    }
#line 9271 "ripper.c"
    break;

  case 346:
#line 3123 "ripper.y"
                    {
			(yyval.val) = p->case_labels;
			p->case_labels = Qnil;
		    }
#line 9280 "ripper.c"
    break;

  case 347:
#line 3129 "ripper.y"
                    {
			if (RTEST(p->case_labels)) rb_hash_clear(p->case_labels);
			p->case_labels = (yyvsp[-2].val);
#if 0
			(yyval.val) = NEW_CASE((yyvsp[-4].val), (yyvsp[-1].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-4].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-4].val);v2=(yyvsp[-1].val);v3=dispatch2(case,v1,v2);(yyval.val)=v3;}
		    }
#line 9294 "ripper.c"
    break;

  case 348:
#line 3139 "ripper.y"
                    {
			(yyval.val) = p->case_labels;
			p->case_labels = 0;
		    }
#line 9303 "ripper.c"
    break;

  case 349:
#line 3145 "ripper.y"
                    {
			if (RTEST(p->case_labels)) rb_hash_clear(p->case_labels);
			p->case_labels = (yyvsp[-2].val);
#if 0
			(yyval.val) = NEW_CASE2((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=Qnil;v2=(yyvsp[-1].val);v3=dispatch2(case,v1,v2);(yyval.val)=v3;}
		    }
#line 9316 "ripper.c"
    break;

  case 350:
#line 3156 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_CASE3((yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[-1].val);v3=dispatch2(case,v1,v2);(yyval.val)=v3;}
		    }
#line 9327 "ripper.c"
    break;

  case 351:
#line 3165 "ripper.y"
                    {
#if 0
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

			switch (nd_type((yyvsp[-4].val))) {
			  case NODE_LASGN:
			  case NODE_DASGN: /* e.each {|internal_var| a = internal_var; ... } */
			    (yyvsp[-4].val)->nd_value = internal_var;
			    id = 0;
			    m->nd_plen = 1;
			    m->nd_next = (yyvsp[-4].val);
			    break;
			  case NODE_MASGN: /* e.each {|*internal_var| a, b, c = (internal_var.length == 1 && Array === (tmp = internal_var[0]) ? tmp : internal_var); ... } */
			    m->nd_next = node_assign(p, (yyvsp[-4].val), NEW_FOR_MASGN(internal_var, &(yylsp[-4])), NO_LEX_CTXT, &(yylsp[-4]));
			    break;
			  default: /* e.each {|*internal_var| @a, B, c[1], d.attr = internal_val; ... } */
			    m->nd_next = node_assign(p, NEW_MASGN(NEW_LIST((yyvsp[-4].val), &(yylsp[-4])), 0, &(yylsp[-4])), internal_var, NO_LEX_CTXT, &(yylsp[-4]));
			}
			/* {|*internal_id| <m> = internal_id; ... } */
			args = new_args(p, m, 0, id, 0, new_args_tail(p, 0, 0, 0, &(yylsp[-4])), &(yylsp[-4]));
			scope = NEW_NODE(NODE_SCOPE, tbl, (yyvsp[-1].val), args, &(yyloc));
			(yyval.val) = NEW_FOR((yyvsp[-2].val), scope, &(yyloc));
			fixpos((yyval.val), (yyvsp[-4].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(for,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 9371 "ripper.c"
    break;

  case 352:
#line 3205 "ripper.y"
                    {
			if (p->ctxt.in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[-1]));
			    yyerror1(&loc, "class definition in method body");
			}
			p->ctxt.in_class = 1;
			local_push(p, 0);
		    }
#line 9384 "ripper.c"
    break;

  case 353:
#line 3215 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_CLASS((yyvsp[-4].val), (yyvsp[-1].val), (yyvsp[-3].val), &(yyloc));
			nd_set_line((yyval.val)->nd_body, (yylsp[0]).end_pos.lineno);
			set_line_body((yyvsp[-1].val), (yylsp[-3]).end_pos.lineno);
			nd_set_line((yyval.val), (yylsp[-3]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-1].val);v4=dispatch3(class,v1,v2,v3);(yyval.val)=v4;}
			local_pop(p);
			p->ctxt.in_class = (yyvsp[-5].ctxt).in_class;
			p->ctxt.shareable_constant_value = (yyvsp[-5].ctxt).shareable_constant_value;
		    }
#line 9401 "ripper.c"
    break;

  case 354:
#line 3228 "ripper.y"
                    {
			p->ctxt.in_def = 0;
			p->ctxt.in_class = 0;
			local_push(p, 0);
		    }
#line 9411 "ripper.c"
    break;

  case 355:
#line 3236 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_SCLASS((yyvsp[-4].val), (yyvsp[-1].val), &(yyloc));
			nd_set_line((yyval.val)->nd_body, (yylsp[0]).end_pos.lineno);
			set_line_body((yyvsp[-1].val), nd_line((yyvsp[-4].val)));
			fixpos((yyval.val), (yyvsp[-4].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-4].val);v2=(yyvsp[-1].val);v3=dispatch2(sclass,v1,v2);(yyval.val)=v3;}
			local_pop(p);
			p->ctxt.in_def = (yyvsp[-6].ctxt).in_def;
			p->ctxt.in_class = (yyvsp[-6].ctxt).in_class;
			p->ctxt.shareable_constant_value = (yyvsp[-6].ctxt).shareable_constant_value;
		    }
#line 9429 "ripper.c"
    break;

  case 356:
#line 3250 "ripper.y"
                    {
			if (p->ctxt.in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			    yyerror1(&loc, "module definition in method body");
			}
			p->ctxt.in_class = 1;
			local_push(p, 0);
		    }
#line 9442 "ripper.c"
    break;

  case 357:
#line 3260 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MODULE((yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
			nd_set_line((yyval.val)->nd_body, (yylsp[0]).end_pos.lineno);
			set_line_body((yyvsp[-1].val), (yylsp[-3]).end_pos.lineno);
			nd_set_line((yyval.val), (yylsp[-3]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[-1].val);v3=dispatch2(module,v1,v2);(yyval.val)=v3;}
			local_pop(p);
			p->ctxt.in_class = (yyvsp[-4].ctxt).in_class;
			p->ctxt.shareable_constant_value = (yyvsp[-4].ctxt).shareable_constant_value;
		    }
#line 9459 "ripper.c"
    break;

  case 358:
#line 3276 "ripper.y"
                    {
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
#if 0
			(yyval.val) = set_defun_body(p, (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=get_value((yyvsp[-3].val));v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(def,v1,v2,v3);(yyval.val)=v4;}
			local_pop(p);
		    }
#line 9472 "ripper.c"
    break;

  case 359:
#line 3288 "ripper.y"
                    {
			restore_defun(p, (yyvsp[-3].node)->nd_defn);
#if 0
			(yyval.val) = set_defun_body(p, (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[-1].val), &(yyloc));
#endif
			(yyvsp[-3].val) = get_value((yyvsp[-3].val));

			{VALUE v1,v2,v3,v4,v5,v6;v1=AREF((yyvsp[-3].val), 0);v2=AREF((yyvsp[-3].val), 1);v3=AREF((yyvsp[-3].val), 2);v4=(yyvsp[-2].val);v5=(yyvsp[-1].val);v6=dispatch5(defs,v1,v2,v3,v4,v5);(yyval.val)=v6;}
			local_pop(p);
		    }
#line 9487 "ripper.c"
    break;

  case 360:
#line 3299 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_BREAK(0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=dispatch0(args_new);v2=v1;v3=dispatch1(break,v2);(yyval.val)=v3;}
		    }
#line 9498 "ripper.c"
    break;

  case 361:
#line 3306 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_NEXT(0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=dispatch0(args_new);v2=v1;v3=dispatch1(next,v2);(yyval.val)=v3;}
		    }
#line 9509 "ripper.c"
    break;

  case 362:
#line 3313 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_REDO(&(yyloc));
#endif
			{VALUE v1;v1=dispatch0(redo);(yyval.val)=v1;}
		    }
#line 9520 "ripper.c"
    break;

  case 363:
#line 3320 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_RETRY(&(yyloc));
#endif
			{VALUE v1;v1=dispatch0(retry);(yyval.val)=v1;}
		    }
#line 9531 "ripper.c"
    break;

  case 364:
#line 3329 "ripper.y"
                    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 9540 "ripper.c"
    break;

  case 365:
#line 3336 "ripper.y"
                    {
			token_info_push(p, "begin", &(yyloc));
		    }
#line 9548 "ripper.c"
    break;

  case 366:
#line 3342 "ripper.y"
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
#line 9567 "ripper.c"
    break;

  case 367:
#line 3359 "ripper.y"
                    {
			token_info_push(p, "unless", &(yyloc));
		    }
#line 9575 "ripper.c"
    break;

  case 368:
#line 3365 "ripper.y"
                    {
			token_info_push(p, "while", &(yyloc));
		    }
#line 9583 "ripper.c"
    break;

  case 369:
#line 3371 "ripper.y"
                    {
			token_info_push(p, "until", &(yyloc));
		    }
#line 9591 "ripper.c"
    break;

  case 370:
#line 3377 "ripper.y"
                    {
			token_info_push(p, "case", &(yyloc));
		    }
#line 9599 "ripper.c"
    break;

  case 371:
#line 3383 "ripper.y"
                    {
			token_info_push(p, "for", &(yyloc));
		    }
#line 9607 "ripper.c"
    break;

  case 372:
#line 3389 "ripper.y"
                    {
			token_info_push(p, "class", &(yyloc));
			(yyval.ctxt) = p->ctxt;
		    }
#line 9616 "ripper.c"
    break;

  case 373:
#line 3396 "ripper.y"
                    {
			token_info_push(p, "module", &(yyloc));
			(yyval.ctxt) = p->ctxt;
		    }
#line 9625 "ripper.c"
    break;

  case 374:
#line 3403 "ripper.y"
                    {
			token_info_push(p, "def", &(yyloc));
			p->ctxt.in_argdef = 1;
		    }
#line 9634 "ripper.c"
    break;

  case 375:
#line 3410 "ripper.y"
                    {
			token_info_push(p, "do", &(yyloc));
		    }
#line 9642 "ripper.c"
    break;

  case 376:
#line 3416 "ripper.y"
                    {
			token_info_push(p, "do", &(yyloc));
		    }
#line 9650 "ripper.c"
    break;

  case 377:
#line 3422 "ripper.y"
                    {
			token_info_warn(p, "rescue", p->token_info, 1, &(yyloc));
		    }
#line 9658 "ripper.c"
    break;

  case 378:
#line 3428 "ripper.y"
                    {
			token_info_warn(p, "ensure", p->token_info, 1, &(yyloc));
		    }
#line 9666 "ripper.c"
    break;

  case 379:
#line 3434 "ripper.y"
                    {
			token_info_warn(p, "when", p->token_info, 0, &(yyloc));
		    }
#line 9674 "ripper.c"
    break;

  case 380:
#line 3440 "ripper.y"
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
#line 9691 "ripper.c"
    break;

  case 381:
#line 3455 "ripper.y"
                    {
			WARN_EOL("elsif");
			token_info_warn(p, "elsif", p->token_info, 1, &(yyloc));
		    }
#line 9700 "ripper.c"
    break;

  case 382:
#line 3462 "ripper.y"
                    {
			token_info_pop(p, "end", &(yyloc));
		    }
#line 9708 "ripper.c"
    break;

  case 383:
#line 3468 "ripper.y"
                    {
			if (p->ctxt.in_class && !p->ctxt.in_def && !dyna_in_block(p))
			    yyerror1(&(yylsp[0]), "Invalid return in class/module body");
		    }
#line 9717 "ripper.c"
    break;

  case 390:
#line 3487 "ripper.y"
                    {
#if 0
			(yyval.val) = new_if(p, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-3].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-3].val);v2=(yyvsp[-1].val);v3=escape_Qundef((yyvsp[0].val));v4=dispatch3(elsif,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 9729 "ripper.c"
    break;

  case 392:
#line 3498 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(else,v1);(yyval.val)=v2;}
		    }
#line 9740 "ripper.c"
    break;

  case 395:
#line 3511 "ripper.y"
                    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
			mark_lvar_used(p, (yyval.val));
#endif
			(yyval.val)=assignable(p, (yyvsp[0].val));
		    }
#line 9752 "ripper.c"
    break;

  case 396:
#line 3519 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(mlhs_paren,v1);(yyval.val)=v2;}
		    }
#line 9763 "ripper.c"
    break;

  case 397:
#line 3528 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(mlhs_add,v2,v3);(yyval.val)=v4;}
		    }
#line 9774 "ripper.c"
    break;

  case 398:
#line 3535 "ripper.y"
                    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(mlhs_add,v1,v2);(yyval.val)=v3;}
		    }
#line 9785 "ripper.c"
    break;

  case 399:
#line 3544 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 9796 "ripper.c"
    break;

  case 400:
#line 3551 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(mlhs_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 9807 "ripper.c"
    break;

  case 401:
#line 3558 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-4].val), NEW_POSTARG((yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=dispatch2(mlhs_add_star,v1,v2);v4=v3;v5=(yyvsp[0].val);v6=dispatch2(mlhs_add_post,v4,v5);(yyval.val)=v6;}
		    }
#line 9818 "ripper.c"
    break;

  case 402:
#line 3565 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN(0, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(mlhs_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 9829 "ripper.c"
    break;

  case 403:
#line 3572 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_MASGN(0, NEW_POSTARG((yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[-2].val);v4=dispatch2(mlhs_add_star,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(mlhs_add_post,v5,v6);(yyval.val)=v7;}
		    }
#line 9840 "ripper.c"
    break;

  case 404:
#line 3581 "ripper.y"
                    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
			mark_lvar_used(p, (yyval.val));
#endif
			(yyval.val)=assignable(p, (yyvsp[0].val));
		    }
#line 9852 "ripper.c"
    break;

  case 405:
#line 3589 "ripper.y"
                    {
#if 0
			(yyval.val) = NODE_SPECIAL_NO_NAME_REST;
#endif
			(yyval.val)=Qnil;
		    }
#line 9863 "ripper.c"
    break;

  case 407:
#line 3598 "ripper.y"
                             {(yyval.val) = ID2VAL(idNil);}
#line 9869 "ripper.c"
    break;

  case 408:
#line 3601 "ripper.y"
                  {p->ctxt.in_argdef = 0;}
#line 9875 "ripper.c"
    break;

  case 410:
#line 3604 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 9883 "ripper.c"
    break;

  case 411:
#line 3608 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 9891 "ripper.c"
    break;

  case 412:
#line 3612 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 9899 "ripper.c"
    break;

  case 413:
#line 3616 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].val), &(yylsp[0]));
		    }
#line 9907 "ripper.c"
    break;

  case 414:
#line 3622 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 9915 "ripper.c"
    break;

  case 415:
#line 3626 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 9923 "ripper.c"
    break;

  case 416:
#line 3632 "ripper.y"
                    {
			/* magic number for rest_id in iseq_set_arguments() */
#if 0
			(yyval.val) = NODE_SPECIAL_EXCESSIVE_COMMA;
#endif
			{VALUE v1;v1=dispatch0(excessed_comma);(yyval.val)=v1;}
		    }
#line 9935 "ripper.c"
    break;

  case 417:
#line 3642 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9943 "ripper.c"
    break;

  case 418:
#line 3646 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-7].val), (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9951 "ripper.c"
    break;

  case 419:
#line 3650 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-3].val), (yyvsp[-1].val), Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9959 "ripper.c"
    break;

  case 420:
#line 3654 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), (yyvsp[-3].val), Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9967 "ripper.c"
    break;

  case 421:
#line 3658 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9975 "ripper.c"
    break;

  case 422:
#line 3662 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
			(yyval.val) = new_args(p, (yyvsp[-1].val), Qnone, (yyvsp[0].val), Qnone, (yyval.val), &(yyloc));
		    }
#line 9984 "ripper.c"
    break;

  case 423:
#line 3667 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), Qnone, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9992 "ripper.c"
    break;

  case 424:
#line 3671 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-1].val), Qnone, Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 10000 "ripper.c"
    break;

  case 425:
#line 3675 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-3].val), (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 10008 "ripper.c"
    break;

  case 426:
#line 3679 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 10016 "ripper.c"
    break;

  case 427:
#line 3683 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-1].val), Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 10024 "ripper.c"
    break;

  case 428:
#line 3687 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 10032 "ripper.c"
    break;

  case 429:
#line 3691 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, Qnone, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 10040 "ripper.c"
    break;

  case 430:
#line 3695 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, Qnone, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 10048 "ripper.c"
    break;

  case 431:
#line 3699 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 10056 "ripper.c"
    break;

  case 433:
#line 3706 "ripper.y"
                    {
			p->command_start = TRUE;
		    }
#line 10064 "ripper.c"
    break;

  case 434:
#line 3712 "ripper.y"
                    {
			p->cur_arg = 0;
			p->max_numparam = ORDINAL_PARAM;
			p->ctxt.in_argdef = 0;
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8,v9,v10,v11;v1=Qnil;v2=Qnil;v3=Qnil;v4=Qnil;v5=Qnil;v6=Qnil;v7=Qnil;v8=dispatch7(params,v1,v2,v3,v4,v5,v6,v7);v9=v8;v10=escape_Qundef((yyvsp[-1].val));v11=dispatch2(block_var,v9,v10);(yyval.val)=v11;}
		    }
#line 10078 "ripper.c"
    break;

  case 435:
#line 3722 "ripper.y"
                    {
			p->cur_arg = 0;
			p->max_numparam = ORDINAL_PARAM;
			p->ctxt.in_argdef = 0;
#if 0
			(yyval.val) = (yyvsp[-2].val);
#endif
			{VALUE v1,v2,v3;v1=escape_Qundef((yyvsp[-2].val));v2=escape_Qundef((yyvsp[-1].val));v3=dispatch2(block_var,v1,v2);(yyval.val)=v3;}
		    }
#line 10092 "ripper.c"
    break;

  case 436:
#line 3735 "ripper.y"
                    {
			(yyval.val) = 0;
		    }
#line 10100 "ripper.c"
    break;

  case 437:
#line 3739 "ripper.y"
                    {
#if 0
			(yyval.val) = 0;
#endif
			(yyval.val)=(yyvsp[-1].val);
		    }
#line 10111 "ripper.c"
    break;

  case 438:
#line 3748 "ripper.y"
                        {(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));}
#line 10117 "ripper.c"
    break;

  case 439:
#line 3750 "ripper.y"
                        {(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));}
#line 10123 "ripper.c"
    break;

  case 440:
#line 3754 "ripper.y"
                    {
			new_bv(p, get_id((yyvsp[0].val)));
			(yyval.val)=get_value((yyvsp[0].val));
		    }
#line 10132 "ripper.c"
    break;

  case 441:
#line 3759 "ripper.y"
                    {
			(yyval.val) = 0;
		    }
#line 10140 "ripper.c"
    break;

  case 442:
#line 3765 "ripper.y"
                    {
			token_info_push(p, "->", &(yylsp[0]));
			(yyvsp[0].vars) = dyna_push(p);
			(yyval.num) = p->lex.lpar_beg;
			p->lex.lpar_beg = p->lex.paren_nest;
		    }
#line 10151 "ripper.c"
    break;

  case 443:
#line 3771 "ripper.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 10160 "ripper.c"
    break;

  case 444:
#line 3775 "ripper.y"
                    {
			(yyval.node) = numparam_push(p);
		    }
#line 10168 "ripper.c"
    break;

  case 445:
#line 3779 "ripper.y"
                    {
			CMDARG_PUSH(0);
		    }
#line 10176 "ripper.c"
    break;

  case 446:
#line 3783 "ripper.y"
                    {
			int max_numparam = p->max_numparam;
			p->lex.lpar_beg = (yyvsp[-5].num);
			p->max_numparam = (yyvsp[-4].num);
			CMDARG_POP();
			(yyvsp[-2].val) = args_with_numbered(p, (yyvsp[-2].val), max_numparam);
#if 0
                        {
                            YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
                            (yyval.val) = NEW_LAMBDA((yyvsp[-2].val), (yyvsp[0].val), &loc);
                            nd_set_line((yyval.val)->nd_body, (yylsp[0]).end_pos.lineno);
                            nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
			    nd_set_first_loc((yyval.val), (yylsp[-6]).beg_pos);
                        }
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(lambda,v1,v2);(yyval.val)=v3;}
			numparam_pop(p, (yyvsp[-3].node));
			dyna_pop(p, (yyvsp[-6].vars));
		    }
#line 10200 "ripper.c"
    break;

  case 447:
#line 3805 "ripper.y"
                    {
			p->ctxt.in_argdef = 0;
#if 0
			(yyval.val) = (yyvsp[-2].val);
			p->max_numparam = ORDINAL_PARAM;
#endif
			{VALUE v1,v2;v1=(yyvsp[-2].val);v2=dispatch1(paren,v1);(yyval.val)=v2;}
		    }
#line 10213 "ripper.c"
    break;

  case 448:
#line 3814 "ripper.y"
                    {
			p->ctxt.in_argdef = 0;
#if 0
			if (!args_info_empty_p((yyvsp[0].val)->nd_ainfo))
			    p->max_numparam = ORDINAL_PARAM;
#endif
			(yyval.val) = (yyvsp[0].val);
		    }
#line 10226 "ripper.c"
    break;

  case 449:
#line 3825 "ripper.y"
                    {
			token_info_pop(p, "}", &(yylsp[0]));
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 10235 "ripper.c"
    break;

  case 450:
#line 3830 "ripper.y"
                    {
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 10243 "ripper.c"
    break;

  case 451:
#line 3836 "ripper.y"
                    {
			(yyval.val) = (yyvsp[-1].val);
#if 0
			(yyval.val)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
#endif
		    }
#line 10255 "ripper.c"
    break;

  case 452:
#line 3846 "ripper.y"
                    {
#if 0
			if (nd_type_p((yyvsp[-1].val), NODE_YIELD)) {
			    compile_error(p, "block given to yield");
			}
			else {
			    block_dup_check(p, (yyvsp[-1].val)->nd_args, (yyvsp[0].val));
			}
			(yyval.val) = method_add_block(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-1].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(method_add_block,v1,v2);(yyval.val)=v3;}
		    }
#line 10273 "ripper.c"
    break;

  case 453:
#line 3860 "ripper.y"
                    {
#if 0
			(yyval.val) = new_qcall(p, (yyvsp[-2].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=v6==Qundef ? v5 : dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 10284 "ripper.c"
    break;

  case 454:
#line 3867 "ripper.y"
                    {
#if 0
			(yyval.val) = new_command_qcall(p, (yyvsp[-3].val), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-2]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=(yyvsp[-1].val);v5=dispatch4(command_call,v1,v2,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=v7==Qundef ? v6 : dispatch2(method_add_block,v6,v7);(yyval.val)=v8;}
		    }
#line 10295 "ripper.c"
    break;

  case 455:
#line 3874 "ripper.y"
                    {
#if 0
			(yyval.val) = new_command_qcall(p, (yyvsp[-3].val), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-2]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=(yyvsp[-1].val);v5=dispatch4(command_call,v1,v2,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=dispatch2(method_add_block,v6,v7);(yyval.val)=v8;}
		    }
#line 10306 "ripper.c"
    break;

  case 456:
#line 3883 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
			(yyval.val)->nd_args = (yyvsp[0].val);
			nd_set_last_loc((yyvsp[-1].val), (yylsp[0]).end_pos);
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-1].val);v2=dispatch1(fcall,v1);v3=v2;v4=(yyvsp[0].val);v5=dispatch2(method_add_arg,v3,v4);(yyval.val)=v5;}
		    }
#line 10319 "ripper.c"
    break;

  case 457:
#line 3892 "ripper.y"
                    {
#if 0
			(yyval.val) = new_qcall(p, (yyvsp[-2].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-1]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=v6==Qundef ? v5 : dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 10331 "ripper.c"
    break;

  case 458:
#line 3900 "ripper.y"
                    {
#if 0
			(yyval.val) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-1]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-3].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-1].val);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 10343 "ripper.c"
    break;

  case 459:
#line 3908 "ripper.y"
                    {
#if 0
			(yyval.val) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].val), (yyvsp[0].val), Qnull, &(yylsp[0]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[0].val);v4=dispatch3(call,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 10354 "ripper.c"
    break;

  case 460:
#line 3915 "ripper.y"
                    {
#if 0
			(yyval.val) = new_qcall(p, (yyvsp[-1].val), (yyvsp[-2].val), ID2VAL(idCall), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-1]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=ID2VAL(idCall);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 10366 "ripper.c"
    break;

  case 461:
#line 3923 "ripper.y"
                    {
#if 0
			(yyval.val) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].val), ID2VAL(idCall), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-1]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-2].val);v2=ID2VAL(idCOLON2);v3=ID2VAL(idCall);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 10378 "ripper.c"
    break;

  case 462:
#line 3931 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_SUPER((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(super,v1);(yyval.val)=v2;}
		    }
#line 10389 "ripper.c"
    break;

  case 463:
#line 3938 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_ZSUPER(&(yyloc));
#endif
			{VALUE v1;v1=dispatch0(zsuper);(yyval.val)=v1;}
		    }
#line 10400 "ripper.c"
    break;

  case 464:
#line 3945 "ripper.y"
                    {
#if 0
			if ((yyvsp[-3].val) && nd_type_p((yyvsp[-3].val), NODE_SELF))
			    (yyval.val) = NEW_FCALL(tAREF, (yyvsp[-1].val), &(yyloc));
			else
			    (yyval.val) = NEW_CALL((yyvsp[-3].val), tAREF, (yyvsp[-1].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-3].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=escape_Qundef((yyvsp[-1].val));v3=dispatch2(aref,v1,v2);(yyval.val)=v3;}
		    }
#line 10415 "ripper.c"
    break;

  case 465:
#line 3958 "ripper.y"
                    {
			(yyval.val) = (yyvsp[-1].val);
#if 0
			(yyval.val)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
#endif
		    }
#line 10427 "ripper.c"
    break;

  case 466:
#line 3966 "ripper.y"
                    {
			(yyval.val) = (yyvsp[-1].val);
#if 0
			(yyval.val)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
#endif
		    }
#line 10439 "ripper.c"
    break;

  case 467:
#line 3975 "ripper.y"
                  {(yyval.vars) = dyna_push(p);}
#line 10445 "ripper.c"
    break;

  case 468:
#line 3976 "ripper.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 10454 "ripper.c"
    break;

  case 469:
#line 3980 "ripper.y"
                    {
			(yyval.node) = numparam_push(p);
		    }
#line 10462 "ripper.c"
    break;

  case 470:
#line 3984 "ripper.y"
                    {
			int max_numparam = p->max_numparam;
			p->max_numparam = (yyvsp[-3].num);
			(yyvsp[-1].val) = args_with_numbered(p, (yyvsp[-1].val), max_numparam);
#if 0
			(yyval.val) = NEW_ITER((yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=escape_Qundef((yyvsp[-1].val));v2=(yyvsp[0].val);v3=dispatch2(brace_block,v1,v2);(yyval.val)=v3;}
			numparam_pop(p, (yyvsp[-2].node));
			dyna_pop(p, (yyvsp[-4].vars));
		    }
#line 10478 "ripper.c"
    break;

  case 471:
#line 3997 "ripper.y"
                  {(yyval.vars) = dyna_push(p);}
#line 10484 "ripper.c"
    break;

  case 472:
#line 3998 "ripper.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 10493 "ripper.c"
    break;

  case 473:
#line 4002 "ripper.y"
                    {
			(yyval.node) = numparam_push(p);
			CMDARG_PUSH(0);
		    }
#line 10502 "ripper.c"
    break;

  case 474:
#line 4007 "ripper.y"
                    {
			int max_numparam = p->max_numparam;
			p->max_numparam = (yyvsp[-3].num);
			(yyvsp[-1].val) = args_with_numbered(p, (yyvsp[-1].val), max_numparam);
#if 0
			(yyval.val) = NEW_ITER((yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=escape_Qundef((yyvsp[-1].val));v2=(yyvsp[0].val);v3=dispatch2(do_block,v1,v2);(yyval.val)=v3;}
			CMDARG_POP();
			numparam_pop(p, (yyvsp[-2].node));
			dyna_pop(p, (yyvsp[-4].vars));
		    }
#line 10519 "ripper.c"
    break;

  case 475:
#line 4022 "ripper.y"
                    {
#if 0
			check_literal_when(p, (yyvsp[0].val), &(yylsp[0]));
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add,v2,v3);(yyval.val)=v4;}
		    }
#line 10531 "ripper.c"
    break;

  case 476:
#line 4030 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_SPLAT((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 10542 "ripper.c"
    break;

  case 477:
#line 4037 "ripper.y"
                    {
#if 0
			check_literal_when(p, (yyvsp[0].val), &(yylsp[0]));
			(yyval.val) = last_arg_append(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(args_add,v1,v2);(yyval.val)=v3;}
		    }
#line 10554 "ripper.c"
    break;

  case 478:
#line 4045 "ripper.y"
                    {
#if 0
			(yyval.val) = rest_arg_append(p, (yyvsp[-3].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[0].val);v3=dispatch2(args_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 10565 "ripper.c"
    break;

  case 479:
#line 4056 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_WHEN((yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-3].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-3].val);v2=(yyvsp[-1].val);v3=escape_Qundef((yyvsp[0].val));v4=dispatch3(when,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 10577 "ripper.c"
    break;

  case 482:
#line 4070 "ripper.y"
                    {
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 10589 "ripper.c"
    break;

  case 483:
#line 4077 "ripper.y"
                    {
			(yyval.tbl) = push_pktbl(p);
		    }
#line 10597 "ripper.c"
    break;

  case 484:
#line 4081 "ripper.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			pop_pvtbl(p, (yyvsp[-3].tbl));
			p->ctxt.in_kwarg = (yyvsp[-4].ctxt).in_kwarg;
		    }
#line 10607 "ripper.c"
    break;

  case 485:
#line 4088 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_IN((yyvsp[-4].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-4].val);v2=(yyvsp[-1].val);v3=escape_Qundef((yyvsp[0].val));v4=dispatch3(in,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 10618 "ripper.c"
    break;

  case 489:
#line 4102 "ripper.y"
                    {
#if 0
			(yyval.val) = new_if(p, (yyvsp[0].val), (yyvsp[-2].val), 0, &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(if_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 10630 "ripper.c"
    break;

  case 490:
#line 4110 "ripper.y"
                    {
#if 0
			(yyval.val) = new_unless(p, (yyvsp[0].val), (yyvsp[-2].val), 0, &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(unless_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 10642 "ripper.c"
    break;

  case 492:
#line 4121 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, Qnone, 1, 0, Qnone, &(yyloc));
			(yyval.val) = new_array_pattern(p, Qnone, get_value((yyvsp[-1].val)), (yyval.val), &(yyloc));
		    }
#line 10651 "ripper.c"
    break;

  case 493:
#line 4126 "ripper.y"
                    {
			(yyval.val) = new_array_pattern(p, Qnone, get_value((yyvsp[-2].val)), (yyvsp[0].val), &(yyloc));
#if 0
			nd_set_first_loc((yyval.val), (yylsp[-2]).beg_pos);
#endif

		    }
#line 10663 "ripper.c"
    break;

  case 494:
#line 4134 "ripper.y"
                    {
			(yyval.val) = new_find_pattern(p, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 10671 "ripper.c"
    break;

  case 495:
#line 4138 "ripper.y"
                    {
			(yyval.val) = new_array_pattern(p, Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 10679 "ripper.c"
    break;

  case 496:
#line 4142 "ripper.y"
                    {
			(yyval.val) = new_hash_pattern(p, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 10687 "ripper.c"
    break;

  case 498:
#line 4151 "ripper.y"
                    {
#if 0
			NODE *n = NEW_LIST((yyvsp[-2].val), &(yyloc));
			n = list_append(p, n, (yyvsp[0].val));
			(yyval.val) = new_hash(p, n, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=STATIC_ID2SYM(id_assoc);v3=(yyvsp[0].val);v4=dispatch3(binary,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 10700 "ripper.c"
    break;

  case 500:
#line 4163 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_NODE(NODE_OR, (yyvsp[-2].val), (yyvsp[0].val), 0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=STATIC_ID2SYM(idOr);v3=(yyvsp[0].val);v4=dispatch3(binary,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 10711 "ripper.c"
    break;

  case 502:
#line 4172 "ripper.y"
                      {(yyval.tbl) = push_pktbl(p);}
#line 10717 "ripper.c"
    break;

  case 503:
#line 4173 "ripper.y"
                      {(yyval.tbl) = push_pktbl(p);}
#line 10723 "ripper.c"
    break;

  case 506:
#line 4178 "ripper.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.val) = new_array_pattern(p, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), &(yyloc));
#if 0
			nd_set_first_loc((yyval.val), (yylsp[-3]).beg_pos);
#endif

		    }
#line 10736 "ripper.c"
    break;

  case 507:
#line 4187 "ripper.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.val) = new_find_pattern(p, (yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
#if 0
			nd_set_first_loc((yyval.val), (yylsp[-3]).beg_pos);
#endif

		    }
#line 10749 "ripper.c"
    break;

  case 508:
#line 4196 "ripper.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.val) = new_hash_pattern(p, (yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
#if 0
			nd_set_first_loc((yyval.val), (yylsp[-3]).beg_pos);
#endif

		    }
#line 10762 "ripper.c"
    break;

  case 509:
#line 4205 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.val) = new_array_pattern(p, (yyvsp[-2].val), Qnone, (yyval.val), &(yyloc));
		    }
#line 10771 "ripper.c"
    break;

  case 510:
#line 4210 "ripper.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.val) = new_array_pattern(p, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), &(yyloc));
#if 0
			nd_set_first_loc((yyval.val), (yylsp[-3]).beg_pos);
#endif

		    }
#line 10784 "ripper.c"
    break;

  case 511:
#line 4219 "ripper.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.val) = new_find_pattern(p, (yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
#if 0
			nd_set_first_loc((yyval.val), (yylsp[-3]).beg_pos);
#endif

		    }
#line 10797 "ripper.c"
    break;

  case 512:
#line 4228 "ripper.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.val) = new_hash_pattern(p, (yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
#if 0
			nd_set_first_loc((yyval.val), (yylsp[-3]).beg_pos);
#endif

		    }
#line 10810 "ripper.c"
    break;

  case 513:
#line 4237 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.val) = new_array_pattern(p, (yyvsp[-2].val), Qnone, (yyval.val), &(yyloc));
		    }
#line 10819 "ripper.c"
    break;

  case 514:
#line 4242 "ripper.y"
                    {
			(yyval.val) = new_array_pattern(p, Qnone, Qnone, (yyvsp[-1].val), &(yyloc));
		    }
#line 10827 "ripper.c"
    break;

  case 515:
#line 4246 "ripper.y"
                    {
			(yyval.val) = new_find_pattern(p, Qnone, (yyvsp[-1].val), &(yyloc));
		    }
#line 10835 "ripper.c"
    break;

  case 516:
#line 4250 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.val) = new_array_pattern(p, Qnone, Qnone, (yyval.val), &(yyloc));
		    }
#line 10844 "ripper.c"
    break;

  case 517:
#line 4255 "ripper.y"
                    {
			(yyval.tbl) = push_pktbl(p);
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 0;
		    }
#line 10854 "ripper.c"
    break;

  case 518:
#line 4261 "ripper.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
			(yyval.val) = new_hash_pattern(p, Qnone, (yyvsp[-1].val), &(yyloc));
		    }
#line 10864 "ripper.c"
    break;

  case 519:
#line 4267 "ripper.y"
                    {
			(yyval.val) = new_hash_pattern_tail(p, Qnone, 0, &(yyloc));
			(yyval.val) = new_hash_pattern(p, Qnone, (yyval.val), &(yyloc));
		    }
#line 10873 "ripper.c"
    break;

  case 520:
#line 4271 "ripper.y"
                          {(yyval.tbl) = push_pktbl(p);}
#line 10879 "ripper.c"
    break;

  case 521:
#line 4272 "ripper.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 10888 "ripper.c"
    break;

  case 522:
#line 4279 "ripper.y"
                    {
#if 0
			NODE *pre_args = NEW_LIST((yyvsp[0].val), &(yyloc));
			(yyval.val) = new_array_pattern_tail(p, pre_args, 0, 0, Qnone, &(yyloc));
#endif
			(yyval.val) = new_array_pattern_tail(p, rb_ary_new_from_args(1, get_value((yyvsp[0].val))), 0, 0, Qnone, &(yyloc));

		    }
#line 10901 "ripper.c"
    break;

  case 523:
#line 4288 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, (yyvsp[0].val), 1, 0, Qnone, &(yyloc));
		    }
#line 10909 "ripper.c"
    break;

  case 524:
#line 4292 "ripper.y"
                    {
#if 0
			(yyval.val) = new_array_pattern_tail(p, list_concat((yyvsp[-1].val), (yyvsp[0].val)), 0, 0, Qnone, &(yyloc));
#endif
			VALUE pre_args = rb_ary_concat((yyvsp[-1].val), get_value((yyvsp[0].val)));
			(yyval.val) = new_array_pattern_tail(p, pre_args, 0, 0, Qnone, &(yyloc));

		    }
#line 10922 "ripper.c"
    break;

  case 525:
#line 4301 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, (yyvsp[-2].val), 1, (yyvsp[0].val), Qnone, &(yyloc));
		    }
#line 10930 "ripper.c"
    break;

  case 526:
#line 4305 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, (yyvsp[-4].val), 1, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
		    }
#line 10938 "ripper.c"
    break;

  case 527:
#line 4309 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, (yyvsp[-1].val), 1, 0, Qnone, &(yyloc));
		    }
#line 10946 "ripper.c"
    break;

  case 528:
#line 4313 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, (yyvsp[-3].val), 1, 0, (yyvsp[0].val), &(yyloc));
		    }
#line 10954 "ripper.c"
    break;

  case 530:
#line 4320 "ripper.y"
                    {
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 10962 "ripper.c"
    break;

  case 531:
#line 4324 "ripper.y"
                    {
#if 0
			(yyval.val) = list_concat((yyvsp[-2].val), (yyvsp[-1].val));
#endif
			(yyval.val)=rb_ary_concat((yyvsp[-2].val), get_value((yyvsp[-1].val)));
		    }
#line 10973 "ripper.c"
    break;

  case 532:
#line 4333 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, Qnone, 1, (yyvsp[0].val), Qnone, &(yyloc));
		    }
#line 10981 "ripper.c"
    break;

  case 533:
#line 4337 "ripper.y"
                    {
			(yyval.val) = new_array_pattern_tail(p, Qnone, 1, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
		    }
#line 10989 "ripper.c"
    break;

  case 534:
#line 4343 "ripper.y"
                    {
			(yyval.val) = new_find_pattern_tail(p, (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));

			if (rb_warning_category_enabled_p(RB_WARN_CATEGORY_EXPERIMENTAL))
			    rb_warn0L_experimental(nd_line((yyval.val)), "Find pattern is experimental, and the behavior may change in future versions of Ruby!");
		    }
#line 11000 "ripper.c"
    break;

  case 535:
#line 4353 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 11008 "ripper.c"
    break;

  case 536:
#line 4357 "ripper.y"
                    {
			(yyval.val) = 0;
		    }
#line 11016 "ripper.c"
    break;

  case 538:
#line 4364 "ripper.y"
                    {
#if 0
			(yyval.val) = list_concat((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_concat((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 11027 "ripper.c"
    break;

  case 539:
#line 4373 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			(yyval.val)=rb_ary_new_from_args(1, get_value((yyvsp[0].val)));
		    }
#line 11038 "ripper.c"
    break;

  case 540:
#line 4382 "ripper.y"
                    {
			(yyval.val) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[-2].val), &(yyloc)), (yyvsp[0].val), &(yyloc));
		    }
#line 11046 "ripper.c"
    break;

  case 541:
#line 4386 "ripper.y"
                    {
			(yyval.val) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[0].val), &(yyloc)), 0, &(yyloc));
		    }
#line 11054 "ripper.c"
    break;

  case 542:
#line 4390 "ripper.y"
                    {
			(yyval.val) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[-1].val), &(yyloc)), 0, &(yyloc));
		    }
#line 11062 "ripper.c"
    break;

  case 543:
#line 4394 "ripper.y"
                    {
			(yyval.val) =  new_hash_pattern_tail(p, new_hash(p, Qnone, &(yyloc)), (yyvsp[0].val), &(yyloc));
		    }
#line 11070 "ripper.c"
    break;

  case 544:
#line 4400 "ripper.y"
                        {(yyval.val)=rb_ary_new_from_args(1, (yyvsp[0].val));}
#line 11076 "ripper.c"
    break;

  case 545:
#line 4402 "ripper.y"
                    {
#if 0
			(yyval.val) = list_concat((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), (yyvsp[0].val));
		    }
#line 11087 "ripper.c"
    break;

  case 546:
#line 4411 "ripper.y"
                    {
			error_duplicate_pattern_key(p, get_id((yyvsp[-1].val)), &(yylsp[-1]));
#if 0
			(yyval.val) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[-1].val)), &(yyloc)), &(yyloc)), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_new_from_args(2, get_value((yyvsp[-1].val)), get_value((yyvsp[0].val)));
		    }
#line 11099 "ripper.c"
    break;

  case 547:
#line 4419 "ripper.y"
                    {
			error_duplicate_pattern_key(p, get_id((yyvsp[0].val)), &(yylsp[0]));
			if ((yyvsp[0].val) && !is_local_id(get_id((yyvsp[0].val)))) {
			    yyerror1(&(yylsp[0]), "key must be valid as local variables");
			}
			error_duplicate_pattern_variable(p, get_id((yyvsp[0].val)), &(yylsp[0]));
#if 0
			(yyval.val) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[0].val)), &(yyloc)), &(yyloc)), assignable(p, (yyvsp[0].val), 0, &(yyloc)));
#endif
			(yyval.val)=rb_ary_new_from_args(2, get_value((yyvsp[0].val)), Qnil);
		    }
#line 11115 "ripper.c"
    break;

  case 549:
#line 4434 "ripper.y"
                    {
			YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
#if 0
			if (!(yyvsp[-1].val) || nd_type_p((yyvsp[-1].val), NODE_STR)) {
			    NODE *node = dsym_node(p, (yyvsp[-1].val), &loc);
			    (yyval.val) = SYM2ID(node->nd_lit);
			}
#endif
			if (ripper_is_node_yylval((yyvsp[-1].val)) && RNODE((yyvsp[-1].val))->nd_cval) {
			    VALUE label = RNODE((yyvsp[-1].val))->nd_cval;
			    VALUE rval = RNODE((yyvsp[-1].val))->nd_rval;
			    (yyval.val) = ripper_new_yylval(p, rb_intern_str(label), rval, label);
			    RNODE((yyval.val))->nd_loc = loc;
			}

			else {
			    yyerror1(&loc, "symbol literal with interpolation is not allowed");
			    (yyval.val) = 0;
			}
		    }
#line 11140 "ripper.c"
    break;

  case 550:
#line 4457 "ripper.y"
                    {
		        (yyval.val) = (yyvsp[0].val);
		    }
#line 11148 "ripper.c"
    break;

  case 551:
#line 4461 "ripper.y"
                    {
		        (yyval.val) = 0;
		    }
#line 11156 "ripper.c"
    break;

  case 552:
#line 4467 "ripper.y"
                    {
		        (yyval.val) = 0;
		    }
#line 11164 "ripper.c"
    break;

  case 554:
#line 4473 "ripper.y"
                             {(yyval.val) = ID2VAL(idNil);}
#line 11170 "ripper.c"
    break;

  case 556:
#line 4478 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-2].val));
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(dot2,v1,v2);(yyval.val)=v3;}
		    }
#line 11183 "ripper.c"
    break;

  case 557:
#line 4487 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-2].val));
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT3((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(dot3,v1,v2);(yyval.val)=v3;}
		    }
#line 11196 "ripper.c"
    break;

  case 558:
#line 4496 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-1].val));
			(yyval.val) = NEW_DOT2((yyvsp[-1].val), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=Qnil;v3=dispatch2(dot2,v1,v2);(yyval.val)=v3;}
		    }
#line 11208 "ripper.c"
    break;

  case 559:
#line 4504 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[-1].val));
			(yyval.val) = NEW_DOT3((yyvsp[-1].val), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=Qnil;v3=dispatch2(dot3,v1,v2);(yyval.val)=v3;}
		    }
#line 11220 "ripper.c"
    break;

  case 563:
#line 4515 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT2(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=Qnil;v2=(yyvsp[0].val);v3=dispatch2(dot2,v1,v2);(yyval.val)=v3;}
		    }
#line 11232 "ripper.c"
    break;

  case 564:
#line 4523 "ripper.y"
                    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT3(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=Qnil;v2=(yyvsp[0].val);v3=dispatch2(dot3,v1,v2);(yyval.val)=v3;}
		    }
#line 11244 "ripper.c"
    break;

  case 573:
#line 4541 "ripper.y"
                    {
#if 0
			if (!((yyval.val) = gettable(p, (yyvsp[0].val), &(yyloc)))) (yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 11255 "ripper.c"
    break;

  case 575:
#line 4551 "ripper.y"
                    {
#if 0
			error_duplicate_pattern_variable(p, (yyvsp[0].val), &(yylsp[0]));
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 11267 "ripper.c"
    break;

  case 576:
#line 4561 "ripper.y"
                    {
#if 0
			NODE *n = gettable(p, (yyvsp[0].val), &(yyloc));
			if (!(nd_type_p(n, NODE_LVAR) || nd_type_p(n, NODE_DVAR))) {
			    compile_error(p, "%"PRIsVALUE": no such local variable", rb_id2str((yyvsp[0].val)));
			}
			(yyval.val) = n;
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 11282 "ripper.c"
    break;

  case 577:
#line 4572 "ripper.y"
                    {
#if 0
			if (!((yyval.val) = gettable(p, (yyvsp[0].val), &(yyloc)))) (yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
                    }
#line 11293 "ripper.c"
    break;

  case 578:
#line 4581 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_BEGIN((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(begin,v1);(yyval.val)=v2;}
		    }
#line 11304 "ripper.c"
    break;

  case 579:
#line 4590 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_COLON3((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(top_const_ref,v1);(yyval.val)=v2;}
		    }
#line 11315 "ripper.c"
    break;

  case 580:
#line 4597 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_COLON2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_ref,v1,v2);(yyval.val)=v3;}
		    }
#line 11326 "ripper.c"
    break;

  case 581:
#line 4604 "ripper.y"
                   {
#if 0
			(yyval.val) = gettable(p, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		   }
#line 11337 "ripper.c"
    break;

  case 582:
#line 4615 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_RESBODY((yyvsp[-4].val),
					 (yyvsp[-3].val) ? block_append(p, node_assign(p, (yyvsp[-3].val), NEW_ERRINFO(&(yylsp[-3])), NO_LEX_CTXT, &(yylsp[-3])), (yyvsp[-1].val)) : (yyvsp[-1].val),
					 (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-4].val)?(yyvsp[-4].val):(yyvsp[-1].val));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=escape_Qundef((yyvsp[-4].val));v2=escape_Qundef((yyvsp[-3].val));v3=escape_Qundef((yyvsp[-1].val));v4=escape_Qundef((yyvsp[0].val));v5=dispatch4(rescue,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 11351 "ripper.c"
    break;

  case 584:
#line 4628 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 11362 "ripper.c"
    break;

  case 585:
#line 4635 "ripper.y"
                    {
#if 0
			if (!((yyval.val) = splat_array((yyvsp[0].val)))) (yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 11373 "ripper.c"
    break;

  case 587:
#line 4645 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 11381 "ripper.c"
    break;

  case 589:
#line 4652 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(ensure,v1);(yyval.val)=v2;}
		    }
#line 11392 "ripper.c"
    break;

  case 593:
#line 4666 "ripper.y"
                    {
#if 0
			NODE *node = (yyvsp[0].val);
			if (!node) {
			    node = NEW_STR(STR_NEW0(), &(yyloc));
                            RB_OBJ_WRITTEN(p->ast, Qnil, node->nd_lit);
			}
			else {
			    node = evstr2dstr(p, node);
			}
			(yyval.val) = node;
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 11411 "ripper.c"
    break;

  case 596:
#line 4685 "ripper.y"
                    {
#if 0
			(yyval.val) = literal_concat(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(string_concat,v1,v2);(yyval.val)=v3;}
		    }
#line 11422 "ripper.c"
    break;

  case 597:
#line 4694 "ripper.y"
                    {
#if 0
			(yyval.val) = heredoc_dedent(p, (yyvsp[-1].val));
			if ((yyval.val)) nd_set_loc((yyval.val), &(yyloc));
#endif
			{VALUE v1,v2;v1=heredoc_dedent(p, (yyvsp[-1].val));v2=dispatch1(string_literal,v1);(yyval.val)=v2;}
		    }
#line 11434 "ripper.c"
    break;

  case 598:
#line 4704 "ripper.y"
                    {
#if 0
			(yyval.val) = new_xstring(p, heredoc_dedent(p, (yyvsp[-1].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=heredoc_dedent(p, (yyvsp[-1].val));v2=dispatch1(xstring_literal,v1);(yyval.val)=v2;}
		    }
#line 11445 "ripper.c"
    break;

  case 599:
#line 4713 "ripper.y"
                    {
			(yyval.val) = new_regexp(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 11453 "ripper.c"
    break;

  case 600:
#line 4719 "ripper.y"
                    {
#if 0
			(yyval.val) = make_list((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 11464 "ripper.c"
    break;

  case 601:
#line 4728 "ripper.y"
                    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(words_new);(yyval.val)=v1;}
		    }
#line 11475 "ripper.c"
    break;

  case 602:
#line 4735 "ripper.y"
                    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), evstr2dstr(p, (yyvsp[-1].val)));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(words_add,v1,v2);(yyval.val)=v3;}
		    }
#line 11486 "ripper.c"
    break;

  case 603:
#line 4744 "ripper.y"
                        {{VALUE v1,v2,v3,v4;v1=dispatch0(word_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(word_add,v2,v3);(yyval.val)=v4;}}
#line 11492 "ripper.c"
    break;

  case 604:
#line 4746 "ripper.y"
                    {
#if 0
			(yyval.val) = literal_concat(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(word_add,v1,v2);(yyval.val)=v3;}
		    }
#line 11503 "ripper.c"
    break;

  case 605:
#line 4755 "ripper.y"
                    {
#if 0
			(yyval.val) = make_list((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 11514 "ripper.c"
    break;

  case 606:
#line 4764 "ripper.y"
                    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(symbols_new);(yyval.val)=v1;}
		    }
#line 11525 "ripper.c"
    break;

  case 607:
#line 4771 "ripper.y"
                    {
#if 0
			(yyval.val) = symbol_append(p, (yyvsp[-2].val), evstr2dstr(p, (yyvsp[-1].val)));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(symbols_add,v1,v2);(yyval.val)=v3;}
		    }
#line 11536 "ripper.c"
    break;

  case 608:
#line 4780 "ripper.y"
                    {
#if 0
			(yyval.val) = make_list((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 11547 "ripper.c"
    break;

  case 609:
#line 4789 "ripper.y"
                    {
#if 0
			(yyval.val) = make_list((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 11558 "ripper.c"
    break;

  case 610:
#line 4798 "ripper.y"
                    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(qwords_new);(yyval.val)=v1;}
		    }
#line 11569 "ripper.c"
    break;

  case 611:
#line 4805 "ripper.y"
                    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), (yyvsp[-1].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(qwords_add,v1,v2);(yyval.val)=v3;}
		    }
#line 11580 "ripper.c"
    break;

  case 612:
#line 4814 "ripper.y"
                    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(qsymbols_new);(yyval.val)=v1;}
		    }
#line 11591 "ripper.c"
    break;

  case 613:
#line 4821 "ripper.y"
                    {
#if 0
			(yyval.val) = symbol_append(p, (yyvsp[-2].val), (yyvsp[-1].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(qsymbols_add,v1,v2);(yyval.val)=v3;}
		    }
#line 11602 "ripper.c"
    break;

  case 614:
#line 4830 "ripper.y"
                    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(string_content);(yyval.val)=v1;}
#if 0
#endif
			(yyval.val) = ripper_new_yylval(p, 0, (yyval.val), 0);

		    }
#line 11617 "ripper.c"
    break;

  case 615:
#line 4841 "ripper.y"
                    {
#if 0
			(yyval.val) = literal_concat(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(string_add,v1,v2);(yyval.val)=v3;}
#if 0
#endif
			if (ripper_is_node_yylval((yyvsp[-1].val)) && ripper_is_node_yylval((yyvsp[0].val)) &&
			    !RNODE((yyvsp[-1].val))->nd_cval) {
			    RNODE((yyvsp[-1].val))->nd_cval = RNODE((yyvsp[0].val))->nd_cval;
			    RNODE((yyvsp[-1].val))->nd_rval = add_mark_object(p, (yyval.val));
			    (yyval.val) = (yyvsp[-1].val);
			}

		    }
#line 11637 "ripper.c"
    break;

  case 616:
#line 4859 "ripper.y"
                    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(xstring_new);(yyval.val)=v1;}
		    }
#line 11648 "ripper.c"
    break;

  case 617:
#line 4866 "ripper.y"
                    {
#if 0
			(yyval.val) = literal_concat(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(xstring_add,v1,v2);(yyval.val)=v3;}
		    }
#line 11659 "ripper.c"
    break;

  case 618:
#line 4875 "ripper.y"
                    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(regexp_new);(yyval.val)=v1;}
#if 0
#endif
			(yyval.val) = ripper_new_yylval(p, 0, (yyval.val), 0);

		    }
#line 11674 "ripper.c"
    break;

  case 619:
#line 4886 "ripper.y"
                    {
#if 0
			NODE *head = (yyvsp[-1].val), *tail = (yyvsp[0].val);
			if (!head) {
			    (yyval.val) = tail;
			}
			else if (!tail) {
			    (yyval.val) = head;
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
			    (yyval.val) = list_append(p, head, tail);
			}
#endif
			VALUE s1 = 1, s2 = 0, n1 = (yyvsp[-1].val), n2 = (yyvsp[0].val);
			if (ripper_is_node_yylval(n1)) {
			    s1 = RNODE(n1)->nd_cval;
			    n1 = RNODE(n1)->nd_rval;
			}
			if (ripper_is_node_yylval(n2)) {
			    s2 = RNODE(n2)->nd_cval;
			    n2 = RNODE(n2)->nd_rval;
			}
			(yyval.val) = dispatch2(regexp_add, n1, n2);
			if (!s1 && s2) {
			    (yyval.val) = ripper_new_yylval(p, 0, (yyval.val), s2);
			}

		    }
#line 11717 "ripper.c"
    break;

  case 620:
#line 4927 "ripper.y"
                        {(yyval.val)=ripper_new_yylval(p, 0, get_value((yyvsp[0].val)), (yyvsp[0].val));}
#line 11723 "ripper.c"
    break;

  case 621:
#line 4929 "ripper.y"
                    {
			/* need to backup p->lex.strterm so that a string literal `%&foo,#$&,bar&` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 11734 "ripper.c"
    break;

  case 622:
#line 4936 "ripper.y"
                    {
			p->lex.strterm = (yyvsp[-1].strterm);
#if 0
			(yyval.val) = NEW_EVSTR((yyvsp[0].val), &(yyloc));
			nd_set_line((yyval.val), (yylsp[0]).end_pos.lineno);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(string_dvar,v1);(yyval.val)=v2;}
		    }
#line 11747 "ripper.c"
    break;

  case 623:
#line 4945 "ripper.y"
                    {
			CMDARG_PUSH(0);
			COND_PUSH(0);
		    }
#line 11756 "ripper.c"
    break;

  case 624:
#line 4949 "ripper.y"
                    {
			/* need to backup p->lex.strterm so that a string literal `%!foo,#{ !0 },bar!` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
		    }
#line 11766 "ripper.c"
    break;

  case 625:
#line 4954 "ripper.y"
                    {
			(yyval.num) = p->lex.state;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 11775 "ripper.c"
    break;

  case 626:
#line 4958 "ripper.y"
                    {
			(yyval.num) = p->lex.brace_nest;
			p->lex.brace_nest = 0;
		    }
#line 11784 "ripper.c"
    break;

  case 627:
#line 4962 "ripper.y"
                    {
			(yyval.num) = p->heredoc_indent;
			p->heredoc_indent = 0;
		    }
#line 11793 "ripper.c"
    break;

  case 628:
#line 4967 "ripper.y"
                    {
			COND_POP();
			CMDARG_POP();
			p->lex.strterm = (yyvsp[-5].strterm);
			SET_LEX_STATE((yyvsp[-4].num));
			p->lex.brace_nest = (yyvsp[-3].num);
			p->heredoc_indent = (yyvsp[-2].num);
			p->heredoc_line_indent = -1;
#if 0
			if ((yyvsp[-1].val)) (yyvsp[-1].val)->flags &= ~NODE_FL_NEWLINE;
			(yyval.val) = new_evstr(p, (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(string_embexpr,v1);(yyval.val)=v2;}
		    }
#line 11812 "ripper.c"
    break;

  case 629:
#line 4984 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_GVAR((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 11823 "ripper.c"
    break;

  case 630:
#line 4991 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_IVAR((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 11834 "ripper.c"
    break;

  case 631:
#line 4998 "ripper.y"
                    {
#if 0
			(yyval.val) = NEW_CVAR((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 11845 "ripper.c"
    break;

  case 635:
#line 5012 "ripper.y"
                    {
			SET_LEX_STATE(EXPR_END);
#if 0
			(yyval.val) = NEW_LIT(ID2SYM((yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[0].val);v2=dispatch1(symbol,v1);v3=v2;v4=dispatch1(symbol_literal,v3);(yyval.val)=v4;}
		    }
#line 11857 "ripper.c"
    break;

  case 640:
#line 5028 "ripper.y"
                    {
			SET_LEX_STATE(EXPR_END);
#if 0
			(yyval.val) = dsym_node(p, (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(dyna_symbol,v1);(yyval.val)=v2;}
		    }
#line 11869 "ripper.c"
    break;

  case 642:
#line 5039 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[0].val);
			RB_OBJ_WRITE(p->ast, &(yyval.val)->nd_lit, negate_lit(p, (yyval.val)->nd_lit));
#endif
			{VALUE v1,v2,v3;v1=ID2VAL(idUMinus);v2=(yyvsp[0].val);v3=dispatch2(unary,v1,v2);(yyval.val)=v3;}
		    }
#line 11881 "ripper.c"
    break;

  case 655:
#line 5066 "ripper.y"
                              {(yyval.val) = KWD2EID(nil, (yyvsp[0].val));}
#line 11887 "ripper.c"
    break;

  case 656:
#line 5067 "ripper.y"
                               {(yyval.val) = KWD2EID(self, (yyvsp[0].val));}
#line 11893 "ripper.c"
    break;

  case 657:
#line 5068 "ripper.y"
                               {(yyval.val) = KWD2EID(true, (yyvsp[0].val));}
#line 11899 "ripper.c"
    break;

  case 658:
#line 5069 "ripper.y"
                                {(yyval.val) = KWD2EID(false, (yyvsp[0].val));}
#line 11905 "ripper.c"
    break;

  case 659:
#line 5070 "ripper.y"
                                  {(yyval.val) = KWD2EID(_FILE__, (yyvsp[0].val));}
#line 11911 "ripper.c"
    break;

  case 660:
#line 5071 "ripper.y"
                                  {(yyval.val) = KWD2EID(_LINE__, (yyvsp[0].val));}
#line 11917 "ripper.c"
    break;

  case 661:
#line 5072 "ripper.y"
                                      {(yyval.val) = KWD2EID(_ENCODING__, (yyvsp[0].val));}
#line 11923 "ripper.c"
    break;

  case 662:
#line 5076 "ripper.y"
                    {
#if 0
			if (!((yyval.val) = gettable(p, (yyvsp[0].val), &(yyloc)))) (yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			if (id_is_var(p, get_id((yyvsp[0].val)))) {
			    (yyval.val) = dispatch1(var_ref, (yyvsp[0].val));
			}
			else {
			    (yyval.val) = dispatch1(vcall, (yyvsp[0].val));
			}

		    }
#line 11940 "ripper.c"
    break;

  case 663:
#line 5089 "ripper.y"
                    {
#if 0
			if (!((yyval.val) = gettable(p, (yyvsp[0].val), &(yyloc)))) (yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 11951 "ripper.c"
    break;

  case 664:
#line 5098 "ripper.y"
                    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 11962 "ripper.c"
    break;

  case 665:
#line 5105 "ripper.y"
                    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 11973 "ripper.c"
    break;

  case 668:
#line 5118 "ripper.y"
                    {
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 11982 "ripper.c"
    break;

  case 669:
#line 5123 "ripper.y"
                    {
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 11990 "ripper.c"
    break;

  case 670:
#line 5127 "ripper.y"
                    {
#if 0
			(yyval.val) = 0;
#endif
			(yyval.val)=Qnil;
		    }
#line 12001 "ripper.c"
    break;

  case 672:
#line 5137 "ripper.y"
                    {
			p->ctxt.in_argdef = 0;
			(yyval.val) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[-1]));
			(yyval.val) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyval.val), &(yylsp[-1]));
		    }
#line 12011 "ripper.c"
    break;

  case 673:
#line 5145 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(paren,v1);(yyval.val)=v2;}
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
			p->ctxt.in_argdef = 0;
		    }
#line 12025 "ripper.c"
    break;

  case 675:
#line 5157 "ripper.y"
                    {
			(yyval.ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			p->ctxt.in_argdef = 1;
			SET_LEX_STATE(p->lex.state|EXPR_LABEL); /* force for args */
		    }
#line 12036 "ripper.c"
    break;

  case 676:
#line 5164 "ripper.y"
                    {
			p->ctxt.in_kwarg = (yyvsp[-2].ctxt).in_kwarg;
			p->ctxt.in_argdef = 0;
			(yyval.val) = (yyvsp[-1].val);
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 12048 "ripper.c"
    break;

  case 677:
#line 5174 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 12056 "ripper.c"
    break;

  case 678:
#line 5178 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 12064 "ripper.c"
    break;

  case 679:
#line 5182 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 12072 "ripper.c"
    break;

  case 680:
#line 5186 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].val), &(yylsp[0]));
		    }
#line 12080 "ripper.c"
    break;

  case 681:
#line 5190 "ripper.y"
                    {
			add_forwarding_args(p);
			(yyval.val) = new_args_tail(p, Qnone, (yyvsp[0].val), ID2VAL(idFWD_BLOCK), &(yylsp[0]));
		    }
#line 12089 "ripper.c"
    break;

  case 682:
#line 5197 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 12097 "ripper.c"
    break;

  case 683:
#line 5201 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 12105 "ripper.c"
    break;

  case 684:
#line 5207 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 12113 "ripper.c"
    break;

  case 685:
#line 5211 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-7].val), (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 12121 "ripper.c"
    break;

  case 686:
#line 5215 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-3].val), (yyvsp[-1].val), Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 12129 "ripper.c"
    break;

  case 687:
#line 5219 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), (yyvsp[-3].val), Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 12137 "ripper.c"
    break;

  case 688:
#line 5223 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 12145 "ripper.c"
    break;

  case 689:
#line 5227 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), Qnone, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 12153 "ripper.c"
    break;

  case 690:
#line 5231 "ripper.y"
                    {
			(yyval.val) = new_args(p, (yyvsp[-1].val), Qnone, Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 12161 "ripper.c"
    break;

  case 691:
#line 5235 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-3].val), (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 12169 "ripper.c"
    break;

  case 692:
#line 5239 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 12177 "ripper.c"
    break;

  case 693:
#line 5243 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-1].val), Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 12185 "ripper.c"
    break;

  case 694:
#line 5247 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 12193 "ripper.c"
    break;

  case 695:
#line 5251 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, Qnone, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 12201 "ripper.c"
    break;

  case 696:
#line 5255 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, Qnone, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 12209 "ripper.c"
    break;

  case 697:
#line 5259 "ripper.y"
                    {
			(yyval.val) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 12217 "ripper.c"
    break;

  case 698:
#line 5263 "ripper.y"
                    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
			(yyval.val) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyval.val), &(yylsp[0]));
		    }
#line 12226 "ripper.c"
    break;

  case 699:
#line 5270 "ripper.y"
                    {
#if 0
			(yyval.val) = idFWD_KWREST;
#endif
			{VALUE v1;v1=dispatch0(args_forward);(yyval.val)=v1;}
		    }
#line 12237 "ripper.c"
    break;

  case 700:
#line 5279 "ripper.y"
                    {
			static const char mesg[] = "formal argument cannot be a constant";
#if 0
			yyerror1(&(yylsp[0]), mesg);
			(yyval.val) = 0;
#endif
			{VALUE v1,v2,v3;v1=ERR_MESG();v2=(yyvsp[0].val);v3=dispatch2(param_error,v1,v2);(yyval.val)=v3;}ripper_error(p);
		    }
#line 12250 "ripper.c"
    break;

  case 701:
#line 5288 "ripper.y"
                    {
			static const char mesg[] = "formal argument cannot be an instance variable";
#if 0
			yyerror1(&(yylsp[0]), mesg);
			(yyval.val) = 0;
#endif
			{VALUE v1,v2,v3;v1=ERR_MESG();v2=(yyvsp[0].val);v3=dispatch2(param_error,v1,v2);(yyval.val)=v3;}ripper_error(p);
		    }
#line 12263 "ripper.c"
    break;

  case 702:
#line 5297 "ripper.y"
                    {
			static const char mesg[] = "formal argument cannot be a global variable";
#if 0
			yyerror1(&(yylsp[0]), mesg);
			(yyval.val) = 0;
#endif
			{VALUE v1,v2,v3;v1=ERR_MESG();v2=(yyvsp[0].val);v3=dispatch2(param_error,v1,v2);(yyval.val)=v3;}ripper_error(p);
		    }
#line 12276 "ripper.c"
    break;

  case 703:
#line 5306 "ripper.y"
                    {
			static const char mesg[] = "formal argument cannot be a class variable";
#if 0
			yyerror1(&(yylsp[0]), mesg);
			(yyval.val) = 0;
#endif
			{VALUE v1,v2,v3;v1=ERR_MESG();v2=(yyvsp[0].val);v3=dispatch2(param_error,v1,v2);(yyval.val)=v3;}ripper_error(p);
		    }
#line 12289 "ripper.c"
    break;

  case 705:
#line 5318 "ripper.y"
                    {
			formal_argument(p, (yyvsp[0].val));
			p->max_numparam = ORDINAL_PARAM;
			(yyval.val) = (yyvsp[0].val);
		    }
#line 12299 "ripper.c"
    break;

  case 706:
#line 5326 "ripper.y"
                    {
			ID id = get_id((yyvsp[0].val));
			arg_var(p, id);
			p->cur_arg = id;
			(yyval.val) = (yyvsp[0].val);
		    }
#line 12310 "ripper.c"
    break;

  case 707:
#line 5335 "ripper.y"
                    {
			p->cur_arg = 0;
#if 0
			(yyval.val) = NEW_ARGS_AUX((yyvsp[0].val), 1, &NULL_LOC);
#endif
			(yyval.val)=get_value((yyvsp[0].val));
		    }
#line 12322 "ripper.c"
    break;

  case 708:
#line 5343 "ripper.y"
                    {
#if 0
			ID tid = internal_id(p);
			YYLTYPE loc;
			loc.beg_pos = (yylsp[-1]).beg_pos;
			loc.end_pos = (yylsp[-1]).beg_pos;
			arg_var(p, tid);
			if (dyna_in_block(p)) {
			    (yyvsp[-1].val)->nd_value = NEW_DVAR(tid, &loc);
			}
			else {
			    (yyvsp[-1].val)->nd_value = NEW_LVAR(tid, &loc);
			}
			(yyval.val) = NEW_ARGS_AUX(tid, 1, &NULL_LOC);
			(yyval.val)->nd_next = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(mlhs_paren,v1);(yyval.val)=v2;}
		    }
#line 12345 "ripper.c"
    break;

  case 709:
#line 5364 "ripper.y"
                        {(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));}
#line 12351 "ripper.c"
    break;

  case 710:
#line 5366 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-2].val);
			(yyval.val)->nd_plen++;
			(yyval.val)->nd_next = block_append(p, (yyval.val)->nd_next, (yyvsp[0].val)->nd_next);
			rb_discard_node(p, (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 12365 "ripper.c"
    break;

  case 711:
#line 5379 "ripper.y"
                    {
			arg_var(p, formal_argument(p, (yyvsp[0].val)));
			p->cur_arg = get_id((yyvsp[0].val));
			p->max_numparam = ORDINAL_PARAM;
			p->ctxt.in_argdef = 0;
			(yyval.val) = (yyvsp[0].val);
		    }
#line 12377 "ripper.c"
    break;

  case 712:
#line 5389 "ripper.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
#if 0
			(yyval.val) = new_kw_arg(p, assignable(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[-1].val))), get_value((yyvsp[0].val)));
		    }
#line 12390 "ripper.c"
    break;

  case 713:
#line 5398 "ripper.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
#if 0
			(yyval.val) = new_kw_arg(p, assignable(p, (yyvsp[0].val), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[0].val))), 0);
		    }
#line 12403 "ripper.c"
    break;

  case 714:
#line 5409 "ripper.y"
                    {
			p->ctxt.in_argdef = 1;
#if 0
			(yyval.val) = new_kw_arg(p, assignable(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[-1].val))), get_value((yyvsp[0].val)));
		    }
#line 12415 "ripper.c"
    break;

  case 715:
#line 5417 "ripper.y"
                    {
			p->ctxt.in_argdef = 1;
#if 0
			(yyval.val) = new_kw_arg(p, assignable(p, (yyvsp[0].val), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[0].val))), 0);
		    }
#line 12427 "ripper.c"
    break;

  case 716:
#line 5427 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 12438 "ripper.c"
    break;

  case 717:
#line 5434 "ripper.y"
                    {
#if 0
			(yyval.val) = kwd_append((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 12449 "ripper.c"
    break;

  case 718:
#line 5444 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 12460 "ripper.c"
    break;

  case 719:
#line 5451 "ripper.y"
                    {
#if 0
			(yyval.val) = kwd_append((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 12471 "ripper.c"
    break;

  case 722:
#line 5464 "ripper.y"
                    {
#if 0
#endif
			{VALUE v1,v2;v1=Qnil;v2=dispatch1(nokw_param,v1);(yyval.val)=v2;}
		    }
#line 12481 "ripper.c"
    break;

  case 723:
#line 5472 "ripper.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].val))));
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(kwrest_param,v1);(yyval.val)=v2;}
		    }
#line 12493 "ripper.c"
    break;

  case 724:
#line 5480 "ripper.y"
                    {
#if 0
			(yyval.val) = internal_id(p);
			arg_var(p, (yyval.val));
#endif
			{VALUE v1,v2;v1=Qnil;v2=dispatch1(kwrest_param,v1);(yyval.val)=v2;}
		    }
#line 12505 "ripper.c"
    break;

  case 725:
#line 5490 "ripper.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
#if 0
			(yyval.val) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[-2].val))), get_value((yyvsp[0].val)));
		    }
#line 12518 "ripper.c"
    break;

  case 726:
#line 5501 "ripper.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
#if 0
			(yyval.val) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[-2].val))), get_value((yyvsp[0].val)));
		    }
#line 12531 "ripper.c"
    break;

  case 727:
#line 5512 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 12542 "ripper.c"
    break;

  case 728:
#line 5519 "ripper.y"
                    {
#if 0
			(yyval.val) = opt_arg_append((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 12553 "ripper.c"
    break;

  case 729:
#line 5528 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 12564 "ripper.c"
    break;

  case 730:
#line 5535 "ripper.y"
                    {
#if 0
			(yyval.val) = opt_arg_append((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 12575 "ripper.c"
    break;

  case 733:
#line 5548 "ripper.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].val))));
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(rest_param,v1);(yyval.val)=v2;}
		    }
#line 12587 "ripper.c"
    break;

  case 734:
#line 5556 "ripper.y"
                    {
#if 0
			(yyval.val) = internal_id(p);
			arg_var(p, (yyval.val));
#endif
			{VALUE v1,v2;v1=Qnil;v2=dispatch1(rest_param,v1);(yyval.val)=v2;}
		    }
#line 12599 "ripper.c"
    break;

  case 737:
#line 5570 "ripper.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].val))));
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(blockarg,v1);(yyval.val)=v2;}
		    }
#line 12611 "ripper.c"
    break;

  case 738:
#line 5578 "ripper.y"
                    {
#if 0
                        arg_var(p, shadowing_lvar(p, get_id(ANON_BLOCK_ID)));
#endif
                    (yyval.val) = dispatch1(blockarg, Qnil);

                    }
#line 12623 "ripper.c"
    break;

  case 739:
#line 5588 "ripper.y"
                    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 12631 "ripper.c"
    break;

  case 740:
#line 5592 "ripper.y"
                    {
			(yyval.val) = Qnull;
		    }
#line 12639 "ripper.c"
    break;

  case 741:
#line 5598 "ripper.y"
                    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 12648 "ripper.c"
    break;

  case 742:
#line 5602 "ripper.y"
                      {SET_LEX_STATE(EXPR_BEG);}
#line 12654 "ripper.c"
    break;

  case 743:
#line 5603 "ripper.y"
                    {
#if 0
			switch (nd_type((yyvsp[-1].val))) {
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
			    value_expr((yyvsp[-1].val));
			    break;
			}
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(paren,v1);(yyval.val)=v2;}
		    }
#line 12680 "ripper.c"
    break;

  case 745:
#line 5628 "ripper.y"
                    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(assoclist_from_args,v1);(yyval.val)=v2;}
		    }
#line 12691 "ripper.c"
    break;

  case 746:
#line 5637 "ripper.y"
                        {(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));}
#line 12697 "ripper.c"
    break;

  case 747:
#line 5639 "ripper.y"
                    {
#if 0
			NODE *assocs = (yyvsp[-2].val);
			NODE *tail = (yyvsp[0].val);
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
			(yyval.val) = assocs;
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 12722 "ripper.c"
    break;

  case 748:
#line 5662 "ripper.y"
                    {
#if 0
			if (nd_type_p((yyvsp[-2].val), NODE_STR)) {
			    nd_set_type((yyvsp[-2].val), NODE_LIT);
			    RB_OBJ_WRITE(p->ast, &(yyvsp[-2].val)->nd_lit, rb_fstring((yyvsp[-2].val)->nd_lit));
			}
			(yyval.val) = list_append(p, NEW_LIST((yyvsp[-2].val), &(yyloc)), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(assoc_new,v1,v2);(yyval.val)=v3;}
		    }
#line 12737 "ripper.c"
    break;

  case 749:
#line 5673 "ripper.y"
                    {
#if 0
			(yyval.val) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[-1].val)), &(yylsp[-1])), &(yyloc)), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(assoc_new,v1,v2);(yyval.val)=v3;}
		    }
#line 12748 "ripper.c"
    break;

  case 750:
#line 5680 "ripper.y"
                    {
#if 0
			NODE *val = gettable(p, (yyvsp[0].val), &(yyloc));
			if (!val) val = NEW_BEGIN(0, &(yyloc));
			(yyval.val) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[0].val)), &(yylsp[0])), &(yyloc)), val);
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=Qnil;v3=dispatch2(assoc_new,v1,v2);(yyval.val)=v3;}
		    }
#line 12761 "ripper.c"
    break;

  case 751:
#line 5689 "ripper.y"
                    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-3]), &(yylsp[-1]));
			(yyval.val) = list_append(p, NEW_LIST(dsym_node(p, (yyvsp[-2].val), &loc), &loc), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-2].val);v2=dispatch1(dyna_symbol,v1);v3=v2;v4=(yyvsp[0].val);v5=dispatch2(assoc_new,v3,v4);(yyval.val)=v5;}
		    }
#line 12773 "ripper.c"
    break;

  case 752:
#line 5697 "ripper.y"
                    {
#if 0
                        if (nd_type_p((yyvsp[0].val), NODE_HASH) &&
                            !((yyvsp[0].val)->nd_head && (yyvsp[0].val)->nd_head->nd_alen)) {
                            static VALUE empty_hash;
                            if (!empty_hash) {
                                empty_hash = rb_obj_freeze(rb_hash_new());
                                rb_gc_register_mark_object(empty_hash);
                            }
                            (yyval.val) = list_append(p, NEW_LIST(0, &(yyloc)), NEW_LIT(empty_hash, &(yyloc)));
                        }
                        else
                            (yyval.val) = list_append(p, NEW_LIST(0, &(yyloc)), (yyvsp[0].val));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(assoc_splat,v1);(yyval.val)=v2;}
		    }
#line 12794 "ripper.c"
    break;

  case 779:
#line 5765 "ripper.y"
                      {yyerrok;token_flush(p);}
#line 12800 "ripper.c"
    break;

  case 780:
#line 5766 "ripper.y"
                       {token_flush(p);}
#line 12806 "ripper.c"
    break;

  case 782:
#line 5770 "ripper.y"
                            {yyerrok;}
#line 12812 "ripper.c"
    break;

  case 783:
#line 5774 "ripper.y"
                    {
			(yyval.val) = Qnull;
		    }
#line 12820 "ripper.c"
    break;


#line 12824 "ripper.c"

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
#line 5778 "ripper.y"

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

    /* ignore newline, +/- is a sign. */
    rb_define_const(Ripper, "EXPR_BEG", INT2NUM(EXPR_BEG));
    /* newline significant, +/- is an operator. */
    rb_define_const(Ripper, "EXPR_END", INT2NUM(EXPR_END));
    /* ditto, and unbound braces. */
    rb_define_const(Ripper, "EXPR_ENDARG", INT2NUM(EXPR_ENDARG));
    /* ditto, and unbound braces. */
    rb_define_const(Ripper, "EXPR_ENDFN", INT2NUM(EXPR_ENDFN));
    /* newline significant, +/- is an operator. */
    rb_define_const(Ripper, "EXPR_ARG", INT2NUM(EXPR_ARG));
    /* newline significant, +/- is an operator. */
    rb_define_const(Ripper, "EXPR_CMDARG", INT2NUM(EXPR_CMDARG));
    /* newline significant, +/- is an operator. */
    rb_define_const(Ripper, "EXPR_MID", INT2NUM(EXPR_MID));
    /* ignore newline, no reserved words. */
    rb_define_const(Ripper, "EXPR_FNAME", INT2NUM(EXPR_FNAME));
    /* right after `.' or `::', no reserved words. */
    rb_define_const(Ripper, "EXPR_DOT", INT2NUM(EXPR_DOT));
    /* immediate after `class', no here document. */
    rb_define_const(Ripper, "EXPR_CLASS", INT2NUM(EXPR_CLASS));
    /* flag bit, label is allowed. */
    rb_define_const(Ripper, "EXPR_LABEL", INT2NUM(EXPR_LABEL));
    /* flag bit, just after a label. */
    rb_define_const(Ripper, "EXPR_LABELED", INT2NUM(EXPR_LABELED));
    /* symbol literal as FNAME. */
    rb_define_const(Ripper, "EXPR_FITEM", INT2NUM(EXPR_FITEM));
    /* equals to +EXPR_BEG+ */
    rb_define_const(Ripper, "EXPR_VALUE", INT2NUM(EXPR_VALUE));
    /* equals to <tt>(EXPR_BEG | EXPR_MID | EXPR_CLASS)</tt> */
    rb_define_const(Ripper, "EXPR_BEG_ANY", INT2NUM(EXPR_BEG_ANY));
    /* equals to <tt>(EXPR_ARG | EXPR_CMDARG)</tt> */
    rb_define_const(Ripper, "EXPR_ARG_ANY", INT2NUM(EXPR_ARG_ANY));
    /* equals to <tt>(EXPR_END | EXPR_ENDARG | EXPR_ENDFN)</tt> */
    rb_define_const(Ripper, "EXPR_END_ANY", INT2NUM(EXPR_END_ANY));
    /* equals to +0+ */
    rb_define_const(Ripper, "EXPR_NONE", INT2NUM(EXPR_NONE));

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
