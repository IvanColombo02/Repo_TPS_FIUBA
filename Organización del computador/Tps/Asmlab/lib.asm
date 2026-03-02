global strClone
global strPrint
global strCmp
global strLen
global strDelete

global arrayNew
global arrayDelete
global arrayPrint
global arrayGetSize
global arrayAddLast
global arrayGet
global arrayRemove
global arraySwap

global cardCmp
global cardClone
global cardAddStacked
global cardDelete
global cardGetSuit
global cardGetNumber
global cardGetStacked
global cardPrint
global cardNew

;TIPOS
TYPE_CARD EQU 3
TYPE_INT EQU 1
;ARRAY 
ARRAY_OFF_TYPE EQU 0
ARRAY_OFF_SIZE EQU 4
ARRAY_OFF_CAPACITY EQU 5
ARRAY_OFF_DATA EQU 8

ARRAY_SIZE_MEM EQU 16

;CARD 
CARD_OFF_SUIT EQU 0
CARD_OFF_NUMBER EQU 8
CARD_OFF_STACKED EQU 16
CARD_SIZE_MEM EQU 24
section .data
    ch_abre db "[" , 0
    ch_cierra db "]" , 0
    format db "%s", 0
    nullStr db "NULL", 0
    coma db ",", 0
    listaOp db "{%s-%d-", 0
    listaCl db "}", 0
    intFormat db "%d", 0

section .text
    extern listAddFirst
    extern getPrintFunction
    extern getCloneFunction
    extern getDeleteFunction
    extern listNew
    extern malloc
    extern fprintf
    extern free
    extern intDelete
    extern listDelete
    extern intCmp
    extern intClone
    extern listClone
    extern listPrint
    
; ** String **
;char* strClone(char* a);
strClone:
    push    rbp
    mov     rbp, rsp
    push    r12 
    push    r13
    push    r14
    push    rbx

    mov     r13, rdi
    mov     rdi, r13
    call    strLen
    mov     r12, rax 
    inc     r12 
 
    mov     rdi, r12
    call    malloc
    mov     rbx, rax 
    mov     r14, rbx

    while:
        mov     al, [r13] 
        mov     [rbx], al 
        inc     r13 
        inc     rbx 
        cmp     al, 0
        jne     while 
    
    mov     rax, r14
    pop     rbx
    pop     r14
    pop     r13
    pop     r12
    pop     rbp
ret

;void strPrint(char* a, FILE* pFile)
strPrint:
    push    rbp
    mov     rbp, rsp 
    push    r12
    push    r13
    mov     r12, rdi ; r12 es a
    mov     r13, rsi ; r13 es el pFile
    cmp     byte [r12] , 0
    je      .vacio

    mov     rdi , r13 
    mov     rsi , format 
    mov     rdx , r12  
    jmp     .fin

    .vacio:
        mov     rdi, r13
        mov     rsi, format
        mov     rdx , nullStr

    .fin:
        call fprintf
        pop     r13
        pop     r12
        pop     rbp
ret
;uint32_t strLen(char* a);
strLen: 
    push    rbp
    mov     rbp, rsp 
    push    r12
    sub     rsp , 8
    mov     r12, 0
    .while:
        mov     dl, [rdi + r12]    
        inc     r12               
        cmp     dl, 0            
        jne .while           
    dec     r12                  
    .fin: 
    mov     rax, r12
    add     rsp , 8
    pop     r12
    pop     rbp
ret  

; int32_t strCmp(char* a, char* b);
strCmp:
    push    rbp
    mov     rbp, rsp
    push    r12
    push    r13
    push    r14
    push    r15

    mov     r12, rdi        
    mov     r14, rsi   

    call    strLen
    mov     r13, rax       
    mov     rdi, r14
    call    strLen
    mov     r15, rax      

    cmp     r13, r15
    jg      .b_mas_grande
    jl      .a_mas_grande

    mov     rdi, r12        
    mov     rsi, r14        
    xor     r12, r12       

    .while:
        mov     al, [rdi + r12] 
        mov     bl, [rsi + r12]
        cmp     al, bl
        jne     .a_mas_grande
        cmp     al, 0
        je      .son_iguales
        inc     r12
        jmp     .while

    .son_iguales:
        xor     eax, eax
        jmp     .fin

    .a_mas_grande:
        mov     eax, 1
        jmp     .fin
    .b_mas_grande:
        mov     eax, -1
        jmp     .fin

    .fin:
        pop     r15
        pop     r14
        pop     r13
        pop     r12
        pop     rbp
