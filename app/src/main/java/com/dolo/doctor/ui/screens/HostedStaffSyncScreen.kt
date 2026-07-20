package com.dolo.doctor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dolo.doctor.data.model.UserRole
import com.dolo.doctor.hosted.*
import com.dolo.doctor.ui.components.ElevatedSection
import com.dolo.doctor.ui.components.PageHeader
import com.dolo.doctor.ui.components.PrimaryAction
import com.dolo.doctor.ui.components.StatusPill
import kotlinx.coroutines.delay

@Composable fun HostedStaffSyncScreen(localRole:UserRole,onBack:()->Unit,viewModel:HostedStaffViewModel){
 val state=viewModel.uiState;var pin by remember{mutableStateOf("")};var selectedSessionId by remember{mutableStateOf<String?>(null)}
 LaunchedEffect(state.snapshot?.sessions){if(selectedSessionId==null)selectedSessionId=state.snapshot?.sessions?.firstOrNull()?.id}
 LaunchedEffect(state.snapshot!=null){if(state.snapshot!=null)while(true){delay(15_000);viewModel.refresh()}}
 LazyColumn(Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{PageHeader("Hosted staff queue",onBack)}
  item{ElevatedSection("Stage 16F hosted access control",state.message){StatusPill(if(state.error)"Needs attention" else if(state.snapshot!=null)"Connected" else "Not connected",state.snapshot!=null&&!state.error);Text("Local Doctor/Assistant data remains separate and is never uploaded.",color=MaterialTheme.colorScheme.onSurfaceVariant)}}
  if(state.snapshot==null){item{ElevatedSection("Connect hosted identity",if(localRole==UserRole.DOCTOR)"Seeded Doctor" else "Seeded queue Assistant"){OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(4)},Modifier.fillMaxWidth(),label={Text("Demo PIN")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),visualTransformation=PasswordVisualTransformation(),singleLine=true);Text("Use demo PIN 1234. No real credential is sent.",color=MaterialTheme.colorScheme.onSurfaceVariant);PrimaryAction(if(state.loading)"Connecting..." else "Connect to hosted prototype",{viewModel.connect(if(localRole==UserRole.DOCTOR)HostedStaffRole.DOCTOR else HostedStaffRole.ASSISTANT,pin)},enabled=pin.length==4&&!state.loading)}}}
  state.snapshot?.let{snapshot->
   item{ElevatedSection(snapshot.clinic.name,"${snapshot.clinic.doctorName} • ${snapshot.clinic.city}"){Text("Identity: ${snapshot.role.name}",fontWeight=FontWeight.Bold);Text("Permissions: ${snapshot.permissions.sorted().joinToString()}",color=MaterialTheme.colorScheme.onSurfaceVariant);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({viewModel.refresh()},enabled=!state.loading){Text("Refresh")};OutlinedButton({viewModel.logout()}){Text("Disconnect hosted")}}}}
   if(snapshot.role==HostedStaffRole.DOCTOR){item{Text("Hosted Assistant access",style=MaterialTheme.typography.titleLarge)};if(snapshot.assistants.isEmpty())item{Text("No hosted Assistants assigned.",color=MaterialTheme.colorScheme.onSurfaceVariant)}else items(snapshot.assistants,key={"assistant-${it.id}"}){assistant->HostedAssistantAccessCard(assistant,state.loading,viewModel::updateAssistant)}}
   item{Text("Clinic sessions",style=MaterialTheme.typography.titleLarge)}
   items(snapshot.sessions,key={it.id}){session->FilterChip(selected=selectedSessionId==session.id,onClick={selectedSessionId=session.id},label={Text("${session.date} • ${session.name} • ${session.available} available")},modifier=Modifier.fillMaxWidth())}
   val session= snapshot.sessions.firstOrNull{it.id==selectedSessionId};val queue=snapshot.queues.firstOrNull{it.sessionId==selectedSessionId};val appointments=snapshot.appointments.filter{it.sessionId==selectedSessionId}
   if(session!=null){item{ElevatedSection("Authoritative queue","${session.date} • ${session.name}"){Text("Queue: ${queue?.status?:"NOT_STARTED"} | Current token: ${queue?.currentToken?:"None"}",fontWeight=FontWeight.Bold);val status=queue?.status?:"NOT_STARTED";val primary=when(status){"NOT_STARTED"->"START";"ACTIVE"->"PAUSE";"PAUSED"->"RESUME";else->null};if(primary!=null)PrimaryAction(primary.replace('_',' '),{viewModel.sessionCommand(session.id,status,queue?.lastCalledToken?:0,primary)},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions);PrimaryAction("Call next",{viewModel.sessionCommand(session.id,status,queue?.lastCalledToken?:0,"CALL_NEXT")},enabled=!state.loading&&status=="ACTIVE"&&"MANAGE_QUEUE" in snapshot.permissions)}}
    item{Text("Session appointments",style=MaterialTheme.typography.titleLarge)}
    if(appointments.isEmpty())item{Text("No hosted appointments in this session.",color=MaterialTheme.colorScheme.onSurfaceVariant)} else items(appointments,key={it.id}){appointment->val entry=queue?.entries?.firstOrNull{it.appointmentId==appointment.id};ElevatedSection("Token ${appointment.token} • ${appointment.patientName}","Status: ${entry?.status?:appointment.status}"){Text("Clinic fee: ${entry?.feeStatus?:appointment.feeStatus} | Receipt: ${entry?.receipt?.ifBlank{"Pending"}?:appointment.receipt.ifBlank{"Pending"}}",color=MaterialTheme.colorScheme.onSurfaceVariant);if(entry==null&&appointment.feeStatus=="PENDING")PrimaryAction("Confirm clinic fee and admit",{viewModel.admit(session.id,appointment.id)},enabled=!state.loading&&"CONFIRM_CLINIC_FEE" in snapshot.permissions);entry?.let{q->when(q.status){"IN_CONSULTATION"->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({viewModel.appointmentCommand(session.id,q.appointmentId,q.status,"COMPLETE")},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions){Text("Complete")};OutlinedButton({viewModel.appointmentCommand(session.id,q.appointmentId,q.status,"SKIP")},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions){Text("Skip")}};"SKIPPED"->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({viewModel.appointmentCommand(session.id,q.appointmentId,q.status,"RESUME_WAITING")},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions){Text("Resume waiting")};OutlinedButton({viewModel.appointmentCommand(session.id,q.appointmentId,q.status,"ABSENT")},enabled=!state.loading&&"MANAGE_QUEUE" in snapshot.permissions){Text("Absent")}};else->Unit}}}}
   }
  }
 }
}
@Composable private fun HostedAssistantAccessCard(assistant:HostedAssistant,loading:Boolean,onSave:(HostedAssistant,Boolean,Set<String>)->Unit){
 var active by remember(assistant.id,assistant.active){mutableStateOf(assistant.active)}
 var queue by remember(assistant.id,assistant.permissions){mutableStateOf("MANAGE_QUEUE" in assistant.permissions)}
 var fee by remember(assistant.id,assistant.permissions){mutableStateOf("CONFIRM_CLINIC_FEE" in assistant.permissions)}
 ElevatedSection(assistant.displayName,assistant.phone){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Account active",fontWeight=FontWeight.Bold);Switch(active,{active=it})}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Manage queue");Switch(queue,{queue=it})}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Confirm clinic fee");Switch(fee,{fee=it})}
  Text("Changes are enforced by the hosted API. Disabling access blocks the Assistant's hosted session without changing local app data.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  PrimaryAction("Save hosted access",{onSave(assistant,active,buildSet{if(queue)add("MANAGE_QUEUE");if(fee)add("CONFIRM_CLINIC_FEE")})},enabled=!loading)
 }
}
