#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* ******************************************************************
 ALTERNATING BIT AND GO-BACK-N NETWORK EMULATOR: SLIGHTLY MODIFIED
 FROM VERSION 1.1 of J.F.Kurose

   This code should be used for PA2, unidirectional or bidirectional
   data transfer protocols (from A to B. Bidirectional transfer of data
   is for extra credit and is not required).  Network properties:
   - one way network delay averages five time units (longer if there
       are other packets in the channel for GBN), but can be larger
   - frames can be corrupted (either the header or the data portion)
       or lost, according to user-defined probabilities
   - frames will be delivered in the order in which they were sent
       (although some can be lost).
**********************************************************************/

#define BIDIRECTIONAL 1 /* change to 1 if you're doing extra credit */
/* and write a routine called B_output */
#define PIGGYBACKING 1

#define IN_LAYER_3 1
#define ACK_PENDING 2
#define TIME_BEFORE_TIMER_INT 30
#define CALLING_ENTITY_A 0
#define CALLING_ENTITY_B 1

#define DATA_FRAME 0
#define ACK_FRAME 1
#define DATA_WITH_PIGGY_ACK_FRAME 2

/* a "pkt" is the data unit passed from layer 5 (teachers code) to layer  */
/* 4 (students' code).  It contains the data (characters) to be delivered */
/* to layer 5 via the students transport level protocol entities.         */

struct pkt
{
    char data[4];
};

/* a frame is the data unit passed from layer 4 (students code) to layer */
/* 3 (teachers code).  Note the pre-defined frame structure, which all   */
/* students must follow. */

struct frm
{
    int seqnum;
    int acknum;
    int checksum;
    char payload[4];
    int type;
};

struct frm A_frame;
int A_sequence_number;
int B_exp_seq_num;
int A_state;

struct frm B_frame;
int B_sequence_number;
int A_exp_seq_num;
int B_state;
int B_outstanding_ack;
int B_ack_code_piggy;

/********* FUNCTION PROTOTYPES. DEFINED IN THE LATER PART******************/
void starttimer(int AorB, float increment);
void stoptimer(int AorB);
void tolayer1(int AorB, struct frm frame);
void tolayer3(int AorB, char datasent[4]);

/********* STUDENTS WRITE THE NEXT SEVEN ROUTINES *********/
struct frm get_frame_for_A(struct pkt packet);
struct frm get_frame_for_B(struct pkt packet);
int find_checksum(int seqnum, int acknum, char *payload);
void give_acknowledgement_to_A(int acknowledgement_code);
void give_acknowledgement_to_B(int acknowledgement_code);
void show_frm(struct frm frame);

/* called from layer 5, passed the data to be sent to other side */
void A_output(struct pkt packet)
{
    printf("\n\n----------- Inside A_output -----------\n");
    if (A_state == IN_LAYER_3)
    {
        printf("\nA is in layer 3 state\n");
        A_state = ACK_PENDING;
        A_frame = get_frame_for_A(packet);
        show_frm(A_frame);
        printf("\nA is sending a frame to Layer1 and Waiting for acknowledgement\n");
        tolayer1(CALLING_ENTITY_A, A_frame);
        starttimer(CALLING_ENTITY_A, TIME_BEFORE_TIMER_INT);
    }
    else
    {
        printf("\nA is waiting for acknowledgement of %s, dropping %s\n", A_frame.payload, packet.data);
    }
}

void data_non_piggy(struct pkt packet)
{
    printf("\nB is in layer 3 state\n");
    B_state = ACK_PENDING;
    B_frame = get_frame_for_B(packet);
    printf("\nB is sending a frame to Layer1 and Waiting for acknowledgement\n");
    tolayer1(CALLING_ENTITY_B, B_frame);
    starttimer(CALLING_ENTITY_B, TIME_BEFORE_TIMER_INT);
}
/* need be completed only for extra credit */
void B_output(struct pkt packet)
{
    printf("\n\n----------- Inside B_output -----------\n");
    if (B_state == IN_LAYER_3)
    {
        if (PIGGYBACKING == 1)
        {
            if (B_outstanding_ack == 1)
            {
                B_outstanding_ack = 0;
                printf("\nB is in layer 3 state\n");
                B_state = ACK_PENDING;
                B_frame = get_frame_for_B(packet);
                B_frame.acknum = B_exp_seq_num;
                if (B_ack_code_piggy == B_exp_seq_num)
                {
                    B_exp_seq_num = 1 - B_exp_seq_num;
                }
                B_frame.type = DATA_WITH_PIGGY_ACK_FRAME;
                show_frm(B_frame);
                printf("\nB sending Piggyback ack\n");
                printf("\nB is sending a frame to Layer1 and Waiting for acknowledgement\n");
                tolayer1(CALLING_ENTITY_B, B_frame);
                starttimer(CALLING_ENTITY_B, TIME_BEFORE_TIMER_INT);
            }
            else
            {
                data_non_piggy(packet);
            }
        }
        else
        {
            data_non_piggy(packet);
        }
    }
    else
    {
        printf("\nB is waiting for acknowledgement of %s, dropping %s\n", B_frame.payload, packet.data);
    }
}