ret

;void strDelete(char* a);
strDelete:
    push    rbp
    mov     rbp, rsp 
    call    free 
    pop     rbp
ret

; ** Array **

; uint8_t arrayGetSize(array_t* a)
arrayGetSize:
    push    rbp
    mov     rbp, rsp
    push    r12
    sub     rsp, 8
    mov     r12, rdi ; r12 es el array
    mov     rax, [r12 + ARRAY_OFF_SIZE]
    add     rsp, 8
    pop     r12
    pop     rbp
ret

; void arrayAddLast(array_t* a, void* data)
arrayAddLast:
    push    rbp
    mov     rbp, rsp
    push    r12
    push    r13
    push    rbx 
    push    r15
    push    r14
    sub     rsp , 8

    mov     r12 , rdi ; r12 es el array
    mov     r13 , rsi ; r13 es data

    mov     bl, byte [r12 + ARRAY_OFF_SIZE] ; bl es size
    mov     cl, byte [r12 + ARRAY_OFF_CAPACITY] ; cl es capacidad
    cmp     bl, cl
    je      .fin

    mov     rdi , [r12+ARRAY_OFF_TYPE]
    call    getCloneFunction ;consigo mi funcion clonar con el tipo del array
    mov     rdi , r13
    call    rax

    mov     r14, rax ; r14 es el nuevo dato

    xor     rbx, rbx ;pongo en 0 a rbx
    mov     bl, [r12 + ARRAY_OFF_SIZE] ; rbx es size
    mov     r15 , [r12 + ARRAY_OFF_DATA] ; r15 sera data del array
    mov     [r15 + rbx*8], r14
    inc     byte [r12 + ARRAY_OFF_SIZE] 

    .fin:

        add     rsp, 8
        pop     r14
        pop     r15
        pop     rbx
        pop     r13
        pop     r12
        pop     rbp
ret

; void* arrayGet(array_t* a, uint8_t i)
arrayGet:
    push    rbp 
    mov     rbp, rsp 
    push    r12 
    push    r13
    push    rbx
    push    r14

    mov     r14, rsi ; r14 es i
    mov     r12 , rdi ; r12 es el array
    xor     rbx, rbx 
    mov     bl , [rdi+ARRAY_OFF_SIZE] ;rbx es size
    cmp     r14 ,  rbx
    jg .invalida

    mov     r13 , [r12+ARRAY_OFF_DATA] ; r13 es void** data 
    mov     rax, [r13 + r14*8] 
    jmp .fin

    .invalida:
        mov     rax , 0 
    .fin:
        pop     r14
        pop     rbx
        pop     r13
        pop     r12
        pop     rbp
ret

; array_t* arrayNew(type_t t, uint8_t capacity)
arrayNew:
    push    rbp
    mov     rbp, rsp
    push    r12 
    push    rbx 
    push    r13
    sub     rsp, 8 

    mov     rbx , rdi ; rbx es type_t 
    mov     r12, rsi ; r12 es capacidad 
    mov     rdi , 16
    call    malloc ; rax es la direccion de malloc
    mov     r13, rax ; r13 es el array

    mov     [r13 + ARRAY_OFF_TYPE] , rbx
    mov     byte[r13 + ARRAY_OFF_SIZE] , 0
    mov     byte[r13 + ARRAY_OFF_CAPACITY] , r12b
    ;Ocupemosnos de crear espacio para void** data => capacidad * 8 al void** data al ser un puntero
    
    imul    r12, 8
    mov     rdi, rax
    call    malloc
    mov     [r13 + ARRAY_OFF_DATA], rax
    mov     rax,r13 ; rax es el array
    
    add     rsp, 8
    pop     r13
    pop     rbx
    pop     r12
    pop     rbp
