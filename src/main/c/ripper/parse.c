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
#line 14 "parse.y"


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
#include "internal/encoding.h"
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

#if defined(__GNUC__) && !defined(__clang__)
// Suppress "parameter passing for argument of type 'struct
// lex_context' changed" notes.  `struct lex_context` is file scope,
// and has no ABI compatibility issue.
RBIMPL_WARNING_PUSH()
RBIMPL_WARNING_IGNORED(-Wpsabi)
RBIMPL_WARNING_POP()
// Not sure why effective even after popped.
#endif

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
#define RUBY_SET_YYLLOC_OF_DELAYED_TOKEN(Current)			\
    rb_parser_set_location_of_delayed_token(p, &(Current))
#define RUBY_SET_YYLLOC_OF_HEREDOC_END(Current)				\
    rb_parser_set_location_of_heredoc_end(p, &(Current))
#define RUBY_SET_YYLLOC_OF_DUMMY_END(Current)				\
    rb_parser_set_location_of_dummy_end(p, &(Current))
#define RUBY_SET_YYLLOC_OF_NONE(Current)				\
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
#define NUMPARAM_ID_TO_IDX(id) (unsigned int)(((id) >> ID_SCOPE_SHIFT) - (tNUMPARAM_1 - 1))
#define NUMPARAM_IDX_TO_ID(idx) TOKEN2LOCALID((tNUMPARAM_1 - 1 + (idx)))
static int
numparam_id_p(ID id)
{
    if (!is_local_id(id) || id < (tNUMPARAM_1 << ID_SCOPE_SHIFT)) return 0;
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
    YYLTYPE *yylloc;

    struct {
	rb_strterm_t *strterm;
	VALUE (*gets)(struct parser_params*,VALUE);
	VALUE input;
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

    struct {
	VALUE token;
	int beg_line;
	int beg_col;
	int end_line;
	int end_col;
    } delayed;

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
    unsigned int error_tolerant: 1;
    unsigned int keep_tokens: 1;

    NODE *eval_tree_begin;
    NODE *eval_tree;
    VALUE error_buffer;
    VALUE debug_lines;
    const struct rb_iseq_struct *parent_iseq;
    /* store specific keyword locations to generate dummy end token */
    VALUE end_expect_token_locations;
    /* id for terms */
    int token_id;
    /* Array for term tokens */
    VALUE tokens;
#else
    /* Ripper only */

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

#ifndef RIPPER
static void flush_debug_buffer(struct parser_params *p, VALUE out, VALUE str);

static void
debug_end_expect_token_locations(struct parser_params *p, const char *name)
{
    if(p->debug) {
        VALUE mesg = rb_sprintf("%s: ", name);
        rb_str_catf(mesg, " %"PRIsVALUE"\n", p->end_expect_token_locations);
        flush_debug_buffer(p, p->debug_output, mesg);
    }
}

static void
push_end_expect_token_locations(struct parser_params *p, const rb_code_position_t *pos)
{
    if(NIL_P(p->end_expect_token_locations)) return;
    rb_ary_push(p->end_expect_token_locations, rb_ary_new_from_args(2, INT2NUM(pos->lineno), INT2NUM(pos->column)));
    debug_end_expect_token_locations(p, "push_end_expect_token_locations");
}

static void
pop_end_expect_token_locations(struct parser_params *p)
{
    if(NIL_P(p->end_expect_token_locations)) return;
    rb_ary_pop(p->end_expect_token_locations);
    debug_end_expect_token_locations(p, "pop_end_expect_token_locations");
}

static VALUE
peek_end_expect_token_locations(struct parser_params *p)
{
    if(NIL_P(p->end_expect_token_locations)) return Qnil;
    return rb_ary_last(0, 0, p->end_expect_token_locations);
}

static ID
parser_token2id(enum yytokentype tok)
{
    switch ((int) tok) {
#define TOKEN2ID(tok) case tok: return rb_intern(#tok);
#define TOKEN2ID2(tok, name) case tok: return rb_intern(name);
      TOKEN2ID2(' ', "words_sep")
      TOKEN2ID2('!', "!")
      TOKEN2ID2('%', "%");
      TOKEN2ID2('&', "&");
      TOKEN2ID2('*', "*");
      TOKEN2ID2('+', "+");
      TOKEN2ID2('-', "-");
      TOKEN2ID2('/', "/");
      TOKEN2ID2('<', "<");
      TOKEN2ID2('=', "=");
      TOKEN2ID2('>', ">");
      TOKEN2ID2('?', "?");
      TOKEN2ID2('^', "^");
      TOKEN2ID2('|', "|");
      TOKEN2ID2('~', "~");
      TOKEN2ID2(':', ":");
      TOKEN2ID2(',', ",");
      TOKEN2ID2('.', ".");
      TOKEN2ID2(';', ";");
      TOKEN2ID2('`', "`");
      TOKEN2ID2('\n', "nl");
      TOKEN2ID2('{', "{");
      TOKEN2ID2('}', "}");
      TOKEN2ID2('[', "[");
      TOKEN2ID2(']', "]");
      TOKEN2ID2('(', "(");
      TOKEN2ID2(')', ")");
      TOKEN2ID(keyword_class);
      TOKEN2ID(keyword_module);
      TOKEN2ID(keyword_def);
      TOKEN2ID(keyword_undef);
      TOKEN2ID(keyword_begin);
      TOKEN2ID(keyword_rescue);
      TOKEN2ID(keyword_ensure);
      TOKEN2ID(keyword_end);
      TOKEN2ID(keyword_if);
      TOKEN2ID(keyword_unless);
      TOKEN2ID(keyword_then);
      TOKEN2ID(keyword_elsif);
      TOKEN2ID(keyword_else);
      TOKEN2ID(keyword_case);
      TOKEN2ID(keyword_when);
      TOKEN2ID(keyword_while);
      TOKEN2ID(keyword_until);
      TOKEN2ID(keyword_for);
      TOKEN2ID(keyword_break);
      TOKEN2ID(keyword_next);
      TOKEN2ID(keyword_redo);
      TOKEN2ID(keyword_retry);
      TOKEN2ID(keyword_in);
      TOKEN2ID(keyword_do);
      TOKEN2ID(keyword_do_cond);
      TOKEN2ID(keyword_do_block);
      TOKEN2ID(keyword_do_LAMBDA);
      TOKEN2ID(keyword_return);
      TOKEN2ID(keyword_yield);
      TOKEN2ID(keyword_super);
      TOKEN2ID(keyword_self);
      TOKEN2ID(keyword_nil);
      TOKEN2ID(keyword_true);
      TOKEN2ID(keyword_false);
      TOKEN2ID(keyword_and);
      TOKEN2ID(keyword_or);
      TOKEN2ID(keyword_not);
      TOKEN2ID(modifier_if);
      TOKEN2ID(modifier_unless);
      TOKEN2ID(modifier_while);
      TOKEN2ID(modifier_until);
      TOKEN2ID(modifier_rescue);
      TOKEN2ID(keyword_alias);
      TOKEN2ID(keyword_defined);
      TOKEN2ID(keyword_BEGIN);
      TOKEN2ID(keyword_END);
      TOKEN2ID(keyword__LINE__);
      TOKEN2ID(keyword__FILE__);
      TOKEN2ID(keyword__ENCODING__);
      TOKEN2ID(tIDENTIFIER);
      TOKEN2ID(tFID);
      TOKEN2ID(tGVAR);
      TOKEN2ID(tIVAR);
      TOKEN2ID(tCONSTANT);
      TOKEN2ID(tCVAR);
      TOKEN2ID(tLABEL);
      TOKEN2ID(tINTEGER);
      TOKEN2ID(tFLOAT);
      TOKEN2ID(tRATIONAL);
      TOKEN2ID(tIMAGINARY);
      TOKEN2ID(tCHAR);
      TOKEN2ID(tNTH_REF);
      TOKEN2ID(tBACK_REF);
      TOKEN2ID(tSTRING_CONTENT);
      TOKEN2ID(tREGEXP_END);
      TOKEN2ID(tDUMNY_END);
      TOKEN2ID(tSP);
      TOKEN2ID(tUPLUS);
      TOKEN2ID(tUMINUS);
      TOKEN2ID(tPOW);
      TOKEN2ID(tCMP);
      TOKEN2ID(tEQ);
      TOKEN2ID(tEQQ);
      TOKEN2ID(tNEQ);
      TOKEN2ID(tGEQ);
      TOKEN2ID(tLEQ);
      TOKEN2ID(tANDOP);
      TOKEN2ID(tOROP);
      TOKEN2ID(tMATCH);
      TOKEN2ID(tNMATCH);
      TOKEN2ID(tDOT2);
      TOKEN2ID(tDOT3);
      TOKEN2ID(tBDOT2);
      TOKEN2ID(tBDOT3);
      TOKEN2ID(tAREF);
      TOKEN2ID(tASET);
      TOKEN2ID(tLSHFT);
      TOKEN2ID(tRSHFT);
      TOKEN2ID(tANDDOT);
      TOKEN2ID(tCOLON2);
      TOKEN2ID(tCOLON3);
      TOKEN2ID(tOP_ASGN);
      TOKEN2ID(tASSOC);
      TOKEN2ID(tLPAREN);
      TOKEN2ID(tLPAREN_ARG);
      TOKEN2ID(tRPAREN);
      TOKEN2ID(tLBRACK);
      TOKEN2ID(tLBRACE);
      TOKEN2ID(tLBRACE_ARG);
      TOKEN2ID(tSTAR);
      TOKEN2ID(tDSTAR);
      TOKEN2ID(tAMPER);
      TOKEN2ID(tLAMBDA);
      TOKEN2ID(tSYMBEG);
      TOKEN2ID(tSTRING_BEG);
      TOKEN2ID(tXSTRING_BEG);
      TOKEN2ID(tREGEXP_BEG);
      TOKEN2ID(tWORDS_BEG);
      TOKEN2ID(tQWORDS_BEG);
      TOKEN2ID(tSYMBOLS_BEG);
      TOKEN2ID(tQSYMBOLS_BEG);
      TOKEN2ID(tSTRING_END);
      TOKEN2ID(tSTRING_DEND);
      TOKEN2ID(tSTRING_DBEG);
      TOKEN2ID(tSTRING_DVAR);
      TOKEN2ID(tLAMBEG);
      TOKEN2ID(tLABEL_END);
      TOKEN2ID(tIGNORED_NL);
      TOKEN2ID(tCOMMENT);
      TOKEN2ID(tEMBDOC_BEG);
      TOKEN2ID(tEMBDOC);
      TOKEN2ID(tEMBDOC_END);
      TOKEN2ID(tHEREDOC_BEG);
      TOKEN2ID(tHEREDOC_END);
      TOKEN2ID(k__END__);
      TOKEN2ID(tLOWEST);
      TOKEN2ID(tUMINUS_NUM);
      TOKEN2ID(tLAST_TOKEN);
#undef TOKEN2ID
#undef TOKEN2ID2
    }

    rb_bug("parser_token2id: unknown token %d", tok);

    UNREACHABLE_RETURN(0);
}

#endif

RBIMPL_ATTR_NONNULL((1, 2, 3))
static int parser_yyerror(struct parser_params*, const YYLTYPE *yylloc, const char*);
RBIMPL_ATTR_NONNULL((1, 2))
static int parser_yyerror0(struct parser_params*, const char*);
#define yyerror0(msg) parser_yyerror0(p, (msg))
#define yyerror1(loc, msg) parser_yyerror(p, (loc), (msg))
#define yyerror(yylloc, p, msg) parser_yyerror(p, yylloc, msg)
#define token_flush(ptr) ((ptr)->lex.ptok = (ptr)->lex.pcur)
#define lex_goto_eol(p) ((p)->lex.pcur = (p)->lex.pend)
#define lex_eol_p(p) ((p)->lex.pcur >= (p)->lex.pend)
#define lex_eol_n_p(p,n) ((p)->lex.pcur+(n) >= (p)->lex.pend)

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
YYLTYPE *rb_parser_set_location_of_delayed_token(struct parser_params *p, YYLTYPE *yylloc);
YYLTYPE *rb_parser_set_location_of_heredoc_end(struct parser_params *p, YYLTYPE *yylloc);
YYLTYPE *rb_parser_set_location_of_dummy_end(struct parser_params *p, YYLTYPE *yylloc);
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
#define idFWD_KWREST idPow /* Use simple "**", as tDSTAR is "**arg" */
#define idFWD_BLOCK  '&'
#define idFWD_ALL    idDot3
#define FORWARD_ARGS_WITH_RUBY2_KEYWORDS

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

#define debug_token_line(p, name, line) if (p->debug) rb_parser_printf(p, name ":%d (%d: %ld|%ld|%ld)\n", line, p->ruby_sourceline, p->lex.ptok - p->lex.pbeg, p->lex.pcur - p->lex.ptok, p->lex.pend - p->lex.pcur)

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

#line 1385 "parse.c"

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
    tDUMNY_END = 323,              /* "dummy end"  */
    tSP = 324,                     /* "escaped space"  */
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
    tCOLON3 = 325,                 /* ":: at EXPR_BEG"  */
    tOP_ASGN = 326,                /* "operator-assignment"  */
    tASSOC = 327,                  /* "=>"  */
    tLPAREN = 328,                 /* "("  */
    tLPAREN_ARG = 329,             /* "( arg"  */
    tRPAREN = 330,                 /* ")"  */
    tLBRACK = 331,                 /* "["  */
    tLBRACE = 332,                 /* "{"  */
    tLBRACE_ARG = 333,             /* "{ arg"  */
    tSTAR = 334,                   /* "*"  */
    tDSTAR = 335,                  /* "**arg"  */
    tAMPER = 336,                  /* "&"  */
    tLAMBDA = 337,                 /* "->"  */
    tSYMBEG = 338,                 /* "symbol literal"  */
    tSTRING_BEG = 339,             /* "string literal"  */
    tXSTRING_BEG = 340,            /* "backtick literal"  */
    tREGEXP_BEG = 341,             /* "regexp literal"  */
    tWORDS_BEG = 342,              /* "word list"  */
    tQWORDS_BEG = 343,             /* "verbatim word list"  */
    tSYMBOLS_BEG = 344,            /* "symbol list"  */
    tQSYMBOLS_BEG = 345,           /* "verbatim symbol list"  */
    tSTRING_END = 346,             /* "terminator"  */
    tSTRING_DEND = 347,            /* "'}'"  */
    tSTRING_DBEG = 348,            /* tSTRING_DBEG  */
    tSTRING_DVAR = 349,            /* tSTRING_DVAR  */
    tLAMBEG = 350,                 /* tLAMBEG  */
    tLABEL_END = 351,              /* tLABEL_END  */
    tIGNORED_NL = 352,             /* tIGNORED_NL  */
    tCOMMENT = 353,                /* tCOMMENT  */
    tEMBDOC_BEG = 354,             /* tEMBDOC_BEG  */
    tEMBDOC = 355,                 /* tEMBDOC  */
    tEMBDOC_END = 356,             /* tEMBDOC_END  */
    tHEREDOC_BEG = 357,            /* tHEREDOC_BEG  */
    tHEREDOC_END = 358,            /* tHEREDOC_END  */
    k__END__ = 359,                /* k__END__  */
    tLOWEST = 360,                 /* tLOWEST  */
    tUMINUS_NUM = 361,             /* tUMINUS_NUM  */
    tLAST_TOKEN = 362              /* tLAST_TOKEN  */
  };
  typedef enum yytokentype yytoken_kind_t;
#endif

/* Value type.  */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
union YYSTYPE
{
#line 1375 "parse.y"

    VALUE val;
    NODE *node;
    ID id;
    int num;
    st_table *tbl;
    const struct vtable *vars;
    struct rb_strterm_struct *strterm;
    struct lex_context ctxt;

#line 1576 "parse.c"

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
  YYSYMBOL_tDUMNY_END = 68,                /* "dummy end"  */
  YYSYMBOL_69_ = 69,                       /* '.'  */
  YYSYMBOL_70_backslash_ = 70,             /* "backslash"  */
  YYSYMBOL_tSP = 71,                       /* "escaped space"  */
  YYSYMBOL_72_escaped_horizontal_tab_ = 72, /* "escaped horizontal tab"  */
  YYSYMBOL_73_escaped_form_feed_ = 73,     /* "escaped form feed"  */
  YYSYMBOL_74_escaped_carriage_return_ = 74, /* "escaped carriage return"  */
  YYSYMBOL_75_escaped_vertical_tab_ = 75,  /* "escaped vertical tab"  */
  YYSYMBOL_tUPLUS = 76,                    /* "unary+"  */
  YYSYMBOL_tUMINUS = 77,                   /* "unary-"  */
  YYSYMBOL_tPOW = 78,                      /* "**"  */
  YYSYMBOL_tCMP = 79,                      /* "<=>"  */
  YYSYMBOL_tEQ = 80,                       /* "=="  */
  YYSYMBOL_tEQQ = 81,                      /* "==="  */
  YYSYMBOL_tNEQ = 82,                      /* "!="  */
  YYSYMBOL_tGEQ = 83,                      /* ">="  */
  YYSYMBOL_tLEQ = 84,                      /* "<="  */
  YYSYMBOL_tANDOP = 85,                    /* "&&"  */
  YYSYMBOL_tOROP = 86,                     /* "||"  */
  YYSYMBOL_tMATCH = 87,                    /* "=~"  */
  YYSYMBOL_tNMATCH = 88,                   /* "!~"  */
  YYSYMBOL_tDOT2 = 89,                     /* ".."  */
  YYSYMBOL_tDOT3 = 90,                     /* "..."  */
  YYSYMBOL_tBDOT2 = 91,                    /* "(.."  */
  YYSYMBOL_tBDOT3 = 92,                    /* "(..."  */
  YYSYMBOL_tAREF = 93,                     /* "[]"  */
  YYSYMBOL_tASET = 94,                     /* "[]="  */
  YYSYMBOL_tLSHFT = 95,                    /* "<<"  */
  YYSYMBOL_tRSHFT = 96,                    /* ">>"  */
  YYSYMBOL_tANDDOT = 97,                   /* "&."  */
  YYSYMBOL_tCOLON2 = 98,                   /* "::"  */
  YYSYMBOL_tCOLON3 = 99,                   /* ":: at EXPR_BEG"  */
  YYSYMBOL_tOP_ASGN = 100,                 /* "operator-assignment"  */
  YYSYMBOL_tASSOC = 101,                   /* "=>"  */
  YYSYMBOL_tLPAREN = 102,                  /* "("  */
  YYSYMBOL_tLPAREN_ARG = 103,              /* "( arg"  */
  YYSYMBOL_tRPAREN = 104,                  /* ")"  */
  YYSYMBOL_tLBRACK = 105,                  /* "["  */
  YYSYMBOL_tLBRACE = 106,                  /* "{"  */
  YYSYMBOL_tLBRACE_ARG = 107,              /* "{ arg"  */
  YYSYMBOL_tSTAR = 108,                    /* "*"  */
  YYSYMBOL_tDSTAR = 109,                   /* "**arg"  */
  YYSYMBOL_tAMPER = 110,                   /* "&"  */
  YYSYMBOL_tLAMBDA = 111,                  /* "->"  */
  YYSYMBOL_tSYMBEG = 112,                  /* "symbol literal"  */
  YYSYMBOL_tSTRING_BEG = 113,              /* "string literal"  */
  YYSYMBOL_tXSTRING_BEG = 114,             /* "backtick literal"  */
  YYSYMBOL_tREGEXP_BEG = 115,              /* "regexp literal"  */
  YYSYMBOL_tWORDS_BEG = 116,               /* "word list"  */
  YYSYMBOL_tQWORDS_BEG = 117,              /* "verbatim word list"  */
  YYSYMBOL_tSYMBOLS_BEG = 118,             /* "symbol list"  */
  YYSYMBOL_tQSYMBOLS_BEG = 119,            /* "verbatim symbol list"  */
  YYSYMBOL_tSTRING_END = 120,              /* "terminator"  */
  YYSYMBOL_tSTRING_DEND = 121,             /* "'}'"  */
  YYSYMBOL_tSTRING_DBEG = 122,             /* tSTRING_DBEG  */
  YYSYMBOL_tSTRING_DVAR = 123,             /* tSTRING_DVAR  */
  YYSYMBOL_tLAMBEG = 124,                  /* tLAMBEG  */
  YYSYMBOL_tLABEL_END = 125,               /* tLABEL_END  */
  YYSYMBOL_tIGNORED_NL = 126,              /* tIGNORED_NL  */
  YYSYMBOL_tCOMMENT = 127,                 /* tCOMMENT  */
  YYSYMBOL_tEMBDOC_BEG = 128,              /* tEMBDOC_BEG  */
  YYSYMBOL_tEMBDOC = 129,                  /* tEMBDOC  */
  YYSYMBOL_tEMBDOC_END = 130,              /* tEMBDOC_END  */
  YYSYMBOL_tHEREDOC_BEG = 131,             /* tHEREDOC_BEG  */
  YYSYMBOL_tHEREDOC_END = 132,             /* tHEREDOC_END  */
  YYSYMBOL_k__END__ = 133,                 /* k__END__  */
  YYSYMBOL_tLOWEST = 134,                  /* tLOWEST  */
  YYSYMBOL_135_ = 135,                     /* '='  */
  YYSYMBOL_136_ = 136,                     /* '?'  */
  YYSYMBOL_137_ = 137,                     /* ':'  */
  YYSYMBOL_138_ = 138,                     /* '>'  */
  YYSYMBOL_139_ = 139,                     /* '<'  */
  YYSYMBOL_140_ = 140,                     /* '|'  */
  YYSYMBOL_141_ = 141,                     /* '^'  */
  YYSYMBOL_142_ = 142,                     /* '&'  */
  YYSYMBOL_143_ = 143,                     /* '+'  */
  YYSYMBOL_144_ = 144,                     /* '-'  */
  YYSYMBOL_145_ = 145,                     /* '*'  */
  YYSYMBOL_146_ = 146,                     /* '/'  */
  YYSYMBOL_147_ = 147,                     /* '%'  */
  YYSYMBOL_tUMINUS_NUM = 148,              /* tUMINUS_NUM  */
  YYSYMBOL_149_ = 149,                     /* '!'  */
  YYSYMBOL_150_ = 150,                     /* '~'  */
  YYSYMBOL_tLAST_TOKEN = 151,              /* tLAST_TOKEN  */
  YYSYMBOL_152_ = 152,                     /* '{'  */
  YYSYMBOL_153_ = 153,                     /* '}'  */
  YYSYMBOL_154_ = 154,                     /* '['  */
  YYSYMBOL_155_ = 155,                     /* ','  */
  YYSYMBOL_156_ = 156,                     /* '`'  */
  YYSYMBOL_157_ = 157,                     /* '('  */
  YYSYMBOL_158_ = 158,                     /* ')'  */
  YYSYMBOL_159_ = 159,                     /* ']'  */
  YYSYMBOL_160_ = 160,                     /* ';'  */
  YYSYMBOL_161_ = 161,                     /* ' '  */
  YYSYMBOL_162_n_ = 162,                   /* '\n'  */
  YYSYMBOL_YYACCEPT = 163,                 /* $accept  */
  YYSYMBOL_program = 164,                  /* program  */
  YYSYMBOL_165_1 = 165,                    /* $@1  */
  YYSYMBOL_top_compstmt = 166,             /* top_compstmt  */
  YYSYMBOL_top_stmts = 167,                /* top_stmts  */
  YYSYMBOL_top_stmt = 168,                 /* top_stmt  */
  YYSYMBOL_begin_block = 169,              /* begin_block  */
  YYSYMBOL_bodystmt = 170,                 /* bodystmt  */
  YYSYMBOL_171_2 = 171,                    /* $@2  */
  YYSYMBOL_compstmt = 172,                 /* compstmt  */
  YYSYMBOL_stmts = 173,                    /* stmts  */
  YYSYMBOL_stmt_or_begin = 174,            /* stmt_or_begin  */
  YYSYMBOL_175_3 = 175,                    /* $@3  */
  YYSYMBOL_stmt = 176,                     /* stmt  */
  YYSYMBOL_177_4 = 177,                    /* $@4  */
  YYSYMBOL_command_asgn = 178,             /* command_asgn  */
  YYSYMBOL_command_rhs = 179,              /* command_rhs  */
  YYSYMBOL_expr = 180,                     /* expr  */
  YYSYMBOL_181_5 = 181,                    /* @5  */
  YYSYMBOL_182_6 = 182,                    /* @6  */
  YYSYMBOL_183_7 = 183,                    /* @7  */
  YYSYMBOL_184_8 = 184,                    /* @8  */
  YYSYMBOL_def_name = 185,                 /* def_name  */
  YYSYMBOL_defn_head = 186,                /* defn_head  */
  YYSYMBOL_defs_head = 187,                /* defs_head  */
  YYSYMBOL_188_9 = 188,                    /* $@9  */
  YYSYMBOL_expr_value = 189,               /* expr_value  */
  YYSYMBOL_expr_value_do = 190,            /* expr_value_do  */
  YYSYMBOL_191_10 = 191,                   /* $@10  */
  YYSYMBOL_192_11 = 192,                   /* $@11  */
  YYSYMBOL_command_call = 193,             /* command_call  */
  YYSYMBOL_block_command = 194,            /* block_command  */
  YYSYMBOL_cmd_brace_block = 195,          /* cmd_brace_block  */
  YYSYMBOL_fcall = 196,                    /* fcall  */
  YYSYMBOL_command = 197,                  /* command  */
  YYSYMBOL_mlhs = 198,                     /* mlhs  */
  YYSYMBOL_mlhs_inner = 199,               /* mlhs_inner  */
  YYSYMBOL_mlhs_basic = 200,               /* mlhs_basic  */
  YYSYMBOL_mlhs_item = 201,                /* mlhs_item  */
  YYSYMBOL_mlhs_head = 202,                /* mlhs_head  */
  YYSYMBOL_mlhs_post = 203,                /* mlhs_post  */
  YYSYMBOL_mlhs_node = 204,                /* mlhs_node  */
  YYSYMBOL_lhs = 205,                      /* lhs  */
  YYSYMBOL_cname = 206,                    /* cname  */
  YYSYMBOL_cpath = 207,                    /* cpath  */
  YYSYMBOL_fname = 208,                    /* fname  */
  YYSYMBOL_fitem = 209,                    /* fitem  */
  YYSYMBOL_undef_list = 210,               /* undef_list  */
  YYSYMBOL_211_12 = 211,                   /* $@12  */
  YYSYMBOL_op = 212,                       /* op  */
  YYSYMBOL_reswords = 213,                 /* reswords  */
  YYSYMBOL_arg = 214,                      /* arg  */
  YYSYMBOL_215_13 = 215,                   /* $@13  */
  YYSYMBOL_relop = 216,                    /* relop  */
  YYSYMBOL_rel_expr = 217,                 /* rel_expr  */
  YYSYMBOL_lex_ctxt = 218,                 /* lex_ctxt  */
  YYSYMBOL_arg_value = 219,                /* arg_value  */
  YYSYMBOL_aref_args = 220,                /* aref_args  */
  YYSYMBOL_arg_rhs = 221,                  /* arg_rhs  */
  YYSYMBOL_paren_args = 222,               /* paren_args  */
  YYSYMBOL_opt_paren_args = 223,           /* opt_paren_args  */
  YYSYMBOL_opt_call_args = 224,            /* opt_call_args  */
  YYSYMBOL_call_args = 225,                /* call_args  */
  YYSYMBOL_command_args = 226,             /* command_args  */
  YYSYMBOL_227_14 = 227,                   /* $@14  */
  YYSYMBOL_block_arg = 228,                /* block_arg  */
  YYSYMBOL_opt_block_arg = 229,            /* opt_block_arg  */
  YYSYMBOL_args = 230,                     /* args  */
  YYSYMBOL_mrhs_arg = 231,                 /* mrhs_arg  */
  YYSYMBOL_mrhs = 232,                     /* mrhs  */
  YYSYMBOL_primary = 233,                  /* primary  */
  YYSYMBOL_234_15 = 234,                   /* $@15  */
  YYSYMBOL_235_16 = 235,                   /* $@16  */
  YYSYMBOL_236_17 = 236,                   /* $@17  */
  YYSYMBOL_237_18 = 237,                   /* $@18  */
  YYSYMBOL_238_19 = 238,                   /* @19  */
  YYSYMBOL_239_20 = 239,                   /* @20  */
  YYSYMBOL_240_21 = 240,                   /* $@21  */
  YYSYMBOL_241_22 = 241,                   /* $@22  */
  YYSYMBOL_242_23 = 242,                   /* $@23  */
  YYSYMBOL_243_24 = 243,                   /* $@24  */
  YYSYMBOL_244_25 = 244,                   /* $@25  */
  YYSYMBOL_primary_value = 245,            /* primary_value  */
  YYSYMBOL_k_begin = 246,                  /* k_begin  */
  YYSYMBOL_k_if = 247,                     /* k_if  */
  YYSYMBOL_k_unless = 248,                 /* k_unless  */
  YYSYMBOL_k_while = 249,                  /* k_while  */
  YYSYMBOL_k_until = 250,                  /* k_until  */
  YYSYMBOL_k_case = 251,                   /* k_case  */
  YYSYMBOL_k_for = 252,                    /* k_for  */
  YYSYMBOL_k_class = 253,                  /* k_class  */
  YYSYMBOL_k_module = 254,                 /* k_module  */
  YYSYMBOL_k_def = 255,                    /* k_def  */
  YYSYMBOL_k_do = 256,                     /* k_do  */
  YYSYMBOL_k_do_block = 257,               /* k_do_block  */
  YYSYMBOL_k_rescue = 258,                 /* k_rescue  */
  YYSYMBOL_k_ensure = 259,                 /* k_ensure  */
  YYSYMBOL_k_when = 260,                   /* k_when  */
  YYSYMBOL_k_else = 261,                   /* k_else  */
  YYSYMBOL_k_elsif = 262,                  /* k_elsif  */
  YYSYMBOL_k_end = 263,                    /* k_end  */
  YYSYMBOL_k_return = 264,                 /* k_return  */
  YYSYMBOL_then = 265,                     /* then  */
  YYSYMBOL_do = 266,                       /* do  */
  YYSYMBOL_if_tail = 267,                  /* if_tail  */
  YYSYMBOL_opt_else = 268,                 /* opt_else  */
  YYSYMBOL_for_var = 269,                  /* for_var  */
  YYSYMBOL_f_marg = 270,                   /* f_marg  */
  YYSYMBOL_f_marg_list = 271,              /* f_marg_list  */
  YYSYMBOL_f_margs = 272,                  /* f_margs  */
  YYSYMBOL_f_rest_marg = 273,              /* f_rest_marg  */
  YYSYMBOL_f_any_kwrest = 274,             /* f_any_kwrest  */
  YYSYMBOL_f_eq = 275,                     /* f_eq  */
  YYSYMBOL_276_26 = 276,                   /* $@26  */
  YYSYMBOL_block_args_tail = 277,          /* block_args_tail  */
  YYSYMBOL_opt_block_args_tail = 278,      /* opt_block_args_tail  */
  YYSYMBOL_excessed_comma = 279,           /* excessed_comma  */
  YYSYMBOL_block_param = 280,              /* block_param  */
  YYSYMBOL_opt_block_param = 281,          /* opt_block_param  */
  YYSYMBOL_block_param_def = 282,          /* block_param_def  */
  YYSYMBOL_opt_bv_decl = 283,              /* opt_bv_decl  */
  YYSYMBOL_bv_decls = 284,                 /* bv_decls  */
  YYSYMBOL_bvar = 285,                     /* bvar  */
  YYSYMBOL_lambda = 286,                   /* lambda  */
  YYSYMBOL_287_27 = 287,                   /* @27  */
  YYSYMBOL_288_28 = 288,                   /* @28  */
  YYSYMBOL_289_29 = 289,                   /* @29  */
  YYSYMBOL_290_30 = 290,                   /* $@30  */
  YYSYMBOL_f_larglist = 291,               /* f_larglist  */
  YYSYMBOL_lambda_body = 292,              /* lambda_body  */
  YYSYMBOL_293_31 = 293,                   /* $@31  */
  YYSYMBOL_do_block = 294,                 /* do_block  */
  YYSYMBOL_block_call = 295,               /* block_call  */
  YYSYMBOL_method_call = 296,              /* method_call  */
  YYSYMBOL_brace_block = 297,              /* brace_block  */
  YYSYMBOL_brace_body = 298,               /* brace_body  */
  YYSYMBOL_299_32 = 299,                   /* @32  */
  YYSYMBOL_300_33 = 300,                   /* @33  */
  YYSYMBOL_301_34 = 301,                   /* @34  */
  YYSYMBOL_do_body = 302,                  /* do_body  */
  YYSYMBOL_303_35 = 303,                   /* @35  */
  YYSYMBOL_304_36 = 304,                   /* @36  */
  YYSYMBOL_305_37 = 305,                   /* @37  */
  YYSYMBOL_case_args = 306,                /* case_args  */
  YYSYMBOL_case_body = 307,                /* case_body  */
  YYSYMBOL_cases = 308,                    /* cases  */
  YYSYMBOL_p_case_body = 309,              /* p_case_body  */
  YYSYMBOL_310_38 = 310,                   /* @38  */
  YYSYMBOL_311_39 = 311,                   /* @39  */
  YYSYMBOL_312_40 = 312,                   /* $@40  */
  YYSYMBOL_p_cases = 313,                  /* p_cases  */
  YYSYMBOL_p_top_expr = 314,               /* p_top_expr  */
  YYSYMBOL_p_top_expr_body = 315,          /* p_top_expr_body  */
  YYSYMBOL_p_expr = 316,                   /* p_expr  */
  YYSYMBOL_p_as = 317,                     /* p_as  */
  YYSYMBOL_p_alt = 318,                    /* p_alt  */
  YYSYMBOL_p_lparen = 319,                 /* p_lparen  */
  YYSYMBOL_p_lbracket = 320,               /* p_lbracket  */
  YYSYMBOL_p_expr_basic = 321,             /* p_expr_basic  */
  YYSYMBOL_322_41 = 322,                   /* @41  */
  YYSYMBOL_323_42 = 323,                   /* @42  */
  YYSYMBOL_p_args = 324,                   /* p_args  */
  YYSYMBOL_p_args_head = 325,              /* p_args_head  */
  YYSYMBOL_p_args_tail = 326,              /* p_args_tail  */
  YYSYMBOL_p_find = 327,                   /* p_find  */
  YYSYMBOL_p_rest = 328,                   /* p_rest  */
  YYSYMBOL_p_args_post = 329,              /* p_args_post  */
  YYSYMBOL_p_arg = 330,                    /* p_arg  */
  YYSYMBOL_p_kwargs = 331,                 /* p_kwargs  */
  YYSYMBOL_p_kwarg = 332,                  /* p_kwarg  */
  YYSYMBOL_p_kw = 333,                     /* p_kw  */
  YYSYMBOL_p_kw_label = 334,               /* p_kw_label  */
  YYSYMBOL_p_kwrest = 335,                 /* p_kwrest  */
  YYSYMBOL_p_kwnorest = 336,               /* p_kwnorest  */
  YYSYMBOL_p_any_kwrest = 337,             /* p_any_kwrest  */
  YYSYMBOL_p_value = 338,                  /* p_value  */
  YYSYMBOL_p_primitive = 339,              /* p_primitive  */
  YYSYMBOL_p_variable = 340,               /* p_variable  */
  YYSYMBOL_p_var_ref = 341,                /* p_var_ref  */
  YYSYMBOL_p_expr_ref = 342,               /* p_expr_ref  */
  YYSYMBOL_p_const = 343,                  /* p_const  */
  YYSYMBOL_opt_rescue = 344,               /* opt_rescue  */
  YYSYMBOL_exc_list = 345,                 /* exc_list  */
  YYSYMBOL_exc_var = 346,                  /* exc_var  */
  YYSYMBOL_opt_ensure = 347,               /* opt_ensure  */
  YYSYMBOL_literal = 348,                  /* literal  */
  YYSYMBOL_strings = 349,                  /* strings  */
  YYSYMBOL_string = 350,                   /* string  */
  YYSYMBOL_string1 = 351,                  /* string1  */
  YYSYMBOL_xstring = 352,                  /* xstring  */
  YYSYMBOL_regexp = 353,                   /* regexp  */
  YYSYMBOL_words = 354,                    /* words  */
  YYSYMBOL_word_list = 355,                /* word_list  */
  YYSYMBOL_word = 356,                     /* word  */
  YYSYMBOL_symbols = 357,                  /* symbols  */
  YYSYMBOL_symbol_list = 358,              /* symbol_list  */
  YYSYMBOL_qwords = 359,                   /* qwords  */
  YYSYMBOL_qsymbols = 360,                 /* qsymbols  */
  YYSYMBOL_qword_list = 361,               /* qword_list  */
  YYSYMBOL_qsym_list = 362,                /* qsym_list  */
  YYSYMBOL_string_contents = 363,          /* string_contents  */
  YYSYMBOL_xstring_contents = 364,         /* xstring_contents  */
  YYSYMBOL_regexp_contents = 365,          /* regexp_contents  */
  YYSYMBOL_string_content = 366,           /* string_content  */
  YYSYMBOL_367_43 = 367,                   /* @43  */
  YYSYMBOL_368_44 = 368,                   /* $@44  */
  YYSYMBOL_369_45 = 369,                   /* @45  */
  YYSYMBOL_370_46 = 370,                   /* @46  */
  YYSYMBOL_371_47 = 371,                   /* @47  */
  YYSYMBOL_372_48 = 372,                   /* @48  */
  YYSYMBOL_string_dvar = 373,              /* string_dvar  */
  YYSYMBOL_symbol = 374,                   /* symbol  */
  YYSYMBOL_ssym = 375,                     /* ssym  */
  YYSYMBOL_sym = 376,                      /* sym  */
  YYSYMBOL_dsym = 377,                     /* dsym  */
  YYSYMBOL_numeric = 378,                  /* numeric  */
  YYSYMBOL_simple_numeric = 379,           /* simple_numeric  */
  YYSYMBOL_nonlocal_var = 380,             /* nonlocal_var  */
  YYSYMBOL_user_variable = 381,            /* user_variable  */
  YYSYMBOL_keyword_variable = 382,         /* keyword_variable  */
  YYSYMBOL_var_ref = 383,                  /* var_ref  */
  YYSYMBOL_var_lhs = 384,                  /* var_lhs  */
  YYSYMBOL_backref = 385,                  /* backref  */
  YYSYMBOL_superclass = 386,               /* superclass  */
  YYSYMBOL_387_49 = 387,                   /* $@49  */
  YYSYMBOL_f_opt_paren_args = 388,         /* f_opt_paren_args  */
  YYSYMBOL_f_paren_args = 389,             /* f_paren_args  */
  YYSYMBOL_f_arglist = 390,                /* f_arglist  */
  YYSYMBOL_391_50 = 391,                   /* @50  */
  YYSYMBOL_args_tail = 392,                /* args_tail  */
  YYSYMBOL_opt_args_tail = 393,            /* opt_args_tail  */
  YYSYMBOL_f_args = 394,                   /* f_args  */
  YYSYMBOL_args_forward = 395,             /* args_forward  */
  YYSYMBOL_f_bad_arg = 396,                /* f_bad_arg  */
  YYSYMBOL_f_norm_arg = 397,               /* f_norm_arg  */
  YYSYMBOL_f_arg_asgn = 398,               /* f_arg_asgn  */
  YYSYMBOL_f_arg_item = 399,               /* f_arg_item  */
  YYSYMBOL_f_arg = 400,                    /* f_arg  */
  YYSYMBOL_f_label = 401,                  /* f_label  */
  YYSYMBOL_f_kw = 402,                     /* f_kw  */
  YYSYMBOL_f_block_kw = 403,               /* f_block_kw  */
  YYSYMBOL_f_block_kwarg = 404,            /* f_block_kwarg  */
  YYSYMBOL_f_kwarg = 405,                  /* f_kwarg  */
  YYSYMBOL_kwrest_mark = 406,              /* kwrest_mark  */
  YYSYMBOL_f_no_kwarg = 407,               /* f_no_kwarg  */
  YYSYMBOL_f_kwrest = 408,                 /* f_kwrest  */
  YYSYMBOL_f_opt = 409,                    /* f_opt  */
  YYSYMBOL_f_block_opt = 410,              /* f_block_opt  */
  YYSYMBOL_f_block_optarg = 411,           /* f_block_optarg  */
  YYSYMBOL_f_optarg = 412,                 /* f_optarg  */
  YYSYMBOL_restarg_mark = 413,             /* restarg_mark  */
  YYSYMBOL_f_rest_arg = 414,               /* f_rest_arg  */
  YYSYMBOL_blkarg_mark = 415,              /* blkarg_mark  */
  YYSYMBOL_f_block_arg = 416,              /* f_block_arg  */
  YYSYMBOL_opt_f_block_arg = 417,          /* opt_f_block_arg  */
  YYSYMBOL_singleton = 418,                /* singleton  */
  YYSYMBOL_419_51 = 419,                   /* $@51  */
  YYSYMBOL_assoc_list = 420,               /* assoc_list  */
  YYSYMBOL_assocs = 421,                   /* assocs  */
  YYSYMBOL_assoc = 422,                    /* assoc  */
  YYSYMBOL_operation = 423,                /* operation  */
  YYSYMBOL_operation2 = 424,               /* operation2  */
  YYSYMBOL_operation3 = 425,               /* operation3  */
  YYSYMBOL_dot_or_colon = 426,             /* dot_or_colon  */
  YYSYMBOL_call_op = 427,                  /* call_op  */
  YYSYMBOL_call_op2 = 428,                 /* call_op2  */
  YYSYMBOL_opt_terms = 429,                /* opt_terms  */
  YYSYMBOL_opt_nl = 430,                   /* opt_nl  */
  YYSYMBOL_rparen = 431,                   /* rparen  */
  YYSYMBOL_rbracket = 432,                 /* rbracket  */
  YYSYMBOL_rbrace = 433,                   /* rbrace  */
  YYSYMBOL_trailer = 434,                  /* trailer  */
  YYSYMBOL_term = 435,                     /* term  */
  YYSYMBOL_terms = 436,                    /* terms  */
  YYSYMBOL_none = 437                      /* none  */
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
#define YYLAST   15194

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  163
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  275
/* YYNRULES -- Number of rules.  */
#define YYNRULES  782
/* YYNSTATES -- Number of states.  */
#define YYNSTATES  1304

/* YYMAXUTOK -- Last valid token kind.  */
#define YYMAXUTOK   362


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
       0,     2,     2,     2,     2,     2,     2,     2,     2,    72,
     162,    75,    73,    74,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,   161,   149,     2,     2,     2,   147,   142,     2,
     157,   158,   145,   143,   155,   144,    69,   146,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,   137,   160,
     139,   135,   138,   136,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,   154,    70,   159,   141,     2,   156,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,   152,   140,   153,   150,     2,    89,    90,
      91,    92,    76,    77,    78,    79,    95,    96,    84,    83,
      80,    81,    82,    87,    88,    93,    94,    98,    85,    86,
      97,     2,     2,     2,     2,     2,     2,     2,     2,     2,
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
      65,    66,    67,    68,    71,    99,   100,   101,   102,   103,
     104,   105,   106,   107,   108,   109,   110,   111,   112,   113,
     114,   115,   116,   117,   118,   119,   120,   121,   122,   123,
     124,   125,   126,   127,   128,   129,   130,   131,   132,   133,
     134,   148,   151
};

#if YYDEBUG
/* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
static const yytype_int16 yyrline[] =
{
       0,  1580,  1580,  1580,  1606,  1612,  1619,  1626,  1635,  1636,
    1642,  1655,  1653,  1664,  1675,  1681,  1688,  1695,  1704,  1709,
    1708,  1718,  1718,  1725,  1732,  1742,  1751,  1758,  1766,  1774,
    1786,  1798,  1808,  1822,  1823,  1831,  1838,  1846,  1853,  1854,
    1862,  1869,  1876,  1884,  1891,  1898,  1906,  1913,  1924,  1936,
    1949,  1963,  1973,  1978,  1987,  1990,  1991,  1995,  1999,  2003,
    2008,  2016,  2007,  2030,  2038,  2029,  2051,  2054,  2071,  2081,
    2080,  2099,  2104,  2112,  2112,  2112,  2118,  2119,  2122,  2123,
    2132,  2142,  2152,  2161,  2172,  2179,  2186,  2193,  2200,  2208,
    2216,  2223,  2230,  2239,  2240,  2249,  2250,  2259,  2266,  2273,
    2280,  2287,  2294,  2301,  2308,  2315,  2322,  2331,  2332,  2341,
    2348,  2357,  2364,  2373,  2380,  2387,  2394,  2404,  2411,  2421,
    2428,  2435,  2445,  2452,  2459,  2466,  2473,  2480,  2487,  2494,
    2501,  2511,  2519,  2522,  2529,  2536,  2545,  2546,  2547,  2548,
    2553,  2556,  2563,  2566,  2573,  2573,  2583,  2584,  2585,  2586,
    2587,  2588,  2589,  2590,  2591,  2592,  2593,  2594,  2595,  2596,
    2597,  2598,  2599,  2600,  2601,  2602,  2603,  2604,  2605,  2606,
    2607,  2608,  2609,  2610,  2611,  2612,  2615,  2615,  2615,  2616,
    2616,  2617,  2617,  2617,  2618,  2618,  2618,  2618,  2619,  2619,
    2619,  2619,  2620,  2620,  2620,  2621,  2621,  2621,  2621,  2622,
    2622,  2622,  2622,  2623,  2623,  2623,  2623,  2624,  2624,  2624,
    2624,  2625,  2625,  2625,  2625,  2626,  2626,  2629,  2636,  2643,
    2650,  2657,  2664,  2671,  2679,  2687,  2695,  2704,  2713,  2721,
    2729,  2737,  2745,  2749,  2753,  2757,  2761,  2765,  2769,  2773,
    2777,  2781,  2785,  2789,  2793,  2797,  2798,  2802,  2806,  2810,
    2814,  2818,  2822,  2826,  2830,  2834,  2838,  2842,  2842,  2847,
    2856,  2867,  2879,  2892,  2906,  2912,  2913,  2914,  2915,  2918,
    2922,  2929,  2935,  2942,  2943,  2947,  2954,  2963,  2968,  2978,
    2985,  2997,  3011,  3012,  3015,  3016,  3017,  3021,  3028,  3037,
    3045,  3052,  3060,  3068,  3072,  3072,  3109,  3116,  3128,  3132,
    3139,  3146,  3153,  3164,  3171,  3178,  3192,  3193,  3197,  3204,
    3211,  3220,  3221,  3222,  3223,  3224,  3225,  3226,  3227,  3228,
    3229,  3230,  3238,  3237,  3252,  3252,  3259,  3259,  3267,  3275,
    3282,  3289,  3296,  3304,  3311,  3318,  3325,  3332,  3332,  3337,
    3341,  3345,  3352,  3353,  3361,  3362,  3373,  3384,  3394,  3405,
    3404,  3421,  3420,  3435,  3444,  3487,  3486,  3510,  3509,  3532,
    3531,  3556,  3554,  3573,  3571,  3590,  3597,  3604,  3611,  3620,
    3627,  3636,  3656,  3665,  3674,  3683,  3692,  3701,  3711,  3721,
    3728,  3738,  3747,  3753,  3759,  3765,  3780,  3787,  3794,  3800,
    3807,  3808,  3809,  3812,  3813,  3816,  3817,  3829,  3830,  3839,
    3840,  3843,  3851,  3860,  3867,  3876,  3883,  3890,  3897,  3904,
    3913,  3921,  3930,  3931,  3934,  3934,  3936,  3940,  3944,  3948,
    3954,  3959,  3964,  3974,  3978,  3982,  3986,  3990,  3994,  3999,
    4003,  4007,  4011,  4015,  4019,  4023,  4027,  4031,  4037,  4038,
    4044,  4054,  4067,  4071,  4080,  4082,  4086,  4091,  4098,  4104,
    4108,  4112,  4097,  4137,  4146,  4157,  4163,  4162,  4174,  4184,
    4198,  4205,  4212,  4221,  4230,  4238,  4246,  4253,  4261,  4269,
    4276,  4283,  4296,  4304,  4314,  4315,  4319,  4314,  4336,  4337,
    4341,  4336,  4360,  4368,  4375,  4383,  4392,  4404,  4405,  4409,
    4416,  4420,  4408,  4435,  4436,  4439,  4440,  4448,  4458,  4459,
    4464,  4472,  4476,  4480,  4486,  4489,  4498,  4501,  4508,  4511,
    4512,  4514,  4515,  4516,  4525,  4534,  4543,  4548,  4557,  4566,
    4575,  4580,  4584,  4588,  4594,  4593,  4605,  4610,  4610,  4617,
    4626,  4630,  4639,  4643,  4647,  4650,  4654,  4663,  4667,  4673,
    4680,  4684,  4690,  4691,  4700,  4709,  4713,  4717,  4721,  4727,
    4729,  4738,  4746,  4760,  4761,  4784,  4788,  4794,  4800,  4801,
    4804,  4805,  4814,  4823,  4831,  4839,  4840,  4841,  4842,  4850,
    4860,  4861,  4862,  4863,  4864,  4865,  4866,  4867,  4868,  4875,
    4878,  4888,  4899,  4908,  4917,  4924,  4931,  4940,  4961,  4964,
    4971,  4978,  4981,  4985,  4988,  4995,  4998,  4999,  5002,  5019,
    5020,  5021,  5030,  5040,  5049,  5055,  5065,  5071,  5080,  5082,
    5091,  5101,  5107,  5116,  5125,  5135,  5141,  5151,  5157,  5167,
    5177,  5196,  5202,  5212,  5222,  5263,  5266,  5265,  5282,  5286,
    5291,  5295,  5299,  5281,  5320,  5327,  5334,  5341,  5344,  5345,
    5348,  5358,  5359,  5362,  5372,  5373,  5383,  5384,  5385,  5386,
    5389,  5390,  5391,  5394,  5395,  5396,  5399,  5400,  5401,  5402,
    5403,  5404,  5405,  5408,  5421,  5430,  5437,  5446,  5447,  5451,
    5450,  5460,  5468,  5469,  5477,  5489,  5490,  5490,  5506,  5510,
    5514,  5518,  5522,  5532,  5537,  5542,  5546,  5550,  5554,  5558,
    5562,  5566,  5570,  5574,  5578,  5582,  5586,  5590,  5594,  5599,
    5605,  5618,  5627,  5636,  5645,  5656,  5657,  5665,  5674,  5682,
    5703,  5705,  5718,  5728,  5737,  5748,  5756,  5766,  5773,  5783,
    5790,  5799,  5800,  5803,  5811,  5819,  5829,  5840,  5851,  5858,
    5867,  5874,  5883,  5884,  5887,  5895,  5905,  5906,  5909,  5917,
    5927,  5931,  5937,  5942,  5942,  5966,  5967,  5976,  5978,  6001,
    6012,  6019,  6028,  6036,  6053,  6067,  6068,  6069,  6072,  6073,
    6076,  6077,  6078,  6081,  6082,  6085,  6086,  6089,  6090,  6093,
    6094,  6097,  6098,  6101,  6104,  6107,  6110,  6111,  6114,  6115,
    6122,  6123,  6127
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
  "\"literal content\"", "tREGEXP_END", "\"dummy end\"", "'.'",
  "\"backslash\"", "\"escaped space\"", "\"escaped horizontal tab\"",
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
  "tSTRING_DVAR", "tLAMBEG", "tLABEL_END", "tIGNORED_NL", "tCOMMENT",
  "tEMBDOC_BEG", "tEMBDOC", "tEMBDOC_END", "tHEREDOC_BEG", "tHEREDOC_END",
  "k__END__", "tLOWEST", "'='", "'?'", "':'", "'>'", "'<'", "'|'", "'^'",
  "'&'", "'+'", "'-'", "'*'", "'/'", "'%'", "tUMINUS_NUM", "'!'", "'~'",
  "tLAST_TOKEN", "'{'", "'}'", "'['", "','", "'`'", "'('", "')'", "']'",
  "';'", "' '", "'\\n'", "$accept", "program", "$@1", "top_compstmt",
  "top_stmts", "top_stmt", "begin_block", "bodystmt", "$@2", "compstmt",
  "stmts", "stmt_or_begin", "$@3", "stmt", "$@4", "command_asgn",
  "command_rhs", "expr", "@5", "@6", "@7", "@8", "def_name", "defn_head",
  "defs_head", "$@9", "expr_value", "expr_value_do", "$@10", "$@11",
  "command_call", "block_command", "cmd_brace_block", "fcall", "command",
  "mlhs", "mlhs_inner", "mlhs_basic", "mlhs_item", "mlhs_head",
  "mlhs_post", "mlhs_node", "lhs", "cname", "cpath", "fname", "fitem",
  "undef_list", "$@12", "op", "reswords", "arg", "$@13", "relop",
  "rel_expr", "lex_ctxt", "arg_value", "aref_args", "arg_rhs",
  "paren_args", "opt_paren_args", "opt_call_args", "call_args",
  "command_args", "$@14", "block_arg", "opt_block_arg", "args", "mrhs_arg",
  "mrhs", "primary", "$@15", "$@16", "$@17", "$@18", "@19", "@20", "$@21",
  "$@22", "$@23", "$@24", "$@25", "primary_value", "k_begin", "k_if",
  "k_unless", "k_while", "k_until", "k_case", "k_for", "k_class",
  "k_module", "k_def", "k_do", "k_do_block", "k_rescue", "k_ensure",
  "k_when", "k_else", "k_elsif", "k_end", "k_return", "then", "do",
  "if_tail", "opt_else", "for_var", "f_marg", "f_marg_list", "f_margs",
  "f_rest_marg", "f_any_kwrest", "f_eq", "$@26", "block_args_tail",
  "opt_block_args_tail", "excessed_comma", "block_param",
  "opt_block_param", "block_param_def", "opt_bv_decl", "bv_decls", "bvar",
  "lambda", "@27", "@28", "@29", "$@30", "f_larglist", "lambda_body",
  "$@31", "do_block", "block_call", "method_call", "brace_block",
  "brace_body", "@32", "@33", "@34", "do_body", "@35", "@36", "@37",
  "case_args", "case_body", "cases", "p_case_body", "@38", "@39", "$@40",
  "p_cases", "p_top_expr", "p_top_expr_body", "p_expr", "p_as", "p_alt",
  "p_lparen", "p_lbracket", "p_expr_basic", "@41", "@42", "p_args",
  "p_args_head", "p_args_tail", "p_find", "p_rest", "p_args_post", "p_arg",
  "p_kwargs", "p_kwarg", "p_kw", "p_kw_label", "p_kwrest", "p_kwnorest",
  "p_any_kwrest", "p_value", "p_primitive", "p_variable", "p_var_ref",
  "p_expr_ref", "p_const", "opt_rescue", "exc_list", "exc_var",
  "opt_ensure", "literal", "strings", "string", "string1", "xstring",
  "regexp", "words", "word_list", "word", "symbols", "symbol_list",
  "qwords", "qsymbols", "qword_list", "qsym_list", "string_contents",
  "xstring_contents", "regexp_contents", "string_content", "@43", "$@44",
  "@45", "@46", "@47", "@48", "string_dvar", "symbol", "ssym", "sym",
  "dsym", "numeric", "simple_numeric", "nonlocal_var", "user_variable",
  "keyword_variable", "var_ref", "var_lhs", "backref", "superclass",
  "$@49", "f_opt_paren_args", "f_paren_args", "f_arglist", "@50",
  "args_tail", "opt_args_tail", "f_args", "args_forward", "f_bad_arg",
  "f_norm_arg", "f_arg_asgn", "f_arg_item", "f_arg", "f_label", "f_kw",
  "f_block_kw", "f_block_kwarg", "f_kwarg", "kwrest_mark", "f_no_kwarg",
  "f_kwrest", "f_opt", "f_block_opt", "f_block_optarg", "f_optarg",
  "restarg_mark", "f_rest_arg", "blkarg_mark", "f_block_arg",
  "opt_f_block_arg", "singleton", "$@51", "assoc_list", "assocs", "assoc",
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

#define YYPACT_NINF (-1065)

#define yypact_value_is_default(Yyn) \
  ((Yyn) == YYPACT_NINF)

#define YYTABLE_NINF (-783)

#define yytable_value_is_error(Yyn) \
  ((Yyn) == YYTABLE_NINF)

/* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
   STATE-NUM.  */
static const yytype_int16 yypact[] =
{
   -1065,   144,  4385, -1065, -1065, -1065, -1065, -1065,  9742, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, 10562, 10562, -1065, -1065,
   -1065,  6084,  5616, -1065, -1065, -1065, -1065,    32,  9588,    11,
      92,   197, -1065, -1065, -1065,  4824,  5772, -1065, -1065,  4992,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, 12135, 12135,
   12135, 12135,   351,  7722,  8484, 11046, 11409, 10044, -1065,  9434,
   -1065, -1065, -1065,   272,   284,   359,   362,  1229, 12256, 12135,
   -1065,   340, -1065,  1436, -1065,   551,    20,    20, -1065, -1065,
     194,   432,   406, -1065,   308, 12498, -1065,   442,  2224,   819,
     622,   676, -1065, 10441, 10441, -1065, -1065,  8605, 12617, 12736,
   12855,  9279, 10562, -1065,   582,   249, -1065, -1065,   373, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065,    37,   299, -1065,   509,   342, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065,   507, -1065, -1065, -1065,   528,
   12135,   637,  7882, 12135, 12135, 12135, -1065, 12135,    20,    20,
   -1065,   563,  4625,   603, -1065, -1065,   552,   754,    80,   302,
     615,   306,   569, -1065, -1065,  9037, -1065, 10562, 10683, -1065,
   -1065,  9158, -1065, 12377,   751, -1065,   575,  8042, -1065,  8202,
   -1065, -1065,   589,   602,   194, -1065,   758, -1065,   687,  4962,
    4962,   389, -1065,  7722,   612,   340, -1065,  1436,    11,   643,
   -1065,  1436,    11,   639,   601,   609, -1065,   603,   638,   609,
   -1065,    11,   744,  1229, 12974,   646,   646,   664, -1065,   774,
     858,   866,   871, -1065, -1065,   855, -1065, -1065,  1196,  1209,
     450, -1065, -1065, -1065, -1065,   753, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065,  8727, 10441, 10441, 10441, 10441, 10320, 12377,
   12377,  2298,   705,   713, -1065,  2298, -1065,   718, -1065, -1065,
   -1065, -1065,   715, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
    7722, 10182,   706, -1065, -1065, 12135, 12135, 12135, 12135, 12135,
   -1065, -1065, 12135, 12135, 12135, 12135, 12135, 12135, 12135, 12135,
   -1065, 12135, -1065, -1065, 12135, 12135, 12135, 12135, 12135, 12135,
   12135, 12135, 12135, 12135, -1065, -1065, 13553, 10562, 13652,  6868,
   -1065,   551,   185,   185,  7600, 10441,  7600,   340, -1065,   703,
     814, -1065, -1065,   935,   874,    77,   128,   132,   851,   883,
   12377,   119, -1065,   741,  1039, -1065, -1065, -1065, -1065,    83,
     325,   529,   534,   561,   568,   632,   633,   670, -1065, -1065,
   -1065, -1065,   679, -1065, -1065, -1065, 15038, -1065, -1065, -1065,
   -1065, -1065, -1065,   390, -1065, -1065, -1065,  1072,   748,   761,
   -1065, 12135, 10804, -1065, -1065, 13751, 10562, 13850, -1065, -1065,
   11167, -1065,    11,   733, -1065, -1065, 12135,    11, -1065,   750,
      11,   776, -1065,   352, -1065, -1065, -1065, -1065, -1065,  9742,
   -1065, 12135,   757,   786, 13751, 13850, -1065,    92,    11, -1065,
   -1065,  8885,   789,    11, -1065, -1065, 11288, -1065, -1065, 11409,
   -1065, -1065, -1065,   575,  1071, -1065, -1065,   805, -1065, 12974,
   13949, 10562, 14048, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065,  1215,    85,  1233,    88, 12135, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065,  1404, -1065, -1065, -1065,
   -1065, -1065,   817, -1065, -1065,    11, -1065, -1065, -1065,   806,
   -1065,   831, 12135, -1065,   833,   134, -1065, -1065, -1065,   834,
     938,   838,   942, -1065, 12256,  6868,   340, 12256,  6868,   843,
   -1065, -1065, -1065,   138, -1065,   138, 11530, -1065,    11, 12974,
     844, -1065, 11530, -1065,   687,  3311,  3311,  3311,  3311,  3382,
    2366,  3311,  3311,  4962,  4962,   771,   771, -1065,  4036,  1358,
    1358,  1227,   266,   266,   687,   687,   687,  1300,  1300,  6240,
    5148,  6552,  5304, -1065, -1065,   602, -1065,    11,   852,   412,
   -1065,   539, -1065, -1065,  5928,   138,   990, -1065,  6990,   993,
    7356,   138,   109,   138,   984,  1004,   175, 14147, 10562, 14246,
   -1065,   551, -1065,  1071, -1065, -1065, -1065, 14345, 10562, 14444,
    6868, 12377, -1065, -1065, -1065, -1065, -1065,  3687, 12256, 12256,
    9742, 12135, 12135, 12135, 12135, -1065, 12135,   603, -1065,   569,
    4540,  5460,    11,   451,   456, 12135, 12135, -1065, -1065, -1065,
   -1065, 10925, -1065, 11167, -1065, -1065, 12377,  4625, -1065, -1065,
     602,   602, 12135, -1065,   315, -1065, -1065,   609, 12974,   805,
      76,   660,    11,   300,   391,  2980, -1065,  1189, -1065,   526,
   -1065,   862, -1065, -1065,   535,   863, -1065,   687,  1404,  1062,
   -1065,   875,    11,   881, -1065,    40, -1065, -1065, -1065, 12135,
     878,  2298, -1065, -1065,   339, -1065, -1065, -1065,  2298, -1065,
   -1065,  1709, -1065, -1065,   982,  3514,   138, -1065,   994,  3654,
     138, -1065, -1065,   888, -1065, -1065, -1065, -1065, -1065, 12135,
   -1065,   889,   892,   999, -1065, -1065,   805, 12974, -1065, -1065,
    1005,   913,  3176, -1065, -1065, -1065,  1075,   461, 13379, 13379,
     914, -1065, -1065, -1065,   715,   896,   599, 10804, -1065, -1065,
   -1065, -1065,   715, -1065, -1065, -1065, 11651,   662, -1065,   723,
   -1065,  1047, -1065, -1065, -1065, -1065, -1065, -1065,  1004,   138,
   -1065, 11772,   138,    99,   139,    11,   179,   204,  7600,   340,
   10441,  6868,   996,   660, -1065,    11,   138,   352,  9896,   249,
     432, -1065,  3731, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065,   480, -1065, -1065,    11,   911,   352, -1065, -1065,
   -1065,   471,  2298, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065,    11, -1065,  1404, -1065,
    1333, -1065, -1065, -1065, -1065, -1065,   912,   919, -1065,  1016,
     817,   924, -1065,   932, -1065,   924, 12135, 12135, -1065, 12135,
   12135, -1065,   951, -1065,   951,   937, 11893, 10320,   805, 10320,
   -1065, 12135, 14543, 10562, 14642, -1065, -1065, -1065,  1928,  1928,
     565, -1065, 13286,   314,  1043, -1065,  1002, -1065, -1065,    28,
   -1065,   960, -1065, -1065, -1065,   969, -1065,   970, -1065, 13565,
   -1065, -1065, -1065, -1065,   756, -1065, -1065, -1065,   328, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,   136, -1065,
   12135, 12256, 12256, -1065, -1065, -1065, 12256, 12256, -1065,   889,
   -1065,  1026, -1065, -1065, -1065,  7600, -1065, -1065, -1065, -1065,
    7600, 10441,   138, -1065, -1065,   138, -1065, -1065,   138, -1065,
   12135, -1065,   146, -1065,   214,   138,  6868,   340,   138, -1065,
   -1065, -1065, -1065, -1065, -1065, 12135, 12135, -1065, 12135, 12135,
   -1065, 11167, -1065,    11,    75, -1065, -1065, -1065,   973,   975,
    2298, -1065,  1709, -1065, -1065,  1709, -1065,  1709, -1065, -1065,
    4625,  4625,  4625,  4625,  2064,  8202, -1065, -1065,  6868, 12135,
     977, -1065, -1065, 12256,  4625,  6396,  6708,    11,   485,   490,
   -1065, -1065, -1065, -1065, 13565,    41,    11, 13472, -1065,    11,
     978, -1065,   394,   953, -1065, -1065,  1162, -1065, 10441, -1065,
    1082, 13472, 13565, 13565,   394,  1038,  1928,  1928,   565,    60,
     696, 13379, 13379, -1065,  4625, -1065, -1065, -1065, -1065, 12256,
   -1065, -1065, -1065, -1065, 13093,   185, -1065, -1065,  7478, -1065,
     185, -1065, -1065, 13379, -1065, -1065, 12014,  7112, -1065,   138,
   -1065, -1065, 12135,   983,   986, -1065,  8202, -1065, -1065,  1333,
    1333,   924,   987,   924,   924,   817, -1065,    11,  1007,   806,
    1008, 13212, -1065,  1010, -1065,  1012,  1020, -1065, -1065, -1065,
    1022,   499,    29, -1065,  1038,  1023,  1024, -1065, -1065, -1065,
      11, -1065, -1065,    11, -1065, -1065,  1028, -1065,  1033, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065,    11,    11,    11,
      11,    11,    11, -1065, -1065,  1093, -1065,  1102,   210,   224,
     259,  6868,  1142,  6990,   185,   884, 12135, -1065,   768, -1065,
   -1065,  1325,  6868,  1006,  8362,   975, -1065,  1709, -1065, -1065,
   -1065, -1065,  1017, -1065, 13212,  1252, -1065, -1065,  1125,  1117,
     339, -1065,  1252, -1065,   755, -1065, -1065, 13565, -1065,   472,
   -1065, -1065, 13565, 13472, -1065, -1065, -1065, -1065, -1065, -1065,
     291, 14741, 10562, 14840,   990, -1065,   723, -1065, 10441, 10441,
   -1065, -1065, -1065, -1065, -1065,   627, -1065, -1065,   138, -1065,
    1069,   924, -1065,  1117, -1065,  1052,  1054, -1065, 14939, -1065,
     817,  1055, -1065,  1056,  1055,  1058,  1058, -1065, -1065,   105,
     166,    11,   318,   367, -1065, -1065,  7234, -1065, -1065,  1325,
   -1065, -1065, -1065, -1065,  1252, -1065,   755, -1065,  1059,  1060,
   -1065,   755, -1065,   755, -1065, -1065, 13565,   374,   725, -1065,
    1055,  1063,  1055,  1055, -1065, -1065, -1065, -1065,   755, -1065,
   -1065, -1065,  1055, -1065
};

/* YYDEFACT[STATE-NUM] -- Default reduction number in state STATE-NUM.
   Performed when YYTABLE does not specify something else to do.  Zero
   means the default is an error.  */
static const yytype_int16 yydefact[] =
{
       2,     0,     0,     1,    39,   377,   378,   379,     0,   370,
     371,   372,   375,   373,   374,   376,   365,   366,   367,   368,
     389,   294,   294,   657,   656,   658,   659,   771,     0,   771,
       0,     0,   661,   660,   662,   755,   757,   651,   650,   756,
     652,   646,   647,   648,   649,   599,   667,   668,     0,     0,
       0,     0,     0,     0,     0,   782,   782,   105,   448,   619,
     619,   621,   623,     0,     0,     0,     0,     0,     0,     0,
       3,   769,     6,     8,    33,    38,   676,   676,    55,    77,
     294,    76,     0,    93,     0,    97,   107,     0,    66,   245,
     264,     0,   322,     0,     0,    73,    73,     0,     0,     0,
       0,     0,   333,   344,    78,   342,   311,   312,   598,   600,
     313,   314,   315,   317,   316,   318,   597,   638,   639,   596,
     644,   655,   663,   664,   319,     0,   320,    81,     5,   186,
     197,   187,   210,   183,   203,   193,   192,   213,   214,   208,
     191,   190,   185,   211,   215,   216,   195,   184,   198,   202,
     204,   196,   189,   205,   212,   207,   206,   199,   209,   194,
     182,   201,   200,   181,   188,   179,   180,   176,   177,   178,
     136,   138,   137,   171,   172,   167,   149,   150,   151,   158,
     155,   157,   152,   153,   173,   174,   159,   160,   164,   168,
     154,   156,   146,   147,   148,   161,   162,   163,   165,   166,
     169,   170,   175,   141,   143,    26,   139,   140,   142,     0,
     751,     0,     0,   302,   754,   297,   619,     0,   676,   676,
     289,     0,   272,   300,    91,   293,   782,     0,   663,   664,
       0,   320,   782,   747,    92,   771,    89,     0,   782,   469,
      88,   771,   772,     0,     0,    21,   257,     0,     9,     0,
     365,   366,   336,   470,     0,   239,     0,   333,   240,   230,
     231,   330,    19,     0,     0,   769,    16,    18,   771,    95,
      15,   326,   771,     0,   771,   771,   273,     0,     0,   771,
     745,   771,     0,     0,     0,   676,   676,   103,   369,     0,
     113,   114,   121,   449,   641,     0,   640,   642,     0,     0,
       0,   606,   615,   611,   617,   645,    59,   251,   252,   778,
     779,     4,   780,     0,     0,     0,     0,     0,     0,     0,
       0,   699,     0,   675,   361,   699,   673,     0,   363,   380,
     474,   463,    82,   478,   341,   381,   478,   459,   782,   109,
       0,   101,    98,   782,    63,     0,     0,     0,     0,     0,
     267,   268,     0,     0,     0,     0,   228,   229,     0,     0,
      60,     0,   265,   266,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   765,   766,     0,   782,     0,     0,
      72,    71,     0,     0,     0,     0,     0,   769,   351,   770,
       0,   400,   399,     0,     0,   663,   664,   320,   131,   132,
       0,     0,   134,   671,     0,   663,   664,   320,   359,   206,
     199,   209,   194,   176,   177,   178,   136,   137,   743,    68,
      67,   742,     0,    90,   768,   767,     0,   343,   601,   782,
     782,   144,   750,   330,   301,   753,   296,     0,     0,     0,
     782,     0,     0,   290,   299,     0,   782,     0,   782,   782,
       0,   291,   771,     0,   335,   295,   700,   771,   285,   782,
     771,   782,   284,   771,   340,    58,    23,    25,    24,     0,
     337,     0,     0,     0,     0,     0,   782,     0,   771,   328,
      14,     0,    94,   771,   325,   331,   777,   776,   274,   777,
     276,   332,   746,     0,   120,   645,   111,   106,   675,     0,
       0,   782,     0,   450,   625,   643,   628,   626,   620,   602,
     603,   622,   604,   624,     0,     0,     0,     0,     0,   781,
       7,    27,    28,    29,    30,    31,    56,    57,   706,   703,
     702,   701,   704,   712,   721,   700,     0,   733,   722,   737,
     736,   732,   782,   723,   698,   771,   682,   705,   707,   708,
     710,   684,   714,   719,   782,   725,   413,   412,   730,   684,
     735,   684,   739,   681,     0,     0,     0,     0,     0,     0,
     475,   474,    83,     0,   479,     0,     0,   271,   771,     0,
      99,   110,     0,    64,   237,   244,   246,   247,   248,   255,
     256,   249,   250,   226,   227,   253,   254,    61,   771,   241,
     242,   243,   232,   233,   234,   235,   236,   269,   270,   755,
     757,   756,   759,   468,   758,   294,   466,   771,   782,   755,
     757,   756,   759,   467,   294,     0,   782,   391,     0,   390,
       0,     0,     0,     0,   349,     0,   330,     0,   782,     0,
      73,   357,   131,   132,   133,   669,   355,     0,   782,     0,
       0,     0,   763,   764,    69,   755,   756,   294,     0,     0,
       0,     0,     0,     0,     0,   749,   305,   303,   298,   782,
     755,   756,   771,   755,   756,     0,     0,   748,   334,   773,
     279,   286,   281,   288,   339,    22,     0,   258,    10,    32,
       0,   782,     0,    20,    96,    17,   327,   771,     0,   104,
     760,   119,   771,   755,   756,   699,   629,     0,   605,     0,
     608,     0,   613,   610,     0,     0,   614,   238,     0,   411,
     403,   405,   771,   408,   401,     0,   680,   741,   674,     0,
       0,     0,   691,   713,     0,   679,   557,   724,     0,   694,
     734,     0,   696,   738,    47,   260,     0,   677,    49,   262,
       0,   472,   476,     0,   387,   388,   473,   480,   458,   302,
      34,   307,     0,    37,   306,   108,   102,     0,    54,    40,
      52,     0,   277,   300,   217,    35,     0,   320,     0,     0,
       0,   782,   782,   465,    86,     0,   471,   286,   782,   782,
     283,   464,    84,   282,   323,   382,   782,   782,   588,   782,
     392,   782,   347,   394,    74,   393,   348,   489,     0,     0,
     384,     0,     0,   760,   329,   771,   755,   756,     0,     0,
       0,     0,   131,   132,   135,   771,     0,   771,     0,   460,
      79,    41,   277,   218,    51,   225,   145,   752,   304,   292,
     782,   782,   471,   782,   782,   771,   782,   771,   224,   275,
     112,   471,   699,   451,   454,   630,   634,   635,   636,   627,
     637,   607,   609,   616,   612,   618,   771,   410,     0,   709,
       0,   740,   726,   415,   683,   711,   684,   684,   720,   725,
     782,   684,   731,   684,   708,   684,     0,     0,   362,     0,
       0,   364,   782,    80,   782,   310,     0,     0,   100,     0,
     782,     0,     0,   782,     0,   580,   586,   553,     0,     0,
       0,   527,   771,   524,   541,   619,     0,   579,    65,   498,
     504,   506,   508,   502,   501,   537,   503,   546,   549,   552,
     558,   559,   548,   511,   560,   512,   565,   566,   567,   570,
     571,   572,   573,   574,   576,   575,   577,   578,   556,    62,
       0,     0,     0,    87,   774,   782,     0,     0,    85,   589,
     590,   782,   591,   383,   385,     0,    11,    13,   595,   386,
       0,     0,     0,   395,   397,     0,    75,   490,     0,   353,
       0,   482,     0,   352,   471,     0,     0,     0,     0,   471,
     360,   744,    70,   461,   462,     0,     0,   782,     0,     0,
     280,   287,   338,   771,     0,   631,   402,   404,   406,   409,
       0,   687,     0,   689,   678,     0,   695,     0,   692,   697,
      48,   261,    50,   263,   771,     0,   439,   438,     0,   305,
     308,    36,    53,     0,   278,   755,   756,   771,   755,   756,
     568,   569,   132,   584,     0,   529,   771,   530,   534,   771,
       0,   523,     0,     0,   526,   540,     0,   581,     0,   582,
       0,   499,     0,     0,   547,   551,   563,   564,     0,   510,
     509,     0,     0,   555,   259,    46,   222,    45,   223,     0,
      43,   220,    44,   221,     0,     0,   593,   594,     0,   398,
       0,   345,   346,     0,   350,   483,     0,     0,   354,     0,
     670,   356,     0,     0,   442,   456,     0,   452,   632,     0,
       0,   684,   684,   684,   684,   782,   437,   771,     0,   708,
     421,   716,   717,   782,   728,   421,   421,   419,   477,   481,
     309,   471,   771,   521,   544,   532,   531,   522,   535,   619,
     771,   775,   554,   771,   505,   500,   537,   507,   538,   542,
     550,   545,   561,   562,   585,   520,   516,   771,   771,   771,
     771,   771,   771,    42,   219,     0,   592,     0,   663,   664,
     320,     0,   782,     0,     0,   495,     0,   484,   782,   358,
     453,     0,     0,     0,     0,   407,   688,     0,   685,   690,
     693,   418,     0,   440,     0,   422,   430,   428,     0,   715,
       0,   417,     0,   433,     0,   435,   528,     0,   536,     0,
     525,   583,     0,     0,   513,   514,   515,   517,   518,   519,
     330,     0,   782,     0,   782,    12,   782,   491,     0,     0,
     485,   487,   488,   486,   446,   771,   444,   447,     0,   455,
       0,   684,   441,   727,   420,   421,   421,   330,     0,   718,
     782,   421,   729,   421,   421,   533,   538,   539,   543,   760,
     329,   771,   755,   756,   587,   396,     0,   496,   497,     0,
     443,   457,   633,   686,     0,   425,     0,   427,   760,   329,
     416,     0,   434,     0,   431,   436,     0,   471,   782,   445,
     421,   421,   421,   421,   493,   494,   492,   426,     0,   423,
     429,   432,   421,   424
};

/* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
   -1065, -1065, -1065,   976, -1065,   926,   763,  -522, -1065,   -28,
   -1065,   769, -1065,    39, -1065,  -229,  -542,   -67, -1065, -1065,
   -1065, -1065,   414,  2325,  2587, -1065,   -24,   -77, -1065, -1065,
      -6, -1065,  -424,  1029,   -10,  1153,  -153,    10,   -75, -1065,
    -401,     7,  2986,  -392,  1155,   -56,   -13, -1065, -1065,    -4,
   -1065,  3847, -1065,  1171, -1065,   623,  2014, -1065,   675,    26,
     604,  -360,    65,    16, -1065,  -319,  -203,     5, -1065,  -457,
     -20, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065,   917, -1065, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065, -1065, -1065, -1065, -1065, -1065, -1065, -1065,   466, -1065,
    -430,  1837,  -372, -1065,    42,  -783, -1065,  -799,  -785,   547,
     401,   -92,   154, -1065,   250,  -417, -1065, -1065,   383, -1065,
    -905, -1065,    43,   519, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065,   449, -1065, -1065,  -104,   712, -1065, -1065, -1065,   959,
   -1065, -1065, -1065, -1065,  -773, -1065,    23, -1065, -1065, -1065,
   -1065, -1065,  -722,   -43, -1065, -1065, -1065, -1065,   253, -1065,
   -1065,  -177, -1065,  -702,  -812,  -939,  -966,  -898,  -909, -1065,
     256, -1065, -1065,  -632,   257, -1065,  -782,   264, -1065, -1065,
   -1065,   101, -1065, -1065,   156,   562,   631, -1065,  1218,   736,
    1173,  1370, -1065,   836,  1486, -1065,  1719,  1813, -1065, -1065,
     -55, -1065, -1065,  -179, -1065, -1065, -1065, -1065, -1065, -1065,
   -1065,    24, -1065, -1065, -1065, -1065,    27,   -57,  2864,    14,
    1256,  3061,  1912, -1065, -1065,    63,   697,    57, -1065,  -274,
    -500,  -302,  -194, -1012,  -505,  -313,  -673,   235,  -514,   624,
     164, -1065, -1065,  -246, -1065,  -706,  -672, -1064,   170,   635,
   -1065,  -541, -1065,  -532,  -540, -1065, -1065, -1065,    33,  -394,
    -342,  -336, -1065, -1065,   -71, -1065,   -14,   290,   243,  -294,
     228,  -188,   -58,     1,    -2
};

/* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
       0,     1,     2,    70,    71,    72,   248,   625,  1088,   626,
     265,   266,   477,   267,   469,    74,   769,    75,   597,   779,
     583,   778,   419,   218,   219,   828,   382,   384,   385,   976,
      78,    79,   572,   254,    81,    82,   268,    83,    84,    85,
     497,    86,   221,   402,   403,   203,   204,   205,   660,   612,
     207,    88,   471,   372,    89,   576,   223,   273,   774,   613,
     791,   457,   458,   236,   237,   225,   443,   618,   763,   764,
      90,   379,   272,   483,   686,   808,   635,   821,   819,   650,
     565,   568,   256,    92,    93,    94,    95,    96,    97,    98,
      99,   100,   101,   333,   336,   796,   965,   811,   970,   971,
     756,   257,   628,   804,   972,   973,   394,   720,   721,   722,
     723,   542,   729,   730,  1244,  1196,  1197,  1117,  1025,  1026,
    1103,  1235,  1236,   103,   293,   503,   705,  1004,   853,  1107,
    1182,   337,   104,   105,   334,   569,   570,   752,   892,   573,
     574,   757,   894,   982,   812,  1233,   809,   977,  1093,  1266,
    1296,  1174,   918,  1134,   920,   921,  1071,  1072,   922,  1052,
    1044,  1046,  1047,  1048,   924,   925,  1148,  1050,   926,   927,
     928,   929,   930,   543,   932,   933,   934,   935,   936,   937,
     938,   797,   961,  1085,   967,   106,   107,   108,   109,   110,
     111,   112,   514,   709,   113,   516,   114,   115,   515,   517,
     295,   299,   300,   508,   707,   706,   855,  1005,  1108,  1184,
     859,   116,   117,   296,   118,   119,   120,   121,   228,   229,
     124,   230,   231,   646,   820,   322,   323,   324,   325,   874,
     732,   545,   546,   547,   548,   884,   550,   551,   552,   553,
    1122,  1123,   554,   555,   556,   557,   558,  1124,  1125,   559,
     560,   561,   562,   563,   726,   422,   651,   278,   461,   233,
     127,   690,   616,   654,   649,   426,   311,   453,   454,   786,
    1054,   488,   629,   389,   270
};

/* YYTABLE[YYPACT[STATE-NUM]] -- What to do in state STATE-NUM.  If
   positive, shift that token.  If negative, reduce the rule whose
   number is the opposite.  If YYTABLE_NINF, syntax error.  */
static const yytype_int16 yytable[] =
{
     128,   427,   297,   294,   206,   298,   220,   220,   549,   644,
     342,   630,   549,   312,   735,   245,   123,   617,   975,   386,
     378,   226,   226,   566,   206,   264,   381,   381,   880,   451,
     381,   724,   208,   425,   614,   978,   614,   288,   240,   312,
     615,    73,   624,   746,   460,   420,   750,   544,   239,   232,
     232,   544,   208,   276,   280,   206,   677,   949,   875,   739,
     274,   742,   306,   269,   287,   288,   882,   123,   123,  1007,
     383,   291,   313,   387,   326,   326,   923,   923,   288,   288,
     288,   224,   234,   388,   614,  1009,   672,   490,   275,   279,
     657,   492,   220,   271,   305,   677,   332,   206,   699,   291,
    1049,  -117,  -122,   614,  1105,   614,   331,   226,  1135,   615,
     478,   624,   396,   406,   406,   406,   831,   834,  -126,  1118,
     511,   513,  1146,   668,  -126,   775,  1040,  1041,   826,  1060,
    1060,   668,   614,   614,   328,   232,   803,  -665,  1252,   691,
     327,   702,  1060,  1140,     3,   758,   931,   931,   754,  1136,
     539,   711,  -657,  -123,   715,  -782,   447,  -130,   614,   627,
     614,   437,  1159,  1162,  -128,  1149,   691,   423,   736,  1237,
     736,   642,  -122,   242,   463,   643,   465,   321,   766,  -128,
    -665,  -657,   540,  1061,   264,   475,   737,   578,  1073,   241,
     877,   242,  -113,   871,   242,   794,  -544,   883,   627,  1106,
    -129,   802,   871,   806,  -125,   712,   755,   312,   716,   496,
    1252,  -117,  1192,   724,   867,  -122,   326,   326,   502,  -771,
     329,   473,   242,  -122,   444,   220,   123,   220,   220,  -127,
     444,  -117,  -113,  -755,  -117,   264,   462,  -123,  -117,  -124,
     226,  1255,   226,   459,   247,   128,  1256,   381,   381,   381,
     381,   480,   526,   527,  -117,   824,  -755,  1237,   451,  1158,
    1161,   123,  -755,   123,   288,  -126,   481,  -126,   232,   309,
     232,   310,  -130,   269,  1257,   329,   328,   123,   815,   239,
     331,   438,   439,  -114,  1152,  1153,    73,  -121,   825,   677,
     521,   522,   523,   524,  -119,   614,  -756,   614,   291,   988,
     452,  1096,   455,   691,  -129,   614,   309,   614,   310,  1149,
     495,  1007,   264,   691,  1149,  1258,   888,   243,   381,   246,
     891,   288,   639,  -756,  1185,  -116,  -128,   123,  -128,   312,
    -120,  -125,   123,   641,  -116,   710,   577,   710,   882,   960,
    1014,   577,   875,   328,   345,   309,   330,   310,   580,   249,
     269,   238,    73,   768,   123,   291,   631,   525,   633,  -118,
     953,   632,   668,   724,   668,   724,   898,   220,   958,  -115,
    -122,  1175,  -122,   634,   622,   462,  1011,  1013,   842,   979,
    -127,  1016,   983,  1018,  -123,  1019,  -123,  -124,  1258,   319,
     320,   923,   549,   123,  -656,  1231,   990,   533,   123,  -666,
     123,   330,  -666,   854,   623,  1232,   449,   261,   851,  1075,
    1077,   369,   370,   371,  1080,  1082,  -118,   534,   549,  -130,
     931,  -130,   622,  -656,   496,   549,  1068,   577,   577,   768,
     768,   544,   931,   301,  -123,  -116,   220,  -123,   577,   931,
     931,  -130,   430,   622,   462,   302,   577,   577,   538,   539,
     -94,  -129,   907,  -129,  -114,  -116,   685,   444,  -116,   444,
     335,   931,  -116,   339,  1099,   206,   839,  -771,   668,  1112,
    -108,   622,   534,   623,   577,   669,   242,  -130,  -125,   288,
    -125,   540,  1069,  1126,   464,  1070,    60,   845,   879,   476,
     476,   220,  1127,   208,  1250,   123,  -115,  -121,   622,   462,
     309,   623,   310,   538,   496,  1294,  1129,  1139,   747,   849,
    1121,   482,   788,   291,   242,   484,   504,   512,  1043,   697,
     303,   984,   875,   304,  -129,  -129,  -118,  -127,   623,  -127,
     862,   989,   948,   948,  -124,   862,  -124,  1163,   504,   549,
     727,   338,  1091,  1037,  -120,  1092,  -118,  -125,  1094,  -118,
    1003,   843,   727,  -118,   744,  1098,   844,   748,  1101,   288,
     614,   430,   614,   818,   487,   487,   615,  -116,   624,   487,
     760,   493,   506,   507,   805,  1191,   770,   343,   544,   123,
     997,   762,   123,  1201,   827,   788,  -125,   762,   319,   320,
     789,  -127,   504,   291,   506,   507,  -130,  1142,  -658,   955,
     799,   504,   801,  -659,   724,   724,  -115,   677,   875,   429,
    1097,  1186,  1188,  1189,  1190,  -124,   444,   642,  1051,   847,
    -125,  1042,   793,   850,   798,  -127,  -115,  -658,   220,  -115,
    -661,   784,  -659,  -115,  -124,   622,   462,  -660,   220,   789,
     792,   783,   123,   839,   123,   622,   462,   836,   506,   507,
     790,   374,   770,   770,  1246,   793,   206,   506,   507,  -661,
    1238,  1253,   431,  1127,   123,   623,  -660,   444,   871,  1179,
    1127,   963,  1127,   830,  -127,   623,  1154,   964,   288,   375,
     424,  1121,   668,   790,   208,   241,  1121,   861,  1121,   793,
    1121,  -369,   496,   433,  -118,   678,   864,   549,   440,   955,
     680,  -662,  -653,   682,   441,   904,   684,   442,  1203,  1205,
    1280,  1119,   291,  1171,   846,   448,   783,   790,  1173,  -369,
    -369,   694,   768,   768,   450,   993,   696,   768,   768,  -329,
    -662,  -653,   470,  1291,  -124,   919,   919,   969,   964,  -654,
     964,  1273,  1127,  1131,  1127,   374,   235,   288,   652,  1127,
     807,  1127,  1133,   381,  -115,  1137,   486,  -329,  -329,   238,
    1121,   986,  1121,   242,   489,   345,  1127,  1121,  -654,  1121,
     479,   242,   420,   375,   376,  1155,  -369,   653,   -93,   577,
     577,   291,  1269,   964,  1121,   810,   577,   577,   728,   242,
     985,   491,   947,   947,   962,   968,   987,   974,   485,   974,
     494,   762,  1227,   321,   768,   466,   948,   528,  1271,   529,
     530,   531,   532,   533,  -329,   467,   468,  -756,   948,   499,
     846,   765,   571,   374,   206,   948,   948,   374,  1275,  1277,
     377,   518,   123,   534,  1282,   123,  1284,  1285,   577,   577,
     564,   577,   577,   374,   444,  1066,  1067,   948,  -672,   345,
     768,   375,   445,   567,  -771,   375,   474,   536,   242,  1059,
    1056,   581,  1261,   519,   538,   539,  1217,  1218,  1219,  1045,
     636,   375,   500,  1297,  1299,  1300,  1301,  -755,   727,   614,
     645,   614,  1119,   662,  1145,  1303,  1065,   691,   780,  1119,
    1027,   679,  1027,   220,  1157,  1160,   663,   540,   577,   640,
     622,   462,   350,   351,   381,   681,   614,   785,   446,  -756,
     688,   123,   446,   123,   367,   368,   369,   370,   371,    91,
    -653,   504,   947,   947,  1228,  1229,   947,  -663,   501,  1100,
     623,   683,  1115,   227,   227,  -664,  1031,  1087,  1032,   689,
    -320,  -414,  1089,   947,  -108,   770,   770,  1090,  -653,  -653,
     770,   770,  -654,   577,   879,  -663,  -663,   362,   363,  1086,
     698,  1119,   785,  -664,  -664,   869,   582,  1287,  -320,  -320,
      91,    91,   725,   881,   289,   505,   885,   506,   507,   123,
    -654,  -654,   498,   498,   123,   227,   731,   487,   734,   738,
     740,   381,   785,   741,   743,   577,   751,  1128,   795,   767,
     123,  1132,   289,  -755,   374,  -653,   800,   787,  -755,   807,
     227,   227,  -663,   873,   227,   393,   404,   404,  1045,   227,
    -664,   810,  -760,   863,   865,  -320,   886,   770,  1045,  1045,
     868,    80,   375,   637,  1143,  -756,   870,  -654,   889,   123,
    -756,   893,   123,   897,  -300,    80,    80,   896,   900,   899,
     919,   950,   658,   659,  1057,   954,    37,    38,   947,    40,
    1172,   947,   964,   664,   288,  -760,  1001,  1010,   737,  1178,
     991,   675,   676,   770,  1012,   947,   947,   947,  1183,  1015,
     947,   947,    80,    80,  1209,   947,   947,  1017,  1000,   638,
    1002,  1024,  -301,  -760,  -760,  1055,  1223,    80,  1169,   692,
    1062,   288,   123,  1115,  1058,   785,  1141,   947,   374,  1006,
    1115,   123,  1115,   727,   528,   785,   529,   530,   531,   532,
     123,   727,    80,    80,  1063,  1064,    80,  1084,  1109,    91,
    1110,    80,  -303,  1138,   905,   406,   375,   647,   504,  1060,
    -330,  1180,  1187,  1224,   374,  1226,  1181,  1193,  -760,  1220,
    -760,   963,   227,  -755,   227,   227,  1240,  1242,   227,  1239,
     227,   381,   381,  1195,    91,  1200,    91,  1202,  -330,  -330,
     968,   374,   375,   902,   288,  1204,   974,  -304,  1207,  1208,
      91,  1247,  1115,  1212,  1115,   123,   374,   123,  1213,  1115,
    1272,  1115,   509,   648,   506,   507,   123,   661,   123,   375,
    1221,   289,   785,  1053,  1267,  1268,  1115,  1274,   406,  1276,
    1281,  1283,   220,  1286,   375,  1248,  -755,  -756,  1298,   622,
     462,   947,   798,   472,   974,  -330,   947,   947,   504,   903,
      91,   227,   227,   227,   227,    91,   227,   227,  1288,   520,
     693,    80,   992,   856,   857,  1111,   858,  1113,   727,   623,
     695,   391,  1114,    46,    47,   408,  1222,    91,   289,  1120,
     373,   829,   504,   966,    80,   866,    80,    80,  1265,  1008,
      80,   648,    80,  1194,  1116,   504,    80,  1028,    80,   994,
     123,   504,   509,   753,   506,   507,   974,  1142,    41,    42,
      43,    44,    80,  1104,   227,   575,    91,   917,   917,   504,
     947,    91,   227,    91,   528,   345,   529,   530,   531,   532,
     533,  1295,  1289,  1156,  1104,  1147,   509,   227,   506,   507,
    1150,  1151,   358,   359,  1144,  1264,   428,   785,  1225,   510,
     534,   506,   507,   833,   835,   708,   785,   506,   507,   785,
     939,   939,    80,    80,    80,    80,    80,    80,    80,    80,
     833,   835,   714,   713,   536,   506,   507,   421,   878,   785,
     537,   538,   539,   227,  1249,  1245,   876,   848,  1210,    80,
     367,   368,   369,   370,   371,  1206,     0,  1234,   345,   529,
     530,   531,   532,     0,     0,   528,  1211,   529,   530,   531,
     532,     0,     0,     0,   540,   358,   359,   541,    91,     0,
    1214,  1215,  1216,     0,   951,   952,    80,  1104,    80,   940,
     940,   956,   957,    80,    80,    80,   289,     0,   227,     0,
       0,     0,  1241,     0,     0,     0,     0,   917,   917,    80,
    1053,   917,     0,     0,     0,   718,   345,  1251,     0,  1254,
     364,   365,   366,   367,   368,   369,   370,   371,   917,     0,
     785,   785,   785,   358,   359,     0,   528,     0,   529,   530,
     531,   532,     0,   995,   996,     0,   998,   999,     0,     0,
     939,   939,     0,     0,   939,    80,   314,   315,   316,   317,
     318,   227,    91,     0,   227,    91,     0,     0,     0,     0,
       0,   939,     0,   227,     0,     0,   289,     0,     0,   776,
     366,   367,   368,   369,   370,   371,   718,     0,     0,  1290,
      80,  1292,   719,     0,   941,   941,     0,     0,  1293,     0,
       0,     0,     0,  1033,     0,  1270,     0,     0,     0,     0,
      80,     0,     0,  1302,     0,     0,     0,     0,     0,   940,
     940,     0,     0,   940,     0,    91,     0,    91,     0,     0,
       0,   785,     0,     0,     0,   227,     0,     0,     0,     0,
     940,     0,     0,   917,     0,   227,   917,    91,   227,     0,
       0,     0,     0,     0,     0,   776,   776,     0,  1079,     0,
     917,   917,   917,     0,     0,   917,   917,     0,     0,     0,
     917,   917,     0,    80,    80,     0,    80,    80,     0,     0,
       0,     0,     0,   227,     0,    80,   939,     0,     0,   939,
       0,    80,   917,     0,     0,   289,     0,     0,     0,     0,
    1102,     0,     0,   939,   939,   939,  1076,  1078,   939,   939,
       0,  1081,  1083,   939,   939,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   941,   941,     0,     0,   941,     0,
       0,     0,     0,     0,     0,   939,     0,    80,     0,    80,
       0,     0,     0,     0,     0,   941,     0,    80,     0,     0,
    1076,  1078,     0,  1081,  1083,   940,     0,    80,   940,    80,
      80,     0,     0,     0,   289,     0,     0,    80,    80,     0,
       0,     0,   940,   940,   940,     0,     0,   940,   940,     0,
       0,     0,   940,   940,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    80,     0,     0,     0,     0,
       0,     0,     0,     0,   940,     0,   917,     0,     0,     0,
       0,   917,   917,     0,     0,    91,     0,   227,    91,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  1164,     0,     0,     0,     0,     0,
       0,   528,     0,   529,   530,   531,   532,   533,     0,   939,
       0,     0,     0,     0,   939,   939,     0,  1164,     0,     0,
     941,     0,     0,   941,     0,     0,     0,   534,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   941,   941,   941,
       0,   535,   941,   941,     0,   917,     0,   941,   941,     0,
       0,   536,     0,     0,    91,     0,    91,     0,   538,   539,
     227,     0,     0,     0,     0,     0,     0,     0,     0,   941,
       0,     0,     0,     0,     0,     0,     0,     0,   940,   102,
       0,     0,     0,   940,   940,     0,     0,    80,   939,    80,
      80,   540,     0,   102,   102,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   776,   776,
       0,     0,     0,   776,   776,     0,     0,     0,     0,     0,
       0,     0,    91,     0,     0,     0,     0,    91,   227,     0,
     102,   102,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    91,     0,   102,     0,     0,     0,     0,
       0,     0,     0,     0,   126,     0,     0,   940,     0,     0,
       0,     0,     0,     0,     0,     0,    80,     0,    80,     0,
     102,   102,    80,     0,   102,     0,     0,     0,     0,   102,
       0,     0,    91,   941,     0,    91,     0,     0,   941,   941,
     776,   942,   942,     0,     0,     0,     0,     0,     0,     0,
       0,    23,    24,    25,    26,   126,   126,     0,     0,   292,
       0,     0,     0,     0,     0,   227,     0,    32,    33,    34,
      80,    80,     0,     0,     0,    80,    80,    41,    42,    43,
      44,    45,     0,     0,    80,     0,   776,   292,     0,    80,
      80,  1167,     0,     0,     0,    91,     0,     0,     0,     0,
     397,   407,   407,     0,    91,    80,     0,     0,     0,     0,
       0,     0,   941,    91,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  1199,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,   102,
       0,     0,     0,     0,    80,     0,     0,    80,     0,     0,
       0,     0,    80,     0,     0,     0,     0,     0,     0,     0,
     277,     0,   102,     0,   102,   102,   283,     0,   102,     0,
     102,   942,   942,     0,   102,   942,   102,    80,    91,     0,
      91,     0,     0,     0,     0,     0,     0,     0,     0,    91,
     102,    91,   942,     0,     0,     0,     0,     0,    80,     0,
       0,  1243,     0,     0,     0,     0,   528,    80,   529,   530,
     531,   532,   533,     0,   126,     0,    80,     0,     0,     0,
       0,     0,     0,     0,     0,    80,     0,     0,     0,   227,
       0,     0,   534,     0,     0,   227,   227,     0,   943,   943,
     102,   102,   102,   102,   102,   102,   102,   102,     0,   126,
       0,   126,     0,     0,     0,     0,   536,     0,     0,     0,
       0,     0,   537,   538,   539,   126,     0,   102,     0,     0,
       0,     0,     0,    91,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   292,     0,     0,     0,
      80,     0,    80,     0,     0,     0,   540,     0,     0,   541,
       0,    80,     0,    80,   102,     0,   102,   942,     0,     0,
     942,   102,   102,   102,   432,   126,   242,   434,   435,   436,
     126,     0,     0,     0,   942,   942,   942,   102,     0,   942,
     942,     0,     0,     0,   942,   942,     0,     0,     0,   344,
       0,    80,   126,   292,     0,     0,     0,    80,    80,     0,
       0,     0,     0,     0,   944,   944,   942,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   943,   943,
       0,     0,   943,   102,     0,     0,     0,     0,     0,     0,
       0,   126,     0,     0,     0,    80,   126,     0,   126,   943,
       0,     0,   345,   346,   347,   348,   349,   350,   351,   352,
     353,   354,   355,   356,   357,     0,     0,     0,   102,   358,
     359,     0,     0,     0,     0,   360,     0,    76,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   102,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     528,     0,   529,   530,   531,   532,   533,     0,     0,     0,
     361,     0,   362,   363,   364,   365,   366,   367,   368,   369,
     370,   371,     0,     0,     0,     0,   534,     0,    76,    76,
     942,     0,   285,     0,     0,   942,   942,     0,     0,     0,
     535,     0,     0,   126,   944,   944,     0,     0,   944,     0,
     536,   102,   102,     0,   102,   102,   537,   538,   539,     0,
     285,   292,     0,   102,   943,   944,     0,   943,     0,   102,
       0,     0,     0,   285,   285,   285,     0,     0,     0,     0,
       0,   943,   943,   943,     0,     0,   943,   943,     0,     0,
     540,   943,   943,   541,   345,   346,   347,   348,   349,   350,
     351,   352,     0,   354,   355,   665,   667,     0,     0,   942,
       0,   358,   359,   943,   277,   102,     0,   102,     0,     0,
       0,     0,     0,     0,     0,   102,     0,   126,     0,     0,
     126,     0,     0,     0,     0,   102,     0,   102,   102,     0,
       0,   292,     0,     0,   777,   102,   102,   945,   945,     0,
     667,     0,     0,   277,   362,   363,   364,   365,   366,   367,
     368,   369,   370,   371,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   102,     0,     0,     0,     0,     0,     0,
     944,     0,     0,   944,     0,     0,     0,    76,     0,     0,
     126,     0,   126,     0,     0,     0,     0,   944,   944,   944,
       0,     0,   944,   944,     0,     0,     0,   944,   944,     0,
       0,     0,   126,     0,     0,     0,   733,     0,     0,     0,
     777,   777,    76,     0,    76,     0,     0,   943,     0,   944,
       0,     0,   943,   943,     0,     0,     0,     0,    76,    77,
     761,   946,   946,     0,     0,     0,   773,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   285,
     292,     0,     0,     0,     0,     0,     0,     0,     0,   860,
       0,     0,     0,     0,     0,     0,     0,   945,   945,     0,
       0,   945,     0,     0,     0,     0,     0,     0,    76,     0,
      77,    77,     0,    76,   286,     0,     0,     0,   945,     0,
       0,     0,     0,     0,     0,   102,   943,   102,   102,     0,
       0,     0,     0,     0,     0,    76,   285,     0,     0,     0,
       0,     0,   286,     0,     0,   837,     0,     0,     0,   292,
     838,     0,     0,     0,     0,   286,   286,   286,     0,     0,
       0,     0,     0,   944,     0,   667,     0,   277,   944,   944,
       0,     0,     0,     0,    76,     0,     0,     0,     0,    76,
       0,    76,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   946,   946,     0,     0,   946,     0,     0,     0,     0,
     126,     0,     0,   126,   102,     0,   102,     0,     0,     0,
     102,     0,   946,   872,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   945,     0,     0,   945,     0,     0,     0,
       0,     0,   944,   895,     0,     0,     0,     0,     0,     0,
     945,   945,   945,     0,     0,   945,   945,     0,   102,   102,
     945,   945,     0,   102,   102,     0,     0,     0,     0,    77,
       0,   667,   102,     0,     0,     0,    76,   102,   102,   126,
     959,   126,   945,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   102,   285,   981,     0,     0,     0,     0,
       0,     0,     0,     0,    77,     0,    77,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      77,     0,     0,     0,     0,     0,     0,   946,     0,     0,
     946,     0,   102,   777,   777,   102,   122,     0,   777,   777,
     102,   286,     0,     0,   946,   946,   946,   126,     0,   946,
     946,     0,   126,     0,   946,   946,     0,     0,     0,     0,
      76,     0,     0,    76,     0,   102,     0,     0,   126,     0,
      77,     0,     0,     0,   285,    77,   946,    76,     0,     0,
    1030,     0,     0,     0,     0,     0,   102,   122,   122,     0,
       0,   290,     0,     0,     0,   102,   945,    77,   286,     0,
       0,   945,   945,     0,   102,     0,     0,   126,     0,     0,
     126,     0,     0,   102,     0,   777,     0,     0,     0,   290,
       0,     0,     0,    76,     0,    76,     0,     0,     0,     0,
       0,     0,   395,   405,   405,   405,    77,     0,     0,     0,
       0,    77,     0,    77,     0,    76,     0,     0,     0,     0,
       0,     0,     0,    76,    76,     0,     0,     0,    87,     0,
       0,   777,     0,     0,  1095,     0,  1170,     0,     0,     0,
     126,     0,     0,     0,     0,   945,     0,     0,   102,   126,
     102,     0,     0,     0,     0,   277,     0,     0,   126,   102,
     946,   102,     0,   285,     0,   946,   946,     0,     0,     0,
       0,     0,   528,   407,   529,   530,   531,   532,   533,    87,
      87,     0,     0,  1130,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   534,   102,
       0,     0,     0,   125,     0,   102,   102,     0,    77,     0,
       0,     0,   535,     0,     0,     0,   122,     0,     0,     0,
       0,     0,   536,   126,   392,   126,   286,     0,   537,   538,
     539,     0,   285,     0,   126,     0,   126,     0,     0,   946,
       0,     0,     0,   102,     0,     0,   407,     0,     0,     0,
    1177,   122,     0,   122,   125,   125,     0,     0,     0,     0,
       0,     0,   540,     0,     0,   541,     0,   122,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   852,     0,     0,
       0,     0,     0,    76,     0,     0,    76,     0,   290,     0,
       0,     0,    77,     0,     0,    77,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   286,     0,     0,    77,
       0,     0,     0,     0,     0,     0,     0,   122,   126,     0,
       0,     0,   122,     0,     0,     0,     0,     0,     0,     0,
    1230,     0,     0,     0,     0,     0,     0,     0,    87,     0,
       0,     0,     0,     0,   122,   290,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    77,     0,    77,     0,     0,
     901,     0,    76,     0,    76,     0,     0,     0,     0,     0,
       0,     0,     0,    87,     0,    87,     0,    77,     0,     0,
       0,     0,     0,   122,     0,    77,    77,     0,   122,    87,
     122,     0,     0,     0,   345,   346,   347,   348,   349,   350,
     351,   352,   353,   354,   355,   356,   357,     0,     0,     0,
       0,   358,   359,   125,     0,     0,    76,    76,     0,     0,
       0,    76,    76,     0,     0,   286,     0,     0,     0,     0,
      76,     0,     0,     0,     0,    76,     0,     0,     0,    87,
       0,     0,     0,     0,    87,     0,     0,     0,   125,     0,
     125,    76,   361,     0,   362,   363,   364,   365,   366,   367,
     368,   369,   370,   371,   125,     0,    87,     0,     0,     0,
       0,  -272,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   122,     0,     0,     0,     0,
      76,     0,     0,    76,   286,     0,     0,     0,    76,     0,
       0,     0,     0,   290,     0,    87,     0,     0,     0,     0,
      87,     0,    87,     0,   125,     0,     0,     0,     0,   125,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   345,
    -783,  -783,  -783,  -783,   350,   351,     0,     0,  -783,  -783,
       0,   125,     0,     0,    76,    77,   358,   359,    77,   285,
       0,     0,     0,    76,     0,     0,     0,     0,     0,     0,
       0,     0,    76,     0,     0,     0,     0,     0,     0,   122,
       0,    76,   122,     0,     0,     0,     0,     0,     0,     0,
     125,     0,     0,   290,     0,   125,   285,   125,     0,   362,
     363,   364,   365,   366,   367,   368,   369,   370,   371,     0,
     345,   346,   347,   348,   349,   350,   351,    87,     0,   354,
     355,     0,     0,     0,     0,     0,     0,   358,   359,     0,
       0,     0,     0,     0,    77,     0,    77,     0,     0,     0,
       0,     0,   122,     0,   122,     0,    76,     0,    76,     0,
       0,     0,     0,     0,     0,     0,     0,    76,     0,    76,
       0,     0,     0,     0,   122,     0,     0,     0,     0,   285,
     362,   363,   364,   365,   366,   367,   368,   369,   370,   371,
       0,     0,     0,     0,     0,     0,     0,     0,    77,    77,
       0,     0,   125,    77,    77,     0,     0,     0,     0,     0,
       0,    87,    77,     0,    87,     0,     0,    77,   887,     0,
       0,     0,   290,     0,     0,     0,     0,     0,   771,     0,
       0,     0,     0,    77,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    76,   345,   346,   347,   348,   349,   350,   351,   352,
     353,   354,   355,   356,   357,     0,     0,     0,     0,   358,
     359,     0,    77,     0,    87,    77,    87,     0,     0,     0,
      77,     0,     0,     0,     0,     0,   125,     0,     0,   125,
       0,   290,     0,     0,     0,     0,    87,     0,     0,     0,
       0,     0,     0,   125,   771,   771,     0,     0,     0,     0,
     361,     0,   362,   363,   364,   365,   366,   367,   368,   369,
     370,   371,     0,     0,     0,     0,    77,     0,     0,     0,
       0,   286,     0,     0,     0,    77,     0,     0,     0,     0,
       0,     0,   122,     0,    77,   122,     0,  -782,     0,   125,
       0,   125,     0,    77,     0,  -782,  -782,  -782,   890,     0,
    -782,  -782,  -782,     0,  -782,     0,     0,     0,   286,     0,
       0,   125,  -782,  -782,  -782,     0,     0,     0,     0,   125,
     125,     0,     0,     0,  -782,  -782,     0,  -782,  -782,  -782,
    -782,  -782,   345,   346,   347,   348,   349,   350,   351,   352,
     353,   354,   355,   356,   357,     0,     0,     0,     0,   358,
     359,     0,     0,     0,     0,  -782,  -782,     0,    77,     0,
      77,   122,     0,   122,     0,     0,     0,     0,     0,    77,
       0,    77,     0,     0,     0,   901,     0,     0,     0,     0,
       0,   286,     0,     0,  -782,  -782,     0,     0,     0,     0,
     361,     0,   362,   363,   364,   365,   366,   367,   368,   369,
     370,   371,     0,     0,    87,     0,     0,    87,  -782,   345,
     346,   347,   348,   349,   350,   351,   352,   353,   354,   355,
     356,   357,     0,     0,     0,     0,   358,   359,     0,   122,
       0,     0,     0,     0,   122,     0,     0,     0,     0,  -782,
    -782,     0,     0,     0,   238,  -782,     0,  -782,     0,  -782,
     122,     0,     0,    77,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   222,   222,     0,     0,   361,     0,   362,
     363,   364,   365,   366,   367,   368,   369,   370,   371,   125,
       0,     0,   125,    87,     0,    87,     0,     0,     0,   122,
       0,     0,   122,     0,     0,   255,   258,   259,   260,     0,
       0,     0,   222,   222,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   307,   308,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   771,   771,     0,
       0,     0,   771,   771,     0,     0,     0,     0,  1168,   222,
       0,    87,   122,     0,     0,     0,    87,     0,   125,     0,
     125,   122,     0,     0,     0,     0,     0,     0,     0,     0,
     122,     0,    87,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   405,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    87,   125,   125,    87,     0,     0,   125,   125,   771,
       0,     0,     0,     0,     0,     0,   125,     0,     0,     0,
       0,   125,     0,     0,     0,   122,     0,   122,     0,     0,
       0,     0,     0,     0,     0,     0,   122,   125,   122,     0,
       0,     0,     0,     0,     0,     0,     0,   222,   405,     0,
     222,   222,   222,     0,   307,   771,     0,     0,     0,     0,
    1166,     0,     0,     0,    87,     0,     0,     0,     0,     0,
       0,     0,   222,    87,   222,   222,   125,     0,     0,   125,
       0,     0,    87,     0,   125,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   345,   346,   347,   348,   349,   350,
     351,   352,   353,   354,   355,   356,   357,     0,     0,     0,
     122,   358,   359,     0,     0,     0,     0,     0,     0,     0,
     125,     0,     0,     0,     0,     0,     0,     0,     0,   125,
       0,     0,     0,     0,     0,     0,     0,    87,   125,    87,
       0,     0,     0,     0,     0,     0,     0,   125,    87,     0,
      87,     0,   361,     0,   362,   363,   364,   365,   366,   367,
     368,   369,   370,   371,     0,     0,     0,     0,     0,     0,
       0,     0,   584,   585,   586,   587,   588,     0,   242,   589,
     590,   591,   592,   593,   594,   595,   596,     0,   598,     0,
       0,   599,   600,   601,   602,   603,   604,   605,   606,   607,
     608,     0,     0,     0,   222,     0,     0,     0,     0,     0,
       0,     0,   125,     0,   125,     0,     0,     0,     0,     0,
       0,     0,     0,   125,     0,   125,     0,     0,     0,     0,
       0,     0,    87,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   222,   222,
       0,     0,     0,   222,     0,     0,     0,   222,     0,     0,
       0,     0,     0,   260,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   687,     0,
       0,     0,     0,     0,     0,     0,     0,   125,     0,     0,
       0,     0,     0,   222,     0,     0,   222,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   222,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   717,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -782,     4,     0,     5,     6,
       7,     8,     9,     0,     0,     0,    10,    11,     0,   222,
       0,    12,     0,    13,    14,    15,    16,    17,    18,    19,
       0,   745,     0,     0,   749,    20,    21,    22,    23,    24,
      25,    26,     0,   222,    27,     0,     0,     0,     0,   772,
      28,    29,    30,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,    52,   222,     0,    53,    54,     0,
      55,    56,     0,    57,     0,   222,    58,    59,    60,    61,
      62,    63,    64,    65,    66,   832,   832,     0,   222,   745,
     749,   832,     0,   222,     0,     0,     0,     0,     0,     0,
       0,     0,   832,   832,     0,     0,     0,     0,   222,     0,
     222,     0,     0,    67,    68,    69,     0,     0,     0,   832,
    -760,     0,     0,     0,     0,  -782,     0,  -782,  -760,  -760,
    -760,     0,     0,  -760,  -760,  -760,     0,  -760,     0,     0,
       0,     0,     0,     0,     0,  -760,  -760,  -760,  -760,  -760,
       0,     0,     0,     0,     0,     0,   222,  -760,  -760,     0,
    -760,  -760,  -760,  -760,  -760,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   222,     0,  -760,  -760,
       0,     0,     0,     0,     0,     0,     0,     0,  -760,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,
    -760,     0,     0,     0,   222,  -760,  -760,  -760,  -760,     0,
     840,  -760,     0,   222,     0,     0,     0,  -760,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   222,     0,
       0,  -760,     0,     0,  -760,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -126,  -760,  -760,  -760,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,     0,     0,
       0,     0,  -760,  -760,  -760,  -760,     0,     0,  -760,  -760,
    -760,     0,  -760,   345,   346,   347,   348,   349,   350,   351,
     352,   353,   354,   355,   356,   357,     0,     0,     0,     0,
     358,   359,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  1020,  1021,     0,  1022,  1023,     0,     0,
       0,     0,     0,   222,     0,     0,     0,     0,  1034,     0,
     222,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   361,     0,   362,   363,   364,   365,   366,   367,   368,
     369,   370,   371,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  1074,   832,   832,
       0,     0,     0,   832,   832,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -653,     0,     0,   222,     0,     0,
       0,     0,  -653,  -653,  -653,     0,     0,  -653,  -653,  -653,
       0,  -653,   832,   832,     0,   832,   832,     0,   222,  -653,
       0,  -653,  -653,  -653,     0,     0,     0,     0,     0,     0,
       0,  -653,  -653,     0,  -653,  -653,  -653,  -653,  -653,     0,
       0,     0,     0,     0,     0,     0,   222,     0,     0,     0,
     832,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,  -653,  -653,     0,     0,     0,     0,     0,     0,
       0,     0,  -653,  -653,  -653,  -653,  -653,  -653,  -653,  -653,
    -653,  -653,  -653,  -653,  -653,     0,     0,     0,     0,  -653,
    -653,  -653,  -653,     0,  -653,  -653,   832,     0,     0,     0,
       0,  -653,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   222,     0,  -653,     0,     0,  -653,   832,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -653,
    -653,  -653,  -653,  -653,  -653,  -653,  -653,  -653,  -653,  -653,
    -653,  -653,     0,     0,     0,     0,     0,  -653,  -653,  -653,
       0,     0,  -653,  -653,  -653,     0,  -653,     0,     0,     0,
       0,     0,  -654,     0,     0,     0,     0,     0,     0,     0,
    -654,  -654,  -654,     0,     0,  -654,  -654,  -654,     0,  -654,
       0,     0,     0,     0,     0,     0,     0,  -654,     0,  -654,
    -654,  -654,     0,   222,     0,     0,     0,     0,     0,  -654,
    -654,     0,  -654,  -654,  -654,  -654,  -654,     0,     0,     0,
     345,   346,   347,   348,   349,   350,   351,   352,   353,   354,
     355,  -783,  -783,     0,     0,     0,     0,   358,   359,     0,
    -654,  -654,     0,     0,     0,     0,     0,     0,     0,   222,
    -654,  -654,  -654,  -654,  -654,  -654,  -654,  -654,  -654,  -654,
    -654,  -654,  -654,     0,     0,     0,     0,  -654,  -654,  -654,
    -654,     0,  -654,  -654,     0,     0,     0,     0,     0,  -654,
     362,   363,   364,   365,   366,   367,   368,   369,   370,   371,
       0,     0,     0,  -654,     0,     0,  -654,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -654,  -654,  -654,
    -654,  -654,  -654,  -654,  -654,  -654,  -654,  -654,  -654,  -654,
       0,     0,     0,     0,     0,  -654,  -654,  -654,  -761,     0,
    -654,  -654,  -654,     0,  -654,     0,  -761,  -761,  -761,     0,
       0,  -761,  -761,  -761,     0,  -761,     0,     0,     0,     0,
       0,     0,     0,  -761,  -761,  -761,  -761,  -761,     0,     0,
       0,     0,     0,     0,     0,  -761,  -761,     0,  -761,  -761,
    -761,  -761,  -761,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  -761,  -761,     0,     0,
       0,     0,     0,     0,     0,     0,  -761,  -761,  -761,  -761,
    -761,  -761,  -761,  -761,  -761,  -761,  -761,  -761,  -761,     0,
       0,     0,     0,  -761,  -761,  -761,  -761,     0,     0,  -761,
       0,     0,     0,     0,     0,  -761,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -761,
       0,     0,  -761,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -761,  -761,  -761,  -761,  -761,  -761,
    -761,  -761,  -761,  -761,  -761,  -761,     0,     0,     0,     0,
    -761,  -761,  -761,  -761,  -762,     0,  -761,  -761,  -761,     0,
    -761,     0,  -762,  -762,  -762,     0,     0,  -762,  -762,  -762,
       0,  -762,     0,     0,     0,     0,     0,     0,     0,  -762,
    -762,  -762,  -762,  -762,     0,     0,     0,     0,     0,     0,
       0,  -762,  -762,     0,  -762,  -762,  -762,  -762,  -762,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,  -762,  -762,     0,     0,     0,     0,     0,     0,
       0,     0,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,
    -762,  -762,  -762,  -762,  -762,     0,     0,     0,     0,  -762,
    -762,  -762,  -762,     0,     0,  -762,     0,     0,     0,     0,
       0,  -762,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -762,     0,     0,  -762,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,  -762,
    -762,  -762,     0,     0,     0,     0,  -762,  -762,  -762,  -762,
    -329,     0,  -762,  -762,  -762,     0,  -762,     0,  -329,  -329,
    -329,     0,     0,  -329,  -329,  -329,     0,  -329,     0,     0,
       0,     0,     0,     0,     0,  -329,     0,  -329,  -329,  -329,
       0,     0,     0,     0,     0,     0,     0,  -329,  -329,     0,
    -329,  -329,  -329,  -329,  -329,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -329,  -329,
       0,     0,     0,     0,     0,     0,     0,     0,  -329,  -329,
    -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,
    -329,     0,     0,     0,     0,  -329,  -329,  -329,  -329,     0,
     841,  -329,     0,     0,     0,     0,     0,  -329,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -329,     0,     0,  -329,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -128,  -329,  -329,  -329,  -329,
    -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,     0,     0,
       0,     0,     0,  -329,  -329,  -329,  -470,     0,  -329,  -329,
    -329,     0,  -329,     0,  -470,  -470,  -470,     0,     0,  -470,
    -470,  -470,     0,  -470,     0,     0,     0,     0,     0,     0,
       0,  -470,  -470,  -470,  -470,     0,     0,     0,     0,     0,
       0,     0,     0,  -470,  -470,     0,  -470,  -470,  -470,  -470,
    -470,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -470,  -470,     0,     0,     0,     0,
       0,     0,     0,     0,  -470,  -470,  -470,  -470,  -470,  -470,
    -470,  -470,  -470,  -470,  -470,  -470,  -470,     0,     0,     0,
       0,  -470,  -470,  -470,  -470,     0,     0,  -470,     0,     0,
       0,     0,     0,  -470,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -470,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,  -470,     0,  -470,  -470,  -470,  -470,  -470,  -470,
    -470,  -470,  -470,  -470,     0,     0,     0,     0,  -470,  -470,
    -470,  -470,  -321,   238,  -470,  -470,  -470,     0,  -470,     0,
    -321,  -321,  -321,     0,     0,  -321,  -321,  -321,     0,  -321,
       0,     0,     0,     0,     0,     0,     0,  -321,     0,  -321,
    -321,  -321,     0,     0,     0,     0,     0,     0,     0,  -321,
    -321,     0,  -321,  -321,  -321,  -321,  -321,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -321,  -321,     0,     0,     0,     0,     0,     0,     0,     0,
    -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,
    -321,  -321,  -321,     0,     0,     0,     0,  -321,  -321,  -321,
    -321,     0,     0,  -321,     0,     0,     0,     0,     0,  -321,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -321,     0,     0,  -321,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -321,  -321,
    -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,
       0,     0,     0,     0,     0,  -321,  -321,  -321,  -782,     0,
    -321,  -321,  -321,     0,  -321,     0,  -782,  -782,  -782,     0,
       0,  -782,  -782,  -782,     0,  -782,     0,     0,     0,     0,
       0,     0,     0,  -782,  -782,  -782,  -782,     0,     0,     0,
       0,     0,     0,     0,     0,  -782,  -782,     0,  -782,  -782,
    -782,  -782,  -782,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  -782,  -782,     0,     0,
       0,     0,     0,     0,     0,     0,  -782,  -782,  -782,  -782,
    -782,  -782,  -782,  -782,  -782,  -782,  -782,  -782,  -782,     0,
       0,     0,     0,  -782,  -782,  -782,  -782,     0,     0,  -782,
       0,     0,     0,     0,     0,  -782,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -782,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -782,     0,  -782,  -782,  -782,  -782,
    -782,  -782,  -782,  -782,  -782,  -782,     0,     0,     0,     0,
    -782,  -782,  -782,  -782,  -336,   238,  -782,  -782,  -782,     0,
    -782,     0,  -336,  -336,  -336,     0,     0,  -336,  -336,  -336,
       0,  -336,     0,     0,     0,     0,     0,     0,     0,  -336,
       0,  -336,  -336,     0,     0,     0,     0,     0,     0,     0,
       0,  -336,  -336,     0,  -336,  -336,  -336,  -336,  -336,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,  -336,  -336,     0,     0,     0,     0,     0,     0,
       0,     0,  -336,  -336,  -336,  -336,  -336,  -336,  -336,  -336,
    -336,  -336,  -336,  -336,  -336,     0,     0,     0,     0,  -336,
    -336,  -336,  -336,     0,     0,  -336,     0,     0,     0,     0,
       0,  -336,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -336,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -336,     0,  -336,  -336,  -336,  -336,  -336,  -336,  -336,  -336,
    -336,  -336,     0,     0,     0,     0,     0,  -336,  -336,  -336,
    -760,   235,  -336,  -336,  -336,     0,  -336,     0,  -760,  -760,
    -760,     0,     0,     0,  -760,  -760,     0,  -760,     0,     0,
       0,     0,     0,     0,     0,  -760,  -760,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -760,  -760,     0,
    -760,  -760,  -760,  -760,  -760,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,  -760,  -760,
       0,     0,     0,     0,     0,     0,     0,     0,  -760,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,
    -760,     0,     0,     0,     0,  -760,  -760,  -760,  -760,     0,
     781,  -760,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -760,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -126,  -760,     0,  -760,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,  -760,     0,     0,
       0,     0,  -760,  -760,  -760,  -117,  -760,     0,  -760,     0,
    -760,     0,  -760,     0,  -760,  -760,  -760,     0,     0,     0,
    -760,  -760,     0,  -760,     0,     0,     0,     0,     0,     0,
       0,  -760,  -760,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -760,  -760,     0,  -760,  -760,  -760,  -760,
    -760,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,  -760,  -760,     0,     0,     0,     0,
       0,     0,     0,     0,  -760,  -760,  -760,  -760,  -760,  -760,
    -760,  -760,  -760,  -760,  -760,  -760,  -760,     0,     0,     0,
       0,  -760,  -760,  -760,  -760,     0,   781,  -760,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -760,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  -126,  -760,     0,  -760,  -760,  -760,  -760,  -760,  -760,
    -760,  -760,  -760,  -760,     0,     0,     0,     0,  -760,  -760,
    -760,  -760,  -329,     0,  -760,     0,  -760,     0,  -760,     0,
    -329,  -329,  -329,     0,     0,     0,  -329,  -329,     0,  -329,
       0,     0,     0,     0,     0,     0,     0,  -329,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -329,
    -329,     0,  -329,  -329,  -329,  -329,  -329,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    -329,  -329,     0,     0,     0,     0,     0,     0,     0,     0,
    -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,
    -329,  -329,  -329,     0,     0,     0,     0,  -329,  -329,  -329,
    -329,     0,   782,  -329,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -329,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,  -128,  -329,     0,
    -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,
       0,     0,     0,     0,     0,  -329,  -329,  -119,  -329,     0,
    -329,     0,  -329,     0,  -329,     0,  -329,  -329,  -329,     0,
       0,     0,  -329,  -329,     0,  -329,     0,     0,     0,     0,
       0,     0,     0,  -329,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,  -329,  -329,     0,  -329,  -329,
    -329,  -329,  -329,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,  -329,  -329,     0,     0,
       0,     0,     0,     0,     0,     0,  -329,  -329,  -329,  -329,
    -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,  -329,     0,
       0,     0,     0,  -329,  -329,  -329,  -329,     0,   782,  -329,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,  -329,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,  -128,  -329,     0,  -329,  -329,  -329,  -329,
    -329,  -329,  -329,  -329,  -329,  -329,     0,     0,     0,     0,
       0,  -329,  -329,  -329,     0,     0,  -329,     0,  -329,     4,
    -329,     5,     6,     7,     8,     9,  -782,  -782,  -782,    10,
      11,     0,     0,  -782,    12,     0,    13,    14,    15,    16,
      17,    18,    19,     0,     0,     0,     0,     0,    20,    21,
      22,    23,    24,    25,    26,     0,     0,    27,     0,     0,
       0,     0,     0,    28,    29,   262,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,  -782,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,    52,     0,     0,
      53,    54,     0,    55,    56,     0,    57,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     4,     0,     5,     6,     7,     8,     9,     0,     0,
    -782,    10,    11,     0,  -782,  -782,    12,     0,    13,    14,
      15,    16,    17,    18,    19,     0,    67,    68,    69,     0,
      20,    21,    22,    23,    24,    25,    26,     0,  -782,    27,
    -782,     0,     0,     0,     0,    28,    29,   262,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,  -782,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,    52,
       0,     0,    53,    54,     0,    55,    56,     0,    57,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     4,     0,     5,     6,     7,     8,     9,
       0,     0,  -782,    10,    11,     0,     0,  -782,    12,  -782,
      13,    14,    15,    16,    17,    18,    19,     0,    67,    68,
      69,     0,    20,    21,    22,    23,    24,    25,    26,     0,
    -782,    27,  -782,     0,     0,     0,     0,    28,    29,   262,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
    -782,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,    52,     0,     0,    53,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     4,     0,     5,     6,     7,
       8,     9,     0,     0,  -782,    10,    11,     0,     0,  -782,
      12,     0,    13,    14,    15,    16,    17,    18,    19,  -782,
      67,    68,    69,     0,    20,    21,    22,    23,    24,    25,
      26,     0,  -782,    27,  -782,     0,     0,     0,     0,    28,
      29,   262,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,  -782,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,    52,     0,     0,    53,    54,     0,    55,
      56,     0,    57,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     4,     0,     5,
       6,     7,     8,     9,     0,     0,  -782,    10,    11,     0,
       0,  -782,    12,     0,    13,    14,    15,    16,    17,    18,
      19,     0,    67,    68,    69,     0,    20,    21,    22,    23,
      24,    25,    26,     0,  -782,    27,  -782,     0,     0,     0,
       0,    28,    29,   262,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,  -782,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,    52,     0,     0,    53,    54,
       0,    55,    56,     0,    57,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     4,
       0,     5,     6,     7,     8,     9,     0,  -782,  -782,    10,
      11,     0,     0,     0,    12,     0,    13,    14,    15,    16,
      17,    18,    19,     0,    67,    68,    69,     0,    20,    21,
      22,    23,    24,    25,    26,     0,  -782,    27,  -782,     0,
       0,     0,     0,    28,    29,   262,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,  -782,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,    52,     0,     0,
      53,    54,     0,    55,    56,     0,    57,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     4,     0,     5,     6,     7,     8,     9,     0,     0,
    -782,    10,    11,     0,     0,     0,    12,     0,    13,    14,
      15,    16,    17,    18,    19,     0,    67,    68,    69,     0,
      20,    21,    22,    23,    24,    25,    26,     0,  -782,    27,
    -782,     0,     0,     0,     0,    28,    29,   262,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,  -782,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,    52,
       0,     0,    53,    54,     0,    55,    56,     0,    57,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     4,     0,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,    67,    68,
      69,     0,    20,    21,    22,    23,    24,    25,    26,     0,
    -782,    27,  -782,     0,     0,     0,     0,    28,    29,   262,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,    52,     0,     0,   263,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      67,    68,    69,     0,     0,     0,     0,     0,     0,     0,
    -782,     0,  -782,     4,  -782,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,     0,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   262,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,    52,     0,     0,    53,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      67,    68,    69,     0,     0,     0,     0,     0,     0,     0,
    -782,     0,  -782,     4,  -782,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,     0,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,    30,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,    52,     0,     0,    53,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      67,    68,    69,     0,     0,  -782,     0,     0,     0,     0,
       0,     0,  -782,     4,  -782,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,     0,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   262,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,    52,     0,     0,    53,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      67,    68,    69,     0,     0,  -782,     0,     0,     0,     0,
       0,     0,  -782,     4,  -782,     5,     6,     7,     8,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,     0,
       0,     0,    20,    21,    22,    23,    24,    25,    26,     0,
       0,    27,     0,     0,     0,     0,     0,    28,    29,   262,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,    52,     0,     0,    53,    54,     0,    55,    56,     0,
      57,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,  -782,     0,     4,     0,     5,     6,     7,
       8,     9,     0,     0,     0,    10,    11,     0,     0,     0,
      12,     0,    13,    14,    15,    16,    17,    18,    19,     0,
      67,    68,    69,     0,    20,    21,    22,    23,    24,    25,
      26,     0,  -782,    27,  -782,     0,     0,     0,     0,    28,
      29,     0,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,    52,     0,     0,    53,    54,     0,    55,
      56,     0,    57,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,   380,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,     0,     0,
       0,    12,  -769,    13,    14,    15,    16,    17,    18,    19,
       0,     0,    67,    68,    69,    20,    21,    22,    23,    24,
      25,    26,  -324,     0,    27,     0,  -324,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,   211,     0,     0,   212,    54,     0,
      55,    56,     0,     0,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,  -770,     4,     0,
       5,     6,     7,     8,     9,     0,     0,     0,    10,    11,
       0,     0,     0,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,    67,    68,    69,     0,    20,    21,    22,
      23,    24,    25,    26,     0,   309,    27,   310,     0,     0,
       0,     0,    28,    29,    30,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,    52,     0,     0,    53,
      54,     0,    55,    56,     0,    57,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    67,    68,    69,     0,     0,
    -770,     0,     0,     0,     0,     0,     4,   519,     5,     6,
       7,     8,     9,  -770,  -770,  -770,    10,    11,     0,  -770,
    -770,    12,  -770,    13,    14,    15,    16,    17,    18,    19,
    -770,     0,     0,     0,     0,    20,    21,    22,    23,    24,
      25,    26,     0,     0,    27,     0,     0,     0,     0,     0,
      28,    29,   262,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,  -770,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,    52,     0,     0,    53,    54,     0,
      55,    56,     0,    57,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,  -770,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    67,    68,    69,     0,     0,  -770,     0,
       5,     6,     7,  -770,     9,   519,     0,     0,    10,    11,
       0,     0,     0,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,     0,     0,     0,     0,    20,    21,    22,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,   210,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,   211,     0,     0,   212,
      54,     0,    55,    56,     0,   213,   214,   215,    58,    59,
     216,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     5,     6,     7,     0,     9,     0,     0,     0,    10,
      11,     0,     0,     0,    12,     0,    13,    14,    15,    16,
      17,    18,    19,     0,     0,    67,   217,    69,    20,    21,
      22,    23,    24,    25,    26,     0,     0,    27,     0,   242,
       0,     0,     0,     0,    29,     0,     0,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,   211,     0,     0,
     212,    54,     0,    55,    56,     0,     0,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,   129,   130,   131,   132,   133,   134,   135,   136,
     137,   138,   139,   140,   141,   142,   143,   144,   145,   146,
     147,   148,   149,   150,   151,   152,    67,    68,    69,   153,
     154,   155,   409,   410,   411,   412,   160,   161,   162,     0,
     242,     0,     0,     0,   163,   164,   165,   166,   413,   414,
     415,   416,   171,    37,    38,   417,    40,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   173,   174,   175,   176,   177,
     178,   179,   180,   181,     0,     0,   182,   183,     0,     0,
       0,     0,   184,   185,   186,   187,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   188,   189,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   190,   191,   192,
     193,   194,   195,   196,   197,   198,   199,     0,   200,   201,
       0,     0,     0,     0,     0,   202,   418,   129,   130,   131,
     132,   133,   134,   135,   136,   137,   138,   139,   140,   141,
     142,   143,   144,   145,   146,   147,   148,   149,   150,   151,
     152,     0,     0,     0,   153,   154,   155,   156,   157,   158,
     159,   160,   161,   162,     0,     0,     0,     0,     0,   163,
     164,   165,   166,   167,   168,   169,   170,   171,    37,    38,
     172,    40,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
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
       0,     0,     0,     0,   173,   174,   175,   176,   177,   178,
     179,   180,   181,     0,     0,   182,   183,     0,     0,     0,
       0,   184,   185,   186,   187,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   188,   189,     0,     0,
      59,     0,     0,     0,     0,     0,     0,     0,     0,     0,
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
       0,     0,     0,     0,     0,     0,     0,     0,   173,   174,
     175,   176,   177,   178,   179,   180,   181,     0,     0,   182,
     183,     0,     0,     0,     0,   184,   185,   186,   187,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     188,   189,     0,     0,    59,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     190,   191,   192,   193,   194,   195,   196,   197,   198,   199,
       0,   200,   201,     0,     0,     0,     0,     0,   202,   129,
     130,   131,   132,   133,   134,   135,   136,   137,   138,   139,
     140,   141,   142,   143,   144,   145,   146,   147,   148,   149,
     150,   151,   152,     0,     0,     0,   153,   154,   155,   156,
     157,   158,   159,   160,   161,   162,     0,     0,     0,     0,
       0,   163,   164,   165,   166,   167,   168,   169,   170,   171,
       0,     0,   172,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   173,   174,   175,   176,   177,   178,   179,   180,
     181,     0,     0,   182,   183,     0,     0,     0,     0,   184,
     185,   186,   187,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   188,   189,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,     5,     6,     7,
       0,     9,   202,     0,     0,    10,    11,     0,     0,     0,
      12,     0,    13,    14,    15,   250,   251,    18,    19,     0,
       0,     0,     0,     0,    20,   252,   253,    23,    24,    25,
      26,     0,     0,   209,     0,     0,     0,     0,     0,     0,
     281,     0,     0,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   282,     0,     0,   212,    54,     0,    55,
      56,     0,     0,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     5,     6,     7,     0,     9,
       0,     0,   283,    10,    11,     0,     0,     0,    12,   284,
      13,    14,    15,   250,   251,    18,    19,     0,     0,     0,
       0,     0,    20,   252,   253,    23,    24,    25,    26,     0,
       0,   209,     0,     0,     0,     0,     0,     0,   281,     0,
       0,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   282,     0,     0,   212,    54,     0,    55,    56,     0,
       0,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     4,     0,     5,     6,     7,     8,     9,     0,     0,
     283,    10,    11,     0,     0,     0,    12,   579,    13,    14,
      15,    16,    17,    18,    19,     0,     0,     0,     0,     0,
      20,    21,    22,    23,    24,    25,    26,     0,     0,    27,
       0,     0,     0,     0,     0,    28,    29,     0,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,    52,
       0,     0,    53,    54,     0,    55,    56,     0,    57,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,   380,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,     0,     0,     0,    12,     0,    13,
      14,    15,    16,    17,    18,    19,     0,     0,    67,    68,
      69,    20,    21,    22,    23,    24,    25,    26,     0,     0,
      27,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    50,    51,     0,     0,     0,     0,     0,     0,
     211,     0,     0,   212,    54,     0,    55,    56,     0,     0,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,     0,     0,     0,     0,     5,     6,     7,     0,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,    16,    17,    18,    19,     0,     0,    67,
      68,    69,    20,    21,    22,    23,    24,    25,    26,     0,
       0,   209,     0,     0,     0,     0,     0,     0,    29,     0,
       0,    32,    33,    34,    35,    36,    37,    38,    39,    40,
     210,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
     213,   214,   215,    58,    59,   216,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
       0,    13,    14,    15,    16,    17,    18,    19,     0,     0,
      67,   217,    69,    20,    21,    22,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,   210,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    50,   456,     0,     0,     0,     0,
       0,     0,   211,     0,     0,   212,    54,     0,    55,    56,
       0,   213,   214,   215,    58,    59,   216,    61,    62,    63,
      64,    65,    66,     0,     0,     0,     0,     5,     6,     7,
       0,     9,     0,     0,     0,    10,    11,     0,     0,     0,
      12,     0,    13,    14,    15,   250,   251,    18,    19,     0,
       0,    67,   217,    69,    20,   252,   253,    23,    24,    25,
      26,     0,     0,   209,     0,     0,     0,     0,     0,     0,
      29,     0,     0,    32,    33,    34,    35,    36,    37,    38,
      39,    40,   210,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,   211,     0,     0,   212,    54,     0,    55,
      56,     0,   666,   214,   215,    58,    59,   216,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,     0,     0,
       0,    12,     0,    13,    14,    15,   250,   251,    18,    19,
       0,     0,    67,   217,    69,    20,   252,   253,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,   210,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,   456,     0,     0,
       0,     0,     0,     0,   211,     0,     0,   212,    54,     0,
      55,    56,     0,   666,   214,   215,    58,    59,   216,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     5,
       6,     7,     0,     9,     0,     0,     0,    10,    11,     0,
       0,     0,    12,     0,    13,    14,    15,   250,   251,    18,
      19,     0,     0,    67,   217,    69,    20,   252,   253,    23,
      24,    25,    26,     0,     0,   209,     0,     0,     0,     0,
       0,     0,    29,     0,     0,    32,    33,    34,    35,    36,
      37,    38,    39,    40,   210,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,   213,   214,     0,    58,    59,   216,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
       0,     0,     0,    12,     0,    13,    14,    15,   250,   251,
      18,    19,     0,     0,    67,   217,    69,    20,   252,   253,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,   210,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,   211,     0,     0,   212,
      54,     0,    55,    56,     0,     0,   214,   215,    58,    59,
     216,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     5,     6,     7,     0,     9,     0,     0,     0,    10,
      11,     0,     0,     0,    12,     0,    13,    14,    15,   250,
     251,    18,    19,     0,     0,    67,   217,    69,    20,   252,
     253,    23,    24,    25,    26,     0,     0,   209,     0,     0,
       0,     0,     0,     0,    29,     0,     0,    32,    33,    34,
      35,    36,    37,    38,    39,    40,   210,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    48,    49,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    50,
      51,     0,     0,     0,     0,     0,     0,   211,     0,     0,
     212,    54,     0,    55,    56,     0,   666,   214,     0,    58,
      59,   216,    61,    62,    63,    64,    65,    66,     0,     0,
       0,     0,     5,     6,     7,     0,     9,     0,     0,     0,
      10,    11,     0,     0,     0,    12,     0,    13,    14,    15,
     250,   251,    18,    19,     0,     0,    67,   217,    69,    20,
     252,   253,    23,    24,    25,    26,     0,     0,   209,     0,
       0,     0,     0,     0,     0,    29,     0,     0,    32,    33,
      34,    35,    36,    37,    38,    39,    40,   210,    41,    42,
      43,    44,    45,    46,    47,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    48,    49,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      50,    51,     0,     0,     0,     0,     0,     0,   211,     0,
       0,   212,    54,     0,    55,    56,     0,     0,   214,     0,
      58,    59,   216,    61,    62,    63,    64,    65,    66,     0,
       0,     0,     0,     5,     6,     7,     0,     9,     0,     0,
       0,    10,    11,     0,     0,     0,    12,     0,    13,    14,
      15,    16,    17,    18,    19,     0,     0,    67,   217,    69,
      20,    21,    22,    23,    24,    25,    26,     0,     0,   209,
       0,     0,     0,     0,     0,     0,    29,     0,     0,    32,
      33,    34,    35,    36,    37,    38,    39,    40,     0,    41,
      42,    43,    44,    45,    46,    47,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    48,    49,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    50,    51,     0,     0,     0,     0,     0,     0,   211,
       0,     0,   212,    54,     0,    55,    56,     0,   759,     0,
       0,    58,    59,    60,    61,    62,    63,    64,    65,    66,
       0,     0,     0,     0,     5,     6,     7,     0,     9,     0,
       0,     0,    10,    11,     0,     0,     0,    12,     0,    13,
      14,    15,   250,   251,    18,    19,     0,     0,    67,   217,
      69,    20,   252,   253,    23,    24,    25,    26,     0,     0,
     209,     0,     0,     0,     0,     0,     0,    29,     0,     0,
      32,    33,    34,    35,    36,    37,    38,    39,    40,     0,
      41,    42,    43,    44,    45,    46,    47,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    48,    49,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    50,    51,     0,     0,     0,     0,     0,     0,
     211,     0,     0,   212,    54,     0,    55,    56,     0,   759,
       0,     0,    58,    59,    60,    61,    62,    63,    64,    65,
      66,     0,     0,     0,     0,     5,     6,     7,     0,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,   250,   251,    18,    19,     0,     0,    67,
     217,    69,    20,   252,   253,    23,    24,    25,    26,     0,
       0,   209,     0,     0,     0,     0,     0,     0,    29,     0,
       0,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    48,    49,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    50,    51,     0,     0,     0,     0,     0,
       0,   211,     0,     0,   212,    54,     0,    55,    56,     0,
     980,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
       0,    13,    14,    15,   250,   251,    18,    19,     0,     0,
      67,   217,    69,    20,   252,   253,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,    29,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    48,
      49,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    50,    51,     0,     0,     0,     0,
       0,     0,   211,     0,     0,   212,    54,     0,    55,    56,
       0,  1029,     0,     0,    58,    59,    60,    61,    62,    63,
      64,    65,    66,     0,     0,     0,     0,     5,     6,     7,
       0,     9,     0,     0,     0,    10,    11,     0,     0,     0,
      12,     0,    13,    14,    15,   250,   251,    18,    19,     0,
       0,    67,   217,    69,    20,   252,   253,    23,    24,    25,
      26,     0,     0,   209,     0,     0,     0,     0,     0,     0,
      29,     0,     0,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      48,    49,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    50,    51,     0,     0,     0,
       0,     0,     0,   211,     0,     0,   212,    54,     0,    55,
      56,     0,  1176,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,     0,     0,
       0,    12,     0,    13,    14,    15,   250,   251,    18,    19,
       0,     0,    67,   217,    69,    20,   252,   253,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,    29,     0,     0,    32,    33,    34,    35,    36,    37,
      38,    39,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    48,    49,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    50,    51,     0,     0,
       0,     0,     0,     0,   211,     0,     0,   212,    54,     0,
      55,    56,     0,     0,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     5,
       6,     7,     0,     9,     0,     0,     0,    10,    11,     0,
       0,     0,    12,     0,    13,    14,    15,    16,    17,    18,
      19,     0,     0,    67,   217,    69,    20,    21,    22,    23,
      24,    25,    26,     0,     0,   209,     0,     0,     0,     0,
       0,     0,    29,     0,     0,    32,    33,    34,    35,    36,
      37,    38,    39,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    48,    49,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    50,    51,     0,
       0,     0,     0,     0,     0,   211,     0,     0,   212,    54,
       0,    55,    56,     0,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
       0,     0,     0,    12,     0,    13,    14,    15,    16,    17,
      18,    19,     0,     0,    67,   217,    69,    20,    21,    22,
      23,    24,    25,    26,     0,     0,    27,     0,     0,     0,
       0,     0,     0,    29,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    48,    49,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    50,    51,
       0,     0,     0,     0,     0,     0,   211,     0,     0,   212,
      54,     0,    55,    56,     0,     0,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,     0,
       0,     5,     6,     7,     0,     9,     0,     0,     0,    10,
      11,     0,     0,     0,    12,     0,    13,    14,    15,   250,
     251,    18,    19,     0,     0,    67,    68,    69,    20,   252,
     253,    23,    24,    25,    26,     0,     0,   209,     0,     0,
       0,     0,     0,     0,   281,     0,     0,    32,    33,    34,
      35,    36,    37,    38,    39,    40,     0,    41,    42,    43,
      44,    45,    46,    47,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   282,     0,     0,
     340,    54,     0,    55,    56,     0,   341,     0,     0,    58,
      59,    60,    61,    62,    63,    64,    65,    66,     0,     0,
       5,     6,     7,     0,     9,     0,     0,     0,    10,    11,
       0,     0,     0,    12,     0,    13,    14,    15,   250,   251,
      18,    19,     0,     0,     0,     0,   283,    20,   252,   253,
      23,    24,    25,    26,     0,     0,   209,     0,     0,     0,
       0,     0,     0,   281,     0,     0,    32,    33,    34,    35,
      36,    37,    38,    39,    40,     0,    41,    42,    43,    44,
      45,    46,    47,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   390,     0,     0,    53,
      54,     0,    55,    56,     0,    57,     0,     0,    58,    59,
      60,    61,    62,    63,    64,    65,    66,     0,     0,     5,
       6,     7,     0,     9,     0,     0,     0,    10,    11,     0,
       0,     0,    12,     0,    13,    14,    15,   250,   251,    18,
      19,     0,     0,     0,     0,   283,    20,   252,   253,    23,
      24,    25,    26,     0,     0,   209,     0,     0,     0,     0,
       0,     0,   281,     0,     0,    32,    33,    34,   398,    36,
      37,    38,   399,    40,     0,    41,    42,    43,    44,    45,
      46,    47,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   400,     0,     0,     0,   401,     0,     0,   212,    54,
       0,    55,    56,     0,     0,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     5,     6,
       7,     0,     9,     0,     0,     0,    10,    11,     0,     0,
       0,    12,     0,    13,    14,    15,   250,   251,    18,    19,
       0,     0,     0,     0,   283,    20,   252,   253,    23,    24,
      25,    26,     0,     0,   209,     0,     0,     0,     0,     0,
       0,   281,     0,     0,    32,    33,    34,   398,    36,    37,
      38,   399,    40,     0,    41,    42,    43,    44,    45,    46,
      47,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   401,     0,     0,   212,    54,     0,
      55,    56,     0,     0,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     5,     6,     7,
       0,     9,     0,     0,     0,    10,    11,     0,     0,     0,
      12,     0,    13,    14,    15,   250,   251,    18,    19,     0,
       0,     0,     0,   283,    20,   252,   253,    23,    24,    25,
      26,     0,     0,   209,     0,     0,     0,     0,     0,     0,
     281,     0,     0,    32,    33,    34,    35,    36,    37,    38,
      39,    40,     0,    41,    42,    43,    44,    45,    46,    47,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   282,     0,     0,   340,    54,     0,    55,
      56,     0,     0,     0,     0,    58,    59,    60,    61,    62,
      63,    64,    65,    66,     0,     0,     5,     6,     7,     0,
       9,     0,     0,     0,    10,    11,     0,     0,     0,    12,
       0,    13,    14,    15,   250,   251,    18,    19,     0,     0,
       0,     0,   283,    20,   252,   253,    23,    24,    25,    26,
       0,     0,   209,     0,     0,     0,     0,     0,     0,   281,
       0,     0,    32,    33,    34,    35,    36,    37,    38,    39,
      40,     0,    41,    42,    43,    44,    45,    46,    47,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,  1165,     0,     0,   212,    54,     0,    55,    56,
       0,     0,     0,     0,    58,    59,    60,    61,    62,    63,
      64,    65,    66,     0,     0,     5,     6,     7,     0,     9,
       0,     0,     0,    10,    11,     0,     0,     0,    12,     0,
      13,    14,    15,   250,   251,    18,    19,     0,     0,     0,
       0,   283,    20,   252,   253,    23,    24,    25,    26,     0,
       0,   209,     0,     0,     0,     0,     0,     0,   281,     0,
       0,    32,    33,    34,    35,    36,    37,    38,    39,    40,
       0,    41,    42,    43,    44,    45,    46,    47,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,  1198,     0,     0,   212,    54,     0,    55,    56,    23,
      24,    25,    26,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,    32,    33,    34,   905,     0,
       0,     0,   906,     0,     0,    41,    42,    43,    44,    45,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     283,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   908,   909,     0,
       0,     0,     0,     0,     0,   910,     0,     0,   911,     0,
       0,   912,   913,     0,   914,     0,     0,    58,    59,    60,
      61,    62,    63,    64,    65,    66,     0,     0,     0,     0,
       0,     0,    23,    24,    25,    26,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   916,    32,    33,
      34,   905,     0,     0,   283,   906,     0,   907,    41,    42,
      43,    44,    45,     0,     0,     0,     0,     0,   242,     0,
       0,     0,     0,     0,     0,     0,     0,   534,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     908,   909,     0,     0,     0,     0,     0,     0,   910,     0,
       0,   911,     0,     0,   912,   913,     0,   914,   538,     0,
      58,    59,   915,    61,    62,    63,    64,    65,    66,     0,
       0,     0,     0,     0,     0,    23,    24,    25,    26,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     916,    32,    33,    34,   905,     0,     0,   283,   906,     0,
       0,    41,    42,    43,    44,    45,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   908,   909,     0,     0,     0,     0,     0,
       0,   910,     0,     0,   911,     0,     0,   912,   913,     0,
     914,     0,     0,    58,    59,    60,    61,    62,    63,    64,
      65,    66,     0,     0,     0,     0,     0,     0,    23,    24,
      25,    26,     0,     0,     0,   609,   610,     0,     0,   611,
       0,     0,     0,   916,    32,    33,    34,   905,     0,     0,
     283,   906,     0,     0,    41,    42,    43,    44,    45,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,   908,   909,     0,     0,
       0,   188,   189,     0,   910,     0,     0,   911,     0,     0,
     912,   913,     0,     0,     0,     0,    58,    59,    60,    61,
      62,    63,    64,    65,    66,     0,     0,     0,     0,     0,
       0,   190,   191,   192,   193,   194,   195,   196,   197,   198,
     199,     0,   200,   201,   619,   620,   916,     0,   621,   202,
     238,     0,     0,   283,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   173,   174,
     175,   176,   177,   178,   179,   180,   181,     0,     0,   182,
     183,     0,     0,     0,     0,   184,   185,   186,   187,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     188,   189,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     190,   191,   192,   193,   194,   195,   196,   197,   198,   199,
       0,   200,   201,   670,   610,     0,     0,   671,   202,   238,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   173,   174,   175,
     176,   177,   178,   179,   180,   181,     0,     0,   182,   183,
       0,     0,     0,     0,   184,   185,   186,   187,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   188,
     189,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   190,
     191,   192,   193,   194,   195,   196,   197,   198,   199,     0,
     200,   201,   673,   620,     0,     0,   674,   202,   238,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   173,   174,   175,   176,
     177,   178,   179,   180,   181,     0,     0,   182,   183,     0,
       0,     0,     0,   184,   185,   186,   187,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   188,   189,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   190,   191,
     192,   193,   194,   195,   196,   197,   198,   199,     0,   200,
     201,   700,   610,     0,     0,   701,   202,   238,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   173,   174,   175,   176,   177,
     178,   179,   180,   181,     0,     0,   182,   183,     0,     0,
       0,     0,   184,   185,   186,   187,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   188,   189,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   190,   191,   192,
     193,   194,   195,   196,   197,   198,   199,     0,   200,   201,
     703,   620,     0,     0,   704,   202,   238,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   173,   174,   175,   176,   177,   178,
     179,   180,   181,     0,     0,   182,   183,     0,     0,     0,
       0,   184,   185,   186,   187,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   188,   189,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   190,   191,   192,   193,
     194,   195,   196,   197,   198,   199,     0,   200,   201,   813,
     610,     0,     0,   814,   202,   238,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   173,   174,   175,   176,   177,   178,   179,
     180,   181,     0,     0,   182,   183,     0,     0,     0,     0,
     184,   185,   186,   187,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   188,   189,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   190,   191,   192,   193,   194,
     195,   196,   197,   198,   199,     0,   200,   201,   816,   620,
       0,     0,   817,   202,   238,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   173,   174,   175,   176,   177,   178,   179,   180,
     181,     0,     0,   182,   183,     0,     0,     0,     0,   184,
     185,   186,   187,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   188,   189,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   190,   191,   192,   193,   194,   195,
     196,   197,   198,   199,     0,   200,   201,   822,   610,     0,
       0,   823,   202,   238,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   173,   174,   175,   176,   177,   178,   179,   180,   181,
       0,     0,   182,   183,     0,     0,     0,     0,   184,   185,
     186,   187,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   188,   189,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   190,   191,   192,   193,   194,   195,   196,
     197,   198,   199,     0,   200,   201,   655,   620,     0,     0,
     656,   202,   238,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     173,   174,   175,   176,   177,   178,   179,   180,   181,     0,
       0,   182,   183,     0,     0,     0,     0,   184,   185,   186,
     187,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   188,   189,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   190,   191,   192,   193,   194,   195,   196,   197,
     198,   199,     0,   200,   201,  1035,   610,     0,     0,  1036,
     202,   238,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   173,
     174,   175,   176,   177,   178,   179,   180,   181,     0,     0,
     182,   183,     0,     0,     0,     0,   184,   185,   186,   187,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   188,   189,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   190,   191,   192,   193,   194,   195,   196,   197,   198,
     199,     0,   200,   201,  1038,   620,     0,     0,  1039,   202,
     238,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   173,   174,
     175,   176,   177,   178,   179,   180,   181,     0,     0,   182,
     183,     0,     0,     0,     0,   184,   185,   186,   187,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     188,   189,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     190,   191,   192,   193,   194,   195,   196,   197,   198,   199,
       0,   200,   201,  1259,   610,     0,     0,  1260,   202,   238,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   173,   174,   175,
     176,   177,   178,   179,   180,   181,     0,     0,   182,   183,
       0,     0,     0,     0,   184,   185,   186,   187,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   188,
     189,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   190,
     191,   192,   193,   194,   195,   196,   197,   198,   199,     0,
     200,   201,  1262,   620,     0,     0,  1263,   202,   238,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   173,   174,   175,   176,
     177,   178,   179,   180,   181,     0,     0,   182,   183,     0,
       0,     0,     0,   184,   185,   186,   187,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   188,   189,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   190,   191,
     192,   193,   194,   195,   196,   197,   198,   199,     0,   200,
     201,  1278,   610,     0,     0,  1279,   202,   238,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   173,   174,   175,   176,   177,
     178,   179,   180,   181,     0,     0,   182,   183,     0,     0,
       0,     0,   184,   185,   186,   187,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   188,   189,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   190,   191,   192,
     193,   194,   195,   196,   197,   198,   199,     0,   200,   201,
     655,   620,     0,     0,   656,   202,   238,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   173,   174,   175,   176,   177,   178,
     179,   180,   181,     0,     0,   182,   183,     0,     0,     0,
       0,   184,   185,   186,   187,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   188,   189,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   190,   191,   192,   193,
     194,   195,   196,   197,   198,   199,     0,   200,   201,     0,
       0,     0,     0,     0,   202
};

static const yytype_int16 yycheck[] =
{
       2,   105,    59,    59,     8,    60,    16,    17,   321,   401,
      85,   383,   325,    71,   554,    28,     2,   377,   801,    96,
      91,    16,    17,   325,    28,    53,    93,    94,   734,   232,
      97,   536,     8,   104,   376,   808,   378,    57,    22,    97,
     376,     2,   378,   565,   238,   101,   568,   321,    22,    16,
      17,   325,    28,    55,    56,    59,   450,   779,   731,   559,
      55,   561,    68,    53,    57,    85,   738,    53,    54,   868,
      94,    57,    71,    97,    76,    77,   778,   779,    98,    99,
     100,    16,    17,    97,   426,   870,   446,   275,    55,    56,
     426,   279,   102,    54,    67,   489,    80,   101,   499,    85,
     912,    25,    25,   445,    29,   447,    80,   102,  1047,   445,
     263,   447,    98,    99,   100,   101,   658,   659,    13,  1024,
     299,   300,  1061,   442,    25,   582,   908,   909,   650,   101,
     101,   450,   474,   475,    77,   102,    27,   100,  1202,   475,
      77,   501,   101,  1052,     0,   575,   778,   779,    10,  1047,
     110,    66,    69,    25,    66,   135,   227,    25,   500,    13,
     502,   216,  1071,  1072,    25,  1063,   502,   102,    34,  1181,
      34,    52,   135,   162,   241,    56,   243,   157,   579,    13,
     100,    98,   142,   155,   212,   256,    52,   340,    52,   157,
     731,   162,   155,   725,   162,   625,   155,   738,    13,   124,
      25,   631,   734,   633,    25,   120,    68,   265,   120,   284,
    1274,   135,  1117,   718,   719,   135,   218,   219,   289,   159,
      26,   249,   162,    13,   226,   235,   212,   237,   238,    25,
     232,   155,   155,   157,   158,   263,   238,    13,   162,    25,
     235,  1207,   237,   238,   152,   247,  1212,   314,   315,   316,
     317,   265,   319,   320,   155,   647,   157,  1269,   461,  1071,
    1072,   247,   157,   249,   284,   160,   265,   162,   235,   160,
     237,   162,    13,   263,  1213,    26,   219,   263,   638,   253,
     254,   218,   219,   155,  1066,  1067,   247,   155,   648,   683,
     314,   315,   316,   317,   155,   637,   157,   639,   284,   821,
     235,   155,   237,   639,    13,   647,   160,   649,   162,  1207,
     283,  1110,   340,   649,  1212,  1213,   746,    27,   385,    29,
     750,   341,   393,   157,  1109,    25,   160,   313,   162,   387,
     155,    13,   318,   400,   155,   514,   338,   516,  1010,   796,
     880,   343,  1015,   286,    78,   160,   152,   162,   341,   152,
     340,   157,   313,   582,   340,   341,   384,   318,   386,   155,
     784,   385,   681,   868,   683,   870,   767,   377,   792,   155,
     160,  1093,   162,   387,   378,   377,   876,   877,   672,   809,
      13,   881,   812,   883,   160,   885,   162,    13,  1286,    37,
      38,  1093,   705,   379,    69,  1178,   826,    58,   384,   100,
     386,   152,   100,   705,   378,  1178,   100,    56,   702,   951,
     952,   145,   146,   147,   956,   957,    25,    78,   731,   160,
    1052,   162,   426,    98,   499,   738,    98,   429,   430,   658,
     659,   705,  1064,   161,   135,   135,   446,   135,   440,  1071,
    1072,   135,   100,   447,   446,   161,   448,   449,   109,   110,
     135,   160,    58,   162,   155,   155,   469,   459,   158,   461,
      28,  1093,   162,   155,   986,   469,   669,   153,   787,  1010,
     155,   475,    78,   447,   476,   442,   162,   135,   160,   499,
     162,   142,   154,  1024,   241,   157,   113,   681,   734,   100,
     100,   501,  1024,   469,  1200,   481,    25,   155,   502,   501,
     160,   475,   162,   109,   579,  1288,  1028,   113,   566,   697,
    1024,   268,   100,   499,   162,   272,    66,    67,   910,   486,
     161,   815,  1195,   161,   135,   135,   135,   160,   502,   162,
     709,   825,   778,   779,   160,   714,   162,  1079,    66,   852,
     542,   135,   972,   903,   155,   975,   155,   135,   978,   158,
     852,   100,   554,   162,   564,   985,   100,   567,   988,   579,
     902,   100,   904,   640,   274,   275,   902,   155,   904,   279,
     576,   281,   122,   123,   632,  1115,   582,   135,   852,   565,
     100,   576,   568,  1123,   651,   100,   135,   582,    37,    38,
     100,   135,    66,   579,   122,   123,   135,   125,    69,   100,
     628,    66,   630,    69,  1109,  1110,   135,  1001,  1281,   100,
     982,  1111,  1112,  1113,  1114,   135,   618,    52,   912,   686,
     135,    56,   624,   698,   626,   135,   155,    98,   638,   158,
      69,   615,    98,   162,   135,   639,   638,    69,   648,   100,
     624,   615,   628,   846,   630,   649,   648,   660,   122,   123,
     624,    69,   658,   659,  1195,   657,   660,   122,   123,    98,
    1182,  1202,   155,  1195,   650,   639,    98,   669,  1200,  1099,
    1202,     9,  1204,   657,   135,   649,  1068,    15,   698,    97,
      98,  1195,  1001,   657,   660,   157,  1200,   161,  1202,   691,
    1204,    69,   767,    56,   155,   452,   161,  1010,   135,   100,
     457,    69,    69,   460,   101,   776,   463,   155,  1125,  1126,
    1250,  1024,   698,  1085,   681,   100,   690,   691,  1090,    97,
      98,   478,   951,   952,   155,   829,   483,   956,   957,    69,
      98,    98,   157,  1274,   135,   778,   779,    14,    15,    69,
      15,  1241,  1274,  1037,  1276,    69,   157,   767,    69,  1281,
      25,  1283,  1046,   820,   155,  1049,   155,    97,    98,   157,
    1274,   819,  1276,   162,   155,    78,  1298,  1281,    98,  1283,
     158,   162,   828,    97,    98,  1069,   154,    98,   135,   781,
     782,   767,   155,    15,  1298,    17,   788,   789,   545,   162,
     818,   153,   778,   779,   796,   797,   820,   799,   159,   801,
      56,   796,  1174,   157,  1033,    54,  1052,    52,  1238,    54,
      55,    56,    57,    58,   154,    64,    65,   157,  1064,   155,
     787,   578,   107,    69,   828,  1071,  1072,    69,  1245,  1246,
     154,    78,   818,    78,  1251,   821,  1253,  1254,   840,   841,
     135,   843,   844,    69,   846,    89,    90,  1093,   135,    78,
    1079,    97,    98,   135,   158,    97,    98,   102,   162,   916,
     915,   155,  1222,   160,   109,   110,  1160,  1161,  1162,   912,
      56,    97,    98,  1290,  1291,  1292,  1293,    26,   880,  1221,
     139,  1223,  1195,   135,  1061,  1302,   929,  1223,   598,  1202,
     892,   158,   894,   903,  1071,  1072,   135,   142,   900,    25,
     904,   903,    83,    84,   971,   155,  1248,   617,   154,    26,
     153,   897,   154,   899,   143,   144,   145,   146,   147,     2,
      69,    66,   908,   909,    40,    41,   912,    69,   154,   987,
     904,   155,  1024,    16,    17,    69,   897,   965,   899,   153,
      69,   135,   970,   929,   155,   951,   952,   971,    97,    98,
     956,   957,    69,   955,  1200,    97,    98,   138,   139,   961,
     155,  1274,   672,    97,    98,   722,   343,  1261,    97,    98,
      53,    54,   155,   738,    57,   120,   741,   122,   123,   965,
      97,    98,   285,   286,   970,    68,   155,   697,   155,   155,
      52,  1058,   702,   155,    52,   997,   153,  1025,     8,   155,
     986,  1044,    85,   152,    69,   154,    13,   155,   157,    25,
      93,    94,   154,   135,    97,    98,    99,   100,  1061,   102,
     154,    17,    26,   161,   161,   154,    44,  1033,  1071,  1072,
     155,     2,    97,    98,  1058,   152,   155,   154,    44,  1025,
     157,   153,  1028,    44,   155,    16,    17,   155,   135,    44,
    1093,   137,   429,   430,    52,   159,    54,    55,  1044,    57,
    1088,  1047,    15,   440,  1084,    69,   155,   155,    52,  1097,
     827,   448,   449,  1079,   155,  1061,  1062,  1063,  1106,   155,
    1066,  1067,    53,    54,  1139,  1071,  1072,   155,   845,   154,
     847,   140,   155,    97,    98,    52,  1167,    68,  1084,   476,
     140,  1121,  1088,  1195,   102,   815,   153,  1093,    69,   866,
    1202,  1097,  1204,  1115,    52,   825,    54,    55,    56,    57,
    1106,  1123,    93,    94,   155,   155,    97,   101,   155,   212,
     155,   102,   155,   155,    52,  1121,    97,    98,    66,   101,
      69,   158,   155,  1171,    69,  1173,   160,   140,   152,    56,
     154,     9,   235,   157,   237,   238,  1184,   140,   241,   153,
     243,  1228,  1229,   155,   247,   155,   249,   155,    97,    98,
    1172,    69,    97,    98,  1194,   155,  1178,   155,   155,   155,
     263,    56,  1274,   155,  1276,  1171,    69,  1173,   155,  1281,
     121,  1283,   120,   154,   122,   123,  1182,   125,  1184,    97,
      98,   284,   912,   913,  1228,  1229,  1298,   155,  1194,   155,
     155,   155,  1222,   155,    97,    98,   157,   157,   155,  1223,
    1222,  1207,  1224,   247,  1226,   154,  1212,  1213,    66,   154,
     313,   314,   315,   316,   317,   318,   319,   320,  1266,   313,
     477,   212,   828,    54,    55,  1010,    57,  1012,  1250,  1223,
     481,    98,  1017,    64,    65,   100,   154,   340,   341,  1024,
      89,   657,    66,   797,   235,   718,   237,   238,  1226,   868,
     241,   154,   243,  1119,  1024,    66,   247,   894,   249,   830,
    1266,    66,   120,   571,   122,   123,  1288,   125,    59,    60,
      61,    62,   263,  1003,   377,   336,   379,   778,   779,    66,
    1286,   384,   385,   386,    52,    78,    54,    55,    56,    57,
      58,  1288,  1269,  1070,  1024,  1062,   120,   400,   122,   123,
    1064,  1064,    95,    96,  1060,  1224,   108,  1037,  1172,   120,
      78,   122,   123,   658,   659,   120,  1046,   122,   123,  1049,
     778,   779,   313,   314,   315,   316,   317,   318,   319,   320,
     675,   676,   516,   120,   102,   122,   123,   101,   734,  1069,
     108,   109,   110,   446,  1200,  1195,   731,   692,  1140,   340,
     143,   144,   145,   146,   147,  1132,    -1,    52,    78,    54,
      55,    56,    57,    -1,    -1,    52,  1143,    54,    55,    56,
      57,    -1,    -1,    -1,   142,    95,    96,   145,   481,    -1,
    1157,  1158,  1159,    -1,   781,   782,   377,  1117,   379,   778,
     779,   788,   789,   384,   385,   386,   499,    -1,   501,    -1,
      -1,    -1,  1187,    -1,    -1,    -1,    -1,   908,   909,   400,
    1140,   912,    -1,    -1,    -1,   102,    78,  1202,    -1,  1204,
     140,   141,   142,   143,   144,   145,   146,   147,   929,    -1,
    1160,  1161,  1162,    95,    96,    -1,    52,    -1,    54,    55,
      56,    57,    -1,   840,   841,    -1,   843,   844,    -1,    -1,
     908,   909,    -1,    -1,   912,   446,    40,    41,    42,    43,
      44,   564,   565,    -1,   567,   568,    -1,    -1,    -1,    -1,
      -1,   929,    -1,   576,    -1,    -1,   579,    -1,    -1,   582,
     142,   143,   144,   145,   146,   147,   102,    -1,    -1,  1274,
     481,  1276,   108,    -1,   778,   779,    -1,    -1,  1283,    -1,
      -1,    -1,    -1,   900,    -1,  1235,    -1,    -1,    -1,    -1,
     501,    -1,    -1,  1298,    -1,    -1,    -1,    -1,    -1,   908,
     909,    -1,    -1,   912,    -1,   628,    -1,   630,    -1,    -1,
      -1,  1261,    -1,    -1,    -1,   638,    -1,    -1,    -1,    -1,
     929,    -1,    -1,  1044,    -1,   648,  1047,   650,   651,    -1,
      -1,    -1,    -1,    -1,    -1,   658,   659,    -1,   955,    -1,
    1061,  1062,  1063,    -1,    -1,  1066,  1067,    -1,    -1,    -1,
    1071,  1072,    -1,   564,   565,    -1,   567,   568,    -1,    -1,
      -1,    -1,    -1,   686,    -1,   576,  1044,    -1,    -1,  1047,
      -1,   582,  1093,    -1,    -1,   698,    -1,    -1,    -1,    -1,
     997,    -1,    -1,  1061,  1062,  1063,   951,   952,  1066,  1067,
      -1,   956,   957,  1071,  1072,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   908,   909,    -1,    -1,   912,    -1,
      -1,    -1,    -1,    -1,    -1,  1093,    -1,   628,    -1,   630,
      -1,    -1,    -1,    -1,    -1,   929,    -1,   638,    -1,    -1,
     995,   996,    -1,   998,   999,  1044,    -1,   648,  1047,   650,
     651,    -1,    -1,    -1,   767,    -1,    -1,   658,   659,    -1,
      -1,    -1,  1061,  1062,  1063,    -1,    -1,  1066,  1067,    -1,
      -1,    -1,  1071,  1072,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   686,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1093,    -1,  1207,    -1,    -1,    -1,
      -1,  1212,  1213,    -1,    -1,   818,    -1,   820,   821,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,  1079,    -1,    -1,    -1,    -1,    -1,
      -1,    52,    -1,    54,    55,    56,    57,    58,    -1,  1207,
      -1,    -1,    -1,    -1,  1212,  1213,    -1,  1102,    -1,    -1,
    1044,    -1,    -1,  1047,    -1,    -1,    -1,    78,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1061,  1062,  1063,
      -1,    92,  1066,  1067,    -1,  1286,    -1,  1071,  1072,    -1,
      -1,   102,    -1,    -1,   897,    -1,   899,    -1,   109,   110,
     903,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1093,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1207,     2,
      -1,    -1,    -1,  1212,  1213,    -1,    -1,   818,  1286,   820,
     821,   142,    -1,    16,    17,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   951,   952,
      -1,    -1,    -1,   956,   957,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   965,    -1,    -1,    -1,    -1,   970,   971,    -1,
      53,    54,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   986,    -1,    68,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     2,    -1,    -1,  1286,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   897,    -1,   899,    -1,
      93,    94,   903,    -1,    97,    -1,    -1,    -1,    -1,   102,
      -1,    -1,  1025,  1207,    -1,  1028,    -1,    -1,  1212,  1213,
    1033,   778,   779,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    33,    34,    35,    36,    53,    54,    -1,    -1,    57,
      -1,    -1,    -1,    -1,    -1,  1058,    -1,    49,    50,    51,
     951,   952,    -1,    -1,    -1,   956,   957,    59,    60,    61,
      62,    63,    -1,    -1,   965,    -1,  1079,    85,    -1,   970,
     971,  1084,    -1,    -1,    -1,  1088,    -1,    -1,    -1,    -1,
      98,    99,   100,    -1,  1097,   986,    -1,    -1,    -1,    -1,
      -1,    -1,  1286,  1106,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1121,   111,
     112,   113,   114,   115,   116,   117,   118,   119,    -1,   212,
      -1,    -1,    -1,    -1,  1025,    -1,    -1,  1028,    -1,    -1,
      -1,    -1,  1033,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      56,    -1,   235,    -1,   237,   238,   148,    -1,   241,    -1,
     243,   908,   909,    -1,   247,   912,   249,  1058,  1171,    -1,
    1173,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1182,
     263,  1184,   929,    -1,    -1,    -1,    -1,    -1,  1079,    -1,
      -1,  1194,    -1,    -1,    -1,    -1,    52,  1088,    54,    55,
      56,    57,    58,    -1,   212,    -1,  1097,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,  1106,    -1,    -1,    -1,  1222,
      -1,    -1,    78,    -1,    -1,  1228,  1229,    -1,   778,   779,
     313,   314,   315,   316,   317,   318,   319,   320,    -1,   247,
      -1,   249,    -1,    -1,    -1,    -1,   102,    -1,    -1,    -1,
      -1,    -1,   108,   109,   110,   263,    -1,   340,    -1,    -1,
      -1,    -1,    -1,  1266,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   284,    -1,    -1,    -1,
    1171,    -1,  1173,    -1,    -1,    -1,   142,    -1,    -1,   145,
      -1,  1182,    -1,  1184,   377,    -1,   379,  1044,    -1,    -1,
    1047,   384,   385,   386,   210,   313,   162,   213,   214,   215,
     318,    -1,    -1,    -1,  1061,  1062,  1063,   400,    -1,  1066,
    1067,    -1,    -1,    -1,  1071,  1072,    -1,    -1,    -1,    25,
      -1,  1222,   340,   341,    -1,    -1,    -1,  1228,  1229,    -1,
      -1,    -1,    -1,    -1,   778,   779,  1093,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   908,   909,
      -1,    -1,   912,   446,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   379,    -1,    -1,    -1,  1266,   384,    -1,   386,   929,
      -1,    -1,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    90,    -1,    -1,    -1,   481,    95,
      96,    -1,    -1,    -1,    -1,   101,    -1,     2,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   501,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      52,    -1,    54,    55,    56,    57,    58,    -1,    -1,    -1,
     136,    -1,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,    -1,    -1,    -1,    -1,    78,    -1,    53,    54,
    1207,    -1,    57,    -1,    -1,  1212,  1213,    -1,    -1,    -1,
      92,    -1,    -1,   481,   908,   909,    -1,    -1,   912,    -1,
     102,   564,   565,    -1,   567,   568,   108,   109,   110,    -1,
      85,   499,    -1,   576,  1044,   929,    -1,  1047,    -1,   582,
      -1,    -1,    -1,    98,    99,   100,    -1,    -1,    -1,    -1,
      -1,  1061,  1062,  1063,    -1,    -1,  1066,  1067,    -1,    -1,
     142,  1071,  1072,   145,    78,    79,    80,    81,    82,    83,
      84,    85,    -1,    87,    88,   441,   442,    -1,    -1,  1286,
      -1,    95,    96,  1093,   450,   628,    -1,   630,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   638,    -1,   565,    -1,    -1,
     568,    -1,    -1,    -1,    -1,   648,    -1,   650,   651,    -1,
      -1,   579,    -1,    -1,   582,   658,   659,   778,   779,    -1,
     486,    -1,    -1,   489,   138,   139,   140,   141,   142,   143,
     144,   145,   146,   147,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   686,    -1,    -1,    -1,    -1,    -1,    -1,
    1044,    -1,    -1,  1047,    -1,    -1,    -1,   212,    -1,    -1,
     628,    -1,   630,    -1,    -1,    -1,    -1,  1061,  1062,  1063,
      -1,    -1,  1066,  1067,    -1,    -1,    -1,  1071,  1072,    -1,
      -1,    -1,   650,    -1,    -1,    -1,   552,    -1,    -1,    -1,
     658,   659,   247,    -1,   249,    -1,    -1,  1207,    -1,  1093,
      -1,    -1,  1212,  1213,    -1,    -1,    -1,    -1,   263,     2,
     576,   778,   779,    -1,    -1,    -1,   582,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   284,
     698,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   707,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   908,   909,    -1,
      -1,   912,    -1,    -1,    -1,    -1,    -1,    -1,   313,    -1,
      53,    54,    -1,   318,    57,    -1,    -1,    -1,   929,    -1,
      -1,    -1,    -1,    -1,    -1,   818,  1286,   820,   821,    -1,
      -1,    -1,    -1,    -1,    -1,   340,   341,    -1,    -1,    -1,
      -1,    -1,    85,    -1,    -1,   661,    -1,    -1,    -1,   767,
     666,    -1,    -1,    -1,    -1,    98,    99,   100,    -1,    -1,
      -1,    -1,    -1,  1207,    -1,   681,    -1,   683,  1212,  1213,
      -1,    -1,    -1,    -1,   379,    -1,    -1,    -1,    -1,   384,
      -1,   386,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   908,   909,    -1,    -1,   912,    -1,    -1,    -1,    -1,
     818,    -1,    -1,   821,   897,    -1,   899,    -1,    -1,    -1,
     903,    -1,   929,   729,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1044,    -1,    -1,  1047,    -1,    -1,    -1,
      -1,    -1,  1286,   759,    -1,    -1,    -1,    -1,    -1,    -1,
    1061,  1062,  1063,    -1,    -1,  1066,  1067,    -1,   951,   952,
    1071,  1072,    -1,   956,   957,    -1,    -1,    -1,    -1,   212,
      -1,   787,   965,    -1,    -1,    -1,   481,   970,   971,   897,
     796,   899,  1093,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   986,   499,   811,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   247,    -1,   249,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     263,    -1,    -1,    -1,    -1,    -1,    -1,  1044,    -1,    -1,
    1047,    -1,  1025,   951,   952,  1028,     2,    -1,   956,   957,
    1033,   284,    -1,    -1,  1061,  1062,  1063,   965,    -1,  1066,
    1067,    -1,   970,    -1,  1071,  1072,    -1,    -1,    -1,    -1,
     565,    -1,    -1,   568,    -1,  1058,    -1,    -1,   986,    -1,
     313,    -1,    -1,    -1,   579,   318,  1093,   582,    -1,    -1,
     896,    -1,    -1,    -1,    -1,    -1,  1079,    53,    54,    -1,
      -1,    57,    -1,    -1,    -1,  1088,  1207,   340,   341,    -1,
      -1,  1212,  1213,    -1,  1097,    -1,    -1,  1025,    -1,    -1,
    1028,    -1,    -1,  1106,    -1,  1033,    -1,    -1,    -1,    85,
      -1,    -1,    -1,   628,    -1,   630,    -1,    -1,    -1,    -1,
      -1,    -1,    98,    99,   100,   101,   379,    -1,    -1,    -1,
      -1,   384,    -1,   386,    -1,   650,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   658,   659,    -1,    -1,    -1,     2,    -1,
      -1,  1079,    -1,    -1,   980,    -1,  1084,    -1,    -1,    -1,
    1088,    -1,    -1,    -1,    -1,  1286,    -1,    -1,  1171,  1097,
    1173,    -1,    -1,    -1,    -1,  1001,    -1,    -1,  1106,  1182,
    1207,  1184,    -1,   698,    -1,  1212,  1213,    -1,    -1,    -1,
      -1,    -1,    52,  1121,    54,    55,    56,    57,    58,    53,
      54,    -1,    -1,  1029,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    78,  1222,
      -1,    -1,    -1,     2,    -1,  1228,  1229,    -1,   481,    -1,
      -1,    -1,    92,    -1,    -1,    -1,   212,    -1,    -1,    -1,
      -1,    -1,   102,  1171,    98,  1173,   499,    -1,   108,   109,
     110,    -1,   767,    -1,  1182,    -1,  1184,    -1,    -1,  1286,
      -1,    -1,    -1,  1266,    -1,    -1,  1194,    -1,    -1,    -1,
    1096,   247,    -1,   249,    53,    54,    -1,    -1,    -1,    -1,
      -1,    -1,   142,    -1,    -1,   145,    -1,   263,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   157,    -1,    -1,
      -1,    -1,    -1,   818,    -1,    -1,   821,    -1,   284,    -1,
      -1,    -1,   565,    -1,    -1,   568,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   579,    -1,    -1,   582,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   313,  1266,    -1,
      -1,    -1,   318,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1176,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   212,    -1,
      -1,    -1,    -1,    -1,   340,   341,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   628,    -1,   630,    -1,    -1,
      44,    -1,   897,    -1,   899,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   247,    -1,   249,    -1,   650,    -1,    -1,
      -1,    -1,    -1,   379,    -1,   658,   659,    -1,   384,   263,
     386,    -1,    -1,    -1,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    90,    -1,    -1,    -1,
      -1,    95,    96,   212,    -1,    -1,   951,   952,    -1,    -1,
      -1,   956,   957,    -1,    -1,   698,    -1,    -1,    -1,    -1,
     965,    -1,    -1,    -1,    -1,   970,    -1,    -1,    -1,   313,
      -1,    -1,    -1,    -1,   318,    -1,    -1,    -1,   247,    -1,
     249,   986,   136,    -1,   138,   139,   140,   141,   142,   143,
     144,   145,   146,   147,   263,    -1,   340,    -1,    -1,    -1,
      -1,   155,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   481,    -1,    -1,    -1,    -1,
    1025,    -1,    -1,  1028,   767,    -1,    -1,    -1,  1033,    -1,
      -1,    -1,    -1,   499,    -1,   379,    -1,    -1,    -1,    -1,
     384,    -1,   386,    -1,   313,    -1,    -1,    -1,    -1,   318,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    78,
      79,    80,    81,    82,    83,    84,    -1,    -1,    87,    88,
      -1,   340,    -1,    -1,  1079,   818,    95,    96,   821,  1084,
      -1,    -1,    -1,  1088,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,  1097,    -1,    -1,    -1,    -1,    -1,    -1,   565,
      -1,  1106,   568,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     379,    -1,    -1,   579,    -1,   384,  1121,   386,    -1,   138,
     139,   140,   141,   142,   143,   144,   145,   146,   147,    -1,
      78,    79,    80,    81,    82,    83,    84,   481,    -1,    87,
      88,    -1,    -1,    -1,    -1,    -1,    -1,    95,    96,    -1,
      -1,    -1,    -1,    -1,   897,    -1,   899,    -1,    -1,    -1,
      -1,    -1,   628,    -1,   630,    -1,  1171,    -1,  1173,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1182,    -1,  1184,
      -1,    -1,    -1,    -1,   650,    -1,    -1,    -1,    -1,  1194,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   951,   952,
      -1,    -1,   481,   956,   957,    -1,    -1,    -1,    -1,    -1,
      -1,   565,   965,    -1,   568,    -1,    -1,   970,    44,    -1,
      -1,    -1,   698,    -1,    -1,    -1,    -1,    -1,   582,    -1,
      -1,    -1,    -1,   986,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,  1266,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    90,    -1,    -1,    -1,    -1,    95,
      96,    -1,  1025,    -1,   628,  1028,   630,    -1,    -1,    -1,
    1033,    -1,    -1,    -1,    -1,    -1,   565,    -1,    -1,   568,
      -1,   767,    -1,    -1,    -1,    -1,   650,    -1,    -1,    -1,
      -1,    -1,    -1,   582,   658,   659,    -1,    -1,    -1,    -1,
     136,    -1,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,    -1,    -1,    -1,    -1,  1079,    -1,    -1,    -1,
      -1,  1084,    -1,    -1,    -1,  1088,    -1,    -1,    -1,    -1,
      -1,    -1,   818,    -1,  1097,   821,    -1,     0,    -1,   628,
      -1,   630,    -1,  1106,    -1,     8,     9,    10,    44,    -1,
      13,    14,    15,    -1,    17,    -1,    -1,    -1,  1121,    -1,
      -1,   650,    25,    26,    27,    -1,    -1,    -1,    -1,   658,
     659,    -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,
      43,    44,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    90,    -1,    -1,    -1,    -1,    95,
      96,    -1,    -1,    -1,    -1,    68,    69,    -1,  1171,    -1,
    1173,   897,    -1,   899,    -1,    -1,    -1,    -1,    -1,  1182,
      -1,  1184,    -1,    -1,    -1,    44,    -1,    -1,    -1,    -1,
      -1,  1194,    -1,    -1,    97,    98,    -1,    -1,    -1,    -1,
     136,    -1,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,    -1,    -1,   818,    -1,    -1,   821,   121,    78,
      79,    80,    81,    82,    83,    84,    85,    86,    87,    88,
      89,    90,    -1,    -1,    -1,    -1,    95,    96,    -1,   965,
      -1,    -1,    -1,    -1,   970,    -1,    -1,    -1,    -1,   152,
     153,    -1,    -1,    -1,   157,   158,    -1,   160,    -1,   162,
     986,    -1,    -1,  1266,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    16,    17,    -1,    -1,   136,    -1,   138,
     139,   140,   141,   142,   143,   144,   145,   146,   147,   818,
      -1,    -1,   821,   897,    -1,   899,    -1,    -1,    -1,  1025,
      -1,    -1,  1028,    -1,    -1,    48,    49,    50,    51,    -1,
      -1,    -1,    55,    56,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    68,    69,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   951,   952,    -1,
      -1,    -1,   956,   957,    -1,    -1,    -1,    -1,  1084,   102,
      -1,   965,  1088,    -1,    -1,    -1,   970,    -1,   897,    -1,
     899,  1097,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1106,    -1,   986,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,  1121,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,  1025,   951,   952,  1028,    -1,    -1,   956,   957,  1033,
      -1,    -1,    -1,    -1,    -1,    -1,   965,    -1,    -1,    -1,
      -1,   970,    -1,    -1,    -1,  1171,    -1,  1173,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1182,   986,  1184,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   210,  1194,    -1,
     213,   214,   215,    -1,   217,  1079,    -1,    -1,    -1,    -1,
    1084,    -1,    -1,    -1,  1088,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   235,  1097,   237,   238,  1025,    -1,    -1,  1028,
      -1,    -1,  1106,    -1,  1033,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    90,    -1,    -1,    -1,
    1266,    95,    96,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    1079,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1088,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1171,  1097,  1173,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1106,  1182,    -1,
    1184,    -1,   136,    -1,   138,   139,   140,   141,   142,   143,
     144,   145,   146,   147,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   345,   346,   347,   348,   349,    -1,   162,   352,
     353,   354,   355,   356,   357,   358,   359,    -1,   361,    -1,
      -1,   364,   365,   366,   367,   368,   369,   370,   371,   372,
     373,    -1,    -1,    -1,   377,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,  1171,    -1,  1173,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1182,    -1,  1184,    -1,    -1,    -1,    -1,
      -1,    -1,  1266,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   441,   442,
      -1,    -1,    -1,   446,    -1,    -1,    -1,   450,    -1,    -1,
      -1,    -1,    -1,   456,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   471,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,  1266,    -1,    -1,
      -1,    -1,    -1,   486,    -1,    -1,   489,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   501,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   518,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,     0,     1,    -1,     3,     4,
       5,     6,     7,    -1,    -1,    -1,    11,    12,    -1,   552,
      -1,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,   564,    -1,    -1,   567,    30,    31,    32,    33,    34,
      35,    36,    -1,   576,    39,    -1,    -1,    -1,    -1,   582,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    76,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,
      -1,    -1,    -1,    -1,    99,   638,    -1,   102,   103,    -1,
     105,   106,    -1,   108,    -1,   648,   111,   112,   113,   114,
     115,   116,   117,   118,   119,   658,   659,    -1,   661,   662,
     663,   664,    -1,   666,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   675,   676,    -1,    -1,    -1,    -1,   681,    -1,
     683,    -1,    -1,   148,   149,   150,    -1,    -1,    -1,   692,
       0,    -1,    -1,    -1,    -1,   160,    -1,   162,     8,     9,
      10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    25,    26,    27,    28,    29,
      -1,    -1,    -1,    -1,    -1,    -1,   729,    37,    38,    -1,
      40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   759,    -1,    68,    69,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    78,    79,
      80,    81,    82,    83,    84,    85,    86,    87,    88,    89,
      90,    -1,    -1,    -1,   787,    95,    96,    97,    98,    -1,
     100,   101,    -1,   796,    -1,    -1,    -1,   107,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   811,    -1,
      -1,   121,    -1,    -1,   124,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   135,   136,   137,   138,   139,
     140,   141,   142,   143,   144,   145,   146,   147,    -1,    -1,
      -1,    -1,   152,   153,   154,   155,    -1,    -1,   158,   159,
     160,    -1,   162,    78,    79,    80,    81,    82,    83,    84,
      85,    86,    87,    88,    89,    90,    -1,    -1,    -1,    -1,
      95,    96,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   886,   887,    -1,   889,   890,    -1,    -1,
      -1,    -1,    -1,   896,    -1,    -1,    -1,    -1,   901,    -1,
     903,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   136,    -1,   138,   139,   140,   141,   142,   143,   144,
     145,   146,   147,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   950,   951,   952,
      -1,    -1,    -1,   956,   957,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     0,    -1,    -1,   980,    -1,    -1,
      -1,    -1,     8,     9,    10,    -1,    -1,    13,    14,    15,
      -1,    17,   995,   996,    -1,   998,   999,    -1,  1001,    25,
      -1,    27,    28,    29,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    37,    38,    -1,    40,    41,    42,    43,    44,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,  1029,    -1,    -1,    -1,
    1033,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    68,    69,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    90,    -1,    -1,    -1,    -1,    95,
      96,    97,    98,    -1,   100,   101,  1079,    -1,    -1,    -1,
      -1,   107,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,  1096,    -1,   121,    -1,    -1,   124,  1102,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   135,
     136,   137,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,    -1,    -1,    -1,    -1,    -1,   153,   154,   155,
      -1,    -1,   158,   159,   160,    -1,   162,    -1,    -1,    -1,
      -1,    -1,     0,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
       8,     9,    10,    -1,    -1,    13,    14,    15,    -1,    17,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,    -1,    27,
      28,    29,    -1,  1176,    -1,    -1,    -1,    -1,    -1,    37,
      38,    -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    90,    -1,    -1,    -1,    -1,    95,    96,    -1,
      68,    69,    -1,    -1,    -1,    -1,    -1,    -1,    -1,  1222,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    90,    -1,    -1,    -1,    -1,    95,    96,    97,
      98,    -1,   100,   101,    -1,    -1,    -1,    -1,    -1,   107,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
      -1,    -1,    -1,   121,    -1,    -1,   124,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   135,   136,   137,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
      -1,    -1,    -1,    -1,    -1,   153,   154,   155,     0,    -1,
     158,   159,   160,    -1,   162,    -1,     8,     9,    10,    -1,
      -1,    13,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    25,    26,    27,    28,    29,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,
      42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    68,    69,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    90,    -1,
      -1,    -1,    -1,    95,    96,    97,    98,    -1,    -1,   101,
      -1,    -1,    -1,    -1,    -1,   107,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   121,
      -1,    -1,   124,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   136,   137,   138,   139,   140,   141,
     142,   143,   144,   145,   146,   147,    -1,    -1,    -1,    -1,
     152,   153,   154,   155,     0,    -1,   158,   159,   160,    -1,
     162,    -1,     8,     9,    10,    -1,    -1,    13,    14,    15,
      -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,
      26,    27,    28,    29,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    37,    38,    -1,    40,    41,    42,    43,    44,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    68,    69,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    90,    -1,    -1,    -1,    -1,    95,
      96,    97,    98,    -1,    -1,   101,    -1,    -1,    -1,    -1,
      -1,   107,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   121,    -1,    -1,   124,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     136,   137,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,    -1,    -1,    -1,    -1,   152,   153,   154,   155,
       0,    -1,   158,   159,   160,    -1,   162,    -1,     8,     9,
      10,    -1,    -1,    13,    14,    15,    -1,    17,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    25,    -1,    27,    28,    29,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,
      40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,    69,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    78,    79,
      80,    81,    82,    83,    84,    85,    86,    87,    88,    89,
      90,    -1,    -1,    -1,    -1,    95,    96,    97,    98,    -1,
     100,   101,    -1,    -1,    -1,    -1,    -1,   107,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   121,    -1,    -1,   124,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   135,   136,   137,   138,   139,
     140,   141,   142,   143,   144,   145,   146,   147,    -1,    -1,
      -1,    -1,    -1,   153,   154,   155,     0,    -1,   158,   159,
     160,    -1,   162,    -1,     8,     9,    10,    -1,    -1,    13,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    25,    26,    27,    28,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    68,    69,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    90,    -1,    -1,    -1,
      -1,    95,    96,    97,    98,    -1,    -1,   101,    -1,    -1,
      -1,    -1,    -1,   107,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   121,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   136,    -1,   138,   139,   140,   141,   142,   143,
     144,   145,   146,   147,    -1,    -1,    -1,    -1,   152,   153,
     154,   155,     0,   157,   158,   159,   160,    -1,   162,    -1,
       8,     9,    10,    -1,    -1,    13,    14,    15,    -1,    17,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,    -1,    27,
      28,    29,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,
      38,    -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      68,    69,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    90,    -1,    -1,    -1,    -1,    95,    96,    97,
      98,    -1,    -1,   101,    -1,    -1,    -1,    -1,    -1,   107,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   121,    -1,    -1,   124,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   136,   137,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
      -1,    -1,    -1,    -1,    -1,   153,   154,   155,     0,    -1,
     158,   159,   160,    -1,   162,    -1,     8,     9,    10,    -1,
      -1,    13,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    25,    26,    27,    28,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,
      42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    68,    69,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    90,    -1,
      -1,    -1,    -1,    95,    96,    97,    98,    -1,    -1,   101,
      -1,    -1,    -1,    -1,    -1,   107,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   121,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   136,    -1,   138,   139,   140,   141,
     142,   143,   144,   145,   146,   147,    -1,    -1,    -1,    -1,
     152,   153,   154,   155,     0,   157,   158,   159,   160,    -1,
     162,    -1,     8,     9,    10,    -1,    -1,    13,    14,    15,
      -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,
      -1,    27,    28,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    37,    38,    -1,    40,    41,    42,    43,    44,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    68,    69,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    78,    79,    80,    81,    82,    83,    84,    85,
      86,    87,    88,    89,    90,    -1,    -1,    -1,    -1,    95,
      96,    97,    98,    -1,    -1,   101,    -1,    -1,    -1,    -1,
      -1,   107,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   121,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     136,    -1,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,    -1,    -1,    -1,    -1,    -1,   153,   154,   155,
       0,   157,   158,   159,   160,    -1,   162,    -1,     8,     9,
      10,    -1,    -1,    -1,    14,    15,    -1,    17,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    25,    26,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,    -1,
      40,    41,    42,    43,    44,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    68,    69,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    78,    79,
      80,    81,    82,    83,    84,    85,    86,    87,    88,    89,
      90,    -1,    -1,    -1,    -1,    95,    96,    97,    98,    -1,
     100,   101,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   121,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   135,   136,    -1,   138,   139,
     140,   141,   142,   143,   144,   145,   146,   147,    -1,    -1,
      -1,    -1,   152,   153,   154,   155,     0,    -1,   158,    -1,
     160,    -1,   162,    -1,     8,     9,    10,    -1,    -1,    -1,
      14,    15,    -1,    17,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    25,    26,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    37,    38,    -1,    40,    41,    42,    43,
      44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    68,    69,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    78,    79,    80,    81,    82,    83,
      84,    85,    86,    87,    88,    89,    90,    -1,    -1,    -1,
      -1,    95,    96,    97,    98,    -1,   100,   101,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   121,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   135,   136,    -1,   138,   139,   140,   141,   142,   143,
     144,   145,   146,   147,    -1,    -1,    -1,    -1,   152,   153,
     154,   155,     0,    -1,   158,    -1,   160,    -1,   162,    -1,
       8,     9,    10,    -1,    -1,    -1,    14,    15,    -1,    17,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    25,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    37,
      38,    -1,    40,    41,    42,    43,    44,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      68,    69,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      78,    79,    80,    81,    82,    83,    84,    85,    86,    87,
      88,    89,    90,    -1,    -1,    -1,    -1,    95,    96,    97,
      98,    -1,   100,   101,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   121,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   135,   136,    -1,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
      -1,    -1,    -1,    -1,    -1,   153,   154,   155,     0,    -1,
     158,    -1,   160,    -1,   162,    -1,     8,     9,    10,    -1,
      -1,    -1,    14,    15,    -1,    17,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    25,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    37,    38,    -1,    40,    41,
      42,    43,    44,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    68,    69,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    90,    -1,
      -1,    -1,    -1,    95,    96,    97,    98,    -1,   100,   101,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   121,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   135,   136,    -1,   138,   139,   140,   141,
     142,   143,   144,   145,   146,   147,    -1,    -1,    -1,    -1,
      -1,   153,   154,   155,    -1,    -1,   158,    -1,   160,     1,
     162,     3,     4,     5,     6,     7,     8,     9,    10,    11,
      12,    -1,    -1,    15,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,
      -1,    -1,    -1,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    -1,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    68,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    76,    77,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,
      92,    -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,
     102,   103,    -1,   105,   106,    -1,   108,    -1,    -1,   111,
     112,   113,   114,   115,   116,   117,   118,   119,    -1,    -1,
      -1,     1,    -1,     3,     4,     5,     6,     7,    -1,    -1,
      10,    11,    12,    -1,    14,    15,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,    -1,   148,   149,   150,    -1,
      30,    31,    32,    33,    34,    35,    36,    -1,   160,    39,
     162,    -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    63,    64,    65,    -1,    -1,    68,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    91,    92,    -1,    -1,    -1,    -1,    -1,    -1,    99,
      -1,    -1,   102,   103,    -1,   105,   106,    -1,   108,    -1,
      -1,   111,   112,   113,   114,   115,   116,   117,   118,   119,
      -1,    -1,    -1,     1,    -1,     3,     4,     5,     6,     7,
      -1,    -1,    10,    11,    12,    -1,    -1,    15,    16,    17,
      18,    19,    20,    21,    22,    23,    24,    -1,   148,   149,
     150,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
     160,    39,   162,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      68,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,
     108,    -1,    -1,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,     1,    -1,     3,     4,     5,
       6,     7,    -1,    -1,    10,    11,    12,    -1,    -1,    15,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    25,
     148,   149,   150,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,   160,    39,   162,    -1,    -1,    -1,    -1,    45,
      46,    47,    48,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      76,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,    -1,
      -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,   105,
     106,    -1,   108,    -1,    -1,   111,   112,   113,   114,   115,
     116,   117,   118,   119,    -1,    -1,    -1,     1,    -1,     3,
       4,     5,     6,     7,    -1,    -1,    10,    11,    12,    -1,
      -1,    15,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,    -1,   148,   149,   150,    -1,    30,    31,    32,    33,
      34,    35,    36,    -1,   160,    39,   162,    -1,    -1,    -1,
      -1,    45,    46,    47,    48,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    76,    77,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,
      -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,   102,   103,
      -1,   105,   106,    -1,   108,    -1,    -1,   111,   112,   113,
     114,   115,   116,   117,   118,   119,    -1,    -1,    -1,     1,
      -1,     3,     4,     5,     6,     7,    -1,     9,    10,    11,
      12,    -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,    -1,   148,   149,   150,    -1,    30,    31,
      32,    33,    34,    35,    36,    -1,   160,    39,   162,    -1,
      -1,    -1,    -1,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    -1,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    68,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    76,    77,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,
      92,    -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,
     102,   103,    -1,   105,   106,    -1,   108,    -1,    -1,   111,
     112,   113,   114,   115,   116,   117,   118,   119,    -1,    -1,
      -1,     1,    -1,     3,     4,     5,     6,     7,    -1,    -1,
      10,    11,    12,    -1,    -1,    -1,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,    -1,   148,   149,   150,    -1,
      30,    31,    32,    33,    34,    35,    36,    -1,   160,    39,
     162,    -1,    -1,    -1,    -1,    45,    46,    47,    48,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    63,    64,    65,    -1,    -1,    68,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    91,    92,    -1,    -1,    -1,    -1,    -1,    -1,    99,
      -1,    -1,   102,   103,    -1,   105,   106,    -1,   108,    -1,
      -1,   111,   112,   113,   114,   115,   116,   117,   118,   119,
      -1,    -1,    -1,     1,    -1,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,   148,   149,
     150,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
     160,    39,   162,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,
     108,    -1,    -1,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     148,   149,   150,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     158,    -1,   160,     1,   162,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,
     108,    -1,    -1,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     148,   149,   150,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     158,    -1,   160,     1,   162,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,
     108,    -1,    -1,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     148,   149,   150,    -1,    -1,   153,    -1,    -1,    -1,    -1,
      -1,    -1,   160,     1,   162,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,
     108,    -1,    -1,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     148,   149,   150,    -1,    -1,   153,    -1,    -1,    -1,    -1,
      -1,    -1,   160,     1,   162,     3,     4,     5,     6,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,
     108,    -1,    -1,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,   121,    -1,     1,    -1,     3,     4,     5,
       6,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    -1,
     148,   149,   150,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,   160,    39,   162,    -1,    -1,    -1,    -1,    45,
      46,    -1,    48,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      76,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,    -1,
      -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,   105,
     106,    -1,   108,    -1,    -1,   111,   112,   113,   114,   115,
     116,   117,   118,   119,    -1,    -1,     1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,
      -1,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,   148,   149,   150,    30,    31,    32,    33,    34,
      35,    36,   158,    -1,    39,    -1,   162,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    76,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,
      -1,    -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,
     105,   106,    -1,    -1,    -1,    -1,   111,   112,   113,   114,
     115,   116,   117,   118,   119,    -1,    -1,     0,     1,    -1,
       3,     4,     5,     6,     7,    -1,    -1,    -1,    11,    12,
      -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,   148,   149,   150,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,   160,    39,   162,    -1,    -1,
      -1,    -1,    45,    46,    47,    48,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    76,    77,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,    92,
      -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,   102,
     103,    -1,   105,   106,    -1,   108,    -1,    -1,   111,   112,
     113,   114,   115,   116,   117,   118,   119,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   148,   149,   150,    -1,    -1,
     153,    -1,    -1,    -1,    -1,    -1,     1,   160,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    -1,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    68,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    76,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,
      -1,    -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,
     105,   106,    -1,   108,    -1,    -1,   111,   112,   113,   114,
     115,   116,   117,   118,   119,    -1,   121,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   148,   149,   150,    -1,    -1,   153,    -1,
       3,     4,     5,   158,     7,   160,    -1,    -1,    11,    12,
      -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,    -1,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    58,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    76,    77,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,    92,
      -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,   102,
     103,    -1,   105,   106,    -1,   108,   109,   110,   111,   112,
     113,   114,   115,   116,   117,   118,   119,    -1,    -1,    -1,
      -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,
      12,    -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,    -1,    -1,   148,   149,   150,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,   162,
      -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    -1,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    76,    77,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,
      92,    -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,
     102,   103,    -1,   105,   106,    -1,    -1,    -1,    -1,   111,
     112,   113,   114,   115,   116,   117,   118,   119,    -1,    -1,
      -1,    -1,     3,     4,     5,     6,     7,     8,     9,    10,
      11,    12,    13,    14,    15,    16,    17,    18,    19,    20,
      21,    22,    23,    24,    25,    26,   148,   149,   150,    30,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    -1,
     162,    -1,    -1,    -1,    45,    46,    47,    48,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    76,    77,    78,    79,    80,
      81,    82,    83,    84,    -1,    -1,    87,    88,    -1,    -1,
      -1,    -1,    93,    94,    95,    96,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   108,   109,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   138,   139,   140,
     141,   142,   143,   144,   145,   146,   147,    -1,   149,   150,
      -1,    -1,    -1,    -1,    -1,   156,   157,     3,     4,     5,
       6,     7,     8,     9,    10,    11,    12,    13,    14,    15,
      16,    17,    18,    19,    20,    21,    22,    23,    24,    25,
      26,    -1,    -1,    -1,    30,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    -1,    -1,    -1,    -1,    -1,    45,
      46,    47,    48,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      76,    77,    78,    79,    80,    81,    82,    83,    84,    -1,
      -1,    87,    88,    -1,    -1,    -1,    -1,    93,    94,    95,
      96,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   108,   109,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,    -1,   149,   150,    -1,    -1,    -1,    -1,    -1,
     156,     3,     4,     5,     6,     7,     8,     9,    10,    11,
      12,    13,    14,    15,    16,    17,    18,    19,    20,    21,
      22,    23,    24,    25,    26,    -1,    -1,    -1,    30,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    -1,    -1,
      -1,    -1,    -1,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    54,    -1,    56,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    76,    77,    78,    79,    80,    81,
      82,    83,    84,    -1,    -1,    87,    88,    -1,    -1,    -1,
      -1,    93,    94,    95,    96,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   108,   109,    -1,    -1,
     112,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   138,   139,   140,   141,
     142,   143,   144,   145,   146,   147,    -1,   149,   150,    -1,
      -1,    -1,    -1,    -1,   156,     3,     4,     5,     6,     7,
       8,     9,    10,    11,    12,    13,    14,    15,    16,    17,
      18,    19,    20,    21,    22,    23,    24,    25,    26,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,
      48,    49,    50,    51,    52,    53,    -1,    -1,    56,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      78,    79,    80,    81,    82,    83,    84,    -1,    -1,    87,
      88,    -1,    -1,    -1,    -1,    93,    94,    95,    96,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     108,   109,    -1,    -1,   112,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
      -1,   149,   150,    -1,    -1,    -1,    -1,    -1,   156,     3,
       4,     5,     6,     7,     8,     9,    10,    11,    12,    13,
      14,    15,    16,    17,    18,    19,    20,    21,    22,    23,
      24,    25,    26,    -1,    -1,    -1,    30,    31,    32,    33,
      34,    35,    36,    37,    38,    39,    -1,    -1,    -1,    -1,
      -1,    45,    46,    47,    48,    49,    50,    51,    52,    53,
      -1,    -1,    56,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    76,    77,    78,    79,    80,    81,    82,    83,
      84,    -1,    -1,    87,    88,    -1,    -1,    -1,    -1,    93,
      94,    95,    96,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   108,   109,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   138,   139,   140,   141,   142,   143,
     144,   145,   146,   147,    -1,   149,   150,     3,     4,     5,
      -1,     7,   156,    -1,    -1,    11,    12,    -1,    -1,    -1,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    -1,
      -1,    -1,    -1,    -1,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,
      46,    -1,    -1,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,   105,
     106,    -1,    -1,    -1,    -1,   111,   112,   113,   114,   115,
     116,   117,   118,   119,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,
      -1,    -1,   148,    11,    12,    -1,    -1,    -1,    16,   155,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
      -1,    -1,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,
      -1,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,
      -1,    -1,    -1,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,     1,    -1,     3,     4,     5,     6,     7,    -1,    -1,
     148,    11,    12,    -1,    -1,    -1,    16,   155,    18,    19,
      20,    21,    22,    23,    24,    -1,    -1,    -1,    -1,    -1,
      30,    31,    32,    33,    34,    35,    36,    -1,    -1,    39,
      -1,    -1,    -1,    -1,    -1,    45,    46,    -1,    48,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    91,    92,    -1,    -1,    -1,    -1,    -1,    -1,    99,
      -1,    -1,   102,   103,    -1,   105,   106,    -1,   108,    -1,
      -1,   111,   112,   113,   114,   115,   116,   117,   118,   119,
      -1,    -1,     1,    -1,     3,     4,     5,    -1,     7,    -1,
      -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,   148,   149,
     150,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    63,    64,    65,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,    -1,
      99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,    -1,
      -1,    -1,   111,   112,   113,   114,   115,   116,   117,   118,
     119,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,   148,
     149,   150,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,
      -1,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      58,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,
     108,   109,   110,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
     148,   149,   150,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    58,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,
      77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,
      -1,    -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,
      -1,   108,   109,   110,   111,   112,   113,   114,   115,   116,
     117,   118,   119,    -1,    -1,    -1,    -1,     3,     4,     5,
      -1,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    -1,
      -1,   148,   149,   150,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,
      46,    -1,    -1,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    58,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      76,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,    -1,
      -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,   105,
     106,    -1,   108,   109,   110,   111,   112,   113,   114,   115,
     116,   117,   118,   119,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,
      -1,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,   148,   149,   150,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    76,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,
      -1,    -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,
     105,   106,    -1,   108,   109,   110,   111,   112,   113,   114,
     115,   116,   117,   118,   119,    -1,    -1,    -1,    -1,     3,
       4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,    -1,
      -1,    -1,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,    -1,    -1,   148,   149,   150,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    58,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    76,    77,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,
      -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,   102,   103,
      -1,   105,   106,    -1,   108,   109,    -1,   111,   112,   113,
     114,   115,   116,   117,   118,   119,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
      -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,   148,   149,   150,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    58,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    76,    77,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,    92,
      -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,   102,
     103,    -1,   105,   106,    -1,    -1,   109,   110,   111,   112,
     113,   114,   115,   116,   117,   118,   119,    -1,    -1,    -1,
      -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,
      12,    -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,    -1,    -1,   148,   149,   150,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,
      -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    58,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    76,    77,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,
      92,    -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,
     102,   103,    -1,   105,   106,    -1,   108,   109,    -1,   111,
     112,   113,   114,   115,   116,   117,   118,   119,    -1,    -1,
      -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,
      11,    12,    -1,    -1,    -1,    16,    -1,    18,    19,    20,
      21,    22,    23,    24,    -1,    -1,   148,   149,   150,    30,
      31,    32,    33,    34,    35,    36,    -1,    -1,    39,    -1,
      -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,
      51,    52,    53,    54,    55,    56,    57,    58,    59,    60,
      61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    76,    77,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      91,    92,    -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,
      -1,   102,   103,    -1,   105,   106,    -1,    -1,   109,    -1,
     111,   112,   113,   114,   115,   116,   117,   118,   119,    -1,
      -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,    -1,
      -1,    11,    12,    -1,    -1,    -1,    16,    -1,    18,    19,
      20,    21,    22,    23,    24,    -1,    -1,   148,   149,   150,
      30,    31,    32,    33,    34,    35,    36,    -1,    -1,    39,
      -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,    49,
      50,    51,    52,    53,    54,    55,    56,    57,    -1,    59,
      60,    61,    62,    63,    64,    65,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    91,    92,    -1,    -1,    -1,    -1,    -1,    -1,    99,
      -1,    -1,   102,   103,    -1,   105,   106,    -1,   108,    -1,
      -1,   111,   112,   113,   114,   115,   116,   117,   118,   119,
      -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,
      -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,    18,
      19,    20,    21,    22,    23,    24,    -1,    -1,   148,   149,
     150,    30,    31,    32,    33,    34,    35,    36,    -1,    -1,
      39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,    -1,
      49,    50,    51,    52,    53,    54,    55,    56,    57,    -1,
      59,    60,    61,    62,    63,    64,    65,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,    -1,
      99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,   108,
      -1,    -1,   111,   112,   113,   114,   115,   116,   117,   118,
     119,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,   148,
     149,   150,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,
      -1,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    -1,
     108,    -1,    -1,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
     148,   149,   150,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,
      77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,
      -1,    -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,
      -1,   108,    -1,    -1,   111,   112,   113,   114,   115,   116,
     117,   118,   119,    -1,    -1,    -1,    -1,     3,     4,     5,
      -1,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    -1,
      -1,   148,   149,   150,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,
      46,    -1,    -1,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      76,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,    -1,
      -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,   105,
     106,    -1,   108,    -1,    -1,   111,   112,   113,   114,   115,
     116,   117,   118,   119,    -1,    -1,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,
      -1,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,   148,   149,   150,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    76,    77,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,
      -1,    -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,
     105,   106,    -1,    -1,    -1,    -1,   111,   112,   113,   114,
     115,   116,   117,   118,   119,    -1,    -1,    -1,    -1,     3,
       4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,    -1,
      -1,    -1,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,    -1,    -1,   148,   149,   150,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    76,    77,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,
      -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,   102,   103,
      -1,   105,   106,    -1,    -1,    -1,    -1,   111,   112,   113,
     114,   115,   116,   117,   118,   119,    -1,    -1,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
      -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,   148,   149,   150,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    76,    77,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,    92,
      -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,   102,
     103,    -1,   105,   106,    -1,    -1,    -1,    -1,   111,   112,
     113,   114,   115,   116,   117,   118,   119,    -1,    -1,    -1,
      -1,     3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,
      12,    -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,
      22,    23,    24,    -1,    -1,   148,   149,   150,    30,    31,
      32,    33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,
      -1,    -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,
      52,    53,    54,    55,    56,    57,    -1,    59,    60,    61,
      62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,
     102,   103,    -1,   105,   106,    -1,   108,    -1,    -1,   111,
     112,   113,   114,   115,   116,   117,   118,   119,    -1,    -1,
       3,     4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,
      -1,    -1,    -1,    16,    -1,    18,    19,    20,    21,    22,
      23,    24,    -1,    -1,    -1,    -1,   148,    30,    31,    32,
      33,    34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,
      -1,    -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,
      53,    54,    55,    56,    57,    -1,    59,    60,    61,    62,
      63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,   102,
     103,    -1,   105,   106,    -1,   108,    -1,    -1,   111,   112,
     113,   114,   115,   116,   117,   118,   119,    -1,    -1,     3,
       4,     5,    -1,     7,    -1,    -1,    -1,    11,    12,    -1,
      -1,    -1,    16,    -1,    18,    19,    20,    21,    22,    23,
      24,    -1,    -1,    -1,    -1,   148,    30,    31,    32,    33,
      34,    35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,
      -1,    -1,    46,    -1,    -1,    49,    50,    51,    52,    53,
      54,    55,    56,    57,    -1,    59,    60,    61,    62,    63,
      64,    65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    95,    -1,    -1,    -1,    99,    -1,    -1,   102,   103,
      -1,   105,   106,    -1,    -1,    -1,    -1,   111,   112,   113,
     114,   115,   116,   117,   118,   119,    -1,    -1,     3,     4,
       5,    -1,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,
      -1,    16,    -1,    18,    19,    20,    21,    22,    23,    24,
      -1,    -1,    -1,    -1,   148,    30,    31,    32,    33,    34,
      35,    36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,
      -1,    46,    -1,    -1,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    -1,    59,    60,    61,    62,    63,    64,
      65,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,
     105,   106,    -1,    -1,    -1,    -1,   111,   112,   113,   114,
     115,   116,   117,   118,   119,    -1,    -1,     3,     4,     5,
      -1,     7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,
      16,    -1,    18,    19,    20,    21,    22,    23,    24,    -1,
      -1,    -1,    -1,   148,    30,    31,    32,    33,    34,    35,
      36,    -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,
      46,    -1,    -1,    49,    50,    51,    52,    53,    54,    55,
      56,    57,    -1,    59,    60,    61,    62,    63,    64,    65,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    99,    -1,    -1,   102,   103,    -1,   105,
     106,    -1,    -1,    -1,    -1,   111,   112,   113,   114,   115,
     116,   117,   118,   119,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,
      -1,    18,    19,    20,    21,    22,    23,    24,    -1,    -1,
      -1,    -1,   148,    30,    31,    32,    33,    34,    35,    36,
      -1,    -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,
      -1,    -1,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    -1,    59,    60,    61,    62,    63,    64,    65,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,
      -1,    -1,    -1,    -1,   111,   112,   113,   114,   115,   116,
     117,   118,   119,    -1,    -1,     3,     4,     5,    -1,     7,
      -1,    -1,    -1,    11,    12,    -1,    -1,    -1,    16,    -1,
      18,    19,    20,    21,    22,    23,    24,    -1,    -1,    -1,
      -1,   148,    30,    31,    32,    33,    34,    35,    36,    -1,
      -1,    39,    -1,    -1,    -1,    -1,    -1,    -1,    46,    -1,
      -1,    49,    50,    51,    52,    53,    54,    55,    56,    57,
      -1,    59,    60,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,   103,    -1,   105,   106,    33,
      34,    35,    36,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,    49,    50,    51,    52,    -1,
      -1,    -1,    56,    -1,    -1,    59,    60,    61,    62,    63,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     148,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,
      -1,    -1,    -1,    -1,    -1,    99,    -1,    -1,   102,    -1,
      -1,   105,   106,    -1,   108,    -1,    -1,   111,   112,   113,
     114,   115,   116,   117,   118,   119,    -1,    -1,    -1,    -1,
      -1,    -1,    33,    34,    35,    36,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   141,    49,    50,
      51,    52,    -1,    -1,   148,    56,    -1,    58,    59,    60,
      61,    62,    63,    -1,    -1,    -1,    -1,    -1,   162,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    78,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      91,    92,    -1,    -1,    -1,    -1,    -1,    -1,    99,    -1,
      -1,   102,    -1,    -1,   105,   106,    -1,   108,   109,    -1,
     111,   112,   113,   114,   115,   116,   117,   118,   119,    -1,
      -1,    -1,    -1,    -1,    -1,    33,    34,    35,    36,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     141,    49,    50,    51,    52,    -1,    -1,   148,    56,    -1,
      -1,    59,    60,    61,    62,    63,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    91,    92,    -1,    -1,    -1,    -1,    -1,
      -1,    99,    -1,    -1,   102,    -1,    -1,   105,   106,    -1,
     108,    -1,    -1,   111,   112,   113,   114,   115,   116,   117,
     118,   119,    -1,    -1,    -1,    -1,    -1,    -1,    33,    34,
      35,    36,    -1,    -1,    -1,    52,    53,    -1,    -1,    56,
      -1,    -1,    -1,   141,    49,    50,    51,    52,    -1,    -1,
     148,    56,    -1,    -1,    59,    60,    61,    62,    63,    76,
      77,    78,    79,    80,    81,    82,    83,    84,    -1,    -1,
      87,    88,    -1,    -1,    -1,    -1,    93,    94,    95,    96,
      -1,    -1,    -1,    -1,    -1,    -1,    91,    92,    -1,    -1,
      -1,   108,   109,    -1,    99,    -1,    -1,   102,    -1,    -1,
     105,   106,    -1,    -1,    -1,    -1,   111,   112,   113,   114,
     115,   116,   117,   118,   119,    -1,    -1,    -1,    -1,    -1,
      -1,   138,   139,   140,   141,   142,   143,   144,   145,   146,
     147,    -1,   149,   150,    52,    53,   141,    -1,    56,   156,
     157,    -1,    -1,   148,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      78,    79,    80,    81,    82,    83,    84,    -1,    -1,    87,
      88,    -1,    -1,    -1,    -1,    93,    94,    95,    96,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     108,   109,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
      -1,   149,   150,    52,    53,    -1,    -1,    56,   156,   157,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    78,
      79,    80,    81,    82,    83,    84,    -1,    -1,    87,    88,
      -1,    -1,    -1,    -1,    93,    94,    95,    96,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   108,
     109,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   138,
     139,   140,   141,   142,   143,   144,   145,   146,   147,    -1,
     149,   150,    52,    53,    -1,    -1,    56,   156,   157,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    78,    79,
      80,    81,    82,    83,    84,    -1,    -1,    87,    88,    -1,
      -1,    -1,    -1,    93,    94,    95,    96,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   108,   109,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   138,   139,
     140,   141,   142,   143,   144,   145,   146,   147,    -1,   149,
     150,    52,    53,    -1,    -1,    56,   156,   157,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    76,    77,    78,    79,    80,
      81,    82,    83,    84,    -1,    -1,    87,    88,    -1,    -1,
      -1,    -1,    93,    94,    95,    96,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   108,   109,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   138,   139,   140,
     141,   142,   143,   144,   145,   146,   147,    -1,   149,   150,
      52,    53,    -1,    -1,    56,   156,   157,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    76,    77,    78,    79,    80,    81,
      82,    83,    84,    -1,    -1,    87,    88,    -1,    -1,    -1,
      -1,    93,    94,    95,    96,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   108,   109,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   138,   139,   140,   141,
     142,   143,   144,   145,   146,   147,    -1,   149,   150,    52,
      53,    -1,    -1,    56,   156,   157,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    76,    77,    78,    79,    80,    81,    82,
      83,    84,    -1,    -1,    87,    88,    -1,    -1,    -1,    -1,
      93,    94,    95,    96,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   108,   109,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   138,   139,   140,   141,   142,
     143,   144,   145,   146,   147,    -1,   149,   150,    52,    53,
      -1,    -1,    56,   156,   157,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    76,    77,    78,    79,    80,    81,    82,    83,
      84,    -1,    -1,    87,    88,    -1,    -1,    -1,    -1,    93,
      94,    95,    96,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   108,   109,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   138,   139,   140,   141,   142,   143,
     144,   145,   146,   147,    -1,   149,   150,    52,    53,    -1,
      -1,    56,   156,   157,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    76,    77,    78,    79,    80,    81,    82,    83,    84,
      -1,    -1,    87,    88,    -1,    -1,    -1,    -1,    93,    94,
      95,    96,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   108,   109,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   138,   139,   140,   141,   142,   143,   144,
     145,   146,   147,    -1,   149,   150,    52,    53,    -1,    -1,
      56,   156,   157,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      76,    77,    78,    79,    80,    81,    82,    83,    84,    -1,
      -1,    87,    88,    -1,    -1,    -1,    -1,    93,    94,    95,
      96,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   108,   109,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,    -1,   149,   150,    52,    53,    -1,    -1,    56,
     156,   157,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,
      77,    78,    79,    80,    81,    82,    83,    84,    -1,    -1,
      87,    88,    -1,    -1,    -1,    -1,    93,    94,    95,    96,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   108,   109,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   138,   139,   140,   141,   142,   143,   144,   145,   146,
     147,    -1,   149,   150,    52,    53,    -1,    -1,    56,   156,
     157,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,
      78,    79,    80,    81,    82,    83,    84,    -1,    -1,    87,
      88,    -1,    -1,    -1,    -1,    93,    94,    95,    96,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     108,   109,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
      -1,   149,   150,    52,    53,    -1,    -1,    56,   156,   157,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    78,
      79,    80,    81,    82,    83,    84,    -1,    -1,    87,    88,
      -1,    -1,    -1,    -1,    93,    94,    95,    96,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   108,
     109,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   138,
     139,   140,   141,   142,   143,   144,   145,   146,   147,    -1,
     149,   150,    52,    53,    -1,    -1,    56,   156,   157,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    76,    77,    78,    79,
      80,    81,    82,    83,    84,    -1,    -1,    87,    88,    -1,
      -1,    -1,    -1,    93,    94,    95,    96,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   108,   109,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   138,   139,
     140,   141,   142,   143,   144,   145,   146,   147,    -1,   149,
     150,    52,    53,    -1,    -1,    56,   156,   157,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    76,    77,    78,    79,    80,
      81,    82,    83,    84,    -1,    -1,    87,    88,    -1,    -1,
      -1,    -1,    93,    94,    95,    96,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   108,   109,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   138,   139,   140,
     141,   142,   143,   144,   145,   146,   147,    -1,   149,   150,
      52,    53,    -1,    -1,    56,   156,   157,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    76,    77,    78,    79,    80,    81,
      82,    83,    84,    -1,    -1,    87,    88,    -1,    -1,    -1,
      -1,    93,    94,    95,    96,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   108,   109,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   138,   139,   140,   141,
     142,   143,   144,   145,   146,   147,    -1,   149,   150,    -1,
      -1,    -1,    -1,    -1,   156
};

/* YYSTOS[STATE-NUM] -- The symbol kind of the accessing symbol of
   state STATE-NUM.  */
static const yytype_int16 yystos[] =
{
       0,   164,   165,     0,     1,     3,     4,     5,     6,     7,
      11,    12,    16,    18,    19,    20,    21,    22,    23,    24,
      30,    31,    32,    33,    34,    35,    36,    39,    45,    46,
      47,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    59,    60,    61,    62,    63,    64,    65,    76,    77,
      91,    92,    99,   102,   103,   105,   106,   108,   111,   112,
     113,   114,   115,   116,   117,   118,   119,   148,   149,   150,
     166,   167,   168,   176,   178,   180,   186,   187,   193,   194,
     196,   197,   198,   200,   201,   202,   204,   205,   214,   217,
     233,   245,   246,   247,   248,   249,   250,   251,   252,   253,
     254,   255,   264,   286,   295,   296,   348,   349,   350,   351,
     352,   353,   354,   357,   359,   360,   374,   375,   377,   378,
     379,   380,   381,   382,   383,   384,   385,   423,   437,     3,
       4,     5,     6,     7,     8,     9,    10,    11,    12,    13,
      14,    15,    16,    17,    18,    19,    20,    21,    22,    23,
      24,    25,    26,    30,    31,    32,    33,    34,    35,    36,
      37,    38,    39,    45,    46,    47,    48,    49,    50,    51,
      52,    53,    56,    76,    77,    78,    79,    80,    81,    82,
      83,    84,    87,    88,    93,    94,    95,    96,   108,   109,
     138,   139,   140,   141,   142,   143,   144,   145,   146,   147,
     149,   150,   156,   208,   209,   210,   212,   213,   374,    39,
      58,    99,   102,   108,   109,   110,   113,   149,   186,   187,
     197,   205,   214,   219,   225,   228,   230,   245,   381,   382,
     384,   385,   421,   422,   225,   157,   226,   227,   157,   222,
     226,   157,   162,   430,    54,   209,   430,   152,   169,   152,
      21,    22,    31,    32,   196,   214,   245,   264,   214,   214,
     214,    56,    47,   102,   172,   173,   174,   176,   199,   200,
     437,   176,   235,   220,   230,   421,   437,   219,   420,   421,
     437,    46,    99,   148,   155,   186,   187,   204,   233,   245,
     381,   382,   385,   287,   208,   363,   376,   380,   363,   364,
     365,   161,   161,   161,   161,   379,   193,   214,   214,   160,
     162,   429,   435,   436,    40,    41,    42,    43,    44,    37,
      38,   157,   388,   389,   390,   391,   437,   388,   390,    26,
     152,   222,   226,   256,   297,    28,   257,   294,   135,   155,
     102,   108,   201,   135,    25,    78,    79,    80,    81,    82,
      83,    84,    85,    86,    87,    88,    89,    90,    95,    96,
     101,   136,   138,   139,   140,   141,   142,   143,   144,   145,
     146,   147,   216,   216,    69,    97,    98,   154,   427,   234,
       1,   180,   189,   189,   190,   191,   190,   189,   429,   436,
      99,   198,   205,   245,   269,   381,   382,   385,    52,    56,
      95,    99,   206,   207,   245,   381,   382,   385,   207,    33,
      34,    35,    36,    49,    50,    51,    52,    56,   157,   185,
     208,   383,   418,   225,    98,   427,   428,   297,   351,   100,
     100,   155,   219,    56,   219,   219,   219,   363,   388,   388,
     135,   101,   155,   229,   437,    98,   154,   427,   100,   100,
     155,   229,   225,   430,   431,   225,    92,   224,   225,   230,
     395,   421,   437,   180,   431,   180,    54,    64,    65,   177,
     157,   215,   166,   172,    98,   427,   100,   175,   199,   158,
     429,   436,   431,   236,   431,   159,   155,   430,   434,   155,
     434,   153,   434,   430,    56,   379,   201,   203,   389,   155,
      98,   154,   427,   288,    66,   120,   122,   123,   366,   120,
     120,   366,    67,   366,   355,   361,   358,   362,    78,   160,
     168,   189,   189,   189,   189,   176,   180,   180,    52,    54,
      55,    56,    57,    58,    78,    92,   102,   108,   109,   110,
     142,   145,   274,   336,   392,   394,   395,   396,   397,   398,
     399,   400,   401,   402,   405,   406,   407,   408,   409,   412,
     413,   414,   415,   416,   135,   243,   394,   135,   244,   298,
     299,   107,   195,   302,   303,   302,   218,   437,   199,   155,
     204,   155,   218,   183,   214,   214,   214,   214,   214,   214,
     214,   214,   214,   214,   214,   214,   214,   181,   214,   214,
     214,   214,   214,   214,   214,   214,   214,   214,   214,    52,
      53,    56,   212,   222,   423,   424,   425,   224,   230,    52,
      53,    56,   212,   222,   424,   170,   172,    13,   265,   435,
     265,   172,   189,   172,   429,   239,    56,    98,   154,   427,
      25,   180,    52,    56,   206,   139,   386,    98,   154,   427,
     242,   419,    69,    98,   426,    52,    56,   424,   218,   218,
     211,   125,   135,   135,   218,   219,   108,   219,   228,   421,
      52,    56,   224,    52,    56,   218,   218,   422,   431,   158,
     431,   155,   431,   155,   431,   209,   237,   214,   153,   153,
     424,   424,   218,   169,   431,   174,   431,   421,   155,   203,
      52,    56,   224,    52,    56,   289,   368,   367,   120,   356,
     366,    66,   120,   120,   356,    66,   120,   214,   102,   108,
     270,   271,   272,   273,   397,   155,   417,   437,   431,   275,
     276,   155,   393,   219,   155,   417,    34,    52,   155,   393,
      52,   155,   393,    52,   197,   214,   170,   435,   197,   214,
     170,   153,   300,   298,    10,    68,   263,   304,   263,   108,
     193,   219,   230,   231,   232,   431,   203,   155,   178,   179,
     193,   205,   214,   219,   221,   232,   245,   385,   184,   182,
     430,   100,   100,   222,   226,   430,   432,   155,   100,   100,
     222,   223,   226,   437,   263,     8,   258,   344,   437,   172,
      13,   172,   263,    27,   266,   435,   263,    25,   238,   309,
      17,   260,   307,    52,    56,   224,    52,    56,   190,   241,
     387,   240,    52,    56,   206,   224,   170,   180,   188,   223,
     226,   179,   214,   221,   179,   221,   209,   219,   219,   229,
     100,   100,   432,   100,   100,   395,   421,   180,   221,   434,
     201,   432,   157,   291,   394,   369,    54,    55,    57,   373,
     385,   161,   366,   161,   161,   161,   272,   397,   155,   431,
     155,   416,   219,   135,   392,   399,   412,   414,   402,   406,
     408,   400,   409,   414,   398,   400,    44,    44,   263,    44,
      44,   263,   301,   153,   305,   219,   155,    44,   203,    44,
     135,    44,    98,   154,   427,    52,    56,    58,    91,    92,
      99,   102,   105,   106,   108,   113,   141,   286,   315,   316,
     317,   318,   321,   326,   327,   328,   331,   332,   333,   334,
     335,   336,   337,   338,   339,   340,   341,   342,   343,   348,
     349,   352,   353,   354,   357,   359,   360,   382,   406,   315,
     137,   218,   218,   195,   159,   100,   218,   218,   195,   219,
     232,   345,   437,     9,    15,   259,   261,   347,   437,    14,
     261,   262,   267,   268,   437,   268,   192,   310,   307,   263,
     108,   219,   306,   263,   432,   172,   435,   189,   170,   432,
     263,   431,   185,   297,   294,   218,   218,   100,   218,   218,
     431,   155,   431,   394,   290,   370,   431,   270,   273,   271,
     155,   393,   155,   393,   417,   155,   393,   155,   393,   393,
     214,   214,   214,   214,   140,   281,   282,   437,   281,   108,
     219,   176,   176,   218,   214,    52,    56,   224,    52,    56,
     339,   339,    56,   206,   323,   316,   324,   325,   326,   327,
     330,   432,   322,   430,   433,    52,   363,    52,   102,   380,
     101,   155,   140,   155,   155,   316,    89,    90,    98,   154,
     157,   319,   320,    52,   214,   179,   221,   179,   221,   218,
     179,   221,   179,   221,   101,   346,   437,   172,   171,   172,
     189,   263,   263,   311,   263,   219,   155,   265,   263,   170,
     435,   263,   218,   283,   430,    29,   124,   292,   371,   155,
     155,   400,   414,   400,   400,   274,   277,   280,   283,   398,
     400,   401,   403,   404,   410,   411,   414,   416,   172,   170,
     219,   432,   316,   432,   316,   328,   330,   432,   155,   113,
     331,   153,   125,   189,   340,   324,   328,   321,   329,   330,
     333,   337,   339,   339,   206,   432,   431,   324,   327,   331,
     324,   327,   331,   179,   221,    99,   205,   245,   381,   382,
     385,   265,   172,   265,   314,   315,   108,   219,   172,   263,
     158,   160,   293,   172,   372,   271,   393,   155,   393,   393,
     393,   417,   283,   140,   275,   155,   278,   279,    99,   245,
     155,   417,   155,   278,   155,   278,   431,   155,   155,   363,
     433,   431,   155,   155,   431,   431,   431,   432,   432,   432,
      56,    98,   154,   427,   172,   347,   172,   265,    40,    41,
     219,   268,   307,   308,    52,   284,   285,   396,   170,   153,
     172,   400,   140,   245,   277,   411,   414,    56,    98,   403,
     408,   400,   410,   414,   400,   329,   329,   328,   330,    52,
      56,   224,    52,    56,   344,   267,   312,   189,   189,   155,
     430,   263,   121,   393,   155,   278,   155,   278,    52,    56,
     417,   155,   278,   155,   278,   278,   155,   432,   172,   285,
     400,   414,   400,   400,   268,   309,   313,   278,   155,   278,
     278,   278,   400,   278
};

/* YYR1[RULE-NUM] -- Symbol kind of the left-hand side of rule RULE-NUM.  */
static const yytype_int16 yyr1[] =
{
       0,   163,   165,   164,   166,   167,   167,   167,   168,   168,
     169,   171,   170,   170,   172,   173,   173,   173,   174,   175,
     174,   177,   176,   176,   176,   176,   176,   176,   176,   176,
     176,   176,   176,   176,   176,   176,   176,   176,   176,   176,
     178,   178,   178,   178,   178,   178,   178,   178,   178,   178,
     178,   178,   179,   179,   179,   180,   180,   180,   180,   180,
     181,   182,   180,   183,   184,   180,   180,   185,   186,   188,
     187,   189,   189,   191,   192,   190,   193,   193,   194,   194,
     195,   196,   197,   197,   197,   197,   197,   197,   197,   197,
     197,   197,   197,   198,   198,   199,   199,   200,   200,   200,
     200,   200,   200,   200,   200,   200,   200,   201,   201,   202,
     202,   203,   203,   204,   204,   204,   204,   204,   204,   204,
     204,   204,   205,   205,   205,   205,   205,   205,   205,   205,
     205,   206,   206,   207,   207,   207,   208,   208,   208,   208,
     208,   209,   209,   210,   211,   210,   212,   212,   212,   212,
     212,   212,   212,   212,   212,   212,   212,   212,   212,   212,
     212,   212,   212,   212,   212,   212,   212,   212,   212,   212,
     212,   212,   212,   212,   212,   212,   213,   213,   213,   213,
     213,   213,   213,   213,   213,   213,   213,   213,   213,   213,
     213,   213,   213,   213,   213,   213,   213,   213,   213,   213,
     213,   213,   213,   213,   213,   213,   213,   213,   213,   213,
     213,   213,   213,   213,   213,   213,   213,   214,   214,   214,
     214,   214,   214,   214,   214,   214,   214,   214,   214,   214,
     214,   214,   214,   214,   214,   214,   214,   214,   214,   214,
     214,   214,   214,   214,   214,   214,   214,   214,   214,   214,
     214,   214,   214,   214,   214,   214,   214,   215,   214,   214,
     214,   214,   214,   214,   214,   216,   216,   216,   216,   217,
     217,   218,   219,   220,   220,   220,   220,   221,   221,   222,
     222,   222,   223,   223,   224,   224,   224,   224,   224,   225,
     225,   225,   225,   225,   227,   226,   228,   228,   229,   229,
     230,   230,   230,   230,   230,   230,   231,   231,   232,   232,
     232,   233,   233,   233,   233,   233,   233,   233,   233,   233,
     233,   233,   234,   233,   235,   233,   236,   233,   233,   233,
     233,   233,   233,   233,   233,   233,   233,   237,   233,   233,
     233,   233,   233,   233,   233,   233,   233,   233,   233,   238,
     233,   239,   233,   233,   233,   240,   233,   241,   233,   242,
     233,   243,   233,   244,   233,   233,   233,   233,   233,   245,
     246,   247,   248,   249,   250,   251,   252,   253,   254,   255,
     256,   257,   258,   259,   260,   261,   262,   263,   263,   264,
     265,   265,   265,   266,   266,   267,   267,   268,   268,   269,
     269,   270,   270,   271,   271,   272,   272,   272,   272,   272,
     273,   273,   274,   274,   276,   275,   277,   277,   277,   277,
     278,   278,   279,   280,   280,   280,   280,   280,   280,   280,
     280,   280,   280,   280,   280,   280,   280,   280,   281,   281,
     282,   282,   283,   283,   284,   284,   285,   285,   287,   288,
     289,   290,   286,   291,   291,   292,   293,   292,   294,   295,
     295,   295,   295,   296,   296,   296,   296,   296,   296,   296,
     296,   296,   297,   297,   299,   300,   301,   298,   303,   304,
     305,   302,   306,   306,   306,   306,   307,   308,   308,   310,
     311,   312,   309,   313,   313,   314,   314,   314,   315,   315,
     315,   315,   315,   315,   316,   317,   317,   318,   318,   319,
     320,   321,   321,   321,   321,   321,   321,   321,   321,   321,
     321,   321,   321,   321,   322,   321,   321,   323,   321,   324,
     324,   324,   324,   324,   324,   325,   325,   326,   326,   327,
     328,   328,   329,   329,   330,   331,   331,   331,   331,   332,
     332,   333,   333,   334,   334,   335,   335,   336,   337,   337,
     338,   338,   338,   338,   338,   338,   338,   338,   338,   338,
     339,   339,   339,   339,   339,   339,   339,   339,   339,   339,
     340,   341,   341,   342,   343,   343,   343,   344,   344,   345,
     345,   345,   346,   346,   347,   347,   348,   348,   349,   350,
     350,   350,   351,   352,   353,   354,   355,   355,   356,   356,
     357,   358,   358,   359,   360,   361,   361,   362,   362,   363,
     363,   364,   364,   365,   365,   366,   367,   366,   368,   369,
     370,   371,   372,   366,   373,   373,   373,   373,   374,   374,
     375,   376,   376,   377,   378,   378,   379,   379,   379,   379,
     380,   380,   380,   381,   381,   381,   382,   382,   382,   382,
     382,   382,   382,   383,   383,   384,   384,   385,   385,   387,
     386,   386,   388,   388,   389,   390,   391,   390,   392,   392,
     392,   392,   392,   393,   393,   394,   394,   394,   394,   394,
     394,   394,   394,   394,   394,   394,   394,   394,   394,   394,
     395,   396,   396,   396,   396,   397,   397,   398,   399,   399,
     400,   400,   401,   402,   402,   403,   403,   404,   404,   405,
     405,   406,   406,   407,   408,   408,   409,   410,   411,   411,
     412,   412,   413,   413,   414,   414,   415,   415,   416,   416,
     417,   417,   418,   419,   418,   420,   420,   421,   421,   422,
     422,   422,   422,   422,   422,   423,   423,   423,   424,   424,
     425,   425,   425,   426,   426,   427,   427,   428,   428,   429,
     429,   430,   430,   431,   432,   433,   434,   434,   435,   435,
     436,   436,   437
};

/* YYR2[RULE-NUM] -- Number of symbols on the right-hand side of rule RULE-NUM.  */
static const yytype_int8 yyr2[] =
{
       0,     2,     0,     2,     2,     1,     1,     3,     1,     2,
       3,     0,     6,     3,     2,     1,     1,     3,     1,     0,
       3,     0,     4,     3,     3,     3,     2,     3,     3,     3,
       3,     3,     4,     1,     4,     4,     6,     4,     1,     1,
       4,     4,     7,     6,     6,     6,     6,     4,     6,     4,
       6,     4,     1,     3,     1,     1,     3,     3,     3,     2,
       0,     0,     5,     0,     0,     5,     1,     1,     2,     0,
       5,     1,     1,     0,     0,     4,     1,     1,     1,     4,
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
       3,     1,     1,     1,     2,     4,     2,     1,     3,     3,
       5,     3,     1,     1,     1,     1,     2,     4,     2,     1,
       2,     2,     4,     1,     0,     2,     2,     1,     2,     1,
       1,     2,     1,     3,     4,     3,     1,     1,     3,     4,
       2,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     0,     4,     0,     3,     0,     4,     3,     3,
       2,     3,     3,     1,     4,     3,     1,     0,     6,     4,
       3,     2,     1,     2,     1,     6,     6,     4,     4,     0,
       6,     0,     5,     5,     6,     0,     6,     0,     7,     0,
       5,     0,     5,     0,     5,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     2,     1,     1,     1,     5,     1,     2,     1,
       1,     1,     3,     1,     3,     1,     3,     5,     1,     3,
       2,     1,     1,     1,     0,     2,     4,     2,     2,     1,
       2,     0,     1,     6,     8,     4,     6,     4,     2,     6,
       2,     4,     6,     2,     4,     2,     4,     1,     1,     1,
       3,     4,     1,     4,     1,     3,     1,     1,     0,     0,
       0,     0,     7,     4,     1,     3,     0,     4,     3,     2,
       4,     5,     5,     2,     4,     4,     3,     3,     3,     2,
       1,     4,     3,     3,     0,     0,     0,     5,     0,     0,
       0,     5,     1,     2,     3,     4,     5,     1,     1,     0,
       0,     0,     8,     1,     1,     1,     3,     3,     1,     2,
       3,     1,     1,     1,     1,     3,     1,     3,     1,     1,
       1,     1,     1,     4,     4,     4,     3,     4,     4,     4,
       3,     3,     3,     2,     0,     4,     2,     0,     4,     1,
       1,     2,     2,     4,     1,     2,     3,     1,     3,     5,
       2,     1,     1,     3,     1,     3,     1,     2,     1,     1,
       3,     2,     1,     1,     3,     2,     1,     2,     1,     1,
       1,     3,     3,     2,     2,     1,     1,     1,     2,     2,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     2,     2,     4,     2,     3,     1,     6,     1,     1,
       1,     1,     2,     1,     2,     1,     1,     1,     1,     1,
       1,     2,     3,     3,     3,     4,     0,     3,     1,     2,
       4,     0,     3,     4,     4,     0,     3,     0,     3,     0,
       2,     0,     2,     0,     2,     1,     0,     3,     0,     0,
       0,     0,     0,     8,     1,     1,     1,     1,     1,     1,
       2,     1,     1,     3,     1,     2,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     0,
       4,     0,     1,     1,     3,     1,     0,     3,     4,     2,
       2,     1,     1,     2,     0,     6,     8,     4,     6,     4,
       6,     2,     4,     6,     2,     4,     2,     4,     1,     0,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     3,
       1,     3,     1,     2,     1,     2,     1,     1,     3,     1,
       3,     1,     1,     1,     2,     1,     3,     3,     1,     3,
       1,     3,     1,     1,     2,     1,     1,     1,     2,     1,
       2,     1,     1,     0,     4,     1,     2,     1,     3,     3,
       2,     1,     4,     2,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     0,
       1,     0,     1,     2,     2,     2,     1,     1,     1,     1,
       1,     2,     0
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
#line 1339 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6513 "parse.c"
        break;

    case YYSYMBOL_tFID: /* "method"  */
#line 1339 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6525 "parse.c"
        break;

    case YYSYMBOL_tGVAR: /* "global variable"  */
#line 1339 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6537 "parse.c"
        break;

    case YYSYMBOL_tIVAR: /* "instance variable"  */
#line 1339 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6549 "parse.c"
        break;

    case YYSYMBOL_tCONSTANT: /* "constant"  */
#line 1339 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6561 "parse.c"
        break;

    case YYSYMBOL_tCVAR: /* "class variable"  */
#line 1339 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6573 "parse.c"
        break;

    case YYSYMBOL_tLABEL: /* "label"  */
#line 1339 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6585 "parse.c"
        break;

    case YYSYMBOL_tINTEGER: /* "integer literal"  */
#line 1346 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6597 "parse.c"
        break;

    case YYSYMBOL_tFLOAT: /* "float literal"  */
#line 1346 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6609 "parse.c"
        break;

    case YYSYMBOL_tRATIONAL: /* "rational literal"  */
#line 1346 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6621 "parse.c"
        break;

    case YYSYMBOL_tIMAGINARY: /* "imaginary literal"  */
#line 1346 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6633 "parse.c"
        break;

    case YYSYMBOL_tCHAR: /* "char literal"  */
#line 1346 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6645 "parse.c"
        break;

    case YYSYMBOL_tNTH_REF: /* "numbered reference"  */
#line 1353 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "$%ld", ((*yyvaluep).node)->nd_nth);
#else
    rb_parser_printf(p, "%"PRIsVALUE, ((*yyvaluep).node));
#endif
}
#line 6657 "parse.c"
        break;

    case YYSYMBOL_tBACK_REF: /* "back reference"  */
#line 1360 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "$%c", (int)((*yyvaluep).node)->nd_nth);
#else
    rb_parser_printf(p, "%"PRIsVALUE, ((*yyvaluep).node));
#endif
}
#line 6669 "parse.c"
        break;

    case YYSYMBOL_tSTRING_CONTENT: /* "literal content"  */
#line 1346 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%+"PRIsVALUE, ((*yyvaluep).node)->nd_lit);
#else
    rb_parser_printf(p, "%+"PRIsVALUE, get_value(((*yyvaluep).node)));
#endif
}
#line 6681 "parse.c"
        break;

    case YYSYMBOL_tOP_ASGN: /* "operator-assignment"  */
#line 1339 "parse.y"
         {
#ifndef RIPPER
    rb_parser_printf(p, "%"PRIsVALUE, rb_id2str(((*yyvaluep).id)));
#else
    rb_parser_printf(p, "%"PRIsVALUE, RNODE(((*yyvaluep).id))->nd_rval);
#endif
}
#line 6693 "parse.c"
        break;

    case YYSYMBOL_top_compstmt: /* top_compstmt  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6706 "parse.c"
        break;

    case YYSYMBOL_top_stmts: /* top_stmts  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6719 "parse.c"
        break;

    case YYSYMBOL_top_stmt: /* top_stmt  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6732 "parse.c"
        break;

    case YYSYMBOL_begin_block: /* begin_block  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6745 "parse.c"
        break;

    case YYSYMBOL_bodystmt: /* bodystmt  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6758 "parse.c"
        break;

    case YYSYMBOL_compstmt: /* compstmt  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6771 "parse.c"
        break;

    case YYSYMBOL_stmts: /* stmts  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6784 "parse.c"
        break;

    case YYSYMBOL_stmt_or_begin: /* stmt_or_begin  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6797 "parse.c"
        break;

    case YYSYMBOL_stmt: /* stmt  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6810 "parse.c"
        break;

    case YYSYMBOL_command_asgn: /* command_asgn  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6823 "parse.c"
        break;

    case YYSYMBOL_command_rhs: /* command_rhs  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6836 "parse.c"
        break;

    case YYSYMBOL_expr: /* expr  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6849 "parse.c"
        break;

    case YYSYMBOL_def_name: /* def_name  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6862 "parse.c"
        break;

    case YYSYMBOL_defn_head: /* defn_head  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6875 "parse.c"
        break;

    case YYSYMBOL_defs_head: /* defs_head  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6888 "parse.c"
        break;

    case YYSYMBOL_expr_value: /* expr_value  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6901 "parse.c"
        break;

    case YYSYMBOL_expr_value_do: /* expr_value_do  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6914 "parse.c"
        break;

    case YYSYMBOL_command_call: /* command_call  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6927 "parse.c"
        break;

    case YYSYMBOL_block_command: /* block_command  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6940 "parse.c"
        break;

    case YYSYMBOL_cmd_brace_block: /* cmd_brace_block  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6953 "parse.c"
        break;

    case YYSYMBOL_fcall: /* fcall  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6966 "parse.c"
        break;

    case YYSYMBOL_command: /* command  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6979 "parse.c"
        break;

    case YYSYMBOL_mlhs: /* mlhs  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 6992 "parse.c"
        break;

    case YYSYMBOL_mlhs_inner: /* mlhs_inner  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7005 "parse.c"
        break;

    case YYSYMBOL_mlhs_basic: /* mlhs_basic  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7018 "parse.c"
        break;

    case YYSYMBOL_mlhs_item: /* mlhs_item  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7031 "parse.c"
        break;

    case YYSYMBOL_mlhs_head: /* mlhs_head  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7044 "parse.c"
        break;

    case YYSYMBOL_mlhs_post: /* mlhs_post  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7057 "parse.c"
        break;

    case YYSYMBOL_mlhs_node: /* mlhs_node  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7070 "parse.c"
        break;

    case YYSYMBOL_lhs: /* lhs  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7083 "parse.c"
        break;

    case YYSYMBOL_cpath: /* cpath  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7096 "parse.c"
        break;

    case YYSYMBOL_fitem: /* fitem  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7109 "parse.c"
        break;

    case YYSYMBOL_undef_list: /* undef_list  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7122 "parse.c"
        break;

    case YYSYMBOL_arg: /* arg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7135 "parse.c"
        break;

    case YYSYMBOL_rel_expr: /* rel_expr  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7148 "parse.c"
        break;

    case YYSYMBOL_arg_value: /* arg_value  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7161 "parse.c"
        break;

    case YYSYMBOL_aref_args: /* aref_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7174 "parse.c"
        break;

    case YYSYMBOL_arg_rhs: /* arg_rhs  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7187 "parse.c"
        break;

    case YYSYMBOL_paren_args: /* paren_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7200 "parse.c"
        break;

    case YYSYMBOL_opt_paren_args: /* opt_paren_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7213 "parse.c"
        break;

    case YYSYMBOL_opt_call_args: /* opt_call_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7226 "parse.c"
        break;

    case YYSYMBOL_call_args: /* call_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7239 "parse.c"
        break;

    case YYSYMBOL_command_args: /* command_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7252 "parse.c"
        break;

    case YYSYMBOL_block_arg: /* block_arg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7265 "parse.c"
        break;

    case YYSYMBOL_opt_block_arg: /* opt_block_arg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7278 "parse.c"
        break;

    case YYSYMBOL_args: /* args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7291 "parse.c"
        break;

    case YYSYMBOL_mrhs_arg: /* mrhs_arg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7304 "parse.c"
        break;

    case YYSYMBOL_mrhs: /* mrhs  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7317 "parse.c"
        break;

    case YYSYMBOL_primary: /* primary  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7330 "parse.c"
        break;

    case YYSYMBOL_primary_value: /* primary_value  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7343 "parse.c"
        break;

    case YYSYMBOL_if_tail: /* if_tail  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7356 "parse.c"
        break;

    case YYSYMBOL_opt_else: /* opt_else  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7369 "parse.c"
        break;

    case YYSYMBOL_for_var: /* for_var  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7382 "parse.c"
        break;

    case YYSYMBOL_f_marg: /* f_marg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7395 "parse.c"
        break;

    case YYSYMBOL_f_marg_list: /* f_marg_list  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7408 "parse.c"
        break;

    case YYSYMBOL_f_margs: /* f_margs  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7421 "parse.c"
        break;

    case YYSYMBOL_f_rest_marg: /* f_rest_marg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7434 "parse.c"
        break;

    case YYSYMBOL_block_args_tail: /* block_args_tail  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7447 "parse.c"
        break;

    case YYSYMBOL_opt_block_args_tail: /* opt_block_args_tail  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7460 "parse.c"
        break;

    case YYSYMBOL_block_param: /* block_param  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7473 "parse.c"
        break;

    case YYSYMBOL_opt_block_param: /* opt_block_param  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7486 "parse.c"
        break;

    case YYSYMBOL_block_param_def: /* block_param_def  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7499 "parse.c"
        break;

    case YYSYMBOL_opt_bv_decl: /* opt_bv_decl  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7512 "parse.c"
        break;

    case YYSYMBOL_bv_decls: /* bv_decls  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7525 "parse.c"
        break;

    case YYSYMBOL_bvar: /* bvar  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7538 "parse.c"
        break;

    case YYSYMBOL_lambda: /* lambda  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7551 "parse.c"
        break;

    case YYSYMBOL_f_larglist: /* f_larglist  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7564 "parse.c"
        break;

    case YYSYMBOL_lambda_body: /* lambda_body  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7577 "parse.c"
        break;

    case YYSYMBOL_do_block: /* do_block  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7590 "parse.c"
        break;

    case YYSYMBOL_block_call: /* block_call  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7603 "parse.c"
        break;

    case YYSYMBOL_method_call: /* method_call  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7616 "parse.c"
        break;

    case YYSYMBOL_brace_block: /* brace_block  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7629 "parse.c"
        break;

    case YYSYMBOL_brace_body: /* brace_body  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7642 "parse.c"
        break;

    case YYSYMBOL_do_body: /* do_body  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7655 "parse.c"
        break;

    case YYSYMBOL_case_args: /* case_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7668 "parse.c"
        break;

    case YYSYMBOL_case_body: /* case_body  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7681 "parse.c"
        break;

    case YYSYMBOL_cases: /* cases  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7694 "parse.c"
        break;

    case YYSYMBOL_p_case_body: /* p_case_body  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7707 "parse.c"
        break;

    case YYSYMBOL_p_cases: /* p_cases  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7720 "parse.c"
        break;

    case YYSYMBOL_p_top_expr: /* p_top_expr  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7733 "parse.c"
        break;

    case YYSYMBOL_p_top_expr_body: /* p_top_expr_body  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7746 "parse.c"
        break;

    case YYSYMBOL_p_expr: /* p_expr  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7759 "parse.c"
        break;

    case YYSYMBOL_p_as: /* p_as  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7772 "parse.c"
        break;

    case YYSYMBOL_p_alt: /* p_alt  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7785 "parse.c"
        break;

    case YYSYMBOL_p_expr_basic: /* p_expr_basic  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7798 "parse.c"
        break;

    case YYSYMBOL_p_args: /* p_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7811 "parse.c"
        break;

    case YYSYMBOL_p_args_head: /* p_args_head  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7824 "parse.c"
        break;

    case YYSYMBOL_p_args_tail: /* p_args_tail  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7837 "parse.c"
        break;

    case YYSYMBOL_p_find: /* p_find  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7850 "parse.c"
        break;

    case YYSYMBOL_p_args_post: /* p_args_post  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7863 "parse.c"
        break;

    case YYSYMBOL_p_arg: /* p_arg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7876 "parse.c"
        break;

    case YYSYMBOL_p_kwargs: /* p_kwargs  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7889 "parse.c"
        break;

    case YYSYMBOL_p_kwarg: /* p_kwarg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7902 "parse.c"
        break;

    case YYSYMBOL_p_kw: /* p_kw  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7915 "parse.c"
        break;

    case YYSYMBOL_p_value: /* p_value  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7928 "parse.c"
        break;

    case YYSYMBOL_p_primitive: /* p_primitive  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7941 "parse.c"
        break;

    case YYSYMBOL_p_variable: /* p_variable  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7954 "parse.c"
        break;

    case YYSYMBOL_p_var_ref: /* p_var_ref  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7967 "parse.c"
        break;

    case YYSYMBOL_p_expr_ref: /* p_expr_ref  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7980 "parse.c"
        break;

    case YYSYMBOL_p_const: /* p_const  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 7993 "parse.c"
        break;

    case YYSYMBOL_opt_rescue: /* opt_rescue  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8006 "parse.c"
        break;

    case YYSYMBOL_exc_list: /* exc_list  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8019 "parse.c"
        break;

    case YYSYMBOL_exc_var: /* exc_var  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8032 "parse.c"
        break;

    case YYSYMBOL_opt_ensure: /* opt_ensure  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8045 "parse.c"
        break;

    case YYSYMBOL_literal: /* literal  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8058 "parse.c"
        break;

    case YYSYMBOL_strings: /* strings  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8071 "parse.c"
        break;

    case YYSYMBOL_string: /* string  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8084 "parse.c"
        break;

    case YYSYMBOL_string1: /* string1  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8097 "parse.c"
        break;

    case YYSYMBOL_xstring: /* xstring  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8110 "parse.c"
        break;

    case YYSYMBOL_regexp: /* regexp  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8123 "parse.c"
        break;

    case YYSYMBOL_words: /* words  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8136 "parse.c"
        break;

    case YYSYMBOL_word_list: /* word_list  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8149 "parse.c"
        break;

    case YYSYMBOL_word: /* word  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8162 "parse.c"
        break;

    case YYSYMBOL_symbols: /* symbols  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8175 "parse.c"
        break;

    case YYSYMBOL_symbol_list: /* symbol_list  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8188 "parse.c"
        break;

    case YYSYMBOL_qwords: /* qwords  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8201 "parse.c"
        break;

    case YYSYMBOL_qsymbols: /* qsymbols  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8214 "parse.c"
        break;

    case YYSYMBOL_qword_list: /* qword_list  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8227 "parse.c"
        break;

    case YYSYMBOL_qsym_list: /* qsym_list  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8240 "parse.c"
        break;

    case YYSYMBOL_string_contents: /* string_contents  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8253 "parse.c"
        break;

    case YYSYMBOL_xstring_contents: /* xstring_contents  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8266 "parse.c"
        break;

    case YYSYMBOL_regexp_contents: /* regexp_contents  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8279 "parse.c"
        break;

    case YYSYMBOL_string_content: /* string_content  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8292 "parse.c"
        break;

    case YYSYMBOL_string_dvar: /* string_dvar  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8305 "parse.c"
        break;

    case YYSYMBOL_symbol: /* symbol  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8318 "parse.c"
        break;

    case YYSYMBOL_ssym: /* ssym  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8331 "parse.c"
        break;

    case YYSYMBOL_dsym: /* dsym  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8344 "parse.c"
        break;

    case YYSYMBOL_numeric: /* numeric  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8357 "parse.c"
        break;

    case YYSYMBOL_simple_numeric: /* simple_numeric  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8370 "parse.c"
        break;

    case YYSYMBOL_var_ref: /* var_ref  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8383 "parse.c"
        break;

    case YYSYMBOL_var_lhs: /* var_lhs  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8396 "parse.c"
        break;

    case YYSYMBOL_backref: /* backref  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8409 "parse.c"
        break;

    case YYSYMBOL_superclass: /* superclass  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8422 "parse.c"
        break;

    case YYSYMBOL_f_opt_paren_args: /* f_opt_paren_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8435 "parse.c"
        break;

    case YYSYMBOL_f_paren_args: /* f_paren_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8448 "parse.c"
        break;

    case YYSYMBOL_f_arglist: /* f_arglist  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8461 "parse.c"
        break;

    case YYSYMBOL_args_tail: /* args_tail  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8474 "parse.c"
        break;

    case YYSYMBOL_opt_args_tail: /* opt_args_tail  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8487 "parse.c"
        break;

    case YYSYMBOL_f_args: /* f_args  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8500 "parse.c"
        break;

    case YYSYMBOL_f_arg_item: /* f_arg_item  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8513 "parse.c"
        break;

    case YYSYMBOL_f_arg: /* f_arg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8526 "parse.c"
        break;

    case YYSYMBOL_f_kw: /* f_kw  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8539 "parse.c"
        break;

    case YYSYMBOL_f_block_kw: /* f_block_kw  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8552 "parse.c"
        break;

    case YYSYMBOL_f_block_kwarg: /* f_block_kwarg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8565 "parse.c"
        break;

    case YYSYMBOL_f_kwarg: /* f_kwarg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8578 "parse.c"
        break;

    case YYSYMBOL_f_opt: /* f_opt  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8591 "parse.c"
        break;

    case YYSYMBOL_f_block_opt: /* f_block_opt  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8604 "parse.c"
        break;

    case YYSYMBOL_f_block_optarg: /* f_block_optarg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8617 "parse.c"
        break;

    case YYSYMBOL_f_optarg: /* f_optarg  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8630 "parse.c"
        break;

    case YYSYMBOL_singleton: /* singleton  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8643 "parse.c"
        break;

    case YYSYMBOL_assoc_list: /* assoc_list  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8656 "parse.c"
        break;

    case YYSYMBOL_assocs: /* assocs  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8669 "parse.c"
        break;

    case YYSYMBOL_assoc: /* assoc  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8682 "parse.c"
        break;

    case YYSYMBOL_none: /* none  */
#line 1331 "parse.y"
         {
#ifndef RIPPER
    if (((*yyvaluep).node)) {
	rb_parser_printf(p, "%s", ruby_node_name(nd_type(((*yyvaluep).node))));
    }
#else
#endif
}
#line 8695 "parse.c"
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
#line 1371 "parse.y"
{
    RUBY_SET_YYLLOC_OF_NONE(yylloc);
}

#line 9190 "parse.c"

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
#line 1580 "parse.y"
                   {
			SET_LEX_STATE(EXPR_BEG);
			local_push(p, ifndef_ripper(1)+0);
		    }
#line 9406 "parse.c"
    break;

  case 3: /* program: $@1 top_compstmt  */
#line 1585 "parse.y"
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
#line 9430 "parse.c"
    break;

  case 4: /* top_compstmt: top_stmts opt_terms  */
#line 1607 "parse.y"
                    {
			(yyval.node) = void_stmts(p, (yyvsp[-1].node));
		    }
#line 9438 "parse.c"
    break;

  case 5: /* top_stmts: none  */
#line 1613 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, void_stmt!) %*/
		    }
#line 9449 "parse.c"
    break;

  case 6: /* top_stmts: top_stmt  */
#line 1620 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = newline_node((yyvsp[0].node));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, $1) %*/
		    }
#line 9460 "parse.c"
    break;

  case 7: /* top_stmts: top_stmts terms top_stmt  */
#line 1627 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = block_append(p, (yyvsp[-2].node), newline_node((yyvsp[0].node)));
		    /*% %*/
		    /*% ripper: stmts_add!($1, $3) %*/
		    }
#line 9471 "parse.c"
    break;

  case 9: /* top_stmt: "`BEGIN'" begin_block  */
#line 1637 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9479 "parse.c"
    break;

  case 10: /* begin_block: '{' top_compstmt '}'  */
#line 1643 "parse.y"
                    {
		    /*%%%*/
			p->eval_tree_begin = block_append(p, p->eval_tree_begin,
							  NEW_BEGIN((yyvsp[-1].node), &(yyloc)));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: BEGIN!($2) %*/
		    }
#line 9492 "parse.c"
    break;

  case 11: /* $@2: %empty  */
#line 1655 "parse.y"
                         {if (!(yyvsp[-1].node)) {yyerror1(&(yylsp[0]), "else without rescue is useless");}}
#line 9498 "parse.c"
    break;

  case 12: /* bodystmt: compstmt opt_rescue k_else $@2 compstmt opt_ensure  */
#line 1658 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_bodystmt(p, (yyvsp[-5].node), (yyvsp[-4].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: bodystmt!(escape_Qundef($1), escape_Qundef($2), escape_Qundef($5), escape_Qundef($6)) %*/
		    }
#line 9509 "parse.c"
    break;

  case 13: /* bodystmt: compstmt opt_rescue opt_ensure  */
#line 1667 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_bodystmt(p, (yyvsp[-2].node), (yyvsp[-1].node), 0, (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: bodystmt!(escape_Qundef($1), escape_Qundef($2), Qnil, escape_Qundef($3)) %*/
		    }
#line 9520 "parse.c"
    break;

  case 14: /* compstmt: stmts opt_terms  */
#line 1676 "parse.y"
                    {
			(yyval.node) = void_stmts(p, (yyvsp[-1].node));
		    }
#line 9528 "parse.c"
    break;

  case 15: /* stmts: none  */
#line 1682 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, void_stmt!) %*/
		    }
#line 9539 "parse.c"
    break;

  case 16: /* stmts: stmt_or_begin  */
#line 1689 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = newline_node((yyvsp[0].node));
		    /*% %*/
		    /*% ripper: stmts_add!(stmts_new!, $1) %*/
		    }
#line 9550 "parse.c"
    break;

  case 17: /* stmts: stmts terms stmt_or_begin  */
#line 1696 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = block_append(p, (yyvsp[-2].node), newline_node((yyvsp[0].node)));
		    /*% %*/
		    /*% ripper: stmts_add!($1, $3) %*/
		    }
#line 9561 "parse.c"
    break;

  case 18: /* stmt_or_begin: stmt  */
#line 1705 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9569 "parse.c"
    break;

  case 19: /* $@3: %empty  */
#line 1709 "parse.y"
                    {
			yyerror1(&(yylsp[0]), "BEGIN is permitted only at toplevel");
		    }
#line 9577 "parse.c"
    break;

  case 20: /* stmt_or_begin: "`BEGIN'" $@3 begin_block  */
#line 1713 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9585 "parse.c"
    break;

  case 21: /* $@4: %empty  */
#line 1718 "parse.y"
                                      {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 9591 "parse.c"
    break;

  case 22: /* stmt: "`alias'" fitem $@4 fitem  */
#line 1719 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_ALIAS((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: alias!($2, $4) %*/
		    }
#line 9602 "parse.c"
    break;

  case 23: /* stmt: "`alias'" "global variable" "global variable"  */
#line 1726 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_VALIAS((yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_alias!($2, $3) %*/
		    }
#line 9613 "parse.c"
    break;

  case 24: /* stmt: "`alias'" "global variable" "back reference"  */
#line 1733 "parse.y"
                    {
		    /*%%%*/
			char buf[2];
			buf[0] = '$';
			buf[1] = (char)(yyvsp[0].node)->nd_nth;
			(yyval.node) = NEW_VALIAS((yyvsp[-1].id), rb_intern2(buf, 2), &(yyloc));
		    /*% %*/
		    /*% ripper: var_alias!($2, $3) %*/
		    }
#line 9627 "parse.c"
    break;

  case 25: /* stmt: "`alias'" "global variable" "numbered reference"  */
#line 1743 "parse.y"
                    {
			static const char mesg[] = "can't make alias for the number variables";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: alias_error!(ERR_MESG(), $3) %*/
		    }
#line 9640 "parse.c"
    break;

  case 26: /* stmt: "`undef'" undef_list  */
#line 1752 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: undef!($2) %*/
		    }
#line 9651 "parse.c"
    break;

  case 27: /* stmt: stmt "`if' modifier" expr_value  */
#line 1759 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[0].node), remove_begin((yyvsp[-2].node)), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: if_mod!($3, $1) %*/
		    }
#line 9663 "parse.c"
    break;

  case 28: /* stmt: stmt "`unless' modifier" expr_value  */
#line 1767 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_unless(p, (yyvsp[0].node), remove_begin((yyvsp[-2].node)), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: unless_mod!($3, $1) %*/
		    }
#line 9675 "parse.c"
    break;

  case 29: /* stmt: stmt "`while' modifier" expr_value  */
#line 1775 "parse.y"
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
#line 9691 "parse.c"
    break;

  case 30: /* stmt: stmt "`until' modifier" expr_value  */
#line 1787 "parse.y"
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
#line 9707 "parse.c"
    break;

  case 31: /* stmt: stmt "`rescue' modifier" stmt  */
#line 1799 "parse.y"
                    {
		    /*%%%*/
			NODE *resq;
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			resq = NEW_RESBODY(0, remove_begin((yyvsp[0].node)), 0, &loc);
			(yyval.node) = NEW_RESCUE(remove_begin((yyvsp[-2].node)), resq, 0, &(yyloc));
		    /*% %*/
		    /*% ripper: rescue_mod!($1, $3) %*/
		    }
#line 9721 "parse.c"
    break;

  case 32: /* stmt: "`END'" '{' compstmt '}'  */
#line 1809 "parse.y"
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
#line 9739 "parse.c"
    break;

  case 34: /* stmt: mlhs '=' lex_ctxt command_call  */
#line 1824 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: massign!($1, $4) %*/
		    }
#line 9751 "parse.c"
    break;

  case 35: /* stmt: lhs '=' lex_ctxt mrhs  */
#line 1832 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: assign!($1, $4) %*/
		    }
#line 9762 "parse.c"
    break;

  case 36: /* stmt: mlhs '=' lex_ctxt mrhs_arg "`rescue' modifier" stmt  */
#line 1839 "parse.y"
                    {
                    /*%%%*/
                        YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			(yyval.node) = node_assign(p, (yyvsp[-5].node), NEW_RESCUE((yyvsp[-2].node), NEW_RESBODY(0, remove_begin((yyvsp[0].node)), 0, &loc), 0, &(yyloc)), (yyvsp[-3].ctxt), &(yyloc));
                    /*% %*/
                    /*% ripper: massign!($1, rescue_mod!($4, $6)) %*/
                    }
#line 9774 "parse.c"
    break;

  case 37: /* stmt: mlhs '=' lex_ctxt mrhs_arg  */
#line 1847 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: massign!($1, $4) %*/
		    }
#line 9785 "parse.c"
    break;

  case 39: /* stmt: error  */
#line 1855 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_ERROR(&(yyloc));
		    /*% %*/
		    }
#line 9795 "parse.c"
    break;

  case 40: /* command_asgn: lhs '=' lex_ctxt command_rhs  */
#line 1863 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: assign!($1, $4) %*/
		    }
#line 9806 "parse.c"
    break;

  case 41: /* command_asgn: var_lhs "operator-assignment" lex_ctxt command_rhs  */
#line 1870 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_op_assign(p, (yyvsp[-3].node), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!($1, $2, $4) %*/
		    }
#line 9817 "parse.c"
    break;

  case 42: /* command_asgn: primary_value '[' opt_call_args rbracket "operator-assignment" lex_ctxt command_rhs  */
#line 1877 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_ary_op_assign(p, (yyvsp[-6].node), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[0].node), &(yylsp[-4]), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(aref_field!($1, escape_Qundef($3)), $5, $7) %*/

		    }
#line 9829 "parse.c"
    break;

  case 43: /* command_asgn: primary_value call_op "local variable or method" "operator-assignment" lex_ctxt command_rhs  */
#line 1885 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 9840 "parse.c"
    break;

  case 44: /* command_asgn: primary_value call_op "constant" "operator-assignment" lex_ctxt command_rhs  */
#line 1892 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 9851 "parse.c"
    break;

  case 45: /* command_asgn: primary_value "::" "constant" "operator-assignment" lex_ctxt command_rhs  */
#line 1899 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-5]), &(yylsp[-3]));
			(yyval.node) = new_const_op_assign(p, NEW_COLON2((yyvsp[-5].node), (yyvsp[-3].id), &loc), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(const_path_field!($1, $3), $4, $6) %*/
		    }
#line 9863 "parse.c"
    break;

  case 46: /* command_asgn: primary_value "::" "local variable or method" "operator-assignment" lex_ctxt command_rhs  */
#line 1907 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), ID2VAL(idCOLON2), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 9874 "parse.c"
    break;

  case 47: /* command_asgn: defn_head f_opt_paren_args '=' command  */
#line 1914 "parse.y"
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
#line 9889 "parse.c"
    break;

  case 48: /* command_asgn: defn_head f_opt_paren_args '=' command "`rescue' modifier" arg  */
#line 1925 "parse.y"
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
#line 9905 "parse.c"
    break;

  case 49: /* command_asgn: defs_head f_opt_paren_args '=' command  */
#line 1937 "parse.y"
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
#line 9922 "parse.c"
    break;

  case 50: /* command_asgn: defs_head f_opt_paren_args '=' command "`rescue' modifier" arg  */
#line 1950 "parse.y"
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
#line 9940 "parse.c"
    break;

  case 51: /* command_asgn: backref "operator-assignment" lex_ctxt command_rhs  */
#line 1964 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[-3].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), assign!(var_field(p, $1), $4)) %*/
		    }
#line 9952 "parse.c"
    break;

  case 52: /* command_rhs: command_call  */
#line 1974 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 9961 "parse.c"
    break;

  case 53: /* command_rhs: command_call "`rescue' modifier" stmt  */
#line 1979 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			value_expr((yyvsp[-2].node));
			(yyval.node) = NEW_RESCUE((yyvsp[-2].node), NEW_RESBODY(0, remove_begin((yyvsp[0].node)), 0, &loc), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: rescue_mod!($1, $3) %*/
		    }
#line 9974 "parse.c"
    break;

  case 56: /* expr: expr "`and'" expr  */
#line 1992 "parse.y"
                    {
			(yyval.node) = logop(p, idAND, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9982 "parse.c"
    break;

  case 57: /* expr: expr "`or'" expr  */
#line 1996 "parse.y"
                    {
			(yyval.node) = logop(p, idOR, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 9990 "parse.c"
    break;

  case 58: /* expr: "`not'" opt_nl expr  */
#line 2000 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[0].node), &(yylsp[0])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 9998 "parse.c"
    break;

  case 59: /* expr: '!' command_call  */
#line 2004 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[0].node), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 10006 "parse.c"
    break;

  case 60: /* @5: %empty  */
#line 2008 "parse.y"
                    {
			value_expr((yyvsp[-1].node));
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 10019 "parse.c"
    break;

  case 61: /* @6: %empty  */
#line 2016 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
		    }
#line 10027 "parse.c"
    break;

  case 62: /* expr: arg "=>" @5 @6 p_top_expr_body  */
#line 2020 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-1].tbl));
			pop_pvtbl(p, (yyvsp[-2].tbl));
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
		    /*%%%*/
			(yyval.node) = NEW_CASE3((yyvsp[-4].node), NEW_IN((yyvsp[0].node), 0, 0, &(yylsp[0])), &(yyloc));
		    /*% %*/
		    /*% ripper: case!($1, in!($5, Qnil, Qnil)) %*/
		    }
#line 10041 "parse.c"
    break;

  case 63: /* @7: %empty  */
#line 2030 "parse.y"
                    {
			value_expr((yyvsp[-1].node));
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 10054 "parse.c"
    break;

  case 64: /* @8: %empty  */
#line 2038 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
		    }
#line 10062 "parse.c"
    break;

  case 65: /* expr: arg "`in'" @7 @8 p_top_expr_body  */
#line 2042 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-1].tbl));
			pop_pvtbl(p, (yyvsp[-2].tbl));
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
		    /*%%%*/
			(yyval.node) = NEW_CASE3((yyvsp[-4].node), NEW_IN((yyvsp[0].node), NEW_TRUE(&(yylsp[0])), NEW_FALSE(&(yylsp[0])), &(yylsp[0])), &(yyloc));
		    /*% %*/
		    /*% ripper: case!($1, in!($5, Qnil, Qnil)) %*/
		    }
#line 10076 "parse.c"
    break;

  case 67: /* def_name: fname  */
#line 2055 "parse.y"
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
#line 10095 "parse.c"
    break;

  case 68: /* defn_head: k_def def_name  */
#line 2072 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    /*%%%*/
			(yyval.node) = NEW_NODE(NODE_DEFN, 0, (yyval.node)->nd_mid, (yyval.node), &(yyloc));
		    /*% %*/
		    }
#line 10106 "parse.c"
    break;

  case 69: /* $@9: %empty  */
#line 2081 "parse.y"
                    {
			SET_LEX_STATE(EXPR_FNAME);
			p->ctxt.in_argdef = 1;
		    }
#line 10115 "parse.c"
    break;

  case 70: /* defs_head: k_def singleton dot_or_colon $@9 def_name  */
#line 2086 "parse.y"
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
#line 10131 "parse.c"
    break;

  case 71: /* expr_value: expr  */
#line 2100 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 10140 "parse.c"
    break;

  case 72: /* expr_value: error  */
#line 2105 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_ERROR(&(yyloc));
		    /*% %*/
		    }
#line 10150 "parse.c"
    break;

  case 73: /* $@10: %empty  */
#line 2112 "parse.y"
                  {COND_PUSH(1);}
#line 10156 "parse.c"
    break;

  case 74: /* $@11: %empty  */
#line 2112 "parse.y"
                                                {COND_POP();}
#line 10162 "parse.c"
    break;

  case 75: /* expr_value_do: $@10 expr_value do $@11  */
#line 2113 "parse.y"
                    {
			(yyval.node) = (yyvsp[-2].node);
		    }
#line 10170 "parse.c"
    break;

  case 79: /* block_command: block_call call_op2 operation2 command_args  */
#line 2124 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, $2, $3), $4) %*/
		    }
#line 10181 "parse.c"
    break;

  case 80: /* cmd_brace_block: "{ arg" brace_body '}'  */
#line 2133 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 10193 "parse.c"
    break;

  case 81: /* fcall: operation  */
#line 2143 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_FCALL((yyvsp[0].id), 0, &(yyloc));
			nd_set_line((yyval.node), p->tokline);
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 10205 "parse.c"
    break;

  case 82: /* command: fcall command_args  */
#line 2153 "parse.y"
                    {
		    /*%%%*/
			(yyvsp[-1].node)->nd_args = (yyvsp[0].node);
			nd_set_last_loc((yyvsp[-1].node), (yylsp[0]).end_pos);
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: command!($1, $2) %*/
		    }
#line 10218 "parse.c"
    break;

  case 83: /* command: fcall command_args cmd_brace_block  */
#line 2162 "parse.y"
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
#line 10233 "parse.c"
    break;

  case 84: /* command: primary_value call_op operation2 command_args  */
#line 2173 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), Qnull, &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: command_call!($1, $2, $3, $4) %*/
		    }
#line 10244 "parse.c"
    break;

  case 85: /* command: primary_value call_op operation2 command_args cmd_brace_block  */
#line 2180 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-3].id), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(command_call!($1, $2, $3, $4), $5) %*/
		    }
#line 10255 "parse.c"
    break;

  case 86: /* command: primary_value "::" operation2 command_args  */
#line 2187 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), Qnull, &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: command_call!($1, $2, $3, $4) %*/
		    }
#line 10266 "parse.c"
    break;

  case 87: /* command: primary_value "::" operation2 command_args cmd_brace_block  */
#line 2194 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, ID2VAL(idCOLON2), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(command_call!($1, $2, $3, $4), $5) %*/
		   }
#line 10277 "parse.c"
    break;

  case 88: /* command: "`super'" command_args  */
#line 2201 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SUPER((yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: super!($2) %*/
		    }
#line 10289 "parse.c"
    break;

  case 89: /* command: "`yield'" command_args  */
#line 2209 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_yield(p, (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: yield!($2) %*/
		    }
#line 10301 "parse.c"
    break;

  case 90: /* command: k_return call_args  */
#line 2217 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RETURN(ret_args(p, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: return!($2) %*/
		    }
#line 10312 "parse.c"
    break;

  case 91: /* command: "`break'" call_args  */
#line 2224 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BREAK(ret_args(p, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: break!($2) %*/
		    }
#line 10323 "parse.c"
    break;

  case 92: /* command: "`next'" call_args  */
#line 2231 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_NEXT(ret_args(p, (yyvsp[0].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: next!($2) %*/
		    }
#line 10334 "parse.c"
    break;

  case 94: /* mlhs: "(" mlhs_inner rparen  */
#line 2241 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 10345 "parse.c"
    break;

  case 96: /* mlhs_inner: "(" mlhs_inner rparen  */
#line 2251 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(NEW_LIST((yyvsp[-1].node), &(yyloc)), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 10356 "parse.c"
    break;

  case 97: /* mlhs_basic: mlhs_head  */
#line 2260 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[0].node), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 10367 "parse.c"
    break;

  case 98: /* mlhs_basic: mlhs_head mlhs_item  */
#line 2267 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(list_append(p, (yyvsp[-1].node),(yyvsp[0].node)), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $2) %*/
		    }
#line 10378 "parse.c"
    break;

  case 99: /* mlhs_basic: mlhs_head "*" mlhs_node  */
#line 2274 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!($1, $3) %*/
		    }
#line 10389 "parse.c"
    break;

  case 100: /* mlhs_basic: mlhs_head "*" mlhs_node ',' mlhs_post  */
#line 2281 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-4].node), NEW_POSTARG((yyvsp[-2].node),(yyvsp[0].node),&(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!($1, $3), $5) %*/
		    }
#line 10400 "parse.c"
    break;

  case 101: /* mlhs_basic: mlhs_head "*"  */
#line 2288 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-1].node), NODE_SPECIAL_NO_NAME_REST, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!($1, Qnil) %*/
		    }
#line 10411 "parse.c"
    break;

  case 102: /* mlhs_basic: mlhs_head "*" ',' mlhs_post  */
#line 2295 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-3].node), NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!($1, Qnil), $4) %*/
		    }
#line 10422 "parse.c"
    break;

  case 103: /* mlhs_basic: "*" mlhs_node  */
#line 2302 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!(mlhs_new!, $2) %*/
		    }
#line 10433 "parse.c"
    break;

  case 104: /* mlhs_basic: "*" mlhs_node ',' mlhs_post  */
#line 2309 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NEW_POSTARG((yyvsp[-2].node),(yyvsp[0].node),&(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!(mlhs_new!, $2), $4) %*/
		    }
#line 10444 "parse.c"
    break;

  case 105: /* mlhs_basic: "*"  */
#line 2316 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NODE_SPECIAL_NO_NAME_REST, &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!(mlhs_new!, Qnil) %*/
		    }
#line 10455 "parse.c"
    break;

  case 106: /* mlhs_basic: "*" ',' mlhs_post  */
#line 2323 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NEW_POSTARG(NODE_SPECIAL_NO_NAME_REST, (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!(mlhs_new!, Qnil), $3) %*/
		    }
#line 10466 "parse.c"
    break;

  case 108: /* mlhs_item: "(" mlhs_inner rparen  */
#line 2333 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 10477 "parse.c"
    break;

  case 109: /* mlhs_head: mlhs_item ','  */
#line 2342 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[-1].node), &(yylsp[-1]));
		    /*% %*/
		    /*% ripper: mlhs_add!(mlhs_new!, $1) %*/
		    }
#line 10488 "parse.c"
    break;

  case 110: /* mlhs_head: mlhs_head mlhs_item ','  */
#line 2349 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $2) %*/
		    }
#line 10499 "parse.c"
    break;

  case 111: /* mlhs_post: mlhs_item  */
#line 2358 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add!(mlhs_new!, $1) %*/
		    }
#line 10510 "parse.c"
    break;

  case 112: /* mlhs_post: mlhs_post ',' mlhs_item  */
#line 2365 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $3) %*/
		    }
#line 10521 "parse.c"
    break;

  case 113: /* mlhs_node: user_variable  */
#line 2374 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 10532 "parse.c"
    break;

  case 114: /* mlhs_node: keyword_variable  */
#line 2381 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 10543 "parse.c"
    break;

  case 115: /* mlhs_node: primary_value '[' opt_call_args rbracket  */
#line 2388 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = aryset(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: aref_field!($1, escape_Qundef($3)) %*/
		    }
#line 10554 "parse.c"
    break;

  case 116: /* mlhs_node: primary_value call_op "local variable or method"  */
#line 2395 "parse.y"
                    {
			if ((yyvsp[-1].id) == tANDDOT) {
			    yyerror1(&(yylsp[-1]), "&. inside multiple assignment destination");
			}
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 10568 "parse.c"
    break;

  case 117: /* mlhs_node: primary_value "::" "local variable or method"  */
#line 2405 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), idCOLON2, (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_field!($1, $3) %*/
		    }
#line 10579 "parse.c"
    break;

  case 118: /* mlhs_node: primary_value call_op "constant"  */
#line 2412 "parse.y"
                    {
			if ((yyvsp[-1].id) == tANDDOT) {
			    yyerror1(&(yylsp[-1]), "&. inside multiple assignment destination");
			}
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 10593 "parse.c"
    break;

  case 119: /* mlhs_node: primary_value "::" "constant"  */
#line 2422 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, const_path_field!($1, $3)) %*/
		    }
#line 10604 "parse.c"
    break;

  case 120: /* mlhs_node: ":: at EXPR_BEG" "constant"  */
#line 2429 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON3((yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, top_const_field!($2)) %*/
		    }
#line 10615 "parse.c"
    break;

  case 121: /* mlhs_node: backref  */
#line 2436 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[0].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), var_field(p, $1)) %*/
		    }
#line 10627 "parse.c"
    break;

  case 122: /* lhs: user_variable  */
#line 2446 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 10638 "parse.c"
    break;

  case 123: /* lhs: keyword_variable  */
#line 2453 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 10649 "parse.c"
    break;

  case 124: /* lhs: primary_value '[' opt_call_args rbracket  */
#line 2460 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = aryset(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: aref_field!($1, escape_Qundef($3)) %*/
		    }
#line 10660 "parse.c"
    break;

  case 125: /* lhs: primary_value call_op "local variable or method"  */
#line 2467 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 10671 "parse.c"
    break;

  case 126: /* lhs: primary_value "::" "local variable or method"  */
#line 2474 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), idCOLON2, (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 10682 "parse.c"
    break;

  case 127: /* lhs: primary_value call_op "constant"  */
#line 2481 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = attrset(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: field!($1, $2, $3) %*/
		    }
#line 10693 "parse.c"
    break;

  case 128: /* lhs: primary_value "::" "constant"  */
#line 2488 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, const_path_field!($1, $3)) %*/
		    }
#line 10704 "parse.c"
    break;

  case 129: /* lhs: ":: at EXPR_BEG" "constant"  */
#line 2495 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = const_decl(p, NEW_COLON3((yyvsp[0].id), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: const_decl(p, top_const_field!($2)) %*/
		    }
#line 10715 "parse.c"
    break;

  case 130: /* lhs: backref  */
#line 2502 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[0].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), var_field(p, $1)) %*/
		    }
#line 10727 "parse.c"
    break;

  case 131: /* cname: "local variable or method"  */
#line 2512 "parse.y"
                    {
			static const char mesg[] = "class/module name must be CONSTANT";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
		    /*% %*/
		    /*% ripper[error]: class_name_error!(ERR_MESG(), $1) %*/
		    }
#line 10739 "parse.c"
    break;

  case 133: /* cpath: ":: at EXPR_BEG" cname  */
#line 2523 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON3((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: top_const_ref!($2) %*/
		    }
#line 10750 "parse.c"
    break;

  case 134: /* cpath: cname  */
#line 2530 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2(0, (yyval.node), &(yyloc));
		    /*% %*/
		    /*% ripper: const_ref!($1) %*/
		    }
#line 10761 "parse.c"
    break;

  case 135: /* cpath: primary_value "::" cname  */
#line 2537 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_ref!($1, $3) %*/
		    }
#line 10772 "parse.c"
    break;

  case 139: /* fname: op  */
#line 2549 "parse.y"
                    {
			SET_LEX_STATE(EXPR_ENDFN);
			(yyval.id) = (yyvsp[0].id);
		    }
#line 10781 "parse.c"
    break;

  case 141: /* fitem: fname  */
#line 2557 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIT(ID2SYM((yyvsp[0].id)), &(yyloc));
		    /*% %*/
		    /*% ripper: symbol_literal!($1) %*/
		    }
#line 10792 "parse.c"
    break;

  case 143: /* undef_list: fitem  */
#line 2567 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_UNDEF((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 10803 "parse.c"
    break;

  case 144: /* $@12: %empty  */
#line 2573 "parse.y"
                                 {SET_LEX_STATE(EXPR_FNAME|EXPR_FITEM);}
#line 10809 "parse.c"
    break;

  case 145: /* undef_list: undef_list ',' $@12 fitem  */
#line 2574 "parse.y"
                    {
		    /*%%%*/
			NODE *undef = NEW_UNDEF((yyvsp[0].node), &(yylsp[0]));
			(yyval.node) = block_append(p, (yyvsp[-3].node), undef);
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($4)) %*/
		    }
#line 10821 "parse.c"
    break;

  case 146: /* op: '|'  */
#line 2583 "parse.y"
                                { ifndef_ripper((yyval.id) = '|'); }
#line 10827 "parse.c"
    break;

  case 147: /* op: '^'  */
#line 2584 "parse.y"
                                { ifndef_ripper((yyval.id) = '^'); }
#line 10833 "parse.c"
    break;

  case 148: /* op: '&'  */
#line 2585 "parse.y"
                                { ifndef_ripper((yyval.id) = '&'); }
#line 10839 "parse.c"
    break;

  case 149: /* op: "<=>"  */
#line 2586 "parse.y"
                                { ifndef_ripper((yyval.id) = tCMP); }
#line 10845 "parse.c"
    break;

  case 150: /* op: "=="  */
#line 2587 "parse.y"
                                { ifndef_ripper((yyval.id) = tEQ); }
#line 10851 "parse.c"
    break;

  case 151: /* op: "==="  */
#line 2588 "parse.y"
                                { ifndef_ripper((yyval.id) = tEQQ); }
#line 10857 "parse.c"
    break;

  case 152: /* op: "=~"  */
#line 2589 "parse.y"
                                { ifndef_ripper((yyval.id) = tMATCH); }
#line 10863 "parse.c"
    break;

  case 153: /* op: "!~"  */
#line 2590 "parse.y"
                                { ifndef_ripper((yyval.id) = tNMATCH); }
#line 10869 "parse.c"
    break;

  case 154: /* op: '>'  */
#line 2591 "parse.y"
                                { ifndef_ripper((yyval.id) = '>'); }
#line 10875 "parse.c"
    break;

  case 155: /* op: ">="  */
#line 2592 "parse.y"
                                { ifndef_ripper((yyval.id) = tGEQ); }
#line 10881 "parse.c"
    break;

  case 156: /* op: '<'  */
#line 2593 "parse.y"
                                { ifndef_ripper((yyval.id) = '<'); }
#line 10887 "parse.c"
    break;

  case 157: /* op: "<="  */
#line 2594 "parse.y"
                                { ifndef_ripper((yyval.id) = tLEQ); }
#line 10893 "parse.c"
    break;

  case 158: /* op: "!="  */
#line 2595 "parse.y"
                                { ifndef_ripper((yyval.id) = tNEQ); }
#line 10899 "parse.c"
    break;

  case 159: /* op: "<<"  */
#line 2596 "parse.y"
                                { ifndef_ripper((yyval.id) = tLSHFT); }
#line 10905 "parse.c"
    break;

  case 160: /* op: ">>"  */
#line 2597 "parse.y"
                                { ifndef_ripper((yyval.id) = tRSHFT); }
#line 10911 "parse.c"
    break;

  case 161: /* op: '+'  */
#line 2598 "parse.y"
                                { ifndef_ripper((yyval.id) = '+'); }
#line 10917 "parse.c"
    break;

  case 162: /* op: '-'  */
#line 2599 "parse.y"
                                { ifndef_ripper((yyval.id) = '-'); }
#line 10923 "parse.c"
    break;

  case 163: /* op: '*'  */
#line 2600 "parse.y"
                                { ifndef_ripper((yyval.id) = '*'); }
#line 10929 "parse.c"
    break;

  case 164: /* op: "*"  */
#line 2601 "parse.y"
                                { ifndef_ripper((yyval.id) = '*'); }
#line 10935 "parse.c"
    break;

  case 165: /* op: '/'  */
#line 2602 "parse.y"
                                { ifndef_ripper((yyval.id) = '/'); }
#line 10941 "parse.c"
    break;

  case 166: /* op: '%'  */
#line 2603 "parse.y"
                                { ifndef_ripper((yyval.id) = '%'); }
#line 10947 "parse.c"
    break;

  case 167: /* op: "**"  */
#line 2604 "parse.y"
                                { ifndef_ripper((yyval.id) = tPOW); }
#line 10953 "parse.c"
    break;

  case 168: /* op: "**arg"  */
#line 2605 "parse.y"
                                { ifndef_ripper((yyval.id) = tDSTAR); }
#line 10959 "parse.c"
    break;

  case 169: /* op: '!'  */
#line 2606 "parse.y"
                                { ifndef_ripper((yyval.id) = '!'); }
#line 10965 "parse.c"
    break;

  case 170: /* op: '~'  */
#line 2607 "parse.y"
                                { ifndef_ripper((yyval.id) = '~'); }
#line 10971 "parse.c"
    break;

  case 171: /* op: "unary+"  */
#line 2608 "parse.y"
                                { ifndef_ripper((yyval.id) = tUPLUS); }
#line 10977 "parse.c"
    break;

  case 172: /* op: "unary-"  */
#line 2609 "parse.y"
                                { ifndef_ripper((yyval.id) = tUMINUS); }
#line 10983 "parse.c"
    break;

  case 173: /* op: "[]"  */
#line 2610 "parse.y"
                                { ifndef_ripper((yyval.id) = tAREF); }
#line 10989 "parse.c"
    break;

  case 174: /* op: "[]="  */
#line 2611 "parse.y"
                                { ifndef_ripper((yyval.id) = tASET); }
#line 10995 "parse.c"
    break;

  case 175: /* op: '`'  */
#line 2612 "parse.y"
                                { ifndef_ripper((yyval.id) = '`'); }
#line 11001 "parse.c"
    break;

  case 217: /* arg: lhs '=' lex_ctxt arg_rhs  */
#line 2630 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = node_assign(p, (yyvsp[-3].node), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: assign!($1, $4) %*/
		    }
#line 11012 "parse.c"
    break;

  case 218: /* arg: var_lhs "operator-assignment" lex_ctxt arg_rhs  */
#line 2637 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_op_assign(p, (yyvsp[-3].node), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!($1, $2, $4) %*/
		    }
#line 11023 "parse.c"
    break;

  case 219: /* arg: primary_value '[' opt_call_args rbracket "operator-assignment" lex_ctxt arg_rhs  */
#line 2644 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_ary_op_assign(p, (yyvsp[-6].node), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[0].node), &(yylsp[-4]), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(aref_field!($1, escape_Qundef($3)), $5, $7) %*/
		    }
#line 11034 "parse.c"
    break;

  case 220: /* arg: primary_value call_op "local variable or method" "operator-assignment" lex_ctxt arg_rhs  */
#line 2651 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 11045 "parse.c"
    break;

  case 221: /* arg: primary_value call_op "constant" "operator-assignment" lex_ctxt arg_rhs  */
#line 2658 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), (yyvsp[-4].id), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 11056 "parse.c"
    break;

  case 222: /* arg: primary_value "::" "local variable or method" "operator-assignment" lex_ctxt arg_rhs  */
#line 2665 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_attr_op_assign(p, (yyvsp[-5].node), ID2VAL(idCOLON2), (yyvsp[-3].id), (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(field!($1, $2, $3), $4, $6) %*/
		    }
#line 11067 "parse.c"
    break;

  case 223: /* arg: primary_value "::" "constant" "operator-assignment" lex_ctxt arg_rhs  */
#line 2672 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-5]), &(yylsp[-3]));
			(yyval.node) = new_const_op_assign(p, NEW_COLON2((yyvsp[-5].node), (yyvsp[-3].id), &loc), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(const_path_field!($1, $3), $4, $6) %*/
		    }
#line 11079 "parse.c"
    break;

  case 224: /* arg: ":: at EXPR_BEG" "constant" "operator-assignment" lex_ctxt arg_rhs  */
#line 2680 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-4]), &(yylsp[-3]));
			(yyval.node) = new_const_op_assign(p, NEW_COLON3((yyvsp[-3].id), &loc), (yyvsp[-2].id), (yyvsp[0].node), (yyvsp[-1].ctxt), &(yyloc));
		    /*% %*/
		    /*% ripper: opassign!(top_const_field!($2), $3, $5) %*/
		    }
#line 11091 "parse.c"
    break;

  case 225: /* arg: backref "operator-assignment" lex_ctxt arg_rhs  */
#line 2688 "parse.y"
                    {
		    /*%%%*/
			rb_backref_error(p, (yyvsp[-3].node));
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper[error]: backref_error(p, RNODE($1), opassign!(var_field(p, $1), $2, $4)) %*/
		    }
#line 11103 "parse.c"
    break;

  case 226: /* arg: arg ".." arg  */
#line 2696 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, $3) %*/
		    }
#line 11116 "parse.c"
    break;

  case 227: /* arg: arg "..." arg  */
#line 2705 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, $3) %*/
		    }
#line 11129 "parse.c"
    break;

  case 228: /* arg: arg ".."  */
#line 2714 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT2((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, Qnil) %*/
		    }
#line 11141 "parse.c"
    break;

  case 229: /* arg: arg "..."  */
#line 2722 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT3((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, Qnil) %*/
		    }
#line 11153 "parse.c"
    break;

  case 230: /* arg: "(.." arg  */
#line 2730 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!(Qnil, $2) %*/
		    }
#line 11165 "parse.c"
    break;

  case 231: /* arg: "(..." arg  */
#line 2738 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!(Qnil, $2) %*/
		    }
#line 11177 "parse.c"
    break;

  case 232: /* arg: arg '+' arg  */
#line 2746 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '+', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11185 "parse.c"
    break;

  case 233: /* arg: arg '-' arg  */
#line 2750 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '-', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11193 "parse.c"
    break;

  case 234: /* arg: arg '*' arg  */
#line 2754 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '*', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11201 "parse.c"
    break;

  case 235: /* arg: arg '/' arg  */
#line 2758 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '/', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11209 "parse.c"
    break;

  case 236: /* arg: arg '%' arg  */
#line 2762 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '%', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11217 "parse.c"
    break;

  case 237: /* arg: arg "**" arg  */
#line 2766 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idPow, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11225 "parse.c"
    break;

  case 238: /* arg: tUMINUS_NUM simple_numeric "**" arg  */
#line 2770 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, call_bin_op(p, (yyvsp[-2].node), idPow, (yyvsp[0].node), &(yylsp[-2]), &(yyloc)), idUMinus, &(yylsp[-3]), &(yyloc));
		    }
#line 11233 "parse.c"
    break;

  case 239: /* arg: "unary+" arg  */
#line 2774 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, (yyvsp[0].node), idUPlus, &(yylsp[-1]), &(yyloc));
		    }
#line 11241 "parse.c"
    break;

  case 240: /* arg: "unary-" arg  */
#line 2778 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, (yyvsp[0].node), idUMinus, &(yylsp[-1]), &(yyloc));
		    }
#line 11249 "parse.c"
    break;

  case 241: /* arg: arg '|' arg  */
#line 2782 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '|', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11257 "parse.c"
    break;

  case 242: /* arg: arg '^' arg  */
#line 2786 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '^', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11265 "parse.c"
    break;

  case 243: /* arg: arg '&' arg  */
#line 2790 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), '&', (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11273 "parse.c"
    break;

  case 244: /* arg: arg "<=>" arg  */
#line 2794 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idCmp, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11281 "parse.c"
    break;

  case 246: /* arg: arg "==" arg  */
#line 2799 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idEq, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11289 "parse.c"
    break;

  case 247: /* arg: arg "===" arg  */
#line 2803 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idEqq, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11297 "parse.c"
    break;

  case 248: /* arg: arg "!=" arg  */
#line 2807 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idNeq, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11305 "parse.c"
    break;

  case 249: /* arg: arg "=~" arg  */
#line 2811 "parse.y"
                    {
			(yyval.node) = match_op(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11313 "parse.c"
    break;

  case 250: /* arg: arg "!~" arg  */
#line 2815 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idNeqTilde, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11321 "parse.c"
    break;

  case 251: /* arg: '!' arg  */
#line 2819 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[0].node), &(yylsp[0])), '!', &(yylsp[-1]), &(yyloc));
		    }
#line 11329 "parse.c"
    break;

  case 252: /* arg: '~' arg  */
#line 2823 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, (yyvsp[0].node), '~', &(yylsp[-1]), &(yyloc));
		    }
#line 11337 "parse.c"
    break;

  case 253: /* arg: arg "<<" arg  */
#line 2827 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idLTLT, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11345 "parse.c"
    break;

  case 254: /* arg: arg ">>" arg  */
#line 2831 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), idGTGT, (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11353 "parse.c"
    break;

  case 255: /* arg: arg "&&" arg  */
#line 2835 "parse.y"
                    {
			(yyval.node) = logop(p, idANDOP, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11361 "parse.c"
    break;

  case 256: /* arg: arg "||" arg  */
#line 2839 "parse.y"
                    {
			(yyval.node) = logop(p, idOROP, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11369 "parse.c"
    break;

  case 257: /* $@13: %empty  */
#line 2842 "parse.y"
                                         {p->ctxt.in_defined = 1;}
#line 11375 "parse.c"
    break;

  case 258: /* arg: "`defined?'" opt_nl $@13 arg  */
#line 2843 "parse.y"
                    {
			p->ctxt.in_defined = 0;
			(yyval.node) = new_defined(p, (yyvsp[0].node), &(yyloc));
		    }
#line 11384 "parse.c"
    break;

  case 259: /* arg: arg '?' arg opt_nl ':' arg  */
#line 2848 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-5].node));
			(yyval.node) = new_if(p, (yyvsp[-5].node), (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-5].node));
		    /*% %*/
		    /*% ripper: ifop!($1, $3, $6) %*/
		    }
#line 11397 "parse.c"
    break;

  case 260: /* arg: defn_head f_opt_paren_args '=' arg  */
#line 2857 "parse.y"
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
#line 11412 "parse.c"
    break;

  case 261: /* arg: defn_head f_opt_paren_args '=' arg "`rescue' modifier" arg  */
#line 2868 "parse.y"
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
#line 11428 "parse.c"
    break;

  case 262: /* arg: defs_head f_opt_paren_args '=' arg  */
#line 2880 "parse.y"
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
#line 11445 "parse.c"
    break;

  case 263: /* arg: defs_head f_opt_paren_args '=' arg "`rescue' modifier" arg  */
#line 2893 "parse.y"
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
#line 11463 "parse.c"
    break;

  case 264: /* arg: primary  */
#line 2907 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 11471 "parse.c"
    break;

  case 265: /* relop: '>'  */
#line 2912 "parse.y"
                       {(yyval.id) = '>';}
#line 11477 "parse.c"
    break;

  case 266: /* relop: '<'  */
#line 2913 "parse.y"
                       {(yyval.id) = '<';}
#line 11483 "parse.c"
    break;

  case 267: /* relop: ">="  */
#line 2914 "parse.y"
                       {(yyval.id) = idGE;}
#line 11489 "parse.c"
    break;

  case 268: /* relop: "<="  */
#line 2915 "parse.y"
                       {(yyval.id) = idLE;}
#line 11495 "parse.c"
    break;

  case 269: /* rel_expr: arg relop arg  */
#line 2919 "parse.y"
                    {
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11503 "parse.c"
    break;

  case 270: /* rel_expr: rel_expr relop arg  */
#line 2923 "parse.y"
                    {
			rb_warning1("comparison '%s' after comparison", WARN_ID((yyvsp[-1].id)));
			(yyval.node) = call_bin_op(p, (yyvsp[-2].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    }
#line 11512 "parse.c"
    break;

  case 271: /* lex_ctxt: none  */
#line 2930 "parse.y"
                    {
			(yyval.ctxt) = p->ctxt;
		    }
#line 11520 "parse.c"
    break;

  case 272: /* arg_value: arg  */
#line 2936 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 11529 "parse.c"
    break;

  case 274: /* aref_args: args trailer  */
#line 2944 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 11537 "parse.c"
    break;

  case 275: /* aref_args: args ',' assocs trailer  */
#line 2948 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? arg_append(p, (yyvsp[-3].node), new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].node);
		    /*% %*/
		    /*% ripper: args_add!($1, bare_assoc_hash!($3)) %*/
		    }
#line 11548 "parse.c"
    break;

  case 276: /* aref_args: assocs trailer  */
#line 2955 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? NEW_LIST(new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : 0;
		    /*% %*/
		    /*% ripper: args_add!(args_new!, bare_assoc_hash!($1)) %*/
		    }
#line 11559 "parse.c"
    break;

  case 277: /* arg_rhs: arg  */
#line 2964 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 11568 "parse.c"
    break;

  case 278: /* arg_rhs: arg "`rescue' modifier" arg  */
#line 2969 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			(yyval.node) = rescued_expr(p, (yyvsp[-2].node), (yyvsp[0].node), &(yylsp[-2]), &(yylsp[-1]), &(yylsp[0]));
		    /*% %*/
		    /*% ripper: rescue_mod!($1, $3) %*/
		    }
#line 11580 "parse.c"
    break;

  case 279: /* paren_args: '(' opt_call_args rparen  */
#line 2979 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: arg_paren!(escape_Qundef($2)) %*/
		    }
#line 11591 "parse.c"
    break;

  case 280: /* paren_args: '(' args ',' args_forward rparen  */
#line 2986 "parse.y"
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
#line 11607 "parse.c"
    break;

  case 281: /* paren_args: '(' args_forward rparen  */
#line 2998 "parse.y"
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
#line 11623 "parse.c"
    break;

  case 286: /* opt_call_args: args ','  */
#line 3018 "parse.y"
                    {
		      (yyval.node) = (yyvsp[-1].node);
		    }
#line 11631 "parse.c"
    break;

  case 287: /* opt_call_args: args ',' assocs ','  */
#line 3022 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? arg_append(p, (yyvsp[-3].node), new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].node);
		    /*% %*/
		    /*% ripper: args_add!($1, bare_assoc_hash!($3)) %*/
		    }
#line 11642 "parse.c"
    break;

  case 288: /* opt_call_args: assocs ','  */
#line 3029 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? NEW_LIST(new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yylsp[-1])) : 0;
		    /*% %*/
		    /*% ripper: args_add!(args_new!, bare_assoc_hash!($1)) %*/
		    }
#line 11653 "parse.c"
    break;

  case 289: /* call_args: command  */
#line 3038 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!(args_new!, $1) %*/
		    }
#line 11665 "parse.c"
    break;

  case 290: /* call_args: args opt_block_arg  */
#line 3046 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = arg_blk_pass((yyvsp[-1].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: args_add_block!($1, $2) %*/
		    }
#line 11676 "parse.c"
    break;

  case 291: /* call_args: assocs opt_block_arg  */
#line 3053 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? NEW_LIST(new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yylsp[-1])) : 0;
			(yyval.node) = arg_blk_pass((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: args_add_block!(args_add!(args_new!, bare_assoc_hash!($1)), $2) %*/
		    }
#line 11688 "parse.c"
    break;

  case 292: /* call_args: args ',' assocs opt_block_arg  */
#line 3061 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node) ? arg_append(p, (yyvsp[-3].node), new_hash(p, (yyvsp[-1].node), &(yylsp[-1])), &(yyloc)) : (yyvsp[-3].node);
			(yyval.node) = arg_blk_pass((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: args_add_block!(args_add!($1, bare_assoc_hash!($3)), $4) %*/
		    }
#line 11700 "parse.c"
    break;

  case 294: /* $@14: %empty  */
#line 3072 "parse.y"
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
#line 11722 "parse.c"
    break;

  case 295: /* command_args: $@14 call_args  */
#line 3090 "parse.y"
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
#line 11744 "parse.c"
    break;

  case 296: /* block_arg: "&" arg_value  */
#line 3110 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BLOCK_PASS((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: $2 %*/
		    }
#line 11755 "parse.c"
    break;

  case 297: /* block_arg: "&"  */
#line 3117 "parse.y"
                    {
                        if (!local_id(p, idFWD_BLOCK)) {
                            compile_error(p, "no anonymous block parameter");
                        }
                    /*%%%*/
                        (yyval.node) = NEW_BLOCK_PASS(NEW_LVAR(idFWD_BLOCK, &(yylsp[0])), &(yyloc));
                    /*% %*/
                    /*% ripper: Qnil %*/
                    }
#line 11769 "parse.c"
    break;

  case 298: /* opt_block_arg: ',' block_arg  */
#line 3129 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 11777 "parse.c"
    break;

  case 299: /* opt_block_arg: none  */
#line 3133 "parse.y"
                    {
			(yyval.node) = 0;
		    }
#line 11785 "parse.c"
    break;

  case 300: /* args: arg_value  */
#line 3140 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!(args_new!, $1) %*/
		    }
#line 11796 "parse.c"
    break;

  case 301: /* args: "*" arg_value  */
#line 3147 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SPLAT((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!(args_new!, $2) %*/
		    }
#line 11807 "parse.c"
    break;

  case 302: /* args: "*"  */
#line 3154 "parse.y"
                    {
                        if (!local_id(p, idFWD_REST) ||
                            local_id(p, idFWD_ALL)) {
                            compile_error(p, "no anonymous rest parameter");
                        }
		    /*%%%*/
			(yyval.node) = NEW_SPLAT(NEW_LVAR(idFWD_REST, &(yylsp[0])), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!(args_new!, Qnil) %*/
		    }
#line 11822 "parse.c"
    break;

  case 303: /* args: args ',' arg_value  */
#line 3165 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = last_arg_append(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!($1, $3) %*/
		    }
#line 11833 "parse.c"
    break;

  case 304: /* args: args ',' "*" arg_value  */
#line 3172 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!($1, $4) %*/
		    }
#line 11844 "parse.c"
    break;

  case 305: /* args: args ',' "*"  */
#line 3179 "parse.y"
                    {
                        if (!local_id(p, idFWD_REST) ||
                            local_id(p, idFWD_ALL)) {
                            compile_error(p, "no anonymous rest parameter");
                        }
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-2].node), NEW_LVAR(idFWD_REST, &(yylsp[0])), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!($1, Qnil) %*/
		    }
#line 11859 "parse.c"
    break;

  case 308: /* mrhs: args ',' arg_value  */
#line 3198 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = last_arg_append(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mrhs_add!(mrhs_new_from_args!($1), $3) %*/
		    }
#line 11870 "parse.c"
    break;

  case 309: /* mrhs: args ',' "*" arg_value  */
#line 3205 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mrhs_add_star!(mrhs_new_from_args!($1), $4) %*/
		    }
#line 11881 "parse.c"
    break;

  case 310: /* mrhs: "*" arg_value  */
#line 3212 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SPLAT((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mrhs_add_star!(mrhs_new!, $2) %*/
		    }
#line 11892 "parse.c"
    break;

  case 321: /* primary: "method"  */
#line 3231 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_FCALL((yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_arg!(fcall!($1), args_new!) %*/
		    }
#line 11903 "parse.c"
    break;

  case 322: /* $@15: %empty  */
#line 3238 "parse.y"
                    {
			CMDARG_PUSH(0);
		    }
#line 11911 "parse.c"
    break;

  case 323: /* primary: k_begin $@15 bodystmt k_end  */
#line 3243 "parse.y"
                    {
			CMDARG_POP();
		    /*%%%*/
			set_line_body((yyvsp[-1].node), (yylsp[-3]).end_pos.lineno);
			(yyval.node) = NEW_BEGIN((yyvsp[-1].node), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-3]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: begin!($3) %*/
		    }
#line 11925 "parse.c"
    break;

  case 324: /* $@16: %empty  */
#line 3252 "parse.y"
                              {SET_LEX_STATE(EXPR_ENDARG);}
#line 11931 "parse.c"
    break;

  case 325: /* primary: "( arg" $@16 rparen  */
#line 3253 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: paren!(0) %*/
		    }
#line 11942 "parse.c"
    break;

  case 326: /* $@17: %empty  */
#line 3259 "parse.y"
                                   {SET_LEX_STATE(EXPR_ENDARG);}
#line 11948 "parse.c"
    break;

  case 327: /* primary: "( arg" stmt $@17 rparen  */
#line 3260 "parse.y"
                    {
		    /*%%%*/
			if (nd_type_p((yyvsp[-2].node), NODE_SELF)) (yyvsp[-2].node)->nd_state = 0;
			(yyval.node) = (yyvsp[-2].node);
		    /*% %*/
		    /*% ripper: paren!($2) %*/
		    }
#line 11960 "parse.c"
    break;

  case 328: /* primary: "(" compstmt ')'  */
#line 3268 "parse.y"
                    {
		    /*%%%*/
			if (nd_type_p((yyvsp[-1].node), NODE_SELF)) (yyvsp[-1].node)->nd_state = 0;
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: paren!($2) %*/
		    }
#line 11972 "parse.c"
    break;

  case 329: /* primary: primary_value "::" "constant"  */
#line 3276 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_ref!($1, $3) %*/
		    }
#line 11983 "parse.c"
    break;

  case 330: /* primary: ":: at EXPR_BEG" "constant"  */
#line 3283 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON3((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: top_const_ref!($2) %*/
		    }
#line 11994 "parse.c"
    break;

  case 331: /* primary: "[" aref_args ']'  */
#line 3290 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!(escape_Qundef($2)) %*/
		    }
#line 12005 "parse.c"
    break;

  case 332: /* primary: "{" assoc_list '}'  */
#line 3297 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_hash(p, (yyvsp[-1].node), &(yyloc));
			(yyval.node)->nd_brace = TRUE;
		    /*% %*/
		    /*% ripper: hash!(escape_Qundef($2)) %*/
		    }
#line 12017 "parse.c"
    break;

  case 333: /* primary: k_return  */
#line 3305 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RETURN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: return0! %*/
		    }
#line 12028 "parse.c"
    break;

  case 334: /* primary: "`yield'" '(' call_args rparen  */
#line 3312 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_yield(p, (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: yield!(paren!($3)) %*/
		    }
#line 12039 "parse.c"
    break;

  case 335: /* primary: "`yield'" '(' rparen  */
#line 3319 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_YIELD(0, &(yyloc));
		    /*% %*/
		    /*% ripper: yield!(paren!(args_new!)) %*/
		    }
#line 12050 "parse.c"
    break;

  case 336: /* primary: "`yield'"  */
#line 3326 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_YIELD(0, &(yyloc));
		    /*% %*/
		    /*% ripper: yield0! %*/
		    }
#line 12061 "parse.c"
    break;

  case 337: /* $@18: %empty  */
#line 3332 "parse.y"
                                             {p->ctxt.in_defined = 1;}
#line 12067 "parse.c"
    break;

  case 338: /* primary: "`defined?'" opt_nl '(' $@18 expr rparen  */
#line 3333 "parse.y"
                    {
			p->ctxt.in_defined = 0;
			(yyval.node) = new_defined(p, (yyvsp[-1].node), &(yyloc));
		    }
#line 12076 "parse.c"
    break;

  case 339: /* primary: "`not'" '(' expr rparen  */
#line 3338 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, (yyvsp[-1].node), &(yylsp[-1])), METHOD_NOT, &(yylsp[-3]), &(yyloc));
		    }
#line 12084 "parse.c"
    break;

  case 340: /* primary: "`not'" '(' rparen  */
#line 3342 "parse.y"
                    {
			(yyval.node) = call_uni_op(p, method_cond(p, new_nil(&(yylsp[-1])), &(yylsp[-1])), METHOD_NOT, &(yylsp[-2]), &(yyloc));
		    }
#line 12092 "parse.c"
    break;

  case 341: /* primary: fcall brace_block  */
#line 3346 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = method_add_block(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(method_add_arg!(fcall!($1), args_new!), $2) %*/
		    }
#line 12103 "parse.c"
    break;

  case 343: /* primary: method_call brace_block  */
#line 3354 "parse.y"
                    {
		    /*%%%*/
			block_dup_check(p, (yyvsp[-1].node)->nd_args, (yyvsp[0].node));
			(yyval.node) = method_add_block(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!($1, $2) %*/
		    }
#line 12115 "parse.c"
    break;

  case 345: /* primary: k_if expr_value then compstmt if_tail k_end  */
#line 3366 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[-4].node), (yyvsp[-2].node), (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: if!($2, $4, escape_Qundef($5)) %*/
		    }
#line 12127 "parse.c"
    break;

  case 346: /* primary: k_unless expr_value then compstmt opt_else k_end  */
#line 3377 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_unless(p, (yyvsp[-4].node), (yyvsp[-2].node), (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: unless!($2, $4, escape_Qundef($5)) %*/
		    }
#line 12139 "parse.c"
    break;

  case 347: /* primary: k_while expr_value_do compstmt k_end  */
#line 3387 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_WHILE(cond(p, (yyvsp[-2].node), &(yylsp[-2])), (yyvsp[-1].node), 1, &(yyloc));
			fixpos((yyval.node), (yyvsp[-2].node));
		    /*% %*/
		    /*% ripper: while!($2, $3) %*/
		    }
#line 12151 "parse.c"
    break;

  case 348: /* primary: k_until expr_value_do compstmt k_end  */
#line 3397 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_UNTIL(cond(p, (yyvsp[-2].node), &(yylsp[-2])), (yyvsp[-1].node), 1, &(yyloc));
			fixpos((yyval.node), (yyvsp[-2].node));
		    /*% %*/
		    /*% ripper: until!($2, $3) %*/
		    }
#line 12163 "parse.c"
    break;

  case 349: /* @19: %empty  */
#line 3405 "parse.y"
                    {
			(yyval.val) = p->case_labels;
			p->case_labels = Qnil;
		    }
#line 12172 "parse.c"
    break;

  case 350: /* primary: k_case expr_value opt_terms @19 case_body k_end  */
#line 3411 "parse.y"
                    {
			if (RTEST(p->case_labels)) rb_hash_clear(p->case_labels);
			p->case_labels = (yyvsp[-2].val);
		    /*%%%*/
			(yyval.node) = NEW_CASE((yyvsp[-4].node), (yyvsp[-1].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-4].node));
		    /*% %*/
		    /*% ripper: case!($2, $5) %*/
		    }
#line 12186 "parse.c"
    break;

  case 351: /* @20: %empty  */
#line 3421 "parse.y"
                    {
			(yyval.val) = p->case_labels;
			p->case_labels = 0;
		    }
#line 12195 "parse.c"
    break;

  case 352: /* primary: k_case opt_terms @20 case_body k_end  */
#line 3427 "parse.y"
                    {
			if (RTEST(p->case_labels)) rb_hash_clear(p->case_labels);
			p->case_labels = (yyvsp[-2].val);
		    /*%%%*/
			(yyval.node) = NEW_CASE2((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: case!(Qnil, $4) %*/
		    }
#line 12208 "parse.c"
    break;

  case 353: /* primary: k_case expr_value opt_terms p_case_body k_end  */
#line 3438 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_CASE3((yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: case!($2, $4) %*/
		    }
#line 12219 "parse.c"
    break;

  case 354: /* primary: k_for for_var "`in'" expr_value_do compstmt k_end  */
#line 3447 "parse.y"
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
#line 12263 "parse.c"
    break;

  case 355: /* $@21: %empty  */
#line 3487 "parse.y"
                    {
			if (p->ctxt.in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-2]), &(yylsp[-1]));
			    yyerror1(&loc, "class definition in method body");
			}
			p->ctxt.in_class = 1;
			local_push(p, 0);
		    }
#line 12276 "parse.c"
    break;

  case 356: /* primary: k_class cpath superclass $@21 bodystmt k_end  */
#line 3497 "parse.y"
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
#line 12293 "parse.c"
    break;

  case 357: /* $@22: %empty  */
#line 3510 "parse.y"
                    {
			p->ctxt.in_def = 0;
			p->ctxt.in_class = 0;
			local_push(p, 0);
		    }
#line 12303 "parse.c"
    break;

  case 358: /* primary: k_class "<<" expr $@22 term bodystmt k_end  */
#line 3518 "parse.y"
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
#line 12321 "parse.c"
    break;

  case 359: /* $@23: %empty  */
#line 3532 "parse.y"
                    {
			if (p->ctxt.in_def) {
			    YYLTYPE loc = code_loc_gen(&(yylsp[-1]), &(yylsp[0]));
			    yyerror1(&loc, "module definition in method body");
			}
			p->ctxt.in_class = 1;
			local_push(p, 0);
		    }
#line 12334 "parse.c"
    break;

  case 360: /* primary: k_module cpath $@23 bodystmt k_end  */
#line 3542 "parse.y"
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
#line 12351 "parse.c"
    break;

  case 361: /* $@24: %empty  */
#line 3556 "parse.y"
                    {
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[-1]).beg_pos);
		    /*% %*/
		    }
#line 12361 "parse.c"
    break;

  case 362: /* primary: defn_head f_arglist $@24 bodystmt k_end  */
#line 3563 "parse.y"
                    {
			restore_defun(p, (yyvsp[-4].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-4].node), (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: def!(get_value($1), $2, $4) %*/
			local_pop(p);
		    }
#line 12374 "parse.c"
    break;

  case 363: /* $@25: %empty  */
#line 3573 "parse.y"
                    {
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[-1]).beg_pos);
		    /*% %*/
		    }
#line 12384 "parse.c"
    break;

  case 364: /* primary: defs_head f_arglist $@25 bodystmt k_end  */
#line 3580 "parse.y"
                    {
			restore_defun(p, (yyvsp[-4].node)->nd_defn);
		    /*%%%*/
			(yyval.node) = set_defun_body(p, (yyvsp[-4].node), (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%
			$1 = get_value($1);
		    %*/
		    /*% ripper: defs!(AREF($1, 0), AREF($1, 1), AREF($1, 2), $2, $4) %*/
			local_pop(p);
		    }
#line 12399 "parse.c"
    break;

  case 365: /* primary: "`break'"  */
#line 3591 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BREAK(0, &(yyloc));
		    /*% %*/
		    /*% ripper: break!(args_new!) %*/
		    }
#line 12410 "parse.c"
    break;

  case 366: /* primary: "`next'"  */
#line 3598 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_NEXT(0, &(yyloc));
		    /*% %*/
		    /*% ripper: next!(args_new!) %*/
		    }
#line 12421 "parse.c"
    break;

  case 367: /* primary: "`redo'"  */
#line 3605 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_REDO(&(yyloc));
		    /*% %*/
		    /*% ripper: redo! %*/
		    }
#line 12432 "parse.c"
    break;

  case 368: /* primary: "`retry'"  */
#line 3612 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RETRY(&(yyloc));
		    /*% %*/
		    /*% ripper: retry! %*/
		    }
#line 12443 "parse.c"
    break;

  case 369: /* primary_value: primary  */
#line 3621 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 12452 "parse.c"
    break;

  case 370: /* k_begin: "`begin'"  */
#line 3628 "parse.y"
                    {
			token_info_push(p, "begin", &(yyloc));
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12463 "parse.c"
    break;

  case 371: /* k_if: "`if'"  */
#line 3637 "parse.y"
                    {
			WARN_EOL("if");
			token_info_push(p, "if", &(yyloc));
			if (p->token_info && p->token_info->nonspc &&
			    p->token_info->next && !strcmp(p->token_info->next->token, "else")) {
			    const char *tok = p->lex.ptok - rb_strlen_lit("if");
			    const char *beg = p->lex.pbeg + p->token_info->next->beg.column;
			    beg += rb_strlen_lit("else");
			    while (beg < tok && ISSPACE(*beg)) beg++;
			    if (beg == tok) {
				p->token_info->nonspc = 0;
			    }
			}
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12485 "parse.c"
    break;

  case 372: /* k_unless: "`unless'"  */
#line 3657 "parse.y"
                    {
			token_info_push(p, "unless", &(yyloc));
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12496 "parse.c"
    break;

  case 373: /* k_while: "`while'"  */
#line 3666 "parse.y"
                    {
			token_info_push(p, "while", &(yyloc));
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12507 "parse.c"
    break;

  case 374: /* k_until: "`until'"  */
#line 3675 "parse.y"
                    {
			token_info_push(p, "until", &(yyloc));
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12518 "parse.c"
    break;

  case 375: /* k_case: "`case'"  */
#line 3684 "parse.y"
                    {
			token_info_push(p, "case", &(yyloc));
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12529 "parse.c"
    break;

  case 376: /* k_for: "`for'"  */
#line 3693 "parse.y"
                    {
			token_info_push(p, "for", &(yyloc));
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12540 "parse.c"
    break;

  case 377: /* k_class: "`class'"  */
#line 3702 "parse.y"
                    {
			token_info_push(p, "class", &(yyloc));
			(yyval.ctxt) = p->ctxt;
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12552 "parse.c"
    break;

  case 378: /* k_module: "`module'"  */
#line 3712 "parse.y"
                    {
			token_info_push(p, "module", &(yyloc));
			(yyval.ctxt) = p->ctxt;
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12564 "parse.c"
    break;

  case 379: /* k_def: "`def'"  */
#line 3722 "parse.y"
                    {
			token_info_push(p, "def", &(yyloc));
			p->ctxt.in_argdef = 1;
		    }
#line 12573 "parse.c"
    break;

  case 380: /* k_do: "`do'"  */
#line 3729 "parse.y"
                    {
			token_info_push(p, "do", &(yyloc));
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/

		    }
#line 12585 "parse.c"
    break;

  case 381: /* k_do_block: "`do' for block"  */
#line 3739 "parse.y"
                    {
			token_info_push(p, "do", &(yyloc));
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 12596 "parse.c"
    break;

  case 382: /* k_rescue: "`rescue'"  */
#line 3748 "parse.y"
                    {
			token_info_warn(p, "rescue", p->token_info, 1, &(yyloc));
		    }
#line 12604 "parse.c"
    break;

  case 383: /* k_ensure: "`ensure'"  */
#line 3754 "parse.y"
                    {
			token_info_warn(p, "ensure", p->token_info, 1, &(yyloc));
		    }
#line 12612 "parse.c"
    break;

  case 384: /* k_when: "`when'"  */
#line 3760 "parse.y"
                    {
			token_info_warn(p, "when", p->token_info, 0, &(yyloc));
		    }
#line 12620 "parse.c"
    break;

  case 385: /* k_else: "`else'"  */
#line 3766 "parse.y"
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
#line 12637 "parse.c"
    break;

  case 386: /* k_elsif: "`elsif'"  */
#line 3781 "parse.y"
                    {
			WARN_EOL("elsif");
			token_info_warn(p, "elsif", p->token_info, 1, &(yyloc));
		    }
#line 12646 "parse.c"
    break;

  case 387: /* k_end: "`end'"  */
#line 3788 "parse.y"
                    {
			token_info_pop(p, "end", &(yyloc));
		    /*%%%*/
			pop_end_expect_token_locations(p);
		    /*% %*/
		    }
#line 12657 "parse.c"
    break;

  case 388: /* k_end: "dummy end"  */
#line 3795 "parse.y"
                    {
			compile_error(p, "syntax error, unexpected end-of-input");
		    }
#line 12665 "parse.c"
    break;

  case 389: /* k_return: "`return'"  */
#line 3801 "parse.y"
                    {
			if (p->ctxt.in_class && !p->ctxt.in_def && !dyna_in_block(p))
			    yyerror1(&(yylsp[0]), "Invalid return in class/module body");
		    }
#line 12674 "parse.c"
    break;

  case 396: /* if_tail: k_elsif expr_value then compstmt if_tail  */
#line 3820 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[-3].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-3].node));
		    /*% %*/
		    /*% ripper: elsif!($2, $4, escape_Qundef($5)) %*/
		    }
#line 12686 "parse.c"
    break;

  case 398: /* opt_else: k_else compstmt  */
#line 3831 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: else!($2) %*/
		    }
#line 12697 "parse.c"
    break;

  case 401: /* f_marg: f_norm_arg  */
#line 3844 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
			mark_lvar_used(p, (yyval.node));
		    /*% %*/
		    /*% ripper: assignable(p, $1) %*/
		    }
#line 12709 "parse.c"
    break;

  case 402: /* f_marg: "(" f_margs rparen  */
#line 3852 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: mlhs_paren!($2) %*/
		    }
#line 12720 "parse.c"
    break;

  case 403: /* f_marg_list: f_marg  */
#line 3861 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add!(mlhs_new!, $1) %*/
		    }
#line 12731 "parse.c"
    break;

  case 404: /* f_marg_list: f_marg_list ',' f_marg  */
#line 3868 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: mlhs_add!($1, $3) %*/
		    }
#line 12742 "parse.c"
    break;

  case 405: /* f_margs: f_marg_list  */
#line 3877 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[0].node), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 12753 "parse.c"
    break;

  case 406: /* f_margs: f_marg_list ',' f_rest_marg  */
#line 3884 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!($1, $3) %*/
		    }
#line 12764 "parse.c"
    break;

  case 407: /* f_margs: f_marg_list ',' f_rest_marg ',' f_marg_list  */
#line 3891 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN((yyvsp[-4].node), NEW_POSTARG((yyvsp[-2].node), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!($1, $3), $5) %*/
		    }
#line 12775 "parse.c"
    break;

  case 408: /* f_margs: f_rest_marg  */
#line 3898 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_star!(mlhs_new!, $1) %*/
		    }
#line 12786 "parse.c"
    break;

  case 409: /* f_margs: f_rest_marg ',' f_marg_list  */
#line 3905 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_MASGN(0, NEW_POSTARG((yyvsp[-2].node), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: mlhs_add_post!(mlhs_add_star!(mlhs_new!, $1), $3) %*/
		    }
#line 12797 "parse.c"
    break;

  case 410: /* f_rest_marg: "*" f_norm_arg  */
#line 3914 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
			mark_lvar_used(p, (yyval.node));
		    /*% %*/
		    /*% ripper: assignable(p, $2) %*/
		    }
#line 12809 "parse.c"
    break;

  case 411: /* f_rest_marg: "*"  */
#line 3922 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NODE_SPECIAL_NO_NAME_REST;
		    /*% %*/
		    /*% ripper: Qnil %*/
		    }
#line 12820 "parse.c"
    break;

  case 413: /* f_any_kwrest: f_no_kwarg  */
#line 3931 "parse.y"
                             {(yyval.id) = ID2VAL(idNil);}
#line 12826 "parse.c"
    break;

  case 414: /* $@26: %empty  */
#line 3934 "parse.y"
                  {p->ctxt.in_argdef = 0;}
#line 12832 "parse.c"
    break;

  case 416: /* block_args_tail: f_block_kwarg ',' f_kwrest opt_f_block_arg  */
#line 3937 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 12840 "parse.c"
    break;

  case 417: /* block_args_tail: f_block_kwarg opt_f_block_arg  */
#line 3941 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-1].node), Qnone, (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 12848 "parse.c"
    break;

  case 418: /* block_args_tail: f_any_kwrest opt_f_block_arg  */
#line 3945 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 12856 "parse.c"
    break;

  case 419: /* block_args_tail: f_block_arg  */
#line 3949 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].id), &(yylsp[0]));
		    }
#line 12864 "parse.c"
    break;

  case 420: /* opt_block_args_tail: ',' block_args_tail  */
#line 3955 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 12872 "parse.c"
    break;

  case 421: /* opt_block_args_tail: %empty  */
#line 3959 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 12880 "parse.c"
    break;

  case 422: /* excessed_comma: ','  */
#line 3965 "parse.y"
                    {
			/* magic number for rest_id in iseq_set_arguments() */
		    /*%%%*/
			(yyval.id) = NODE_SPECIAL_EXCESSIVE_COMMA;
		    /*% %*/
		    /*% ripper: excessed_comma! %*/
		    }
#line 12892 "parse.c"
    break;

  case 423: /* block_param: f_arg ',' f_block_optarg ',' f_rest_arg opt_block_args_tail  */
#line 3975 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12900 "parse.c"
    break;

  case 424: /* block_param: f_arg ',' f_block_optarg ',' f_rest_arg ',' f_arg opt_block_args_tail  */
#line 3979 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-7].node), (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12908 "parse.c"
    break;

  case 425: /* block_param: f_arg ',' f_block_optarg opt_block_args_tail  */
#line 3983 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12916 "parse.c"
    break;

  case 426: /* block_param: f_arg ',' f_block_optarg ',' f_arg opt_block_args_tail  */
#line 3987 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12924 "parse.c"
    break;

  case 427: /* block_param: f_arg ',' f_rest_arg opt_block_args_tail  */
#line 3991 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12932 "parse.c"
    break;

  case 428: /* block_param: f_arg excessed_comma  */
#line 3995 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
			(yyval.node) = new_args(p, (yyvsp[-1].node), Qnone, (yyvsp[0].id), Qnone, (yyval.node), &(yyloc));
		    }
#line 12941 "parse.c"
    break;

  case 429: /* block_param: f_arg ',' f_rest_arg ',' f_arg opt_block_args_tail  */
#line 4000 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12949 "parse.c"
    break;

  case 430: /* block_param: f_arg opt_block_args_tail  */
#line 4004 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-1].node), Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12957 "parse.c"
    break;

  case 431: /* block_param: f_block_optarg ',' f_rest_arg opt_block_args_tail  */
#line 4008 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12965 "parse.c"
    break;

  case 432: /* block_param: f_block_optarg ',' f_rest_arg ',' f_arg opt_block_args_tail  */
#line 4012 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12973 "parse.c"
    break;

  case 433: /* block_param: f_block_optarg opt_block_args_tail  */
#line 4016 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12981 "parse.c"
    break;

  case 434: /* block_param: f_block_optarg ',' f_arg opt_block_args_tail  */
#line 4020 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 12989 "parse.c"
    break;

  case 435: /* block_param: f_rest_arg opt_block_args_tail  */
#line 4024 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 12997 "parse.c"
    break;

  case 436: /* block_param: f_rest_arg ',' f_arg opt_block_args_tail  */
#line 4028 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 13005 "parse.c"
    break;

  case 437: /* block_param: block_args_tail  */
#line 4032 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 13013 "parse.c"
    break;

  case 439: /* opt_block_param: block_param_def  */
#line 4039 "parse.y"
                    {
			p->command_start = TRUE;
		    }
#line 13021 "parse.c"
    break;

  case 440: /* block_param_def: '|' opt_bv_decl '|'  */
#line 4045 "parse.y"
                    {
			p->cur_arg = 0;
			p->max_numparam = ORDINAL_PARAM;
			p->ctxt.in_argdef = 0;
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: block_var!(params!(Qnil,Qnil,Qnil,Qnil,Qnil,Qnil,Qnil), escape_Qundef($2)) %*/
		    }
#line 13035 "parse.c"
    break;

  case 441: /* block_param_def: '|' block_param opt_bv_decl '|'  */
#line 4055 "parse.y"
                    {
			p->cur_arg = 0;
			p->max_numparam = ORDINAL_PARAM;
			p->ctxt.in_argdef = 0;
		    /*%%%*/
			(yyval.node) = (yyvsp[-2].node);
		    /*% %*/
		    /*% ripper: block_var!(escape_Qundef($2), escape_Qundef($3)) %*/
		    }
#line 13049 "parse.c"
    break;

  case 442: /* opt_bv_decl: opt_nl  */
#line 4068 "parse.y"
                    {
			(yyval.node) = 0;
		    }
#line 13057 "parse.c"
    break;

  case 443: /* opt_bv_decl: opt_nl ';' bv_decls opt_nl  */
#line 4072 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: $3 %*/
		    }
#line 13068 "parse.c"
    break;

  case 446: /* bvar: "local variable or method"  */
#line 4087 "parse.y"
                    {
			new_bv(p, get_id((yyvsp[0].id)));
		    /*% ripper: get_value($1) %*/
		    }
#line 13077 "parse.c"
    break;

  case 447: /* bvar: f_bad_arg  */
#line 4092 "parse.y"
                    {
			(yyval.node) = 0;
		    }
#line 13085 "parse.c"
    break;

  case 448: /* @27: %empty  */
#line 4098 "parse.y"
                    {
			token_info_push(p, "->", &(yylsp[0]));
			(yyvsp[0].vars) = dyna_push(p);
			(yyval.num) = p->lex.lpar_beg;
			p->lex.lpar_beg = p->lex.paren_nest;
		    }
#line 13096 "parse.c"
    break;

  case 449: /* @28: %empty  */
#line 4104 "parse.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 13105 "parse.c"
    break;

  case 450: /* @29: %empty  */
#line 4108 "parse.y"
                    {
			(yyval.node) = numparam_push(p);
		    }
#line 13113 "parse.c"
    break;

  case 451: /* $@30: %empty  */
#line 4112 "parse.y"
                    {
			CMDARG_PUSH(0);
		    }
#line 13121 "parse.c"
    break;

  case 452: /* lambda: "->" @27 @28 @29 f_larglist $@30 lambda_body  */
#line 4116 "parse.y"
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
#line 13145 "parse.c"
    break;

  case 453: /* f_larglist: '(' f_args opt_bv_decl ')'  */
#line 4138 "parse.y"
                    {
			p->ctxt.in_argdef = 0;
		    /*%%%*/
			(yyval.node) = (yyvsp[-2].node);
			p->max_numparam = ORDINAL_PARAM;
		    /*% %*/
		    /*% ripper: paren!($2) %*/
		    }
#line 13158 "parse.c"
    break;

  case 454: /* f_larglist: f_args  */
#line 4147 "parse.y"
                    {
			p->ctxt.in_argdef = 0;
		    /*%%%*/
			if (!args_info_empty_p((yyvsp[0].node)->nd_ainfo))
			    p->max_numparam = ORDINAL_PARAM;
		    /*% %*/
			(yyval.node) = (yyvsp[0].node);
		    }
#line 13171 "parse.c"
    break;

  case 455: /* lambda_body: tLAMBEG compstmt '}'  */
#line 4158 "parse.y"
                    {
			token_info_pop(p, "}", &(yylsp[0]));
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 13180 "parse.c"
    break;

  case 456: /* $@31: %empty  */
#line 4163 "parse.y"
                    {
		    /*%%%*/
			push_end_expect_token_locations(p, &(yylsp[0]).beg_pos);
		    /*% %*/
		    }
#line 13190 "parse.c"
    break;

  case 457: /* lambda_body: "`do' for lambda" $@31 bodystmt k_end  */
#line 4169 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 13198 "parse.c"
    break;

  case 458: /* do_block: k_do_block do_body k_end  */
#line 4175 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 13210 "parse.c"
    break;

  case 459: /* block_call: command do_block  */
#line 4185 "parse.y"
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
#line 13228 "parse.c"
    break;

  case 460: /* block_call: block_call call_op2 operation2 opt_paren_args  */
#line 4199 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
		    /*% %*/
		    /*% ripper: opt_event(:method_add_arg!, call!($1, $2, $3), $4) %*/
		    }
#line 13239 "parse.c"
    break;

  case 461: /* block_call: block_call call_op2 operation2 opt_paren_args brace_block  */
#line 4206 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-3].id), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: opt_event(:method_add_block!, command_call!($1, $2, $3, $4), $5) %*/
		    }
#line 13250 "parse.c"
    break;

  case 462: /* block_call: block_call call_op2 operation2 command_args do_block  */
#line 4213 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_command_qcall(p, (yyvsp[-3].id), (yyvsp[-4].node), (yyvsp[-2].id), (yyvsp[-1].node), (yyvsp[0].node), &(yylsp[-2]), &(yyloc));
		    /*% %*/
		    /*% ripper: method_add_block!(command_call!($1, $2, $3, $4), $5) %*/
		    }
#line 13261 "parse.c"
    break;

  case 463: /* method_call: fcall paren_args  */
#line 4222 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
			(yyval.node)->nd_args = (yyvsp[0].node);
			nd_set_last_loc((yyvsp[-1].node), (yylsp[0]).end_pos);
		    /*% %*/
		    /*% ripper: method_add_arg!(fcall!($1), $2) %*/
		    }
#line 13274 "parse.c"
    break;

  case 464: /* method_call: primary_value call_op operation2 opt_paren_args  */
#line 4231 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-2].id), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: opt_event(:method_add_arg!, call!($1, $2, $3), $4) %*/
		    }
#line 13286 "parse.c"
    break;

  case 465: /* method_call: primary_value "::" operation2 paren_args  */
#line 4239 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, $2, $3), $4) %*/
		    }
#line 13298 "parse.c"
    break;

  case 466: /* method_call: primary_value "::" operation3  */
#line 4247 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].node), (yyvsp[0].id), Qnull, &(yylsp[0]), &(yyloc));
		    /*% %*/
		    /*% ripper: call!($1, $2, $3) %*/
		    }
#line 13309 "parse.c"
    break;

  case 467: /* method_call: primary_value call_op paren_args  */
#line 4254 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, (yyvsp[-1].id), (yyvsp[-2].node), ID2VAL(idCall), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, $2, ID2VAL(idCall)), $3) %*/
		    }
#line 13321 "parse.c"
    break;

  case 468: /* method_call: primary_value "::" paren_args  */
#line 4262 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_qcall(p, ID2VAL(idCOLON2), (yyvsp[-2].node), ID2VAL(idCall), (yyvsp[0].node), &(yylsp[-1]), &(yyloc));
			nd_set_line((yyval.node), (yylsp[-1]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: method_add_arg!(call!($1, $2, ID2VAL(idCall)), $3) %*/
		    }
#line 13333 "parse.c"
    break;

  case 469: /* method_call: "`super'" paren_args  */
#line 4270 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SUPER((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: super!($2) %*/
		    }
#line 13344 "parse.c"
    break;

  case 470: /* method_call: "`super'"  */
#line 4277 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_ZSUPER(&(yyloc));
		    /*% %*/
		    /*% ripper: zsuper! %*/
		    }
#line 13355 "parse.c"
    break;

  case 471: /* method_call: primary_value '[' opt_call_args rbracket  */
#line 4284 "parse.y"
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
#line 13370 "parse.c"
    break;

  case 472: /* brace_block: '{' brace_body '}'  */
#line 4297 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 13382 "parse.c"
    break;

  case 473: /* brace_block: k_do do_body k_end  */
#line 4305 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    /*%%%*/
			(yyval.node)->nd_body->nd_loc = code_loc_gen(&(yylsp[-2]), &(yylsp[0]));
			nd_set_line((yyval.node), (yylsp[-2]).end_pos.lineno);
		    /*% %*/
		    }
#line 13394 "parse.c"
    break;

  case 474: /* @32: %empty  */
#line 4314 "parse.y"
                  {(yyval.vars) = dyna_push(p);}
#line 13400 "parse.c"
    break;

  case 475: /* @33: %empty  */
#line 4315 "parse.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 13409 "parse.c"
    break;

  case 476: /* @34: %empty  */
#line 4319 "parse.y"
                    {
			(yyval.node) = numparam_push(p);
		    }
#line 13417 "parse.c"
    break;

  case 477: /* brace_body: @32 @33 @34 opt_block_param compstmt  */
#line 4323 "parse.y"
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
#line 13433 "parse.c"
    break;

  case 478: /* @35: %empty  */
#line 4336 "parse.y"
                  {(yyval.vars) = dyna_push(p);}
#line 13439 "parse.c"
    break;

  case 479: /* @36: %empty  */
#line 4337 "parse.y"
                    {
			(yyval.num) = p->max_numparam;
			p->max_numparam = 0;
		    }
#line 13448 "parse.c"
    break;

  case 480: /* @37: %empty  */
#line 4341 "parse.y"
                    {
			(yyval.node) = numparam_push(p);
			CMDARG_PUSH(0);
		    }
#line 13457 "parse.c"
    break;

  case 481: /* do_body: @35 @36 @37 opt_block_param bodystmt  */
#line 4346 "parse.y"
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
#line 13474 "parse.c"
    break;

  case 482: /* case_args: arg_value  */
#line 4361 "parse.y"
                    {
		    /*%%%*/
			check_literal_when(p, (yyvsp[0].node), &(yylsp[0]));
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!(args_new!, $1) %*/
		    }
#line 13486 "parse.c"
    break;

  case 483: /* case_args: "*" arg_value  */
#line 4369 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_SPLAT((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!(args_new!, $2) %*/
		    }
#line 13497 "parse.c"
    break;

  case 484: /* case_args: case_args ',' arg_value  */
#line 4376 "parse.y"
                    {
		    /*%%%*/
			check_literal_when(p, (yyvsp[0].node), &(yylsp[0]));
			(yyval.node) = last_arg_append(p, (yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add!($1, $3) %*/
		    }
#line 13509 "parse.c"
    break;

  case 485: /* case_args: case_args ',' "*" arg_value  */
#line 4384 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = rest_arg_append(p, (yyvsp[-3].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: args_add_star!($1, $4) %*/
		    }
#line 13520 "parse.c"
    break;

  case 486: /* case_body: k_when case_args then compstmt cases  */
#line 4395 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_WHEN((yyvsp[-3].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
			fixpos((yyval.node), (yyvsp[-3].node));
		    /*% %*/
		    /*% ripper: when!($2, $4, escape_Qundef($5)) %*/
		    }
#line 13532 "parse.c"
    break;

  case 489: /* @38: %empty  */
#line 4409 "parse.y"
                    {
			SET_LEX_STATE(EXPR_BEG|EXPR_LABEL);
			p->command_start = FALSE;
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			(yyval.tbl) = push_pvtbl(p);
		    }
#line 13544 "parse.c"
    break;

  case 490: /* @39: %empty  */
#line 4416 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
		    }
#line 13552 "parse.c"
    break;

  case 491: /* $@40: %empty  */
#line 4420 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			pop_pvtbl(p, (yyvsp[-3].tbl));
			p->ctxt.in_kwarg = (yyvsp[-4].ctxt).in_kwarg;
		    }
#line 13562 "parse.c"
    break;

  case 492: /* p_case_body: "`in'" @38 @39 p_top_expr then $@40 compstmt p_cases  */
#line 4427 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_IN((yyvsp[-4].node), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: in!($4, $7, escape_Qundef($8)) %*/
		    }
#line 13573 "parse.c"
    break;

  case 496: /* p_top_expr: p_top_expr_body "`if' modifier" expr_value  */
#line 4441 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_if(p, (yyvsp[0].node), (yyvsp[-2].node), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: if_mod!($3, $1) %*/
		    }
#line 13585 "parse.c"
    break;

  case 497: /* p_top_expr: p_top_expr_body "`unless' modifier" expr_value  */
#line 4449 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_unless(p, (yyvsp[0].node), (yyvsp[-2].node), 0, &(yyloc));
			fixpos((yyval.node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: unless_mod!($3, $1) %*/
		    }
#line 13597 "parse.c"
    break;

  case 499: /* p_top_expr_body: p_expr ','  */
#line 4460 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 1, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, Qnone, get_value((yyvsp[-1].node)), (yyval.node), &(yyloc));
		    }
#line 13606 "parse.c"
    break;

  case 500: /* p_top_expr_body: p_expr ',' p_args  */
#line 4465 "parse.y"
                    {
			(yyval.node) = new_array_pattern(p, Qnone, get_value((yyvsp[-2].node)), (yyvsp[0].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-2]).beg_pos);
		    /*%
		    %*/
		    }
#line 13618 "parse.c"
    break;

  case 501: /* p_top_expr_body: p_find  */
#line 4473 "parse.y"
                    {
			(yyval.node) = new_find_pattern(p, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 13626 "parse.c"
    break;

  case 502: /* p_top_expr_body: p_args_tail  */
#line 4477 "parse.y"
                    {
			(yyval.node) = new_array_pattern(p, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 13634 "parse.c"
    break;

  case 503: /* p_top_expr_body: p_kwargs  */
#line 4481 "parse.y"
                    {
			(yyval.node) = new_hash_pattern(p, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 13642 "parse.c"
    break;

  case 505: /* p_as: p_expr "=>" p_variable  */
#line 4490 "parse.y"
                    {
		    /*%%%*/
			NODE *n = NEW_LIST((yyvsp[-2].node), &(yyloc));
			n = list_append(p, n, (yyvsp[0].node));
			(yyval.node) = new_hash(p, n, &(yyloc));
		    /*% %*/
		    /*% ripper: binary!($1, STATIC_ID2SYM((id_assoc)), $3) %*/
		    }
#line 13655 "parse.c"
    break;

  case 507: /* p_alt: p_alt '|' p_expr_basic  */
#line 4502 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_NODE(NODE_OR, (yyvsp[-2].node), (yyvsp[0].node), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: binary!($1, STATIC_ID2SYM(idOr), $3) %*/
		    }
#line 13666 "parse.c"
    break;

  case 509: /* p_lparen: '('  */
#line 4511 "parse.y"
                      {(yyval.tbl) = push_pktbl(p);}
#line 13672 "parse.c"
    break;

  case 510: /* p_lbracket: '['  */
#line 4512 "parse.y"
                      {(yyval.tbl) = push_pktbl(p);}
#line 13678 "parse.c"
    break;

  case 513: /* p_expr_basic: p_const p_lparen p_args rparen  */
#line 4517 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_array_pattern(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 13691 "parse.c"
    break;

  case 514: /* p_expr_basic: p_const p_lparen p_find rparen  */
#line 4526 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_find_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 13704 "parse.c"
    break;

  case 515: /* p_expr_basic: p_const p_lparen p_kwargs rparen  */
#line 4535 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_hash_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 13717 "parse.c"
    break;

  case 516: /* p_expr_basic: p_const '(' rparen  */
#line 4544 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, (yyvsp[-2].node), Qnone, (yyval.node), &(yyloc));
		    }
#line 13726 "parse.c"
    break;

  case 517: /* p_expr_basic: p_const p_lbracket p_args rbracket  */
#line 4549 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_array_pattern(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 13739 "parse.c"
    break;

  case 518: /* p_expr_basic: p_const p_lbracket p_find rbracket  */
#line 4558 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_find_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 13752 "parse.c"
    break;

  case 519: /* p_expr_basic: p_const p_lbracket p_kwargs rbracket  */
#line 4567 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = new_hash_pattern(p, (yyvsp[-3].node), (yyvsp[-1].node), &(yyloc));
		    /*%%%*/
			nd_set_first_loc((yyval.node), (yylsp[-3]).beg_pos);
		    /*%
		    %*/
		    }
#line 13765 "parse.c"
    break;

  case 520: /* p_expr_basic: p_const '[' rbracket  */
#line 4576 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, (yyvsp[-2].node), Qnone, (yyval.node), &(yyloc));
		    }
#line 13774 "parse.c"
    break;

  case 521: /* p_expr_basic: "[" p_args rbracket  */
#line 4581 "parse.y"
                    {
			(yyval.node) = new_array_pattern(p, Qnone, Qnone, (yyvsp[-1].node), &(yyloc));
		    }
#line 13782 "parse.c"
    break;

  case 522: /* p_expr_basic: "[" p_find rbracket  */
#line 4585 "parse.y"
                    {
			(yyval.node) = new_find_pattern(p, Qnone, (yyvsp[-1].node), &(yyloc));
		    }
#line 13790 "parse.c"
    break;

  case 523: /* p_expr_basic: "[" rbracket  */
#line 4589 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 0, 0, Qnone, &(yyloc));
			(yyval.node) = new_array_pattern(p, Qnone, Qnone, (yyval.node), &(yyloc));
		    }
#line 13799 "parse.c"
    break;

  case 524: /* @41: %empty  */
#line 4594 "parse.y"
                    {
			(yyval.tbl) = push_pktbl(p);
			(yyvsp[0].ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 0;
		    }
#line 13809 "parse.c"
    break;

  case 525: /* p_expr_basic: "{" @41 p_kwargs rbrace  */
#line 4600 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			p->ctxt.in_kwarg = (yyvsp[-3].ctxt).in_kwarg;
			(yyval.node) = new_hash_pattern(p, Qnone, (yyvsp[-1].node), &(yyloc));
		    }
#line 13819 "parse.c"
    break;

  case 526: /* p_expr_basic: "{" rbrace  */
#line 4606 "parse.y"
                    {
			(yyval.node) = new_hash_pattern_tail(p, Qnone, 0, &(yyloc));
			(yyval.node) = new_hash_pattern(p, Qnone, (yyval.node), &(yyloc));
		    }
#line 13828 "parse.c"
    break;

  case 527: /* @42: %empty  */
#line 4610 "parse.y"
                          {(yyval.tbl) = push_pktbl(p);}
#line 13834 "parse.c"
    break;

  case 528: /* p_expr_basic: "(" @42 p_expr rparen  */
#line 4611 "parse.y"
                    {
			pop_pktbl(p, (yyvsp[-2].tbl));
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 13843 "parse.c"
    break;

  case 529: /* p_args: p_expr  */
#line 4618 "parse.y"
                    {
		    /*%%%*/
			NODE *pre_args = NEW_LIST((yyvsp[0].node), &(yyloc));
			(yyval.node) = new_array_pattern_tail(p, pre_args, 0, 0, Qnone, &(yyloc));
		    /*%
			$$ = new_array_pattern_tail(p, rb_ary_new_from_args(1, get_value($1)), 0, 0, Qnone, &@$);
		    %*/
		    }
#line 13856 "parse.c"
    break;

  case 530: /* p_args: p_args_head  */
#line 4627 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[0].node), 1, 0, Qnone, &(yyloc));
		    }
#line 13864 "parse.c"
    break;

  case 531: /* p_args: p_args_head p_arg  */
#line 4631 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_array_pattern_tail(p, list_concat((yyvsp[-1].node), (yyvsp[0].node)), 0, 0, Qnone, &(yyloc));
		    /*%
			VALUE pre_args = rb_ary_concat($1, get_value($2));
			$$ = new_array_pattern_tail(p, pre_args, 0, 0, Qnone, &@$);
		    %*/
		    }
#line 13877 "parse.c"
    break;

  case 532: /* p_args: p_args_head p_rest  */
#line 4640 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-1].node), 1, (yyvsp[0].id), Qnone, &(yyloc));
		    }
#line 13885 "parse.c"
    break;

  case 533: /* p_args: p_args_head p_rest ',' p_args_post  */
#line 4644 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, (yyvsp[-3].node), 1, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    }
#line 13893 "parse.c"
    break;

  case 535: /* p_args_head: p_arg ','  */
#line 4651 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 13901 "parse.c"
    break;

  case 536: /* p_args_head: p_args_head p_arg ','  */
#line 4655 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_concat((yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: rb_ary_concat($1, get_value($2)) %*/
		    }
#line 13912 "parse.c"
    break;

  case 537: /* p_args_tail: p_rest  */
#line 4664 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 1, (yyvsp[0].id), Qnone, &(yyloc));
		    }
#line 13920 "parse.c"
    break;

  case 538: /* p_args_tail: p_rest ',' p_args_post  */
#line 4668 "parse.y"
                    {
			(yyval.node) = new_array_pattern_tail(p, Qnone, 1, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc));
		    }
#line 13928 "parse.c"
    break;

  case 539: /* p_find: p_rest ',' p_args_post ',' p_rest  */
#line 4674 "parse.y"
                    {
			(yyval.node) = new_find_pattern_tail(p, (yyvsp[-4].id), (yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    }
#line 13936 "parse.c"
    break;

  case 540: /* p_rest: "*" "local variable or method"  */
#line 4681 "parse.y"
                    {
			(yyval.id) = (yyvsp[0].id);
		    }
#line 13944 "parse.c"
    break;

  case 541: /* p_rest: "*"  */
#line 4685 "parse.y"
                    {
			(yyval.id) = 0;
		    }
#line 13952 "parse.c"
    break;

  case 543: /* p_args_post: p_args_post ',' p_arg  */
#line 4692 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_concat((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_concat($1, get_value($3)) %*/
		    }
#line 13963 "parse.c"
    break;

  case 544: /* p_arg: p_expr  */
#line 4701 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_ary_new_from_args(1, get_value($1)) %*/
		    }
#line 13974 "parse.c"
    break;

  case 545: /* p_kwargs: p_kwarg ',' p_any_kwrest  */
#line 4710 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[-2].node), &(yyloc)), (yyvsp[0].id), &(yyloc));
		    }
#line 13982 "parse.c"
    break;

  case 546: /* p_kwargs: p_kwarg  */
#line 4714 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[0].node), &(yyloc)), 0, &(yyloc));
		    }
#line 13990 "parse.c"
    break;

  case 547: /* p_kwargs: p_kwarg ','  */
#line 4718 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_unique_key_hash(p, (yyvsp[-1].node), &(yyloc)), 0, &(yyloc));
		    }
#line 13998 "parse.c"
    break;

  case 548: /* p_kwargs: p_any_kwrest  */
#line 4722 "parse.y"
                    {
			(yyval.node) =  new_hash_pattern_tail(p, new_hash(p, Qnone, &(yyloc)), (yyvsp[0].id), &(yyloc));
		    }
#line 14006 "parse.c"
    break;

  case 550: /* p_kwarg: p_kwarg ',' p_kw  */
#line 4730 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_concat((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, $3) %*/
		    }
#line 14017 "parse.c"
    break;

  case 551: /* p_kw: p_kw_label p_expr  */
#line 4739 "parse.y"
                    {
			error_duplicate_pattern_key(p, get_id((yyvsp[-1].id)), &(yylsp[-1]));
		    /*%%%*/
			(yyval.node) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[-1].id)), &(yylsp[-1])), &(yyloc)), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_new_from_args(2, get_value($1), get_value($2)) %*/
		    }
#line 14029 "parse.c"
    break;

  case 552: /* p_kw: p_kw_label  */
#line 4747 "parse.y"
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
#line 14045 "parse.c"
    break;

  case 554: /* p_kw_label: "string literal" string_contents tLABEL_END  */
#line 4762 "parse.y"
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
#line 14070 "parse.c"
    break;

  case 555: /* p_kwrest: kwrest_mark "local variable or method"  */
#line 4785 "parse.y"
                    {
		        (yyval.id) = (yyvsp[0].id);
		    }
#line 14078 "parse.c"
    break;

  case 556: /* p_kwrest: kwrest_mark  */
#line 4789 "parse.y"
                    {
		        (yyval.id) = 0;
		    }
#line 14086 "parse.c"
    break;

  case 557: /* p_kwnorest: kwrest_mark "`nil'"  */
#line 4795 "parse.y"
                    {
		        (yyval.id) = 0;
		    }
#line 14094 "parse.c"
    break;

  case 559: /* p_any_kwrest: p_kwnorest  */
#line 4801 "parse.y"
                             {(yyval.id) = ID2VAL(idNil);}
#line 14100 "parse.c"
    break;

  case 561: /* p_value: p_primitive ".." p_primitive  */
#line 4806 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, $3) %*/
		    }
#line 14113 "parse.c"
    break;

  case 562: /* p_value: p_primitive "..." p_primitive  */
#line 4815 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-2].node));
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3((yyvsp[-2].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, $3) %*/
		    }
#line 14126 "parse.c"
    break;

  case 563: /* p_value: p_primitive ".."  */
#line 4824 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT2((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!($1, Qnil) %*/
		    }
#line 14138 "parse.c"
    break;

  case 564: /* p_value: p_primitive "..."  */
#line 4832 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[-1].node));
			(yyval.node) = NEW_DOT3((yyvsp[-1].node), new_nil_at(p, &(yylsp[0]).end_pos), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!($1, Qnil) %*/
		    }
#line 14150 "parse.c"
    break;

  case 568: /* p_value: "(.." p_primitive  */
#line 4843 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT2(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot2!(Qnil, $2) %*/
		    }
#line 14162 "parse.c"
    break;

  case 569: /* p_value: "(..." p_primitive  */
#line 4851 "parse.y"
                    {
		    /*%%%*/
			value_expr((yyvsp[0].node));
			(yyval.node) = NEW_DOT3(new_nil_at(p, &(yylsp[-1]).beg_pos), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dot3!(Qnil, $2) %*/
		    }
#line 14174 "parse.c"
    break;

  case 578: /* p_primitive: keyword_variable  */
#line 4869 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc)))) (yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 14185 "parse.c"
    break;

  case 580: /* p_variable: "local variable or method"  */
#line 4879 "parse.y"
                    {
		    /*%%%*/
			error_duplicate_pattern_variable(p, (yyvsp[0].id), &(yylsp[0]));
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 14197 "parse.c"
    break;

  case 581: /* p_var_ref: '^' "local variable or method"  */
#line 4889 "parse.y"
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
#line 14212 "parse.c"
    break;

  case 582: /* p_var_ref: '^' nonlocal_var  */
#line 4900 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc)))) (yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($2) %*/
                    }
#line 14223 "parse.c"
    break;

  case 583: /* p_expr_ref: '^' "(" expr_value rparen  */
#line 4909 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_BEGIN((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: begin!($3) %*/
		    }
#line 14234 "parse.c"
    break;

  case 584: /* p_const: ":: at EXPR_BEG" cname  */
#line 4918 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON3((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: top_const_ref!($2) %*/
		    }
#line 14245 "parse.c"
    break;

  case 585: /* p_const: p_const "::" cname  */
#line 4925 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_COLON2((yyvsp[-2].node), (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: const_path_ref!($1, $3) %*/
		    }
#line 14256 "parse.c"
    break;

  case 586: /* p_const: "constant"  */
#line 4932 "parse.y"
                   {
		    /*%%%*/
			(yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		   }
#line 14267 "parse.c"
    break;

  case 587: /* opt_rescue: k_rescue exc_list exc_var then compstmt opt_rescue  */
#line 4943 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_RESBODY((yyvsp[-4].node),
					 (yyvsp[-3].node) ? block_append(p, node_assign(p, (yyvsp[-3].node), NEW_ERRINFO(&(yylsp[-3])), NO_LEX_CTXT, &(yylsp[-3])), (yyvsp[-1].node)) : (yyvsp[-1].node),
					 (yyvsp[0].node), &(yyloc));

                        if ((yyvsp[-4].node)) {
                            fixpos((yyval.node), (yyvsp[-4].node));
                        }
                        else if ((yyvsp[-3].node)) {
                            fixpos((yyval.node), (yyvsp[-3].node));
                        }
                        else {
                            fixpos((yyval.node), (yyvsp[-1].node));
                        }
		    /*% %*/
		    /*% ripper: rescue!(escape_Qundef($2), escape_Qundef($3), escape_Qundef($5), escape_Qundef($6)) %*/
		    }
#line 14290 "parse.c"
    break;

  case 589: /* exc_list: arg_value  */
#line 4965 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_LIST((yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 14301 "parse.c"
    break;

  case 590: /* exc_list: mrhs  */
#line 4972 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = splat_array((yyvsp[0].node)))) (yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: $1 %*/
		    }
#line 14312 "parse.c"
    break;

  case 592: /* exc_var: "=>" lhs  */
#line 4982 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 14320 "parse.c"
    break;

  case 594: /* opt_ensure: k_ensure compstmt  */
#line 4989 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: ensure!($2) %*/
		    }
#line 14331 "parse.c"
    break;

  case 598: /* strings: string  */
#line 5003 "parse.y"
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
#line 14350 "parse.c"
    break;

  case 601: /* string: string string1  */
#line 5022 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: string_concat!($1, $2) %*/
		    }
#line 14361 "parse.c"
    break;

  case 602: /* string1: "string literal" string_contents "terminator"  */
#line 5031 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = heredoc_dedent(p, (yyvsp[-1].node));
			if ((yyval.node)) nd_set_loc((yyval.node), &(yyloc));
		    /*% %*/
		    /*% ripper: string_literal!(heredoc_dedent(p, $2)) %*/
		    }
#line 14373 "parse.c"
    break;

  case 603: /* xstring: "backtick literal" xstring_contents "terminator"  */
#line 5041 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = new_xstring(p, heredoc_dedent(p, (yyvsp[-1].node)), &(yyloc));
		    /*% %*/
		    /*% ripper: xstring_literal!(heredoc_dedent(p, $2)) %*/
		    }
#line 14384 "parse.c"
    break;

  case 604: /* regexp: "regexp literal" regexp_contents tREGEXP_END  */
#line 5050 "parse.y"
                    {
			(yyval.node) = new_regexp(p, (yyvsp[-1].node), (yyvsp[0].num), &(yyloc));
		    }
#line 14392 "parse.c"
    break;

  case 605: /* words: "word list" ' ' word_list "terminator"  */
#line 5056 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 14403 "parse.c"
    break;

  case 606: /* word_list: %empty  */
#line 5065 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: words_new! %*/
		    }
#line 14414 "parse.c"
    break;

  case 607: /* word_list: word_list word ' '  */
#line 5072 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), evstr2dstr(p, (yyvsp[-1].node)));
		    /*% %*/
		    /*% ripper: words_add!($1, $2) %*/
		    }
#line 14425 "parse.c"
    break;

  case 609: /* word: word string_content  */
#line 5083 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: word_add!($1, $2) %*/
		    }
#line 14436 "parse.c"
    break;

  case 610: /* symbols: "symbol list" ' ' symbol_list "terminator"  */
#line 5092 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 14447 "parse.c"
    break;

  case 611: /* symbol_list: %empty  */
#line 5101 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: symbols_new! %*/
		    }
#line 14458 "parse.c"
    break;

  case 612: /* symbol_list: symbol_list word ' '  */
#line 5108 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = symbol_append(p, (yyvsp[-2].node), evstr2dstr(p, (yyvsp[-1].node)));
		    /*% %*/
		    /*% ripper: symbols_add!($1, $2) %*/
		    }
#line 14469 "parse.c"
    break;

  case 613: /* qwords: "verbatim word list" ' ' qword_list "terminator"  */
#line 5117 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 14480 "parse.c"
    break;

  case 614: /* qsymbols: "verbatim symbol list" ' ' qsym_list "terminator"  */
#line 5126 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = make_list((yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: array!($3) %*/
		    }
#line 14491 "parse.c"
    break;

  case 615: /* qword_list: %empty  */
#line 5135 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: qwords_new! %*/
		    }
#line 14502 "parse.c"
    break;

  case 616: /* qword_list: qword_list "literal content" ' '  */
#line 5142 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, (yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: qwords_add!($1, $2) %*/
		    }
#line 14513 "parse.c"
    break;

  case 617: /* qsym_list: %empty  */
#line 5151 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: qsymbols_new! %*/
		    }
#line 14524 "parse.c"
    break;

  case 618: /* qsym_list: qsym_list "literal content" ' '  */
#line 5158 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = symbol_append(p, (yyvsp[-2].node), (yyvsp[-1].node));
		    /*% %*/
		    /*% ripper: qsymbols_add!($1, $2) %*/
		    }
#line 14535 "parse.c"
    break;

  case 619: /* string_contents: %empty  */
#line 5167 "parse.y"
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
#line 14550 "parse.c"
    break;

  case 620: /* string_contents: string_contents string_content  */
#line 5178 "parse.y"
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
#line 14570 "parse.c"
    break;

  case 621: /* xstring_contents: %empty  */
#line 5196 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: xstring_new! %*/
		    }
#line 14581 "parse.c"
    break;

  case 622: /* xstring_contents: xstring_contents string_content  */
#line 5203 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = literal_concat(p, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    /*% %*/
		    /*% ripper: xstring_add!($1, $2) %*/
		    }
#line 14592 "parse.c"
    break;

  case 623: /* regexp_contents: %empty  */
#line 5212 "parse.y"
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
#line 14607 "parse.c"
    break;

  case 624: /* regexp_contents: regexp_contents string_content  */
#line 5223 "parse.y"
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
#line 14650 "parse.c"
    break;

  case 626: /* @43: %empty  */
#line 5266 "parse.y"
                    {
			/* need to backup p->lex.strterm so that a string literal `%&foo,#$&,bar&` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 14661 "parse.c"
    break;

  case 627: /* string_content: tSTRING_DVAR @43 string_dvar  */
#line 5273 "parse.y"
                    {
			p->lex.strterm = (yyvsp[-1].strterm);
		    /*%%%*/
			(yyval.node) = NEW_EVSTR((yyvsp[0].node), &(yyloc));
			nd_set_line((yyval.node), (yylsp[0]).end_pos.lineno);
		    /*% %*/
		    /*% ripper: string_dvar!($3) %*/
		    }
#line 14674 "parse.c"
    break;

  case 628: /* $@44: %empty  */
#line 5282 "parse.y"
                    {
			CMDARG_PUSH(0);
			COND_PUSH(0);
		    }
#line 14683 "parse.c"
    break;

  case 629: /* @45: %empty  */
#line 5286 "parse.y"
                    {
			/* need to backup p->lex.strterm so that a string literal `%!foo,#{ !0 },bar!` can be parsed */
			(yyval.strterm) = p->lex.strterm;
			p->lex.strterm = 0;
		    }
#line 14693 "parse.c"
    break;

  case 630: /* @46: %empty  */
#line 5291 "parse.y"
                    {
			(yyval.num) = p->lex.state;
			SET_LEX_STATE(EXPR_BEG);
		    }
#line 14702 "parse.c"
    break;

  case 631: /* @47: %empty  */
#line 5295 "parse.y"
                    {
			(yyval.num) = p->lex.brace_nest;
			p->lex.brace_nest = 0;
		    }
#line 14711 "parse.c"
    break;

  case 632: /* @48: %empty  */
#line 5299 "parse.y"
                    {
			(yyval.num) = p->heredoc_indent;
			p->heredoc_indent = 0;
		    }
#line 14720 "parse.c"
    break;

  case 633: /* string_content: tSTRING_DBEG $@44 @45 @46 @47 @48 compstmt "'}'"  */
#line 5304 "parse.y"
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
#line 14739 "parse.c"
    break;

  case 634: /* string_dvar: "global variable"  */
#line 5321 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_GVAR((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 14750 "parse.c"
    break;

  case 635: /* string_dvar: "instance variable"  */
#line 5328 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_IVAR((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 14761 "parse.c"
    break;

  case 636: /* string_dvar: "class variable"  */
#line 5335 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = NEW_CVAR((yyvsp[0].id), &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 14772 "parse.c"
    break;

  case 640: /* ssym: "symbol literal" sym  */
#line 5349 "parse.y"
                    {
			SET_LEX_STATE(EXPR_END);
		    /*%%%*/
			(yyval.node) = NEW_LIT(ID2SYM((yyvsp[0].id)), &(yyloc));
		    /*% %*/
		    /*% ripper: symbol_literal!(symbol!($2)) %*/
		    }
#line 14784 "parse.c"
    break;

  case 643: /* dsym: "symbol literal" string_contents "terminator"  */
#line 5363 "parse.y"
                    {
			SET_LEX_STATE(EXPR_END);
		    /*%%%*/
			(yyval.node) = dsym_node(p, (yyvsp[-1].node), &(yyloc));
		    /*% %*/
		    /*% ripper: dyna_symbol!($2) %*/
		    }
#line 14796 "parse.c"
    break;

  case 645: /* numeric: tUMINUS_NUM simple_numeric  */
#line 5374 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
			RB_OBJ_WRITE(p->ast, &(yyval.node)->nd_lit, negate_lit(p, (yyval.node)->nd_lit));
		    /*% %*/
		    /*% ripper: unary!(ID2VAL(idUMinus), $2) %*/
		    }
#line 14808 "parse.c"
    break;

  case 656: /* keyword_variable: "`nil'"  */
#line 5399 "parse.y"
                              {(yyval.id) = KWD2EID(nil, (yyvsp[0].id));}
#line 14814 "parse.c"
    break;

  case 657: /* keyword_variable: "`self'"  */
#line 5400 "parse.y"
                               {(yyval.id) = KWD2EID(self, (yyvsp[0].id));}
#line 14820 "parse.c"
    break;

  case 658: /* keyword_variable: "`true'"  */
#line 5401 "parse.y"
                               {(yyval.id) = KWD2EID(true, (yyvsp[0].id));}
#line 14826 "parse.c"
    break;

  case 659: /* keyword_variable: "`false'"  */
#line 5402 "parse.y"
                                {(yyval.id) = KWD2EID(false, (yyvsp[0].id));}
#line 14832 "parse.c"
    break;

  case 660: /* keyword_variable: "`__FILE__'"  */
#line 5403 "parse.y"
                                  {(yyval.id) = KWD2EID(_FILE__, (yyvsp[0].id));}
#line 14838 "parse.c"
    break;

  case 661: /* keyword_variable: "`__LINE__'"  */
#line 5404 "parse.y"
                                  {(yyval.id) = KWD2EID(_LINE__, (yyvsp[0].id));}
#line 14844 "parse.c"
    break;

  case 662: /* keyword_variable: "`__ENCODING__'"  */
#line 5405 "parse.y"
                                      {(yyval.id) = KWD2EID(_ENCODING__, (yyvsp[0].id));}
#line 14850 "parse.c"
    break;

  case 663: /* var_ref: user_variable  */
#line 5409 "parse.y"
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
#line 14867 "parse.c"
    break;

  case 664: /* var_ref: keyword_variable  */
#line 5422 "parse.y"
                    {
		    /*%%%*/
			if (!((yyval.node) = gettable(p, (yyvsp[0].id), &(yyloc)))) (yyval.node) = NEW_BEGIN(0, &(yyloc));
		    /*% %*/
		    /*% ripper: var_ref!($1) %*/
		    }
#line 14878 "parse.c"
    break;

  case 665: /* var_lhs: user_variable  */
#line 5431 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 14889 "parse.c"
    break;

  case 666: /* var_lhs: keyword_variable  */
#line 5438 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = assignable(p, (yyvsp[0].id), 0, &(yyloc));
		    /*% %*/
		    /*% ripper: assignable(p, var_field(p, $1)) %*/
		    }
#line 14900 "parse.c"
    break;

  case 669: /* $@49: %empty  */
#line 5451 "parse.y"
                    {
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 14909 "parse.c"
    break;

  case 670: /* superclass: '<' $@49 expr_value term  */
#line 5456 "parse.y"
                    {
			(yyval.node) = (yyvsp[-1].node);
		    }
#line 14917 "parse.c"
    break;

  case 671: /* superclass: %empty  */
#line 5460 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = 0;
		    /*% %*/
		    /*% ripper: Qnil %*/
		    }
#line 14928 "parse.c"
    break;

  case 673: /* f_opt_paren_args: none  */
#line 5470 "parse.y"
                    {
			p->ctxt.in_argdef = 0;
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[-1]));
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyval.node), &(yylsp[-1]));
		    }
#line 14938 "parse.c"
    break;

  case 674: /* f_paren_args: '(' f_args rparen  */
#line 5478 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: paren!($2) %*/
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
			p->ctxt.in_argdef = 0;
		    }
#line 14952 "parse.c"
    break;

  case 676: /* @50: %empty  */
#line 5490 "parse.y"
                    {
			(yyval.ctxt) = p->ctxt;
			p->ctxt.in_kwarg = 1;
			p->ctxt.in_argdef = 1;
			SET_LEX_STATE(p->lex.state|EXPR_LABEL); /* force for args */
		    }
#line 14963 "parse.c"
    break;

  case 677: /* f_arglist: @50 f_args term  */
#line 5497 "parse.y"
                    {
			p->ctxt.in_kwarg = (yyvsp[-2].ctxt).in_kwarg;
			p->ctxt.in_argdef = 0;
			(yyval.node) = (yyvsp[-1].node);
			SET_LEX_STATE(EXPR_BEG);
			p->command_start = TRUE;
		    }
#line 14975 "parse.c"
    break;

  case 678: /* args_tail: f_kwarg ',' f_kwrest opt_f_block_arg  */
#line 5507 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-3].node), (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 14983 "parse.c"
    break;

  case 679: /* args_tail: f_kwarg opt_f_block_arg  */
#line 5511 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, (yyvsp[-1].node), Qnone, (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 14991 "parse.c"
    break;

  case 680: /* args_tail: f_any_kwrest opt_f_block_arg  */
#line 5515 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, (yyvsp[-1].id), (yyvsp[0].id), &(yylsp[-1]));
		    }
#line 14999 "parse.c"
    break;

  case 681: /* args_tail: f_block_arg  */
#line 5519 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, (yyvsp[0].id), &(yylsp[0]));
		    }
#line 15007 "parse.c"
    break;

  case 682: /* args_tail: args_forward  */
#line 5523 "parse.y"
                    {
			add_forwarding_args(p);
			(yyval.node) = new_args_tail(p, Qnone, (yyvsp[0].id), ID2VAL(idFWD_BLOCK), &(yylsp[0]));
		    /*%%%*/
			((yyval.node)->nd_ainfo)->forwarding = 1;
		    /*% %*/
		    }
#line 15019 "parse.c"
    break;

  case 683: /* opt_args_tail: ',' args_tail  */
#line 5533 "parse.y"
                    {
			(yyval.node) = (yyvsp[0].node);
		    }
#line 15027 "parse.c"
    break;

  case 684: /* opt_args_tail: %empty  */
#line 5537 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
		    }
#line 15035 "parse.c"
    break;

  case 685: /* f_args: f_arg ',' f_optarg ',' f_rest_arg opt_args_tail  */
#line 5543 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 15043 "parse.c"
    break;

  case 686: /* f_args: f_arg ',' f_optarg ',' f_rest_arg ',' f_arg opt_args_tail  */
#line 5547 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-7].node), (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 15051 "parse.c"
    break;

  case 687: /* f_args: f_arg ',' f_optarg opt_args_tail  */
#line 5551 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 15059 "parse.c"
    break;

  case 688: /* f_args: f_arg ',' f_optarg ',' f_arg opt_args_tail  */
#line 5555 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 15067 "parse.c"
    break;

  case 689: /* f_args: f_arg ',' f_rest_arg opt_args_tail  */
#line 5559 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-3].node), Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 15075 "parse.c"
    break;

  case 690: /* f_args: f_arg ',' f_rest_arg ',' f_arg opt_args_tail  */
#line 5563 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-5].node), Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 15083 "parse.c"
    break;

  case 691: /* f_args: f_arg opt_args_tail  */
#line 5567 "parse.y"
                    {
			(yyval.node) = new_args(p, (yyvsp[-1].node), Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 15091 "parse.c"
    break;

  case 692: /* f_args: f_optarg ',' f_rest_arg opt_args_tail  */
#line 5571 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 15099 "parse.c"
    break;

  case 693: /* f_args: f_optarg ',' f_rest_arg ',' f_arg opt_args_tail  */
#line 5575 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-5].node), (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 15107 "parse.c"
    break;

  case 694: /* f_args: f_optarg opt_args_tail  */
#line 5579 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-1].node), Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 15115 "parse.c"
    break;

  case 695: /* f_args: f_optarg ',' f_arg opt_args_tail  */
#line 5583 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, (yyvsp[-3].node), Qnone, (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 15123 "parse.c"
    break;

  case 696: /* f_args: f_rest_arg opt_args_tail  */
#line 5587 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-1].id), Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 15131 "parse.c"
    break;

  case 697: /* f_args: f_rest_arg ',' f_arg opt_args_tail  */
#line 5591 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, (yyvsp[-3].id), (yyvsp[-1].node), (yyvsp[0].node), &(yyloc));
		    }
#line 15139 "parse.c"
    break;

  case 698: /* f_args: args_tail  */
#line 5595 "parse.y"
                    {
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyvsp[0].node), &(yyloc));
		    }
#line 15147 "parse.c"
    break;

  case 699: /* f_args: %empty  */
#line 5599 "parse.y"
                    {
			(yyval.node) = new_args_tail(p, Qnone, Qnone, Qnone, &(yylsp[0]));
			(yyval.node) = new_args(p, Qnone, Qnone, Qnone, Qnone, (yyval.node), &(yylsp[0]));
		    }
#line 15156 "parse.c"
    break;

  case 700: /* args_forward: "(..."  */
#line 5606 "parse.y"
                    {
		    /*%%%*/
#ifdef FORWARD_ARGS_WITH_RUBY2_KEYWORDS
			(yyval.id) = 0;
#else
			(yyval.id) = idFWD_KWREST;
#endif
		    /*% %*/
		    /*% ripper: args_forward! %*/
		    }
#line 15171 "parse.c"
    break;

  case 701: /* f_bad_arg: "constant"  */
#line 5619 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be a constant";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 15184 "parse.c"
    break;

  case 702: /* f_bad_arg: "instance variable"  */
#line 5628 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be an instance variable";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 15197 "parse.c"
    break;

  case 703: /* f_bad_arg: "global variable"  */
#line 5637 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be a global variable";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 15210 "parse.c"
    break;

  case 704: /* f_bad_arg: "class variable"  */
#line 5646 "parse.y"
                    {
			static const char mesg[] = "formal argument cannot be a class variable";
		    /*%%%*/
			yyerror1(&(yylsp[0]), mesg);
			(yyval.id) = 0;
		    /*% %*/
		    /*% ripper[error]: param_error!(ERR_MESG(), $1) %*/
		    }
#line 15223 "parse.c"
    break;

  case 706: /* f_norm_arg: "local variable or method"  */
#line 5658 "parse.y"
                    {
			formal_argument(p, (yyvsp[0].id));
			p->max_numparam = ORDINAL_PARAM;
			(yyval.id) = (yyvsp[0].id);
		    }
#line 15233 "parse.c"
    break;

  case 707: /* f_arg_asgn: f_norm_arg  */
#line 5666 "parse.y"
                    {
			ID id = get_id((yyvsp[0].id));
			arg_var(p, id);
			p->cur_arg = id;
			(yyval.id) = (yyvsp[0].id);
		    }
#line 15244 "parse.c"
    break;

  case 708: /* f_arg_item: f_arg_asgn  */
#line 5675 "parse.y"
                    {
			p->cur_arg = 0;
		    /*%%%*/
			(yyval.node) = NEW_ARGS_AUX((yyvsp[0].id), 1, &NULL_LOC);
		    /*% %*/
		    /*% ripper: get_value($1) %*/
		    }
#line 15256 "parse.c"
    break;

  case 709: /* f_arg_item: "(" f_margs rparen  */
#line 5683 "parse.y"
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
#line 15279 "parse.c"
    break;

  case 711: /* f_arg: f_arg ',' f_arg_item  */
#line 5706 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-2].node);
			(yyval.node)->nd_plen++;
			(yyval.node)->nd_next = block_append(p, (yyval.node)->nd_next, (yyvsp[0].node)->nd_next);
			rb_discard_node(p, (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 15293 "parse.c"
    break;

  case 712: /* f_label: "label"  */
#line 5719 "parse.y"
                    {
			arg_var(p, formal_argument(p, (yyvsp[0].id)));
			p->cur_arg = get_id((yyvsp[0].id));
			p->max_numparam = ORDINAL_PARAM;
			p->ctxt.in_argdef = 0;
			(yyval.id) = (yyvsp[0].id);
		    }
#line 15305 "parse.c"
    break;

  case 713: /* f_kw: f_label arg_value  */
#line 5729 "parse.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[-1].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($2)) %*/
		    }
#line 15318 "parse.c"
    break;

  case 714: /* f_kw: f_label  */
#line 5738 "parse.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[0].id), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), 0) %*/
		    }
#line 15331 "parse.c"
    break;

  case 715: /* f_block_kw: f_label primary_value  */
#line 5749 "parse.y"
                    {
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[-1].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($2)) %*/
		    }
#line 15343 "parse.c"
    break;

  case 716: /* f_block_kw: f_label  */
#line 5757 "parse.y"
                    {
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = new_kw_arg(p, assignable(p, (yyvsp[0].id), NODE_SPECIAL_REQUIRED_KEYWORD, &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), 0) %*/
		    }
#line 15355 "parse.c"
    break;

  case 717: /* f_block_kwarg: f_block_kw  */
#line 5767 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 15366 "parse.c"
    break;

  case 718: /* f_block_kwarg: f_block_kwarg ',' f_block_kw  */
#line 5774 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = kwd_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 15377 "parse.c"
    break;

  case 719: /* f_kwarg: f_kw  */
#line 5784 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 15388 "parse.c"
    break;

  case 720: /* f_kwarg: f_kwarg ',' f_kw  */
#line 5791 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = kwd_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 15399 "parse.c"
    break;

  case 723: /* f_no_kwarg: p_kwnorest  */
#line 5804 "parse.y"
                    {
		    /*%%%*/
		    /*% %*/
		    /*% ripper: nokw_param!(Qnil) %*/
		    }
#line 15409 "parse.c"
    break;

  case 724: /* f_kwrest: kwrest_mark "local variable or method"  */
#line 5812 "parse.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].id))));
		    /*%%%*/
			(yyval.id) = (yyvsp[0].id);
		    /*% %*/
		    /*% ripper: kwrest_param!($2) %*/
		    }
#line 15421 "parse.c"
    break;

  case 725: /* f_kwrest: kwrest_mark  */
#line 5820 "parse.y"
                    {
			arg_var(p, idFWD_KWREST);
		    /*%%%*/
			(yyval.id) = idFWD_KWREST;
		    /*% %*/
		    /*% ripper: kwrest_param!(Qnil) %*/
		    }
#line 15433 "parse.c"
    break;

  case 726: /* f_opt: f_arg_asgn f_eq arg_value  */
#line 5830 "parse.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($3)) %*/
		    }
#line 15446 "parse.c"
    break;

  case 727: /* f_block_opt: f_arg_asgn f_eq primary_value  */
#line 5841 "parse.y"
                    {
			p->cur_arg = 0;
			p->ctxt.in_argdef = 1;
		    /*%%%*/
			(yyval.node) = NEW_OPT_ARG(0, assignable(p, (yyvsp[-2].id), (yyvsp[0].node), &(yyloc)), &(yyloc));
		    /*% %*/
		    /*% ripper: rb_assoc_new(get_value(assignable(p, $1)), get_value($3)) %*/
		    }
#line 15459 "parse.c"
    break;

  case 728: /* f_block_optarg: f_block_opt  */
#line 5852 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 15470 "parse.c"
    break;

  case 729: /* f_block_optarg: f_block_optarg ',' f_block_opt  */
#line 5859 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = opt_arg_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 15481 "parse.c"
    break;

  case 730: /* f_optarg: f_opt  */
#line 5868 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[0].node);
		    /*% %*/
		    /*% ripper: rb_ary_new3(1, get_value($1)) %*/
		    }
#line 15492 "parse.c"
    break;

  case 731: /* f_optarg: f_optarg ',' f_opt  */
#line 5875 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = opt_arg_append((yyvsp[-2].node), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: rb_ary_push($1, get_value($3)) %*/
		    }
#line 15503 "parse.c"
    break;

  case 734: /* f_rest_arg: restarg_mark "local variable or method"  */
#line 5888 "parse.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].id))));
		    /*%%%*/
			(yyval.id) = (yyvsp[0].id);
		    /*% %*/
		    /*% ripper: rest_param!($2) %*/
		    }
#line 15515 "parse.c"
    break;

  case 735: /* f_rest_arg: restarg_mark  */
#line 5896 "parse.y"
                    {
			arg_var(p, idFWD_REST);
		    /*%%%*/
			(yyval.id) = idFWD_REST;
		    /*% %*/
		    /*% ripper: rest_param!(Qnil) %*/
		    }
#line 15527 "parse.c"
    break;

  case 738: /* f_block_arg: blkarg_mark "local variable or method"  */
#line 5910 "parse.y"
                    {
			arg_var(p, shadowing_lvar(p, get_id((yyvsp[0].id))));
		    /*%%%*/
			(yyval.id) = (yyvsp[0].id);
		    /*% %*/
		    /*% ripper: blockarg!($2) %*/
		    }
#line 15539 "parse.c"
    break;

  case 739: /* f_block_arg: blkarg_mark  */
#line 5918 "parse.y"
                    {
			arg_var(p, idFWD_BLOCK);
                    /*%%%*/
			(yyval.id) = idFWD_BLOCK;
                    /*% %*/
		    /*% ripper: blockarg!(Qnil) %*/
                    }
#line 15551 "parse.c"
    break;

  case 740: /* opt_f_block_arg: ',' f_block_arg  */
#line 5928 "parse.y"
                    {
			(yyval.id) = (yyvsp[0].id);
		    }
#line 15559 "parse.c"
    break;

  case 741: /* opt_f_block_arg: none  */
#line 5932 "parse.y"
                    {
			(yyval.id) = Qnull;
		    }
#line 15567 "parse.c"
    break;

  case 742: /* singleton: var_ref  */
#line 5938 "parse.y"
                    {
			value_expr((yyvsp[0].node));
			(yyval.node) = (yyvsp[0].node);
		    }
#line 15576 "parse.c"
    break;

  case 743: /* $@51: %empty  */
#line 5942 "parse.y"
                      {SET_LEX_STATE(EXPR_BEG);}
#line 15582 "parse.c"
    break;

  case 744: /* singleton: '(' $@51 expr rparen  */
#line 5943 "parse.y"
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
#line 15608 "parse.c"
    break;

  case 746: /* assoc_list: assocs trailer  */
#line 5968 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = (yyvsp[-1].node);
		    /*% %*/
		    /*% ripper: assoclist_from_args!($1) %*/
		    }
#line 15619 "parse.c"
    break;

  case 748: /* assocs: assocs ',' assoc  */
#line 5979 "parse.y"
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
#line 15644 "parse.c"
    break;

  case 749: /* assoc: arg_value "=>" arg_value  */
#line 6002 "parse.y"
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
#line 15659 "parse.c"
    break;

  case 750: /* assoc: "label" arg_value  */
#line 6013 "parse.y"
                    {
		    /*%%%*/
			(yyval.node) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[-1].id)), &(yylsp[-1])), &(yyloc)), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: assoc_new!($1, $2) %*/
		    }
#line 15670 "parse.c"
    break;

  case 751: /* assoc: "label"  */
#line 6020 "parse.y"
                    {
		    /*%%%*/
			NODE *val = gettable(p, (yyvsp[0].id), &(yyloc));
			if (!val) val = NEW_BEGIN(0, &(yyloc));
			(yyval.node) = list_append(p, NEW_LIST(NEW_LIT(ID2SYM((yyvsp[0].id)), &(yylsp[0])), &(yyloc)), val);
		    /*% %*/
		    /*% ripper: assoc_new!($1, Qnil) %*/
		    }
#line 15683 "parse.c"
    break;

  case 752: /* assoc: "string literal" string_contents tLABEL_END arg_value  */
#line 6029 "parse.y"
                    {
		    /*%%%*/
			YYLTYPE loc = code_loc_gen(&(yylsp[-3]), &(yylsp[-1]));
			(yyval.node) = list_append(p, NEW_LIST(dsym_node(p, (yyvsp[-2].node), &loc), &loc), (yyvsp[0].node));
		    /*% %*/
		    /*% ripper: assoc_new!(dyna_symbol!($2), $4) %*/
		    }
#line 15695 "parse.c"
    break;

  case 753: /* assoc: "**arg" arg_value  */
#line 6037 "parse.y"
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
#line 15716 "parse.c"
    break;

  case 754: /* assoc: "**arg"  */
#line 6054 "parse.y"
                    {
                        if (!local_id(p, idFWD_KWREST) ||
                            local_id(p, idFWD_ALL)) {
                            compile_error(p, "no anonymous keyword rest parameter");
                        }
		    /*%%%*/
                        (yyval.node) = list_append(p, NEW_LIST(0, &(yyloc)),
                                         NEW_LVAR(idFWD_KWREST, &(yyloc)));
		    /*% %*/
		    /*% ripper: assoc_splat!(Qnil) %*/
		    }
#line 15732 "parse.c"
    break;

  case 778: /* term: ';'  */
#line 6114 "parse.y"
                      {yyerrok;token_flush(p);}
#line 15738 "parse.c"
    break;

  case 779: /* term: '\n'  */
#line 6116 "parse.y"
                    {
			(yyloc).end_pos = (yyloc).beg_pos;
			token_flush(p);
		    }
#line 15747 "parse.c"
    break;

  case 781: /* terms: terms ';'  */
#line 6123 "parse.y"
                            {yyerrok;}
#line 15753 "parse.c"
    break;

  case 782: /* none: %empty  */
#line 6127 "parse.y"
                    {
			(yyval.node) = Qnull;
		    }
#line 15761 "parse.c"
    break;


#line 15765 "parse.c"

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

#line 6131 "parse.y"

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
#define has_delayed_token(p) (!NIL_P(p->delayed.token))

#ifndef RIPPER
#define literal_flush(p, ptr) ((p)->lex.ptok = (ptr))
#define dispatch_scan_event(p, t) parser_dispatch_scan_event(p, t, __LINE__)

static bool
parser_has_token(struct parser_params *p)
{
    if (p->keep_tokens && (p->lex.pcur < p->lex.ptok)) rb_bug("lex.pcur < lex.ptok. (line: %d) %ld|%ld|%ld", p->ruby_sourceline, p->lex.ptok - p->lex.pbeg, p->lex.pcur - p->lex.ptok, p->lex.pend - p->lex.pcur);
    return p->lex.pcur > p->lex.ptok;
}

static VALUE
code_loc_to_ary(const rb_code_location_t *loc)
{
    VALUE ary = rb_ary_new_from_args(4,
	INT2NUM(loc->beg_pos.lineno), INT2NUM(loc->beg_pos.column),
	INT2NUM(loc->end_pos.lineno), INT2NUM(loc->end_pos.column));
    rb_obj_freeze(ary);

    return ary;
}

static void
parser_append_tokens(struct parser_params *p, VALUE str, enum yytokentype t, int line)
{
    VALUE ary;
    int token_id;

    ary = rb_ary_new2(4);
    token_id = p->token_id;
    rb_ary_push(ary, INT2FIX(token_id));
    rb_ary_push(ary, ID2SYM(parser_token2id(t)));
    rb_ary_push(ary, str);
    rb_ary_push(ary, code_loc_to_ary(p->yylloc));
    rb_obj_freeze(ary);
    rb_ary_push(p->tokens, ary);
    p->token_id++;

    if (p->debug) {
	rb_parser_printf(p, "Append tokens (line: %d) %"PRIsVALUE"\n", line, ary);
    }
}

static void
parser_dispatch_scan_event(struct parser_params *p, enum yytokentype t, int line)
{
    debug_token_line(p, "parser_dispatch_scan_event", line);

    if (!parser_has_token(p)) return;

    RUBY_SET_YYLLOC(*p->yylloc);

    if (p->keep_tokens) {
	VALUE str = STR_NEW(p->lex.ptok, p->lex.pcur - p->lex.ptok);
	parser_append_tokens(p, str, t, line);
    }

    token_flush(p);
}

#define dispatch_delayed_token(p, t) parser_dispatch_delayed_token(p, t, __LINE__)
static void
parser_dispatch_delayed_token(struct parser_params *p, enum yytokentype t, int line)
{
    int saved_line = p->ruby_sourceline;
    const char *saved_tokp = p->lex.ptok;

    debug_token_line(p, "parser_dispatch_delayed_token", line);

    if (!has_delayed_token(p)) return;

    RUBY_SET_YYLLOC_OF_DELAYED_TOKEN(*p->yylloc);

    if (p->keep_tokens) {
	p->ruby_sourceline = p->delayed.beg_line;
	p->lex.ptok = p->lex.pbeg + p->delayed.beg_col;
	parser_append_tokens(p, p->delayed.token, t, line);
	p->ruby_sourceline = saved_line;
	p->lex.ptok = saved_tokp;
    }

    p->delayed.token = Qnil;
}
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
    RUBY_SET_YYLLOC(*p->yylloc);
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

    if (!has_delayed_token(p)) return;
    p->ruby_sourceline = p->delayed.beg_line;
    p->lex.ptok = p->lex.pbeg + p->delayed.beg_col;
    add_mark_object(p, yylval_rval = ripper_dispatch1(p, ripper_token2eventid(t), p->delayed.token));
    p->delayed.token = Qnil;
    p->ruby_sourceline = saved_line;
    p->lex.ptok = saved_tokp;
}
#define dispatch_delayed_token(p, t) ripper_dispatch_delayed_token(p, t)
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
    if (n || p->error_p) {
	VALUE mesg = p->error_buffer;
	if (!mesg) {
	    mesg = rb_class_new_instance(0, 0, rb_eSyntaxError);
	}
	if (!p->error_tolerant) {
	    rb_set_errinfo(mesg);
	    return FALSE;
	}
    }
    tree = p->eval_tree;
    if (!tree) {
	tree = NEW_NIL(&NULL_LOC);
    }
    else {
	VALUE opt = p->compile_option;
	VALUE tokens = p->tokens;
	NODE *prelude;
	NODE *body = parser_append_options(p, tree->nd_body);
	if (!opt) opt = rb_obj_hide(rb_ident_hash_new());
	rb_hash_aset(opt, rb_sym_intern_ascii_cstr("coverage_enabled"), cov);
	prelude = block_append(p, p->eval_tree_begin, body);
	tree->nd_body = prelude;
        RB_OBJ_WRITE(p->ast, &p->ast->body.compile_option, opt);
	if (p->keep_tokens) {
	    rb_obj_freeze(tokens);
	    rb_ast_set_tokens(p->ast, tokens);
	}
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
	if (is_ascii_string(str)) {
	}
	else if (rb_is_usascii_enc(enc0) && enc != rb_utf8_encoding()) {
	    rb_enc_associate(str, rb_ascii8bit_encoding());
	}
    }

    return str;
}

#define peek(p,c) peek_n(p, (c), 0)
#define peek_n(p,c,n) (!lex_eol_n_p(p, n) && (c) == (unsigned char)(p)->lex.pcur[n])
#define peekc(p) peekc_n(p, 0)
#define peekc_n(p,n) (lex_eol_n_p(p, n) ? -1 : (unsigned char)(p)->lex.pcur[n])

static void
add_delayed_token(struct parser_params *p, const char *tok, const char *end, int line)
{
#ifndef RIPPER
    debug_token_line(p, "add_delayed_token", line);
#endif

    if (tok < end) {
	if (!has_delayed_token(p)) {
	    p->delayed.token = rb_str_buf_new(end - tok);
	    rb_enc_associate(p->delayed.token, p->enc);
	    p->delayed.beg_line = p->ruby_sourceline;
	    p->delayed.beg_col = rb_long2int(tok - p->lex.pbeg);
	}
	rb_str_buf_cat(p->delayed.token, tok, end - tok);
	p->delayed.end_line = p->ruby_sourceline;
	p->delayed.end_col = rb_long2int(end - p->lex.pbeg);
	p->lex.ptok = end;
    }
}

static int
nextline(struct parser_params *p, int set_encoding)
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
#ifndef RIPPER
	if (p->debug_lines) {
	    if (set_encoding) rb_enc_associate(v, p->enc);
	    rb_ary_push(p->debug_lines, v);
	}
#endif
	p->cr_seen = FALSE;
    }
    else if (NIL_P(v)) {
	/* after here-document without terminator */
	goto end_of_input;
    }
    add_delayed_token(p, p->lex.ptok, p->lex.pend, __LINE__);
    if (p->heredoc_end > 0) {
	p->ruby_sourceline = p->heredoc_end;
	p->heredoc_end = 0;
    }
    p->ruby_sourceline++;
    p->lex.pbeg = p->lex.pcur = RSTRING_PTR(v);
    p->lex.pend = p->lex.pcur + RSTRING_LEN(v);
    token_flush(p);
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
nextc0(struct parser_params *p, int set_encoding)
{
    int c;

    if (UNLIKELY((p->lex.pcur == p->lex.pend) || p->eofp || RTEST(p->lex.nextline))) {
	if (nextline(p, set_encoding)) return -1;
    }
    c = (unsigned char)*p->lex.pcur++;
    if (UNLIKELY(c == '\r')) {
	c = parser_cr(p, c);
    }

    return c;
}
#define nextc(p) nextc0(p, TRUE)

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
    p->lex.pcur += numlen;
    if (p->lex.strterm == NULL ||
        (p->lex.strterm->flags & STRTERM_HEREDOC) ||
        (p->lex.strterm->u.literal.u1.func != str_regexp)) {
        if (wide ? (numlen == 0 || numlen > 6) : (numlen < 4))  {
            literal_flush(p, p->lex.pcur);
            yyerror0("invalid Unicode escape");
            return wide && numlen > 0;
        }
        if (codepoint > 0x10ffff) {
            literal_flush(p, p->lex.pcur);
            yyerror0("invalid Unicode codepoint (too large)");
            return wide;
        }
        if ((codepoint & 0xfffff800) == 0xd800) {
            literal_flush(p, p->lex.pcur);
            yyerror0("invalid Unicode codepoint");
            return wide;
        }
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

static int tokadd_mbchar(struct parser_params *p, int c);

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
        if (regexp_literal && p->lex.strterm->u.literal.u1.func == str_regexp) {
            /*
             * Skip parsing validation code and copy bytes as-is until term or
             * closing brace, in order to correctly handle extended regexps where
             * invalid unicode escapes are allowed in comments. The regexp parser
             * does its own validation and will catch any issues.
             */
            tokadd(p, open_brace);
            while (++p->lex.pcur < p->lex.pend) {
                int c = peekc(p);
                if (c == close_brace) {
                    tokadd(p, c);
                    ++p->lex.pcur;
                    break;
                }
                else if (c == term) {
                    break;
                }
                if (c == '\\' && p->lex.pcur + 1 < p->lex.pend) {
                    tokadd(p, c);
                    c = *++p->lex.pcur;
                }
                tokadd_mbchar(p, c);
            }
        }
        else {
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
#ifdef RIPPER
    const int heredoc_end = (p->heredoc_end ? p->heredoc_end + 1 : 0);
    int top_of_line = FALSE;
#endif

#define mixed_error(enc1, enc2) \
    (void)(erred || (parser_mixed_error(p, enc1, enc2), erred = true))
#define mixed_escape(beg, enc1, enc2) \
    (void)(erred || (parser_mixed_escape(p, beg, enc1, enc2), erred = true))

    while ((c = nextc(p)) != -1) {
	if (p->heredoc_indent > 0) {
	    parser_update_heredoc_indent(p, c);
	}
#ifdef RIPPER
        if (top_of_line && heredoc_end == p->ruby_sourceline) {
            pushback(p, c);
            break;
        }
#endif

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
	    unsigned char c2 = *p->lex.pcur;
	    if (c2 == '$' || c2 == '@' || c2 == '{') {
		pushback(p, c);
		break;
	    }
	}
	else if (c == '\\') {
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
#ifdef RIPPER
	top_of_line = (c == '\n');
#endif
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
static void
flush_string_content(struct parser_params *p, rb_encoding *enc)
{
    if (has_delayed_token(p)) {
	ptrdiff_t len = p->lex.pcur - p->lex.ptok;
	if (len > 0) {
	    rb_enc_str_buf_cat(p->delayed.token, p->lex.ptok, len, enc);
	    p->delayed.end_line = p->ruby_sourceline;
	    p->delayed.end_col = rb_long2int(p->lex.pcur - p->lex.pbeg);
	}
	dispatch_delayed_token(p, tSTRING_CONTENT);
	p->lex.ptok = p->lex.pcur;
    }
    dispatch_scan_event(p, tSTRING_CONTENT);
}
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
	SET_LEX_STATE(EXPR_ARG|EXPR_LABELED);
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
	    add_delayed_token(p, p->lex.ptok, p->lex.pcur, __LINE__);
	    return ' ';
	}
	return parser_string_term(p, func);
    }
    if (space) {
	pushback(p, c);
	add_delayed_token(p, p->lex.ptok, p->lex.pcur, __LINE__);
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
    RUBY_SET_YYLLOC_FROM_STRTERM_HEREDOC(*p->yylloc);
    lex_goto_eol(p);
    token_flush(p);
}

#else
#define dispatch_heredoc_end(p) parser_dispatch_heredoc_end(p, __LINE__)
static void
parser_dispatch_heredoc_end(struct parser_params *p, int line)
{
    if (has_delayed_token(p))
	dispatch_delayed_token(p, tSTRING_CONTENT);

    if (p->keep_tokens) {
	VALUE str = STR_NEW(p->lex.ptok, p->lex.pend - p->lex.ptok);
	RUBY_SET_YYLLOC_OF_HEREDOC_END(*p->yylloc);
	parser_append_tokens(p, str, tHEREDOC_END, line);
    }

    RUBY_SET_YYLLOC_FROM_STRTERM_HEREDOC(*p->yylloc);
    lex_goto_eol(p);
    token_flush(p);
}
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
			rb_is_usascii_enc(p->enc) &&
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
    int c = nextc0(p, FALSE);
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
#ifndef RIPPER
	    if (p->debug_lines) {
		rb_enc_associate(p->lex.lastline, p->enc);
	    }
#endif
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
        else if (!ISASCII(c = peekc(p))) {
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
    int enforce_keyword_end = 0;

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

#ifndef RIPPER
    if (!NIL_P(peek_end_expect_token_locations(p))) {
	VALUE end_loc;
	int lineno, column;
	int beg_pos = (int)(p->lex.ptok - p->lex.pbeg);

	end_loc = peek_end_expect_token_locations(p);
	lineno = NUM2INT(rb_ary_entry(end_loc, 0));
	column = NUM2INT(rb_ary_entry(end_loc, 1));

	if (p->debug) {
	    rb_parser_printf(p, "enforce_keyword_end check. current: (%d, %d), peek: (%d, %d)\n",
				p->ruby_sourceline, beg_pos, lineno, column);
	}

	if ((p->ruby_sourceline > lineno) && (beg_pos <= column)) {
	    const struct kwtable *kw;

	    if ((IS_lex_state(EXPR_DOT)) && (kw = rb_reserved_word(tok(p), toklen(p))) && (kw && kw->id[0] == keyword_end)) {
		if (p->debug) rb_parser_printf(p, "enforce_keyword_end is enabled\n");
		enforce_keyword_end = 1;
	    }
	}
    }
#endif

    if (mb == ENC_CODERANGE_7BIT && (!IS_lex_state(EXPR_DOT) || enforce_keyword_end)) {
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
	    if (IS_lex_state_for(state, (EXPR_BEG | EXPR_LABELED | EXPR_CLASS)))
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
        (lvar_defined(p, ident) || NUMPARAM_ID_P(ident))) {
        SET_LEX_STATE(EXPR_END|EXPR_LABEL);
    }
    return result;
}

static void
warn_cr(struct parser_params *p)
{
    if (!p->cr_seen) {
	p->cr_seen = TRUE;
	/* carried over with p->lex.nextline for nextc() */
	rb_warn0("encountered \\r in middle of line, treated as a mere space");
    }
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
	    token_flush(p);
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
#ifndef RIPPER
    token_flush(p);
#endif
  retry:
    last_state = p->lex.state;
    switch (c = nextc(p)) {
      case '\0':		/* NUL */
      case '\004':		/* ^D */
      case '\032':		/* ^Z */
      case -1:			/* end of script. */
	p->eofp  = 1;
#ifndef RIPPER
	if (!NIL_P(p->end_expect_token_locations) && RARRAY_LEN(p->end_expect_token_locations) > 0) {
	    pop_end_expect_token_locations(p);
	    RUBY_SET_YYLLOC_OF_DUMMY_END(*p->yylloc);
	    return tDUMNY_END;
	}
#endif
	/* Set location for end-of-input because dispatch_scan_event is not called. */
	RUBY_SET_YYLLOC(*p->yylloc);
	return 0;

	/* white spaces */
      case '\r':
	warn_cr(p);
	/* fall through */
      case ' ': case '\t': case '\f':
      case '\13': /* '\v' */
	space_seen = 1;
	while ((c = nextc(p))) {
	    switch (c) {
	      case '\r':
		warn_cr(p);
		/* fall through */
	      case ' ': case '\t': case '\f':
	      case '\13': /* '\v' */
		break;
	      default:
		goto outofloop;
	    }
	}
      outofloop:
	pushback(p, c);
	dispatch_scan_event(p, tSP);
#ifndef RIPPER
	token_flush(p);
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
		if (space_seen) {
		    dispatch_scan_event(p, tSP);
		    token_flush(p);
		}
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
		lex_goto_eol(p);
		if (c != -1) {
		    p->lex.ptok = p->lex.pcur;
		}
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
    p->yylloc = yylloc;

    t = parser_yylex(p);

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
	opt_arg_append(kwlist, kw);
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
    if (UNDEF_P(lit)) return;
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
rb_parser_set_location_of_delayed_token(struct parser_params *p, YYLTYPE *yylloc)
{
    yylloc->beg_pos.lineno = p->delayed.beg_line;
    yylloc->beg_pos.column = p->delayed.beg_col;
    yylloc->end_pos.lineno = p->delayed.end_line;
    yylloc->end_pos.column = p->delayed.end_col;

    return yylloc;
}

YYLTYPE *
rb_parser_set_location_of_heredoc_end(struct parser_params *p, YYLTYPE *yylloc)
{
    int sourceline = p->ruby_sourceline;
    int beg_pos = (int)(p->lex.ptok - p->lex.pbeg);
    int end_pos = (int)(p->lex.pend - p->lex.pbeg);
    return rb_parser_set_pos(yylloc, sourceline, beg_pos, end_pos);
}

YYLTYPE *
rb_parser_set_location_of_dummy_end(struct parser_params *p, YYLTYPE *yylloc)
{
    yylloc->end_pos = yylloc->beg_pos;

    return yylloc;
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
    if (dyna_in_block(p)) {
	if (dvar_curr(p, name)) {
	    if (is_private_local_id(name)) return 1;
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
	    if (is_private_local_id(name)) return 1;
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
		if (!UNDEF_P(e)) {
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
		if (!UNDEF_P(k) && !UNDEF_P(v)) {
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
    if (nd_type_p(node, NODE_BLOCK_PASS)) {
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

    if (args->forwarding) {
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

#ifdef FORWARD_ARGS_WITH_RUBY2_KEYWORDS
    args->ruby2_keywords = args->forwarding;
#else
    args->ruby2_keywords = 0;
#endif

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
vtable_chain_free(struct parser_params *p, struct vtable *table)
{
    while (!DVARS_TERMINAL_P(table)) {
        struct vtable *cur_table = table;
        table = cur_table->prev;
        vtable_free(cur_table);
    }
}

static void
local_free(struct parser_params *p, struct local_vars *local)
{
    vtable_chain_free(p, local->used);

# if WARN_PAST_SCOPE
    vtable_chain_free(p, local->past);
# endif

    vtable_chain_free(p, local->args);
    vtable_chain_free(p, local->vars);

    ruby_sized_xfree(local, sizeof(struct local_vars));
}

static void
local_pop(struct parser_params *p)
{
    struct local_vars *local = p->lvtbl->prev;
    if (p->lvtbl->used) {
        warn_unused_var(p, p->lvtbl);
    }

    local_free(p, p->lvtbl);
    p->lvtbl = local;

    CMDARG_POP();
    COND_POP();
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
    if (local_id(p, idFWD_ALL)) return TRUE;
    compile_error(p, "unexpected ...");
    return FALSE;
}

static void
add_forwarding_args(struct parser_params *p)
{
    arg_var(p, idFWD_REST);
#ifndef FORWARD_ARGS_WITH_RUBY2_KEYWORDS
    arg_var(p, idFWD_KWREST);
#endif
    arg_var(p, idFWD_BLOCK);
    arg_var(p, idFWD_ALL);
}

#ifndef RIPPER
static NODE *
new_args_forward_call(struct parser_params *p, NODE *leading, const YYLTYPE *loc, const YYLTYPE *argsloc)
{
    NODE *rest = NEW_LVAR(idFWD_REST, loc);
#ifndef FORWARD_ARGS_WITH_RUBY2_KEYWORDS
    NODE *kwrest = list_append(p, NEW_LIST(0, loc), NEW_LVAR(idFWD_KWREST, loc));
#endif
    NODE *block = NEW_BLOCK_PASS(NEW_LVAR(idFWD_BLOCK, loc), loc);
    NODE *args = leading ? rest_arg_append(p, leading, rest, argsloc) : NEW_SPLAT(rest, loc);
#ifndef FORWARD_ARGS_WITH_RUBY2_KEYWORDS
    args = arg_append(p, args, new_hash(p, kwrest, loc), loc);
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
	    !is_ascii_string(str)) {
            goto error;
	}
	ENCODING_SET(str, idx);
    }
    else if (RE_OPTION_ENCODING_NONE(options)) {
        if (!ENCODING_IS_ASCII8BIT(str) &&
            !is_ascii_string(str)) {
            c = 'n';
            goto error;
        }
	rb_enc_associate(str, rb_ascii8bit_encoding());
    }
    else if (rb_is_usascii_enc(p->enc)) {
	if (!is_ascii_string(str)) {
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
	NODE *irs = NEW_LIST(NEW_GVAR(rb_intern("$/"), LOC), LOC);

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
	    NODE *chomp = NEW_LIT(ID2SYM(rb_intern("chomp")), LOC);
	    chomp = list_append(p, NEW_LIST(chomp, LOC), NEW_TRUE(LOC));
	    irs = list_append(p, irs, NEW_HASH(chomp, LOC));
	}

	node = NEW_WHILE(NEW_FCALL(idGets, irs, LOC), node, 1, LOC);
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
    p->delayed.token = Qnil;
#ifdef RIPPER
    p->result = Qnil;
    p->parsing_thread = Qnil;
#else
    p->error_buffer = Qfalse;
    p->end_expect_token_locations = Qnil;
    p->token_id = 0;
    p->tokens = Qnil;
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
    rb_gc_mark(p->lex.lastline);
    rb_gc_mark(p->lex.nextline);
    rb_gc_mark(p->ruby_sourcefile_string);
    rb_gc_mark((VALUE)p->lex.strterm);
    rb_gc_mark((VALUE)p->ast);
    rb_gc_mark(p->case_labels);
    rb_gc_mark(p->delayed.token);
#ifndef RIPPER
    rb_gc_mark(p->debug_lines);
    rb_gc_mark(p->compile_option);
    rb_gc_mark(p->error_buffer);
    rb_gc_mark(p->end_expect_token_locations);
    rb_gc_mark(p->tokens);
#else
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
        prev = local->prev;
        local_free(p, local);
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

void
rb_parser_error_tolerant(VALUE vparser)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    p->error_tolerant = 1;
    p->end_expect_token_locations = rb_ary_new();
}

void
rb_parser_keep_tokens(VALUE vparser)
{
    struct parser_params *p;

    TypedData_Get_Struct(vparser, struct parser_params, &parser_data_type, p);
    p->keep_tokens = 1;
    p->tokens = rb_ary_new();
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
    if (NIL_P(x)) return x;
    if (UNDEF_P(x))
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
    if (UNDEF_P(v)) return Qnil;
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
    if (UNDEF_P(obj)) {
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