void handle_ack(struct frm frame)
{
    int temp_cs = find_checksum(frame.seqnum, frame.acknum, frame.payload);
    if (A_sequence_number != frame.acknum)
    {
        printf("\n----------Expected Acknowledgement: %d---------Found: %d-----------\n", A_sequence_number, frame.acknum);
        return;
    }
    if (A_state != ACK_PENDING)
    {
        printf("\n---------A is not waiting for any acknowledgement----------\n");
        return;
    }
    if (temp_cs != frame.checksum)
    {
        printf("\n-------Checksum doesn't match-----frame Corruption-------\n");
        return;
    }
    printf("\nA got proper acknowledgement:::::Acknum: %d::::: packet: %s\n", frame.acknum, A_frame.payload);
    printf("\nA is stopping timer and going back to non waiting state\n");
    stoptimer(CALLING_ENTITY_A);
    A_sequence_number = 1 - A_sequence_number;
    A_state = IN_LAYER_3;
}

void handle_data(struct frm frame)
{
    int temp_cs = find_checksum(frame.seqnum, frame.acknum, frame.payload);
    int acknowledgement_code = 1 - A_exp_seq_num;
    if (temp_cs != frame.checksum || frame.seqnum != A_exp_seq_num)
    {
        if (temp_cs != frame.checksum)
            printf("\n-------Checksum doesn't match-----frame Corruption-------\n");
        if (frame.seqnum != A_exp_seq_num)
            printf("\n-------Sequence number doesn't match-------- Wanted: %d------Found: %d\n", A_exp_seq_num, frame.seqnum);
        printf("\nA is sending NACK by alternating the acknowledgement number::::frame acknum: %d\n", acknowledgement_code);
        give_acknowledgement_to_B(acknowledgement_code);
        return;
    }
    printf("\nA is sending proper acknowledgement to B and sending the frame to layer 3\n");
    give_acknowledgement_to_B(A_exp_seq_num);
    tolayer3(CALLING_ENTITY_A, frame.payload);
    A_exp_seq_num = acknowledgement_code;
}
/* called from layer 3, when a frame arrives for layer 4 */
void A_input(struct frm frame)
{
    printf("\n\n----------- Inside A_input -----------\n");
    show_frm(frame);
    if (frame.type == ACK_FRAME)
    {
        handle_ack(frame);
    }
    else if (frame.type == DATA_FRAME)
    {
        handle_data(frame);
    }
    else if (frame.type == DATA_WITH_PIGGY_ACK_FRAME)
    {

        if (A_state != ACK_PENDING)
        {
            printf("\n---------A is not waiting for any acknowledgement----------\n");
            return;
        }
        int temp_cs = find_checksum(frame.seqnum, frame.acknum, frame.payload);
        int acknowledgement_code = 1 - A_exp_seq_num;
        if (temp_cs != frame.checksum)
        {
            printf("\n-------Checksum doesn't match-----frame Corruption-------\n");
        }
        else if (A_sequence_number != frame.acknum)
        {
            printf("\n----------Expected Acknowledgement: %d---------Found: %d-----------\n", A_sequence_number, frame.acknum);
        }
        else
        {
            printf("\nA got proper acknowledgement:::::Acknum: %d::::: packet: %s\n", frame.acknum, A_frame.payload);
            printf("\nA is stopping timer and going back to non waiting state\n");
            stoptimer(CALLING_ENTITY_A);
            A_sequence_number = 1 - A_sequence_number;
            A_state = IN_LAYER_3;
        }

        if (frame.seqnum != A_exp_seq_num)
        {
            printf("\n-------Sequence number doesn't match-------- Wanted: %d------Found: %d\n", A_exp_seq_num, frame.seqnum);
            printf("\nA is sending NACK by alternating the acknowledgement number::::frame acknum: %d\n", acknowledgement_code);
            give_acknowledgement_to_B(acknowledgement_code);
        }
        else
        {
            printf("\nA is sending proper acknowledgement to B and sending the frame to layer 3\n");
            give_acknowledgement_to_B(A_exp_seq_num);
            tolayer3(CALLING_ENTITY_A, frame.payload);
            A_exp_seq_num = acknowledgement_code;
        }
    }
}

