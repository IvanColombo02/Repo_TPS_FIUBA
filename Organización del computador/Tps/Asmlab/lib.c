#include "lib.h"

funcCmp_t *getCompareFunction(type_t t)
{
    switch (t)
    {
    case TypeInt:
        return (funcCmp_t *)&intCmp;
        break;
    case TypeString:
        return (funcCmp_t *)&strCmp;
        break;
    case TypeCard:
        return (funcCmp_t *)&cardCmp;
        break;
    default:
        break;
    }
    return 0;
}
funcClone_t *getCloneFunction(type_t t)
{
    switch (t)
    {
    case TypeInt:
        return (funcClone_t *)&intClone;
        break;
    case TypeString:
        return (funcClone_t *)&strClone;
        break;
    case TypeCard:
        return (funcClone_t *)&cardClone;
        break;
    default:
        break;
    }
    return 0;
}
funcDelete_t *getDeleteFunction(type_t t)
{
    switch (t)
    {
    case TypeInt:
        return (funcDelete_t *)&intDelete;
        break;
    case TypeString:
        return (funcDelete_t *)&strDelete;
        break;
    case TypeCard:
        return (funcDelete_t *)&cardDelete;
        break;
    default:
        break;
    }
    return 0;
}
funcPrint_t *getPrintFunction(type_t t)
{
    switch (t)
    {
    case TypeInt:
        return (funcPrint_t *)&intPrint;
        break;
    case TypeString:
        return (funcPrint_t *)&strPrint;
        break;
    case TypeCard:
        return (funcPrint_t *)&cardPrint;
        break;
    default:
        break;
    }
    return 0;
}

/** Int **/

int32_t intCmp(int32_t *a, int32_t *b)
{
    if (*a > *b) {
        return -1;
    }
    if (*a < *b) {
        return 1;
    }
    return 0;
}

void intDelete(int32_t *a)
{
    free(a);
}

void intPrint(int32_t *a, FILE *pFile)
{
    fprintf(pFile, "%d", *a);
}

int32_t *intClone(int32_t *a)
{
    int32_t* num = malloc(sizeof(int32_t));
    *num = *a;
    return num;
}

/** Lista **/

list_t *listNew(type_t t)
{
    list_t* list = calloc(1, sizeof(list_t));
    list->type = t;
    return list;
}

uint8_t listGetSize(list_t *l)
{
    return l->size;
}

void *listGet(list_t *l, uint8_t i)
{
    if (i >= l->size)return NULL;
    listElem_t *nodo_actual = l->first;
    for (uint8_t j = 0; j < i; j++) 
        nodo_actual = nodo_actual->next;
    return nodo_actual->data;
}

void listAddFirst(list_t *l, void *data)
{
    listElem_t *nodo_nuevo = calloc(1, sizeof(listElem_t));
    funcClone_t* funcion_clone = getCloneFunction(l->type);
    nodo_nuevo->data = funcion_clone(data); 
    if(l->size == 0)
        l->last = nodo_nuevo;
    else nodo_nuevo->next = l->first;
    l->first = nodo_nuevo;
    l->size++;
}

void listAddLast(list_t *l, void *data)
{
    listElem_t *nodo_nuevo = calloc(1, sizeof(listElem_t));
    funcClone_t* funcion_clone = getCloneFunction(l->type);
    nodo_nuevo->data = funcion_clone(data); 
    if(l->size == 0)
        l->first = nodo_nuevo;
    else{
        nodo_nuevo->prev = l->last;
        l->last->next = nodo_nuevo;
    }
    l->last = nodo_nuevo;
    l->size++;
}

list_t *listClone(list_t *l)
{
    list_t* nueva_lista = listNew(l->type);
    listElem_t* nodo_actual = l->first;
    funcClone_t* funcion_clonar = getCloneFunction(l->type);
    while(nodo_actual){
        void* nuevo_elemento = funcion_clonar(nodo_actual->data);
        listAddLast(nueva_lista,nuevo_elemento);
        nodo_actual = nodo_actual->next;
    }
    return nueva_lista;
}

