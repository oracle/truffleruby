/* A Bison parser, made by GNU Bison 3.0.4.  */

/* Bison implementation for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2015 Free Software Foundation, Inc.

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

/* Identify Bison output.  */
#define YYBISON 1

/* Bison version.  */
#define YYBISON_VERSION "3.0.4"

/* Skeleton name.  */
#define YYSKELETON_NAME "yacc.c"

/* Pure parsers.  */
#define YYPURE 1

/* Push parsers.  */
#define YYPUSH 0

/* Pull parsers.  */
#define YYPULL 1




/* Copy the first part of user declarations.  */
#line 12 "ripper.y" /* yacc.c:339  */


#if !YYPURE
# error needs pure parser
#endif
#define YYDEBUG 1
#define YYERROR_VERBOSE 1
#define YYSTACK_USE_ALLOCA 0
#define YYLTYPE rb_code_location_t
#define YYLTYPE_IS_DECLARED 1

#include "ruby/ruby.h"
#include "ruby/st.h"
#include "ruby/encoding.h"
#include "internal.h"
#include "node.h"
#include "parse.h"
#include "symbol.h"
#include "regenc.h"
#include <stdio.h>
#include <errno.h>
#include <ctype.h>
#include "probes.h"

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
#define YYPRINT(out, tok, val)	parser_token_value_print(p, (tok), &(val))
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
};

#define DVARS_INHERIT ((void*)1)
#define DVARS_TOPSCOPE NULL
#define DVARS_TERMINAL_P(tbl) ((tbl) == DVARS_INHERIT || (tbl) == DVARS_TOPSCOPE)

typedef struct token_info {
    const char *token;
    int linenum;
    int column;
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
    int line_count;
    int ruby_sourceline;	/* current line no. */
    const char *ruby_sourcefile; /* current source file */
    VALUE ruby_sourcefile_string;
    rb_encoding *enc;
    token_info *token_info;
    VALUE compile_option;

    VALUE debug_buffer;
    VALUE debug_output;

    ID cur_arg;

    rb_ast_t *ast;
    int node_id;

    unsigned int command_start:1;
    unsigned int eofp: 1;
    unsigned int ruby__end__seen: 1;
    unsigned int debug: 1;
    unsigned int has_shebang: 1;
    unsigned int in_defined: 1;
    unsigned int in_main: 1;
    unsigned int in_kwarg: 1;
    unsigned int in_def: 1;
    unsigned int in_class: 1;
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
    unsigned int warn_location: 1;

    NODE *eval_tree_begin;
    NODE *eval_tree;
    VALUE error_buffer;
    VALUE debug_lines;
    const struct rb_block *base_block;
#else
    /* Ripper only */

    VALUE delayed;
    int delayed_line;
    int delayed_col;

    VALUE value;
    VALUE result;
    VALUE parsing_thread;
#endif
};

#define new_tmpbuf() \
    (rb_imemo_tmpbuf_t *)add_mark_object(p, rb_imemo_tmpbuf_auto_free_pointer(NULL))

#define intern_cstr(n,l,en) rb_intern3(n,l,en)

#define STR_NEW(ptr,len) rb_enc_str_new((ptr),(len),p->enc)
#define STR_NEW0() rb_enc_str_new(0,0,p->enc)
#define STR_NEW2(ptr) rb_enc_str_new((ptr),strlen(ptr),p->enc)
#define STR_NEW3(ptr,len,e,func) parser_str_new((ptr),(len),(e),(func),p->enc)
#define TOK_INTERN() intern_cstr(tok(p), toklen(p), p->enc)

static int parser_yyerror(struct parser_params*, const YYLTYPE *yylloc, const char*);
#define yyerror0(msg) parser_yyerror(p, NULL, (msg))
#define yyerror1(loc, msg) parser_yyerror(p, (loc), (msg))
#define yyerror(yylloc, p, msg) parser_yyerror(p, yylloc, msg)
#define token_flush(ptr) ((ptr)->lex.ptok = (ptr)->lex.pcur)

#ifdef RIPPER
#define compile_for_eval	(0)
#else
#define compile_for_eval	(p->base_block != 0 && !p->in_main)
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

static inline VALUE
add_mark_object(struct parser_params *p, VALUE obj)
{
    if (!SPECIAL_CONST_P(obj)
#ifdef RIPPER
	&& !RB_TYPE_P(obj, T_NODE) /* Ripper jumbles NODE objects and other objects... */
#endif
    ) {
	rb_ast_add_mark_object(p->ast, obj);
    }
    return obj;
}

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
static NODE *new_if(struct parser_params*,NODE*,NODE*,NODE*,const YYLTYPE*);
static NODE *new_unless(struct parser_params*,NODE*,NODE*,NODE*,const YYLTYPE*);
static NODE *logop(struct parser_params*,ID,NODE*,NODE*,const YYLTYPE*,const YYLTYPE*);

static NODE *newline_node(NODE*);
static void fixpos(NODE*,NODE*);

static int value_expr_gen(struct parser_params*,NODE*);
static void void_expr(struct parser_params*,NODE*);
static NODE *remove_begin(NODE*);
static NODE *remove_begin_all(NODE*);
#define value_expr(node) value_expr_gen(p, (node) = remove_begin(node))
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
static NODE *evstr2dstr(struct parser_params*,NODE*);
static NODE *splat_array(NODE*);

static NODE *call_bin_op(struct parser_params*,NODE*,ID,NODE*,const YYLTYPE*,const YYLTYPE*);
static NODE *call_uni_op(struct parser_params*,NODE*,ID,const YYLTYPE*,const YYLTYPE*);
static NODE *new_qcall(struct parser_params* p, ID atype, NODE *recv, ID mid, NODE *args, const YYLTYPE *op_loc, const YYLTYPE *loc);
static NODE *new_command_qcall(struct parser_params* p, ID atype, NODE *recv, ID mid, NODE *args, NODE *block, const YYLTYPE *op_loc, const YYLTYPE *loc);
static NODE *method_add_block(struct parser_params*p, NODE *m, NODE *b, const YYLTYPE *loc) {b->nd_iter = m; b->nd_loc = *loc; return b;}

static NODE *new_args(struct parser_params*,NODE*,NODE*,ID,NODE*,NODE*,const YYLTYPE*);
static NODE *new_args_tail(struct parser_params*,NODE*,ID,ID,const YYLTYPE*);
static NODE *new_kw_arg(struct parser_params *p, NODE *k, const YYLTYPE *loc);

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
static NODE *node_assign(struct parser_params*,NODE*,NODE*,const YYLTYPE*);

static NODE *new_op_assign(struct parser_params *p, NODE *lhs, ID op, NODE *rhs, const YYLTYPE *loc);
static NODE *new_ary_op_assign(struct parser_params *p, NODE *ary, NODE *args, ID op, NODE *rhs, const YYLTYPE *args_loc, const YYLTYPE *loc);
static NODE *new_attr_op_assign(struct parser_params *p, NODE *lhs, ID atype, ID attr, ID op, NODE *rhs, const YYLTYPE *loc);
static NODE *new_const_op_assign(struct parser_params *p, NODE *lhs, ID op, NODE *rhs, const YYLTYPE *loc);
static NODE *new_bodystmt(struct parser_params *p, NODE *head, NODE *rescue, NODE *rescue_else, NODE *ensure, const YYLTYPE *loc);

static NODE *const_decl(struct parser_params *p, NODE* path, const YYLTYPE *loc);

static NODE *opt_arg_append(NODE*, NODE*);
static NODE *kwd_append(NODE*, NODE*);

static NODE *new_hash(struct parser_params *p, NODE *hash, const YYLTYPE *loc);

static NODE *new_defined(struct parser_params *p, NODE *expr, const YYLTYPE *loc);

static NODE *new_regexp(struct parser_params *, NODE *, int, const YYLTYPE *);

#define make_array(ary, loc) ((ary) ? (nd_set_loc(ary, loc), ary) : NEW_ZARRAY(loc))

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
#define get_id(id) (id)
#define get_value(val) (val)
#else  /* RIPPER */
#define NODE_RIPPER NODE_CDECL

static inline VALUE
ripper_new_yylval(struct parser_params *p, ID a, VALUE b, VALUE c)
{
    add_mark_object(p, b);
    add_mark_object(p, c);
    return (VALUE)NEW_CDECL(a, b, c, &NULL_LOC);
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
static VALUE assign_error(struct parser_params *p, VALUE a);

static VALUE parser_reg_compile(struct parser_params*, VALUE, int, VALUE *);

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
void rb_parser_set_location_from_strterm_heredoc(struct parser_params *p, rb_strterm_heredoc_t *here, YYLTYPE *yylloc);
void rb_parser_set_location_of_none(struct parser_params *p, YYLTYPE *yylloc);
void rb_parser_set_location(struct parser_params *p, YYLTYPE *yylloc);
RUBY_SYMBOL_EXPORT_END

static void parser_token_value_print(struct parser_params *p, enum yytokentype type, const YYSTYPE *valp);
static ID formal_argument(struct parser_params*, ID);
static ID shadowing_lvar(struct parser_params*,ID);
static void new_bv(struct parser_params*,ID);

static void local_push(struct parser_params*,int);
static void local_pop(struct parser_params*);
static void local_var(struct parser_params*, ID);
static void arg_var(struct parser_params*, ID);
static int  local_id(struct parser_params *p, ID id);
static int  local_id_ref(struct parser_params*, ID, ID **);
static ID   internal_id(struct parser_params*);

static const struct vtable *dyna_push(struct parser_params *);
static void dyna_pop(struct parser_params*, const struct vtable *);
static int dyna_in_block(struct parser_params*);
#define dyna_var(p, id) local_var(p, id)
static int dvar_defined(struct parser_params*, ID);
static int dvar_defined_ref(struct parser_params*, ID, ID**);
static int dvar_curr(struct parser_params*,ID);

static int lvar_defined(struct parser_params*, ID);

#ifdef RIPPER
# define METHOD_NOT idNOT
#else
# define METHOD_NOT '!'
#endif

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

struct rb_strterm_heredoc_struct {
    SIGNED_VALUE sourceline; /* lineno of the line that contains `<<"END"` */
    VALUE term;		/* `"END"` of `<<"END"` */
    VALUE lastline;	/* the string of line that contains `<<"END"` */
    union {
	VALUE dummy;
	long lastidx;	/* the column of `<<"END"` */
    } u3;
};

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
	rb_gc_mark(heredoc->term);
	rb_gc_mark(heredoc->lastline);
    }
}
#endif

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

#define new_defined(p,expr,loc) dispatch1(defined, (expr))

static VALUE heredoc_dedent(struct parser_params*,VALUE);

#else
#define ID2VAL(id) ((VALUE)(id))
#define TOKEN2VAL(t) ID2VAL(t)
#define KWD2EID(t, v) keyword_##t
#endif /* RIPPER */

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
static ID id_warn, id_warning, id_gets;
# define WARN_S_L(s,l) STR_NEW(s,l)
# define WARN_S(s) STR_NEW2(s)
# define WARN_I(i) INT2NUM(i)
# define WARN_ID(i) rb_id2str(i)
# define PRIsWARN "s"
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
# define PRIsWARN PRIsVALUE
# define WARN_ARGS(fmt,n) WARN_ARGS_L(p->ruby_sourceline,fmt,n)
# define WARN_ARGS_L(l,fmt,n) p->ruby_sourcefile, (l), (fmt)
# define WARN_CALL rb_compile_warn
# define WARNING_ARGS(fmt,n) WARN_ARGS(fmt,n)
# define WARNING_ARGS_L(l,fmt,n) WARN_ARGS_L(l,fmt,n)
# define WARNING_CALL rb_compile_warning
PRINTF_ARGS(static void parser_compile_error(struct parser_params*, const char *fmt, ...), 2, 3);
# define compile_error parser_compile_error
#endif

static void token_info_push(struct parser_params*, const char *token, const rb_code_location_t *loc);
static void token_info_pop(struct parser_params*, const char *token, const rb_code_location_t *loc);
static void token_info_warn(struct parser_params *p, const char *token, token_info *ptinfo_beg, int same, const rb_code_location_t *loc);

#line 815 "ripper.c" /* yacc.c:339  */

# ifndef YY_NULLPTR
#  if defined __cplusplus && 201103L <= __cplusplus
#   define YY_NULLPTR nullptr
#  else
#   define YY_NULLPTR 0
#  endif
# endif

/* Enabling verbose error messages.  */
#ifdef YYERROR_VERBOSE
# undef YYERROR_VERBOSE
# define YYERROR_VERBOSE 1
#else
# define YYERROR_VERBOSE 0
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
    tSTRING_CONTENT = 318,
    tCHAR = 319,
    tNTH_REF = 320,
    tBACK_REF = 321,
    tREGEXP_END = 322,
    tSP = 323,
    tUPLUS = 130,
    tUMINUS = 131,
    tPOW = 132,
    tCMP = 133,
    tEQ = 138,
    tEQQ = 139,
    tNEQ = 140,
    tGEQ = 137,
    tLEQ = 136,
    tANDOP = 146,
    tOROP = 147,
    tMATCH = 141,
    tNMATCH = 142,
    tDOT2 = 128,
    tDOT3 = 129,
    tAREF = 143,
    tASET = 144,
    tLSHFT = 134,
    tRSHFT = 135,
    tANDDOT = 148,
    tCOLON2 = 145,
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
    tSTRING_DBEG = 345,
    tSTRING_DEND = 346,
    tSTRING_DVAR = 347,
    tSTRING_END = 348,
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
#line 769 "ripper.y" /* yacc.c:355  */

    VALUE val;
    NODE *node;
    ID id;
    int num;
    const struct vtable *vars;
    struct rb_strterm_struct *strterm;

#line 982 "ripper.c" /* yacc.c:355  */
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



/* Copy the second part of user declarations.  */

#line 1012 "ripper.c" /* yacc.c:358  */

#ifdef short
# undef short
#endif

#ifdef YYTYPE_UINT8
typedef YYTYPE_UINT8 yytype_uint8;
#else
typedef unsigned char yytype_uint8;
#endif

#ifdef YYTYPE_INT8
typedef YYTYPE_INT8 yytype_int8;
#else
typedef signed char yytype_int8;
#endif

#ifdef YYTYPE_UINT16
typedef YYTYPE_UINT16 yytype_uint16;
#else
typedef unsigned short int yytype_uint16;
#endif

#ifdef YYTYPE_INT16
typedef YYTYPE_INT16 yytype_int16;
#else
typedef short int yytype_int16;
#endif

#ifndef YYSIZE_T
# ifdef __SIZE_TYPE__
#  define YYSIZE_T __SIZE_TYPE__
# elif defined size_t
#  define YYSIZE_T size_t
# elif ! defined YYSIZE_T
#  include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  define YYSIZE_T size_t
# else
#  define YYSIZE_T unsigned int
# endif
#endif

#define YYSIZE_MAXIMUM ((YYSIZE_T) -1)

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

#ifndef YY_ATTRIBUTE
# if (defined __GNUC__                                               \
      && (2 < __GNUC__ || (__GNUC__ == 2 && 96 <= __GNUC_MINOR__)))  \
     || defined __SUNPRO_C && 0x5110 <= __SUNPRO_C
#  define YY_ATTRIBUTE(Spec) __attribute__(Spec)
# else
#  define YY_ATTRIBUTE(Spec) /* empty */
# endif
#endif

#ifndef YY_ATTRIBUTE_PURE
# define YY_ATTRIBUTE_PURE   YY_ATTRIBUTE ((__pure__))
#endif

#ifndef YY_ATTRIBUTE_UNUSED
# define YY_ATTRIBUTE_UNUSED YY_ATTRIBUTE ((__unused__))
#endif

#if !defined _Noreturn \
     && (!defined __STDC_VERSION__ || __STDC_VERSION__ < 201112)
# if defined _MSC_VER && 1200 <= _MSC_VER
#  define _Noreturn __declspec (noreturn)
# else
#  define _Noreturn YY_ATTRIBUTE ((__noreturn__))
# endif
#endif

/* Suppress unused-variable warnings by "using" E.  */
#if ! defined lint || defined __GNUC__
# define YYUSE(E) ((void) (E))
#else
# define YYUSE(E) /* empty */
#endif

#if defined __GNUC__ && 407 <= __GNUC__ * 100 + __GNUC_MINOR__
/* Suppress an incorrect diagnostic about yylval being uninitialized.  */
# define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN \
    _Pragma ("GCC diagnostic push") \
    _Pragma ("GCC diagnostic ignored \"-Wuninitialized\"")\
    _Pragma ("GCC diagnostic ignored \"-Wmaybe-uninitialized\"")
# define YY_IGNORE_MAYBE_UNINITIALIZED_END \
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
  yytype_int16 yyss_alloc;
  YYSTYPE yyvs_alloc;
  YYLTYPE yyls_alloc;
};

/* The size of the maximum gap between one aligned stack and the next.  */
# define YYSTACK_GAP_MAXIMUM (sizeof (union yyalloc) - 1)

/* The size of an array large to enough to hold all stacks, each with
   N elements.  */
# define YYSTACK_BYTES(N) \
     ((N) * (sizeof (yytype_int16) + sizeof (YYSTYPE) + sizeof (YYLTYPE)) \
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
        YYSIZE_T yynewbytes;                                            \
        YYCOPY (&yyptr->Stack_alloc, Stack, yysize);                    \
        Stack = &yyptr->Stack_alloc;                                    \
        yynewbytes = yystacksize * sizeof (*Stack) + YYSTACK_GAP_MAXIMUM; \
        yyptr += yynewbytes / sizeof (*yyptr);                          \
      }                                                                 \
    while (0)

#endif

#if defined YYCOPY_NEEDED && YYCOPY_NEEDED
/* Copy COUNT objects from SRC to DST.  The source and destination do
   not overlap.  */
# ifndef YYCOPY
#  if defined __GNUC__ && 1 < __GNUC__
#   define YYCOPY(Dst, Src, Count) \
      __builtin_memcpy (Dst, Src, (Count) * sizeof (*(Src)))
#  else
#   define YYCOPY(Dst, Src, Count)              \
      do                                        \
        {                                       \
          YYSIZE_T yyi;                         \
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
#define YYLAST   12570

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  152
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  215
/* YYNRULES -- Number of rules.  */
#define YYNRULES  640
/* YYNSTATES -- Number of states.  */
#define YYNSTATES  1076

/* YYTRANSLATE[YYX] -- Symbol number corresponding to YYX as returned
   by yylex, with out-of-bounds checking.  */
#define YYUNDEFTOK  2
#define YYMAXUTOK   353

#define YYTRANSLATE(YYX)                                                \
  ((unsigned int) (YYX) <= YYMAXUTOK ? yytranslate[YYX] : YYUNDEFTOK)

/* YYTRANSLATE[TOKEN-NUM] -- Symbol number corresponding to TOKEN-NUM
   as returned by yylex, without out-of-bounds checking.  */
static const yytype_uint8 yytranslate[] =
{
       0,     2,     2,     2,     2,     2,     2,     2,     2,    71,
     151,    74,    72,    73,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,   150,   138,     2,     2,     2,   136,   131,     2,
     146,   147,   134,   132,   144,   133,    68,   135,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,   126,   149,
     128,   124,   127,   125,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,   143,    69,   148,   130,     2,   145,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,   141,   129,   142,   139,     2,    88,    89,
      75,    76,    77,    78,    92,    93,    83,    82,    79,    80,
      81,    86,    87,    90,    91,    95,    84,    85,    94,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
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
      65,    66,    67,    70,    96,    97,    98,    99,   100,   101,
     102,   103,   104,   105,   106,   107,   108,   109,   110,   111,
     112,   113,   114,   115,   116,   117,   118,   119,   120,   121,
     122,   123,   137,   140
};

#if YYDEBUG
  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
static const yytype_uint16 yyrline[] =
{
       0,   936,   936,   936,   962,   968,   975,   982,   989,   995,
     996,  1002,  1015,  1013,  1024,  1035,  1041,  1048,  1055,  1062,
    1068,  1073,  1072,  1081,  1081,  1088,  1095,  1105,  1113,  1120,
    1128,  1136,  1148,  1160,  1170,  1184,  1185,  1193,  1201,  1208,
    1211,  1218,  1225,  1233,  1240,  1247,  1255,  1262,  1272,  1277,
    1286,  1289,  1290,  1294,  1298,  1302,  1306,  1309,  1316,  1316,
    1316,  1322,  1323,  1326,  1327,  1336,  1346,  1356,  1365,  1376,
    1383,  1390,  1397,  1404,  1412,  1420,  1427,  1434,  1443,  1444,
    1453,  1454,  1463,  1470,  1477,  1484,  1491,  1498,  1505,  1512,
    1519,  1526,  1535,  1536,  1545,  1552,  1561,  1568,  1577,  1584,
    1591,  1598,  1605,  1612,  1619,  1626,  1633,  1643,  1650,  1657,
    1664,  1671,  1678,  1685,  1692,  1699,  1709,  1716,  1719,  1726,
    1733,  1742,  1743,  1744,  1745,  1750,  1757,  1758,  1761,  1768,
    1771,  1778,  1778,  1788,  1789,  1790,  1791,  1792,  1793,  1794,
    1795,  1796,  1797,  1798,  1799,  1800,  1801,  1802,  1803,  1804,
    1805,  1806,  1807,  1808,  1809,  1810,  1811,  1812,  1813,  1814,
    1815,  1816,  1817,  1820,  1820,  1820,  1821,  1821,  1822,  1822,
    1822,  1823,  1823,  1823,  1823,  1824,  1824,  1824,  1824,  1825,
    1825,  1825,  1826,  1826,  1826,  1826,  1827,  1827,  1827,  1827,
    1828,  1828,  1828,  1828,  1829,  1829,  1829,  1829,  1830,  1830,
    1830,  1830,  1831,  1831,  1834,  1841,  1848,  1856,  1864,  1872,
    1880,  1888,  1895,  1903,  1912,  1921,  1933,  1945,  1949,  1953,
    1957,  1961,  1965,  1969,  1973,  1977,  1981,  1985,  1989,  1993,
    1997,  1998,  2002,  2006,  2010,  2014,  2018,  2022,  2026,  2030,
    2034,  2038,  2042,  2042,  2047,  2056,  2062,  2063,  2064,  2065,
    2068,  2072,  2079,  2086,  2087,  2091,  2098,  2107,  2112,  2123,
    2132,  2133,  2136,  2137,  2138,  2142,  2149,  2158,  2166,  2173,
    2181,  2189,  2193,  2193,  2230,  2239,  2243,  2249,  2256,  2263,
    2270,  2279,  2280,  2283,  2290,  2297,  2306,  2307,  2308,  2309,
    2310,  2311,  2312,  2313,  2314,  2315,  2316,  2324,  2323,  2338,
    2338,  2345,  2345,  2352,  2359,  2366,  2373,  2380,  2388,  2395,
    2402,  2409,  2416,  2416,  2421,  2425,  2429,  2436,  2437,  2446,
    2445,  2456,  2467,  2478,  2488,  2498,  2508,  2515,  2561,  2560,
    2584,  2583,  2606,  2605,  2629,  2634,  2628,  2654,  2655,  2654,
    2679,  2686,  2693,  2700,  2709,  2716,  2722,  2728,  2734,  2740,
    2746,  2752,  2758,  2764,  2770,  2776,  2782,  2788,  2794,  2800,
    2806,  2814,  2820,  2826,  2833,  2834,  2835,  2838,  2839,  2842,
    2843,  2855,  2856,  2865,  2866,  2869,  2876,  2885,  2892,  2901,
    2908,  2915,  2922,  2929,  2936,  2943,  2950,  2957,  2967,  2971,
    2975,  2979,  2985,  2990,  2995,  2999,  3003,  3007,  3011,  3015,
    3024,  3028,  3032,  3036,  3040,  3044,  3048,  3052,  3056,  3062,
    3063,  3069,  3077,  3084,  3095,  3099,  3108,  3110,  3114,  3119,
    3125,  3128,  3133,  3125,  3153,  3160,  3166,  3171,  3177,  3187,
    3201,  3208,  3215,  3224,  3233,  3241,  3249,  3256,  3264,  3272,
    3279,  3286,  3299,  3307,  3317,  3317,  3328,  3329,  3328,  3341,
    3353,  3354,  3357,  3369,  3372,  3379,  3386,  3389,  3393,  3396,
    3403,  3406,  3407,  3414,  3417,  3433,  3434,  3435,  3444,  3454,
    3463,  3469,  3479,  3485,  3494,  3496,  3505,  3515,  3521,  3530,
    3539,  3549,  3555,  3565,  3571,  3581,  3587,  3597,  3603,  3613,
    3623,  3664,  3666,  3665,  3682,  3686,  3691,  3695,  3699,  3681,
    3720,  3727,  3734,  3741,  3744,  3754,  3755,  3756,  3757,  3760,
    3770,  3771,  3781,  3782,  3783,  3784,  3787,  3788,  3789,  3790,
    3791,  3794,  3795,  3796,  3797,  3798,  3799,  3800,  3803,  3816,
    3825,  3832,  3841,  3842,  3846,  3845,  3855,  3863,  3872,  3872,
    3886,  3890,  3894,  3898,  3904,  3909,  3914,  3918,  3922,  3926,
    3930,  3934,  3938,  3942,  3946,  3950,  3954,  3958,  3962,  3966,
    3971,  3977,  3985,  3993,  4001,  4011,  4012,  4019,  4028,  4036,
    4059,  4061,  4074,  4083,  4091,  4101,  4108,  4117,  4124,  4134,
    4141,  4150,  4151,  4154,  4162,  4172,  4182,  4192,  4199,  4208,
    4215,  4224,  4225,  4228,  4236,  4246,  4247,  4250,  4260,  4264,
    4273,  4278,  4278,  4302,  4303,  4312,  4314,  4337,  4348,  4355,
    4363,  4376,  4377,  4378,  4381,  4382,  4383,  4384,  4387,  4388,
    4389,  4392,  4393,  4396,  4397,  4400,  4401,  4404,  4405,  4408,
    4409,  4412,  4415,  4418,  4419,  4420,  4423,  4424,  4427,  4428,
    4432
};
#endif

#if YYDEBUG || YYERROR_VERBOSE || 0
/* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
   First, the terminals, then, starting at YYNTOKENS, nonterminals.  */
static const char *const yytname[] =
{
  "\"end-of-input\"", "error", "$undefined", "\"class\"", "\"module\"",
  "\"def\"", "\"undef\"", "\"begin\"", "\"rescue\"", "\"ensure\"",
  "\"end\"", "\"if\"", "\"unless\"", "\"then\"", "\"elsif\"", "\"else\"",
  "\"case\"", "\"when\"", "\"while\"", "\"until\"", "\"for\"", "\"break\"",
  "\"next\"", "\"redo\"", "\"retry\"", "\"in\"", "\"do\"",
  "\"do (for condition)\"", "\"do (for block)\"", "\"do (for lambda)\"",
  "\"return\"", "\"yield\"", "\"super\"", "\"self\"", "\"nil\"",
  "\"true\"", "\"false\"", "\"and\"", "\"or\"", "\"not\"",
  "\"if (modifier)\"", "\"unless (modifier)\"", "\"while (modifier)\"",
  "\"until (modifier)\"", "\"rescue (modifier)\"", "\"alias\"",
  "\"defined?\"", "\"BEGIN\"", "\"END\"", "\"__LINE__\"", "\"__FILE__\"",
  "\"__ENCODING__\"", "tIDENTIFIER", "tFID", "tGVAR", "tIVAR", "tCONSTANT",
  "tCVAR", "tLABEL", "tINTEGER", "tFLOAT", "tRATIONAL", "tIMAGINARY",
  "tSTRING_CONTENT", "tCHAR", "tNTH_REF", "tBACK_REF", "tREGEXP_END",
  "'.'", "\"backslash\"", "\"escaped space\"",
  "\"escaped horizontal tab\"", "\"escaped form feed\"",
  "\"escaped carriage return\"", "\"escaped vertical tab\"", "\"unary+\"",
  "\"unary-\"", "\"**\"", "\"<=>\"", "\"==\"", "\"===\"", "\"!=\"",
  "\">=\"", "\"<=\"", "\"&&\"", "\"||\"", "\"=~\"", "\"!~\"", "\"..\"",
  "\"...\"", "\"[]\"", "\"[]=\"", "\"<<\"", "\">>\"", "\"&.\"", "\"::\"",
  "\":: at EXPR_BEG\"", "tOP_ASGN", "\"=>\"", "\"(\"", "\"( arg\"",
  "\")\"", "\"[\"", "\"{\"", "\"{ arg\"", "\"*\"", "\"**arg\"", "\"&\"",
  "\"->\"", "tSYMBEG", "tSTRING_BEG", "tXSTRING_BEG", "tREGEXP_BEG",
  "tWORDS_BEG", "tQWORDS_BEG", "tSYMBOLS_BEG", "tQSYMBOLS_BEG",
  "tSTRING_DBEG", "tSTRING_DEND", "tSTRING_DVAR", "tSTRING_END", "tLAMBEG",
  "tLABEL_END", "tLOWEST", "'='", "'?'", "':'", "'>'", "'<'", "'|'", "'^'",
  "'&'", "'+'", "'-'", "'*'", "'/'", "'%'", "tUMINUS_NUM", "'!'", "'~'",
  "tLAST_TOKEN", "'{'", "'}'", "'['", "','", "'`'", "'('", "')'", "']'",
  "';'", "' '", "'\\n'", "$accept", "program", "$@1", "top_compstmt",
  "top_stmts", "top_stmt", "begin_block", "bodystmt", "$@2", "compstmt",
  "stmts", "stmt_or_begin", "$@3", "stmt", "$@4", "command_asgn",
  "command_rhs", "expr", "expr_value", "expr_value_do", "$@5", "$@6",
  "command_call", "block_command", "cmd_brace_block", "fcall", "command",
  "mlhs", "mlhs_inner", "mlhs_basic", "mlhs_item", "mlhs_head",
  "mlhs_post", "mlhs_node", "lhs", "cname", "cpath", "fname", "fsym",
  "fitem", "undef_list", "$@7", "op", "reswords", "arg", "$@8", "relop",
  "rel_expr", "arg_value", "aref_args", "arg_rhs", "paren_args",
  "opt_paren_args", "opt_call_args", "call_args", "command_args", "$@9",
  "block_arg", "opt_block_arg", "args", "mrhs_arg", "mrhs", "primary",
  "$@10", "$@11", "$@12", "$@13", "$@14", "$@15", "@16", "$@17", "@18",
  "@19", "@20", "@21", "primary_value", "k_begin", "k_if", "k_unless",
  "k_while", "k_until", "k_case", "k_for", "k_class", "k_module", "k_def",
  "k_do", "k_do_block", "k_rescue", "k_ensure", "k_when", "k_else",
  "k_elsif", "k_end", "k_return", "then", "do", "if_tail", "opt_else",
  "for_var", "f_marg", "f_marg_list", "f_margs", "block_args_tail",
  "opt_block_args_tail", "block_param", "opt_block_param",
  "block_param_def", "opt_bv_decl", "bv_decls", "bvar", "lambda", "@22",
  "@23", "$@24", "f_larglist", "lambda_body", "do_block", "block_call",
  "method_call", "brace_block", "brace_body", "@25", "do_body", "@26",
  "$@27", "case_body", "cases", "opt_rescue", "exc_list", "exc_var",
  "opt_ensure", "literal", "strings", "string", "string1", "xstring",
  "regexp", "words", "word_list", "word", "symbols", "symbol_list",
  "qwords", "qsymbols", "qword_list", "qsym_list", "string_contents",
  "xstring_contents", "regexp_contents", "string_content", "@28", "$@29",
  "@30", "@31", "@32", "@33", "string_dvar", "symbol", "sym", "dsym",
  "numeric", "simple_numeric", "user_variable", "keyword_variable",
  "var_ref", "var_lhs", "backref", "superclass", "$@34", "f_arglist",
  "@35", "args_tail", "opt_args_tail", "f_args", "f_bad_arg", "f_norm_arg",
  "f_arg_asgn", "f_arg_item", "f_arg", "f_label", "f_kw", "f_block_kw",
  "f_block_kwarg", "f_kwarg", "kwrest_mark", "f_kwrest", "f_opt",
  "f_block_opt", "f_block_optarg", "f_optarg", "restarg_mark",
  "f_rest_arg", "blkarg_mark", "f_block_arg", "opt_f_block_arg",
  "singleton", "$@36", "assoc_list", "assocs", "assoc", "operation",
  "operation2", "operation3", "dot_or_colon", "call_op", "call_op2",
  "opt_terms", "opt_nl", "rparen", "rbracket", "trailer", "term", "terms",
  "none", YY_NULLPTR
};
#endif

# ifdef YYPRINT
/* YYTOKNUM[NUM] -- (External) token number corresponding to the
   (internal) symbol number NUM (which must be that of a token).  */
static const yytype_uint16 yytoknum[] =
{
       0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
     275,   276,   277,   278,   279,   280,   281,   282,   283,   284,
     285,   286,   287,   288,   289,   290,   291,   292,   293,   294,
     295,   296,   297,   298,   299,   300,   301,   302,   303,   304,
     305,   306,   307,   308,   309,   310,   311,   312,   313,   314,
     315,   316,   317,   318,   319,   320,   321,   322,    46,    92,
     323,     9,    12,    13,    11,   130,   131,   132,   133,   138,
     139,   140,   137,   136,   146,   147,   141,   142,   128,   129,
     143,   144,   134,   135,   148,   145,   324,   325,   326,   327,
     328,   329,   330,   331,   332,   333,   334,   335,   336,   337,
     338,   339,   340,   341,   342,   343,   344,   345,   346,   347,
     348,   349,   350,   351,    61,    63,    58,    62,    60,   124,
      94,    38,    43,    45,    42,    47,    37,   352,    33,   126,
     353,   123,   125,    91,    44,    96,    40,    41,    93,    59,
      32,    10
};
# endif

#define YYPACT_NINF -856

#define yypact_value_is_default(Yystate) \
  (!!((Yystate) == (-856)))

#define YYTABLE_NINF -641

#define yytable_value_is_error(Yytable_value) \
  (!!((Yytable_value) == (-641)))

  /* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
     STATE-NUM.  */
static const yytype_int16 yypact[] =
{
    -856,    88,  3334,  -856,  8359,  -856,  -856,  -856,  7952,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  8483,  8483,  -856,  -856,
    -856,  4611,  4176,  -856,  -856,  -856,  -856,   -16,  7809,   -50,
     -24,   -19,  -856,  -856,  -856,  3451,  4321,  -856,  -856,  3596,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  9971,  9971,
      95,  6035,  8607,  8855,  9227,  3047,  -856,  7666,  -856,  -856,
    -856,    78,   107,   111,   115,   976, 10095,  9971,  -856,    16,
    -856,  1161,  -856,   518,  -856,  -856,    63,   213,   198,  -856,
     145, 10343,  -856,   203,  3429,   983,   346,   366,  -856, 10219,
   10219,  -856,  -856,  7003, 10463, 10583, 10703,  7522,  8483,    61,
      71,  -856,  -856,   231,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,    24,   268,  -856,   278,   325,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
     240,  -856,  -856,  -856,  -856,   241,  9971,   342,  6184,  9971,
    9971,  9971,  -856,  9971,  -856,   276,  3429,   310,  -856,  -856,
     277,   422,    30,   153,   329,   233,   286,  -856,  -856,  6879,
    -856,  8483,  8483,  -856,  -856,  7127,  -856, 10219,   695,  -856,
     300,  6333,  -856,  6482,  -856,  -856,   306,   324,    63,  -856,
     507,  -856,   430,   398,  8607,  -856,  6035,   328,    16,  -856,
    1161,   -50,   370,  -856,  1161,   -50,   372,   -28,   196,  -856,
     310,   406,   196,  -856,   -50,   487,   976, 10823,   416,  -856,
     552,   554,   558,   652,  -856,  -856,  -856,  -856,  -856,   447,
    -856,   609,   874,   419,  -856,  -856,  -856,  -856,   473,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  7251, 10219, 10219, 10219,
   10219,  8607, 10219, 10219,  -856,  -856,  -856,   458,  -856,  -856,
    -856,  -856,  -856,  9351,  -856,  6035,  8232,   434,  9351,  9971,
    9971,  9971,  9971,  9971,  -856,  -856,  9971,  9971,  9971,  9971,
    9971,  9971,  9971,  9971,  9971,  -856,  -856,  9971,  9971,  9971,
    9971,  9971,  9971,  9971,  9971,  9971,  9971,  -856,  -856, 11105,
    8483, 11193,  5340,   518,   151,   151,  6631, 10219,  6631,    16,
     557,   436,   536,  -856,  -856,   705,   568,    57,    87,   101,
     795,   812, 10219,   200,  -856,   486,   712,  -856,  -856,  -856,
    -856,    52,   361,   383,   423,   462,   476,   593,   594,   597,
    -856,  -856,  -856,   598,  -856,  -856,  -856, 12425,  -856,  -856,
   10095, 10095,  -856,  -856,   572,  -856,  -856,  -856,  1100,  9971,
    9971,  8731,  -856,  -856, 11281,  8483, 11369,  9971,  9971,  8979,
    -856,   -50,   464,  -856,  -856,   -50,  -856,   471,   474,  -856,
      36,  -856,  -856,  -856,  -856,  -856,  7952,  -856,  9971,   488,
     490, 11281, 11369,  9971,  1161,   -24,   -50,  -856,  -856,  7375,
     484,   -50,  -856,  -856,  9103,  -856,  -856,  9227,  -856,  -856,
    -856,   300,   718,  -856,  -856,   493, 10823, 11457,  8483, 11545,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,   886,    14,   899,    65,  9971,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,   502,    33,  -856,  -856,
     650,  -856,   650,  9971,  -856,   524,   529,  -856,  -856,   -50,
   10823,   532,  -856,  -856,  -856,   639,   561,  2721,  -856,  -856,
    -856,   779,   582,   430,  1571,  1571,  1571,  1571,  1693,  2127,
    1571,  1571,  3719,  3719,  1131,  1131,  4727,  1402,  1402,  1039,
     658,   658,   430,   430,   430,  1278,  1278,  4756,  3741,  5046,
    3886,  -856,   324,  -856,   -50,   417,  -856,   483,  -856,  -856,
    4466,   650,   701,  -856,  5489,   698,  5787,   650,    40,   650,
     557,  -856,  9475,   650,   127, 11633,  8483, 11721,  -856,   518,
    -856,   718,  -856,  -856,  -856, 11809,  8483, 11897,  5340, 10219,
    -856,  -856,  -856,  -856,  -856,  -856,  2366,  -856,  3574,  -856,
    -856,  -856,  7952,  9971,  -856,  9971,   310,  -856,   286,  3172,
    4031,   -50,   584,   651,  -856,  -856,  -856,  -856,  8731,  8979,
    -856,  -856, 10219,  3429,  -856,  -856,   324,   324,  -856,  -856,
      42,  -856,  -856,   196, 10823,   493,   382,   478,   -50,    56,
     353,  1218,  -856,   854,  -856,   229,  -856,   567,  -856,  -856,
     408,   575,  -856,   430,  -856,  -856,  1155,  6482,  -856,  -856,
     590,  -856,  -856,    33,  -856,   595,  9599,  -856,   493, 10823,
    8607, 10095,  9971, 11985,  8483, 12073,   616, 10095, 10095,  -856,
     458,   606,   546, 10095, 10095,  -856,  -856,   458,  -856,  -856,
    -856,  9723,   217,  -856,   321,  -856,   730,  -856,  -856,  -856,
    -856,  -856,   650,    66,  -856,   165,   298,   -50,   138,   157,
    6631,    16, 10219,  5340,  1014,   478,  -856,   -50,   650,    36,
     618,  8095,    71,   213,  -856,  -856,  -856,  -856,  9971,  9971,
     665,  9971,  9971,   624,    36,  -856,  -856,   388,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  1129,  -856,  -856,  -856,  -856,
    -856,  1386,  -856,  -856,  -856,  -856,  -856,   666,  -856,   667,
    9971,  -856,   671,   764,   676,  -856,   680,   776,   683,   778,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,   -50,   710,   717,   706, 10943,  -856,   708,
     676,  -856,   715,   726,  -856,   729,  -856,  -856,  5340,  9971,
     727,   493,  -856,  3429,  4901,  5191,   -50,   725,   738,  9971,
    -856,  -856,  -856,  -856,  -856,  -856, 10095,  -856,  -856,  -856,
    -856,  -856,   524,  -856,   787,  -856,  -856,  -856,  6631,  -856,
    -856,  -856,  -856,  6631, 10219,   650,  -856,  -856,   650,  -856,
    -856,  9847,  5638,   170,   650,  5340,    16,   650,  -856,  -856,
    -856,  1386,  5340,  1386,  -856,  -856,  -856,  9971,  8979,  -856,
    1129,   534,  -856,   748,   -50,  -856,   -50,    73,  9971,  1386,
    -856,  -856,   825,  -856,  -856,  -856,    46,  -856,  1386,  -856,
    -856,  1290,  -856,  -856,  -856,   765,  -856, 10943,  1386,  -856,
     844,   833,   825,  -856,  -856,  1386,  -856,  1290,  -856,   957,
    -856,   766,   775,  3429,  -856,  -856, 11063,   151,  -856,  -856,
    5911,  -856,   151,  -856,  -856,  -856,   401,  -856,   650,  -856,
    -856,   -50,   650,    16,   618,   -50,  1259,   768,  1274,  -856,
     770,  5340,  6482,  -856,  -856,  -856,  -856,   771,   774,  -856,
     676,  -856,   782,  -856,   789,  -856,   782,  -856,  -856,   833,
    -856,   790,   791,  -856, 12161,  -856,   676,   799,  -856,   801,
     799,  -856,   311,  -856,  -856,   883,  -856,   835,   210,   255,
     282,  5340,   937,  5489,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  5340,  -856,   803,  1259,   722,  -856,  -856,   650,   808,
    1386,  -856,  1290,  -856,  -856,  1290,  -856,  1290,  -856,  -856,
    6755,  1386,  -856,  1290,  -856,   805,   815,  -856,  1290,  -856,
    1290,  -856,  -856,   957,  -856,   294, 12249,  8483, 12337,   701,
    -856,   321,   650,  1259,   803,  1259,   822,  -856,  -856,   782,
     826,   782,   782,   856,   799,   828,   799,   799,  -856,    91,
     250,   -50,   315,   347,  -856,  -856,  -856,   803,  1259,  -856,
    1290,  -856,  -856,  -856,  -856,  -856,  1290,  -856,  -856,  -856,
     350,   803,   782,   799,  -856,  -856
};

  /* YYDEFACT[STATE-NUM] -- Default reduction number in state STATE-NUM.
     Performed when YYTABLE does not specify something else to do.  Zero
     means the default is an error.  */
static const yytype_uint16 yydefact[] =
{
       2,     0,     0,     1,     0,   352,   353,   354,     0,   345,
     346,   347,   350,   348,   349,   351,   340,   341,   342,   343,
     363,   272,   272,   522,   521,   523,   524,   629,     0,   629,
       0,     0,   526,   525,   527,   611,   613,   518,   517,   612,
     520,   512,   513,   514,   515,   465,   532,   533,     0,     0,
       0,     0,   299,   640,   640,    90,   319,   485,   485,   487,
     489,     0,     0,     0,     0,     0,     0,     0,     3,   627,
       6,     9,    35,    39,    51,    62,   272,    61,     0,    78,
       0,    82,    92,     0,    56,   230,   245,     0,   297,     0,
       0,    58,    58,   627,     0,     0,     0,     0,   308,    63,
     317,   286,   287,   464,   466,   288,   289,   290,   292,   291,
     293,   462,   463,   461,   510,   528,   529,   294,     0,   295,
      66,     5,     8,   173,   184,   174,   197,   170,   190,   180,
     179,   200,   201,   195,   178,   177,   172,   198,   202,   203,
     182,   171,   185,   189,   191,   183,   176,   192,   199,   194,
     193,   186,   196,   181,   169,   188,   187,   168,   175,   166,
     167,   163,   164,   165,   121,   123,   122,   158,   159,   154,
     136,   137,   138,   145,   142,   144,   139,   140,   160,   161,
     146,   147,   151,   155,   141,   143,   133,   134,   135,   148,
     149,   150,   152,   153,   156,   157,   162,   126,   128,   130,
      28,   124,   125,   127,   129,     0,     0,     0,     0,     0,
       0,     0,   485,     0,   267,     0,   252,   277,    76,   271,
     640,     0,   528,   529,     0,   295,   640,   605,    77,   629,
      74,     0,   640,   439,    73,   629,   630,     0,     0,    23,
     242,     0,    10,     0,   340,   341,   311,   440,     0,   224,
       0,   308,   225,   305,     0,    21,     0,     0,   627,    17,
      20,   629,    80,    16,   301,   629,     0,   633,   633,   253,
       0,     0,   633,   603,   629,     0,     0,     0,    88,   344,
       0,    98,    99,   106,   420,   507,   506,   508,   505,     0,
     504,     0,     0,     0,   472,   481,   477,   483,   511,    55,
     236,   237,   636,   637,     4,   638,   628,     0,     0,     0,
       0,     0,     0,     0,   355,   444,   433,    67,   446,   316,
     356,   446,   429,     0,    94,     0,    86,    83,     0,     0,
       0,     0,     0,     0,   248,   249,     0,     0,     0,     0,
     215,   216,     0,     0,     0,   246,   247,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   623,   624,     0,
     640,     0,     0,    57,     0,     0,     0,     0,     0,   627,
       0,   628,     0,   374,   373,     0,     0,   528,   529,   295,
     116,   117,     0,     0,   119,   536,     0,   528,   529,   295,
     332,   193,   186,   196,   181,   163,   164,   165,   121,   122,
     601,   334,   600,     0,    75,   626,   625,     0,   318,   467,
       0,     0,   131,   608,   305,   278,   610,   274,     0,     0,
       0,     0,   268,   276,     0,   640,     0,     0,     0,     0,
     269,   629,     0,   310,   273,   629,   263,   640,   640,   262,
     629,   315,    54,    25,    27,    26,     0,   312,     0,     0,
       0,     0,     0,     0,    19,     0,   629,   303,    15,   628,
      79,   629,   300,   306,   635,   634,   254,   635,   256,   307,
     604,     0,   105,   511,    96,    91,     0,     0,   640,     0,
     320,   421,   491,   494,   492,   509,   486,   468,   469,   488,
     470,   490,     0,     0,     0,     0,     0,   639,     7,    29,
      30,    31,    32,    33,    52,    53,     0,   640,   444,    68,
       0,   447,     0,     0,    36,   282,     0,    38,   281,   629,
       0,    84,    95,    50,    40,    48,     0,   257,   277,   204,
      37,     0,   295,   222,   229,   231,   232,   233,   240,   241,
     234,   235,   213,   214,   238,   239,   629,   226,   227,   228,
     217,   218,   219,   220,   221,   250,   251,   614,   616,   615,
     617,   438,   272,   436,   629,   614,   616,   615,   617,   437,
     272,     0,   640,   365,     0,   364,     0,     0,     0,     0,
       0,   359,     0,     0,   305,     0,   640,     0,    58,   330,
     116,   117,   118,   534,   328,     0,   640,     0,     0,     0,
     335,   621,   622,   337,   614,   615,   272,    41,   257,   205,
      47,   212,     0,     0,   607,     0,   279,   275,   640,   614,
     615,   629,   614,   615,   606,   309,   631,   259,   264,   266,
     314,    24,     0,   243,    11,    34,     0,   640,   211,    22,
      81,    18,   302,   633,     0,    89,   618,   104,   629,   614,
     615,   560,   495,     0,   471,     0,   474,     0,   479,   476,
       0,     0,   480,   223,   442,   412,   629,     0,   410,   409,
       0,   362,   443,   640,   428,   285,     0,    93,    87,     0,
       0,     0,     0,     0,   640,     0,     0,     0,     0,   435,
      71,     0,   441,     0,     0,   261,   434,    69,   260,   298,
     357,   640,   640,   453,   640,   366,   640,   323,   368,    59,
     367,   324,     0,     0,   326,   618,   304,   629,   614,   615,
       0,     0,     0,     0,   116,   117,   120,   629,     0,   629,
     538,     0,   430,    64,   132,   609,   280,   270,     0,     0,
     441,     0,     0,   640,   629,   255,    97,   441,   566,   563,
     562,   561,   564,   572,   581,     0,   592,   582,   596,   595,
     591,   560,   422,   559,   425,   565,   567,   568,   570,   545,
     574,   579,   640,   584,   640,   589,   545,   594,   545,     0,
     543,   496,   500,   501,   502,   493,   503,   473,   475,   482,
     478,   484,   408,   629,     0,   568,   393,   576,   577,   640,
     640,   587,   393,   393,   391,   414,   445,    65,     0,     0,
     283,    85,    49,   258,   614,   615,   629,   614,   615,     0,
      46,   209,    45,   210,    72,   632,     0,    43,   207,    44,
     208,    70,   454,   455,   640,   456,   358,   360,     0,    12,
      14,   460,   361,     0,     0,     0,   369,   371,     0,    60,
     325,     0,     0,   441,     0,     0,     0,     0,   441,   333,
     602,   560,     0,   560,   338,   431,   432,     0,   265,   313,
       0,   386,   377,   379,   629,   375,   629,     0,     0,     0,
     552,   573,     0,   541,   599,   583,     0,   542,     0,   555,
     593,     0,   557,   597,   497,     0,   411,     0,   399,   401,
       0,   575,     0,   389,   390,     0,   404,     0,   406,     0,
     448,   284,   441,   244,    42,   206,     0,     0,   458,   459,
       0,   372,     0,   321,   322,   279,   640,   327,     0,   535,
     329,   629,     0,     0,   538,   629,     0,   384,     0,   569,
       0,     0,     0,   423,   585,   544,   571,   545,   545,   580,
     640,   598,   545,   590,   545,   568,   545,   498,   413,   586,
     392,   393,   393,   305,     0,   578,   640,   393,   588,   393,
     393,   418,   629,   416,   419,     0,   457,     0,   528,   529,
     295,     0,   640,     0,   450,   451,   449,   331,   537,   336,
     539,     0,   376,   387,     0,   382,   378,   424,     0,     0,
       0,   548,     0,   550,   540,     0,   556,     0,   553,   558,
       0,     0,   396,     0,   398,   618,   304,   388,     0,   405,
       0,   402,   407,     0,   415,   305,     0,   640,     0,   640,
      13,   640,     0,     0,   385,     0,   380,   427,   426,   545,
     545,   545,   545,     0,   393,   393,   393,   393,   417,   618,
     304,   629,   614,   615,   452,   370,   339,   383,     0,   549,
       0,   546,   551,   554,   499,   397,     0,   394,   400,   403,
     441,   381,   545,   393,   547,   395
};

  /* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
    -856,  -856,  -856,   741,  -856,    23,   540,  -438,  -856,   -29,
    -856,   530,  -856,     6,  -856,  -214,  -311,    81,   -88,   -80,
    -856,  -856,   -30,  -856,  -166,   736,    59,   903,  -164,    -1,
     -48,  -856,  -412,     8,  2062,  -322,   904,   -54,  -856,   -14,
    -856,  -856,    13,  -856,  1459,  -856,   916,  -856,   777,  -856,
     426,    97,   409,  -353,    74,   -11,  -856,  -375,  -203,    15,
    -856,  -315,   -39,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,    43,  -856,  -856,  -856,  -856,
    -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,  -856,
    -856,   318,  -856,   305,   767,  -340,  -856,    -9,  -662,  -856,
    -855,  -852,   137,   362,   254,  -856,   357,  -856,  -678,  -856,
       9,  -856,  -856,  -856,  -856,  -856,  -856,   317,  -856,  -856,
     -99,   523,  -856,   733,  -856,  -856,  -554,  -856,    22,  -856,
    -856,    76,  -856,  -856,  -856,   952,  -856,  -856,  -856,  -856,
     566,  -856,  -856,  -856,  -856,  -856,  -856,     4,  -856,  -856,
    -258,  -856,  -856,  -856,  -856,  -856,  -856,  -856,     1,  -856,
      11,  -856,   -17,  1925,  2034,   965,  2368,  1642,  -856,  -856,
     135,  -856,  -580,  -197,  -376,  -798,  -725,   291,  -813,   180,
     238,   199,   181,  -856,  -856,  -856,   -97,  -792,  -827,   186,
     212,  -856,  -573,  -856,  -299,  -624,  -856,  -856,  -856,    90,
    -380,  -856,  -321,  -856,  -856,   -81,  -856,   -56,   -12,   632,
    -568,  -248,   -65,     0,    -2
};

  /* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
      -1,     1,     2,    68,    69,    70,   242,   571,   920,   572,
     258,   259,   455,   260,   446,    72,   524,    73,   364,   366,
     367,   849,    74,    75,   509,   248,    77,    78,   261,    79,
      80,    81,   475,    82,   215,   384,   385,   197,   198,   199,
     200,   612,   560,   202,    84,   448,   355,    85,   217,   266,
     529,   561,   696,   435,   436,   230,   231,   219,   422,   437,
     517,   518,    86,   362,   265,   461,   632,   284,   723,   721,
     598,   600,   730,   731,   934,   250,    88,    89,    90,    91,
      92,    93,    94,    95,    96,    97,   318,   321,   701,   838,
     582,   843,   844,   672,   251,   574,   709,   845,   846,   376,
     872,   873,   874,   960,   899,   793,   667,   668,   794,   972,
     973,   480,   481,   651,   877,   762,   943,   322,    99,   100,
     319,   506,   507,   510,   511,   673,   583,   986,   702,   834,
     917,   840,   101,   102,   103,   104,   105,   106,   107,   492,
     655,   108,   494,   109,   110,   493,   495,   289,   292,   293,
     486,   653,   652,   781,   894,   957,  1010,   785,   111,   290,
     112,   113,   114,   222,   223,   117,   224,   225,   594,   722,
     862,   863,   945,   880,   764,   765,   766,   955,   768,   769,
     770,   771,   798,   799,   772,   773,   774,   775,   801,   802,
     776,   777,   778,   779,   780,   883,   403,   599,   271,   438,
     227,   120,   636,   563,   603,   597,   407,   304,   432,   433,
     692,   466,   575,   371,   263
};

  /* YYTABLE[YYPACT[STATE-NUM]] -- What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule whose
     number is the opposite.  If YYTABLE_NINF, syntax error.  */
static const yytype_int16 yytable[] =
{
     121,   408,   365,   288,   305,   369,   361,   564,    71,   203,
      71,   234,   368,   530,   239,   237,   279,   240,   406,   204,
     468,   201,   257,   430,   470,   576,   712,   122,   305,   203,
     875,   220,   220,   327,   489,   491,   299,   370,   562,   204,
     570,   201,   279,   401,   848,    87,   617,    87,   298,   624,
     262,   269,   273,   740,   617,   279,   279,   279,   264,   221,
     221,   592,   291,   278,   645,   317,   946,   708,   267,   306,
     201,   763,   621,   312,   313,   214,   214,   657,   968,   573,
     747,  -101,  -107,   996,   993,   946,   606,   624,     3,   314,
     218,   228,   456,   803,    87,    87,   953,   314,   280,   607,
     610,   236,   941,   562,  -111,   570,   226,   226,   678,   221,
     201,   974,  -108,   220,   523,   895,   464,   241,   665,   233,
    -522,  -530,   243,   465,   280,   648,  -115,  -530,   661,   357,
     235,   637,   221,   221,   658,   236,   221,   375,   386,   386,
     426,   221,  1034,   268,   272,   875,   937,  -522,  -107,   853,
     887,   253,  -114,   758,  -107,   358,   405,   214,   637,   858,
     728,   519,   666,  -110,   573,   302,   -79,   303,   -98,   452,
     363,   363,   404,   316,   363,   903,   904,   759,   996,   257,
    -101,   763,  -112,  1057,   968,   662,   -93,   236,   226,   302,
    -111,   303,   946,   305,   942,  -109,   523,   523,   940,   479,
    -101,   -98,   458,  -101,   315,   946,  1071,  -101,   953,   232,
     851,   875,   315,   875,   450,   302,   418,   303,   423,   499,
     500,   501,   502,  -107,   423,   974,   836,   257,   294,   474,
     439,   -99,   837,   717,   656,   430,   656,  -614,   279,   121,
    -111,   320,  -111,   727,   220,  -106,   220,    71,   912,   624,
    -531,    87,   590,   617,   617,   262,   591,   295,   459,   473,
     454,   296,   471,  -113,   984,   297,   637,   811,  -108,   875,
    1036,  -105,   221,   726,   221,   221,   637,  -108,   221,   578,
     221,   763,  -101,   763,    87,   857,    87,   279,   214,   324,
     214,   214,   482,   514,   587,  -115,   257,    87,   525,    87,
     302,  -103,   303,   431,   305,   434,   948,  -114,   875,  -102,
     875,  -614,    71,   580,  -100,   954,   440,   503,   442,   226,
     280,   226,   323,  -113,   262,   962,  1004,   328,  -110,   498,
     428,   816,   969,   875,   521,   842,   837,   577,   516,   579,
     467,    58,  1017,   516,   233,   316,   483,   465,   484,    87,
     221,   221,   221,   221,    87,   221,   221,  -115,   439,  -107,
    -112,  -107,   562,  -109,   570,  -531,   221,   804,    87,   280,
     910,   531,   985,   852,   568,   410,   820,   822,  -103,   787,
     525,   525,   827,   829,   412,   876,   833,   235,   363,   363,
     363,   363,  -108,   504,   505,   745,  -615,   788,   414,  -113,
     419,  -113,   788,   221,  -108,    87,  -108,  -102,   420,    87,
     221,    87,   -99,  -100,  -344,   737,   837,   928,   581,   214,
     568,   421,   411,   439,   932,   221,   427,  1040,   474,  -521,
     429,  -115,   631,  -115,   357,   423,   423,   279,  1045,   568,
    -344,  -344,  -104,  -114,  -615,  -114,   447,   203,   363,  -115,
     685,  -523,   229,   531,   531,  1023,  -521,   204,   569,   201,
     358,   359,   236,   589,  -110,   568,  -110,   523,   221,  -106,
     232,   482,   474,   523,   523,   457,   439,  -103,  -523,   523,
     523,   279,   482,  1070,   214,   931,   490,   933,   624,  -344,
     357,  -524,   568,   617,   -78,   453,  -112,  -103,  -112,  -109,
    -103,  -109,    87,   998,  -103,   669,  -102,   329,   720,   360,
     482,   618,  -100,   710,   693,   914,   358,   424,  -524,   280,
     463,   221,  -114,   569,   824,   483,  -102,   484,  -614,  -102,
    -526,   831,  -100,  -102,   686,  -100,   483,   214,   484,  -100,
     737,  -110,  -105,   472,  -525,   704,  -304,   706,   469,   569,
     496,   690,   691,  1032,   643,   312,   313,  -526,   790,   697,
     476,  -101,   508,   280,   483,   425,   484,   485,   698,   800,
     703,  -525,  -304,  -304,   581,   357,   569,   981,   522,   889,
     694,   892,   983,   951,   439,   497,   748,   951,   749,   750,
     751,   752,   584,   588,   439,   733,   746,   713,   734,   804,
     568,   358,   451,   951,   698,   279,   804,  -112,   804,   691,
     568,   626,   523,   203,   593,   628,   423,    87,   629,    87,
     357,  -304,  -528,   204,  -615,   201,  -529,  -103,   -93,   221,
     634,   474,   635,   865,   856,   698,   691,   644,   806,   221,
     279,    87,   221,   826,   664,   214,   358,   477,  -528,  -528,
     425,   525,  -529,  -529,   805,   214,   855,   525,   525,   689,
     671,  -527,  -516,   525,   525,  -519,   601,   695,  -277,   453,
    -109,   669,   482,   676,  1051,   221,   679,   864,   936,   411,
     729,   741,   439,   680,   569,   681,   812,   280,  -527,  -516,
    -100,   854,  -519,   602,   569,   478,  -114,  -528,   568,   835,
     841,  -529,   847,   695,   847,   691,  -115,   637,  -110,   700,
      87,   705,   804,   744,   804,   691,   516,   789,   743,   804,
    -295,   804,   280,    87,   531,   791,   483,   221,   484,   487,
     531,   531,   807,   689,   695,   329,   531,   531,    76,  -278,
      76,   423,   819,   214,   201,   837,  -295,  -295,   742,   443,
    1001,  1003,    76,    76,   825,  1006,   922,  1008,   279,  1009,
     444,   445,   867,    87,   861,   221,    87,   804,   868,    98,
     884,    98,   884,   357,   748,  -112,   749,   750,   751,   752,
     357,   805,   569,    98,    98,   950,  -305,    76,    76,  -109,
     878,   929,   352,   353,   354,  -295,   525,   884,   884,   358,
     585,   800,    76,   363,   691,   966,   358,   595,   800,   919,
     800,   879,  -305,  -305,   921,   882,   885,   674,    98,    98,
     886,  -611,   693,   926,   888,    76,    76,   891,   890,    76,
     893,   270,   918,    98,    76,   694,   609,   611,  -612,   896,
     901,   897,  1059,  1061,  1062,  1063,   796,   357,   586,  -110,
     898,    87,   902,   609,   611,   596,    98,    98,   279,   905,
      98,  -305,  -112,  -516,   805,    98,  1035,   441,   990,   531,
     907,  -279,   826,   358,   683,  1074,   699,   279,   909,   638,
    -519,    87,   707,   753,   711,   916,    87,   221,   714,  -516,
    -516,   982,   938,   460,   958,    87,  1028,   462,    87,  -109,
     963,   357,   754,   357,   797,    87,  -519,  -519,   782,   783,
    -280,   784,   994,   999,   800,  1000,   800,   997,  1002,    46,
      47,   800,   684,   800,   847,   363,  1005,   358,   964,   358,
    1026,   757,   758,  1007,  1011,  1013,  -611,   482,  -516,  1025,
     959,  -611,   767,  1018,    76,  1020,   836,  1033,   884,   482,
    1038,  -614,  1029,  -612,  1031,  -519,   759,   795,  -612,   977,
    1024,  -615,   482,    87,   884,    76,  1058,    76,    76,   800,
    1060,    76,  1066,    76,  1064,    98,   596,    76,  1027,    76,
     841,  1043,   449,   413,    87,    87,   415,   416,   417,   641,
      76,   483,    76,   484,   488,   639,    98,   373,    98,    98,
     390,   356,    98,   483,    98,   484,   654,   935,    98,   971,
      98,   749,   750,   751,   752,   732,   483,   850,   484,   659,
     839,    98,  1055,    98,    87,   439,    87,   703,   792,   847,
     808,   670,  1048,   859,    87,    41,    42,    43,    44,   691,
    -618,   568,    76,    76,    76,    76,    76,    76,    76,    76,
     866,  1054,   767,    87,   512,   409,   906,   908,  1030,    76,
     660,    76,   402,   625,    76,   334,   335,   627,   952,   991,
     221,   956,   630,    98,    98,    98,    98,    98,    98,    98,
      98,   949,  -618,   965,   961,   967,   214,   970,   640,     0,
      98,   947,    98,   642,     0,    98,    76,     0,    76,     0,
     515,     0,    76,    76,    76,   528,     0,     0,  -618,  -618,
     345,   346,     0,   821,   823,     0,   329,     0,    76,   828,
     830,     0,     0,     0,     0,   569,     0,    98,     0,    98,
       0,   342,   343,    98,    98,    98,   797,     0,     0,     0,
     797,     0,     0,   797,     0,   797,    76,    76,     0,    98,
     923,   677,   767,   924,   767,  -618,     0,  -618,     0,   927,
    -614,    76,   930,   482,   821,   823,     0,   828,   830,     0,
     767,   350,   351,   352,   353,   354,     0,    98,    98,   767,
    1039,   748,  1041,   749,   750,   751,   752,  1042,     0,   795,
       0,  1044,    98,  1046,     0,    76,   795,   614,   616,     0,
    1047,   307,   308,   309,   310,   311,   270,   748,   329,   749,
     750,   751,   752,   753,    76,  1012,  1014,   483,     0,   484,
     487,  1019,   613,  1021,  1022,     0,    98,     0,   870,     0,
       0,     0,   754,   987,   871,     0,     0,   989,     0,     0,
    1072,   616,     0,     0,   270,    98,  1073,     0,     0,   797,
       0,   797,   915,     0,   755,     0,   797,     0,   797,     0,
     756,   757,   758,   350,   351,   352,   353,   354,     0,     0,
     748,     0,   749,   750,   751,   752,   753,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   759,     0,     0,   760,
     675,   767,     0,   915,     0,   754,     0,     0,  1065,  1067,
    1068,  1069,   795,  1037,   797,     0,   236,     0,     0,     0,
      76,   748,    76,   749,   750,   751,   752,   755,     0,     0,
       0,     0,    76,   756,   757,   758,   748,  1075,   749,   750,
     751,   752,    76,     0,    76,    76,     0,  1056,     0,     0,
       0,    98,   748,    98,   749,   750,   751,   752,   753,   759,
       0,     0,   760,    98,     0,   329,     0,     0,   870,   528,
       0,   860,     0,    98,   761,    98,    98,   754,    76,     0,
     342,   343,     0,   870,     0,     0,   869,     0,     0,   995,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   755,
     735,     0,   736,     0,     0,     0,   757,   758,     0,    98,
       0,     0,     0,    76,     0,   616,   270,   347,   348,   349,
     350,   351,   352,   353,   354,     0,    76,    76,     0,     0,
      76,   759,     0,    76,    76,     0,     0,     0,     0,    76,
      76,     0,     0,     0,    98,     0,     0,     0,   748,     0,
     749,   750,   751,   752,   753,     0,     0,    98,    98,     0,
       0,    98,     0,   810,    98,    98,    76,     0,    76,    76,
      98,    98,     0,   754,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   216,   216,     0,   832,   329,
       0,     0,     0,     0,     0,   755,     0,    98,     0,    98,
      98,   756,   757,   758,   342,   343,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   939,   249,   252,     0,
       0,     0,   216,   216,     0,     0,     0,   759,     0,     0,
     760,     0,     0,     0,     0,   300,   301,     0,     0,     0,
       0,     0,     0,   349,   350,   351,   352,   353,   354,     0,
       0,     0,     0,     0,    76,     0,     0,   881,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   216,     0,     0,
       0,     0,    76,   988,     0,     0,     0,   992,     0,     0,
       0,     0,     0,     0,    76,    98,     0,     0,     0,    76,
      76,     0,     0,     0,     0,     0,   911,     0,    76,     0,
       0,    76,     0,    98,     0,     0,     0,     0,    76,     0,
       0,     0,     0,     0,     0,    98,     0,     0,     0,     0,
      98,    98,     0,     0,     0,     0,     0,     0,     0,    98,
       0,     0,    98,     0,     0,     0,     0,     0,   925,    98,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   119,   270,   119,     0,   329,  -641,
    -641,  -641,  -641,   334,   335,   944,    76,  -641,  -641,     0,
       0,     0,     0,   342,   343,   216,     0,     0,   216,   216,
     216,     0,   300,     0,     0,     0,     0,    76,    76,     0,
       0,     0,     0,     0,     0,     0,     0,    98,   216,     0,
     216,   216,     0,   119,   119,     0,     0,   283,   345,   346,
     347,   348,   349,   350,   351,   352,   353,   354,    98,    98,
       0,     0,     0,     0,     0,     0,     0,    76,     0,    76,
       0,     0,     0,   283,     0,     0,     0,    76,     0,     0,
       0,     0,     0,     0,     0,     0,   379,   389,   389,     0,
       0,     0,     0,     0,     0,     0,    76,     0,    98,     0,
      98,     0,     0,     0,     0,     0,     0,     0,    98,     0,
       0,     0,     0,    76,     0,     0,     0,     0,     0,     0,
     329,   330,   331,   332,   333,   334,   335,    98,     0,   338,
     339,     0,   216,     0,     0,   342,   343,   527,   533,   534,
     535,   536,   537,     0,    98,   538,   539,   540,   541,   542,
     543,   544,   545,   546,     0,     0,   547,   548,   549,   550,
     551,   552,   553,   554,   555,   556,     0,     0,     0,   216,
     345,   346,   347,   348,   349,   350,   351,   352,   353,   354,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     119,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   608,
     608,     0,     0,     0,     0,     0,     0,     0,   608,   216,
     216,     0,     0,   119,   216,   119,   608,   608,   216,     0,
       0,     0,     0,     0,     0,     0,   119,     0,   119,     0,
       0,     0,     0,     0,     0,     0,     0,   633,     0,     0,
       0,     0,   608,     0,     0,     0,     0,     0,     0,   283,
       0,     0,     0,   216,     0,     0,   216,   115,     0,   115,
       0,     0,     0,     0,     0,     0,     0,   216,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   119,     0,
       0,     0,     0,   119,     0,   663,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   119,   283,     0,
     532,     0,   216,     0,     0,     0,   115,   115,     0,     0,
     281,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   119,     0,   281,     0,   119,     0,
     119,     0,     0,     0,     0,     0,     0,     0,     0,   377,
     387,   387,   387,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   116,     0,   116,     0,
       0,   216,     0,     0,     0,   216,     0,     0,     0,     0,
       0,     0,   532,   532,     0,   216,     0,     0,     0,     0,
       0,     0,     0,     0,    83,     0,    83,     0,     0,     0,
       0,     0,   216,     0,   216,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   116,   116,   216,   216,   282,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   119,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    83,    83,   282,     0,     0,   283,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   378,   388,
     388,   388,     0,   115,     0,   216,     0,     0,     0,     0,
     608,   813,     0,   216,     0,     0,   608,   608,     0,     0,
       0,     0,   608,   608,     0,     0,   374,     0,     0,     0,
     216,     0,   283,     0,     0,     0,   115,     0,   115,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   115,
       0,   115,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   608,   608,     0,
     608,   608,   281,     0,   329,   330,   331,   332,   333,   334,
     335,   336,     0,   338,   339,     0,   119,     0,   119,   342,
     343,     0,     0,     0,     0,     0,     0,     0,     0,   216,
       0,   115,     0,     0,     0,     0,   115,     0,     0,     0,
     119,     0,   116,     0,     0,     0,     0,     0,     0,     0,
     115,   281,     0,     0,   345,   346,   347,   348,   349,   350,
     351,   352,   353,   354,     0,     0,     0,     0,   216,     0,
      83,     0,     0,     0,     0,   116,     0,   116,   913,     0,
       0,     0,     0,     0,     0,   608,   283,   115,   116,     0,
     116,   115,     0,   115,     0,   786,     0,     0,     0,     0,
       0,     0,     0,    83,     0,    83,     0,     0,     0,   119,
     216,   282,     0,     0,     0,     0,    83,     0,    83,     0,
       0,   283,   119,   532,     0,     0,   608,   216,     0,   532,
     532,     0,     0,     0,     0,   532,   532,   216,     0,     0,
     116,     0,     0,     0,     0,   116,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   116,
     282,     0,   119,     0,     0,   119,  -640,     0,    83,     0,
     118,     0,   118,    83,  -640,  -640,  -640,     0,     0,  -640,
    -640,  -640,     0,  -640,   115,     0,     0,    83,     0,     0,
     526,     0,  -640,  -640,     0,     0,   116,     0,     0,     0,
     116,   281,   116,  -640,  -640,     0,  -640,  -640,  -640,  -640,
    -640,     0,     0,     0,     0,     0,     0,     0,     0,   118,
     118,     0,     0,     0,    83,     0,     0,     0,    83,     0,
      83,     0,     0,     0,  -640,     0,     0,     0,     0,   389,
       0,     0,     0,     0,     0,   281,     0,     0,     0,     0,
     119,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -640,  -640,     0,     0,     0,     0,     0,     0,   532,     0,
       0,     0,   526,   526,     0,     0,     0,     0,     0,     0,
     119,     0,     0,     0,  -640,   119,   216,     0,     0,     0,
       0,     0,     0,   116,   119,     0,     0,   119,     0,   115,
       0,   115,     0,     0,   119,     0,     0,  -640,  -640,     0,
     282,     0,   232,  -640,     0,  -640,     0,  -640,     0,     0,
       0,    83,     0,   115,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   389,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   282,     0,     0,     0,   980,     0,
       0,     0,   119,     0,     0,     0,     0,     0,     0,   281,
       0,     0,     0,     0,     0,     0,   118,     0,     0,     0,
       0,     0,     0,   119,   119,     0,     0,     0,     0,     0,
       0,     0,   115,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   281,   115,     0,     0,   116,   118,
     116,   118,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   118,   119,   118,   119,     0,     0,     0,     0,
       0,     0,   116,   119,     0,     0,    83,     0,    83,     0,
       0,     0,     0,     0,     0,   115,     0,     0,   115,     0,
       0,     0,   119,     0,     0,     0,     0,     0,     0,     0,
      83,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   118,     0,     0,     0,   282,   118,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   118,     0,     0,   118,     0,     0,     0,
       0,   116,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   282,   116,     0,     0,     0,     0,     0,
       0,     0,   387,     0,     0,     0,     0,     0,     0,    83,
     118,     0,     0,   115,   118,     0,   118,     0,     0,     0,
       0,     0,    83,   526,     0,     0,     0,     0,     0,   526,
     526,     0,     0,     0,   116,   526,   526,   116,     0,     0,
       0,     0,     0,   115,     0,   682,     0,     0,   115,     0,
       0,     0,     0,     0,     0,     0,     0,   115,   118,   118,
     115,     0,    83,     0,     0,    83,     0,   115,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   329,   330,
     331,   332,   333,   334,   335,   336,   337,   338,   339,   340,
     341,     0,     0,   342,   343,     0,     0,     0,     0,     0,
       0,     0,   387,     0,     0,     0,     0,   118,     0,     0,
       0,   388,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   978,   116,     0,     0,   115,   344,     0,   345,   346,
     347,   348,   349,   350,   351,   352,   353,   354,     0,     0,
       0,     0,     0,     0,     0,  -252,   115,   115,     0,     0,
      83,     0,   116,     0,     0,     0,     0,   116,     0,     0,
       0,     0,     0,     0,     0,     0,   116,     0,   526,   116,
       0,     0,     0,     0,     0,     0,   116,     0,     0,     0,
      83,     0,     0,     0,     0,    83,   115,     0,   115,     0,
       0,     0,     0,     0,    83,     0,   115,    83,     0,     0,
       0,     0,     0,     0,    83,     0,     0,     0,     0,     0,
       0,   388,     0,     0,     0,   115,     0,     0,     0,     0,
       0,     0,   118,     0,   118,     0,     0,     0,     0,     0,
     979,     0,     0,     0,   116,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   118,     0,     0,     0,
       0,     0,     0,     0,     0,   116,   116,     0,   976,     0,
       0,     0,    83,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    83,    83,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   116,     0,   116,     0,     0,
       0,     0,     0,     0,     0,   116,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   118,     0,     0,     0,     0,
       0,     0,     0,    83,   116,    83,     0,     0,   118,   118,
       5,     6,     7,    83,     9,   118,   118,     0,    10,    11,
       0,   118,   118,    12,     0,    13,    14,    15,   244,   245,
      18,    19,    83,     0,     0,     0,     0,    20,   246,   247,
      23,    24,    25,    26,     0,     0,   205,     0,   118,     0,
       0,   118,     0,   274,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
       0,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   275,     0,     0,   208,    52,     0,    53,
      54,     0,     0,     0,     0,    56,    57,    58,    59,    60,
      61,    62,    63,    64,     0,     0,     0,     0,     0,     0,
       0,     0,  -618,     0,     0,     0,   118,     0,     0,     0,
    -618,  -618,  -618,     0,   276,  -618,  -618,  -618,     0,  -618,
       0,   277,     0,     0,   118,     0,     0,     0,  -618,  -618,
    -618,  -618,     0,     0,     0,     0,   118,     0,     0,  -618,
    -618,   118,  -618,  -618,  -618,  -618,  -618,     0,     0,     0,
     118,     0,     0,   118,     0,     0,     0,     0,     0,     0,
     118,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -618,     0,     0,     0,     0,     0,     0,     0,     0,  -618,
    -618,  -618,  -618,  -618,  -618,  -618,  -618,  -618,  -618,  -618,
    -618,  -618,     0,     0,  -618,  -618,  -618,  -618,     0,   738,
    -618,     0,     0,     0,     0,     0,  -618,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   118,     0,
    -618,     0,     0,  -618,     0,     0,  -111,  -618,  -618,  -618,
    -618,  -618,  -618,  -618,  -618,  -618,  -618,  -618,  -618,   118,
     118,     0,     0,  -618,  -618,  -618,  -618,     0,     0,  -618,
    -618,  -618,     0,  -618,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -640,     4,     0,     5,     6,     7,
       8,     9,     0,     0,     0,    10,    11,     0,     0,   118,
      12,   118,    13,    14,    15,    16,    17,    18,    19,   118,
       0,     0,     0,     0,    20,    21,    22,    23,    24,    25,
      26,     0,     0,    27,     0,     0,     0,     0,   118,    28,
      29,    30,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,     0,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      50,     0,     0,    51,    52,     0,    53,    54,     0,    55,
       0,     0,    56,    57,    58,    59,    60,    61,    62,    63,
      64,  -516,     0,     0,     0,     0,     0,     0,     0,  -516,
    -516,  -516,     0,     0,  -516,  -516,  -516,     0,  -516,     0,
       0,    65,    66,    67,     0,     0,  -516,     0,  -516,  -516,
    -516,     0,     0,  -640,     0,  -640,     0,     0,  -516,  -516,
       0,  -516,  -516,  -516,  -516,  -516,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   329,   330,   331,   332,
     333,   334,   335,   336,   337,   338,   339,   340,   341,  -516,
       0,   342,   343,     0,     0,     0,     0,     0,  -516,  -516,
    -516,  -516,  -516,  -516,  -516,  -516,  -516,  -516,  -516,  -516,
    -516,     0,     0,  -516,  -516,  -516,  -516,     0,  -516,  -516,
       0,     0,     0,     0,   344,  -516,   345,   346,   347,   348,
     349,   350,   351,   352,   353,   354,     0,     0,     0,  -516,
       0,     0,  -516,     0,     0,  -516,  -516,  -516,  -516,  -516,
    -516,  -516,  -516,  -516,  -516,  -516,  -516,  -516,     0,     0,
       0,     0,     0,  -516,  -516,  -516,  -519,     0,  -516,  -516,
    -516,     0,  -516,     0,  -519,  -519,  -519,     0,     0,  -519,
    -519,  -519,     0,  -519,     0,     0,     0,     0,   682,     0,
       0,  -519,     0,  -519,  -519,  -519,     0,     0,     0,     0,
       0,     0,     0,  -519,  -519,     0,  -519,  -519,  -519,  -519,
    -519,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   329,   330,   331,   332,   333,   334,   335,   336,   337,
     338,   339,   340,   341,  -519,     0,   342,   343,     0,     0,
       0,     0,     0,  -519,  -519,  -519,  -519,  -519,  -519,  -519,
    -519,  -519,  -519,  -519,  -519,  -519,     0,     0,  -519,  -519,
    -519,  -519,     0,  -519,  -519,     0,     0,     0,     0,   344,
    -519,   345,   346,   347,   348,   349,   350,   351,   352,   353,
     354,     0,     0,     0,  -519,     0,     0,  -519,     0,     0,
    -519,  -519,  -519,  -519,  -519,  -519,  -519,  -519,  -519,  -519,
    -519,  -519,  -519,     0,     0,     0,     0,     0,  -519,  -519,
    -519,  -619,     0,  -519,  -519,  -519,     0,  -519,     0,  -619,
    -619,  -619,     0,     0,  -619,  -619,  -619,     0,  -619,     0,
       0,     0,     0,     0,     0,     0,     0,  -619,  -619,  -619,
    -619,     0,     0,     0,     0,     0,     0,     0,  -619,  -619,
       0,  -619,  -619,  -619,  -619,  -619,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   329,   330,   331,   332,
     333,   334,   335,   336,   337,   338,   339,  -641,  -641,  -619,
       0,   342,   343,     0,     0,     0,     0,     0,  -619,  -619,
    -619,  -619,  -619,  -619,  -619,  -619,  -619,  -619,  -619,  -619,
    -619,     0,     0,  -619,  -619,  -619,  -619,     0,     0,  -619,
       0,     0,     0,     0,     0,  -619,   345,   346,   347,   348,
     349,   350,   351,   352,   353,   354,     0,     0,     0,  -619,
       0,     0,  -619,     0,     0,     0,  -619,  -619,  -619,  -619,
    -619,  -619,  -619,  -619,  -619,  -619,  -619,  -619,     0,     0,
       0,     0,  -619,  -619,  -619,  -619,  -620,     0,  -619,  -619,
    -619,     0,  -619,     0,  -620,  -620,  -620,     0,     0,  -620,
    -620,  -620,     0,  -620,     0,     0,     0,     0,     0,     0,
       0,     0,  -620,  -620,  -620,  -620,     0,     0,     0,     0,
       0,     0,     0,  -620,  -620,     0,  -620,  -620,  -620,  -620,
    -620,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -620,     0,     0,     0,     0,     0,
       0,     0,     0,  -620,  -620,  -620,  -620,  -620,  -620,  -620,
    -620,  -620,  -620,  -620,  -620,  -620,     0,     0,  -620,  -620,
    -620,  -620,     0,     0,  -620,     0,     0,     0,     0,     0,
    -620,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -620,     0,     0,  -620,     0,     0,
       0,  -620,  -620,  -620,  -620,  -620,  -620,  -620,  -620,  -620,
    -620,  -620,  -620,     0,     0,     0,     0,  -620,  -620,  -620,
    -620,  -304,     0,  -620,  -620,  -620,     0,  -620,     0,  -304,
    -304,  -304,     0,     0,  -304,  -304,  -304,     0,  -304,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -304,  -304,
    -304,     0,     0,     0,     0,     0,     0,     0,  -304,  -304,
       0,  -304,  -304,  -304,  -304,  -304,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -304,
       0,     0,     0,     0,     0,     0,     0,     0,  -304,  -304,
    -304,  -304,  -304,  -304,  -304,  -304,  -304,  -304,  -304,  -304,
    -304,     0,     0,  -304,  -304,  -304,  -304,     0,   739,  -304,
       0,     0,     0,     0,     0,  -304,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -304,
       0,     0,  -304,     0,     0,  -113,  -304,  -304,  -304,  -304,
    -304,  -304,  -304,  -304,  -304,  -304,  -304,  -304,     0,     0,
       0,     0,     0,  -304,  -304,  -304,  -440,     0,  -304,  -304,
    -304,     0,  -304,     0,  -440,  -440,  -440,     0,     0,  -440,
    -440,  -440,     0,  -440,     0,     0,     0,     0,     0,     0,
       0,     0,  -440,  -440,  -440,     0,     0,     0,     0,     0,
       0,     0,     0,  -440,  -440,     0,  -440,  -440,  -440,  -440,
    -440,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -440,     0,     0,     0,     0,     0,
       0,     0,     0,  -440,  -440,  -440,  -440,  -440,  -440,  -440,
    -440,  -440,  -440,  -440,  -440,  -440,     0,     0,  -440,  -440,
    -440,  -440,     0,     0,  -440,     0,     0,     0,     0,     0,
    -440,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -440,     0,     0,     0,     0,     0,
       0,  -440,     0,  -440,  -440,  -440,  -440,  -440,  -440,  -440,
    -440,  -440,  -440,     0,     0,     0,     0,  -440,  -440,  -440,
    -440,  -296,   232,  -440,  -440,  -440,     0,  -440,     0,  -296,
    -296,  -296,     0,     0,  -296,  -296,  -296,     0,  -296,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -296,  -296,
    -296,     0,     0,     0,     0,     0,     0,     0,  -296,  -296,
       0,  -296,  -296,  -296,  -296,  -296,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -296,
       0,     0,     0,     0,     0,     0,     0,     0,  -296,  -296,
    -296,  -296,  -296,  -296,  -296,  -296,  -296,  -296,  -296,  -296,
    -296,     0,     0,  -296,  -296,  -296,  -296,     0,     0,  -296,
       0,     0,     0,     0,     0,  -296,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -296,
       0,     0,  -296,     0,     0,     0,  -296,  -296,  -296,  -296,
    -296,  -296,  -296,  -296,  -296,  -296,  -296,  -296,     0,     0,
       0,     0,     0,  -296,  -296,  -296,  -640,     0,  -296,  -296,
    -296,     0,  -296,     0,  -640,  -640,  -640,     0,     0,  -640,
    -640,  -640,     0,  -640,     0,     0,     0,     0,     0,     0,
       0,     0,  -640,  -640,  -640,     0,     0,     0,     0,     0,
       0,     0,     0,  -640,  -640,     0,  -640,  -640,  -640,  -640,
    -640,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -640,     0,     0,     0,     0,     0,
       0,     0,     0,  -640,  -640,  -640,  -640,  -640,  -640,  -640,
    -640,  -640,  -640,  -640,  -640,  -640,     0,     0,  -640,  -640,
    -640,  -640,     0,     0,  -640,     0,     0,     0,     0,     0,
    -640,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -640,     0,     0,     0,     0,     0,
       0,  -640,     0,  -640,  -640,  -640,  -640,  -640,  -640,  -640,
    -640,  -640,  -640,     0,     0,     0,     0,  -640,  -640,  -640,
    -640,  -311,   232,  -640,  -640,  -640,     0,  -640,     0,  -311,
    -311,  -311,     0,     0,  -311,  -311,  -311,     0,  -311,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -311,  -311,
       0,     0,     0,     0,     0,     0,     0,     0,  -311,  -311,
       0,  -311,  -311,  -311,  -311,  -311,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -311,
       0,     0,     0,     0,     0,     0,     0,     0,  -311,  -311,
    -311,  -311,  -311,  -311,  -311,  -311,  -311,  -311,  -311,  -311,
    -311,     0,     0,  -311,  -311,  -311,  -311,     0,     0,  -311,
       0,     0,     0,     0,     0,  -311,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -311,
       0,     0,     0,     0,     0,     0,  -311,     0,  -311,  -311,
    -311,  -311,  -311,  -311,  -311,  -311,  -311,  -311,     0,     0,
       0,     0,     0,  -311,  -311,  -311,  -618,   229,  -311,  -311,
    -311,     0,  -311,     0,  -618,  -618,  -618,     0,     0,     0,
    -618,  -618,     0,  -618,     0,     0,     0,     0,     0,     0,
       0,     0,  -618,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -618,  -618,     0,  -618,  -618,  -618,  -618,
    -618,     0,     0,     0,   329,   330,   331,   332,   333,   334,
     335,   336,   337,   338,   339,   340,   341,     0,     0,   342,
     343,     0,     0,     0,  -618,     0,     0,     0,     0,     0,
       0,     0,     0,  -618,  -618,  -618,  -618,  -618,  -618,  -618,
    -618,  -618,  -618,  -618,  -618,  -618,     0,     0,  -618,  -618,
    -618,  -618,   344,   687,   345,   346,   347,   348,   349,   350,
     351,   352,   353,   354,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -618,     0,     0,     0,   236,     0,
    -111,  -618,     0,  -618,  -618,  -618,  -618,  -618,  -618,  -618,
    -618,  -618,  -618,     0,     0,     0,     0,  -618,  -618,  -618,
    -102,  -618,     0,  -618,     0,  -618,     0,  -618,     0,  -618,
    -618,  -618,     0,     0,     0,  -618,  -618,     0,  -618,     0,
       0,     0,     0,     0,     0,     0,     0,  -618,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -618,  -618,
       0,  -618,  -618,  -618,  -618,  -618,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -618,
       0,     0,     0,     0,     0,     0,     0,     0,  -618,  -618,
    -618,  -618,  -618,  -618,  -618,  -618,  -618,  -618,  -618,  -618,
    -618,     0,     0,  -618,  -618,  -618,  -618,     0,   687,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -618,
       0,     0,     0,     0,     0,  -111,  -618,     0,  -618,  -618,
    -618,  -618,  -618,  -618,  -618,  -618,  -618,  -618,     0,     0,
       0,     0,  -618,  -618,  -618,  -618,  -304,     0,  -618,     0,
    -618,     0,  -618,     0,  -304,  -304,  -304,     0,     0,     0,
    -304,  -304,     0,  -304,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -304,  -304,     0,  -304,  -304,  -304,  -304,
    -304,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -304,     0,     0,     0,     0,     0,
       0,     0,     0,  -304,  -304,  -304,  -304,  -304,  -304,  -304,
    -304,  -304,  -304,  -304,  -304,  -304,     0,     0,  -304,  -304,
    -304,  -304,     0,   688,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -304,     0,     0,     0,     0,     0,
    -113,  -304,     0,  -304,  -304,  -304,  -304,  -304,  -304,  -304,
    -304,  -304,  -304,     0,     0,     0,     0,     0,  -304,  -304,
    -104,  -304,     0,  -304,     0,  -304,     0,  -304,     0,  -304,
    -304,  -304,     0,     0,     0,  -304,  -304,     0,  -304,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -304,  -304,
       0,  -304,  -304,  -304,  -304,  -304,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -304,
       0,     0,     0,     0,     0,     0,     0,     0,  -304,  -304,
    -304,  -304,  -304,  -304,  -304,  -304,  -304,  -304,  -304,  -304,
    -304,     0,     0,  -304,  -304,  -304,  -304,     0,   688,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -304,
       0,     0,     0,     0,     0,  -113,  -304,     0,  -304,  -304,
    -304,  -304,  -304,  -304,  -304,  -304,  -304,  -304,     0,     0,
       0,     0,     0,  -304,  -304,  -304,     0,     0,  -304,     0,
    -304,   254,  -304,     5,     6,     7,     8,     9,  -640,  -640,
    -640,    10,    11,     0,     0,  -640,    12,     0,    13,    14,
      15,    16,    17,    18,    19,     0,     0,     0,     0,     0,
      20,    21,    22,    23,    24,    25,    26,     0,     0,    27,
       0,     0,     0,     0,     0,    28,    29,   255,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,     0,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,    48,    49,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,     0,     0,    51,
      52,     0,    53,    54,     0,    55,     0,     0,    56,    57,
      58,    59,    60,    61,    62,    63,    64,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    65,    66,    67,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -640,
     254,  -640,     5,     6,     7,     8,     9,     0,     0,  -640,
      10,    11,     0,  -640,  -640,    12,     0,    13,    14,    15,
      16,    17,    18,    19,     0,     0,     0,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,    27,     0,
       0,     0,     0,     0,    28,    29,   255,    31,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,     0,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,     0,     0,    51,    52,
       0,    53,    54,     0,    55,     0,     0,    56,    57,    58,
      59,    60,    61,    62,    63,    64,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    65,    66,    67,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -640,   254,
    -640,     5,     6,     7,     8,     9,     0,     0,  -640,    10,
      11,     0,     0,  -640,    12,  -640,    13,    14,    15,    16,
      17,    18,    19,     0,     0,     0,     0,     0,    20,    21,
      22,    23,    24,    25,    26,     0,     0,    27,     0,     0,
       0,     0,     0,    28,    29,   255,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,     0,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    50,     0,     0,    51,    52,     0,
      53,    54,     0,    55,     0,     0,    56,    57,    58,    59,
      60,    61,    62,    63,    64,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    65,    66,    67,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -640,   254,  -640,
       5,     6,     7,     8,     9,     0,     0,  -640,    10,    11,
       0,     0,  -640,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,    27,     0,     0,     0,
       0,     0,    28,    29,   255,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
       0,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,     0,     0,    51,    52,     0,    53,
      54,     0,    55,     0,     0,    56,    57,    58,    59,    60,
      61,    62,    63,    64,     0,     0,     0,     0,     0,     0,
       0,     0,   254,     0,     5,     6,     7,     8,     9,     0,
    -640,  -640,    10,    11,    65,    66,    67,    12,     0,    13,
      14,    15,    16,    17,    18,    19,  -640,     0,  -640,     0,
       0,    20,    21,    22,    23,    24,    25,    26,     0,     0,
      27,     0,     0,     0,     0,     0,    28,    29,   255,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,     0,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,     0,     0,
      51,    52,     0,    53,    54,     0,    55,     0,     0,    56,
      57,    58,    59,    60,    61,    62,    63,    64,     0,     0,
       0,     0,     0,     0,     0,     0,   254,     0,     5,     6,
       7,     8,     9,     0,     0,     0,    10,    11,    65,    66,
      67,    12,     0,    13,    14,    15,    16,    17,    18,    19,
    -640,     0,  -640,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,    27,     0,     0,     0,     0,     0,
      28,    29,   255,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,     0,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,     0,     0,   256,    52,     0,    53,    54,     0,
      55,     0,     0,    56,    57,    58,    59,    60,    61,    62,
      63,    64,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    65,    66,    67,     0,     0,     0,     0,     0,
       0,     0,  -640,     0,  -640,   254,  -640,     5,     6,     7,
       8,     9,     0,     0,     0,    10,    11,     0,     0,     0,
      12,     0,    13,    14,    15,    16,    17,    18,    19,     0,
       0,     0,     0,     0,    20,    21,    22,    23,    24,    25,
      26,     0,     0,    27,     0,     0,     0,     0,     0,    28,
      29,   255,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,     0,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      50,     0,     0,    51,    52,     0,    53,    54,     0,    55,
       0,     0,    56,    57,    58,    59,    60,    61,    62,    63,
      64,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    65,    66,    67,     0,     0,     0,     0,     0,     0,
       0,  -640,     0,  -640,     4,  -640,     5,     6,     7,     8,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
       0,     0,     0,    20,    21,    22,    23,    24,    25,    26,
       0,     0,    27,     0,     0,     0,     0,     0,    28,    29,
      30,    31,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
       0,     0,    51,    52,     0,    53,    54,     0,    55,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      65,    66,    67,     0,     0,  -640,     0,     0,     0,     0,
       0,     0,  -640,   254,  -640,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,     0,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   255,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,     0,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,     0,
       0,    51,    52,     0,    53,    54,     0,    55,     0,     0,
      56,    57,    58,    59,    60,    61,    62,    63,    64,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    65,
      66,    67,     0,     0,  -640,     0,     0,     0,     0,     0,
       0,  -640,   254,  -640,     5,     6,     7,     8,     9,     0,
       0,  -640,    10,    11,     0,     0,     0,    12,     0,    13,
      14,    15,    16,    17,    18,    19,     0,     0,     0,     0,
       0,    20,    21,    22,    23,    24,    25,    26,     0,     0,
      27,     0,     0,     0,     0,     0,    28,    29,   255,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,     0,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,     0,     0,
      51,    52,     0,    53,    54,     0,    55,     0,     0,    56,
      57,    58,    59,    60,    61,    62,    63,    64,     0,     0,
       0,     0,     0,     0,     0,     0,   254,     0,     5,     6,
       7,     8,     9,     0,     0,     0,    10,    11,    65,    66,
      67,    12,     0,    13,    14,    15,    16,    17,    18,    19,
    -640,     0,  -640,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,    27,     0,     0,     0,     0,     0,
      28,    29,   255,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,     0,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,     0,     0,    51,    52,     0,    53,    54,     0,
      55,     0,     0,    56,    57,    58,    59,    60,    61,    62,
      63,    64,     0,  -640,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    65,    66,    67,    12,     0,    13,    14,    15,
      16,    17,    18,    19,  -640,     0,  -640,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,   205,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,   206,    41,    42,
      43,    44,     0,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   207,     0,     0,   208,    52,
       0,    53,    54,     0,   209,   210,   211,    56,    57,   212,
      59,    60,    61,    62,    63,    64,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    65,   213,    67,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
     236,     0,     0,    20,    21,    22,    23,    24,    25,    26,
       0,     0,    27,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   207,
       0,     0,   208,    52,     0,    53,    54,     0,     0,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      65,    66,    67,    12,     0,    13,    14,    15,    16,    17,
      18,    19,   302,     0,   303,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,    27,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
       0,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   207,     0,     0,   208,    52,     0,    53,
      54,     0,     0,     0,     0,    56,    57,    58,    59,    60,
      61,    62,    63,    64,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     8,     9,     0,
       0,     0,    10,    11,    65,    66,    67,    12,     0,    13,
      14,    15,    16,    17,    18,    19,     0,     0,   236,     0,
       0,    20,    21,    22,    23,    24,    25,    26,     0,     0,
      27,     0,     0,     0,     0,     0,    28,    29,    30,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,     0,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,     0,     0,
      51,    52,     0,    53,    54,     0,    55,     0,     0,    56,
      57,    58,    59,    60,    61,    62,    63,    64,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     8,     9,     0,     0,     0,    10,    11,    65,    66,
      67,    12,     0,    13,    14,    15,    16,    17,    18,    19,
     497,     0,     0,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,    27,     0,     0,     0,     0,     0,
      28,    29,   255,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,     0,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,     0,     0,    51,    52,     0,    53,    54,     0,
      55,     0,     0,    56,    57,    58,    59,    60,    61,    62,
      63,    64,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    65,    66,    67,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   497,   123,   124,   125,   126,   127,
     128,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,   139,   140,   141,   142,   143,   144,   145,   146,     0,
       0,     0,   147,   148,   149,   391,   392,   393,   394,   154,
     155,   156,     0,     0,     0,     0,     0,   157,   158,   159,
     160,   395,   396,   397,   398,   165,    37,    38,   399,    40,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   167,   168,   169,
     170,   171,   172,   173,   174,   175,     0,     0,   176,   177,
       0,     0,   178,   179,   180,   181,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   182,   183,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   184,
     185,   186,   187,   188,   189,   190,   191,   192,   193,     0,
     194,   195,     0,     0,     0,     0,     0,   196,   400,   123,
     124,   125,   126,   127,   128,   129,   130,   131,   132,   133,
     134,   135,   136,   137,   138,   139,   140,   141,   142,   143,
     144,   145,   146,     0,     0,     0,   147,   148,   149,   150,
     151,   152,   153,   154,   155,   156,     0,     0,     0,     0,
       0,   157,   158,   159,   160,   161,   162,   163,   164,   165,
     285,   286,   166,   287,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   167,   168,   169,   170,   171,   172,   173,   174,   175,
       0,     0,   176,   177,     0,     0,   178,   179,   180,   181,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   182,   183,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   184,   185,   186,   187,   188,   189,   190,
     191,   192,   193,     0,   194,   195,     0,     0,     0,     0,
       0,   196,   123,   124,   125,   126,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,   137,   138,   139,   140,
     141,   142,   143,   144,   145,   146,     0,     0,     0,   147,
     148,   149,   150,   151,   152,   153,   154,   155,   156,     0,
       0,     0,     0,     0,   157,   158,   159,   160,   161,   162,
     163,   164,   165,   238,     0,   166,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   167,   168,   169,   170,   171,   172,
     173,   174,   175,     0,     0,   176,   177,     0,     0,   178,
     179,   180,   181,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   182,   183,     0,     0,    57,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   184,   185,   186,   187,
     188,   189,   190,   191,   192,   193,     0,   194,   195,     0,
       0,     0,     0,     0,   196,   123,   124,   125,   126,   127,
     128,   129,   130,   131,   132,   133,   134,   135,   136,   137,
     138,   139,   140,   141,   142,   143,   144,   145,   146,     0,
       0,     0,   147,   148,   149,   150,   151,   152,   153,   154,
     155,   156,     0,     0,     0,     0,     0,   157,   158,   159,
     160,   161,   162,   163,   164,   165,     0,     0,   166,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   167,   168,   169,
     170,   171,   172,   173,   174,   175,     0,     0,   176,   177,
       0,     0,   178,   179,   180,   181,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   182,   183,     0,
       0,    57,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   184,
     185,   186,   187,   188,   189,   190,   191,   192,   193,     0,
     194,   195,     0,     0,     0,     0,     0,   196,   123,   124,
     125,   126,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,   139,   140,   141,   142,   143,   144,
     145,   146,     0,     0,     0,   147,   148,   149,   150,   151,
     152,   153,   154,   155,   156,     0,     0,     0,     0,     0,
     157,   158,   159,   160,   161,   162,   163,   164,   165,     0,
       0,   166,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     167,   168,   169,   170,   171,   172,   173,   174,   175,     0,
       0,   176,   177,     0,     0,   178,   179,   180,   181,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     182,   183,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   184,   185,   186,   187,   188,   189,   190,   191,
     192,   193,     0,   194,   195,     5,     6,     7,     0,     9,
     196,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,   244,   245,    18,    19,     0,     0,     0,
       0,     0,    20,   246,   247,    23,    24,    25,    26,     0,
       0,   205,     0,     0,     0,     0,     0,     0,   274,     0,
       0,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,     0,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   275,     0,
       0,   208,    52,     0,    53,    54,     0,     0,     0,     0,
      56,    57,    58,    59,    60,    61,    62,    63,    64,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     8,     9,     0,     0,   276,
      10,    11,     0,     0,     0,    12,   520,    13,    14,    15,
      16,    17,    18,    19,     0,     0,     0,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,    27,     0,
       0,     0,     0,     0,    28,    29,    30,    31,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,     0,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,     0,     0,    51,    52,
       0,    53,    54,     0,    55,     0,     0,    56,    57,    58,
      59,    60,    61,    62,    63,    64,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    65,    66,    67,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
       0,     0,     0,    20,    21,    22,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,   206,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   207,
       0,     0,   208,    52,     0,    53,    54,     0,   209,   210,
     211,    56,    57,   212,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     8,     9,     0,     0,     0,    10,    11,
      65,   213,    67,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,    27,     0,     0,     0,
       0,     0,    28,    29,     0,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
       0,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,     0,     0,    51,    52,     0,    53,
      54,     0,    55,     0,     0,    56,    57,    58,    59,    60,
      61,    62,    63,    64,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,    65,    66,    67,    12,     0,    13,
      14,    15,   244,   245,    18,    19,     0,     0,     0,     0,
       0,    20,   246,   247,    23,    24,    25,    26,     0,     0,
     205,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,   206,
      41,    42,    43,    44,     0,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   207,     0,     0,
     208,    52,     0,    53,    54,     0,   615,   210,   211,    56,
      57,   212,    59,    60,    61,    62,    63,    64,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    65,   213,
      67,    12,     0,    13,    14,    15,   244,   245,    18,    19,
       0,     0,     0,     0,     0,    20,   246,   247,    23,    24,
      25,    26,     0,     0,   205,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,   206,    41,    42,    43,    44,     0,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   207,     0,     0,   208,    52,     0,    53,    54,     0,
     209,   210,     0,    56,    57,   212,    59,    60,    61,    62,
      63,    64,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    65,   213,    67,    12,     0,    13,    14,    15,
     244,   245,    18,    19,     0,     0,     0,     0,     0,    20,
     246,   247,    23,    24,    25,    26,     0,     0,   205,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,   206,    41,    42,
      43,    44,     0,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   207,     0,     0,   208,    52,
       0,    53,    54,     0,     0,   210,   211,    56,    57,   212,
      59,    60,    61,    62,    63,    64,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    65,   213,    67,    12,
       0,    13,    14,    15,   244,   245,    18,    19,     0,     0,
       0,     0,     0,    20,   246,   247,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,   206,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   207,
       0,     0,   208,    52,     0,    53,    54,     0,   615,   210,
       0,    56,    57,   212,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      65,   213,    67,    12,     0,    13,    14,    15,   244,   245,
      18,    19,     0,     0,     0,     0,     0,    20,   246,   247,
      23,    24,    25,    26,     0,     0,   205,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,   206,    41,    42,    43,    44,
       0,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   207,     0,     0,   208,    52,     0,    53,
      54,     0,     0,   210,     0,    56,    57,   212,    59,    60,
      61,    62,    63,    64,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,    65,   213,    67,    12,     0,    13,
      14,    15,    16,    17,    18,    19,     0,     0,     0,     0,
       0,    20,    21,    22,    23,    24,    25,    26,     0,     0,
     205,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,     0,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   207,     0,     0,
     208,    52,     0,    53,    54,     0,   513,     0,     0,    56,
      57,    58,    59,    60,    61,    62,    63,    64,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    65,   213,
      67,    12,     0,    13,    14,    15,   244,   245,    18,    19,
       0,     0,     0,     0,     0,    20,   246,   247,    23,    24,
      25,    26,     0,     0,   205,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,     0,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   207,     0,     0,   208,    52,     0,    53,    54,     0,
     209,     0,     0,    56,    57,    58,    59,    60,    61,    62,
      63,    64,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    65,   213,    67,    12,     0,    13,    14,    15,
     244,   245,    18,    19,     0,     0,     0,     0,     0,    20,
     246,   247,    23,    24,    25,    26,     0,     0,   205,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,     0,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   207,     0,     0,   208,    52,
       0,    53,    54,     0,   809,     0,     0,    56,    57,    58,
      59,    60,    61,    62,    63,    64,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    65,   213,    67,    12,
       0,    13,    14,    15,   244,   245,    18,    19,     0,     0,
       0,     0,     0,    20,   246,   247,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   207,
       0,     0,   208,    52,     0,    53,    54,     0,   513,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
      65,   213,    67,    12,     0,    13,    14,    15,   244,   245,
      18,    19,     0,     0,     0,     0,     0,    20,   246,   247,
      23,    24,    25,    26,     0,     0,   205,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
       0,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   207,     0,     0,   208,    52,     0,    53,
      54,     0,   615,     0,     0,    56,    57,    58,    59,    60,
      61,    62,    63,    64,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,    65,   213,    67,    12,     0,    13,
      14,    15,   244,   245,    18,    19,     0,     0,     0,     0,
       0,    20,   246,   247,    23,    24,    25,    26,     0,     0,
     205,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,     0,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   207,     0,     0,
     208,    52,     0,    53,    54,     0,     0,     0,     0,    56,
      57,    58,    59,    60,    61,    62,    63,    64,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,    65,   213,
      67,    12,     0,    13,    14,    15,    16,    17,    18,    19,
       0,     0,     0,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,   205,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,     0,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   207,     0,     0,   208,    52,     0,    53,    54,     0,
       0,     0,     0,    56,    57,    58,    59,    60,    61,    62,
      63,    64,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,    65,   213,    67,    12,     0,    13,    14,    15,
      16,    17,    18,    19,     0,     0,     0,     0,     0,    20,
      21,    22,    23,    24,    25,    26,     0,     0,    27,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,     0,    41,    42,
      43,    44,     0,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   207,     0,     0,   208,    52,
       0,    53,    54,     0,     0,     0,     0,    56,    57,    58,
      59,    60,    61,    62,    63,    64,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,    65,    66,    67,    12,
       0,    13,    14,    15,   244,   245,    18,    19,     0,     0,
       0,     0,     0,    20,   246,   247,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,   274,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   275,
       0,     0,   325,    52,     0,    53,    54,     0,   326,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
     276,    13,    14,    15,   244,   245,    18,    19,     0,     0,
       0,     0,     0,    20,   246,   247,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,   274,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   372,
       0,     0,    51,    52,     0,    53,    54,     0,    55,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
     276,    13,    14,    15,   244,   245,    18,    19,     0,     0,
       0,     0,     0,    20,   246,   247,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,   274,
       0,     0,    32,    33,    34,   380,    36,    37,    38,   381,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   382,     0,     0,     0,   383,
       0,     0,   208,    52,     0,    53,    54,     0,     0,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
     276,    13,    14,    15,   244,   245,    18,    19,     0,     0,
       0,     0,     0,    20,   246,   247,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,   274,
       0,     0,    32,    33,    34,   380,    36,    37,    38,   381,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   383,
       0,     0,   208,    52,     0,    53,    54,     0,     0,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
     276,    13,    14,    15,   244,   245,    18,    19,     0,     0,
       0,     0,     0,    20,   246,   247,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,   274,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   275,
       0,     0,   325,    52,     0,    53,    54,     0,     0,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
     276,    13,    14,    15,   244,   245,    18,    19,     0,     0,
       0,     0,     0,    20,   246,   247,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,   274,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   900,
       0,     0,   208,    52,     0,    53,    54,     0,     0,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
       0,     0,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
     276,    13,    14,    15,   244,   245,    18,    19,     0,     0,
       0,     0,     0,    20,   246,   247,    23,    24,    25,    26,
       0,     0,   205,     0,     0,     0,     0,     0,     0,   274,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,     0,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   557,   558,   975,
       0,   559,   208,    52,     0,    53,    54,     0,     0,     0,
       0,    56,    57,    58,    59,    60,    61,    62,    63,    64,
     167,   168,   169,   170,   171,   172,   173,   174,   175,     0,
       0,   176,   177,     0,     0,   178,   179,   180,   181,     0,
     276,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     182,   183,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   184,   185,   186,   187,   188,   189,   190,   191,
     192,   193,     0,   194,   195,   565,   566,     0,     0,   567,
     196,   232,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   167,   168,
     169,   170,   171,   172,   173,   174,   175,     0,     0,   176,
     177,     0,     0,   178,   179,   180,   181,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   182,   183,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     184,   185,   186,   187,   188,   189,   190,   191,   192,   193,
       0,   194,   195,   619,   558,     0,     0,   620,   196,   232,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   167,   168,   169,   170,
     171,   172,   173,   174,   175,     0,     0,   176,   177,     0,
       0,   178,   179,   180,   181,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   182,   183,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   184,   185,
     186,   187,   188,   189,   190,   191,   192,   193,     0,   194,
     195,   622,   566,     0,     0,   623,   196,   232,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   167,   168,   169,   170,   171,   172,
     173,   174,   175,     0,     0,   176,   177,     0,     0,   178,
     179,   180,   181,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   182,   183,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   184,   185,   186,   187,
     188,   189,   190,   191,   192,   193,     0,   194,   195,   646,
     558,     0,     0,   647,   196,   232,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   167,   168,   169,   170,   171,   172,   173,   174,
     175,     0,     0,   176,   177,     0,     0,   178,   179,   180,
     181,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   182,   183,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   184,   185,   186,   187,   188,   189,
     190,   191,   192,   193,     0,   194,   195,   649,   566,     0,
       0,   650,   196,   232,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     167,   168,   169,   170,   171,   172,   173,   174,   175,     0,
       0,   176,   177,     0,     0,   178,   179,   180,   181,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     182,   183,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   184,   185,   186,   187,   188,   189,   190,   191,
     192,   193,     0,   194,   195,   715,   558,     0,     0,   716,
     196,   232,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   167,   168,
     169,   170,   171,   172,   173,   174,   175,     0,     0,   176,
     177,     0,     0,   178,   179,   180,   181,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   182,   183,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     184,   185,   186,   187,   188,   189,   190,   191,   192,   193,
       0,   194,   195,   718,   566,     0,     0,   719,   196,   232,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   167,   168,   169,   170,
     171,   172,   173,   174,   175,     0,     0,   176,   177,     0,
       0,   178,   179,   180,   181,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   182,   183,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   184,   185,
     186,   187,   188,   189,   190,   191,   192,   193,     0,   194,
     195,   724,   558,     0,     0,   725,   196,   232,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   167,   168,   169,   170,   171,   172,
     173,   174,   175,     0,     0,   176,   177,     0,     0,   178,
     179,   180,   181,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   182,   183,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   184,   185,   186,   187,
     188,   189,   190,   191,   192,   193,     0,   194,   195,   604,
     566,     0,     0,   605,   196,   232,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   167,   168,   169,   170,   171,   172,   173,   174,
     175,     0,     0,   176,   177,     0,     0,   178,   179,   180,
     181,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   182,   183,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   184,   185,   186,   187,   188,   189,
     190,   191,   192,   193,     0,   194,   195,   814,   558,     0,
       0,   815,   196,   232,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     167,   168,   169,   170,   171,   172,   173,   174,   175,     0,
       0,   176,   177,     0,     0,   178,   179,   180,   181,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     182,   183,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   184,   185,   186,   187,   188,   189,   190,   191,
     192,   193,     0,   194,   195,   817,   566,     0,     0,   818,
     196,   232,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   167,   168,
     169,   170,   171,   172,   173,   174,   175,     0,     0,   176,
     177,     0,     0,   178,   179,   180,   181,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   182,   183,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     184,   185,   186,   187,   188,   189,   190,   191,   192,   193,
       0,   194,   195,  1015,   558,     0,     0,  1016,   196,   232,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   167,   168,   169,   170,
     171,   172,   173,   174,   175,     0,     0,   176,   177,     0,
       0,   178,   179,   180,   181,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   182,   183,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   184,   185,
     186,   187,   188,   189,   190,   191,   192,   193,     0,   194,
     195,  1049,   558,     0,     0,  1050,   196,   232,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   167,   168,   169,   170,   171,   172,
     173,   174,   175,     0,     0,   176,   177,     0,     0,   178,
     179,   180,   181,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   182,   183,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   184,   185,   186,   187,
     188,   189,   190,   191,   192,   193,     0,   194,   195,  1052,
     566,     0,     0,  1053,   196,   232,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   167,   168,   169,   170,   171,   172,   173,   174,
     175,     0,     0,   176,   177,     0,     0,   178,   179,   180,
     181,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   182,   183,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   184,   185,   186,   187,   188,   189,
     190,   191,   192,   193,     0,   194,   195,   604,   566,     0,
       0,   605,   196,   232,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     167,   168,   169,   170,   171,   172,   173,   174,   175,     0,
       0,   176,   177,     0,     0,   178,   179,   180,   181,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     182,   183,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   184,   185,   186,   187,   188,   189,   190,   191,
     192,   193,     0,   194,   195,     0,     0,     0,     0,     0,
     196
};

static const yytype_int16 yycheck[] =
{
       2,   100,    90,    57,    69,    93,    87,   360,     2,     8,
       4,    22,    92,   328,    28,    27,    55,    29,    99,     8,
     268,     8,    51,   226,   272,   365,   580,     4,    93,    28,
     755,    16,    17,    81,   292,   293,    66,    93,   359,    28,
     361,    28,    81,    97,   706,     2,   421,     4,    65,   429,
      51,    53,    54,   621,   429,    94,    95,    96,    52,    16,
      17,   383,    58,    55,   476,    76,   879,    27,    53,    69,
      57,   651,   425,    37,    38,    16,    17,    63,   905,    13,
     648,    25,    25,   938,   936,   898,   407,   467,     0,    26,
      16,    17,   256,   666,    51,    52,   888,    26,    55,   410,
     411,   151,    29,   424,    13,   426,    16,    17,   520,    66,
      97,   909,    25,    98,   328,   793,   144,   141,    85,    22,
      68,    97,   141,   151,    81,   478,    25,    97,    63,    68,
     146,   452,    89,    90,   120,   151,    93,    94,    95,    96,
     221,    98,   994,    53,    54,   870,   871,    95,   124,   717,
     774,    56,    25,   107,   124,    94,    95,    98,   479,   727,
     598,   325,   129,    25,    13,   149,   124,   151,   144,   250,
      89,    90,    98,    76,    93,   799,   800,   131,  1033,   208,
     124,   761,    25,  1035,  1011,   120,   144,   151,    98,   149,
      25,   151,  1005,   258,   121,    25,   410,   411,   876,   280,
     144,   144,   258,   147,   141,  1018,  1058,   151,  1000,   146,
     144,   936,   141,   938,   243,   149,   212,   151,   220,   307,
     308,   309,   310,    13,   226,  1023,     9,   256,   150,   277,
     232,   144,    15,   586,   492,   438,   494,   146,   277,   241,
     149,    28,   151,   596,   229,   144,   231,   241,   816,   629,
      97,   208,    52,   628,   629,   256,    56,   150,   258,   276,
     254,   150,   274,    13,   926,   150,   587,   679,    13,   994,
     995,   144,   229,   595,   231,   232,   597,   124,   235,   367,
     237,   861,   144,   863,   241,   723,   243,   326,   229,   144,
     231,   232,    63,   323,   375,    13,   325,   254,   328,   256,
     149,   144,   151,   229,   369,   231,   879,    13,  1033,   144,
    1035,   146,   306,   369,   144,   888,   235,   311,   237,   229,
     277,   231,   124,    25,   325,   898,   950,   124,    13,   306,
      97,   684,   905,  1058,   326,    14,    15,   366,   323,   368,
     144,   110,   966,   328,   247,   248,   117,   151,   119,   306,
     307,   308,   309,   310,   311,   312,   313,   124,   360,   149,
      13,   151,   683,    13,   685,    97,   323,   666,   325,   326,
     808,   328,   926,   713,   361,    97,   687,   688,    25,   150,
     410,   411,   693,   694,   144,   761,   701,   146,   307,   308,
     309,   310,   124,   312,   313,   643,   146,   655,    56,   149,
     124,   151,   660,   360,   149,   362,   151,    25,    98,   366,
     367,   368,   144,    25,    68,   618,    15,   855,    17,   360,
     407,   144,    97,   425,   862,   382,    97,  1000,   476,    68,
     144,   149,   446,   151,    68,   437,   438,   476,  1011,   426,
      94,    95,   144,   149,   146,   151,   146,   446,   367,   124,
     531,    68,   146,   410,   411,   144,    95,   446,   361,   446,
      94,    95,   151,   382,   149,   452,   151,   681,   425,   144,
     146,    63,   520,   687,   688,   147,   478,   124,    95,   693,
     694,   520,    63,  1051,   425,   861,    67,   863,   868,   143,
      68,    68,   479,   868,   124,    97,   149,   144,   151,   149,
     147,   151,   459,   941,   151,   507,   124,    77,   588,   143,
      63,   421,   124,   578,    97,   826,    94,    95,    95,   476,
     148,   478,   124,   426,   690,   117,   144,   119,   146,   147,
      68,   697,   144,   151,   546,   147,   117,   478,   119,   151,
     743,   124,   144,    56,    68,   574,    68,   576,   142,   452,
      77,   562,   564,   991,   464,    37,    38,    95,   150,   570,
     144,   144,   104,   520,   117,   143,   119,   120,   570,   666,
     572,    95,    94,    95,    17,    68,   479,   917,   144,   776,
      97,   778,   922,   882,   586,   149,    52,   886,    54,    55,
      56,    57,    56,    25,   596,   606,   644,   582,   612,   898,
     587,    94,    95,   902,   606,   644,   905,   124,   907,   621,
     597,   147,   826,   612,   128,   144,   618,   574,   144,   576,
      68,   143,    68,   612,   146,   612,    68,   144,   144,   586,
     142,   679,   142,   732,   722,   637,   648,   144,   667,   596,
     679,   598,   599,    97,   142,   586,    94,    95,    94,    95,
     143,   681,    94,    95,   666,   596,   721,   687,   688,   562,
      10,    68,    68,   693,   694,    68,    68,   570,   144,    97,
     124,   673,    63,   144,  1027,   632,   144,   731,   144,    97,
     599,    97,   684,    44,   587,   124,   680,   644,    95,    95,
     144,   720,    95,    95,   597,   143,   124,   143,   685,   701,
     702,   143,   704,   606,   706,   717,   124,  1028,   124,     8,
     667,    13,  1011,   632,  1013,   727,   701,   150,   628,  1018,
      68,  1020,   679,   680,   681,   150,   117,   684,   119,   120,
     687,   688,   142,   636,   637,    77,   693,   694,     2,   144,
       4,   743,   126,   684,   731,    15,    94,    95,    97,    54,
     947,   948,    16,    17,   148,   952,   844,   954,   797,   956,
      65,    66,    97,   720,   146,   722,   723,  1066,   144,     2,
     772,     4,   774,    68,    52,   124,    54,    55,    56,    57,
      68,   793,   685,    16,    17,   882,    68,    51,    52,   124,
     124,   856,   134,   135,   136,   143,   826,   799,   800,    94,
      95,   898,    66,   722,   816,   902,    94,    95,   905,   838,
     907,   144,    94,    95,   843,   144,    52,   512,    51,    52,
     144,    26,    97,   852,   144,    89,    90,   144,    52,    93,
      52,    54,   834,    66,    98,    97,   410,   411,    26,   129,
     797,   124,  1039,  1040,  1041,  1042,   666,    68,   143,   124,
     144,   808,   144,   427,   428,   143,    89,    90,   897,   144,
      93,   143,   124,    68,   876,    98,   144,   235,   933,   826,
     144,   144,    97,    94,    95,  1072,   571,   916,   149,   453,
      68,   838,   577,    58,   579,    98,   843,   844,   583,    94,
      95,   920,   144,   261,   129,   852,   977,   265,   855,   124,
      56,    68,    77,    68,   666,   862,    94,    95,    54,    55,
     144,    57,   144,   942,  1011,   144,  1013,   147,   144,    65,
      66,  1018,   143,  1020,   926,   844,   144,    94,    95,    94,
      95,   106,   107,   144,   144,   144,   141,    63,   143,    56,
     897,   146,   651,   144,   208,   144,     9,   144,   950,    63,
     142,   146,   981,   141,   983,   143,   131,   666,   146,   916,
     972,   146,    63,   920,   966,   229,   144,   231,   232,  1066,
     144,   235,   144,   237,   118,   208,   143,   241,   143,   243,
     982,  1010,   241,   206,   941,   942,   209,   210,   211,   459,
     254,   117,   256,   119,   120,   455,   229,    94,   231,   232,
      96,    85,   235,   117,   237,   119,   120,   870,   241,    52,
     243,    54,    55,    56,    57,   606,   117,   712,   119,   120,
     702,   254,  1031,   256,   981,  1027,   983,  1029,   666,  1031,
     673,   508,  1023,   728,   991,    59,    60,    61,    62,  1051,
      26,  1028,   306,   307,   308,   309,   310,   311,   312,   313,
     733,  1029,   761,  1010,   321,   103,   802,   803,   982,   323,
     494,   325,    97,   431,   328,    82,    83,   435,   888,   934,
    1027,   891,   440,   306,   307,   308,   309,   310,   311,   312,
     313,   882,    68,   902,   898,   905,  1027,   907,   456,    -1,
     323,   879,   325,   461,    -1,   328,   360,    -1,   362,    -1,
     323,    -1,   366,   367,   368,   328,    -1,    -1,    94,    95,
     127,   128,    -1,   687,   688,    -1,    77,    -1,   382,   693,
     694,    -1,    -1,    -1,    -1,  1028,    -1,   360,    -1,   362,
      -1,    92,    93,   366,   367,   368,   898,    -1,    -1,    -1,
     902,    -1,    -1,   905,    -1,   907,   410,   411,    -1,   382,
     845,   519,   861,   848,   863,   141,    -1,   143,    -1,   854,
     146,   425,   857,    63,   738,   739,    -1,   741,   742,    -1,
     879,   132,   133,   134,   135,   136,    -1,   410,   411,   888,
    1000,    52,  1002,    54,    55,    56,    57,  1007,    -1,   898,
      -1,  1011,   425,  1013,    -1,   459,   905,   420,   421,    -1,
    1020,    40,    41,    42,    43,    44,   429,    52,    77,    54,
      55,    56,    57,    58,   478,   961,   962,   117,    -1,   119,
     120,   967,   122,   969,   970,    -1,   459,    -1,    99,    -1,
      -1,    -1,    77,   928,   105,    -1,    -1,   932,    -1,    -1,
    1060,   464,    -1,    -1,   467,   478,  1066,    -1,    -1,  1011,
      -1,  1013,   826,    -1,    99,    -1,  1018,    -1,  1020,    -1,
     105,   106,   107,   132,   133,   134,   135,   136,    -1,    -1,
      52,    -1,    54,    55,    56,    57,    58,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   131,    -1,    -1,   134,
     513,  1000,    -1,   867,    -1,    77,    -1,    -1,  1044,  1045,
    1046,  1047,  1011,   998,  1066,    -1,   151,    -1,    -1,    -1,
     574,    52,   576,    54,    55,    56,    57,    99,    -1,    -1,
      -1,    -1,   586,   105,   106,   107,    52,  1073,    54,    55,
      56,    57,   596,    -1,   598,   599,    -1,  1032,    -1,    -1,
      -1,   574,    52,   576,    54,    55,    56,    57,    58,   131,
      -1,    -1,   134,   586,    -1,    77,    -1,    -1,    99,   582,
      -1,   729,    -1,   596,   146,   598,   599,    77,   632,    -1,
      92,    93,    -1,    99,    -1,    -1,   744,    -1,    -1,   105,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    99,
     613,    -1,   615,    -1,    -1,    -1,   106,   107,    -1,   632,
      -1,    -1,    -1,   667,    -1,   628,   629,   129,   130,   131,
     132,   133,   134,   135,   136,    -1,   680,   681,    -1,    -1,
     684,   131,    -1,   687,   688,    -1,    -1,    -1,    -1,   693,
     694,    -1,    -1,    -1,   667,    -1,    -1,    -1,    52,    -1,
      54,    55,    56,    57,    58,    -1,    -1,   680,   681,    -1,
      -1,   684,    -1,   676,   687,   688,   720,    -1,   722,   723,
     693,   694,    -1,    77,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    16,    17,    -1,   701,    77,
      -1,    -1,    -1,    -1,    -1,    99,    -1,   720,    -1,   722,
     723,   105,   106,   107,    92,    93,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   874,    48,    49,    -1,
      -1,    -1,    53,    54,    -1,    -1,    -1,   131,    -1,    -1,
     134,    -1,    -1,    -1,    -1,    66,    67,    -1,    -1,    -1,
      -1,    -1,    -1,   131,   132,   133,   134,   135,   136,    -1,
      -1,    -1,    -1,    -1,   808,    -1,    -1,   770,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    98,    -1,    -1,
      -1,    -1,   826,   931,    -1,    -1,    -1,   935,    -1,    -1,
      -1,    -1,    -1,    -1,   838,   808,    -1,    -1,    -1,   843,
     844,    -1,    -1,    -1,    -1,    -1,   809,    -1,   852,    -1,
      -1,   855,    -1,   826,    -1,    -1,    -1,    -1,   862,    -1,
      -1,    -1,    -1,    -1,    -1,   838,    -1,    -1,    -1,    -1,
     843,   844,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   852,
      -1,    -1,   855,    -1,    -1,    -1,    -1,    -1,   851,   862,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     2,   868,     4,    -1,    77,    78,
      79,    80,    81,    82,    83,   878,   920,    86,    87,    -1,
      -1,    -1,    -1,    92,    93,   206,    -1,    -1,   209,   210,
     211,    -1,   213,    -1,    -1,    -1,    -1,   941,   942,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   920,   229,    -1,
     231,   232,    -1,    51,    52,    -1,    -1,    55,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,   941,   942,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   981,    -1,   983,
      -1,    -1,    -1,    81,    -1,    -1,    -1,   991,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    94,    95,    96,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1010,    -1,   981,    -1,
     983,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   991,    -1,
      -1,    -1,    -1,  1027,    -1,    -1,    -1,    -1,    -1,    -1,
      77,    78,    79,    80,    81,    82,    83,  1010,    -1,    86,
      87,    -1,   323,    -1,    -1,    92,    93,   328,   329,   330,
     331,   332,   333,    -1,  1027,   336,   337,   338,   339,   340,
     341,   342,   343,   344,    -1,    -1,   347,   348,   349,   350,
     351,   352,   353,   354,   355,   356,    -1,    -1,    -1,   360,
     127,   128,   129,   130,   131,   132,   133,   134,   135,   136,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     208,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   410,
     411,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   419,   420,
     421,    -1,    -1,   241,   425,   243,   427,   428,   429,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   254,    -1,   256,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   448,    -1,    -1,
      -1,    -1,   453,    -1,    -1,    -1,    -1,    -1,    -1,   277,
      -1,    -1,    -1,   464,    -1,    -1,   467,     2,    -1,     4,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   478,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   306,    -1,
      -1,    -1,    -1,   311,    -1,   496,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   325,   326,    -1,
     328,    -1,   513,    -1,    -1,    -1,    51,    52,    -1,    -1,
      55,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   362,    -1,    81,    -1,   366,    -1,
     368,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    94,
      95,    96,    97,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     2,    -1,     4,    -1,
      -1,   582,    -1,    -1,    -1,   586,    -1,    -1,    -1,    -1,
      -1,    -1,   410,   411,    -1,   596,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     2,    -1,     4,    -1,    -1,    -1,
      -1,    -1,   613,    -1,   615,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    51,    52,   628,   629,    55,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   459,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    51,    52,    81,    -1,    -1,   476,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    94,    95,
      96,    97,    -1,   208,    -1,   676,    -1,    -1,    -1,    -1,
     681,   682,    -1,   684,    -1,    -1,   687,   688,    -1,    -1,
      -1,    -1,   693,   694,    -1,    -1,    94,    -1,    -1,    -1,
     701,    -1,   520,    -1,    -1,    -1,   241,    -1,   243,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   254,
      -1,   256,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   738,   739,    -1,
     741,   742,   277,    -1,    77,    78,    79,    80,    81,    82,
      83,    84,    -1,    86,    87,    -1,   574,    -1,   576,    92,
      93,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   770,
      -1,   306,    -1,    -1,    -1,    -1,   311,    -1,    -1,    -1,
     598,    -1,   208,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     325,   326,    -1,    -1,   127,   128,   129,   130,   131,   132,
     133,   134,   135,   136,    -1,    -1,    -1,    -1,   809,    -1,
     208,    -1,    -1,    -1,    -1,   241,    -1,   243,   819,    -1,
      -1,    -1,    -1,    -1,    -1,   826,   644,   362,   254,    -1,
     256,   366,    -1,   368,    -1,   653,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   241,    -1,   243,    -1,    -1,    -1,   667,
     851,   277,    -1,    -1,    -1,    -1,   254,    -1,   256,    -1,
      -1,   679,   680,   681,    -1,    -1,   867,   868,    -1,   687,
     688,    -1,    -1,    -1,    -1,   693,   694,   878,    -1,    -1,
     306,    -1,    -1,    -1,    -1,   311,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   325,
     326,    -1,   720,    -1,    -1,   723,     0,    -1,   306,    -1,
       2,    -1,     4,   311,     8,     9,    10,    -1,    -1,    13,
      14,    15,    -1,    17,   459,    -1,    -1,   325,    -1,    -1,
     328,    -1,    26,    27,    -1,    -1,   362,    -1,    -1,    -1,
     366,   476,   368,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    51,
      52,    -1,    -1,    -1,   362,    -1,    -1,    -1,   366,    -1,
     368,    -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,   797,
      -1,    -1,    -1,    -1,    -1,   520,    -1,    -1,    -1,    -1,
     808,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      94,    95,    -1,    -1,    -1,    -1,    -1,    -1,   826,    -1,
      -1,    -1,   410,   411,    -1,    -1,    -1,    -1,    -1,    -1,
     838,    -1,    -1,    -1,   118,   843,  1027,    -1,    -1,    -1,
      -1,    -1,    -1,   459,   852,    -1,    -1,   855,    -1,   574,
      -1,   576,    -1,    -1,   862,    -1,    -1,   141,   142,    -1,
     476,    -1,   146,   147,    -1,   149,    -1,   151,    -1,    -1,
      -1,   459,    -1,   598,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   897,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   520,    -1,    -1,    -1,   916,    -1,
      -1,    -1,   920,    -1,    -1,    -1,    -1,    -1,    -1,   644,
      -1,    -1,    -1,    -1,    -1,    -1,   208,    -1,    -1,    -1,
      -1,    -1,    -1,   941,   942,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   667,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   679,   680,    -1,    -1,   574,   241,
     576,   243,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   254,   981,   256,   983,    -1,    -1,    -1,    -1,
      -1,    -1,   598,   991,    -1,    -1,   574,    -1,   576,    -1,
      -1,    -1,    -1,    -1,    -1,   720,    -1,    -1,   723,    -1,
      -1,    -1,  1010,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     598,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   306,    -1,    -1,    -1,   644,   311,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   325,    -1,    -1,   328,    -1,    -1,    -1,
      -1,   667,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   679,   680,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   797,    -1,    -1,    -1,    -1,    -1,    -1,   667,
     362,    -1,    -1,   808,   366,    -1,   368,    -1,    -1,    -1,
      -1,    -1,   680,   681,    -1,    -1,    -1,    -1,    -1,   687,
     688,    -1,    -1,    -1,   720,   693,   694,   723,    -1,    -1,
      -1,    -1,    -1,   838,    -1,    44,    -1,    -1,   843,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   852,   410,   411,
     855,    -1,   720,    -1,    -1,   723,    -1,   862,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    92,    93,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   897,    -1,    -1,    -1,    -1,   459,    -1,    -1,
      -1,   797,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   916,   808,    -1,    -1,   920,   125,    -1,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   144,   941,   942,    -1,    -1,
     808,    -1,   838,    -1,    -1,    -1,    -1,   843,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   852,    -1,   826,   855,
      -1,    -1,    -1,    -1,    -1,    -1,   862,    -1,    -1,    -1,
     838,    -1,    -1,    -1,    -1,   843,   981,    -1,   983,    -1,
      -1,    -1,    -1,    -1,   852,    -1,   991,   855,    -1,    -1,
      -1,    -1,    -1,    -1,   862,    -1,    -1,    -1,    -1,    -1,
      -1,   897,    -1,    -1,    -1,  1010,    -1,    -1,    -1,    -1,
      -1,    -1,   574,    -1,   576,    -1,    -1,    -1,    -1,    -1,
     916,    -1,    -1,    -1,   920,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   598,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   941,   942,    -1,   916,    -1,
      -1,    -1,   920,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   941,   942,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   981,    -1,   983,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   991,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   667,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   981,  1010,   983,    -1,    -1,   680,   681,
       3,     4,     5,   991,     7,   687,   688,    -1,    11,    12,
      -1,   693,   694,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,  1010,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,   720,    -1,
      -1,   723,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      -1,    64,    65,    66,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    96,    -1,    -1,    99,   100,    -1,   102,
     103,    -1,    -1,    -1,    -1,   108,   109,   110,   111,   112,
     113,   114,   115,   116,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     0,    -1,    -1,    -1,   808,    -1,    -1,    -1,
       8,     9,    10,    -1,   137,    13,    14,    15,    -1,    17,
      -1,   144,    -1,    -1,   826,    -1,    -1,    -1,    26,    27,
      28,    29,    -1,    -1,    -1,    -1,   838,    -1,    -1,    37,
      38,   843,    40,    41,    42,    43,    44,    -1,    -1,    -1,
     852,    -1,    -1,   855,    -1,    -1,    -1,    -1,    -1,    -1,
     862,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      68,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    -1,    -1,    92,    93,    94,    95,    -1,    97,
      98,    -1,    -1,    -1,    -1,    -1,   104,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   920,    -1,
     118,    -1,    -1,   121,    -1,    -1,   124,   125,   126,   127,
     128,   129,   130,   131,   132,   133,   134,   135,   136,   941,
     942,    -1,    -1,   141,   142,   143,   144,    -1,    -1,   147,
     148,   149,    -1,   151,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     0,     1,    -1,     3,     4,     5,
       6,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,   981,
      16,   983,    18,    19,    20,    21,    22,    23,    24,   991,
      -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,  1010,    45,
      46,    47,    48,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    -1,    64,    65,
      66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      96,    -1,    -1,    99,   100,    -1,   102,   103,    -1,   105,
      -1,    -1,   108,   109,   110,   111,   112,   113,   114,   115,
     116,     0,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     8,
       9,    10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,
      -1,   137,   138,   139,    -1,    -1,    25,    -1,    27,    28,
      29,    -1,    -1,   149,    -1,   151,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    68,
      -1,    92,    93,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    92,    93,    94,    95,    -1,    97,    98,
      -1,    -1,    -1,    -1,   125,   104,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,    -1,    -1,    -1,   118,
      -1,    -1,   121,    -1,    -1,   124,   125,   126,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,    -1,
      -1,    -1,    -1,   142,   143,   144,     0,    -1,   147,   148,
     149,    -1,   151,    -1,     8,     9,    10,    -1,    -1,    13,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    44,    -1,
      -1,    25,    -1,    27,    28,    29,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    77,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    68,    -1,    92,    93,    -1,    -1,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    -1,    -1,    92,    93,
      94,    95,    -1,    97,    98,    -1,    -1,    -1,    -1,   125,
     104,   127,   128,   129,   130,   131,   132,   133,   134,   135,
     136,    -1,    -1,    -1,   118,    -1,    -1,   121,    -1,    -1,
     124,   125,   126,   127,   128,   129,   130,   131,   132,   133,
     134,   135,   136,    -1,    -1,    -1,    -1,    -1,   142,   143,
     144,     0,    -1,   147,   148,   149,    -1,   151,    -1,     8,
       9,    10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    26,    27,    28,
      29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    77,    78,    79,    80,
      81,    82,    83,    84,    85,    86,    87,    88,    89,    68,
      -1,    92,    93,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    92,    93,    94,    95,    -1,    -1,    98,
      -1,    -1,    -1,    -1,    -1,   104,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,    -1,    -1,    -1,   118,
      -1,    -1,   121,    -1,    -1,    -1,   125,   126,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,    -1,
      -1,    -1,   141,   142,   143,   144,     0,    -1,   147,   148,
     149,    -1,   151,    -1,     8,     9,    10,    -1,    -1,    13,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    26,    27,    28,    29,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    98,    -1,    -1,    -1,    -1,    -1,
     104,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   118,    -1,    -1,   121,    -1,    -1,
      -1,   125,   126,   127,   128,   129,   130,   131,   132,   133,
     134,   135,   136,    -1,    -1,    -1,    -1,   141,   142,   143,
     144,     0,    -1,   147,   148,   149,    -1,   151,    -1,     8,
       9,    10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    27,    28,
      29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    92,    93,    94,    95,    -1,    97,    98,
      -1,    -1,    -1,    -1,    -1,   104,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   118,
      -1,    -1,   121,    -1,    -1,   124,   125,   126,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,    -1,
      -1,    -1,    -1,   142,   143,   144,     0,    -1,   147,   148,
     149,    -1,   151,    -1,     8,     9,    10,    -1,    -1,    13,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    26,    27,    28,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    98,    -1,    -1,    -1,    -1,    -1,
     104,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   118,    -1,    -1,    -1,    -1,    -1,
      -1,   125,    -1,   127,   128,   129,   130,   131,   132,   133,
     134,   135,   136,    -1,    -1,    -1,    -1,   141,   142,   143,
     144,     0,   146,   147,   148,   149,    -1,   151,    -1,     8,
       9,    10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    27,    28,
      29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    92,    93,    94,    95,    -1,    -1,    98,
      -1,    -1,    -1,    -1,    -1,   104,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   118,
      -1,    -1,   121,    -1,    -1,    -1,   125,   126,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,    -1,
      -1,    -1,    -1,   142,   143,   144,     0,    -1,   147,   148,
     149,    -1,   151,    -1,     8,     9,    10,    -1,    -1,    13,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    26,    27,    28,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    -1,    -1,    92,    93,
      94,    95,    -1,    -1,    98,    -1,    -1,    -1,    -1,    -1,
     104,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   118,    -1,    -1,    -1,    -1,    -1,
      -1,   125,    -1,   127,   128,   129,   130,   131,   132,   133,
     134,   135,   136,    -1,    -1,    -1,    -1,   141,   142,   143,
     144,     0,   146,   147,   148,   149,    -1,   151,    -1,     8,
       9,    10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    27,    28,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    92,    93,    94,    95,    -1,    -1,    98,
      -1,    -1,    -1,    -1,    -1,   104,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   118,
      -1,    -1,    -1,    -1,    -1,    -1,   125,    -1,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,    -1,
      -1,    -1,    -1,   142,   143,   144,     0,   146,   147,   148,
     149,    -1,   151,    -1,     8,     9,    10,    -1,    -1,    -1,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    26,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    77,    78,    79,    80,    81,    82,
      83,    84,    85,    86,    87,    88,    89,    -1,    -1,    92,
      93,    -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    -1,    -1,    92,    93,
      94,    95,   125,    97,   127,   128,   129,   130,   131,   132,
     133,   134,   135,   136,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   118,    -1,    -1,    -1,   151,    -1,
     124,   125,    -1,   127,   128,   129,   130,   131,   132,   133,
     134,   135,   136,    -1,    -1,    -1,    -1,   141,   142,   143,
     144,     0,    -1,   147,    -1,   149,    -1,   151,    -1,     8,
       9,    10,    -1,    -1,    -1,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    26,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    92,    93,    94,    95,    -1,    97,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   118,
      -1,    -1,    -1,    -1,    -1,   124,   125,    -1,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,    -1,
      -1,    -1,   141,   142,   143,   144,     0,    -1,   147,    -1,
     149,    -1,   151,    -1,     8,     9,    10,    -1,    -1,    -1,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    77,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    -1,    -1,    92,    93,
      94,    95,    -1,    97,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   118,    -1,    -1,    -1,    -1,    -1,
     124,   125,    -1,   127,   128,   129,   130,   131,   132,   133,
     134,   135,   136,    -1,    -1,    -1,    -1,    -1,   142,   143,
     144,     0,    -1,   147,    -1,   149,    -1,   151,    -1,     8,
       9,    10,    -1,    -1,    -1,    14,    15,    -1,    17,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    77,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    -1,    -1,    92,    93,    94,    95,    -1,    97,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   118,
      -1,    -1,    -1,    -1,    -1,   124,   125,    -1,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,    -1,
      -1,    -1,    -1,   142,   143,   144,    -1,    -1,   147,    -1,
     149,     1,   151,     3,     4,     5,     6,     7,     8,     9,
      10,    11,    12,    -1,    -1,    15,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,
      30,    31,    32,    33,    34,    35,    36,    -1,    -1,    39,
      -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    -1,    64,    65,    66,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,    99,
     100,    -1,   102,   103,    -1,   105,    -1,    -1,   108,   109,
     110,   111,   112,   113,   114,   115,   116,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   137,   138,   139,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   149,
       1,   151,     3,     4,     5,     6,     7,    -1,    -1,    10,
      11,    12,    -1,    14,    15,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    -1,    64,    65,    66,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,    99,   100,
      -1,   102,   103,    -1,   105,    -1,    -1,   108,   109,   110,
     111,   112,   113,   114,   115,   116,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   137,   138,   139,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   149,     1,
     151,     3,     4,     5,     6,     7,    -1,    -1,    10,    11,
      12,    -1,    -1,    15,    16,    17,    18,    19,    20,    21,
      22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,
      -1,    -1,    -1,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    -1,    59,    60,    61,
      62,    -1,    64,    65,    66,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    96,    -1,    -1,    99,   100,    -1,
     102,   103,    -1,   105,    -1,    -1,   108,   109,   110,   111,
     112,   113,   114,   115,   116,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   137,   138,   139,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   149,     1,   151,
       3,     4,     5,     6,     7,    -1,    -1,    10,    11,    12,
      -1,    -1,    15,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    45,    46,    47,    48,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      -1,    64,    65,    66,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    96,    -1,    -1,    99,   100,    -1,   102,
     103,    -1,   105,    -1,    -1,   108,   109,   110,   111,   112,
     113,   114,   115,   116,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     1,    -1,     3,     4,     5,     6,     7,    -1,
       9,    10,    11,    12,   137,   138,   139,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,   149,    -1,   151,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    -1,    64,    65,    66,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,
      99,   100,    -1,   102,   103,    -1,   105,    -1,    -1,   108,
     109,   110,   111,   112,   113,   114,   115,   116,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     1,    -1,     3,     4,
       5,     6,     7,    -1,    -1,    -1,    11,    12,   137,   138,
     139,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
     149,    -1,   151,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    -1,    64,
      65,    66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    96,    -1,    -1,    99,   100,    -1,   102,   103,    -1,
     105,    -1,    -1,   108,   109,   110,   111,   112,   113,   114,
     115,   116,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   137,   138,   139,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   147,    -1,   149,     1,   151,     3,     4,     5,
       6,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    -1,
      -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    45,
      46,    47,    48,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    -1,    64,    65,
      66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,
      76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      96,    -1,    -1,    99,   100,    -1,   102,   103,    -1,   105,
      -1,    -1,   108,   109,   110,   111,   112,   113,   114,   115,
     116,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   137,   138,   139,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   147,    -1,   149,     1,   151,     3,     4,     5,     6,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,   105,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     137,   138,   139,    -1,    -1,   142,    -1,    -1,    -1,    -1,
      -1,    -1,   149,     1,   151,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    -1,    64,    65,    66,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,    -1,
      -1,    99,   100,    -1,   102,   103,    -1,   105,    -1,    -1,
     108,   109,   110,   111,   112,   113,   114,   115,   116,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   137,
     138,   139,    -1,    -1,   142,    -1,    -1,    -1,    -1,    -1,
      -1,   149,     1,   151,     3,     4,     5,     6,     7,    -1,
      -1,    10,    11,    12,    -1,    -1,    -1,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    -1,    64,    65,    66,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,
      99,   100,    -1,   102,   103,    -1,   105,    -1,    -1,   108,
     109,   110,   111,   112,   113,   114,   115,   116,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     1,    -1,     3,     4,
       5,     6,     7,    -1,    -1,    -1,    11,    12,   137,   138,
     139,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
     149,    -1,   151,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    -1,    64,
      65,    66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    96,    -1,    -1,    99,   100,    -1,   102,   103,    -1,
     105,    -1,    -1,   108,   109,   110,   111,   112,   113,   114,
     115,   116,    -1,   118,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,   137,   138,   139,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,   149,    -1,   151,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    58,    59,    60,
      61,    62,    -1,    64,    65,    66,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,    99,   100,
      -1,   102,   103,    -1,   105,   106,   107,   108,   109,   110,
     111,   112,   113,   114,   115,   116,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,   137,   138,   139,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
     151,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,    -1,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
     137,   138,   139,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,   149,    -1,   151,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      -1,    64,    65,    66,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    96,    -1,    -1,    99,   100,    -1,   102,
     103,    -1,    -1,    -1,    -1,   108,   109,   110,   111,   112,
     113,   114,   115,   116,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,     6,     7,    -1,
      -1,    -1,    11,    12,   137,   138,   139,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,   151,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    -1,    64,    65,    66,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,
      99,   100,    -1,   102,   103,    -1,   105,    -1,    -1,   108,
     109,   110,   111,   112,   113,   114,   115,   116,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,     6,     7,    -1,    -1,    -1,    11,    12,   137,   138,
     139,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
     149,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    -1,    64,
      65,    66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    96,    -1,    -1,    99,   100,    -1,   102,   103,    -1,
     105,    -1,    -1,   108,   109,   110,   111,   112,   113,   114,
     115,   116,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   137,   138,   139,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   149,     3,     4,     5,     6,     7,
       8,     9,    10,    11,    12,    13,    14,    15,    16,    17,
      18,    19,    20,    21,    22,    23,    24,    25,    26,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    77,
      78,    79,    80,    81,    82,    83,    -1,    -1,    86,    87,
      -1,    -1,    90,    91,    92,    93,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   105,   106,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   127,
     128,   129,   130,   131,   132,   133,   134,   135,   136,    -1,
     138,   139,    -1,    -1,    -1,    -1,    -1,   145,   146,     3,
       4,     5,     6,     7,     8,     9,    10,    11,    12,    13,
      14,    15,    16,    17,    18,    19,    20,    21,    22,    23,
      24,    25,    26,    -1,    -1,    -1,    30,    31,    32,    33,
      34,    35,    36,    37,    38,    39,    -1,    -1,    -1,    -1,
      -1,    45,    46,    47,    48,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    75,    76,    77,    78,    79,    80,    81,    82,    83,
      -1,    -1,    86,    87,    -1,    -1,    90,    91,    92,    93,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   105,   106,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   127,   128,   129,   130,   131,   132,   133,
     134,   135,   136,    -1,   138,   139,    -1,    -1,    -1,    -1,
      -1,   145,     3,     4,     5,     6,     7,     8,     9,    10,
      11,    12,    13,    14,    15,    16,    17,    18,    19,    20,
      21,    22,    23,    24,    25,    26,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    -1,
      -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,    50,
      51,    52,    53,    54,    -1,    56,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    77,    78,    79,    80,
      81,    82,    83,    -1,    -1,    86,    87,    -1,    -1,    90,
      91,    92,    93,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   105,   106,    -1,    -1,   109,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,    -1,   138,   139,    -1,
      -1,    -1,    -1,    -1,   145,     3,     4,     5,     6,     7,
       8,     9,    10,    11,    12,    13,    14,    15,    16,    17,
      18,    19,    20,    21,    22,    23,    24,    25,    26,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    -1,    -1,    56,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    77,
      78,    79,    80,    81,    82,    83,    -1,    -1,    86,    87,
      -1,    -1,    90,    91,    92,    93,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   105,   106,    -1,
      -1,   109,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   127,
     128,   129,   130,   131,   132,   133,   134,   135,   136,    -1,
     138,   139,    -1,    -1,    -1,    -1,    -1,   145,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    -1,    -1,    -1,    -1,    -1,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    -1,
      -1,    56,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    90,    91,    92,    93,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     105,   106,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,    -1,   138,   139,     3,     4,     5,    -1,     7,
     145,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,
      -1,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    -1,    64,    65,    66,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,    -1,
      -1,    99,   100,    -1,   102,   103,    -1,    -1,    -1,    -1,
     108,   109,   110,   111,   112,   113,   114,   115,   116,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,     6,     7,    -1,    -1,   137,
      11,    12,    -1,    -1,    -1,    16,   144,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    -1,    64,    65,    66,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,    99,   100,
      -1,   102,   103,    -1,   105,    -1,    -1,   108,   109,   110,
     111,   112,   113,   114,   115,   116,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,   137,   138,   139,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    58,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,   105,   106,
     107,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,     6,     7,    -1,    -1,    -1,    11,    12,
     137,   138,   139,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    45,    46,    -1,    48,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      -1,    64,    65,    66,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    96,    -1,    -1,    99,   100,    -1,   102,
     103,    -1,   105,    -1,    -1,   108,   109,   110,   111,   112,
     113,   114,   115,   116,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,
      -1,    -1,    11,    12,   137,   138,   139,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    58,
      59,    60,    61,    62,    -1,    64,    65,    66,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,
      99,   100,    -1,   102,   103,    -1,   105,   106,   107,   108,
     109,   110,   111,   112,   113,   114,   115,   116,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,   137,   138,
     139,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59,    60,    61,    62,    -1,    64,
      65,    66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    96,    -1,    -1,    99,   100,    -1,   102,   103,    -1,
     105,   106,    -1,   108,   109,   110,   111,   112,   113,   114,
     115,   116,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,   137,   138,   139,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    58,    59,    60,
      61,    62,    -1,    64,    65,    66,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,    99,   100,
      -1,   102,   103,    -1,    -1,   106,   107,   108,   109,   110,
     111,   112,   113,   114,   115,   116,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,   137,   138,   139,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    58,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,   105,   106,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
     137,   138,   139,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    58,    59,    60,    61,    62,
      -1,    64,    65,    66,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    96,    -1,    -1,    99,   100,    -1,   102,
     103,    -1,    -1,   106,    -1,   108,   109,   110,   111,   112,
     113,   114,   115,   116,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,
      -1,    -1,    11,    12,   137,   138,   139,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    -1,    64,    65,    66,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,
      99,   100,    -1,   102,   103,    -1,   105,    -1,    -1,   108,
     109,   110,   111,   112,   113,   114,   115,   116,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,   137,   138,
     139,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    -1,    64,
      65,    66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    96,    -1,    -1,    99,   100,    -1,   102,   103,    -1,
     105,    -1,    -1,   108,   109,   110,   111,   112,   113,   114,
     115,   116,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,   137,   138,   139,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    -1,    64,    65,    66,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,    99,   100,
      -1,   102,   103,    -1,   105,    -1,    -1,   108,   109,   110,
     111,   112,   113,   114,   115,   116,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,   137,   138,   139,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,   105,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
     137,   138,   139,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      -1,    64,    65,    66,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    96,    -1,    -1,    99,   100,    -1,   102,
     103,    -1,   105,    -1,    -1,   108,   109,   110,   111,   112,
     113,   114,   115,   116,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,
      -1,    -1,    11,    12,   137,   138,   139,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,
      -1,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    -1,    64,    65,    66,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,
      99,   100,    -1,   102,   103,    -1,    -1,    -1,    -1,   108,
     109,   110,   111,   112,   113,   114,   115,   116,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,   137,   138,
     139,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    -1,    64,
      65,    66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    96,    -1,    -1,    99,   100,    -1,   102,   103,    -1,
      -1,    -1,    -1,   108,   109,   110,   111,   112,   113,   114,
     115,   116,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,   137,   138,   139,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    59,    60,
      61,    62,    -1,    64,    65,    66,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    96,    -1,    -1,    99,   100,
      -1,   102,   103,    -1,    -1,    -1,    -1,   108,   109,   110,
     111,   112,   113,   114,   115,   116,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,   137,   138,   139,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,   105,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
     137,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,   105,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
     137,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    92,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,    -1,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
     137,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,    -1,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
     137,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,    -1,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
     137,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    96,
      -1,    -1,    99,   100,    -1,   102,   103,    -1,    -1,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
     137,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,    -1,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    -1,    64,    65,    66,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    52,    53,    96,
      -1,    56,    99,   100,    -1,   102,   103,    -1,    -1,    -1,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    90,    91,    92,    93,    -1,
     137,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     105,   106,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,    -1,   138,   139,    52,    53,    -1,    -1,    56,
     145,   146,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      77,    78,    79,    80,    81,    82,    83,    -1,    -1,    86,
      87,    -1,    -1,    90,    91,    92,    93,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   105,   106,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     127,   128,   129,   130,   131,   132,   133,   134,   135,   136,
      -1,   138,   139,    52,    53,    -1,    -1,    56,   145,   146,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    77,    78,
      79,    80,    81,    82,    83,    -1,    -1,    86,    87,    -1,
      -1,    90,    91,    92,    93,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   105,   106,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,   138,
     139,    52,    53,    -1,    -1,    56,   145,   146,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    77,    78,    79,    80,
      81,    82,    83,    -1,    -1,    86,    87,    -1,    -1,    90,
      91,    92,    93,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   105,   106,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,    -1,   138,   139,    52,
      53,    -1,    -1,    56,   145,   146,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    77,    78,    79,    80,    81,    82,
      83,    -1,    -1,    86,    87,    -1,    -1,    90,    91,    92,
      93,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   105,   106,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   127,   128,   129,   130,   131,   132,
     133,   134,   135,   136,    -1,   138,   139,    52,    53,    -1,
      -1,    56,   145,   146,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    90,    91,    92,    93,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     105,   106,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,    -1,   138,   139,    52,    53,    -1,    -1,    56,
     145,   146,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      77,    78,    79,    80,    81,    82,    83,    -1,    -1,    86,
      87,    -1,    -1,    90,    91,    92,    93,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   105,   106,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     127,   128,   129,   130,   131,   132,   133,   134,   135,   136,
      -1,   138,   139,    52,    53,    -1,    -1,    56,   145,   146,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    77,    78,
      79,    80,    81,    82,    83,    -1,    -1,    86,    87,    -1,
      -1,    90,    91,    92,    93,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   105,   106,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,   138,
     139,    52,    53,    -1,    -1,    56,   145,   146,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    77,    78,    79,    80,
      81,    82,    83,    -1,    -1,    86,    87,    -1,    -1,    90,
      91,    92,    93,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   105,   106,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,    -1,   138,   139,    52,
      53,    -1,    -1,    56,   145,   146,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    77,    78,    79,    80,    81,    82,
      83,    -1,    -1,    86,    87,    -1,    -1,    90,    91,    92,
      93,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   105,   106,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   127,   128,   129,   130,   131,   132,
     133,   134,   135,   136,    -1,   138,   139,    52,    53,    -1,
      -1,    56,   145,   146,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    90,    91,    92,    93,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     105,   106,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,    -1,   138,   139,    52,    53,    -1,    -1,    56,
     145,   146,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      77,    78,    79,    80,    81,    82,    83,    -1,    -1,    86,
      87,    -1,    -1,    90,    91,    92,    93,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   105,   106,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     127,   128,   129,   130,   131,   132,   133,   134,   135,   136,
      -1,   138,   139,    52,    53,    -1,    -1,    56,   145,   146,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    75,    76,    77,    78,
      79,    80,    81,    82,    83,    -1,    -1,    86,    87,    -1,
      -1,    90,    91,    92,    93,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   105,   106,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   127,   128,
     129,   130,   131,   132,   133,   134,   135,   136,    -1,   138,
     139,    52,    53,    -1,    -1,    56,   145,   146,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    75,    76,    77,    78,    79,    80,
      81,    82,    83,    -1,    -1,    86,    87,    -1,    -1,    90,
      91,    92,    93,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   105,   106,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   127,   128,   129,   130,
     131,   132,   133,   134,   135,   136,    -1,   138,   139,    52,
      53,    -1,    -1,    56,   145,   146,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    75,    76,    77,    78,    79,    80,    81,    82,
      83,    -1,    -1,    86,    87,    -1,    -1,    90,    91,    92,
      93,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   105,   106,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   127,   128,   129,   130,   131,   132,
     133,   134,   135,   136,    -1,   138,   139,    52,    53,    -1,
      -1,    56,   145,   146,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    -1,
      -1,    86,    87,    -1,    -1,    90,    91,    92,    93,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     105,   106,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,    -1,   138,   139,    -1,    -1,    -1,    -1,    -1,
     145
};

  /* YYSTOS[STATE-NUM] -- The (internal number of the) accessing
     symbol of state STATE-NUM.  */
static const yytype_uint16 yystos[] =
{
       0,   153,   154,     0,     1,     3,     4,     5,     6,     7,
      11,    12,    16,    18,    19,    20,    21,    22,    23,    24,
      30,    31,    32,    33,    34,    35,    36,    39,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    59,    60,    61,    62,    64,    65,    66,    75,    76,
      96,    99,   100,   102,   103,   105,   108,   109,   110,   111,
     112,   113,   114,   115,   116,   137,   138,   139,   155,   156,
     157,   165,   167,   169,   174,   175,   177,   178,   179,   181,
     182,   183,   185,   186,   196,   199,   214,   227,   228,   229,
     230,   231,   232,   233,   234,   235,   236,   237,   246,   270,
     271,   284,   285,   286,   287,   288,   289,   290,   293,   295,
     296,   310,   312,   313,   314,   315,   316,   317,   318,   319,
     353,   366,   157,     3,     4,     5,     6,     7,     8,     9,
      10,    11,    12,    13,    14,    15,    16,    17,    18,    19,
      20,    21,    22,    23,    24,    25,    26,    30,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    56,    75,    76,    77,
      78,    79,    80,    81,    82,    83,    86,    87,    90,    91,
      92,    93,   105,   106,   127,   128,   129,   130,   131,   132,
     133,   134,   135,   136,   138,   139,   145,   189,   190,   191,
     192,   194,   195,   310,   312,    39,    58,    96,    99,   105,
     106,   107,   110,   138,   178,   186,   196,   200,   206,   209,
     211,   227,   315,   316,   318,   319,   351,   352,   206,   146,
     207,   208,   146,   203,   207,   146,   151,   360,    54,   191,
     360,   141,   158,   141,    21,    22,    31,    32,   177,   196,
     227,   246,   196,    56,     1,    47,    99,   161,   162,   163,
     165,   180,   181,   366,   165,   216,   201,   211,   351,   366,
     200,   350,   351,   366,    46,    96,   137,   144,   185,   214,
     227,   315,   316,   319,   219,    54,    55,    57,   189,   299,
     311,   299,   300,   301,   150,   150,   150,   150,   314,   174,
     196,   196,   149,   151,   359,   364,   365,    40,    41,    42,
      43,    44,    37,    38,    26,   141,   203,   207,   238,   272,
      28,   239,   269,   124,   144,    99,   105,   182,   124,    77,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    92,    93,   125,   127,   128,   129,   130,   131,
     132,   133,   134,   135,   136,   198,   198,    68,    94,    95,
     143,   357,   215,   169,   170,   170,   171,   172,   171,   170,
     359,   365,    96,   179,   186,   227,   251,   315,   316,   319,
      52,    56,    92,    96,   187,   188,   227,   315,   316,   319,
     188,    33,    34,    35,    36,    49,    50,    51,    52,    56,
     146,   189,   317,   348,   206,    95,   357,   358,   272,   287,
      97,    97,   144,   200,    56,   200,   200,   200,   299,   124,
      98,   144,   210,   366,    95,   143,   357,    97,    97,   144,
     210,   206,   360,   361,   206,   205,   206,   211,   351,   366,
     169,   361,   169,    54,    65,    66,   166,   146,   197,   155,
     161,    95,   357,    97,   165,   164,   180,   147,   359,   365,
     361,   217,   361,   148,   144,   151,   363,   144,   363,   142,
     363,   360,    56,   314,   182,   184,   144,    95,   143,   357,
     263,   264,    63,   117,   119,   120,   302,   120,   120,   302,
      67,   302,   291,   297,   294,   298,    77,   149,   157,   170,
     170,   170,   170,   165,   169,   169,   273,   274,   104,   176,
     275,   276,   275,   105,   174,   200,   211,   212,   213,   180,
     144,   185,   144,   167,   168,   174,   186,   196,   200,   202,
     213,   227,   319,   196,   196,   196,   196,   196,   196,   196,
     196,   196,   196,   196,   196,   196,   196,   196,   196,   196,
     196,   196,   196,   196,   196,   196,   196,    52,    53,    56,
     194,   203,   354,   355,   205,    52,    53,    56,   194,   203,
     354,   159,   161,    13,   247,   364,   247,   161,   170,   161,
     359,    17,   242,   278,    56,    95,   143,   357,    25,   169,
      52,    56,   187,   128,   320,    95,   143,   357,   222,   349,
     223,    68,    95,   356,    52,    56,   354,   168,   196,   202,
     168,   202,   193,   122,   200,   105,   200,   209,   351,    52,
      56,   205,    52,    56,   352,   361,   147,   361,   144,   144,
     361,   191,   218,   196,   142,   142,   354,   354,   202,   158,
     361,   163,   361,   351,   144,   184,    52,    56,   205,    52,
      56,   265,   304,   303,   120,   292,   302,    63,   120,   120,
     292,    63,   120,   196,   142,    85,   129,   258,   259,   366,
     273,    10,   245,   277,   245,   200,   144,   361,   184,   144,
      44,   124,    44,    95,   143,   357,   360,    97,    97,   203,
     207,   360,   362,    97,    97,   203,   204,   207,   366,   245,
       8,   240,   280,   366,   161,    13,   161,   245,    27,   248,
     364,   245,   278,   211,   245,    52,    56,   205,    52,    56,
     171,   221,   321,   220,    52,    56,   187,   205,   159,   169,
     224,   225,   204,   207,   191,   200,   200,   210,    97,    97,
     362,    97,    97,   351,   169,   363,   182,   362,    52,    54,
      55,    56,    57,    58,    77,    99,   105,   106,   107,   131,
     134,   146,   267,   324,   326,   327,   328,   329,   330,   331,
     332,   333,   336,   337,   338,   339,   342,   343,   344,   345,
     346,   305,    54,    55,    57,   309,   319,   150,   302,   150,
     150,   150,   255,   257,   260,   329,   331,   332,   334,   335,
     338,   340,   341,   344,   346,   360,   161,   142,   258,   105,
     200,   184,   165,   196,    52,    56,   205,    52,    56,   126,
     168,   202,   168,   202,   176,   148,    97,   168,   202,   168,
     202,   176,   200,   213,   281,   366,     9,    15,   241,   243,
     283,   366,    14,   243,   244,   249,   250,   366,   250,   173,
     245,   144,   247,   362,   161,   364,   170,   159,   362,   245,
     361,   146,   322,   323,   189,   272,   269,    97,   144,   361,
      99,   105,   252,   253,   254,   328,   326,   266,   124,   144,
     325,   200,   144,   347,   366,    52,   144,   347,   144,   325,
      52,   144,   325,    52,   306,   260,   129,   124,   144,   256,
      96,   227,   144,   347,   347,   144,   256,   144,   256,   149,
     159,   200,   362,   196,   168,   202,    98,   282,   366,   161,
     160,   161,   170,   245,   245,   200,   161,   245,   159,   364,
     245,   326,   159,   326,   226,   254,   144,   328,   144,   361,
     260,    29,   121,   268,   200,   324,   330,   342,   344,   333,
     338,   346,   331,   339,   344,   329,   331,   307,   129,   227,
     255,   341,   344,    56,    95,   334,   338,   331,   340,   344,
     331,    52,   261,   262,   327,    96,   186,   227,   315,   316,
     319,   247,   161,   247,   250,   278,   279,   245,   361,   245,
     364,   322,   361,   253,   144,   105,   252,   147,   159,   161,
     144,   325,   144,   325,   347,   144,   325,   144,   325,   325,
     308,   144,   256,   144,   256,    52,    56,   347,   144,   256,
     144,   256,   256,   144,   360,    56,    95,   143,   357,   161,
     283,   161,   159,   144,   253,   144,   328,   245,   142,   331,
     344,   331,   331,   161,   331,   344,   331,   331,   262,    52,
      56,   205,    52,    56,   280,   249,   245,   253,   144,   325,
     144,   325,   325,   325,   118,   256,   144,   256,   256,   256,
     362,   253,   331,   331,   325,   256
};

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
static const yytype_uint16 yyr1[] =
{
       0,   152,   154,   153,   155,   156,   156,   156,   156,   157,
     157,   158,   160,   159,   159,   161,   162,   162,   162,   162,
     163,   164,   163,   166,   165,   165,   165,   165,   165,   165,
     165,   165,   165,   165,   165,   165,   165,   165,   165,   165,
     167,   167,   167,   167,   167,   167,   167,   167,   168,   168,
     168,   169,   169,   169,   169,   169,   169,   170,   172,   173,
     171,   174,   174,   175,   175,   176,   177,   178,   178,   178,
     178,   178,   178,   178,   178,   178,   178,   178,   179,   179,
     180,   180,   181,   181,   181,   181,   181,   181,   181,   181,
     181,   181,   182,   182,   183,   183,   184,   184,   185,   185,
     185,   185,   185,   185,   185,   185,   185,   186,   186,   186,
     186,   186,   186,   186,   186,   186,   187,   187,   188,   188,
     188,   189,   189,   189,   189,   189,   190,   190,   191,   191,
     192,   193,   192,   194,   194,   194,   194,   194,   194,   194,
     194,   194,   194,   194,   194,   194,   194,   194,   194,   194,
     194,   194,   194,   194,   194,   194,   194,   194,   194,   194,
     194,   194,   194,   195,   195,   195,   195,   195,   195,   195,
     195,   195,   195,   195,   195,   195,   195,   195,   195,   195,
     195,   195,   195,   195,   195,   195,   195,   195,   195,   195,
     195,   195,   195,   195,   195,   195,   195,   195,   195,   195,
     195,   195,   195,   195,   196,   196,   196,   196,   196,   196,
     196,   196,   196,   196,   196,   196,   196,   196,   196,   196,
     196,   196,   196,   196,   196,   196,   196,   196,   196,   196,
     196,   196,   196,   196,   196,   196,   196,   196,   196,   196,
     196,   196,   197,   196,   196,   196,   198,   198,   198,   198,
     199,   199,   200,   201,   201,   201,   201,   202,   202,   203,
     204,   204,   205,   205,   205,   205,   205,   206,   206,   206,
     206,   206,   208,   207,   209,   210,   210,   211,   211,   211,
     211,   212,   212,   213,   213,   213,   214,   214,   214,   214,
     214,   214,   214,   214,   214,   214,   214,   215,   214,   216,
     214,   217,   214,   214,   214,   214,   214,   214,   214,   214,
     214,   214,   218,   214,   214,   214,   214,   214,   214,   219,
     214,   214,   214,   214,   214,   214,   214,   214,   220,   214,
     221,   214,   222,   214,   223,   224,   214,   225,   226,   214,
     214,   214,   214,   214,   227,   228,   229,   230,   231,   232,
     233,   234,   235,   236,   237,   238,   239,   240,   241,   242,
     243,   244,   245,   246,   247,   247,   247,   248,   248,   249,
     249,   250,   250,   251,   251,   252,   252,   253,   253,   254,
     254,   254,   254,   254,   254,   254,   254,   254,   255,   255,
     255,   255,   256,   256,   257,   257,   257,   257,   257,   257,
     257,   257,   257,   257,   257,   257,   257,   257,   257,   258,
     258,   259,   259,   259,   260,   260,   261,   261,   262,   262,
     264,   265,   266,   263,   267,   267,   268,   268,   269,   270,
     270,   270,   270,   271,   271,   271,   271,   271,   271,   271,
     271,   271,   272,   272,   274,   273,   276,   277,   275,   278,
     279,   279,   280,   280,   281,   281,   281,   282,   282,   283,
     283,   284,   284,   284,   285,   286,   286,   286,   287,   288,
     289,   290,   291,   291,   292,   292,   293,   294,   294,   295,
     296,   297,   297,   298,   298,   299,   299,   300,   300,   301,
     301,   302,   303,   302,   304,   305,   306,   307,   308,   302,
     309,   309,   309,   309,   310,   311,   311,   311,   311,   312,
     313,   313,   314,   314,   314,   314,   315,   315,   315,   315,
     315,   316,   316,   316,   316,   316,   316,   316,   317,   317,
     318,   318,   319,   319,   321,   320,   320,   322,   323,   322,
     324,   324,   324,   324,   325,   325,   326,   326,   326,   326,
     326,   326,   326,   326,   326,   326,   326,   326,   326,   326,
     326,   327,   327,   327,   327,   328,   328,   329,   330,   330,
     331,   331,   332,   333,   333,   334,   334,   335,   335,   336,
     336,   337,   337,   338,   338,   339,   340,   341,   341,   342,
     342,   343,   343,   344,   344,   345,   345,   346,   347,   347,
     348,   349,   348,   350,   350,   351,   351,   352,   352,   352,
     352,   353,   353,   353,   354,   354,   354,   354,   355,   355,
     355,   356,   356,   357,   357,   358,   358,   359,   359,   360,
     360,   361,   362,   363,   363,   363,   364,   364,   365,   365,
     366
};

  /* YYR2[YYN] -- Number of symbols on the right hand side of rule YYN.  */
static const yytype_uint8 yyr2[] =
{
       0,     2,     0,     2,     2,     1,     1,     3,     2,     1,
       2,     3,     0,     6,     3,     2,     1,     1,     3,     2,
       1,     0,     3,     0,     4,     3,     3,     3,     2,     3,
       3,     3,     3,     3,     4,     1,     3,     3,     3,     1,
       3,     3,     6,     5,     5,     5,     5,     3,     1,     3,
       1,     1,     3,     3,     3,     2,     1,     1,     0,     0,
       4,     1,     1,     1,     4,     3,     1,     2,     3,     4,
       5,     4,     5,     2,     2,     2,     2,     2,     1,     3,
       1,     3,     1,     2,     3,     5,     2,     4,     2,     4,
       1,     3,     1,     3,     2,     3,     1,     3,     1,     1,
       4,     3,     3,     3,     3,     2,     1,     1,     1,     4,
       3,     3,     3,     3,     2,     1,     1,     1,     2,     1,
       3,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     0,     4,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     3,     3,     6,     5,     5,     5,
       5,     4,     3,     3,     3,     2,     2,     3,     3,     3,
       3,     3,     3,     4,     2,     2,     3,     3,     3,     3,
       1,     3,     3,     3,     3,     3,     2,     2,     3,     3,
       3,     3,     0,     4,     6,     1,     1,     1,     1,     1,
       3,     3,     1,     1,     2,     4,     2,     1,     3,     3,
       1,     1,     1,     1,     2,     4,     2,     1,     2,     2,
       4,     1,     0,     2,     2,     2,     1,     1,     2,     3,
       4,     1,     1,     3,     4,     2,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     0,     4,     0,
       3,     0,     4,     3,     3,     2,     3,     3,     1,     4,
       3,     1,     0,     6,     4,     3,     2,     1,     2,     0,
       3,     6,     6,     4,     4,     5,     4,     6,     0,     6,
       0,     7,     0,     5,     0,     0,     7,     0,     0,     9,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     2,     1,     1,     1,
       5,     1,     2,     1,     1,     1,     3,     1,     3,     1,
       4,     6,     3,     5,     2,     4,     1,     3,     4,     2,
       2,     1,     2,     0,     6,     8,     4,     6,     4,     2,
       6,     2,     4,     6,     2,     4,     2,     4,     1,     1,
       1,     3,     1,     4,     1,     4,     1,     3,     1,     1,
       0,     0,     0,     5,     4,     1,     3,     3,     3,     2,
       4,     5,     5,     2,     4,     4,     3,     3,     3,     2,
       1,     4,     3,     3,     0,     3,     0,     0,     4,     5,
       1,     1,     6,     1,     1,     1,     1,     2,     1,     2,
       1,     1,     1,     1,     1,     1,     1,     2,     3,     3,
       3,     4,     0,     3,     1,     2,     4,     0,     3,     4,
       4,     0,     3,     0,     3,     0,     2,     0,     2,     0,
       2,     1,     0,     3,     0,     0,     0,     0,     0,     8,
       1,     1,     1,     1,     2,     1,     1,     1,     1,     3,
       1,     2,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     0,     4,     0,     3,     0,     3,
       4,     2,     2,     1,     2,     0,     6,     8,     4,     6,
       4,     6,     2,     4,     6,     2,     4,     2,     4,     1,
       0,     1,     1,     1,     1,     1,     1,     1,     1,     3,
       1,     3,     1,     2,     1,     2,     1,     1,     3,     1,
       3,     1,     1,     2,     1,     3,     3,     1,     3,     1,
       3,     1,     1,     2,     1,     1,     1,     2,     2,     1,
       1,     0,     4,     1,     2,     1,     3,     3,     2,     4,
       2,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     0,     1,     0,
       1,     2,     2,     0,     1,     1,     1,     1,     1,     2,
       0
};


#define yyerrok         (yyerrstatus = 0)
#define yyclearin       (yychar = YYEMPTY)
#define YYEMPTY         (-2)
#define YYEOF           0

#define YYACCEPT        goto yyacceptlab
#define YYABORT         goto yyabortlab
#define YYERROR         goto yyerrorlab


#define YYRECOVERING()  (!!yyerrstatus)

#define YYBACKUP(Token, Value)                                  \
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
static unsigned
yy_location_print_ (FILE *yyo, YYLTYPE const * const yylocp)
{
  unsigned res = 0;
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


/*----------------------------------------.
| Print this symbol's value on YYOUTPUT.  |
`----------------------------------------*/

static void
yy_symbol_value_print (FILE *yyoutput, int yytype, YYSTYPE const * const yyvaluep, YYLTYPE const * const yylocationp, struct parser_params *p)
{
  FILE *yyo = yyoutput;
  YYUSE (yyo);
  YYUSE (yylocationp);
  YYUSE (p);
  if (!yyvaluep)
    return;
# ifdef YYPRINT
  if (yytype < YYNTOKENS)
    YYPRINT (yyoutput, yytoknum[yytype], *yyvaluep);
# endif
  YYUSE (yytype);
}


/*--------------------------------.
| Print this symbol on YYOUTPUT.  |
`--------------------------------*/

static void
yy_symbol_print (FILE *yyoutput, int yytype, YYSTYPE const * const yyvaluep, YYLTYPE const * const yylocationp, struct parser_params *p)
{
  YYFPRINTF (p, "%s %s (",
             yytype < YYNTOKENS ? "token" : "nterm", yytname[yytype]);

  YY_LOCATION_PRINT (yyoutput, *yylocationp);
  YYFPRINTF (p, ": ");
  yy_symbol_value_print (yyoutput, yytype, yyvaluep, yylocationp, p);
  YYFPRINTF (p, ")");
}

/*------------------------------------------------------------------.
| yy_stack_print -- Print the state stack from its BOTTOM up to its |
| TOP (included).                                                   |
`------------------------------------------------------------------*/

static void
ruby_parser_yy_stack_print (yytype_int16 *yybottom, yytype_int16 *yytop, struct parser_params *p)
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
yy_reduce_print (yytype_int16 *yyssp, YYSTYPE *yyvsp, YYLTYPE *yylsp, int yyrule, struct parser_params *p)
{
  unsigned long int yylno = yyrline[yyrule];
  int yynrhs = yyr2[yyrule];
  int yyi;
  YYFPRINTF (p, "Reducing stack by rule %d (line %lu):\n",
             yyrule - 1, yylno);
  /* The symbols being reduced.  */
  for (yyi = 0; yyi < yynrhs; yyi++)
    {
      YYFPRINTF (p, "   $%d = ", yyi + 1);
      yy_symbol_print (stderr,
                       yystos[yyssp[yyi + 1 - yynrhs]],
                       &(yyvsp[(yyi + 1) - (yynrhs)])
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
#   define yystrlen strlen
#  else
/* Return the length of YYSTR.  */
static YYSIZE_T
yystrlen (const char *yystr)
{
  YYSIZE_T yylen;
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
static YYSIZE_T
yytnamerr (char *yyres, const char *yystr)
{
  if (*yystr == '"')
    {
      YYSIZE_T yyn = 0;
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
            /* Fall through.  */
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

  if (! yyres)
    return yystrlen (yystr);

  return yystpcpy (yyres, yystr) - yyres;
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
yysyntax_error (YYSIZE_T *yymsg_alloc, char **yymsg,
                yytype_int16 *yyssp, int yytoken)
{
  YYSIZE_T yysize0 = yytnamerr (YY_NULLPTR, yytname[yytoken]);
  YYSIZE_T yysize = yysize0;
  enum { YYERROR_VERBOSE_ARGS_MAXIMUM = 5 };
  /* Internationalized format string. */
  const char *yyformat = YY_NULLPTR;
  /* Arguments of yyformat. */
  char const *yyarg[YYERROR_VERBOSE_ARGS_MAXIMUM];
  /* Number of reported tokens (one for the "unexpected", one per
     "expected"). */
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
  if (yytoken != YYEMPTY)
    {
      int yyn = yypact[*yyssp];
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
                  YYSIZE_T yysize1 = yysize + yytnamerr (YY_NULLPTR, yytname[yyx]);
                  if (! (yysize <= yysize1
                         && yysize1 <= YYSTACK_ALLOC_MAXIMUM))
                    return 2;
                  yysize = yysize1;
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
      YYCASE_(0, YY_("syntax error"));
      YYCASE_(1, YY_("syntax error, unexpected %s"));
      YYCASE_(2, YY_("syntax error, unexpected %s, expecting %s"));
      YYCASE_(3, YY_("syntax error, unexpected %s, expecting %s or %s"));
      YYCASE_(4, YY_("syntax error, unexpected %s, expecting %s or %s or %s"));
      YYCASE_(5, YY_("syntax error, unexpected %s, expecting %s or %s or %s or %s"));
# undef YYCASE_
    }

  {
    YYSIZE_T yysize1 = yysize + yystrlen (yyformat);
    if (! (yysize <= yysize1 && yysize1 <= YYSTACK_ALLOC_MAXIMUM))
      return 2;
    yysize = yysize1;
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
          yyp++;
          yyformat++;
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

    int yystate;
    /* Number of tokens to shift before error messages enabled.  */
    int yyerrstatus;

    /* The stacks and their tools:
       'yyss': related to states.
       'yyvs': related to semantic values.
       'yyls': related to locations.

       Refer to the stacks through separate pointers, to allow yyoverflow
       to reallocate them elsewhere.  */

    /* The state stack.  */
    yytype_int16 yyssa[YYINITDEPTH];
    yytype_int16 *yyss;
    yytype_int16 *yyssp;

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

    YYSIZE_T yystacksize;

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
  YYSIZE_T yymsg_alloc = sizeof yymsgbuf;
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
#line 765 "ripper.y" /* yacc.c:1429  */
{
    RUBY_SET_YYLLOC_OF_NONE(yylloc);
}

#line 5157 "ripper.c" /* yacc.c:1429  */
  yylsp[0] = yylloc;
  goto yysetstate;

/*------------------------------------------------------------.
| yynewstate -- Push a new state, which is found in yystate.  |
`------------------------------------------------------------*/
 yynewstate:
  /* In all cases, when you get here, the value and location stacks
     have just been pushed.  So pushing a state here evens the stacks.  */
  yyssp++;

 yysetstate:
  *yyssp = yystate;

  if (yyss + yystacksize - 1 <= yyssp)
    {
      /* Get the current used size of the three stacks, in elements.  */
      YYSIZE_T yysize = yyssp - yyss + 1;

#ifdef yyoverflow
      {
        /* Give user a chance to reallocate the stack.  Use copies of
           these so that the &'s don't force the real ones into
           memory.  */
        YYSTYPE *yyvs1 = yyvs;
        yytype_int16 *yyss1 = yyss;
        YYLTYPE *yyls1 = yyls;

        /* Each stack pointer address is followed by the size of the
           data in use in that stack, in bytes.  This used to be a
           conditional around just the two extra args, but that might
           be undefined if yyoverflow is a macro.  */
        yyoverflow (YY_("memory exhausted"),
                    &yyss1, yysize * sizeof (*yyssp),
                    &yyvs1, yysize * sizeof (*yyvsp),
                    &yyls1, yysize * sizeof (*yylsp),
                    &yystacksize);

        yyls = yyls1;
        yyss = yyss1;
        yyvs = yyvs1;
      }
#else /* no yyoverflow */
# ifndef YYSTACK_RELOCATE
      goto yyexhaustedlab;
# else
      /* Extend the stack our own way.  */
      if (YYMAXDEPTH <= yystacksize)
        goto yyexhaustedlab;
      yystacksize *= 2;
      if (YYMAXDEPTH < yystacksize)
        yystacksize = YYMAXDEPTH;

      {
        yytype_int16 *yyss1 = yyss;
        union yyalloc *yyptr =
          (union yyalloc *) YYSTACK_ALLOC (YYSTACK_BYTES (yystacksize));
        if (! yyptr)
          goto yyexhaustedlab;
        YYSTACK_RELOCATE (yyss_alloc, yyss);
        YYSTACK_RELOCATE (yyvs_alloc, yyvs);
        YYSTACK_RELOCATE (yyls_alloc, yyls);
#  undef YYSTACK_RELOCATE
        if (yyss1 != yyssa)
          YYSTACK_FREE (yyss1);
      }
# endif
#endif /* no yyoverflow */

      yyssp = yyss + yysize - 1;
      yyvsp = yyvs + yysize - 1;
      yylsp = yyls + yysize - 1;

      YYDPRINTF ((p, "Stack size increased to %lu\n",
                  (unsigned long int) yystacksize));

      if (yyss + yystacksize - 1 <= yyssp)
        YYABORT;
    }

  YYDPRINTF ((p, "Entering state %d\n", yystate));

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

  /* Discard the shifted token.  */
  yychar = YYEMPTY;

  yystate = yyn;
  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END
  *++yylsp = yylloc;
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
| yyreduce -- Do a reduction.  |
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

  /* Default location.  */
  YYLLOC_DEFAULT (yyloc, (yylsp - yylen), yylen);
  YY_REDUCE_PRINT (yyn);
  switch (yyn)
    {
        case 2:
#line 936 "ripper.y" /* yacc.c:1646  */
    {
			SET_LEX_STATE(EXPR_BEG);
			local_push(p, ifndef_ripper(1)+0);
		    }
#line 5349 "ripper.c" /* yacc.c:1646  */
    break;

  case 3:
#line 941 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			if ((yyvsp[0].val) && !compile_for_eval) {
			    NODE *node = (yyvsp[0].val);
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
			p->eval_tree = NEW_SCOPE(0, block_append(p, p->eval_tree, (yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(program,v1);p->result=v2;}
			local_pop(p);
		    }
#line 5373 "ripper.c" /* yacc.c:1646  */
    break;

  case 4:
#line 963 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = void_stmts(p, (yyvsp[-1].val));
		    }
#line 5381 "ripper.c" /* yacc.c:1646  */
    break;

  case 5:
#line 969 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=dispatch0(stmts_new);v2=dispatch0(void_stmt);v3=v1;v4=v2;v5=dispatch2(stmts_add,v3,v4);(yyval.val)=v5;}
		    }
#line 5392 "ripper.c" /* yacc.c:1646  */
    break;

  case 6:
#line 976 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = newline_node((yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(stmts_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(stmts_add,v2,v3);(yyval.val)=v4;}
		    }
#line 5403 "ripper.c" /* yacc.c:1646  */
    break;

  case 7:
#line 983 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = block_append(p, (yyvsp[-2].val), newline_node((yyvsp[0].val)));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(stmts_add,v1,v2);(yyval.val)=v3;}
		    }
#line 5414 "ripper.c" /* yacc.c:1646  */
    break;

  case 8:
#line 990 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = remove_begin((yyvsp[0].val));
		    }
#line 5422 "ripper.c" /* yacc.c:1646  */
    break;

  case 10:
#line 997 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 5430 "ripper.c" /* yacc.c:1646  */
    break;

  case 11:
#line 1003 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			p->eval_tree_begin = block_append(p, p->eval_tree_begin,
							  NEW_BEGIN((yyvsp[-1].val), &(yyloc)));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(BEGIN,v1);(yyval.val)=v2;}
		    }
#line 5443 "ripper.c" /* yacc.c:1646  */
    break;

  case 12:
#line 1015 "ripper.y" /* yacc.c:1646  */
    {if (!(yyvsp[-1].val)) {yyerror1(&(yylsp[0]), "else without rescue is useless");}}
#line 5449 "ripper.c" /* yacc.c:1646  */
    break;

  case 13:
#line 1018 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_bodystmt(p, (yyvsp[-5].val), (yyvsp[-4].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=escape_Qundef((yyvsp[-5].val));v2=escape_Qundef((yyvsp[-4].val));v3=escape_Qundef((yyvsp[-1].val));v4=escape_Qundef((yyvsp[0].val));v5=dispatch4(bodystmt,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 5460 "ripper.c" /* yacc.c:1646  */
    break;

  case 14:
#line 1027 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_bodystmt(p, (yyvsp[-2].val), (yyvsp[-1].val), 0, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=escape_Qundef((yyvsp[-2].val));v2=escape_Qundef((yyvsp[-1].val));v3=Qnil;v4=escape_Qundef((yyvsp[0].val));v5=dispatch4(bodystmt,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 5471 "ripper.c" /* yacc.c:1646  */
    break;

  case 15:
#line 1036 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = void_stmts(p, (yyvsp[-1].val));
		    }
#line 5479 "ripper.c" /* yacc.c:1646  */
    break;

  case 16:
#line 1042 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=dispatch0(stmts_new);v2=dispatch0(void_stmt);v3=v1;v4=v2;v5=dispatch2(stmts_add,v3,v4);(yyval.val)=v5;}
		    }
#line 5490 "ripper.c" /* yacc.c:1646  */
    break;

  case 17:
#line 1049 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = newline_node((yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(stmts_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(stmts_add,v2,v3);(yyval.val)=v4;}
		    }
#line 5501 "ripper.c" /* yacc.c:1646  */
    break;

  case 18:
#line 1056 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = block_append(p, (yyvsp[-2].val), newline_node((yyvsp[0].val)));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(stmts_add,v1,v2);(yyval.val)=v3;}
		    }
#line 5512 "ripper.c" /* yacc.c:1646  */
    break;

  case 19:
#line 1063 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = remove_begin((yyvsp[0].val));
		    }
#line 5520 "ripper.c" /* yacc.c:1646  */
    break;

  case 20:
#line 1069 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 5528 "ripper.c" /* yacc.c:1646  */
    break;

  case 21:
#line 1073 "ripper.y" /* yacc.c:1646  */
    {
			yyerror1(&(yylsp[0]), "BEGIN is permitted only at toplevel");
		    }
#line 5536 "ripper.c" /* yacc.c:1646  */
    break;

  case 22:
#line 1077 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 5544 "ripper.c" /* yacc.c:1646  */
    break;

  case 23:
#line 1081 "ripper.y" /* yacc.c:1646  */
    {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 5550 "ripper.c" /* yacc.c:1646  */
    break;

  case 24:
#line 1082 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_ALIAS((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(alias,v1,v2);(yyval.val)=v3;}
		    }
#line 5561 "ripper.c" /* yacc.c:1646  */
    break;

  case 25:
#line 1089 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_VALIAS((yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(var_alias,v1,v2);(yyval.val)=v3;}
		    }
#line 5572 "ripper.c" /* yacc.c:1646  */
    break;

  case 26:
#line 1096 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			char buf[2];
			buf[0] = '$';
			buf[1] = (char)(yyvsp[0].val)->nd_nth;
			(yyval.val) = NEW_VALIAS((yyvsp[-1].val), rb_intern2(buf, 2), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(var_alias,v1,v2);(yyval.val)=v3;}
		    }
#line 5586 "ripper.c" /* yacc.c:1646  */
    break;

  case 27:
#line 1106 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			yyerror1(&(yylsp[0]), "can't make alias for the number variables");
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(var_alias,v1,v2);v4=v3;v5=dispatch1(alias_error,v4);(yyval.val)=v5;}ripper_error(p);
		    }
#line 5598 "ripper.c" /* yacc.c:1646  */
    break;

  case 28:
#line 1114 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(undef,v1);(yyval.val)=v2;}
		    }
#line 5609 "ripper.c" /* yacc.c:1646  */
    break;

  case 29:
#line 1121 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_if(p, (yyvsp[0].val), remove_begin((yyvsp[-2].val)), 0, &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(if_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 5621 "ripper.c" /* yacc.c:1646  */
    break;

  case 30:
#line 1129 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_unless(p, (yyvsp[0].val), remove_begin((yyvsp[-2].val)), 0, &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(unless_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 5633 "ripper.c" /* yacc.c:1646  */
    break;

  case 31:
#line 1137 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			if ((yyvsp[-2].val) && nd_type((yyvsp[-2].val)) == NODE_BEGIN) {
			    (yyval.val) = NEW_WHILE(cond(p, (yyvsp[0].val), &(yylsp[0])), (yyvsp[-2].val)->nd_body, 0, &(yyloc));
			}
			else {
			    (yyval.val) = NEW_WHILE(cond(p, (yyvsp[0].val), &(yylsp[0])), (yyvsp[-2].val), 1, &(yyloc));
			}
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(while_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 5649 "ripper.c" /* yacc.c:1646  */
    break;

  case 32:
#line 1149 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			if ((yyvsp[-2].val) && nd_type((yyvsp[-2].val)) == NODE_BEGIN) {
			    (yyval.val) = NEW_UNTIL(cond(p, (yyvsp[0].val), &(yylsp[0])), (yyvsp[-2].val)->nd_body, 0, &(yyloc));
			}
			else {
			    (yyval.val) = NEW_UNTIL(cond(p, (yyvsp[0].val), &(yylsp[0])), (yyvsp[-2].val), 1, &(yyloc));
			}
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[0].val);v2=(yyvsp[-2].val);v3=dispatch2(until_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 5665 "ripper.c" /* yacc.c:1646  */
    break;

  case 33:
#line 1161 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			NODE *resq;
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			resq = NEW_RESBODY(0, remove_begin((yyvsp[0].val)), 0, &loc);
			(yyval.val) = NEW_RESCUE(remove_begin((yyvsp[-2].val)), resq, 0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 5679 "ripper.c" /* yacc.c:1646  */
    break;

  case 34:
#line 1171 "ripper.y" /* yacc.c:1646  */
    {
			if (p->in_def) {
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
#line 5697 "ripper.c" /* yacc.c:1646  */
    break;

  case 36:
#line 1186 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = node_assign(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(massign,v1,v2);(yyval.val)=v3;}
		    }
#line 5709 "ripper.c" /* yacc.c:1646  */
    break;

  case 37:
#line 1194 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = node_assign(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(assign,v1,v2);(yyval.val)=v3;}
		    }
#line 5721 "ripper.c" /* yacc.c:1646  */
    break;

  case 38:
#line 1202 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = node_assign(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(massign,v1,v2);(yyval.val)=v3;}
		    }
#line 5732 "ripper.c" /* yacc.c:1646  */
    break;

  case 40:
#line 1212 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = node_assign(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(assign,v1,v2);(yyval.val)=v3;}
		    }
#line 5743 "ripper.c" /* yacc.c:1646  */
    break;

  case 41:
#line 1219 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_op_assign(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(opassign,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 5754 "ripper.c" /* yacc.c:1646  */
    break;

  case 42:
#line 1226 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_ary_op_assign(p, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-3]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-5].val);v2=escape_Qundef((yyvsp[-3].val));v3=dispatch2(aref_field,v1,v2);v4=v3;v5=(yyvsp[-1].val);v6=(yyvsp[0].val);v7=dispatch3(opassign,v4,v5,v6);(yyval.val)=v7;}

		    }
#line 5766 "ripper.c" /* yacc.c:1646  */
    break;

  case 43:
#line 1234 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-4].val), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-1].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 5777 "ripper.c" /* yacc.c:1646  */
    break;

  case 44:
#line 1241 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-4].val), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-1].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 5788 "ripper.c" /* yacc.c:1646  */
    break;

  case 45:
#line 1248 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-4]), &(yylsp[-2]));
			(yyval.val) = new_const_op_assign(p, NEW_COLON2((yyvsp[-4].val), (yyvsp[-2].val), &loc), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=dispatch2(const_path_field,v1,v2);v4=v3;v5=(yyvsp[-1].val);v6=(yyvsp[0].val);v7=dispatch3(opassign,v4,v5,v6);(yyval.val)=v7;}
		    }
#line 5800 "ripper.c" /* yacc.c:1646  */
    break;

  case 46:
#line 1256 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-4].val), ID2VAL(idCOLON2), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-2].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-1].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 5811 "ripper.c" /* yacc.c:1646  */
    break;

  case 47:
#line 1263 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			rb_backref_error(p, (yyvsp[-2].val));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=var_field(p, (yyvsp[-2].val));v2=(yyvsp[0].val);v3=dispatch2(assign,v1,v2);v4=v3;v5=dispatch1(assign_error,v4);(yyval.val)=v5;}ripper_error(p);
		    }
#line 5823 "ripper.c" /* yacc.c:1646  */
    break;

  case 48:
#line 1273 "ripper.y" /* yacc.c:1646  */
    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 5832 "ripper.c" /* yacc.c:1646  */
    break;

  case 49:
#line 1278 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			value_expr((yyvsp[-2].val));
			(yyval.val) = NEW_RESCUE((yyvsp[-2].val), NEW_RESBODY(0, remove_begin((yyvsp[0].val)), 0, &loc), 0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 5845 "ripper.c" /* yacc.c:1646  */
    break;

  case 52:
#line 1291 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = logop(p, idAND, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 5853 "ripper.c" /* yacc.c:1646  */
    break;

  case 53:
#line 1295 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = logop(p, idOR, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 5861 "ripper.c" /* yacc.c:1646  */
    break;

  case 54:
#line 1299 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_uni_op(p, method_cond(p, (yyvsp[0].val), &(yylsp[0])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 5869 "ripper.c" /* yacc.c:1646  */
    break;

  case 55:
#line 1303 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_uni_op(p, method_cond(p, (yyvsp[0].val), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 5877 "ripper.c" /* yacc.c:1646  */
    break;

  case 57:
#line 1310 "ripper.y" /* yacc.c:1646  */
    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 5886 "ripper.c" /* yacc.c:1646  */
    break;

  case 58:
#line 1316 "ripper.y" /* yacc.c:1646  */
    {COND_PUSH(1);}
#line 5892 "ripper.c" /* yacc.c:1646  */
    break;

  case 59:
#line 1316 "ripper.y" /* yacc.c:1646  */
    {COND_POP();}
#line 5898 "ripper.c" /* yacc.c:1646  */
    break;

  case 60:
#line 1317 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[-2].val);
		    }
#line 5906 "ripper.c" /* yacc.c:1646  */
    break;

  case 64:
#line 1328 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_qcall(p, (yyvsp[-2].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 5917 "ripper.c" /* yacc.c:1646  */
    break;

  case 65:
#line 1337 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[-1].val);
#if 0
			(yyval.val)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
#endif
		    }
#line 5929 "ripper.c" /* yacc.c:1646  */
    break;

  case 66:
#line 1347 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_FCALL((yyvsp[0].val), 0, &(yyloc));
			nd_set_line((yyval.val), p->tokline);
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 5941 "ripper.c" /* yacc.c:1646  */
    break;

  case 67:
#line 1357 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyvsp[-1].val)->nd_args = (yyvsp[0].val);
			nd_set_last_loc((yyvsp[-1].val), (yylsp[0]).end_pos);
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(command,v1,v2);(yyval.val)=v3;}
		    }
#line 5954 "ripper.c" /* yacc.c:1646  */
    break;

  case 68:
#line 1366 "ripper.y" /* yacc.c:1646  */
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
#line 5969 "ripper.c" /* yacc.c:1646  */
    break;

  case 69:
#line 1377 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_command_qcall(p, (yyvsp[-2].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), Qnull, &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=(yyvsp[0].val);v5=dispatch4(command_call,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 5980 "ripper.c" /* yacc.c:1646  */
    break;

  case 70:
#line 1384 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_command_qcall(p, (yyvsp[-3].val), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-2]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=(yyvsp[-1].val);v5=dispatch4(command_call,v1,v2,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=dispatch2(method_add_block,v6,v7);(yyval.val)=v8;}
		    }
#line 5991 "ripper.c" /* yacc.c:1646  */
    break;

  case 71:
#line 1391 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), Qnull, &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-3].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-1].val);v4=(yyvsp[0].val);v5=dispatch4(command_call,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 6002 "ripper.c" /* yacc.c:1646  */
    break;

  case 72:
#line 1398 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-2]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-2].val);v4=(yyvsp[-1].val);v5=dispatch4(command_call,v1,v2,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=dispatch2(method_add_block,v6,v7);(yyval.val)=v8;}
		   }
#line 6013 "ripper.c" /* yacc.c:1646  */
    break;

  case 73:
#line 1405 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_SUPER((yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(super,v1);(yyval.val)=v2;}
		    }
#line 6025 "ripper.c" /* yacc.c:1646  */
    break;

  case 74:
#line 1413 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_yield(p, (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(yield,v1);(yyval.val)=v2;}
		    }
#line 6037 "ripper.c" /* yacc.c:1646  */
    break;

  case 75:
#line 1421 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_RETURN(ret_args(p, (yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(return,v1);(yyval.val)=v2;}
		    }
#line 6048 "ripper.c" /* yacc.c:1646  */
    break;

  case 76:
#line 1428 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_BREAK(ret_args(p, (yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(break,v1);(yyval.val)=v2;}
		    }
#line 6059 "ripper.c" /* yacc.c:1646  */
    break;

  case 77:
#line 1435 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_NEXT(ret_args(p, (yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(next,v1);(yyval.val)=v2;}
		    }
#line 6070 "ripper.c" /* yacc.c:1646  */
    break;

  case 79:
#line 1445 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(mlhs_paren,v1);(yyval.val)=v2;}
		    }
#line 6081 "ripper.c" /* yacc.c:1646  */
    break;

  case 81:
#line 1455 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(NEW_LIST((yyvsp[-1].val), &(yyloc)), 0, &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(mlhs_paren,v1);(yyval.val)=v2;}
		    }
#line 6092 "ripper.c" /* yacc.c:1646  */
    break;

  case 82:
#line 1464 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 6103 "ripper.c" /* yacc.c:1646  */
    break;

  case 83:
#line 1471 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(list_append(p, (yyvsp[-1].val),(yyvsp[0].val)), 0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(mlhs_add,v1,v2);(yyval.val)=v3;}
		    }
#line 6114 "ripper.c" /* yacc.c:1646  */
    break;

  case 84:
#line 1478 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(mlhs_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 6125 "ripper.c" /* yacc.c:1646  */
    break;

  case 85:
#line 1485 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-4].val), NEW_POSTARG((yyvsp[-2].val),(yyvsp[0].val),&(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=dispatch2(mlhs_add_star,v1,v2);v4=v3;v5=(yyvsp[0].val);v6=dispatch2(mlhs_add_post,v4,v5);(yyval.val)=v6;}
		    }
#line 6136 "ripper.c" /* yacc.c:1646  */
    break;

  case 86:
#line 1492 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-1].val), NODE_SPECIAL_NO_NAME_REST, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=Qnil;v3=dispatch2(mlhs_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 6147 "ripper.c" /* yacc.c:1646  */
    break;

  case 87:
#line 1499 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-3].val), NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-3].val);v2=Qnil;v3=dispatch2(mlhs_add_star,v1,v2);v4=v3;v5=(yyvsp[0].val);v6=dispatch2(mlhs_add_post,v4,v5);(yyval.val)=v6;}
		    }
#line 6158 "ripper.c" /* yacc.c:1646  */
    break;

  case 88:
#line 1506 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(0, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(mlhs_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 6169 "ripper.c" /* yacc.c:1646  */
    break;

  case 89:
#line 1513 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(0, NEW_POSTARG((yyvsp[-2].val),(yyvsp[0].val),&(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[-2].val);v4=dispatch2(mlhs_add_star,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(mlhs_add_post,v5,v6);(yyval.val)=v7;}
		    }
#line 6180 "ripper.c" /* yacc.c:1646  */
    break;

  case 90:
#line 1520 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(0, NODE_SPECIAL_NO_NAME_REST, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=Qnil;v4=dispatch2(mlhs_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 6191 "ripper.c" /* yacc.c:1646  */
    break;

  case 91:
#line 1527 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(0, NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=dispatch0(mlhs_new);v2=v1;v3=Qnil;v4=dispatch2(mlhs_add_star,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(mlhs_add_post,v5,v6);(yyval.val)=v7;}
		    }
#line 6202 "ripper.c" /* yacc.c:1646  */
    break;

  case 93:
#line 1537 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(mlhs_paren,v1);(yyval.val)=v2;}
		    }
#line 6213 "ripper.c" /* yacc.c:1646  */
    break;

  case 94:
#line 1546 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[-1].val), &(yylsp[-1]));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[-1].val);v4=dispatch2(mlhs_add,v2,v3);(yyval.val)=v4;}
		    }
#line 6224 "ripper.c" /* yacc.c:1646  */
    break;

  case 95:
#line 1553 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), (yyvsp[-1].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(mlhs_add,v1,v2);(yyval.val)=v3;}
		    }
#line 6235 "ripper.c" /* yacc.c:1646  */
    break;

  case 96:
#line 1562 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(mlhs_add,v2,v3);(yyval.val)=v4;}
		    }
#line 6246 "ripper.c" /* yacc.c:1646  */
    break;

  case 97:
#line 1569 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(mlhs_add,v1,v2);(yyval.val)=v3;}
		    }
#line 6257 "ripper.c" /* yacc.c:1646  */
    break;

  case 98:
#line 1578 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 6268 "ripper.c" /* yacc.c:1646  */
    break;

  case 99:
#line 1585 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 6279 "ripper.c" /* yacc.c:1646  */
    break;

  case 100:
#line 1592 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = aryset(p, (yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=escape_Qundef((yyvsp[-1].val));v3=dispatch2(aref_field,v1,v2);(yyval.val)=v3;}
		    }
#line 6290 "ripper.c" /* yacc.c:1646  */
    break;

  case 101:
#line 1599 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 6301 "ripper.c" /* yacc.c:1646  */
    break;

  case 102:
#line 1606 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), idCOLON2, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_field,v1,v2);(yyval.val)=v3;}
		    }
#line 6312 "ripper.c" /* yacc.c:1646  */
    break;

  case 103:
#line 1613 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 6323 "ripper.c" /* yacc.c:1646  */
    break;

  case 104:
#line 1620 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = const_decl(p, NEW_COLON2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_field,v1,v2);(yyval.val)=const_decl(p, v3);}
		    }
#line 6334 "ripper.c" /* yacc.c:1646  */
    break;

  case 105:
#line 1627 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = const_decl(p, NEW_COLON3((yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(top_const_field,v1);(yyval.val)=const_decl(p, v2);}
		    }
#line 6345 "ripper.c" /* yacc.c:1646  */
    break;

  case 106:
#line 1634 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			rb_backref_error(p, (yyvsp[0].val));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=var_field(p, (yyvsp[0].val));v2=dispatch1(assign_error,v1);(yyval.val)=v2;}ripper_error(p);
		    }
#line 6357 "ripper.c" /* yacc.c:1646  */
    break;

  case 107:
#line 1644 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 6368 "ripper.c" /* yacc.c:1646  */
    break;

  case 108:
#line 1651 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 6379 "ripper.c" /* yacc.c:1646  */
    break;

  case 109:
#line 1658 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = aryset(p, (yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=escape_Qundef((yyvsp[-1].val));v3=dispatch2(aref_field,v1,v2);(yyval.val)=v3;}
		    }
#line 6390 "ripper.c" /* yacc.c:1646  */
    break;

  case 110:
#line 1665 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 6401 "ripper.c" /* yacc.c:1646  */
    break;

  case 111:
#line 1672 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), idCOLON2, (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 6412 "ripper.c" /* yacc.c:1646  */
    break;

  case 112:
#line 1679 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = attrset(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(field,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 6423 "ripper.c" /* yacc.c:1646  */
    break;

  case 113:
#line 1686 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = const_decl(p, NEW_COLON2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_field,v1,v2);(yyval.val)=const_decl(p, v3);}
		    }
#line 6434 "ripper.c" /* yacc.c:1646  */
    break;

  case 114:
#line 1693 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = const_decl(p, NEW_COLON3((yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(top_const_field,v1);(yyval.val)=const_decl(p, v2);}
		    }
#line 6445 "ripper.c" /* yacc.c:1646  */
    break;

  case 115:
#line 1700 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			rb_backref_error(p, (yyvsp[0].val));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=var_field(p, (yyvsp[0].val));v2=dispatch1(assign_error,v1);(yyval.val)=v2;}ripper_error(p);
		    }
#line 6457 "ripper.c" /* yacc.c:1646  */
    break;

  case 116:
#line 1710 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			yyerror1(&(yylsp[0]), "class/module name must be CONSTANT");
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(class_name_error,v1);(yyval.val)=v2;}ripper_error(p);
		    }
#line 6468 "ripper.c" /* yacc.c:1646  */
    break;

  case 118:
#line 1720 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_COLON3((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(top_const_ref,v1);(yyval.val)=v2;}
		    }
#line 6479 "ripper.c" /* yacc.c:1646  */
    break;

  case 119:
#line 1727 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_COLON2(0, (yyval.val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(const_ref,v1);(yyval.val)=v2;}
		    }
#line 6490 "ripper.c" /* yacc.c:1646  */
    break;

  case 120:
#line 1734 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_COLON2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_ref,v1,v2);(yyval.val)=v3;}
		    }
#line 6501 "ripper.c" /* yacc.c:1646  */
    break;

  case 124:
#line 1746 "ripper.y" /* yacc.c:1646  */
    {
			SET_LEX_STATE(EXPR_ENDFN);
			(yyval.val) = (yyvsp[0].val);
		    }
#line 6510 "ripper.c" /* yacc.c:1646  */
    break;

  case 125:
#line 1751 "ripper.y" /* yacc.c:1646  */
    {
			SET_LEX_STATE(EXPR_ENDFN);
			(yyval.val) = (yyvsp[0].val);
		    }
#line 6519 "ripper.c" /* yacc.c:1646  */
    break;

  case 128:
#line 1762 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_LIT(ID2SYM((yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(symbol_literal,v1);(yyval.val)=v2;}
		    }
#line 6530 "ripper.c" /* yacc.c:1646  */
    break;

  case 130:
#line 1772 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_UNDEF((yyvsp[0].val), &(yyloc));
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 6541 "ripper.c" /* yacc.c:1646  */
    break;

  case 131:
#line 1778 "ripper.y" /* yacc.c:1646  */
    {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 6547 "ripper.c" /* yacc.c:1646  */
    break;

  case 132:
#line 1779 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			NODE *undef = NEW_UNDEF((yyvsp[0].val), &(yylsp[0]));
			(yyval.val) = block_append(p, (yyvsp[-3].val), undef);
#endif
			(yyval.val)=rb_ary_push((yyvsp[-3].val), get_value((yyvsp[0].val)));
		    }
#line 6559 "ripper.c" /* yacc.c:1646  */
    break;

  case 133:
#line 1788 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '|'); }
#line 6565 "ripper.c" /* yacc.c:1646  */
    break;

  case 134:
#line 1789 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '^'); }
#line 6571 "ripper.c" /* yacc.c:1646  */
    break;

  case 135:
#line 1790 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '&'); }
#line 6577 "ripper.c" /* yacc.c:1646  */
    break;

  case 136:
#line 1791 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tCMP); }
#line 6583 "ripper.c" /* yacc.c:1646  */
    break;

  case 137:
#line 1792 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tEQ); }
#line 6589 "ripper.c" /* yacc.c:1646  */
    break;

  case 138:
#line 1793 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tEQQ); }
#line 6595 "ripper.c" /* yacc.c:1646  */
    break;

  case 139:
#line 1794 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tMATCH); }
#line 6601 "ripper.c" /* yacc.c:1646  */
    break;

  case 140:
#line 1795 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tNMATCH); }
#line 6607 "ripper.c" /* yacc.c:1646  */
    break;

  case 141:
#line 1796 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '>'); }
#line 6613 "ripper.c" /* yacc.c:1646  */
    break;

  case 142:
#line 1797 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tGEQ); }
#line 6619 "ripper.c" /* yacc.c:1646  */
    break;

  case 143:
#line 1798 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '<'); }
#line 6625 "ripper.c" /* yacc.c:1646  */
    break;

  case 144:
#line 1799 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tLEQ); }
#line 6631 "ripper.c" /* yacc.c:1646  */
    break;

  case 145:
#line 1800 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tNEQ); }
#line 6637 "ripper.c" /* yacc.c:1646  */
    break;

  case 146:
#line 1801 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tLSHFT); }
#line 6643 "ripper.c" /* yacc.c:1646  */
    break;

  case 147:
#line 1802 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tRSHFT); }
#line 6649 "ripper.c" /* yacc.c:1646  */
    break;

  case 148:
#line 1803 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '+'); }
#line 6655 "ripper.c" /* yacc.c:1646  */
    break;

  case 149:
#line 1804 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '-'); }
#line 6661 "ripper.c" /* yacc.c:1646  */
    break;

  case 150:
#line 1805 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '*'); }
#line 6667 "ripper.c" /* yacc.c:1646  */
    break;

  case 151:
#line 1806 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '*'); }
#line 6673 "ripper.c" /* yacc.c:1646  */
    break;

  case 152:
#line 1807 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '/'); }
#line 6679 "ripper.c" /* yacc.c:1646  */
    break;

  case 153:
#line 1808 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '%'); }
#line 6685 "ripper.c" /* yacc.c:1646  */
    break;

  case 154:
#line 1809 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tPOW); }
#line 6691 "ripper.c" /* yacc.c:1646  */
    break;

  case 155:
#line 1810 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tDSTAR); }
#line 6697 "ripper.c" /* yacc.c:1646  */
    break;

  case 156:
#line 1811 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '!'); }
#line 6703 "ripper.c" /* yacc.c:1646  */
    break;

  case 157:
#line 1812 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '~'); }
#line 6709 "ripper.c" /* yacc.c:1646  */
    break;

  case 158:
#line 1813 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tUPLUS); }
#line 6715 "ripper.c" /* yacc.c:1646  */
    break;

  case 159:
#line 1814 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tUMINUS); }
#line 6721 "ripper.c" /* yacc.c:1646  */
    break;

  case 160:
#line 1815 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tAREF); }
#line 6727 "ripper.c" /* yacc.c:1646  */
    break;

  case 161:
#line 1816 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = tASET); }
#line 6733 "ripper.c" /* yacc.c:1646  */
    break;

  case 162:
#line 1817 "ripper.y" /* yacc.c:1646  */
    { ifndef_ripper((yyval.val) = '`'); }
#line 6739 "ripper.c" /* yacc.c:1646  */
    break;

  case 204:
#line 1835 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = node_assign(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(assign,v1,v2);(yyval.val)=v3;}
		    }
#line 6750 "ripper.c" /* yacc.c:1646  */
    break;

  case 205:
#line 1842 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_op_assign(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(opassign,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 6761 "ripper.c" /* yacc.c:1646  */
    break;

  case 206:
#line 1849 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = new_ary_op_assign(p, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-3]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-5].val);v2=escape_Qundef((yyvsp[-3].val));v3=dispatch2(aref_field,v1,v2);v4=v3;v5=(yyvsp[-1].val);v6=(yyvsp[0].val);v7=dispatch3(opassign,v4,v5,v6);(yyval.val)=v7;}
		    }
#line 6773 "ripper.c" /* yacc.c:1646  */
    break;

  case 207:
#line 1857 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-4].val), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-1].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 6785 "ripper.c" /* yacc.c:1646  */
    break;

  case 208:
#line 1865 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-4].val), (yyvsp[-3].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-1].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 6797 "ripper.c" /* yacc.c:1646  */
    break;

  case 209:
#line 1873 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = new_attr_op_assign(p, (yyvsp[-4].val), ID2VAL(idCOLON2), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-2].val);v4=dispatch3(field,v1,v2,v3);v5=v4;v6=(yyvsp[-1].val);v7=(yyvsp[0].val);v8=dispatch3(opassign,v5,v6,v7);(yyval.val)=v8;}
		    }
#line 6809 "ripper.c" /* yacc.c:1646  */
    break;

  case 210:
#line 1881 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-4]), &(yylsp[-2]));
			(yyval.val) = new_const_op_assign(p, NEW_COLON2((yyvsp[-4].val), (yyvsp[-2].val), &loc), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=dispatch2(const_path_field,v1,v2);v4=v3;v5=(yyvsp[-1].val);v6=(yyvsp[0].val);v7=dispatch3(opassign,v4,v5,v6);(yyval.val)=v7;}
		    }
#line 6821 "ripper.c" /* yacc.c:1646  */
    break;

  case 211:
#line 1889 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_const_op_assign(p, NEW_COLON3((yyvsp[-2].val), &(yyloc)), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-2].val);v2=dispatch1(top_const_field,v1);v3=v2;v4=(yyvsp[-1].val);v5=(yyvsp[0].val);v6=dispatch3(opassign,v3,v4,v5);(yyval.val)=v6;}
		    }
#line 6832 "ripper.c" /* yacc.c:1646  */
    break;

  case 212:
#line 1896 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			rb_backref_error(p, (yyvsp[-2].val));
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=var_field(p, (yyvsp[-2].val));v2=(yyvsp[-1].val);v3=(yyvsp[0].val);v4=dispatch3(opassign,v1,v2,v3);v5=v4;v6=dispatch1(assign_error,v5);(yyval.val)=v6;}ripper_error(p);
		    }
#line 6844 "ripper.c" /* yacc.c:1646  */
    break;

  case 213:
#line 1904 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[-2].val));
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(dot2,v1,v2);(yyval.val)=v3;}
		    }
#line 6857 "ripper.c" /* yacc.c:1646  */
    break;

  case 214:
#line 1913 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[-2].val));
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_DOT3((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(dot3,v1,v2);(yyval.val)=v3;}
		    }
#line 6870 "ripper.c" /* yacc.c:1646  */
    break;

  case 215:
#line 1922 "ripper.y" /* yacc.c:1646  */
    {
#if 0
                        YYLTYPE loc;
                        loc.beg_pos = (yylsp[0]).end_pos;
                        loc.end_pos = (yylsp[0]).end_pos;

			value_expr((yyvsp[-1].val));
			(yyval.val) = NEW_DOT2((yyvsp[-1].val), new_nil(&loc), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=Qnil;v3=dispatch2(dot2,v1,v2);(yyval.val)=v3;}
		    }
#line 6886 "ripper.c" /* yacc.c:1646  */
    break;

  case 216:
#line 1934 "ripper.y" /* yacc.c:1646  */
    {
#if 0
                        YYLTYPE loc;
                        loc.beg_pos = (yylsp[0]).end_pos;
                        loc.end_pos = (yylsp[0]).end_pos;

			value_expr((yyvsp[-1].val));
			(yyval.val) = NEW_DOT3((yyvsp[-1].val), new_nil(&loc), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=Qnil;v3=dispatch2(dot3,v1,v2);(yyval.val)=v3;}
		    }
#line 6902 "ripper.c" /* yacc.c:1646  */
    break;

  case 217:
#line 1946 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '+', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 6910 "ripper.c" /* yacc.c:1646  */
    break;

  case 218:
#line 1950 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '-', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 6918 "ripper.c" /* yacc.c:1646  */
    break;

  case 219:
#line 1954 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '*', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 6926 "ripper.c" /* yacc.c:1646  */
    break;

  case 220:
#line 1958 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '/', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 6934 "ripper.c" /* yacc.c:1646  */
    break;

  case 221:
#line 1962 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '%', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 6942 "ripper.c" /* yacc.c:1646  */
    break;

  case 222:
#line 1966 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idPow, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 6950 "ripper.c" /* yacc.c:1646  */
    break;

  case 223:
#line 1970 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_uni_op(p, call_bin_op(p, (yyvsp[-2].val), idPow, (yyvsp[0].val), &(yylsp[-2]), &(yyloc)), idUMinus, &(yylsp[-3]), &(yyloc));
		    }
#line 6958 "ripper.c" /* yacc.c:1646  */
    break;

  case 224:
#line 1974 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_uni_op(p, (yyvsp[0].val), idUPlus, &(yylsp[-1]), &(yyloc));
		    }
#line 6966 "ripper.c" /* yacc.c:1646  */
    break;

  case 225:
#line 1978 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_uni_op(p, (yyvsp[0].val), idUMinus, &(yylsp[-1]), &(yyloc));
		    }
#line 6974 "ripper.c" /* yacc.c:1646  */
    break;

  case 226:
#line 1982 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '|', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 6982 "ripper.c" /* yacc.c:1646  */
    break;

  case 227:
#line 1986 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '^', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 6990 "ripper.c" /* yacc.c:1646  */
    break;

  case 228:
#line 1990 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), '&', (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 6998 "ripper.c" /* yacc.c:1646  */
    break;

  case 229:
#line 1994 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idCmp, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7006 "ripper.c" /* yacc.c:1646  */
    break;

  case 231:
#line 1999 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idEq, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7014 "ripper.c" /* yacc.c:1646  */
    break;

  case 232:
#line 2003 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idEqq, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7022 "ripper.c" /* yacc.c:1646  */
    break;

  case 233:
#line 2007 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idNeq, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7030 "ripper.c" /* yacc.c:1646  */
    break;

  case 234:
#line 2011 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = match_op(p, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7038 "ripper.c" /* yacc.c:1646  */
    break;

  case 235:
#line 2015 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idNeqTilde, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7046 "ripper.c" /* yacc.c:1646  */
    break;

  case 236:
#line 2019 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_uni_op(p, method_cond(p, (yyvsp[0].val), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 7054 "ripper.c" /* yacc.c:1646  */
    break;

  case 237:
#line 2023 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_uni_op(p, (yyvsp[0].val), '~', &(yylsp[-1]), &(yyloc));
		    }
#line 7062 "ripper.c" /* yacc.c:1646  */
    break;

  case 238:
#line 2027 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idLTLT, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7070 "ripper.c" /* yacc.c:1646  */
    break;

  case 239:
#line 2031 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), idGTGT, (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7078 "ripper.c" /* yacc.c:1646  */
    break;

  case 240:
#line 2035 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = logop(p, idANDOP, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7086 "ripper.c" /* yacc.c:1646  */
    break;

  case 241:
#line 2039 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = logop(p, idOROP, (yyvsp[-2].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7094 "ripper.c" /* yacc.c:1646  */
    break;

  case 242:
#line 2042 "ripper.y" /* yacc.c:1646  */
    {p->in_defined = 1;}
#line 7100 "ripper.c" /* yacc.c:1646  */
    break;

  case 243:
#line 2043 "ripper.y" /* yacc.c:1646  */
    {
			p->in_defined = 0;
			(yyval.val) = new_defined(p, (yyvsp[0].val), &(yyloc));
		    }
#line 7109 "ripper.c" /* yacc.c:1646  */
    break;

  case 244:
#line 2048 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[-5].val));
			(yyval.val) = new_if(p, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-5].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-5].val);v2=(yyvsp[-3].val);v3=(yyvsp[0].val);v4=dispatch3(ifop,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 7122 "ripper.c" /* yacc.c:1646  */
    break;

  case 245:
#line 2057 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 7130 "ripper.c" /* yacc.c:1646  */
    break;

  case 246:
#line 2062 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = '>';}
#line 7136 "ripper.c" /* yacc.c:1646  */
    break;

  case 247:
#line 2063 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = '<';}
#line 7142 "ripper.c" /* yacc.c:1646  */
    break;

  case 248:
#line 2064 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = idGE;}
#line 7148 "ripper.c" /* yacc.c:1646  */
    break;

  case 249:
#line 2065 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = idLE;}
#line 7154 "ripper.c" /* yacc.c:1646  */
    break;

  case 250:
#line 2069 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7162 "ripper.c" /* yacc.c:1646  */
    break;

  case 251:
#line 2073 "ripper.y" /* yacc.c:1646  */
    {
			rb_warning1("comparison '%s' after comparison", WARN_ID((yyvsp[-1].val)));
			(yyval.val) = call_bin_op(p, (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
		    }
#line 7171 "ripper.c" /* yacc.c:1646  */
    break;

  case 252:
#line 2080 "ripper.y" /* yacc.c:1646  */
    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 7180 "ripper.c" /* yacc.c:1646  */
    break;

  case 254:
#line 2088 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 7188 "ripper.c" /* yacc.c:1646  */
    break;

  case 255:
#line 2092 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? arg_append(p, (yyvsp[-3].val), new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].val);
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-1].val);v2=dispatch1(bare_assoc_hash,v1);v3=(yyvsp[-3].val);v4=v2;v5=dispatch2(args_add,v3,v4);(yyval.val)=v5;}
		    }
#line 7199 "ripper.c" /* yacc.c:1646  */
    break;

  case 256:
#line 2099 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? NEW_LIST(new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yyloc)) : 0;
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=dispatch0(args_new);v2=(yyvsp[-1].val);v3=dispatch1(bare_assoc_hash,v2);v4=v1;v5=v3;v6=dispatch2(args_add,v4,v5);(yyval.val)=v6;}
		    }
#line 7210 "ripper.c" /* yacc.c:1646  */
    break;

  case 257:
#line 2108 "ripper.y" /* yacc.c:1646  */
    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 7219 "ripper.c" /* yacc.c:1646  */
    break;

  case 258:
#line 2113 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			value_expr((yyvsp[-2].val));
			(yyval.val) = NEW_RESCUE((yyvsp[-2].val), NEW_RESBODY(0, remove_begin((yyvsp[0].val)), 0, &loc), 0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(rescue_mod,v1,v2);(yyval.val)=v3;}
		    }
#line 7232 "ripper.c" /* yacc.c:1646  */
    break;

  case 259:
#line 2124 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=escape_Qundef((yyvsp[-1].val));v2=dispatch1(arg_paren,v1);(yyval.val)=v2;}
		    }
#line 7243 "ripper.c" /* yacc.c:1646  */
    break;

  case 264:
#line 2139 "ripper.y" /* yacc.c:1646  */
    {
		      (yyval.val) = (yyvsp[-1].val);
		    }
#line 7251 "ripper.c" /* yacc.c:1646  */
    break;

  case 265:
#line 2143 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? arg_append(p, (yyvsp[-3].val), new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].val);
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-1].val);v2=dispatch1(bare_assoc_hash,v1);v3=(yyvsp[-3].val);v4=v2;v5=dispatch2(args_add,v3,v4);(yyval.val)=v5;}
		    }
#line 7262 "ripper.c" /* yacc.c:1646  */
    break;

  case 266:
#line 2150 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? NEW_LIST(new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yylsp[-1])) : 0;
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=dispatch0(args_new);v2=(yyvsp[-1].val);v3=dispatch1(bare_assoc_hash,v2);v4=v1;v5=v3;v6=dispatch2(args_add,v4,v5);(yyval.val)=v6;}
		    }
#line 7273 "ripper.c" /* yacc.c:1646  */
    break;

  case 267:
#line 2159 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			value_expr((yyvsp[0].val));
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add,v2,v3);(yyval.val)=v4;}
		    }
#line 7285 "ripper.c" /* yacc.c:1646  */
    break;

  case 268:
#line 2167 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = arg_blk_pass((yyvsp[-1].val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(args_add_block,v1,v2);(yyval.val)=v3;}
		    }
#line 7296 "ripper.c" /* yacc.c:1646  */
    break;

  case 269:
#line 2174 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? NEW_LIST(new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yylsp[-1])) : 0;
			(yyval.val) = arg_blk_pass((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8,v9;v1=dispatch0(args_new);v2=(yyvsp[-1].val);v3=dispatch1(bare_assoc_hash,v2);v4=v1;v5=v3;v6=dispatch2(args_add,v4,v5);v7=v6;v8=(yyvsp[0].val);v9=dispatch2(args_add_block,v7,v8);(yyval.val)=v9;}
		    }
#line 7308 "ripper.c" /* yacc.c:1646  */
    break;

  case 270:
#line 2182 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val) ? arg_append(p, (yyvsp[-3].val), new_hash(p, (yyvsp[-1].val), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].val);
			(yyval.val) = arg_blk_pass((yyval.val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-1].val);v2=dispatch1(bare_assoc_hash,v1);v3=(yyvsp[-3].val);v4=v2;v5=dispatch2(args_add,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=dispatch2(args_add_block,v6,v7);(yyval.val)=v8;}
		    }
#line 7320 "ripper.c" /* yacc.c:1646  */
    break;

  case 271:
#line 2190 "ripper.y" /* yacc.c:1646  */
    {{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add_block,v2,v3);(yyval.val)=v4;}}
#line 7326 "ripper.c" /* yacc.c:1646  */
    break;

  case 272:
#line 2193 "ripper.y" /* yacc.c:1646  */
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
#line 7348 "ripper.c" /* yacc.c:1646  */
    break;

  case 273:
#line 2211 "ripper.y" /* yacc.c:1646  */
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
#line 7370 "ripper.c" /* yacc.c:1646  */
    break;

  case 274:
#line 2231 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_BLOCK_PASS((yyvsp[0].val), &(yyloc));
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 7381 "ripper.c" /* yacc.c:1646  */
    break;

  case 275:
#line 2240 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 7389 "ripper.c" /* yacc.c:1646  */
    break;

  case 276:
#line 2244 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = 0;
		    }
#line 7397 "ripper.c" /* yacc.c:1646  */
    break;

  case 277:
#line 2250 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add,v2,v3);(yyval.val)=v4;}
		    }
#line 7408 "ripper.c" /* yacc.c:1646  */
    break;

  case 278:
#line 2257 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_SPLAT((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(args_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(args_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 7419 "ripper.c" /* yacc.c:1646  */
    break;

  case 279:
#line 2264 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = last_arg_append(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(args_add,v1,v2);(yyval.val)=v3;}
		    }
#line 7430 "ripper.c" /* yacc.c:1646  */
    break;

  case 280:
#line 2271 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = rest_arg_append(p, (yyvsp[-3].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[0].val);v3=dispatch2(args_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 7441 "ripper.c" /* yacc.c:1646  */
    break;

  case 283:
#line 2284 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = last_arg_append(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-2].val);v2=dispatch1(mrhs_new_from_args,v1);v3=v2;v4=(yyvsp[0].val);v5=dispatch2(mrhs_add,v3,v4);(yyval.val)=v5;}
		    }
#line 7452 "ripper.c" /* yacc.c:1646  */
    break;

  case 284:
#line 2291 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = rest_arg_append(p, (yyvsp[-3].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-3].val);v2=dispatch1(mrhs_new_from_args,v1);v3=v2;v4=(yyvsp[0].val);v5=dispatch2(mrhs_add_star,v3,v4);(yyval.val)=v5;}
		    }
#line 7463 "ripper.c" /* yacc.c:1646  */
    break;

  case 285:
#line 2298 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_SPLAT((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mrhs_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(mrhs_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 7474 "ripper.c" /* yacc.c:1646  */
    break;

  case 296:
#line 2317 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_FCALL((yyvsp[0].val), 0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[0].val);v2=dispatch1(fcall,v1);v3=dispatch0(args_new);v4=v2;v5=v3;v6=dispatch2(method_add_arg,v4,v5);(yyval.val)=v6;}
		    }
#line 7485 "ripper.c" /* yacc.c:1646  */
    break;

  case 297:
#line 2324 "ripper.y" /* yacc.c:1646  */
    {
			CMDARG_PUSH(0);
		    }
#line 7493 "ripper.c" /* yacc.c:1646  */
    break;

  case 298:
#line 2329 "ripper.y" /* yacc.c:1646  */
    {
			CMDARG_POP();
#if 0
			set_line_body((yyvsp[-1].val), (yylsp[-3]).end_pos.lineno);
			(yyval.val) = NEW_BEGIN((yyvsp[-1].val), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-3]).end_pos.lineno);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(begin,v1);(yyval.val)=v2;}
		    }
#line 7507 "ripper.c" /* yacc.c:1646  */
    break;

  case 299:
#line 2338 "ripper.y" /* yacc.c:1646  */
    {SET_LEX_STATE(EXPR_ENDARG);}
#line 7513 "ripper.c" /* yacc.c:1646  */
    break;

  case 300:
#line 2339 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=0;v2=dispatch1(paren,v1);(yyval.val)=v2;}
		    }
#line 7524 "ripper.c" /* yacc.c:1646  */
    break;

  case 301:
#line 2345 "ripper.y" /* yacc.c:1646  */
    {SET_LEX_STATE(EXPR_ENDARG);}
#line 7530 "ripper.c" /* yacc.c:1646  */
    break;

  case 302:
#line 2346 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-2].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-2].val);v2=dispatch1(paren,v1);(yyval.val)=v2;}
		    }
#line 7541 "ripper.c" /* yacc.c:1646  */
    break;

  case 303:
#line 2353 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(paren,v1);(yyval.val)=v2;}
		    }
#line 7552 "ripper.c" /* yacc.c:1646  */
    break;

  case 304:
#line 2360 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_COLON2((yyvsp[-2].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(const_path_ref,v1,v2);(yyval.val)=v3;}
		    }
#line 7563 "ripper.c" /* yacc.c:1646  */
    break;

  case 305:
#line 2367 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_COLON3((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(top_const_ref,v1);(yyval.val)=v2;}
		    }
#line 7574 "ripper.c" /* yacc.c:1646  */
    break;

  case 306:
#line 2374 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = make_array((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=escape_Qundef((yyvsp[-1].val));v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 7585 "ripper.c" /* yacc.c:1646  */
    break;

  case 307:
#line 2381 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_hash(p, (yyvsp[-1].val), &(yyloc));
			(yyval.val)->nd_alen = TRUE;
#endif
			{VALUE v1,v2;v1=escape_Qundef((yyvsp[-1].val));v2=dispatch1(hash,v1);(yyval.val)=v2;}
		    }
#line 7597 "ripper.c" /* yacc.c:1646  */
    break;

  case 308:
#line 2389 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_RETURN(0, &(yyloc));
#endif
			{VALUE v1;v1=dispatch0(return0);(yyval.val)=v1;}
		    }
#line 7608 "ripper.c" /* yacc.c:1646  */
    break;

  case 309:
#line 2396 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_yield(p, (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-1].val);v2=dispatch1(paren,v1);v3=v2;v4=dispatch1(yield,v3);(yyval.val)=v4;}
		    }
#line 7619 "ripper.c" /* yacc.c:1646  */
    break;

  case 310:
#line 2403 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_YIELD(0, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=dispatch0(args_new);v2=v1;v3=dispatch1(paren,v2);v4=v3;v5=dispatch1(yield,v4);(yyval.val)=v5;}
		    }
#line 7630 "ripper.c" /* yacc.c:1646  */
    break;

  case 311:
#line 2410 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_YIELD(0, &(yyloc));
#endif
			{VALUE v1;v1=dispatch0(yield0);(yyval.val)=v1;}
		    }
#line 7641 "ripper.c" /* yacc.c:1646  */
    break;

  case 312:
#line 2416 "ripper.y" /* yacc.c:1646  */
    {p->in_defined = 1;}
#line 7647 "ripper.c" /* yacc.c:1646  */
    break;

  case 313:
#line 2417 "ripper.y" /* yacc.c:1646  */
    {
			p->in_defined = 0;
			(yyval.val) = new_defined(p, (yyvsp[-1].val), &(yyloc));
		    }
#line 7656 "ripper.c" /* yacc.c:1646  */
    break;

  case 314:
#line 2422 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_uni_op(p, method_cond(p, (yyvsp[-1].val), &(yylsp[-1])), METHOD_NOT, &(yylsp[-3]), &(yyloc));
		    }
#line 7664 "ripper.c" /* yacc.c:1646  */
    break;

  case 315:
#line 2426 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = call_uni_op(p, method_cond(p, new_nil(&(yylsp[-1])), &(yylsp[-1])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 7672 "ripper.c" /* yacc.c:1646  */
    break;

  case 316:
#line 2430 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = method_add_block(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8,v9;v1=(yyvsp[-1].val);v2=dispatch1(fcall,v1);v3=dispatch0(args_new);v4=v2;v5=v3;v6=dispatch2(method_add_arg,v4,v5);v7=v6;v8=(yyvsp[0].val);v9=dispatch2(method_add_block,v7,v8);(yyval.val)=v9;}
		    }
#line 7683 "ripper.c" /* yacc.c:1646  */
    break;

  case 318:
#line 2438 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			block_dup_check(p, (yyvsp[-1].val)->nd_args, (yyvsp[0].val));
			(yyval.val) = method_add_block(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(method_add_block,v1,v2);(yyval.val)=v3;}
		    }
#line 7695 "ripper.c" /* yacc.c:1646  */
    break;

  case 319:
#line 2446 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "->", &(yylsp[0]));
		    }
#line 7703 "ripper.c" /* yacc.c:1646  */
    break;

  case 320:
#line 2450 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
#if 0
                        nd_set_first_loc((yyval.val), (yylsp[-2]).beg_pos);
#endif
		    }
#line 7714 "ripper.c" /* yacc.c:1646  */
    break;

  case 321:
#line 2460 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_if(p, (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-4].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=escape_Qundef((yyvsp[-1].val));v4=dispatch3(if,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 7726 "ripper.c" /* yacc.c:1646  */
    break;

  case 322:
#line 2471 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_unless(p, (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-4].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=escape_Qundef((yyvsp[-1].val));v4=dispatch3(unless,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 7738 "ripper.c" /* yacc.c:1646  */
    break;

  case 323:
#line 2481 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_WHILE(cond(p, (yyvsp[-2].val), &(yylsp[-2])), (yyvsp[-1].val), 1, &(yyloc));
			fixpos((yyval.val), (yyvsp[-2].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(while,v1,v2);(yyval.val)=v3;}
		    }
#line 7750 "ripper.c" /* yacc.c:1646  */
    break;

  case 324:
#line 2491 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_UNTIL(cond(p, (yyvsp[-2].val), &(yylsp[-2])), (yyvsp[-1].val), 1, &(yyloc));
			fixpos((yyval.val), (yyvsp[-2].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(until,v1,v2);(yyval.val)=v3;}
		    }
#line 7762 "ripper.c" /* yacc.c:1646  */
    break;

  case 325:
#line 2501 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_CASE((yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-3].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[-1].val);v3=dispatch2(case,v1,v2);(yyval.val)=v3;}
		    }
#line 7774 "ripper.c" /* yacc.c:1646  */
    break;

  case 326:
#line 2509 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_CASE2((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=Qnil;v2=(yyvsp[-1].val);v3=dispatch2(case,v1,v2);(yyval.val)=v3;}
		    }
#line 7785 "ripper.c" /* yacc.c:1646  */
    break;

  case 327:
#line 2518 "ripper.y" /* yacc.c:1646  */
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
			rb_imemo_tmpbuf_t *tmpbuf = new_tmpbuf();
			ID *tbl = ALLOC_N(ID, 2);
			tbl[0] = 1 /* length of local var table */; tbl[1] = id /* internal id */;
			tmpbuf->ptr = (VALUE *)tbl;

			switch (nd_type((yyvsp[-4].val))) {
			  case NODE_LASGN:
			  case NODE_DASGN:
			  case NODE_DASGN_CURR: /* e.each {|internal_var| a = internal_var; ... } */
			    (yyvsp[-4].val)->nd_value = internal_var;
			    id = 0;
			    m->nd_plen = 1;
			    m->nd_next = (yyvsp[-4].val);
			    break;
			  case NODE_MASGN: /* e.each {|*internal_var| a, b, c = (internal_var.length == 1 && Array === (tmp = internal_var[0]) ? tmp : internal_var); ... } */
			    m->nd_next = node_assign(p, (yyvsp[-4].val), NEW_FOR_MASGN(internal_var, &(yylsp[-4])), &(yylsp[-4]));
			    break;
			  default: /* e.each {|*internal_var| @a, B, c[1], d.attr = internal_val; ... } */
			    m->nd_next = node_assign(p, NEW_MASGN(NEW_LIST((yyvsp[-4].val), &(yylsp[-4])), 0, &(yylsp[-4])), internal_var, &(yylsp[-4]));
			}
			/* {|*internal_id| <m> = internal_id; ... } */
			args = new_args(p, m, 0, id, 0, new_args_tail(p, 0, 0, 0, &(yylsp[-4])), &(yylsp[-4]));
			scope = NEW_NODE(NODE_SCOPE, tbl, (yyvsp[-1].val), args, &(yyloc));
			(yyval.val) = NEW_FOR((yyvsp[-2].val), scope, &(yyloc));
			fixpos((yyval.val), (yyvsp[-4].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-4].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(for,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 7832 "ripper.c" /* yacc.c:1646  */
    break;

  case 328:
#line 2561 "ripper.y" /* yacc.c:1646  */
    {
			if (p->in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[-1]));
			    yyerror1(&loc, "class definition in method body");
			}
			(yyvsp[-2].num) = p->in_class;
			p->in_class = 1;
			local_push(p, 0);
		    }
#line 7846 "ripper.c" /* yacc.c:1646  */
    break;

  case 329:
#line 2572 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_CLASS((yyvsp[-4].val), (yyvsp[-1].val), (yyvsp[-3].val), &(yyloc));
			nd_set_line((yyval.val)->nd_body, (yylsp[0]).end_pos.lineno);
			set_line_body((yyvsp[-1].val), (yylsp[-3]).end_pos.lineno);
			nd_set_line((yyval.val), (yylsp[-3]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-1].val);v4=dispatch3(class,v1,v2,v3);(yyval.val)=v4;}
			local_pop(p);
			p->in_class = (yyvsp[-5].num) & 1;
		    }
#line 7862 "ripper.c" /* yacc.c:1646  */
    break;

  case 330:
#line 2584 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.num) = (p->in_class << 1) | p->in_def;
			p->in_def = 0;
			p->in_class = 0;
			local_push(p, 0);
		    }
#line 7873 "ripper.c" /* yacc.c:1646  */
    break;

  case 331:
#line 2593 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_SCLASS((yyvsp[-4].val), (yyvsp[-1].val), &(yyloc));
			nd_set_line((yyval.val)->nd_body, (yylsp[0]).end_pos.lineno);
			set_line_body((yyvsp[-1].val), nd_line((yyvsp[-4].val)));
			fixpos((yyval.val), (yyvsp[-4].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-4].val);v2=(yyvsp[-1].val);v3=dispatch2(sclass,v1,v2);(yyval.val)=v3;}
			local_pop(p);
			p->in_def = (yyvsp[-3].num) & 1;
			p->in_class = ((yyvsp[-3].num) >> 1) & 1;
		    }
#line 7890 "ripper.c" /* yacc.c:1646  */
    break;

  case 332:
#line 2606 "ripper.y" /* yacc.c:1646  */
    {
			if (p->in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			    yyerror1(&loc, "module definition in method body");
			}
			(yyvsp[-1].num) = p->in_class;
			p->in_class = 1;
			local_push(p, 0);
		    }
#line 7904 "ripper.c" /* yacc.c:1646  */
    break;

  case 333:
#line 2617 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MODULE((yyvsp[-3].val), (yyvsp[-1].val), &(yyloc));
			nd_set_line((yyval.val)->nd_body, (yylsp[0]).end_pos.lineno);
			set_line_body((yyvsp[-1].val), (yylsp[-3]).end_pos.lineno);
			nd_set_line((yyval.val), (yylsp[-3]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=(yyvsp[-1].val);v3=dispatch2(module,v1,v2);(yyval.val)=v3;}
			local_pop(p);
			p->in_class = (yyvsp[-4].num) & 1;
		    }
#line 7920 "ripper.c" /* yacc.c:1646  */
    break;

  case 334:
#line 2629 "ripper.y" /* yacc.c:1646  */
    {
			local_push(p, 0);
			(yyval.id) = p->cur_arg;
			p->cur_arg = 0;
		    }
#line 7930 "ripper.c" /* yacc.c:1646  */
    break;

  case 335:
#line 2634 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.num) = p->in_def;
			p->in_def = 1;
		    }
#line 7939 "ripper.c" /* yacc.c:1646  */
    break;

  case 336:
#line 2641 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			NODE *body = remove_begin((yyvsp[-1].val));
			reduce_nodes(p, &body);
			(yyval.val) = NEW_DEFN((yyvsp[-5].val), (yyvsp[-2].val), body, &(yyloc));
			nd_set_line((yyval.val)->nd_defn, (yylsp[0]).end_pos.lineno);
			set_line_body(body, (yylsp[-6]).beg_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-5].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(def,v1,v2,v3);(yyval.val)=v4;}
			local_pop(p);
			p->in_def = (yyvsp[-3].num) & 1;
			p->cur_arg = (yyvsp[-4].id);
		    }
#line 7957 "ripper.c" /* yacc.c:1646  */
    break;

  case 337:
#line 2654 "ripper.y" /* yacc.c:1646  */
    {SET_LEX_STATE(EXPR_FNAME);}
#line 7963 "ripper.c" /* yacc.c:1646  */
    break;

  case 338:
#line 2655 "ripper.y" /* yacc.c:1646  */
    {
			(yyvsp[-1].num) = p->in_def;
			p->in_def = 1;
			SET_LEX_STATE(EXPR_ENDFN|EXPR_LABEL); /* force for args */
			local_push(p, 0);
			(yyval.id) = p->cur_arg;
			p->cur_arg = 0;
		    }
#line 7976 "ripper.c" /* yacc.c:1646  */
    break;

  case 339:
#line 2666 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			NODE *body = remove_begin((yyvsp[-1].val));
			reduce_nodes(p, &body);
			(yyval.val) = NEW_DEFS((yyvsp[-7].val), (yyvsp[-4].val), (yyvsp[-2].val), body, &(yyloc));
			nd_set_line((yyval.val)->nd_defn, (yylsp[0]).end_pos.lineno);
			set_line_body(body, (yylsp[-8]).beg_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-7].val);v2=(yyvsp[-6].val);v3=(yyvsp[-4].val);v4=(yyvsp[-2].val);v5=(yyvsp[-1].val);v6=dispatch5(defs,v1,v2,v3,v4,v5);(yyval.val)=v6;}
			local_pop(p);
			p->in_def = (yyvsp[-5].num) & 1;
			p->cur_arg = (yyvsp[-3].id);
		    }
#line 7994 "ripper.c" /* yacc.c:1646  */
    break;

  case 340:
#line 2680 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_BREAK(0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=dispatch0(args_new);v2=v1;v3=dispatch1(break,v2);(yyval.val)=v3;}
		    }
#line 8005 "ripper.c" /* yacc.c:1646  */
    break;

  case 341:
#line 2687 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_NEXT(0, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=dispatch0(args_new);v2=v1;v3=dispatch1(next,v2);(yyval.val)=v3;}
		    }
#line 8016 "ripper.c" /* yacc.c:1646  */
    break;

  case 342:
#line 2694 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_REDO(&(yyloc));
#endif
			{VALUE v1;v1=dispatch0(redo);(yyval.val)=v1;}
		    }
#line 8027 "ripper.c" /* yacc.c:1646  */
    break;

  case 343:
#line 2701 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_RETRY(&(yyloc));
#endif
			{VALUE v1;v1=dispatch0(retry);(yyval.val)=v1;}
		    }
#line 8038 "ripper.c" /* yacc.c:1646  */
    break;

  case 344:
#line 2710 "ripper.y" /* yacc.c:1646  */
    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 8047 "ripper.c" /* yacc.c:1646  */
    break;

  case 345:
#line 2717 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "begin", &(yyloc));
		    }
#line 8055 "ripper.c" /* yacc.c:1646  */
    break;

  case 346:
#line 2723 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "if", &(yyloc));
		    }
#line 8063 "ripper.c" /* yacc.c:1646  */
    break;

  case 347:
#line 2729 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "unless", &(yyloc));
		    }
#line 8071 "ripper.c" /* yacc.c:1646  */
    break;

  case 348:
#line 2735 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "while", &(yyloc));
		    }
#line 8079 "ripper.c" /* yacc.c:1646  */
    break;

  case 349:
#line 2741 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "until", &(yyloc));
		    }
#line 8087 "ripper.c" /* yacc.c:1646  */
    break;

  case 350:
#line 2747 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "case", &(yyloc));
		    }
#line 8095 "ripper.c" /* yacc.c:1646  */
    break;

  case 351:
#line 2753 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "for", &(yyloc));
		    }
#line 8103 "ripper.c" /* yacc.c:1646  */
    break;

  case 352:
#line 2759 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "class", &(yyloc));
		    }
#line 8111 "ripper.c" /* yacc.c:1646  */
    break;

  case 353:
#line 2765 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "module", &(yyloc));
		    }
#line 8119 "ripper.c" /* yacc.c:1646  */
    break;

  case 354:
#line 2771 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "def", &(yyloc));
		    }
#line 8127 "ripper.c" /* yacc.c:1646  */
    break;

  case 355:
#line 2777 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "do", &(yyloc));
		    }
#line 8135 "ripper.c" /* yacc.c:1646  */
    break;

  case 356:
#line 2783 "ripper.y" /* yacc.c:1646  */
    {
			token_info_push(p, "do", &(yyloc));
		    }
#line 8143 "ripper.c" /* yacc.c:1646  */
    break;

  case 357:
#line 2789 "ripper.y" /* yacc.c:1646  */
    {
			token_info_warn(p, "rescue", p->token_info, 1, &(yyloc));
		    }
#line 8151 "ripper.c" /* yacc.c:1646  */
    break;

  case 358:
#line 2795 "ripper.y" /* yacc.c:1646  */
    {
			token_info_warn(p, "ensure", p->token_info, 1, &(yyloc));
		    }
#line 8159 "ripper.c" /* yacc.c:1646  */
    break;

  case 359:
#line 2801 "ripper.y" /* yacc.c:1646  */
    {
			token_info_warn(p, "when", p->token_info, 0, &(yyloc));
		    }
#line 8167 "ripper.c" /* yacc.c:1646  */
    break;

  case 360:
#line 2807 "ripper.y" /* yacc.c:1646  */
    {
			token_info *ptinfo_beg = p->token_info;
			int same = ptinfo_beg && strcmp(ptinfo_beg->token, "case") != 0;
			token_info_warn(p, "else", p->token_info, same, &(yyloc));
		    }
#line 8177 "ripper.c" /* yacc.c:1646  */
    break;

  case 361:
#line 2815 "ripper.y" /* yacc.c:1646  */
    {
			token_info_warn(p, "elsif", p->token_info, 1, &(yyloc));
		    }
#line 8185 "ripper.c" /* yacc.c:1646  */
    break;

  case 362:
#line 2821 "ripper.y" /* yacc.c:1646  */
    {
			token_info_pop(p, "end", &(yyloc));
		    }
#line 8193 "ripper.c" /* yacc.c:1646  */
    break;

  case 363:
#line 2827 "ripper.y" /* yacc.c:1646  */
    {
			if (p->in_class && !p->in_def && !dyna_in_block(p))
			    yyerror1(&(yylsp[0]), "Invalid return in class/module body");
		    }
#line 8202 "ripper.c" /* yacc.c:1646  */
    break;

  case 370:
#line 2846 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_if(p, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-3].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-3].val);v2=(yyvsp[-1].val);v3=escape_Qundef((yyvsp[0].val));v4=dispatch3(elsif,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 8214 "ripper.c" /* yacc.c:1646  */
    break;

  case 372:
#line 2857 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(else,v1);(yyval.val)=v2;}
		    }
#line 8225 "ripper.c" /* yacc.c:1646  */
    break;

  case 375:
#line 2870 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, (yyvsp[0].val));
		    }
#line 8236 "ripper.c" /* yacc.c:1646  */
    break;

  case 376:
#line 2877 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(mlhs_paren,v1);(yyval.val)=v2;}
		    }
#line 8247 "ripper.c" /* yacc.c:1646  */
    break;

  case 377:
#line 2886 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(mlhs_add,v2,v3);(yyval.val)=v4;}
		    }
#line 8258 "ripper.c" /* yacc.c:1646  */
    break;

  case 378:
#line 2893 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(mlhs_add,v1,v2);(yyval.val)=v3;}
		    }
#line 8269 "ripper.c" /* yacc.c:1646  */
    break;

  case 379:
#line 2902 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 8280 "ripper.c" /* yacc.c:1646  */
    break;

  case 380:
#line 2909 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-3].val), assignable(p, (yyvsp[0].val), 0, &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=assignable(p, (yyvsp[0].val));v3=dispatch2(mlhs_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 8291 "ripper.c" /* yacc.c:1646  */
    break;

  case 381:
#line 2916 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-5].val), NEW_POSTARG(assignable(p, (yyvsp[-2].val), 0, &(yyloc)), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-5].val);v2=assignable(p, (yyvsp[-2].val));v3=dispatch2(mlhs_add_star,v1,v2);v4=v3;v5=(yyvsp[0].val);v6=dispatch2(mlhs_add_post,v4,v5);(yyval.val)=v6;}
		    }
#line 8302 "ripper.c" /* yacc.c:1646  */
    break;

  case 382:
#line 2923 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-2].val), NODE_SPECIAL_NO_NAME_REST, &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=Qnil;v3=dispatch2(mlhs_add_star,v1,v2);(yyval.val)=v3;}
		    }
#line 8313 "ripper.c" /* yacc.c:1646  */
    break;

  case 383:
#line 2930 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN((yyvsp[-4].val), NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6;v1=(yyvsp[-4].val);v2=Qnil;v3=dispatch2(mlhs_add_star,v1,v2);v4=v3;v5=(yyvsp[0].val);v6=dispatch2(mlhs_add_post,v4,v5);(yyval.val)=v6;}
		    }
#line 8324 "ripper.c" /* yacc.c:1646  */
    break;

  case 384:
#line 2937 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(0, assignable(p, (yyvsp[0].val), 0, &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=assignable(p, (yyvsp[0].val));v4=dispatch2(mlhs_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 8335 "ripper.c" /* yacc.c:1646  */
    break;

  case 385:
#line 2944 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(0, NEW_POSTARG(assignable(p, (yyvsp[-2].val), 0, &(yyloc)), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=dispatch0(mlhs_new);v2=v1;v3=assignable(p, (yyvsp[-2].val));v4=dispatch2(mlhs_add_star,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(mlhs_add_post,v5,v6);(yyval.val)=v7;}
		    }
#line 8346 "ripper.c" /* yacc.c:1646  */
    break;

  case 386:
#line 2951 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(0, NODE_SPECIAL_NO_NAME_REST, &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=dispatch0(mlhs_new);v2=v1;v3=Qnil;v4=dispatch2(mlhs_add_star,v2,v3);(yyval.val)=v4;}
		    }
#line 8357 "ripper.c" /* yacc.c:1646  */
    break;

  case 387:
#line 2958 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_MASGN(0, NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=dispatch0(mlhs_new);v2=v1;v3=Qnil;v4=dispatch2(mlhs_add_star,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(mlhs_add_post,v5,v6);(yyval.val)=v7;}
		    }
#line 8368 "ripper.c" /* yacc.c:1646  */
    break;

  case 388:
#line 2968 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 8376 "ripper.c" /* yacc.c:1646  */
    break;

  case 389:
#line 2972 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 8384 "ripper.c" /* yacc.c:1646  */
    break;

  case 390:
#line 2976 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 8392 "ripper.c" /* yacc.c:1646  */
    break;

  case 391:
#line 2980 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].val), &(yylsp[0]));
		    }
#line 8400 "ripper.c" /* yacc.c:1646  */
    break;

  case 392:
#line 2986 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 8408 "ripper.c" /* yacc.c:1646  */
    break;

  case 393:
#line 2990 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 8416 "ripper.c" /* yacc.c:1646  */
    break;

  case 394:
#line 2996 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 8424 "ripper.c" /* yacc.c:1646  */
    break;

  case 395:
#line 3000 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-7].val), (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 8432 "ripper.c" /* yacc.c:1646  */
    break;

  case 396:
#line 3004 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-3].val), (yyvsp[-1].val), Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 8440 "ripper.c" /* yacc.c:1646  */
    break;

  case 397:
#line 3008 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), (yyvsp[-3].val), Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 8448 "ripper.c" /* yacc.c:1646  */
    break;

  case 398:
#line 3012 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 8456 "ripper.c" /* yacc.c:1646  */
    break;

  case 399:
#line 3016 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			/* magic number for rest_id in iseq_set_arguments() */
			const ID excessed_comma = 1;
			(yyval.val) = new_args(p, (yyvsp[-1].val), Qnone, excessed_comma, Qnone, new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[-1])), &(yyloc));
#endif
			{VALUE v1;v1=dispatch0(excessed_comma);(yyval.val)=new_args(p, (yyvsp[-1].val), Qnone, v1, Qnone, new_args_tail(p, Qnone, Qnone, Qnone, NULL), NULL);}
		    }
#line 8469 "ripper.c" /* yacc.c:1646  */
    break;

  case 400:
#line 3025 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), Qnone, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 8477 "ripper.c" /* yacc.c:1646  */
    break;

  case 401:
#line 3029 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-1].val), Qnone, Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 8485 "ripper.c" /* yacc.c:1646  */
    break;

  case 402:
#line 3033 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-3].val), (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 8493 "ripper.c" /* yacc.c:1646  */
    break;

  case 403:
#line 3037 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 8501 "ripper.c" /* yacc.c:1646  */
    break;

  case 404:
#line 3041 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-1].val), Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 8509 "ripper.c" /* yacc.c:1646  */
    break;

  case 405:
#line 3045 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 8517 "ripper.c" /* yacc.c:1646  */
    break;

  case 406:
#line 3049 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, Qnone, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 8525 "ripper.c" /* yacc.c:1646  */
    break;

  case 407:
#line 3053 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, Qnone, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 8533 "ripper.c" /* yacc.c:1646  */
    break;

  case 408:
#line 3057 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 8541 "ripper.c" /* yacc.c:1646  */
    break;

  case 410:
#line 3064 "ripper.y" /* yacc.c:1646  */
    {
			p->command_start = TRUE;
		    }
#line 8549 "ripper.c" /* yacc.c:1646  */
    break;

  case 411:
#line 3070 "ripper.y" /* yacc.c:1646  */
    {
			p->cur_arg = 0;
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1,v2,v3;v1=params_new(Qnil, Qnil, Qnil, Qnil, Qnil, Qnil, Qnil);v2=escape_Qundef((yyvsp[-1].val));v3=dispatch2(block_var,v1,v2);(yyval.val)=v3;}
		    }
#line 8561 "ripper.c" /* yacc.c:1646  */
    break;

  case 412:
#line 3078 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1,v2,v3;v1=params_new(Qnil, Qnil, Qnil, Qnil, Qnil, Qnil, Qnil);v2=Qnil;v3=dispatch2(block_var,v1,v2);(yyval.val)=v3;}
		    }
#line 8572 "ripper.c" /* yacc.c:1646  */
    break;

  case 413:
#line 3085 "ripper.y" /* yacc.c:1646  */
    {
			p->cur_arg = 0;
#if 0
			(yyval.val) = (yyvsp[-2].val);
#endif
			{VALUE v1,v2,v3;v1=escape_Qundef((yyvsp[-2].val));v2=escape_Qundef((yyvsp[-1].val));v3=dispatch2(block_var,v1,v2);(yyval.val)=v3;}
		    }
#line 8584 "ripper.c" /* yacc.c:1646  */
    break;

  case 414:
#line 3096 "ripper.y" /* yacc.c:1646  */
    {
		      (yyval.val) = 0;
		    }
#line 8592 "ripper.c" /* yacc.c:1646  */
    break;

  case 415:
#line 3100 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			(yyval.val)=(yyvsp[-1].val);
		    }
#line 8603 "ripper.c" /* yacc.c:1646  */
    break;

  case 416:
#line 3109 "ripper.y" /* yacc.c:1646  */
    {(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));}
#line 8609 "ripper.c" /* yacc.c:1646  */
    break;

  case 417:
#line 3111 "ripper.y" /* yacc.c:1646  */
    {(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));}
#line 8615 "ripper.c" /* yacc.c:1646  */
    break;

  case 418:
#line 3115 "ripper.y" /* yacc.c:1646  */
    {
			new_bv(p, get_id((yyvsp[0].val)));
			(yyval.val)=get_value((yyvsp[0].val));
		    }
#line 8624 "ripper.c" /* yacc.c:1646  */
    break;

  case 419:
#line 3120 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = 0;
		    }
#line 8632 "ripper.c" /* yacc.c:1646  */
    break;

  case 420:
#line 3125 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.vars) = dyna_push(p);
		    }
#line 8640 "ripper.c" /* yacc.c:1646  */
    break;

  case 421:
#line 3128 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.num) = p->lex.lpar_beg;
			p->lex.lpar_beg = p->lex.paren_nest;
		    }
#line 8649 "ripper.c" /* yacc.c:1646  */
    break;

  case 422:
#line 3133 "ripper.y" /* yacc.c:1646  */
    {
			CMDARG_PUSH(0);
		    }
#line 8657 "ripper.c" /* yacc.c:1646  */
    break;

  case 423:
#line 3137 "ripper.y" /* yacc.c:1646  */
    {
			p->lex.lpar_beg = (yyvsp[-3].num);
			CMDARG_POP();
#if 0
                        {
                            YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
                            (yyval.val) = NEW_LAMBDA((yyvsp[-2].val), (yyvsp[0].val), &loc);
                            nd_set_line((yyval.val)->nd_body, (yylsp[0]).end_pos.lineno);
                            nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
                        }
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(lambda,v1,v2);(yyval.val)=v3;}
			dyna_pop(p, (yyvsp[-4].vars));
		    }
#line 8676 "ripper.c" /* yacc.c:1646  */
    break;

  case 424:
#line 3154 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-2].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-2].val);v2=dispatch1(paren,v1);(yyval.val)=v2;}
		    }
#line 8687 "ripper.c" /* yacc.c:1646  */
    break;

  case 425:
#line 3161 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 8695 "ripper.c" /* yacc.c:1646  */
    break;

  case 426:
#line 3167 "ripper.y" /* yacc.c:1646  */
    {
			token_info_pop(p, "}", &(yylsp[0]));
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 8704 "ripper.c" /* yacc.c:1646  */
    break;

  case 427:
#line 3172 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 8712 "ripper.c" /* yacc.c:1646  */
    break;

  case 428:
#line 3178 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[-1].val);
#if 0
			(yyval.val)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
#endif
		    }
#line 8724 "ripper.c" /* yacc.c:1646  */
    break;

  case 429:
#line 3188 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			if (nd_type((yyvsp[-1].val)) == NODE_YIELD) {
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
#line 8742 "ripper.c" /* yacc.c:1646  */
    break;

  case 430:
#line 3202 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_qcall(p, (yyvsp[-2].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=v6==Qundef ? v5 : dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 8753 "ripper.c" /* yacc.c:1646  */
    break;

  case 431:
#line 3209 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_command_qcall(p, (yyvsp[-3].val), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-2]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=(yyvsp[-1].val);v5=dispatch4(command_call,v1,v2,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=v7==Qundef ? v6 : dispatch2(method_add_block,v6,v7);(yyval.val)=v8;}
		    }
#line 8764 "ripper.c" /* yacc.c:1646  */
    break;

  case 432:
#line 3216 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_command_qcall(p, (yyvsp[-3].val), (yyvsp[-4].val), (yyvsp[-2].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-2]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7,v8;v1=(yyvsp[-4].val);v2=(yyvsp[-3].val);v3=(yyvsp[-2].val);v4=(yyvsp[-1].val);v5=dispatch4(command_call,v1,v2,v3,v4);v6=v5;v7=(yyvsp[0].val);v8=dispatch2(method_add_block,v6,v7);(yyval.val)=v8;}
		    }
#line 8775 "ripper.c" /* yacc.c:1646  */
    break;

  case 433:
#line 3225 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
			(yyval.val)->nd_args = (yyvsp[0].val);
			nd_set_last_loc((yyvsp[-1].val), (yylsp[0]).end_pos);
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-1].val);v2=dispatch1(fcall,v1);v3=v2;v4=(yyvsp[0].val);v5=dispatch2(method_add_arg,v3,v4);(yyval.val)=v5;}
		    }
#line 8788 "ripper.c" /* yacc.c:1646  */
    break;

  case 434:
#line 3234 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_qcall(p, (yyvsp[-2].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-1]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-3].val);v2=(yyvsp[-2].val);v3=(yyvsp[-1].val);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=v6==Qundef ? v5 : dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 8800 "ripper.c" /* yacc.c:1646  */
    break;

  case 435:
#line 3242 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-1]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-3].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[-1].val);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 8812 "ripper.c" /* yacc.c:1646  */
    break;

  case 436:
#line 3250 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].val), (yyvsp[0].val), Qnull, &(yylsp[0]), &(yyloc));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-2].val);v2=ID2VAL(idCOLON2);v3=(yyvsp[0].val);v4=dispatch3(call,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 8823 "ripper.c" /* yacc.c:1646  */
    break;

  case 437:
#line 3257 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_qcall(p, (yyvsp[-1].val), (yyvsp[-2].val), ID2VAL(idCall), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-1]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=ID2VAL(idCall);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 8835 "ripper.c" /* yacc.c:1646  */
    break;

  case 438:
#line 3265 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].val), ID2VAL(idCall), (yyvsp[0].val), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.val), (yylsp[-1]).end_pos.lineno);
#endif
			{VALUE v1,v2,v3,v4,v5,v6,v7;v1=(yyvsp[-2].val);v2=ID2VAL(idCOLON2);v3=ID2VAL(idCall);v4=dispatch3(call,v1,v2,v3);v5=v4;v6=(yyvsp[0].val);v7=dispatch2(method_add_arg,v5,v6);(yyval.val)=v7;}
		    }
#line 8847 "ripper.c" /* yacc.c:1646  */
    break;

  case 439:
#line 3273 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_SUPER((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(super,v1);(yyval.val)=v2;}
		    }
#line 8858 "ripper.c" /* yacc.c:1646  */
    break;

  case 440:
#line 3280 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_ZSUPER(&(yyloc));
#endif
			{VALUE v1;v1=dispatch0(zsuper);(yyval.val)=v1;}
		    }
#line 8869 "ripper.c" /* yacc.c:1646  */
    break;

  case 441:
#line 3287 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			if ((yyvsp[-3].val) && nd_type((yyvsp[-3].val)) == NODE_SELF)
			    (yyval.val) = NEW_FCALL(tAREF, (yyvsp[-1].val), &(yyloc));
			else
			    (yyval.val) = NEW_CALL((yyvsp[-3].val), tAREF, (yyvsp[-1].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-3].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-3].val);v2=escape_Qundef((yyvsp[-1].val));v3=dispatch2(aref,v1,v2);(yyval.val)=v3;}
		    }
#line 8884 "ripper.c" /* yacc.c:1646  */
    break;

  case 442:
#line 3300 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[-1].val);
#if 0
			(yyval.val)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
#endif
		    }
#line 8896 "ripper.c" /* yacc.c:1646  */
    break;

  case 443:
#line 3308 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[-1].val);
#if 0
			(yyval.val)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.val), (yylsp[-2]).end_pos.lineno);
#endif
		    }
#line 8908 "ripper.c" /* yacc.c:1646  */
    break;

  case 444:
#line 3317 "ripper.y" /* yacc.c:1646  */
    {(yyval.vars) = dyna_push(p);}
#line 8914 "ripper.c" /* yacc.c:1646  */
    break;

  case 445:
#line 3319 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_ITER((yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=escape_Qundef((yyvsp[-1].val));v2=(yyvsp[0].val);v3=dispatch2(brace_block,v1,v2);(yyval.val)=v3;}
			dyna_pop(p, (yyvsp[-2].vars));
		    }
#line 8926 "ripper.c" /* yacc.c:1646  */
    break;

  case 446:
#line 3328 "ripper.y" /* yacc.c:1646  */
    {(yyval.vars) = dyna_push(p);}
#line 8932 "ripper.c" /* yacc.c:1646  */
    break;

  case 447:
#line 3329 "ripper.y" /* yacc.c:1646  */
    {CMDARG_PUSH(0);}
#line 8938 "ripper.c" /* yacc.c:1646  */
    break;

  case 448:
#line 3331 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_ITER((yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=escape_Qundef((yyvsp[-1].val));v2=(yyvsp[0].val);v3=dispatch2(do_block,v1,v2);(yyval.val)=v3;}
			CMDARG_POP();
			dyna_pop(p, (yyvsp[-3].vars));
		    }
#line 8951 "ripper.c" /* yacc.c:1646  */
    break;

  case 449:
#line 3344 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_WHEN((yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-3].val));
#endif
			{VALUE v1,v2,v3,v4;v1=(yyvsp[-3].val);v2=(yyvsp[-1].val);v3=escape_Qundef((yyvsp[0].val));v4=dispatch3(when,v1,v2,v3);(yyval.val)=v4;}
		    }
#line 8963 "ripper.c" /* yacc.c:1646  */
    break;

  case 452:
#line 3360 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_RESBODY((yyvsp[-4].val),
					 (yyvsp[-3].val) ? block_append(p, node_assign(p, (yyvsp[-3].val), NEW_ERRINFO(&(yylsp[-3])), &(yylsp[-3])), (yyvsp[-1].val)) : (yyvsp[-1].val),
					 (yyvsp[0].val), &(yyloc));
			fixpos((yyval.val), (yyvsp[-4].val)?(yyvsp[-4].val):(yyvsp[-1].val));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=escape_Qundef((yyvsp[-4].val));v2=escape_Qundef((yyvsp[-3].val));v3=escape_Qundef((yyvsp[-1].val));v4=escape_Qundef((yyvsp[0].val));v5=dispatch4(rescue,v1,v2,v3,v4);(yyval.val)=v5;}
		    }
#line 8977 "ripper.c" /* yacc.c:1646  */
    break;

  case 454:
#line 3373 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_LIST((yyvsp[0].val), &(yyloc));
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 8988 "ripper.c" /* yacc.c:1646  */
    break;

  case 455:
#line 3380 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			if (!((yyval.val) = splat_array((yyvsp[0].val)))) (yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 8999 "ripper.c" /* yacc.c:1646  */
    break;

  case 457:
#line 3390 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 9007 "ripper.c" /* yacc.c:1646  */
    break;

  case 459:
#line 3397 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(ensure,v1);(yyval.val)=v2;}
		    }
#line 9018 "ripper.c" /* yacc.c:1646  */
    break;

  case 462:
#line 3408 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_LIT(ID2SYM((yyvsp[0].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(symbol_literal,v1);(yyval.val)=v2;}
		    }
#line 9029 "ripper.c" /* yacc.c:1646  */
    break;

  case 464:
#line 3418 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			NODE *node = (yyvsp[0].val);
			if (!node) {
			    node = NEW_STR(add_mark_object(p, STR_NEW0()), &(yyloc));
			}
			else {
			    node = evstr2dstr(p, node);
			}
			(yyval.val) = node;
#endif
			(yyval.val)=(yyvsp[0].val);
		    }
#line 9047 "ripper.c" /* yacc.c:1646  */
    break;

  case 467:
#line 3436 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = literal_concat(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(string_concat,v1,v2);(yyval.val)=v3;}
		    }
#line 9058 "ripper.c" /* yacc.c:1646  */
    break;

  case 468:
#line 3445 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = heredoc_dedent(p, (yyvsp[-1].val));
			if ((yyval.val)) nd_set_loc((yyval.val), &(yyloc));
#endif
			{VALUE v1,v2;v1=heredoc_dedent(p, (yyvsp[-1].val));v2=dispatch1(string_literal,v1);(yyval.val)=v2;}
		    }
#line 9070 "ripper.c" /* yacc.c:1646  */
    break;

  case 469:
#line 3455 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_xstring(p, heredoc_dedent(p, (yyvsp[-1].val)), &(yyloc));
#endif
			{VALUE v1,v2;v1=heredoc_dedent(p, (yyvsp[-1].val));v2=dispatch1(xstring_literal,v1);(yyval.val)=v2;}
		    }
#line 9081 "ripper.c" /* yacc.c:1646  */
    break;

  case 470:
#line 3464 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_regexp(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9089 "ripper.c" /* yacc.c:1646  */
    break;

  case 471:
#line 3470 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = make_array((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 9100 "ripper.c" /* yacc.c:1646  */
    break;

  case 472:
#line 3479 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(words_new);(yyval.val)=v1;}
		    }
#line 9111 "ripper.c" /* yacc.c:1646  */
    break;

  case 473:
#line 3486 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), evstr2dstr(p, (yyvsp[-1].val)));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(words_add,v1,v2);(yyval.val)=v3;}
		    }
#line 9122 "ripper.c" /* yacc.c:1646  */
    break;

  case 474:
#line 3495 "ripper.y" /* yacc.c:1646  */
    {{VALUE v1,v2,v3,v4;v1=dispatch0(word_new);v2=v1;v3=(yyvsp[0].val);v4=dispatch2(word_add,v2,v3);(yyval.val)=v4;}}
#line 9128 "ripper.c" /* yacc.c:1646  */
    break;

  case 475:
#line 3497 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = literal_concat(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(word_add,v1,v2);(yyval.val)=v3;}
		    }
#line 9139 "ripper.c" /* yacc.c:1646  */
    break;

  case 476:
#line 3506 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = make_array((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 9150 "ripper.c" /* yacc.c:1646  */
    break;

  case 477:
#line 3515 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(symbols_new);(yyval.val)=v1;}
		    }
#line 9161 "ripper.c" /* yacc.c:1646  */
    break;

  case 478:
#line 3522 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = symbol_append(p, (yyvsp[-2].val), evstr2dstr(p, (yyvsp[-1].val)));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(symbols_add,v1,v2);(yyval.val)=v3;}
		    }
#line 9172 "ripper.c" /* yacc.c:1646  */
    break;

  case 479:
#line 3531 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = make_array((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 9183 "ripper.c" /* yacc.c:1646  */
    break;

  case 480:
#line 3540 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = make_array((yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(array,v1);(yyval.val)=v2;}
		    }
#line 9194 "ripper.c" /* yacc.c:1646  */
    break;

  case 481:
#line 3549 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(qwords_new);(yyval.val)=v1;}
		    }
#line 9205 "ripper.c" /* yacc.c:1646  */
    break;

  case 482:
#line 3556 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = list_append(p, (yyvsp[-2].val), (yyvsp[-1].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(qwords_add,v1,v2);(yyval.val)=v3;}
		    }
#line 9216 "ripper.c" /* yacc.c:1646  */
    break;

  case 483:
#line 3565 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(qsymbols_new);(yyval.val)=v1;}
		    }
#line 9227 "ripper.c" /* yacc.c:1646  */
    break;

  case 484:
#line 3572 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = symbol_append(p, (yyvsp[-2].val), (yyvsp[-1].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[-1].val);v3=dispatch2(qsymbols_add,v1,v2);(yyval.val)=v3;}
		    }
#line 9238 "ripper.c" /* yacc.c:1646  */
    break;

  case 485:
#line 3581 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(string_content);(yyval.val)=v1;}
		    }
#line 9249 "ripper.c" /* yacc.c:1646  */
    break;

  case 486:
#line 3588 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = literal_concat(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(string_add,v1,v2);(yyval.val)=v3;}
		    }
#line 9260 "ripper.c" /* yacc.c:1646  */
    break;

  case 487:
#line 3597 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(xstring_new);(yyval.val)=v1;}
		    }
#line 9271 "ripper.c" /* yacc.c:1646  */
    break;

  case 488:
#line 3604 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = literal_concat(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(xstring_add,v1,v2);(yyval.val)=v3;}
		    }
#line 9282 "ripper.c" /* yacc.c:1646  */
    break;

  case 489:
#line 3613 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			{VALUE v1;v1=dispatch0(regexp_new);(yyval.val)=v1;}
#if 0
#endif
			(yyval.val) = ripper_new_yylval(p, 0, (yyval.val), 0);

		    }
#line 9297 "ripper.c" /* yacc.c:1646  */
    break;

  case 490:
#line 3624 "ripper.y" /* yacc.c:1646  */
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
#line 9340 "ripper.c" /* yacc.c:1646  */
    break;

  case 492:
#line 3666 "ripper.y" /* yacc.c:1646  */
    {
			/* need to backup p->lex.strterm so that a string literal `%&foo,#$&,bar&` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 9351 "ripper.c" /* yacc.c:1646  */
    break;

  case 493:
#line 3673 "ripper.y" /* yacc.c:1646  */
    {
			p->lex.strterm = (yyvsp[-1].strterm);
#if 0
			(yyval.val) = NEW_EVSTR((yyvsp[0].val), &(yyloc));
			nd_set_line((yyval.val), (yylsp[0]).end_pos.lineno);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(string_dvar,v1);(yyval.val)=v2;}
		    }
#line 9364 "ripper.c" /* yacc.c:1646  */
    break;

  case 494:
#line 3682 "ripper.y" /* yacc.c:1646  */
    {
			CMDARG_PUSH(0);
			COND_PUSH(0);
		    }
#line 9373 "ripper.c" /* yacc.c:1646  */
    break;

  case 495:
#line 3686 "ripper.y" /* yacc.c:1646  */
    {
			/* need to backup p->lex.strterm so that a string literal `%!foo,#{ !0 },bar!` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
		    }
#line 9383 "ripper.c" /* yacc.c:1646  */
    break;

  case 496:
#line 3691 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.num) = p->lex.state;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 9392 "ripper.c" /* yacc.c:1646  */
    break;

  case 497:
#line 3695 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.num) = p->lex.brace_nest;
			p->lex.brace_nest = 0;
		    }
#line 9401 "ripper.c" /* yacc.c:1646  */
    break;

  case 498:
#line 3699 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.num) = p->heredoc_indent;
			p->heredoc_indent = 0;
		    }
#line 9410 "ripper.c" /* yacc.c:1646  */
    break;

  case 499:
#line 3704 "ripper.y" /* yacc.c:1646  */
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
#line 9429 "ripper.c" /* yacc.c:1646  */
    break;

  case 500:
#line 3721 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_GVAR((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 9440 "ripper.c" /* yacc.c:1646  */
    break;

  case 501:
#line 3728 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_IVAR((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 9451 "ripper.c" /* yacc.c:1646  */
    break;

  case 502:
#line 3735 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = NEW_CVAR((yyvsp[0].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 9462 "ripper.c" /* yacc.c:1646  */
    break;

  case 504:
#line 3745 "ripper.y" /* yacc.c:1646  */
    {
			SET_LEX_STATE(EXPR_END);
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(symbol,v1);(yyval.val)=v2;}
		    }
#line 9474 "ripper.c" /* yacc.c:1646  */
    break;

  case 509:
#line 3761 "ripper.y" /* yacc.c:1646  */
    {
			SET_LEX_STATE(EXPR_END);
#if 0
			(yyval.val) = dsym_node(p, (yyvsp[-1].val), &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(dyna_symbol,v1);(yyval.val)=v2;}
		    }
#line 9486 "ripper.c" /* yacc.c:1646  */
    break;

  case 511:
#line 3772 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[0].val);
			add_mark_object(p, (yyval.val)->nd_lit = negate_lit(p, (yyval.val)->nd_lit));
#endif
			{VALUE v1,v2,v3;v1=ID2VAL(idUMinus);v2=(yyvsp[0].val);v3=dispatch2(unary,v1,v2);(yyval.val)=v3;}
		    }
#line 9498 "ripper.c" /* yacc.c:1646  */
    break;

  case 521:
#line 3794 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = KWD2EID(nil, (yyvsp[0].val));}
#line 9504 "ripper.c" /* yacc.c:1646  */
    break;

  case 522:
#line 3795 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = KWD2EID(self, (yyvsp[0].val));}
#line 9510 "ripper.c" /* yacc.c:1646  */
    break;

  case 523:
#line 3796 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = KWD2EID(true, (yyvsp[0].val));}
#line 9516 "ripper.c" /* yacc.c:1646  */
    break;

  case 524:
#line 3797 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = KWD2EID(false, (yyvsp[0].val));}
#line 9522 "ripper.c" /* yacc.c:1646  */
    break;

  case 525:
#line 3798 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = KWD2EID(_FILE__, (yyvsp[0].val));}
#line 9528 "ripper.c" /* yacc.c:1646  */
    break;

  case 526:
#line 3799 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = KWD2EID(_LINE__, (yyvsp[0].val));}
#line 9534 "ripper.c" /* yacc.c:1646  */
    break;

  case 527:
#line 3800 "ripper.y" /* yacc.c:1646  */
    {(yyval.val) = KWD2EID(_ENCODING__, (yyvsp[0].val));}
#line 9540 "ripper.c" /* yacc.c:1646  */
    break;

  case 528:
#line 3804 "ripper.y" /* yacc.c:1646  */
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
#line 9557 "ripper.c" /* yacc.c:1646  */
    break;

  case 529:
#line 3817 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			if (!((yyval.val) = gettable(p, (yyvsp[0].val), &(yyloc)))) (yyval.val) = NEW_BEGIN(0, &(yyloc));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(var_ref,v1);(yyval.val)=v2;}
		    }
#line 9568 "ripper.c" /* yacc.c:1646  */
    break;

  case 530:
#line 3826 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 9579 "ripper.c" /* yacc.c:1646  */
    break;

  case 531:
#line 3833 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = assignable(p, (yyvsp[0].val), 0, &(yyloc));
#endif
			(yyval.val)=assignable(p, var_field(p, (yyvsp[0].val)));
		    }
#line 9590 "ripper.c" /* yacc.c:1646  */
    break;

  case 534:
#line 3846 "ripper.y" /* yacc.c:1646  */
    {
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 9599 "ripper.c" /* yacc.c:1646  */
    break;

  case 535:
#line 3851 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[-1].val);
		    }
#line 9607 "ripper.c" /* yacc.c:1646  */
    break;

  case 536:
#line 3855 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			(yyval.val)=Qnil;
		    }
#line 9618 "ripper.c" /* yacc.c:1646  */
    break;

  case 537:
#line 3864 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(paren,v1);(yyval.val)=v2;}
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 9631 "ripper.c" /* yacc.c:1646  */
    break;

  case 538:
#line 3872 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.num) = p->in_kwarg;
			p->in_kwarg = 1;
			SET_LEX_STATE(p->lex.state|EXPR_LABEL); /* force for args */
		    }
#line 9641 "ripper.c" /* yacc.c:1646  */
    break;

  case 539:
#line 3878 "ripper.y" /* yacc.c:1646  */
    {
			p->in_kwarg = !!(yyvsp[-2].num);
			(yyval.val) = (yyvsp[-1].val);
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 9652 "ripper.c" /* yacc.c:1646  */
    break;

  case 540:
#line 3887 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 9660 "ripper.c" /* yacc.c:1646  */
    break;

  case 541:
#line 3891 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 9668 "ripper.c" /* yacc.c:1646  */
    break;

  case 542:
#line 3895 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yylsp[-1]));
		    }
#line 9676 "ripper.c" /* yacc.c:1646  */
    break;

  case 543:
#line 3899 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].val), &(yylsp[0]));
		    }
#line 9684 "ripper.c" /* yacc.c:1646  */
    break;

  case 544:
#line 3905 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 9692 "ripper.c" /* yacc.c:1646  */
    break;

  case 545:
#line 3909 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 9700 "ripper.c" /* yacc.c:1646  */
    break;

  case 546:
#line 3915 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9708 "ripper.c" /* yacc.c:1646  */
    break;

  case 547:
#line 3919 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-7].val), (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9716 "ripper.c" /* yacc.c:1646  */
    break;

  case 548:
#line 3923 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-3].val), (yyvsp[-1].val), Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9724 "ripper.c" /* yacc.c:1646  */
    break;

  case 549:
#line 3927 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), (yyvsp[-3].val), Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9732 "ripper.c" /* yacc.c:1646  */
    break;

  case 550:
#line 3931 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9740 "ripper.c" /* yacc.c:1646  */
    break;

  case 551:
#line 3935 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-5].val), Qnone, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9748 "ripper.c" /* yacc.c:1646  */
    break;

  case 552:
#line 3939 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, (yyvsp[-1].val), Qnone, Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9756 "ripper.c" /* yacc.c:1646  */
    break;

  case 553:
#line 3943 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-3].val), (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9764 "ripper.c" /* yacc.c:1646  */
    break;

  case 554:
#line 3947 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-5].val), (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9772 "ripper.c" /* yacc.c:1646  */
    break;

  case 555:
#line 3951 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-1].val), Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9780 "ripper.c" /* yacc.c:1646  */
    break;

  case 556:
#line 3955 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, (yyvsp[-3].val), Qnone, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9788 "ripper.c" /* yacc.c:1646  */
    break;

  case 557:
#line 3959 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, Qnone, (yyvsp[-1].val), Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9796 "ripper.c" /* yacc.c:1646  */
    break;

  case 558:
#line 3963 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, Qnone, (yyvsp[-3].val), (yyvsp[-1].val), (yyvsp[0].val), &(yyloc));
		    }
#line 9804 "ripper.c" /* yacc.c:1646  */
    break;

  case 559:
#line 3967 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].val), &(yyloc));
		    }
#line 9812 "ripper.c" /* yacc.c:1646  */
    break;

  case 560:
#line 3971 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
			(yyval.val) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyval.val), &(yylsp[0]));
		    }
#line 9821 "ripper.c" /* yacc.c:1646  */
    break;

  case 561:
#line 3978 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			yyerror1(&(yylsp[0]), "formal argument cannot be a constant");
			(yyval.val) = 0;
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(param_error,v1);(yyval.val)=v2;}ripper_error(p);
		    }
#line 9833 "ripper.c" /* yacc.c:1646  */
    break;

  case 562:
#line 3986 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			yyerror1(&(yylsp[0]), "formal argument cannot be an instance variable");
			(yyval.val) = 0;
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(param_error,v1);(yyval.val)=v2;}ripper_error(p);
		    }
#line 9845 "ripper.c" /* yacc.c:1646  */
    break;

  case 563:
#line 3994 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			yyerror1(&(yylsp[0]), "formal argument cannot be a global variable");
			(yyval.val) = 0;
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(param_error,v1);(yyval.val)=v2;}ripper_error(p);
		    }
#line 9857 "ripper.c" /* yacc.c:1646  */
    break;

  case 564:
#line 4002 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			yyerror1(&(yylsp[0]), "formal argument cannot be a class variable");
			(yyval.val) = 0;
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(param_error,v1);(yyval.val)=v2;}ripper_error(p);
		    }
#line 9869 "ripper.c" /* yacc.c:1646  */
    break;

  case 566:
#line 4013 "ripper.y" /* yacc.c:1646  */
    {
			formal_argument(p, get_id((yyvsp[0].val)));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 9878 "ripper.c" /* yacc.c:1646  */
    break;

  case 567:
#line 4020 "ripper.y" /* yacc.c:1646  */
    {
			ID id = get_id((yyvsp[0].val));
			arg_var(p, id);
			p->cur_arg = id;
			(yyval.val) = (yyvsp[0].val);
		    }
#line 9889 "ripper.c" /* yacc.c:1646  */
    break;

  case 568:
#line 4029 "ripper.y" /* yacc.c:1646  */
    {
			p->cur_arg = 0;
#if 0
			(yyval.val) = NEW_ARGS_AUX((yyvsp[0].val), 1, &NULL_LOC);
#endif
			(yyval.val)=get_value((yyvsp[0].val));
		    }
#line 9901 "ripper.c" /* yacc.c:1646  */
    break;

  case 569:
#line 4037 "ripper.y" /* yacc.c:1646  */
    {
			ID tid = internal_id(p);
#if 0
			YYLTYPE loc;
			loc.beg_pos = (yylsp[-1]).beg_pos;
			loc.end_pos = (yylsp[-1]).beg_pos;
#endif
			arg_var(p, tid);
#if 0
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
#line 9926 "ripper.c" /* yacc.c:1646  */
    break;

  case 570:
#line 4060 "ripper.y" /* yacc.c:1646  */
    {(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));}
#line 9932 "ripper.c" /* yacc.c:1646  */
    break;

  case 571:
#line 4062 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-2].val);
			(yyval.val)->nd_plen++;
			(yyval.val)->nd_next = block_append(p, (yyval.val)->nd_next, (yyvsp[0].val)->nd_next);
			rb_discard_node(p, (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 9946 "ripper.c" /* yacc.c:1646  */
    break;

  case 572:
#line 4075 "ripper.y" /* yacc.c:1646  */
    {
			ID id = get_id((yyvsp[0].val));
			arg_var(p, formal_argument(p, id));
			p->cur_arg = id;
			(yyval.val) = (yyvsp[0].val);
		    }
#line 9957 "ripper.c" /* yacc.c:1646  */
    break;

  case 573:
#line 4084 "ripper.y" /* yacc.c:1646  */
    {
			p->cur_arg = 0;
#if 0
			(yyval.val) = new_kw_arg(p, assignable(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[-1].val))), get_value((yyvsp[0].val)));
		    }
#line 9969 "ripper.c" /* yacc.c:1646  */
    break;

  case 574:
#line 4092 "ripper.y" /* yacc.c:1646  */
    {
			p->cur_arg = 0;
#if 0
			(yyval.val) = new_kw_arg(p, assignable(p, (yyvsp[0].val), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[0].val))), 0);
		    }
#line 9981 "ripper.c" /* yacc.c:1646  */
    break;

  case 575:
#line 4102 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_kw_arg(p, assignable(p, (yyvsp[-1].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[-1].val))), get_value((yyvsp[0].val)));
		    }
#line 9992 "ripper.c" /* yacc.c:1646  */
    break;

  case 576:
#line 4109 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = new_kw_arg(p, assignable(p, (yyvsp[0].val), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[0].val))), 0);
		    }
#line 10003 "ripper.c" /* yacc.c:1646  */
    break;

  case 577:
#line 4118 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 10014 "ripper.c" /* yacc.c:1646  */
    break;

  case 578:
#line 4125 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = kwd_append((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 10025 "ripper.c" /* yacc.c:1646  */
    break;

  case 579:
#line 4135 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 10036 "ripper.c" /* yacc.c:1646  */
    break;

  case 580:
#line 4142 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = kwd_append((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 10047 "ripper.c" /* yacc.c:1646  */
    break;

  case 583:
#line 4155 "ripper.y" /* yacc.c:1646  */
    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].val))));
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(kwrest_param,v1);(yyval.val)=v2;}
		    }
#line 10059 "ripper.c" /* yacc.c:1646  */
    break;

  case 584:
#line 4163 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = internal_id(p);
			arg_var(p, (yyval.val));
#endif
			{VALUE v1,v2;v1=Qnil;v2=dispatch1(kwrest_param,v1);(yyval.val)=v2;}
		    }
#line 10071 "ripper.c" /* yacc.c:1646  */
    break;

  case 585:
#line 4173 "ripper.y" /* yacc.c:1646  */
    {
			p->cur_arg = 0;
#if 0
			(yyval.val) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[-2].val))), get_value((yyvsp[0].val)));
		    }
#line 10083 "ripper.c" /* yacc.c:1646  */
    break;

  case 586:
#line 4183 "ripper.y" /* yacc.c:1646  */
    {
			p->cur_arg = 0;
#if 0
			(yyval.val) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].val), (yyvsp[0].val), &(yyloc)), &(yyloc));
#endif
			(yyval.val)=rb_assoc_new(get_value(assignable(p, (yyvsp[-2].val))), get_value((yyvsp[0].val)));
		    }
#line 10095 "ripper.c" /* yacc.c:1646  */
    break;

  case 587:
#line 4193 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 10106 "ripper.c" /* yacc.c:1646  */
    break;

  case 588:
#line 4200 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = opt_arg_append((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 10117 "ripper.c" /* yacc.c:1646  */
    break;

  case 589:
#line 4209 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));
		    }
#line 10128 "ripper.c" /* yacc.c:1646  */
    break;

  case 590:
#line 4216 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = opt_arg_append((yyvsp[-2].val), (yyvsp[0].val));
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 10139 "ripper.c" /* yacc.c:1646  */
    break;

  case 593:
#line 4229 "ripper.y" /* yacc.c:1646  */
    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].val))));
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(rest_param,v1);(yyval.val)=v2;}
		    }
#line 10151 "ripper.c" /* yacc.c:1646  */
    break;

  case 594:
#line 4237 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = internal_id(p);
			arg_var(p, (yyval.val));
#endif
			{VALUE v1,v2;v1=Qnil;v2=dispatch1(rest_param,v1);(yyval.val)=v2;}
		    }
#line 10163 "ripper.c" /* yacc.c:1646  */
    break;

  case 597:
#line 4251 "ripper.y" /* yacc.c:1646  */
    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].val))));
#if 0
			(yyval.val) = (yyvsp[0].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(blockarg,v1);(yyval.val)=v2;}
		    }
#line 10175 "ripper.c" /* yacc.c:1646  */
    break;

  case 598:
#line 4261 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = (yyvsp[0].val);
		    }
#line 10183 "ripper.c" /* yacc.c:1646  */
    break;

  case 599:
#line 4265 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = 0;
#endif
			(yyval.val)=Qundef;
		    }
#line 10194 "ripper.c" /* yacc.c:1646  */
    break;

  case 600:
#line 4274 "ripper.y" /* yacc.c:1646  */
    {
			value_expr((yyvsp[0].val));
			(yyval.val) = (yyvsp[0].val);
		    }
#line 10203 "ripper.c" /* yacc.c:1646  */
    break;

  case 601:
#line 4278 "ripper.y" /* yacc.c:1646  */
    {SET_LEX_STATE(EXPR_BEG);}
#line 10209 "ripper.c" /* yacc.c:1646  */
    break;

  case 602:
#line 4279 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			switch (nd_type((yyvsp[-1].val))) {
			  case NODE_STR:
			  case NODE_DSTR:
			  case NODE_XSTR:
			  case NODE_DXSTR:
			  case NODE_DREGX:
			  case NODE_LIT:
			  case NODE_ARRAY:
			  case NODE_ZARRAY:
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
#line 10235 "ripper.c" /* yacc.c:1646  */
    break;

  case 604:
#line 4304 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = (yyvsp[-1].val);
#endif
			{VALUE v1,v2;v1=(yyvsp[-1].val);v2=dispatch1(assoclist_from_args,v1);(yyval.val)=v2;}
		    }
#line 10246 "ripper.c" /* yacc.c:1646  */
    break;

  case 605:
#line 4313 "ripper.y" /* yacc.c:1646  */
    {(yyval.val)=rb_ary_new3(1, get_value((yyvsp[0].val)));}
#line 10252 "ripper.c" /* yacc.c:1646  */
    break;

  case 606:
#line 4315 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			NODE *assocs = (yyvsp[-2].val);
			NODE *tail = (yyvsp[0].val);
			if (!assocs) {
			    assocs = tail;
			}
			else if (tail) {
			    if (assocs->nd_head &&
				!tail->nd_head && nd_type(tail->nd_next) == NODE_ARRAY &&
				nd_type(tail->nd_next->nd_head) == NODE_HASH) {
				/* DSTAR */
				tail = tail->nd_next->nd_head->nd_head;
			    }
			    assocs = list_concat(assocs, tail);
			}
			(yyval.val) = assocs;
#endif
			(yyval.val)=rb_ary_push((yyvsp[-2].val), get_value((yyvsp[0].val)));
		    }
#line 10277 "ripper.c" /* yacc.c:1646  */
    break;

  case 607:
#line 4338 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			if (nd_type((yyvsp[-2].val)) == NODE_STR) {
			    nd_set_type((yyvsp[-2].val), NODE_LIT);
			    add_mark_object(p, (yyvsp[-2].val)->nd_lit = rb_fstring((yyvsp[-2].val)->nd_lit));
			}
			(yyval.val) = list_append(p, NEW_LIST((yyvsp[-2].val), &(yyloc)), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-2].val);v2=(yyvsp[0].val);v3=dispatch2(assoc_new,v1,v2);(yyval.val)=v3;}
		    }
#line 10292 "ripper.c" /* yacc.c:1646  */
    break;

  case 608:
#line 4349 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			(yyval.val) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[-1].val)), &(yylsp[-1])), &(yyloc)), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3;v1=(yyvsp[-1].val);v2=(yyvsp[0].val);v3=dispatch2(assoc_new,v1,v2);(yyval.val)=v3;}
		    }
#line 10303 "ripper.c" /* yacc.c:1646  */
    break;

  case 609:
#line 4356 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			YYLTYPE loc = code_loc_gen(&(yylsp[-3]), &(yylsp[-1]));
			(yyval.val) = list_append(p, NEW_LIST(dsym_node(p, (yyvsp[-2].val), &loc), &loc), (yyvsp[0].val));
#endif
			{VALUE v1,v2,v3,v4,v5;v1=(yyvsp[-2].val);v2=dispatch1(dyna_symbol,v1);v3=v2;v4=(yyvsp[0].val);v5=dispatch2(assoc_new,v3,v4);(yyval.val)=v5;}
		    }
#line 10315 "ripper.c" /* yacc.c:1646  */
    break;

  case 610:
#line 4364 "ripper.y" /* yacc.c:1646  */
    {
#if 0
			if (nd_type((yyvsp[0].val)) == NODE_HASH &&
			    !((yyvsp[0].val)->nd_head && (yyvsp[0].val)->nd_head->nd_alen))
			    (yyval.val) = 0;
			else
			    (yyval.val) = list_append(p, NEW_LIST(0, &(yyloc)), (yyvsp[0].val));
#endif
			{VALUE v1,v2;v1=(yyvsp[0].val);v2=dispatch1(assoc_splat,v1);(yyval.val)=v2;}
		    }
#line 10330 "ripper.c" /* yacc.c:1646  */
    break;

  case 636:
#line 4423 "ripper.y" /* yacc.c:1646  */
    {yyerrok;token_flush(p);}
#line 10336 "ripper.c" /* yacc.c:1646  */
    break;

  case 637:
#line 4424 "ripper.y" /* yacc.c:1646  */
    {token_flush(p);}
#line 10342 "ripper.c" /* yacc.c:1646  */
    break;

  case 639:
#line 4428 "ripper.y" /* yacc.c:1646  */
    {yyerrok;}
#line 10348 "ripper.c" /* yacc.c:1646  */
    break;

  case 640:
#line 4432 "ripper.y" /* yacc.c:1646  */
    {
			(yyval.val) = Qnull;
		    }
#line 10356 "ripper.c" /* yacc.c:1646  */
    break;


#line 10360 "ripper.c" /* yacc.c:1646  */
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

  yyn = yyr1[yyn];

  yystate = yypgoto[yyn - YYNTOKENS] + *yyssp;
  if (0 <= yystate && yystate <= YYLAST && yycheck[yystate] == *yyssp)
    yystate = yytable[yystate];
  else
    yystate = yydefgoto[yyn - YYNTOKENS];

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
# define YYSYNTAX_ERROR yysyntax_error (&yymsg_alloc, &yymsg, \
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
            yymsg = (char *) YYSTACK_ALLOC (yymsg_alloc);
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

  /* Pacify compilers like GCC when the user code never invokes
     YYERROR and the label yyerrorlab therefore never appears in user
     code.  */
  if (/*CONSTCOND*/ 0)
     goto yyerrorlab;

  yyerror_range[1] = yylsp[1-yylen];
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
                  yystos[*yyssp], yyvsp, yylsp, p);
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
#line 4436 "ripper.y" /* yacc.c:1906  */

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
# define set_yylval_str(x) set_yylval_node(NEW_STR(x, &_cur_loc))
# define set_yylval_literal(x) set_yylval_node(NEW_LIT(x, &_cur_loc))
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
# define set_yylval_str(x) (yylval.val = (x))
# define set_yylval_num(x) (yylval.val = ripper_new_yylval(p, (x), 0, 0))
# define set_yylval_id(x)  (void)(x)
# define set_yylval_name(x) (void)(yylval.val = ripper_yylval_id(p, x))
# define set_yylval_literal(x) (void)(x)
# define set_yylval_node(x) (void)(x)
# define yylval_id() yylval.id
# define _cur_loc NULL_LOC /* dummy */
#endif

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
ripper_scan_event_val(struct parser_params *p, int t)
{
    VALUE str = STR_NEW(p->lex.ptok, p->lex.pcur - p->lex.ptok);
    VALUE rval = ripper_dispatch1(p, ripper_token2eventid(t), str);
    token_flush(p);
    return rval;
}

static void
ripper_dispatch_scan_event(struct parser_params *p, int t)
{
    if (!ripper_has_scan_event(p)) return;
    add_mark_object(p, yylval_rval = ripper_scan_event_val(p, t));
}
#define dispatch_scan_event(p, t) ripper_dispatch_scan_event(p, t)

static void
ripper_dispatch_delayed_token(struct parser_params *p, int t)
{
    int saved_line = p->ruby_sourceline;
    const char *saved_tokp = p->lex.ptok;

    p->ruby_sourceline = p->delayed_line;
    p->lex.ptok = p->lex.pbeg + p->delayed_col;
    add_mark_object(p, yylval_rval = ripper_dispatch1(p, ripper_token2eventid(t), p->delayed));
    p->delayed = Qnil;
    p->ruby_sourceline = saved_line;
    p->lex.ptok = saved_tokp;
}
#define dispatch_delayed_token(p, t) ripper_dispatch_delayed_token(p, t)
#define has_delayed_token(p) (!NIL_P(p->delayed))
#endif /* RIPPER */

#include "ruby/regex.h"
#include "ruby/util.h"

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
setup_token_info(token_info *ptinfo, const char *ptr, const rb_code_location_t *loc)
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

    ptinfo->linenum = loc->beg_pos.lineno;
    ptinfo->column = column;
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
    setup_token_info(ptinfo, p->lex.pbeg, loc);

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
token_info_warn(struct parser_params *p, const char *token, token_info *ptinfo_beg, int same, const rb_code_location_t *loc)
{
    token_info ptinfo_end_body, *ptinfo_end = &ptinfo_end_body;
    if (!p->token_info_enabled) return;
    if (!ptinfo_beg) return;
    setup_token_info(ptinfo_end, p->lex.pbeg, loc);
    if (ptinfo_beg->linenum == ptinfo_end->linenum) return; /* ignore one-line block */
    if (ptinfo_beg->nonspc || ptinfo_end->nonspc) return; /* ignore keyword in the middle of a line */
    if (ptinfo_beg->column == ptinfo_end->column) return; /* the indents are matched */
    if (!same && ptinfo_beg->column < ptinfo_end->column) return;
    rb_warn3L(ptinfo_end->linenum,
	      "mismatched indentations at '%s' with '%s' at %d",
	      WARN_S(token), WARN_S(ptinfo_beg->token), WARN_I(ptinfo_beg->linenum));
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

static int
parser_yyerror(struct parser_params *p, const YYLTYPE *yylloc, const char *msg)
{
#ifndef RIPPER
    const int max_line_margin = 30;
    const char *ptr, *ptr_end, *pt, *pb;
    const char *pre = "", *post = "", *pend;
    const char *code = "", *caret = "", *newline = "";
    const char *lim;
    char *buf;
    long len;
    int i;
    YYLTYPE current;

    if (!yylloc) {
	RUBY_SET_YYLLOC(current);
	yylloc = &current;
    }
    else if ((p->ruby_sourceline != yylloc->beg_pos.lineno &&
	      p->ruby_sourceline != yylloc->end_pos.lineno) ||
	     (yylloc->beg_pos.lineno == yylloc->end_pos.lineno &&
	      yylloc->beg_pos.column == yylloc->end_pos.column)) {
	compile_error(p, "%s", msg);
	return 0;
    }

    pend = p->lex.pend;
    if (pend > p->lex.pbeg && pend[-1] == '\n') {
	if (--pend > p->lex.pbeg && pend[-1] == '\r') --pend;
    }

    pt = (p->ruby_sourceline == yylloc->end_pos.lineno) ?
	    p->lex.pbeg + yylloc->end_pos.column : p->lex.pend;
    ptr = ptr_end = pt < pend ? pt : pend;
    lim = ptr - p->lex.pbeg > max_line_margin ? ptr - max_line_margin : p->lex.pbeg;
    while ((lim < ptr) && (*(ptr-1) != '\n')) ptr--;

    lim = pend - ptr_end > max_line_margin ? ptr_end + max_line_margin : pend;
    while ((ptr_end < lim) && (*ptr_end != '\n')) ptr_end++;

    len = ptr_end - ptr;
    if (len > 4) {
	if (ptr > p->lex.pbeg) {
	    ptr = rb_enc_prev_char(p->lex.pbeg, ptr, pt, rb_enc_get(p->lex.lastline));
	    if (ptr > p->lex.pbeg) pre = "...";
	}
	if (ptr_end < pend) {
	    ptr_end = rb_enc_prev_char(pt, ptr_end, pend, rb_enc_get(p->lex.lastline));
	    if (ptr_end < pend) post = "...";
	}
    }
    pb = p->lex.pbeg;
    if (p->ruby_sourceline == yylloc->beg_pos.lineno) {
	pb += yylloc->beg_pos.column;
	if (pb > pt) pb = pt;
    }
    if (pb < ptr) pb = ptr;
    if (len <= 4 && yylloc->beg_pos.lineno == yylloc->end_pos.lineno) {
	compile_error(p, "%s", msg);
    }
    else if (!p->error_buffer && rb_stderr_tty_p()) {
#define CSI_BEGIN "\033["
#define CSI_SGR "m"
	compile_error(p, "%s\n"
		      CSI_BEGIN""CSI_SGR"%s" /* pre */
		      CSI_BEGIN"1"CSI_SGR"%.*s"
		      CSI_BEGIN"1;4"CSI_SGR"%.*s"
		      CSI_BEGIN";1"CSI_SGR"%.*s"
		      CSI_BEGIN""CSI_SGR"%s" /* post */,
		      msg, pre,
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
	newline = "\n";
	compile_error(p, "%s%s""%s%.*s%s%s""%s%s",
		      msg, newline,
		      pre, (int)len, code, post, newline,
		      pre, caret);
    }
#else
    dispatch1(parse_error, STR_NEW2(msg));
    ripper_error(p);
#endif /* !RIPPER */
    return 0;
}

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
	ruby_sized_xfree(tbl, sizeof(tbl));
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

    if (!compile_for_eval && rb_safe_level() == 0 && !NIL_P(p->ruby_sourcefile_string)) {
	p->debug_lines = debug_lines(p->ruby_sourcefile_string);
	if (p->debug_lines && p->ruby_sourceline > 0) {
	    VALUE str = STR_NEW0();
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
	add_mark_object(p, opt);
	tree->nd_body = prelude;
	p->ast->body.compile_option = opt;
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
rb_compile_string(const char *f, VALUE s, int line)
{
    must_be_ascii_compatible(s);
    return parser_compile_string(rb_parser_new(), rb_filesystem_str_new_cstr(f), s, line);
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

rb_ast_t*
rb_compile_cstr(const char *f, const char *s, int len, int line)
{
    VALUE str = rb_str_new(s, len);
    return parser_compile_string(rb_parser_new(), rb_filesystem_str_new_cstr(f), str, line);
}

rb_ast_t*
rb_parser_compile_cstr(VALUE vparser, const char *f, const char *s, int len, int line)
{
    VALUE str = rb_str_new(s, len);
    return parser_compile_string(vparser, rb_filesystem_str_new_cstr(f), str, line);
}

VALUE rb_io_gets_internal(VALUE io);

static VALUE
lex_io_gets(struct parser_params *p, VALUE io)
{
    return rb_io_gets_internal(io);
}

rb_ast_t*
rb_compile_file(const char *f, VALUE file, int start)
{
    VALUE vparser = rb_parser_new();

    return rb_parser_compile_file(vparser, f, file, start);
}

rb_ast_t*
rb_parser_compile_file(VALUE vparser, const char *f, VALUE file, int start)
{
    return rb_parser_compile_file_path(vparser, rb_filesystem_str_new_cstr(f), file, start);
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
	    p->delayed = rb_str_buf_new(1024);
	    rb_enc_associate(p->delayed, p->enc);
	    p->delayed_line = p->ruby_sourceline;
	    p->delayed_col = (int)(tok - p->lex.pbeg);
	}
	rb_str_buf_cat(p->delayed, tok, end - tok);
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

	if (!p->lex.input || NIL_P(v = lex_getline(p))) {
	    p->eofp = 1;
	    lex_goto_eol(p);
	    return -1;
	}
	p->cr_seen = FALSE;
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
    else if (!p->cr_seen) {
	p->cr_seen = TRUE;
	/* carried over with p->lex.nextline for nextc() */
	rb_warn0("encountered \\r in middle of line, treated as a mere space");
    }
    return c;
}

static inline int
nextc(struct parser_params *p)
{
    int c;

    if (UNLIKELY((p->lex.pcur == p->lex.pend) || p->eofp || p->lex.nextline)) {
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
	p->lex.ptok = p->lex.pcur;
	yyerror0("invalid hex escape");
	return 0;
    }
    p->lex.pcur += *numlen;
    return c;
}

#define tokcopy(p, n) memcpy(tokspace(p, n), (p)->lex.pcur - (n), (n))

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
	    static const char mixed_utf8[] = "UTF-8 mixed within %s source";
	    size_t len = sizeof(mixed_utf8) - 2 + strlen(rb_enc_name(*encp));
	    char *mesg = alloca(len);
	    snprintf(mesg, len, mixed_utf8, rb_enc_name(*encp));
	    yyerror0(mesg);
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
static int
parser_tokadd_utf8(struct parser_params *p, rb_encoding **encp,
		   int string_literal, int symbol_literal, int regexp_literal)
{
    /*
     * If string_literal is true, then we allow multiple codepoints
     * in \u{}, and add the codepoints to the current token.
     * Otherwise we're parsing a character literal and return a single
     * codepoint without adding it
     */

    const int open_brace = '{', close_brace = '}';

    if (regexp_literal) { tokadd(p, '\\'); tokadd(p, 'u'); }

    if (peek(p, open_brace)) {  /* handle \u{...} form */
	int c, last = nextc(p);
	if (p->lex.pcur >= p->lex.pend) goto unterminated;
	while (ISSPACE(c = *p->lex.pcur) && ++p->lex.pcur < p->lex.pend);
	while (c != close_brace) {
	    if (regexp_literal) tokadd(p, last);
	    if (!tokadd_codepoint(p, encp, regexp_literal, TRUE)) {
		break;
	    }
	    while (ISSPACE(c = *p->lex.pcur)) {
		if (++p->lex.pcur >= p->lex.pend) goto unterminated;
		last = c;
	    }
	}

	if (c != close_brace) {
	  unterminated:
	    literal_flush(p, p->lex.pcur);
	    yyerror0("unterminated Unicode escape");
	    return 0;
	}

	if (regexp_literal) tokadd(p, close_brace);
	nextc(p);
    }
    else {			/* handle \uxxxx form */
	if (!tokadd_codepoint(p, encp, regexp_literal, FALSE)) {
	    return 0;
	}
    }

    return TRUE;
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
	    if (peek(p, 'u')) goto eof;
	    return read_escape(p, flags|ESCAPE_META, encp) | 0x80;
	}
	else if (c == -1 || !ISASCII(c)) goto eof;
	else {
	    return ((c & 0xff) | 0x80);
	}

      case 'C':
	if ((c = nextc(p)) != '-') {
	    goto eof;
	}
      case 'c':
	if (flags & ESCAPE_CONTROL) goto eof;
	if ((c = nextc(p))== '\\') {
	    if (peek(p, 'u')) goto eof;
	    c = read_escape(p, flags|ESCAPE_CONTROL, encp);
	}
	else if (c == '?')
	    return 0177;
	else if (c == -1 || !ISASCII(c)) goto eof;
	return c & 0x9f;

      eof:
      case -1:
        yyerror0("Invalid escape character syntax");
	pushback(p, c);
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
	tokfix(p);
	compile_error(p, "unknown regexp option%s - %*s",
		      toklen(p) > 1 ? "s" : "", toklen(p), tok(p));
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
    static const char mixed_msg[] = "%s mixed within %s source";
    const char *n1 = rb_enc_name(enc1), *n2 = rb_enc_name(enc2);
    const size_t len = sizeof(mixed_msg) - 4 + strlen(n1) + strlen(n2);
    char *errbuf = ALLOCA_N(char, len);
    snprintf(errbuf, len, mixed_msg, n1, n2);
    yyerror0(errbuf);
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
		if (!parser_tokadd_utf8(p, enc, term,
					func & STR_FUNC_SYMBOL,
					func & STR_FUNC_REGEXP)) {
		    return -1;
		}
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
	    rb_enc_str_buf_cat(p->delayed, p->lex.ptok, len, enc);
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
    add_mark_object(p, lit = STR_NEW3(tok(p), toklen(p), enc, func));
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
    int c = nextc(p), term, func = 0, term_len = 2;
    enum yytokentype token = tSTRING_BEG;
    long len;
    int newline = 0;
    int indent = 0;

    if (c == '-') {
	c = nextc(p);
	term_len++;
	func = STR_FUNC_INDENT;
    }
    else if (c == '~') {
	c = nextc(p);
	term_len++;
	func = STR_FUNC_INDENT;
	indent = INT_MAX;
    }
    switch (c) {
      case '\'':
	term_len++;
	func |= str_squote; goto quoted;
      case '"':
	term_len++;
	func |= str_dquote; goto quoted;
      case '`':
	term_len++;
	token = tXSTRING_BEG;
	func |= str_xquote; goto quoted;

      quoted:
	term_len++;
	newtok(p);
	tokadd(p, term_len);
	tokadd(p, func);
	term = c;
	while ((c = nextc(p)) != -1 && c != term) {
	    if (tokadd_mbchar(p, c) == -1) return 0;
	    if (!newline && c == '\n') newline = 1;
	    else if (newline) newline = 2;
	}
	if (c == -1) {
	    yyerror(NULL, p, "unterminated here document identifier");
	    return -1;
	}
	switch (newline) {
	  case 1:
	    rb_warn0("here document identifier ends with a newline");
	    if (--p->tokidx > 0 && p->tokenbuf[p->tokidx] == '\r') --p->tokidx;
	    break;
	  case 2:
	    compile_error(p, "here document identifier across newlines, never match");
	    return -1;
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
	newtok(p);
	tokadd(p, term_len);
	tokadd(p, func |= str_dquote);
	do {
	    if (tokadd_mbchar(p, c) == -1) return 0;
	} while ((c = nextc(p)) != -1 && parser_is_identchar(p));
	pushback(p, c);
	break;
    }

    tokfix(p);
    dispatch_scan_event(p, tHEREDOC_BEG);
    len = p->lex.pcur - p->lex.pbeg;
    lex_goto_eol(p);

    p->lex.strterm = new_strterm(STR_NEW(tok(p), toklen(p)), /* term */
				 p->lex.lastline, /* lastline */
				 len, /* lastidx */
				 p->ruby_sourceline);
    p->lex.strterm->flags |= STRTERM_HEREDOC;

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
    p->lex.pcur = p->lex.pbeg + here->u3.lastidx;
    p->heredoc_end = p->ruby_sourceline;
    p->ruby_sourceline = (int)here->sourceline;
    token_flush(p);
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
    if (nd_type(root) == NODE_ARRAY) str_node = root->nd_head;

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
	    if (nd_type(node) != NODE_ARRAY) break;
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
	if (c == '.') {
	    c = peekc_n(p, 1);
	    if (ISDIGIT(c)) {
		yyerror0("unexpected fraction part after numeric literal");
		p->lex.pcur += 2;
		while (parser_is_identchar(p)) nextc(p);
	    }
	}
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
    add_mark_object(p, v);
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

    eos = RSTRING_PTR(here->term);
    len = RSTRING_LEN(here->term) - 2; /* here->term includes term_len and func */
    eos++; /* skip term_len */
    indent = (func = *eos++) & STR_FUNC_INDENT;

    if ((c = nextc(p)) == -1) {
      error:
	compile_error(p, "can't find string \"%s\" anywhere before EOF", eos);
#ifdef RIPPER
	if (!has_delayed_token(p)) {
	    dispatch_scan_event(p, tSTRING_CONTENT);
	}
	else {
	    if (str) {
		rb_str_append(p->delayed, str);
	    }
	    else if ((len = p->lex.pcur - p->lex.ptok) > 0) {
		if (!(func & STR_FUNC_REGEXP) && rb_enc_asciicompat(enc)) {
		    int cr = ENC_CODERANGE_UNKNOWN;
		    rb_str_coderange_scan_restartable(p->lex.ptok, p->lex.pcur, enc, &cr);
		    if (cr != ENC_CODERANGE_7BIT &&
			p->enc == rb_usascii_encoding() &&
			enc != rb_utf8_encoding()) {
			enc = rb_ascii8bit_encoding();
		    }
		}
		rb_enc_str_buf_cat(p->delayed, p->lex.ptok, len, enc);
	    }
	    dispatch_delayed_token(p, tSTRING_CONTENT);
	}
	lex_goto_eol(p);
#endif
      restore:
	heredoc_restore(p, &p->lex.strterm->u.heredoc);
	p->lex.strterm = 0;
	return 0;
    }
    bol = was_bol(p);
    /* `heredoc_line_indent == -1` means
     * - "after an interpolation in the same line", or
     * - "in a continuing line"
     */
    if (bol &&
	(p->heredoc_line_indent != -1 || (p->heredoc_line_indent = 0)) &&
	whole_match_p(p, eos, len, indent)) {
	dispatch_heredoc_end(p);
	heredoc_restore(p, &p->lex.strterm->u.heredoc);
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
		add_mark_object(p, str);
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
    p->lex.strterm = NEW_STRTERM(func | STR_FUNC_TERM, 0, 0);
    set_yylval_str(str);
    add_mark_object(p, str);
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
    rb_warning1("ambiguous first argument; put parentheses or a space even after `%c' operator", WARN_I(c));
#else
    dispatch1(arg_ambiguous, rb_usascii_str_new(&c, 1));
#endif
    return TRUE;
}

static ID
formal_argument(struct parser_params *p, ID lhs)
{
    switch (id_type(lhs)) {
      case ID_LOCAL:
	break;
#ifndef RIPPER
      case ID_CONST:
	yyerror0("formal argument cannot be a constant");
	return 0;
      case ID_INSTANCE:
	yyerror0("formal argument cannot be an instance variable");
	return 0;
      case ID_GLOBAL:
	yyerror0("formal argument cannot be a global variable");
	return 0;
      case ID_CLASS:
	yyerror0("formal argument cannot be a class variable");
	return 0;
      default:
	yyerror0("formal argument must be local variable");
	return 0;
#else
      default:
	lhs = dispatch1(param_error, lhs);
	ripper_error(p);
	return 0;
#endif
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
	if (strcasecmp(val, "true") == 0) {
	    return TRUE;
	}
	break;
      case 'f': case 'F':
	if (strcasecmp(val, "false") == 0) {
	    return FALSE;
	}
	break;
    }
    rb_compile_warning(p->ruby_sourcefile, p->ruby_sourceline, "invalid value for %s: %s", name, val);
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
	char tmp[30];
      trailing_uc:
	literal_flush(p, p->lex.pcur - 1);
	snprintf(tmp, sizeof(tmp), "trailing `%c' in number", nondigit);
	yyerror0(tmp);
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
	    if (c2) {
		rb_warn1("invalid character syntax; use ?\\%c", WARN_I(c2));
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
	    if (!parser_tokadd_utf8(p, &enc, -1, 0, 0))
		return 0;
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
    add_mark_object(p, lit = STR_NEW3(tok(p), toklen(p), enc, 0));
    set_yylval_str(lit);
    SET_LEX_STATE(EXPR_END);
    return tCHAR;
}

static enum yytokentype
parse_percent(struct parser_params *p, const int space_seen, const enum lex_state_e last_state)
{
    register int c;

    if (IS_BEG()) {
	int term;
	int paren;

	c = nextc(p);
      quotation:
	if (c == -1 || !ISALNUM(c)) {
	    term = c;
	    c = 'Q';
	}
	else {
	    term = nextc(p);
	    if (rb_enc_isalnum(term, p->enc) || !parser_isascii(p)) {
		yyerror0("unknown type of %string");
		return 0;
	    }
	}
	if (c == -1 || term == -1) {
	    compile_error(p, "unterminated quoted string meets end of file");
	    return 0;
	}
	paren = term;
	if (term == '(') term = ')';
	else if (term == '[') term = ']';
	else if (term == '{') term = '}';
	else if (term == '<') term = '>';
	else paren = 0;

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
    register int c;

    SET_LEX_STATE(EXPR_END);
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
	set_yylval_node(NEW_NTH_REF(parse_numvar(p), &_cur_loc));
	return tNTH_REF;

      default:
	if (!parser_is_identchar(p)) {
	    if (c == -1 || ISSPACE(c)) {
		compile_error(p, "`$' without identifiers is not allowed as a global variable name");
	    }
	    else {
		pushback(p, c);
		compile_error(p, "`$%c' is not allowed as a global variable name", c);
	    }
	    return 0;
	}
      case '0':
	tokadd(p, '$');
    }

    if (tokadd_ident(p, c)) return 0;
    SET_LEX_STATE(EXPR_END);
    tokenize_ident(p, last_state);
    return tGVAR;
}

static enum yytokentype
parse_atmark(struct parser_params *p, const enum lex_state_e last_state)
{
    enum yytokentype result = tIVAR;
    register int c = nextc(p);

    newtok(p);
    tokadd(p, '@');
    if (c == '@') {
	result = tCVAR;
	tokadd(p, '@');
	c = nextc(p);
    }
    if (c == -1 || ISSPACE(c)) {
	if (result == tIVAR) {
	    compile_error(p, "`@' without identifiers is not allowed as an instance variable name");
	}
	else {
	    compile_error(p, "`@@' without identifiers is not allowed as a class variable name");
	}
	return 0;
    }
    else if (ISDIGIT(c) || !parser_is_identchar(p)) {
	pushback(p, c);
	if (result == tIVAR) {
	    compile_error(p, "`@%c' is not allowed as an instance variable name", c);
	}
	else {
	    compile_error(p, "`@@%c' is not allowed as a class variable name", c);
	}
	return 0;
    }

    if (tokadd_ident(p, c)) return 0;
    SET_LEX_STATE(EXPR_END);
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
	    SET_LEX_STATE(kw->state);
	    if (IS_lex_state_for(state, EXPR_FNAME)) {
		set_yylval_name(rb_intern2(tok(p), toklen(p)));
		return kw->id[0];
	    }
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
      case ' ': case '\t': case '\f': case '\r':
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
	    if (!c && p->in_kwarg) {
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
	    if (strncmp(p->lex.pcur, "begin", 5) == 0 && ISSPACE(p->lex.pcur[5])) {
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
		    if (c != '=') continue;
		    if (c == '=' && strncmp(p->lex.pcur, "end", 3) == 0 &&
			(p->lex.pcur + 3 == p->lex.pend || ISSPACE(p->lex.pcur[3]))) {
			break;
		    }
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
	last_state = p->lex.state;
	c = nextc(p);
	if (c == '<' &&
	    !IS_lex_state(EXPR_DOT | EXPR_CLASS) &&
	    !IS_END() &&
	    (!IS_ARG() || IS_lex_state(EXPR_LABELED) || space_seen)) {
	    int token = heredoc_identifier(p);
	    if (token) return token;
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

      case '.':
	SET_LEX_STATE(EXPR_BEG);
	if ((c = nextc(p)) == '.') {
	    if ((c = nextc(p)) == '.') {
		return tDOT3;
	    }
	    pushback(p, c);
	    return tDOT2;
	}
	pushback(p, c);
	if (c != -1 && ISDIGIT(c)) {
	    yyerror0("no .<digit> floating literal anymore; put 0 before dot");
	}
	set_yylval_id('.');
	SET_LEX_STATE(EXPR_DOT);
	return '.';

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
	if (lambda_beginning_p()) {
	    SET_LEX_STATE(EXPR_BEG);
	    COND_PUSH(0);
	    CMDARG_PUSH(0);
	    p->lex.paren_nest++;
	    return tLAMBEG;
	}
	p->lex.paren_nest++;
	if (IS_lex_state(EXPR_LABELED))
	    c = tLBRACE;      /* hash */
	else if (IS_lex_state(EXPR_ARG_ANY | EXPR_END | EXPR_ENDFN))
	    c = '{';          /* block (primary) */
	else if (IS_lex_state(EXPR_ENDARG))
	    c = tLBRACE_ARG;  /* block (expr) */
	else
	    c = tLBRACE;      /* hash */
	COND_PUSH(0);
	CMDARG_PUSH(0);
	SET_LEX_STATE(c != tLBRACE ? EXPR_BEG : EXPR_BEG|EXPR_LABEL);
	if (c != tLBRACE) p->command_start = TRUE;
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
    if (has_delayed_token(p))
	dispatch_delayed_token(p, t);
    else if (t != 0)
	dispatch_scan_event(p, t);

    if (p->lex.strterm && (p->lex.strterm->flags & STRTERM_HEREDOC))
	RUBY_SET_YYLLOC_FROM_STRTERM_HEREDOC(*yylloc);
    else
	RUBY_SET_YYLLOC(*yylloc);

    return t;
}

#define LVAR_USED ((ID)1 << (sizeof(ID) * CHAR_BIT - 1))

static NODE*
node_newnode(struct parser_params *p, enum node_type type, VALUE a0, VALUE a1, VALUE a2, const rb_code_location_t *loc)
{
    NODE *n = rb_ast_newnode(p->ast);

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

/* concat two string literals */
static NODE *
literal_concat(struct parser_params *p, NODE *head, NODE *tail, const YYLTYPE *loc)
{
    enum node_type htype;
    NODE *headlast;
    VALUE lit;

    if (!head) return tail;
    if (!tail) return head;

    htype = nd_type(head);
    if (htype == NODE_EVSTR) {
	NODE *node = NEW_DSTR(add_mark_object(p, STR_NEW0()), loc);
	head = list_append(p, node, head);
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
	if (htype == NODE_DSTR && (headlast = head->nd_next->nd_end->nd_head) &&
	    nd_type(headlast) == NODE_STR) {
	    htype = NODE_STR;
	    lit = headlast->nd_lit;
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
	    head->nd_next->nd_end->nd_next = tail->nd_next;
	    head->nd_next->nd_end = tail->nd_next->nd_end;
	    rb_discard_node(p, tail);
	}
	else if (htype == NODE_DSTR && (headlast = head->nd_next->nd_end->nd_head) &&
		 nd_type(headlast) == NODE_STR) {
	    lit = headlast->nd_lit;
	    if (!literal_concat0(p, lit, tail->nd_lit))
		goto error;
	    tail->nd_lit = Qnil;
	    goto append;
	}
	else {
	    nd_set_type(tail, NODE_ARRAY);
	    tail->nd_head = NEW_STR(tail->nd_lit, loc);
	    list_concat(head, tail);
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
	node = list_append(p, NEW_DSTR(add_mark_object(p, STR_NEW0()), &node->nd_loc), node);
    }
    return node;
}

static NODE *
new_evstr(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    NODE *head = node;

    if (node) {
	switch (nd_type(node)) {
	  case NODE_STR: case NODE_DSTR: case NODE_EVSTR:
	    return node;
	}
    }
    return NEW_EVSTR(head, loc);
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

#define WARN_LOCATION(type) do { \
    if (p->warn_location) { \
	rb_warning0(type" in eval may not return location in binding;" \
		    " use Binding#source_location instead"); \
    } \
} while (0)

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
	WARN_LOCATION("__FILE__");
	{
	    VALUE file = p->ruby_sourcefile_string;
	    if (NIL_P(file))
		file = rb_str_new(0, 0);
	    else
		file = rb_str_dup(file);
	    node = NEW_STR(add_mark_object(p, file), loc);
	}
	return node;
      case keyword__LINE__:
	WARN_LOCATION("__LINE__");
	return NEW_LIT(INT2FIX(p->tokline), loc);
      case keyword__ENCODING__:
	return NEW_LIT(add_mark_object(p, rb_enc_from_encoding(p->enc)), loc);
    }
    switch (id_type(id)) {
      case ID_LOCAL:
	if (dyna_in_block(p) && dvar_defined_ref(p, id, &vidp)) {
	    if (id == p->cur_arg) {
		rb_warn1("circular argument reference - %"PRIsWARN, rb_id2str(id));
	    }
	    if (vidp) *vidp |= LVAR_USED;
	    node = NEW_DVAR(id, loc);
	    return node;
	}
	if (local_id_ref(p, id, &vidp)) {
	    if (id == p->cur_arg) {
		rb_warn1("circular argument reference - %"PRIsWARN, rb_id2str(id));
	    }
	    if (vidp) *vidp |= LVAR_USED;
	    node = NEW_LVAR(id, loc);
	    return node;
	}
# if WARN_PAST_SCOPE
	if (!p->in_defined && RTEST(ruby_verbose) && past_dvar_p(p, id)) {
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
    if (nd_type(symbol) == NODE_DSTR) {
	nd_set_type(symbol, NODE_DSYM);
    }
    else {
	nd_set_type(symbol, NODE_LIT);
	symbol->nd_lit = add_mark_object(p, rb_str_intern(symbol->nd_lit));
    }
    return list_append(p, symbols, symbol);
}

static NODE *
new_regexp(struct parser_params *p, NODE *node, int options, const YYLTYPE *loc)
{
    NODE *list, *prev;
    VALUE lit;

    if (!node) {
	return NEW_LIT(add_mark_object(p, reg_compile(p, STR_NEW0(), options)), loc);
    }
    switch (nd_type(node)) {
      case NODE_STR:
	{
	    VALUE src = node->nd_lit;
	    nd_set_type(node, NODE_LIT);
	    nd_set_loc(node, loc);
	    add_mark_object(p, node->nd_lit = reg_compile(p, src, options));
	}
	break;
      default:
	add_mark_object(p, lit = STR_NEW0());
	node = NEW_NODE(NODE_DSTR, lit, 1, NEW_LIST(node, loc), loc);
      case NODE_DSTR:
	nd_set_type(node, NODE_DREGX);
	nd_set_loc(node, loc);
	node->nd_cflag = options & RE_OPTION_MASK;
	if (!NIL_P(node->nd_lit)) reg_fragment_check(p, node->nd_lit, options);
	for (list = (prev = node)->nd_next; list; list = list->nd_next) {
	    if (nd_type(list->nd_head) == NODE_STR) {
		VALUE tail = list->nd_head->nd_lit;
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
	    add_mark_object(p, node->nd_lit = reg_compile(p, src, options));
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
	add_mark_object(p, lit);
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

#else  /* !RIPPER */
static int
id_is_var(struct parser_params *p, ID id)
{
    if (is_notop_id(id)) {
	switch (id & ID_SCOPE_MASK) {
	  case ID_GLOBAL: case ID_INSTANCE: case ID_CONST: case ID_CLASS:
	    return 1;
	  case ID_LOCAL:
	    if (dyna_in_block(p) && dvar_defined(p, id)) return 1;
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
static const char rb_parser_lex_state_names[][13] = {
    "EXPR_BEG",    "EXPR_END",    "EXPR_ENDARG", "EXPR_ENDFN",  "EXPR_ARG",
    "EXPR_CMDARG", "EXPR_MID",    "EXPR_FNAME",  "EXPR_DOT",    "EXPR_CLASS",
    "EXPR_LABEL",  "EXPR_LABELED","EXPR_FITEM",
};

static VALUE
append_lex_state_name(enum lex_state_e state, VALUE buf)
{
    int i, sep = 0;
    unsigned int mask = 1;
    static const char none[] = "EXPR_NONE";

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
    VALUE mesg = rb_str_new_cstr("internal p error: ");

    va_start(ap, fmt);
    rb_str_vcatf(mesg, fmt, ap);
    va_end(ap);
    parser_yyerror(p, NULL, RSTRING_PTR(mesg));
    RB_GC_GUARD(mesg);

    mesg = rb_str_new(0, 0);
    append_lex_state_name(p->lex.state, mesg);
    compile_error(p, "p->lex.state: %"PRIsVALUE, mesg);
    rb_str_resize(mesg, 0);
    append_bitstack_value(p->cond_stack, mesg);
    compile_error(p, "p->cond_stack: %"PRIsVALUE, mesg);
    rb_str_resize(mesg, 0);
    append_bitstack_value(p->cmdarg_stack, mesg);
    compile_error(p, "p->cmdarg_stack: %"PRIsVALUE, mesg);
    if (p->debug_output == rb_stdout)
	p->debug_output = rb_stderr;
    p->debug = TRUE;
}

void
rb_parser_set_location_from_strterm_heredoc(struct parser_params *p, rb_strterm_heredoc_t *here, YYLTYPE *yylloc)
{
    const char *eos = RSTRING_PTR(here->term);
    long term_len = RSTRING_LEN(here->term) - 2 + (unsigned char)eos[0];

    yylloc->beg_pos.lineno = (int)here->sourceline;
    yylloc->beg_pos.column = (int)(here->u3.lastidx - term_len);
    yylloc->end_pos.lineno = (int)here->sourceline;
    yylloc->end_pos.column = (int)(here->u3.lastidx);
}

void
rb_parser_set_location_of_none(struct parser_params *p, YYLTYPE *yylloc)
{
    yylloc->beg_pos.lineno = p->ruby_sourceline;
    yylloc->beg_pos.column = (int)(p->lex.ptok - p->lex.pbeg);
    yylloc->end_pos.lineno = p->ruby_sourceline;
    yylloc->end_pos.column = (int)(p->lex.ptok - p->lex.pbeg);
}

void
rb_parser_set_location(struct parser_params *p, YYLTYPE *yylloc)
{
    yylloc->beg_pos.lineno = p->ruby_sourceline;
    yylloc->beg_pos.column = (int)(p->lex.ptok - p->lex.pbeg);
    yylloc->end_pos.lineno = p->ruby_sourceline;
    yylloc->end_pos.column = (int)(p->lex.pcur - p->lex.pbeg);
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
	v = valp->val;
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
	if (!p->in_def) return NODE_CDECL;
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
    if (err) lhs = assign_error(p, lhs);
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

static NODE *
arg_append(struct parser_params *p, NODE *node1, NODE *node2, const YYLTYPE *loc)
{
    if (!node1) return NEW_LIST(node2, &node2->nd_loc);
    switch (nd_type(node1))  {
      case NODE_ARRAY:
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
        if (nd_type(node1->nd_body) != NODE_ARRAY) break;
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
	if (nd_type(node2) != NODE_ARRAY) break;
	node1->nd_body = list_concat(NEW_LIST(node1->nd_body, loc), node2);
	nd_set_type(node1, NODE_ARGSCAT);
	return node1;
      case NODE_ARGSCAT:
	if (nd_type(node2) != NODE_ARRAY ||
	    nd_type(node1->nd_body) != NODE_ARRAY) break;
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
    if ((nd_type(rest_arg) == NODE_ARRAY) && (n1 = splat_array(args)) != 0) {
	return list_concat(n1, rest_arg);
    }
    return arg_concat(p, args, rest_arg, loc);
}

static NODE *
splat_array(NODE* node)
{
    if (nd_type(node) == NODE_SPLAT) node = node->nd_head;
    if (nd_type(node) == NODE_ARRAY) return node;
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
node_assign(struct parser_params *p, NODE *lhs, NODE *rhs, const YYLTYPE *loc)
{
    if (!lhs) return 0;

    switch (nd_type(lhs)) {
      case NODE_GASGN:
      case NODE_IASGN:
      case NODE_LASGN:
      case NODE_DASGN:
      case NODE_DASGN_CURR:
      case NODE_MASGN:
      case NODE_CDECL:
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

static int
value_expr_gen(struct parser_params *p, NODE *node)
{
    int cond = 0;

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
	    if (!cond) yyerror1(&node->nd_loc, "void value expression");
	    /* or "control never reach"? */
	    return FALSE;

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
		node = node->nd_else;
		break;
	    }
	    else if (!node->nd_else) {
		node = node->nd_body;
		break;
	    }
	    if (!value_expr(node->nd_body)) return FALSE;
	    node = node->nd_else;
	    break;

	  case NODE_AND:
	  case NODE_OR:
	    cond = 1;
	    node = node->nd_2nd;
	    break;

	  case NODE_LASGN:
	  case NODE_DASGN:
	  case NODE_DASGN_CURR:
	  case NODE_MASGN:
	    mark_lvar_used(p, node);
	    return TRUE;

	  default:
	    return TRUE;
	}
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
      case NODE_ARRAY:
	do {
	    if (!is_static_content(node->nd_head)) return 0;
	} while ((node = node->nd_next) != 0);
      case NODE_LIT:
      case NODE_STR:
      case NODE_NIL:
      case NODE_TRUE:
      case NODE_FALSE:
      case NODE_ZARRAY:
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

static void
warn_unless_e_option(struct parser_params *p, NODE *node, const char *str)
{
    if (!e_option_supplied(p)) parser_warn(p, node, str);
}

static void
warning_unless_e_option(struct parser_params *p, NODE *node, const char *str)
{
    if (!e_option_supplied(p)) parser_warning(p, node, str);
}

static NODE *cond0(struct parser_params*,NODE*,int,const YYLTYPE*);

static NODE*
range_op(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    enum node_type type;

    if (node == 0) return 0;

    type = nd_type(node);
    value_expr(node);
    if (type == NODE_LIT && FIXNUM_P(node->nd_lit)) {
	warn_unless_e_option(p, node, "integer literal in conditional range");
	return NEW_CALL(node, tEQ, NEW_LIST(NEW_GVAR(rb_intern("$."), loc), loc), loc);
    }
    return cond0(p, node, FALSE, loc);
}

static int
literal_node(NODE *node)
{
    if (!node) return 1;	/* same as NODE_NIL */
    if (!(node = nd_once_body(node))) return 1;
    switch (nd_type(node)) {
      case NODE_LIT:
      case NODE_STR:
      case NODE_DSTR:
      case NODE_EVSTR:
      case NODE_DREGX:
      case NODE_DSYM:
	return 2;
      case NODE_TRUE:
      case NODE_FALSE:
      case NODE_NIL:
	return 1;
    }
    return 0;
}

static NODE*
cond0(struct parser_params *p, NODE *node, int method_op, const YYLTYPE *loc)
{
    if (node == 0) return 0;
    if (!(node = nd_once_body(node))) return 0;
    assign_in_cond(p, node);

    switch (nd_type(node)) {
      case NODE_DSTR:
      case NODE_EVSTR:
      case NODE_STR:
	if (!method_op) rb_warn0("string literal in condition");
	break;

      case NODE_DREGX:
	{
	    if (!method_op)
		warning_unless_e_option(p, node, "regex literal in condition");

	    return NEW_MATCH2(node, NEW_GVAR(idLASTLINE, loc), loc);
	}

      case NODE_AND:
      case NODE_OR:
	node->nd_1st = cond0(p, node->nd_1st, FALSE, loc);
	node->nd_2nd = cond0(p, node->nd_2nd, FALSE, loc);
	break;

      case NODE_DOT2:
      case NODE_DOT3:
	node->nd_beg = range_op(p, node->nd_beg, loc);
	node->nd_end = range_op(p, node->nd_end, loc);
	if (nd_type(node) == NODE_DOT2) nd_set_type(node,NODE_FLIP2);
	else if (nd_type(node) == NODE_DOT3) nd_set_type(node, NODE_FLIP3);
	if (!method_op && !e_option_supplied(p)) {
	    int b = literal_node(node->nd_beg);
	    int e = literal_node(node->nd_end);
	    if ((b == 1 && e == 1) || (b + e >= 2 && RTEST(ruby_verbose))) {
		parser_warn(p, node, "range literal in condition");
	    }
	}
	break;

      case NODE_DSYM:
	if (!method_op) parser_warning(p, node, "literal in condition");
	break;

      case NODE_LIT:
	if (RB_TYPE_P(node->nd_lit, T_REGEXP)) {
	    if (!method_op)
		warn_unless_e_option(p, node, "regex literal in condition");
	    nd_set_type(node, NODE_MATCH);
	}
	else if (node->nd_lit == Qtrue ||
		 node->nd_lit == Qfalse) {
	    /* booleans are OK, e.g., while true */
	}
	else {
	    if (!method_op)
		parser_warning(p, node, "literal in condition");
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
    return cond0(p, node, FALSE, loc);
}

static NODE*
method_cond(struct parser_params *p, NODE *node, const YYLTYPE *loc)
{
    if (node == 0) return 0;
    return cond0(p, node, TRUE, loc);
}

static NODE*
new_if(struct parser_params *p, NODE *cc, NODE *left, NODE *right, const YYLTYPE *loc)
{
    if (!cc) return right;
    cc = cond0(p, cc, FALSE, loc);
    return newline_node(NEW_IF(cc, left, right, loc));
}

static NODE*
new_unless(struct parser_params *p, NODE *cc, NODE *left, NODE *right, const YYLTYPE *loc)
{
    if (!cc) return right;
    cc = cond0(p, cc, FALSE, loc);
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
	if (nd_type(node) == NODE_ARRAY) {
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
	RRATIONAL_SET_NUM(lit, negate_lit(p, RRATIONAL(lit)->num));
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

    p->ruby_sourceline = saved_line;
    nd_set_loc(tail, loc);

    return tail;
}

static NODE*
new_args_tail(struct parser_params *p, NODE *kw_args, ID kw_rest_arg, ID block, const YYLTYPE *loc)
{
    int saved_line = p->ruby_sourceline;
    struct rb_args_info *args;
    NODE *node;
    rb_imemo_tmpbuf_t *tmpbuf = new_tmpbuf();

    args = ZALLOC(struct rb_args_info);
    tmpbuf->ptr = (VALUE *)args;
    node = NEW_NODE(NODE_ARGS, 0, 0, args, &NULL_LOC);
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

	args->kw_rest_arg = NEW_DVAR(kw_rest_arg, loc);
	args->kw_rest_arg->nd_cflag = kw_bits;
    }
    else if (kw_rest_arg) {
	args->kw_rest_arg = NEW_DVAR(kw_rest_arg, loc);
    }

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
	add_mark_object(p, node->nd_lit = ID2SYM(rb_intern_str(lit)));
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
    return result;
}

static NODE *
new_hash(struct parser_params *p, NODE *hash, const YYLTYPE *loc)
{
    if (hash) hash = remove_duplicate_keys(p, hash);
    return NEW_HASH(hash, loc);
}
#endif /* !RIPPER */

#ifndef RIPPER
static NODE *
new_op_assign(struct parser_params *p, NODE *lhs, ID op, NODE *rhs, const YYLTYPE *loc)
{
    NODE *asgn;

    if (lhs) {
	ID vid = lhs->nd_vid;
	YYLTYPE lhs_loc = lhs->nd_loc;
	if (op == tOROP) {
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
	    lhs->nd_value = rhs;
	    nd_set_loc(lhs, loc);
	    asgn = NEW_OP_ASGN_AND(gettable(p, vid, &lhs_loc), lhs, loc);
	}
	else {
	    asgn = lhs;
	    asgn->nd_value = NEW_CALL(gettable(p, vid, &lhs_loc), op, NEW_LIST(rhs, &rhs->nd_loc), loc);
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

    args = make_array(args, args_loc);
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
new_const_op_assign(struct parser_params *p, NODE *lhs, ID op, NODE *rhs, const YYLTYPE *loc)
{
    NODE *asgn;

    if (lhs) {
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
    if (p->in_def) {
	yyerror1(loc, "dynamic constant assignment");
    }
    return NEW_CDECL(0, 0, (path), loc);
}
#else
static VALUE
const_decl(struct parser_params *p, VALUE path)
{
    if (p->in_def) {
	path = dispatch1(assign_error, path);
	ripper_error(p);
    }
    return path;
}

static VALUE
assign_error(struct parser_params *p, VALUE a)
{
    a = dispatch1(assign_error, a);
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
    int i, cnt;
    ID *v, *u;

    if (!local->used) return;
    v = local->vars->tbl;
    u = local->used->tbl;
    cnt = local->used->pos;
    if (cnt != local->vars->pos) {
	rb_parser_fatal(p, "local->used->pos != local->vars->pos");
    }
    for (i = 0; i < cnt; ++i) {
	if (!v[i] || (u[i] & LVAR_USED)) continue;
	if (is_private_local_id(v[i])) continue;
	rb_warn1L((int)u[i], "assigned but unused variable - %"PRIsWARN, rb_id2str(v[i]));
    }
}

static void
local_push(struct parser_params *p, int toplevel_scope)
{
    struct local_vars *local;
    int inherits_dvars = toplevel_scope && (compile_for_eval || p->in_main /* is p->in_main really needed? */);
    int warn_unused_vars = RTEST(ruby_verbose);

    local = ALLOC(struct local_vars);
    local->prev = p->lvtbl;
    local->args = vtable_alloc(0);
    local->vars = vtable_alloc(inherits_dvars ? DVARS_INHERIT : DVARS_TOPSCOPE);
#ifndef RIPPER
    if (toplevel_scope && compile_for_eval) warn_unused_vars = 0;
    if (toplevel_scope && e_option_supplied(p)) warn_unused_vars = 0;
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
    rb_imemo_tmpbuf_t *tmpbuf = new_tmpbuf();

    if (cnt <= 0) return 0;
    buf = ALLOC_N(ID, cnt + 1);
    tmpbuf->ptr = (void *)buf;
    MEMCPY(buf+1, p->lvtbl->args->tbl, ID, cnt_args);
    /* remove IDs duplicated to warn shadowing */
    for (i = 0, j = cnt_args+1; i < cnt_vars; ++i) {
	ID id = p->lvtbl->vars->tbl[i];
	if (!vtable_included(p->lvtbl->args, id)) {
	    buf[j++] = id;
	}
    }
    if (--j < cnt) tmpbuf->ptr = (void *)REALLOC_N(buf, ID, (cnt = j) + 1);
    buf[0] = cnt;

    return buf;
}
#endif

static void
arg_var(struct parser_params *p, ID id)
{
    vtable_add(p->lvtbl->args, id);
}

static void
local_var(struct parser_params *p, ID id)
{
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
	return rb_local_defined(id, p->base_block);
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

    if (vars == DVARS_INHERIT) {
        return rb_dvar_defined(id, p->base_block);
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
    if (len < MAX_WORD_LENGTH && rb_reserved_word(s, (int)len))
        return ST_CONTINUE;
    if (rb_enc_symname_type(s, len, enc, (1U<<ID_LOCAL)) != ID_LOCAL)
        return ST_CONTINUE;

    var = intern_cstr(s, len, enc);
    node = node_assign(p, assignable(p, var, 0, arg->loc), NEW_LIT(ID2SYM(var), arg->loc), arg->loc);
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

void
rb_parser_warn_location(VALUE vparser, int warn)
{
    struct parser_params *p;
    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    p->warn_location = warn;
}

static NODE *
parser_append_options(struct parser_params *p, NODE *node)
{
    static const YYLTYPE default_location = {{1, 0}, {1, 0}};
    const YYLTYPE *const LOC = &default_location;

    if (p->do_print) {
	NODE *print = NEW_FCALL(rb_intern("print"),
				NEW_ARRAY(NEW_GVAR(idLASTLINE, LOC), LOC),
				LOC);
	node = block_append(p, node, print);
    }

    if (p->do_loop) {
	if (p->do_split) {
	    NODE *split = NEW_GASGN(rb_intern("$F"),
				    NEW_CALL(NEW_GVAR(idLASTLINE, LOC),
					     rb_intern("split"), 0, LOC),
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
#endif /* !RIPPER */

static ID
internal_id(struct parser_params *p)
{
    const ID max_id = RB_ID_SERIAL_MAX & ~0xffff;
    ID id = (ID)vtable_size(p->lvtbl->args) + (ID)vtable_size(p->lvtbl->vars);
    id = max_id - id;
    return ID_STATIC_SYM | ID_INTERNAL | (id << ID_SCOPE_SHIFT);
}

static void
parser_initialize(struct parser_params *p)
{
    /* note: we rely on TypedData_Make_Struct to set most fields to 0 */
    p->command_start = TRUE;
    p->ruby_sourcefile_string = Qnil;
    p->lex.lpar_beg = -1; /* make lambda_beginning_p() == FALSE at first */
    p->node_id = 0;
#ifdef RIPPER
    p->delayed = Qnil;
    p->result = Qnil;
    p->parsing_thread = Qnil;
#else
    p->error_buffer = Qfalse;
#endif
    p->debug_buffer = Qnil;
    p->debug_output = rb_stdout;
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
#ifndef RIPPER
    rb_gc_mark(p->debug_lines);
    rb_gc_mark(p->compile_option);
    rb_gc_mark(p->error_buffer);
#else
    rb_gc_mark(p->delayed);
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
rb_parser_set_context(VALUE vparser, const struct rb_block *base, int main)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    p->error_buffer = main ? Qfalse : Qnil;
    p->base_block = base;
    p->in_main = main;
    return vparser;
}
#endif

#ifdef RIPPER
#define rb_parser_end_seen_p ripper_parser_end_seen_p
#define rb_parser_encoding ripper_parser_encoding
#define rb_parser_get_yydebug ripper_parser_get_yydebug
#define rb_parser_set_yydebug ripper_parser_set_yydebug
static VALUE ripper_parser_end_seen_p(VALUE vparser);
static VALUE ripper_parser_encoding(VALUE vparser);
static VALUE ripper_parser_get_yydebug(VALUE self);
static VALUE ripper_parser_set_yydebug(VALUE self, VALUE flag);

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
#endif

#ifdef RIPPER
#ifdef RIPPER_DEBUG
extern int rb_is_pointer_to_heap(VALUE);

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
    if (!rb_is_pointer_to_heap(x))
        rb_raise(rb_eArgError, "invalid pointer: %p", x);
    switch (BUILTIN_TYPE(x)) {
      case T_STRING:
      case T_OBJECT:
      case T_ARRAY:
      case T_BIGNUM:
      case T_FLOAT:
      case T_COMPLEX:
      case T_RATIONAL:
        return x;
      case T_NODE:
	if (nd_type(x) != NODE_RIPPER) {
	    rb_raise(rb_eArgError, "NODE given: %p", x);
	}
	return ((NODE *)x)->nd_rval;
      default:
        rb_raise(rb_eArgError, "wrong type of ruby object: %p (%s)",
                 x, rb_obj_classname(x));
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
    rb_define_method(Ripper, "end_seen?", rb_parser_end_seen_p, 0);
    rb_define_method(Ripper, "encoding", rb_parser_encoding, 0);
    rb_define_method(Ripper, "yydebug", rb_parser_get_yydebug, 0);
    rb_define_method(Ripper, "yydebug=", rb_parser_set_yydebug, 1);
    rb_define_method(Ripper, "error?", ripper_error_p, 0);
#ifdef RIPPER_DEBUG
    rb_define_method(rb_mKernel, "assert_Qundef", ripper_assert_Qundef, 2);
    rb_define_method(rb_mKernel, "rawVALUE", ripper_value, 1);
    rb_define_method(rb_mKernel, "validate_object", ripper_validate_object, 1);
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