/* called when A's timer goes off */
void A_timerinterrupt(void)
{
    printf("\n\n----------- Inside A_timerinterrupt -----------\n");
    if (A_state == ACK_PENDING)
    {
        printf("\n------ State: ACK_PENDING ------- A is sending the last frame again to layer 1-----\n");
        tolayer1(CALLING_ENTITY_A, A_frame);
        starttimer(CALLING_ENTITY_A, TIME_BEFORE_TIMER_INT);
    }
    else
    {
        printf("\n------ State: A not waiting for any acknowledgement -----\n");
    }
}

/* the following routine will be called once (only) before any other */
/* entity A routines are called. You can use it to do any initialization */
void A_init(void)
{
    printf("\n----------- Initializing A -----------\n");
    A_state = IN_LAYER_3;
    A_sequence_number = 0;
    A_exp_seq_num = 0;
}

void ack_non_piggy(struct frm frame)
{

    int temp_cs = find_checksum(frame.seqnum, frame.acknum, frame.payload);
    B_outstanding_ack = 0;
    int acknowledgement_code = 1 - B_exp_seq_num;
    if (temp_cs != frame.checksum || frame.seqnum != B_exp_seq_num)
    {
        if (temp_cs != frame.checksum)
            printf("\n-------Checksum doesn't match-----frame Corruption-------\n");
        if (frame.seqnum != B_exp_seq_num)
            printf("\n-------Sequence number doesn't match-------- Wanted: %d------Found: %d\n", B_exp_seq_num, frame.seqnum);
        printf("\nB is sending NACK by alternating the acknowledgement number::::frame acknum: %d\n", acknowledgement_code);
        give_acknowledgement_to_A(acknowledgement_code);
        return;
    }
    printf("\nB is sending proper acknowledgement to A and sending the frame to layer 3\n");
    give_acknowledgement_to_A(B_exp_seq_num);
    tolayer3(CALLING_ENTITY_B, frame.payload);
    B_exp_seq_num = acknowledgement_code;
}
/* Note that with simplex transfer from a-to-B, there is no B_output() */
/* called from layer 3, when a frame arrives for layer 4 at B*/
void B_input(struct frm frame)
{
    printf("\n\n----------- Inside B_input -----------\n");
    show_frm(frame);
    if (frame.type == DATA_FRAME)
    {

        if (PIGGYBACKING == 1)
        {
            if (B_outstanding_ack == 0)
            {
                B_outstanding_ack = 1;
                int temp_cs = find_checksum(frame.seqnum, frame.acknum, frame.payload);
                if (temp_cs != frame.checksum)
                {
                    printf("\n-------Checksum doesn't match-----frame Corruption-------\n");
                    B_ack_code_piggy = 1 - B_exp_seq_num;
                }
                else if (B_exp_seq_num != frame.seqnum)
                {
                    printf("\n--------Sequence number doesn't match---------\n");
                    B_ack_code_piggy = 1 - B_exp_seq_num;
                }
                else
                {
                    printf("\n--------Frame received------");
                    B_ack_code_piggy = B_exp_seq_num;
                }
                printf("\nSent for piggy backing\n");
            }
            else
            {
                ack_non_piggy(frame);
            }
        }
        else
        {
            ack_non_piggy(frame);
        }
    }
    else if (frame.type == ACK_FRAME)
    {
        int temp_cs = find_checksum(frame.seqnum, frame.acknum, frame.payload);
        if (B_sequence_number != frame.acknum)
        {
            printf("\n----------Expected Acknowledgement: %d---------Found: %d-----------\n", B_sequence_number, frame.acknum);
            return;
        }
        if (B_state != ACK_PENDING)
        {
            printf("\n---------B is not waiting for any acknowledgement----------\n");
            return;
        }
        if (temp_cs != frame.checksum)
        {
            printf("\n-------Checksum doesn't match-----frame Corruption-------\n");
            return;
        }
        printf("\nB got proper acknowledgement:::::Acknum: %d::::: packet: %s\n", frame.acknum, B_frame.payload);
        printf("\nB is stopping timer and going back to non waiting state\n");
        stoptimer(CALLING_ENTITY_B);
        B_sequence_number = 1 - B_sequence_number;
        B_state = IN_LAYER_3;
    }
}