void *listRemove(list_t *l, uint8_t i)
{
    if (i > l->size)return NULL;
    listElem_t *nodo_actual = l->first;
    for (uint8_t j = 0; j < i; j++) 
        nodo_actual = nodo_actual->next;
    listElem_t* nodo_anterior = nodo_actual->prev;
    listElem_t* nodo_siguiente = nodo_actual->next;
    nodo_anterior->next = nodo_siguiente;
    nodo_siguiente->prev = nodo_anterior;

    //Copiamos data
    funcClone_t* funcion_clone = getCloneFunction(l->type);
    void* data = funcion_clone(nodo_actual->data);

    //Eliminamos
    funcDelete_t* delete_funcion = getDeleteFunction(l->type);
    delete_funcion(nodo_actual->data);
    free(nodo_actual);
    l->size--;
    return data;
}

void listSwap(list_t *l, uint8_t i, uint8_t j)
{
    if( j > l->size || i > l->size)return;
    listElem_t* nodo_i = l->first;
    for(int n_i = 0; n_i < i; n_i++) nodo_i = nodo_i->next;
    listElem_t* nodo_j = l->first;
    for(int n_j = 0; n_j < j; n_j++) nodo_j = nodo_j->next;
    void* aux = nodo_i->data;
    nodo_i->data = nodo_j->data;
    nodo_j->data = aux;
}

void listDelete(list_t *l)
{
    listElem_t* nodo_actual = l->first;
    listElem_t* nodo_siguiente = NULL;
    funcDelete_t* delete_funcion = getDeleteFunction(l->type);
    while(nodo_actual){
        nodo_siguiente = nodo_actual->next;
        delete_funcion(nodo_actual->data);
        free(nodo_actual);
        nodo_actual = nodo_siguiente;
    }
    free(l);
}

void listPrint(list_t* l, FILE *pFile)
{
    if(l->size <= 0){
        fprintf(pFile, "[]");
        return;
    }
    listElem_t* nodo_actual = l->first;
    fprintf(pFile, "[");
    funcPrint_t* funcion_imprimir = getPrintFunction(l->type);
    for (int i = 0; i < l->size; i++) {
        funcion_imprimir(nodo_actual->data, pFile);
        if (i < l->size - 1) fprintf(pFile, ", ");
        nodo_actual = nodo_actual->next;
    }
    fprintf(pFile, "]");
}

/** Game **/

game_t *gameNew(void *cardDeck, funcGet_t *funcGet, funcRemove_t *funcRemove, funcSize_t *funcSize, funcPrint_t *funcPrint, funcDelete_t *funcDelete)
{
    game_t *game = (game_t *)malloc(sizeof(game_t));
    game->cardDeck = cardDeck;
    game->funcGet = funcGet;
    game->funcRemove = funcRemove;
    game->funcSize = funcSize;
    game->funcPrint = funcPrint;
    game->funcDelete = funcDelete;
    return game;
}
int gamePlayStep(game_t *g)
{
    int applied = 0;
    uint8_t i = 0;
    while (applied == 0 && i + 2 < g->funcSize(g->cardDeck))
    {
        card_t *a = g->funcGet(g->cardDeck, i);
        card_t *b = g->funcGet(g->cardDeck, i + 1);
        card_t *c = g->funcGet(g->cardDeck, i + 2);
        if (strCmp(cardGetSuit(a), cardGetSuit(c)) == 0 || intCmp(cardGetNumber(a), cardGetNumber(c)) == 0)
        {
            card_t *removed = g->funcRemove(g->cardDeck, i);
            cardAddStacked(b, removed);
            cardDelete(removed);
            applied = 1;
        }
        i++;
    }
    return applied;
}
uint8_t gameGetCardDeckSize(game_t *g)
{
    return g->funcSize(g->cardDeck);
}
void gameDelete(game_t *g)
{
    g->funcDelete(g->cardDeck);
    free(g);
}
void gamePrint(game_t *g, FILE *pFile)
{
    g->funcPrint(g->cardDeck, pFile);
}
//bn