ret

; void* arrayRemove(array_t* a, uint8_t i)arrayRemove:
arrayRemove:
    push    rbp
    mov     rbp, rsp
    push    r12
    push    r13
    push    rbx
    push    r14
    push    r15
    sub     rsp, 8

    mov     r12, rdi ; r12 es array
    mov     r13 , rsi ; r13 es i
    cmp     r13b ,  [r12+ARRAY_OFF_SIZE]
    jge .fuera_rango
    
    mov     r14 , [r12 + ARRAY_OFF_DATA] ; r14 es void**data
    mov     rbx , [r14 + r13*8] ; rbx es el data a eliminar

    ;clono el data
    mov     rdi , [r12 +ARRAY_OFF_TYPE]
    call    getCloneFunction
    mov     rdi , rbx
    call    rax
    ;rax es el puntero clonado
    mov     r15, rax

    mov     rdi , [r12 +ARRAY_OFF_TYPE]
    call    getDeleteFunction
    ;rax es la funcion
    mov     rdi , rbx
    call    rax 
    ;rax en teoria no es nada

    inc     r13 ;sera mi contador [6, 3, Nada, 8] => elijo 8
    cmp     r13b , [r12 + ARRAY_OFF_SIZE]
    jg .fin ;caso de que me vaya de rango

    .mover_elementos:
        mov     rbx , [r14 + r13*8] ;el prox elem a mover 
        mov     [r14 + (r13-1)*8] , rbx ;lo pongo en su posicion anterior [6, 3, Nada, 8] <=> [6, 3, 8, nada]
        inc     r13 ; busco el proximo
        cmp     r13b , [r12 + ARRAY_OFF_SIZE] 
        jg .fin_mover
        jmp .mover_elementos

    .fin_mover
        dec     byte[r12 + ARRAY_OFF_SIZE]
        mov     rax , r15
        jmp .fin ; voy a fin
        
    .fuera_rango:
        mov     rax, 0
        
    .fin:
        add     rsp,8
        pop     r15
        pop     r14
        pop     rbx
        pop     r13
        pop     r12
        pop     rbp
ret

; void arraySwap(array_t* a, uint8_t i, uint8_t j)
arraySwap:
    push    rbp
    mov     rbp, rsp 
    push    r12
    push    r13
    push    r14
    push    r15
    push    rbx
    sub     rsp , 8

    mov     r12, rdi ; r12 es array
    mov     r13, rsi ; r13 es i
    mov     r14, rdx ; r14 es j
    
    cmp     r13b , [r12 + ARRAY_OFF_SIZE]
    jge .fuera_rango
    cmp     r14b , [r12 + ARRAY_OFF_SIZE]
    jge .fuera_rango
    mov     rsi, r13
    mov     rdx, r14

    ; [x, 6 , x , 8 ] => [x, 6 , x , 8 ]
    mov     r15 , [r12 + ARRAY_OFF_DATA] ; r15 es void** data
    mov     rbx , [r15+rsi*8] ; rbx es el auxiliar de 6
    mov     r13 , [r15+rdx*8] ; r13 es el 8
    mov     [r15+rsi*8], r13  ; muevo donde estaba el 6, al 8
    mov     [r15+rdx*8] , rbx
    
    .fuera_rango:
        mov     rax , 0
    .fin :
        add     rsp, 8
        pop     rbx
        pop     r15
        pop     r14
        pop     r13
        pop     r12
        pop     rbp
ret