/* called when B's timer goes off */
void B_timerinterrupt(void)
{
    printf("\n\n----------- Inside B_timerinterrupt -----------\n");
    if (B_state == ACK_PENDING)
    {
        printf("\n------ State: ACK_PENDING ------- B is sending the last frame again to layer 1-----\n");
        if (PIGGYBACKING == 1)
        {
            if (B_outstanding_ack == 1)
            {
                B_frame.acknum = B_ack_code_piggy;
                B_frame.type = DATA_WITH_PIGGY_ACK_FRAME;
                if (B_exp_seq_num == B_ack_code_piggy)
                {
                    B_exp_seq_num = 1 - B_exp_seq_num;
                }
                B_outstanding_ack = 0;
            }
        }

        tolayer1(CALLING_ENTITY_B, B_frame);
        starttimer(CALLING_ENTITY_B, TIME_BEFORE_TIMER_INT);
    }
    else
    {
        printf("\n------ State: B not waiting for any acknowledgement -----\n");
    }
}

/* the following rouytine will be called once (only) before any other */
/* entity B routines are called. You can use it to do any initialization */
void B_init(void)
{
    printf("\n\n----------- Initializing B -----------\n");
    B_exp_seq_num = 0;
    B_state = IN_LAYER_3;
    B_sequence_number = 0;
}

int find_checksum(int seqnum, int acknum, char *payload)
{
    int cs = seqnum + acknum;
    for (int i = 0; i < 4; i++)
    {
        cs = cs + payload[i];
    }
    return cs;
}

void give_acknowledgement_to_A(int acknowledgement_code)
{
    struct frm temp_frm;
    temp_frm.acknum = acknowledgement_code;
    temp_frm.checksum = find_checksum(temp_frm.seqnum, temp_frm.acknum, temp_frm.payload);
    temp_frm.type = ACK_FRAME;
    tolayer1(CALLING_ENTITY_B, temp_frm);
}

struct frm get_frame_for_A(struct pkt packet)
{
    struct frm temp_frm;
    temp_frm.seqnum = A_sequence_number;
    for (int i = 0; i < 4; i++)
    {
        temp_frm.payload[i] = packet.data[i];
    }
    temp_frm.checksum = find_checksum(temp_frm.seqnum, temp_frm.acknum, temp_frm.payload);
    temp_frm.type = DATA_FRAME;
    return temp_frm;
}

void give_acknowledgement_to_B(int acknowledgement_code)
{
    struct frm temp_frm;
    temp_frm.acknum = acknowledgement_code;
    temp_frm.checksum = find_checksum(temp_frm.seqnum, temp_frm.acknum, temp_frm.payload);
    temp_frm.type = ACK_FRAME;
    tolayer1(CALLING_ENTITY_A, temp_frm);
}

struct frm get_frame_for_B(struct pkt packet)
{
    struct frm temp_frm;
    temp_frm.seqnum = B_sequence_number;
    for (int i = 0; i < 4; i++)
    {
        temp_frm.payload[i] = packet.data[i];
    }
    temp_frm.checksum = find_checksum(temp_frm.seqnum, temp_frm.acknum, temp_frm.payload);
    temp_frm.type = DATA_FRAME;
    return temp_frm;
}

void show_frm(struct frm frame)
{
    printf("\n\nFrame(type: %d)\n", frame.type);
    printf("Payload: %s\n", frame.payload);
    printf("Seqnum: %d\t", frame.seqnum);
    printf("Acknum: %d\t", frame.acknum);
    printf("Checksum: %d\t", frame.checksum);
}

/*****************************************************************
***************** NETWORK EMULATION CODE STARTS BELOW ***********
The code below emulates the layer 3 and below network environment:
    - emulates the tranmission and delivery (possibly with bit-level corruption
        and frame loss) of frames across the layer 3/4 interface
    - handles the starting/stopping of a timer, and generates timer
        interrupts (resulting in calling students timer handler).
    - generates packet to be sent (passed from later 5 to 4)

THERE IS NOT REASON THAT ANY STUDENT SHOULD HAVE TO READ OR UNDERSTAND
THE CODE BELOW.  YOU SHOLD NOT TOUCH, OR REFERENCE (in your code) ANY
OF THE DATA STRUCTURES BELOW.  If you're interested in how I designed
the emulator, you're welcome to look at the code - but again, you should have
to, and you defeinitely should not have to modify
******************************************************************/

struct event
{
    float evtime;       /* event time */
    int evtype;         /* event type code */
    int eventity;       /* entity where event occurs */
    struct frm *frmptr; /* ptr to frame (if any) assoc w/ this event */
    struct event *prev;
    struct event *next;
};
struct event *evlist = NULL; /* the event list */

/* possible events: */
#define TIMER_INTERRUPT 0
#define FROM_LAYER3 1
#define FROM_LAYER1 2

#define OFF 0
#define ON 1
#define A 0
#define B 1

int TRACE = 1;   /* for my debugging */
int nsim = 0;    /* number of packets from 5 to 4 so far */
int nsimmax = 0; /* number of pkts to generate, then stop */
float time = 0.000;
float lossprob;    /* probability that a frame is dropped  */
float corruptprob; /* probability that one bit is frame is flipped */
float lambda;      /* arrival rate of packets from layer 5 */
int ntolayer1;     /* number sent into layer 3 */
int nlost;         /* number lost in media */
int ncorrupt;      /* number corrupted by media*/

void init();
void generate_next_arrival(void);
void insertevent(struct event *p);

int main()
{
    struct event *eventptr;
    struct pkt pkt2give;
    struct frm frm2give;

    int i, j;
    char c;

    init();
    A_init();
    B_init();

    while (1)
    {
        eventptr = evlist; /* get next event to simulate */
        if (eventptr == NULL)
            goto terminate;
        evlist = evlist->next; /* remove this event from event list */
        if (evlist != NULL)
            evlist->prev = NULL;
        if (TRACE >= 2)
        {
            printf("\nEVENT time: %f,", eventptr->evtime);
            printf("  type: %d", eventptr->evtype);
            if (eventptr->evtype == 0)
                printf(", timerinterrupt  ");
            else if (eventptr->evtype == 1)
                printf(", fromlayer3 ");
            else
                printf(", fromlayer1 ");
            printf(" entity: %d\n", eventptr->eventity);
        }
        time = eventptr->evtime; /* update time to next event time */
        if (eventptr->evtype == FROM_LAYER3)
        {
            if (nsim < nsimmax)
            {
                if (nsim + 1 < nsimmax)
                    generate_next_arrival(); /* set up future arrival */
                /* fill in pkt to give with string of same letter */
                j = nsim % 26;
                for (i = 0; i < 4; i++)
                    pkt2give.data[i] = 97 + j;
                pkt2give.data[3] = 0;
                if (TRACE > 2)
                {
                    printf("          MAINLOOP: data given to student: ");
                    for (i = 0; i < 4; i++)
                        printf("%c", pkt2give.data[i]);
                    printf("\n");
                }
                nsim++;
                if (eventptr->eventity == A)
                    A_output(pkt2give);
                else
                    B_output(pkt2give);
            }
        }
        else if (eventptr->evtype == FROM_LAYER1)
        {
            frm2give.seqnum = eventptr->frmptr->seqnum;
            frm2give.acknum = eventptr->frmptr->acknum;
            frm2give.checksum = eventptr->frmptr->checksum;
            frm2give.type = eventptr->frmptr->type;
            for (i = 0; i < 4; i++)
                frm2give.payload[i] = eventptr->frmptr->payload[i];
            if (eventptr->eventity == A) /* deliver frame by calling */
                A_input(frm2give);       /* appropriate entity */
            else
                B_input(frm2give);
            free(eventptr->frmptr); /* free the memory for frame */
        }
        else if (eventptr->evtype == TIMER_INTERRUPT)
        {
            if (eventptr->eventity == A)
                A_timerinterrupt();
            else
                B_timerinterrupt();
        }
        else
        {
            printf("INTERNAL PANIC: unknown event type \n");
        }
        free(eventptr);
    }

terminate:
    printf(
        " Simulator terminated at time %f\n after sending %d pkts from layer3\n",
        time, nsim);
}