; void arrayDelete(array_t* a) {
arrayDelete:
    push    rbp
    mov     rbp, rsp
    push    r13
    push    r15

    mov     r13, rdi ; r13 es mi array
    mov     r15, [r13 + ARRAY_OFF_DATA] ;r15 es void** data
    mov     rdi , r15  ; Carga el puntero a los datos
    call    free         ; Libera la memoria de los datos
    mov     rdi , r13
    call    free ; elimino array
    
    pop     r15
    pop     r13
    pop     rbp
ret

;void arrayPrint(array_t* a, FILE* pFile)
arrayPrint:
    push    rbp
    mov     rbp, rsp 
    push    r12
    push    r13
    push    r14
    push    r15
    push    rbx
    sub     rsp, 8

    mov     r12, rdi ; r12 es array
    mov     r13, rsi ; r13 es pFile

    mov     rdi, [r12 + ARRAY_OFF_TYPE] ; consigo el tipo
    call    getPrintFunction
    mov     r14, rax ; r14 es la funcion print del tipo

    ;imprimo [
    mov     rdi, r13
    mov     rsi, format
    mov     rdx , ch_abre
    call    fprintf
    mov     bl , [r12 + ARRAY_OFF_SIZE]
    cmp     bl, 0 
    je  .array_vacio

    xor     rbx , rbx ;rbx es el contador
    mov     r15, [r12 + ARRAY_OFF_DATA] ;r15 es el void** data

    .while:
        mov     rdi , [r15 + rbx*8] ;es el dato actual
        mov     rsi, r13
        call    r14
        inc     rbx
        cmp     bl, [r12 + ARRAY_OFF_SIZE]
        jge .fin
        mov     rdi, r13
        mov     rsi, format
        mov     rdx , coma
        call    fprintf
        jmp .while                  

    .array_vacio:
        mov     rax, 0
    .fin:
        mov     rdi, r13
        mov     rsi, format
        mov     rdx , ch_cierra 
        call    fprintf
        add     rsp , 8
        pop     rbx
        pop     r15
        pop     r14
        pop     r13
        pop     r12
        pop     rbp 
ret


; ** Card **

; card_t* cardNew(char* suit, int32_t* number)
cardNew: 
    push    rbp
    mov     rbp, rsp
    push    r12
    push    r13
    push    r14
    push    r15
    sub     rsp, 8

    mov     r12, rdi ; r12 es suit
    mov     r13, rsi ; r13 es number

    mov     rdi, CARD_SIZE_MEM
    call    malloc
    mov     r14, rax ; r14 es card

    mov     rdi , r12
    call    strClone
    ;rax es el str copiado

    mov     [r14 + CARD_OFF_SUIT], rax
    mov     rdi , TYPE_INT
    call    getCloneFunction
    ;rax es la funcion clone
    mov     rdi, r13
    call     rax
    ;rax es el int clonado
    mov     [r14 + CARD_OFF_NUMBER], rax
    mov     rdi, TYPE_CARD
    call    listNew
    ;rax es la lista
    mov     [r14 + CARD_OFF_STACKED], rax
    mov     rax, r14
    add     rsp, 8
    pop     r15
    pop     r14
    pop     r13
    pop     r12
    pop     rbp
ret

;char* cardGetSuit(card_t* c)
cardGetSuit:
    push    rbp
    mov     rbp, rsp
    push    r12
    sub     rsp, 8

    mov     r12, rdi ; r12 es c
    mov     rax, [r12 + CARD_OFF_SUIT]

    add     rsp, 8
    pop     r12
    pop     rbp
ret

;int32_t* cardGetNumber(card_t* c)
cardGetNumber:
    push    rbp
    mov     rbp, rsp
    push    r12
    sub     rsp, 8
    
    mov     r12, rdi ; r12 es c
    mov     rax, [r12 + CARD_OFF_NUMBER]

    add     rsp, 8
    pop     r12
    pop     rbp
ret

;list_t* cardGetStacked(card_t* c)
cardGetStacked:
    push    rbp
    mov     rbp, rsp
    push    r12
    sub     rsp, 8

    mov     r12, rdi ; r12 es c
    mov     rax, [r12 + CARD_OFF_STACKED]

    add     rsp, 8
    pop     r12
    pop     rbp
ret


;void cardPrint(card_t* c, FILE* pFile)
cardPrint:
    push    rbp
    mov     rbp, rsp
    push    r12
    push    r13
  
    mov     r12, rdi ;aca guardo c
    mov     r13, rsi ;aca guardo pfile

    mov     rdi, r13
    mov     rsi, listaOp
    mov     rdx, [r12 + CARD_OFF_SUIT]
    mov     rcx, [r12 + CARD_OFF_NUMBER]
    call    fprintf
    mov     rdi , [r12 + CARD_OFF_STACKED]
    mov     rsi, r13
    call    listPrint 
    mov     rdi, r13
    mov     rsi , format
    mov     rdx , listaCl

    pop     r13
    pop     r12
    pop     rbp

ret


;int32_t cardCmp(card_t* a, card_t* b)
cardCmp:
    push    rbp
    mov     rbp, rsp
    push    r12
    push    r13

    mov     r12, rdi ; r12 es a
    mov     r13, rsi ; r13 es b
    
    mov     rdi, [r12 + CARD_OFF_SUIT]; suit de a
    mov     rsi, [r13 + CARD_OFF_SUIT]; suit de b

    call    strCmp
    cmp     rax, 0
    jne    .fin
    mov     rdi, [r12 + CARD_OFF_NUMBER];number de a
    mov     rsi, [r13 + CARD_OFF_NUMBER];number de b
    call    intCmp

    pop     r13
    pop     r12
    pop     rbp
    ret

    .fin
    pop     r13
    pop     r12
    pop     rbp
ret
    
;card_t* cardClone(card_t* c)
cardClone:
    push    rbp
    mov     rbp, rsp
    push    r12
    push    r13

    mov     r12, rdi; r12 es c
    mov     rdi, CARD_SIZE_MEM
    call    malloc ;rax es clone

    mov     r13, rax
    mov     rdi, [r12 + CARD_OFF_SUIT]
    
    call    strClone ;rax es el clon de c->suit

    mov     [r13 + CARD_OFF_SUIT], rax
    mov     rdi, [r12 + CARD_OFF_NUMBER]

    call    intClone ;rax es el clon de c->number

    mov     [r13 + CARD_OFF_NUMBER], rax
    mov     rdi, [r12 + CARD_OFF_STACKED]

    call    listClone ;rax es el clon de c->stacked

    mov     [r13 + CARD_OFF_STACKED], rax

    mov     rax, r13

    pop     r13
    pop     r12
    pop     rbp
    ret


;void cardAddStacked(card_t* c, card_t* card)
cardAddStacked:
    push    rbp
    mov     rbp, rsp    
    push    r12
    push    r13
    push    r14
    push    r15

    mov     r12, rdi ; r12 es c
    mov     r13, rsi ; r13 es card

    mov     rdi, CARD_SIZE_MEM
    call    malloc; rax es card copia

    mov     r14, rax
    mov     rdi, [r13 + CARD_OFF_SUIT]

    call    strClone ;rax es card_copia -> suit

    mov     [r14 + CARD_OFF_SUIT], rax
    mov     rdi, [r13 + CARD_OFF_NUMBER]

    call    intClone ;rax es card_copia -> number

    mov     [r14 + CARD_OFF_NUMBER], rax
    mov     rdi, [r13 + CARD_OFF_STACKED]

    call    listClone ;rax es card_copia -> stacked

    mov     [r14 + CARD_OFF_STACKED], rax

    mov     rdi, [r12 + CARD_OFF_STACKED]
    mov     rsi, r14

    call    listAddFirst

    mov    rdi, [r14 + CARD_OFF_SUIT]
    call   strDelete

    mov     rdi, [r14 + CARD_OFF_NUMBER]
    call    intDelete

    mov     rdi, [r14 + CARD_OFF_STACKED]
    call    listDelete

    mov     rdi, r14
    call    free

    pop     r15
    pop     r14
    pop     r13
    pop     r12
    pop     rbp
ret

;void cardDelete(card_t* c)
cardDelete:
    push    rbp
    mov     rbp, rsp
    push    r12
    sub     rsp, 8

    mov     r12, rdi
    mov     rdi, [r12 + CARD_OFF_SUIT]

    call    strDelete

    mov     rdi, [r12 + CARD_OFF_NUMBER]

    call    intDelete

    mov     rdi, [r12 + CARD_OFF_STACKED]

    call    listDelete

    mov     rdi, r12

    call    free

    add     rsp, 8
    pop     r12
    pop     rbp
ret