void init() /* initialize the simulator */
{
    int i;
    float sum, avg;
    float jimsrand();

    printf("-----  Stop and Wait Network Simulator Version 1.1 -------- \n\n");
    printf("Enter the number of packets to simulate: ");
    scanf("%d", &nsimmax);
    printf("Enter  frame loss probability [enter 0.0 for no loss]:");
    scanf("%f", &lossprob);
    printf("Enter frame corruption probability [0.0 for no corruption]:");
    scanf("%f", &corruptprob);
    printf("Enter average time between packets from sender's layer3 [ > 0.0]:");
    scanf("%f", &lambda);
    printf("Enter TRACE:");
    scanf("%d", &TRACE);

    srand(9999); /* init random number generator */
    sum = 0.0;   /* test random number generator for students */
    for (i = 0; i < 1000; i++)
        sum = sum + jimsrand(); /* jimsrand() should be uniform in [0,1] */
    avg = sum / 1000.0;
    if (avg < 0.25 || avg > 0.75)
    {
        printf("It is likely that random number generation on your machine\n");
        printf("is different from what this emulator expects.  Please take\n");
        printf("a look at the routine jimsrand() in the emulator code. Sorry. \n");
        exit(1);
    }

    ntolayer1 = 0;
    nlost = 0;
    ncorrupt = 0;

    time = 0.0;              /* initialize time to 0.0 */
    generate_next_arrival(); /* initialize event list */
}

/****************************************************************************/
/* jimsrand(): return a float in range [0,1].  The routine below is used to */
/* isolate all random number generation in one location.  We assume that the*/
/* system-supplied rand() function return an int in therange [0,mmm]        */
/****************************************************************************/
float jimsrand(void)
{
    double mmm = RAND_MAX;
    float x;          /* individual students may need to change mmm */
    x = rand() / mmm; /* x should be uniform in [0,1] */
    return (x);
}

/********************* EVENT HANDLINE ROUTINES *******/
/*  The next set of routines handle the event list   */
/*****************************************************/

void generate_next_arrival(void)
{
    double x, log(), ceil();
    struct event *evptr;
    float ttime;
    int tempint;

    if (TRACE > 2)
        printf("          GENERATE NEXT ARRIVAL: creating new arrival\n");

    x = lambda * jimsrand() * 2; /* x is uniform on [0,2*lambda] */
    /* having mean of lambda        */
    evptr = (struct event *)malloc(sizeof(struct event));
    evptr->evtime = time + x;
    evptr->evtype = FROM_LAYER3;
    if (BIDIRECTIONAL && (jimsrand() > 0.5))
        evptr->eventity = B;
    else
        evptr->eventity = A;
    insertevent(evptr);
}

void insertevent(struct event *p)
{
    struct event *q, *qold;

    if (TRACE > 2)
    {
        printf("            INSERTEVENT: time is %lf\n", time);
        printf("            INSERTEVENT: future time will be %lf\n", p->evtime);
    }
    q = evlist;    /* q points to header of list in which p struct inserted */
    if (q == NULL) /* list is empty */
    {
        evlist = p;
        p->next = NULL;
        p->prev = NULL;
    }
    else
    {
        for (qold = q; q != NULL && p->evtime > q->evtime; q = q->next)
            qold = q;
        if (q == NULL) /* end of list */
        {
            qold->next = p;
            p->prev = qold;
            p->next = NULL;
        }
        else if (q == evlist) /* front of list */
        {
            p->next = evlist;
            p->prev = NULL;
            p->next->prev = p;
            evlist = p;
        }
        else /* middle of list */
        {
            p->next = q;
            p->prev = q->prev;
            q->prev->next = p;
            q->prev = p;
        }
    }
}

void printevlist(void)
{
    struct event *q;
    int i;
    printf("--------------\nEvent List Follows:\n");
    for (q = evlist; q != NULL; q = q->next)
    {
        printf("Event time: %f, type: %d entity: %d\n", q->evtime, q->evtype,
               q->eventity);
    }
    printf("--------------\n");
}

/********************** Student-callable ROUTINES ***********************/

/* called by students routine to cancel a previously-started timer */
void stoptimer(int AorB /* A or B is trying to stop timer */)
{
    struct event *q, *qold;

    if (TRACE > 2)
        printf("          STOP TIMER: stopping timer at %f\n", time);
    /* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next)  */
    for (q = evlist; q != NULL; q = q->next)
        if ((q->evtype == TIMER_INTERRUPT && q->eventity == AorB))
        {
            /* remove this event */
            if (q->next == NULL && q->prev == NULL)
                evlist = NULL;        /* remove first and only event on list */
            else if (q->next == NULL) /* end of list - there is one in front */
                q->prev->next = NULL;
            else if (q == evlist) /* front of list - there must be event after */
            {
                q->next->prev = NULL;
                evlist = q->next;
            }
            else /* middle of list */
            {
                q->next->prev = q->prev;
                q->prev->next = q->next;
            }
            free(q);
            return;
        }
    printf("Warning: unable to cancel your timer. It wasn't running.\n");
}

void starttimer(int AorB /* A or B is trying to start timer */, float increment)
{
    struct event *q;
    struct event *evptr;

    if (TRACE > 2)
        printf("          START TIMER: starting timer at %f\n", time);
    /* be nice: check to see if timer is already started, if so, then  warn */
    /* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next)  */
    for (q = evlist; q != NULL; q = q->next)
        if ((q->evtype == TIMER_INTERRUPT && q->eventity == AorB))
        {
            printf("Warning: attempt to start a timer that is already started\n");
            return;
        }

    /* create future event for when timer goes off */
    evptr = (struct event *)malloc(sizeof(struct event));
    evptr->evtime = time + increment;
    evptr->evtype = TIMER_INTERRUPT;
    evptr->eventity = AorB;
    insertevent(evptr);
}

/************************** TOLAYER3 ***************/
void tolayer1(int AorB, struct frm frame)
{
    struct frm *myfrmptr;
    struct event *evptr, *q;
    float lastime, x;
    int i;

    ntolayer1++;

    /* simulate losses: */
    if (jimsrand() < lossprob)
    {
        nlost++;
        if (TRACE > 0)
            printf("          TOLAYER1: frame being lost\n");
        return;
    }

    /* make a copy of the frame student just gave me since he/she may decide */
    /* to do something with the frame after we return back to him/her */
    myfrmptr = (struct frm *)malloc(sizeof(struct frm));
    myfrmptr->seqnum = frame.seqnum;
    myfrmptr->acknum = frame.acknum;
    myfrmptr->checksum = frame.checksum;
    myfrmptr->type = frame.type;
    for (i = 0; i < 4; i++)
        myfrmptr->payload[i] = frame.payload[i];
    if (TRACE > 2)
    {
        printf("          TOLAYER1: seq: %d, ack %d, check: %d ", myfrmptr->seqnum,
               myfrmptr->acknum, myfrmptr->checksum);
        for (i = 0; i < 4; i++)
            printf("%c", myfrmptr->payload[i]);
        printf("\n");
    }

    /* create future event for arrival of frame at the other side */
    evptr = (struct event *)malloc(sizeof(struct event));
    evptr->evtype = FROM_LAYER1;      /* frame will pop out from layer3 */
    evptr->eventity = (AorB + 1) % 2; /* event occurs at other entity */
    evptr->frmptr = myfrmptr;         /* save ptr to my copy of frame */
    /* finally, compute the arrival time of frame at the other end.
       medium can not reorder, so make sure frame arrives between 1 and 10
       time units after the latest arrival time of frames
       currently in the medium on their way to the destination */
    lastime = time;
    /* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next) */
    for (q = evlist; q != NULL; q = q->next)
        if ((q->evtype == FROM_LAYER1 && q->eventity == evptr->eventity))
            lastime = q->evtime;
    evptr->evtime = lastime + 1 + 9 * jimsrand();

    /* simulate corruption: */
    if (jimsrand() < corruptprob)
    {
        ncorrupt++;
        if ((x = jimsrand()) < .75)
            myfrmptr->payload[0] = 'Z'; /* corrupt payload */
        else if (x < .875)
            myfrmptr->seqnum = 999999;
        else
            myfrmptr->acknum = 999999;
        if (TRACE > 0)
            printf("          TOLAYER1: frame being corrupted\n");
    }

    if (TRACE > 2)
        printf("          TOLAYER1: scheduling arrival on other side\n");
    insertevent(evptr);
}

void tolayer3(int AorB, char datasent[4])
{
    int i;
    if (TRACE > 2)
    {
        printf("          TOLAYER3: data received: ");
        for (i = 0; i < 4; i++)
            printf("%c", datasent[i]);
        printf("\n");
    }
}
